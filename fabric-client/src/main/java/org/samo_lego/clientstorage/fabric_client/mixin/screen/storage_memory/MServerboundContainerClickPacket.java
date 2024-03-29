package org.samo_lego.clientstorage.fabric_client.mixin.screen.storage_memory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

@Mixin(ServerboundContainerClickPacket.class)
public class MServerboundContainerClickPacket {
    @Mutable
    @Shadow
    @Final
    private Int2ObjectMap<ItemStack> changedSlots;

    @Mutable
    @Shadow
    @Final
    private ItemStack carriedItem;

    @Mutable
    @Shadow
    @Final
    private ClickType clickType;

    @Mutable
    @Shadow
    @Final
    private int slotNum;

    @Mutable
    @Shadow
    @Final
    private int containerId;

    @Mutable
    @Shadow
    @Final
    private int stateId;

    /**
     * Modifies {@link ClickType#QUICK_MOVE} type ("shift-click") packets
     * to PICKUP and "putdown" packets, if there's a preset item available
     * for current container and clicked item.
     *
     * @param containerId
     * @param stateId
     * @param slotNum
     * @param buttonNum
     * @param clickType
     * @param carriedStack
     * @param transferData
     * @param ci
     */
    @Inject(method = "<init>(IIIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/item/ItemStack;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("TAIL"))
    private void clientstorage$constructor(int containerId, int stateId, int slotNum, int buttonNum, ClickType clickType, ItemStack carriedStack, Int2ObjectMap<ItemStack> transferData, CallbackInfo ci) {
        if (clickType == ClickType.QUICK_MOVE && config.storageMemory.enabled) {
            // Get current player inventory
            final LocalPlayer player = Minecraft.getInstance().player;
            ((ICSPlayer) player).cs_getLastInteractedContainer().ifPresent(container -> {
                if (slotNum < container.getContainerSize()) {
                    // Transferring FROM container to inventory, cancel modifications
                    return;
                }

                // Prevent normal shift click but act as item was transferred manually
                if (container instanceof BaseContainerBlockEntity be) {
                    final var layout = config.storageMemory.get(be);
                    if (layout.isPresent()) {
                        final var inventoryLayout = layout.get();
                        // Gets stack that wants to be shift-clicked
                        ItemStack stackToMove = ItemStack.EMPTY;
                        int destinationSlot = -999;  // where the item DEFAULTS to be transferred to
                        for (var transferPair : transferData.int2ObjectEntrySet()) {
                            var item = transferPair.getValue();
                            if (!item.isEmpty()) {
                                destinationSlot = transferPair.getIntKey();
                                stackToMove = item;
                                break;
                            }
                        }

                        if (destinationSlot == -999 || stackToMove.getItem() == inventoryLayout.get(destinationSlot)) {
                            // We got lucky, no need to modify packet
                            return;
                        }

                        // Gets the appropriate slot to put item in, if it exists
                        int wantedSlot = -1;
                        for (var slotItem : inventoryLayout.int2ObjectEntrySet()) {
                            int slotIx = slotItem.getIntKey();
                            ItemStack containerItem = player.containerMenu.getItems().get(slotIx);

                            if (slotItem.getValue() == stackToMove.getItem() && containerItem.getCount() + stackToMove.getCount() <= stackToMove.getMaxStackSize()) {
                                wantedSlot = slotIx;
                                break;
                            }
                        }


                        if (wantedSlot != -1) {
                            // Fix broken visual inventory
                            player.containerMenu.getSlot(destinationSlot).set(ItemStack.EMPTY);
                            player.containerMenu.getSlot(wantedSlot).set(stackToMove.copy());

                            // We found preset item, move it to the correct slot
                            // Construct "Hey I want to pickup (shift-clicked) item" packet
                            final var clickedItem = transferData.remove(destinationSlot);
                            var newPacket = new ServerboundContainerClickPacket(containerId, stateId, slotNum, buttonNum, ClickType.PICKUP, stackToMove, transferData);
                            Minecraft.getInstance().getConnection().send(newPacket);

                            // Construct the missing map
                            var newTransferData = Int2ObjectMaps.singleton(destinationSlot, clickedItem);

                            // Now, modify *this* packet to emulate putting item DOWN in correct slot
                            this.carriedItem = ItemStack.EMPTY;
                            this.clickType = ClickType.PICKUP;
                            this.slotNum = wantedSlot;
                            this.changedSlots = newTransferData;
                        } else {
                            // We didn't find preset item, so we just cancel the shift-click
                            // Kinda cursed, but it works
                            this.containerId += 1;  // Save server some calculations

                            // Update messed inventory data
                            player.containerMenu.getSlot(destinationSlot).set(ItemStack.EMPTY);
                            player.containerMenu.getSlot(slotNum).set(stackToMove);
                        }
                    }
                }
            });
        }
    }
}
