import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class I18nPackBuilder {

    private I18nPackBuilder() {
    }

    public static void main(String[] args) throws Exception {
        // FontDatBuilder uses AWT for rasterizing glyphs.
        System.setProperty("java.awt.headless", "true");

        String baseDir = ".";
        int argPos = 0;
        if (args != null && args.length > 0) {
            baseDir = args[0];
            argPos = 1;
        }

        String[] langs;
        if (args != null && args.length > argPos) {
            int n = args.length - argPos;
            langs = new String[n];
            for (int i = 0; i < n; i++) {
                langs[i] = args[argPos + i];
            }
        } else {
            langs = new String[] { "en", "ja", "zht" };
        }

        for (int i = 0; i < langs.length; i++) {
            String lang = langs[i];
            if (lang == null || lang.length() == 0) {
                continue;
            }
            buildOne(baseDir, lang);
        }
    }

    private static void buildOne(String baseDir, String lang) throws Exception {
        File root = new File(baseDir);
        File inDir = new File(new File(new File(root, "res"), "i18n"), lang);
        File outFile = new File(new File(new File(root, "res"), "i18n"), lang + ".dat");

        if (!inDir.exists() || !inDir.isDirectory()) {
            System.out.println("I18nPackBuilder: skip missing dir: " + inDir.getPath());
            return;
        }

        byte[] fontDat = buildFontDat(root, lang);

        EntryList list = new EntryList();
        scanDir(list, inDir, inDir);

        // Always regenerate the bitmap font from TTFs and pack it into i18n/<lang>.dat.
        // Runtime key: /res/i18n/<lang>/font/font.dat
        list.addBytes("font/font.dat", fontDat);

        int count = list.size;
        if (count <= 0) {
            System.out.println("I18nPackBuilder: no entries: " + inDir.getPath());
            return;
        }

        byte[][] keyBytes = new byte[count][];
        int[] dataLen = new int[count];
        byte[][] data = new byte[count][];

        int tableBytes = 8;
        for (int i = 0; i < count; i++) {
            String rel = list.relPath[i];
            String key = "/res/i18n/" + lang + "/" + rel;
            byte[] kb;
            try {
                kb = key.getBytes("UTF-8");
            } catch (Throwable t) {
                kb = key.getBytes();
            }
            keyBytes[i] = kb;
            tableBytes += 2 + kb.length + 4 + 4;

            byte[] bytes = (list.bytes[i] != null) ? list.bytes[i] : readAllBytes(list.file[i]);
            if (bytes == null) {
                bytes = new byte[0];
            }
            data[i] = bytes;
            dataLen[i] = bytes.length;
        }

        int dataPos = tableBytes;
        int[] offsets = new int[count];
        for (int i = 0; i < count; i++) {
            offsets[i] = dataPos;
            dataPos += dataLen[i];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(dataPos);
        baos.write('I');
        baos.write('1');
        baos.write('8');
        baos.write('N');
        baos.write(1);
        baos.write(0);
        writeU16(baos, count);

        for (int i = 0; i < count; i++) {
            byte[] kb = keyBytes[i];
            writeU16(baos, kb.length);
            baos.write(kb);
            writeU32(baos, offsets[i]);
            writeU32(baos, dataLen[i]);
        }

        for (int i = 0; i < count; i++) {
            baos.write(data[i]);
        }

        byte[] out = baos.toByteArray();
        ensureParent(outFile);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            fos.write(out);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable ignore) {
                }
            }
        }

        System.out.println("I18nPackBuilder: wrote " + outFile.getPath() + " entries=" + count + " bytes=" + out.length);
    }

    private static void ensureParent(File f) {
        if (f == null) {
            return;
        }
        File p = f.getParentFile();
        if (p != null && !p.exists()) {
            p.mkdirs();
        }
    }

    private static void scanDir(EntryList out, File root, File dir) {
        File[] items = dir.listFiles();
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.length; i++) {
            File f = items[i];
            if (f == null) {
                continue;
            }
            if (f.isDirectory()) {
                if ("font".equalsIgnoreCase(f.getName())) {
                    continue;
                }
                scanDir(out, root, f);
                continue;
            }
            if (!f.isFile()) {
                continue;
            }

            String rel = relPath(root, f);
            if (rel == null || rel.length() == 0) {
                continue;
            }

            out.addFile(f, rel);
        }
    }

    private static byte[] buildFontDat(File root, String lang) throws Exception {
        File ttf = resolveFontTtf(root, lang);
        if (ttf == null || !ttf.isFile()) {
            throw new java.io.IOException("TTF not found: " + (ttf == null ? "" : ttf.getAbsolutePath()));
        }

        File scanDir = new File(new File(new File(root, "res"), "i18n"), lang);
        if (!scanDir.isDirectory()) {
            throw new java.io.IOException("i18n dir not found: " + scanDir.getAbsolutePath());
        }

        File outDir = new File(new File(root, "build"), "i18n-font");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File out = new File(outDir, lang + "-font.dat");

        FontDatBuilder.BuildConfig cfg = new FontDatBuilder.BuildConfig();
        cfg.ttfFile = ttf;
        cfg.lang = lang;
        cfg.cellW = 12;
        cfg.cellH = 12;
        cfg.baseline = Integer.valueOf(10);
        cfg.fontSize = 12;
        cfg.scanTextDat = true;
        cfg.outFile = out;

        List<File> scanDirs = new ArrayList<File>();
        scanDirs.add(scanDir);
        cfg.scanDirs = scanDirs;

        FontDatBuilder.buildToFile(cfg);

        byte[] bytes = readAllBytes(out);
        if (bytes == null || bytes.length == 0) {
            throw new java.io.IOException("Generated font.dat is empty: " + out.getAbsolutePath());
        }
        return bytes;
    }

    private static File resolveFontTtf(File root, String lang) {
        File fontDir = new File(root, "font");
        if ("ja".equals(lang)) {
            return new File(fontDir, "ja.ttf");
        }
        if ("en".equals(lang) || "zht".equals(lang)) {
            return new File(fontDir, "zh_hant.ttf");
        }
        return new File(fontDir, "zh_hant.ttf");
    }

    private static String relPath(File root, File f) {
        try {
            String rp = root.getCanonicalPath();
            String fp = f.getCanonicalPath();
            if (!fp.startsWith(rp)) {
                return null;
            }
            String rel = fp.substring(rp.length());
            while (rel.startsWith("\\") || rel.startsWith("/")) {
                rel = rel.substring(1);
            }
            rel = rel.replace('\\', '/');
            return rel;
        } catch (Throwable t) {
            return null;
        }
    }

    private static byte[] readAllBytes(File f) {
        if (f == null) {
            return null;
        }
        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            while (true) {
                int r = is.read(buf);
                if (r <= 0) {
                    break;
                }
                baos.write(buf, 0, r);
            }
            return baos.toByteArray();
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

    private static void writeU16(ByteArrayOutputStream baos, int v) {
        baos.write((v >>> 8) & 0xFF);
        baos.write(v & 0xFF);
    }

    private static void writeU32(ByteArrayOutputStream baos, int v) {
        baos.write((v >>> 24) & 0xFF);
        baos.write((v >>> 16) & 0xFF);
        baos.write((v >>> 8) & 0xFF);
        baos.write(v & 0xFF);
    }

    private static final class EntryList {
        File[] file;
        byte[][] bytes;
        String[] relPath;
        int size;

        EntryList() {
            file = new File[64];
            bytes = new byte[64][];
            relPath = new String[64];
            size = 0;
        }

        void addFile(File f, String rel) {
            int n = size;
            if (n >= file.length) {
                int nn = n * 2;
                File[] nf = new File[nn];
                byte[][] nb = new byte[nn][];
                String[] nr = new String[nn];
                System.arraycopy(file, 0, nf, 0, n);
                System.arraycopy(bytes, 0, nb, 0, n);
                System.arraycopy(relPath, 0, nr, 0, n);
                file = nf;
                bytes = nb;
                relPath = nr;
            }
            file[n] = f;
            bytes[n] = null;
            relPath[n] = rel;
            size = n + 1;
        }

        void addBytes(String rel, byte[] data) {
            int n = size;
            if (n >= file.length) {
                int nn = n * 2;
                File[] nf = new File[nn];
                byte[][] nb = new byte[nn][];
                String[] nr = new String[nn];
                System.arraycopy(file, 0, nf, 0, n);
                System.arraycopy(bytes, 0, nb, 0, n);
                System.arraycopy(relPath, 0, nr, 0, n);
                file = nf;
                bytes = nb;
                relPath = nr;
            }
            file[n] = null;
            bytes[n] = data;
            relPath[n] = rel;
            size = n + 1;
        }
    }
}
