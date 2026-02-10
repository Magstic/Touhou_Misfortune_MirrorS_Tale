package touhou;

public final class GameProgress {

    public static final int UNIT_COUNT = 6;
    public static final int SPELL_COUNT = 116;

    private static final int OFFSET_USERDATA = 20;
    private static final int LEN_USERDATA = 32;
    private static final int OFFSET_SCORELIST = 200;
    private static final int LEN_SCORELIST = 360;
    private static final int OFFSET_SC_CHARECNT = 560;
    private static final int LEN_SC_CHARECNT = 1800;
    private static final int OFFSET_SC_BONUSCNT = 2360;
    private static final int LEN_SC_BONUSCNT = 1800;

    private static final int OFFSET_MUSIC_PLAYED = 116;
    private static final int LEN_MUSIC_PLAYED = 20;
    public static final int MUSIC_COUNT = 18;

    public static final GameProgress INSTANCE = new GameProgress();

    // userData[] layout (mirrors DoJa  userdata block).
    public static final int UD_TOTAL_BOOT_TIME_SEC = 0;
    public static final int UD_TOTAL_PLAY_TIME_SEC = 1;
    public static final int UD_TOTAL_CLEAR_COUNT = 2;
    public static final int UD_TOTAL_CONTINUE_COUNT = 3;
    public static final int UD_TOTAL_RETRY_COUNT = 4;

    public static final int UD_PRACTICE_EASY_COUNT = 5;
    public static final int UD_PRACTICE_NORMAL_COUNT = 6;
    public static final int UD_PRACTICE_HARD_COUNT = 7;
    public static final int UD_PRACTICE_LUNATIC_COUNT = 8;

    public static final int UD_NORMAL_EASY_COUNT = 9;
    public static final int UD_NORMAL_NORMAL_COUNT = 10;
    public static final int UD_NORMAL_HARD_COUNT = 11;
    public static final int UD_NORMAL_LUNATIC_COUNT = 12;

    public static final int UD_EXTRA_COUNT = 13;

    public final int[][] scBonusCnt;
    public final int[][] scChareCnt;
    public final int[][] scoreList;
    public final int[] userData;
    public final int[] musicPlayed;

    private boolean loaded;

    private GameProgress() {
        scBonusCnt = new int[UNIT_COUNT][SPELL_COUNT];
        scChareCnt = new int[UNIT_COUNT][SPELL_COUNT];
        scoreList = new int[UNIT_COUNT][15];
        userData = new int[16];
        musicPlayed = new int[MUSIC_COUNT];
    }

    public void loadFromSp() {
        loadUserData();
        loadScoreList();
        loadSpellCounts();
        loadMusicPlayed();

        loaded = true;
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loadFromSp();
    }

    public void saveToSp() {
        ensureLoaded();
        saveUserData();
        saveScoreList();
        saveSpellCounts();
        saveMusicPlayed();
    }

    public void saveMusicPlayedToSp() {
        ensureLoaded();
        saveMusicPlayed();
    }

    // Mark a track as played/unlocked (used by Music Room and auto-unlock on playback).
    public static void unlockMusicPlayed(int trackId) {
        if (trackId < 0 || trackId >= MUSIC_COUNT) {
            return;
        }
        INSTANCE.ensureLoaded();
        if (INSTANCE.musicPlayed[trackId] != 0) {
            return;
        }
        INSTANCE.musicPlayed[trackId] = 1;
        INSTANCE.saveMusicPlayedToSp();
    }

    // Persist only user data (mirrors DoJa  writeByteArray userdata block).
    public void saveUserDataToSp() {
        saveUserData();
    }

    public void addTotalBootTimeSeconds(int deltaSeconds) {
        if (deltaSeconds <= 0) {
            return;
        }
        ensureLoaded();
        long v = (long) userData[UD_TOTAL_BOOT_TIME_SEC] + (long) deltaSeconds;
        if (v > Integer.MAX_VALUE) {
            v = Integer.MAX_VALUE;
        }
        userData[UD_TOTAL_BOOT_TIME_SEC] = (int) v;
        saveUserData();
    }

