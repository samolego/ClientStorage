package org.samo_lego.clientstorage.fabric_client.inventory;

public enum ItemDisplayType {
    /**
     * Merge all same items originated from same container.
     */
    MERGE_PER_CONTAINER,

    /**
     * Merge all same items.
     */
    MERGE_ALL,

    /**
     * Show each stack separately.
     */
    SEPARATE_ALL,
}
