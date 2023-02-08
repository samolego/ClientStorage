package org.samo_lego.clientstorage.fabric_client.mixin.screen.storage_memory;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
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
    private Int2ObjectMap<Item> activePreset;
    @Unique
    private Slot activeSlot;

    /**
     * Assigns the active preset to the container screen.
     *
     * @param abstractContainerMenu
     * @param _inventory
     * @param component
     * @param ci
     */
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


    /**
     * Just there to save current rendered slot
     *
     * @param poseStack
     * @param slot
     * @param ci
     */
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void saveSlot(PoseStack poseStack, Slot slot, CallbackInfo ci) {
        this.activeSlot = slot;
    }


    /**
     * Modifies the stack that is being rendered.
     * If it's empty, it will try to render one
     * with count 0 from the preset.
     * todo: make rendering partially transparent
     *
     * @param stack stack being rendered
     * @return (modified if needed) stack to render
     */
    @ModifyVariable(method = "renderSlot",
            at = @At(value = "STORE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack renderItemTransparencyForPreset(ItemStack stack) {
        // Activate only if enabled
        final LocalPlayer player = Minecraft.getInstance().player;
        if (this.activePreset == null || !stack.isEmpty() || player == null || !player.hasContainerOpen()) return stack;

        final var presetItem = activePreset.get(this.activeSlot.index);
        if (presetItem != null) {
            var fake = new ItemStack(presetItem) {
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
