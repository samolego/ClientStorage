package org.samo_lego.clientstorage.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.casts.IRemoteStack;

import static org.samo_lego.clientstorage.event.EventHandler.lastHitResult;

public class RemoteStackPacket {
    private static boolean accessingItem = false;

    public static boolean isAccessingItem() {
        return accessingItem;
    }

    public static void take(ItemStack remote) {
        // Get first free slot in player's inventory (to move items to)
        var player = Minecraft.getInstance().player;
        int freeSlot = -999;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                freeSlot = i;
                break;
            }
        }

        if (freeSlot == -999) {
            System.out.println("No free slot in player's inventory");
            return;
        }


        var remoteStack = (IRemoteStack) (Object) remote;

        // Send interaction packet to server
        BlockEntity blockEntity = remoteStack.cs_getContainer();
        BlockPos blockPos = blockEntity.getBlockPos();
        BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);

        //player.closeContainer();

        int containerId = player.containerMenu.containerId;

        // Close crafting
        player.connection.send(new ServerboundContainerClosePacket(containerId));

        // Helps us ignore GUI open packet later then
        accessingItem = true;
        // Open container
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, 0));

        ItemStack transferredStack = remote.copy();

        var map = new Int2ObjectOpenHashMap<ItemStack>();
        map.put(remoteStack.cs_getSlotId(), ItemStack.EMPTY);
        map.put(freeSlot, transferredStack);

        var transferPacket = new ServerboundContainerClickPacket(containerId + 1, 1, remoteStack.cs_getSlotId(), 0, ClickType.QUICK_MOVE, transferredStack.copy(), map);
        // Send transfer item packet
        player.connection.send(transferPacket);

        // Clear item todo
        remote.setCount(0);

        // Close container
        player.connection.send(new ServerboundContainerClosePacket(containerId));

        // Open crafting again
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastHitResult, 0));
        System.out.println("Item transferred to " + freeSlot);
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(containerId, freeSlot, 0, ClickType.PICKUP_ALL, player);

        // Set item to be picked up by the mouse
        /*map.clear();
        map.put(freeSlot, ItemStack.EMPTY);
        player.connection.send(new ServerboundContainerClickPacket(containerId + 2, 0, freeSlot, 0, ClickType.PICKUP, transferredStack, map));*/

        accessingItem = false;
    }
}
