package org.samo_lego.clientstorage.mixin;

import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundUseItemOnPacket.class)
public class MPlayerInteractBlockC2SPacket {
    //@Inject(method = "<init>(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)V", at = @At("TAIL"))
    private void initPacket(InteractionHand hand, BlockHitResult blockHitResult, CallbackInfo ci) {
        //MinecraftClient.getInstance().player;
        //INTERACTION_Q.add(blockHitResult.getBlockPos());
    }
}
