package com.nzoros.randombutton.event;

import com.nzoros.randombutton.CubeState;
import com.nzoros.randombutton.event.events.*;
import com.nzoros.randombutton.event.loot.LootEvents;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import com.nzoros.randombutton.event.runtime.HomingStarEvent;
import com.nzoros.randombutton.event.runtime.MeteorEvent;
import com.nzoros.randombutton.event.runtime.ParticleShowEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public final class RandomEventRegistry {
    private static final List<RandomEvent> EVENTS = new ArrayList<>();
    private static final Map<String, RandomEvent> BY_ID = new HashMap<>();
    private static final Map<String, Integer> RANDOM_COUNTS = new HashMap<>();
    private static int totalRandomActivations;

    private RandomEventRegistry() { }

    public static void init() {
        if (!EVENTS.isEmpty()) return;
        register(new WeatherEvent());
        register(new RandomEffectEvent());
        EffectVarietyEvents.register();
        ZombieVarietyEvents.register();
        register(new HarmlessMobEvent());
        register(new RandomSoundEvent());
        register(new RandomStructureEvent());
        LootEvents.register();

        register(configured("Experience", EventDisposition.BENEFICIAL,
            c -> c.player().giveExperiencePoints(10 + c.level().random.nextInt(16))));
        register(configured("Heal", EventDisposition.BENEFICIAL, c -> {
            float targetHealth = c.player().getMaxHealth() * 0.6F;
            c.player().heal(Math.max(0.0F, targetHealth - c.player().getHealth()));
        }));
        register(configured("Launch", EventDisposition.HARMFUL,
            RandomEventRegistry::launchPlayer));
        register(configured("Blast Wave", EventDisposition.HARMFUL,
            RandomEventRegistry::blastWave));
        register(configured("Falling TNT", EventDisposition.HARMFUL,
            RandomEventRegistry::spawnFallingTnt));
        register(new ConfiguredRandomEvent("Small Meteor", "meteor", EventDisposition.CHAOTIC, c -> spawnMeteor(c, 1)));
        register(configured("Medium Meteor", EventDisposition.CHAOTIC,
            c -> spawnMeteor(c, 3)));
        register(configured("Large Meteor", EventDisposition.CHAOTIC,
            c -> spawnMeteor(c, 5)));
        register(configured("TNT", EventDisposition.HARMFUL,
            RandomEventRegistry::spawnTnt));
        register(configured("Sheep", EventDisposition.BENEFICIAL,
            RandomEventRegistry::spawnSheepParty));
        register(configured("Bees", EventDisposition.CHAOTIC,
            c -> spawnMobs(c, EntityType.BEE, 4, false)));
        register(configured("Zombies", EventDisposition.HARMFUL,
            c -> spawnMobs(c, EntityType.ZOMBIE, 4, true)));
        register(configured("Skeletons", EventDisposition.HARMFUL,
            c -> spawnMobs(c, EntityType.SKELETON, 2, true)));
        register(configured("Creeper Ambush", EventDisposition.HARMFUL,
            c -> spawnMobs(c, EntityType.CREEPER, 3, true)));
        register(configured("Vindicator Hunt", EventDisposition.HARMFUL,
            c -> spawnMobs(c, EntityType.VINDICATOR, 3, true)));
        register(configured("Ravager Charge", EventDisposition.HARMFUL,
            c -> spawnMobs(c, EntityType.RAVAGER, 1, true)));
        register(configured("Phantom Attack", EventDisposition.HARMFUL,
            RandomEventRegistry::spawnPhantoms));
        register(configured("Witch Curse", EventDisposition.HARMFUL,
            c -> spawnMobs(c, EntityType.WITCH, 2, true)));
        register(configured("Mob Swarm", EventDisposition.HARMFUL,
            RandomEventRegistry::spawnMobSwarm));
        register(configured("Darkness", EventDisposition.HARMFUL,
            RandomEventRegistry::startDarknessAttack));
        register(configured("Mob Roulette", EventDisposition.CHAOTIC,
            RandomEventRegistry::runMobRoulette));
        register(configured("Random Teleport", EventDisposition.CHAOTIC,
            RandomEventRegistry::randomTeleport));
        register(configured("Loot or Death", EventDisposition.CHAOTIC,
            RandomEventRegistry::lootOrDeath));
        register(configured("Red Guardian", EventDisposition.HARMFUL,
            RandomEventRegistry::spawnMiniBoss));
        register(configured("Fireworks", EventDisposition.CHAOTIC,
            c -> EventRuntime.start(new ParticleShowEvent(c.player().getUUID()))));
        register(new ConfiguredRandomEvent("Homing Wind Charge", "homing_wind", EventDisposition.HARMFUL, c -> EventRuntime.start(new HomingStarEvent(c.player()))));
        register(configured("Jackpot", EventDisposition.BENEFICIAL, c -> {
            give(c.player(), Items.DIAMOND, 3);
            give(c.player(), Items.EMERALD, 12);
            c.player().giveExperienceLevels(3);
        }));
    }

    public static void register(RandomEvent event) {
        if (BY_ID.putIfAbsent(event.id(), event) != null) {
            throw new IllegalArgumentException("Duplicate Random Event id: " + event.id());
        }
        EVENTS.add(event);
    }

    public static List<RandomEvent> events() {
        if (EVENTS.isEmpty()) init();
        return Collections.unmodifiableList(EVENTS);
    }

    public static RandomEvent find(String id) {
        if (EVENTS.isEmpty()) init();
        return BY_ID.get(id);
    }

    public static int totalRandomActivations() {
        return totalRandomActivations;
    }

    public static int randomCount(String id) {
        return RANDOM_COUNTS.getOrDefault(id, 0);
    }

    public static void triggerRandom(ServerLevel level, ServerPlayer player, BlockPos origin) {
        if (EVENTS.isEmpty()) init();
        if (EventRuntime.hasActive(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.randombutton.event_in_progress"), true);
            return;
        }

        CubeState.Mode mode = CubeState.mode(player);
        List<RandomEvent> eligible = EVENTS.stream()
            .filter(event -> event.canRun(level, player, origin))
            .filter(event -> mode == CubeState.Mode.NONE
                || event.disposition() == (mode == CubeState.Mode.LUCKY
                    ? EventDisposition.BENEFICIAL : EventDisposition.HARMFUL))
            .toList();
        if (eligible.isEmpty()) return;
        RandomEvent event = eligible.get(level.random.nextInt(eligible.size()));
        execute(level, player, origin, event);
        totalRandomActivations++;
        RANDOM_COUNTS.merge(event.id(), 1, Integer::sum);
        CubeState.recordRandomActivation(player);
    }

    public static boolean triggerNamed(ServerLevel level, ServerPlayer player, BlockPos origin, String id) {
        RandomEvent event = find(id);
        if (event == null || !event.canRun(level, player, origin)) return false;
        if (EventRuntime.hasActive(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.randombutton.event_in_progress"), true);
            return false;
        }
        execute(level, player, origin, event);
        return true;
    }

    private static void execute(ServerLevel level, ServerPlayer player, BlockPos origin, RandomEvent event) {
        event.execute(level, player, origin);
        player.sendSystemMessage(Component.translatable("message.randombutton.event_triggered",
            event.displayName()), true);
    }

    private static ConfiguredRandomEvent configured(
        String name, EventDisposition disposition,
        ConfiguredRandomEvent.EventAction action
    ) {
        return new ConfiguredRandomEvent(name, disposition, action);
    }

    private static void give(ServerPlayer player, Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static void spawnMobs(EventContext context, EntityType<? extends Mob> type, int count, boolean hostile) {
        for (int i = 0; i < count; i++) {
            Mob mob = type.create(context.level());
            if (mob == null) continue;
            BlockPos spawn = context.randomGroundOutsideLobby();
            mob.moveTo(spawn, context.level().random.nextFloat() * 360.0F, 0.0F);
            mob.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(spawn),
                MobSpawnType.EVENT, null);
            mob.setPersistenceRequired();
            if (hostile) mob.setTarget(context.player());
            context.level().addFreshEntity(mob);
            EventRuntime.trackTemporary(mob, hostile ? 20 * 90 : 20 * 180);
        }
    }

    private static void spawnSheepParty(EventContext context) {
        DyeColor[] colors = DyeColor.values();
        for (int i = 0; i < 5; i++) {
            Sheep sheep = EntityType.SHEEP.create(context.level());
            if (sheep == null) continue;
            BlockPos spawn = context.randomGroundOutsideLobby();
            sheep.moveTo(spawn, context.level().random.nextFloat() * 360.0F, 0.0F);
            sheep.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(spawn),
                MobSpawnType.EVENT, null);
            sheep.setColor(colors[context.level().random.nextInt(colors.length)]);
            context.level().addFreshEntity(sheep);
            EventRuntime.trackTemporary(sheep, 20 * 180);
        }
    }

    private static void spawnPhantoms(EventContext context) {
        for (int i = 0; i < 3; i++) {
            var phantom = EntityType.PHANTOM.create(context.level());
            if (phantom == null) continue;
            double angle = Math.PI * 2.0 * i / 3.0;
            phantom.moveTo(context.player().getX() + Math.cos(angle) * 7.0,
                context.player().getY() + 8.0,
                context.player().getZ() + Math.sin(angle) * 7.0);
            phantom.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(phantom.blockPosition()),
                MobSpawnType.EVENT, null);
            phantom.setTarget(context.player());
            phantom.setPersistenceRequired();
            context.level().addFreshEntity(phantom);
            EventRuntime.trackTemporary(phantom, 20 * 90);
        }
    }

    private static void spawnMobSwarm(EventContext context) {
        for (int i = 0; i < 8; i++) {
            EntityType<? extends Mob> type = switch (i % 3) {
                case 0 -> EntityType.ZOMBIE;
                case 1 -> EntityType.SPIDER;
                default -> EntityType.SKELETON;
            };
            spawnMobs(context, type, 1, true);
        }
    }

    private static void startDarknessAttack(EventContext context) {
        context.player().addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 15, 0));
        context.player().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 8, 0));
        spawnMobs(context, EntityType.ZOMBIE, 3, true);
    }

    private static void runMobRoulette(EventContext context) {
        switch (context.level().random.nextInt(7)) {
            case 0 -> spawnMobs(context, EntityType.ZOMBIE, 5, true);
            case 1 -> spawnMobs(context, EntityType.SPIDER, 4, true);
            case 2 -> spawnMobs(context, EntityType.SKELETON, 3, true);
            case 3 -> spawnMobs(context, EntityType.CREEPER, 2, true);
            case 4 -> spawnMobs(context, EntityType.VINDICATOR, 2, true);
            case 5 -> spawnMobs(context, EntityType.WITCH, 1, true);
            default -> spawnMobs(context, EntityType.RAVAGER, 1, true);
        }
    }

    private static void randomTeleport(EventContext context) {
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = context.level().random.nextDouble() * Math.PI * 2.0;
            int distance = 20 + context.level().random.nextInt(21);
            int x = context.player().blockPosition().getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = context.player().blockPosition().getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = context.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, y, z);
            if (!context.level().getFluidState(feet.below()).isEmpty()
                || !context.level().getBlockState(feet).isAir()
                || !context.level().getBlockState(feet.above()).isAir()) {
                continue;
            }
            context.player().teleportTo(context.level(), x + 0.5, y, z + 0.5,
                context.player().getYRot(), context.player().getXRot());
            return;
        }
    }

    private static void lootOrDeath(EventContext context) {
        give(context.player(), Items.GOLDEN_APPLE, 1);
        spawnMobs(context, EntityType.VINDICATOR, 3, true);
    }

    private static void spawnTnt(EventContext context) {
        for (int i = 0; i < 6; i++) {
            var tnt = EntityType.TNT.create(context.level());
            if (tnt == null) continue;
            double angle = Math.PI * 2.0 * i / 6.0;
            tnt.moveTo(context.player().getX() + Math.cos(angle) * 2.0,
                context.player().getY() + 4.0 + (i % 2),
                context.player().getZ() + Math.sin(angle) * 2.0);
            tnt.setFuse(35 + i * 2);
            context.level().addFreshEntity(tnt);
        }
    }

    private static void spawnFallingTnt(EventContext context) {
        var tnt = EntityType.TNT.create(context.level());
        if (tnt == null) return;
        tnt.moveTo(context.player().getX(), context.player().getY() + 5.0,
            context.player().getZ());
        tnt.setFuse(45);
        context.level().addFreshEntity(tnt);
        context.level().playSound(null, context.player().blockPosition(),
            SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void spawnMeteor(EventContext context, int diameter) {
        EventRuntime.start(new MeteorEvent(context.player().getUUID(), context.level().dimension(),
            context.randomGroundOutsideLobby(), diameter));
    }

    private static void blastWave(EventContext context) {
        double dx = context.player().getX() - (context.buttonPos().getX() + 0.5);
        double dz = context.player().getZ() - (context.buttonPos().getZ() + 0.5);
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 0.1) {
            double angle = context.level().random.nextDouble() * Math.PI * 2.0;
            dx = Math.cos(angle);
            dz = Math.sin(angle);
            length = 1.0;
        }

        context.player().hurt(context.player().damageSources().explosion(null, null), 6.0F);
        context.player().push(dx / length * 2.4, 1.15, dz / length * 2.4);
        context.player().hurtMarked = true;
        context.level().playSound(null, context.player().blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
            SoundSource.BLOCKS, 1.2F, 0.85F);
        context.level().sendParticles(ParticleTypes.EXPLOSION, context.player().getX(),
            context.player().getY() + 0.8, context.player().getZ(), 12, 1.0, 0.7, 1.0, 0.05);
    }

    private static void launchPlayer(EventContext context) {
        var movement = context.player().getDeltaMovement();
        context.player().setDeltaMovement(movement.x, 1.8, movement.z);
        context.player().hasImpulse = true;
        context.player().hurtMarked = true;
        context.level().playSound(null, context.player().blockPosition(),
            SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 0.9F);
        context.level().sendParticles(ParticleTypes.CLOUD, context.player().getX(),
            context.player().getY(), context.player().getZ(), 18, 0.35, 0.1, 0.35, 0.08);
    }

    private static void spawnMiniBoss(EventContext context) {
        var boss = EntityType.ZOMBIE.create(context.level());
        if (boss == null) return;
        BlockPos spawn = context.randomGroundOutsideLobby();
        boss.moveTo(spawn, context.level().random.nextFloat() * 360.0F, 0.0F);
        boss.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(spawn),
            MobSpawnType.EVENT, null);
        boss.setCustomName(Component.literal("The Red Guardian"));
        boss.setCustomNameVisible(true);
        boss.setPersistenceRequired();
        boss.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60 * 5, 1));
        boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60 * 5, 1));
        boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60 * 5, 0));
        boss.setTarget(context.player());
        context.level().addFreshEntity(boss);
        EventRuntime.trackTemporary(boss, 20 * 300);
    }
}