    public void addTotalPlayTimeSeconds(int deltaSeconds) {
        if (deltaSeconds <= 0) {
            return;
        }
        ensureLoaded();
        long v = (long) userData[UD_TOTAL_PLAY_TIME_SEC] + (long) deltaSeconds;
        if (v > Integer.MAX_VALUE) {
            v = Integer.MAX_VALUE;
        }
        userData[UD_TOTAL_PLAY_TIME_SEC] = (int) v;
        saveUserData();
    }

    public void incClearCount() {
        incU16(UD_TOTAL_CLEAR_COUNT, 1);
    }

    public void incContinueCount() {
        incU16(UD_TOTAL_CONTINUE_COUNT, 1);
    }

    public void incRetryCount() {
        incU16(UD_TOTAL_RETRY_COUNT, 1);
    }

    public void incPlayCountNormalByDifficulty(int level) {
        int idx = UD_NORMAL_EASY_COUNT + level;
        if (level < 0 || level > 3) {
            return;
        }
        incU16(idx, 1);
    }

    public void incPlayCountPracticeByDifficulty(int level) {
        int idx = UD_PRACTICE_EASY_COUNT + level;
        if (level < 0 || level > 3) {
            return;
        }
        incU16(idx, 1);
    }

    public void incPlayCountExtra() {
        incU16(UD_EXTRA_COUNT, 1);
    }

    private void incU16(int idx, int delta) {
        if (delta <= 0) {
            return;
        }
        ensureLoaded();
        if (idx < 0 || idx >= userData.length) {
            return;
        }
        int v = userData[idx];
        if (v < 0) {
            v = 0;
        }
        v += delta;
        if (v > 65535) {
            v = 65535;
        }
        userData[idx] = v;
        saveUserData();
    }

    // Persist only score list (mirrors DoJa  df()/writeByteArray scorelist block).
    public void saveScoreListToSp() {
        saveScoreList();
    }

    // Reload only score list from SP (avoids overwriting other progress arrays).
    public void loadScoreListFromSp() {
        loadScoreList();
    }

    // Persist SpellCard stats immediately (mirrors DoJa  af()).
    public void saveSpellCountsToSp() {
        saveSpellCounts();
    }

    private void loadUserData() {
        byte[] b = SpStore.read(OFFSET_USERDATA, LEN_USERDATA);
        if (b == null || b.length < LEN_USERDATA) {
            return;
        }

        int p = 0;
        userData[0] = readI32LE(b, p);
        p += 4;
        userData[1] = readI32LE(b, p);
        p += 4;
        userData[2] = readU16LE(b, p);
        p += 2;
        userData[3] = readU16LE(b, p);
        p += 2;
        userData[4] = readU16LE(b, p);
        p += 2;
        userData[5] = readU16LE(b, p);
        p += 2;
        userData[6] = readU16LE(b, p);
        p += 2;
        userData[7] = readU16LE(b, p);
        p += 2;
        userData[8] = readU16LE(b, p);
        p += 2;
        userData[9] = readU16LE(b, p);
        p += 2;
        userData[10] = readU16LE(b, p);
        p += 2;
        userData[11] = readU16LE(b, p);
        p += 2;
        userData[12] = readU16LE(b, p);
        p += 2;
        userData[13] = readU16LE(b, p);
    }

    private void saveUserData() {
        byte[] b = new byte[LEN_USERDATA];
        int p = 0;
        writeI32LE(b, p, userData[0]);
        p += 4;
        writeI32LE(b, p, userData[1]);
        p += 4;
        writeU16LE(b, p, userData[2]);
        p += 2;
        writeU16LE(b, p, userData[3]);
        p += 2;
        writeU16LE(b, p, userData[4]);
        p += 2;
        writeU16LE(b, p, userData[5]);
        p += 2;
        writeU16LE(b, p, userData[6]);
        p += 2;
        writeU16LE(b, p, userData[7]);
        p += 2;
        writeU16LE(b, p, userData[8]);
        p += 2;
        writeU16LE(b, p, userData[9]);
        p += 2;
        writeU16LE(b, p, userData[10]);
        p += 2;
        writeU16LE(b, p, userData[11]);
        p += 2;
        writeU16LE(b, p, userData[12]);
        p += 2;
        writeU16LE(b, p, userData[13]);
        SpStore.write(OFFSET_USERDATA, b);
    }

