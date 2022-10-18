package org.samo_lego.clientstorage.fabric_client.compatibility.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ServerboundItemTakePacket {

    /**
     * Creates a {@link ServerboundCustomPayloadPacket} for taking an item from a stash.
     *
     * @param stashId    id of the stash to take item from
     * @param freeSlotId id of the slot to set item to on server
     * @param stack      stack to take
     * @return packet
     */
    public static ServerboundCustomPayloadPacket newPacket(ResourceLocation stashId, int freeSlotId, ItemStack stack) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(stashId.toString());
        buf.writeBoolean(true);  // true as take item from stash
        buf.writeInt(freeSlotId);
        buf.writeItem(stack);

        return new ServerboundCustomPayloadPacket(ItemNetworking.TRANSFER_ITEM_C2S, buf);
    }
}
