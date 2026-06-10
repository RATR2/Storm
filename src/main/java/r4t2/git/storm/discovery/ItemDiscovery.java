package r4t2.git.storm.discovery;

import com.google.gson.*;
import r4t2.git.storm.pack.PackIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans every assets/namespace/items/*.json in the pack and
 * extracts the model reference from each item definition.
 *
 * Java 1.21.4+ item format:
 * {
 *   "model": {
 *     "type": "minecraft:model",
 *     "model": "example:item/magic_sword"
 *   }
 * }
 *
 * The "type" can be "minecraft:model", "model", or absent; we handle all cases.
 * Other model types (composite, condition, range_dispatch, etc.) are skipped with
 * a warning since they don't have a single static model to convert.
 */
public class ItemDiscovery {

    private final PackIndex index;
    private final Gson gson = new GsonBuilder().create();

    public ItemDiscovery(PackIndex index) {
        this.index = index;
    }

    public List<DiscoveredItem> discover() {
        List<DiscoveredItem> items = new ArrayList<>();

        for (String namespace : index.namespaces()) {
            String prefix = "assets/" + namespace + "/items";
            for (String path : index.pathsUnder(prefix)) {
                if (!path.endsWith(".json")) continue;
                String text = index.getText(path);
                if (text == null) continue;
                String filename = path.substring(path.lastIndexOf('/') + 1, path.length() - 5);
                String itemId = namespace + ":" + filename;
                String modelRef = extractModelReference(text, itemId);
                if (modelRef != null) {
                    items.add(new DiscoveredItem(itemId, modelRef));
                }
            }
        }

        return items;
    }

    private String extractModelReference(String json, String itemId) {
        try {
            JsonObject root = gson.fromJson(json, JsonObject.class);
            if (!root.has("model")) return null;

            JsonElement modelEl = root.get("model");
            if (modelEl.isJsonPrimitive()) {
                return modelEl.getAsString();
            }
            if (modelEl.isJsonObject()) {
                JsonObject modelObj = modelEl.getAsJsonObject();
                String type = modelObj.has("type") ? modelObj.get("type").getAsString() : "model";
                if (type.startsWith("minecraft:")) type = type.substring("minecraft:".length());

                switch (type) {
                    case "model":
                        if (modelObj.has("model")) {
                            return modelObj.get("model").getAsString();
                        }
                        break;
                    default:
                        return null;
                }
            }
        } catch (JsonSyntaxException e) {
            // Malformed JSON
        }
        return null;
    }
}
