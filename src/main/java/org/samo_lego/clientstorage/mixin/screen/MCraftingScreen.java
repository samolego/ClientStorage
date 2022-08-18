package org.samo_lego.clientstorage.mixin.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.samo_lego.clientstorage.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(CraftingScreen.class)
public abstract class MCraftingScreen extends AbstractContainerScreen<CraftingMenu> {

    private static final int Y_MOVE = 36;

    private final CraftingScreen craftingScreen = (CraftingScreen) (Object) this;
    private static final ResourceLocation TEXTURE_SEARCH = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");

    public MCraftingScreen(CraftingMenu craftingMenu, Inventory inventory, Component component) {
        super(craftingMenu, inventory, component);
    }

    @ModifyVariable(
            method = "renderBg(Lcom/mojang/blaze3d/vertex/PoseStack;FII)V",
            at = @At("STORE"),
            ordinal = 3
    )
    private int moveY(int l) {
        return l + Y_MOVE;
    }

    @Inject(method = "renderBg(Lcom/mojang/blaze3d/vertex/PoseStack;FII)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addBackground(PoseStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci, int startX, int y) {
        //RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        //Minecraft.getInstance().getTextureManager().bindForSetup(TEXTURE_SEARCH);
        RenderSystem.setShaderTexture(0, TEXTURE_SEARCH);
        final int SEARCHBAR_HEIGHT = 71;
        final int SEARCHBAR_BOTTOM_HEIGHT = 24;
        final int SEARCHBAR_BOTTOM_START = 111;
        final int SEARCHBAR_WIDTH = 195;
        //((HandledScreenAccessor) craftingScreen).setBackgroundHeight(SEARCHBAR_HEIGHT);

        craftingScreen.blit(matrices, (craftingScreen.width - SEARCHBAR_WIDTH) / 2, y - SEARCHBAR_HEIGHT - 6, 0, 0, SEARCHBAR_WIDTH, SEARCHBAR_HEIGHT);
        craftingScreen.blit(matrices, (craftingScreen.width - SEARCHBAR_WIDTH) / 2, y - SEARCHBAR_BOTTOM_HEIGHT, 0, SEARCHBAR_BOTTOM_START, SEARCHBAR_WIDTH, SEARCHBAR_BOTTOM_HEIGHT);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(CallbackInfo ci) {
        this.titleLabelY += Y_MOVE;
        this.inventoryLabelY += Y_MOVE;
    }

    @Inject(method = "hasClickedOutside", at = @At("TAIL"), cancellable = true)
    private void hasClickedOutside(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
        boolean out = mouseX < (double) left || /*mouseY < (double) top ||*/ mouseX >= (double)(left + this.imageWidth) /*|| mouseY >= (double)(top + this.imageHeight)*/;
        cir.setReturnValue(out);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void slotClicked(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (slot instanceof RemoteSlot remoteSlot) {
            ItemStack item = remoteSlot.getItem();
            System.out.println("Stack " + item + " clicked (in " + ((IRemoteStack) (Object) item).cs_getContainer() + ")");
            remoteSlot.onTake(minecraft.player, item);
            ci.cancel();
        }
    }
}
