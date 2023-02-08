package org.samo_lego.clientstorage.fabric_client.mixin.screen.storage_memory;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

@Mixin(AbstractContainerScreen.class)
public class MAbstractContainerScreen {

    @Unique
    private boolean enableButtons;
    @Unique
    private Int2ObjectMap<ItemStack> activePreset;
    @Unique
    private Slot activeSlot;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(AbstractContainerMenu abstractContainerMenu, Inventory _inventory, Component component, CallbackInfo ci) {
        // Get last interacted container, only apply if BaseContainerBlockEntity
        ((ICSPlayer) Minecraft.getInstance().player).cs_getLastInteractedContainer().ifPresent(container -> {
            if (container instanceof BaseContainerBlockEntity be) {
                // Make storage memory presets available
                this.enableButtons = true;

                final var inventory = config.storageMemory.get(be);
                if (inventory != null) {
                    this.activePreset = inventory;
                }
                System.out.println("[ContainerScreen] Storage presets available! : " + inventory);
            }
        });
    }


    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void saveSlot(PoseStack poseStack, Slot slot, CallbackInfo ci) {
        this.activeSlot = slot;
    }

    @ModifyVariable(method = "renderSlot",
            at = @At(value = "STORE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack renderItemTransparencyForPreset(ItemStack stack) {
        // Activate only if enabled
        if (this.activePreset == null || !stack.isEmpty()) return stack;

        final var presetStack = activePreset.get(this.activeSlot.index);
        if (presetStack != null) {
            var fake = new ItemStack(presetStack.getItem()) {
                @Override
                public boolean isEmpty() {
                    return false;
                }
            };
            fake.setCount(0);
            return fake;
        }

        return stack;
    }
}
