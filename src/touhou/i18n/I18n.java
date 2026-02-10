package touhou.i18n;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class I18n {
    public static final int LANG_JA = 0;
    public static final int LANG_EN = 1;
    public static final int LANG_ZHT = 2;

    private static final String[] LANG_CODES = new String[] { "ja", "en", "zht" };

    private static int langId = LANG_JA;

    private I18n() {
    }

    public static void setLanguageId(int id) {
        if (id < 0 || id >= LANG_CODES.length) {
            id = LANG_JA;
        }
        langId = id;
    }

    public static int getLanguageId() {
        return langId;
    }

    public static String getLanguageCode() {
        if (langId < 0 || langId >= LANG_CODES.length) {
            return LANG_CODES[LANG_JA];
        }
        return LANG_CODES[langId];
    }

    public static String path(String subpath) {
        if (subpath == null) {
            subpath = "";
        }
        if (subpath.startsWith("/")) {
            subpath = subpath.substring(1);
        }
        return "/res/i18n/" + getLanguageCode() + "/" + subpath;
    }

    public static byte[] readResourceBytes(String path) {
        if (path == null) {
            return null;
        }

        byte[] packed = I18nPack.readBytes(path);
        if (packed != null) {
            return packed;
        }

        InputStream is = null;
        try {
            is = I18n.class.getResourceAsStream(path);
            if (is == null && path != null && path.startsWith("/res/")) {
                is = I18n.class.getResourceAsStream(path.substring(4));
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

    public static String readUtf8TextResource(String path) {
        try {
            byte[] bytes = readResourceBytes(path);
            if (bytes == null) {
                return null;
            }
            try {
                return new String(bytes, "UTF-8");
            } catch (Throwable t) {
                return new String(bytes);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    public static String[] splitLinesSimple(String text) {
        if (text == null) {
            return new String[0];
        }

        // Ignore trailing line breaks to avoid creating an extra empty entry.
        int effectiveLen = text.length();
        while (effectiveLen > 0) {
            char c = text.charAt(effectiveLen - 1);
            if (c == '\n' || c == '\r') {
                effectiveLen--;
                continue;
            }
            break;
        }
        if (effectiveLen <= 0) {
            return new String[0];
        }

        int lines = 1;
        for (int i = 0; i < effectiveLen; i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }

        String[] out = new String[lines];
        int pos = 0;
        int start = 0;
        for (int i = 0; i <= effectiveLen; i++) {
            boolean end = (i == effectiveLen);
            if (!end && text.charAt(i) != '\n') {
                continue;
            }

            int e = i;
            if (e > start && text.charAt(e - 1) == '\r') {
                e--;
            }
            out[pos++] = text.substring(start, e);
            start = i + 1;
        }

        return out;
    }
}
