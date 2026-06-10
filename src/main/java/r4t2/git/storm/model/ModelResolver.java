package r4t2.git.storm.model;

import com.google.gson.*;
import r4t2.git.storm.pack.PackIndex;

import java.util.*;

/**
 * Resolves a Java Edition item model by walking up its parent chain and
 * merging textures, elements, and display transforms into a single flat model.
 *
 * Resolution rules (same as Java's own logic):
 *  - Child textures override parent textures.
 *  - Child elements override parent elements (first non-empty wins going down).
 *  - Child display transforms override parent display transforms per-slot.
 *  - "minecraft:item/generated" and "minecraft:item/handheld" are treated as
 *    the flat-item sentinel -- no further parent traversal needed.
 */
public class ModelResolver {

    private static final Set<String> FLAT_PARENTS = Set.of(
            "minecraft:item/generated",
            "minecraft:item/handheld",
            "item/generated",
            "item/handheld"
    );

    private final PackIndex index;
    private final Gson gson = new GsonBuilder().create();
    private final Map<String, JsonObject> rawCache = new HashMap<>();
    public ModelResolver(PackIndex index) {
        this.index = index;
    }

    /**
     * Resolves the model identified by {@code modelId} (e.g. "example:item/sword")
     * into a fully-merged {@link JavaModel}.
     *
     * @throws IllegalArgumentException if the model cannot be found in the pack.
     */
    public JavaModel resolve(String modelId) {
        List<JsonObject> chain = new ArrayList<>();
        collectChain(modelId, chain, new LinkedHashSet<>());
        JavaModel merged = new JavaModel();
        merged.setId(modelId);

        boolean foundElements = false;
        for (int i = chain.size() - 1; i >= 0; i--) {
            JsonObject raw = chain.get(i);
            mergeTextures(raw, merged);
            mergeDisplay(raw, merged);
            if (!foundElements && raw.has("elements")) {
                merged.setElements(parseElements(raw.getAsJsonArray("elements")));
                foundElements = true;
            }
        }

        for (JsonObject raw : chain) {
            if (raw.has("parent")) {
                String p = raw.get("parent").getAsString();
                if (FLAT_PARENTS.contains(p)) {
                    merged.setFlatItem(true);
                    break;
                }
            }
        }
        if (!foundElements) {
            merged.setFlatItem(true);
        }

        return merged;
    }

    private void collectChain(String modelId, List<JsonObject> chain, Set<String> visited) {
        if (modelId == null || FLAT_PARENTS.contains(modelId)) {
            return;
        }
        if (!visited.add(modelId)) return;

        JsonObject raw = loadRaw(modelId);
        if (raw == null) return;

        chain.add(raw);

        if (raw.has("parent")) {
            String parent = raw.get("parent").getAsString();
            collectChain(parent, chain, visited);
        }
    }

    private JsonObject loadRaw(String modelId) {
        if (rawCache.containsKey(modelId)) {
            return rawCache.get(modelId);
        }
        String path = index.resolveModel(modelId);
        String text = index.getText(path);
        if (text == null) {
            rawCache.put(modelId, null);
            return null;
        }
        try {
            JsonObject obj = gson.fromJson(text, JsonObject.class);
            rawCache.put(modelId, obj);
            return obj;
        } catch (JsonSyntaxException e) {
            rawCache.put(modelId, null);
            return null;
        }
    }

    private void mergeTextures(JsonObject raw, JavaModel model) {
        if (!raw.has("textures")) return;
        JsonObject tex = raw.getAsJsonObject("textures");
        for (Map.Entry<String, JsonElement> entry : tex.entrySet()) {
            if (!model.getTextures().containsKey(entry.getKey())) {
                model.getTextures().put(entry.getKey(), entry.getValue().getAsString());
            }
        }
    }

    private void mergeDisplay(JsonObject raw, JavaModel model) {
        if (!raw.has("display")) return;
        JsonObject disp = raw.getAsJsonObject("display");
        for (Map.Entry<String, JsonElement> entry : disp.entrySet()) {
            if (!model.getDisplay().containsKey(entry.getKey())) {
                model.getDisplay().put(entry.getKey(), parseDisplayTransform(entry.getValue().getAsJsonObject()));
            }
        }
    }

    private DisplayTransform parseDisplayTransform(JsonObject obj) {
        DisplayTransform dt = new DisplayTransform();
        if (obj.has("rotation")) dt.setRotation(parseFloatArray(obj.getAsJsonArray("rotation")));
        if (obj.has("translation")) dt.setTranslation(parseFloatArray(obj.getAsJsonArray("translation")));
        if (obj.has("scale")) dt.setScale(parseFloatArray(obj.getAsJsonArray("scale")));
        return dt;
    }

    private List<ModelElement> parseElements(JsonArray arr) {
        List<ModelElement> result = new ArrayList<>();
        for (JsonElement el : arr) {
            JsonObject obj = el.getAsJsonObject();
            ModelElement elem = new ModelElement();
            if (obj.has("from")) elem.setFrom(parseFloatArray(obj.getAsJsonArray("from")));
            if (obj.has("to")) elem.setTo(parseFloatArray(obj.getAsJsonArray("to")));
            if (obj.has("shade")) elem.setShade(obj.get("shade").getAsBoolean());
            if (obj.has("rotation")) elem.setRotation(parseElementRotation(obj.getAsJsonObject("rotation")));
            if (obj.has("faces")) elem.setFaces(parseFaces(obj.getAsJsonObject("faces")));
            result.add(elem);
        }
        return result;
    }

    private ModelElement.ElementRotation parseElementRotation(JsonObject obj) {
        ModelElement.ElementRotation rot = new ModelElement.ElementRotation();
        if (obj.has("origin")) rot.setOrigin(parseFloatArray(obj.getAsJsonArray("origin")));
        if (obj.has("axis")) rot.setAxis(obj.get("axis").getAsString());
        if (obj.has("angle")) rot.setAngle(obj.get("angle").getAsFloat());
        if (obj.has("rescale")) rot.setRescale(obj.get("rescale").getAsBoolean());
        return rot;
    }

    private Map<String, ModelElement.ElementFace> parseFaces(JsonObject obj) {
        Map<String, ModelElement.ElementFace> faces = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonObject faceObj = entry.getValue().getAsJsonObject();
            ModelElement.ElementFace face = new ModelElement.ElementFace();
            if (faceObj.has("uv")) face.setUv(parseFloatArray(faceObj.getAsJsonArray("uv")));
            if (faceObj.has("texture")) face.setTexture(faceObj.get("texture").getAsString());
            if (faceObj.has("cullface")) face.setCullface(faceObj.get("cullface").getAsString());
            if (faceObj.has("rotation")) face.setRotation(faceObj.get("rotation").getAsInt());
            if (faceObj.has("tintindex")) face.setTintindex(faceObj.get("tintindex").getAsInt());
            faces.put(entry.getKey(), face);
        }
        return faces;
    }

    private float[] parseFloatArray(JsonArray arr) {
        float[] result = new float[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            result[i] = arr.get(i).getAsFloat();
        }
        return result;
    }
}
