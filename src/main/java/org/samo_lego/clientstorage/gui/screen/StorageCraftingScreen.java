package org.samo_lego.clientstorage.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static org.samo_lego.clientstorage.ClientStorage.STORAGE_CRAFTING_SCREEN_HANDLER;

import com.mojang.blaze3d.vertex.PoseStack;


@Environment(EnvType.CLIENT)
public class StorageCraftingScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    private static final ResourceLocation TEXTURE_CRAFTING = new ResourceLocation("textures/gui/container/crafting_table.png");
    private static final ResourceLocation TEXTURE_SEARCH = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
    private final RecipeBookComponent recipeBook = new RecipeBookComponent();
    private EditBox searchBox;
    private boolean narrow;

    public StorageCraftingScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        System.out.println("CUSTOM SCREEN");
    }
}
