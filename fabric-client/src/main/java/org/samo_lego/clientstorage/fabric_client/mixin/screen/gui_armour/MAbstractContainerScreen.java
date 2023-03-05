package org.samo_lego.clientstorage.fabric_client.mixin.screen.gui_armour;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.casts.IArmorMenu;
import org.samo_lego.clientstorage.fabric_client.inventory.ArmorSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

@Mixin(AbstractContainerScreen.class)
public abstract class MAbstractContainerScreen {

    private final AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected abstract void renderSlot(PoseStack poseStack, Slot slot);

    @Shadow
    protected abstract boolean isHovering(Slot slot, double d, double e);

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lcom/mojang/blaze3d/vertex/PoseStack;FII)V",
                    shift = At.Shift.AFTER))
    private void addArmorSlotBg(PoseStack poseStack, int x, int y, float f, CallbackInfo ci) {
        if (!config.enabled || self instanceof InventoryScreen) {
        }

        // Draw background
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lcom/mojang/blaze3d/vertex/PoseStack;II)V"))
    private void addArmorSlots(PoseStack poseStack, int x, int y, float f, CallbackInfo ci) {
        if (!config.enabled || self instanceof InventoryScreen) return;
        // Draw 5 armor slots
        NonNullList<ArmorSlot> armorSlots = ((IArmorMenu) this.self.getMenu()).cs_getArmorSlots();
        for (Slot slot : armorSlots) {
            if (slot.isActive()) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                this.renderSlot(poseStack, slot);

                if (this.isHovering(slot, x, y)) {
                    this.hoveredSlot = slot;
                    AbstractContainerScreen.renderSlotHighlight(poseStack, slot.x, slot.y, self.getBlitOffset());
                }
            }
        }
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void hasClickedOutside(double clickX, double clickY, int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (!config.enabled || self instanceof InventoryScreen || this.hoveredSlot == null) return;

        int startX = this.hoveredSlot.x;
        int startY = this.hoveredSlot.y;
        int endX = startX + 5 * 18;
        int endY = startY + 18;

        if (clickX >= startX && clickX <= endX && clickY >= startY && clickY <= endY) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "findSlot", at = @At("TAIL"), cancellable = true)
    private void findSlot(double x, double y, CallbackInfoReturnable<Slot> cir) {
        if (!config.enabled || self instanceof InventoryScreen) return;

        // Loop through armor slots
        NonNullList<ArmorSlot> armorSlots = ((IArmorMenu) this.self.getMenu()).cs_getArmorSlots();
        for (Slot slot : armorSlots) {
            if (this.isHovering(slot, x, y)) {
                cir.setReturnValue(slot);
                return;
            }
        }
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void slotClicked(Slot slot, int i, int j, ClickType clickType, CallbackInfo ci) {
        if (slot instanceof ArmorSlot armorSlot) {
            armorSlot.onClick(clickType);
            ci.cancel();
        }
    }
}
