package org.samo_lego.clientstorage.fabric_client.mixin.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class MAbstractContainerScreen extends Screen {

    @Unique
    private static final String UNITS = "kMGTPE";  // Overkill with exa, but whatever

    @Unique
    private Slot slot;

    @Unique
    boolean renderWithSmallText;

    protected MAbstractContainerScreen(Component component) {
        super(component);
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void onSlotRender(PoseStack matrices, Slot slot, CallbackInfo ci) {
        this.slot = slot;
    }

    @ModifyVariable(method = "renderSlot", at = @At(value = "STORE"))
    private String changeItemCountLabel(String label) {
        if (this.slot instanceof RemoteSlot) {
            // Custom count label
            int count = this.slot.getItem().getCount();

            this.renderWithSmallText = false; // Use normal-sized text for small numbers

            // Use physics notation for large numbers
            if (count >= 1000) {
                this.renderWithSmallText = true; // Use small text for large numbers
                int exp = (int) (Math.log(count) / Math.log(1000));
                return String.format("%.1f%c", count / Math.pow(1000, exp), UNITS.charAt(exp - 1));
            }
        }
        return label;
    }

    @Redirect(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void renderGuiItemWithDifferentTextSize(ItemRenderer itemRenderer, Font fontRenderer, ItemStack stack, int x, int y, @Nullable String countLabel) {
        if(renderWithSmallText) {
            itemRenderer.renderGuiItem(stack, x, y); // Render Normally without count label
            PoseStack textMatrixStack = new PoseStack(); // Create new matrix stack for transforming text size
            textMatrixStack.scale(0.5F, 0.5F, 1); // Scale matrix stack to make text smaller
            textMatrixStack.translate(0, 0, itemRenderer.blitOffset + itemRenderer.ITEM_COUNT_BLIT_OFFSET); // Offset text z position so that it is in front of item
            fontRenderer.drawShadow(textMatrixStack, countLabel, x * 2 + 31 - fontRenderer.width(countLabel), y * 2 + 23, ChatFormatting.WHITE.getColor()); // Render count label
        } else {
            itemRenderer.renderGuiItemDecorations(fontRenderer, stack, x, y, countLabel); // Render Normally
        }
    }
}
