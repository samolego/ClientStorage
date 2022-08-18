package org.samo_lego.clientstorage.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.samo_lego.clientstorage.casts.IRemoteCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;

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
        System.out.println("Slot: " + slotId + " stack " + stack);
        if (clientstorage$currentPos == null || slotId == -1 || clientStorage$currentSyncId == -1) {
            System.out.println("Return to parent method");
            return;
        }

        // content
        BlockEntity be = player.getCommandSenderWorld().getBlockEntity(clientstorage$currentPos);
        if (be instanceof Container && slotId < ((Container) be).getContainerSize()) {
            ((Container) be).setItem(slotId, stack);
            System.out.println("Empty: " + ((Container) player.getCommandSenderWorld().getBlockEntity(clientstorage$currentPos)).isEmpty());
            ci.cancel();
        }
    }


    @Inject(
            method = "handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onInventoryPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!INTERACTION_Q.isEmpty() /*&& packet.getSyncId() == MinecraftClient.getInstance().player.currentScreenHandler.syncId*/) {
            clientstorage$currentPos = INTERACTION_Q.removeFirst();

            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(clientstorage$currentPos);
            if (be instanceof Container container) {
                // Invalidating old cache
                System.out.println("Checking " + clientstorage$currentPos + ", empty:: -> " + container.isEmpty());
                container.clearContent();
                List<ItemStack> items = packet.getItems();
                for (int i = 0; i < items.size() && i < container.getContainerSize(); ++i) {
                    container.setItem(i, items.get(i));
                }
            }
            this.clientStorage$currentSyncId = packet.getContainerId();
            ci.cancel();
        } else {
            clientstorage$currentPos = null;
            this.clientStorage$currentSyncId = -1;

            try {
                AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
                if (menu.getType() == MenuType.CRAFTING) {
                    System.out.println("REFRESHING");
                    ((IRemoteCrafting) menu).refreshRemoteInventory();
                }
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }
}
