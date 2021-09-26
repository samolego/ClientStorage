package org.samo_lego.clientstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;

import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(ServerboundUseItemOnPacket.class)
public class PlayerInteractBlockC2SPacketMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)V", at = @At("TAIL"))
    private void initPacket(InteractionHand hand, BlockHitResult blockHitResult, CallbackInfo ci) {
        //MinecraftClient.getInstance().player;
        //INTERACTION_Q.add(blockHitResult.getBlockPos());
    }
}
