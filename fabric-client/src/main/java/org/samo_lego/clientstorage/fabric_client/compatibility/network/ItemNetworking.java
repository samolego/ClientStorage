package org.samo_lego.clientstorage.fabric_client.compatibility.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.clientstorage.fabric_client.compatibility.StashContainer;
import org.samo_lego.clientstorage.fabric_client.compatibility.StashSupport;

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
        final var nbt = buf.readNbt();

        final ListTag listTag = nbt.getList("Items", 10);
        final LinkedList<ItemStack> items = new LinkedList<>();

        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            ItemStack stack = ItemStack.of(compoundTag);

            // Split stacks to multiple if they are too big
            while (stack.getCount() > stack.getMaxStackSize()) {
                ItemStack splitStack = stack.copy();
                splitStack.setCount(stack.getMaxStackSize());
                stack.shrink(stack.getMaxStackSize());
                items.add(splitStack);
            }

            items.addLast(stack);
        }

        StashSupport.addStashContainer(new StashContainer(stashId, items));
    }
}
