package org.samo_lego.clientstorage.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;

@Environment(EnvType.CLIENT)
public class StorageCraftingScreenHandler extends CraftingScreenHandler {

    public StorageCraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory);
        System.out.println("CUSTOM CRAFTING.");
    }
}
