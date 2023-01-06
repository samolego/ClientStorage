package org.samo_lego.clientstorage.fabric_client.casts;

import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;

import java.util.Optional;

public interface ICSPlayer {
    /**
     * Marks that player is accessing an item via the crafting terminal.
     *
     * @param accessing whether player is accessing an item.
     */
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

    /**
     * Sets last container that player has interacted with.
     * If player has interacted with a non-container, this should be set to null.
     *
     * @param container last interacted container.
     */

    void cs_setLastInteractedContainer(@Nullable InteractableContainer container);
}
