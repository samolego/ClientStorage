package org.samo_lego.clientstorage.fabric_server;

import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.clientstorage.common.ClientStorage;
import org.samo_lego.clientstorage.common.Config;

import java.util.concurrent.atomic.AtomicReference;

import static org.samo_lego.clientstorage.common.ClientStorage.NETWORK_CHANNEL;

public class ServerStorageFabric implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        new ClientStorage(FabricLoader.getInstance().getConfigDir().toFile(), ClientStorage.Platform.FABRIC);

        var config = new AtomicReference<>(Config.load(Config.class, Config::new));

        final var channelId = new ResourceLocation(NETWORK_CHANNEL);
        ServerPlayNetworking.registerGlobalReceiver(channelId, (server, player, handler, buf, responseSender) -> {
        });

        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (!channels.contains(channelId)) return;

            if (config.get().debug) {
                LogUtils.getLogger().debug("Player {} is using ClientStorage mod, sending config.", handler.player.getGameProfile().getName());
            }

            var byteBuf = new FriendlyByteBuf(Unpooled.buffer());
            byteBuf.writeBytes(config.get().pack());

            handler.send(new ClientboundCustomPayloadPacket(channelId, byteBuf));
        });


        // Config reloading
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
            config.set(Config.load(Config.class, config::get));

            var byteBuf = new FriendlyByteBuf(Unpooled.buffer());
            byteBuf.writeBytes(config.get().pack());

            server.getPlayerList().broadcastAll(new ClientboundCustomPayloadPacket(channelId, byteBuf));
        });
    }
}
