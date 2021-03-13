package org.samo_lego.clientstorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.samo_lego.clientstorage.cache.BlockEntityCache;
import org.samo_lego.clientstorage.event.EventHandler;
import org.samo_lego.clientstorage.gui.screen.StorageCraftingScreen;
import org.samo_lego.clientstorage.gui.screen.StorageCraftingScreenHandler;
import org.samo_lego.clientstorage.inventory.RemoteInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ClientStorage implements ClientModInitializer {

	public static final Map<BlockEntity, BlockEntityCache> BLOCK_CACHE = new HashMap<>();

	public static final PriorityQueue<BlockPos> INTERACTION_Q = new PriorityQueue<>();

	public static ScreenHandlerType<StorageCraftingScreenHandler> STORAGE_CRAFTING_SCREEN_HANDLER;

	@Override
	public void onInitializeClient() {
		UseBlockCallback.EVENT.register(EventHandler::onUseBlock);


		STORAGE_CRAFTING_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier("minecraft", "crafting_table"), StorageCraftingScreenHandler::new);
		ScreenRegistry.register(STORAGE_CRAFTING_SCREEN_HANDLER, StorageCraftingScreen::new);
	}
}
