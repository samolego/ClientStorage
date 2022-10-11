package org.samo_lego.clientstorage.fabric_client.inventory;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;


public class RemoteSlot extends Slot {
    public RemoteSlot(RemoteInventory inventory, int slot, int x, int y) {
        super(inventory, slot, x, y);
    }

    public void onTake(ItemStack stack) {
        IRemoteStack remote = (IRemoteStack) stack;

        if (remote.cs_getContainer() != null) {
            remote.cs_transferToPlayer(this);
            RemoteInventory.getInstance().removeItemNoUpdate(this.getContainerSlot()).setCount(0);
        }
    }

    public void onPut(ItemStack stack) {
        IRemoteStack remote = (IRemoteStack) stack;
        remote.put();
    }
}
