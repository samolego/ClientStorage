package org.samo_lego.clientstorage.fabric_client.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.inventory.MenuType;
import org.samo_lego.clientstorage.fabric_client.event.EventHandler;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.sounds.SoundSource.BLOCKS;
import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.fakePacketsActive;
import static org.samo_lego.clientstorage.fabric_client.network.RemoteStackPacket.isAccessingItem;


// todo  - putting item manually in container does not update the cache of it
@Mixin(ClientPacketListener.class)
public class MClientPacketListener {

    @Shadow
    private ClientLevel level;
    @Unique
    private boolean craftingScreen;
    @Unique
    private boolean receivedInventory;
    @Unique
    private int containerId = Integer.MIN_VALUE;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("TAIL"))
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundUseItemOnPacket pc) {
            System.out.println("Interact " + pc.getHitResult().getBlockPos());
        }
    }

    @Inject(method = "handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;getContainerId()I"
            ),
            cancellable = true
    )
    private void onInventoryPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        System.out.println("Inventory packet (crafting screen: " + craftingScreen + ")");
        if (isAccessingItem()) {
            ci.cancel();
            return;
        }
        if (!this.craftingScreen && this.containerId == packet.getContainerId()) {
            EventHandler.onInventoryPacket(packet);
            this.receivedInventory = true;

            if (fakePacketsActive()) {
                ci.cancel();
            }
        }

        this.containerId = Integer.MIN_VALUE;
    }

    @Inject(method = "handleOpenScreen",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/MenuScreens;create(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/Minecraft;ILnet/minecraft/network/chat/Component;)V"),
            cancellable = true)
    private void onOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        this.craftingScreen = packet.getType() == MenuType.CRAFTING;
        this.containerId = packet.getContainerId();
        System.out.println("Open screen packet");

        if (this.craftingScreen) {
            EventHandler.onFinalCraftingOpen();
        } else if (isAccessingItem() || fakePacketsActive()) {
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
                    shift = At.Shift.AFTER))
    private void onServerBrand(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        PacketLimiter.tryRecognizeServer();
    }

    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    private void onBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        BlockPos pos = packet.getPos().immutable();
        System.out.println("BU: " + pos + " (crafting screen: " + craftingScreen + ") (received inventory: " + receivedInventory + ")");

        if (this.level.getBlockEntity(pos) != null) {
            if (!this.craftingScreen && this.receivedInventory) {
                EventHandler.applyInventoryToBE(packet);
            }
            // Prevent double triggering, as Minecraft Server sends 2 packets for block updates
            this.receivedInventory = false;
        }
    }
}
