package org.samo_lego.clientstorage.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.samo_lego.clientstorage.common.ClientStorage;
import org.samo_lego.clientstorage.common.Config;

import static org.samo_lego.clientstorage.common.ClientStorage.NETWORK_CHANNEL;

public class ClientStorageBukkit extends JavaPlugin implements Listener {

    private static Config config;

    @Override
    public void onEnable() {
        new ClientStorage(getDataFolder(), ClientStorage.Platform.BUKKIT);

        config = Config.load(Config.class, () -> new Config(false));

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, NETWORK_CHANNEL);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, NETWORK_CHANNEL, this::onCustomPayload);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private void onCustomPayload(String s, Player player, byte[] bytes) {
        System.out.printf("Channel -- %s -- Received custom payload from %s.%n", s, player.getName());
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        System.out.println("Player joined: " + player.getName());

        player.sendPluginMessage(this, NETWORK_CHANNEL, config.pack());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        System.out.println("Interacted: " + event.getClickedBlock().getLocation() + ", direction: " + event.getBlockFace());

        player.sendPluginMessage(this, NETWORK_CHANNEL, config.pack());
    }
}
