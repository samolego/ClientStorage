package org.samo_lego.clientstorage.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;

public class EventHandler {

    public static BlockHitResult lastHitResult = null;

    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(pos);

            if (blockState.getBlock() == Blocks.CRAFTING_TABLE) {
                lastHitResult = hitResult;
                if (!INTERACTION_Q.isEmpty()) INTERACTION_Q.clear();
                System.out.println("Crafting system search.");

                world.getChunkAt(player.blockPosition()).getBlockEntities().forEach((position, blockEntity) -> { // todo cache
                    // Check if within reach
                    if (blockEntity instanceof Container && player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position)) < MAX_INTERACTION_DISTANCE) {
                        System.out.println("Found " + position + ", empty: " + ((Container) blockEntity).isEmpty());
                        //if (((Container) blockEntity).isEmpty()) {
                            BlockPos blockPos = blockEntity.getBlockPos();
                            BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);

                            INTERACTION_Q.addLast(blockPos);
                            System.out.println("q: " + INTERACTION_Q);
                            ((LocalPlayer) player).connection.send(new ServerboundUseItemOnPacket(hand, result, 0));
                            int containerId = player.containerMenu.containerId;
                            ((LocalPlayer) player).connection.send(new ServerboundContainerClosePacket(containerId));
                        /*} else {
                            // We already know the items
                            if (Minecraft.getInstance().screen instanceof CraftingScreen) {
                                var menu = Minecraft.getInstance().player.containerMenu;
                                ((IRemoteCrafting) menu).refreshRemoteInventory();
                            }
                        }*/
                    }
                });

            }
        } else {
            System.out.println("Not client side.");
        }
        return InteractionResult.PASS;
    }
}
