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

import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;

public class EventHandler {

    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if(world.isClientSide()) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(pos);

            if(blockState.getBlock() == Blocks.CRAFTING_TABLE) {
                if(!INTERACTION_Q.isEmpty())
                    INTERACTION_Q.clear();
                System.out.println("Crafting system search.");

                world.getChunkAt(player.blockPosition()).getBlockEntities().forEach((position, blockEntity) -> { //todo cache
                    // Check if within reach
                    if(position.closerThan(player.position(), 5.0D) && blockEntity instanceof Container) {
                        System.out.println("Found "+ position + ", empty: " + ((Container) blockEntity).isEmpty());
                        //if(((Inventory) blockEntity).isEmpty()) {
                            Vec3 xyz = new Vec3(position.getX(), position.getY(), position.getZ());
                            BlockHitResult result = new BlockHitResult(xyz, Direction.UP, pos, false);

                            INTERACTION_Q.add(pos);
                            ((LocalPlayer) player).connection.send(new ServerboundUseItemOnPacket(hand, result));
                            int containerId = player.containerMenu.containerId;
                            ((LocalPlayer) player).connection.send(new ServerboundContainerClosePacket(containerId));
                        //}
                    }
                });

            }
        }
        return InteractionResult.PASS;
    }
}
