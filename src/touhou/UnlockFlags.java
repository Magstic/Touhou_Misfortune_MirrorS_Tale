package touhou;

public final class UnlockFlags {
    private static final int OFFSET_UFLAGS = 106;
    private static final int LEN_UFLAGS = 10;

    private static final int IDX_EXTRA_UNLOCKED = 0;
    private static final int IDX_FLAG1 = 1;
    private static final int IDX_ALICE_UNLOCKED = 2;
    private static final int IDX_SPELL_PRACTICE_UNLOCKED = 3;
    private static final int IDX_REPLAY_RMS_WARMED = 4;

    private static final byte[] flags = new byte[LEN_UFLAGS];
    private static boolean loaded;

    private UnlockFlags() {
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        try {
            byte[] b = SpStore.read(OFFSET_UFLAGS, LEN_UFLAGS);
            if (b == null) {
                return;
            }
            int n = b.length;
            if (n > LEN_UFLAGS) {
                n = LEN_UFLAGS;
            }
            for (int i = 0; i < n; i++) {
                flags[i] = b[i];
            }
        } catch (Throwable t) {
            // Ignore storage errors.
        }
    }

    private static void save() {
        byte[] b = new byte[LEN_UFLAGS];
        for (int i = 0; i < LEN_UFLAGS; i++) {
            b[i] = flags[i];
        }
        SpStore.write(OFFSET_UFLAGS, b);
    }

    public static boolean isExtraUnlocked() {
        ensureLoaded();
        return flags[IDX_EXTRA_UNLOCKED] != 0;
    }

    public static void setExtraUnlocked(boolean v) {
        ensureLoaded();
        byte nv = (byte) (v ? 1 : 0);
        if (flags[IDX_EXTRA_UNLOCKED] == nv) {
            return;
        }
        flags[IDX_EXTRA_UNLOCKED] = nv;
        save();
    }

    public static boolean isAliceUnlocked() {
        ensureLoaded();
        if (flags[IDX_ALICE_UNLOCKED] != 0) {
            return true;
        }
        if (qualifiesAliceFromSpellCaptures()) {
            flags[IDX_ALICE_UNLOCKED] = 1;
            save();
            return true;
        }
        return false;
    }

    public static void setAliceUnlocked(boolean v) {
        ensureLoaded();
        byte nv = (byte) (v ? 1 : 0);
        if (flags[IDX_ALICE_UNLOCKED] == nv) {
            return;
        }
        flags[IDX_ALICE_UNLOCKED] = nv;
        save();
    }

    private static boolean qualifiesAliceFromSpellCaptures() {
        // Unlock Alice when unique captured SpellCards >= 41.
        // Count is based on sc_bonuscnt (captured/bonus), summed across all units.
        try {
            GameProgress.INSTANCE.loadFromSp();
            int[][] bonus = GameProgress.INSTANCE.scBonusCnt;
            int unique = 0;
            for (int i = 0; i < GameProgress.SPELL_COUNT; i++) {
                int sum = 0;
                for (int u = 0; u < GameProgress.UNIT_COUNT; u++) {
                    sum += bonus[u][i];
                }
                if (sum != 0) {
                    unique++;
                    if (unique >= 41) {
                        return true;
                    }
                }
            }
            return unique >= 41;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean qualifiesExtraOrNextExtra(int clearedStage, int gamemode, int startLevel, int remainingContinues, int runStartStage, boolean startPracBoss) {
        // Unlock condition:
        // - Story mode only (Game Start)
        // - Clear Stage 6
        // - Started from Stage 1 (no stage select)
        // - Not boss practice start
        // - Normal+ difficulty
        // - No continues used in this run
        if (clearedStage != 5) {
            return false;
        }
        if (gamemode != 0) {
            return false;
        }
        if (runStartStage != 0) {
            return false;
        }
        if (startPracBoss) {
            return false;
        }
        if (startLevel < 1) {
            return false;
        }
        if (remainingContinues != 3) {
            return false;
        }
        return true;
    }

    public static boolean qualifiesNextExtraOnStoryClear(int clearedStage, int gamemode, int startLevel, int remainingContinues, int runStartStage, boolean startPracBoss) {
        return qualifiesExtraOrNextExtra(clearedStage, gamemode, startLevel, remainingContinues, runStartStage, startPracBoss);
    }

    public static void tryUnlockExtraOnStoryClear(int clearedStage, int gamemode, int startLevel, int remainingContinues, int runStartStage, boolean startPracBoss) {
        if (!qualifiesExtraOrNextExtra(clearedStage, gamemode, startLevel, remainingContinues, runStartStage, startPracBoss)) {
            return;
        }
        setExtraUnlocked(true);
    }

    public static void tryUnlockAliceOnExtraClear(int clearedStage, int gamemode) {
        // PLUS: clear EX stage to unlock Alice.
        if (clearedStage != 6) {
            return;
        }
        if (gamemode != 1) {
            return;
        }
        setAliceUnlocked(true);
    }

    public static boolean isSpellPracticeUnlocked() {
        ensureLoaded();
        return flags[IDX_SPELL_PRACTICE_UNLOCKED] != 0;
    }

    // One-time startup warmup flag: indicates Replay-related RMS stores were initialized.
    public static boolean isReplayRmsWarmed() {
        ensureLoaded();
        return flags[IDX_REPLAY_RMS_WARMED] != 0;
    }

    public static void setReplayRmsWarmed(boolean v) {
        ensureLoaded();
        byte nv = (byte) (v ? 1 : 0);
        if (flags[IDX_REPLAY_RMS_WARMED] == nv) {
            return;
        }
        flags[IDX_REPLAY_RMS_WARMED] = nv;
        save();
    }

    public static void tryUnlockSpellPracticeOnAnyEnding() {
        // Original behavior: unlock SpellPractice after seeing any ending.
        ensureLoaded();
        if (flags[IDX_SPELL_PRACTICE_UNLOCKED] != 0) {
            return;
        }
        flags[IDX_SPELL_PRACTICE_UNLOCKED] = 1;
        save();
    }

    public static void tryUnlockFlag1FromStageOp(int gamemode) {
        // Original behavior: do not unlock in spell practice.
        if (gamemode == 3) {
            return;
        }
        ensureLoaded();
        if (flags[IDX_FLAG1] == 0) {
            flags[IDX_FLAG1] = 1;
            save();
        }
    }
}
