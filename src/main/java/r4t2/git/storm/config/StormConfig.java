package r4t2.git.storm.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class StormConfig {

    private final String packPath;
    private final String outputDir;
    private final boolean debug;

    private StormConfig(String packPath, String outputDir, boolean debug) {
        this.packPath = packPath;
        this.outputDir = outputDir;
        this.debug = debug;
    }

    public static StormConfig load(Path configPath) {
        Map<String, String> values = new HashMap<>();
        try {
            for (String line : Files.readAllLines(configPath)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int colon = trimmed.indexOf(':');
                if (colon < 0) continue;
                String key = trimmed.substring(0, colon).trim();
                String value = trimmed.substring(colon + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException e) {
            // Fall through to defaults
        }

        String packPath = values.getOrDefault("pack-path", "plugins/CraftEngine/build.zip");
        String outputDir = values.getOrDefault("output-dir", "plugins/Geyser-Spigot/custom_mappings");
        boolean debug = Boolean.parseBoolean(values.getOrDefault("debug", "false"));
        return new StormConfig(packPath, outputDir, debug);
    }

    public String getPackPath() {
        return packPath;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public boolean isDebug() {
        return debug;
    }
}
