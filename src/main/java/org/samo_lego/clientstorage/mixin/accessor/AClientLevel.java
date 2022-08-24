package org.samo_lego.clientstorage.mixin.accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientLevel.class)
public interface AClientLevel {
    @Invoker("getBlockStatePredictionHandler")
    BlockStatePredictionHandler cs_getBlockStatePredictionHandler();
}
