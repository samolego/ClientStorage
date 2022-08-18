package org.samo_lego.clientstorage.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Connection.class)
public class MClientConnection {
    @Inject(method = "genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V", at = @At("HEAD"))
    private static void onReceivedPacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundContainerSetContentPacket) {
            System.out.println("InventoryS2CPacket");
        } else if (packet instanceof ClientboundContainerSetSlotPacket) {
            System.out.println("ScreenHandlerSlotUpdateS2CPacket");
        } else if (packet instanceof ClientboundOpenScreenPacket) {
            System.out.println("OpenScreenS2CPacket");
        }
    }
}
