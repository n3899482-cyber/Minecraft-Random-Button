package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.EventContext;
import com.nzoros.randombutton.event.EventDisposition;
import com.nzoros.randombutton.event.RandomEvent;
import com.nzoros.randombutton.event.structure.AnvilTrapScenario;
import com.nzoros.randombutton.event.structure.StructureScenario;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public final class RandomStructureEvent implements RandomEvent {
    private static final List<StructureScenario> SCENARIOS = List.of(new AnvilTrapScenario());

    @Override
    public String name() {
        return "Random Structure";
    }

    @Override
    public EventDisposition disposition() {
        return EventDisposition.CHAOTIC;
    }

    @Override
    public void execute(ServerLevel level, ServerPlayer player, BlockPos origin) {
        BlockPos destination = findDestination(level, origin);
        if (destination == null) return;
        StructureScenario scenario = SCENARIOS.get(level.random.nextInt(SCENARIOS.size()));
        scenario.start(new EventContext(level, player, origin), destination);
    }

    private static BlockPos findDestination(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            int distance = 50 + level.random.nextInt(251);
            int centerX = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int centerZ = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int minimum = Integer.MAX_VALUE;
            int maximum = Integer.MIN_VALUE;
            boolean safe = true;
            for (int x = -3; x <= 3 && safe; x++) {
                for (int z = -3; z <= 3; z++) {
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX + x, centerZ + z);
                    BlockPos support = new BlockPos(centerX + x, y - 1, centerZ + z);
                    if (!level.getFluidState(support).isEmpty()
                        || level.getBlockState(support).is(BlockTags.LOGS)
                        || level.getBlockState(support).is(BlockTags.LEAVES)
                        || !level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
                        safe = false;
                        break;
                    }
                    minimum = Math.min(minimum, y);
                    maximum = Math.max(maximum, y);
                }
            }
            if (safe && maximum - minimum <= 3) return new BlockPos(centerX, maximum, centerZ);
        }
        return null;
    }
}
