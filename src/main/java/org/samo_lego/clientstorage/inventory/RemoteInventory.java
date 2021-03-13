package org.samo_lego.clientstorage.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import org.samo_lego.clientstorage.mixin.accessor.DefaultedListAccessor;

import java.util.Iterator;
import java.util.stream.Collectors;

public class RemoteInventory implements Inventory {

    private final DefaultedList<ItemStack> stacks;
    private final ScreenHandler handler;
    private final int width;
    private final int height;

    public RemoteInventory(ScreenHandler handler, int width, int height) {
        this.stacks = DefaultedList.ofSize(width * height, ItemStack.EMPTY);
        this.handler = handler;
        this.width = width;
        this.height = height;
    }

    @Override
    public int size() {
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
    public ItemStack getStack(int slot) {
        return slot >= this.size() ? ItemStack.EMPTY : this.stacks.get(slot);
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
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = Inventories.splitStack(this.stacks, slot, amount);
        if (!stack.isEmpty()) {
            this.handler.onContentChanged(this);
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
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.stacks, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        System.out.println("Setting " + slot);
        try {
            this.stacks.set(slot, stack);
            this.handler.onContentChanged(this);
        } catch(ArrayIndexOutOfBoundsException | CrashException e) {
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
        this.handler.onContentChanged(this);

    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }
}
