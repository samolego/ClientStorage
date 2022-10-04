package org.samo_lego.clientstorage.fabric_client.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerBoxBlock.class)
public interface AShulkerBoxBlock {
    @Invoker("canOpen")
    static boolean canOpen(BlockState state, Level world, BlockPos position, ShulkerBoxBlockEntity shulker) {
        throw new AssertionError();
    }
}
