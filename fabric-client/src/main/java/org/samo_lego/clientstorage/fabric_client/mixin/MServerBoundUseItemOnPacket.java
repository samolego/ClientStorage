package org.samo_lego.clientstorage.fabric_client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.phys.BlockHitResult;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundUseItemOnPacket.class)
public class MServerBoundUseItemOnPacket {

    @Shadow
    @Final
    private BlockHitResult blockHit;

    @Inject(method = "write", at = @At("TAIL"))
    private void onWrite(FriendlyByteBuf buf, CallbackInfo ci) {
        final var blockPos = this.blockHit.getBlockPos();
        final var player = (ICSPlayer) Minecraft.getInstance().player;

        final var blockEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
        if (blockEntity instanceof Container container) {
            player.cs_setLastInteractedContainer(container);
        } else {
            player.cs_setLastInteractedContainer(null);
        }
    }
}
