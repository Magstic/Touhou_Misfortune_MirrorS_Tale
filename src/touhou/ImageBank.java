package touhou;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Image;

public final class ImageBank {
    private static final int[][] STAGEEX_TD = new int[][] {
            { 0x0E7625, 0x109325, 0x104C26, 0x0EB71B, 0x12A627, 0x0E8323, 0x16CE2A, 0x0C6322, 0x17EC28, 0x13D920, 0x10F90C, 0x22C44E, 0x26D356,
                    0x0A4621, 0x083F1D, 0x073C1B },
            { 0x600E76, 0x6F1093, 0x48104C, 0x7A0EB7, 0x7B12A6, 0x650E83, 0x9216CE, 0x530C63, 0xA017EC, 0x9013D9, 0x8F0CF9, 0xA922C4, 0xB826D3,
                    0x430A46, 0x3C083F, 0x39073C },
            { 0x766C0E, 0x937F10, 0x494C10, 0xB78E0E, 0xA68D12, 0x83730E, 0xCEA816, 0x635D0C, 0xECB917, 0xD9A713, 0xF9AA0C, 0xC4BC22, 0xD3CC26,
                    0x42460A, 0x3B3F08, 0x383C07 },
            { 0x0D5428, 0x106B2E, 0x0CFD0B, 0x158237, 0x19F62F, 0x18D033, 0x0B8A1A, 0x2CCD6A, 0x1BB041, 0x2BE664, 0x27B460, 0x1FEA43, 0x0EAA1D,
                    0x1F9C4E, 0x1FCA48, 0x073C1C },
            { 0x540D35, 0x6B1049, 0xFD0BF2, 0x82155A, 0xF619D4, 0xD018AB, 0x8A0B75, 0xCD2C87, 0xB01B82, 0xE62BA4, 0xB42774, 0xEA1FBC, 0xAA0E93,
                    0x9C1F66, 0xCA1F99, 0x3C0724 },
            { 0x39540D, 0x4D6B10, 0xFDFB0B, 0x608215, 0xDFF619, 0xB4D018, 0x7B8A0B, 0x8ECD2C, 0x89B01B, 0xACE62B, 0x7AB427, 0xC6EA1F, 0x9AAA0E,
                    0x6C9C1F, 0xA1CA1F, 0x273C07 },
    };

    private final Image[] images;
    private final String[] pathByIndex;

    private final Image[] alphaImageCacheImg;
    private final int[] alphaImageCacheImgIndex;
    private final int[] alphaImageCacheAlpha;
    private int alphaImageCacheNext;

    private final Image[] alphaKeyImageCacheImg;
    private final int[] alphaKeyImageCacheImgIndex;
    private final int[] alphaKeyImageCacheAlpha;
    private final int[] alphaKeyImageCacheKeyRgb;
    private int alphaKeyImageCacheNext;

    private final Image[] alphaRegionCacheImg;
    private final int[] alphaRegionCacheImgIndex;
    private final int[] alphaRegionCacheX;
    private final int[] alphaRegionCacheY;
    private final int[] alphaRegionCacheW;
    private final int[] alphaRegionCacheH;
    private final int[] alphaRegionCacheAlpha;
    private int alphaRegionCacheNext;

    private long lastRecoverAt;

    // Perf: alpha cache statistics (per instance).
    private int perfAlphaImageReq;
    private int perfAlphaImageHit;
    private int perfAlphaImageCreate;
    private int perfAlphaRegionReq;
    private int perfAlphaRegionHit;
    private int perfAlphaRegionCreate;
    private int perfAlphaKeyReq;
    private int perfAlphaKeyHit;
    private int perfAlphaKeyCreate;

