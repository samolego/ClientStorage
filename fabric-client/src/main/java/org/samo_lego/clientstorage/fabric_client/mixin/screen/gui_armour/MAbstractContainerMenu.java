package org.samo_lego.clientstorage.fabric_client.mixin.screen.gui_armour;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import org.samo_lego.clientstorage.fabric_client.casts.IArmorMenu;
import org.samo_lego.clientstorage.fabric_client.inventory.ArmorInventory;
import org.samo_lego.clientstorage.fabric_client.inventory.ArmorSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

@Mixin(AbstractContainerMenu.class)
public class MAbstractContainerMenu implements IArmorMenu {

    @Unique
    private static final int ARMOR_SLOT_COUNT = 5;
    @Unique
    private final AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
    @Unique
    private NonNullList<ArmorSlot> armorSlots;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(MenuType<?> menuType, int i, CallbackInfo ci) {
        if (!config.enabled || self instanceof InventoryMenu) return;

        this.armorSlots = NonNullList.create();
        // Create 5 new slots
        for (int ix = 0; ix < ARMOR_SLOT_COUNT; ++ix) {
            this.armorSlots.add(new ArmorSlot(ArmorInventory.getInstance(), ix, -16, 84 + ix * 18));
        }
    }

    @Override
    public NonNullList<ArmorSlot> cs_getArmorSlots() {
        return this.armorSlots;
    }
}
