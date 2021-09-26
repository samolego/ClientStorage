package org.samo_lego.clientstorage.mixin.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.casts.RemoteCrafting;
import org.samo_lego.clientstorage.inventory.RemoteInventory;
import org.samo_lego.clientstorage.mixin.accessor.ScreenHandlerAccessor;
import org.samo_lego.clientstorage.mixin.accessor.SlotAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingScreenHandlerMixin implements RemoteCrafting {

    private final CraftingMenu screenHandler = (CraftingMenu) (Object) this;
    private final RemoteInventory remoteInventory = new RemoteInventory(screenHandler, 9, 3);

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void constructor(int syncId, Inventory playerInventory, ContainerLevelAccess context, CallbackInfo ci) {
        // Moving slots down
        screenHandler.slots.forEach(slot -> ((SlotAccessor) slot).setY(slot.y + 36));

        for(int m = 0; m < remoteInventory.getHeight(); ++m) {
            for(int l = 0; l < remoteInventory.getWidth(); ++l) {
                ((ScreenHandlerAccessor) screenHandler).addSlotToHandler(new Slot(this.remoteInventory, l + m * 9, l * 18 - 1, m * 18 - 23));
            }
        }
        //refreshRemoteInventory();
        System.out.println("Creating GUI");
    }


    @Override
    public RemoteInventory getRemoteInventory() {
        return this.remoteInventory;
    }

    @Override
    public void refreshRemoteInventory() {
        BlockPos position = Minecraft.getInstance().player.blockPosition();
        Minecraft.getInstance().level.getChunkAt(position).getBlockEntities().forEach((pos, blockEntity) -> {
            // Check if within reach
            if(pos.closerThan(position, 5.0D) && blockEntity instanceof Container) {
                System.out.println("Found container: " + pos+", empty: "+ ((Container) blockEntity).isEmpty());
                if(!((Container) blockEntity).isEmpty()) {
                    for(int i = 0; i < ((Container) blockEntity).getContainerSize(); ++i) {
                        ItemStack stack = ((Container) blockEntity).getItem(i);
                        remoteInventory.addStack(stack);
                    }
                }
            }
        });
    }
}
