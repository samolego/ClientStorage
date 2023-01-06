package org.samo_lego.clientstorage.fabric_client.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public interface InteractableContainer extends Container {
    Predicate<? super Entity> CONTAINER_ENTITY_SELECTOR = (entity) -> entity instanceof InteractableContainer;

    void cs_sendInteractionPacket();

    boolean cs_isDelayed();

    void cs_parseOpenPacket(Packet<?> packet);

    /**
     * Mark this container to be glowing.
     */
    void cs_markGlowing();

    /**
     * Returns the position of this container.
     *
     * @return position of this container.
     */

    Vec3 cs_position();

    /**
     * Whether this container is an entity.
     *
     * @return true if this container is an entity, false otherwise.
     */
    default boolean isEntity() {
        return this instanceof Entity;
    }

    /**
     * Gets this container's name.
     *
     * @return name of this container.
     */
    Component cs_getName();

    /**
     * Gets this container's information (name and position) as string.
     *
     * @return information of this container.
     */
    default String cs_info() {
        return String.format("%s @ %s", this.cs_getName().getString(), new BlockPos(this.cs_position()).toShortString());
    }
}
