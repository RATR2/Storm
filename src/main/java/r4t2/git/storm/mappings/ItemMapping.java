package r4t2.git.storm.mappings;

import r4t2.git.storm.output.BedrockGeometry;

/**
 * Represents a fully-converted custom item, carrying all data needed to:
 *  - Write a Geyser custom item mapping JSON entry.
 *  - Write a Bedrock item_texture.json entry.
 *  - Write a Bedrock geometry.json entry (3D items only).
 *  - Copy the texture PNG into the .mcpack.
 */
public class ItemMapping {

    public enum Type { FLAT, GEOMETRY }

    private final Type type;
    private final String javaItemId;
    private final String bedrockIdentifier;
    private final String bedrockTextureKey;
    private final String sourceTexturePath;
    private final String namespacedTextureId;
    private final BedrockGeometry geometry;

    private ItemMapping(Type type, String javaItemId, String bedrockIdentifier,
                        String bedrockTextureKey, String sourceTexturePath,
                        String namespacedTextureId, BedrockGeometry geometry) {
        this.type = type;
        this.javaItemId = javaItemId;
        this.bedrockIdentifier = bedrockIdentifier;
        this.bedrockTextureKey = bedrockTextureKey;
        this.sourceTexturePath = sourceTexturePath;
        this.namespacedTextureId = namespacedTextureId;
        this.geometry = geometry;
    }

    public static ItemMapping flat(String javaItemId, String bedrockIdentifier,
                                   String bedrockTextureKey, String sourceTexturePath,
                                   String namespacedTextureId) {
        return new ItemMapping(Type.FLAT, javaItemId, bedrockIdentifier,
                bedrockTextureKey, sourceTexturePath, namespacedTextureId, null);
    }

    public static ItemMapping geometry(String javaItemId, String bedrockIdentifier,
                                       String bedrockTextureKey, String sourceTexturePath,
                                       String namespacedTextureId, BedrockGeometry geo) {
        return new ItemMapping(Type.GEOMETRY, javaItemId, bedrockIdentifier,
                bedrockTextureKey, sourceTexturePath, namespacedTextureId, geo);
    }

    public Type getType() { return type; }
    public boolean isFlat() { return type == Type.FLAT; }
    public boolean isGeometry() { return type == Type.GEOMETRY; }

    public String getJavaItemId() { return javaItemId; }
    public String getBedrockIdentifier() { return bedrockIdentifier; }
    public String getBedrockTextureKey() { return bedrockTextureKey; }
    public String getSourceTexturePath() { return sourceTexturePath; }
    public String getNamespacedTextureId() { return namespacedTextureId; }
    public BedrockGeometry getGeometry() { return geometry; }

    @Override
    public String toString() {
        return "ItemMapping{" + type + ", java='" + javaItemId
                + "', bedrock='" + bedrockIdentifier + "'}";
    }
}
