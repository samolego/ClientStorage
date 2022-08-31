package org.samo_lego.clientstorage;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;
import org.samo_lego.clientstorage.event.EventHandler;

import java.util.concurrent.LinkedBlockingDeque;

import static org.samo_lego.clientstorage.event.EventHandler.resetFakePackets;

public class ClientStorage implements ClientModInitializer {

	public static final LinkedBlockingDeque<BlockHitResult> INTERACTION_Q = new LinkedBlockingDeque<>();
	public static boolean enabled = true;

	@Override
	public void onInitializeClient() {
		UseBlockCallback.EVENT.register(EventHandler::onUseBlock);

		var keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.clientstorage.toggle_mod",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN, // Not bound
				"key.categories.inventory"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (keyBinding.consumeClick()) {
				enabled = !enabled;
				var color = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
				var message = "addServer.resourcePack." + (enabled ? "enabled" : "disabled");

				resetFakePackets();

				client.player.sendSystemMessage(Component.literal("[ClientStorage] ")
						.withStyle(ChatFormatting.LIGHT_PURPLE)
						.append(Component.translatable(message).withStyle(color)));
			}
		});

		ClientLoginConnectionEvents.INIT.register((handler, client) -> {
			enabled = true;
			resetFakePackets();
		});
	}

}
