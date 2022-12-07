package org.samo_lego.clientstorage.fabric_client.mixin.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteSlot;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.AScreen;
import org.samo_lego.clientstorage.fabric_client.util.ESPRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;
import static org.samo_lego.clientstorage.fabric_client.mixin.accessor.ACreativeModeInventoryScreen.CREATIVE_TABS_LOCATION;

@Environment(EnvType.CLIENT)
@Mixin(CraftingScreen.class)
public abstract class MCraftingScreen extends AbstractContainerScreen<CraftingMenu> implements ContainerEventHandler, AScreen {

    @Unique
    private static final int Y_MOVE = 36;

    @Unique
    private static final int SEARCHBAR_HEIGHT = 77;
    @Unique
    private static final int SEARCHBAR_BOTTOM_HEIGHT = 24;
    @Unique
    private static final int SEARCHBAR_BOTTOM_START = 111;
    @Unique
    private static final int SEARCHBAR_WIDTH = 195;

    @Unique
    private EditBox searchBox;

    @Unique
    private final CraftingScreen self = (CraftingScreen) (Object) this;

    @Unique
    private static final ResourceLocation TEXTURE_SEARCH = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    private ImageButton recipeBook;

    public MCraftingScreen(CraftingMenu craftingMenu, Inventory inventory, Component component) {
        super(craftingMenu, inventory, component);
    }

    @ModifyVariable(
            method = "renderBg(Lcom/mojang/blaze3d/vertex/PoseStack;FII)V",
            at = @At("STORE"),
            ordinal = 3
    )
    private int moveY(int defaultY) {
        if (!config.enabled) return defaultY;

        return defaultY + Y_MOVE;
    }

    @Inject(method = "renderBg(Lcom/mojang/blaze3d/vertex/PoseStack;FII)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addBackground(PoseStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci, int startX, int y) {
        if (!config.enabled) return;
        RenderSystem.setShaderTexture(0, TEXTURE_SEARCH);

        // Added inventory
        self.blit(matrices, startX, y - SEARCHBAR_HEIGHT, 0, 0, SEARCHBAR_WIDTH, SEARCHBAR_HEIGHT - SEARCHBAR_BOTTOM_HEIGHT);
        self.blit(matrices, startX, y - SEARCHBAR_BOTTOM_HEIGHT, 0, SEARCHBAR_BOTTOM_START, SEARCHBAR_WIDTH, SEARCHBAR_BOTTOM_HEIGHT);

        // Move recipe book down a bit
        this.recipeBook.setPosition(this.leftPos + 5, this.height / 2 - 49 + Y_MOVE);

        // Search bar
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        int topX = this.leftPos + 175;
        int topY = this.topPos - 23;
        int k = topY + 54;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // Scrollbar
        RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION());
        boolean canScroll = RemoteInventory.getInstance().getRows() > 3;
        this.blit(matrices, topX, topY + (int) ((float) (k - topY - 17) * RemoteInventory.getInstance().scrollOffset()), 232 + (canScroll ? 0 : 12), 0, 12, 15);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(CallbackInfo ci) {
        if (!config.enabled) return;

        this.titleLabelY += Y_MOVE;
        this.inventoryLabelY += Y_MOVE;

        if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (!config.enabled) return;

        // Move crafting book down for Y_MOVE
        List<Renderable> renderables = this.getRenderables();
        this.recipeBook = (ImageButton) renderables.get(renderables.size() - 1);
        this.recipeBook.setY(Y_MOVE + this.recipeBook.getY());

        this.searchBox = new EditBox(this.font, this.leftPos + 83, this.topPos - 35, 84, this.font.lineHeight, Component.translatable("itemGroup.search"));
        final String activeFilter = RemoteInventory.getInstance().getActiveFilter();
        this.searchBox.setFocus(config.focusSearchBar || !activeFilter.isEmpty());
        this.searchBox.setValue(activeFilter);
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(0xFFFFFF);
        this.addWidget(this.searchBox);
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void containerTick(CallbackInfo ci) {
        if (this.searchBox != null && config.enabled) {
            this.searchBox.tick();
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (config.enabled && this.searchBox.isFocused()) {
            String string = this.searchBox.getValue();

            if (this.searchBox.charTyped(chr, modifiers)) {
                if (!string.equals(this.searchBox.getValue())) {
                    this.refreshSearchResults();
                }
                return true;
            }
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (config.enabled && this.searchBox.isFocused()) {
            String string = this.searchBox.getValue();
            if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                if (!string.equals(this.searchBox.getValue())) {
                    this.refreshSearchResults();
                }
                return true;
            }

            if (keyCode != 256) {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void refreshSearchResults() {
        RemoteInventory.getInstance().refreshSearchResults(this.searchBox.getValue());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int rows = RemoteInventory.getInstance().getRows();
        if (rows < 4) {
            return false;
        }

        float f = (float) (amount / (double) rows);
        var scrollOffs = Mth.clamp(RemoteInventory.getInstance().scrollOffset() - f, 0.0f, 1.0f);
        RemoteInventory.getInstance().scrollTo(scrollOffs);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (config.enabled) {
            int y = this.topPos - 23;
            int topY = y + 54;

            int x = this.leftPos + 175;
            int maxX = x + 12;

            if (mouseY >= y && mouseY <= topY && mouseX >= x && mouseX <= maxX) {
                int rows = RemoteInventory.getInstance().getRows();
                if (rows > 3) {
                    float amount = (float) ((mouseY - y) / (double) (topY - y));
                    float f = Math.round(amount * rows) / (float) rows;
                    var scrollOffs = Mth.clamp(f, 0.0f, 1.0f);
                    RemoteInventory.getInstance().scrollTo(scrollOffs);
                    return true;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Inject(method = "hasClickedOutside", at = @At("TAIL"), cancellable = true)
    private void hasClickedOutside(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!config.enabled) return;

        boolean out = mouseX < (double) left || mouseX >= (double) (left + this.imageWidth);
        cir.setReturnValue(out);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void slotClicked(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (config.enabled) {
            if (slot instanceof RemoteSlot remoteSlot) {
                if (Screen.hasControlDown() || Screen.hasAltDown() || !config.enableItemTransfers) {
                    // Mark container
                    final ItemStack item = RemoteInventory.getInstance().getItem(slot.getContainerSlot());
                    BlockPos blockPos = ((IRemoteStack) item).cs_getContainer().getBlockPos();
                    ESPRender.markPos(blockPos);
                    ci.cancel();
                    return;
                }
                final ItemStack item = RemoteInventory.getInstance().removeItemNoUpdate(slot.getContainerSlot());
                final ItemStack carried = minecraft.player.containerMenu.getCarried();

                if (carried.isEmpty() && !item.isEmpty()) {
                    // Taking item out from remote inventory
                    remoteSlot.onTake(item, actionType);
                } else if (!carried.isEmpty() && item.isEmpty()) {
                    // Putting item into remote inventory
                    remoteSlot.onPut(carried);
                }
                ci.cancel();
            } else if (config.enableItemTransfers &&
                    actionType == ClickType.QUICK_MOVE &&
                    !(slot instanceof ResultSlot) &&
                    slot != null &&
                    !(slot.container instanceof CraftingContainer) &&
                    slot.hasItem()) {
                // Shift click item in the remote inventory
                ((IRemoteStack) slot.getItem()).cs_transfer2Remote(false, slotId);
                ci.cancel();
            }
        }
    }


}
