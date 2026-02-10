package touhou;

import javax.microedition.rms.RecordStore;

public final class GameOptions {
    private static final String STORE = "th_opt";
    private static final int SP_OFFSET = 3;

    public static final int SIZE = 16;

    public static final int IDX_SE_MASK = 12;
    public static final int IDX_SE_MASK_HI = 13;

    public static final int IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD = 14;

    public static final int IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT = 15;

    public static final int IDX_LANGUAGE = 11;

    public static final int LANG_JA = 0;
    public static final int LANG_EN = 1;
    public static final int LANG_ZHT = 2;

    // Compat option slot (existing unused index).
    public static final int IDX_DYNAMIC_CUTIN = 9;

    // Compat: alpha feature switches packed into opt[3].
    private static final int IDX_COMPAT_ALPHA_FLAGS = 3;
    private static final int COMPAT_ALPHA_INIT = 0x80;
    private static final int COMPAT_ALPHA_MASK = 0x07;
    private static final int COMPAT_ALPHA_BOMB = 0x01;
    private static final int COMPAT_ALPHA_BOSS_CUTIN = 0x02;
    private static final int COMPAT_ALPHA_PLAYER_SHOT = 0x04;

    // Compat: misc feature switches packed into opt[10].
    private static final int IDX_COMPAT_MISC_FLAGS = 10;
    private static final int COMPAT_MISC_HEAVY_BG_OFF = 0x01;
    private static final int COMPAT_MISC_BLIND_MASK_NO_FADE = 0x02;
    private static final int COMPAT_MISC_HIT_SPARK_OFF = 0x04;

    // Debug: overlay switches packed into opt[3] high bits.
    private static final int DEBUG_INIT = 0x40;
    private static final int DEBUG_MASK = 0x38;
    private static final int DEBUG_FPS = 0x08;
    private static final int DEBUG_PERF = 0x10;
    private static final int DEBUG_RESOURCE = 0x20;

    private GameOptions() {
    }

