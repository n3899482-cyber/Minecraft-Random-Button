package com.nzoros.randombutton.mixin;

import com.nzoros.randombutton.lobby.LobbyReturnManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class RecallMenuMixin {
    @Inject(method = "removed(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"))
    private void randombutton$returnCursorSigilOnClose(Player player, CallbackInfo callback) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (player.level().isClientSide() || !LobbyReturnManager.isRecallItem(menu.getCarried())) return;
        ItemStack sigil = menu.getCarried();
        menu.setCarried(ItemStack.EMPTY);
        if (player.getInventory().add(sigil)) return;
        int selected = player.getInventory().selected;
        ItemStack displaced = player.getInventory().getItem(selected);
        player.getInventory().setItem(selected, sigil);
        if (!displaced.isEmpty()) player.drop(displaced, false);
    }

    @Inject(method = "clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V",
        at = @At("HEAD"), cancellable = true)
    private void randombutton$keepSigilInInventory(int slotId, int button, ClickType clickType,
                                                    Player player, CallbackInfo callback) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        Slot slot = slotId >= 0 && slotId < menu.slots.size() ? menu.slots.get(slotId) : null;
        boolean carried = LobbyReturnManager.isRecallItem(menu.getCarried());
        boolean inSlot = slot != null && LobbyReturnManager.isRecallItem(slot.getItem());

        if ((clickType == ClickType.THROW && inSlot)
            || (clickType == ClickType.PICKUP && slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && carried)
            || (clickType == ClickType.QUICK_MOVE && inSlot && menu != player.inventoryMenu
                && slot.container == player.getInventory())
            || (clickType == ClickType.PICKUP && carried && slot != null
                && slot.container != player.getInventory())
            || (clickType == ClickType.SWAP && slot != null
                && slot.container != player.getInventory()
                && button >= 0 && button < player.getInventory().getContainerSize()
                && LobbyReturnManager.isRecallItem(player.getInventory().getItem(button)))
            || (clickType == ClickType.QUICK_CRAFT && carried)) {
            callback.cancel();
        }
    }
}
