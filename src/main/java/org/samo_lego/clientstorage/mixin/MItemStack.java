package org.samo_lego.clientstorage.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class MItemStack implements IRemoteStack {

    private int slotId;
    private BlockEntity parentContainer;
    private int count;

    @Override
    public int getSlotId() {
        return slotId;
    }

    @Override
    public void setSlotId(int slotId) {

    }

    @Override
    public BlockEntity getContainer() {
        return this.parentContainer;
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
