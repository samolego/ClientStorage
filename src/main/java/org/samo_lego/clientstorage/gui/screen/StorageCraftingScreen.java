package org.samo_lego.clientstorage.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static org.samo_lego.clientstorage.ClientStorage.STORAGE_CRAFTING_SCREEN_HANDLER;


@Environment(EnvType.CLIENT)
public class StorageCraftingScreen extends HandledScreen<ScreenHandler> {

    private static final Identifier TEXTURE_CRAFTING = new Identifier("textures/gui/container/crafting_table.png");
    private static final Identifier TEXTURE_SEARCH = new Identifier("textures/gui/container/creative_inventory/tab_item_search.png");
    private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");
    private final RecipeBookWidget recipeBook = new RecipeBookWidget();
    private TextFieldWidget searchBox;
    private boolean narrow;

    public StorageCraftingScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        System.out.println("CUSTOM SCREEN");
    }
}
