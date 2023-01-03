package org.samo_lego.clientstorage.fabric_client.mixin.storage;

import net.minecraft.world.entity.vehicle.ContainerEntity;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerEntity.class)
public interface MContainerEntity extends InteractableContainerEntity {
}
