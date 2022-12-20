package org.samo_lego.clientstorage.fabric_client.mixin.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.phys.BlockHitResult;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.util.ContainerUtil;
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

    /**
     * Saves the last interacted container.
     *
     * @param buf
     * @param ci
     */
    @Inject(method = "write", at = @At("TAIL"))
    private void onWrite(FriendlyByteBuf buf, CallbackInfo ci) {
        final var blockPos = this.blockHit.getBlockPos();
        final var player = (ICSPlayer) Minecraft.getInstance().player;

        final var blockEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
        player.cs_setLastInteractedContainer(ContainerUtil.getContainer(blockEntity));
    }
}
