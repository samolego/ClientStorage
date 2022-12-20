package org.samo_lego.clientstorage.fabric_client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.fabric_client.ClientStorageFabric;
import org.samo_lego.clientstorage.fabric_client.casts.IRemoteStack;
import org.samo_lego.clientstorage.fabric_client.config.FabricConfig;
import org.samo_lego.clientstorage.fabric_client.inventory.RemoteInventory;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.AMultiPlayerGamemode;
import org.samo_lego.clientstorage.fabric_client.mixin.accessor.AShulkerBoxBlock;
import org.samo_lego.clientstorage.fabric_client.util.ContainerUtil;
import org.samo_lego.clientstorage.fabric_client.util.ESPRender;
import org.samo_lego.clientstorage.fabric_client.util.PlayerLookUtil;
import org.samo_lego.clientstorage.fabric_client.util.StorageCache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

/**
 * The heart of the mod.
 */
public class ContainerDiscovery {

    public static final LinkedBlockingDeque<List<ItemStack>> RECEIVED_INVENTORIES = new LinkedBlockingDeque<>();
    private static final LinkedBlockingDeque<BlockPos> INTERACTION_Q = new LinkedBlockingDeque<>();
    public static BlockHitResult lastCraftingHit = null;

    private static boolean fakePackets = false;
    private static final Set<Runnable> actions = new HashSet<>();
    private static final int[] DIRECTIONS = new int[]{-1, 1};

    public static boolean fakePacketsActive() {
        return fakePackets;
    }


    public static void resetFakePackets() {
        fakePackets = false;
    }

    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (fakePackets) return InteractionResult.FAIL;

