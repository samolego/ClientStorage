package org.samo_lego.clientstorage.fabric_client.mixin.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MItemRenderer {

    @Inject(method = "tryRenderGuiItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V"))
    private void renderItemSuggestion(LivingEntity livingEntity, ItemStack itemStack, int i, int j, int k, int l, CallbackInfo ci) {
        // Render partially transparent item if it's a "suggested item" (count 0)
        if (itemStack.getCount() == 0) {
            final var poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            GuiComponent.fill(poseStack, i, j, i + 16, j + 16, 0x80FFFFFF);
            poseStack.popPose();
        }
    }

    @ModifyVariable(method = "tryRenderGuiItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At("LOAD"), ordinal = 0, argsOnly = true)
    private ItemStack modifyStack(ItemStack stack) {
        if (stack.isEmpty()) {
            if (Minecraft.getInstance().player.hasContainerOpen()) {
                var fake = new ItemStack(Items.DIAMOND) {
                    @Override
                    public boolean isEmpty() {
                        return false;
                    }
                };
                fake.setCount(0);
                return fake;
            }
        }
        return stack;
    }
}
