package org.samo_lego.clientstorage.fabric_client.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.AMultiPlayerGamemode;
import org.samo_lego.clientstorage.fabric_client.render.ESPRender;
import org.samo_lego.clientstorage.fabric_client.util.PlayerLookUtil;

/**
 * Implementation of {@link InteractableContainer}, used for containers that are blocks.
 */
public interface InteractableContainerBlock extends InteractableContainer {
    @Override
    default void cs_sendInteractionPacket() {
        InteractableContainer.super.cs_sendInteractionPacket();
        var gm = (AMultiPlayerGamemode) Minecraft.getInstance().gameMode;
        final var hitResult = PlayerLookUtil.raycastTo(this.cs_position());
        gm.cs_startPrediction(Minecraft.getInstance().level, i ->
                new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, i));

    }

    @Override
    default boolean cs_isDelayed() {
        return true;
    }

    @Override
    default void cs_markGlowing() {
        ESPRender.markBlock(BlockPos.containing(this.cs_position()));
    }
}
