package org.samo_lego.clientstorage.fabric_server;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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

        var config = new AtomicReference<>(Config.load(Config.class, () -> new Config(false)));


        final var channelId = new ResourceLocation(NETWORK_CHANNEL);
        ServerPlayNetworking.registerGlobalReceiver(channelId, (server, player, handler, buf, responseSender) -> {
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var byteBuf = new FriendlyByteBuf(Unpooled.buffer());
            byteBuf.writeBytes(config.get().pack());

            handler.send(new ClientboundCustomPayloadPacket(channelId, byteBuf));
        });

        // Config reloading
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
            config.set(Config.load(Config.class, config::get));
        });
    }
}