    private void loadScoreList() {
        byte[] b = SpStore.read(OFFSET_SCORELIST, LEN_SCORELIST);
        if (b == null || b.length < LEN_SCORELIST) {
            return;
        }

        for (int u = 0; u < UNIT_COUNT; u++) {
            for (int i = 0; i < 15; i++) {
                int p = 60 * u + (i << 2);
                scoreList[u][i] = readI32LE(b, p);
            }
        }
    }

    private void saveScoreList() {
        byte[] b = new byte[LEN_SCORELIST];
        for (int u = 0; u < UNIT_COUNT; u++) {
            for (int i = 0; i < 15; i++) {
                int p = 60 * u + (i << 2);
                writeI32LE(b, p, scoreList[u][i]);
            }
        }
        SpStore.write(OFFSET_SCORELIST, b);
    }

    private void loadSpellCounts() {
        byte[] ch = SpStore.read(OFFSET_SC_CHARECNT, LEN_SC_CHARECNT);
        if (ch != null && ch.length >= LEN_SC_CHARECNT) {
            for (int u = 0; u < UNIT_COUNT; u++) {
                for (int i = 0; i < SPELL_COUNT; i++) {
                    int p = 300 * u + (i << 1);
                    scChareCnt[u][i] = readU16LE(ch, p);
                }
            }
        }

        byte[] bo = SpStore.read(OFFSET_SC_BONUSCNT, LEN_SC_BONUSCNT);
        if (bo != null && bo.length >= LEN_SC_BONUSCNT) {
            for (int u = 0; u < UNIT_COUNT; u++) {
                for (int i = 0; i < SPELL_COUNT; i++) {
                    int p = 300 * u + (i << 1);
                    scBonusCnt[u][i] = readU16LE(bo, p);
                }
            }
        }
    }

    private void saveSpellCounts() {
        byte[] ch = new byte[LEN_SC_CHARECNT];
        for (int u = 0; u < UNIT_COUNT; u++) {
            for (int i = 0; i < SPELL_COUNT; i++) {
                int p = 300 * u + (i << 1);
                writeU16LE(ch, p, scChareCnt[u][i]);
            }
        }
        SpStore.write(OFFSET_SC_CHARECNT, ch);

        byte[] bo = new byte[LEN_SC_BONUSCNT];
        for (int u = 0; u < UNIT_COUNT; u++) {
            for (int i = 0; i < SPELL_COUNT; i++) {
                int p = 300 * u + (i << 1);
                writeU16LE(bo, p, scBonusCnt[u][i]);
            }
        }
        SpStore.write(OFFSET_SC_BONUSCNT, bo);
    }

    private void loadMusicPlayed() {
        byte[] b = SpStore.read(OFFSET_MUSIC_PLAYED, LEN_MUSIC_PLAYED);
        if (b == null || b.length < LEN_MUSIC_PLAYED) {
            return;
        }
        for (int i = 0; i < MUSIC_COUNT; i++) {
            musicPlayed[i] = b[i] & 0xFF;
        }
    }

    private void saveMusicPlayed() {
        byte[] b = new byte[LEN_MUSIC_PLAYED];
        for (int i = 0; i < MUSIC_COUNT; i++) {
            b[i] = (byte) (musicPlayed[i] & 0xFF);
        }
        SpStore.write(OFFSET_MUSIC_PLAYED, b);
    }

    private static int readU16LE(byte[] b, int p) {
        return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8);
    }

    private static int readI32LE(byte[] b, int p) {
        return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8) | ((b[p + 2] & 0xFF) << 16) | ((b[p + 3] & 0xFF) << 24);
    }

    private static void writeU16LE(byte[] b, int p, int v) {
        b[p] = (byte) (v & 0xFF);
        b[p + 1] = (byte) ((v >> 8) & 0xFF);
    }

    private static void writeI32LE(byte[] b, int p, int v) {
        b[p] = (byte) (v & 0xFF);
        b[p + 1] = (byte) ((v >> 8) & 0xFF);
        b[p + 2] = (byte) ((v >> 16) & 0xFF);
        b[p + 3] = (byte) ((v >> 24) & 0xFF);
    }
}