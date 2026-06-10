package r4t2.git.storm.pack;

/**
 * Splits a namespaced ID like "example:item/my_sword" into its
 * namespace ("example") and path ("item/my_sword") components.
 * Falls back to "minecraft" as the default namespace.
 */
public class NamespaceResolver {

    private final String namespace;
    private final String path;

    public NamespaceResolver(String namespacedId) {
        if (namespacedId == null || namespacedId.isEmpty()) {
            this.namespace = "minecraft";
            this.path = "";
            return;
        }
        int colon = namespacedId.indexOf(':');
        if (colon < 0) {
            this.namespace = "minecraft";
            this.path = namespacedId;
        } else {
            this.namespace = namespacedId.substring(0, colon);
            this.path = namespacedId.substring(colon + 1);
        }
    }

    public String namespace() {
        return namespace;
    }

    public String path() {
        return path;
    }

    public String full() {
        return namespace + ":" + path;
    }

    public String toBedrockId() {
        return (namespace + "_" + path).replace('/', '_').replace(':', '_');
    }

    @Override
    public String toString() {
        return full();
    }
}
