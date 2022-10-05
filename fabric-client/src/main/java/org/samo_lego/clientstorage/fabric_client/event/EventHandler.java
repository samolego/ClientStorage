package org.samo_lego.clientstorage.fabric_client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
import org.samo_lego.clientstorage.fabric_client.util.PlayerLookUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;

import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

/**
 * The heart of the mod.
 */
public class EventHandler {

    public static final Map<BlockPos, Integer> FREE_SPACE_CONTAINERS = new HashMap<>();
    public static final LinkedBlockingDeque<List<ItemStack>> RECEIVED_INVENTORIES = new LinkedBlockingDeque<>();
    private static final LinkedBlockingDeque<BlockPos> INTERACTION_Q = new LinkedBlockingDeque<>();
    public static BlockHitResult lastCraftingHit = null;

    private static boolean fakePackets = false;

    public static boolean fakePacketsActive() {
        return fakePackets;
    }


    public static void resetFakePackets() {
        fakePackets = false;
    }

    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (fakePackets) return InteractionResult.FAIL;

        if (world.isClientSide() && !player.isShiftKeyDown()) {
            BlockPos craftingPos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(craftingPos);

            if (blockState.getBlock() == Blocks.CRAFTING_TABLE) {
                lastCraftingHit = hitResult;

                RECEIVED_INVENTORIES.clear();
                INTERACTION_Q.clear();
                RemoteInventory.getInstance().reset();
                FREE_SPACE_CONTAINERS.clear();

                if (config.enabled) {
                    BlockPos.MutableBlockPos mutable = player.blockPosition().mutable();
                    Set<LevelChunk> chunks2check = new HashSet<>();

                    // Get chunks to check
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            mutable.set(craftingPos.getX() + i * config.maxDist, craftingPos.getY(), craftingPos.getZ() + j * config.maxDist);
                            chunks2check.add(world.getChunkAt(mutable));
                        }
                    }

                    chunks2check.forEach(levelChunk -> levelChunk.getBlockEntities().forEach((position, blockEntity) -> {
                        position = position.mutable();
                        // Check if within reach
                        if (blockEntity instanceof Container container && player.getEyePosition().distanceTo(Vec3.atCenterOf(position)) < config.maxDist) {
                            // Check if container can be opened
                            // (avoid sending packets to those that client knows they can't be opened)
                            boolean canOpen = true;
                            BlockState state = blockEntity.getBlockState();
                            if (blockEntity instanceof ChestBlockEntity) {
                                canOpen = state.getMenuProvider(world, position) != null;
                            } else if (blockEntity instanceof ShulkerBoxBlockEntity shulker) {
                                canOpen = AShulkerBoxBlock.canOpen(state, world, position, shulker);
                            }


                            if (canOpen) {
                                boolean singleplayer = Minecraft.getInstance().isLocalServer();
                                if (singleplayer) {
                                    // We "cheat" here and copy the server side inventory to client if in singleplayer
                                    // Reason being that it's "cheaper" and also that
                                    // client was behaving differently than when playing on server
                                    var serverBE = (BaseContainerBlockEntity) Minecraft.getInstance()
                                            .getSingleplayerServer()
                                            .getLevel(world.dimension())
                                            .getChunkAt(position)
                                            .getBlockEntity(position);

                                    var serverContainer = (Container) serverBE;

                                    if (serverContainer != null && serverBE.canOpen(player) && !serverContainer.isEmpty()) {
                                        for (int i = 0; i < container.getContainerSize(); ++i) {
                                            container.setItem(i, serverContainer.getItem(i));
                                        }
                                    }
                                }

                                if (!singleplayer && (container.isEmpty() || !config.enableCaching)) {
                                    System.out.println("Empty container at " + position);
                                    INTERACTION_Q.add(position);
                                    FREE_SPACE_CONTAINERS.put(position, container.getContainerSize());
                                } else if (!container.isEmpty()) {
                                    System.out.println("Non-empty container at " + position);
                                    for (int i = 0; i < container.getContainerSize(); ++i) {
                                        ItemStack stack = container.getItem(i);
                                        if (!stack.isEmpty()) {
                                            EventHandler.addRemoteItem(blockEntity, i, stack);
                                            System.out.println("Added " + stack + " to remote inventory");
                                        } else {
                                            FREE_SPACE_CONTAINERS.compute(position, (key, value) -> value == null ? 1 : value + 1);
                                        }
                                    }
                                }
                            }
                        }
                    }));

                    if (!INTERACTION_Q.isEmpty()) {
                        CompletableFuture.runAsync(EventHandler::sendPackets);
                        return InteractionResult.FAIL;  // We'll open the crafting table later
                    }

                    RemoteInventory.getInstance().sort();
                }
            }
        }
        return InteractionResult.PASS;
    }

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
            boolean sameBlock = hitResult.getBlockPos().equals(blockPos);

            if (!config.lookThroughBlocks && !sameBlock) {
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

            if (config.lookThroughBlocks && !sameBlock) {
                // Todo get right block face if hitting through blocks
                Direction nearest = Direction.getNearest(client.player.getX(), client.player.getEyeY(), client.player.getZ());
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

            if (be instanceof Container container) {
                // This is a container, apply inventory changes
                var stacks = RECEIVED_INVENTORIES.removeFirst();

                System.out.println(pos.toShortString() + " -> stacks: " + stacks.stream().filter(stack -> !stack.isEmpty()).toList());

                System.out.print("Adding:");
                // Invalidating old cache
                for (int i = 0; i < stacks.size() && i < container.getContainerSize(); ++i) {
                    var stack = stacks.get(i);

                    int count = stack.getCount();

                    System.out.print(" " + stack);
                    if (fakePackets) {
                        // Also add to remote inventory
                        if (count > 0) {
                            // Add to crafting screen
                            EventHandler.addRemoteItem(be, i, stacks.get(i));
                        } else {
                            // This container has more space
                            FREE_SPACE_CONTAINERS.compute(be.getBlockPos(), (key, value) -> value == null ? 1 : value + 1);
                        }
                    }

                    container.setItem(i, stack);
                }
                System.out.println();
            }
        } else {
            System.out.println("No inventory to apply to " + pos);
        }
    }

    public static void onFinalCraftingOpen() {
        fakePackets = false;
        RECEIVED_INVENTORIES.clear();
        RemoteInventory.getInstance().sort();
    }
}
