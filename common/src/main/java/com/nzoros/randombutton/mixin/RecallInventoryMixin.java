package com.nzoros.randombutton.mixin;

import com.nzoros.randombutton.lobby.LobbyReturnManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class RecallInventoryMixin {
    @Inject(method = "removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD"), cancellable = true)
    private void randombutton$keepSelectedSigil(boolean entireStack,
                                                CallbackInfoReturnable<ItemStack> callback) {
        Inventory inventory = (Inventory) (Object) this;
        if (LobbyReturnManager.isRecallItem(inventory.getSelected())) {
            callback.setReturnValue(ItemStack.EMPTY);
        }
    }
}
