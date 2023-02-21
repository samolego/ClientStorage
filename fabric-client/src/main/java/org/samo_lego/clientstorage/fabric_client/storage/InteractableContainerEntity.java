package org.samo_lego.clientstorage.fabric_client.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.fabric_client.render.ESPRender;

/**
 * Implementation of {@link InteractableContainer}, used for entities.
 */
public interface InteractableContainerEntity extends InteractableContainer {
    @Override
    default void cs_sendInteractionPacket() {
        InteractableContainer.super.cs_sendInteractionPacket();
        final var player = Minecraft.getInstance().player;

        // As player needs to shift-click some containers, e.g. chest boats, we need to send "player shifting" packet
        // Note: this is not actually needed as "secondary action type" boolean is determined by
        // ServerboundInteractPacket#createInteractionPacket(..., sneak: bool, ...)
        // But since we'd like to mimic vanilla behaviour, we still send it.
        ServerboundPlayerCommandPacket packet = new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY);
        player.connection.send(packet);

        // Send interaction packet
        // First, we send interact_at
        final var interactionPacketAt = ServerboundInteractPacket.createInteractionPacket((Entity) this, true, InteractionHand.MAIN_HAND,
                player.position().subtract(this.cs_position()));
        player.connection.send(interactionPacketAt);

        // Then, we send the normal interact
        // if interact_at isn't send, we can have problems
        // with chestboats, as that would make us ride them
        final var interactionPacket = ServerboundInteractPacket.createInteractionPacket((Entity) this, true, InteractionHand.MAIN_HAND);
        player.connection.send(interactionPacket);

        // Send "player stopped shifting" packet
        packet = new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY);
        player.connection.send(packet);
    }

    @Override
    default boolean cs_isDelayed() {
        return false;
    }

    @Override
    default void cs_markGlowing() {
        // Apply fake glowing effect to the entity
        ESPRender.markEntity((Entity) this);
    }

    @Override
    default Vec3 cs_position() {
        return ((Entity) this).position();
    }

    @Override
    default Component cs_getName() {
        return ((Entity) this).getName();
    }
}
