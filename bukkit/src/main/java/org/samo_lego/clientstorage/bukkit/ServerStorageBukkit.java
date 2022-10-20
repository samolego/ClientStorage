package org.samo_lego.clientstorage.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.samo_lego.clientstorage.common.ClientStorage;
import org.samo_lego.clientstorage.common.Config;

import static org.samo_lego.clientstorage.common.ClientStorage.NETWORK_CHANNEL;

public class ServerStorageBukkit extends JavaPlugin implements Listener {

    private static Config config;

    @Override
    public void onEnable() {
        new ClientStorage(getDataFolder(), ClientStorage.Platform.BUKKIT);

        config = Config.load(Config.class, Config::new);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, NETWORK_CHANNEL);
        this.getServer().getPluginManager().registerEvents(this, this);

        // Send config to any players on reload as well
        this.getServer().getOnlinePlayers().forEach(player ->
                player.sendPluginMessage(this, NETWORK_CHANNEL, config.pack()));
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @EventHandler
    public void onPlayerChannel(PlayerRegisterChannelEvent event) {
        if (event.getChannel().equals(NETWORK_CHANNEL)) {
            // Triggered when player with ClientStorage connects to the server
            event.getPlayer().sendPluginMessage(this, NETWORK_CHANNEL, config.pack());
        }
    }
}
