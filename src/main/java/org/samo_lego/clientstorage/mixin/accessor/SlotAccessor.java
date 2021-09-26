package org.samo_lego.clientstorage.mixin.accessor;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Accessor("x")
    void setX(int x);

    @Accessor("y")
    void setY(int y);
}
