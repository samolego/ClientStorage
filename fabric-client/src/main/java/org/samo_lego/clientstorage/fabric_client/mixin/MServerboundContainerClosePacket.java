package org.samo_lego.clientstorage.fabric_client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.event.EventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundContainerClosePacket.class)
public class MServerboundContainerClosePacket {

    @Inject(method = "<init>(I)V", at = @At("TAIL"))
    private void onWrite(CallbackInfo ci) {
        // Reset "accessing item" status
        ((ICSPlayer) Minecraft.getInstance().player).cs_setAccessingItem(false);
        // Save inventory
        EventHandler.onInventoryClose();
    }
}
