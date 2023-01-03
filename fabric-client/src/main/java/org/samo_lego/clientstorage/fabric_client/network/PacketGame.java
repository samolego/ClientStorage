package org.samo_lego.clientstorage.fabric_client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;

import static org.samo_lego.clientstorage.fabric_client.event.ContainerDiscovery.lastCraftingHit;

public class PacketGame {
    /**
     * Sends a screen close packet with appropriate container id.
     */
    public static void closeCurrentScreen() {
        LocalPlayer player = Minecraft.getInstance().player;
        int containerId = player.containerMenu.containerId + (((ICSPlayer) player).cs_isAccessingItem() ? 1 : 0);
        Minecraft.getInstance().getConnection().send(new ServerboundContainerClosePacket(containerId));
    }


    /**
     * Opens crafting table back.
     */
    public static void openCrafting() {
        int containerId = Minecraft.getInstance().player.containerMenu.containerId;
        Minecraft.getInstance().getConnection().send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, containerId));
    }

}
