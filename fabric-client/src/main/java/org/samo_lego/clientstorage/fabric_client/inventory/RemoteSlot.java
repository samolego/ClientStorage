package org.samo_lego.clientstorage.fabric_client.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;
import org.samo_lego.clientstorage.fabric_client.compatibility.StashContainer;
import org.samo_lego.clientstorage.fabric_client.event.EventHandler;
import org.samo_lego.clientstorage.fabric_client.util.PlayerLookUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.lastCraftingHit;


public class RemoteSlot extends Slot {
    public RemoteSlot(RemoteInventory inventory, int slot, int x, int y) {
        super(inventory, slot, x, y);
    }

    private static void setCarried(int pickSlot, ItemStack stack) {
        final var player = Minecraft.getInstance().player;
        // Set carried stack at cursor
        //player.containerMenu.clicked(pickSlot, 0, ClickType.PICKUP, player);

        // This is cursed but I couldn't manage to find the right place to execute this
        CompletableFuture.delayedExecutor(150, TimeUnit.MILLISECONDS).execute(() -> {
            Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, pickSlot, 0, ClickType.PICKUP, player);
        });

        // Works but doesn't update the cursor
        /*final var map = new Int2ObjectOpenHashMap<ItemStack>();
        map.clear();
        map.put(pickSlot, ItemStack.EMPTY);

        var clickPacket = new ServerboundContainerClickPacket(player.containerMenu.containerId + 2, 1, pickSlot, 0, ClickType.PICKUP, stack, map);
        player.connection.send(clickPacket);

        player.containerMenu.setCarried(stack);
        player.inventoryMenu.setCarried(stack);
        player.getInventory().removeItemNoUpdate(pickSlot);
        System.out.println("Set carried stack to " + stack);*/
    }

    public void onTake(ItemStack stack, ClickType clickType) {
        IRemoteStack remoteStack = (IRemoteStack) stack;

        if (remoteStack.cs_getContainer() != null) {
            // Get first free slot in player's inventory (to move items to)
            var player = Minecraft.getInstance().player;

            int freeSlot = player.getInventory().getSlotWithRemainingSpace(stack);
            if (freeSlot == -1) {
                NonNullList<ItemStack> items = player.getInventory().items;
                for (int i = items.size() - 1; i >= 0; --i) {
                    if (items.get(i).isEmpty()) {
                        freeSlot = i + 9;
                        break;
                    }
                }
            }

            if (freeSlot == -1) {
                return;
            }

            // Send interaction packet to server
            BlockEntity blockEntity = remoteStack.cs_getContainer();
            BlockPos blockPos = blockEntity.getBlockPos();

            // Remove item from client container
            ((Container) blockEntity).setItem(remoteStack.cs_getSlotId(), ItemStack.EMPTY);

            if (blockEntity instanceof StashContainer stash) {
                // Different item take logic for stashes
                stash.takeItem(stack, clickType == ClickType.QUICK_MOVE ? freeSlot : -1);
                return;
            }

            int containerId = player.containerMenu.containerId;

            // Close crafting
            player.connection.send(new ServerboundContainerClosePacket(containerId));

            // Helps us ignore GUI open packet later then
            ((ICSPlayer) player).cs_setAccessingItem(true);
            // Open container
            var hitResult = PlayerLookUtil.raycastTo(blockPos);
            player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0));

            var map = new Int2ObjectOpenHashMap<ItemStack>();
            map.put(remoteStack.cs_getSlotId(), ItemStack.EMPTY);
            map.put(freeSlot, stack.copy());

            // todo if 1 same item already in inv, it merges together
            var packet = new ServerboundContainerClickPacket(containerId + 1, 1, remoteStack.cs_getSlotId(), 0, ClickType.QUICK_MOVE, ItemStack.EMPTY, map);
            // Send transfer item packet
            player.connection.send(packet);

            ((IRemoteStack) stack).cs_clearData();

            // Close container
            player.connection.send(new ServerboundContainerClosePacket(containerId + 1));

            // Open crafting again
            player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, containerId));

            if (clickType != ClickType.QUICK_MOVE) {
                // Set item to be picked up by the mouse todo
                final int pickSlot = freeSlot;
                EventHandler.supplyAction(() -> setCarried(pickSlot, stack));
            }
        }
    }

    public void onPut(ItemStack stack) {
        IRemoteStack remote = (IRemoteStack) stack;
        remote.cs_transfer2Remote();
    }
}
