package org.samo_lego.clientstorage.fabric_client.config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public class StorageMemoryConfig {

    public Set<StorageMemoryConfig> cellConfigs = new HashSet<>();

    public record StorageMemoryInstance(String name, String blockName, BlockPos pos, Block block) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof StorageMemoryInstance other) {
                return this.pos.equals(other.pos) && this.block.equals(other.block) && this.blockName.equals(other.blockName);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (((this.blockName.hashCode() * 31 + this.pos.hashCode()) * 31) + this.block.hashCode()) * 31;
        }
    }
}
