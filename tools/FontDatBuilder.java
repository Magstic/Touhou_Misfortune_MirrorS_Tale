import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * Desktop tool: scan project texts and build res/i18n/<lang>/font/font.dat.
 */
public final class FontDatBuilder {

    public static final class BuildConfig {
        public File ttfFile;
        public List<File> scanDirs;
        public String lang;
        public int cellW = 12;
        public int cellH = 12;
        public Integer baseline = null;
        public int fontSize = 12;
        public boolean scanTextDat = true;
        public File outFile;
    }

    public static final class BuildResult {
        public final int glyphCount;
        public final int missingGlyphCount;
        public final String missingGlyphReport;
        public final int cellW;
        public final int cellH;
        public final int baseline;
        public final File outFile;
        public final long outBytes;

        public BuildResult(int glyphCount, int missingGlyphCount, String missingGlyphReport, int cellW, int cellH, int baseline, File outFile,
                long outBytes) {
            this.glyphCount = glyphCount;
            this.missingGlyphCount = missingGlyphCount;
            this.missingGlyphReport = missingGlyphReport;
            this.cellW = cellW;
            this.cellH = cellH;
            this.baseline = baseline;
            this.outFile = outFile;
            this.outBytes = outBytes;
        }
    }

    public static BuildResult buildToFile(BuildConfig cfg) throws Exception {
        if (cfg == null) {
            throw new IllegalArgumentException("cfg");
        }
        if (cfg.ttfFile == null || !cfg.ttfFile.isFile()) {
            throw new IOException("ttf not found: " + (cfg.ttfFile == null ? "" : cfg.ttfFile.getAbsolutePath()));
        }
        if (cfg.scanDirs == null || cfg.scanDirs.size() == 0) {
            throw new IOException("scanDirs is empty");
        }
        if (cfg.outFile == null) {
            throw new IOException("outFile is null");
        }

        TreeSet<Integer> cps = new TreeSet<Integer>();
        addAsciiBasic(cps);

        List<File> scannedFiles = new ArrayList<File>();

        if (cfg.scanTextDat) {
            scanTextDatInDirs(cps, cfg.scanDirs, cfg.lang, scannedFiles);
        }
        if (cps.size() <= 0) {
            throw new IOException("No glyphs collected");
        }
        if (cps.size() > 65535) {
            throw new IOException("glyphCount too large (u16): " + cps.size());
        }

        Font font = loadTtf(cfg.ttfFile.getAbsolutePath()).deriveFont(Font.PLAIN, (float) cfg.fontSize);

        TreeSet<Integer> filtered = new TreeSet<Integer>();
        List<Integer> missing = new ArrayList<Integer>();
        for (Integer v : cps) {
            int cp = v.intValue();
            if (canDisplay(font, cp)) {
                filtered.add(v);
            } else {
                missing.add(v);
            }
        }
        String missingReport = (missing.size() > 0)
                ? (formatMissingList(missing) + formatMissingLocations(missing, scannedFiles))
                : "";
        cps = filtered;

        FontMetrics fm = measure(font, cfg.cellW, cfg.cellH);
        int baseline = (cfg.baseline != null) ? cfg.baseline.intValue() : fm.getAscent();
        if (cfg.baseline == null) {
            int maxBase = cfg.cellH - 1;
            try {
                maxBase = cfg.cellH - 1 - fm.getDescent();
            } catch (Throwable ignore) {
            }
            if (maxBase < 0) {
                maxBase = 0;
            }
            if (baseline > maxBase) {
                baseline = maxBase;
            }
        }
        if (baseline < 0) {
            baseline = 0;
        }
        if (baseline >= cfg.cellH) {
            baseline = cfg.cellH - 1;
        }

        byte[] dat = buildDat(font, baseline, cfg.cellW, cfg.cellH, cps);
        ensureParentDir(cfg.outFile);
        writeFile(cfg.outFile, dat);
        return new BuildResult(cps.size(), missing.size(), missingReport, cfg.cellW, cfg.cellH, baseline, cfg.outFile, dat.length);
    }

    private static boolean canDisplay(Font font, int cp) {
        if (font == null) {
            return false;
        }
        try {
            return font.canDisplay(cp);
        } catch (Throwable t) {
            return (cp >= 0 && cp <= 0xFFFF) ? font.canDisplay((char) cp) : false;
        }
    }

