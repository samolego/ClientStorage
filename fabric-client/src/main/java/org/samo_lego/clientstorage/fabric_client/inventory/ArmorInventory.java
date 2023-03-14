package org.samo_lego.clientstorage.fabric_client.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArmorInventory implements Container {
    private static final ArmorInventory INSTANCE = new ArmorInventory();

    public static Container getInstance() {
        return INSTANCE;
    }

    private NonNullList<ItemStack> getItems() {
        return Minecraft.getInstance().player.inventoryMenu.getItems();
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return this.getItems().get(ArmorSlot.getSlotIndex(i));
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
