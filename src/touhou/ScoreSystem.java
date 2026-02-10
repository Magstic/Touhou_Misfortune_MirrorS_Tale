package touhou;

import javax.microedition.lcdui.Graphics;

import touhou.battle.BattleHudModel;
import touhou.i18n.TextId;
import touhou.i18n.UiText;
import touhou.replay.ReplaySnapshotReader;
import touhou.replay.ReplaySnapshotWriter;
import touhou.ui.UiDraw;

public final class ScoreSystem {
    public interface Listener {
        void onLifeOrBombIncreased();
    }
    public static final int EVT_ADD_SCORE = 1;
    public static final int EVT_GRAZE = 2;
    public static final int EVT_BOMB_USED = 3;
    public static final int EVT_BOMB_TICK = 4;
    public static final int EVT_STAGE_ADVANCED = 5;
    public static final int EVT_SCORE_ITEM_COLLECTED = 6;
    public static final int EVT_SPELL_ID_CHANGED = 7;
    public static final int EVT_SPELL_FINISH = 8;
    public static final int EVT_TICK = 9;
    public static final int EVT_SET_SPELL_BONUS_ARMED = 10;
    public static final int EVT_ADD_BOMB = 11;
    public static final int EVT_ADD_PLAYER = 12;
    public static final int EVT_PLAYER_MISS = 13;
    public static final int EVT_CONTINUE_USED = 14;
    public static final int EVT_CONTINUE_RESET = 15;

    // Mirrors DoJa : yd(int) updates score and stage score, and maintains hi score.
    private int score;
    private int stageScore;
    private int hiScore;

    // Mirrors DoJa : s_scrate (score rate multiplier used by SpellCard Bonus).
    private int scoreRate;

    // Mirrors DoJa : s_crisis (affects point item value, increased by graze).
    private int crisis;

    // Mirrors DoJa : s_graze (display counter capped at 9999).
    private int graze;

    // Mirrors DoJa : player/bomb counters shown on HUD.
    private int player;
    private int bomb;

    // Mirrors DoJa : s_level and m_gamemode for scorelist bookkeeping.
    private int currentLevel;
    private int currentGameMode;

    // Mirrors DoJa : q/s/r for SpellCard Bonus.
    private boolean spellBonusArmed;
    private int spellBonusDisplayTicks;
    private int lastSpellBonusScore;

    private int currentUnit;
    private int currentSpellId;

    private int lastNotifiedSpellId;

    private int extendCnt;
    private int extendRank;

    private GameProgress progress;

    // Disable persistence writes during replay playback (DoJa: guarded by !di).
    private boolean progressWriteEnabled = true;

    private BattleHudModel hud;

    private Listener listener;

    // Cheat support: when disabled, score stays 0 and cannot be increased.
    private boolean scoreEnabled = true;

    public ScoreSystem() {
        resetForNewRun(0, 0, 3, 0, 0, 0);
    }

