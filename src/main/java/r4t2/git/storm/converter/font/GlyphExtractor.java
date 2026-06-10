package r4t2.git.storm.converter.font;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Crops individual glyph images from a Java Edition bitmap font sprite sheet.
 *
 * Given a FontProvider and its raw PNG bytes, this class:
 *  1. Loads the sprite sheet as a BufferedImage.
 *  2. Divides it into a grid of (rows x cols) cells.
 *  3. Crops each cell to a BufferedImage keyed by Unicode codepoint.
 *  4. Skips cells mapped to '\u0000' (the Java "skip" sentinel).
 */
public class GlyphExtractor {

    /**
     * Extracts all glyphs from the given provider and raw PNG data.
     *
     * @param provider The font provider describing the char grid.
     * @param pngBytes Raw bytes of the sprite sheet PNG.
     * @return Map from Unicode codepoint (int) to glyph BufferedImage.
     * @throws IOException if the PNG cannot be decoded.
     */
    public Map<Integer, BufferedImage> extract(FontProvider provider, byte[] pngBytes) throws IOException {
        BufferedImage sheet = ImageIO.read(new ByteArrayInputStream(pngBytes));
        if (sheet == null) {
            throw new IOException("Could not decode PNG for font: " + provider.getFile());
        }

        int rows = provider.rows();
        int cols = provider.cols();

        if (rows == 0 || cols == 0) return Map.of();

        int cellW = sheet.getWidth() / cols;
        int cellH = sheet.getHeight() / rows;

        Map<Integer, BufferedImage> glyphs = new HashMap<>();

        for (int row = 0; row < provider.getCharRows().size(); row++) {
            String rowStr = provider.getCharRows().get(row);
            for (int col = 0; col < rowStr.length(); col++) {
                char ch = rowStr.charAt(col);
                if (ch == '\u0000') continue; // skip sentinel

                int x = col * cellW;
                int y = row * cellH;

                // Guard against sheet being smaller than expected
                if (x + cellW > sheet.getWidth() || y + cellH > sheet.getHeight()) continue;

                BufferedImage cell = sheet.getSubimage(x, y, cellW, cellH);
                // Make a copy so we're not holding a reference to the whole sheet
                BufferedImage copy = new BufferedImage(cellW, cellH, BufferedImage.TYPE_INT_ARGB);
                copy.getGraphics().drawImage(cell, 0, 0, null);
                glyphs.put((int) ch, copy);
            }
        }

        return glyphs;
    }
}
