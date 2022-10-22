package org.samo_lego.clientstorage.fabric_client.compatibility.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.samo_lego.clientstorage.fabric_client.compatibility.network.ItemNetworking;

public class ServerboundRequestStoragePacket {
    private static final int PROTOCOL_VERSION = 1;

    /**
     * Sends a packet to request server storages, e.g. stashes.
     *
     * @return packet
     */
    public static ServerboundCustomPayloadPacket newPacket() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(PROTOCOL_VERSION);
        return new ServerboundCustomPayloadPacket(ItemNetworking.REQUEST_STORAGE_C2S, buf);
    }
}
