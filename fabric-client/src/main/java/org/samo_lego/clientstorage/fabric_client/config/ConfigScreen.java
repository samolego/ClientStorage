package org.samo_lego.clientstorage.fabric_client.config;

import dev.isxander.yacl.api.ButtonOption;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.ActionController;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.cycling.EnumController;
import dev.isxander.yacl.gui.controllers.slider.DoubleSliderController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.inventory.ItemBehaviour;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;
import org.samo_lego.clientstorage.fabric_client.util.ESPRender;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

public class ConfigScreen {

    /**
     * Creates config screen for the mod with the help
     * of awesome YACLibrary.
     *
     * @param parent parent screen.
     * @return config screen.
     */
    public static Screen createConfigScreen(@Nullable Screen parent) {
        var builder = YetAnotherConfigLib
                .createBuilder()
                .title(Component.translatable("mco.configure.world.settings.title"))
                .save(config::save);


        var mainCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.general"));

        var displayCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.display"));

        var messageCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.messages"));

        var serverSyncCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.server_sync"));

        var serverConfigCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.server_config"));

        var customLimiterCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.custom_limiter"));


        var storagePresetsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.storage_presets"));

        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("key.clientstorage.toggle_mod"))
                .binding(true, () -> config.enabled, value -> config.enabled = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.enable_caching"))
                .tooltip(Component.translatable("tooltip.clientstorage.enable_caching"))
                .binding(true, () -> config.enableCaching, value -> config.enableCaching = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.focus_search"))
                .tooltip(Component.translatable("tooltip.clientstorage.focus_search"))
                .binding(false, () -> config.focusSearchBar, value -> config.focusSearchBar = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(double.class)
                .name(Component.translatable("settings.clientstorage.max_distance"))
                .tooltip(Component.translatable("tooltip.clientstorage.max_distance"))
                .binding(Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE), () -> config.maxDist, value -> config.maxDist = value)
                .controller(opt -> new DoubleSliderController(opt, 1, 6, 0.5))
                .build());


        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.allow_item_transfers"))
                .tooltip(Component.translatable("tooltip.clientstorage.allow_item_transfers"))
                .binding(true, () -> config.enableItemTransfers, value -> config.enableItemTransfers = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.enable_block_search"))
                .tooltip(Component.translatable("tooltip.clientstorage.enable_block_search"))
                .binding(true, () -> config.enableBlocks, value -> config.enableBlocks = value)
                .controller(TickBoxController::new)
                .build());

        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.enable_entity_search"))
                .tooltip(Component.translatable("tooltip.clientstorage.enable_entity_search"))
                .binding(true, () -> config.enableEntities, value -> config.enableEntities = value)
                .controller(TickBoxController::new)
                .build());


        // Display
        displayCategory.option(Option.createBuilder(ItemBehaviour.ItemDisplayType.class)
                .name(Component.translatable("settings.clientstorage.merge_same_stacks"))
                .tooltip(Component.translatable("tooltip.clientstorage.merge_same_stacks"))
                .binding(ItemBehaviour.ItemDisplayType.MERGE_ALL, () -> config.itemDisplayType, value -> config.itemDisplayType = value)
                .controller(EnumController::new)
                .build());


        displayCategory.option(Option.createBuilder(ItemBehaviour.ItemDataTooltip.class)
                .name(Component.translatable("settings.clientstorage.additional_tooltip"))
                .tooltip(Component.translatable("tooltip.clientstorage.additional_tooltip"))
                .binding(ItemBehaviour.ItemDataTooltip.ALWAYS_SHOW, () -> config.locationTooltip, value -> config.locationTooltip = value)
                .controller(EnumController::new)
                .build());

        displayCategory.option(ButtonOption.createBuilder()
                .name(Component.translatable("settings.clientstorage.clear_esps"))
                .tooltip(Component.translatable("tooltip.clientstorage.clear_esps"))
                .action((yaclScreen, buttonOption) -> ESPRender.reset())
                .controller(ActionController::new)
                .build());


