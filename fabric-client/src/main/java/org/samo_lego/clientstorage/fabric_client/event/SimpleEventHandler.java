package org.samo_lego.clientstorage.fabric_client.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.samo_lego.clientstorage.fabric_client.ClientStorageFabric;
import org.samo_lego.clientstorage.fabric_client.config.ConfigScreen;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;
import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.displayMessage;
import static org.samo_lego.clientstorage.fabric_client.event.ContainerDiscovery.resetFakePackets;

public class SimpleEventHandler {

    private final KeyMapping TOGGLE_MOD_KEY;
    private final KeyMapping MOD_SETTINGS_KEY;

    public SimpleEventHandler() {
        TOGGLE_MOD_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.clientstorage.toggle_mod",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // Not bound
                "clientstorage.category"
        ));

        MOD_SETTINGS_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.clientstorage.open_settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // Not bound
                "clientstorage.category"
        ));
    }

    public void onLogin(ClientHandshakePacketListenerImpl listener, Minecraft minecraft) {
        resetFakePackets();
        if (config.allowSyncServer()) {
            config.clearServerSettings();
        } else {
            config.setStrictServerSettings();
        }
    }

    public void onClientTick(Minecraft client) {
        if (TOGGLE_MOD_KEY.consumeClick()) {
            ClientStorageFabric.config.enabled = !ClientStorageFabric.config.enabled;
            var color = ClientStorageFabric.config.enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
            var message = "addServer.resourcePack." + (ClientStorageFabric.config.enabled ? "enabled" : "disabled");

            resetFakePackets();
            displayMessage(Component.translatable(message).withStyle(color));
        } else if (MOD_SETTINGS_KEY.consumeClick()) {
            client.setScreen(ConfigScreen.createConfigScreen(client.screen));
        }
    }
}