    public ImageBank(int capacity) {
        images = new Image[capacity];
        pathByIndex = new String[capacity];

        alphaImageCacheImg = new Image[24];
        alphaImageCacheImgIndex = new int[24];
        alphaImageCacheAlpha = new int[24];
        alphaImageCacheNext = 0;

        alphaKeyImageCacheImg = new Image[12];
        alphaKeyImageCacheImgIndex = new int[12];
        alphaKeyImageCacheAlpha = new int[12];
        alphaKeyImageCacheKeyRgb = new int[12];
        alphaKeyImageCacheNext = 0;

        alphaRegionCacheImg = new Image[32];
        alphaRegionCacheImgIndex = new int[32];
        alphaRegionCacheX = new int[32];
        alphaRegionCacheY = new int[32];
        alphaRegionCacheW = new int[32];
        alphaRegionCacheH = new int[32];
        alphaRegionCacheAlpha = new int[32];
        alphaRegionCacheNext = 0;

        setPath(0, "/res/sp/000.gif");
        setPath(1, "/res/sp/001.gif");
        setPath(2, "/res/sp/002.gif");
        setPath(3, "/res/sp/003.gif");
        setPath(4, "/res/sp/003.gif");
        setPath(5, "/res/sp/005.gif");
        setPath(11, "/res/sp/011.gif");
        setPath(18, "/res/sp/018.gif");

        setPath(6, "/res/sp/006.gif");
        setPath(7, "/res/sp/007.gif");
        setPath(8, "/res/sp/008.gif");
        setPath(9, "/res/sp/009.gif");
        setPath(10, "/res/sp/010.gif");
        setPath(19, "/res/sp/019.gif");
        setPath(20, "/res/sp/020.gif");
        setPath(67, "/res/sp/067.gif");

        setPath(21, "/res/sp/021.gif");
        setPath(22, "/res/sp/022.gif");
        setPath(23, "/res/sp/023.gif");
        setPath(24, "/res/sp/024.gif");

        setPath(12, "/res/sp/012.gif");
        setPath(13, "/res/sp/013.gif");
        setPath(14, "/res/sp/014.gif");
        setPath(15, "/res/sp/015.gif");
        setPath(16, "/res/sp/016.gif");
        setPath(17, "/res/sp/017.gif");

        setPath(31, "/res/sp/031.gif");
        setPath(42, "/res/sp/042.gif");
        setPath(57, "/res/sp/057.gif");
        setPath(58, "/res/sp/058.gif");
        setPath(61, "/res/sp/061.gif");
        setPath(73, "/res/sp/073.gif");
        setPath(85, "/res/sp/085.gif");

        setPath(86, "/res/sp/086.gif");
        setPath(87, "/res/sp/087.gif");
        setPath(88, "/res/sp/088.gif");
        setPath(89, "/res/sp/089.gif");
        setPath(90, "/res/sp/090.gif");
        setPath(91, "/res/sp/091.gif");
        setPath(92, "/res/sp/092.gif");
        setPath(93, "/res/sp/093.gif");
        setPath(94, "/res/sp/094.gif");

        setPath(25, "/res/sp/025.gif");
        setPath(26, "/res/sp/026.gif");
        setPath(27, "/res/sp/027.gif");
        setPath(32, "/res/sp/032.gif");
        setPath(33, "/res/sp/033.gif");
        setPath(34, "/res/sp/034.gif");
        setPath(35, "/res/sp/035.gif");
        setPath(36, "/res/sp/036.gif");
        setPath(37, "/res/sp/037.gif");
        setPath(38, "/res/sp/038.gif");
        setPath(43, "/res/sp/043.gif");
        setPath(44, "/res/sp/044.gif");
        setPath(45, "/res/sp/045.gif");
        setPath(46, "/res/sp/046.gif");
        setPath(47, "/res/sp/047.gif");
        setPath(48, "/res/sp/048.gif");
        setPath(49, "/res/sp/049.gif");

        setPath(40, "/res/sp/040.gif");
        setPath(41, "/res/sp/041.gif");

        setPath(28, "/res/sp/028.gif");
        setPath(29, "/res/sp/029.gif");
        setPath(30, "/res/sp/030.gif");
        setPath(39, "/res/sp/039.gif");
        setPath(50, "/res/sp/050.gif");
        setPath(51, "/res/sp/051.gif");
        setPath(52, "/res/sp/052.gif");
        setPath(53, "/res/sp/053.gif");
        setPath(54, "/res/sp/054.gif");
        setPath(55, "/res/sp/055.gif");
        setPath(56, "/res/sp/056.gif");
        setPath(59, "/res/sp/059.gif");
        setPath(60, "/res/sp/060.gif");
        setPath(72, "/res/sp/072.gif");
        setPath(74, "/res/sp/074.gif");
        setPath(75, "/res/sp/075.gif");

        setPath(62, "/res/sp/062.gif");
        setPath(63, "/res/sp/063.gif");
        setPath(64, "/res/sp/064.gif");
        setPath(65, "/res/sp/065.gif");
        setPath(66, "/res/sp/066.gif");
        setPath(68, "/res/sp/068.gif");
        setPath(69, "/res/sp/069.gif");
        setPath(70, "/res/sp/070.gif");
        setPath(71, "/res/sp/071.gif");
        setPath(76, "/res/sp/076.gif");
        setPath(77, "/res/sp/077.gif");
        setPath(81, "/res/sp/081.gif");
    }

