package r4t2.git.storm.mappings;

import com.google.gson.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes the Geyser custom item mappings JSON file.
 *
 * Geyser mappings format (custom_mappings/*.json):
 * {
 *   "format_version": "1",
 *   "items": {
 *     "example:magic_sword": [
 *       {
 *         "name": "storm:example_magic_sword",
 *         "display_name": "Example Magic Sword",
 *         "icon": "example_item_magic_sword",
 *         "allow_offhand": true,
 *         "texture_size": 16,
 *         "bedrock_data": 0
 *       }
 *     ]
 *   }
 * }
 *
 * For 3D items the entry also includes:
 *   "geometry": "geometry.storm.example_item_magic_sword"
 *   "render_offsets": { ... }
 */
public class GeyserMappingWriter {

    private static final String FORMAT_VERSION = "1";
    private final Path outputDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public GeyserMappingWriter(Path outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Writes all item mappings to {@code storm_mappings.json} in the output directory.
     *
     * @return The path of the written file.
     */
    public Path write(List<ItemMapping> mappings) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("format_version", FORMAT_VERSION);

        JsonObject items = new JsonObject();

        for (ItemMapping mapping : mappings) {
            JsonArray entries = new JsonArray();
            JsonObject entry = buildEntry(mapping);
            entries.add(entry);
            items.add(mapping.getJavaItemId(), entries);
        }

        root.add("items", items);

        Path outFile = outputDir.resolve("storm_mappings.json");
        Files.writeString(outFile, gson.toJson(root), StandardCharsets.UTF_8);
        return outFile;
    }

    private JsonObject buildEntry(ItemMapping mapping) {
        JsonObject entry = new JsonObject();
        entry.addProperty("name", mapping.getBedrockIdentifier());
        entry.addProperty("display_name", toDisplayName(mapping.getJavaItemId()));
        entry.addProperty("icon", mapping.getBedrockTextureKey());
        entry.addProperty("allow_offhand", true);
        entry.addProperty("texture_size", 16);
        entry.addProperty("bedrock_data", 0);

        if (mapping.isGeometry() && mapping.getGeometry() != null) {
            entry.addProperty("geometry", mapping.getGeometry().getIdentifier());
            entry.add("render_offsets", buildDefaultRenderOffsets());
        }

        return entry;
    }

    private JsonObject buildDefaultRenderOffsets() {
        JsonObject offsets = new JsonObject();

        offsets.add("main_hand", buildOffsetSlot(
                new float[]{0, 0, 0},
                new float[]{0, 0, 0},
                new float[]{1, 1, 1}
        ));
        offsets.add("off_hand", buildOffsetSlot(
                new float[]{0, 0, 0},
                new float[]{0, 0, 0},
                new float[]{1, 1, 1}
        ));

        return offsets;
    }

    private JsonObject buildOffsetSlot(float[] position, float[] rotation, float[] scale) {
        JsonObject slot = new JsonObject();
        slot.add("position", floatArray(position));
        slot.add("rotation", floatArray(rotation));
        slot.add("scale", floatArray(scale));
        return slot;
    }

    private JsonArray floatArray(float[] arr) {
        JsonArray ja = new JsonArray();
        for (float f : arr) ja.add(f);
        return ja;
    }

    private String toDisplayName(String itemId) {
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        String[] parts = path.replace('/', '_').split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }
}
