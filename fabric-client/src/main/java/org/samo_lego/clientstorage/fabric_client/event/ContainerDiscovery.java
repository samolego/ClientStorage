package org.samo_lego.clientstorage.fabric_client.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.fabric_client.ClientStorageFabric;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;
import org.samo_lego.clientstorage.fabric_client.config.FabricConfig;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.ACompoundContainer;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.AMultiPlayerGamemode;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;
import org.samo_lego.clientstorage.fabric_client.util.ContainerUtil;
import org.samo_lego.clientstorage.fabric_client.util.ESPRender;
import org.samo_lego.clientstorage.fabric_client.util.PlayerLookUtil;
import org.samo_lego.clientstorage.fabric_client.util.StorageCache;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

/**
 * The heart of the mod.
 */
public class ContainerDiscovery {

    private static final Queue<InteractableContainer> INTERACTION_Q = new ConcurrentLinkedQueue<>();
    private static final Queue<InteractableContainer> EXPECTED_INVENTORIES = new ConcurrentLinkedQueue<>();
    public static BlockHitResult lastCraftingHit = null;

    private static long fakePacketsDuration = 0;
    private static final Set<Runnable> actions = new HashSet<>();
    private static final int[] DIRECTIONS = new int[]{-1, 1};

    public static boolean fakePacketsActive() {
        return fakePacketsDuration > System.currentTimeMillis();
    }


    public static void resetFakePackets() {
        fakePacketsDuration = 0;
    }

    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (fakePacketsActive()) return InteractionResult.FAIL;
        ((ICSPlayer) player).cs_setLastInteractedContainer(null);

