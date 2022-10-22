package org.samo_lego.clientstorage.fabric_client.compatibility;

import org.samo_lego.clientstorage.fabric_client.compatibility.network.ItemNetworking;

import java.util.HashSet;
import java.util.Set;

public class StashSupport {

    /**
     * Stores available stashes to put items to.
     */
    public static final Set<StashContainer> STASHES = new HashSet<>();

    public static void enable() {
        ItemNetworking.registerChannels();
    }

    public static void disable() {
        ItemNetworking.unregisterChannels();
    }
}