    private static String formatMissingList(List<Integer> missing) {
        StringBuffer out = new StringBuffer();
        out.append("Missing glyphs:\n");

        int limit = 200;
        for (int i = 0; i < missing.size() && i < limit; i++) {
            int cp = missing.get(i).intValue();
            out.append("- U+");
            out.append(toHex4(cp));
            out.append("('");
            appendPrintableChar(out, cp);
            out.append("')\n");
        }
        if (missing.size() > limit) {
            out.append("- ... (+");
            out.append(missing.size() - limit);
            out.append(" more)\n");
        }
        return out.toString();
    }

    private static void appendPrintableChar(StringBuffer out, int cp) {
        if (out == null) {
            return;
        }
        if (cp < 0 || !Character.isValidCodePoint(cp)) {
            out.append('?');
            return;
        }
        if (cp == 0x20) {
            out.append(' ');
            return;
        }
        if (Character.isISOControl(cp) || cp == 0x7F) {
            out.append('?');
            return;
        }
        try {
            out.append(new String(Character.toChars(cp)));
        } catch (Throwable t) {
            out.append('?');
        }
    }

    private static String formatMissingLocations(List<Integer> missing, List<File> files) {
        if (missing == null || missing.size() == 0) {
            return "";
        }
        if (files == null || files.size() == 0) {
            return "";
        }

        TreeSet<Integer> missingSet = new TreeSet<Integer>();
        for (int i = 0; i < missing.size(); i++) {
            missingSet.add(missing.get(i));
        }

        StringBuffer out = new StringBuffer();
        out.append("Missing glyph locations (first hits):\n");

        int totalHits = 0;
        int maxHits = 40;

        for (int i = 0; i < files.size() && totalHits < maxHits; i++) {
            File f = files.get(i);
            if (f == null || !f.isFile()) {
                continue;
            }
            String text;
            try {
                text = readTextFileUtf8(f);
            } catch (Throwable t) {
                continue;
            }
            if (text == null || text.length() == 0) {
                continue;
            }

            int line = 1;
            int col = 1;
            for (int p = 0; p < text.length() && totalHits < maxHits; p++) {
                char c = text.charAt(p);
                if (c == '\n') {
                    line++;
                    col = 1;
                    continue;
                }
                if (c == '\r') {
                    continue;
                }

                Integer key = Integer.valueOf((int) c);
                if (missingSet.contains(key)) {
                    out.append("- ");
                    out.append(f.getPath());
                    out.append(":");
                    out.append(line);
                    out.append(":");
                    out.append(col);
                    out.append(" U+");
                    out.append(toHex4((int) c));
                    out.append("('");
                    if (c >= 0x20 && c != 0x7F) {
                        out.append(c);
                    } else {
                        out.append('?');
                    }
                    out.append("') ctx=\"");
                    out.append(extractContext(text, p));
                    out.append("\"\n");
                    totalHits++;
                }
                col++;
            }
        }

        if (totalHits == 0) {
            out.append("- (not found in scanned files)\n");
        }
        return out.toString();
    }

    private static String extractContext(String s, int pos) {
        if (s == null || s.length() == 0) {
            return "";
        }
        int start = pos - 12;
        if (start < 0) {
            start = 0;
        }
        int end = pos + 13;
        if (end > s.length()) {
            end = s.length();
        }
        String sub = s.substring(start, end);
        sub = sub.replace('\r', ' ');
        sub = sub.replace('\n', ' ');
        sub = sub.replace('\t', ' ');
        return sub;
    }

    private static String toHex4(int v) {
        String s = Integer.toHexString(v).toUpperCase();
        while (s.length() < 4) {
            s = "0" + s;
        }
        return s;
    }

    private static void addAsciiBasic(TreeSet<Integer> cps) {
        for (int cp = 0x20; cp <= 0x7E; cp++) {
            cps.add(Integer.valueOf(cp));
        }
    }

