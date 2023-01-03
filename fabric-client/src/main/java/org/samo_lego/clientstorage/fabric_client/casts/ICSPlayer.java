package org.samo_lego.clientstorage.fabric_client.casts;

import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;

import java.util.Optional;

public interface ICSPlayer {
    void cs_setAccessingItem(boolean accessing);

    /**
     * Whether mod is in state of accessing item.
     * Open screen packets should be ignored if true.
     *
     * @return true if accessing item, false otherwise
     */
    boolean cs_isAccessingItem();

    /**
     * Gets last container that player
     * has interacted with.
     *
     * @return last interacted container
     */
    Optional<InteractableContainer> cs_getLastInteractedContainer();

    void cs_setLastInteractedContainer(InteractableContainer container);
}
