package org.samo_lego.clientstorage.fabric_client.config;

import net.minecraft.network.FriendlyByteBuf;
import org.samo_lego.clientstorage.common.Config;
import org.samo_lego.clientstorage.fabric_client.network.PacketLimiter;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;

public class FabricConfig extends Config {

    public static PacketLimiter limiter = PacketLimiter.VANILLA;

    public double maxDist;

    public boolean informServerType;
    public boolean enabled;
    public boolean informSearch;

    public CustomLimiter customLimiter;

    public boolean enableCaching;

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
        this.lookThroughBlocks = buf.readBoolean();
    }
}
