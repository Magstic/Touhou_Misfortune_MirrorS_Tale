package touhou.stage;

import touhou.SoundEffectSystem;
import touhou.replay.ReplaySnapshotReader;
import touhou.replay.ReplaySnapshotWriter;

public final class BossController {
    public interface Host extends BossState, BossWorld, BossServices {}

    public interface BossState {
        boolean isBossMode();
        int getStage();
        int getGameMode();
        int getChara();
        int getLevel();
        int getPower();
        int getFi();
        int getMGCnt();
        void setMGCnt(int v);
        int getBossF();
        void setBossF(int v);
        int getBbaria();
        void setBbaria(int v);
        int getBstock();
        int getPlayerXFixed();
        int getPlayerYFixed();
        int getSpellPracticeBossStep();
        int getTimeStop();
        void setTimeStop(int v);
    }

    public interface BossWorld {
        void stageJb(int enemyIdx);
        int stageFb(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9);
        int[] getBossX();
        int[] getBossY();
        int[] getBossWrk();
        int[][] getEnemyList();
    }

    public interface BossServices {
        void setResetFlag(int v);
        void setQ(boolean v);
        void playSound(int id, boolean loop);
        void aimEnemyBulletsToPlayer();
        void setEnemyBulletGlobalDir(int deg);
        void setEnemyBulletLock(int idx, int value);
        void spawnStageEffect(int effectId, int xFixed, int yFixed);
        void spawnEnemyBullet(int bulletId, int moveType, int angleDeg, int speedParam, int xFixed, int yFixed);
        void convertEnemyBulletsTo81Rain(int bossYFixed, int gc);
        void setEnemyBulletsDeltaForIdAndMoveType(int bulletId, int moveType, int newDelta);
        void setEnemyBulletsDeltaForIdIfDeltaZero(int bulletId, int baseDelta, int deltaStep);
    }

    public Host host() {
        return host;
    }

    private final Host host;

    private final BossStageLogic[] stageLogic = new BossStageLogic[7];

    final int[] bwave = new int[3];
    int bspellcnt = 255;
    boolean batkf;
    int bspellstep;

    int k;
    int l;
    int m;
    int n;
    final int[] o = new int[3];
    final int[] p = new int[3];
    int j;
    int zbLastFi = -1;
    int spellId = -1;

    // Proactive GC: armed when boss state changes, consumed by GameCore.
    private boolean gcArmed;

    public BossController(Host host) {
        this.host = host;

        stageLogic[0] = new Stage1BossLogic();
        stageLogic[1] = new Stage2BossLogic();
        stageLogic[2] = new Stage3BossLogic();
        stageLogic[3] = new Stage4BossLogic();
        stageLogic[4] = new Stage5BossLogic();
        stageLogic[5] = new Stage6BossLogic();
        stageLogic[6] = new StageExtraBossLogic();
    }

    private BossStageLogic getStageLogic(int stage) {
        if (stage < 0 || stage >= stageLogic.length) {
            return null;
        }
        return stageLogic[stage];
    }

    public void reset() {
        bwave[0] = 0;
        bwave[1] = 0;
        bwave[2] = 0;
        bspellcnt = 255;
        batkf = false;
        bspellstep = 0;

        k = 0;
        l = 0;
        m = 0;
        n = 0;
        o[0] = 0;
        o[1] = 0;
        o[2] = 0;
        p[0] = 0;
        p[1] = 0;
        p[2] = 0;
        j = 0;
        spellId = -1;
    }

    // Replay snapshot I/O.
    public void writeSnapshot(ReplaySnapshotWriter w) {
        if (w == null) {
            return;
        }

        w.writeI32LE(bwave.length);
        for (int i = 0; i < bwave.length; i++) {
            w.writeI32LE(bwave[i]);
        }

        w.writeI32LE(bspellcnt);
        w.writeBool(batkf);
        w.writeI32LE(bspellstep);

        w.writeI32LE(k);
        w.writeI32LE(l);
        w.writeI32LE(m);
        w.writeI32LE(n);

        w.writeI32LE(o.length);
        for (int i = 0; i < o.length; i++) {
            w.writeI32LE(o[i]);
        }

        w.writeI32LE(p.length);
        for (int i = 0; i < p.length; i++) {
            w.writeI32LE(p[i]);
        }

        w.writeI32LE(j);
        w.writeI32LE(zbLastFi);
        w.writeI32LE(spellId);
    }

