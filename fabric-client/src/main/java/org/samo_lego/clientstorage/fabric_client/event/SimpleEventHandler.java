package org.samo_lego.clientstorage.fabric_client.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.samo_lego.clientstorage.fabric_client.ClientStorageFabric;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;
import org.samo_lego.clientstorage.fabric_client.commands.CSearchCommand;
import org.samo_lego.clientstorage.fabric_client.config.ConfigScreen;
import org.samo_lego.clientstorage.fabric_client.inventory.ItemBehaviour;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;
import org.samo_lego.clientstorage.fabric_client.util.ESPRender;
import org.samo_lego.clientstorage.fabric_client.util.StorageCache;

import java.util.List;
import java.util.Optional;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

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

        UseEntityCallback.EVENT.register(this::onEntityInteract);
        ItemTooltipCallback.EVENT.register(this::onItemTooltip);

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientLoginConnectionEvents.INIT.register(this::onLogin);

        ClientCommandRegistrationCallback.EVENT.register(CSearchCommand::register);
        UseBlockCallback.EVENT.register(ContainerDiscovery::onUseBlock);
        WorldRenderEvents.LAST.register(ESPRender::onRender);
    }

    /**
     * Adds additional item tooltips for items that are stored in
     * remote containers, if certain conditions are met.
     *
     * @param stack       item stack
     * @param tooltipFlag tooltip flag
     * @param lines       tooltip lines
     */
    private void onItemTooltip(ItemStack stack, TooltipFlag tooltipFlag, List<Component> lines) {
        final var remote = (IRemoteStack) stack;
        // Only show tooltips if parent container is set and if player has crafting screen open
        if (remote.cs_getContainer() != null && Minecraft.getInstance().player.containerMenu instanceof CraftingMenu &&
                (config.locationTooltip == ItemBehaviour.ItemDataTooltip.ALWAYS_SHOW || (
                        config.locationTooltip == ItemBehaviour.ItemDataTooltip.REQUIRE_SHIFT && Screen.hasShiftDown() ||
                                config.locationTooltip == ItemBehaviour.ItemDataTooltip.REQUIRE_CTRL && Screen.hasControlDown() ||
                                config.locationTooltip == ItemBehaviour.ItemDataTooltip.REQUIRE_ALT && Screen.hasAltDown()))) {

            final int count = stack.getCount();
            final int maxStackSize = stack.getMaxStackSize();

            final boolean overstacked = count > maxStackSize && maxStackSize > 1;
            if (overstacked) {
                lines.add(Component.empty());  // Empty line
                // Split the count into multiple stacks
                var stackTooltip = Component.literal(count / maxStackSize + " * " + maxStackSize);

                int leftover = count % maxStackSize;
                if (leftover != 0) {
                    stackTooltip.append(" + " + leftover);
                }

                lines.add(stackTooltip.withStyle(ChatFormatting.GRAY));
            }

            if ((config.itemDisplayType != ItemBehaviour.ItemDisplayType.MERGE_ALL) || (count <= maxStackSize)) {
                if (!overstacked) {
                    lines.add(Component.empty());  // Empty line
                }

                var containerInfo = Component.literal(remote.cs_getContainer().cs_info());
                lines.add(containerInfo.withStyle(ChatFormatting.GRAY));
            }
        }
    }

    /**
     * Handles inventory closing.
     * If player has interacted with a container before,
     * the inventory is saved to the cache.
     */
    public static void onInventoryClose() {
        final var player = Minecraft.getInstance().player;

        Optional<InteractableContainer> container = ((ICSPlayer) player).cs_getLastInteractedContainer();

        container.ifPresent(inv -> {
            ClientStorageFabric.tryLog("Saving inventory to cache for " + inv.cs_info(), ChatFormatting.AQUA);
            final NonNullList<ItemStack> items = player.containerMenu.getItems();

            int emptySlots = 0;
            if (items.size() - 36 != inv.getContainerSize()) {
                ClientStorageFabric.tryLog("Mismatch inventory size. Got: " + (items.size() - 36) + ", world container: " + inv.getContainerSize(), ChatFormatting.RED);
            }
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack stack = items.get(i);

                inv.setItem(i, stack);

                if (stack.isEmpty()) {
                    ++emptySlots;
                }
            }


            if (emptySlots == 0) {
                StorageCache.FREE_SPACE_CONTAINERS.remove(inv);
            } else {
                StorageCache.FREE_SPACE_CONTAINERS.put(inv, emptySlots);
            }

            if (!inv.isEmpty()) {
                StorageCache.CACHED_INVENTORIES.add(inv);
            }
        });

        ((ICSPlayer) player).cs_setLastInteractedContainer(null);
    }

    private InteractionResult onEntityInteract(Player player, Level level, InteractionHand interactionHand, Entity entity, @Nullable EntityHitResult result) {
        ESPRender.removeEntity(entity);

        if (ContainerDiscovery.fakePacketsActive()) {
            ClientStorageFabric.tryLog("Fake packet mode is still active but shouldn't be!", ChatFormatting.RED);
        }

        if (entity instanceof InteractableContainer container) {
            ((ICSPlayer) player).cs_setLastInteractedContainer(container);
        } else {
            ((ICSPlayer) player).cs_setLastInteractedContainer(null);
        }

        return InteractionResult.PASS;
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
        if (ClientStorageFabric.config.allowSyncServer()) {
            ClientStorageFabric.config.clearServerSettings();
        } else {
            ClientStorageFabric.config.setStrictServerSettings();
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
            ClientStorageFabric.displayMessage(Component.translatable(message).withStyle(color));
        } else if (MOD_SETTINGS_KEY.consumeClick()) {
            client.setScreen(ConfigScreen.createConfigScreen(client.screen));
        }
    }
}
