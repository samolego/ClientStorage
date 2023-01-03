package org.samo_lego.clientstorage.fabric_client.mixin;

import net.minecraft.world.entity.player.Player;
import org.samo_lego.clientstorage.fabric_client.casts.ICSPlayer;
import org.samo_lego.clientstorage.fabric_client.storage.InteractableContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(Player.class)
public class MPlayer_ICSPlayer implements ICSPlayer {

    @Unique
    private boolean accessingItem;
    @Unique
    private InteractableContainer lastInteractedContainer;

    @Override
    public void cs_setAccessingItem(boolean accessing) {
        this.accessingItem = accessing;
    }

    @Override
    public boolean cs_isAccessingItem() {
        return this.accessingItem;
    }

    @Override
    public Optional<InteractableContainer> cs_getLastInteractedContainer() {
        return Optional.ofNullable(this.lastInteractedContainer);
    }

    @Override
    public void cs_setLastInteractedContainer(InteractableContainer container) {
        this.lastInteractedContainer = container;
    }
}
