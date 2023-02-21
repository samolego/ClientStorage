package org.samo_lego.clientstorage.fabric_client.mixin.screen.storage_memory;

import net.minecraft.client.Minecraft;
import org.samo_lego.clientstorage.fabric_client.render.TransparencyBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MMinecraft {
    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void clientstorage$resizeDisplay(CallbackInfo ci) {
        TransparencyBuffer.resizeDisplay();
    }
}
