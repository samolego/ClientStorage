package org.samo_lego.clientstorage.fabric_client.config;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.common.Config;
import org.samo_lego.clientstorage.fabric_client.config.storage_memory.StorageMemoryConfig;
import org.samo_lego.clientstorage.fabric_client.inventory.ItemBehaviour;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.SERVER_CONFIG_CHANNEL;
import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

public class FabricConfig extends Config {

    public static PacketLimiter limiter = PacketLimiter.VANILLA;
    public StorageMemoryConfig storageMemory;

    public double maxDist;

    public ItemBehaviour.ItemDisplayType itemDisplayType = ItemBehaviour.ItemDisplayType.MERGE_ALL;

    public ItemBehaviour.ItemDataTooltip locationTooltip = ItemBehaviour.ItemDataTooltip.ALWAYS_SHOW;

    public boolean informServerType;
    public boolean enabled;
    public boolean informSearch;

    public CustomLimiter customLimiter;

    public boolean enableCaching;

    public boolean enableBlocks;
    public boolean enableEntities;

    @Nullable
    private static Config serverConfig = null;
    public boolean focusSearchBar = false;
    public boolean enableItemTransfers = true;
    private boolean allowSyncServer = true;

    public FabricConfig() {
        super(true);
        this.maxDist = Math.sqrt(MAX_INTERACTION_DISTANCE);

        this.informSearch = true;
        this.enabled = true;
        this.informServerType = true;
        this.enableCaching = true;
        this.customLimiter = new CustomLimiter();
        this.enableBlocks = true;
        this.enableEntities = true;
        this.storageMemory = new StorageMemoryConfig();
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

    public static boolean isNotOnServer() {
        return Minecraft.getInstance().player == null || Minecraft.getInstance().hasSingleplayerServer();
    }

    public void unpack(FriendlyByteBuf buf) {
        try {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

            serverConfig = Config.GSON.fromJson(input.readUTF(), Config.class);
        } catch (JsonSyntaxException ignored) {
            // Server has sent invalid config, ignore it
            serverConfig = null;
        }
    }

    public void clearServerSettings() {
        serverConfig = null;
    }

    public boolean hasServerSettings() {
        return serverConfig != null;
    }

    @Override
    public boolean lookThroughBlocks() {
        if (!this.allowEditLookThroughBlocks()) {
            if (serverConfig != null) {
                return serverConfig.lookThroughBlocks();
            }
            return super.lookThroughBlocks();
        }
        return super.lookThroughBlocks();
    }

    public boolean allowEditLookThroughBlocks() {
        return isNotOnServer() || (serverConfig != null && serverConfig.lookThroughBlocks());
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
        serverConfig = new Config();
    }


}
