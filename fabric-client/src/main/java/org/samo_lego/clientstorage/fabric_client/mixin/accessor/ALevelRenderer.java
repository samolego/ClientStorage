package org.samo_lego.clientstorage.fabric_client.mixin.accessor;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface ALevelRenderer {
    @Accessor("entityEffect")
    PostChain getEntityEffect();

    @Accessor("renderBuffers")
    RenderBuffers getRenderBuffers();
}
