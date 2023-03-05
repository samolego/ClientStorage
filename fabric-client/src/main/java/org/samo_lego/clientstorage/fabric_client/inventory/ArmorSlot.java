package org.samo_lego.clientstorage.fabric_client.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.network.PacketGame;

import java.util.Optional;

public class ArmorSlot extends Slot {
    public ArmorSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public void onTake(Player player, ItemStack itemStack) {
        super.onTake(player, itemStack);
        System.out.println("ArmourSlot#onTake " + this.container.getItem(this.index) + " " + this.index);
    }

    @Override
    public Optional<ItemStack> tryRemove(int i, int j, Player player) {
        System.out.println("ArmourSlot#tryRemove " + this.container.getItem(this.index) + " " + this.index);
        return super.tryRemove(i, j, player);
    }

    @Override
    public ItemStack safeTake(int i, int j, Player player) {
        System.out.println("ArmourSlot#safeTake " + this.container.getItem(this.index) + " " + this.index);
        return super.safeTake(i, j, player);
    }

    public void onClick(ClickType click) {
        System.out.println("ArmourSlot#onClick " + this.container.getItem(this.index) + " " + this.index);

        final var player = Minecraft.getInstance().player;
        // Send inventory close packet
        PacketGame.closeCurrentScreen();
        ((ICSPlayer) player).cs_setAccessingItem(true);

        // Transfer item to player inventory
        // Send shift click packet
        player.connection.send(new ServerboundContainerClickPacket(0, this.index, 0, 0, ClickType.QUICK_MOVE, ItemStack.EMPTY, Int2ObjectMaps.emptyMap()));

        ((ICSPlayer) player).cs_setAccessingItem(false);

        // Send inventory open packet
        ((ICSPlayer) player).cs_getLastInteractedContainer().get().cs_sendInteractionPacket();
    }
}
