package org.samo_lego.clientstorage.fabric_client.mixin.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.inventory.MenuType;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.event.ContainerDiscovery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.sounds.SoundSource.BLOCKS;


@Mixin(ClientPacketListener.class)
public class MClientPacketListener {

    @Unique
    private boolean craftingScreen;
    @Unique
    private int containerId = Integer.MIN_VALUE;


    /**
     * Handles the item stacks sent for interacted container.
     *
     * @param packet
     * @param ci
     */
    @Inject(method = "handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;getContainerId()I"),
            cancellable = true)
    private void onInventoryPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (((ICSPlayer) Minecraft.getInstance().player).cs_isAccessingItem()) {
            ci.cancel();
            return;
        }
        if (!this.craftingScreen) {
            assert this.containerId == packet.getContainerId() || this.containerId == Integer.MIN_VALUE : "Container screen ID mismatch";
            ContainerDiscovery.onInventoryPacket(packet);

            if (ContainerDiscovery.fakePacketsActive()) {
                ci.cancel();
            }
        }

        this.containerId = Integer.MIN_VALUE;
    }

    /**
     * Handles screen opening packet.
     * If player is accesing items via
     * terminal, screen packets are ignored.
     *
     * @param packet
     * @param ci
     */
    @Inject(method = "handleOpenScreen",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/MenuScreens;create(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/Minecraft;ILnet/minecraft/network/chat/Component;)V"),
            cancellable = true)
    private void onOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        this.craftingScreen = packet.getType() == MenuType.CRAFTING;
        this.containerId = packet.getContainerId();

        if (this.craftingScreen) {
            ((ICSPlayer) Minecraft.getInstance().player).cs_setAccessingItem(false);
        } else if (((ICSPlayer) Minecraft.getInstance().player).cs_isAccessingItem() || ContainerDiscovery.fakePacketsActive()) {

            ci.cancel();
        }
    }

    /**
     * Disables incoming block sounds (e.g. barrel opening)
     * if it was generated due to mod's packets.
     *
     * @param packet
     * @param ci
     */
    @Inject(method = "handleSoundEvent",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void onSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
        // Cancel sounds if item search is active
        if (packet.getSource().equals(BLOCKS) && (((ICSPlayer) Minecraft.getInstance().player).cs_isAccessingItem() || ContainerDiscovery.fakePacketsActive())) {
            ci.cancel();
        }
    }

}
