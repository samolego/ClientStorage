package org.samo_lego.clientstorage.fabric_client.mixin.network;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class MClientCommonPacketListenerImpl_BrandRecognizer {

    /**
     * Tries to recognize server type from server brand packet.
     *
     * @param packet
     * @param ci
     */
    @Inject(method = "handleCustomPayload",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/telemetry/WorldSessionTelemetryManager;onServerBrandReceived(Ljava/lang/String;)V",
                    shift = At.Shift.BEFORE))
    private void onServerBrand(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        PacketLimiter.tryRecognizeServer();
    }
}
