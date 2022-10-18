package org.samo_lego.clientstorage.fabric_client.config;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.samo_lego.clientstorage.common.Config;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;
import org.samo_lego.clientstorage.fabric_client.util.ItemDisplayType;
import org.samo_lego.clientstorage.fabric_client.util.ItemLocationTooltip;

import java.util.Optional;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.SERVER_CONFIG_CHANNEL;
import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

public class FabricConfig extends Config {

    public static PacketLimiter limiter = PacketLimiter.VANILLA;

    public double maxDist;

    public ItemDisplayType itemDisplayType = ItemDisplayType.MERGE_ALL;

    public ItemLocationTooltip locationTooltip = ItemLocationTooltip.ALWAYS_SHOW;

    public boolean informServerType;
    public boolean enabled;
    public boolean informSearch;

    public CustomLimiter customLimiter;

    public boolean enableCaching;

    private static Optional<Config> serverConfig = Optional.empty();
    public boolean focusSearchBar = false;
    private boolean allowSyncServer = true;

    public FabricConfig() {
        super(true);
        this.maxDist = Math.sqrt(MAX_INTERACTION_DISTANCE);

        this.informSearch = true;
        this.enabled = true;
        this.informServerType = true;
        this.enableCaching = true;
        this.customLimiter = new CustomLimiter();
    }


    @Override
    public void onLoad() {
        PacketLimiter.CUSTOM.setDelay(this.customLimiter.delay);
        PacketLimiter.CUSTOM.setThreshold(this.customLimiter.threshold);
    }

    @Override
    public void save() {
        this.customLimiter.delay = PacketLimiter.CUSTOM.getDelay();
        this.customLimiter.threshold = PacketLimiter.CUSTOM.getThreshold();

        super.save();
    }

    public void unpack(FriendlyByteBuf buf) {
        try {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

            final var sentConfig = Config.GSON.fromJson(input.readUTF(), Config.class);
            serverConfig = Optional.of(sentConfig);
        } catch (JsonSyntaxException ignored) {
            // Server has sent invalid config, ignore it
            serverConfig = Optional.empty();
        }
    }

    public static boolean isPlayingServer() {
        return Minecraft.getInstance().player != null && !Minecraft.getInstance().hasSingleplayerServer();
    }

    public void clearServerSettings() {
        serverConfig = Optional.empty();
    }

    public boolean hasServerSettings() {
        return serverConfig.isPresent();
    }

    @Override
    public boolean lookThroughBlocks() {
        if (!this.allowEditLookThroughBlocks()) {
            return serverConfig.map(Config::lookThroughBlocks).orElseGet(super::lookThroughBlocks);
        }
        return super.lookThroughBlocks();
    }

    public boolean allowEditLookThroughBlocks() {
        return !isPlayingServer() || (serverConfig.isPresent() && serverConfig.get().lookThroughBlocks());
    }

    public boolean allowSyncServer() {
        return this.allowSyncServer;
    }

    public void setAllowSyncServer(boolean allow) {
        if (!this.allowSyncServer && allow) {
            ClientPlayNetworking.registerGlobalReceiver(SERVER_CONFIG_CHANNEL, (client, handler, buf, responseSender) -> config.unpack(buf));
            config.clearServerSettings();
        } else if (this.allowSyncServer && !allow) {
            ClientPlayNetworking.unregisterGlobalReceiver(SERVER_CONFIG_CHANNEL);
            this.setStrictServerSettings();
        }
        this.allowSyncServer = allow;
    }

    public void setStrictServerSettings() {
        serverConfig = Optional.of(new Config());
    }


}
