package org.samo_lego.clientstorage.inventory;

import net.minecraft.core.Registry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RemoteInventory implements Container {

    private final List<ItemStack> stacks;

    public RemoteInventory() {
        this.stacks = new ArrayList<>();
    }

    public void sort() {
        this.stacks.sort(Comparator.comparingInt(stack -> Registry.ITEM.getId(stack.getItem())));
    }

    @Override
    public int getContainerSize() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.size() == 0 || this.stacks.stream().noneMatch(ItemStack::isEmpty);
    }

    /**
     * Fetches the stack currently stored at the given slot. If the slot is empty,
     * or is outside the bounds of this inventory, returns see {@link ItemStack#EMPTY}.
     *
     * @param slot
     */
    @Override
    public ItemStack getItem(int slot) {
        return slot >= this.getContainerSize() ? ItemStack.EMPTY : this.stacks.get(slot);
    }

    /**
     * Removes a specific number of items from the given slot.
     *
     * @param slot
     * @param amount
     *
     * @return the removed items as a stack
     */
    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= stacks.size() || stacks.get(slot).isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        return stacks.get(slot).split(amount);
    }

    /**
     * Removes the stack currently stored at the indicated slot.
     *
     * @param slot
     *
     * @return the stack previously stored at the indicated slot.
     */
    @Override
    public ItemStack removeItemNoUpdate(int slot) {

        if (slot < 0 || slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }

        return stacks.remove(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0) {
            return;
        }
        if (stack.isEmpty() && slot < stacks.size()) {
            this.stacks.remove(slot);
            return;
        }
        if (slot >= stacks.size()) {
            this.stacks.add(stack);
        } else {
            this.stacks.set(slot, stack);
        }
    }

    public void addStack(ItemStack remoteStack) {
        this.stacks.add(remoteStack);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.stacks.clear();
    }
}
