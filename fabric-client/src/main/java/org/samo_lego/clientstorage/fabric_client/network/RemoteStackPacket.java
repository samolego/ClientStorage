package org.samo_lego.clientstorage.network;

public class RemoteStackPacket {
    public static boolean accessingItem = false;

    public static boolean isAccessingItem() {
        return accessingItem;
    }
}
