package touhou.i18n;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class I18nPack {
    private static byte[] pack;

    private static String[] keys;
    private static int[] offsets;
    private static int[] lengths;

    private static int[] slotToEntry;
    private static int slotMask;

    private I18nPack() {
    }

    public static void loadForCurrentLanguage() {
        load(I18n.getLanguageCode());
    }

    public static void load(String langCode) {
        if (langCode == null || langCode.length() == 0) {
            langCode = "ja";
        }
        String path = "/res/i18n/" + langCode + ".dat";
        byte[] bytes = readBytesFromResource(path);
        if (bytes == null || bytes.length < 8) {
            clear();
            return;
        }
        if (!parse(bytes)) {
            clear();
        }
    }

    public static byte[] readBytes(String path) {
        if (path == null) {
            return null;
        }
        byte[] p = pack;
        if (p == null) {
            return null;
        }

        int idx = findEntry(path);
        if (idx < 0) {
            return null;
        }

        int off = offsets[idx];
        int len = lengths[idx];
        if (off < 0 || len < 0 || off + len > p.length) {
            return null;
        }

        byte[] out = new byte[len];
        System.arraycopy(p, off, out, 0, len);
        return out;
    }

    private static int findEntry(String key) {
        int[] slots = slotToEntry;
        if (slots == null) {
            return -1;
        }

        int mask = slotMask;
        int h = key.hashCode();
        int pos = mix(h) & mask;
        for (int i = 0; i < slots.length; i++) {
            int v = slots[pos];
            if (v == 0) {
                return -1;
            }
            int entryIdx = v - 1;
            String k = keys[entryIdx];
            if (k != null && k.equals(key)) {
                return entryIdx;
            }
            pos = (pos + 1) & mask;
        }
        return -1;
    }

    private static int mix(int h) {
        h ^= (h >>> 16);
        h *= 0x7feb352d;
        h ^= (h >>> 15);
        h *= 0x846ca68b;
        h ^= (h >>> 16);
        return h;
    }

    private static void clear() {
        pack = null;
        keys = null;
        offsets = null;
        lengths = null;
        slotToEntry = null;
        slotMask = 0;
    }

    private static boolean parse(byte[] bytes) {
        int p = 0;
        if (bytes[p++] != 'I' || bytes[p++] != '1' || bytes[p++] != '8' || bytes[p++] != 'N') {
            return false;
        }
        int ver = bytes[p++] & 0xFF;
        if (ver != 1) {
            return false;
        }
        // reserved
        p++;
        int count = ((bytes[p] & 0xFF) << 8) | (bytes[p + 1] & 0xFF);
        p += 2;
        if (count <= 0) {
            return false;
        }

        String[] k = new String[count];
        int[] off = new int[count];
        int[] len = new int[count];

        for (int i = 0; i < count; i++) {
            if (p + 2 > bytes.length) {
                return false;
            }
            int keyLen = ((bytes[p] & 0xFF) << 8) | (bytes[p + 1] & 0xFF);
            p += 2;
            if (keyLen <= 0 || p + keyLen > bytes.length) {
                return false;
            }

            String key;
            try {
                key = new String(bytes, p, keyLen, "UTF-8");
            } catch (Throwable t) {
                key = new String(bytes, p, keyLen);
            }
            p += keyLen;

            if (p + 8 > bytes.length) {
                return false;
            }
            int o = ((bytes[p] & 0xFF) << 24) | ((bytes[p + 1] & 0xFF) << 16) | ((bytes[p + 2] & 0xFF) << 8) | (bytes[p + 3] & 0xFF);
            p += 4;
            int l = ((bytes[p] & 0xFF) << 24) | ((bytes[p + 1] & 0xFF) << 16) | ((bytes[p + 2] & 0xFF) << 8) | (bytes[p + 3] & 0xFF);
            p += 4;

            if (o < 0 || l < 0 || o + l > bytes.length) {
                return false;
            }

            k[i] = key;
            off[i] = o;
            len[i] = l;
        }

        int cap = 1;
        while (cap < (count * 2)) {
            cap <<= 1;
        }
        if (cap < 16) {
            cap = 16;
        }
        int[] slots = new int[cap];
        int mask = cap - 1;
        for (int i = 0; i < count; i++) {
            String key = k[i];
            int pos = mix(key.hashCode()) & mask;
            while (true) {
                if (slots[pos] == 0) {
                    slots[pos] = i + 1;
                    break;
                }
                pos = (pos + 1) & mask;
            }
        }

        pack = bytes;
        keys = k;
        offsets = off;
        lengths = len;
        slotToEntry = slots;
        slotMask = mask;
        return true;
    }

    private static byte[] readBytesFromResource(String path) {
        InputStream is = null;
        try {
            is = I18nPack.class.getResourceAsStream(path);
            if (is == null && path.startsWith("/res/")) {
                is = I18nPack.class.getResourceAsStream(path.substring(4));
            }
            if (is == null) {
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[512];
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
}
