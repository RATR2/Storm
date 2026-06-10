package r4t2.git.storm.pack;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory index of every file in the resource pack ZIP.
 * All paths use forward slashes and are relative to the ZIP root
 * (e.g. "assets/example/items/my_sword.json").
 */
public class PackIndex {

    private final Map<String, byte[]> entries;

    public PackIndex(Map<String, byte[]> entries) {
        this.entries = Collections.unmodifiableMap(entries);
    }

    public byte[] get(String path) {
        return entries.get(path);
    }

    public String getText(String path) {
        byte[] data = entries.get(path);
        return data != null ? new String(data, StandardCharsets.UTF_8) : null;
    }

    public boolean has(String path) {
        return entries.containsKey(path);
    }

    public Set<String> allPaths() {
        return entries.keySet();
    }

    public int entryCount() {
        return entries.size();
    }

    public Set<String> namespaces() {
        Set<String> ns = new LinkedHashSet<>();
        for (String path : entries.keySet()) {
            if (path.startsWith("assets/")) {
                String[] parts = path.split("/", 3);
                if (parts.length >= 2) ns.add(parts[1]);
            }
        }
        return ns;
    }

    public List<String> pathsUnder(String prefix) {
        String p = prefix.endsWith("/") ? prefix : prefix + "/";
        return entries.keySet().stream()
                .filter(k -> k.startsWith(p))
                .sorted()
                .collect(Collectors.toList());
    }

    public String resolveModel(String namespacedId) {
        NamespaceResolver nr = new NamespaceResolver(namespacedId);
        return "assets/" + nr.namespace() + "/models/" + nr.path() + ".json";
    }

    public String resolveTexture(String namespacedId) {
        NamespaceResolver nr = new NamespaceResolver(namespacedId);
        return "assets/" + nr.namespace() + "/textures/" + nr.path() + ".png";
    }
}
