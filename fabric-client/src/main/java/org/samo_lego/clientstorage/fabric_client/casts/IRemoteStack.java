package org.samo_lego.clientstorage.fabric_client.casts;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteSlot;
import org.samo_lego.clientstorage.fabric_client.util.PlayerLookUtil;

import java.util.concurrent.atomic.AtomicReference;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;
import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.FREE_SPACE_CONTAINERS;
import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.lastCraftingHit;

public interface IRemoteStack {

    int CRAFTING_SLOT_OFFSET = 10;

    int cs_getSlotId();

    void cs_setSlotId(int slotId);

    BlockEntity cs_getContainer();

    void cs_setContainer(BlockEntity parent);

    default void cs_clearData() {
        this.cs_setContainer(null);
        this.cs_setSlotId(-1);
    }


    static ItemStack fromStack(ItemStack stack, BlockEntity blockEntity, int slot) {
        // Add properties to ItemStack via IRemoteStack interface
        IRemoteStack remote = (IRemoteStack) stack;
        remote.cs_setSlotId(slot);
        remote.cs_setContainer(blockEntity);

        return stack;
    }


    default void cs_transferToPlayer(RemoteSlot remoteSlot) {
        // Get first free slot in player's inventory (to move items to)
        var player = Minecraft.getInstance().player;
        var remote = (ItemStack) this;

        int freeSlot = player.getInventory().getSlotWithRemainingSpace(remote);
        if (freeSlot == -1) {
            freeSlot = player.getInventory().getFreeSlot();
        }


        if (freeSlot == -1) {
            return;
        }


        var remoteStack = (IRemoteStack) remote;

        // Send interaction packet to server
        BlockEntity blockEntity = remoteStack.cs_getContainer();
        BlockPos blockPos = blockEntity.getBlockPos();

        var result = PlayerLookUtil.raycastTo(blockPos);
        // Whether block is in "reach", not behind another block
        boolean behindWall = !result.getBlockPos().equals(blockPos);

        if (behindWall) {
            if (config.lookThroughBlocks()) {
                // Todo get right block face if hitting through blocks
                Direction nearest = PlayerLookUtil.getBlockDirection(blockPos);
                result = new BlockHitResult(Vec3.atCenterOf(blockPos), nearest, blockPos, false);
            } else {
                // This container is behind a block, so we can't open it
                player.sendSystemMessage(Component.literal("Container is behind a block!").withStyle(ChatFormatting.DARK_RED));
                return;
            }
        }

        // Remove item from client container
        ((Container) blockEntity).setItem(remoteStack.cs_getSlotId(), ItemStack.EMPTY);
        RemoteInventory.getInstance().removeItemNoUpdate(remoteSlot.index);

        int containerId = player.containerMenu.containerId;

        // Close crafting
        player.connection.send(new ServerboundContainerClosePacket(containerId));


        // Helps us ignore GUI open packet later then
        ((ICSPlayer) player).cs_setAccessingItem(true);
        // Open container
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, 0));

        ItemStack transferredStack = remote.copy();
        ((IRemoteStack) transferredStack).cs_clearData();

        var map = new Int2ObjectOpenHashMap<ItemStack>();
        map.put(remoteStack.cs_getSlotId(), ItemStack.EMPTY);
        map.put(freeSlot, transferredStack);

        // todo if 1 same item already in inv, it merges together
        var transferPacket = new ServerboundContainerClickPacket(containerId + 1, 1, remoteStack.cs_getSlotId(), 0, ClickType.QUICK_MOVE, transferredStack, map);
        // Send transfer item packet
        player.connection.send(transferPacket);

        // Close container
        player.connection.send(new ServerboundContainerClosePacket(containerId + 1));

        // Open crafting again
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, containerId));

        // Set item to be picked up by the mouse todo
    }


    default void put() {
        // Get first free slot in player's inventory (to move items to)
        var player = Minecraft.getInstance().player;

        AtomicReference<BlockHitResult> blockHit = new AtomicReference<>();

        var container = FREE_SPACE_CONTAINERS.entrySet().stream().filter(blockPosIntegerEntry -> {
            var blockPos = blockPosIntegerEntry.getKey();

            blockHit.set(PlayerLookUtil.raycastTo(blockPos));
            // Whether block is in "reach", not behind another block
            boolean behindWall = !blockHit.get().getBlockPos().equals(blockPos);

            if (behindWall) {
                if (config.lookThroughBlocks()) {
                    Direction nearest = PlayerLookUtil.getBlockDirection(blockPos);
                    blockHit.set(new BlockHitResult(Vec3.atCenterOf(blockPos), nearest, blockPos, false));
                    return true;
                }

                // This container is behind a block, so we can't open it
                return false;
            }
            return true;
        }).findAny();


        if (container.isEmpty()) {
            player.sendSystemMessage(Component.literal("No free space containers found.").withStyle(ChatFormatting.RED));
            return;
        }

        final ItemStack stack = (ItemStack) this;

        int freeSlot = -1;
        NonNullList<Slot> slots = player.containerMenu.slots;
        for (int i = CRAFTING_SLOT_OFFSET; i < slots.size(); ++i) {
            var slot = slots.get(i);

            if (!slot.hasItem()) {
                freeSlot = i;
                break;
            }
        }

        if (freeSlot == -1) {
            return;
        }

        int containerId = player.containerMenu.containerId;

        // "Put" item in free slot
        var map = new Int2ObjectOpenHashMap<ItemStack>();
        map.put(freeSlot, stack);
        var transferPacket = new ServerboundContainerClickPacket(containerId, 1, freeSlot, 0, ClickType.PICKUP, ItemStack.EMPTY, map);
        player.connection.send(transferPacket);

        // Close crafting
        player.connection.send(new ServerboundContainerClosePacket(containerId));

        // Helps us ignore GUI open packet later then
        ((ICSPlayer) player).cs_setAccessingItem(true);
        // Open container
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHit.get(), 0));

        ItemStack transferredStack = stack.copy();


        // Get first container slot
        var pos = container.get().getKey();
        Container storage = (Container) player.getLevel().getBlockEntity(pos);


        // Free slot in player's inv now has different index due to new container being open ...
        freeSlot = freeSlot - CRAFTING_SLOT_OFFSET + storage.getContainerSize();

        map.clear();
        map.put(freeSlot, ItemStack.EMPTY);

        // Get first free slot in container
        int containerSlot;
        for (containerSlot = 0; containerSlot < storage.getContainerSize(); ++containerSlot) {
            if (storage.getItem(containerSlot).isEmpty()) {
                map.put(containerSlot, transferredStack);
                break;
            }
        }

        transferPacket = new ServerboundContainerClickPacket(containerId + 1, 1, freeSlot, 0, ClickType.QUICK_MOVE, transferredStack, map);
        // Send transfer item packet
        player.connection.send(transferPacket);

        // Close container
        player.connection.send(new ServerboundContainerClosePacket(containerId + 1));

        // Open crafting again
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, containerId));

        // Add to remote inventory
        ItemStack transfered = stack.copy();
        ((IRemoteStack) transfered).cs_setSlotId(containerSlot);

        ((IRemoteStack) transfered).cs_setContainer((BlockEntity) storage);
        RemoteInventory.getInstance().addStack(transfered);

        storage.setItem(containerSlot, transfered);
    }
}
