package org.samo_lego.clientstorage.fabric_client.mixin.network;

import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.samo_lego.clientstorage.fabric_client.event.ContainerDiscovery;
import org.samo_lego.clientstorage.fabric_client.event.SimpleEventHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundContainerClosePacket.class)
public class MServerboundContainerClosePacket {

    @Shadow
    @Final
    private int containerId;

    @Inject(method = "<init>(I)V", at = @At("TAIL"))
    private void onWrite(CallbackInfo ci) {
        if (!ContainerDiscovery.fakePacketsActive() && this.containerId != 0) {
            // Save inventory
            SimpleEventHandler.onInventoryClose();
        }
    }
}
