package com.nzoros.randombutton;

import com.nzoros.randombutton.event.runtime.CubeAnimation;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CubeItem extends Item {
    private final CubeState.Mode mode;

    public CubeItem(CubeState.Mode mode, Properties properties) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            CubeState.add(serverPlayer, mode);
            EventRuntime.startVisual(new CubeAnimation(serverPlayer, new ItemStack(this), mode));
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.getCooldowns().addCooldown(this, 10);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
