package org.samo_lego.clientstorage.fabric_client.config.storage_memory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class StorageMemoryPreset {

    private String name;
    private BlockPos pos;
    private Component blockName;
    private Block block;


    private StorageMemoryPreset() {
    }

    public static StorageMemoryPreset createPresetFrom(BaseContainerBlockEntity container, String presetName) {
        var preset = new StorageMemoryPreset();
        preset.name = presetName;
        preset.pos = container instanceof ShulkerBoxBlockEntity ? BlockPos.ZERO : container.getBlockPos();
        preset.blockName = container.getDisplayName();
        preset.block = container.getBlockState().getBlock();

        return preset;
    }

    public static StorageMemoryPreset of(BaseContainerBlockEntity container) {
        return createPresetFrom(container, "");
    }

    public String getPresetName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof StorageMemoryPreset other) {
            return this.pos.equals(other.pos) && this.block.equals(other.block) && this.blockName.equals(other.blockName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (((this.blockName.hashCode() * 31 + this.pos.hashCode()) * 31) + this.block.hashCode()) * 31;
    }

    @Override
    public String toString() {
        return String.format("%s@%%s_%s", this.blockName.toString(), this.pos.toShortString(), BuiltInRegistries.BLOCK.getKey(this.block));
    }
}
