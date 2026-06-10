package r4t2.git.storm.output;

/**
 * Represents one entry in Bedrock Edition's item_texture.json.
 *
 * Format:
 * {
 *   "texture_data": {
 *     "example_item_sword": {
 *       "textures": "textures/items/storm/example_item_sword"
 *     }
 *   }
 * }
 *
 * The key is the short-name referenced by attachables and custom item definitions.
 * The value is the path inside the Bedrock resource pack (without .png extension).
 */
public class BedrockItemEntry {

    private final String key;
    private final String bedrockPath;
    private final String sourcePath;

    public BedrockItemEntry(String key, String bedrockPath, String sourcePath) {
        this.key = key;
        this.bedrockPath = bedrockPath;
        this.sourcePath = sourcePath;
    }

    public String getKey() { return key; }
    public String getBedrockPath() { return bedrockPath; }
    public String getSourcePath() { return sourcePath; }

    public String bedrockPathWithExtension() {
        return bedrockPath + ".png";
    }
}
