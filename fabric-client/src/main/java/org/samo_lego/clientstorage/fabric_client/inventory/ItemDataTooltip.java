package org.samo_lego.clientstorage.fabric_client.inventory;

public enum ItemDataTooltip {
    /**
     * Show item tooltip only when holding shift key.
     */
    REQUIRE_SHIFT,
    /**
     * Show item tooltip only when holding alt key.
     */
    REQUIRE_ALT,
    /**
     * Don't show tooltip data at all.
     */
    ALWAYS_HIDE,
    ALWAYS_SHOW,
    /**
     * Show item tooltip only when holding control key.
     */
    REQUIRE_CTRL,
}
