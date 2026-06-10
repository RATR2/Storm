package r4t2.git.storm.model;

import java.util.*;

/**
 * Represents a fully-parsed Java Edition model JSON.
 * After resolution, {@code elements} and {@code textures} are
 * merged from the entire parent chain.
 */
public class JavaModel {

    private String id;
    private String parent;
    private Map<String, String> textures = new LinkedHashMap<>();
    private List<ModelElement> elements = new ArrayList<>();

    private Map<String, DisplayTransform> display = new LinkedHashMap<>();
    private boolean flatItem = false;
    private boolean vanillaFlatParent = false;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParent() { return parent; }
    public void setParent(String parent) { this.parent = parent; }

    public Map<String, String> getTextures() { return textures; }
    public void setTextures(Map<String, String> textures) { this.textures = textures; }

    public List<ModelElement> getElements() { return elements; }
    public void setElements(List<ModelElement> elements) { this.elements = elements; }

    public Map<String, DisplayTransform> getDisplay() { return display; }
    public void setDisplay(Map<String, DisplayTransform> display) { this.display = display; }

    public boolean isFlatItem() { return flatItem; }
    public void setFlatItem(boolean flatItem) { this.flatItem = flatItem; }

    public boolean isVanillaFlatParent() { return vanillaFlatParent; }
    public void setVanillaFlatParent(boolean vanillaFlatParent) { this.vanillaFlatParent = vanillaFlatParent; }

    public String resolveTexture(String key) {
        String value = key;
        Set<String> visited = new HashSet<>();
        while (value != null && value.startsWith("#")) {
            String ref = value.substring(1);
            if (!visited.add(ref)) break; 
            value = textures.get(ref);
        }
        return value;
    }

    @Override
    public String toString() {
        return "JavaModel{id='" + id + "', parent='" + parent + "', "
                + "elements=" + elements.size() + ", flatItem=" + flatItem + "}";
    }
}
