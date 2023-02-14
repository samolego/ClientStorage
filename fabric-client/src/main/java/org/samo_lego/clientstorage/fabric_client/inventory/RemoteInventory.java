package org.samo_lego.clientstorage.fabric_client.inventory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;
import static org.samo_lego.clientstorage.fabric_client.util.StorageCache.FREE_SPACE_CONTAINERS;

public class RemoteInventory implements Container {
    private static RemoteInventory INSTANCE;


    /**
     * Holds all the items in the inventories.
     * List index represents the slot number.
     * Inserted list contains itemstacks.
     * First one is on display as well.
     */
    private final List<LinkedList<ItemStack>> stacks;
    private List<LinkedList<ItemStack>> searchStacks;
    private float scrollOffset = 0.0f;
    private String searchValue;

    public RemoteInventory() {
        this.stacks = Collections.synchronizedList(new ArrayList<>());
        this.searchValue = "";
        INSTANCE = this;
    }

    public static RemoteInventory getInstance() {
        return INSTANCE;
    }

    public void sort() {
        this.stacks.sort((stacksA, stacksB) -> {
            if (stacksA.isEmpty()) {
                return 1;
            } else if (stacksB.isEmpty()) {
                return -1;
            } else {
                ItemStack itemA = stacksA.getFirst();
                ItemStack itemB = stacksB.getFirst();

                return Item.getId(itemA.getItem()) - Item.getId(itemB.getItem());
            }
        });
    }

    @Override
    public int getContainerSize() {
        return Objects.requireNonNullElse(this.searchStacks, this.stacks).size();
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.size() == 0 ||
                this.stacks.stream().allMatch(List::isEmpty) ||
                this.stacks.stream().allMatch(stacks -> stacks.stream().allMatch(ItemStack::isEmpty));
    }

    /**
     * Fetches the stack currently stored at the given slot. If the slot is empty,
     * or is outside the bounds of this inventory, returns see {@link ItemStack#EMPTY}.
     * <p>
     * Note: this can be just a placeholder item, e.g. overstacked item.
     * </p>
     */
    @Override
    public synchronized ItemStack getItem(int slot) {
        slot = this.getOffsetSlot(slot);
        if (slot < 0 || slot >= this.getContainerSize()) return ItemStack.EMPTY;

        return Objects.requireNonNullElse(this.searchStacks, this.stacks).get(slot).getFirst();
    }

    private int getOffsetSlot(int slot) {
        return slot + Math.round(this.scrollOffset * Math.max(this.getRows() - 3, 0)) * 9;
    }

    /**
     * Just there as interface requires it.
     * You can only take whole stacks currently.
     */
    @ApiStatus.OverrideOnly
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
     * @param slot slot index
     * @return the stack previously stored at the indicated slot.
     */
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        slot = this.getOffsetSlot(slot);
        if (slot < 0 || slot >= this.getContainerSize()) {
            return ItemStack.EMPTY;
        }

        var stacks = Objects.requireNonNullElse(this.searchStacks, this.stacks).get(slot);
        var displayStack = stacks.getFirst();

        final ItemStack removed = stacks.removeLast();
        FREE_SPACE_CONTAINERS.compute(removed.cs_getContainer(), (container, freeSpace) -> freeSpace == null ? removed.getCount() : freeSpace + removed.getCount());

        if (!stacks.isEmpty()) {
            displayStack.shrink(removed.getCount());
        } else {
            if (this.searchStacks != null) {
                // Remove from main stacks as well
                this.stacks.remove(stacks);
            }
            Objects.requireNonNullElse(this.searchStacks, this.stacks).remove(slot);
        }

        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    public synchronized void addStack(ItemStack remoteStack) {
        // Get index of the same items
        if (config.itemDisplayType != ItemBehaviour.ItemDisplayType.SEPARATE_ALL) {
            for (var stacks : this.stacks) {
                final ItemStack firstStack = stacks.getFirst();

                if (ItemStack.isSameItemSameTags(firstStack, remoteStack)) {
                    if (config.itemDisplayType == ItemBehaviour.ItemDisplayType.MERGE_PER_CONTAINER) {
                        // We need to check if items are in the same container as well
                        if (firstStack.cs_getContainer() != remoteStack.cs_getContainer()) {
                            continue;
                        }
                    }

                    stacks.addLast(remoteStack);
                    firstStack.grow(remoteStack.getCount());
                    return;
                }
            }
        }
        // Not found, add new stack
        this.stacks.add(new LinkedList<>(List.of(remoteStack)));
    }

    @Override
    public void setChanged() {
    }

    public int getRows() {
        return (int) Math.ceil(Objects.requireNonNullElse(this.searchStacks, this.stacks).size() / 9.0);
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

        if (value == null || value.isEmpty()) {
            this.searchStacks = null;
            this.searchValue = "";
            return;
        }
        value = value.toLowerCase(Locale.ROOT);

        List<LinkedList<ItemStack>> filtered = this.stacks;
        if (value.startsWith(this.searchValue) && this.searchStacks != null) {
            // Fewer items to search through, use the cached results
            filtered = this.searchStacks;
        }

        this.searchValue = value;

        if (value.startsWith("$")) {
            value = value.substring(1);

            String finalValue = value;
            this.searchStacks = new ArrayList<>(filtered.stream().filter(stackPair -> stackPair.getFirst().getItemHolder().tags().anyMatch(tagKey -> {
                ResourceLocation location = tagKey.location();
                String tagName;
                if (finalValue.contains(":")) {
                    tagName = location.toString();
                } else {
                    tagName = location.getPath();
                }
                tagName = tagName.toLowerCase(Locale.ROOT);

                return tagName.startsWith(finalValue);

            })).toList());
        } else if (value.startsWith("#")) {
            value = value.substring(1);

            String finalValue = value;
            this.searchStacks = new ArrayList<>(filtered.stream().filter(stackPair -> {
                CompoundTag tag = stackPair.getFirst().getTag();
                if (tag == null) {
                    return false;
                }
                return tag.toString().toLowerCase(Locale.ROOT).contains(finalValue);

            }).toList());
        } else if (value.startsWith("@")) {
            String finalValue = value;
            this.searchStacks = new ArrayList<>(filtered.stream().filter(stackPair -> {
                var search = finalValue.substring(1).split(" ");
                String namespace = search[0];
                final var item = stackPair.getFirst();

                boolean namespaceFltr = BuiltInRegistries.ITEM.getKey(item.getItem()).toString().startsWith(namespace);

                if (namespaceFltr && search.length > 1) {
                    return item.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(search[1]);
                }

                return namespaceFltr;
            }).toList());
        } else {
            String finalValue = value;
            this.searchStacks = new ArrayList<>(filtered.stream()
                    .filter(stack ->
                            stack.getFirst().getDisplayName()
                                    .getString().toLowerCase(Locale.ROOT)
                                    .contains(finalValue.toLowerCase(Locale.ROOT)))
                    .toList());
        }
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

    public String getActiveFilter() {
        return this.searchValue;
    }
}
