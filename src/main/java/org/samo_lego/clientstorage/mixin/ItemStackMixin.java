package org.samo_lego.clientstorage.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import org.samo_lego.clientstorage.casts.RemoteStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class ItemStackMixin implements RemoteStack {

    private int slotId;
    private BlockEntity parentContainer;
    private int count;

    @Override
    public int getSlotId() {
        return 0;
    }

    @Override
    public void setSlotId(int slotId) {

    }

    @Override
    public BlockEntity getContainer() {
        return null;
    }

    @Override
    public void setContainer(BlockEntity parent) {

    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }
}
