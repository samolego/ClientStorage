package org.samo_lego.clientstorage.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;

@Mixin(PlayerInteractBlockC2SPacket.class)
public class PlayerInteractBlockC2SPacketMixin {
    @Inject(method = "<init>(Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)V", at = @At("TAIL"))
    private void initPacket(Hand hand, BlockHitResult blockHitResult, CallbackInfo ci) {
        //MinecraftClient.getInstance().player;
        //INTERACTION_Q.add(blockHitResult.getBlockPos());
    }
}
