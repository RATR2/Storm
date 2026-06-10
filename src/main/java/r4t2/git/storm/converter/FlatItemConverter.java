package r4t2.git.storm.converter;

import r4t2.git.storm.discovery.DiscoveredItem;
import r4t2.git.storm.mappings.ItemMapping;
import r4t2.git.storm.model.JavaModel;
import r4t2.git.storm.model.ModelResolver;
import r4t2.git.storm.pack.NamespaceResolver;
import r4t2.git.storm.pack.PackIndex;

/**
 * Converts flat (2D sprite) custom items -- those whose model chain
 * ultimately inherits from minecraft:item/generated or minecraft:item/handheld.
 */
public class FlatItemConverter {

    private final ModelResolver modelResolver;
    private final TextureMapper textureMapper;

    public FlatItemConverter(PackIndex index) {
        this.modelResolver = new ModelResolver(index);
        this.textureMapper = new TextureMapper(index);
    }

    /**
     * Attempts to convert a flat item.
     *
     * @return An ItemMapping, or null if the item is not flat or has no resolvable texture.
     */
    public ItemMapping convert(DiscoveredItem item) {
        JavaModel model;
        try {
            model = modelResolver.resolve(item.getModelReference());
        } catch (Exception e) {
            return null;
        }

        if (!model.isFlatItem()) return null;

        String texId = textureMapper.resolvePrimaryTexture(model);
        if (texId == null) return null;

        // Build a safe Bedrock texture key
        String bedrockKey = textureMapper.toBedrockKey(texId);
        String packTexturePath = textureMapper.toPackPath(texId);

        // Build the Bedrock item identifier: "storm:<safe_name>"
        NamespaceResolver nr = new NamespaceResolver(item.getItemId());
        String bedrockIdentifier = "storm:" + nr.toBedrockId();

        return ItemMapping.flat(
                item.getItemId(),
                bedrockIdentifier,
                bedrockKey,
                packTexturePath,
                texId
        );
    }
}
