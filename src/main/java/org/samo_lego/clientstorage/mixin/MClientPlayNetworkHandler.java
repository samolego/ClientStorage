package org.samo_lego.clientstorage.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.event.EventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.clientstorage.event.EventHandler.fakePacketsActive;
import static org.samo_lego.clientstorage.network.RemoteStackPacket.isAccessingItem;

@Mixin(ClientPacketListener.class)
public class MClientPlayNetworkHandler {

    @Unique
    private BlockPos clientstorage$currentPos = null;
    @Unique
    private int clientStorage$currentSyncId = -1;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void onPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundContainerClickPacket) {
            System.out.println(((ServerboundContainerClickPacket) packet).getSlotNum());
        }
        if(packet instanceof ServerboundUseItemOnPacket) {
            /*BlockPos blockPos = ((PlayerInteractBlockC2SPacket) packet).getBlockHitResult().getBlockPos();
            Vec3d be = ((PlayerInteractBlockC2SPacket) packet).getBlockHitResult().getPos();
            Direction side = ((PlayerInteractBlockC2SPacket) packet).getBlockHitResult().getSide();
            System.out.println("C2S interact " + blockPos + " " + be+ " "+ side);*/
        } /*else if(!(packet instanceof ServerboundMovePlayerPacket))
            System.out.println(packet.getClass());*/


    }

    @Inject(
            method = "handleContainerSetSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/tutorial/Tutorial;onGetItem(Lnet/minecraft/world/item/ItemStack;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void onScreenHandlerSlotUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci, Player player, ItemStack stack, int slotId) {
        // Specific stack
        //System.out.println(((ScreenHandlerSlotUpdateS2CPacket) packet).getItemStack());
        /*System.out.println("Slot: " + slotId + " stack " + stack);
        if (clientstorage$currentPos == null || slotId == -1 || clientStorage$currentSyncId == -1) {
            System.out.println("Return to parent method");
            return;
        }

        // content
        BlockEntity be = player.getCommandSenderWorld().getBlockEntity(clientstorage$currentPos);
        if (be instanceof Container && slotId < ((Container) be).getContainerSize()) {
            //((Container) be).setItem(slotId, stack);
            System.out.println("Empty: " + ((Container) player.getCommandSenderWorld().getBlockEntity(clientstorage$currentPos)).isEmpty());
            ci.cancel();
        }*/
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
        if (isAccessingItem() || fakePacketsActive()) {
            ci.cancel();
        }
    }
}
