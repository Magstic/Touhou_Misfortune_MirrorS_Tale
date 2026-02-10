package touhou.font;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import touhou.i18n.I18n;

public final class BitmapFont {
    private static BitmapFont instance;

    private static final int COLOR_CACHE_SIZE = 8;

    private final int[] codepoints;
    private final byte[] glyphBits;
    private final byte[] glyphAdvance;
    private final int glyphStrideBytes;
    private final int glyphSizeBytes;
    private final int[] glyphScratch;

    private final int maskW;
    private final int maskH;
    private final int cellW;
    private final int cellH;
    private final int baseline;
    private final int cols;

    private final int[] maskPixels;

    private final char[] mapKeys;
    private final short[] mapValues;
    private final int mapMask;

    private final int[] cachedRgb = new int[COLOR_CACHE_SIZE];
    private final Image[] cachedPage = new Image[COLOR_CACHE_SIZE];
    private int cacheNext;

    private BitmapFont(int[] maskPixels, int maskW, int maskH, int cellW, int cellH, int baseline, char[] mapKeys,
            short[] mapValues, int mapMask) {
        this.codepoints = null;
        this.glyphBits = null;
        this.glyphAdvance = null;
        this.glyphStrideBytes = 0;
        this.glyphSizeBytes = 0;
        this.glyphScratch = null;

        this.maskPixels = maskPixels;
        this.maskW = maskW;
        this.maskH = maskH;
        this.cellW = cellW;
        this.cellH = cellH;
        this.baseline = baseline;
        this.cols = (cellW > 0) ? (maskW / cellW) : 0;
        this.mapKeys = mapKeys;
        this.mapValues = mapValues;
        this.mapMask = mapMask;
    }

    private BitmapFont(int cellW, int cellH, int baseline, int[] codepoints, byte[] glyphBits, byte[] glyphAdvance) {
        this.codepoints = codepoints;
        this.glyphBits = glyphBits;
        this.glyphAdvance = glyphAdvance;
        this.glyphStrideBytes = (cellW + 7) >> 3;
        this.glyphSizeBytes = this.glyphStrideBytes * cellH;
        this.glyphScratch = new int[cellW * cellH];

        this.maskPixels = null;
        this.maskW = 0;
        this.maskH = 0;
        this.cellW = cellW;
        this.cellH = cellH;
        this.baseline = baseline;
        this.cols = 0;
        this.mapKeys = null;
        this.mapValues = null;
        this.mapMask = 0;
    }

    public static BitmapFont get() {
        return instance;
    }

    public static void loadForCurrentLanguage() {
        instance = tryLoad(I18n.getLanguageCode());
    }

    public int getCellW() {
        return cellW;
    }

    public int getCellH() {
        return cellH;
    }

    public int getBaseline() {
        return baseline;
    }

    private static BitmapFont tryLoad(String lang) {
        if (lang == null || lang.length() == 0) {
            lang = "ja";
        }

        String base = "/res/i18n/" + lang + "/font/";

        BitmapFont dat = tryLoadDat(base);
        if (dat != null) {
            return dat;
        }

        Image mask;
        try {
            mask = Image.createImage(base + "font_page0.png");
        } catch (Throwable t) {
            mask = null;
        }
        if (mask == null) {
            return null;
        }

        int w = mask.getWidth();
        int h = mask.getHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }

        int[] pixels;
        try {
            pixels = new int[w * h];
            mask.getRGB(pixels, 0, w, 0, 0, w, h);
        } catch (Throwable t) {
            return null;
        }

        String mapText = readText(base + "font_map.txt");
        if (mapText == null) {
            return null;
        }

        // Default metrics for fusion-pixel-font-12px (monospace).
        int cellW = 12;
        int cellH = 12;
        int baseline = 10;

        // Build a small open-addressing map: char -> glyphIndex+1.
        int glyphCount = countChars(mapText);
        int cap = 1;
        while (cap < (glyphCount * 2)) {
            cap <<= 1;
        }
        if (cap < 16) {
            cap = 16;
        }
        char[] keys = new char[cap];
        short[] values = new short[cap];
        int maskBits = cap - 1;

        int glyphIndex = 0;
        for (int i = 0; i < mapText.length(); i++) {
            char c = mapText.charAt(i);
            if (c == '\r' || c == '\n') {
                continue;
            }
            put(keys, values, maskBits, c, glyphIndex + 1);
            glyphIndex++;
        }