    public Image get(int imgIndex) {
        if (imgIndex < 0 || imgIndex >= images.length) {
            return null;
        }
        Image cached = images[imgIndex];
        if (cached != null) {
            return cached;
        }

        // StageEX color variants: generate directly from base, no GIF file needed.
        if (isStageExVariantIndex(imgIndex)) {
            Image variant = buildStageExVariant(imgIndex);
            if (variant != null) {
                images[imgIndex] = variant;
                return variant;
            }
            return null;
        }

        String path = pathByIndex[imgIndex];
        if (path == null) {
            path = defaultPath(imgIndex);
        }

        Image img = loadByPath(path);
        if (img == null) {
            recoverForImageLoad();
            img = loadByPath(path);
        }
        if (img == null) {
            String altPath = alternatePath(imgIndex);
            if (altPath != null && !altPath.equals(path)) {
                img = loadByPath(altPath);
            }
        }

        if (img != null) {
            images[imgIndex] = img;
        }
        return img;
    }

    public Image getAlphaImageColorKey(int imgIndex, int alpha, int keyRgb) {
        if (alpha <= 0) {
            return null;
        }
        if (alpha >= 255) {
            alpha = 255;
        }

        perfAlphaKeyReq++;

        Image base = get(imgIndex);
        if (base == null) {
            return null;
        }

        for (int i = 0; i < alphaKeyImageCacheImg.length; i++) {
            Image cached = alphaKeyImageCacheImg[i];
            if (cached == null) {
                continue;
            }
            if (alphaKeyImageCacheImgIndex[i] == imgIndex && alphaKeyImageCacheAlpha[i] == alpha && alphaKeyImageCacheKeyRgb[i] == keyRgb) {
                perfAlphaKeyHit++;
                return cached;
            }
        }

        int w = base.getWidth();
        int h = base.getHeight();
        Image img;
        try {
            int[] rgb = new int[w * h];
            base.getRGB(rgb, 0, w, 0, 0, w, h);
            int key = keyRgb & 0x00FFFFFF;
            for (int i = 0; i < rgb.length; i++) {
                int p = rgb[i];
                int rgb24 = p & 0x00FFFFFF;
                if (rgb24 == key) {
                    rgb[i] = 0;
                    continue;
                }
                int a0 = (p >>> 24) & 0xFF;
                if (a0 == 0) {
                    rgb[i] = 0;
                    continue;
                }
                int r = (rgb24 >>> 16) & 0xFF;
                int g = (rgb24 >>> 8) & 0xFF;
                int b = rgb24 & 0xFF;
                int lum = (r * 30 + g * 59 + b * 11) / 100;
                int inv = 255 - lum;
                int aPix = (inv * a0) / 255;
                int a = (aPix * alpha) / 255;
                rgb[i] = (a << 24);
            }
            img = Image.createRGBImage(rgb, w, h, true);
        } catch (Throwable t) {
            img = null;
        }

        if (img != null) {
            int slot = alphaKeyImageCacheNext;
            alphaKeyImageCacheNext = (alphaKeyImageCacheNext + 1) % alphaKeyImageCacheImg.length;

            alphaKeyImageCacheImg[slot] = img;
            alphaKeyImageCacheImgIndex[slot] = imgIndex;
            alphaKeyImageCacheAlpha[slot] = alpha;
            alphaKeyImageCacheKeyRgb[slot] = keyRgb;

            perfAlphaKeyCreate++;
        }
        return img;
    }

