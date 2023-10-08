package org.samo_lego.clientstorage.fabric_client.config.storage_memory;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class StorageMemoryPreset {

    private String name;
    private BlockPos pos;
    private String blockName;
    private Block block;

    private StorageMemoryPreset() {
    }

    public StorageMemoryPreset(String name, BlockPos pos, String blockName, Block block) {
        this();
        this.name = name;
        this.pos = pos;
        this.blockName = blockName;
        this.block = block;
    }

    public StorageMemoryPreset(String name, JsonObject json) {
        this();
        this.name = name;
        this.pos = BlockPos.of(json.get("pos").getAsLong());
        this.blockName = json.get("blockName").getAsString();
        this.block = BuiltInRegistries.BLOCK.get(new ResourceLocation(json.get("block").getAsString()));
    }

    public static StorageMemoryPreset createPresetFrom(BaseContainerBlockEntity container, String presetName) {
        var preset = new StorageMemoryPreset();
        preset.name = presetName;
        preset.pos = container instanceof ShulkerBoxBlockEntity ? BlockPos.ZERO : container.getBlockPos();
        preset.blockName = container.getDisplayName().getString();
        preset.block = container.getBlockState().getBlock();

        return preset;
    }

    /*public static StorageMemoryPreset createPresetFrom(CompoundContainer container, String presetName) {
        return createPresetFrom(container, container.toString().getString());
    }*/

    public static StorageMemoryPreset of(BaseContainerBlockEntity container) {
        return createPresetFrom(container, container.getName().getString());
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
        return String.format("%s@%s_%s", this.blockName, this.pos.toShortString(), BuiltInRegistries.BLOCK.getKey(this.block));
    }

    public String containerName() {
        return this.blockName;
    }

    public BlockPos pos() {
        return this.pos;
    }

    public Block block() {
        return this.block;
    }

    public String blockName() {
        return this.blockName;
    }

    public void toJson(JsonObject json) {
        json.addProperty("pos", this.pos.asLong());
        json.addProperty("blockName", this.blockName);
        json.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
    }
}
