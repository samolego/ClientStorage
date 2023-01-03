package org.samo_lego.clientstorage.fabric_client.mixin.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.event.ContainerDiscovery;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundInteractPacket.class)
public class MServerBoundInteractPacket {

    @Shadow
    @Final
    private int entityId;

    /**
     * Saves the last interacted container.
     */
    @Inject(method = "write", at = @At("TAIL"))
    private void onWrite(FriendlyByteBuf buf, CallbackInfo ci) {
        final var player = (ICSPlayer) Minecraft.getInstance().player;

        final var entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity instanceof InteractableContainer container && !ContainerDiscovery.fakePacketsActive()) {
            player.cs_setLastInteractedContainer(container);
        } else {
            player.cs_setLastInteractedContainer(null);
        }
    }
}
