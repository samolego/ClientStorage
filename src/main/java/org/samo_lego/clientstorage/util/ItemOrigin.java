package org.samo_lego.clientstorage.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.samo_lego.clientstorage.casts.IRemoteStack;

public record ItemOrigin(BlockEntity blockEntity, int slot, int count) {

    public ItemOrigin(IRemoteStack remote) {
        this(remote.cs_getContainer(), remote.cs_getSlotId(), remote.cs_getCount());
    }
}
