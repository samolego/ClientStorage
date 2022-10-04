package org.samo_lego.clientstorage.fabric_client.network;

public class RemoteStackPacket {
    public static boolean accessingItem = false;

    public static boolean isAccessingItem() {
        return accessingItem;
    }
}
