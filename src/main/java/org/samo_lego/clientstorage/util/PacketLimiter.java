package org.samo_lego.clientstorage.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.mixin.accessor.AMultiPlayerGamemode;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.samo_lego.clientstorage.ClientStorage.config;

public class PacketLimiter {
    public static final Map<String, PacketLimiter> LIMITERS = null;

    public static final PacketLimiter VANILLA = new PacketLimiter(10, 10);  // Vanilla has no delay, but we don't want to spam packets
    public static final PacketLimiter SPIGOT = new PacketLimiter(30, 4);  // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0062-Limit-block-placement-interaction-packets.patch#59
    public static final PacketLimiter PAPER = new PacketLimiter(300, 8);  // https://github.com/PaperMC/Paper/blob/master/patches/server/0112-Configurable-packet-in-spam-threshold.patch
    private static int receivedCount = 0;
    private static Optional<BlockPos> blockPos = Optional.empty();

    static {
        var vanilla = new PacketLimiter(10, 10);  // Vanilla has no delay, but we don't want to spam packets
        var spigot = new PacketLimiter(30, 4);  // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0062-Limit-block-placement-interaction-packets.patch#59
        var paper = new PacketLimiter(300, 8);  // https://github.com/PaperMC/Paper/blob/master/patches/server/0112-Configurable-packet-in-spam-threshold.patch
    }

    private int delay;
    private int threshold;

    public PacketLimiter(int delay, int threshold) {
        this.delay = delay;
        this.threshold = threshold;
    }

    /**
     * Tries to recognize server in order to get the right packet limiter.
     */
    public static void tryRecognizeServer() {
        var client = Minecraft.getInstance();
        var player = client.player;
        var brand = player.getServerBrand().toLowerCase(Locale.ROOT);

        // If server brand is vanilla, fabric, forge or craftbukkit, use vanilla limiter
        // We use .contains as server might be behind a proxy
        /*if (brand.equals("vanilla") || brand.contains("fabric") || brand.equals("forge") || brand.contains("craftbukkit")) {
            config.limiter = VANILLA;
        } else if (brand.contains("paper") || brand.contains("purpur") || brand.contains("pufferfish")) {
            config.limiter = PAPER;
        } else if (brand.contains("spigot")) {
            config.limiter = SPIGOT;
        } else {*/
        // Not recognized, use packets to identify
        System.out.println("Server brand not recognized, trying to identify server...");
        receivedCount = 0;

        blockPos = Optional.of(player.blockPosition());

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(blockPos.get()), Direction.UP, blockPos.get(), false);
        var gm = (AMultiPlayerGamemode) client.gameMode;

        // Interact with player's feet block 5 times (Spigot has threshold set to 4, Paper to 8)
        for (int i = 0; i < 5; i++) {
            gm.cs_startPrediction(client.level, id ->
                    new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, id));
        }
        //}
    }

    public static void detectServerType(ClientboundBlockUpdatePacket packet) {
        if (blockPos.isPresent()) {
            System.out.println("Recognizing ... " + receivedCount);
            BlockPos pos = packet.getPos();

            if (blockPos.get().equals(pos)) {
                ++receivedCount;
                if (receivedCount > 4) {
                    // Paper
                    config.limiter = PAPER;
                    blockPos = Optional.empty();
                    System.out.println("Detected Paper server");
                }
            }

            // Wait for new packets to arrive
            if (pos instanceof BlockPos.MutableBlockPos) {
                // That wasn't one of our packets, this might be spigot
                config.limiter = SPIGOT;
                blockPos = Optional.empty();
                System.out.println("Detected Spigot server");
            }
        }
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public enum ServerBrand {
        VANILLA,
        SPIGOT,
        PAPER
    }
}
