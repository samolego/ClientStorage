package org.samo_lego.clientstorage;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.samo_lego.clientstorage.config.Config;
import org.samo_lego.clientstorage.event.EventHandler;

import static org.samo_lego.clientstorage.event.EventHandler.resetFakePackets;

public class ClientStorage implements ClientModInitializer {

	public static final String MOD_ID = "clientstorage";
	public static final Component MOD_ID_MSG;
	public static final Config config;

	static {
		config = Config.load();
		MOD_ID_MSG = Component.literal("[").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("ClientStorage").withStyle(ChatFormatting.GOLD))
				.append(Component.literal("] ").withStyle(ChatFormatting.GRAY));
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
				config.enabled = !config.enabled;
				var color = config.enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
				var message = "addServer.resourcePack." + (config.enabled ? "enabled" : "disabled");

				resetFakePackets();
				displayMessage(Component.translatable(message).withStyle(color));
			}
			if (settingsBind.consumeClick()) {
				client.setScreen(Config.createConfigScreen(client.screen));
			}
		});

		ClientPlayConnectionEvents.JOIN.register((listener, sender, minecraft) -> resetFakePackets());
	}
}
