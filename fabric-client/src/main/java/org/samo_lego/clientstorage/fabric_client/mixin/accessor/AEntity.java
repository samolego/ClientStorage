package org.samo_lego.clientstorage.fabric_client.mixin.accessor;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface AEntity {

    @Accessor("FLAG_GLOWING")
    static int FLAG_GLOWING() {
        throw new AssertionError();
    }

    @Invoker("setSharedFlag")
    void cs_setSharedFlag(int flagId, boolean value);
}