    private static void scanResTexts(TreeSet<Integer> cps, File resDir, String lang, List<File> scannedFiles) throws IOException {
        if (resDir == null || !resDir.isDirectory()) {
            return;
        }
        List<File> files = new ArrayList<File>();
        collectFiles(files, resDir);

        String resRoot = normalizePath(resDir.getAbsolutePath());
        String expectedLangRoot = resRoot + "/i18n/" + lang + "/";

        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            String name = f.getName().toLowerCase();
            if (!(name.endsWith(".txt") || name.endsWith(".dat"))) {
                continue;
            }

            String p = normalizePath(f.getAbsolutePath());
            if (p.indexOf("/font/") >= 0) {
                continue;
            }
            int i18nIdx = p.indexOf(resRoot + "/i18n/");
            if (i18nIdx >= 0) {
                // Only scan the selected language under res/i18n/<lang>/
                if (!p.startsWith(expectedLangRoot)) {
                    continue;
                }
                // For i18n resources, scan .txt and text-based .dat.
                // (spcard.dat/sprac.dat are plain UTF-8 text)
            } else {
                // Outside i18n, treat .dat as binary and skip.
                if (name.endsWith(".dat")) {
                    continue;
                }
            }

            if (scannedFiles != null) {
                scannedFiles.add(f);
            }
            String s = readTextFileUtf8(f);
            addStringCodepoints(cps, s);
        }
    }

    private static void scanTextDatInDirs(TreeSet<Integer> cps, List<File> scanDirs, String lang, List<File> scannedFiles)
            throws IOException {
        for (int i = 0; i < scanDirs.size(); i++) {
            File dir = scanDirs.get(i);
            if (dir == null) {
                continue;
            }
            if (dir.isDirectory() && "res".equalsIgnoreCase(dir.getName())) {
                scanResTexts(cps, dir, lang, scannedFiles);
                continue;
            }

            // Generic scan.
            List<File> files = new ArrayList<File>();
            if (dir.isDirectory()) {
                collectFiles(files, dir);
            } else {
                files.add(dir);
            }

            for (int k = 0; k < files.size(); k++) {
                File f = files.get(k);
                if (f == null || !f.isFile()) {
                    continue;
                }
                String name = f.getName().toLowerCase();
                if (!(name.endsWith(".txt") || name.endsWith(".dat"))) {
                    continue;
                }
                String p = normalizePath(f.getAbsolutePath());
                if (p.indexOf("/font/") >= 0) {
                    continue;
                }
                if (lang != null && lang.length() > 0) {
                    int i18nIdx = p.indexOf("/i18n/");
                    if (i18nIdx >= 0) {
                        String want = "/i18n/" + lang + "/";
                        if (p.indexOf(want) < 0) {
                            continue;
                        }
                    } else {
                        // Outside i18n, treat .dat as binary and skip.
                        if (name.endsWith(".dat")) {
                            continue;
                        }
                    }
                } else {
                    // No lang filter: still avoid scanning binary .dat outside i18n.
                    int i18nIdx = p.indexOf("/i18n/");
                    if (i18nIdx < 0 && name.endsWith(".dat")) {
                        continue;
                    }
                }

                if (scannedFiles != null) {
                    scannedFiles.add(f);
                }
                String s = readTextFileUtf8(f);
                addStringCodepoints(cps, s);
            }
        }
    }

    private static void addStringCodepoints(TreeSet<Integer> cps, String s) {
        if (s == null) {
            return;
        }
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c == '\r' || c == '\n' || c == '\t') {
                continue;
            }
            cps.add(Integer.valueOf((int) c));
        }
    }

    private static void collectFiles(List<File> out, File dir) {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        Arrays.sort(children);
        for (int i = 0; i < children.length; i++) {
            File f = children[i];
            if (f.isDirectory()) {
                // Skip build artifacts.
                String name = f.getName();
                if (".git".equals(name) || "build".equals(name) || "dist".equals(name)) {
                    continue;
                }
                collectFiles(out, f);
            } else {
                out.add(f);
            }
        }
    }

    private static String readTextFileUtf8(File f) throws IOException {
        byte[] b = readFileBytes(f);
        if (b == null) {
            return "";
        }
        // Assume UTF-8 for project assets.
        return new String(b, Charset.forName("UTF-8"));
    }

    private static byte[] readFileBytes(File f) throws IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            while (true) {
                int r = in.read(buf);
                if (r < 0) {
                    break;
                }
                if (r > 0) {
                    baos.write(buf, 0, r);
                }
            }
            return baos.toByteArray();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static String normalizePath(String s) {
        if (s == null) {
            return "";
        }
        return s.replace('\\', '/');
    }

    private static Font loadTtf(String path) throws IOException, FontFormatException {
        File f = new File(path);
        if (!f.isFile()) {
            throw new IOException("ttf not found: " + f.getAbsolutePath());
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            return Font.createFont(Font.TRUETYPE_FONT, fis);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static FontMetrics measure(Font font, int cellW, int cellH) {
        BufferedImage img = new BufferedImage(Math.max(1, cellW), Math.max(1, cellH), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setFont(font);
            return g.getFontMetrics();
        } finally {
            g.dispose();
        }
    }

    private static boolean isPunctuation(int cp) {
        switch (cp) {
            case '.':
            case ',':
            case ':':
            case ';':
            case '!':
            case '?':
            case '\'':
            case '"':
            case '(': 
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case '<':
            case '>':
            case '/':
            case '\\':
            case '-':
            case '_':
                return true;
            default:
                break;
        }

        // Common fullwidth/CJK punctuation.
        switch (cp) {
            case 0x3001: // 、
            case 0x3002: // 。
            case 0x3008: // 〈
            case 0x3009: // 〉
            case 0x300A: // 《
            case 0x300B: // 》
            case 0x300C: // 「
            case 0x300D: // 」
            case 0x300E: // 『
            case 0x300F: // 』
            case 0x3010: // 【
            case 0x3011: // 】
            case 0x2018: // ‘
            case 0x2019: // ’
            case 0x201C: // “
            case 0x201D: // ”
            case 0x2026: // …
            case 0xFF01: // ！
            case 0xFF08: // （
            case 0xFF09: // ）
            case 0xFF0C: // ，
            case 0xFF0E: // ．
            case 0xFF1A: // ：
            case 0xFF1B: // ；
            case 0xFF1F: // ？
            case 0xFF3B: // ［
            case 0xFF3D: // ］
            case 0xFF5B: // ｛
            case 0xFF5D: // ｝
                return true;
            default:
                return false;
        }
    }

    private static boolean isLowPunctuation(int cp) {
        switch (cp) {
            case '.':
            case ',':
                return true;
            default:
                break;
        }
        switch (cp) {
            case 0x3001: // 、
            case 0x3002: // 。
            case 0xFF0C: // ，
            case 0xFF0E: // ．
                return true;
            default:
                return false;
        }
    }

    //cont
    private static int punctLeftPad(int cp) {
        return 1;
    }

    private static int punctRightPad(int cp) {
        return 2;
    }

    private static byte[] buildDat(Font font, int baseline, int cellW, int cellH, TreeSet<Integer> cps) throws IOException {
        int glyphCount = cps.size();
        int strideBytes = (cellW + 7) >> 3;
        int glyphSizeBytes = strideBytes * cellH;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Header
        out.write('B');
        out.write('F');
        out.write('N');
        out.write('T');
        out.write(2); // version
        out.write(1); // bpp
        out.write(cellW & 0xFF);
        out.write(cellH & 0xFF);
        out.write(baseline & 0xFF);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write((glyphCount >> 8) & 0xFF);
        out.write(glyphCount & 0xFF);

        // Codepoint table (u32 big-endian)
        int[] cpArr = new int[glyphCount];
        int idx = 0;
        for (Integer v : cps) {
            cpArr[idx++] = v.intValue();
        }
        for (int i = 0; i < cpArr.length; i++) {
            int v = cpArr[i];
            out.write((v >> 24) & 0xFF);
            out.write((v >> 16) & 0xFF);
            out.write((v >> 8) & 0xFF);
            out.write(v & 0xFF);
        }

        // Advance table (u8 per glyph). 0 means fallback to cellW at runtime.
        BufferedImage img = new BufferedImage(cellW, cellH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setFont(font);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

            int[] pixels = new int[cellW * cellH];

            // Pass 1: compute advance from rendered bitmap bounds.
            // If a glyph has leading blank columns, trim them by shifting the bitmap left.
            byte[] adv = new byte[glyphCount];
            byte[] xShift = new byte[glyphCount];
            byte[] yShift = new byte[glyphCount];
            for (int gi = 0; gi < cpArr.length; gi++) {
                Arrays.fill(pixels, 0);
                g.setBackground(new java.awt.Color(0, 0, 0, 0));
                g.clearRect(0, 0, cellW, cellH);
                g.setColor(java.awt.Color.WHITE);

                char[] chars = Character.toChars(cpArr[gi]);
                g.drawString(new String(chars), 0, baseline);

                img.getRGB(0, 0, cellW, cellH, pixels, 0, cellW);

                int maxX = -1;
                int minX = cellW;
                int maxY = -1;
                int minY = cellH;
                for (int y = 0; y < cellH; y++) {
                    int row = y * cellW;
                    boolean rowOn = false;
                    for (int x = 0; x < cellW; x++) {
                        int a = (pixels[row + x] >>> 24) & 0xFF;
                        if (a != 0) {
                            rowOn = true;
                            if (x < minX) {
                                minX = x;
                            }
                            break;
                        }
                    }
                    for (int x = cellW - 1; x >= 0; x--) {
                        int a = (pixels[row + x] >>> 24) & 0xFF;
                        if (a != 0) {
                            rowOn = true;
                            if (x > maxX) {
                                maxX = x;
                            }
                            break;
                        }
                    }
                    if (rowOn) {
                        if (y < minY) {
                            minY = y;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                    }
                }

                int w;
                if (maxX < 0) {
                    // Keep space narrower even if empty.
                    w = (cpArr[gi] == 0x20) ? (cellW >> 1) : cellW;
                    xShift[gi] = 0;
                    yShift[gi] = 0;
                } else {
                    // Trim leading blanks when it actually saves width.
                    if (minX < 0 || minX >= cellW) {
                        minX = 0;
                    }
                    if (isPunctuation(cpArr[gi])) {
                        int lp = punctLeftPad(cpArr[gi]);
                        int rp = punctRightPad(cpArr[gi]);
                        int sx = minX - lp;
                        int ww = (maxX - sx) + 1 + rp;
                        if (sx < -127) {
                            sx = -127;
                        } else if (sx > 127) {
                            sx = 127;
                        }
                        xShift[gi] = (byte) sx;
                        w = ww;

                        int dy = 0;
                        if (isLowPunctuation(cpArr[gi]) && maxY >= 0) {
                            int targetMaxY = baseline;
                            if (targetMaxY < 0) {
                                targetMaxY = 0;
                            }
                            if (targetMaxY > cellH - 1) {
                                targetMaxY = cellH - 1;
                            }
                            dy = targetMaxY - maxY;
                            if (dy < 0) {
                                dy = 0;
                            }
                            int maxDown = (cellH - 1) - maxY;
                            if (dy > maxDown) {
                                dy = maxDown;
                            }
                            int maxUp = minY;
                            if (dy < -maxUp) {
                                dy = -maxUp;
                            }
                        }
                        yShift[gi] = (byte) (dy & 0xFF);
                    } else {
                        int tightW = (maxX - minX) + 2;
                        if (tightW < 1) {
                            tightW = 1;
                        }

                        if (minX > 0 && tightW < cellW) {
                            xShift[gi] = (byte) (minX & 0xFF);
                            w = tightW;
                        } else {
                            xShift[gi] = 0;
                            // +1 padding to avoid touching next glyph.
                            w = maxX + 2;
                        }
                        yShift[gi] = 0;
                    }
                    if (w < 1) {
                        w = 1;
                    }
                    if (w > cellW) {
                        w = cellW;
                    }
                }
                adv[gi] = (byte) (w & 0xFF);
            }
            out.write(adv);

            // Pass 2: write glyph bitmaps.
            for (int gi = 0; gi < cpArr.length; gi++) {
                Arrays.fill(pixels, 0);
                g.setBackground(new java.awt.Color(0, 0, 0, 0));
                g.clearRect(0, 0, cellW, cellH);
                g.setColor(java.awt.Color.WHITE);

                char[] chars = Character.toChars(cpArr[gi]);
                g.drawString(new String(chars), 0, baseline);

                img.getRGB(0, 0, cellW, cellH, pixels, 0, cellW);

                byte[] glyph = new byte[glyphSizeBytes];
                int p = 0;
                int shiftX = xShift[gi];
                int shiftY = yShift[gi] & 0xFF;
                for (int y = 0; y < cellH; y++) {
                    int sy = y - shiftY;
                    for (int xb = 0; xb < strideBytes; xb++) {
                        int b = 0;
                        for (int bit = 0; bit < 8; bit++) {
                            int x = (xb << 3) + bit;
                            b <<= 1;
                            if (x < cellW) {
                                int sx = x + shiftX;
                                int a = (sx >= 0 && sx < cellW && sy >= 0 && sy < cellH)
                                        ? ((pixels[(sy * cellW) + sx] >>> 24) & 0xFF)
                                        : 0;
                                if (a != 0) {
                                    b |= 1;
                                }
                            }
                        }
                        glyph[p++] = (byte) (b & 0xFF);
                    }
                }
                out.write(glyph);
            }
        } finally {
            g.dispose();
        }

        return out.toByteArray();
    }

    private static void ensureParentDir(File f) throws IOException {
        File p = f.getParentFile();
        if (p == null) {
            return;
        }
        if (p.isDirectory()) {
            return;
        }
        if (!p.mkdirs()) {
            throw new IOException("Cannot create dir: " + p.getAbsolutePath());
        }
    }

    private static void writeFile(File f, byte[] data) throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(f);
            os.write(data);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }
}
