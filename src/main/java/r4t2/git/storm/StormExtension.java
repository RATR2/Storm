package r4t2.git.storm;

import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.event.subscribe.Subscribe;
import r4t2.git.storm.config.StormConfig;
import r4t2.git.storm.converter.ItemConverter;
import r4t2.git.storm.converter.font.FontConverter;
import r4t2.git.storm.discovery.DiscoveredItem;
import r4t2.git.storm.discovery.FontDiscovery;
import r4t2.git.storm.discovery.ItemDiscovery;
import r4t2.git.storm.geyser.GeyserRegistrar;
import r4t2.git.storm.mappings.GeyserMappingWriter;
import r4t2.git.storm.mappings.ItemMapping;
import r4t2.git.storm.output.McpackBuilder;
import r4t2.git.storm.pack.PackIndex;
import r4t2.git.storm.pack.PackLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StormExtension implements Extension {

    private StormConfig config;

    @Subscribe
    public void onPostInitialize() {
        logger().info("Storm is starting up...");

        // Save default config if it doesn't exist
        Path configPath = dataFolder().resolve("config.yml");
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(dataFolder());
                try (var in = getClass().getClassLoader().getResourceAsStream("default_config.yml")) {
                    if (in != null) {
                        Files.copy(in, configPath);
                    }
                }
            } catch (Exception e) {
                logger().error("Failed to save default config", e);
            }
        }

        config = StormConfig.load(configPath);

        File packFile = new File(config.getPackPath());
        if (!packFile.exists()) {
            logger().warning("Pack file not found at: " + config.getPackPath());
            logger().warning("Storm will not register any custom items or fonts.");
            return;
        }

        try {
            run(packFile);
        } catch (Exception e) {
            logger().error("Storm encountered a fatal error during conversion", e);
        }
    }

    private void run(File packFile) throws Exception {
        logger().info("Loading pack: " + packFile.getName());

        PackLoader loader = new PackLoader(packFile.toPath(), config.isDebug() ? logger() : null);
        PackIndex index = loader.load();
        logger().info("Indexed " + index.entryCount() + " pack entries across namespaces: " + index.namespaces());

        ItemDiscovery itemDiscovery = new ItemDiscovery(index);
        List<DiscoveredItem> items = itemDiscovery.discover();
        logger().info("Discovered " + items.size() + " custom items");

        FontDiscovery fontDiscovery = new FontDiscovery(index);
        var fontProviders = fontDiscovery.discover();
        logger().info("Discovered " + fontProviders.size() + " font providers");

        ItemConverter itemConverter = new ItemConverter(index, config.isDebug() ? logger() : null);
        List<ItemMapping> mappings = itemConverter.convertAll(items);
        logger().info("Converted " + mappings.size() + " item mappings");

        FontConverter fontConverter = new FontConverter(index);
        var glyphPages = fontConverter.convertAll(fontProviders);
        logger().info("Built " + glyphPages.size() + " Bedrock glyph page(s)");

        Path outputDir = Path.of(config.getOutputDir());
        Files.createDirectories(outputDir);
        GeyserMappingWriter mappingWriter = new GeyserMappingWriter(outputDir);
        Path mappingsFile = mappingWriter.write(mappings);
        logger().info("Wrote Geyser mappings to: " + mappingsFile);

        McpackBuilder mcpackBuilder = new McpackBuilder(outputDir, index);
        Path mcpack = mcpackBuilder.build(mappings, glyphPages);
        logger().info("Built Bedrock resource pack: " + mcpack);

        GeyserRegistrar registrar = new GeyserRegistrar(this, logger());
        registrar.register(mappingsFile, mcpack);

        logger().info("Storm finished! " + mappings.size() + " items and " + glyphPages.size() + " glyph pages registered.");
    }

    public StormConfig getConfig() {
        return config;
    }
}
