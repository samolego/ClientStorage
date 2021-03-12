package org.samo_lego.clientstorage.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "handlePacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;)V", at = @At("HEAD"))
    private static void onReceivedPacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if(packet instanceof InventoryS2CPacket) {
            System.out.println("InventoryS2CPacket");
        } else if(packet instanceof ScreenHandlerSlotUpdateS2CPacket) {
            System.out.println("ScreenHandlerSlotUpdateS2CPacket");
        } else if(packet instanceof OpenScreenS2CPacket) {
            System.out.println("OpenScreenS2CPacket");
        }
    }
}
