package org.samo_lego.clientstorage.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.samo_lego.clientstorage.network.RemoteStackPacket;

public class RemoteSlot extends Slot {
    public RemoteSlot(RemoteInventory inventory, int slot, int x, int y) {
        super(inventory, slot, x, y);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (!stack.isEmpty()) {
            IRemoteStack remote = (IRemoteStack) (Object) stack;
            assert remote.cs_getContainer() != null;
            RemoteStackPacket.take(stack);
        }
    }
}
