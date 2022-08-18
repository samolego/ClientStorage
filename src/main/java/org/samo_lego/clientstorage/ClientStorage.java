package org.samo_lego.clientstorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.samo_lego.clientstorage.cache.BlockEntityCache;
import org.samo_lego.clientstorage.event.EventHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

public class ClientStorage implements ClientModInitializer {

	public static final Map<BlockEntity, BlockEntityCache> BLOCK_CACHE = new HashMap<>();

	public static final LinkedList<BlockPos> INTERACTION_Q = new LinkedList<>();

	@Override
	public void onInitializeClient() {
		UseBlockCallback.EVENT.register(EventHandler::onUseBlock);

		//STORAGE_CRAFTING_SCREEN_HANDLER = ScreenProviderRegistryImpl.INSTANCE.registerFactory(new ResourceLocation("minecraft", "crafting_table"), StorageCraftingScreenHandler::new);
		//ScreenProviderRegistryImpl.register(STORAGE_CRAFTING_SCREEN_HANDLER, StorageCraftingScreen::new);
	}
}
