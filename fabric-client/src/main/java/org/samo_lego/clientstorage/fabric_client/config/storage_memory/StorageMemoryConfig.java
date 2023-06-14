package org.samo_lego.clientstorage.fabric_client.config.storage_memory;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StorageMemoryConfig {

    /**
     * Maps hostname / world name -> map of:
     * storage memory instance -> inventory layout
     */
    @JsonAdapter(Serializer.class)
    private final Map<String, Map<StorageMemoryPreset, Int2ObjectMap<Item>>> memoryConfigs = new HashMap<>();
    public boolean enabled = true;

    private static String getSaveId() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.isSingleplayer()) {
            // get current world name; not optimal, but is something
            final String worldName = mc.getSingleplayerServer().getLevel(mc.player.level().dimension()).toString();
            return worldName.substring("ServerLevel[".length(), worldName.length() - 1);
        }
        return mc.getCurrentServer().ip;
    }

    /**
     * Saves a preset
     *
     * @param preset    preset structure
     * @param inventory inventory layout of items
     */
    public void savePreset(final StorageMemoryPreset preset, final Int2ObjectMap<Item> inventory) {
        assert !preset.getPresetName().isEmpty() : "Added preset must have a name!";

        var hostname = StorageMemoryConfig.getSaveId();

        this.memoryConfigs.compute(hostname, (_s, inventoryData) -> {
            if (inventoryData == null) {
                inventoryData = new HashMap<>();
            }
            inventoryData.put(preset, inventory);

            return inventoryData;
        });
    }

    public void savePreset(final StorageMemoryPreset preset, final Container container) {
        Int2ObjectArrayMap<Item> inventory = new Int2ObjectArrayMap<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                inventory.put(i, stack.getItem());
            }
        }

        savePreset(preset, inventory);
    }

    public Optional<Int2ObjectMap<Item>> get(BaseContainerBlockEntity container) {
        final var memPreset = this.memoryConfigs.get(StorageMemoryConfig.getSaveId());
        if (memPreset != null) {
            return Optional.ofNullable(memPreset.get(StorageMemoryPreset.of(container)));
        }
        return Optional.empty();
    }

    public void removePreset(StorageMemoryPreset preset) {
        this.memoryConfigs.computeIfPresent(StorageMemoryConfig.getSaveId(), (_s, inventoryData) -> {
            inventoryData.remove(preset);
            return inventoryData;
        });
    }

    public boolean containsPreset(BaseContainerBlockEntity container) {
        final var presets = this.memoryConfigs.get(StorageMemoryConfig.getSaveId());

        if (presets != null) {
            return presets.containsKey(StorageMemoryPreset.of(container));
        }
        return false;
    }

    /**
     * Clears all the presets.
     */
    public void clearAll() {
        this.memoryConfigs.clear();
    }

    /**
     * Clears all the presets for current world.
     */
    public void clearForCurrentWorld() {
        this.memoryConfigs.remove(StorageMemoryConfig.getSaveId());
    }

    private static class Serializer implements JsonSerializer<Map<String, Map<StorageMemoryPreset, Int2ObjectMap<Item>>>>, JsonDeserializer<Map<String, Map<StorageMemoryPreset, Int2ObjectMap<Item>>>> {

        @Override
        public Map<String, Map<StorageMemoryPreset, Int2ObjectMap<Item>>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final var result = new HashMap<String, Map<StorageMemoryPreset, Int2ObjectMap<Item>>>();

            for (Map.Entry<String, JsonElement> hostname2inventories : json.getAsJsonObject().entrySet()) {
                final var presetData = new HashMap<StorageMemoryPreset, Int2ObjectMap<Item>>();

                for (Map.Entry<String, JsonElement> preset2inventory : hostname2inventories.getValue().getAsJsonObject().entrySet()) {
                    final var presetInfo = preset2inventory.getValue().getAsJsonObject().get("preset_info").getAsJsonObject();
                    final var preset = new StorageMemoryPreset(preset2inventory.getKey(), presetInfo);

                    final var inventoryData = preset2inventory.getValue().getAsJsonObject().get("inventory").getAsJsonObject();
                    final var inventory = new Int2ObjectArrayMap<Item>();

                    for (Map.Entry<String, JsonElement> invSlot : inventoryData.entrySet()) {
                        final var item = BuiltInRegistries.ITEM.get(new ResourceLocation(invSlot.getValue().getAsString()));
                        inventory.put(Integer.parseInt(invSlot.getKey()), item);
                    }

                    presetData.put(preset, inventory);
                }
                result.put(hostname2inventories.getKey(), presetData);
            }

            return result;
        }

        @Override
        public JsonElement serialize(Map<String, Map<StorageMemoryPreset, Int2ObjectMap<Item>>> src, Type typeOfSrc, JsonSerializationContext context) {
            final var rootData = new JsonObject();

            for (Map.Entry<String, Map<StorageMemoryPreset, Int2ObjectMap<Item>>> entry : src.entrySet()) {
                final var presetData = new JsonObject();

                for (var preset2inventory : entry.getValue().entrySet()) {
                    final StorageMemoryPreset memPreset = preset2inventory.getKey();
                    final var data = new JsonObject();

                    final var presetInfo = new JsonObject();
                    memPreset.toJson(presetInfo);

                    final var inventoryData = new JsonObject();
                    for (var invSlot : preset2inventory.getValue().int2ObjectEntrySet()) {
                        var item = invSlot.getValue();
                        if (item != Items.AIR) {
                            var itemName = BuiltInRegistries.ITEM.getKey(item).toString();
                            inventoryData.addProperty(String.valueOf(invSlot.getIntKey()), itemName);
                        }
                    }


                    data.add("preset_info", presetInfo);
                    data.add("inventory", inventoryData);
                    presetData.add(memPreset.getPresetName(), data);
                }
                rootData.add(entry.getKey(), presetData);
            }

            return rootData;
        }
    }
}
