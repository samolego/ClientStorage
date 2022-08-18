package org.samo_lego.clientstorage.inventory;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.samo_lego.clientstorage.network.RemoteStackPacket;

public class RemoteSlot extends Slot {
    public RemoteSlot(RemoteInventory inventory, int slot, int x, int y) {
        super(inventory, slot, x, y);
    }

    public void onTake(LocalPlayer player, ItemStack stack) {
        if (!stack.isEmpty()) {
            IRemoteStack remote = (IRemoteStack) (Object) stack;
            assert remote.cs_getContainer() != null;
            RemoteStackPacket.take(stack);
        }
    }
}
