package org.samo_lego.clientstorage.mixin.accessor;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface AScreen {
    @Accessor("renderables")
    List<Widget> getRenderables();
}
