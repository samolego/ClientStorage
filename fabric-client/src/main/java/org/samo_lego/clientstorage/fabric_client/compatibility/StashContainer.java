package org.samo_lego.clientstorage.fabric_client.compatibility;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;
import org.samo_lego.clientstorage.fabric_client.compatibility.network.packet.ServerboundItemStorePacket;
import org.samo_lego.clientstorage.fabric_client.compatibility.network.packet.ServerboundItemTakePacket;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;

public class StashContainer extends BaseContainerBlockEntity {
    private final ResourceLocation id;
    private final Iterable<ItemStack> items;

    public StashContainer(ResourceLocation id, Iterable<ItemStack> items) {
        super(BlockEntityType.BARREL, BlockPos.ZERO, Blocks.BARREL.defaultBlockState());
        this.id = id;

        items.forEach(itemStack -> {
            if (!itemStack.isEmpty()) {
                IRemoteStack stack = (IRemoteStack) itemStack;
                stack.cs_setContainer(this);
            }
        });

        this.items = items;
    }


    /**
     * Takes item from stash and stores it in player's inventory at given slot.
     *
     * @param stack      stack to take.
     * @param freeSlotIx slot to store item in.
     */
    public void takeItem(ItemStack stack, int freeSlotIx) {
        var player = Minecraft.getInstance().player;
        player.connection.send(ServerboundItemTakePacket.newPacket(this.id, freeSlotIx, stack));
    }

    /**
     * Stores item at provided slot (in player's inventory) to stash.
     * Also adds the item to the stash's inventory.
     *
     * @param slotIx slot to transfer item from.
     */
    public void putItem(ItemStack stack, int slotIx) {
        var player = Minecraft.getInstance().player;
        player.connection.send(ServerboundItemStorePacket.newPacket(this.id, slotIx));

        ((IRemoteStack) stack).cs_setContainer(this);
        RemoteInventory.getInstance().addStack(stack);
    }

    public void addAllItems() {
        this.items.forEach(itemStack -> {
            if (!itemStack.isEmpty()) {
                RemoteInventory.getInstance().addStack(itemStack);
            }
        });
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Stash");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return null;
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
    }

    /*@Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof StashContainer other) {
            return this.id.equals(other.id);

        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }*/
}
