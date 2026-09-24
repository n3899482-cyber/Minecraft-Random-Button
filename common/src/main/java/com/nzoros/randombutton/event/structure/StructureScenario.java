package com.nzoros.randombutton.event.structure;

import com.nzoros.randombutton.event.EventContext;
import net.minecraft.core.BlockPos;

public interface StructureScenario {
    void start(EventContext context, BlockPos center);
}
