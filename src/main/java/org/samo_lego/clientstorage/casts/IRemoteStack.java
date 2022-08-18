package org.samo_lego.clientstorage.casts;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IRemoteStack {

    int cs_getSlotId();

    void cs_setSlotId(int slotId);

    BlockEntity cs_getContainer();

    void cs_setContainer(BlockEntity parent);

    int cs_getCount();

    void cs_setCount(int count);

    static ItemStack fromStack(ItemStack stack, BlockEntity blockEntity, int slot) {
        // Add properties to ItemStack via IRemoteStack interface
        IRemoteStack remote = (IRemoteStack) (Object) stack;
        remote.cs_setSlotId(slot);
        remote.cs_setContainer(blockEntity);

        return stack;
    }
}