        if (world.isClientSide() && !player.isShiftKeyDown() && config.enabled) {
            BlockPos craftingPos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(craftingPos);

            if (blockState.getBlock() == Blocks.CRAFTING_TABLE) {
                lastCraftingHit = hitResult;

                ContainerDiscovery.resetInventoryCache();

                Set<LevelChunk> chunks2check = ContainerDiscovery.getChunksAround(player.blockPosition(), world);

                chunks2check.forEach(levelChunk -> levelChunk.getBlockEntities().forEach((position, blockEntity) -> {
                    position = position.mutable();
                    // Check if within reach
                    if (blockEntity instanceof Container && player.getEyePosition().distanceTo(Vec3.atCenterOf(position)) < config.maxDist) {
                        // Check if container can be opened
                        // (avoid sending packets to those that client knows they can't be opened)
                        boolean canOpen = ContainerDiscovery.canOpenContainer(blockEntity, player);
                        Container container = ContainerUtil.getContainer(blockEntity);

                        if (canOpen) {
                            boolean singleplayer = Minecraft.getInstance().isLocalServer();
                            if (singleplayer) {
                                ContainerDiscovery.copyServerContent(blockEntity);
                            }

                            if (!singleplayer && (container.isEmpty() || !config.enableCaching)) {
                                INTERACTION_Q.add(position);
                                StorageCache.FREE_SPACE_CONTAINERS.put(position, container.getContainerSize());
                            } else if (!container.isEmpty()) {
                                for (int i = 0; i < container.getContainerSize(); ++i) {
                                    ItemStack stack = container.getItem(i);
                                    if (!stack.isEmpty()) {
                                        ContainerDiscovery.addRemoteItem(blockEntity, i, stack);
                                    } else {
                                        StorageCache.FREE_SPACE_CONTAINERS.compute(position, (key, value) -> value == null ? 1 : value + 1);
                                    }
                                }
                                StorageCache.CACHED_INVENTORIES.add(container);
                            } else {
                                StorageCache.FREE_SPACE_CONTAINERS.put(position, container.getContainerSize());
                            }
                        }
                    }
                }));

                if (!INTERACTION_Q.isEmpty()) {
                    CompletableFuture.runAsync(ContainerDiscovery::sendPackets);
                    return InteractionResult.FAIL;  // We'll open the crafting table later
                }

                RemoteInventory.getInstance().sort();
            } else {
                ESPRender.remove(craftingPos);
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * Copies the content of the container to the client block entity container
     *
     * @param blockEntity block entity to copy to.
     */
    private static void copyServerContent(BlockEntity blockEntity) {
        // We "cheat" here and copy the server side inventory to client if in singleplayer
        // Reason being that it's "cheaper" and also that
        // client was behaving differently than when playing on server
        ServerLevel level = Minecraft.getInstance()
                .getSingleplayerServer()
                .getLevel(blockEntity.getLevel().dimension());
        var serverBE = (BaseContainerBlockEntity) level.getChunkAt(blockEntity.getBlockPos())
                .getBlockEntity(blockEntity.getBlockPos());

        var serverContainer = ContainerUtil.getContainer(serverBE);

        if (serverContainer != null && serverBE.canOpen(Minecraft.getInstance().player) && !serverContainer.isEmpty()) {
            ContainerUtil.copyContent(serverContainer, ContainerUtil.getContainer(blockEntity), true);
        }
    }

    private static void resetInventoryCache() {
        RECEIVED_INVENTORIES.clear();
        INTERACTION_Q.clear();
        RemoteInventory.getInstance().reset();
        StorageCache.FREE_SPACE_CONTAINERS.clear();
    }

    /**
     * Gets the chunks around block position.
     *
     * @param pos   block position
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
     * Checks whether player can open the container from clientside pov.
     *
     * @param containerBE container block entity
     * @param player      player
     * @return true if player can open the container, false otherwise
     */
    public static boolean canOpenContainer(BlockEntity containerBE, Player player) {
        final BlockState containerState = containerBE.getBlockState();
        boolean canOpen = true;

        if (containerBE instanceof ChestBlockEntity) {
            // Check for ceiling
            canOpen = !ChestBlock.isChestBlockedAt(player.getLevel(), containerBE.getBlockPos());

            // Check if chest is double chest
            if (canOpen) {
                DoubleBlockCombiner.BlockType chestType = ChestBlock.getBlockType(containerState);

                if (chestType != DoubleBlockCombiner.BlockType.SINGLE) {
                    // Get the other chest part
                    //((ChestBlock) containerBE.getBlockState().getBlock()).combine(containerState, player.getLevel(), containerBE.getBlockPos(), true);
                    BlockPos otherChestPos = containerBE.getBlockPos().relative(ChestBlock.getConnectedDirection(containerState));
                    BlockState otherChestState = player.getLevel().getBlockState(otherChestPos);

                    // Check if other part can be opened
                    canOpen = otherChestState.getMenuProvider(player.getLevel(), otherChestPos) != null;

                    // Only allow one chest to be opened
                    canOpen &= chestType == DoubleBlockCombiner.BlockType.FIRST;
                }
            }
        } else if (containerBE instanceof ShulkerBoxBlockEntity shulker) {
            canOpen = AShulkerBoxBlock.canOpen(containerState, player.getLevel(), player.getOnPos(), shulker);
        }

        return canOpen;
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
        fakePackets = true;
        for (var blockPos : INTERACTION_Q) {

            var hitResult = PlayerLookUtil.raycastTo(blockPos);
            boolean behindWall = !hitResult.getBlockPos().equals(blockPos);

            if (!config.lookThroughBlocks() && behindWall) {
                // This container is behind a block, so we can't open it
                continue;
            }

            if (count++ >= FabricConfig.limiter.getThreshold()) {
                count = 0;
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (config.lookThroughBlocks() && behindWall) {
                // Todo get right block face if hitting through blocks
                Direction nearest = PlayerLookUtil.getBlockDirection(blockPos);
                hitResult = new BlockHitResult(Vec3.atCenterOf(blockPos), nearest, blockPos, false);
            }

            //lookAt(blockPos);

            final var finalHit = hitResult;
            gm.cs_startPrediction(client.level, i ->
                    new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, finalHit, i));

            // Close container packet
            gm.cs_startPrediction(client.level,
                    ServerboundContainerClosePacket::new);

        }

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


    public static void addRemoteItem(BlockEntity be, int slotId, ItemStack stack) {
        RemoteInventory.getInstance().addStack(IRemoteStack.fromStack(stack, be, slotId));
    }

    public static void onInventoryPacket(ClientboundContainerSetContentPacket packet) {
        RECEIVED_INVENTORIES.addLast(packet.getItems());
    }

    public static void applyInventoryToBE(ClientboundBlockUpdatePacket packet) {
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

                    if (fakePackets) {
                        // Also add to remote inventory
                        if (count > 0) {
                            // Add to crafting screen
                            ContainerDiscovery.addRemoteItem(be, i, stacks.get(i));
                        } else {
                            // This container has more space
                            StorageCache.FREE_SPACE_CONTAINERS.compute(be.getBlockPos(), (key, value) -> value == null ? 1 : value + 1);
                        }
                    }

                    container.setItem(i, stack);
                }
            }
        }
    }

    public static void onCraftingScreenOpen() {
        fakePackets = false;
        RECEIVED_INVENTORIES.clear();
        RemoteInventory.getInstance().sort();
        actions.forEach(Runnable::run);
        actions.clear();
    }

    public static void supplyAction(Runnable action) {
        actions.add(action);
    }
}
