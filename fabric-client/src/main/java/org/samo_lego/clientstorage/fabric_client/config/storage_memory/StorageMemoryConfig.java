package org.samo_lego.clientstorage.fabric_client.config.storage_memory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StorageMemoryConfig {

    /**
     * Maps hostname / world name -> map of:
     * storage memory instance -> inventory layout
     */
    @JsonAdapter(Serializer.class)
    private final Map<String, Map<StorageMemoryPreset, Int2ObjectMap<Item>>> memoryConfigs = new HashMap<>();

    private static String getSaveId() {
        if (Minecraft.getInstance().isSingleplayer()) {
            // get current world name; not optimal, but is something
            return "127.0.0.1";
        }
        return Minecraft.getInstance().getCurrentServer().ip;
    }

    public Collection<Map<StorageMemoryPreset, Int2ObjectMap<Item>>> streamAll() {
        return this.memoryConfigs.values();
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

    public Int2ObjectMap<Item> get(BaseContainerBlockEntity container) {
        final var memPreset = this.memoryConfigs.get(StorageMemoryConfig.getSaveId());
        if (memPreset != null) {
            final var inventory = memPreset.get(StorageMemoryPreset.of(container));

            if (inventory != null) {
                return inventory;
            }
        }
        return Int2ObjectMaps.emptyMap();
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
