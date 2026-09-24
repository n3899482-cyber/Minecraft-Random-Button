package com.nzoros.randombutton;

import com.nzoros.randombutton.event.RandomEventRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;

public final class RandomButtonBlock extends Block {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    private static final int PRESSED_TICKS = 8;
    private static final int COOLDOWN_TICKS = 30;
    private static final int TEST_COOLDOWN_TICKS = 4;
    private static final int TEST_PRESSED_TICKS = 2;
    private static final Map<MinecraftServer, Boolean> TEST_MODES = new WeakHashMap<>();
    private static final VoxelShape IDLE_SHAPE = Shapes.or(
        box(1, 0, 1, 15, 4, 15), box(2, 4, 2, 14, 6, 14), box(4, 6, 4, 12, 10, 12)
    );
    private static final VoxelShape PRESSED_SHAPE = Shapes.or(
        box(1, 0, 1, 15, 4, 15), box(2, 4, 2, 14, 6, 14), box(4, 6, 4, 12, 8, 12)
    );
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();

    public RandomButtonBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVATED, false));
    }

    public static boolean toggleTestMode(MinecraftServer server) {
        boolean enabled = !TEST_MODES.getOrDefault(server, false);
        TEST_MODES.put(server, enabled);
        return enabled;
    }

    public static boolean isTestMode(MinecraftServer server) {
        return TEST_MODES.getOrDefault(server, false);
    }

    public boolean press(ServerLevel level, ServerPlayer player, BlockPos pos, String eventId) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) return false;
        long now = level.getGameTime();
        if (state.getValue(ACTIVATED) || now < playerCooldowns.getOrDefault(player.getUUID(), 0L)) return false;
        if (eventId != null && RandomEventRegistry.find(eventId) == null) return false;

        boolean test = isTestMode(level.getServer());
        playerCooldowns.put(player.getUUID(), now + (test ? TEST_COOLDOWN_TICKS : COOLDOWN_TICKS));
        level.setBlock(pos, state.setValue(ACTIVATED, true), Block.UPDATE_ALL);
        level.scheduleTick(pos, this, test ? TEST_PRESSED_TICKS : PRESSED_TICKS);
        level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.8F, 0.8F);
        if (eventId == null) RandomEventRegistry.triggerRandom(level, player, pos);
        else RandomEventRegistry.triggerNamed(level, player, pos, eventId);
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit
    ) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        return press(serverLevel, serverPlayer, pos, null) ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVATED)) return;
        level.setBlock(pos, state.setValue(ACTIVATED, false), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.6F, 0.9F);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(ACTIVATED) ? PRESSED_SHAPE : IDLE_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
    }
}
