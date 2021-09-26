package org.samo_lego.clientstorage.inventory;

import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public class RemoteInventory implements Container {

    private final NonNullList<ItemStack> stacks;
    private final AbstractContainerMenu handler;
    private final int width;
    private final int height;

    public RemoteInventory(AbstractContainerMenu handler, int width, int height) {
        this.stacks = NonNullList.withSize(width * height, ItemStack.EMPTY);
        this.handler = handler;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getContainerSize() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        Iterator<ItemStack> stackIterator = this.stacks.iterator();

        ItemStack itemStack;
        do {
            if (!stackIterator.hasNext()) {
                return true;
            }

            itemStack = stackIterator.next();
        } while(itemStack.isEmpty());

        return false;
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
        ItemStack stack = ContainerHelper.removeItem(this.stacks, slot, amount);
        if (!stack.isEmpty()) {
            this.handler.slotsChanged(this);
        }

        return stack;
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
        return ContainerHelper.takeItem(this.stacks, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        System.out.println("Setting " + slot);
        try {
            this.stacks.set(slot, stack);
            this.handler.slotsChanged(this);
        } catch(ArrayIndexOutOfBoundsException | ReportedException e) {
            e.printStackTrace();
        }
    }

    public void addStack(ItemStack stack) {
        // 100% there's a better way to do this
        for(int i = 0; i < this.width * this.height; ++i) {
            if(this.stacks.get(i) == ItemStack.EMPTY) {
                this.stacks.set(i, stack);
                break;
            }
        }
        this.handler.slotsChanged(this);

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

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }
}