    public void readSnapshot(ReplaySnapshotReader r) {
        if (r == null) {
            return;
        }

        int nWave = r.readI32LE();
        if (nWave < 0) {
            nWave = 0;
        }
        for (int i = 0; i < nWave; i++) {
            int v = r.readI32LE();
            if (i < bwave.length) {
                bwave[i] = v;
            }
        }
        for (int i = nWave; i < bwave.length; i++) {
            bwave[i] = 0;
        }

        bspellcnt = r.readI32LE();
        batkf = r.readBool();
        bspellstep = r.readI32LE();

        k = r.readI32LE();
        l = r.readI32LE();
        m = r.readI32LE();
        n = r.readI32LE();

        int nO = r.readI32LE();
        if (nO < 0) {
            nO = 0;
        }
        for (int i = 0; i < nO; i++) {
            int v = r.readI32LE();
            if (i < o.length) {
                o[i] = v;
            }
        }
        for (int i = nO; i < o.length; i++) {
            o[i] = 0;
        }

        int nP = r.readI32LE();
        if (nP < 0) {
            nP = 0;
        }
        for (int i = 0; i < nP; i++) {
            int v = r.readI32LE();
            if (i < p.length) {
                p[i] = v;
            }
        }
        for (int i = nP; i < p.length; i++) {
            p[i] = 0;
        }

        j = r.readI32LE();
        zbLastFi = r.readI32LE();
        spellId = r.readI32LE();
    }

    public int getBspellcnt() {
        return bspellcnt;
    }

    public int getBspellstep() {
        return bspellstep;
    }

    public int getBossNameIndex() {
        return n;
    }

    public int getHpBarAnimK() {
        return k;
    }

    public int getHpBarAnimMode() {
        return m;
    }

    public int getHpBarTargetIndex() {
        return l;
    }

    public int getHpBarAnimJ() {
        return j;
    }

    public int getHpBarAnimO0() {
        return o[0];
    }

    public int getHpBarAnimO1() {
        return o[1];
    }

    public int getHpBarAnimO2() {
        return o[2];
    }

    public int getHpBarAnimP0() {
        return p[0];
    }

    public int getHpBarAnimP1() {
        return p[1];
    }

    public int getHpBarAnimP2() {
        return p[2];
    }

    public int getSpellId() {
        return spellId;
    }

    public void resetSpellCountersAfterJb() {
        bspellstep = 0;
        bspellcnt = 255;
        spellId = -1;
    }

    public void tickHpBarOncePerFrame() {
        zbTickOncePerFrame();
    }

    public int applyBossDamage(int dmg) {
        if (dmg <= 0) {
            dmg = 1;
        }
        j -= dmg;
        if (j < 0) {
            j = 0;
        }
        return j;
    }

    public void enterHpBarMode2ForStockTransition() {
        j = 65535;
        yb(l, n, 2);
    }

    public void tick(int enemyIdx) {
        int[][] enemylist = host.getEnemyList();
        int mt = enemylist[enemyIdx][11];
        int beforeBossf = host.getBossF();

        if (mt == 100) {
            if (host.isBossMode()) {
                int bossf = host.getBossF();
                if (bossf == 2) {
                    int gc = host.getMGCnt();
                    if (gc == 0) {
                        host.setBbaria(1);
                    } else if (gc > 10) {
                        host.setBbaria(0);
                    }
                }
            }
            ub(enemyIdx);
            if (bspellstep == 1) {
                // Spell cut-in/chant phase: boss must be invincible.
                host.setBbaria(2);
            } else if (bspellstep == 2) {
                // Spell main phase: boss must be damageable.
                host.setBbaria(0);
            }
            int afterBossf = host.getBossF();
            if (host.getGameMode() == 3 && beforeBossf == 2 && afterBossf != 2) {
                // Spell practice: entering a spell step via wb(step) must be invincible immediately (same frame).
                host.setBbaria(2);
            }
            return;
        }

        if (!host.isBossMode()) {
            // Ensure spell-only state does not leak into stage sections.
            bspellstep = 0;
            return;
        }

        int bossf = host.getBossF();
        if (bossf == 0) {
            return;
        }

        if (bossf == 2) {
            int gc = host.getMGCnt();
            if (gc == 0) {
                host.setBbaria(1);
            } else if (gc > 10) {
                host.setBbaria(0);
            }
        }

        ub(enemyIdx);
        if (bspellstep == 1) {
            // Spell cut-in/chant phase: boss must be invincible.
            host.setBbaria(2);
        } else if (bspellstep == 2) {
            // Spell main phase: boss must be damageable.
            host.setBbaria(0);
        }
        int afterBossf = host.getBossF();
        if (host.getGameMode() == 3 && beforeBossf == 2 && afterBossf != 2) {
            // Spell practice: entering a spell step via wb(step) must be invincible immediately (same frame).
            host.setBbaria(2);
        }
    }

