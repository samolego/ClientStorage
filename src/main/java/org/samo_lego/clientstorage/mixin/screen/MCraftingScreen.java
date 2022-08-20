package org.samo_lego.clientstorage.mixin.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.casts.IRemoteCrafting;
import org.samo_lego.clientstorage.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.clientstorage.event.EventHandler.REMOTE_INV;
import static org.samo_lego.clientstorage.mixin.accessor.ACreativeModeInventoryScreen.CREATIVE_TABS_LOCATION;

@Environment(EnvType.CLIENT)
@Mixin(CraftingScreen.class)
public abstract class MCraftingScreen extends AbstractContainerScreen<CraftingMenu> implements ContainerEventHandler, IRemoteCrafting {

    @Unique
    private static final int Y_MOVE = 36;

    @Unique
    private EditBox searchBox;

    @Unique
    private final CraftingScreen self = (CraftingScreen) (Object) this;

    @Unique
    private static final ResourceLocation TEXTURE_SEARCH = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    @Unique
    private float scrollOffs;

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

        self.blit(matrices, (self.width - SEARCHBAR_WIDTH) / 2, y - SEARCHBAR_HEIGHT - 6, 0, 0, SEARCHBAR_WIDTH, SEARCHBAR_HEIGHT);
        self.blit(matrices, (self.width - SEARCHBAR_WIDTH) / 2, y - SEARCHBAR_BOTTOM_HEIGHT, 0, SEARCHBAR_BOTTOM_START, SEARCHBAR_WIDTH, SEARCHBAR_BOTTOM_HEIGHT);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(CallbackInfo ci) {
        this.titleLabelY += Y_MOVE;
        this.inventoryLabelY += Y_MOVE;

        if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.searchBox = this.getSearchBox(this.font, this.leftPos, this.topPos);
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(0xFFFFFF);
        this.addWidget(this.searchBox);
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void containerTick(CallbackInfo ci) {
        if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        String string = this.searchBox.getValue();

        if (this.searchBox.charTyped(chr, modifiers)) {
            if (!string.equals(this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String string = this.searchBox.getValue();
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!string.equals(this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        if (this.searchBox.isFocused() && keyCode != 256) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void refreshSearchResults() {
        REMOTE_INV.refreshSearchResults(this.searchBox.getValue());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int rows = REMOTE_INV.getRows();
        if (rows < 4) {
            return false;
        }

        float f = (float) (amount / (double) rows);
        this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0f, 1.0f);
        REMOTE_INV.scrollTo(this.scrollOffs);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int y = this.topPos - 23;
        int topY = y + 54;

        if (mouseY >= y && mouseY <= topY) {
            int rows = REMOTE_INV.getRows();
            if (rows > 3) {
                float amount = (float) ((mouseY - y) / (double) (topY - y));
                float f = Math.round(amount * rows) / (float) rows;
                this.scrollOffs = Mth.clamp(f, 0.0f, 1.0f);
                REMOTE_INV.scrollTo(this.scrollOffs);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Inject(method = "hasClickedOutside", at = @At("TAIL"), cancellable = true)
    private void hasClickedOutside(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
        boolean out = mouseX < (double) left || /*mouseY < (double) top ||*/ mouseX >= (double) (left + this.imageWidth) /*|| mouseY >= (double)(top + this.imageHeight)*/;
        cir.setReturnValue(out);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void slotClicked(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (slot instanceof RemoteSlot remoteSlot) {
            ItemStack item = remoteSlot.getItem();

            if (item.isEmpty()) {
                ItemStack carried = minecraft.player.containerMenu.getCarried();
                if (!carried.isEmpty()) {
                    remoteSlot.onPut(minecraft.player, carried);
                }
            } else {
                remoteSlot.onTake(minecraft.player, item);
            }
            ci.cancel();
        }
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        int x = this.leftPos + 165;
        int y = this.topPos - 23;
        int k = y + 54;


        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION());
        this.blit(matrices, x, y + (int) ((float) (k - y - 17) * this.scrollOffs), 232, 0, 12, 15);
    }

}
