package org.samo_lego.clientstorage.fabric_client.config;

import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.EnumController;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.slider.DoubleSliderController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.compatibility.StashSupport;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;
import org.samo_lego.clientstorage.fabric_client.util.ItemDisplayType;
import org.samo_lego.clientstorage.fabric_client.util.ItemLocationTooltip;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

public class ConfigScreen {
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

        var modCompatibility = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.mod_compatibility"));

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



        // Display
        displayCategory.option(Option.createBuilder(ItemDisplayType.class)
                .name(Component.translatable("settings.clientstorage.merge_same_stacks"))
                .tooltip(Component.translatable("tooltip.clientstorage.merge_same_stacks"))
                .binding(ItemDisplayType.MERGE_ALL, () -> config.itemDisplayType, value -> config.itemDisplayType = value)
                .controller(EnumController::new)
                .build());


        displayCategory.option(Option.createBuilder(ItemLocationTooltip.class)
                .name(Component.translatable("settings.clientstorage.location_tooltip"))
                .tooltip(Component.translatable("tooltip.clientstorage.location_tooltip"))
                .binding(ItemLocationTooltip.ALWAYS_SHOW, () -> config.locationTooltip, value -> config.locationTooltip = value)
                .controller(EnumController::new)
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

        // Server sync
        final var serverSyncOption = Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.sync_server_config"))
                .tooltip(Component.translatable("tooltip.clientstorage.sync_server_config"))
                .binding(true, () -> config.allowSyncServer(), config::setAllowSyncServer)
                .controller(TickBoxController::new)
                .build();
        serverSyncOption.setAvailable(!FabricConfig.isPlayingServer());  // Only allow in main menu
        serverSyncCategory.option(serverSyncOption);

        // Server config
        // Allow settings to be changed or not (depending on the server)
        final boolean allowSettings = !FabricConfig.isPlayingServer() ||
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

                    // Disable custom limiter options if not enabled
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


        // Mod compatibility
        modCompatibility.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.enable_stash_support"))
                .tooltip(Component.translatable("tooltip.clientstorage.enable_stash_support"))
                .binding(true, () -> config.stashes, value -> {
                    if (value) {
                        config.stashes = true;
                        StashSupport.enable();
                    } else {
                        config.stashes = false;
                        StashSupport.disable();
                    }
                })
                .controller(TickBoxController::new)
                .build());

        // Append & build categories
        builder.category(mainCategory.build());
        builder.category(displayCategory.build());
        builder.category(messageCategory.build());
        builder.category(serverSyncCategory.build());
        builder.category(serverConfigCategory.build());
        builder.category(customLimiterCategory.build());
        builder.category(modCompatibility.build());

        return builder.build().generateScreen(parent);
    }
}