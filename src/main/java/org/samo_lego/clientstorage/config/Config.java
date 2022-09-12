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
    public static PacketLimiter limiter = PacketLimiter.VANILLA;

    public boolean informServerType = true;
    public boolean enabled = true;
    public boolean informSearch = true;

    public PacketLimiter customLimiter = PacketLimiter.CUSTOM;
    public boolean enableCaching = true;


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
                .binding(true, () -> config.enableCaching, value -> config.enableCaching = value)
                .controller(TickBoxController::new)
                .build());


        mainCategory.option(Option.createBuilder(PacketLimiter.class)
                .name(Component.translatable("settings.clientstorage.limiter_type"))
                .binding(PacketLimiter.getServerLimiter(), () -> config.customLimiter, value -> config.customLimiter = value)
                .controller(EnumController::new)
                .build());


        var customLimiterCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("category.clientstorage.custom_limiter"));

        customLimiterCategory.option(Option.createBuilder(int.class)
                .name(Component.translatable("settings.clientstorage.custom_delay"))
                .tooltip(Component.translatable("tooltip.clientstorage.custom_delay"))
                .binding(config.customLimiter.getDelay(), () -> config.customLimiter.getDelay(), value -> config.customLimiter.setDelay(value))
                .controller(opt -> new IntegerSliderController(opt, 0, 600, 1))
                .build());


        customLimiterCategory.option(Option.createBuilder(int.class)
                .name(Component.translatable("settings.clientstorage.packet_threshold"))
                .tooltip(Component.translatable("tooltip.clientstorage.packet_threshold"))
                .binding(config.customLimiter.getThreshold(), () -> config.customLimiter.getThreshold(), value -> config.customLimiter.setThreshold(value))
                .controller(opt -> new IntegerSliderController(opt, 0, 8, 1))
                .build());


        builder.category(mainCategory.build());
        builder.category(customLimiterCategory.build());

        return builder.build().generateScreen(parent);
    }


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

        return newConfig;
    }

    public void save() {
        try (var writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            getLogger(MOD_ID).error("Problem occurred when saving config: " + e.getMessage());
        }
    }
}
