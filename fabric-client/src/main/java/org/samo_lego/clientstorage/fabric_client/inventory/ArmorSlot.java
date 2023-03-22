package org.samo_lego.clientstorage.fabric_client.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.fabric_client.ClientStorageFabric;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.network.PacketUtil;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;

import java.util.Optional;

import static org.samo_lego.clientstorage.fabric_client.event.ContainerDiscovery.lastCraftingHit;

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

    public static int getSlotIndex(int index) {
        if (index == 4) return 45;  // Offhand
        return index + 5;  // Armor slots
    }

    public void onClick(ClickType _click) {
        System.out.println("ArmourSlot#onClick " + this.container.getItem(this.getContainerSlot()) + " " + this.getContainerSlot());

        final var player = Minecraft.getInstance().player;

        // Send inventory close packet
        PacketUtil.closeCurrentScreen();
        ((ICSPlayer) player).cs_setAccessingItem(true);

        // Transfer item to player inventory
        // Get first empty slot in player inventory
        int emptySlot = -1;
        final var items = player.getInventory().items;
        for (int i = 9; i < items.size(); ++i) {
            if (items.get(i).isEmpty()) {
                emptySlot = i;
                break;
            }
        }

        if (emptySlot != -1) {
            // Send shift click packet
            int slotNum = getSlotIndex(this.getContainerSlot());
            final var map = new Int2ObjectArrayMap<ItemStack>();
            map.put(emptySlot, this.getItem());
            map.put(slotNum, ItemStack.EMPTY);

            player.connection.send(new ServerboundContainerClickPacket(0, 1, slotNum, 0, ClickType.QUICK_MOVE, ItemStack.EMPTY, map));
            // Clear the slot clientside
            this.container.setItem(this.getContainerSlot(), ItemStack.EMPTY);
        }

        PacketUtil.closeInventory();
        ((ICSPlayer) player).cs_setAccessingItem(false);

        // Send inventory open packet
        ((ICSPlayer) player).cs_getLastInteractedContainer().ifPresentOrElse(InteractableContainer::cs_sendInteractionPacket, () -> {
            if (lastCraftingHit != null) {
                // Send open crafting packet
                player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, 0));
            } else {
                ClientStorageFabric.tryLog("Could not find last interacted container!", ChatFormatting.RED);
            }
        });
    }
}
