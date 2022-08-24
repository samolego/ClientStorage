package org.samo_lego.clientstorage.mixin.accessor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.CraftingTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingTableBlock.class)
public interface ACraftingTableBlock {

    @Accessor("CONTAINER_TITLE")
    static Component CONTAINER_TITLE() {
        throw new AssertionError();
    }
}
