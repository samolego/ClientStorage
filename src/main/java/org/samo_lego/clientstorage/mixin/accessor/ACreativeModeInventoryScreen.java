package org.samo_lego.clientstorage.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeInventoryScreen.class)
public interface ACreativeModeInventoryScreen {
    @Accessor("CREATIVE_TABS_LOCATION")
    static ResourceLocation CREATIVE_TABS_LOCATION() {
        throw new AssertionError();
    }
}
