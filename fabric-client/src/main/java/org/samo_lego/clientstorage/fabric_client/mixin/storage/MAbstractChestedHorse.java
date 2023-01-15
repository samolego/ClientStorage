package org.samo_lego.clientstorage.fabric_client.mixin.storage;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(AbstractChestedHorse.class)
public abstract class MAbstractChestedHorse extends AbstractHorse implements InteractableContainerEntity {

    @Shadow
    @Final
    public static int INV_CHEST_COUNT;
    @Unique
    private ItemStack[] inv;


    @Shadow
    protected abstract int getInventorySize();
    @Unique
    private boolean empty;


    protected MAbstractChestedHorse(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    protected void constructor(EntityType<?> entityType, Level level, CallbackInfo ci) {
        this.inv = new ItemStack[INV_CHEST_COUNT + 2];  // + 1 for saddle / carpet on llamam and + 1 for armor
        this.empty = true;
    }

    @Override
    public int getContainerSize() {
        return this.getInventorySize();
    }

    @Override
    public boolean isEmpty() {
        return this.empty;
    }

    @Override
    public ItemStack getItem(int slot) {
        final var stack = this.inv[slot];
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        final ItemStack stack = this.inv[slot];
        this.inv[slot] = ItemStack.EMPTY;
        this.updateEmpty();

        if (stack == null) {
            return ItemStack.EMPTY;
        }
        return stack;
    }


    @Override
    public void setItem(int slot, ItemStack itemStack) {
        this.inv[slot] = itemStack;
        this.updateEmpty();
    }

    private void updateEmpty() {
        this.empty = Arrays.stream(this.inv).noneMatch(stack -> stack != null && !stack.isEmpty());
    }
}
