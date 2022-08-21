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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.casts.IRemoteStack;

import static org.samo_lego.clientstorage.event.EventHandler.ITEM_ORIGINS;
import static org.samo_lego.clientstorage.event.EventHandler.REMOTE_INV;
import static org.samo_lego.clientstorage.event.EventHandler.lastHitResult;

public class RemoteStackPacket {
    private static boolean accessingItem = false;

    public static boolean isAccessingItem() {
        return accessingItem;
    }

    public static void take(ItemStack remote) {
        // Get first free slot in player's inventory (to move items to)
        var player = Minecraft.getInstance().player;

        int freeSlot = player.getInventory().getSlotWithRemainingSpace(remote);
        if (freeSlot == -1) {
            freeSlot = player.getInventory().getFreeSlot();
        }


        if (freeSlot == -1) {
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

        // todo if 1 same item already in inv, it merges together
        var transferPacket = new ServerboundContainerClickPacket(containerId + 1, 1, remoteStack.cs_getSlotId(), 0, ClickType.QUICK_MOVE, transferredStack.copy(), map);
        // Send transfer item packet
        player.connection.send(transferPacket);


        // Close container
        player.connection.send(new ServerboundContainerClosePacket(containerId + 1));

        // Open crafting again
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastHitResult, containerId));


        System.out.println("Item transferred to " + freeSlot);

        // Set item to be picked up by the mouse todo
        //Minecraft.getInstance().gameMode.handleInventoryMouseClick(containerId, freeSlot, 0, ClickType.PICKUP, player);
        player.containerMenu.setCarried(transferredStack);
        Slot slot = player.containerMenu.slots.get(freeSlot);
        slot.onTake(player, transferredStack);
        player.containerMenu.setRemoteCarried(transferredStack);

        accessingItem = false;
    }

    public static void put(ItemStack stack) {
        if (ITEM_ORIGINS.containsKey(stack.getItem())) {
            var origins = ITEM_ORIGINS.get(stack.getItem());

            origins.stream().findFirst().ifPresent(itemOrigin -> {

                var player = Minecraft.getInstance().player;

                player.getInventory().placeItemBackInInventory(stack.copy());
                int freeSlot = player.getInventory().findSlotMatchingItem(stack);

                System.out.println("Item back in slot " + freeSlot);

                int containerId = player.containerMenu.containerId;


                // Set carried item to free slot
                var dropMap = new Int2ObjectOpenHashMap<ItemStack>();
                dropMap.put(freeSlot, stack);
                var clickPacket = new ServerboundContainerClickPacket(containerId, 0, freeSlot, 0, ClickType.PICKUP, ItemStack.EMPTY, dropMap);
                player.connection.send(clickPacket);
                player.containerMenu.setCarried(ItemStack.EMPTY);


                // Send interaction packet to server
                BlockEntity blockEntity = itemOrigin.blockEntity();
                BlockPos blockPos = blockEntity.getBlockPos();
                BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);

                // Helps us ignore GUI open packet later then
                accessingItem = true;

                // Close crafting
                player.connection.send(new ServerboundContainerClosePacket(containerId));
                // Open container
                player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, 0));


                var map = new Int2ObjectOpenHashMap<ItemStack>();
                map.put(itemOrigin.slot(), stack);
                map.put(freeSlot, ItemStack.EMPTY);

                var transferPacket = new ServerboundContainerClickPacket(containerId + 1, 1, itemOrigin.slot(), 0, ClickType.QUICK_MOVE, ItemStack.EMPTY, map);
                // Send transfer item packet
                player.connection.send(transferPacket);
                stack.setCount(0);  // visually clear item

                // Close container
                player.connection.send(new ServerboundContainerClosePacket(containerId));

                // Open crafting again
                player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastHitResult, 0));

                // Show item back in inventory
                REMOTE_INV.addStack(stack);

                accessingItem = false;
                origins.remove(itemOrigin);
            });
        }
    }
}
