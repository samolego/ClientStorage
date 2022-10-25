package org.samo_lego.clientstorage.fabric_client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.ALevelRenderer;

import java.util.HashSet;
import java.util.Set;

import static org.samo_lego.clientstorage.common.ClientStorage.MOD_ID;


/**
 * Taken from <a href="https://github.com/NotSoEpic/shimmer/blob/master/src/main/java/com/dindcrzy/shimmer/xray/Renderer.java">Shimmer mod</a>
 */
public class ESPRender {

    public static final Set<BlockPos> BLOCK_ESPS = new HashSet<>();
    private static final ModelPart.Cube CUBE = new ModelPart.Cube(0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, false, 0, 0);
    private static final RenderType RENDER_LAYER = RenderType.outline(new ResourceLocation(MOD_ID, "textures/none.png"));
    private static boolean isAlreadyRenderingOutline = false;

    public static void render(PoseStack matrices, Camera camera, OutlineBufferSource vertexConsumers) {
        Vec3 pos = camera.getPosition();
        matrices.pushPose();
        matrices.translate(-pos.x, -pos.y, -pos.z);
        renderBlockOutline(matrices, pos, vertexConsumers);
        matrices.popPose();
    }

    public static void renderBlockOutline(PoseStack matrices, Vec3 playerPos, OutlineBufferSource vertexConsumers) {
        vertexConsumers.setColor(255, 255, 255, 255);

        for (BlockPos pos : BLOCK_ESPS) {
            double squareDist = playerPos.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
            if (squareDist > 8 * 8) {

                continue;
            }

            matrices.pushPose();
            matrices.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            matrices.pushPose();
            matrices.translate(-0.5, -0.5, -0.5);

            ResourceLocation textureId = new ResourceLocation("minecraft", "none.png");
            CUBE.compile(matrices.last(), vertexConsumers.getBuffer(RenderType.outline(textureId)), 0, OverlayTexture.WHITE_OVERLAY_V, 0, 0, 0, 0);
            matrices.popPose();
            matrices.popPose();
        }
    }

    public static void onRender(WorldRenderContext context) {
        var worldRenderer = (ALevelRenderer) context.worldRenderer();

        if (!BLOCK_ESPS.isEmpty()) {
            if (!isAlreadyRenderingOutline) {
                worldRenderer.getEntityEffect().process(context.tickDelta());
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }
            render(context.matrixStack(), context.camera(), worldRenderer.getRenderBuffers().outlineBufferSource());
        }
        isAlreadyRenderingOutline = false;
    }

    public static void markPos(BlockPos blockPos) {
        if (BLOCK_ESPS.contains(blockPos)) {
            BLOCK_ESPS.remove(blockPos);
        } else {
            BLOCK_ESPS.add(blockPos);
        }
    }
}
