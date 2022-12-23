package org.samo_lego.clientstorage.fabric_client.mixin.accessor;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompoundContainer.class)
public interface ACompoundContainer {
    @Accessor
    Container getContainer1();

    @Accessor
    Container getContainer2();
}
