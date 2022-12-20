package org.samo_lego.clientstorage.fabric_client.util;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ContainerUtil {

    /**
     * Copies the {@link ItemStack} from the source
     * to the destination container.
     *
     * @param from       source container
     * @param to         destination container
     * @param copyStacks whether to use {@link ItemStack#copy()} or not.
     */
    public static void copyContent(Container from, Container to, boolean copyStacks) {
        for (int i = 0; i < from.getContainerSize(); ++i) {
            var stack = from.getItem(i);
            if (copyStacks) stack = stack.copy();
            to.setItem(i, stack);
            //stackConsumer.accept(stack);
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
    public static Container getContainer(BlockEntity blockEntity) {
        if (blockEntity instanceof Container container) {
            if (blockEntity instanceof ChestBlockEntity chest) {
                var blockState = chest.getBlockState();
                container = ChestBlock.getContainer((ChestBlock) blockState.getBlock(),
                        blockState,
                        blockEntity.getLevel(),
                        blockEntity.getBlockPos(),
                        true);
            }
            return container;
        }
        return null;
    }
}