        if (world.isClientSide() && !player.isShiftKeyDown() && config.enabled) {
            BlockPos craftingPos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(craftingPos);

            if (blockState.getBlock() == Blocks.CRAFTING_TABLE) {
                final boolean singleplayer = Minecraft.getInstance().isLocalServer();
                lastCraftingHit = hitResult;

                ContainerDiscovery.resetInventoryCache();

                if (config.enableBlocks) {
                    Set<LevelChunk> chunks2check = ContainerDiscovery.getChunksAround(player.blockPosition(), world);
                    chunks2check.forEach(levelChunk -> {
                        // Check for blockentity containers
                        levelChunk.getBlockEntities().forEach((position, blockEntity) -> {
                            // Check if within reach
                            if (blockEntity instanceof InteractableContainer && player.getEyePosition().distanceTo(Vec3.atCenterOf(position)) < config.maxDist) {
                                // Check if container can be opened
                                // (avoid sending packets to those that client knows they can't be opened)
                                boolean canOpen = ContainerUtil.canOpenContainer(blockEntity, player);
                                InteractableContainer container = ContainerUtil.getContainer(blockEntity);

                                if (canOpen) {
                                    if (singleplayer && container.isEmpty()) {
                                        ContainerDiscovery.copyServerContent(container);
                                    }

                                    if (!singleplayer && (container.isEmpty() || !config.enableCaching)) {
                                        INTERACTION_Q.add(container);
                                        StorageCache.FREE_SPACE_CONTAINERS.put(container, container.getContainerSize());
                                    } else if (!container.isEmpty()) {
                                        for (int i = 0; i < container.getContainerSize(); ++i) {
                                            ItemStack stack = container.getItem(i);
                                            if (!stack.isEmpty()) {
                                                ContainerDiscovery.addRemoteItem(container, i, stack);
                                            } else {
                                                StorageCache.FREE_SPACE_CONTAINERS.compute(container, (key, value) -> value == null ? 1 : value + 1);
                                            }
                                        }
                                        StorageCache.CACHED_INVENTORIES.add(container);
                                    } else {
                                        StorageCache.FREE_SPACE_CONTAINERS.put(container, container.getContainerSize());
                                    }
                                }
                            }
                        });
                    });
                }

                // Check for other containers (e.g. chest minecarts, etc.)
                if (config.enableEntities) {
                    final var boundingBox = player.getBoundingBox().inflate(config.maxDist);
                    world.getEntities((Entity) null, boundingBox, InteractableContainer.CONTAINER_ENTITY_SELECTOR).forEach(entity -> {
                        final var container = (InteractableContainer) entity;
                        if (singleplayer && container.isEmpty()) {
                            ContainerDiscovery.copyServerContent(container);
                        }

                        if (!singleplayer && (container.isEmpty() || !config.enableCaching)) {
                            INTERACTION_Q.add(container);
                            StorageCache.FREE_SPACE_CONTAINERS.put(container, container.getContainerSize());
                        } else if (!container.isEmpty()) {
                            for (int i = 0; i < container.getContainerSize(); ++i) {
                                ItemStack stack = container.getItem(i);
                                if (!stack.isEmpty()) {
                                    ContainerDiscovery.addRemoteItem(container, i, stack);
                                } else {
                                    StorageCache.FREE_SPACE_CONTAINERS.compute(container, (key, value) -> value == null ? 1 : value + 1);
                                }
                            }
                            StorageCache.CACHED_INVENTORIES.add(container);
                        } else {
                            StorageCache.FREE_SPACE_CONTAINERS.put(container, container.getContainerSize());
                        }
                    });
                }

                if (!INTERACTION_Q.isEmpty()) {
                    CompletableFuture.runAsync(ContainerDiscovery::sendPackets);
                    return InteractionResult.FAIL;  // We'll open the crafting table later
                }

                RemoteInventory.getInstance().sort();
            } else {
                ESPRender.removeBlockPos(craftingPos);

                final BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(craftingPos);
                assert !ContainerDiscovery.fakePacketsActive();
                if (blockEntity instanceof InteractableContainer) {
                    ((ICSPlayer) player).cs_setLastInteractedContainer(ContainerUtil.getContainer(blockEntity));
                } else {
                    ((ICSPlayer) player).cs_setLastInteractedContainer(null);
                }
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * Copies the content of the server container to the client block entity container
     *
     * @param container block entity to copy to.
     */
    private static void copyServerContent(InteractableContainer container) {
        // Double chests need extra work
        if (container instanceof ACompoundContainer cnt) {
            copyServerContent((InteractableContainer) cnt.getContainer1());
            copyServerContent((InteractableContainer) cnt.getContainer2());
        }

        // We "cheat" here and copy the server side inventory to client if in singleplayer
        // Reason being that it's "cheaper" and also that
        // client was behaving differently than when playing on server
        ServerLevel level = Minecraft.getInstance()
                .getSingleplayerServer()
                .getLevel(Minecraft.getInstance().level.dimension());

        // Get server block entity

        final BlockPos pos = new BlockPos(container.cs_position());
        InteractableContainer serverContainer = (InteractableContainer) level.getChunkAt(pos).getBlockEntity(pos);
        if (serverContainer == null) {
            serverContainer = ContainerUtil.getContainer(level, pos);
        }

        if (serverContainer != null) {
            if (container.getContainerSize() != serverContainer.getContainerSize())
                ClientStorageFabric.tryLog(String.format("Server and client container sizes don't match! Client: %s, server: %s",
                        container.cs_info(),
                        serverContainer.cs_info()), ChatFormatting.RED);
            if (!serverContainer.isEmpty() && (serverContainer.cs_isEntity() || ((BaseContainerBlockEntity) serverContainer).canOpen(Minecraft.getInstance().player))) {
                ContainerUtil.copyContent(serverContainer, container, true);
            }
        }
    }

    private static void resetInventoryCache() {
        INTERACTION_Q.clear();
        EXPECTED_INVENTORIES.clear();
        RemoteInventory.getInstance().reset();
        StorageCache.FREE_SPACE_CONTAINERS.clear();
    }

    /**
     * Gets the chunks around block cs_position.
     *
     * @param pos   block cs_position
     * @param world world
     * @return set of chunks
     */
    public static Set<LevelChunk> getChunksAround(BlockPos pos, Level world) {
        final BlockPos.MutableBlockPos mutable = pos.mutable();
        final HashSet<LevelChunk> chunkPositions = new HashSet<>();
        chunkPositions.add(world.getChunkAt(pos));

        // Get chunks to check
        for (int i : DIRECTIONS) {
            for (int j : DIRECTIONS) {
                mutable.set(pos.getX() + i * config.maxDist, pos.getY(), pos.getZ() + j * config.maxDist);
                chunkPositions.add(world.getChunkAt(mutable));
            }
        }
        return chunkPositions;
    }


    /**
     * Sends the packets from interaction queue.
     */
    public static void sendPackets() {
        int count = 0;
        int sleep = FabricConfig.limiter.getDelay();
        Minecraft client = Minecraft.getInstance();

        if (config.informSearch) {
            ClientStorageFabric.displayMessage("gameplay.clientstorage.performing_search");
        }

        var gm = (AMultiPlayerGamemode) client.gameMode;
        fakePacketsDuration = System.currentTimeMillis() + 5000;
        EXPECTED_INVENTORIES.clear();

        ClientStorageFabric.tryLog("Starting to send following packets :: " + INTERACTION_Q, ChatFormatting.GREEN);
        while (!INTERACTION_Q.isEmpty()) {
            try {
                InteractableContainer container = INTERACTION_Q.poll();
                ClientStorageFabric.tryLog("Sending packets :: " + container.cs_info(), ChatFormatting.AQUA);

                var hitResult = PlayerLookUtil.raycastTo(container.cs_position());
                boolean behindWall = hitResult.getBlockPos().getCenter().distanceTo(container.cs_position()) > 1;

                if (!config.lookThroughBlocks() && behindWall) {
                    // This container is behind a block, so we can't open it
                    continue;
                }

                if (container.cs_isDelayed() && count++ >= FabricConfig.limiter.getThreshold()) {
                    count = 0;
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (config.lookThroughBlocks() && behindWall) {
                    // Todo get right block face if hitting through blocks
                    Direction nearest = PlayerLookUtil.getBlockDirection(container.cs_position());
                    hitResult = new BlockHitResult(container.cs_position(), nearest, new BlockPos(container.cs_position()), false);
                }

                //lookAt(containerPos);


                container.cs_sendInteractionPacket();
                // Close container packet
                try {
                    gm.cs_startPrediction(client.level, ServerboundContainerClosePacket::new);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                EXPECTED_INVENTORIES.add(container);
                ClientStorageFabric.tryLog("Added to expected inventories :: " + container.cs_info(), ChatFormatting.AQUA);
            } catch (Exception e) {
                ClientStorageFabric.tryLog("Error while sending packets", ChatFormatting.RED);
                ClientStorageFabric.tryLog(e.getMessage(), ChatFormatting.RED);
                e.printStackTrace();
            }
        }
        ClientStorageFabric.tryLog("Finished sending packets", ChatFormatting.GREEN);

        if (count >= FabricConfig.limiter.getThreshold()) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Send open crafting packet
        gm.cs_startPrediction(client.level, id ->
                new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastCraftingHit, id));
    }


    public static void addRemoteItem(InteractableContainer source, int slotId, ItemStack stack) {
        ClientStorageFabric.tryLog(String.format("Adding %s (origin: %s, slot #%d)", stack, source.cs_info(), slotId), ChatFormatting.DARK_GRAY);
        RemoteInventory.getInstance().addStack(IRemoteStack.fromStack(stack, source, slotId).copy());
    }

    public static void onInventoryPacket(final ClientboundContainerSetContentPacket packet) {
        final InteractableContainer container = EXPECTED_INVENTORIES.poll();
        if (container == null) {
            if (packet.getContainerId() != 0 && fakePacketsActive()) {
                ClientStorageFabric.tryLog("Received unexpected inventory packet", ChatFormatting.RED);
            }
            return;
        }

        var stacks = packet.getItems();
        if (container.getContainerSize() + 36 != stacks.size()) {
            ClientStorageFabric.tryLog(String.format("Container size mismatch, expected %d [%s] but got %d.%n",
                            container.getContainerSize(), container.cs_info(), stacks.size() - 36),
                    ChatFormatting.RED);
        }

        ClientStorageFabric.tryLog(String.format("Received inventory packet for %s with: %s",
                container.cs_info(),
                stacks.stream().filter(s -> !s.isEmpty()).toList()), ChatFormatting.YELLOW);

        container.cs_parseOpenPacket(packet);
    }

    /*public static void applyInventoryToBE(ClientboundBlockUpdatePacket packet) {
        BlockPos pos = packet.getPos();
        if (!RECEIVED_INVENTORIES.isEmpty()) {
            var client = Minecraft.getInstance();
            BlockEntity be = client.level.getBlockEntity(pos);

            if (be instanceof Container) {
                // This is a container, apply inventory changes
                var stacks = RECEIVED_INVENTORIES.removeFirst();

                Container container = ContainerUtil.getContainer(be);

                // Invalidating old cache
                for (int i = 0; i < stacks.size() && i < container.getContainerSize(); ++i) {
                    var stack = stacks.get(i);

                    int count = stack.getCount();

                    if (fakePacketsActive()) {
                        // Also add to remote inventory
                        if (count > 0) {
                            // Add to crafting screen
                            ContainerDiscovery.addRemoteItem((InteractableContainer) be, i, stacks.get(i));
                        } else {
                            // This container has more space
                            StorageCache.FREE_SPACE_CONTAINERS.compute(be.getBlockPos().getCenter(), (key, value) -> value == null ? 1 : value + 1);
                        }
                    }
                    container.setItem(i, stack);
                }
            }
        }
    }*/

    public static void onCraftingScreenOpen() {
        resetFakePackets();
        INTERACTION_Q.clear();
        EXPECTED_INVENTORIES.clear();
        RemoteInventory.getInstance().sort();
        actions.forEach(Runnable::run);
        actions.clear();
    }

    public static void supplyAction(Runnable action) {
        actions.add(action);
    }
}
