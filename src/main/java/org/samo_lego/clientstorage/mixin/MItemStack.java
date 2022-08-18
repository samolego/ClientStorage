package org.samo_lego.clientstorage.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class MItemStack implements IRemoteStack {

    @Unique
    private int slotId;
    @Unique
    private BlockEntity parentContainer;
    @Unique
    private int count;

    @Override
    public int cs_getSlotId() {
        return slotId;
    }

    @Override
    public void cs_setSlotId(int slotId) {
        this.slotId = slotId;
    }

    @Override
    public BlockEntity cs_getContainer() {
        return this.parentContainer;
    }

    @Override
    public void cs_setContainer(BlockEntity parent) {
        this.parentContainer = parent;
    }

    @Override
    public int cs_getCount() {
        return this.count;
    }

    @Override
    public void cs_setCount(int count) {
        this.count = count;
    }
}
