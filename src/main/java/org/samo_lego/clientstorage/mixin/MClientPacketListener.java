package org.samo_lego.clientstorage.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.clientstorage.event.EventHandler;
import org.samo_lego.clientstorage.util.PacketLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.minecraft.sounds.SoundSource.BLOCKS;
import static org.samo_lego.clientstorage.event.EventHandler.fakePacketsActive;
import static org.samo_lego.clientstorage.network.RemoteStackPacket.isAccessingItem;

@Mixin(ClientPacketListener.class)
public class MClientPacketListener {

    @Inject(
            method = "handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;getContainerId()I"
            ),
            cancellable = true
    )
    private void onInventoryPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (isAccessingItem()) {
            ci.cancel();
            return;
        }
        EventHandler.onInventoryPacket(packet, ci);
    }

    @Inject(method = "handleOpenScreen",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/MenuScreens;create(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/Minecraft;ILnet/minecraft/network/chat/Component;)V"),
            cancellable = true)
    private void onOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (isAccessingItem() || fakePacketsActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSoundEvent",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void onSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
        // Cancel sounds if item search is active
        if (packet.getSource().equals(BLOCKS) && (isAccessingItem() || fakePacketsActive())) {
            ci.cancel();
        }
    }


    @Inject(method = "handleCustomPayload",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;setServerBrand(Ljava/lang/String;)V",
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci, ResourceLocation channel, FriendlyByteBuf byteBuf) {
        PacketLimiter.tryRecognizeServer();
    }

    @Inject(method = "handleBlockUpdate", at = @At("HEAD"))
    private void onBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        // Try detect server type from packet interactions
        PacketLimiter.detectServerType(packet);
    }
}
