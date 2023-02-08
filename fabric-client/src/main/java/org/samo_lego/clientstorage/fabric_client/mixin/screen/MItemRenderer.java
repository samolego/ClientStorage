package org.samo_lego.clientstorage.fabric_client.mixin.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemRenderer.class)
public abstract class MItemRenderer {

    @Unique
    private boolean enableTransparency;

    /*@Inject(method = "tryRenderGuiItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V",
                    shift = At.Shift.AFTER))
    private void renderItemSuggestion(LivingEntity livingEntity, ItemStack itemStack, int x, int y, int k, int l, CallbackInfo ci) {
        // Render partially transparent item if it's a "suggested item" (count 0)
        if (itemStack.getCount() == 0) {
            // Render item with 50% transparency
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);*/


            /*final var poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.translate(0, 0, 0.0001);
            GuiComponent.fill(poseStack, i, j, i + 16, j + 16, 0x80FFFFFF);
            poseStack.popPose();
            int color1 = 0x77000000; // Semi-transparent black color
            int color2 = 0x00000000; // Transparent color
            GuiComponent.fill(RenderSystem.getModelViewStack(), x, y, x + 16, y + 16, color1);
        }
    }*/

    @Inject(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
    private void onRenderGuiItem(ItemStack stack, int x, int y, BakedModel bakedModel, CallbackInfo ci) {
        this.enableTransparency = stack.getCount() == 0;
    }


    @Inject(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterRenderGuiItem(ItemStack itemStack, int x, int y, BakedModel bakedModel, CallbackInfo ci, PoseStack poseStack) {
        if (this.enableTransparency) {
            poseStack.pushPose();
            GuiComponent.fill(poseStack, x, y, x + 16, y + 16, 0x77000000);
            poseStack.popPose();
        }
    }

    @ModifyArg(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"),
            index = 3)
    private float renderGuiItem(float f) {
        if (this.enableTransparency) {
            return 0.5F;  // 50% transparency
        }
        return 1.0F;
    }
}
