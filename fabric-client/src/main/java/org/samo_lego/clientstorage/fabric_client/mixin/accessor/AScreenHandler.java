package org.samo_lego.clientstorage.fabric_client.mixin.accessor;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerMenu.class)
public interface AScreenHandler {
    @Invoker("addSlot")
    Slot cs_addSlot(Slot slot);
}
