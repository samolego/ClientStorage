package org.samo_lego.clientstorage.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import org.samo_lego.clientstorage.event.EventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.sounds.SoundSource.BLOCKS;
import static org.samo_lego.clientstorage.event.EventHandler.fakePacketsActive;
import static org.samo_lego.clientstorage.network.RemoteStackPacket.isAccessingItem;

@Mixin(ClientPacketListener.class)
public class MClientPlayNetworkHandler {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void onPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundContainerClickPacket paket) {
            System.out.println(paket.getSlotNum());
        }
        if (packet instanceof ServerboundUseItemOnPacket paket) {
            System.out.println("C2S interact " + paket.getHitResult().getBlockPos());
        }
    }

    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    private void handleBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        System.out.println("S2C block update: (fake=" + fakePacketsActive() + ") -> " + packet.getPos());
    }


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
        System.out.println("S2C open screen: (fake=" + fakePacketsActive() + ") -> " + packet.getTitle().getString());
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
}
