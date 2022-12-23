package org.samo_lego.clientstorage.fabric_client.storage;

import net.minecraft.network.protocol.Packet;

public interface InteractableContainer {
    Packet<?> getInteractionPacket();

    boolean isDelayed();

    void parseOpenPacket(Packet<?> packet);
}
