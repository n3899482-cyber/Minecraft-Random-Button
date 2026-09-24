package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.ConfiguredRandomEvent;
import com.nzoros.randombutton.event.EventContext;
import com.nzoros.randombutton.event.EventDisposition;
import com.nzoros.randombutton.event.RandomEventRegistry;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class ZombieVarietyEvents {
    private static final List<EntityType<? extends Zombie>> TYPES = List.of(
        EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER);

    private ZombieVarietyEvents() { }

    public static void register() {
        RandomEventRegistry.register(new ConfiguredRandomEvent("Baby Zombie Rush", EventDisposition.HARMFUL, c -> spawn(c, EntityType.ZOMBIE, 3, 5, true, false)));
        RandomEventRegistry.register(new ConfiguredRandomEvent("Armored Zombies", EventDisposition.HARMFUL, c -> spawn(c, EntityType.ZOMBIE, 2, 3, false, true)));
        RandomEventRegistry.register(new ConfiguredRandomEvent("Undead Variety", EventDisposition.HARMFUL, ZombieVarietyEvents::mixedGroup));
    }

    private static void mixedGroup(EventContext context) {
        int count = 3 + context.level().random.nextInt(3);
        for (int i = 0; i < count; i++) {
            create(context, TYPES.get(context.level().random.nextInt(TYPES.size())), false, false);
        }
    }

    private static void spawn(EventContext context, EntityType<? extends Zombie> type,
                              int min, int max, boolean baby, boolean armored) {
        int count = min + context.level().random.nextInt(max - min + 1);
        for (int i = 0; i < count; i++) create(context, type, baby, armored);
    }

    private static void create(EventContext context, EntityType<? extends Zombie> type,
                               boolean baby, boolean armored) {
        Zombie zombie = type.create(context.level());
        if (zombie == null) return;
        BlockPos spawn = context.randomGroundOutsideLobby();
        zombie.moveTo(spawn, context.level().random.nextFloat() * 360.0F, 0.0F);
        zombie.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(spawn),
            MobSpawnType.EVENT, null);
        zombie.setBaby(baby);
        if (armored) {
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            if (context.level().random.nextBoolean()) {
                zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }
        zombie.setTarget(context.player());
        zombie.setPersistenceRequired();
        context.level().addFreshEntity(zombie);
        EventRuntime.trackTemporary(zombie, 20 * 90);
    }
}
