package org.samo_lego.clientstorage.fabric_client.compatibility.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.fabric_client.compatibility.StashContainer;
import org.samo_lego.clientstorage.fabric_client.compatibility.network.packet.ServerboundRequestStoragePacket;

import java.util.LinkedList;

import static org.samo_lego.clientstorage.common.ClientStorage.MOD_ID;

public class ItemNetworking {
    public static final ResourceLocation REQUEST_STORAGE_C2S = new ResourceLocation(MOD_ID, "request_storage");
    public static final ResourceLocation PROVIDE_STORAGE_S2C = new ResourceLocation(MOD_ID, "provide_storage");
    public static final ResourceLocation TRANSFER_ITEM_C2S = new ResourceLocation(MOD_ID, "transfer_item");

    public static void registerChannels() {
        ClientPlayNetworking.registerGlobalReceiver(REQUEST_STORAGE_C2S, (client, handler, buf, responseSender) -> {
        });
        ClientPlayNetworking.registerGlobalReceiver(PROVIDE_STORAGE_S2C, ItemNetworking::onStorageReceived);
        ClientPlayNetworking.registerGlobalReceiver(TRANSFER_ITEM_C2S, (client, handler, buf, responseSender) -> {
        });
    }

    public static void unregisterChannels() {
        ClientPlayNetworking.unregisterGlobalReceiver(REQUEST_STORAGE_C2S);
        ClientPlayNetworking.unregisterGlobalReceiver(PROVIDE_STORAGE_S2C);
        ClientPlayNetworking.unregisterGlobalReceiver(TRANSFER_ITEM_C2S);
    }

    private static void onStorageReceived(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
        // Unpack the items from buf
        final var stashId = buf.readResourceLocation();
        final var size = buf.readInt();

        final LinkedList<ItemStack> items = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            ItemStack stack = new ItemStack(Item.byId(buf.readInt()));
            stack.setCount(buf.readInt());

            // NBT tag
            stack.setTag(buf.readNbt());

            while (stack.getCount() > stack.getMaxStackSize()) {
                ItemStack splitStack = stack.copy();
                splitStack.setCount(stack.getMaxStackSize());
                stack.shrink(stack.getMaxStackSize());
                items.add(splitStack);
            }

            items.addLast(stack);

        }

        new StashContainer(stashId, items).addAllItems();
    }

    public static void requestInventory(LocalPlayer player) {
        player.connection.send(ServerboundRequestStoragePacket.newPacket());
    }
}
