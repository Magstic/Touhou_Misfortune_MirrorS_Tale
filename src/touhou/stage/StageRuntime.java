package touhou.stage;

import javax.microedition.lcdui.Graphics;

import touhou.SpStore;
import touhou.UnlockFlags;
import touhou.battle.BattleMath;
import touhou.i18n.I18n;
import touhou.ui.UiDraw;

public final class StageRuntime {

    private static void fillPlayRect(Graphics g, int x, int y, int w, int h, int playX, int playY, int playW, int playH) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int right = playX + playW;
        int bottom = playY + playH;
        if (x > right || y > bottom || x < playX - w || y < playY - h) {
            return;
        }

        if (x + w > right) {
            w -= (x + w - right);
        }
        if (y + h > bottom) {
            h -= (y + h - bottom);
        }
        if (x < playX) {
            w -= (playX - x);
            x = playX;
        }
        if (y < playY) {
            h -= (playY - y);
            y = playY;
        }
        if (w <= 0 || h <= 0) {
            return;
        }
        g.fillRect(x, y, w, h);
    }

    public interface StageServices {
        String readUtf8TextResource(String path);
        String[] splitLinesAndWrap(String text, int wrapByteLen);

        void playSound(int id, boolean loop);

        // Toggle rendering of red hit spark rectangles (DoJa effectId 2/21).
        boolean isHitSparkEnabled();

        int[][] getEffectTable();
        void spawnBulletEffect(int effectId, int xFixed, int yFixed);

        int randomNextInt();

        void drawCcIconSafe(Graphics g, int spriteId, int x, int y);
    }

    public interface StageFlow {
        boolean shouldEnterEndingAfterStageClear();
        void beginEndingTransition();
        void enterStageClearResultPanel();
    }

    public interface Host extends StageServices, StageFlow {
        void clearDropItems();
        void clearEnemies();
        void resetBossController();

        int getStage();
        int getGameMode();

        int getBstock();
        void setBstock(int v);

        void setEnemyAabb(int idx, int xFixed, int yFixed, int radiusFixed,
                int leftExtentFixed, int rightExtentFixed, int topExtentFixed, int bottomExtentFixed,
                boolean canTarget);

        int stageNa();

        int getStep();
        void setStep(int v);

        int getTaCnt();
        void setTaCnt(int v);

        void setIFlash(int v);

        void stageHd(int enemyIdx);
        void stageUb(int enemyIdx);
        void stageDb(int enemyIdx);
        void stageEb(int enemyIdx);

        void freeEnemy(int enemyIdx);

        int getOriginX();
        int getOriginY();

        int getPlayX();
        int getPlayY();
        int getPlayW();
        int getPlayH();

        int getBossF();
        void setBossF(int v);

        int getMGCnt();
        void setMGCnt(int v);

        void setBbaria(int v);

        void setLatterFlag(boolean v);
        void setBossMode(boolean v);

        int[] getBossX();
        int[] getBossY();

        int getStartLevel();

        boolean isReplayPlaybackActive();
        boolean isReplayRecordingActive();

        void recordDialoguePageCount(int encounterIndex, int pageCount);
        int getRecordedDialoguePageCount(int encounterIndex);

        void markBossStartAndCaptureCurrentFrame();

        void setResetFlag(int v);
        void setHFlash(int v);

        void setTargetFps(int fps);
    }

    private final Host host;

    private StageDat stageDat;
    private short[][][] stageTable;

    private final int[][] enemyList = new int[32][22];
    private final int[] bossWrk = new int[28];
    private final int[][] effectList = new int[64][9];

    private String[] talkTable;
    private int talkLoadedStage = -1;
    private int talkLoadedChara = -1;

    private int talkCnt = 3;

    // Dialogue pagination state for opcode 246.
    private int dialogueTalkIndex = -1;
    private int dialoguePage = 0;
    private int dialoguePrevPage = 0;
    private int dialogueScrollY = 0;

    private int dialogueEncounterIndex = -1;

    public StageRuntime(Host host) {
        this.host = host;
    }

    public void ensureStageDatLoaded() {
        if (stageTable != null) {
            return;
        }
        stageDat = StageDat.loadFromResources();
        if (stageDat != null) {
            stageTable = stageDat.getTable();
        }
    }

    public int[][] getEnemyList() {
        return enemyList;
    }

    public int[] getBossWrk() {
        return bossWrk;
    }

    // BossController.Host bridge: expose boss helper buffers via StageRuntime.
    public int[] getBossX() {
        return host.getBossX();
    }

    public int[] getBossY() {
        return host.getBossY();
    }

    public int getBstock() {
        return host.getBstock();
    }

    // StageJb core: cleanup boss summons and related helper slots.
    public void stageJbBossPhaseCleanup() {
        for (int i = 0; i != 10; ++i) {
            int si = bossWrk[8 + i];
            if (si < 0 || si >= enemyList.length) {
                continue;
            }
            if (enemyList[si][0] == 0) {
                continue;
            }
            int mt = enemyList[si][11];
            if (mt == 100 || mt == 101 || mt == 102 || mt == 103) {
                continue;
            }
            stageKcEffect(0, enemyList[si][5], enemyList[si][6]);
            enemyList[si][0] = 0;
            host.freeEnemy(si);
            bossWrk[8 + i] = 0;
        }

        for (int i = 0; i != 32; ++i) {
            if (enemyList[i][0] == 0) {
                continue;
            }
            int id = enemyList[i][1];
            if (id == 20 || id == 21) {
                enemyList[i][0] = 0;
                host.freeEnemy(i);
            }
        }

        for (int i = 8; i <= 13; i++) {
            bossWrk[i] = 0;
        }
    }

    // StageJb core: resolve snow-scatter effectId for spell practice boss defeat.
    public int stageJbResolveSpellPracticeBossDefeatEffectId() {
        int stage = host.getStage();
        int effectId = -1;
        if (stage == 0) {
            effectId = 6;
        } else {
            switch (stage) {
                case 1:
                    effectId = 9;
                    break;
                case 2:
                    effectId = 10;
                    break;
                case 3:
                    effectId = (bossWrk[24] == 0) ? 11 : 12;
                    break;
                case 4:
                    effectId = 13;
                    break;
                case 5:
                    effectId = 14;
                    break;
                case 6:
                    effectId = 15;
                    break;
            }
        }
        return effectId;
    }

    // StageJb core: mt==100 early handling and state transition.
    // Return true if caller should return immediately.
    public boolean stageJbMt100PreProcess(final int n, final int ex, final int ey) {
        if (enemyList[n][7] == 0) {
            return true;
        }

        if (enemyList[n][7] == 2) {
            enemyList[n][0] = 0;
            host.freeEnemy(n);
            host.setStep(0);
            host.setMGCnt(0);
            host.setLatterFlag(true);
            return true;
        }

        enemyList[n][3] = 0;
        host.setResetFlag(4);
        stageKcEffect(0, ex, ey);
        return false;
    }

    // StageJb core: boss defeated (mt 101..103, no stock) enter sequence.
    // Does not post score / practice-end; those remain in upper layer.
    public void stageJbBossDefeatedEnter(final int ex, final int ey) {
        host.setTargetFps(5);
        host.setMGCnt(4990);
        host.setBossF(0);

        int stage = host.getStage();
        if (stage == 0) {
            for (int i = 0; i < 3; ++i) {
                if (enemyList[i][0] == 0) {
                    continue;
                }
                stageKcEffect(6 + i, enemyList[i][5], enemyList[i][6]);
                enemyList[i][0] = 0;
                host.freeEnemy(i);
            }
            return;
        }

        int stageEffect = -1;
        switch (stage) {
            case 1:
                stageEffect = 9;
                break;
            case 2:
                stageEffect = 10;
                break;
            case 3:
                stageEffect = (bossWrk[24] == 0) ? 11 : 12;
                break;
            case 4:
                stageEffect = 13;
                break;
            case 5:
                stageEffect = 14;
                break;
            case 6:
                stageEffect = 15;
                break;
        }
        if (stageEffect >= 0) {
            stageKcEffect(stageEffect, ex, ey);
        }
    }

    // StageJb core: spell practice boss defeated (mt==100) enter sequence.
    public void stageJbSpellPracticeBossDefeatedEnter(final int bossEnemyIdx, final int ex, final int ey) {
        host.setTargetFps(5);
        int effectId = stageJbResolveSpellPracticeBossDefeatEffectId();
        if (effectId >= 0) {
            stageKcEffect(effectId, ex, ey);
        }
        stageJbMt100SpellPracticeBossDefeatedCleanup(bossEnemyIdx);
    }

    // StageJb core: apply boss stock transition state changes (no HUD/score/drop/bossController).
    public void stageJbApplyBossStockTransition(final int bossEnemyIdx, final int ex, final int ey, final boolean consumeStock) {
        host.setResetFlag(4);
        stageKcEffect(0, ex, ey);

        if (consumeStock) {
            int stock = host.getBstock();
            if (stock > 0) {
                host.setBstock(stock - 1);
            }
            host.setMGCnt(-1);
            host.setBossF(2);
        }

        host.setBbaria(1);

        int maxHp = enemyList[bossEnemyIdx][9];
        if (maxHp <= 0) {
            maxHp = 1;
        }
        enemyList[bossEnemyIdx][4] = maxHp;
        enemyList[bossEnemyIdx][16] = 0;
    }

    // StageJb core: mt==100, hp bar anim mode 1 transition state.
    public void stageJbMt100EnterHpBarAnimMode1Transition(final int bossEnemyIdx) {
        host.setMGCnt(-1);
        enemyList[bossEnemyIdx][16] = 0;
        enemyList[bossEnemyIdx][7] = 8;
    }

    // StageJb core: mt==100, non-practice boss defeat state enter (enemy remains for script).
    public void stageJbMt100EnterNonPracticeDefeatedState(final int bossEnemyIdx) {
        enemyList[bossEnemyIdx][4] = 1;
        enemyList[bossEnemyIdx][16] = 0;
        host.setMGCnt(-1);
        enemyList[bossEnemyIdx][7] = 2;
    }

    // StageJb core: mt==100, spell practice boss defeated cleanup (no end flow).
    public void stageJbMt100SpellPracticeBossDefeatedCleanup(final int bossEnemyIdx) {
        host.setBossF(0);
        if (enemyList[bossEnemyIdx][0] != 0) {
            enemyList[bossEnemyIdx][0] = 0;
            host.freeEnemy(bossEnemyIdx);
        }
    }

    // StageJb core: mt==101..103 boss on death common state clear.
    public void stageJbBossClearStateOnDeath(final int bossEnemyIdx) {
        enemyList[bossEnemyIdx][3] = 0;
    }

    // StageJb core: free enemy slot if still alive.
    public void stageJbFreeEnemyIfAlive(final int enemyIdx) {
        if (enemyList[enemyIdx][0] != 0) {
            enemyList[enemyIdx][0] = 0;
            host.freeEnemy(enemyIdx);
        }
    }

    // StageJb core: normal enemy death - free and return enemy type for upper-layer side effects.
    public int stageJbNormalEnemyDeathFreeAndGetEnemyType(final int enemyIdx) {
        int enemyType = enemyList[enemyIdx][8];
        enemyList[enemyIdx][0] = 0;
        host.freeEnemy(enemyIdx);
        return enemyType;
    }

    // StageJb core: normal enemy death - spawn stage effects (no score/sound/drop/bullets).
    public void stageJbNormalEnemyDeathSpawnEffects(final int mt, final int ex, final int ey) {
        stageKcEffect(0, ex, ey);

        if (mt >= 100) {
            int stage = host.getStage();
            int stageEffect = -1;
            if (stage == 3) {
                stageEffect = 11;
            } else if (stage == 4) {
                stageEffect = 13;
            } else if (stage == 5) {
                stageEffect = 14;
            } else if (stage == 6) {
                stageEffect = 15;
            }
            if (stageEffect != -1) {
                stageKcEffect(stageEffect, ex, ey);
            } else {
                for (int i = 0; i < 3; i++) {
                    stageKcEffect(3, ex, ey);
                    stageKcEffect(4, ex, ey);
                    stageKcEffect(5, ex, ey);
                }
            }
        }
    }

    public int[][] getEffectList() {
        return effectList;
    }

    public short[][][] getStageTable() {
        return stageTable;
    }

    public int getTalkCnt() {
        return talkCnt;
    }

    public void setTalkCnt(int v) {
        talkCnt = v;
    }

    public void decTalkCntIfPositive() {
        if (talkCnt > 0) {
            talkCnt--;
        }
    }

    public void ensureTalkTableLoaded(int stage, int chara) {
        if (talkTable != null && talkLoadedStage == stage && talkLoadedChara == chara) {
            return;
        }

        talkLoadedStage = stage;
        talkLoadedChara = chara;

        String prefix;
        switch (chara) {
            case 0:
                prefix = "r";
                break;
            case 1:
                prefix = "m";
                break;
            default:
                prefix = "a";
                break;
        }

        String suffix;
        switch (stage) {
            case 0:
                suffix = "01";
                break;
            case 1:
                suffix = "02";
                break;
            case 2:
                suffix = "03";
                break;
            case 3:
                suffix = "04";
                break;
            case 4:
                suffix = "05";
                break;
            case 5:
                suffix = "06";
                break;
            default:
                suffix = "ex";
                break;
        }

        String path = I18n.path("dialog/" + prefix + suffix + ".txt");
        String text = host.readUtf8TextResource(path);
        if (text == null) {
            path = "/res/dialog/" + prefix + suffix + ".txt";
            text = host.readUtf8TextResource(path);
        }
        if (text == null) {
            talkTable = new String[0];
            return;
        }

        String[] lines = splitLinesSimple(text);
        for (int i = 0; i < lines.length; i++) {
            String line = replaceBrTag(lines[i]);
            lines[i] = wrapDialogueLineWordAware(line);
        }
        talkTable = lines;

        // Dialogue state is tied to talkTable indices.
        dialogueTalkIndex = -1;
        dialoguePage = 0;
        dialoguePrevPage = 0;
        dialogueScrollY = 0;
        dialogueEncounterIndex = -1;
    }

    public String[] getTalkTable() {
        return talkTable;
    }

    public int getDialogueTalkIndex() {
        return dialogueTalkIndex;
    }

    public int getDialoguePage() {
        return dialoguePage;
    }

    public int getDialoguePrevPage() {
        return dialoguePrevPage;
    }

    public int getDialogueScrollY() {
        return dialogueScrollY;
    }

    public boolean isDialogueActiveForInput(int stageNa, int step, int gcntPlusOne) {
        if (stageTable == null) {
            return false;
        }

        if (stageNa < 0 || stageNa >= stageTable.length) {
            return false;
        }
        if (step < 0 || step >= stageTable[stageNa].length) {
            return false;
        }
        if (stageTable[stageNa][step].length < 5) {
            return false;
        }
        if (stageTable[stageNa][step][1] != 246) {
            return false;
        }
        if (stageTable[stageNa][step][0] > gcntPlusOne) {
            return false;
        }
        return true;
    }

    public void stageIb() {
        for (int i = 0; i != 32; ++i) {
            for (int j = 0; j != 22; ++j) {
                enemyList[i][j] = 0;
            }
        }
        for (int k = 0; k < 28; ++k) {
            bossWrk[k] = 0;
        }
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 9; j++) {
                effectList[i][j] = 0;
            }
        }

        host.clearDropItems();
        host.clearEnemies();
        host.resetBossController();
    }

    public int stageFb(final int n, final int n2, final int n3, final int n4, final int n5, final int n6, final int n7, final int n8, final int n9) {
        for (int i = 0; i != 32; ++i) {
            if (enemyList[i][0] == 0) {
                enemyList[i][0] = 1;
                enemyList[i][2] = 0;
                enemyList[i][9] = 0;
                enemyList[i][5] = n8;
                enemyList[i][6] = n9;
                enemyList[i][4] = n3;
                enemyList[i][11] = n5;
                enemyList[i][8] = n2;
                enemyList[i][1] = n;
                enemyList[i][21] = n4;
                enemyList[i][7] = 0;
                enemyList[i][3] = 0;
                enemyList[i][16] = 1;
                enemyList[i][10] = 0;
                enemyList[i][13] = n6 + 270;
                enemyList[i][12] = n7;

                int sp = enemyList[i][12] << 3;
                int dir = enemyList[i][13];
                enemyList[i][14] = speedCos(sp, dir);
                enemyList[i][15] = speedSin(sp, dir);

                switch (n) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        enemyList[i][17] = 458752;
                        enemyList[i][18] = 458752;
                        enemyList[i][19] = 458752;
                        enemyList[i][20] = 196608;
                        break;
                    case 8:
                        enemyList[i][17] = 524288;
                        enemyList[i][18] = 524288;
                        enemyList[i][19] = 524288;
                        enemyList[i][20] = 262144;
                        break;
                    case 20:
                    case 21:
                    case 22:
                        enemyList[i][16] = 0;
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                        enemyList[i][17] = 1048576;
                        enemyList[i][18] = 1048576;
                        enemyList[i][19] = 1048576;
                        enemyList[i][20] = 524288;
                        break;
                    case 17:
                    case 19:
                        enemyList[i][17] = 1048576;
                        enemyList[i][18] = 1048576;
                        enemyList[i][19] = 1048576;
                        enemyList[i][20] = 524288;
                        bossWrk[26] = 10;
                        break;
                    case 18:
                        enemyList[i][16] = 0;
                        break;
                }

                if (enemyList[i][11] == 100 || enemyList[i][11] == 101 || enemyList[i][11] == 102 || enemyList[i][11] == 103) {
                    enemyList[i][16] = 0;
                    enemyList[i][14] = 524288;
                    enemyList[i][15] = 524288;
                    enemyList[i][9] = enemyList[i][4];

                    int stage = host.getStage();
                    int bstock;
                    switch (stage) {
                        case 0:
                            bstock = 1;
                            break;
                        case 1:
                        case 2:
                            bstock = 2;
                            break;
                        case 3:
                        case 4:
                            bstock = 3;
                            break;
                        case 5:
                            bstock = 4;
                            break;
                        case 6:
                            bstock = 8;
                            break;
                        default:
                            bstock = 0;
                            break;
                    }
                    if (enemyList[i][11] == 100) {
                        ++bstock;
                    }
                    if (host.getGameMode() == 3) {
                        bstock = 0;
                    }
                    host.setBstock(bstock);
                }

                int w = enemyList[i][17] + enemyList[i][18];
                int h = enemyList[i][19] + enemyList[i][20];
                int r = (w > h) ? (w >> 1) : (h >> 1);
                host.setEnemyAabb(i, enemyList[i][5], enemyList[i][6], r, enemyList[i][17], enemyList[i][18], enemyList[i][19], enemyList[i][20], enemyList[i][16] != 0);
                return i;
            }
        }
        return -1;
    }

    // Per-frame enemy/boss update and collision processing.
    public void stageGb() {
        for (int i = 0; i != 32; ++i) {
            if (enemyList[i][0] != 0) {
                int mt = enemyList[i][11];
                if (mt >= 100 && mt <= 103) {
                    // Boss: update state first so bbaria/phase changes apply immediately before bullet collision.
                    host.stageUb(i);
                    if (enemyList[i][0] == 0) {
                        host.freeEnemy(i);
                        continue;
                    }

                    host.stageHd(i);
                    if (enemyList[i][0] == 0) {
                        host.freeEnemy(i);
                        continue;
                    }
                } else {
                    host.stageHd(i);
                    if (enemyList[i][0] == 0) {
                        host.freeEnemy(i);
                        continue;
                    }

                    host.stageDb(i);
                    host.stageEb(i);

                    if (enemyList[i][2] > 10 && enemyList[i][11] != 50
                            && (enemyList[i][5] < -1966080 || enemyList[i][6] < -1441792 || enemyList[i][5] > 14680064 || enemyList[i][6] > 17301504)) {
                        enemyList[i][0] = 0;
                    }
                }

                if (enemyList[i][0] != 0) {
                    enemyList[i][2] += 1;
                    int w = enemyList[i][17] + enemyList[i][18];
                    int h = enemyList[i][19] + enemyList[i][20];
                    int r = (w > h) ? (w >> 1) : (h >> 1);
                    host.setEnemyAabb(i, enemyList[i][5], enemyList[i][6], r, enemyList[i][17], enemyList[i][18], enemyList[i][19], enemyList[i][20], enemyList[i][16] != 0);
                } else {
                    host.freeEnemy(i);
                }
            } else {
                host.freeEnemy(i);
            }
        }
    }

    // Advance stage script and spawn events.
    public void stageMa() {
        ensureStageDatLoaded();
        if (stageTable == null) {
            return;
        }

        int na = host.stageNa();
        if (na < 0 || na >= stageTable.length) {
            return;
        }

        int step = host.getStep();
        int gcnt = host.getMGCnt();
        int tacnt = host.getTaCnt();

        if (dialogueScrollY > 0) {
            dialogueScrollY -= 4;
            if (dialogueScrollY < 0) {
                dialogueScrollY = 0;
            }
        }

        if (step < 0 || step >= stageTable[na].length) {
            return;
        }

        while (step >= 0 && step < stageTable[na].length && stageTable[na][step][0] <= gcnt) {
            int op = stageTable[na][step][1];
            if (op == 245) {
                step = 0;
                gcnt = 0;
            } else if (op == 246) {
                int talkIndex = -1;
                if (stageTable[na][step].length > 3) {
                    talkIndex = stageTable[na][step][3];
                }
                boolean newTalk = syncDialogueStateForTalkIndex(talkIndex);
                if (newTalk) {
                    dialogueEncounterIndex++;
                }

                int pageCount = dialoguePageCountForTalkIndex(talkIndex);
                int effectivePageCount = pageCount;
                if (host.isReplayPlaybackActive()) {
                    int recorded = host.getRecordedDialoguePageCount(dialogueEncounterIndex);
                    if (recorded > 0) {
                        effectivePageCount = recorded;
                    }
                }

                if (tacnt < 80) {
                    --step;
                    --gcnt;
                    ++tacnt;
                    decTalkCntIfPositive();
                } else {
                    if (effectivePageCount > 1 && dialoguePage + 1 < effectivePageCount) {
                        dialoguePrevPage = dialoguePage;
                        dialoguePage++;

                        if (dialoguePage + 1 <= pageCount) {
                            dialogueScrollY = 32;
                        } else {
                            dialogueScrollY = 0;
                            dialoguePrevPage = dialoguePage;
                        }

                        tacnt = 0;

                        --step;
                        --gcnt;
                    } else {
                        tacnt = 0;
                        if (!host.isReplayPlaybackActive() && host.isReplayRecordingActive()) {
                            host.recordDialoguePageCount(dialogueEncounterIndex, pageCount);
                        }
                        if (stageTable[na][step].length > 4 && stageTable[na][step][4] == 0) {
                            setTalkCnt(3);
                        }
                    }
                }
            } else {
                if (op == 247) {
                    stageIb();
                    host.setBossF(1);
                    host.setBbaria(1);
                    host.setStep(0);
                    host.setMGCnt(0);
                    host.setBossMode(true);

                    int[] bx = host.getBossX();
                    int[] by = host.getBossY();
                    if (bx != null && bx.length > 0) {
                        bx[0] = 99;
                    }
                    if (by != null && by.length > 0) {
                        by[0] = 44;
                    }

                    host.setTaCnt(tacnt);
                    host.markBossStartAndCaptureCurrentFrame();
                    return;
                }
                if (op == 248) {
                    gcnt = -1;
                    host.setBossF(2);
                    host.setHFlash(1);
                } else if (op == 249 || op == 252 || op == 255) {
                    if (op == 255) {
                        if (host.getGameMode() != 2 && !host.isReplayPlaybackActive()) {
                            UnlockFlags.tryUnlockSpellPracticeOnAnyEnding();
                        }
                    }
                    host.setStep(step);
                    host.setMGCnt(gcnt);
                    host.setTaCnt(tacnt);
                    if (host.shouldEnterEndingAfterStageClear()) {
                        host.beginEndingTransition();
                    } else {
                        host.enterStageClearResultPanel();
                    }
                    return;
                } else if (op == 254) {
                    host.setIFlash(10);
                } else if (op == 250) {
                    int id = stageTable[na][step][2];
                    int xFixed = stageTable[na][step][3] << 16;
                    int yFixed = stageTable[na][step][4] << 16;
                    stageKc(id, xFixed, yFixed);
                } else if (op == 251) {
                    host.playSound(stageTable[na][step][2], true);
                } else if (op == 253) {
                    stagePd(stageTable[na][step][2]);
                } else if (op == 243) {
                    if (host.getGameMode() != 3 && !host.isReplayPlaybackActive()) {
                        UnlockFlags.tryUnlockFlag1FromStageOp(host.getGameMode());
                    }
                } else if (op == 244) {
                    step = -1;
                    gcnt = 0;
                    host.setLatterFlag(true);
                } else {
                    if (stageTable[na][step].length >= 10) {
                        stageFb(stageTable[na][step][1], stageTable[na][step][2], stageTable[na][step][3], stageTable[na][step][4], stageTable[na][step][5],
                                stageTable[na][step][6], stageTable[na][step][7], stageTable[na][step][8] << 16, stageTable[na][step][9] << 16);
                    }
                }
            }
            ++step;
        }

        host.setStep(step);
        host.setMGCnt(gcnt);
        host.setTaCnt(tacnt);
    }

    // Force transition to boss stage script.
    public void stageCe() {
        ensureStageDatLoaded();

        if (stageTable == null) {
            return;
        }
        if (host.getBossF() != 0) {
            return;
        }

        host.setMGCnt(0);
        host.setLatterFlag(true);
        host.setStep(0);

        int n = host.getStage() * 5;
        ++n;
        int step = 0;
        while (n >= 0 && n < stageTable.length && step >= 0 && step < stageTable[n].length && stageTable[n][step][1] != 247) {
            ++step;
        }
        host.setStep(step);

        stageIb();
        host.setBossF(1);
        host.setBbaria(1);
        host.setStep(0);
        host.setMGCnt(0);
        host.setBossMode(true);

        int[] bx = host.getBossX();
        int[] by = host.getBossY();
        if (bx != null && bx.length > 0) {
            bx[0] = 99;
        }
        if (by != null && by.length > 0) {
            by[0] = 44;
        }

        host.markBossStartAndCaptureCurrentFrame();
    }

    // Persist stage progress for score/clear tracking.
    public void stagePd(final int n) {
        if (host.isReplayPlaybackActive()) {
            return;
        }

        if (host.getGameMode() == 3) {
            return;
        }
        int level = host.getStartLevel();
        if (level < 0 || level > 3) {
            return;
        }

        byte[] b = SpStore.read(102, 4);
        if (b == null || b.length < 4) {
            b = new byte[4];
        }

        int curr = b[level] & 0xFF;
        if (curr < n) {
            b[level] = (byte) (n & 0xFF);
            SpStore.write(102, b);
        }
    }

    public void stageLc() {
        for (int i = 0; i < 64; i++) {
            if (effectList[i][0] == 0) {
                continue;
            }

            effectList[i][1] += 1;
            effectList[i][4] += effectList[i][6];
            effectList[i][5] += effectList[i][7];

            int id = effectList[i][3];
            if (id == 21) {
                if (effectList[i][1] >= effectList[i][8]) {
                    effectList[i][0] = 0;
                }
                continue;
            }

            int[] row = null;
            int[][] table = host.getEffectTable();
            if (table != null && id >= 0 && id < table.length) {
                row = table[id];
            }
            if (row == null || row.length == 0) {
                effectList[i][0] = 0;
                continue;
            }

            int frame = effectList[i][2];
            int p = frame << 1;
            if (p < 0 || p >= row.length) {
                effectList[i][0] = 0;
                continue;
            }

            int dur = row[p];
            if (dur < effectList[i][1]) {
                effectList[i][1] = 0;
                effectList[i][2] = frame + 1;
                frame = effectList[i][2];
                p = frame << 1;
                if (p < 0 || p >= row.length) {
                    effectList[i][0] = 0;
                    continue;
                }
                dur = row[p];
            }

            if (id >= 6 && id <= 15) {
                stageKcEffect(3, effectList[i][4], effectList[i][5]);
                stageKcEffect(4, effectList[i][4], effectList[i][5]);
                stageKcEffect(5, effectList[i][4], effectList[i][5]);
                if (effectList[i][2] != 0) {
                    stageKcEffect(3, effectList[i][4], effectList[i][5]);
                    stageKcEffect(3, effectList[i][4], effectList[i][5]);
                    stageKcEffect(4, effectList[i][4], effectList[i][5]);
                    stageKcEffect(4, effectList[i][4], effectList[i][5]);
                    stageKcEffect(5, effectList[i][4], effectList[i][5]);
                    stageKcEffect(5, effectList[i][4], effectList[i][5]);
                    host.setResetFlag(2);
                }
                if (effectList[i][2] == 2) {
                    host.setHFlash(1);
                }
            }

            if (dur == 255) {
                if (effectList[i][8] > 0) {
                    effectList[i][2] = 0;
                    effectList[i][1] = 0;
                    effectList[i][8] -= 1;
                } else {
                    effectList[i][0] = 0;
                }
            }
        }
    }

    public void stageMc(final Graphics g) {
        int ox = host.getOriginX();
        int oy = host.getOriginY();
        int playX = host.getPlayX();
        int playY = host.getPlayY();
        int playW = host.getPlayW();
        int playH = host.getPlayH();

        for (int i = 0; i < 64; i++) {
            if (effectList[i][0] == 0) {
                continue;
            }

            int id = effectList[i][3];
            int x = ox + (effectList[i][4] >> 16);
            int y = oy + (effectList[i][5] >> 16);

            if (id == 21) {
                if (host.isHitSparkEnabled()) {
                    int n = effectList[i][8] - effectList[i][1];
                    int c = (255 - (n << 4)) & 0xFF;
                    int size = 1 + (n >> 2);
                    g.setColor(0xFF0000 | (c << 8) | c);
                    int rx = (effectList[i][4] >> 16);
                    int ry = (effectList[i][5] >> 16);
                    fillPlayRect(g, rx, ry, size, size, playX, playY, playW, playH);
                }
                continue;
            }

            int[] row = null;
            int[][] table = host.getEffectTable();
            if (table != null && id >= 0 && id < table.length) {
                row = table[id];
            }
            if (row == null || row.length == 0) {
                continue;
            }

            int frame = effectList[i][2];
            int spriteIdx = (frame << 1) + 1;
            if (spriteIdx < 0 || spriteIdx >= row.length) {
                continue;
            }
            int spriteId = row[spriteIdx];
            if (spriteId != -1) {
                host.drawCcIconSafe(g, spriteId, x, y);
            }

            //stage start connection lines
            if (id == 23 || id == 25 || id == 27 || id == 29 || id == 31 || id == 33 || id == 35) {
                int intensity = 255 - (effectList[i][2] * 64);
                if (intensity < 0) {
                    intensity = 0;
                } else if (intensity > 255) {
                    intensity = 255;
                }

                int rgb;
                if (id == 23 || id == 29) {
                    rgb = intensity;
                } else if (id == 25 || id == 31) {
                    rgb = intensity << 8;
                } else {
                    rgb = intensity << 16;
                }

                int lineY = 3 + ((effectList[i][5] >> 16) + 10);
                int lineX = playX + 3 + ((effectList[i][4] >> 16) + 25);

                g.setColor(rgb);
                g.drawLine(playX, lineY, playX + playW - 1, lineY);
                g.drawLine(lineX, playY, lineX, playY + playH - 1);
            }

            // DoJa : effectId 2 and 21 both render a small red spark rectangle.
            if (id == 2 && host.isHitSparkEnabled()) {
                int n = effectList[i][8] - effectList[i][1];
                int c = (255 - (n << 4)) & 0xFF;
                int size = 1 + (n >> 2);
                g.setColor(0xFF0000 | (c << 8) | c);
                int rx = (effectList[i][4] >> 16);
                int ry = (effectList[i][5] >> 16);
                fillPlayRect(g, rx, ry, size, size, playX, playY, playW, playH);
            }
        }

        // Boss connection lines (stage 6, celestial sphere).
        if (host.getBossF() == 21) {
            int d2 = (host.getMGCnt() << 4) & 0xFF;
            if (d2 > 127) {
                d2 = 255 - d2;
            }
            int c = 127 + d2;
            int rgb = (c << 16);
            g.setColor(rgb);

            int cnt = bossWrk[6];
            if (cnt > 1) {
                for (int j = 0; j < cnt; ++j) {
                    int a = bossWrk[8 + j];
                    int b = bossWrk[8 + ((j + 1) % cnt)];
                    if (a < 0 || b < 0 || a >= enemyList.length || b >= enemyList.length) {
                        continue;
                    }
                    if (enemyList[a][0] == 0 || enemyList[b][0] == 0) {
                        continue;
                    }
                    int x1 = ox + (enemyList[a][5] >> 16);
                    int y1 = oy + (enemyList[a][6] >> 16);
                    int x2 = ox + (enemyList[b][5] >> 16);
                    int y2 = oy + (enemyList[b][6] >> 16);
                    g.drawLine(x1, y1, x2, y2);
                }
            }
            if (cnt > 3) {
                for (int j = 0; j < cnt; ++j) {
                    int a = bossWrk[8 + j];
                    int b = bossWrk[8 + ((j + 2) % cnt)];
                    if (a < 0 || b < 0 || a >= enemyList.length || b >= enemyList.length) {
                        continue;
                    }
                    if (enemyList[a][0] == 0 || enemyList[b][0] == 0) {
                        continue;
                    }
                    int x1 = ox + (enemyList[a][5] >> 16);
                    int y1 = oy + (enemyList[a][6] >> 16);
                    int x2 = ox + (enemyList[b][5] >> 16);
                    int y2 = oy + (enemyList[b][6] >> 16);
                    g.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }

    public void stageKc(int effectId, int xFixed, int yFixed) {
        int[] row = null;
        int[][] table = host.getEffectTable();
        if (table != null && effectId >= 0 && effectId < table.length) {
            row = table[effectId];
        }
        if (row != null && row.length > 0) {
            stageKcEffect(effectId, xFixed, yFixed);
            return;
        }
        host.spawnBulletEffect(effectId, xFixed, yFixed);
    }

    public void stageKcEffect(int effectId, int xFixed, int yFixed) {
        int slot = -1;
        for (int i = 0; i < 64; i++) {
            if (effectList[i][0] == 0) {
                slot = i;
                break;
            }
        }
        if (slot == -1) {
            int best = 0;
            int bestScore = -1;
            for (int i = 0; i < 64; i++) {
                int score = (effectList[i][2] << 8) + (effectList[i][1] & 0xFF);
                if (score > bestScore) {
                    bestScore = score;
                    best = i;
                }
            }
            slot = best;
        }

        effectList[slot][0] = 1;
        effectList[slot][1] = 0;
        effectList[slot][2] = 0;
        effectList[slot][3] = effectId;
        effectList[slot][4] = xFixed;
        effectList[slot][5] = yFixed;
        effectList[slot][6] = 0;
        effectList[slot][7] = 0;
        effectList[slot][8] = 0;

        if (effectId == 21) {
            effectList[slot][6] = (abs(host.randomNextInt()) % 15 - 7) << 16;
            effectList[slot][7] = (abs(host.randomNextInt()) % 12 + 4) << 16;
            effectList[slot][8] = (abs(host.randomNextInt()) % 16);
            return;
        }
        if (effectId >= 23 && effectId <= 36) {
            if ((effectId & 1) != 0) {
                effectList[slot][6] = 1 << 16;
                effectList[slot][7] = 0;
            } else {
                effectList[slot][6] = 0;
                effectList[slot][7] = 2 << 16;
            }
            return;
        }
        if (effectId == 3 || effectId == 4 || effectId == 5) {
            effectList[slot][6] = (abs(host.randomNextInt()) % 17 - 8) << 16;
            effectList[slot][7] = (abs(host.randomNextInt()) % 17 - 8) << 16;
            effectList[slot][8] = (abs(host.randomNextInt()) % 4);
        }
    }

    private static int abs(int v) {
        if (v < 0) {
            return -v;
        }
        return v;
    }

    private static int speedCos(int n, int dirDeg) {
        return BattleMath.speedCos(n, dirDeg);
    }

    private static int speedSin(int n, int dirDeg) {
        return BattleMath.speedSin(n, dirDeg);
    }

    private boolean syncDialogueStateForTalkIndex(int talkIndex) {
        if (talkIndex == dialogueTalkIndex) {
            return false;
        }
        dialogueTalkIndex = talkIndex;
        dialoguePage = 0;
        dialoguePrevPage = 0;
        dialogueScrollY = 0;

        return true;
    }

    private int dialoguePageCountForTalkIndex(int talkIndex) {
        if (talkTable == null || talkIndex < 0 || talkIndex >= talkTable.length) {
            return 1;
        }
        String s = talkTable[talkIndex];
        if (s == null || s.length() == 0) {
            return 1;
        }
        int lines = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '|') {
                lines++;
            }
        }
        return (lines + 1) / 2;
    }

    private static String[] splitLinesSimple(String text) {
        if (text == null) {
            return new String[0];
        }

        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }

        String[] out = new String[lines];
        int pos = 0;
        int start = 0;
        for (int i = 0; i <= text.length(); i++) {
            boolean end = (i == text.length());
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

    // Replace HTML line breaks with wrap markers.
    private static String replaceBrTag(String s) {
        if (s == null) {
            return null;
        }
        int idx = s.indexOf("<br>");
        if (idx < 0) {
            idx = s.indexOf("<BR>");
        }
        if (idx < 0) {
            return s;
        }
        StringBuffer out = new StringBuffer();
        int p = 0;
        while (true) {
            int i = s.indexOf("<br>", p);
            int len = 4;
            if (i < 0) {
                i = s.indexOf("<BR>", p);
            }
            if (i < 0) {
                out.append(s.substring(p));
                break;
            }
            out.append(s.substring(p, i));
            out.append('|');
            p = i + len;
            if (p >= s.length()) {
                break;
            }
        }
        return out.toString();
    }

    private static String wrapDialogueLineWordAware(String s) {
        if (s == null) {
            return "";
        }
        if (s.length() == 0) {
            return s;
        }

        // Match BattleRenderer dialogue box: fillRectAlpha width=174, text starts at +13 (3px padding).
        final int maxPx = 168;

        StringBuffer out = new StringBuffer();
        int linePx = 0;
        int i = 0;

        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '|') {
                out.append('|');
                linePx = 0;
                i++;
                continue;
            }

            boolean isWs = (c == ' ' || c == '\t');
            int j = i;

            if (isWs) {
                while (j < s.length()) {
                    char wc = s.charAt(j);
                    if (wc != ' ' && wc != '\t') {
                        break;
                    }
                    j++;
                }

                if (linePx == 0) {
                    i = j;
                    continue;
                }

                String token = s.substring(i, j);
                int tokenPx = UiDraw.stringWidth(null, token);
                if (linePx + tokenPx > maxPx) {
                    out.append('|');
                    linePx = 0;
                    i = j;
                    continue;
                }

                out.append(token);
                linePx += tokenPx;
                i = j;
                continue;
            }

            while (j < s.length()) {
                char wc = s.charAt(j);
                if (wc == ' ' || wc == '\t' || wc == '|') {
                    break;
                }
                j++;
            }

            String word = s.substring(i, j);
            int wordPx = UiDraw.stringWidth(null, word);

            if (linePx > 0 && linePx + wordPx > maxPx) {
                out.append('|');
                linePx = 0;
            }

            if (wordPx <= maxPx) {
                out.append(word);
                linePx += wordPx;
                i = j;
                continue;
            }

            // Hard wrap long tokens (no spaces).
            for (int k = 0; k < word.length(); k++) {
                char ch = word.charAt(k);
                int chPx = UiDraw.charWidth(null, ch);
                if (linePx > 0 && linePx + chPx > maxPx) {
                    out.append('|');
                    linePx = 0;
                }
                out.append(ch);
                linePx += chPx;
            }

            i = j;
        }

        return out.toString();
    }
}