package r4t2.git.storm.converter.font;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Assembles Bedrock Edition glyph page PNGs from a collection of extracted glyphs.
 *
 * Bedrock glyph page layout:
 *  - One PNG per Unicode page (upper byte of codepoint).
 *  - File name: glyph_E0.png for page 0xE0, glyph_E1.png for 0xE1, etc.
 *  - Each page is a 256x256 pixel PNG containing a 16x16 grid of glyph cells.
 *  - Cell size: 256/16 = 16 pixels each.
 *  - Position of codepoint U+EABC: page E = "EA", row = B (1), col = C (12).
 *    x = col * 16 = 192, y = row * 16 = 16.
 *
 * Glyphs are resized to fit the cell size (16x16) before blitting.
 *
 * The key format for each page is its upper-byte hex string, e.g. "E0".
 */
public class GlyphPageBuilder {

    /** Side length of each Bedrock glyph page in pixels. */
    public static final int PAGE_SIZE = 256;

    /** Number of cells per row/column (16x16 grid). */
    public static final int GRID = 16;

    /** Pixel size of each glyph cell (256 / 16 = 16). */
    public static final int CELL_SIZE = PAGE_SIZE / GRID;

    /**
     * Builds all necessary glyph pages from the provided glyph map.
     *
     * @param glyphs Map from Unicode codepoint to glyph image (any size).
     * @return Map from page name (e.g. "E0") to PNG bytes.
     */
    public Map<String, byte[]> build(Map<Integer, BufferedImage> glyphs) throws IOException {
        // Group by page (upper byte)
        Map<String, Map<Integer, BufferedImage>> byPage = new LinkedHashMap<>();
        for (Map.Entry<Integer, BufferedImage> entry : glyphs.entrySet()) {
            int cp = entry.getKey();
            String page = String.format("%02X", (cp >> 8) & 0xFF).toUpperCase();
            byPage.computeIfAbsent(page, k -> new HashMap<>()).put(cp, entry.getValue());
        }

        Map<String, byte[]> result = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Integer, BufferedImage>> pageEntry : byPage.entrySet()) {
            String pageName = pageEntry.getKey();
            BufferedImage pageImg = buildPage(pageEntry.getValue());
            result.put(pageName, toPng(pageImg));
        }
        return result;
    }

    private BufferedImage buildPage(Map<Integer, BufferedImage> glyphs) {
        BufferedImage page = new BufferedImage(PAGE_SIZE, PAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = page.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, PAGE_SIZE, PAGE_SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        for (Map.Entry<Integer, BufferedImage> entry : glyphs.entrySet()) {
            int cp = entry.getKey();
            int lowByte = cp & 0xFF;
            int col = lowByte & 0x0F;       // lower nibble
            int row = (lowByte >> 4) & 0x0F; // upper nibble

            int x = col * CELL_SIZE;
            int y = row * CELL_SIZE;

            g.drawImage(entry.getValue(), x, y, CELL_SIZE, CELL_SIZE, null);
        }

        g.dispose();
        return page;
    }

    private byte[] toPng(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return baos.toByteArray();
    }
}