    public Image getAlphaImage(int imgIndex, int alpha) {
        if (alpha <= 0) {
            return null;
        }
        if (alpha >= 255) {
            return get(imgIndex);
        }

        perfAlphaImageReq++;

        Image base = get(imgIndex);
        if (base == null) {
            return null;
        }

        for (int i = 0; i < alphaImageCacheImg.length; i++) {
            Image cached = alphaImageCacheImg[i];
            if (cached == null) {
                continue;
            }
            if (alphaImageCacheImgIndex[i] == imgIndex && alphaImageCacheAlpha[i] == alpha) {
                perfAlphaImageHit++;
                return cached;
            }
        }

        int w = base.getWidth();
        int h = base.getHeight();
        Image img;
        try {
            int[] rgb = new int[w * h];
            base.getRGB(rgb, 0, w, 0, 0, w, h);
            for (int i = 0; i < rgb.length; i++) {
                int p = rgb[i];
                int a0 = (p >>> 24) & 0xFF;
                if (a0 == 0) {
                    rgb[i] = 0;
                    continue;
                }
                int a = (a0 * alpha) / 255;
                rgb[i] = (a << 24) | (p & 0x00FFFFFF);
            }
            img = Image.createRGBImage(rgb, w, h, true);
        } catch (Throwable t) {
            img = null;
        }

        if (img != null) {
            int slot = alphaImageCacheNext;
            alphaImageCacheNext = (alphaImageCacheNext + 1) % alphaImageCacheImg.length;

            alphaImageCacheImg[slot] = img;
            alphaImageCacheImgIndex[slot] = imgIndex;
            alphaImageCacheAlpha[slot] = alpha;

            perfAlphaImageCreate++;
        }
        return img;
    }

    public Image getAlphaRegion(int imgIndex, int srcX, int srcY, int w, int h, int alpha) {
        if (w <= 0 || h <= 0) {
            return null;
        }
        if (alpha <= 0) {
            return null;
        }
        if (alpha > 255) {
            alpha = 255;
        }

        perfAlphaRegionReq++;

        Image base = get(imgIndex);
        if (base == null) {
            return null;
        }

        if (srcX < 0 || srcY < 0) {
            return null;
        }
        int bw = base.getWidth();
        int bh = base.getHeight();
        if (srcX >= bw || srcY >= bh) {
            return null;
        }
        if (srcX + w > bw || srcY + h > bh) {
            return null;
        }

        for (int i = 0; i < alphaRegionCacheImg.length; i++) {
            Image cached = alphaRegionCacheImg[i];
            if (cached == null) {
                continue;
            }
            if (alphaRegionCacheImgIndex[i] == imgIndex && alphaRegionCacheX[i] == srcX && alphaRegionCacheY[i] == srcY && alphaRegionCacheW[i] == w
                    && alphaRegionCacheH[i] == h && alphaRegionCacheAlpha[i] == alpha) {
                perfAlphaRegionHit++;
                return cached;
            }
        }

        Image img;
        try {
            int[] rgb = new int[w * h];
            base.getRGB(rgb, 0, w, srcX, srcY, w, h);
            for (int i = 0; i < rgb.length; i++) {
                int p = rgb[i];
                int a0 = (p >>> 24) & 0xFF;
                if (a0 == 0) {
                    rgb[i] = 0;
                    continue;
                }
                int a = (a0 * alpha) / 255;
                rgb[i] = (a << 24) | (p & 0x00FFFFFF);
            }
            img = Image.createRGBImage(rgb, w, h, true);
        } catch (Throwable t) {
            img = null;
        }

        if (img != null) {
            int slot = alphaRegionCacheNext;
            alphaRegionCacheNext = (alphaRegionCacheNext + 1) % alphaRegionCacheImg.length;

            alphaRegionCacheImg[slot] = img;
            alphaRegionCacheImgIndex[slot] = imgIndex;
            alphaRegionCacheX[slot] = srcX;
            alphaRegionCacheY[slot] = srcY;
            alphaRegionCacheW[slot] = w;
            alphaRegionCacheH[slot] = h;
            alphaRegionCacheAlpha[slot] = alpha;

            perfAlphaRegionCreate++;
        }
        return img;
    }

