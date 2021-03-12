package org.samo_lego.clientstorage.mixin.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.samo_lego.clientstorage.mixin.accessor.HandledScreenAccessor;
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
    private static final Identifier TEXTURE_SEARCH = new Identifier("textures/gui/container/creative_inventory/tab_item_search.png");

    @ModifyVariable(
            method = "drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V",
            at = @At("STORE"),
            ordinal = 3
    )
    private int moveY(int y) {
        return y + 36;
    }

    @Inject(method = "drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addBackground(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci, int startX, int y) {
        //RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE_SEARCH);
        final int SEARCHBAR_HEIGHT = 71;
        final int SEARCHBAR_WIDTH = 195;
        //((HandledScreenAccessor) craftingScreen).setBackgroundHeight(SEARCHBAR_HEIGHT);

        craftingScreen.drawTexture(matrices, (craftingScreen.width - SEARCHBAR_WIDTH) / 2, y - SEARCHBAR_HEIGHT, 0, 0, SEARCHBAR_WIDTH, SEARCHBAR_HEIGHT);
        //craftingScreen.drawTexture(matrices, (craftingScreen.width - SEARCHBAR_WIDTH) / 2, y - SEARCHBAR_HEIGHT, 0, 0, 195, 71);
    }
}
