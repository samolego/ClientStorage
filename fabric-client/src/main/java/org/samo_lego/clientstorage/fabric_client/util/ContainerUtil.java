package org.samo_lego.clientstorage.fabric_client.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;

public class ContainerUtil {

    /**
     * Copies the {@link ItemStack} from the source
     * to the destination container.
     *
     * @param from       source container
     * @param to         destination container
     * @param copyStacks whether to use {@link ItemStack#copy()} or not.
     */
    public static void copyContent(InteractableContainer from, InteractableContainer to, boolean copyStacks) {
        for (int i = 0; i < from.getContainerSize() && i < to.getContainerSize(); ++i) {
            var stack = from.getItem(i);
            if (copyStacks) stack = stack.copy();
            to.setItem(i, stack);
        }
    }


    /**
     * Gets {@link Container} from the {@link BlockEntity}.
     * Accounts for double chests as well.
     * If block entity is not a container, returns null.
     *
     * @param blockEntity block entity to get container for.
     * @return container or null if block entity is not a container.
     */
    @Nullable
    public static InteractableContainer getContainer(@NotNull BlockEntity blockEntity) {
        return getContainer(blockEntity.getLevel(), blockEntity.getBlockPos());
    }


    @Nullable
    public static synchronized InteractableContainer getContainer(Level level, BlockPos pos) {
        return (InteractableContainer) HopperBlockEntity.getContainerAt(level, pos);
    }

    /**
     * Checks whether this container should be included in the list.
     * Default is yes, but if container is a chest, it must be checked
     * whether it's blocked by a block above it. Similar applies to shulkers.
     * Hoppers are also excluded if they're unlocked.
     *
     * @param containerBE container block entity
     * @param player      player
     * @return true if player can open the container, false otherwise
     */
    public static boolean shouldIncludeContainer(BlockEntity containerBE, Player player) {
        final BlockState containerState = containerBE.getBlockState();
        final var blockPos = containerBE.getBlockPos();

        if (containerBE instanceof ChestBlockEntity) {
            // Check for ceiling
            boolean canOpen = !ChestBlock.isChestBlockedAt(player.level(), blockPos);

            // Check if chest is double chest
            if (canOpen) {
                DoubleBlockCombiner.BlockType chestType = ChestBlock.getBlockType(containerState);

                if (chestType != DoubleBlockCombiner.BlockType.SINGLE) {
                    // Get the other chest part
                    BlockPos otherChestPos = blockPos.relative(ChestBlock.getConnectedDirection(containerState));

                    // Check if other part can be opened
                    canOpen = !ChestBlock.isChestBlockedAt(player.level(), otherChestPos);

                    // Only allow one chest to be opened
                    canOpen &= chestType == DoubleBlockCombiner.BlockType.FIRST;
                }
            }
            return canOpen;
        }

        if (containerBE instanceof ShulkerBoxBlockEntity shulker) {
            // ShulkerBoxBlock#canOpen but modified a bit
            if (shulker.getAnimationStatus() != ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
                return true;
            } else {
                AABB aABB = Shulker.getProgressDeltaAabb(containerState.getValue(ShulkerBoxBlock.FACING), 0.0F, 0.5F).move(blockPos).deflate(1.0E-6);

                for (var shape : player.level().getBlockCollisions(null, aABB)) {
                    if (!shape.isEmpty() && !shape.bounds().deflate(1.0E-6).equals(aABB)) {
                        return false;
                    }
                }

            }
        }

        // Check for unlocked hopper
        if (containerBE instanceof HopperBlockEntity) {
            return !containerState.getValue(HopperBlock.ENABLED);
        }

        return true;
    }
}
