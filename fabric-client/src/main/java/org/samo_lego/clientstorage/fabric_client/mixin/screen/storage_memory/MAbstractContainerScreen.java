package org.samo_lego.clientstorage.fabric_client.mixin.screen.storage_memory;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.config.storage_memory.StorageMemoryPreset;
import org.samo_lego.clientstorage.fabric_client.render.TransparencyBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

@Mixin(AbstractContainerScreen.class)
public abstract class MAbstractContainerScreen extends Screen {

    @Shadow
    protected int imageWidth;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Unique
    private int fakeSlot = -999;

    @Unique
    private static boolean usingFakeSlot = false;
    @Unique
    private Button activeButton;

    protected MAbstractContainerScreen(Component component) {
        super(component);
    }

    @Shadow
    public abstract boolean mouseClicked(double d, double e, int i);

    @Nullable
    @Unique
    private Int2ObjectMap<Item> activePreset;
    @Unique
    private Slot activeSlot;

    @Shadow
    protected abstract void slotClicked(Slot slot, int i, int j, ClickType clickType);

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
            if (config.enabled && container instanceof BaseContainerBlockEntity be) {
                // Make storage memory presets available
                final var inventory = config.storageMemory.get(be);
                inventory.ifPresent(itemInt2ObjectMap -> this.activePreset = itemInt2ObjectMap);
            } else {
                this.activePreset = null;
            }
        });
    }


    @Inject(method = "renderSlotHighlight",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(Lnet/minecraft/client/renderer/RenderType;IIIIIII)V"),
            cancellable = true)
    private static void renderSlotHighlight(GuiGraphics graphics, int x, int y, int blitOffset, CallbackInfo ci) {
        if (usingFakeSlot) {
            final int color = 0x7F_FF_F7_00;

            graphics.fillGradient(x, y, x + 16, y + 16, color, color, blitOffset);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();

            ci.cancel();
        }
    }

    /**
     * Just there to save current rendered slot
     *
     * @param guiGraphics
     * @param slot
     * @param ci
     */
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void saveSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        this.activeSlot = slot;
    }

    /**
     * Takes care of item rendering transparency.
     * It renders the item to a framebuffer and then
     * draws framebuffer texture over the slot with transparency.
     *
     * @param guiGraphics graphics
     * @param slot        slot being rendered
     * @param ci
     */
    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void postRenderItem(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (this.fakeSlot == slot.index) {
            TransparencyBuffer.preInject();

            // Align the matrix stack
            //poseStack.pushPose();
            //poseStack.translate(-this.leftPos, -this.topPos, 0.0f);

            // Draw the framebuffer texture
            TransparencyBuffer.drawExtraFramebuffer(guiGraphics);
            //poseStack.popPose();

            TransparencyBuffer.postInject();
        }
    }

    /**
     * Modifies the stack that is being rendered.
     * If it's empty, it will try to render one
     * with count 0 from the preset.
     *
     * @param stack stack being rendered
     * @return (modified if needed) stack to render
     */
    @ModifyVariable(method = "renderSlot",
            at = @At(value = "STORE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack renderItemTransparencyForPreset(ItemStack stack) {
        // Activate only if enabled
        final LocalPlayer player = Minecraft.getInstance().player;
        usingFakeSlot = false;
        if (this.activePreset == null || !stack.isEmpty() || player == null || !player.hasContainerOpen()) return stack;

        final var presetItem = activePreset.get(this.activeSlot.index);

        if (presetItem != null) {
            this.fakeSlot = activeSlot.index;
            usingFakeSlot = true;

            TransparencyBuffer.prepareExtraFramebuffer();

            return new ItemStack(presetItem);
        }

        this.fakeSlot = -999;
        return stack;
    }

    /**
     * Adds buttons to save a preset and to transfer items.
     *
     * @param ci
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void addButtons(CallbackInfo ci) {
        final var container = ((ICSPlayer) Minecraft.getInstance().player).cs_getLastInteractedContainer();
        if (container.isEmpty() || !(container.get() instanceof BaseContainerBlockEntity) || !config.storageMemory.enabled)
            return;

        // Transfer items button
        final var transferBtn = Button
                .builder(Component.literal("↑"), b -> CompletableFuture.runAsync(this::transferStacks))
                .bounds(this.leftPos + this.imageWidth - 24, this.topPos + 72, 16, 10).build();
        this.addRenderableWidget(transferBtn);


        // Preset buttons
        this.initButtons((BaseContainerBlockEntity) container.get());
    }

    /**
     * Adds save / load buttons to screen.
     *
     * @param container
     */
    private void initButtons(BaseContainerBlockEntity container) {
        if (this.activeButton != null) this.removeWidget(this.activeButton);

        final boolean hasPreset = config.storageMemory.containsPreset(container);
        final var symbol = hasPreset ? "x" : "☆";
        final var translation = hasPreset ? "selectWorld.deleteButton" : "structure_block.mode.save";
        final var tooltip = hasPreset ? "tooltip.clientstorage.remove_preset" : "tooltip.clientstorage.save_preset";


        this.activeButton = Button.builder(Component.literal(symbol).append(" ").append(Component.translatable(translation)), b -> {
                    if (hasPreset) {
                        this.removePreset(container);
                    } else {
                        this.savePreset(container);
                    }
                    this.initButtons(container);
                })
                .tooltip(Tooltip.create(Component.translatable(tooltip)))
                .bounds(this.leftPos + this.imageWidth - 52, this.topPos - 20, 50, 20).build();

        this.addRenderableWidget(this.activeButton);
    }

    /**
     * Removes preset from config.
     */
    private void removePreset(BaseContainerBlockEntity blockEntity) {
        this.activePreset = null;
        config.storageMemory.removePreset(StorageMemoryPreset.of(blockEntity));
        config.save();
    }


    /**
     * Saves current inventory layout to preset.
     *
     * @param be block entity of the container
     */
    private void savePreset(BaseContainerBlockEntity be) {
        // Get current item layout and build preset map
        final var map = new Int2ObjectArrayMap<Item>();
        final var slots = Minecraft.getInstance().player.containerMenu.getItems();

        for (int i = 0; i < be.getContainerSize(); ++i) {
            final var item = slots.get(i);
            if (!item.isEmpty()) {
                map.put(i, item.getItem());
            }
        }

        if (map.isEmpty()) {
            this.activePreset = null;
            config.storageMemory.removePreset(StorageMemoryPreset.of(be));
        } else {
            config.storageMemory.savePreset(StorageMemoryPreset.of(be), map);
            this.activePreset = map;
        }
        config.save();
    }

    /**
     * Transfers stacks to the container if they're present in the preset.
     */
    private void transferStacks() {
        if (this.activePreset == null) return;

        // Loop through player's inventory
        final var playerInv = Minecraft.getInstance().player.getInventory();
        final var slots = Minecraft.getInstance().player.containerMenu.slots;

        for (int i = slots.size() - playerInv.getContainerSize(); i < slots.size(); ++i) {
            Slot slot = slots.get(i);
            final var stack = slot.getItem();
            if (!stack.isEmpty()) {
                // If item is in preset, transfer it
                int slotIndex = -1;
                for (var slotItem : this.activePreset.int2ObjectEntrySet()) {
                    if (slotItem.getValue() == stack.getItem() && stack.getCount() + slots.get(slotItem.getIntKey()).getItem().getCount() <= stack.getMaxStackSize()) {
                        slotIndex = slotItem.getIntKey();
                        break;
                    }
                }
                if (slotIndex != -1) {
                    try {
                        // Wait some time to prevent spamming
                        Thread.sleep(70);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.slotClicked(slot, i, 0, ClickType.PICKUP);
                    this.slotClicked(slots.get(slotIndex), slotIndex, 0, ClickType.PICKUP);
                }
            }
        }
    }
}
