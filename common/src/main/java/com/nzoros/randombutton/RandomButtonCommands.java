package com.nzoros.randombutton;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nzoros.randombutton.event.RandomEvent;
import com.nzoros.randombutton.event.RandomEventRegistry;
import com.nzoros.randombutton.lobby.LobbyReturnManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class RandomButtonCommands {
    private RandomButtonCommands() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("randombutton")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("test").executes(context -> {
                boolean enabled = RandomButtonBlock.toggleTestMode(context.getSource().getServer());
                context.getSource().sendSuccess(() -> Component.literal(
                    "Random Button test mode: " + (enabled ? "ON (4 ticks)" : "OFF (30 ticks)")), false);
                return 1;
            }))
            .then(Commands.literal("random").executes(context -> press(context, null)))
            .then(Commands.literal("event").then(Commands.argument("event", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                    RandomEventRegistry.events().stream().map(RandomEvent::id), builder))
                .executes(context -> press(context, StringArgumentType.getString(context, "event")))))
            .then(Commands.literal("stats").executes(context -> {
                CommandSourceStack source = context.getSource();
                source.sendSuccess(() -> Component.literal("Random Button Statistics"), false);
                source.sendSuccess(() -> Component.literal("Total random activations: "
                    + RandomEventRegistry.totalRandomActivations()), false);
                for (RandomEvent event : RandomEventRegistry.events()) {
                    int count = RandomEventRegistry.randomCount(event.id());
                    if (count > 0) source.sendSuccess(() -> Component.literal(event.id() + ": " + count), false);
                }
                return 1;
            }))
            .then(Commands.literal("list").executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal("Random Button Events:"), false);
                for (RandomEvent event : RandomEventRegistry.events()) {
                    context.getSource().sendSuccess(() -> Component.literal(event.id()), false);
                }
                return 1;
            })));
    }

    private static int press(CommandContext<CommandSourceStack> context, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        BlockPos pos = LobbyReturnManager.buttonPosition(player);
        if (pos == null) {
            source.sendFailure(Component.literal("Use this command in the lobby with its Random Button available."));
            return 0;
        }
        if (id != null) {
            RandomEvent event = RandomEventRegistry.find(id);
            if (event == null) {
                source.sendFailure(Component.literal("Unknown event: " + id + ". Use /randombutton list."));
                return 0;
            }
            if (!event.canRun(player.serverLevel(), player, pos)) {
                source.sendFailure(Component.literal("This event cannot run here: " + id));
                return 0;
            }
        }
        RandomButtonBlock button = (RandomButtonBlock) player.serverLevel().getBlockState(pos).getBlock();
        if (!button.press(player.serverLevel(), player, pos, id)) {
            source.sendFailure(Component.literal("Random Button is cooling down."));
            return 0;
        }
        return 1;
    }
}
