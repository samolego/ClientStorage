package org.samo_lego.clientstorage.mixin.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import org.samo_lego.clientstorage.inventory.RemoteInventory;
import org.samo_lego.clientstorage.inventory.RemoteSlot;
import org.samo_lego.clientstorage.mixin.accessor.AScreenHandler;
import org.samo_lego.clientstorage.mixin.accessor.ASlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.clientstorage.ClientStorage.config;

@Mixin(CraftingMenu.class)
public class MCraftingScreenHandler {

    private final CraftingMenu self = (CraftingMenu) (Object) this;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void constructor(int syncId, Inventory playerInventory, ContainerLevelAccess context, CallbackInfo ci) {
        if (!config.enabled) return;

        // Moving slots down
        self.slots.forEach(slot -> ((ASlot) slot).setY(slot.y + 36));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                ((AScreenHandler) self).cs_addSlot(new RemoteSlot(RemoteInventory.getInstance(), col + row * 9, col * 18 + 9, row * 18 - 23));
            }
        }
    }
}
