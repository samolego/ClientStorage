package org.samo_lego.clientstorage.fabric_client.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lwjgl.glfw.GLFW;
import org.samo_lego.clientstorage.fabric_client.ClientStorageFabric;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.commands.CSearchCommand;
import org.samo_lego.clientstorage.fabric_client.config.ConfigScreen;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.ACompoundContainer;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;
import org.samo_lego.clientstorage.fabric_client.util.ESPRender;
import org.samo_lego.clientstorage.fabric_client.util.StorageCache;

import java.util.Optional;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;
import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.displayMessage;

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

    /**
     * Handles inventory closing.
     * If player has interacted with a container before,
     * the inventory is saved to the cache.
     */
    public static void onInventoryClose() {
        final var player = Minecraft.getInstance().player;
        Optional<Container> container = ((ICSPlayer) player).cs_getLastInteractedContainer();

        container.ifPresent(inv -> {
            final NonNullList<ItemStack> items = player.containerMenu.getItems();

            int emptySlots = 0;
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack stack = items.get(i);

                inv.setItem(i, stack);

                if (stack.isEmpty()) {
                    ++emptySlots;
                }
            }

            Container[] containers = new Container[2];
            if (inv instanceof CompoundContainer cc) {
                containers[0] = ((ACompoundContainer) cc).getContainer1();
                containers[1] = ((ACompoundContainer) cc).getContainer2();
            } else {
                containers[0] = containers[1] = inv;
            }

            if (emptySlots == 0) {
                StorageCache.FREE_SPACE_CONTAINERS.remove(((BlockEntity) containers[0]).getBlockPos());
                StorageCache.FREE_SPACE_CONTAINERS.remove(((BlockEntity) containers[1]).getBlockPos());
            } else {
                StorageCache.FREE_SPACE_CONTAINERS.put(((BlockEntity) containers[0]).getBlockPos(), emptySlots);
                StorageCache.FREE_SPACE_CONTAINERS.put(((BlockEntity) containers[1]).getBlockPos(), emptySlots);
            }
        });
    }

    /**
     * Resets mod state on player login.
     *
     * @param listener  client handshake packet listener
     * @param minecraft client instance
     */
    public void onLogin(ClientHandshakePacketListenerImpl listener, Minecraft minecraft) {
        ContainerDiscovery.resetFakePackets();
        ESPRender.reset();
        PacketLimiter.resetServerStatus();
        if (config.allowSyncServer()) {
            config.clearServerSettings();
        } else {
            config.setStrictServerSettings();
        }
    }

    /**
     * Used for detecting the keybind presses.
     *
     * @param client client instance.
     */
    public void onClientTick(Minecraft client) {
        if (TOGGLE_MOD_KEY.consumeClick()) {
            ClientStorageFabric.config.enabled = !ClientStorageFabric.config.enabled;
            var color = ClientStorageFabric.config.enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
            var message = "addServer.resourcePack." + (ClientStorageFabric.config.enabled ? "enabled" : "disabled");

            ContainerDiscovery.resetFakePackets();
            displayMessage(Component.translatable(message).withStyle(color));
        } else if (MOD_SETTINGS_KEY.consumeClick()) {
            client.setScreen(ConfigScreen.createConfigScreen(client.screen));
        }
    }

    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientLoginConnectionEvents.INIT.register(this::onLogin);

        ClientCommandRegistrationCallback.EVENT.register(CSearchCommand::register);
        UseBlockCallback.EVENT.register(ContainerDiscovery::onUseBlock);
        WorldRenderEvents.LAST.register(ESPRender::onRender);

    }
}
