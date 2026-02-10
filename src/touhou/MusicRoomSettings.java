package touhou;

public final class MusicRoomSettings {
    private static final int OFFSET_MR_SETTINGS = 136;
    private static final int LEN_MR_SETTINGS = 4;

    private static final int MAGIC0 = 0x4D; // 'M'
    private static final int MAGIC1 = 0x52; // 'R'
    private static final int VERSION = 0x01;

    private MusicRoomSettings() {
    }

    public static int loadVolume(int defaultVol) {
        int def = clamp100(defaultVol);
        try {
            byte[] b = SpStore.read(OFFSET_MR_SETTINGS, LEN_MR_SETTINGS);
            if (b == null || b.length < 1) {
                return def;
            }
            if (b.length < LEN_MR_SETTINGS) {
                return def;
            }
            if (((b[1] & 0xFF) != MAGIC0) || ((b[2] & 0xFF) != MAGIC1) || ((b[3] & 0xFF) != VERSION)) {
                return def;
            }
            int v = b[0] & 0xFF;
            if (v > 100) {
                return def;
            }
            return v;
        } catch (Throwable t) {
            return def;
        }
    }

    public static void saveVolume(int volume) {
        try {
            byte[] b = new byte[LEN_MR_SETTINGS];
            b[0] = (byte) (clamp100(volume) & 0xFF);
            b[1] = (byte) MAGIC0;
            b[2] = (byte) MAGIC1;
            b[3] = (byte) VERSION;
            SpStore.write(OFFSET_MR_SETTINGS, b);
        } catch (Throwable t) {
        }
    }

    private static int clamp100(int v) {
        if (v < 0) {
            return 0;
        }
        if (v > 100) {
            return 100;
        }
        return v;
    }
}
