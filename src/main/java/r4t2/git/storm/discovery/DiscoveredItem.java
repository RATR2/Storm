package r4t2.git.storm.discovery;

/**
 * Represents a custom item discovered in the Java Edition resource pack.
 *
 * In Java 1.21.4+, items are defined via assets/&lt;ns&gt;/items/&lt;id&gt;.json
 * which contains a "model" field pointing to a model file.
 *
 * Example:
 * assets/example/items/magic_sword.json -> { "model": { "type": "model", "model": "example:item/magic_sword" } }
 */
public class DiscoveredItem {

    private final String itemId;
    private final String modelReference;
    
    public DiscoveredItem(String itemId, String modelReference) {
        this.itemId = itemId;
        this.modelReference = modelReference;
    }

    public String getItemId() {
        return itemId;
    }

    public String getModelReference() {
        return modelReference;
    }

    @Override
    public String toString() {
        return "DiscoveredItem{itemId='" + itemId + "', model='" + modelReference + "'}";
    }
}