    private void zbTickOncePerFrame() {
        int fi = host.getFi();
        if (fi == zbLastFi) {
            return;
        }
        zbLastFi = fi;
        zbTick();
    }

    private void zbTick() {
        if (k != 1) {
            return;
        }

        switch (m) {
            case 0:
            case 3:
                if (o[0] < o[1]) {
                    o[0] += o[2] / 20;
                }
                if (o[0] > o[1]) {
                    o[0] = o[1];
                }
                o[1] = j;
                break;

            case 1:
                if (p[0] >= p[2]) {
                    if (o[0] < o[1]) {
                        o[0] += o[2] / 12;
                    }
                    if (o[0] > o[1]) {
                        o[0] = o[1];
                    }
                } else if (p[0] < p[2]) {
                    p[0] += p[2] / 8;
                }
                o[1] = j;
                break;

            case 2:
                o[0] = j;
                break;
        }

        if (o[0] < 0) {
            o[0] = 0;
        }
    }

    void vb(int enemyIdx, int state) {
        host.setMGCnt(-1);
        host.getEnemyList()[enemyIdx][7] = state;
        gcArmed = true;
    }

    void wb(int bossf) {
        host.setMGCnt(-1);
        host.setBossF(bossf);
        gcArmed = true;
    }

    // Proactive GC: returns true once after a state change, then resets.
    public boolean consumeGcArmed() {
        if (gcArmed) {
            gcArmed = false;
            return true;
        }
        return false;
    }

    void yb(int l, int n, int m) {
        if (host.getGameMode() != 3 || m == 3) {
            k = 1;
            this.l = l;
            this.m = m;
            this.n = n;
            for (int i = 0; i < 3; ++i) {
                o[i] = 0;
            }
            o[1] = j;
            o[2] = j;
            p[1] = 0;
            if (this.m == 1) {
                p[2] = 40;
                return;
            }
            p[2] = 0;
        }
    }

    private void ub(int enemyIdx) {
        int[][] enemylist = host.getEnemyList();
        int[] bossx = host.getBossX();
        int[] bossy = host.getBossY();

        int idx = 0;
        int mt = enemylist[enemyIdx][11];
        if (mt == 102) {
            idx = 1;
        } else if (mt == 103) {
            idx = 2;
        }

        if (bossx[idx] == 0 && bossy[idx] == 0) {
            bossx[idx] = enemylist[enemyIdx][5] >> 16;
            bossy[idx] = enemylist[enemyIdx][6] >> 16;
        }

        if (enemylist[enemyIdx][5] < ((bossx[idx] + 1) << 16)) {
            enemylist[enemyIdx][5] += enemylist[enemyIdx][14];
        }
        if (enemylist[enemyIdx][5] > ((bossx[idx] - 1) << 16)) {
            enemylist[enemyIdx][5] -= enemylist[enemyIdx][14];
        }
        if (enemylist[enemyIdx][6] < ((bossy[idx] + 1) << 16)) {
            enemylist[enemyIdx][6] += enemylist[enemyIdx][15];
        }
        if (enemylist[enemyIdx][6] > ((bossy[idx] - 1) << 16)) {
            enemylist[enemyIdx][6] -= enemylist[enemyIdx][15];
        }

        bwave[idx] = (bwave[idx] + 15) % 360;

        if (bspellcnt >= 0 && bspellcnt != 255 && (host.getFi() & 0xF) == 0 && idx == 0 && host.getTimeStop() == 0) {
            --bspellcnt;
            if (bspellcnt < 10) {
                host.playSound(SoundEffectSystem.SE_CD, false);
            }
        }

        if (mt == 100) {
            BossStageLogic logic = getStageLogic(host.getStage());
            if (logic != null) {
                logic.tickMidboss(this, enemyIdx);
            }
            return;
        }

        BossStageLogic logic = getStageLogic(host.getStage());
        if (logic != null) {
            logic.tickBoss(this, enemyIdx);
        }

        int bossf = host.getBossF();
        if (bossf > 0 && bossf < 10) {
            bspellstep = 0;
            spellId = -1;
        }
    }
//stage1 midboss

//stage2 midboss

//stage3 midboss

//stage1 boss

//stage5 boss(stub)

//stage6 boss(stub)

//stage extra boss(stub)

//stage2 boss

//stage3 boss

//stage4 boss(stub)

}
