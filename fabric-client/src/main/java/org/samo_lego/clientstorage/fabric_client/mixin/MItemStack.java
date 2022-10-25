package org.samo_lego.clientstorage.fabric_client.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;
import org.samo_lego.clientstorage.fabric_client.util.ItemDataTooltip;
import org.samo_lego.clientstorage.fabric_client.util.ItemDisplayType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

@Mixin(ItemStack.class)
public abstract class MItemStack implements IRemoteStack {

    @Shadow
    public abstract int getCount();

    @Shadow
    public abstract int getMaxStackSize();

    @Unique
    private int slotId;
    @Unique
    private BlockEntity parentContainer;

    @Override
    public int cs_getSlotId() {
        return slotId;
    }

    @Override
    public void cs_setSlotId(int slotId) {
        this.slotId = slotId;
    }

    @Override
    public BlockEntity cs_getContainer() {
        return this.parentContainer;
    }

    @Override
    public void cs_setContainer(BlockEntity parent) {
        this.parentContainer = parent;
    }


    @Inject(method = "copy", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onCopy(CallbackInfoReturnable<ItemStack> cir, ItemStack newStack) {
        var remote = (IRemoteStack) newStack;
        remote.cs_setSlotId(this.slotId);
        remote.cs_setContainer(this.parentContainer);
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectLocationTooltip(@Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
        // Only show tooltips if parent container is set and if player has crafting screen open
        if (this.parentContainer instanceof BaseContainerBlockEntity container &&
                Minecraft.getInstance().player.containerMenu instanceof CraftingMenu &&
                (config.locationTooltip == ItemDataTooltip.ALWAYS_SHOW || (
                        config.locationTooltip == ItemDataTooltip.REQUIRE_SHIFT && Screen.hasShiftDown() ||
                                config.locationTooltip == ItemDataTooltip.REQUIRE_CTRL && Screen.hasControlDown() ||
                                config.locationTooltip == ItemDataTooltip.REQUIRE_ALT && Screen.hasAltDown()))) {

            final int count = this.getCount();
            final int maxStackSize = this.getMaxStackSize();

            final boolean overstacked = count > maxStackSize && maxStackSize > 1;
            if (overstacked) {
                list.add(Component.empty());  // Empty line
                // Split the count into multiple stacks
                var stackTooltip = Component.literal(count / maxStackSize + " x " + maxStackSize);

                int leftover = count % maxStackSize;
                if (leftover != 0) {
                    stackTooltip.append(" + " + leftover);
                }

                list.add(stackTooltip.withStyle(ChatFormatting.GRAY));
            }

            if ((config.itemDisplayType != ItemDisplayType.MERGE_ALL) || (count <= maxStackSize)) {
                if (!overstacked) {
                    list.add(Component.empty());  // Empty line
                }

                var name = container.getName();
                var coords = Component.literal(" @ " + this.parentContainer.getBlockPos().toShortString());

                list.add(name.plainCopy().append(coords).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
