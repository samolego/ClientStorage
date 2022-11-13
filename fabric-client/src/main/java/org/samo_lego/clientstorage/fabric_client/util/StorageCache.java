package org.samo_lego.clientstorage.fabric_client.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StorageCache {
    public static final Map<BlockPos, Integer> FREE_SPACE_CONTAINERS = new HashMap<>();
    public static final Set<Container> CACHED_INVENTORIES = new HashSet<>();
}
