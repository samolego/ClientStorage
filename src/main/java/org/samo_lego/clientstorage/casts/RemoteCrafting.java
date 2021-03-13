package org.samo_lego.clientstorage.casts;

import org.samo_lego.clientstorage.inventory.RemoteInventory;

public interface RemoteCrafting {

    RemoteInventory getRemoteInventory();

    void refreshRemoteInventory();
}
