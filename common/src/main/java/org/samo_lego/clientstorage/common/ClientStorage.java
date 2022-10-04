package org.samo_lego.clientstorage.common;

import java.io.File;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class ClientStorage {
    public static final String MOD_ID = "clientstorage";

    public static final String NETWORK_CHANNEL = MOD_ID + ":config";

    public ClientStorage(File configDir, Platform platform) {
        var configFile = new File(configDir + File.separator + MOD_ID + ".json");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Config.CONFIG_FILE = configFile;

        getLogger(MOD_ID).info("Initializing ClientStorage on " + platform.toString().toLowerCase() + " platform.");
    }

    public enum Platform {
        FABRIC,
        BUKKIT;
    }
}
