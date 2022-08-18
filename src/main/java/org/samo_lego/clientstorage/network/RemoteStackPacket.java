package org.samo_lego.clientstorage.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.samo_lego.clientstorage.mixin.accessor.AClientLevel;

public class RemoteStackPacket {
    private static boolean accessingItem = false;

    public static boolean isAccessingItem() {
        return accessingItem;
    }

    public static void take(ItemStack remote) {
        var player = Minecraft.getInstance().player;

        var remoteStack = (IRemoteStack) (Object) remote;

        // Send interaction packet to server
        BlockEntity blockEntity = remoteStack.cs_getContainer();
        BlockPos blockPos = blockEntity.getBlockPos();
        BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);

        player.closeContainer();

        int containerId = player.containerMenu.containerId;

        // Close crafting
        player.connection.send(new ServerboundContainerClosePacket(containerId));

        // Helps us ignore GUI open packet later then
        //accessingItem = true;
        // todo can be 0 ???
        int i = 0;
        try (BlockStatePredictionHandler blockStatePredictionHandler = ((AClientLevel) Minecraft.getInstance().level).cs_predHandler().startPredicting()) {
            i = blockStatePredictionHandler.currentSequence();
        }

        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, i));

        // Open container
        //player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, 0));
        ItemStack transferredStack = remote.copy();


        var map = new Int2ObjectOpenHashMap<ItemStack>();
        map.put(remoteStack.cs_getSlotId(), transferredStack.copy());

        //var transferPacket = new ServerboundContainerClickPacket(containerId, stateId, remoteStack.cs_getSlotId(), 0, ClickType.QUICK_MOVE, transferredStack.copy(), map);
        //player.connection.send(transferPacket);

        // Close container
        //player.connection.send(new ServerboundContainerClosePacket(containerId));

        // Open crafting again
        //player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastHitResult, i + 1));
        accessingItem = false;
        //Minecraft.getInstance().screen = currentScreen;
    }

}
