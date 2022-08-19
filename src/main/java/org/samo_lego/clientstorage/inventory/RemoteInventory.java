package org.samo_lego.clientstorage.inventory;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RemoteInventory implements Container {

    private final List<ItemStack> stacks;
    private List<ItemStack> searched;
    private float scrollOffset = 0.0f;

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
        slot = this.getOffsetSlot(slot);
        if (slot < 0) return ItemStack.EMPTY;

        if (this.searched != null) {
            if (slot >= this.searched.size()) {
                return ItemStack.EMPTY;
            }
            return this.searched.get(slot);
        }


        if (slot >= this.stacks.size()) {
            return ItemStack.EMPTY;
        }

        return this.stacks.get(slot);
    }

    private int getOffsetSlot(int slot) {
        return (int) (slot + this.scrollOffset * this.getRows() * 9);
    }

    /**
     * Removes a specific number of items from the given slot.
     *
     * @param slot
     * @param amount
     * @return the removed items as a stack
     */
    @Override
    public ItemStack removeItem(int slot, int amount) {
        slot = this.getOffsetSlot(slot);
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
        slot = this.getOffsetSlot(slot);
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

    public int getRows() {
        return (int) Math.ceil(this.stacks.size() / 9);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.stacks.clear();
        this.searched = null;
    }

    public void refreshSearchResults(String value) {
        this.scrollTo(0.0f);
        if (value == null || value.isEmpty()) {
            this.searched = null;
            return;
        }

        //SearchTree<ItemStack> searchTree;
        if (value.startsWith("#")) {
            value = value.substring(1);

            String finalValue1 = value;
            this.searched = this.stacks.stream().filter(st -> {
                CompoundTag tag = st.getTag();
                if (tag == null) {
                    return false;
                }
                return tag.toString().toLowerCase(Locale.ROOT).contains(finalValue1.toLowerCase(Locale.ROOT));

            }).collect(Collectors.toList());
            //searchTree = Minecraft.getInstance().getSearchTree(SearchRegistry.CREATIVE_TAGS);
            //var search = searchTree.search(value.toLowerCase(Locale.ROOT)).stream().map(ItemStack::getItem).collect(Collectors.toSet());
            //this.searched = this.stacks.stream().filter(stack -> search.contains(stack.getItem())).collect(Collectors.toList());
        } else if (value.startsWith("@")) {
            String finalValue = value.toLowerCase(Locale.ROOT);
            this.searched = this.stacks.stream().filter(stack ->
                            Registry.ITEM.getKey(stack.getItem()).getNamespace().startsWith(finalValue.substring(1)))
                    .collect(Collectors.toList());
        } else {
            String finalValue = value;
            this.searched = this.stacks.stream()
                    .filter(stack ->
                            stack.getDisplayName()
                                    .getString().toLowerCase(Locale.ROOT)
                                    .contains(finalValue.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
    }

    public void scrollTo(float scrollOffs) {
        this.scrollOffset = scrollOffs;
    }
}
