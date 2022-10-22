package org.samo_lego.clientstorage.fabric_client.compatibility;

import org.samo_lego.clientstorage.fabric_client.compatibility.network.ItemNetworking;

public class StashSupport {

    public static void enable() {
        ItemNetworking.registerChannels();
    }

    public static void disable() {
        ItemNetworking.unregisterChannels();
    }
}
