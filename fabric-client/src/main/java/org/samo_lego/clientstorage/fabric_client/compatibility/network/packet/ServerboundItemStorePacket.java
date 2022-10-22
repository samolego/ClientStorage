package org.samo_lego.clientstorage.fabric_client.compatibility.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.clientstorage.fabric_client.compatibility.network.ItemNetworking;

public class ServerboundItemStorePacket {

    /**
     * Creates a {@link ServerboundCustomPayloadPacket} for putting an item into a stash.
     *
     * @param stashId    id of the stash to put item into
     * @param freeSlotId id of the slot to transfer item from. Should always be in player's inventory, never carried.
     * @return packet
     */
    public static ServerboundCustomPayloadPacket newPacket(ResourceLocation stashId, int freeSlotId) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(stashId.toString());
        buf.writeBoolean(false);  // False as don't take item from stash, but put it in
        buf.writeInt(freeSlotId);

        return new ServerboundCustomPayloadPacket(ItemNetworking.TRANSFER_ITEM_C2S, buf);
    }
}
