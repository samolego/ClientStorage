package org.samo_lego.clientstorage.fabric_client.casts;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.FREE_SPACE_CONTAINERS;
import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.lastCraftingHit;
import static org.samo_lego.clientstorage.fabric_client.network.RemoteStackPacket.accessingItem;

public interface IRemoteStack {

    int cs_getSlotId();

    void cs_setSlotId(int slotId);

    BlockEntity cs_getContainer();

    void cs_setContainer(BlockEntity parent);


    static ItemStack fromStack(ItemStack stack, BlockEntity blockEntity, int slot) {
        // Add properties to ItemStack via IRemoteStack interface
        IRemoteStack remote = (IRemoteStack) stack;
        remote.cs_setSlotId(slot);
        remote.cs_setContainer(blockEntity);

        return stack;
    }


    default void cs_transferToPlayer() {
        // Get first free slot in player's inventory (to move items to)
        var player = Minecraft.getInstance().player;
        var remote = (ItemStack) this;

        int freeSlot = player.getInventory().getSlotWithRemainingSpace(remote);
        if (freeSlot == -1) {
            freeSlot = player.getInventory().getFreeSlot();
        }


        if (freeSlot == -1) {
            return;
        }


        var remoteStack = (IRemoteStack) remote;

        // Send interaction packet to server
        BlockEntity blockEntity = remoteStack.cs_getContainer();
        BlockPos blockPos = blockEntity.getBlockPos();
        BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);

        // Remove item from client container
        ((Container) blockEntity).setItem(remoteStack.cs_getSlotId(), ItemStack.EMPTY);

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
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, containerId));


        // Set item to be picked up by the mouse todo
        //Minecraft.getInstance().gameMode.handleInventoryMouseClick(containerId, freeSlot, 0, ClickType.PICKUP, player);
        player.containerMenu.setCarried(transferredStack);
        Slot slot = player.containerMenu.slots.get(freeSlot);
        slot.onTake(player, transferredStack);
        player.containerMenu.setRemoteCarried(transferredStack);

        accessingItem = false;
    }


    default void put() {
        // Get first free slot in player's inventory (to move items to)
        var player = Minecraft.getInstance().player;
        var container = FREE_SPACE_CONTAINERS.entrySet().stream().findAny();

        if (container.isEmpty()) {
            player.sendSystemMessage(Component.literal("No free space containers found.").withStyle(ChatFormatting.RED));
            return;
        }

        ItemStack stack = (ItemStack) this;

        var freeBlock = container.get();


        // Send interaction packet to server
        var blockPos = freeBlock.getKey();
        var result = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);


        int containerId = player.containerMenu.containerId;

        // Close crafting
        player.connection.send(new ServerboundContainerClosePacket(containerId));

        // Helps us ignore GUI open packet later then
        accessingItem = true;
        // Open container
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, 0));

        ItemStack transferredStack = stack.copy();

        var map = new Int2ObjectOpenHashMap<ItemStack>();
        /*map.put(remoteStack.cs_getSlotId(), ItemStack.EMPTY);
        map.put(freeSlot, transferredStack);*/

        // todo if 1 same item already in inv, it merges together
        //var transferPacket = new ServerboundContainerClickPacket(containerId + 1, 1, remoteStack.cs_getSlotId(), 0, ClickType.QUICK_MOVE, transferredStack.copy(), map);
        // Send transfer item packet
        //player.connection.send(transferPacket);


        // Close container
        player.connection.send(new ServerboundContainerClosePacket(containerId + 1));

        // Open crafting again
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, containerId));


        // Set item to be picked up by the mouse todo
        //Minecraft.getInstance().gameMode.handleInventoryMouseClick(containerId, freeSlot, 0, ClickType.PICKUP, player);
        player.containerMenu.setCarried(transferredStack);
        //Slot slot = player.containerMenu.slots.get(freeSlot);
        //slot.onTake(player, transferredStack);
        player.containerMenu.setRemoteCarried(transferredStack);

        accessingItem = false;
    }
}
