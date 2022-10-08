package org.samo_lego.clientstorage.fabric_client.config;

import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.EnumController;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.slider.DoubleSliderController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

public class ConfigScreen {
    public static Screen createConfigScreen(@Nullable Screen parent) {
        var builder = YetAnotherConfigLib
                .createBuilder()
                .title(Component.translatable("mco.configure.world.settings.title"))
                .save(config::save);


        var mainCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.general"));

        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("key.clientstorage.toggle_mod"))
                .binding(true, () -> config.enabled, value -> config.enabled = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.inform_server_type"))
                .tooltip(Component.translatable("tooltip.clientstorage.inform_server_type"))
                .binding(true, () -> config.informServerType, value -> config.informServerType = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.inform_search"))
                .binding(true, () -> config.informSearch, value -> config.informSearch = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.enable_caching"))
                .tooltip(Component.translatable("tooltip.clientstorage.enable_caching"))
                .binding(true, () -> config.enableCaching, value -> config.enableCaching = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(PacketLimiter.class)
                .name(Component.translatable("settings.clientstorage.limiter_type"))
                .binding(PacketLimiter.getServerLimiter(), () -> FabricConfig.limiter, value -> FabricConfig.limiter = value)
                .controller(EnumController::new)
                .build());

        mainCategory.option(Option.createBuilder(double.class)
                .name(Component.translatable("settings.clientstorage.max_distance"))
                .tooltip(Component.translatable("tooltip.clientstorage.max_distance"))
                .binding(Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE), () -> config.maxDist, value -> config.maxDist = value)
                .controller(opt -> new DoubleSliderController(opt, 1, 6, 0.5))
                .build());


        // Allow settings to be changed or not (depending on the server)
        final boolean allowSettings = Minecraft.getInstance().getCurrentServer() == null || !config.hasServerSettings();

        // Look through blocks
        boolean allowThroughBlocks = allowSettings || config.lookThroughBlocks();
        String key = allowThroughBlocks ? "tooltip.clientstorage.through_block" : "tooltip.clientstorage.server_setting";
        Option<Boolean> throughBlocks = Option.createBuilder(boolean.class)
                .name(Component.translatable("settings.clientstorage.through_block"))
                .tooltip(Component.translatable(key))
                .binding(true, config::lookThroughBlocks, config::setLookThroughBlocks)
                .controller(TickBoxController::new)
                .build();

        throughBlocks.setAvailable(allowThroughBlocks);
        mainCategory.option(throughBlocks);


        var customLimiterCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.custom_limiter"));

        customLimiterCategory.option(Option.createBuilder(int.class)
                .name(Component.translatable("settings.clientstorage.custom_delay"))
                .tooltip(Component.translatable("tooltip.clientstorage.custom_delay"))
                .binding(300, PacketLimiter.CUSTOM::getDelay, PacketLimiter.CUSTOM::setDelay)
                .controller(opt -> new IntegerSliderController(opt, 0, 600, 1))
                .build());


        customLimiterCategory.option(Option.createBuilder(int.class)
                .name(Component.translatable("settings.clientstorage.packet_threshold"))
                .tooltip(Component.translatable("tooltip.clientstorage.packet_threshold"))
                .binding(4, PacketLimiter.CUSTOM::getThreshold, PacketLimiter.CUSTOM::setThreshold)
                .controller(opt -> new IntegerSliderController(opt, 1, 8, 1))
                .build());


        builder.category(mainCategory.build());
        builder.category(customLimiterCategory.build());

        return builder.build().generateScreen(parent);
    }
}