    // Heuristic: emulator heaps are typically much larger than real devices.
    public static boolean isProbablyEmulator() {
        try {
            Runtime rt = Runtime.getRuntime();
            long total = rt.totalMemory();
            return total >= (8L * 1024L * 1024L);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isDynamicCutInEnabled(int[] opt) {
        return opt != null && opt.length > IDX_DYNAMIC_CUTIN && opt[IDX_DYNAMIC_CUTIN] != 0;
    }

    public static boolean isBombOverlayAlphaEnabled(int[] opt) {
        return (compatAlphaFlags(opt) & COMPAT_ALPHA_BOMB) != 0;
    }

    public static boolean isBossSpellCutInAlphaEnabled(int[] opt) {
        return (compatAlphaFlags(opt) & COMPAT_ALPHA_BOSS_CUTIN) != 0;
    }

    public static boolean isPlayerShotAlphaEnabled(int[] opt) {
        return (compatAlphaFlags(opt) & COMPAT_ALPHA_PLAYER_SHOT) != 0;
    }

    public static boolean isHeavyBgEnabled(int[] opt) {
        if (opt == null || opt.length <= IDX_COMPAT_MISC_FLAGS) {
            return true;
        }
        return (opt[IDX_COMPAT_MISC_FLAGS] & COMPAT_MISC_HEAVY_BG_OFF) == 0;
    }

    public static boolean isBlindMaskFadeEnabled(int[] opt) {
        if (opt == null || opt.length <= IDX_COMPAT_MISC_FLAGS) {
            return true;
        }
        // Legacy behavior: when flag not present (0), fade is enabled.
        return (opt[IDX_COMPAT_MISC_FLAGS] & COMPAT_MISC_BLIND_MASK_NO_FADE) == 0;
    }

    public static boolean isHitSparkEnabled(int[] opt) {
        if (opt == null || opt.length <= IDX_COMPAT_MISC_FLAGS) {
            return true;
        }
        return (opt[IDX_COMPAT_MISC_FLAGS] & COMPAT_MISC_HIT_SPARK_OFF) == 0;
    }

    public static boolean isDebugFpsEnabled(int[] opt) {
        return (debugFlags(opt) & DEBUG_FPS) != 0;
    }

    public static boolean isDebugPerfEnabled(int[] opt) {
        return (debugFlags(opt) & DEBUG_PERF) != 0;
    }

    public static boolean isDebugResourceEnabled(int[] opt) {
        return (debugFlags(opt) & DEBUG_RESOURCE) != 0;
    }

    public static void toggleBombOverlayAlpha(int[] opt) {
        toggleCompatAlphaBit(opt, COMPAT_ALPHA_BOMB);
    }

    public static void toggleBossSpellCutInAlpha(int[] opt) {
        toggleCompatAlphaBit(opt, COMPAT_ALPHA_BOSS_CUTIN);
    }

    public static void togglePlayerShotAlpha(int[] opt) {
        toggleCompatAlphaBit(opt, COMPAT_ALPHA_PLAYER_SHOT);
    }

    public static void toggleHeavyBg(int[] opt) {
        if (opt == null || opt.length <= IDX_COMPAT_MISC_FLAGS) {
            return;
        }
        opt[IDX_COMPAT_MISC_FLAGS] ^= COMPAT_MISC_HEAVY_BG_OFF;
    }

    public static void toggleBlindMaskFade(int[] opt) {
        if (opt == null || opt.length <= IDX_COMPAT_MISC_FLAGS) {
            return;
        }
        opt[IDX_COMPAT_MISC_FLAGS] ^= COMPAT_MISC_BLIND_MASK_NO_FADE;
    }

    public static void toggleHitSpark(int[] opt) {
        if (opt == null || opt.length <= IDX_COMPAT_MISC_FLAGS) {
            return;
        }
        opt[IDX_COMPAT_MISC_FLAGS] ^= COMPAT_MISC_HIT_SPARK_OFF;
    }

    public static void toggleDebugFps(int[] opt) {
        toggleDebugBit(opt, DEBUG_FPS);
    }

    public static void toggleDebugPerf(int[] opt) {
        toggleDebugBit(opt, DEBUG_PERF);
    }

    public static void toggleDebugResource(int[] opt) {
        toggleDebugBit(opt, DEBUG_RESOURCE);
    }

    private static void toggleCompatAlphaBit(int[] opt, int bit) {
        if (opt == null || opt.length <= IDX_COMPAT_ALPHA_FLAGS) {
            return;
        }
        int v = opt[IDX_COMPAT_ALPHA_FLAGS] & 0xFF;
        if ((v & COMPAT_ALPHA_INIT) == 0) {
            if ((v & DEBUG_INIT) == 0) {
                v &= ~DEBUG_MASK;
            }
            v = (v & ~COMPAT_ALPHA_MASK) | COMPAT_ALPHA_INIT | COMPAT_ALPHA_MASK;
        }
        v ^= bit;
        v |= COMPAT_ALPHA_INIT;
        opt[IDX_COMPAT_ALPHA_FLAGS] = v;
    }

    private static void toggleDebugBit(int[] opt, int bit) {
        if (opt == null || opt.length <= IDX_COMPAT_ALPHA_FLAGS) {
            return;
        }
        int v = opt[IDX_COMPAT_ALPHA_FLAGS] & 0xFF;
        v ^= bit;
        v |= DEBUG_INIT;
        opt[IDX_COMPAT_ALPHA_FLAGS] = v;
    }

    private static int compatAlphaFlags(int[] opt) {
        if (opt == null || opt.length <= IDX_COMPAT_ALPHA_FLAGS) {
            return COMPAT_ALPHA_MASK;
        }
        int v = opt[IDX_COMPAT_ALPHA_FLAGS] & 0xFF;
        if ((v & COMPAT_ALPHA_INIT) == 0) {
            // Legacy storage: treat as not initialized -> default ON.
            return COMPAT_ALPHA_MASK;
        }
        return v & COMPAT_ALPHA_MASK;
    }

    private static int debugFlags(int[] opt) {
        if (opt == null || opt.length <= IDX_COMPAT_ALPHA_FLAGS) {
            return 0;
        }
        int v = opt[IDX_COMPAT_ALPHA_FLAGS] & 0xFF;
        if ((v & DEBUG_INIT) == 0) {
            // Legacy storage: treat as not initialized -> default OFF.
            return 0;
        }
        return v & DEBUG_MASK;
    }

    public static int[] defaults() {
        int[] opt = new int[SIZE];
        boolean emu = isProbablyEmulator();
        opt[0] = 3;
        opt[1] = 70;
        opt[2] = 100;
        // Compat alpha feature flags.
        opt[3] = COMPAT_ALPHA_INIT | COMPAT_ALPHA_MASK;
        opt[4] = 0;
        opt[5] = 0;
        opt[6] = 10;
        opt[7] = 11;
        opt[8] = 12;
        // Compat defaults by runtime (emulator vs real device).
        opt[9] = emu ? 1 : 0;
        // HeavyBG: always ON; Mistia Fade: ON for emulator, OFF for real device.
        // Hit Spark: always ON.
        opt[10] = emu ? 0 : COMPAT_MISC_BLIND_MASK_NO_FADE;
        opt[IDX_LANGUAGE] = LANG_JA;

        opt[IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD] = 0;

        opt[IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT] = 0;

        // SE_SHOT (bit 10) defaults to OFF even on emulator to avoid noisy rapid-fire sound.
        int seMask = emu ? 0x3BFF : 0x0000;
        opt[IDX_SE_MASK] = seMask & 0xFF;
        opt[IDX_SE_MASK_HI] = (seMask >> 8) & 0xFF;
        return opt;
    }

    public static int[] load() {
        int[] sp = loadFromSp();
        if (sp != null) {
            return sp;
        }

        int[] legacy = loadLegacy();
        if (legacy != null) {
            saveToSp(legacy);
            return legacy;
        }

        int[] def = defaults();
        saveToSp(def);
        saveLegacy(def);
        return def;
    }

    public static void save(int[] opt) {
        if (opt == null) {
            return;
        }

        saveToSp(opt);
        saveLegacy(opt);
    }

    public static void saveFast(int[] opt) {
        if (opt == null) {
            return;
        }
        saveToSp(opt);
    }

    public static boolean isCheatDisableBossBombShield(int[] opt) {
        return opt != null && opt.length > IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD && opt[IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD] != 0;
    }

    public static boolean isCheatDisableItemCollectLimit(int[] opt) {
        return opt != null && opt.length > IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT && opt[IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT] != 0;
    }

    private static byte[] toBytes(int[] opt) {
        byte[] data = new byte[SIZE];
        int[] v = normalizeOrDefault(opt);
        for (int i = 0; i < SIZE; i++) {
            data[i] = (byte) v[i];
        }
        return data;
    }

    private static int[] normalizeOrDefault(int[] opt) {
        int[] def = defaults();
        if (opt == null) {
            return def;
        }

        int n = (opt.length < SIZE) ? opt.length : SIZE;
        for (int i = 0; i < n; i++) {
            def[i] = opt[i];
        }
        // Migrate legacy storage: compat alpha defaults ON; debug defaults OFF.
        int v = def[IDX_COMPAT_ALPHA_FLAGS] & 0xFF;
        int alphaBits;
        int debugInitBit;
        int debugBits;
        if ((v & COMPAT_ALPHA_INIT) == 0) {
            alphaBits = COMPAT_ALPHA_MASK;
            debugInitBit = 0;
            debugBits = 0;
        } else {
            alphaBits = v & COMPAT_ALPHA_MASK;
            if ((v & DEBUG_INIT) == 0) {
                debugInitBit = 0;
                debugBits = 0;
            } else {
                debugInitBit = DEBUG_INIT;
                debugBits = v & DEBUG_MASK;
            }
        }
        def[IDX_COMPAT_ALPHA_FLAGS] = COMPAT_ALPHA_INIT | alphaBits | debugInitBit | debugBits;
        if (opt.length <= IDX_SE_MASK_HI) {
            def[IDX_SE_MASK_HI] = 0;
        }

        def[IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD] = (def[IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD] == 0) ? 0 : 1;
        def[IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT] = (def[IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT] == 0) ? 0 : 1;
        if (!isValid(def)) {
            return defaults();
        }
        return def;
    }

    private static int[] loadFromSp() {
        try {
            byte[] data = SpStore.read(SP_OFFSET, SIZE);
            if (data == null || data.length <= 0) {
                return null;
            }

            int[] opt = new int[SIZE];
            int[] def = defaults();
            for (int i = 0; i < SIZE; i++) {
                opt[i] = def[i];
            }
            int n = (data.length < SIZE) ? data.length : SIZE;
            for (int i = 0; i < n; i++) {
                opt[i] = data[i] & 0xFF;
            }
            if (!isValid(opt)) {
                return null;
            }
            return opt;
        } catch (Throwable t) {
            return null;
        }
    }

    private static void saveToSp(int[] opt) {
        try {
            byte[] data = toBytes(opt);
            SpStore.write(SP_OFFSET, data);
        } catch (Throwable t) {
        }
    }

    private static int[] loadLegacy() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            if (rs.getNumRecords() <= 0) {
                return null;
            }

            byte[] data = rs.getRecord(1);
            if (data == null || data.length <= 0) {
                return null;
            }

            int[] opt = new int[SIZE];
            int[] def = defaults();
            for (int i = 0; i < SIZE; i++) {
                opt[i] = def[i];
            }
            int n = (data.length < SIZE) ? data.length : SIZE;
            for (int i = 0; i < n; i++) {
                opt[i] = data[i] & 0xFF;
            }
            if (data.length <= IDX_SE_MASK_HI) {
                opt[IDX_SE_MASK_HI] = 0;
            }
            if (!isValid(opt)) {
                return null;
            }
            return opt;
        } catch (Throwable t) {
            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static void saveLegacy(int[] opt) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            byte[] data = toBytes(opt);
            if (rs.getNumRecords() <= 0) {
                rs.addRecord(data, 0, data.length);
            } else {
                rs.setRecord(1, data, 0, data.length);
            }
        } catch (Throwable t) {
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static boolean isValid(int[] opt) {
        if (opt == null || opt.length < SIZE) {
            return false;
        }
        if (opt[0] < 1 || opt[0] > 6) {
            return false;
        }
        if (opt[1] < 0 || opt[1] > 100) {
            return false;
        }
        if (opt[2] < 0 || opt[2] > 100) {
            return false;
        }
        if (opt[6] < 0 || opt[6] > 13) {
            return false;
        }
        if (opt[7] < 0 || opt[7] > 13) {
            return false;
        }
        if (opt[8] < 0 || opt[8] > 13) {
            return false;
        }
        if (opt[IDX_SE_MASK] < 0 || opt[IDX_SE_MASK] > 255) {
            return false;
        }
        if (opt[IDX_SE_MASK_HI] < 0 || opt[IDX_SE_MASK_HI] > 63) {
            return false;
        }
        if (opt[IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD] != 0 && opt[IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD] != 1) {
            return false;
        }
        if (opt[IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT] != 0 && opt[IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT] != 1) {
            return false;
        }
        if (opt[IDX_DYNAMIC_CUTIN] != 0 && opt[IDX_DYNAMIC_CUTIN] != 1) {
            return false;
        }
        if (opt[IDX_LANGUAGE] < 0 || opt[IDX_LANGUAGE] > LANG_ZHT) {
            return false;
        }
        return true;
    }
}
