package org.samo_lego.clientstorage.fabric_client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.samo_lego.clientstorage.common.ClientStorage;
import org.samo_lego.clientstorage.common.Config;
import org.samo_lego.clientstorage.fabric_client.config.ConfigScreen;
import org.samo_lego.clientstorage.fabric_client.config.FabricConfig;
import org.samo_lego.clientstorage.fabric_client.event.EventHandler;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;

import static org.samo_lego.clientstorage.fabric_client.event.EventHandler.resetFakePackets;

public class ClientStorageFabric implements ClientModInitializer {
	public static final Component MOD_ID_MSG;
	public static FabricConfig config;

	static {
		MOD_ID_MSG = Component.literal("[").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("ClientStorage").withStyle(ChatFormatting.GOLD))
				.append(Component.literal("] ").withStyle(ChatFormatting.GRAY));

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

		UseBlockCallback.EVENT.register(EventHandler::onUseBlock);

		var toggleBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.clientstorage.toggle_mod",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN, // Not bound
				"clientstorage.category"
		));

		var settingsBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.clientstorage.open_settings",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN, // Not bound
				"clientstorage.category"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (toggleBind.consumeClick()) {
				ClientStorageFabric.config.enabled = !ClientStorageFabric.config.enabled;
				var color = ClientStorageFabric.config.enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
				var message = "addServer.resourcePack." + (ClientStorageFabric.config.enabled ? "enabled" : "disabled");

				resetFakePackets();
				displayMessage(Component.translatable(message).withStyle(color));
			} else if (settingsBind.consumeClick()) {
				client.setScreen(ConfigScreen.createConfigScreen(client.screen));
			}
		});


        final var channel = new ResourceLocation(ClientStorage.NETWORK_CHANNEL);
        ClientPlayNetworking.registerGlobalReceiver(channel, (client, handler, buf, responseSender) -> config.unpack(buf));
        ClientPlayConnectionEvents.JOIN.register((listener, sender, minecraft) -> resetFakePackets());
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> config.clearServerSettings());
	}
}
