package org.samo_lego.clientstorage.fabric_client.mixin.accessor;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface AScreen {
    @Accessor("renderables")
    List<Renderable> getRenderables();
}
