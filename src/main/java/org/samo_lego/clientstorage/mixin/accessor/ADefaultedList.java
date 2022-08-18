package org.samo_lego.clientstorage.mixin.accessor;

import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(NonNullList.class)
public interface ADefaultedList {
    @Accessor("list")
    List<?> getDelegate();
}
