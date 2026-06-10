package r4t2.git.storm.pack;

import org.geysermc.geyser.api.extension.ExtensionLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads every entry from the resource pack ZIP into memory,
 * keyed by their normalized path (e.g. "assets/ns/items/foo.json").
 */
public class PackLoader {

    private final Path zipPath;
    private final ExtensionLogger logger;

    public PackLoader(Path zipPath, ExtensionLogger logger) {
        this.zipPath = zipPath;
        this.logger = logger;
    }

    public PackIndex load() throws IOException {
        Map<String, byte[]> entries = new HashMap<>();

        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            var enumeration = zip.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();
                if (entry.isDirectory()) continue;

                String name = normalizePath(entry.getName());
                try (InputStream in = zip.getInputStream(entry)) {
                    entries.put(name, readAll(in));
                }

                if (logger != null) {
                    logger.debug("[Storm] Indexed: " + name);
                }
            }
        }

        return new PackIndex(entries);
    }

    private static String normalizePath(String raw) {
        String normalized = raw.replace('\\', '/');
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buf.write(chunk, 0, n);
        }
        return buf.toByteArray();
    }
}
