package com.nzoros.randombutton;

import com.nzoros.randombutton.lobby.LobbyReturnManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LobbyReturnItem extends Item {
    public LobbyReturnItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && LobbyReturnManager.returnToLobby(serverPlayer)) {
            player.getCooldowns().addCooldown(this, 20 * 5);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
