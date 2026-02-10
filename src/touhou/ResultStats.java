package touhou;

import touhou.i18n.I18n;

public final class ResultStats {

    public static final ResultStats INSTANCE = new ResultStats();

    public static final int SPELL_COUNT = 116;

    // Other records: playtime accounting start timestamp (ms).
    private long playtimeStartMs;

    // Other records: boot time accounting start timestamp (ms).
    private long boottimeStartMs;

    private boolean spellNamesLoaded;
    private String[] spellNames;

    private ResultStats() {
        playtimeStartMs = 0;
        boottimeStartMs = 0;
        spellNamesLoaded = false;
        spellNames = null;
    }

    // App lifecycle hooks for total boot time.
    public void onAppStart() {
        GameProgress.INSTANCE.loadFromSp();
        boottimeStartMs = System.currentTimeMillis();
    }

    public void onAppPauseOrStop() {
        commitPlaytimeToProgress();
        commitBootTimeToProgress();
    }

    public void onReturnToTitle() {
        // DoJa  commits total boot time on returning to title (u() -> vd()).
        commitBootTimeToProgress();
    }

    public void onResultScreenEnter() {
        ensureSpellNamesLoaded();
        GameProgress.INSTANCE.loadFromSp();
    }

    public int[] getUserData() {
        GameProgress.INSTANCE.loadFromSp();
        return GameProgress.INSTANCE.userData;
    }

    public int[] getScoreList(int unit) {
        GameProgress.INSTANCE.loadFromSp();
        if (unit < 0) {
            unit = 0;
        }
        if (unit > 5) {
            unit = 5;
        }
        return GameProgress.INSTANCE.scoreList[unit];
    }

    public void buildSpellArrays(int[] outBonus, int[] outSeen, int charaSel) {
        GameProgress.INSTANCE.loadFromSp();

        int[][] bonusSrc = GameProgress.INSTANCE.scBonusCnt;
        int[][] seenSrc = GameProgress.INSTANCE.scChareCnt;

        if (charaSel == 0) {
            for (int i = 0; i < SPELL_COUNT; i++) {
                outBonus[i] = 0;
                outSeen[i] = 0;
                for (int u = 0; u < 6; u++) {
                    outBonus[i] += bonusSrc[u][i];
                    outSeen[i] += seenSrc[u][i];
                }
            }
            return;
        }

        int idx = charaSel - 1;
        if (idx < 0) {
            idx = 0;
        }
        if (idx > 5) {
            idx = 5;
        }
        for (int i = 0; i < SPELL_COUNT; i++) {
            outBonus[i] = bonusSrc[idx][i];
            outSeen[i] = seenSrc[idx][i];
        }
    }

    public String getSpellDisplayName(int id, boolean seen) {
        if (!seen) {
            return "\uff1f\uff1f\uff1f\uff1f\uff1f\uff1f\uff1f\uff1f\uff1f\uff1f\uff1f\uff1f";
        }
        ensureSpellNamesLoaded();
        if (spellNames != null && id >= 0 && id < spellNames.length) {
            String s = spellNames[id];
            if (s != null && s.length() > 0) {
                return s;
            }
        }
        return "SpellNo." + (id + 1);
    }

    public String formatTimeSeconds(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        int h = seconds / 3600;
        int m = (seconds / 60) % 60;
        int s = seconds % 60;

        String hh = (h < 10) ? ("0" + h) : String.valueOf(h);
        String mm = (m < 10) ? ("0" + m) : String.valueOf(m);
        String ss = (s < 10) ? ("0" + s) : String.valueOf(s);
        return hh + ":" + mm + ":" + ss;
    }

    public String formatBootTimeSecondsNow(int storedBootSeconds) {
        long v = storedBootSeconds;
        if (v < 0) {
            v = 0;
        }
        if (boottimeStartMs > 0) {
            long now = System.currentTimeMillis();
            long deltaMs = now - boottimeStartMs;
            if (deltaMs > 0) {
                v += (deltaMs / 1000L);
            }
        }
        if (v > Integer.MAX_VALUE) {
            v = Integer.MAX_VALUE;
        }
        return formatTimeSeconds((int) v);
    }

    private void ensureSpellNamesLoaded() {
        if (spellNamesLoaded) {
            return;
        }

        String text = I18n.readUtf8TextResource(I18n.path("spcard.dat"));
        if (text != null) {
            spellNames = I18n.splitLinesSimple(text);
        }
        spellNamesLoaded = true;
    }

    public void onRunStart(boolean replayActive, int gamemode, int level) {
        if (replayActive) {
            return;
        }
        GameProgress.INSTANCE.loadFromSp();
        if (gamemode == 0) {
            GameProgress.INSTANCE.incPlayCountNormalByDifficulty(level);
        } else if (gamemode == 2) {
            GameProgress.INSTANCE.incPlayCountPracticeByDifficulty(level);
        } else if (gamemode == 1) {
            GameProgress.INSTANCE.incPlayCountExtra();
        }
        playtimeStartMs = System.currentTimeMillis();
    }

    public void onSpellPracticeStart(boolean replayActive) {
        if (replayActive) {
            return;
        }
        GameProgress.INSTANCE.loadFromSp();
        playtimeStartMs = System.currentTimeMillis();
    }

    public void onEnterEnding(boolean replayActive) {
        if (replayActive) {
            return;
        }
        commitPlaytimeToProgress();
        GameProgress.INSTANCE.loadFromSp();
        GameProgress.INSTANCE.incClearCount();
    }

    public void onRetry(boolean replayActive) {
        if (replayActive) {
            return;
        }
        GameProgress.INSTANCE.loadFromSp();
        GameProgress.INSTANCE.incRetryCount();
        commitPlaytimeToProgress();
    }

    public void onGameOver(boolean replayActive) {
        if (replayActive) {
            return;
        }
        commitPlaytimeToProgress();
    }

    public void onQuitToTitle(boolean replayActive) {
        if (replayActive) {
            return;
        }
        commitPlaytimeToProgress();
    }

    public void onBattleEndToTitle(boolean replayActive) {
        if (replayActive) {
            return;
        }
        commitPlaytimeToProgress();
    }

    public void onSpellPracticeEndToMenu(boolean replayActive) {
        if (replayActive) {
            return;
        }
        commitPlaytimeToProgress();
    }

    public void onSpellPracticeEndScreenEntered(boolean replayActive) {
        if (replayActive) {
            return;
        }
        commitPlaytimeToProgress();
    }

    public void onFinalResultAfterStaffRoll(boolean replayActive) {
        if (replayActive) {
            return;
        }
        commitPlaytimeToProgress();
    }

    private void commitPlaytimeToProgress() {
        if (playtimeStartMs <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long deltaMs = now - playtimeStartMs;
        if (deltaMs <= 0) {
            playtimeStartMs = now;
            return;
        }
        int deltaSeconds = (int) (deltaMs / 1000L);
        if (deltaSeconds > 0) {
            GameProgress.INSTANCE.loadFromSp();
            GameProgress.INSTANCE.addTotalPlayTimeSeconds(deltaSeconds);
        }
        playtimeStartMs = now;
    }

    private void commitBootTimeToProgress() {
        if (boottimeStartMs <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long deltaMs = now - boottimeStartMs;
        if (deltaMs <= 0) {
            boottimeStartMs = now;
            return;
        }
        int deltaSeconds = (int) (deltaMs / 1000L);
        if (deltaSeconds > 0) {
            GameProgress.INSTANCE.loadFromSp();
            GameProgress.INSTANCE.addTotalBootTimeSeconds(deltaSeconds);
        }
        boottimeStartMs = now;
    }
}
