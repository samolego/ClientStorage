package org.samo_lego.clientstorage.fabric_client.compatibility;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;
import org.samo_lego.clientstorage.fabric_client.compatibility.network.ServerboundItemStorePacket;
import org.samo_lego.clientstorage.fabric_client.compatibility.network.ServerboundItemTakePacket;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;

public class StashContainer extends BlockEntity {
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
     *
     * @param slotIx slot to transfer item from.
     */
    public void putItem(int slotIx) {
        var player = Minecraft.getInstance().player;
        player.connection.send(ServerboundItemStorePacket.newPacket(this.id, slotIx));
    }

    public void addAllItems() {
        this.items.forEach(itemStack -> {
            if (!itemStack.isEmpty()) {
                RemoteInventory.getInstance().addStack(itemStack);
            }
        });
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