        return new BitmapFont(pixels, w, h, cellW, cellH, baseline, keys, values, maskBits);
    }

    private static byte[] readBytes(String path) {
        return I18n.readResourceBytes(path);
    }

    private static BitmapFont tryLoadDat(String base) {
        byte[] bytes = readBytes(base + "font.dat");
        if (bytes == null || bytes.length < 12) {
            return null;
        }

        int p = 0;
        if (bytes[p++] != 'B' || bytes[p++] != 'F' || bytes[p++] != 'N' || bytes[p++] != 'T') {
            return null;
        }
        int ver = bytes[p++] & 0xFF;
        // v1: fixed width. v2: adds per-glyph advance table.
        if (ver != 1 && ver != 2) {
            return null;
        }
        int bpp = bytes[p++] & 0xFF;
        if (bpp != 1) {
            return null;
        }
        int cellW = bytes[p++] & 0xFF;
        int cellH = bytes[p++] & 0xFF;
        int baseline = bytes[p++] & 0xFF;
        // reserved
        p += 3;

        int glyphCount = ((bytes[p] & 0xFF) << 8) | (bytes[p + 1] & 0xFF);
        p += 2;
        if (glyphCount <= 0) {
            return null;
        }
        if (cellW <= 0 || cellH <= 0) {
            return null;
        }

        int tableBytes = glyphCount * 4;
        if (bytes.length < p + tableBytes) {
            return null;
        }
        int[] cps = new int[glyphCount];
        for (int i = 0; i < glyphCount; i++) {
            int v = ((bytes[p] & 0xFF) << 24) | ((bytes[p + 1] & 0xFF) << 16) | ((bytes[p + 2] & 0xFF) << 8) | (bytes[p + 3] & 0xFF);
            cps[i] = v;
            p += 4;
        }

        byte[] adv = null;
        if (ver >= 2) {
            if (bytes.length < p + glyphCount) {
                return null;
            }
            adv = new byte[glyphCount];
            System.arraycopy(bytes, p, adv, 0, glyphCount);
            p += glyphCount;
        }

        int strideBytes = (cellW + 7) >> 3;
        int glyphSizeBytes = strideBytes * cellH;
        int expectedGlyphBytes = glyphCount * glyphSizeBytes;
        if (bytes.length < p + expectedGlyphBytes) {
            return null;
        }
        byte[] glyphBits = new byte[expectedGlyphBytes];
        System.arraycopy(bytes, p, glyphBits, 0, expectedGlyphBytes);
        return new BitmapFont(cellW, cellH, baseline, cps, glyphBits, adv);
    }

    private static String readText(String path) {
        InputStream is = null;
        try {
            is = BitmapFont.class.getResourceAsStream(path);
            if (is == null && path.startsWith("/res/")) {
                is = BitmapFont.class.getResourceAsStream(path.substring(4));
            }
            if (is == null) {
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (true) {
                int r = is.read();
                if (r == -1) {
                    break;
                }
                baos.write(r);
            }

            byte[] bytes = baos.toByteArray();
            try {
                return new String(bytes, "UTF-8");
            } catch (Throwable t) {
                return new String(bytes);
            }
        } catch (Throwable t) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static int countChars(String s) {
        if (s == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\r' || c == '\n') {
                continue;
            }
            n++;
        }
        return n;
    }

    private static void put(char[] keys, short[] values, int mask, char k, int v) {
        int idx = (k * 0x9E37) & mask;
        for (int i = 0; i < keys.length; i++) {
            int p = (idx + i) & mask;
            if (values[p] == 0 || keys[p] == k) {
                keys[p] = k;
                values[p] = (short) v;
                return;
            }
        }
    }

    private int getGlyphIndex(char k) {
        if (codepoints != null) {
            int cp = k;
            int lo = 0;
            int hi = codepoints.length - 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                int v = codepoints[mid];
                if (v == cp) {
                    return mid;
                }
                if (v < cp) {
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            return -1;
        }

        int idx = (k * 0x9E37) & mapMask;
        for (int i = 0; i < mapKeys.length; i++) {
            int p = (idx + i) & mapMask;
            short v = mapValues[p];
            if (v == 0) {
                return -1;
            }
            if (mapKeys[p] == k) {
                return (v & 0xFFFF) - 1;
            }
        }
        return -1;
    }

    public int stringWidth(String s) {
        if (s == null) {
            return 0;
        }
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            w += charWidth(s.charAt(i));
        }
        return w;
    }

    public int charWidth(char c) {
        if (c == '\n' || c == '\r') {
            return 0;
        }
        int glyph = getGlyphIndex(c);
        if (glyphAdvance != null && glyph >= 0) {
            int a = glyphAdvance[glyph] & 0xFF;
            return (a > 0) ? a : cellW;
        }
        return cellW;
    }

    public void drawString(Graphics g, String s, int x, int baselineY, int rgb) {
        if (g == null || s == null) {
            return;
        }

        if (glyphBits != null) {
            drawStringDat(g, s, x, baselineY, rgb);
            return;
        }

        Image page = getColoredPage(rgb);
        if (page == null || cols <= 0) {
            return;
        }

        int y = baselineY - baseline;
        int dx = x;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n' || c == '\r') {
                continue;
            }

            int glyph = getGlyphIndex(c);
            int adv = (glyphAdvance != null && glyph >= 0) ? (glyphAdvance[glyph] & 0xFF) : cellW;
            if (adv <= 0) {
                adv = cellW;
            }

            if (c != ' ' && glyph >= 0) {
                int sx = (glyph % cols) * cellW;
                int sy = (glyph / cols) * cellH;
                g.drawRegion(page, sx, sy, cellW, cellH, 0, dx, y, Graphics.TOP | Graphics.LEFT);
            }
            dx += adv;
        }
    }

    public void drawStringShadow(Graphics g, String s, int x, int baselineY, int rgbMain, int rgbShadow) {
        drawString(g, s, x + 1, baselineY + 1, rgbShadow);
        drawString(g, s, x, baselineY, rgbMain);
    }

    private void drawStringDat(Graphics g, String s, int x, int baselineY, int rgb) {
        int y = baselineY - baseline;
        int dx = x;
        int color = rgb & 0x00FFFFFF;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n' || c == '\r') {
                continue;
            }
            int glyph = getGlyphIndex(c);
            int adv = (glyphAdvance != null && glyph >= 0) ? (glyphAdvance[glyph] & 0xFF) : cellW;
            if (adv <= 0) {
                adv = cellW;
            }
            if (c != ' ' && glyph >= 0) {
                drawGlyphDat(g, glyph, dx, y, color);
            }
            dx += adv;
        }
    }

    private void drawGlyphDat(Graphics g, int glyphIndex, int x, int y, int rgb) {
        int base = glyphIndex * glyphSizeBytes;
        int outPos = 0;
        for (int yy = 0; yy < cellH; yy++) {
            int row = base + (yy * glyphStrideBytes);
            for (int xx = 0; xx < cellW; xx++) {
                int b = glyphBits[row + (xx >> 3)] & 0xFF;
                int on = (b >> (7 - (xx & 7))) & 1;
                glyphScratch[outPos++] = (on != 0) ? (0xFF000000 | rgb) : 0;
            }
        }
        g.drawRGB(glyphScratch, 0, cellW, x, y, cellW, cellH, true);
    }

    private Image getColoredPage(int rgb) {
        rgb &= 0x00FFFFFF;
        for (int i = 0; i < COLOR_CACHE_SIZE; i++) {
            if (cachedPage[i] != null && cachedRgb[i] == rgb) {
                return cachedPage[i];
            }
        }

        int[] out;
        try {
            out = new int[maskPixels.length];
        } catch (Throwable t) {
            return null;
        }

        for (int i = 0; i < maskPixels.length; i++) {
            int a = (maskPixels[i] >>> 24) & 0xFF;
            if (a == 0) {
                out[i] = 0;
            } else {
                out[i] = (a << 24) | rgb;
            }
        }

        Image img;
        try {
            img = Image.createRGBImage(out, maskW, maskH, true);
        } catch (Throwable t) {
            img = null;
        }
        if (img == null) {
            return null;
        }

        int slot = -1;
        for (int i = 0; i < cachedPage.length; i++) {
            if (cachedPage[i] == null) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            slot = (cacheNext++) % cachedPage.length;
        }
        cachedRgb[slot] = rgb;
        cachedPage[slot] = img;
        return img;
    }
}
