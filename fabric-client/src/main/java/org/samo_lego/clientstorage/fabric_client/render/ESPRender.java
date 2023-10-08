package org.samo_lego.clientstorage.fabric_client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.AEntity;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.ALevelRenderer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Taken from <a href="https://github.com/NotSoEpic/shimmer/blob/master/src/main/java/com/dindcrzy/shimmer/xray/Renderer.java">Shimmer mod</a>
 * <p>
 * Takes care of rendering block / entity outlines to see the item origin.
 * </p>
 */
public class ESPRender {

    private static final ModelPart.Cube CUBE = new ModelPart.Cube(0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, false, 0, 0, Set.of(Direction.values()));
    private static final RenderType RENDER_TYPE = RenderType.outline(new ResourceLocation("textures/misc/white.png"));

    /**
     * Stores current entities that are "fake" glowing.
     */
    private static final Set<Entity> ENTITY_ESPS = ConcurrentHashMap.newKeySet();

    /**
     * Stores current block positions that are "fake" glowing.
     */
    private static final Set<BlockPos> BLOCK_ESPS = ConcurrentHashMap.newKeySet();

    public static void render(PoseStack matrices, Camera camera, OutlineBufferSource vertexConsumers) {
        Vec3 pos = camera.getPosition();
        matrices.pushPose();
        matrices.translate(-pos.x, -pos.y, -pos.z);
        renderBlockOutlines(matrices, pos, vertexConsumers);
        matrices.popPose();
    }

    public static void renderBlockOutlines(PoseStack matrices, Vec3 playerPos, OutlineBufferSource vertexConsumers) {
        vertexConsumers.setColor(255, 255, 255, 255);

        for (BlockPos pos : BLOCK_ESPS) {
            double squareDist = playerPos.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
            if (squareDist > 8 * 8) continue;

            matrices.pushPose();
            matrices.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            matrices.pushPose();
            matrices.translate(-0.5, -0.5, -0.5);

            CUBE.compile(matrices.last(), vertexConsumers.getBuffer(RENDER_TYPE), 0, OverlayTexture.WHITE_OVERLAY_V, 0, 0, 0, 0);
            matrices.popPose();
            matrices.popPose();
        }
    }

    public static void onRender(WorldRenderContext context) {
        var worldRenderer = (ALevelRenderer) context.worldRenderer();

        if (!BLOCK_ESPS.isEmpty()) {
            worldRenderer.getEntityEffect().process(context.tickDelta());
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            render(context.matrixStack(), context.camera(), worldRenderer.getRenderBuffers().outlineBufferSource());
        }
    }

    /**
     * Makes block glow.
     *
     * @param blockPos block position to mark as glowing.
     */
    public static void markBlock(BlockPos blockPos) {
        BLOCK_ESPS.add(blockPos);
    }


    /**
     * Clears all block positions
     * that outline is being rendered for.
     */
    public static void reset() {
        synchronized (BLOCK_ESPS) {
            BLOCK_ESPS.clear();
            for (Entity entity : ENTITY_ESPS) {
                ((AEntity) entity).cs_setSharedFlag(AEntity.FLAG_GLOWING(), false);
            }
            ENTITY_ESPS.clear();
        }
    }

    /**
     * Stops rendering outline for given block cs_position.
     *
     * @param pos block cs_position
     */
    public static void removeBlockPos(BlockPos pos) {
        BLOCK_ESPS.remove(pos);
    }

    public static void markEntity(Entity entity) {
        if (!entity.isCurrentlyGlowing()) {
            ((AEntity) entity).cs_setSharedFlag(AEntity.FLAG_GLOWING(), true);  // mark entity glowing clientside as well
            ENTITY_ESPS.add(entity);
        }
    }

    public static void removeEntity(Entity entity) {
        if (ENTITY_ESPS.contains(entity)) {
            ((AEntity) entity).cs_setSharedFlag(AEntity.FLAG_GLOWING(), false);
            ENTITY_ESPS.remove(entity);
        }
    }
}
