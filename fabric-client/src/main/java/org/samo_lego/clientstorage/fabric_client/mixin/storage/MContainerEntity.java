package org.samo_lego.clientstorage.fabric_client.mixin.storage;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerEntity.class)
public interface MContainerEntity extends InteractableContainer {


    default Packet<?> getInteractionPacket() {
        return null;
    }

    default boolean isDelayed() {
        return false;
    }

    default void parseOpenPacket(Packet<?> packet) {

    }
}
