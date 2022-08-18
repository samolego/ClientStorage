package org.samo_lego.clientstorage.casts;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IRemoteStack {

    int getSlotId();

    void setSlotId(int slotId);

    BlockEntity getContainer();

    void setContainer(BlockEntity parent);

    int getCount();

    void setCount(int count);
}
