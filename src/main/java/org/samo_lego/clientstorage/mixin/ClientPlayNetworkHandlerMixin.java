package org.samo_lego.clientstorage.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Unique
    private BlockPos clientstorage$currentPos = null;

    @Inject(method = "sendPacket", at = @At("HEAD"))
    private void onPacket(Packet<?> packet, CallbackInfo ci) {
        if(packet instanceof PlayerInteractBlockC2SPacket) {
            /*BlockPos blockPos = ((PlayerInteractBlockC2SPacket) packet).getBlockHitResult().getBlockPos();
            Vec3d pos = ((PlayerInteractBlockC2SPacket) packet).getBlockHitResult().getPos();
            Direction side = ((PlayerInteractBlockC2SPacket) packet).getBlockHitResult().getSide();
            System.out.println("C2S interact " + blockPos + " " + pos+ " "+ side);*/
        } else if(!(packet instanceof PlayerMoveC2SPacket))
            System.out.println(packet.getClass());
    }

    @Inject(
            method = "onScreenHandlerSlotUpdate(Lnet/minecraft/network/packet/s2c/play/ScreenHandlerSlotUpdateS2CPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/tutorial/TutorialManager;onSlotUpdate(Lnet/minecraft/item/ItemStack;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci, PlayerEntity player, ItemStack stack, int slotId) {
        // Specific stack
        //System.out.println(((ScreenHandlerSlotUpdateS2CPacket) packet).getItemStack());
        System.out.println("Slot: " + slotId);
        try {
            if(clientstorage$currentPos == null || slotId == -1 || INTERACTION_Q.isEmpty() || player.currentScreenHandler.getType() == ScreenHandlerType.CRAFTING) {
                System.out.println("Return to parent method");
                return;
            }
        } catch(UnsupportedOperationException ignored) {
            return;
        }

        // content
        BlockEntity be = player.getEntityWorld().getBlockEntity(clientstorage$currentPos);
        if(be instanceof Inventory) {
            ((Inventory) be).setStack(slotId, stack);
            System.out.println("Empty: " + ((Inventory) player.getEntityWorld().getBlockEntity(clientstorage$currentPos)).isEmpty());
            ci.cancel();
        }
    }


    @Inject(
            method = "onInventory(Lnet/minecraft/network/packet/s2c/play/InventoryS2CPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onInventoryPacket(InventoryS2CPacket packet, CallbackInfo ci) {
        if(!INTERACTION_Q.isEmpty() && packet.getSyncId() == MinecraftClient.getInstance().player.currentScreenHandler.syncId) {
            clientstorage$currentPos = INTERACTION_Q.remove();
            ci.cancel();
        }
        else
            clientstorage$currentPos = null;
    }
}
