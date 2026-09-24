package com.nzoros.randombutton;

import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public final class CubeState {
    private static final String LUCKY = "randombutton.cube_lucky_";
    private static final String UNLUCKY = "randombutton.cube_unlucky_";
    private static final String ACTIVATIONS = "randombutton.cube_activations_";
    private static final String MODE = "randombutton.cube_mode_";

    public enum Mode { NONE, LUCKY, UNLUCKY }

    private CubeState() { }

    public static int lucky(ServerPlayer player) { return read(player, LUCKY); }

    public static int unlucky(ServerPlayer player) { return read(player, UNLUCKY); }

    public static int activations(ServerPlayer player) { return read(player, ACTIVATIONS); }

    public static Mode mode(ServerPlayer player) {
        if (player.getTags().contains(MODE + "lucky") && lucky(player) > 0) return Mode.LUCKY;
        if (player.getTags().contains(MODE + "unlucky") && unlucky(player) > 0) return Mode.UNLUCKY;
        return Mode.NONE;
    }

    public static void add(ServerPlayer player, Mode mode) {
        if (mode == Mode.NONE) return;
        String key = mode == Mode.LUCKY ? LUCKY : UNLUCKY;
        set(player, key, Math.min(Integer.MAX_VALUE - 5, read(player, key)) + 5);
        clear(player, MODE);
        player.addTag(MODE + mode.name().toLowerCase(java.util.Locale.ROOT));
        sync(player);
    }

    public static void recordRandomActivation(ServerPlayer player) {
        Mode active = mode(player);
        if (active != Mode.NONE) {
            String key = active == Mode.LUCKY ? LUCKY : UNLUCKY;
            set(player, key, read(player, key) - 1);
            if (read(player, key) == 0) clear(player, MODE);
        }
        set(player, ACTIVATIONS, Math.min(Integer.MAX_VALUE - 1, activations(player)) + 1);
        sync(player);
    }

    public static void copy(ServerPlayer previous, ServerPlayer current) {
        clear(current, LUCKY);
        clear(current, UNLUCKY);
        clear(current, ACTIVATIONS);
        clear(current, MODE);
        for (String tag : previous.getTags()) {
            if (isCubeTag(tag)) current.addTag(tag);
        }
    }

    public static void sync(ServerPlayer player) {
        MyMod.platform.sendCubeState(player, new CubeStatePayload(activations(player), lucky(player),
            unlucky(player), mode(player).ordinal()));
    }

    private static int read(ServerPlayer player, String prefix) {
        for (String tag : player.getTags()) {
            if (!tag.startsWith(prefix)) continue;
            try {
                return Math.max(0, Integer.parseInt(tag.substring(prefix.length())));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static void set(ServerPlayer player, String prefix, int value) {
        clear(player, prefix);
        if (value > 0) player.addTag(prefix + value);
    }

    private static void clear(ServerPlayer player, String prefix) {
        for (String tag : Set.copyOf(player.getTags())) {
            if (tag.startsWith(prefix)) player.removeTag(tag);
        }
    }

    private static boolean isCubeTag(String tag) {
        return tag.startsWith(LUCKY) || tag.startsWith(UNLUCKY)
            || tag.startsWith(ACTIVATIONS) || tag.startsWith(MODE);
    }
}
