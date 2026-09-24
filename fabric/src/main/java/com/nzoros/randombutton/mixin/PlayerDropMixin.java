package com.nzoros.randombutton.mixin;

import com.nzoros.randombutton.lobby.LobbyReturnManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDropMixin {
    @Inject(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void randombutton$protectRecallSigil(
        ItemStack stack, boolean throwRandomly, boolean retainOwnership,
        CallbackInfoReturnable<ItemEntity> callback
    ) {
        if (!LobbyReturnManager.isRecallItem(stack)) return;
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide() && player.isAlive()
            && !LobbyReturnManager.hasRecallItem(player)) {
            player.getInventory().add(stack);
        }
        callback.setReturnValue(null);
    }
}
