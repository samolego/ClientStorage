package org.samo_lego.clientstorage.fabric_client.mixin.accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface AMultiPlayerGamemode {
    @Invoker("startPrediction")
    void cs_startPrediction(ClientLevel level, PredictiveAction predictiveAction);
}
