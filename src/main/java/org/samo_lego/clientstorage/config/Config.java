package org.samo_lego.clientstorage.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.util.PacketLimiter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

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


    public static Screen createConfigScreen(@Nullable Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("mco.configure.world.settings.title"));

        builder.setSavingRunnable(config::save);

        ConfigCategory mainCategory = builder.getOrCreateCategory(Component.translatable("category.clientstorage.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();


        mainCategory.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("key.clientstorage.toggle_mod"), config.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(bool -> config.enabled = bool)
                .build());

        mainCategory.addEntry(entryBuilder
                .startDropdownMenu(Component.translatable("settings.clientstorage.limiter_type"),
                        DropdownMenuBuilder.TopCellElementBuilder.of(limiter, value -> {
                            try {
                                return PacketLimiter.valueOf(value.toUpperCase());
                            } catch (IllegalArgumentException ignored) {
                                return limiter;
                            }
                        }))
                .setSaveConsumer(packetLimiter -> limiter = packetLimiter)
                .setSelections(List.of(PacketLimiter.values()))
                .build());

        mainCategory.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("settings.clientstorage.inform_server_type"), config.informServerType)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.clientstorage.inform_server_type"))
                .setSaveConsumer(bool -> config.informServerType = bool)
                .build());


        mainCategory.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("settings.clientstorage.inform_search"), config.informSearch)
                .setDefaultValue(true)
                .setSaveConsumer(bool -> config.informSearch = bool)
                .build());


        var customLimiterCategory = builder.getOrCreateCategory(Component.translatable("category.clientstorage.custom_limiter"));
        customLimiterCategory.addEntry(entryBuilder
                .startIntSlider(Component.translatable("settings.clientstorage.custom_delay"),
                        config.customLimiter.getDelay(), 0, 600)
                .setTooltip(Component.translatable("tooltip.clientstorage.custom_delay"))
                .setSaveConsumer(config.customLimiter::setDelay)
                .build());


        customLimiterCategory.addEntry(entryBuilder
                .startIntSlider(Component.translatable("settings.clientstorage.packet_threshold"),
                        config.customLimiter.getThreshold(), 0, 8)
                .setTooltip(Component.translatable("tooltip.clientstorage.packet_threshold"))
                .setSaveConsumer(config.customLimiter::setThreshold)
                .build());

        return builder.build();
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
