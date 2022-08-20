package org.samo_lego.clientstorage.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.casts.IRemoteStack;
import org.samo_lego.clientstorage.inventory.RemoteInventory;
import org.samo_lego.clientstorage.mixin.accessor.AShulkerBoxBlock;
import org.samo_lego.clientstorage.util.ItemOrigin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
import static org.samo_lego.clientstorage.ClientStorage.INTERACTION_Q;

public class EventHandler {

    public static final RemoteInventory REMOTE_INV = new RemoteInventory();
    public static final Map<Item, List<ItemOrigin>> ITEM_ORIGINS = new HashMap<>();

    public static BlockHitResult lastHitResult = null;
    public static int expectedContainerId = -1;

    private static boolean fakePackets = false;

    public static boolean fakePacketsActive() {
        return fakePackets;
    }


    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(pos);

            if (blockState.getBlock() == Blocks.CRAFTING_TABLE) {
                lastHitResult = hitResult;

                INTERACTION_Q.clear();
                REMOTE_INV.clearContent();
                ITEM_ORIGINS.clear();


                fakePackets = true;
                world.getChunkAt(player.blockPosition()).getBlockEntities().forEach((position, blockEntity) -> { // todo cache
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

                            INTERACTION_Q.addLast(blockPos);
                            ((LocalPlayer) player).connection.send(new ServerboundUseItemOnPacket(hand, result, 0));
                            ((LocalPlayer) player).connection.send(new ServerboundContainerClosePacket(0));
                        }
                    }
                });

                System.out.println("Fake packets sent, order: " + INTERACTION_Q);

                fakePackets = false;

            }
        }
        return InteractionResult.PASS;
    }

    public static void addRemoteItem(BlockEntity be, int slotId, ItemStack stack) {
        REMOTE_INV.addStack(IRemoteStack.fromStack(stack, be, slotId));
    }

    public static void onInventoryPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {

        if (!INTERACTION_Q.isEmpty()) {
            var clientstorage$currentPos = INTERACTION_Q.removeFirst();

            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(clientstorage$currentPos);
            if (be instanceof Container container) {
                // Invalidating old cache
                System.out.println("Checking " + be.getBlockPos() + ", empty:: -> " + container.isEmpty());
                //container.clearContent();
                List<ItemStack> items = packet.getItems();
                for (int i = 0; i < items.size() && i < container.getContainerSize(); ++i) {
                    var stack = items.get(i);

                    int count = stack.getCount();

                    //if (stack.isStackable())
                    //    stack.shrink(1);  // We want to leave 1 item behind

                    if (count > 0) {
                        // Add to crafting screen
                        EventHandler.addRemoteItem(be, i, items.get(i));
                    }
                    container.setItem(i, items.get(i));
                }
            }

            if (INTERACTION_Q.isEmpty()) {
                REMOTE_INV.sort();
            }
            //this.clientStorage$currentSyncId = packet.getContainerId();
            ci.cancel();
        } /*else {
            clientstorage$currentPos = null;
            this.clientStorage$currentSyncId = -1;

            try {
                AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
                if (menu.getType() == MenuType.CRAFTING) {
                    //System.out.println("REFRESHING");
                    //((IRemoteCrafting) menu).refreshRemoteInventory();
                }
            } catch (UnsupportedOperationException ignored) {
            }
        }*/
    }
}