        // Messages
        messageCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.inform_server_type"))
                .tooltip(Component.translatable("tooltip.clientstorage.inform_server_type"))
                .binding(true, () -> config.informServerType, value -> config.informServerType = value)
                .controller(TickBoxController::new)
                .build());


        messageCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.inform_search"))
                .binding(true, () -> config.informSearch, value -> config.informSearch = value)
                .controller(TickBoxController::new)
                .build());

        messageCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("generator.minecraft.debug_all_block_states"))
                .binding(false, () -> config.debug, value -> config.debug = value)
                .controller(TickBoxController::new)
                .build());

        // Server sync
        final var serverSyncOption = Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.sync_server_config"))
                .tooltip(Component.translatable("tooltip.clientstorage.sync_server_config"))
                .binding(true, () -> config.allowSyncServer(), config::setAllowSyncServer)
                .controller(TickBoxController::new)
                .build();
        serverSyncOption.setAvailable(FabricConfig.isNotOnServer());  // Only allow in main menu
        serverSyncCategory.option(serverSyncOption);

        // Server config
        // Allow settings to be changed or not (depending on the server)
        final boolean allowSettings = FabricConfig.isNotOnServer() ||
                !config.hasServerSettings();

        // Look through blocks
        boolean allowThroughBlocks = allowSettings || config.allowEditLookThroughBlocks();
        String key = allowThroughBlocks ? "tooltip.clientstorage.through_block" : "tooltip.clientstorage.server_setting";
        Option<Boolean> throughBlocks = Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.through_block"))
                .tooltip(Component.translatable(key))
                .binding(true, config::lookThroughBlocks, config::setLookThroughBlocks)
                .controller(TickBoxController::new)
                .build();

        throughBlocks.setAvailable(allowThroughBlocks);
        serverConfigCategory.option(throughBlocks);


        // Custom limiter
        final var customDelayOption = Option.createBuilder(int.class)
                .name(Component.translatable("settings.clientstorage.custom_delay"))
                .tooltip(Component.translatable("tooltip.clientstorage.custom_delay"))
                .binding(300, PacketLimiter.CUSTOM::getDelay, PacketLimiter.CUSTOM::setDelay)
                .controller(opt -> new IntegerSliderController(opt, 0, 600, 1))
                .build();

        final var thresholdOption = Option.createBuilder(int.class)
                .name(Component.translatable("settings.clientstorage.packet_threshold"))
                .tooltip(Component.translatable("tooltip.clientstorage.packet_threshold"))
                .binding(4, PacketLimiter.CUSTOM::getThreshold, PacketLimiter.CUSTOM::setThreshold)
                .controller(opt -> new IntegerSliderController(opt, 1, 8, 1))
                .build();

        customLimiterCategory.option(Option.createBuilder(PacketLimiter.class)
                .name(Component.translatable("settings.clientstorage.limiter_type"))
                .binding(PacketLimiter.getServerLimiter(), () -> FabricConfig.limiter, value -> {
                    FabricConfig.limiter = value;

                    // Disable custom limiter category if not set to custom
                    if (value != PacketLimiter.CUSTOM) {
                        customDelayOption.setAvailable(false);
                        thresholdOption.setAvailable(false);
                    } else {
                        customDelayOption.setAvailable(true);
                        thresholdOption.setAvailable(true);
                    }
                })
                .controller(EnumController::new)
                .build());

        customDelayOption.setAvailable(FabricConfig.limiter == PacketLimiter.CUSTOM);
        thresholdOption.setAvailable(FabricConfig.limiter == PacketLimiter.CUSTOM);

        customLimiterCategory.option(customDelayOption);
        customLimiterCategory.option(thresholdOption);


        // Storage presets
        storagePresetsCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.enable_presets"))
                .tooltip(Component.translatable("tooltip.clientstorage.enable_presets"))
                .binding(true, () -> config.storageMemory.enabled, value -> config.storageMemory.enabled = value)
                .controller(TickBoxController::new)
                .build());

        storagePresetsCategory.option(ButtonOption.createBuilder()
                .name(Component.translatable("settings.clientstorage.delete_preset"))
                .tooltip(Component.translatable("tooltip.clientstorage.delete_preset"))
                .action((yaclScreen, buttonOption) -> config.storageMemory.clearForCurrentWorld())
                .controller(ActionController::new)
                .build());

        storagePresetsCategory.option(ButtonOption.createBuilder()
                .name(Component.translatable("settings.clientstorage.delete_presets"))
                .tooltip(Component.translatable("tooltip.clientstorage.delete_presets"))
                .action((yaclScreen, buttonOption) -> config.storageMemory.clearAll())
                .controller(ActionController::new)
                .build());


        // Append & build categories
        builder.category(mainCategory.build());
        builder.category(displayCategory.build());
        builder.category(messageCategory.build());
        builder.category(serverSyncCategory.build());
        builder.category(serverConfigCategory.build());
        builder.category(customLimiterCategory.build());
        builder.category(storagePresetsCategory.build());

        return builder.build().generateScreen(parent);
    }
}