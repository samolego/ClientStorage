package org.samo_lego.clientstorage.fabric_client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.clientstorage.common.ClientStorage;
import org.samo_lego.clientstorage.common.Config;
import org.samo_lego.clientstorage.fabric_client.config.FabricConfig;
import org.samo_lego.clientstorage.fabric_client.event.ContainerDiscovery;
import org.samo_lego.clientstorage.fabric_client.event.SimpleEventHandler;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;
import org.samo_lego.clientstorage.fabric_client.util.ESPRender;

public class ClientStorageFabric implements ClientModInitializer {
	public static final Component MOD_ID_MSG;
	public static FabricConfig config;
	public final static ResourceLocation SERVER_CONFIG_CHANNEL;

	static {
		MOD_ID_MSG = Component.literal("[").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("ClientStorage").withStyle(ChatFormatting.GOLD))
				.append(Component.literal("] ").withStyle(ChatFormatting.GRAY));
		SERVER_CONFIG_CHANNEL = new ResourceLocation(ClientStorage.NETWORK_CHANNEL);
		new RemoteInventory();
	}

	public static void displayMessage(String translationKey) {
		displayMessage(Component.translatable(translationKey).withStyle(ChatFormatting.GRAY));
	}

	public static void displayMessage(Component message) {
		LocalPlayer player = Minecraft.getInstance().player;
		player.displayClientMessage(MOD_ID_MSG.copy().append(message), false);
	}

	@Override
	public void onInitializeClient() {
		new ClientStorage(FabricLoader.getInstance().getConfigDir().toFile(), ClientStorage.Platform.FABRIC);
		config = Config.load(FabricConfig.class, FabricConfig::new);

		final var evtHandler = new SimpleEventHandler();

		ClientTickEvents.END_CLIENT_TICK.register(evtHandler::onClientTick);
		ClientLoginConnectionEvents.INIT.register(evtHandler::onLogin);

		UseBlockCallback.EVENT.register(ContainerDiscovery::onUseBlock);

		if (config.allowSyncServer()) {
			ClientPlayNetworking.registerGlobalReceiver(SERVER_CONFIG_CHANNEL, (client, handler, buf, responseSender) -> config.unpack(buf));
		}

		WorldRenderEvents.LAST.register(ESPRender::onRender);
	}
}
