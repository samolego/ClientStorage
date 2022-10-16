package org.samo_lego.clientstorage.fabric_client.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class RemoteInventory implements Container {
    private static RemoteInventory INSTANCE;


    /**
     * Holds all the items in the inventories.
     * List index represents the slot number.
     * Pair.first is the fake item stack with correct amount.
     * Pair.second is the list of items in the slot.
     */
    private final List<Pair<ItemStack, List<ItemStack>>> stacks;
    private List<ItemStack> searchStacks;
    private float scrollOffset = 0.0f;
    private String searchValue;

    public RemoteInventory() {
        this.stacks = new ArrayList<>();
        this.searchValue = "";
        INSTANCE = this;
    }

    public static RemoteInventory getInstance() {
        return INSTANCE;
    }

    public void sort() {
        this.stacks.sort((stacksA, stacksB) -> {
            var first = stacksA.getSecond().stream().findAny();
            var second = stacksB.getSecond().stream().findAny();

            if (first.isPresent() && second.isPresent()) {
                var itemA = first.get();
                var itemB = second.get();

                return Registry.ITEM.getId(itemA.getItem()) - Registry.ITEM.getId(itemB.getItem());

            } else if (first.isPresent()) {
                return -1;
            } else if (second.isPresent()) {
                return 1;
            }
            System.out.println("No items in stacks.");
            return 0;
        });
    }

    @Override
    public int getContainerSize() {
        return Objects.requireNonNullElse(this.searchStacks, this.stacks).size();
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.size() == 0 ||
                this.stacks.stream().allMatch(pair -> pair.getSecond().isEmpty()) ||
                this.stacks.stream().allMatch(pair -> pair.getFirst().isEmpty());
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
        if (slot < 0 || slot >= this.getContainerSize()) return ItemStack.EMPTY;

        if (this.searchStacks != null) {
            return this.searchStacks.get(slot);
        }

        var items = this.stacks.get(slot);

        assert !items.getFirst().isEmpty() : "Item in slot " + slot + " was empty!";

        return items.getFirst();
    }

    private int getOffsetSlot(int slot) {
        return (int) (slot + this.scrollOffset * (this.getRows()) * 9);
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
        System.err.println("RemoteInventory#removeItem with amount called");
        return ItemStack.EMPTY;
        /*slot = this.getOffsetSlot(slot);
        if (slot < 0 || slot >= this.getContainerSize() || this.getItem(slot).isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        if (this.searchStacks != null) {
            this.searchStacks.get(slot).split(amount);
        }
        return this.stacks.get(slot).split(amount);*/
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
        if (slot < 0 || slot >= this.getContainerSize()) {
            return ItemStack.EMPTY;
        }

        var stacks = this.stacks;

        var items = stacks.get(slot);
        var itemType = items.getFirst();

        ItemStack removed = items.getSecond().remove(items.getSecond().size() - 1);
        itemType.shrink(removed.getMaxStackSize());

        if (itemType.isEmpty()) {
            stacks.remove(slot);
        }

        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    public void addStack(ItemStack remoteStack) {
        // Get index of the same items
        for (var pair : this.stacks) {
            final ItemStack key = pair.getFirst();
            if (ItemStack.isSameItemSameTags(key, remoteStack)) {
                pair.getSecond().add(remoteStack);
                key.grow(remoteStack.getCount());
                return;
            }
        }

        // Not found, add new stack
        this.stacks.add(Pair.of(remoteStack.copy(), new LinkedList<>(List.of(remoteStack))));
    }

    @Override
    public void setChanged() {

    }

    public int getRows() {
        return (int) Math.ceil(this.stacks.size() / 9.0);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.stacks.clear();
        this.searchStacks = null;
    }

    public void refreshSearchResults(String value) {
        this.scrollTo(0.0f);

        /*if (value == null || value.isEmpty()) {
            this.searchStacks = null;
            this.searchValue = "";
            return;
        }
        value = value.toLowerCase(Locale.ROOT);

        List<ItemStack> filtered = this.stacks;
        if (value.startsWith(this.searchValue) && this.searchStacks != null) {
            // Fewer items to search through, use the cached results
            filtered = this.searchStacks;
        }

        this.searchValue = value;

        if (value.startsWith("$")) {
            value = value.substring(1);

            String finalValue = value;
            this.searchStacks = filtered.stream().filter(st -> st.getItemHolder().tags().anyMatch(tagKey -> {
                ResourceLocation location = tagKey.location();
                String tagName;
                if (finalValue.contains(":")) {
                    tagName = location.toString();
                } else {
                    tagName = location.getPath();
                }
                tagName = tagName.toLowerCase(Locale.ROOT);

                return tagName.startsWith(finalValue);

            })).collect(Collectors.toList());
        } else if (value.startsWith("#")) {
            value = value.substring(1);

            String finalValue = value;
            this.searchStacks = filtered.stream().filter(st -> {
                CompoundTag tag = st.getTag();
                if (tag == null) {
                    return false;
                }
                return tag.toString().toLowerCase(Locale.ROOT).contains(finalValue);

            }).collect(Collectors.toList());
        } else if (value.startsWith("@")) {
            String finalValue = value;
            this.searchStacks = filtered.stream().filter(stack -> {
                var search = finalValue.substring(1).split(" ");
                String namespace = search[0];

                boolean namespaceFltr = Registry.ITEM.getKey(stack.getItem()).toString().startsWith(namespace);

                if (namespaceFltr && search.length > 1) {
                    return stack.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(search[1]);
                }

                return namespaceFltr;
            }).collect(Collectors.toList());
        } else {
            String finalValue = value;
            this.searchStacks = filtered.stream()
                    .filter(stack ->
                            stack.getDisplayName()
                                    .getString().toLowerCase(Locale.ROOT)
                                    .contains(finalValue.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }*/
    }

    public void scrollTo(float scrollOffs) {
        this.scrollOffset = scrollOffs;
    }

    public float scrollOffset() {
        return this.scrollOffset;
    }

    public void reset() {
        this.scrollTo(0.0f);
        this.clearContent();
        this.searchValue = "";
    }
}
