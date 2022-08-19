package org.samo_lego.clientstorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import org.samo_lego.clientstorage.event.EventHandler;

import java.util.LinkedList;

public class ClientStorage implements ClientModInitializer {

	public static final LinkedList<BlockPos> INTERACTION_Q = new LinkedList<>();

	@Override
	public void onInitializeClient() {
		UseBlockCallback.EVENT.register(EventHandler::onUseBlock);
	}
}
