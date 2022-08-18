package org.samo_lego.clientstorage.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AHandledScreen {
    @Accessor("imageWidth")
    int getBackgroundWidth();

    @Accessor("imageWidth")
    void setBackgroundWidth(int width);

    @Accessor("imageHeight")
    int getBackgroundHeight();

    @Accessor("imageHeight")
    void setBackgroundHeight(int height);
}
