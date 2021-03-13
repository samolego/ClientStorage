package org.samo_lego.clientstorage.mixin.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.clientstorage.casts.RemoteCrafting;
import org.samo_lego.clientstorage.inventory.RemoteInventory;
import org.samo_lego.clientstorage.mixin.accessor.ScreenHandlerAccessor;
import org.samo_lego.clientstorage.mixin.accessor.SlotAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin implements RemoteCrafting {

    private final CraftingScreenHandler screenHandler = (CraftingScreenHandler) (Object) this;
    private final RemoteInventory remoteInventory = new RemoteInventory(screenHandler, 9, 3);

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("RETURN"))
    private void constructor(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
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
        MinecraftClient.getInstance().world.blockEntities.forEach(blockEntity -> {
            // Check if within reach
            if(blockEntity.getPos().isWithinDistance(MinecraftClient.getInstance().player.getPos(), 5.0D) && blockEntity instanceof Inventory) {
                System.out.println("Found container: " + blockEntity.getPos()+", empty: "+ ((Inventory) blockEntity).isEmpty());
                if(!((Inventory) blockEntity).isEmpty()) {
                    for(int i = 0; i < ((Inventory) blockEntity).size(); ++i) {
                        ItemStack stack = ((Inventory) blockEntity).getStack(i);
                        remoteInventory.addStack(stack);
                    }
                }
            }
        });
    }
}
