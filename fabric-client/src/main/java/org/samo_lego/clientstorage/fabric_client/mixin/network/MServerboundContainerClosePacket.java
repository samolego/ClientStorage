package org.samo_lego.clientstorage.fabric_client.mixin.network;

import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.samo_lego.clientstorage.fabric_client.event.EventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundContainerClosePacket.class)
public class MServerboundContainerClosePacket {

    @Inject(method = "<init>(I)V", at = @At("TAIL"))
    private void onWrite(CallbackInfo ci) {
        // Save inventory
        EventHandler.onInventoryClose();
    }
}
