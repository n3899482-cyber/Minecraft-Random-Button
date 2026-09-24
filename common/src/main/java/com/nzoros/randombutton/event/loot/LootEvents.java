package com.nzoros.randombutton.event.loot;

import com.nzoros.randombutton.event.ConfiguredRandomEvent;
import com.nzoros.randombutton.event.EventContext;
import com.nzoros.randombutton.event.EventDisposition;
import com.nzoros.randombutton.event.RandomEventRegistry;
import com.nzoros.randombutton.event.runtime.ActiveEvent;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LootEvents {
    private record Entry(Item item, int min, int max, int weight) { }
    private record Theme(String name, int minTypes, int maxTypes,
                         int color, boolean shower, List<Entry> pool) { }

    private static final Theme BUILDING = theme("Building Explosion",  3, 7, 0xC7AD87, false,
        basic(16, 32, Items.STONE, Items.COBBLESTONE, Items.DEEPSLATE, Items.STONE_BRICKS,
            Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS,
            Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS, Items.MANGROVE_PLANKS, Items.CHERRY_PLANKS),
        basic(4, 16, Items.BRICKS, Items.GLASS, Items.GLASS_PANE, Items.QUARTZ_BLOCK,
            Items.TERRACOTTA, Items.WHITE_CONCRETE, Items.WHITE_CONCRETE_POWDER,
            Items.STONE_SLAB, Items.STONE_STAIRS, Items.COBBLESTONE_WALL, Items.OAK_FENCE));
    private static final Item[] WOOL = {Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL,
        Items.LIGHT_BLUE_WOOL, Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL,
        Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL,
        Items.BROWN_WOOL, Items.GREEN_WOOL, Items.RED_WOOL, Items.BLACK_WOOL};
    private static final Item[] CONCRETE = {Items.WHITE_CONCRETE, Items.ORANGE_CONCRETE, Items.MAGENTA_CONCRETE,
        Items.LIGHT_BLUE_CONCRETE, Items.YELLOW_CONCRETE, Items.LIME_CONCRETE, Items.PINK_CONCRETE,
        Items.GRAY_CONCRETE, Items.LIGHT_GRAY_CONCRETE, Items.CYAN_CONCRETE, Items.PURPLE_CONCRETE,
        Items.BLUE_CONCRETE, Items.BROWN_CONCRETE, Items.GREEN_CONCRETE, Items.RED_CONCRETE, Items.BLACK_CONCRETE};
    private static final Item[] POWDER = {Items.WHITE_CONCRETE_POWDER, Items.ORANGE_CONCRETE_POWDER,
        Items.MAGENTA_CONCRETE_POWDER, Items.LIGHT_BLUE_CONCRETE_POWDER, Items.YELLOW_CONCRETE_POWDER,
        Items.LIME_CONCRETE_POWDER, Items.PINK_CONCRETE_POWDER, Items.GRAY_CONCRETE_POWDER,
        Items.LIGHT_GRAY_CONCRETE_POWDER, Items.CYAN_CONCRETE_POWDER, Items.PURPLE_CONCRETE_POWDER,
        Items.BLUE_CONCRETE_POWDER, Items.BROWN_CONCRETE_POWDER, Items.GREEN_CONCRETE_POWDER,
        Items.RED_CONCRETE_POWDER, Items.BLACK_CONCRETE_POWDER};
    private static final Item[] TERRACOTTA = {Items.WHITE_TERRACOTTA, Items.ORANGE_TERRACOTTA,
        Items.MAGENTA_TERRACOTTA, Items.LIGHT_BLUE_TERRACOTTA, Items.YELLOW_TERRACOTTA,
        Items.LIME_TERRACOTTA, Items.PINK_TERRACOTTA, Items.GRAY_TERRACOTTA,
        Items.LIGHT_GRAY_TERRACOTTA, Items.CYAN_TERRACOTTA, Items.PURPLE_TERRACOTTA,
        Items.BLUE_TERRACOTTA, Items.BROWN_TERRACOTTA, Items.GREEN_TERRACOTTA,
        Items.RED_TERRACOTTA, Items.BLACK_TERRACOTTA};
    private static final Theme COLORFUL = theme("Colorful Block Explosion", 
        5, 8, 0xE657C4, false, basic(2, 6, CONCRETE), basic(2, 6, POWDER),
        basic(2, 6, WOOL), basic(2, 6, TERRACOTTA));
    private static final Theme WOOL_EVENT = theme("Wool Explosion", 
        6, 16, 0xF4D9E8, false, basic(1, 4, WOOL));
    private static final Theme FOOD = theme("Food Explosion",  3, 6, 0xE9A74D, false,
        basic(8, 24, Items.BREAD, Items.APPLE, Items.CARROT, Items.POTATO, Items.BAKED_POTATO,
            Items.BEETROOT, Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP,
            Items.CHICKEN, Items.COOKED_CHICKEN, Items.MUTTON, Items.COOKED_MUTTON,
            Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON));
    private static final Theme RESOURCES = theme("Resource Burst", 
        3, 5, 0xD4A73D, false, List.of(
            entry(Items.COAL, 16, 32, 12), entry(Items.IRON_INGOT, 8, 16, 10),
            entry(Items.COPPER_INGOT, 16, 32, 12), entry(Items.GOLD_INGOT, 4, 8, 6),
            entry(Items.REDSTONE, 16, 32, 10), entry(Items.LAPIS_LAZULI, 8, 16, 9),
            entry(Items.QUARTZ, 8, 16, 9), entry(Items.EMERALD, 1, 3, 1),
            entry(Items.DIAMOND, 1, 3, 1)));
    private static final Theme VALUABLE = theme("Valuable Resource Burst", 
        3, 4, 0x43E5DF, false, List.of(entry(Items.IRON_INGOT, 12, 20, 10),
            entry(Items.GOLD_INGOT, 6, 10, 8), entry(Items.EMERALD, 1, 3, 4),
            entry(Items.DIAMOND, 1, 2, 3)));
    private static final Theme SHOWER = theme("Block Shower", 
        8, 12, 0xB280EA, true, basic(2, 5, CONCRETE), basic(2, 5, WOOL),
        basic(2, 5, TERRACOTTA), basic(2, 6, Items.QUARTZ_BLOCK, Items.BRICKS,
            Items.GLASS, Items.OAK_PLANKS, Items.STONE_BRICKS));
    private static final Theme EQUIPMENT = theme("Equipment Burst", 
        1, 4, 0xA6B9C4, false, equipmentPool());
    private static final Theme ARMOR = theme("Armor Burst", 
        1, 4, 0xD9B66D, false, armorPool());
    private static final Theme WEAPONS = theme("Weapon & Tool Burst", 
        1, 4, 0xADCAD7, false, weaponPool());

    private LootEvents() { }

    public static void register() {
        for (Theme theme : List.of(BUILDING, COLORFUL, WOOL_EVENT, FOOD, RESOURCES, VALUABLE,
            SHOWER, EQUIPMENT, ARMOR, WEAPONS)) {
            RandomEventRegistry.register(new ConfiguredRandomEvent(theme.name, EventDisposition.BENEFICIAL, context -> launch(context, theme)));
        }
        RandomEventRegistry.register(new ConfiguredRandomEvent("Potion Burst", EventDisposition.CHAOTIC,
            LootEvents::potions));
    }

    private static void launch(EventContext context, Theme theme) {
        List<Entry> available = new ArrayList<>(theme.pool);
        List<ItemStack> drops = new ArrayList<>();
        int count = between(context.level(), theme.minTypes, theme.maxTypes);
        for (int i = 0; i < count && !available.isEmpty(); i++) {
            Entry selected = pick(context.level(), available);
            available.remove(selected);
            drops.add(new ItemStack(selected.item, between(context.level(), selected.min, selected.max)));
        }
        EventRuntime.start(new LootFirework(context, drops, theme.color, theme.shower));
    }

    private static void potions(EventContext context) {
        var available = new ArrayList<>(List.of(Potions.SWIFTNESS, Potions.STRENGTH, Potions.REGENERATION,
            Potions.FIRE_RESISTANCE, Potions.NIGHT_VISION, Potions.WATER_BREATHING, Potions.LEAPING,
            Potions.POISON, Potions.SLOWNESS, Potions.WEAKNESS));
        List<ItemStack> drops = new ArrayList<>();
        int count = between(context.level(), 3, 6);
        for (int i = 0; i < count; i++) {
            var potion = available.remove(context.level().random.nextInt(available.size()));
            drops.add(PotionContents.createItemStack(Items.POTION, potion));
        }
        EventRuntime.start(new LootFirework(context, drops, 0xC866E8, false));
    }

    private static Entry pick(ServerLevel level, List<Entry> entries) {
        int total = entries.stream().mapToInt(Entry::weight).sum();
        int roll = level.random.nextInt(total);
        for (Entry entry : entries) {
            roll -= entry.weight;
            if (roll < 0) return entry;
        }
        return entries.getLast();
    }

    private static int between(ServerLevel level, int min, int max) {
        return min + level.random.nextInt(max - min + 1);
    }

    private static Entry entry(Item item, int min, int max, int weight) {
        return new Entry(item, min, max, weight);
    }

    private static List<Entry> basic(int min, int max, Item... items) {
        List<Entry> result = new ArrayList<>();
        for (Item item : items) result.add(entry(item, min, max, 1));
        return result;
    }

    @SafeVarargs
    private static Theme theme(String name, int min, int max,
                               int color, boolean shower, List<Entry>... groups) {
        List<Entry> pool = new ArrayList<>();
        for (List<Entry> group : groups) pool.addAll(group);
        return new Theme(name, min, max, color, shower, List.copyOf(pool));
    }

    private static List<Entry> armorPool() {
        List<Entry> pool = new ArrayList<>();
        addGear(pool, 9, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        addGear(pool, 7, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        addGear(pool, 8, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        addGear(pool, 6, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        addGear(pool, 2, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        addGear(pool, 1, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
        return pool;
    }

    private static List<Entry> weaponPool() {
        List<Entry> pool = new ArrayList<>();
        addGear(pool, 10, Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_PICKAXE,
            Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.STONE_SWORD, Items.STONE_AXE,
            Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_HOE);
        addGear(pool, 8, Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_PICKAXE,
            Items.IRON_SHOVEL, Items.IRON_HOE, Items.BOW, Items.CROSSBOW, Items.SHIELD);
        addGear(pool, 6, Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE,
            Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE);
        addGear(pool, 2, Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE,
            Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.TRIDENT);
        addGear(pool, 1, Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE,
            Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE);
        return pool;
    }

    private static List<Entry> equipmentPool() {
        List<Entry> pool = armorPool();
        pool.addAll(weaponPool());
        return pool;
    }

    private static void addGear(List<Entry> pool, int weight, Item... items) {
        for (Item item : items) pool.add(entry(item, 1, 1, weight));
    }

    private static final class LootFirework implements ActiveEvent {
        private final UUID playerId;
        private final ServerLevel level;
        private final BlockPos origin;
        private final List<ItemStack> drops;
        private final int color;
        private final boolean shower;
        private int ticks;

        private LootFirework(EventContext context, List<ItemStack> drops, int color, boolean shower) {
            this.playerId = context.player().getUUID();
            this.level = context.level();
            this.origin = context.buttonPos();
            this.drops = drops;
            this.color = color;
            this.shower = shower;
            level.playSound(null, origin, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 0.8F, 1.1F);
        }

        @Override
        public UUID playerId() { return playerId; }

        @Override
        public boolean tick(MinecraftServer server) {
            ticks++;
            double x = origin.getX() + 0.5;
            double y = origin.getY() + 1.0 + Math.min(ticks, 16) * 0.2;
            double z = origin.getZ() + 0.5;
            if (ticks < 17) {
                level.sendParticles(new DustParticleOptions(new Vector3f(
                    ((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F,
                    (color & 255) / 255.0F), 1.3F), x, y, z, 5, 0.12, 0.12, 0.12, 0.02);
                return false;
            }
            level.sendParticles(ParticleTypes.FIREWORK, x, y, z, 75, 0.8, 0.8, 0.8, 0.12);
            level.playSound(null, origin, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 0.9F, 1.0F);
            for (ItemStack stack : drops) {
                double dx = (level.random.nextDouble() - 0.5) * 0.7;
                double dz = (level.random.nextDouble() - 0.5) * 0.7;
                ItemEntity item = new ItemEntity(level, x + dx, origin.getY() + (shower ? 3.5 : 1.4), z + dz, stack);
                item.setDeltaMovement((level.random.nextDouble() - 0.5) * 0.26,
                    shower ? 0.02 : 0.18 + level.random.nextDouble() * 0.08,
                    (level.random.nextDouble() - 0.5) * 0.26);
                level.addFreshEntity(item);
            }
            return true;
        }
    }
}