    // Perf counters are sampled by Debug overlay.
    public int consumePerfAlphaImageReq() {
        int v = perfAlphaImageReq;
        perfAlphaImageReq = 0;
        return v;
    }

    public int consumePerfAlphaImageHit() {
        int v = perfAlphaImageHit;
        perfAlphaImageHit = 0;
        return v;
    }

    public int consumePerfAlphaImageCreate() {
        int v = perfAlphaImageCreate;
        perfAlphaImageCreate = 0;
        return v;
    }

    public int consumePerfAlphaRegionReq() {
        int v = perfAlphaRegionReq;
        perfAlphaRegionReq = 0;
        return v;
    }

    public int consumePerfAlphaRegionHit() {
        int v = perfAlphaRegionHit;
        perfAlphaRegionHit = 0;
        return v;
    }

    public int consumePerfAlphaRegionCreate() {
        int v = perfAlphaRegionCreate;
        perfAlphaRegionCreate = 0;
        return v;
    }

    public int consumePerfAlphaKeyReq() {
        int v = perfAlphaKeyReq;
        perfAlphaKeyReq = 0;
        return v;
    }

    public int consumePerfAlphaKeyHit() {
        int v = perfAlphaKeyHit;
        perfAlphaKeyHit = 0;
        return v;
    }

    public int consumePerfAlphaKeyCreate() {
        int v = perfAlphaKeyCreate;
        perfAlphaKeyCreate = 0;
        return v;
    }

    private void setPath(int imgIndex, String path) {
        if (imgIndex < 0 || imgIndex >= pathByIndex.length) {
            return;
        }
        pathByIndex[imgIndex] = path;
    }

    private static String defaultPath(int imgIndex) {
        return "/res/sp/" + pad3(imgIndex) + ".gif";
    }

    // Clear all alpha caches to free memory. Does NOT call System.gc().
    public void clearAlphaCaches() {
        for (int i = 0; i < alphaImageCacheImg.length; i++) {
            alphaImageCacheImg[i] = null;
        }
        for (int i = 0; i < alphaKeyImageCacheImg.length; i++) {
            alphaKeyImageCacheImg[i] = null;
        }
        for (int i = 0; i < alphaRegionCacheImg.length; i++) {
            alphaRegionCacheImg[i] = null;
        }
        alphaImageCacheNext = 0;
        alphaKeyImageCacheNext = 0;
        alphaRegionCacheNext = 0;
    }

