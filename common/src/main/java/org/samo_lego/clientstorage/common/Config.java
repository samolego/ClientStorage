package org.samo_lego.clientstorage.common;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static org.samo_lego.clientstorage.common.ClientStorage.MOD_ID;
import static org.slf4j.LoggerFactory.getLogger;

public class Config {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();
    static File CONFIG_FILE;
    @SerializedName("// Whether to allow the mod to discover containers behind blocks")
    public final String _comment_lookThroughBlocks = "(client default: true)";
    @SerializedName("look_through_blocks")
    private boolean lookThroughBlocks;

    public Config(boolean lookThroughBlocks) {
        this.lookThroughBlocks = lookThroughBlocks;
    }

    public boolean lookThroughBlocks() {
        return this.lookThroughBlocks;
    }

    public static <T extends Config> T load(Class<T> configClass, Supplier<T> defaultConfig) {
        T newConfig = null;
        if (CONFIG_FILE.exists()) {
            try (var fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
                newConfig = GSON.fromJson(fileReader, configClass);
            } catch (IOException e) {
                throw new RuntimeException(MOD_ID + " Problem occurred when trying to load config: ", e);
            }
        }
        if (newConfig == null) {
            newConfig = defaultConfig.get();
        }

        newConfig.onLoad();
        newConfig.save();

        return newConfig;
    }

    public void onLoad() {
    }

    public void save() {
        try (var writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            getLogger(MOD_ID).error("Problem occurred when saving config: " + e.getMessage());
        }
    }

    public byte[] pack() {
        var out = ByteStreams.newDataOutput();
        out.writeUTF(GSON.toJson(this));
        return out.toByteArray();
    }

    public void setLookThroughBlocks(boolean lookThroughBlocks) {
        this.lookThroughBlocks = lookThroughBlocks;
    }

    public static class CustomLimiter {
        public int delay = 300;
        public int threshold = 4;
    }
}
