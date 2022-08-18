package org.samo_lego.clientstorage.mixin.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.casts.IRemoteCrafting;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.samo_lego.clientstorage.inventory.RemoteInventory;
import org.samo_lego.clientstorage.inventory.RemoteSlot;
import org.samo_lego.clientstorage.mixin.accessor.AScreenHandler;
import org.samo_lego.clientstorage.mixin.accessor.ASlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;

@Mixin(CraftingMenu.class)
public class MCraftingScreenHandler implements IRemoteCrafting {

    private final CraftingMenu screenHandler = (CraftingMenu) (Object) this;
    private final RemoteInventory remoteInventory = new RemoteInventory(screenHandler);

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void constructor(int syncId, Inventory playerInventory, ContainerLevelAccess context, CallbackInfo ci) {
        // Moving slots down
        screenHandler.slots.forEach(slot -> ((ASlot) slot).setY(slot.y + 36));

        for (int m = 0; m < 3; ++m) {
            for(int l = 0; l < 9; ++l) {
                ((AScreenHandler) screenHandler).cs_addSlot(new RemoteSlot(this.remoteInventory, l + m * 9, l * 18 - 1, m * 18 - 23));
            }
        }
    }


    @Override
    public RemoteInventory getRemoteInventory() {
        return this.remoteInventory;
    }

    @Override
    public void refreshRemoteInventory() {
        LocalPlayer player = Minecraft.getInstance().player;
        player.getLevel().getChunkAt(player.blockPosition()).getBlockEntities().forEach((position, blockEntity) -> {
            // Check if within reach
            System.out.println("Checking " + position);
            if (blockEntity instanceof Container && player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position)) < MAX_INTERACTION_DISTANCE) {
                System.out.println("Found container: " + position+", empty: "+ ((Container) blockEntity).isEmpty());
                if (!((Container) blockEntity).isEmpty()) {
                    for (int i = 0; i < ((Container) blockEntity).getContainerSize(); ++i) {
                        ItemStack stack = ((Container) blockEntity).getItem(i);

                        if (!stack.isEmpty()) {
                            this.remoteInventory.addStack(IRemoteStack.fromStack(stack, blockEntity, i));
                        }
                    }
                }
            }
        });
    }
}
