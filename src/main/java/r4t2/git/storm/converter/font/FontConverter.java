package r4t2.git.storm.converter.font;

import r4t2.git.storm.pack.PackIndex;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * Orchestrates the full Java -> Bedrock font conversion pipeline.
 *
 * Steps:
 *  1. For each FontProvider, load the sprite sheet PNG from the PackIndex.
 *  2. Use GlyphExtractor to crop each glyph cell.
 *  3. Accumulate all glyphs (multiple providers can contribute to the same page).
 *  4. Use GlyphPageBuilder to render final Bedrock glyph_XX.png pages.
 *
 * Returns a map of Bedrock page name -> PNG bytes,
 * ready to be written into the .mcpack under "font/".
 */
public class FontConverter {

    private final PackIndex index;

    public FontConverter(PackIndex index) {
        this.index = index;
    }

    /**
     * Converts all font providers and returns Bedrock glyph pages.
     *
     * @param providers List of discovered font providers.
     * @return Map from page name (e.g. "E0") to PNG bytes.
     */
    public Map<String, byte[]> convertAll(List<FontProvider> providers) throws IOException {
        GlyphExtractor extractor = new GlyphExtractor();
        Map<Integer, BufferedImage> allGlyphs = new LinkedHashMap<>();

        for (FontProvider provider : providers) {
            String texturePath = provider.resolvedTexturePath();
            byte[] pngBytes = index.get(texturePath);
            if (pngBytes == null) {
                continue;
            }

            try {
                Map<Integer, BufferedImage> glyphs = extractor.extract(provider, pngBytes);
                allGlyphs.putAll(glyphs);
            } catch (IOException e) {
                //
            }
        }

        if (allGlyphs.isEmpty()) {
            return Collections.emptyMap();
        }

        GlyphPageBuilder builder = new GlyphPageBuilder();
        return builder.build(allGlyphs);
    }
}
