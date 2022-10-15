package org.samo_lego.clientstorage.fabric_client.casts;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
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
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;
import org.samo_lego.clientstorage.fabric_client.util.PlayerLookUtil;

import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.FREE_SPACE_CONTAINERS;
import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.lastCraftingHit;

public interface IRemoteStack {

    int CRAFTING_SLOT_OFFSET = 10;

    int cs_getSlotId();

    void cs_setSlotId(int slotId);

    BlockEntity cs_getContainer();

    void cs_setContainer(BlockEntity parent);

    default void cs_clearData() {
        this.cs_setContainer(null);
        this.cs_setSlotId(-1);
    }


    static ItemStack fromStack(ItemStack stack, BlockEntity blockEntity, int slot) {
        // Add properties to ItemStack via IRemoteStack interface
        IRemoteStack remote = (IRemoteStack) stack;
        remote.cs_setSlotId(slot);
        remote.cs_setContainer(blockEntity);

        return stack;
    }


    default void cs_transfer2Remote() {
        var player = Minecraft.getInstance().player;
        // Get first free slot in player's inventory (to move item to)
        int freeSlot = -1;
        NonNullList<Slot> slots = player.containerMenu.slots;
        for (int i = CRAFTING_SLOT_OFFSET; i < slots.size(); ++i) {
            var slot = slots.get(i);

            if (!slot.hasItem()) {
                freeSlot = i;
                break;
            }
        }

        if (freeSlot == -1) {
            return;
        }

        cs_transfer2Remote(true, freeSlot);
    }

    default void cs_transfer2Remote(boolean carried, int freeSlot) {
        var player = Minecraft.getInstance().player;

        var container = FREE_SPACE_CONTAINERS.entrySet().stream().findAny();

        if (container.isEmpty()) {
            player.sendSystemMessage(Component.literal("No free space containers found.").withStyle(ChatFormatting.RED));
            return;
        }

        final ItemStack stack = (ItemStack) this;

        int containerId = player.containerMenu.containerId;

        var map = new Int2ObjectOpenHashMap<ItemStack>();
        if (carried) {
            // "Put" item in free slot
            map.put(freeSlot, stack);
            var transferPacket = new ServerboundContainerClickPacket(containerId, 1, freeSlot, 0, ClickType.PICKUP, ItemStack.EMPTY, map);
            player.connection.send(transferPacket);
        }

        // Close crafting
        player.connection.send(new ServerboundContainerClosePacket(containerId));

        // Helps us ignore GUI open packet later then
        ((ICSPlayer) player).cs_setAccessingItem(true);

        var emptyContainer = container.get();

        // Check left space
        int spaceLeft = emptyContainer.getValue() - 1;
        if (spaceLeft <= 0) {
            FREE_SPACE_CONTAINERS.remove(emptyContainer.getKey());
        } else {
            FREE_SPACE_CONTAINERS.put(emptyContainer.getKey(), spaceLeft);
        }
        var pos = emptyContainer.getKey();
        Container storage = (Container) player.getLevel().getBlockEntity(pos);

        // Open container
        var blockHit = PlayerLookUtil.raycastTo(pos);
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHit, 0));


        // Free slot in player's inv now has different index due to new container being open ...
        freeSlot = freeSlot - CRAFTING_SLOT_OFFSET + storage.getContainerSize();

        map.clear();
        map.put(freeSlot, ItemStack.EMPTY);

        // Get first free slot in container
        int containerSlot;
        for (containerSlot = 0; containerSlot < storage.getContainerSize(); ++containerSlot) {
            if (storage.getItem(containerSlot).isEmpty()) {
                map.put(containerSlot, stack);
                break;
            }
        }

        // Send transfer item packet
        var transferPacket = new ServerboundContainerClickPacket(containerId + 1, 1, freeSlot, 0, ClickType.QUICK_MOVE, stack, map);
        player.connection.send(transferPacket);

        // Close container
        player.connection.send(new ServerboundContainerClosePacket(containerId + 1));

        // Open crafting again
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, containerId));

        // Add to remote inventory
        ((IRemoteStack) stack).cs_setSlotId(containerSlot);

        ((IRemoteStack) stack).cs_setContainer((BlockEntity) storage);

        final var copiedStack = stack.copy();
        RemoteInventory.getInstance().addStack(copiedStack);
        if (containerSlot != storage.getContainerSize())
            storage.setItem(containerSlot, copiedStack);
        else System.out.println("Container @ " + pos.toShortString() + " is full!");
        stack.setCount(0);
    }
}
