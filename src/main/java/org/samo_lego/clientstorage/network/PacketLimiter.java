package org.samo_lego.clientstorage.network;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.samo_lego.clientstorage.ClientStorage;
import org.samo_lego.clientstorage.config.Config;

import java.util.Locale;

import static org.samo_lego.clientstorage.ClientStorage.config;

public enum PacketLimiter {
    VANILLA(10, 10),  // Vanilla has no delay, but we don't want to spam packets
    SPIGOT(30, 4),  // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0062-Limit-block-placement-interaction-packets.patch#59
    PAPER(300, 8),  // https://github.com/PaperMC/Paper/blob/master/patches/server/0112-Configurable-packet-in-spam-threshold.patch
    CUSTOM(300, 4);  // Unknown server type, so we'll use mixed Paper & Spigot values

    private int delay;
    private int threshold;

    PacketLimiter(int delay, int threshold) {
        this.delay = delay;
        this.threshold = threshold;
    }

    /**
     * Tries to recognize server in order to get the right packet limiter.
     */
    public static void tryRecognizeServer() {
        Config.limiter = getServerLimiter();

        Minecraft client = Minecraft.getInstance();
        var brand = client.player.getServerBrand().toLowerCase(Locale.ROOT);

        if (Config.limiter != CUSTOM) {
            if (config.informServerType && !client.hasSingleplayerServer()) {
                ClientStorage.displayMessage(Component.translatable("info.clientstorage.server_type",
                        Component.literal(Config.limiter.toString()).withStyle(ChatFormatting.GOLD)));
            }
        } else {
            // Server type not recognized, inform player
            ClientStorage.displayMessage(Component.translatable("error.clientstorage.unknown_server",
                    Component.literal(brand).withStyle(ChatFormatting.GOLD)));
        }
    }

    public static PacketLimiter getServerLimiter() {
        var client = Minecraft.getInstance();
        var player = client.player;

        if (player == null) {
            return CUSTOM;
        }

        var brand = player.getServerBrand().toLowerCase(Locale.ROOT);

        // If server brand is vanilla, fabric, forge or craftbukkit, use vanilla limiter
        // We use .contains as server might be behind a proxy
        if (brand.equals("vanilla") || brand.contains("fabric") || brand.equals("forge") || brand.contains("craftbukkit")) {
            return VANILLA;
        } else if (brand.contains("paper") || brand.contains("purpur") || brand.contains("pufferfish")) {
            return PAPER;
        } else if (brand.contains("spigot")) {
            return SPIGOT;
        } else {
            return CUSTOM;
        }
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
