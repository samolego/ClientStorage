package org.samo_lego.clientstorage.fabric_client.mixin.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Double chests are special, they are actually two separate containers.
 */
@Mixin(CompoundContainer.class)
public abstract class MCompoundContainer implements InteractableContainerBlock {

    @Unique
    private BlockPos pos;
    @Unique
    private Container container;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(Container container1, Container container2, CallbackInfo ci) {
        this.container = container1;
        if (this.container == null) {
            this.container = container2;
            assert this.container != null;
        }
        this.pos = ((BaseContainerBlockEntity) this.container).getBlockPos();
    }

    @Override
    public Vec3 cs_position() {
        return this.pos.getCenter();
    }

    @Override
    public Component cs_getName() {
        return ((BaseContainerBlockEntity) this.container).getDisplayName();
    }
}
