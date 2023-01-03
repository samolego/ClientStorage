package org.samo_lego.clientstorage.fabric_client.util;

import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StorageCache {

    /**
     * Containers that have some space left. Used for inputting items.
     */
    public static final Map<InteractableContainer, Integer> FREE_SPACE_CONTAINERS = new ConcurrentHashMap<>();

    /**
     * Stores cached inventories.
     */
    public static final Set<InteractableContainer> CACHED_INVENTORIES = ConcurrentHashMap.newKeySet();
}
