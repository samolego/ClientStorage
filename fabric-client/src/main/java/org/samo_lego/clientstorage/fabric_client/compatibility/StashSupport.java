package org.samo_lego.clientstorage.fabric_client.compatibility;

import org.samo_lego.clientstorage.fabric_client.compatibility.network.ItemNetworking;

import java.util.LinkedList;

public class StashSupport {
    private static final LinkedList<StashContainer> STASHES = new LinkedList<>();

    public static void enable() {
        ItemNetworking.registerChannels();
    }

    public static void disable() {
        ItemNetworking.unregisterChannels();
        resetStashes();
    }

    public static void resetStashes() {
        STASHES.clear();
    }

    public static void addStashContainer(StashContainer stashContainer) {
        STASHES.addLast(stashContainer);
    }

    public static void addAllItems() {
        STASHES.forEach(StashContainer::addAllItems);
    }
}
