package org.samo_lego.clientstorage.mixin.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import org.samo_lego.clientstorage.mixin.SlotAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    private final CraftingScreenHandler screenHandler = (CraftingScreenHandler) (Object) this;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("RETURN"))
    private void constructor(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        screenHandler.slots.forEach(slot -> {
            ((SlotAccessor) slot).setY(slot.y + 36);
        });
    }
}