    private void recoverForImageLoad() {
        long now = System.currentTimeMillis();
        if (now - lastRecoverAt < 1000) {
            return;
        }
        lastRecoverAt = now;

        for (int i = 0; i < alphaImageCacheImg.length; i++) {
            alphaImageCacheImg[i] = null;
        }
        for (int i = 0; i < alphaKeyImageCacheImg.length; i++) {
            alphaKeyImageCacheImg[i] = null;
        }
        for (int i = 0; i < alphaRegionCacheImg.length; i++) {
            alphaRegionCacheImg[i] = null;
        }
        alphaImageCacheNext = 0;
        alphaKeyImageCacheNext = 0;
        alphaRegionCacheNext = 0;

        System.gc();
    }

    private static Image loadByPath(String path) {
        Image img = tryLoad(path);
        if (img == null && path.startsWith("/sp/")) {
            img = tryLoad("/res" + path);
        } else if (img == null && path.startsWith("/res/sp/")) {
            img = tryLoad(path.substring(4));
        }
        return img;
    }

    private static String alternatePath(int imgIndex) {
        switch (imgIndex) {
            case 7:
                return "/res/sp/006.gif";
            case 20:
                return "/res/sp/019.gif";
            case 67:
                return "/res/sp/066.gif";
            default:
                return null;
        }
    }

    private static Image tryLoad(String path) {
        try {
            return Image.createImage(path);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean isStageExVariantIndex(int imgIndex) {
        return (imgIndex >= 78 && imgIndex <= 80) || (imgIndex >= 82 && imgIndex <= 84);
    }

    private Image buildStageExVariant(int imgIndex) {
        if (imgIndex >= 78 && imgIndex <= 80) {
            return buildStageExVariantFromBase(77, imgIndex - 78);
        }
        if (imgIndex >= 82 && imgIndex <= 84) {
            return buildStageExVariantFromBase(81, (imgIndex - 82) + 3);
        }
        return null;
    }

    private Image buildStageExVariantFromBase(int baseImgIndex, int tdIndex) {
        if (tdIndex < 0 || tdIndex >= STAGEEX_TD.length) {
            return null;
        }

        String basePath = pathByIndex[baseImgIndex];
        if (basePath == null) {
            basePath = defaultPath(baseImgIndex);
        }

        byte[] base = readResourceBytes(basePath);
        if (base == null && basePath.startsWith("/res/sp/")) {
            base = readResourceBytes(basePath.substring(4));
        }
        if (base == null && basePath.startsWith("/sp/")) {
            base = readResourceBytes("/res" + basePath);
        }
        if (base == null) {
            return null;
        }

        byte[] modified;
        try {
            modified = new byte[base.length];
            System.arraycopy(base, 0, modified, 0, base.length);
        } catch (Throwable t) {
            return null;
        }

        applyGifGlobalPalette(modified, STAGEEX_TD[tdIndex], 0);

        try {
            return Image.createImage(modified, 0, modified.length);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void applyGifGlobalPalette(byte[] gifBytes, int[] palette, int paletteIndex) {
        if (gifBytes == null || palette == null) {
            return;
        }
        int offset = paletteIndex * 3 + 13;
        int need = offset + palette.length * 3;
        if (need > gifBytes.length) {
            return;
        }
        for (int i = 0; i < palette.length; i++) {
            int c = palette[i];
            gifBytes[offset + 0] = (byte) ((c >> 16) & 0xFF);
            gifBytes[offset + 1] = (byte) ((c >> 8) & 0xFF);
            gifBytes[offset + 2] = (byte) (c & 0xFF);
            offset += 3;
        }
    }

    private static byte[] readResourceBytes(String path) {
        if (path == null) {
            return null;
        }

        InputStream is = null;
        try {
            is = ImageBank.class.getResourceAsStream(path);
            if (is == null) {
                return null;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) >= 0) {
                if (n > 0) {
                    bos.write(buf, 0, n);
                }
            }
            return bos.toByteArray();
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

    private static String pad3(int n) {
        if (n < 10) {
            return "00" + n;
        }
        if (n < 100) {
            return "0" + n;
        }
        return String.valueOf(n);
    }
}
