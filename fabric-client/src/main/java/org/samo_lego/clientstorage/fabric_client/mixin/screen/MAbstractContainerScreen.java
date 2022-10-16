package org.samo_lego.clientstorage.fabric_client.mixin.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class MAbstractContainerScreen extends Screen {

    @Unique
    private Slot slot;

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

            // Use physics notation for large numbers
            if (count >= 100) {
                int exp = (int) Math.log10(count);
                // Todo - modify font to be smaller
                return String.format("%.1f%c", count / Math.pow(10, exp), "hkMGTP".charAt(exp - 2));
            }
        }
        return label;
    }
}
