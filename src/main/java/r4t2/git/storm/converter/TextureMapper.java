package r4t2.git.storm.converter;

import r4t2.git.storm.model.JavaModel;
import r4t2.git.storm.pack.NamespaceResolver;
import r4t2.git.storm.pack.PackIndex;

import java.util.*;

/**
 * Resolves Java Edition texture references into their actual pack paths,
 * and produces the Bedrock texture key name used in item_texture.json.
 *
 * In Java, a flat item's primary texture is stored as:
 *   model.textures["layer0"] = "example:item/my_sword"
 *
 * This mapper:
 *  1. Resolves the texture variable chain (e.g. "#layer0" -> "example:item/my_sword").
 *  2. Produces the pack-relative PNG path ("assets/example/textures/item/my_sword.png").
 *  3. Produces the Bedrock texture short-name ("example_item_my_sword").
 */
public class TextureMapper {

    private final PackIndex index;

    public TextureMapper(PackIndex index) {
        this.index = index;
    }
    public String resolvePrimaryTexture(JavaModel model) {
        for (String key : List.of("layer0", "layer1", "all")) {
            String raw = model.getTextures().get(key);
            if (raw != null) {
                return model.resolveTexture("#" + key);
            }
        }
        if (!model.getTextures().isEmpty()) {
            String firstKey = model.getTextures().keySet().iterator().next();
            return model.resolveTexture("#" + firstKey);
        }
        return null;
    }

    /**
     * Returns all texture variables resolved to their actual namespaced IDs.
     * Useful for 3D models that reference multiple textures.
     *
     * @return Map from variable name (without #) to resolved namespaced ID.
     */
    public Map<String, String> resolveAll(JavaModel model) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : model.getTextures().keySet()) {
            String resolved = model.resolveTexture("#" + key);
            if (resolved != null && !resolved.startsWith("#")) {
                result.put(key, resolved);
            }
        }
        return result;
    }

    public String toPackPath(String namespacedTextureId) {
        return index.resolveTexture(namespacedTextureId);
    }

    public String toBedrockKey(String namespacedTextureId) {
        return new NamespaceResolver(namespacedTextureId).toBedrockId();
    }

    public boolean textureExists(String namespacedTextureId) {
        return index.has(toPackPath(namespacedTextureId));
    }
}
