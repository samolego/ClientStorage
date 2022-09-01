package org.samo_lego.clientstorage.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.samo_lego.clientstorage.inventory.RemoteInventory;
import org.samo_lego.clientstorage.mixin.accessor.AMultiPlayerGamemode;
import org.samo_lego.clientstorage.mixin.accessor.AShulkerBoxBlock;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;
import static org.samo_lego.clientstorage.ClientStorage.enabled;

public class EventHandler {

    private static final int MAX_DIST = (int) Math.sqrt(MAX_INTERACTION_DISTANCE);

    public static final RemoteInventory REMOTE_INV = new RemoteInventory();
    public static final Map<BlockPos, Integer> FREE_SPACE_CONTAINERS = new HashMap<>();

    public static BlockHitResult lastHitResult = null;

    private static boolean fakePackets = false;

    public static boolean fakePacketsActive() {
        return fakePackets;
    }


    public static void resetFakePackets() {
        fakePackets = false;
    }


    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide() && !fakePackets) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(pos);

            if (blockState.getBlock() == Blocks.CRAFTING_TABLE) {
                lastHitResult = hitResult;

                INTERACTION_Q.clear();
                REMOTE_INV.reset();
                FREE_SPACE_CONTAINERS.clear();

                if (enabled) {
                    BlockPos.MutableBlockPos mutable = player.blockPosition().mutable();
                    Set<LevelChunk> chunks2check = new HashSet<>();

                    // Get chunks to check
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            mutable.set(pos.getX() + i * MAX_DIST, pos.getY(), pos.getZ() + j * MAX_DIST);
                            chunks2check.add(world.getChunkAt(mutable));
                        }
                    }

                    chunks2check.forEach(levelChunk -> levelChunk.getBlockEntities().forEach((position, blockEntity) -> { // todo cache
                        position = position.mutable();
                        // Check if within reach
                        if (blockEntity instanceof Container && player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position)) < MAX_INTERACTION_DISTANCE) {

                            // Check if container can be opened
                            boolean canOpen = true;
                            BlockState state = blockEntity.getBlockState();
                            if (blockEntity instanceof ChestBlockEntity) {
                                canOpen = state.getMenuProvider(world, position) != null;
                            } else if (blockEntity instanceof ShulkerBoxBlockEntity shulker) {
                                canOpen = AShulkerBoxBlock.canOpen(state, world, position, shulker);
                            }

                            if (canOpen) {
                                BlockPos blockPos = blockEntity.getBlockPos();
                                BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);

                                INTERACTION_Q.addLast(result);
                            }
                        }
                    }));
                    fakePackets = true;
                    CompletableFuture.runAsync(EventHandler::sendPackets);

                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }

    public static void sendPackets() {
        // Spigot compatibility https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/CraftBukkit-Patches/0062-Limit-block-placement-interaction-packets.patch#59
        int count = 0;
        int sleep = INTERACTION_Q.size() < 16 ? 0 : 30;
        Minecraft client = Minecraft.getInstance();
        client.player.sendSystemMessage(Component.literal("[ClientStorage] Searching, please wait ...").withStyle(ChatFormatting.GRAY));
        String brand = client.player.getServerBrand().toLowerCase(Locale.ROOT);

        if (!brand.equals("vanilla") && !brand.equals("fabric")) {
            sleep = 300;
        }

        var gm = (AMultiPlayerGamemode) client.gameMode;

        for (var hit : INTERACTION_Q) {
            if (count++ >= 3) {
                count = 0;
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            gm.cs_startPrediction(client.level, i ->
                    new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, i));

            // Close container packet
            gm.cs_startPrediction(client.level,
                    ServerboundContainerClosePacket::new);

        }

        if (count >= 3) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Send open crafting packet again
        gm.cs_startPrediction(client.level, id ->
                new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, lastHitResult, id));

    }


    public static void addRemoteItem(BlockEntity be, int slotId, ItemStack stack) {
        REMOTE_INV.addStack(IRemoteStack.fromStack(stack, be, slotId));
    }

    public static void onInventoryPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!INTERACTION_Q.isEmpty()) {
            var pos = INTERACTION_Q.removeFirst().getBlockPos();

            var client = Minecraft.getInstance();
            BlockEntity be = client.level.getBlockEntity(pos);
            if (be instanceof Container container) {
                // Invalidating old cache
                List<ItemStack> items = packet.getItems();
                for (int i = 0; i < items.size() && i < container.getContainerSize(); ++i) {
                    var stack = items.get(i);

                    int count = stack.getCount();

                    if (count > 0) {
                        // Add to crafting screen
                        EventHandler.addRemoteItem(be, i, items.get(i));
                    } else {
                        // This container has more space
                        FREE_SPACE_CONTAINERS.compute(be.getBlockPos(), (key, value) -> value == null ? 1 : value + 1);
                    }
                    container.setItem(i, items.get(i));
                }
            }

            // If this was the last packet, sort and start accepting packets again
            if (INTERACTION_Q.isEmpty()) {
                REMOTE_INV.sort();
                fakePackets = false;
            }
            ci.cancel();
        }
    }
}
