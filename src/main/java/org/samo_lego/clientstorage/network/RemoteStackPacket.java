package org.samo_lego.clientstorage.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.casts.IRemoteStack;

public class RemoteStackPacket {

    public static void take(ItemStack remote) {
        var player = Minecraft.getInstance().player;

        var remoteStack = (IRemoteStack) (Object) remote;

        // Send interaction packet to server
        BlockEntity blockEntity = remoteStack.cs_getContainer();
        BlockPos blockPos = blockEntity.getBlockPos();
        BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);

        //var currentScreen = Minecraft.getInstance().screen;
        player.closeContainer();

        int containerId = player.containerMenu.containerId;

        // Close crafting
        player.connection.send(new ServerboundContainerClosePacket(containerId));

        // Open container
        player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, 0));
        ItemStack transferredStack = remote.copy();


        var map = new Int2ObjectOpenHashMap<ItemStack>();
        map.put(remoteStack.cs_getSlotId(), transferredStack.copy());

        //var transferPacket = new ServerboundContainerClickPacket(containerId, stateId, remoteStack.cs_getSlotId(), 0, ClickType.QUICK_MOVE, transferredStack.copy(), map);
        //player.connection.send(transferPacket);

        // Close container
        player.connection.send(new ServerboundContainerClosePacket(containerId));

        //Minecraft.getInstance().screen = currentScreen;
    }

}
