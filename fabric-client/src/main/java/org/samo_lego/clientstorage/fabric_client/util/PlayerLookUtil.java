package org.samo_lego.clientstorage.fabric_client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerLookUtil {
    public static BlockHitResult raycastTo(Vec3 target) {
        var player = Minecraft.getInstance().player;
        var from = player.getEyePosition();
        return player.getLevel().clip(new ClipContext(from, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }


    public static Direction getBlockDirection(Vec3 target) {
        final var player = Minecraft.getInstance().player;

        if (target.y() - 1 > player.getEyeY()) {
            return Direction.DOWN;
        } else if (target.y() + 1 < player.getEyeY()) {
            return Direction.UP;
        } else {
            // Get Y rotation from vector between player and target
            var vec = new Vec3(target.x() - player.getX(), 0, target.z() - player.getZ());
            var yaw = (float) Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90;

            return Direction.fromYRot(yaw);
        }
    }

    public static void lookAt(BlockPos blockPos) {
        var player = Minecraft.getInstance().player;

        // Look at container
        // Add the blockpos and playerpos difference to yRot
        double xDiff = blockPos.getX() - player.getX();
        double zDiff = blockPos.getZ() - player.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(zDiff, xDiff));
        float pitch = (float) Math.toDegrees(Math.atan2(blockPos.getY() - player.getY(), Math.sqrt(xDiff * xDiff + zDiff * zDiff)));

        player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.isOnGround()));
    }
}
