package org.samo_lego.clientstorage.fabric_client.mixin.storage;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractChestedHorse.class)
public abstract class MAbstractChestedHorse extends AbstractHorse implements InteractableContainerEntity {
    @Shadow
    protected abstract int getInventorySize();

    protected MAbstractChestedHorse(EntityType<? extends AbstractChestedHorse> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    public int getContainerSize() {
        System.out.println("ChestedHorseInv: size: " + this.inventory.getContainerSize() + ", vs other: " + this.getInventorySize());
        return this.inventory.getContainerSize() - 1;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.getItem(slot + 1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.inventory.removeItemNoUpdate(slot + 1);
    }


    @Override
    public void setItem(int slot, ItemStack itemStack) {
        this.inventory.setItem(slot + 1, itemStack);
    }

}
