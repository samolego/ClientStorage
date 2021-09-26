package org.samo_lego.clientstorage.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;

@Environment(EnvType.CLIENT)
public class StorageCraftingScreenHandler extends CraftingMenu {

    public StorageCraftingScreenHandler(int syncId, Inventory playerInventory) {
        super(syncId, playerInventory);
        System.out.println("CUSTOM CRAFTING.");
    }
}
