package org.samo_lego.clientstorage.event;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;

public class EventHandler {

    public static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if(world.isClient()) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(pos);

            if(!INTERACTION_Q.isEmpty())
                INTERACTION_Q.clear();

            if(blockState.getBlock() == Blocks.CRAFTING_TABLE) {
                System.out.println("Crafting system search.");

                world.blockEntities.forEach(blockEntity -> { //todo cache
                    // Check if within reach
                    if(blockEntity.getPos().isWithinDistance(player.getPos(), 5.0D) && blockEntity instanceof Inventory) {
                        System.out.println("Found "+ blockEntity.getPos() + ", empty: " + ((Inventory) blockEntity).isEmpty());
                        //if(((Inventory) blockEntity).isEmpty()) {
                            Vec3d vec3d = new Vec3d(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ());
                            BlockHitResult result = new BlockHitResult(vec3d, Direction.UP, blockEntity.getPos(), false);

                            INTERACTION_Q.add(blockEntity.getPos());
                            ((ClientPlayerEntity) player).networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result));
                            ((ClientPlayerEntity) player).networkHandler.sendPacket(new CloseHandledScreenC2SPacket());
                        //}
                    }
                });

            }
        }
        return ActionResult.PASS;
    }
}
