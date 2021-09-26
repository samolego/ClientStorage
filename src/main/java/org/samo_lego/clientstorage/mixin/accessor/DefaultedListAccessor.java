package org.samo_lego.clientstorage.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.core.NonNullList;

@Mixin(NonNullList.class)
public interface DefaultedListAccessor {
    @Accessor("list")
    List<?> getDelegate();
}