    public void setScoreEnabled(boolean enabled) {
        scoreEnabled = enabled;
        if (!scoreEnabled) {
            score = 0;
            stageScore = 0;
        }
        syncHud();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void reset(int initialHiScore) {
        score = 0;
        stageScore = 0;
        hiScore = initialHiScore;

        scoreRate = 100;
        crisis = 0;
        graze = 0;

        if (hud != null) {
            player = hud.getPlayer();
            bomb = hud.getBomb();
        } else {
            player = 0;
            bomb = 0;
        }

        currentSpellId = -1;
        spellBonusArmed = false;
        spellBonusDisplayTicks = 0;
        lastSpellBonusScore = 0;

        lastNotifiedSpellId = -1;
        extendCnt = 50;
        extendRank = 0;

        syncHud();
    }

    // Reset score-related fields for a new run.
    public void resetForNewRun(int initialHiScore, int gamemode, int optLife, int chara, int type, int level) {
        score = 0;
        stageScore = 0;
        hiScore = initialHiScore;

        currentGameMode = gamemode;
        currentLevel = level;

        currentUnit = ((chara << 1) + type);
        if (currentUnit < 0) {
            currentUnit = 0;
        }
        if (currentUnit >= GameProgress.UNIT_COUNT) {
            currentUnit = GameProgress.UNIT_COUNT - 1;
        }

        currentSpellId = -1;

        scoreRate = 100;
        crisis = 0;
        graze = 0;

        if (hud != null) {
            player = hud.getPlayer();
            bomb = hud.getBomb();
        } else {
            player = 0;
            bomb = 0;
        }

        // Mirrors DoJa : m_optlife is only applied for gamemode 0; otherwise fixed to 3.
        int opt = (gamemode == 0) ? optLife : 3;
        if (opt > 3) {
            scoreRate -= (opt << 3);
        }
        if (scoreRate < 60) {
            scoreRate = 60;
        }

        spellBonusArmed = false;
        spellBonusDisplayTicks = 0;
        lastSpellBonusScore = 0;

        lastNotifiedSpellId = -1;
        extendCnt = 50;
        extendRank = 0;

        syncHud();
    }

    // Attach HUD model so ScoreSystem can be the single source of truth for score/hi-score and related counters.
    public void attachHud(BattleHudModel hud) {
        this.hud = hud;
        if (hud != null) {
            player = hud.getPlayer();
            bomb = hud.getBomb();
        }
        syncHud();
    }

    // Single entrypoint for score-related events.
    public void post(int event, int a, int b, int c) {
        switch (event) {
            case EVT_ADD_SCORE:
                addScore(a);
                break;
            case EVT_GRAZE:
                if (a > 0) {
                    graze += a;
                    if (graze > 9999) {
                        graze = 9999;
                    }
                }
                onGraze(a);
                break;
            case EVT_BOMB_USED:
                onBombUsed();
                addBomb(-1);
                break;
            case EVT_BOMB_TICK:
                onBombTick(a);
                break;
            case EVT_STAGE_ADVANCED:
                onStageAdvanced(a);
                break;
            case EVT_SCORE_ITEM_COLLECTED:
                onScoreItemCollected();
                break;
            case EVT_SPELL_ID_CHANGED:
                onSpellIdChanged(a);
                break;
            case EVT_SPELL_FINISH:
                onSpellCardFinished(a, b, c);
                break;
            case EVT_TICK:
                tick();
                break;
            case EVT_SET_SPELL_BONUS_ARMED:
                spellBonusArmed = (a != 0);
                break;
            case EVT_ADD_BOMB:
                addBomb(a);
                break;
            case EVT_ADD_PLAYER:
                addPlayer(a);
                break;
            case EVT_PLAYER_MISS:
                onPlayerMiss(b);
                break;
            case EVT_CONTINUE_USED:
                onContinueUsed(b);
                break;
            case EVT_CONTINUE_RESET:
                onContinueReset();
                break;
            default:
                break;
        }
        syncHud();
    }

    public void setProgress(GameProgress progress) {
        this.progress = progress;
    }

    public void setProgressWriteEnabled(boolean enabled) {
        progressWriteEnabled = enabled;
    }

    // Mirrors DoJa : df() (insert current run score into scorelist top 3 and persist).
    public void commitScoreToProgress() {
        if (progress == null) {
            return;
        }
        if (!progressWriteEnabled) {
            return;
        }
        if (!scoreEnabled) {
            return;
        }
        if (currentGameMode == 2 || currentGameMode == 3) {
            return;
        }

        int unit = currentUnit;
        if (unit < 0 || unit >= progress.scoreList.length) {
            return;
        }

        int base;
        if (currentLevel == 4) {
            base = 12;
        } else {
            base = currentLevel * 3;
        }
        if (base < 0 || base + 2 >= progress.scoreList[unit].length) {
            return;
        }

        int s = score;
        int[] scores = progress.scoreList[unit];
        if (s > scores[base + 0]) {
            scores[base + 2] = scores[base + 1];
            scores[base + 1] = scores[base + 0];
            scores[base + 0] = s;
        } else if (s > scores[base + 1]) {
            scores[base + 2] = scores[base + 1];
            scores[base + 1] = s;
        } else if (s > scores[base + 2]) {
            scores[base + 2] = s;
        }

        progress.saveScoreListToSp();
    }

    public int getScore() {
        return score;
    }

    public int getStageScore() {
        return stageScore;
    }

    public int getHiScore() {
        return hiScore;
    }

    public int getScoreRate() {
        return scoreRate;
    }

    public int getCrisis() {
        return crisis;
    }

    public int getGraze() {
        return graze;
    }

    public int getPlayer() {
        return player;
    }

    public int getBomb() {
        return bomb;
    }

    // Replay snapshot I/O.
    public void writeSnapshot(ReplaySnapshotWriter w) {
        if (w == null) {
            return;
        }

        w.writeI32LE(score);
        w.writeI32LE(stageScore);
        w.writeI32LE(hiScore);
        w.writeI32LE(scoreRate);
        w.writeI32LE(crisis);
        w.writeI32LE(graze);
        w.writeI32LE(player);
        w.writeI32LE(bomb);

        w.writeI32LE(currentLevel);
        w.writeI32LE(currentGameMode);
        w.writeI32LE(currentUnit);
        w.writeI32LE(currentSpellId);

        w.writeBool(spellBonusArmed);
        w.writeI32LE(spellBonusDisplayTicks);
        w.writeI32LE(lastSpellBonusScore);

        w.writeI32LE(lastNotifiedSpellId);
        w.writeI32LE(extendCnt);
        w.writeI32LE(extendRank);
    }

    public void readSnapshot(ReplaySnapshotReader r) {
        if (r == null) {
            return;
        }

        score = r.readI32LE();
        stageScore = r.readI32LE();
        hiScore = r.readI32LE();
        scoreRate = r.readI32LE();
        crisis = r.readI32LE();
        graze = r.readI32LE();
        player = r.readI32LE();
        bomb = r.readI32LE();

        currentLevel = r.readI32LE();
        currentGameMode = r.readI32LE();
        currentUnit = r.readI32LE();
        currentSpellId = r.readI32LE();

        spellBonusArmed = r.readBool();
        spellBonusDisplayTicks = r.readI32LE();
        lastSpellBonusScore = r.readI32LE();

        lastNotifiedSpellId = r.readI32LE();
        extendCnt = r.readI32LE();
        extendRank = r.readI32LE();

        syncHud();
    }

    // Mirrors DoJa : host.setQ(true/false) controls whether SpellCard Bonus can be granted.
    public void setSpellBonusArmed(boolean v) {
        post(EVT_SET_SPELL_BONUS_ARMED, v ? 1 : 0, 0, 0);
    }

    public boolean isSpellBonusArmed() {
        return spellBonusArmed;
    }

    private void onSpellCardStarted(int spellId) {
        currentSpellId = spellId;
        spellBonusArmed = true;
        if (progress == null) {
            return;
        }
        if (!progressWriteEnabled) {
            return;
        }
        if (spellId < 0 || spellId >= GameProgress.SPELL_COUNT) {
            return;
        }
        progress.scChareCnt[currentUnit][spellId] += 1;
        progress.saveSpellCountsToSp();
    }

    // Mirrors DoJa : qc() (SpellCard Bonus calculation and bookkeeping).
    private void onSpellCardFinished(int bspellcnt, int level, int stage) {
        lastSpellBonusScore = 0;

        if (!spellBonusArmed) {
            currentSpellId = -1;
            return;
        }
        spellBonusArmed = false;

        int s = 100 + bspellcnt * 30 + (level + 1) * 200 + stage * 100;
        if (scoreRate > 100) {
            scoreRate = 100;
        }
        if (scoreRate < 60) {
            scoreRate = 60;
        }
        s *= scoreRate;

        spellBonusDisplayTicks = 30;
        lastSpellBonusScore = s;
        addScore(s);

        if (progressWriteEnabled && progress != null && currentSpellId >= 0 && currentSpellId < GameProgress.SPELL_COUNT) {
            progress.scBonusCnt[currentUnit][currentSpellId] += 1;
            progress.saveSpellCountsToSp();
        }
        currentSpellId = -1;
    }

    // Mirrors DoJa : rc() (decrement SpellCard Bonus display timer).
    private void tick() {
        if (spellBonusDisplayTicks != 0) {
            spellBonusDisplayTicks -= 1;
        }
    }

    // Mirrors DoJa : sc(Graphics) (render SpellCard Bonus overlay).
    public void renderSpellBonusOverlay(Graphics g) {
        if (spellBonusDisplayTicks != 0) {
            UiDraw.drawString2(g, null, UiText.get(TextId.SCORE_SPELLCARD_BONUS), 97, 60, 1, 0xFF0000, 0x770000);
            UiDraw.drawString2(g, null, "+" + lastSpellBonusScore, 97, 80, 1, 0xFF0000, 0x770000);
        }
    }

    // Mirrors DoJa : jd() bomb use affects s_scrate and cancels SpellCard Bonus.
    private void onBombUsed() {
        spellBonusArmed = false;
        scoreRate -= 1;
        // Mirrors DoJa : s_crisis -= s_crisis >> 3
        crisis -= (crisis >> 3);
    }

    // Mirrors DoJa : while bomb is active, SpellCard Bonus is cancelled.
    private void onBombTick(int bombCnt) {
        if (bombCnt > 15) {
            spellBonusArmed = false;
        }
    }

    // Mirrors DoJa : stage clear increases s_scrate by (m_stage << 2) and clamps to 100.
    private void onStageAdvanced(int currentStage) {
        // DoJa: q() advances stage first (xd(++m_stage)), then applies s_scrate += (m_stage << 2).
        int nextStage = currentStage + 1;
        scoreRate += (nextStage << 2);
        if (scoreRate > 100) {
            scoreRate = 100;
        }

        // DoJa: q() also resets per-stage bookkeeping before entering next stage.
        stageScore = 0;
        crisis = 0;
    }

    private void onGraze(int count) {
        // Mirrors DoJa  dc(): each grazed bullet increments crisis and grants score based on crisis.
        for (int i = 0; i < count; i++) {
            ++crisis;
            if (crisis > 511) {
                crisis = 511;
            }

            int add;
            if (crisis < 127) {
                add = 10;
            } else if (crisis < 255) {
                add = 20;
            } else if (crisis < 511) {
                add = 30;
            } else {
                add = 40;
            }
            addScore(add);
        }
    }

    // Mirrors DoJa : score item value based on player Y.
    public static int calcScoreItemAdd(int playerYFixed) {
        if (playerYFixed < 4194304) {
            return 1000;
        }
        if (playerYFixed < 7929856) {
            return 500;
        }
        return 300;
    }

    // Mirrors DoJa : crisis item value based on current crisis.
    public static int calcCrisisItemAdd(int crisis) {
        if (crisis < 127) {
            return 20;
        }
        if (crisis < 255) {
            return 40;
        }
        if (crisis < 511) {
            return 60;
        }
        return 100;
    }

    // Mirrors DoJa : id() player death effects.
    private void onPlayerMiss(int stage) {
        spellBonusArmed = false;

        scoreRate -= 4;
        if (stage == 0) {
            scoreRate -= 26;
        }

        crisis -= (crisis >> 1);

        player -= 1;
        if (player < 0) {
            player = 0;
        }

        bomb = 3;
        if (bomb < 0) {
            bomb = 0;
        }
        if (bomb > 7) {
            bomb = 7;
        }
    }

    // Mirrors DoJa : continue penalty to score rate.
    private void onContinueUsed(int stage) {
        if (stage <= 1) {
            scoreRate -= 20;
        } else {
            scoreRate -= 5;
        }
    }

    // Mirrors DoJa : continue resets current score to a small non-zero value.
    private void onContinueReset() {
        if (!scoreEnabled) {
            score = 0;
            stageScore = 0;
            return;
        }
        score = (score % 10) + 1;
        stageScore = score;
        if (score > hiScore) {
            hiScore = score;
        }
    }

    private void addScore(int amount) {
        if (!scoreEnabled) {
            return;
        }
        score += amount;
        stageScore += amount;
        if (score > hiScore) {
            hiScore = score;
        }
    }

    private void addScoreNoStage(int amount) {
        if (!scoreEnabled) {
            return;
        }
        score += amount;
        if (score > hiScore) {
            hiScore = score;
        }
    }

    private void onSpellIdChanged(int spellId) {
        if (spellId == lastNotifiedSpellId) {
            return;
        }
        lastNotifiedSpellId = spellId;
        if (spellId >= 0) {
            onSpellCardStarted(spellId);
        } else {
            currentSpellId = -1;
        }
    }

    private void onScoreItemCollected() {
        --extendCnt;
        if (extendCnt != 0) {
            return;
        }

        addPlayer(1);

        ++extendRank;
        addScore(5000 * extendRank);

        switch (extendRank) {
            case 1:
            case 2:
                extendCnt = 75;
                return;
            case 3:
                extendCnt = 100;
                return;
            case 4:
            case 5:
                extendCnt = 150;
                return;
            default:
                extendCnt = 200;
                return;
        }
    }

    private void syncHud() {
        if (hud == null) {
            return;
        }
        hud.setScore(score);
        hud.setHiScore(hiScore);
        hud.setGraze(graze);
        hud.setPlayer(player);
        hud.setBomb(bomb);
    }

    private void addBomb(int amount) {
        int old = bomb;
        int b = bomb + amount;
        if (b > 7) {
            b = 7;
            // DoJa: ae() awards 5000 to total score only when bomb is already full.
            addScoreNoStage(5000);
        }
        if (b < 0) {
            b = 0;
        }
        bomb = b;
        if (b > old && listener != null) {
            listener.onLifeOrBombIncreased();
        }
    }

    private void addPlayer(int amount) {
        int old = player;
        int pl = player + amount;
        if (pl > 7) {
            pl = 7;
            addBomb(1);
        }
        if (pl < 0) {
            pl = 0;
        }
        player = pl;
        if (pl > old && listener != null) {
            listener.onLifeOrBombIncreased();
        }
    }
}
