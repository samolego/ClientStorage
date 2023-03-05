package org.samo_lego.clientstorage.fabric_client.casts;

import net.minecraft.core.NonNullList;
import org.samo_lego.clientstorage.fabric_client.inventory.ArmorSlot;

public interface IArmorMenu {
    default NonNullList<ArmorSlot> cs_getArmorSlots() {
        return NonNullList.create();
    }
}
