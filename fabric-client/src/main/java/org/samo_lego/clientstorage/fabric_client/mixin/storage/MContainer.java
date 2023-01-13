package org.samo_lego.clientstorage.fabric_client.mixin.storage;

import net.minecraft.world.Container;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Container.class)
public interface MContainer extends InteractableContainer {
}
