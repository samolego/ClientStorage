package org.samo_lego.clientstorage.fabric_client.casts;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;
import org.samo_lego.clientstorage.fabric_client.network.PacketGame;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;

import java.util.Map;
import java.util.Optional;

import static org.samo_lego.clientstorage.fabric_client.util.StorageCache.FREE_SPACE_CONTAINERS;

public interface IRemoteStack {

    /**
     * 3x3 crafting slots + 1 output slot.
     */
    int CRAFTING_SLOT_OFFSET = 10;

    int cs_getSlotId();

    void cs_setSlotId(int slotId);

    /**
     * Assigns remote container data to provided item stack.
     *
     * @param stack     stack to assign data to.
     * @param container origin of the stack.
     * @param slot      slot where stack is located in origin container.
     * @return item stack with added info.
     */
    static ItemStack fromStack(ItemStack stack, InteractableContainer container, int slot) {
        // Add properties to ItemStack via IRemoteStack interface
        IRemoteStack remote = (IRemoteStack) stack;
        remote.cs_setSlotId(slot);
        remote.cs_setContainer(container);

        return stack;
    }

    InteractableContainer cs_getContainer();

    default void cs_clearData() {
        this.cs_setContainer(null);
        this.cs_setSlotId(-1);
    }

    void cs_setContainer(InteractableContainer parent);


    /**
     * Transfers this stack to any of
     * containers with free space left.
     *
     * @see #cs_transfer2Remote(boolean, int)
     */
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

        this.cs_transfer2Remote(true, freeSlot);
    }

    /**
     * Transfers this stack to any of containers
     * that have free space left.
     *
     * @param carried  whether this item stack is currently carried.
     * @param freeSlot any free slot index in player inventory to temporarily put item to.
     * @see org.samo_lego.clientstorage.fabric_client.util.StorageCache#FREE_SPACE_CONTAINERS
     */
    default void cs_transfer2Remote(boolean carried, int freeSlot) {
        var player = Minecraft.getInstance().player;

        Optional<Map.Entry<InteractableContainer, Integer>> containerCandidate = FREE_SPACE_CONTAINERS.entrySet().stream().findAny();

        if (containerCandidate.isEmpty()) {
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

        Map.Entry<InteractableContainer, Integer> emptyContainer = containerCandidate.get();

        // Check left space
        int spaceLeft = emptyContainer.getValue() - stack.getCount();
        if (spaceLeft <= 0) {
            FREE_SPACE_CONTAINERS.remove(emptyContainer.getKey());
        } else {
            FREE_SPACE_CONTAINERS.put(emptyContainer.getKey(), spaceLeft);
        }

        final InteractableContainer container = emptyContainer.getKey();
        // Open container
        container.cs_sendInteractionPacket();

        // Free slot in player's inv now has different index due to new container being open ...
        freeSlot = freeSlot - CRAFTING_SLOT_OFFSET + container.getContainerSize();

        map.clear();
        map.put(freeSlot, ItemStack.EMPTY);

        // Get first free slot in container
        int containerSlot;
        for (containerSlot = 0; containerSlot < container.getContainerSize(); ++containerSlot) {
            if (container.getItem(containerSlot).isEmpty()) {
                map.put(containerSlot, stack);
                break;
            }
        }

        // Send transfer item packet
        var transferPacket = new ServerboundContainerClickPacket(containerId + 1, 1, freeSlot, 0, ClickType.QUICK_MOVE, stack, map);
        player.connection.send(transferPacket);

        // Close container
        PacketGame.closeCurrentScreen();
        // Open crafting again
        PacketGame.openCrafting();

        // Add to remote inventory
        ((IRemoteStack) stack).cs_setSlotId(containerSlot);
        ((IRemoteStack) stack).cs_setContainer(container);

        final var copiedStack = stack.copy();
        RemoteInventory.getInstance().addStack(copiedStack);
        if (containerSlot != container.getContainerSize())
            container.setItem(containerSlot, copiedStack);
        else System.out.println("Container @ " + container + " is full!");
        stack.setCount(0);
    }
}
