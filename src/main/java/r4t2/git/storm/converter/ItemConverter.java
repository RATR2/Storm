package r4t2.git.storm.converter;

import org.geysermc.geyser.api.extension.ExtensionLogger;
import r4t2.git.storm.discovery.DiscoveredItem;
import r4t2.git.storm.mappings.ItemMapping;
import r4t2.git.storm.model.JavaModel;
import r4t2.git.storm.model.ModelResolver;
import r4t2.git.storm.pack.NamespaceResolver;
import r4t2.git.storm.pack.PackIndex;
import r4t2.git.storm.output.BedrockGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates item conversion by delegating to {@link FlatItemConverter}
 * for 2D items and {@link GeometryConverter} for 3D items.
 */
public class ItemConverter {

    private final ExtensionLogger logger;
    private final FlatItemConverter flatConverter;
    private final GeometryConverter geoConverter;
    private final ModelResolver modelResolver;
    private final TextureMapper textureMapper;

    public ItemConverter(PackIndex index, ExtensionLogger logger) {
        this.logger = logger;
        this.flatConverter = new FlatItemConverter(index);
        this.geoConverter = new GeometryConverter();
        this.modelResolver = new ModelResolver(index);
        this.textureMapper = new TextureMapper(index);
    }

    public List<ItemMapping> convertAll(List<DiscoveredItem> items) {
        List<ItemMapping> mappings = new ArrayList<>();

        for (DiscoveredItem item : items) {
            try {
                ItemMapping mapping = convertOne(item);
                if (mapping != null) {
                    mappings.add(mapping);
                }
            } catch (Exception e) {
                if (logger != null) {
                    logger.warning("[Storm] Failed to convert item " + item.getItemId() + ": " + e.getMessage());
                }
            }
        }

        return mappings;
    }

    private ItemMapping convertOne(DiscoveredItem item) {
        JavaModel model = modelResolver.resolve(item.getModelReference());
        if (model.isFlatItem()) {
            return flatConverter.convert(item);
        }
        if (model.getElements().isEmpty()) {
            if (logger != null) {
                logger.warning("[Storm] Item " + item.getItemId() + " is not flat and has no elements -- skipping");
            }
            return null;
        }
        String texId = textureMapper.resolvePrimaryTexture(model);
        if (texId == null) {
            if (logger != null) {
                logger.warning("[Storm] Item " + item.getItemId() + " has no resolvable texture -- skipping");
            }
            return null;
        }

        String bedrockKey = textureMapper.toBedrockKey(texId);
        String packTexPath = textureMapper.toPackPath(texId);
        String bedrockIdentifier = "storm:" + new NamespaceResolver(item.getItemId()).toBedrockId();

        BedrockGeometry geo = geoConverter.convert(model, item.getItemId());

        return ItemMapping.geometry(
                item.getItemId(),
                bedrockIdentifier,
                bedrockKey,
                packTexPath,
                texId,
                geo
        );
    }
}
