package org.samo_lego.clientstorage.fabric_client.casts;

import net.minecraft.world.Container;

import java.util.Optional;

public interface ICSPlayer {
    void cs_setAccessingItem(boolean accessing);

    boolean cs_isAccessingItem();

    Optional<Container> cs_getLastInteractedContainer();

    void cs_setLastInteractedContainer(Container container);
}
