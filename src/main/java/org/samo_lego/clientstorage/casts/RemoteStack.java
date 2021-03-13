package org.samo_lego.clientstorage.casts;

import net.minecraft.block.entity.BlockEntity;

public interface RemoteStack {

    int getSlotId();
    void setSlotId(int slotId);

    BlockEntity getContainer();
    void setContainer(BlockEntity parent);

    int getCount();

    void setCount(int count);
}
