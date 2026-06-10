package r4t2.git.storm.converter.font;

import r4t2.git.storm.pack.NamespaceResolver;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A parsed Java Edition font provider.
 *
 * Supports two provider types:
 *
 * bitmap -- a sprite sheet mapped to a set of characters:
 *   The "chars" field is a list of strings where each string is one ROW
 *   and each character in the string is one COLUMN (one glyph cell).
 *   The null character '\u0000' means "skip this cell".
 *
 *   So for a 3x2 sprite sheet:
 *     chars: ["\uE000\uE001\uE002", "\uE003\uE004\uE005"]
 *   means row 0 has glyphs E000, E001, E002 and row 1 has E003, E004, E005.
 *
 * space -- maps characters to fixed advance widths (no texture):
 *   { "type": "space", "advances": { "\uE100": 4, "\uE101": -1 } }
 */
public class FontProvider {

    public enum Type { BITMAP, SPACE }

    private final String file;
    private final int ascent;
    private final int height;
    private final List<String> charRows;
    private final Map<String, Integer> advances;
    private final String defaultNamespace;
    private final Type type;

    /** Constructor for bitmap providers. */
    public FontProvider(String file, int ascent, int height, List<String> charRows, String defaultNamespace) {
        this.type = Type.BITMAP;
        this.file = file;
        this.ascent = ascent;
        this.height = height;
        this.charRows = List.copyOf(charRows);
        this.advances = Collections.emptyMap();
        this.defaultNamespace = defaultNamespace;
    }

    /** Constructor for space providers. */
    public FontProvider(Map<String, Integer> advances, String defaultNamespace) {
        this.type = Type.SPACE;
        this.file = null;
        this.ascent = 0;
        this.height = 0;
        this.charRows = Collections.emptyList();
        this.advances = Map.copyOf(advances);
        this.defaultNamespace = defaultNamespace;
    }

    public Type getType() { return type; }
    public boolean isBitmap() { return type == Type.BITMAP; } // bitmap provider
    public boolean isSpace() { return type == Type.SPACE; } // space provider
    public String getFile() { return file; }
    public int getAscent() { return ascent; }
    public int getHeight() { return height; }
    public List<String> getCharRows() { return charRows; }
    public Map<String, Integer> getAdvances() { return advances; }
    public String getDefaultNamespace() { return defaultNamespace; }

    public int rows() { return charRows.size(); }

    public int cols() {
        return charRows.stream().mapToInt(String::length).max().orElse(0);
    }

    /**
     * Returns the pack-relative path for the texture image
     * (e.g. "assets/example/textures/font/icons.png").
     * Only valid for bitmap providers.
     */
    public String resolvedTexturePath() {
        if (file == null) return null;
        NamespaceResolver nr = new NamespaceResolver(file.replace(".png", ""));
        // Font files live under textures/ in the pack
        return "assets/" + nr.namespace() + "/textures/" + nr.path() + ".png";
    }

    @Override
    public String toString() {
        if (type == Type.SPACE) {
            return "FontProvider{type=space, advances=" + advances.size() + "}";
        }
        return "FontProvider{type=bitmap, file='" + file + "', rows=" + rows() + ", cols=" + cols() + "}";
    }
}
