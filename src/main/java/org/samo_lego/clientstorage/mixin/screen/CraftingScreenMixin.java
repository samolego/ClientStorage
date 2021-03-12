package org.samo_lego.clientstorage.mixin.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin {

    private final CraftingScreen craftingScreen = (CraftingScreen) (Object) this;

    @ModifyVariable(
            method = "drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V",
            at = @At("STORE"),
            ordinal = 3
    )
    private int moveY(int y) {
        return y + 36;
    }
}
