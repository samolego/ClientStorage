package org.samo_lego.clientstorage.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.EnumController;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.network.PacketLimiter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
import static org.samo_lego.clientstorage.ClientStorage.MOD_ID;
import static org.samo_lego.clientstorage.ClientStorage.config;
import static org.slf4j.LoggerFactory.getLogger;

public class Config {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir() + "/client_storage.json");
    public int maxDist = (int) Math.sqrt(MAX_INTERACTION_DISTANCE);
    public static PacketLimiter limiter = PacketLimiter.VANILLA;

    public boolean informServerType = true;
    public boolean enabled = true;
    public boolean informSearch = true;

    public CustomLimiter customLimiter = new CustomLimiter();

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
                .binding(PacketLimiter.getServerLimiter(), () -> Config.limiter, value -> Config.limiter = value)
                .controller(EnumController::new)
                .build());

        mainCategory.option(Option.createBuilder(int.class)
                .name(Component.translatable("settings.clientstorage.max_distance"))
                .tooltip(Component.translatable("tooltip.clientstorage.max_distance"))
                .binding((int) Math.sqrt(MAX_INTERACTION_DISTANCE), () -> config.maxDist, value -> config.maxDist = value)
                .controller(opt -> new IntegerSliderController(opt, 1, 6, 1))
                .build());


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

    public boolean enableCaching = true;

    public static Config load() {
        Config newConfig = null;
        if (CONFIG_FILE.exists()) {
            try (var fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)
            )) {
                newConfig = GSON.fromJson(fileReader, Config.class);
            } catch (IOException e) {
                throw new RuntimeException(MOD_ID + " Problem occurred when trying to load config: ", e);
            }
        }
        if (newConfig == null)
            newConfig = new Config();

        newConfig.save();

        PacketLimiter.CUSTOM.setDelay(newConfig.customLimiter.delay);
        PacketLimiter.CUSTOM.setThreshold(newConfig.customLimiter.threshold);

        return newConfig;
    }

    public void save() {
        this.customLimiter.delay = PacketLimiter.CUSTOM.getDelay();
        this.customLimiter.threshold = PacketLimiter.CUSTOM.getThreshold();

        try (var writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            getLogger(MOD_ID).error("Problem occurred when saving config: " + e.getMessage());
        }
    }

    public static class CustomLimiter {
        public int delay = 300;
        public int threshold = 4;
    }
}
