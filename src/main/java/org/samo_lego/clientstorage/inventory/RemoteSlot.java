package org.samo_lego.clientstorage.inventory;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.samo_lego.clientstorage.network.RemoteStackPacket;

import static org.samo_lego.clientstorage.event.EventHandler.REMOTE_INV;

public class RemoteSlot extends Slot {
    public RemoteSlot(RemoteInventory inventory, int slot, int x, int y) {
        super(inventory, slot, x, y);
    }

    public void onTake(LocalPlayer player, ItemStack stack) {
        IRemoteStack remote = (IRemoteStack) (Object) stack;

        if (remote.cs_getContainer() != null) {
            RemoteStackPacket.take(stack);
            REMOTE_INV.removeItemNoUpdate(this.getContainerSlot()).setCount(0);
        }
    }

    public void onPut(LocalPlayer player, ItemStack stack) {
        IRemoteStack remote = (IRemoteStack) (Object) stack;
        if (remote.cs_getContainer() != null) {
            RemoteStackPacket.put(stack);
        }
    }
}
