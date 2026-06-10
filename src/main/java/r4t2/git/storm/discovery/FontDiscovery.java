package r4t2.git.storm.discovery;

import com.google.gson.*;
import r4t2.git.storm.converter.font.FontProvider;
import r4t2.git.storm.pack.PackIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scans every assets/namespace/font/*.json in the pack and
 * extracts all font providers.
 *
 * Java font format:
 * {
 *   "providers": [
 *     {
 *       "type": "bitmap",
 *       "file": "example:font/icons.png",
 *       "ascent": 8,
 *       "height": 16,
 *       "chars": ["\uE000\uE001", ...]
 *     },
 *     {
 *       "type": "space",
 *       "advances": {
 *         "\uE100": 4,
 *         "\uE101": -1
 *       }
 *     },
 *     ...
 *   ]
 * }
 */
public class FontDiscovery {

    private final PackIndex index;
    private final Gson gson = new GsonBuilder().create();

    public FontDiscovery(PackIndex index) {
        this.index = index;
    }

    public List<FontProvider> discover() {
        List<FontProvider> providers = new ArrayList<>();

        for (String namespace : index.namespaces()) {
            String prefix = "assets/" + namespace + "/font";
            for (String path : index.pathsUnder(prefix)) {
                if (!path.endsWith(".json")) continue;
                String text = index.getText(path);
                if (text == null) continue;

                try {
                    JsonObject root = gson.fromJson(text, JsonObject.class);
                    if (!root.has("providers")) continue;

                    for (JsonElement el : root.getAsJsonArray("providers")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        String type = obj.has("type") ? obj.get("type").getAsString() : "";

                        FontProvider provider = switch (type) {
                            case "bitmap" -> parseBitmapProvider(obj, namespace);
                            case "space"  -> parseSpaceProvider(obj, namespace);
                            default       -> null;
                        };

                        if (provider != null) {
                            providers.add(provider);
                        }
                    }
                } catch (JsonSyntaxException e) {
                    // Skip malformed font files
                }
            }
        }

        return providers;
    }

    private FontProvider parseBitmapProvider(JsonObject obj, String defaultNamespace) {
        if (!obj.has("file") || !obj.has("chars")) return null;

        String file = obj.get("file").getAsString();
        int ascent = obj.has("ascent") ? obj.get("ascent").getAsInt() : 8;
        int height = obj.has("height") ? obj.get("height").getAsInt() : 16;
        JsonArray charsArr = obj.getAsJsonArray("chars");
        List<String> charRows = new ArrayList<>();
        for (JsonElement row : charsArr) {
            charRows.add(row.getAsString());
        }

        if (charRows.isEmpty()) return null;

        return new FontProvider(file, ascent, height, charRows, defaultNamespace);
    }

    private FontProvider parseSpaceProvider(JsonObject obj, String defaultNamespace) {
        if (!obj.has("advances")) return null;

        JsonObject advancesObj = obj.getAsJsonObject("advances");
        Map<String, Integer> advances = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : advancesObj.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                advances.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }

        if (advances.isEmpty()) return null;

        return new FontProvider(advances, defaultNamespace);
    }
}
