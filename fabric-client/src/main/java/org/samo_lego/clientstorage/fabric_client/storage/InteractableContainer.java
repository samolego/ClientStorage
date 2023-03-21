package org.samo_lego.clientstorage.fabric_client.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.event.ContainerDiscovery;
import org.samo_lego.clientstorage.fabric_client.util.StorageCache;

import java.util.function.Predicate;

public interface InteractableContainer extends Container {
    Predicate<? super Entity> CONTAINER_ENTITY_SELECTOR = (entity) -> entity instanceof InteractableContainer;

    default void cs_sendInteractionPacket() {
        // Save interacted container
        ((ICSPlayer) Minecraft.getInstance().player).cs_setLastInteractedContainer(this);
    }

    boolean cs_isDelayed();

    default void cs_parseOpenPacket(ClientboundContainerSetContentPacket packet) {
        final var stacks = packet.getItems();
        // Writing container content
        boolean added = false;
        for (int i = 0; i < stacks.size() && i < this.getContainerSize(); ++i) {
            var stack = stacks.get(i);

            int count = stack.getCount();

            if (ContainerDiscovery.fakePacketsActive()) {
                // Also add to remote inventory
                if (count > 0) {
                    // Add to crafting screen
                    ContainerDiscovery.addRemoteItem(this, i, stacks.get(i));
                    added = true;
                } else {
                    // This container has more space
                    StorageCache.FREE_SPACE_CONTAINERS.compute(this, (key, value) -> value == null ? 1 : value + 1);
                }
            }
            if (added) {
                StorageCache.CACHED_INVENTORIES.add(this);
            }
            this.setItem(i, stack);
        }
    }

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
    default boolean cs_isEntity() {
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
        // Get position
        final String position = String.format("(%d, %d, %d)",
                (int) this.cs_position().x(),
                (int) this.cs_position().y(),
                (int) this.cs_position().z());
        return String.format("%s @ %s [%d slots]", this.cs_getName().getString(), position, this.getContainerSize());
    }
}
