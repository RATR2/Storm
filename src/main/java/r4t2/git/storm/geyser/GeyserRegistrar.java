package r4t2.git.storm.geyser;

import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineResourcePacksEvent;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.api.pack.PackCodec;
import java.nio.file.Path;

public class GeyserRegistrar {

    private final Extension extension;
    private final ExtensionLogger logger;

    public GeyserRegistrar(Extension extension, ExtensionLogger logger) {
        this.extension = extension;
        this.logger = logger;
    }

    /**
     * Registers the Bedrock resource pack with Geyser so it is sent to connecting clients.
     * The mapping file is already in the custom_mappings directory; Geyser reads it on next reload.
     *
     * @param mappingsFile Path to the written storm_mappings.json.
     * @param mcpackPath   Path to the written storm_resources.mcpack.
     */
    public void register(Path mappingsFile, Path mcpackPath) {
        extension.eventBus().subscribe(GeyserDefineResourcePacksEvent.class, event -> {
            try {
                ResourcePack bedrockPack = ResourcePack.create(PackCodec.path(mcpackPath));
                event.register(bedrockPack);
                logger.info("Successfully registered Bedrock resource pack with Geyser: " + mcpackPath.getFileName());
            } catch (Exception e) {
                logger.error("Failed to register Bedrock resource pack with Geyser lifecycle", e);
            }
        });
    }
}
