package touhou;

import javax.microedition.lcdui.Graphics;

import touhou.replay.ReplaySnapshotReader;
import touhou.replay.ReplaySnapshotWriter;

public final class BulletSystem {
    // Callback for stage effects (mirrors DoJa kc(effectId, x, y)).
    public interface EffectSpawner {
        void spawnEffect(int effectId, int xFixed, int yFixed);
    }

    private EffectSpawner effectSpawner;

    private final int max;

    private final boolean[] active;
    private final boolean[] fromPlayer;
    private final int[] bulletId;
    private final int[] animFrames;
    private final int[] animTick;
    private final int[] moveType;
    private final int[] angleDeg;
    private final int[] delta;
    private final int[] savedDelta;
    private final int[] stageFlag;
    private final int[] keepAlive;
    private final int[] tag;

    private final int[] slotLockStamp;
    private int updateStamp;
    private boolean updateRunning;

    private int allocHint;

    // Perf counters (per frame).
    private int perfSpawnCount;
    private int perfKillCount;
    private int perfAllocFailCount;

    private final int[] power;

    private final int[] x0;
    private final int[] y0;
    private final int[] x;
    private final int[] y;

    private final int[] t;

    private int globalDir;
    private int enemyLevel;

    private int enemyAimTargetX;
    private int enemyAimTargetY;

    private final int[] enemyBulletLock = new int[3];

    private BulletSprites sprites;

    private EnemySystem enemies;

    public BulletSystem(int maxBullets) {
        this.max = maxBullets;
        this.active = new boolean[maxBullets];
        this.fromPlayer = new boolean[maxBullets];
        this.bulletId = new int[maxBullets];
        this.animFrames = new int[maxBullets];
        this.animTick = new int[maxBullets];
        this.moveType = new int[maxBullets];
        this.angleDeg = new int[maxBullets];
        this.delta = new int[maxBullets];
        this.savedDelta = new int[maxBullets];
        this.stageFlag = new int[maxBullets];

        this.keepAlive = new int[maxBullets];

        this.tag = new int[maxBullets];

        this.slotLockStamp = new int[maxBullets];
        this.updateStamp = 1;

        this.power = new int[maxBullets];
        this.x0 = new int[maxBullets];
        this.y0 = new int[maxBullets];
        this.x = new int[maxBullets];
        this.y = new int[maxBullets];
        this.t = new int[maxBullets];

        this.globalDir = 270;
        this.allocHint = 0;
    }

    public void setEffectSpawner(EffectSpawner spawner) {
        this.effectSpawner = spawner;
    }

    public int getMax() {
        return max;
    }

    // Package-private direct array access for hot-path collision detection.
    // Callers must ensure index is in [0, max).
    boolean[] activeArray() { return active; }
    boolean[] fromPlayerArray() { return fromPlayer; }
    int[] xArray() { return x; }
    int[] yArray() { return y; }
    int[] bulletIdArray() { return bulletId; }

    public boolean isActive(int idx) {
        return idx >= 0 && idx < max && active[idx];
    }

    public boolean isFromPlayer(int idx) {
        if (idx < 0 || idx >= max) {
            return false;
        }
        return fromPlayer[idx];
    }

    public int getBulletId(int idx) {
        if (idx < 0 || idx >= max) {
            return 0;
        }
        return bulletId[idx];
    }

    public int getMoveType(int idx) {
        if (idx < 0 || idx >= max) {
            return 0;
        }
        return moveType[idx];
    }

    public int getXFixed(int idx) {
        if (idx < 0 || idx >= max) {
            return 0;
        }
        return x[idx];
    }

    public int getYFixed(int idx) {
        if (idx < 0 || idx >= max) {
            return 0;
        }
        return y[idx];
    }

    public void deactivate(int idx) {
        if (idx < 0 || idx >= max) {
            return;
        }
        kill(idx);
    }

    private void kill(int idx) {
        if (active[idx]) {
            perfKillCount++;
        }
        active[idx] = false;
        if (updateRunning) {
            slotLockStamp[idx] = updateStamp;
        }
    }

    private void deactivateNoLock(int idx) {
        active[idx] = false;
    }

    // Begin a new perf frame (reset per-frame counters).
    public void perfBeginFrame() {
        perfSpawnCount = 0;
        perfKillCount = 0;
        perfAllocFailCount = 0;
    }

    public int consumePerfSpawnCount() {
        int v = perfSpawnCount;
        perfSpawnCount = 0;
        return v;
    }

    public int consumePerfKillCount() {
        int v = perfKillCount;
        perfKillCount = 0;
        return v;
    }

    public int consumePerfAllocFailCount() {
        int v = perfAllocFailCount;
        perfAllocFailCount = 0;
        return v;
    }

    public void setGlobalDir(int deg) {
        globalDir = normalizeDeg(deg);
    }

    public void setEnemyLevel(int level) {
        enemyLevel = level;
    }

    public void setEnemyAimTarget(int xFixed, int yFixed) {
        enemyAimTargetX = xFixed;
        enemyAimTargetY = yFixed;
    }

    public void setEnemyBulletLock(int idx, int value) {
        if (idx < 0 || idx >= enemyBulletLock.length) {
            return;
        }
        enemyBulletLock[idx] = value;
    }

    public void setEnemyBulletsDeltaForIdAndMoveType(int targetBulletId, int targetMoveType, int newDelta) {
        for (int i = 0; i < max; i++) {
            if (!active[i] || fromPlayer[i]) {
                continue;
            }
            if (bulletId[i] != targetBulletId || moveType[i] != targetMoveType) {
                continue;
            }
            delta[i] = newDelta;
        }
    }

    public void setEnemyBulletsDeltaForIdIfDeltaZero(int targetBulletId, int baseDelta, int deltaStep) {
        for (int i = 0; i < max; i++) {
            if (!active[i] || fromPlayer[i]) {
                continue;
            }
            if (bulletId[i] != targetBulletId) {
                continue;
            }
            if (delta[i] != 0) {
                continue;
            }
            int newDelta = baseDelta + ((i & 0x7) * deltaStep);
            delta[i] = newDelta;
            savedDelta[i] = newDelta;

            x0[i] = x[i];
            y0[i] = y[i];
            t[i] = 0;
        }
    }

    public void setSprites(BulletSprites sprites) {
        this.sprites = sprites;
    }

    public void setEnemies(EnemySystem enemies) {
        this.enemies = enemies;
    }

    public void spawn(int bId, int mType, int angDeg, int accelOrSpeed, int spawnX, int spawnY) {
        spawnExOwner(bId, 1, mType, angDeg, accelOrSpeed, spawnX, spawnY, 0, false);
    }

    public void spawnPlayer(int bId, int mType, int angDeg, int accelOrSpeed, int spawnX, int spawnY) {
        spawnExOwner(bId, 1, mType, angDeg, accelOrSpeed, spawnX, spawnY, 0, true);
    }

    public void spawnEx(int baseBulletId, int frames, int mType, int angDeg, int accelOrSpeed, int spawnX, int spawnY, int shotPower) {
        spawnExOwner(baseBulletId, frames, mType, angDeg, accelOrSpeed, spawnX, spawnY, shotPower, false);
    }

    public void spawnPlayerEx(int baseBulletId, int frames, int mType, int angDeg, int accelOrSpeed, int spawnX, int spawnY, int shotPower) {
        spawnExOwner(baseBulletId, frames, mType, angDeg, accelOrSpeed, spawnX, spawnY, shotPower, true);
    }

    public void spawnEnemyDecel(int bId, int angDeg, int speed, int decel, int minSpeed, int spawnX, int spawnY) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = 12;
        angleDeg[idx] = normalizeDeg(angDeg);
        delta[idx] = 0;
        savedDelta[idx] = decel;
        stageFlag[idx] = speed;
        keepAlive[idx] = 0;
        tag[idx] = minSpeed;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    private void spawnEnemyOrbitBullet(int bId, int angDeg, int speedParam, int parentIdx) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = 5;
        angleDeg[idx] = normalizeDeg(angDeg);
        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = speedParam;
        keepAlive[idx] = 0;
        tag[idx] = parentIdx;

        int px = 0;
        int py = 0;
        if (parentIdx >= 0 && parentIdx < max && active[parentIdx]) {
            px = x[parentIdx];
            py = y[parentIdx];
        }
        x0[idx] = px;
        y0[idx] = py;
        x[idx] = px;
        y[idx] = py;

        t[idx] = 0;
    }

    public void spawnEnemyChild91(int bId, int angDeg, int speedParam, int parentIdx, int angleDelta) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = 91;
        angleDeg[idx] = normalizeDeg(angDeg);
        delta[idx] = 0;
        savedDelta[idx] = angleDelta;
        stageFlag[idx] = speedParam;
        keepAlive[idx] = 0;
        tag[idx] = parentIdx;

        int px = 0;
        int py = 0;
        if (parentIdx >= 0 && parentIdx < max && active[parentIdx]) {
            px = x[parentIdx];
            py = y[parentIdx];
        }
        x0[idx] = px;
        y0[idx] = py;
        x[idx] = px;
        y[idx] = py;

        t[idx] = 0;
    }

    public void spawnEnemyChild52(int bId, int angDeg, int speedParam, int parentIdx, int angleDelta) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = 52;
        angleDeg[idx] = normalizeDeg(angDeg);
        delta[idx] = 0;
        savedDelta[idx] = angleDelta;
        stageFlag[idx] = speedParam;
        keepAlive[idx] = 0;
        tag[idx] = parentIdx;

        int px = 0;
        int py = 0;
        if (parentIdx >= 0 && parentIdx < max && active[parentIdx]) {
            px = x[parentIdx];
            py = y[parentIdx];
        }
        x0[idx] = px;
        y0[idx] = py;
        x[idx] = px;
        y[idx] = py;

        t[idx] = 0;
    }

    public void spawnEnemyChild(int bId, int mType, int angDeg, int speedParam, int parentIdx) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = mType;
        angleDeg[idx] = normalizeDeg(angDeg);
        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = speedParam;
        keepAlive[idx] = 0;
        tag[idx] = parentIdx;

        int px = 0;
        int py = 0;
        if (parentIdx >= 0 && parentIdx < max && active[parentIdx]) {
            px = x[parentIdx];
            py = y[parentIdx];
        }
        x0[idx] = px;
        y0[idx] = py;
        x[idx] = px;
        y[idx] = py;

        t[idx] = 0;
    }

    public void spawnEnemyAccel40(int bId, int angDeg, int speedParam, int spawnX, int spawnY) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = 40;
        angleDeg[idx] = normalizeDeg(angDeg);

        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = speedParam;
        tag[idx] = 0;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    public void spawnEnemySpiral43_44(int bId, int mType, int angDeg, int speedParam, int spawnX, int spawnY) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = mType;
        angleDeg[idx] = normalizeDeg(angDeg);

        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = speedParam;
        tag[idx] = 0;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    public void spawnEnemyRamp41(int bId, int angDeg, int speedParam, int spawnX, int spawnY) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = 41;
        angleDeg[idx] = normalizeDeg(angDeg);

        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = speedParam;
        tag[idx] = 0;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    public void spawnEnemyAccel(int bId, int mType, int angDeg, int speedParam, int spawnX, int spawnY) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = mType;
        angleDeg[idx] = normalizeDeg(angDeg);

        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = speedParam;
        tag[idx] = 0;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    public void spawnEnemyStopTurnAccel(int bId, int mType, int angDeg, int speedParam, int spawnX, int spawnY) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = mType;
        angleDeg[idx] = normalizeDeg(angDeg);

        delta[idx] = 0;
        savedDelta[idx] = speedParam;
        stageFlag[idx] = speedParam;
        tag[idx] = 0;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    public void spawnEnemyCurve(int bId, int mType, int angDeg, int accel, int spawnX, int spawnY) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = bId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = mType;
        angleDeg[idx] = normalizeDeg(angDeg);
        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = accel;
        tag[idx] = 1;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    private void spawnExOwner(int baseBulletId, int frames, int mType, int angDeg, int accelOrSpeed, int spawnX, int spawnY, int shotPower, boolean isFromPlayer) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = isFromPlayer;
        bulletId[idx] = baseBulletId;
        animFrames[idx] = (frames <= 1) ? 1 : frames;
        animTick[idx] = 0;
        power[idx] = shotPower;

        moveType[idx] = mType;
        angleDeg[idx] = normalizeDeg(angDeg);
        delta[idx] = accelOrSpeed;
        savedDelta[idx] = accelOrSpeed;
        int initStage = 0;
        if (isFromPlayer && (mType == 4 || mType == 5) && baseBulletId >= 61 && baseBulletId <= 80) {
            initStage = 0;
        }
        if (!isFromPlayer && (mType == 12 || mType == 13 || mType == 14 || mType == 15 || mType == 16
                || mType == 17 || mType == 18 || mType == 19 || mType == 31 || mType == 33 || mType == 34
                || mType == 35 || mType == 36 || mType == 37 || mType == 38 || mType == 39 || mType == 42 || mType == 41 || mType == 43
                || mType == 44 || mType == 45 || mType == 46 || mType == 47 || mType == 48 || mType == 49
                || mType == 50 || mType == 51 || mType == 52 || mType == 53 || mType == 54 || mType == 55
                || mType == 56 || mType == 57 || mType == 62
                || mType == 20 || mType == 23 || mType == 24 || mType == 25 || mType == 58 || mType == 59
                || mType == 60 || mType == 61 || mType == 63 || mType == 64 || mType == 65 || mType == 66
                || mType == 67 || mType == 68 || mType == 69 || mType == 70 || mType == 74 || mType == 75
                || mType == 76 || mType == 77 || mType == 78 || mType == 79 || mType == 80
                || mType == 29 || mType == 30 || mType == 71 || mType == 72 || mType == 73
                || mType == 81 || mType == 82 || mType == 83 || mType == 84
                || mType == 85 || mType == 86 || mType == 87 || mType == 88
                || mType == 89 || mType == 90 || mType == 91)) {
            delta[idx] = 0;
            savedDelta[idx] = 0;
            initStage = accelOrSpeed;
        }
        stageFlag[idx] = initStage;
        keepAlive[idx] = 0;
        if (!isFromPlayer && (mType == 38 || mType == 39 || mType == 42)) {
            keepAlive[idx] = 120;
        }
        if (isFromPlayer && mType == 1) {
            tag[idx] = -1;
        } else {
            tag[idx] = 0;
        }

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    public void spawnEffectTagged(int baseBulletId, int frames, int life, int spawnX, int spawnY, int tagValue) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }
        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = baseBulletId;
        animFrames[idx] = (frames <= 1) ? 1 : frames;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = 2;
        angleDeg[idx] = 270;
        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = (life <= 0) ? 1 : life;
        tag[idx] = tagValue;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    public int getLaserStageFlag(int baseBulletId, int mType, int tagValue, boolean isFromPlayer) {
        for (int i = 0; i < max; i++) {
            if (!active[i]) {
                continue;
            }
            if (fromPlayer[i] != isFromPlayer) {
                continue;
            }
            if (mType == 2 && baseBulletId == 103 && isFromPlayer) {
                int bid = bulletId[i];
                if (bid != 103 && bid != 104) {
                    continue;
                }
            } else {
                if (bulletId[i] != baseBulletId) {
                    continue;
                }
            }
            if (moveType[i] != mType) {
                continue;
            }
            if (tag[i] != tagValue) {
                continue;
            }
            return stageFlag[i];
        }
        return 0;
    }

    public void spawnOrUpdateLaser(int baseBulletId, int mType, int tagValue, int spawnX, int spawnY, int shotPower) {
        spawnOrUpdateLaserOwner(baseBulletId, mType, tagValue, spawnX, spawnY, shotPower, false);
    }

    public void spawnOrUpdatePlayerLaser(int baseBulletId, int mType, int tagValue, int spawnX, int spawnY, int shotPower) {
        spawnOrUpdateLaserOwner(baseBulletId, mType, tagValue, spawnX, spawnY, shotPower, true);
    }

    private void spawnOrUpdateLaserOwner(int baseBulletId, int mType, int tagValue, int spawnX, int spawnY, int shotPower, boolean isFromPlayer) {
        int keep = isFromPlayer ? 32 : 2;
        boolean isMarisaALaser = isFromPlayer && mType == 2 && baseBulletId == 103;
        boolean isMarisaBombLaser = isFromPlayer && mType == 3 && baseBulletId == 105;
        if (isMarisaALaser) {
            keep = 10;
        }
        if (isMarisaBombLaser) {
            keep = 10;
        }

        int yAdj = spawnY;
        if (isMarisaALaser) {
            yAdj = spawnY + Fixed.fromInt(6);
        }
        if (isMarisaBombLaser) {
            yAdj = spawnY + Fixed.fromInt(6);
        }
        for (int i = 0; i < max; i++) {
            if (!active[i]) {
                continue;
            }
            boolean idOk;
            if (isMarisaALaser) {
                int bid = bulletId[i];
                idOk = (bid == 103 || bid == 104);
            } else if (isMarisaBombLaser) {
                int bid = bulletId[i];
                idOk = (bid >= 105 && bid <= 110);
            } else {
                idOk = (bulletId[i] == baseBulletId);
            }

            if (idOk && moveType[i] == mType && tag[i] == tagValue) {
                fromPlayer[i] = isFromPlayer;
                x0[i] = spawnX;
                y0[i] = yAdj;
                x[i] = spawnX;
                y[i] = yAdj;
                power[i] = shotPower;
                if (isMarisaALaser) {
                    stageFlag[i] += 2;
                    if (stageFlag[i] > keep) {
                        stageFlag[i] = keep;
                    }
                } else if (isMarisaBombLaser) {
                    stageFlag[i] += 2;
                    if (stageFlag[i] > keep) {
                        stageFlag[i] = keep;
                    }
                } else {
                    stageFlag[i] = keep;
                }
                return;
            }
        }

        int idx = alloc();
        if (idx < 0) {
            return;
        }

        active[idx] = true;
        fromPlayer[idx] = isFromPlayer;
        bulletId[idx] = baseBulletId;
        animFrames[idx] = 1;
        animTick[idx] = 0;
        power[idx] = shotPower;

        moveType[idx] = mType;
        angleDeg[idx] = 270;
        delta[idx] = 0;
        savedDelta[idx] = 0;
        if (isMarisaALaser) {
            stageFlag[idx] = 0;
        } else if (isMarisaBombLaser) {
            stageFlag[idx] = 0;
        } else {
            stageFlag[idx] = keep;
        }
        tag[idx] = tagValue;

        x0[idx] = spawnX;
        y0[idx] = yAdj;
        x[idx] = spawnX;
        y[idx] = yAdj;

        t[idx] = 0;
    }

    public void update(int areaX, int areaY, int areaW, int areaH) {
        int minX = Fixed.fromInt(areaX - 32);
        int minY = Fixed.fromInt(areaY - 32);
        int maxX = Fixed.fromInt(areaX + areaW + 32);
        int maxY = Fixed.fromInt(areaY + areaH + 32);

        int enemyMinX = Fixed.fromInt(areaX - 30);
        int enemyMinY = Fixed.fromInt(areaY - 30);
        int enemyMaxX = Fixed.fromInt(areaX + areaW + 30);
        int enemyMaxY = Fixed.fromInt(areaY + areaH + 30);

        updateStamp += 1;
        if (updateStamp == 0) {
            updateStamp = 1;
        }
        updateRunning = true;

        for (int i = 0; i < max; i++) {
            if (!active[i]) {
                continue;
            }
            step(i);

            if (!active[i]) {
                continue;
            }

            animTick[i]++;

            if (!fromPlayer[i]) {
                if (keepAlive[i] > 0) {
                    keepAlive[i] -= 1;
                } else if (x[i] < enemyMinX || x[i] > enemyMaxX || y[i] < enemyMinY || y[i] > enemyMaxY) {
                    kill(i);
                }
                continue;
            }

            if (keepAlive[i] > 0) {
                keepAlive[i] -= 1;
            }

            if (x[i] < minX || x[i] > maxX || y[i] < minY || y[i] > maxY) {
                kill(i);
            }
        }

        updateRunning = false;
    }

    public void aimEnemyBulletsTo(int targetXFixed, int targetYFixed) {
        for (int i = 0; i < max; i++) {
            if (!active[i]) {
                continue;
            }
            if (fromPlayer[i]) {
                continue;
            }

            int dx = targetXFixed - x[i];
            int dy = targetYFixed - y[i];
            angleDeg[i] = arcTan2Deg(dy, dx) % 360;

            x0[i] = x[i];
            y0[i] = y[i];
            t[i] = 0;
        }
    }

    public void convertEnemyBulletsTo81Rain(int bossYFixed, int gc) {
        int spd = (1 + (gc & 0x3)) << 3;
        for (int i = 0; i < max; i++) {
            if (!active[i] || fromPlayer[i]) {
                continue;
            }
            if (bulletId[i] < 0 || bulletId[i] == 81) {
                continue;
            }
            if (y[i] >= bossYFixed) {
                continue;
            }

            bulletId[i] = 81;

            int ang = normalizeDeg(angleDeg[i] + 180);
            if (gc >= 100) {
                if (ang > 90 && ang <= 270) {
                    ang = normalizeDeg(ang - 60);
                } else {
                    ang = normalizeDeg(ang + 60);
                }
            }
            angleDeg[i] = ang;

            moveType[i] = 0;
            tag[i] = 0;
            stageFlag[i] = 0;
            delta[i] = spd;
            savedDelta[i] = spd;
            x0[i] = x[i];
            y0[i] = y[i];
            t[i] = 0;
        }
    }

    public int activeCount() {
        int c = 0;
        for (int i = 0; i < max; i++) {
            if (active[i]) {
                c++;
            }
        }
        return c;
    }

    public void clear() {
        for (int i = 0; i < max; i++) {
            active[i] = false;
            keepAlive[i] = 0;
            slotLockStamp[i] = 0;
        }
        allocHint = 0;
    }

    // Replay snapshot I/O.
    public void writeSnapshot(ReplaySnapshotWriter w) {
        if (w == null) {
            return;
        }

        w.writeI32LE(max);
        w.writeI32LE(updateStamp);
        w.writeBool(updateRunning);

        w.writeI32LE(globalDir);
        w.writeI32LE(enemyLevel);
        w.writeI32LE(enemyAimTargetX);
        w.writeI32LE(enemyAimTargetY);

        for (int i = 0; i < enemyBulletLock.length; i++) {
            w.writeI32LE(enemyBulletLock[i]);
        }

        for (int i = 0; i < max; i++) {
            w.writeBool(active[i]);
            w.writeBool(fromPlayer[i]);

            w.writeI32LE(bulletId[i]);
            w.writeI32LE(animFrames[i]);
            w.writeI32LE(animTick[i]);
            w.writeI32LE(moveType[i]);
            w.writeI32LE(angleDeg[i]);
            w.writeI32LE(delta[i]);
            w.writeI32LE(savedDelta[i]);
            w.writeI32LE(stageFlag[i]);
            w.writeI32LE(keepAlive[i]);
            w.writeI32LE(tag[i]);
            w.writeI32LE(slotLockStamp[i]);
            w.writeI32LE(power[i]);

            w.writeI32LE(x0[i]);
            w.writeI32LE(y0[i]);
            w.writeI32LE(x[i]);
            w.writeI32LE(y[i]);
            w.writeI32LE(t[i]);
        }
    }

    public void readSnapshot(ReplaySnapshotReader r) {
        if (r == null) {
            return;
        }

        int storedMax = r.readI32LE();
        updateStamp = r.readI32LE();
        r.readBool();
        updateRunning = false;

        globalDir = r.readI32LE();
        enemyLevel = r.readI32LE();
        enemyAimTargetX = r.readI32LE();
        enemyAimTargetY = r.readI32LE();

        for (int i = 0; i < enemyBulletLock.length; i++) {
            enemyBulletLock[i] = r.readI32LE();
        }

        int readCount = storedMax;
        if (readCount < 0) {
            readCount = 0;
        }
        for (int i = 0; i < readCount; i++) {
            boolean a = r.readBool();
            boolean fp = r.readBool();

            int bId = r.readI32LE();
            int aFrames = r.readI32LE();
            int aTick = r.readI32LE();
            int mType = r.readI32LE();
            int ang = r.readI32LE();
            int d = r.readI32LE();
            int sd = r.readI32LE();
            int sf = r.readI32LE();
            int ka = r.readI32LE();
            int tg = r.readI32LE();
            int lock = r.readI32LE();
            int pow = r.readI32LE();

            int xx0 = r.readI32LE();
            int yy0 = r.readI32LE();
            int xx = r.readI32LE();
            int yy = r.readI32LE();
            int tt = r.readI32LE();

            if (i >= max) {
                continue;
            }

            active[i] = a;
            fromPlayer[i] = fp;
            bulletId[i] = bId;
            animFrames[i] = aFrames;
            animTick[i] = aTick;
            moveType[i] = mType;
            angleDeg[i] = ang;
            delta[i] = d;
            savedDelta[i] = sd;
            stageFlag[i] = sf;
            keepAlive[i] = ka;
            tag[i] = tg;
            slotLockStamp[i] = lock;
            power[i] = pow;
            x0[i] = xx0;
            y0[i] = yy0;
            x[i] = xx;
            y[i] = yy;
            t[i] = tt;
        }

        for (int i = readCount; i < max; i++) {
            active[i] = false;
            fromPlayer[i] = false;
            bulletId[i] = 0;
            animFrames[i] = 0;
            animTick[i] = 0;
            moveType[i] = 0;
            angleDeg[i] = 0;
            delta[i] = 0;
            savedDelta[i] = 0;
            stageFlag[i] = 0;
            keepAlive[i] = 0;
            tag[i] = 0;
            slotLockStamp[i] = 0;
            power[i] = 0;
            x0[i] = 0;
            y0[i] = 0;
            x[i] = 0;
            y[i] = 0;
            t[i] = 0;
        }
    }

    public void clearEnemyBullets() {
        for (int i = 0; i < max; i++) {
            if (!active[i]) {
                continue;
            }
            if (!fromPlayer[i]) {
                active[i] = false;
            }
        }
    }

    // Mirrors DoJa : cb() clears player laser bullets.
    public void clearPlayerLasers() {
        for (int i = 0; i < max; i++) {
            if (!active[i] || !fromPlayer[i]) {
                continue;
            }

            if (!(moveType[i] == 2 || moveType[i] == 3)) {
                continue;
            }

            int id = bulletId[i];
            if (id == 103 || id == 104 || id == 105) {
                active[i] = false;
            }
        }
    }

    public void clearEnemyBulletsWithEffect(int effectBulletId, int effectLife) {
        for (int i = 0; i < max; i++) {
            if (!active[i] || fromPlayer[i]) {
                continue;
            }
            if (moveType[i] == 2 && power[i] == 0 && tag[i] != 0) {
                continue;
            }
            if (bulletId[i] == effectBulletId && power[i] == 0 && moveType[i] == 2 && tag[i] == 0) {
                continue;
            }
            int ex = x[i];
            int ey = y[i];
            active[i] = false;
            if ((i & 1) == 0) {
                spawnEffect(effectBulletId, 1, effectLife, ex, ey);
            }
        }
    }

    // Mirrors DoJa : gc() converts all enemy bullets into vanish animation (462..466).
    public void convertEnemyBulletsToDeathVanish() {
        for (int i = 0; i < max; i++) {
            if (!active[i] || fromPlayer[i]) {
                continue;
            }
            if (moveType[i] == 2 && power[i] == 0) {
                continue;
            }
            bulletId[i] = 462;
            moveType[i] = 32;
            t[i] = 0;
            animTick[i] = 0;
            keepAlive[i] = 5;
        }
    }

    public void spawnEffect(int baseBulletId, int frames, int life, int spawnX, int spawnY) {
        int idx = alloc();
        if (idx < 0) {
            return;
        }
        active[idx] = true;
        fromPlayer[idx] = false;
        bulletId[idx] = baseBulletId;
        animFrames[idx] = (frames <= 1) ? 1 : frames;
        animTick[idx] = 0;
        power[idx] = 0;

        moveType[idx] = 2;
        angleDeg[idx] = 270;
        delta[idx] = 0;
        savedDelta[idx] = 0;
        stageFlag[idx] = (life <= 0) ? 1 : life;
        keepAlive[idx] = 0;
        tag[idx] = 0;

        x0[idx] = spawnX;
        y0[idx] = spawnY;
        x[idx] = spawnX;
        y[idx] = spawnY;

        t[idx] = 0;
    }

    public int getPower(int idx) {
        if (idx < 0 || idx >= max) {
            return 0;
        }
        return power[idx];
    }

    public int getStageFlag(int idx) {
        if (idx < 0 || idx >= max) {
            return 0;
        }
        return stageFlag[idx];
    }

    private void step(int i) {
        int ang = angleDeg[i];
        if (ang < 0 || ang >= 360) {
            ang %= 360;
            if (ang < 0) {
                ang += 360;
            }
            angleDeg[i] = ang;
        }
        int c = Trig.cos0_359(ang);
        int s = Trig.sin0_359(ang);

        int mt = moveType[i];
        int tt = t[i];

        if (!fromPlayer[i] && (mt == 6 || mt == 7 || mt == 8 || mt == 9)) {
            int state = tag[i];
            int dist = delta[i];
            int spd = stageFlag[i];

            if (state == 0) {
                int distFixed = dist << 13;
                x[i] = x0[i] + Fixed.mul(c, distFixed);
                y[i] = y0[i] + Fixed.mul(s, distFixed);
                t[i] = tt + 1;

                dist += spd;
                spd -= 8;
                if (spd < 0) {
                    spd = 0;
                    x0[i] = x[i];
                    y0[i] = y[i];
                    dist = 0;
                    state = 1;
                }

                delta[i] = dist;
                stageFlag[i] = spd;
                tag[i] = state;
                return;
            }

            if (state == 1) {
                dist += 1;
                delta[i] = dist;
                t[i] = tt + 1;

                if (dist < 5) {
                    if (mt == 6) {
                        angleDeg[i] = normalizeDeg(ang + 10);
                    } else if (mt == 8) {
                        angleDeg[i] = normalizeDeg(ang + 350);
                    } else if (mt == 9) {
                        angleDeg[i] = arcTan2Deg(enemyAimTargetY - y[i], enemyAimTargetX - x[i]) % 360;
                    }
                    tag[i] = 2;
                }
                return;
            }

            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            dist += spd;
            spd += 16;
            int maxSpd = savedDelta[i];
            if (spd > maxSpd) {
                spd = maxSpd;
            }

            delta[i] = dist;
            stageFlag[i] = spd;
            return;
        }

        if (!fromPlayer[i] && mt == 41) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            if (dist < 160) {
                delta[i] = dist + 96;
                return;
            }

            dist += stageFlag[i];
            delta[i] = dist;

            if ((tt & 1) == 0 || tt > 70) {
                stageFlag[i] += 1;
            }
            return;
        }

        // Stage EXTRA fodder: accelerating burst bullets.
        if (!fromPlayer[i] && mt == 40) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i];
            dist += spd;
            spd += 2;

            delta[i] = dist;
            stageFlag[i] = spd;
            return;
        }

        // Stage EXTRA fodder: spiral split bullets that transition to moveType 1.
        if (!fromPlayer[i] && (mt == 43 || mt == 44)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            dist += stageFlag[i];
            delta[i] = dist;

            int spd = stageFlag[i] - 1;
            stageFlag[i] = spd;

            if (spd <= 0) {
                stageFlag[i] = 8;
                if (mt == 43) {
                    angleDeg[i] = normalizeDeg(ang + 30);
                } else {
                    angleDeg[i] = normalizeDeg(ang - 30);
                }
                moveType[i] = 1;
                delta[i] = 0;
                x0[i] = x[i];
                y0[i] = y[i];
                t[i] = 0;
                keepAlive[i] = 5;
            }
            return;
        }

        if (!fromPlayer[i] && mt == 45) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            if (tt < 12) {
                dist += 28;
            } else {
                dist += stageFlag[i];
            }
            delta[i] = dist;
            return;
        }

        if (!fromPlayer[i] && (mt == 46 || mt == 47)) {
            int dist = delta[i];
            x[i] = x0[i] + (int) ((((long) c * (long) dist)) >> 3);
            y[i] = y0[i] + (int) ((((long) s * (long) dist)) >> 3);
            t[i] = tt + 1;

            dist += stageFlag[i];
            delta[i] = dist;

            if (mt == 46) {
                if (y[i] > 7929856) {
                    deactivateNoLock(i);
                }
            } else if (y[i] < 7929856) {
                deactivateNoLock(i);
            }
            return;
        }

        if (!fromPlayer[i] && (mt == 48 || mt == 49)) {
            int dist = delta[i];
            x[i] = x0[i] + (int) ((((long) c * (long) dist)) >> 3);
            y[i] = y0[i] + (int) ((((long) s * (long) dist)) >> 3);
            t[i] = tt + 1;

            dist += stageFlag[i];
            delta[i] = dist;

            if (mt == 48) {
                if (x[i] > 6356992) {
                    deactivateNoLock(i);
                }
            } else if (x[i] < 6356992) {
                deactivateNoLock(i);
            }
            return;
        }

        if (!fromPlayer[i] && (mt == 50 || mt == 51)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            dist += stageFlag[i];
            delta[i] = dist;

            int angleDelta = (mt == 50) ? -3 : 3;
            if (tt == 0) {
                for (int a = 0; a < 360; a += 90) {
                    spawnEnemyChild52(96, ang + a, 16, i, angleDelta);
                }
            }

            if (tt == 12) {
                angleDeg[i] = arcTan2Deg(enemyAimTargetY - y[i], enemyAimTargetX - x[i]) % 360;
                x0[i] = x[i];
                y0[i] = y[i];
                delta[i] = 0;
                stageFlag[i] >>= 1;
            }

            if (x[i] > -4587520 && y[i] > -4063232 && x[i] < 17301504 && y[i] < 19922944) {
                keepAlive[i] = 10;
            }

            if (((tt + 1) & 0xF) != 0 || y[i] >= 15335424) {
                return;
            }

            int spawnAng = ang + 180 + (tt << 2);
            if (mt == 50) {
                spawn(84, 54, spawnAng, 0, x[i], y[i]);
            } else {
                spawn(84, 55, spawnAng, 0, x[i], y[i]);
            }
            return;
        }

        if (!fromPlayer[i] && mt == 52) {
            int parent = tag[i];
            if (parent < 0 || parent >= max || !active[parent]) {
                kill(i);
                return;
            }

            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x[parent] + Fixed.mul(c, distFixed);
            y[i] = y[parent] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            dist += stageFlag[i];
            delta[i] = dist;

            angleDeg[i] = normalizeDeg(ang + savedDelta[i]);
            if (tt > 5) {
                stageFlag[i] = 0;
            }

            if (x[i] > -5242880 && y[i] > -4718592 && x[i] < 17956864 && y[i] < 20578304) {
                keepAlive[i] = 30;
            }
            return;
        }

        if (!fromPlayer[i] && mt == 53) {
            int parent = tag[i];
            if (parent < 0 || parent >= max || !active[parent]) {
                deactivateNoLock(i);
                return;
            }

            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x[parent] + Fixed.mul(c, distFixed);
            y[i] = y[parent] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            dist += stageFlag[i];
            delta[i] = dist;

            if (tt > 12) {
                stageFlag[i] = 0;
            }
            return;
        }

        if (!fromPlayer[i] && (mt == 54 || mt == 55)) {
            t[i] = tt + 1;
            if (tt > 50) {
                x0[i] = x[i];
                y0[i] = y[i];
                moveType[i] = (mt == 54) ? 56 : 57;
                stageFlag[i] = 0;
                delta[i] = 0;
                t[i] = 0;
            }
            return;
        }

        if (!fromPlayer[i] && (mt == 56 || mt == 57)) {
            int a = (tt >> 2) * 30;
            int spawnAng = ang + a;
            if (mt == 56) {
                spawnAng = ang - a;
            }

            if ((tt & 0x3) == 0) {
                spawn(84, 59, spawnAng, 0, x[i], y[i]);
            }

            if ((tt >> 2) >= 11) {
                deactivateNoLock(i);
                return;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 58) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i];
            dist += spd;
            delta[i] = dist;

            int bid = bulletId[i];
            if (enemyLevel == 0) {
                if (tt == 3) {
                    spawn(bid, 0, ang + 90, spd, x[i], y[i]);
                    spawn(bid, 0, ang - 90, spd, x[i], y[i]);
                }
                return;
            }

            if (tt != 4 && tt != 9) {
                return;
            }
            switch (enemyLevel) {
                case 1:
                    spawn(bid, 0, ang + 90, spd, x[i], y[i]);
                    spawn(bid, 0, ang - 90, spd, x[i], y[i]);
                    break;
                case 2:
                    spawn(bid, 0, ang + 90 + 30, spd, x[i], y[i]);
                    spawn(bid, 0, ang - 90 + 30, spd, x[i], y[i]);
                    spawn(bid, 0, ang + 90 - 30, spd, x[i], y[i]);
                    spawn(bid, 0, ang - 90 - 30, spd, x[i], y[i]);
                    break;
                case 3:
                default:
                    spawn(bid, 0, ang + 90, spd, x[i], y[i]);
                    spawn(bid, 0, ang - 90, spd, x[i], y[i]);
                    spawn(bid, 0, ang + 90 + 30, spd, x[i], y[i]);
                    spawn(bid, 0, ang - 90 + 30, spd, x[i], y[i]);
                    spawn(bid, 0, ang + 90 - 30, spd, x[i], y[i]);
                    spawn(bid, 0, ang - 90 - 30, spd, x[i], y[i]);
                    break;
            }
            return;
        }

        if (!fromPlayer[i] && mt == 60) {
            int spd = stageFlag[i];
            if (spd > 0) {
                int dist = delta[i];
                int distFixed = dist << 13;
                x[i] = x0[i] + Fixed.mul(c, distFixed);
                y[i] = y0[i] + Fixed.mul(s, distFixed);

                dist += spd;
                delta[i] = dist;

                spd -= 8;
                if (spd <= 0) {
                    spd = -20;
                    angleDeg[i] = normalizeDeg(ang + 180);
                }
                stageFlag[i] = spd;
            }

            if (tt > 100) {
                bulletId[i] = 81;
                moveType[i] = 68;
                x0[i] = x[i];
                y0[i] = y[i];
                delta[i] = 0;
                t[i] = 0;
                return;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && (mt == 63 || mt == 64)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            int newAng = ang;
            if (mt == 63) {
                newAng = ang + 1;
            } else {
                newAng = ang - 1;
            }
            angleDeg[i] = normalizeDeg(newAng);

            int spd = stageFlag[i];
            if (tt < 20) {
                spd -= 6;
            } else if (tt < 40) {
                spd += 6;
            }
            stageFlag[i] = spd;

            if ((tt & 0x3) == 0 && tt < 60 && x[i] > 0 && y[i] > 524288 && x[i] < 12713984 && y[i] < 15335424) {
                int bid = 603;
                int a = angleDeg[i];
                spawn(bid, 0, a + 180, 30, x[i], y[i]);
                spawn(bid, 0, a + 180 + 90, 30, x[i], y[i]);
                spawn(bid, 0, a + 180 - 90, 30, x[i], y[i]);
            }

            dist += spd;
            delta[i] = dist;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 65) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;
            t[i] = tt + 1;

            if (x[i] < 0 || y[i] < 524288 || x[i] > 12713984 || y[i] > 15335424) {
                deactivateNoLock(i);
                int dir = 0;
                if (x[i] < 0) {
                    dir = 90;
                }
                if (y[i] < 524288) {
                    dir = 180;
                }
                if (x[i] > 12713984) {
                    dir = 270;
                }
                if (y[i] > 15335424) {
                    dir = 0;
                }

                for (int n = 0; n < 5; n++) {
                    int bid = 81 + ((i + n) % 6);
                    int a = dir + 270 + (n - 2) * 30;
                    spawn(bid, 0, a, 30, x[i], y[i]);
                }
            }
            return;
        }

        if (!fromPlayer[i] && mt == 66) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            int div = 4;
            if (enemyLevel <= 1) {
                div++;
            }
            if (enemyLevel <= 0) {
                div++;
            }

            if (div > 0 && (tt % div) == 0) {
                spawn(83, 67, ang, stageFlag[i], x[i], y[i]);
            }

            dist += stageFlag[i];
            delta[i] = dist;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 67) {
            if (tt > 70) {
                int a = ang;
                if (a < 180) {
                    a += 270;
                } else {
                    a += 90;
                }
                angleDeg[i] = normalizeDeg(a);

                bulletId[i] = 621;
                moveType[i] = 28;
                stageFlag[i] = 0;
                delta[i] = 0;
                x0[i] = x[i];
                y0[i] = y[i];
                t[i] = 0;
                return;
            }
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 68) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            int spd = stageFlag[i];
            dist += spd;
            delta[i] = dist;

            spd += 8;
            if (spd > 56) {
                spd = 56;
            }
            stageFlag[i] = spd;

            if (tt > 20) {
                bulletId[i] = 462;
                moveType[i] = 32;
                t[i] = 0;
                return;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 32) {
            bulletId[i] = 462 + tt;
            if (tt >= 4) {
                deactivateNoLock(i);
                return;
            }
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 69) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;
            t[i] = tt + 1;

            if (tt == 12) {
                int bid = bulletId[i];
                for (int a = 0; a < 360; a += 60) {
                    spawn(bid, 0, a + 270, 24, x[i], y[i]);
                }
                deactivateNoLock(i);
            }
            return;
        }

        if (!fromPlayer[i] && mt == 70) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;
            t[i] = tt + 1;

            if (tt == 10) {
                int bid = bulletId[i];
                for (int a = 0; a < 360; a += 60) {
                    spawn(bid, 69, a + 270, 24, x[i], y[i]);
                }
                deactivateNoLock(i);
            }
            return;
        }

        if (!fromPlayer[i] && (mt == 71 || mt == 72 || mt == 73)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;

            if (tt == 20) {
                int bx = x[i];
                int by = y[i];

                if (mt == 71) {
                    for (int a = 0; a < 360; a += 15) {
                        spawn(86, 0, ang + a, 24, bx, by);
                    }
                } else if (mt == 72) {
                    for (int a = 0; a < 360; a += 30) {
                        spawn(86, 0, ang + a, 24, bx, by);
                        spawn(86, 0, ang + a + 15, 20, bx, by);
                        spawn(86, 0, ang + a, 28, bx, by);
                        spawn(86, 0, ang + a + 15, 24, bx, by);
                    }
                } else {
                    for (int a = 0; a < 360; a += 45) {
                        spawn(86, 0, ang + a, 24, bx, by);
                        spawn(86, 0, ang + a, 27, bx, by);
                        spawn(86, 0, ang + a, 30, bx, by);

                        spawn(86, 0, ang + a + 15, 24, bx, by);
                        spawn(86, 0, ang + a + 15, 27, bx, by);
                        spawn(86, 0, ang + a + 15, 30, bx, by);

                        spawn(86, 0, ang + a + 30, 24, bx, by);
                        spawn(86, 0, ang + a + 30, 27, bx, by);
                        spawn(86, 0, ang + a + 30, 30, bx, by);
                    }
                }

                int aim = arcTan2Deg(enemyAimTargetY - by, enemyAimTargetX - bx);
                for (int n = 0; n < 13; n++) {
                    int spd = 16 + (n << 3);
                    spawn(603, 74, aim, spd, bx, by);
                    spawn(603, 74, aim + 180, spd, bx, by);

                    if (mt == 72) {
                        spawn(603, 74, aim + 10, spd, bx, by);
                        spawn(603, 74, aim + 180 + 10, spd, bx, by);
                        spawn(603, 74, aim - 10, spd, bx, by);
                        spawn(603, 74, aim + 180 - 10, spd, bx, by);
                    } else if (mt == 73) {
                        spawn(603, 74, aim + 20, spd, bx, by);
                        spawn(603, 74, aim + 180 + 20, spd, bx, by);
                        spawn(603, 74, aim - 20, spd, bx, by);
                        spawn(603, 74, aim + 180 - 20, spd, bx, by);
                    }
                }

                int offAng1 = normalizeDeg(ang + 90);
                int se = (int) (((long) Trig.cos(offAng1) * 50L) >> 3);
                int x1 = bx + se;
                int y1 = by + se;

                int aim1 = arcTan2Deg(enemyAimTargetY - y1, enemyAimTargetX - x1);
                for (int n = 0; n < 13; n++) {
                    int spd = 16 + (n << 3);
                    spawn(603, 74, aim1, spd, x1, y1);
                    spawn(603, 74, aim1 + 180, spd, x1, y1);
                }

                int offAng2 = normalizeDeg(ang + 270);
                se = (int) (((long) Trig.cos(offAng2) * 50L) >> 3);
                int x2 = bx + se;
                int y2 = by + se;

                int aim2 = arcTan2Deg(enemyAimTargetY - y2, enemyAimTargetX - x2);
                for (int n = 0; n < 13; n++) {
                    int spd = 16 + (n << 3);
                    spawn(603, 74, aim2, spd, x2, y2);
                    spawn(603, 74, aim2 + 180, spd, x2, y2);
                }

                deactivateNoLock(i);
                return;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 74) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;

            int spd = stageFlag[i] - 8;
            if (spd < 16) {
                spd = 16;
            }
            stageFlag[i] = spd;

            boolean bounced = false;
            int n;
            int a = ang;
            if (x[i] < 0) {
                n = (a + 90) % 360 - 270;
                a = 90 - n + 270;
                bounced = true;
            }
            if (x[i] > 12713984) {
                n = (a + 90) % 360 - 90;
                a = 270 - n + 270;
                bounced = true;
            }
            if (y[i] < 524288) {
                n = (a + 90) % 360 - 0;
                a = 180 - n + 270;
                bounced = true;
            }
            if (y[i] > 15335424) {
                n = (a + 90) % 360 - 180;
                a = 0 - n + 270;
                bounced = true;
            }

            if (bounced) {
                int jitter = ((tt << 3) + (i << 6)) % 160;
                jitter -= 80;

                moveType[i] = 0;
                tag[i] = 1;
                angleDeg[i] = normalizeDeg(a + 360 + jitter);
                delta[i] = 0;
                bulletId[i] = 81;
                x0[i] = x[i];
                y0[i] = y[i];
                keepAlive[i] = 5;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 75) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;
            keepAlive[i] = 1;

            if (tt == 78) {
                moveType[i] = 0;
                tag[i] = 1;
                angleDeg[i] = normalizeDeg(ang + 180);
                delta[i] = 0;
                bulletId[i] = 81;
                animTick[i] = 0;
                x0[i] = x[i];
                y0[i] = y[i];
                keepAlive[i] = 50;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 76) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);

            int vy = s >> 3;
            int yInt = BulletSystem.c(y0[i] >> 16, vy, stageFlag[i], tt);
            y[i] = yInt << 16;

            dist += stageFlag[i];
            delta[i] = dist;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 77) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;

            if ((tt & 0x1) == 0) {
                if (tt < 10) {
                    int want = arcTan2Deg(enemyAimTargetY - y[i], enemyAimTargetX - x[i]) % 360;
                    int diff = want - ang;
                    if (diff > 180) {
                        diff -= 360;
                    }
                    if (diff < -180) {
                        diff += 360;
                    }
                    angleDeg[i] = normalizeDeg(ang + (diff * 3) / 10);

                    x0[i] = x[i];
                    y0[i] = y[i];
                    delta[i] = 0;
                }

                stageFlag[i] -= 1;
                if (stageFlag[i] <= 0) {
                    kill(i);
                }
            }

            if (x[i] > -4587520 && y[i] > -4063232 && x[i] < 17301504 && y[i] < 19922944) {
                keepAlive[i] = 10;
            }

            if ((tt & 0x7) == 0 && x[i] > 0 && y[i] > 524288 && x[i] < 12713984 && y[i] < 15335424) {
                int a = angleDeg[i];
                for (int n = 0; n < 360; n += 45) {
                    spawn(84, 78, a + n, -30, x[i], y[i]);
                }
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 78) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            if (tt > 50) {
                dist += stageFlag[i];
                delta[i] = dist;
                if (stageFlag[i] < 30) {
                    stageFlag[i] += 3;
                }
            } else if (tt > 48) {
                dist += stageFlag[i] >> 1;
                delta[i] = dist;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && (mt == 79 || mt == 80)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;
            keepAlive[i] = 1;

            if (dist > 1000) {
                moveType[i] = 66;
                // DoJa effectively applies +150 for both 79 and 80 here.
                angleDeg[i] = normalizeDeg(ang + 150);
                delta[i] = 0;
                animTick[i] = 0;
                x0[i] = x[i];
                y0[i] = y[i];
                keepAlive[i] = 50;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && (mt == 81 || mt == 82)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            if ((tt & 0x3) == 0) {
                int a = ang + ((mt == 81) ? 270 : 90);
                spawn(521, 59, a, 0, x[i], y[i]);
            }

            dist += stageFlag[i];
            delta[i] = dist;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 83) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            int accel = stageFlag[i];
            if ((tt & 0x1) == 0) {
                accel -= 1;
                stageFlag[i] = accel;
            }

            if (accel > 0) {
                dist += accel;
            }

            if (accel < -5) {
                dist += accel;
                delta[i] = dist;

                int aim = arcTan2Deg(enemyAimTargetY - y[i], enemyAimTargetX - x[i]) % 360;
                for (int n = 0; n < 5; n++) {
                    int spd = 52 - (n << 2);
                    spawnEnemyStopTurnAccel(537, 7, aim, spd, x[i], y[i]);
                }

                deactivateNoLock(i);
                return;
            }

            delta[i] = dist;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 84) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;

            if (x[i] < 0 || x[i] > 12713984) {
                int dir = 0;
                if (x[i] < 0) {
                    dir = 90;
                }
                if (x[i] > 12713984) {
                    dir = 270;
                }
                spawn(521, 0, dir + 270, stageFlag[i], x[i], y[i]);
                deactivateNoLock(i);
                return;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 85) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            if (x[i] < 0 || x[i] > 12713984 || y[i] > 15335424) {
                angleDeg[i] = arcTan2Deg(enemyAimTargetY - y[i], enemyAimTargetX - x[i]) % 360;
                x0[i] = x[i];
                y0[i] = y[i];
                dist = 0;
                keepAlive[i] = 10;

                dist += stageFlag[i];
                delta[i] = dist;
                t[i] = 1;
                return;
            }

            dist += stageFlag[i];
            delta[i] = dist;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && (mt == 86 || mt == 87 || mt == 88)) {
            int lockIdx = mt - 86;
            if (lockIdx >= 0 && lockIdx < enemyBulletLock.length && enemyBulletLock[lockIdx] != 0) {
                moveType[i] = 0;
                tag[i] = 1;
                delta[i] = 0;
                x0[i] = x[i];
                y0[i] = y[i];
            }
            keepAlive[i] = 20;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && (mt == 89 || mt == 90)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;

            int rot = 3;
            if (mt == 89) {
                rot = -3;
            }
            if (tt == 0) {
                for (int a = 0; a < 360; a += 90) {
                    spawnEnemyChild91(93, ang + a, 16, i, rot);
                }
            }

            if (tt == 12) {
                x0[i] = x[i];
                y0[i] = y[i];
                delta[i] = 0;
                stageFlag[i] >>= 1;
            }

            if (x[i] > -4587520 && y[i] > -4063232 && x[i] < 17301504 && y[i] < 19922944) {
                keepAlive[i] = 10;
            }

            if (((tt + 1) & 0x7) == 0 && y[i] < 15335424) {
                int a = normalizeDeg(ang - 45);
                spawn(81, 0, a, 24, x[i], y[i]);
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 91) {
            int parent = tag[i];
            if (parent < 0 || parent >= max || !active[parent]) {
                kill(i);
                return;
            }

            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x[parent] + Fixed.mul(c, distFixed);
            y[i] = y[parent] + Fixed.mul(s, distFixed);

            dist += stageFlag[i];
            delta[i] = dist;

            angleDeg[i] = normalizeDeg(ang + savedDelta[i]);
            if (tt > 5) {
                stageFlag[i] = 0;
            }

            if (x[i] > -5242880 && y[i] > -4718592 && x[i] < 17956864 && y[i] < 20578304) {
                keepAlive[i] = 30;
            }

            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && (mt == 1 || mt == 28)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i];
            dist += spd;

            int accel = (mt == 1) ? 8 : 2;
            spd += accel;
            if (spd > 56) {
                spd = 56;
            }

            delta[i] = dist;
            stageFlag[i] = spd;
            return;
        }

        if (fromPlayer[i] && mt == 1) {
            int distFixed = delta[i] << 16;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            x0[i] = x[i];
            y0[i] = y[i];
            t[i] = tt + 1;

            int tgt = tag[i];
            if (tgt == -1) {
                if (enemies != null) {
                    tgt = enemies.findNearestTargetByX(x[i]);
                }
                tag[i] = tgt;
            }

            if (tgt != -1) {
                if (enemies == null || !enemies.isTargetable(tgt)) {
                    tag[i] = -1;
                    moveType[i] = 0;
                    t[i] = 0;
                    return;
                }

                int ex = enemies.getXFixed(tgt);
                int ey = enemies.getYFixed(tgt);
                int want = arcTan2Deg(ey - y[i], ex - x[i]);
                int diff = want - ang;
                if (diff > 180) {
                    diff -= 360;
                }
                if (diff < -180) {
                    diff += 360;
                }
                ang = normalizeDeg(ang + ((diff << 2) / 10));
                angleDeg[i] = ang;

                if (bulletId[i] != 48) {
                    int d0 = ang % 360;
                    int d1 = 15 - (((d0 / 22) + 11) & 0xF);
                    bulletId[i] = 32 + d1;
                }
            }
            return;
        }

        if (mt == 2 || mt == 3) {
            x[i] = x0[i];
            y[i] = y0[i];
            t[i] = tt + 1;

            if (fromPlayer[i] && mt == 2 && (bulletId[i] == 103 || bulletId[i] == 104)) {
                if (stageFlag[i] < 0) {
                    active[i] = false;
                    return;
                }
                if (stageFlag[i] < 5) {
                    bulletId[i] = 103;
                } else {
                    bulletId[i] = 104;
                }
                stageFlag[i] -= 1;
                return;
            }

            if (fromPlayer[i] && mt == 3 && (bulletId[i] >= 105 && bulletId[i] <= 110)) {
                if (stageFlag[i] < 0) {
                    active[i] = false;
                    return;
                }
                if (stageFlag[i] < 2) {
                    bulletId[i] = 105;
                } else if (stageFlag[i] < 4) {
                    bulletId[i] = 106;
                } else if (stageFlag[i] < 6) {
                    bulletId[i] = 107;
                } else if (stageFlag[i] < 8) {
                    bulletId[i] = 108;
                } else {
                    bulletId[i] = 109 + ((animTick[i] >> 1) & 1);
                }
                stageFlag[i] -= 1;
                return;
            }

            if (stageFlag[i] > 0) {
                stageFlag[i] -= 1;
            } else {
                active[i] = false;
            }
            return;
        }

        if (!fromPlayer[i] && (mt == 10 || mt == 11)) {
            int dist = stageFlag[i];
            int distFixed = dist << 13;
            int spd = delta[i];
            if (spd > 0) {
                x[i] = x0[i] + Fixed.mul(c, distFixed);
                y[i] = y0[i] + Fixed.mul(s, distFixed);
                stageFlag[i] = dist + spd;
                angleDeg[i] = normalizeDeg(ang + ((mt == 10) ? 1 : -1));
            }
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 35) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i];
            dist += spd;
            delta[i] = dist;

            if (tt == 0) {
                int bid = bulletId[i];
                spawnEnemyChild(bid, 36, ang, 8, i);
                spawnEnemyChild(bid, 36, ang, 12, i);
                spawnEnemyChild(bid, 36, ang, 20, i);
                spawnEnemyChild(bid, 36, ang, 24, i);
                spawnEnemyChild(bid, 36, ang, 28, i);
                spawnEnemyChild(bid, 36, ang, 32, i);
                spawnEnemyChild(bid, 36, ang + 45, 16, i);
                spawnEnemyChild(bid, 36, ang + 30, 16, i);
                spawnEnemyChild(bid, 36, ang + 15, 16, i);
                spawnEnemyChild(bid, 36, ang, 16, i);
                spawnEnemyChild(bid, 36, ang - 15, 16, i);
                spawnEnemyChild(bid, 36, ang - 30, 16, i);
                spawnEnemyChild(bid, 36, ang - 45, 16, i);
            }

            if (((tt + 1) & 0xF) == 0 && y[i] < 13369344) {
                int div;
                switch (enemyLevel) {
                    case 0:
                        div = 8;
                        break;
                    case 1:
                        div = 15;
                        break;
                    case 2:
                        div = 20;
                        break;
                    case 3:
                    default:
                        div = 24;
                        break;
                }
                for (int a = 0; a < 360; a += 360 / div) {
                    spawn(81, 0, ang + a, 20, x[i], y[i]);
                }
            }
            return;
        }

        if (!fromPlayer[i] && mt == 36) {
            int parent = tag[i];
            if (parent < 0 || parent >= max || !active[parent]) {
                moveType[i] = 0;
                stageFlag[i] = 0;
                delta[i] = 32;
                x0[i] = x[i];
                y0[i] = y[i];
                t[i] = 0;
                return;
            }

            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x[parent] + Fixed.mul(c, distFixed);
            y[i] = y[parent] + Fixed.mul(s, distFixed);

            int spd = stageFlag[i];
            dist += spd;
            delta[i] = dist;
            t[i] = tt + 1;

            if (tt > 5) {
                stageFlag[i] = 0;
            }
            return;
        }

        if (!fromPlayer[i] && mt == 37) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i];
            dist += spd;
            delta[i] = dist;

            boolean bounced = false;
            int n72;
            if (x[i] < 0) {
                n72 = (ang + 90) % 360 - 270;
                angleDeg[i] = normalizeDeg(90 - n72 + 270);
                bounced = true;
            }
            if (x[i] > 12713984) {
                n72 = (ang + 90) % 360 - 90;
                angleDeg[i] = normalizeDeg(270 - n72 + 270);
                bounced = true;
            }
            if (y[i] < 524288) {
                n72 = (ang + 90) % 360 - 0;
                angleDeg[i] = normalizeDeg(180 - n72 + 270);
                bounced = true;
            }
            if (bounced) {
                moveType[i] = 0;
                delta[i] = spd;
                stageFlag[i] = 0;
                x0[i] = x[i];
                y0[i] = y[i];
                t[i] = 0;
                int bid = bulletId[i];
                if (bid >= 521 && bid < 585) {
                    bulletId[i] = 521;
                }
            }
            return;
        }

        if (!fromPlayer[i] && mt == 38) {
            int dist = delta[i];
            int spd = stageFlag[i];
            if (dist > 100) {
                angleDeg[i] = 90;
                int step = dist - 100;
                int distFixed = step << 13;
                x[i] = x0[i] + Fixed.mul(Trig.cos(90), distFixed);
                y[i] = y0[i] + Fixed.mul(Trig.sin(90), distFixed);
                dist += spd;
                spd += 1;
            } else {
                int scale;
                if (dist > 50) {
                    scale = 0;
                } else {
                    scale = (50 - dist) << 5;
                }
                int distFixed = scale << 13;
                x[i] = x0[i] + Fixed.mul(c, distFixed);
                y[i] = y0[i] + Fixed.mul(s, distFixed);
                dist += 1;
                spd = 0;
                if (dist == 50) {
                    x0[i] = x[i];
                    y0[i] = y[i];
                }
            }
            delta[i] = dist;
            stageFlag[i] = spd;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 39) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;
            dist += stageFlag[i];
            delta[i] = dist;
            return;
        }

        if (!fromPlayer[i] && mt == 42) {
            t[i] = tt + 1;
            return;
        }

        if (fromPlayer[i] && (mt == 9 || mt == 10 || mt == 11)) {
            int dist = delta[i] * tt;
            int distFixed = dist << 16;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int newT = t[i];
            if ((mt == 9 || mt == 10) && (newT & 1) == 0) {
                delta[i] += 1;
            }
            if (mt == 9 && (newT % 6) == 5 && power[i] > 1) {
                power[i] -= 1;
            }
            if (mt == 11 && newT > 3) {
                active[i] = false;
            }
            return;
        }

        if (!fromPlayer[i] && (mt == 12 || mt == 13) && savedDelta[i] == 0) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            int tick = tag[i];
            int spd = stageFlag[i];
            if (mt == 12) {
                if (tick > 10) {
                    dist += spd;
                } else if (tick > 8) {
                    dist += spd >> 1;
                }
            } else {
                if (tick > 50) {
                    dist += spd;
                } else if (tick > 48) {
                    dist += spd >> 1;
                }
            }

            delta[i] = dist;
            tag[i] = tick + 1;
            t[i] = tt + 1;
            return;
        }

        if (mt == 12) {
            int distFixed = delta[i] << 16;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int dist = delta[i];
            int accel = stageFlag[i];
            dist += accel;
            delta[i] = dist;

            int dec = savedDelta[i];
            if (dec <= 0) {
                dec = 1;
            }
            accel -= dec;
            int minAccel = tag[i];
            if (accel < minAccel) {
                accel = minAccel;
            }
            stageFlag[i] = accel;
            return;
        }

        if (!fromPlayer[i] && (mt == 20 || mt == 59 || mt == 61)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i];
            dist += spd;
            delta[i] = dist;

            if (mt == 20) {
                tag[i] += 1;
                if (tag[i] > 3) {
                    spd -= 8;
                    if (spd < 32) {
                        spd = 32;
                    }
                }
            } else if (mt == 59) {
                spd += 1;
                if (spd > 40) {
                    spd = 40;
                }
            } else {
                spd -= 8;
                if (spd < 16) {
                    spd = 16;
                }
            }
            stageFlag[i] = spd;
            return;
        }

        if (!fromPlayer[i] && (mt == 14 || mt == 15 || mt == 16 || mt == 17 || mt == 18 || mt == 19 || mt == 62)) {
            int step = delta[i];
            int spd = stageFlag[i];
            int distFixed = (int) (((long) step * (long) spd) << 13);
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            if (mt == 18) {
                angleDeg[i] = normalizeDeg(ang - 1);
            } else if (mt == 19) {
                angleDeg[i] = normalizeDeg(ang + 1);
            }

            step += 1;
            delta[i] = step;
            t[i] = tt + 1;

            if (mt == 14 || mt == 15 || mt == 16) {
                if (step % 6 == 0 && y[i] < 14680064) {
                    if (mt != 15) {
                        spawn(255, 0, ang + 180, 40, x[i], y[i]);
                    }
                    if (mt != 14) {
                        spawn(255, 0, ang + 180 + 30, 40, x[i], y[i]);
                        spawn(255, 0, ang + 180 - 30, 40, x[i], y[i]);
                    }
                }
                return;
            }

            if (mt == 17) {
                if (step % 6 == 0) {
                    spawn(90, 13, ang, 24, x[i], y[i]);
                }
                return;
            }

            if (mt == 18 || mt == 19) {
                if (step % 15 == 0) {
                    int aim = arcTan2Deg(enemyAimTargetY - y[i], enemyAimTargetX - x[i]);
                    spawn(239, 13, aim, 24, x[i], y[i]);
                    spawn(239, 13, aim + 180, 24, x[i], y[i]);
                }
                return;
            }

            if (mt == 62) {
                if ((step & 0x7) == 0 && step <= 16) {
                    int div = 8;
                    for (int a = 0; a < 360; a += 360 / div) {
                        int angOut = ang + a;
                        int se = (int) (((long) Trig.cos(angOut) * 30L) >> 3);
                        int te = (int) (((long) Trig.sin(angOut) * 30L) >> 3);
                        spawn(639, 12, angOut, 24, x[i] + se, y[i] + te);
                    }
                }
                return;
            }
        }

        if (!fromPlayer[i] && (mt == 23 || mt == 24 || mt == 25)) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i];
            dist += spd;
            delta[i] = dist;

            if (mt == 25) {
                spd -= 8;
                stageFlag[i] = spd;
                if (spd == 0) {
                    moveType[i] = 10;
                    delta[i] = 0;
                    stageFlag[i] = 0;
                    x0[i] = x[i];
                    y0[i] = y[i];
                    t[i] = 0;
                }
                return;
            }

            boolean out = x[i] < 0 || y[i] < 524288 || x[i] > 12713984 || y[i] > 15335424;
            if (!out) {
                return;
            }

            if (mt == 23) {
                moveType[i] = 0;
                angleDeg[i] = normalizeDeg(ang + 180);
                bulletId[i] = 239;
                delta[i] = spd;
                stageFlag[i] = 0;
                tag[i] = 0;
                x0[i] = x[i];
                y0[i] = y[i];
                t[i] = 0;
                return;
            }

            active[i] = false;
            int base = 0;
            if (x[i] < 0) {
                base = 90;
            }
            if (y[i] < 524288) {
                base = 180;
            }
            if (x[i] > 12713984) {
                base = 270;
            }
            if (y[i] > 15335424) {
                base = 0;
            }
            int count;
            switch (enemyLevel) {
                case 0:
                    count = 4;
                    break;
                case 1:
                    count = 5;
                    break;
                case 2:
                    count = 6;
                    break;
                case 3:
                default:
                    count = 7;
                    break;
            }
            for (int n = 0; n < count; n++) {
                spawnEnemyAccel40(83, base + 270, n << 3, x[i], y[i]);
            }
            return;
        }

        if (!fromPlayer[i] && mt == 29) {
            int tick = delta[i];
            if (tick == 0) {
                int div;
                switch (enemyLevel) {
                    case 0:
                        div = 6;
                        break;
                    case 1:
                        div = 8;
                        break;
                    case 2:
                        div = 10;
                        break;
                    case 3:
                    default:
                        div = 12;
                        break;
                }

                int dir = 1;
                if (x[i] < 6356992) {
                    dir = -1;
                }
                for (int a = 0; a < 360; a += 360 / div) {
                    spawnEnemyChild(83, 30, a, dir, i);
                }
            }

            if (tick > 12) {
                moveType[i] = 0;
                tag[i] = 1;
                tick = -1;
            }
            tick += 1;
            delta[i] = tick;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 30) {
            int parent = tag[i];
            if (parent < 0 || parent >= max || !active[parent]) {
                x0[i] = x[i];
                y0[i] = y[i];
                moveType[i] = 0;
                tag[i] = 1;
                stageFlag[i] = 32;
                angleDeg[i] = normalizeDeg(450);
                delta[i] = 0;
                t[i] = tt + 1;
                return;
            }

            int dist = delta[i];
            int distFixedX = dist << 13;
            int distFixedY = dist << 12;
            x[i] = x[parent] + Fixed.mul(c, distFixedX);
            y[i] = y[parent] + Fixed.mul(s, distFixedY);

            angleDeg[i] = normalizeDeg(ang + stageFlag[i]);

            if (dist < 160) {
                dist += 16;
            } else {
                dist += 8;
            }
            delta[i] = dist;
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 31) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i];
            if (dist < 320) {
                dist += spd;
                delta[i] = dist;
            } else {
                active[i] = false;
                int aim = arcTan2Deg(enemyAimTargetY - y[i], enemyAimTargetX - x[i]);
                int div;
                switch (enemyLevel) {
                    case 0:
                        div = 12;
                        break;
                    case 1:
                        div = 15;
                        break;
                    case 2:
                        div = 20;
                        break;
                    case 3:
                    default:
                        div = 24;
                        break;
                }
                for (int a = 0; a < 360; a += 360 / div) {
                    int angBase = aim + a;
                    for (int n = 0; n < 5; n++) {
                        int speedParam = -24 - (n << 3);
                        spawnEnemyAccel(83, 1, angBase, speedParam, x[i], y[i]);
                    }
                }
            }
            stageFlag[i] = spd + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 33) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int spd = stageFlag[i] - 8;
            dist += stageFlag[i];
            delta[i] = dist;
            stageFlag[i] = spd;
            if (spd == 0) {
                moveType[i] = 11;
                delta[i] = 0;
                stageFlag[i] = 0;
                x0[i] = x[i];
                y0[i] = y[i];
                t[i] = 0;
            }
            return;
        }

        if (!fromPlayer[i] && mt == 34) {
            int dist = delta[i];
            int spd = stageFlag[i];
            if (tt < 15) {
                int distFixed = dist << 13;
                x[i] = x0[i] + Fixed.mul(c, distFixed);
                y[i] = y0[i] + Fixed.mul(s, distFixed);
                dist += spd;
            } else if (tt == 15) {
                x0[i] = x[i];
                y0[i] = y[i];
                dist = 0;
                spd = 0;
            } else {
                int distFixedX = dist << 11;
                int distFixedY = dist << 13;
                x[i] = x0[i] + Fixed.mul(c, distFixedX);
                y[i] = y0[i] + Fixed.mul(s, distFixedY);
                dist += spd;
                spd += 2;

                int n68 = (ang + 90) % 360;
                if (n68 > 180) {
                    n68 -= 2;
                }
                if (n68 < 180) {
                    n68 += 2;
                }
                angleDeg[i] = normalizeDeg(n68 + 270);
            }
            spd += 1;
            delta[i] = dist;
            stageFlag[i] = spd;
            t[i] = tt + 1;
            return;
        }

        // Stage EXTRA fodder: spiral curve bullets (moveType 21/22).
        if (mt == 21 || mt == 22) {
            int dist = delta[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;

            int angDelta = (mt == 21) ? 6 : -6;
            angleDeg[i] = normalizeDeg(ang + angDelta);

            int spd = stageFlag[i];
            dist += spd;
            delta[i] = dist;

            spd -= 8;
            stageFlag[i] = spd;
            if (spd < -40) {
                moveType[i] = 0;
                tag[i] = 1;
            }
            return;
        }

        if (mt == 0 || mt == 8) {
            if (mt == 0 && tag[i] == 1) {
                int distFixed;
                if (!fromPlayer[i]) {
                    distFixed = delta[i] << 13;
                } else {
                    distFixed = delta[i] << 16;
                }
                x[i] = x0[i] + Fixed.mul(c, distFixed);
                y[i] = y0[i] + Fixed.mul(s, distFixed);
                t[i] = tt + 1;
                delta[i] += stageFlag[i];
                return;
            }

            int dist = delta[i] * tt;
            int distFixed;
            if (!fromPlayer[i]) {
                distFixed = dist << 13;
            } else {
                distFixed = dist << 16;
            }
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 4) {
            x[i] = x0[i];
            y[i] = y0[i];

            int div;
            int ringCount = 1;
            switch (enemyLevel) {
                case 0:
                    div = 8;
                    break;
                case 1:
                    div = 12;
                    break;
                case 2:
                    div = 16;
                    break;
                case 3:
                default:
                    div = 24;
                    ringCount = 3;
                    break;
            }

            int age = t[i];
            switch (age) {
                case 1: {
                    int ring = ringCount;
                    while (ring > 0) {
                        int speedParam = (4 + ring) << 3;
                        for (int a = 0; a < 360; a += 360 / div) {
                            spawnEnemyOrbitBullet(83, a, speedParam, i);
                        }
                        ring--;
                    }
                    break;
                }
                case 17: {
                    int ring = ringCount;
                    while (ring > 0) {
                        int speedParam = (-4 - ring) << 3;
                        for (int a = 0; a < 360; a += 360 / div) {
                            spawnEnemyOrbitBullet(83, a, speedParam, i);
                        }
                        ring--;
                    }
                    break;
                }
                case 18: {
                    angleDeg[i] = arcTan2Deg(enemyAimTargetY - y[i], enemyAimTargetX - x[i]) % 360;
                    moveType[i] = 0;
                    x0[i] = x[i];
                    y0[i] = y[i];
                    t[i] = 0;
                    return;
                }
            }

            t[i] = age + 1;
            return;
        }

        if (!fromPlayer[i] && mt == 5) {
            int parent = tag[i];
            if (parent >= 0 && parent < max && active[parent] && t[parent] < 60) {
                int dist = delta[i];
                int distFixed = dist << 13;
                x[i] = x[parent] + Fixed.mul(c, distFixed);
                y[i] = y[parent] + Fixed.mul(s, distFixed);
                angleDeg[i] = normalizeDeg(ang + stageFlag[i]);
                delta[i] = dist + 1;
                t[i] = tt + 1;
                return;
            }

            x0[i] = x[i];
            y0[i] = y[i];
            t[i] = 0;

            if (stageFlag[i] > 0) {
                moveType[i] = 22;
                stageFlag[i] += 24;
                delta[i] = 0;
            } else {
                bulletId[i] = 81;
                int spd = stageFlag[i] >> 1;
                if (spd < 0) {
                    angleDeg[i] = normalizeDeg(ang + 180);
                    spd = -spd;
                }
                tag[i] = 0;
                stageFlag[i] = 0;
                delta[i] = spd;
                moveType[i] = 0;
            }
            return;
        }

        if (mt == 4 || mt == 5) {
            int dist = t[i] * delta[i];
            int distFixed = dist << 16;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            if (mt == 4) {
                angleDeg[i] = normalizeDeg(ang + 7);
            } else {
                angleDeg[i] = normalizeDeg(ang + 353);
            }

            x0[i] = x[i];
            y0[i] = y[i];

            int sf = stageFlag[i];
            if (sf == 2 || sf == 5 || sf == 8) {
                bulletId[i] += 1;
            } else if (sf == 15) {
                if (fromPlayer[i]) {
                    int bid = bulletId[i];
                    if (bid >= 61 && bid <= 80) {
                        if (effectSpawner != null) {
                            effectSpawner.spawnEffect(19, x[i], y[i]);
                        } else {
                            spawnEffectTagged(457, 5, 10, x[i], y[i], 1);
                        }
                    }
                }
                active[i] = false;
                return;
            }
            stageFlag[i] = sf + 1;
            t[i] = 1;
            return;
        }

        if (mt == 6 || mt == 7) {
            int dist = t[i] * delta[i];
            int distFixed = dist << 16;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);

            if (mt == 6) {
                angleDeg[i] = normalizeDeg(ang + 8);
            } else {
                angleDeg[i] = normalizeDeg(ang + 352);
            }

            int sf = stageFlag[i];
            if (sf < 16) {
                t[i] += 1;
            } else if (sf < 28) {
                t[i] -= 1;
            } else if (sf > 32) {
                t[i] += 2;
            }
            stageFlag[i] = sf + 1;

            int newPower = 2 + t[i];
            if (newPower < 1) {
                newPower = 1;
            }
            power[i] = newPower;
            return;
        }

        if (mt == 41 && !fromPlayer[i]) {
            int dist = delta[i];
            int vel = stageFlag[i];
            int distFixed = dist << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            int newT = tt + 1;
            t[i] = newT;

            if (dist < 160) {
                dist += 96;
            } else {
                dist += vel;
                if (((newT & 1) == 0) || newT > 70) {
                    vel += 1;
                }
            }
            delta[i] = dist;
            stageFlag[i] = vel;
            return;
        }

        if (mt == 26) {
            int distFixed = tt << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            if (tt < 240) {
                t[i] = tt + 48;
            } else {
                t[i] = tt + delta[i];
            }
            return;
        }

        if (!fromPlayer[i] && mt == 27) {
            int distFixed = tt << 13;
            x[i] = x0[i] + Fixed.mul(c, distFixed);
            y[i] = y0[i] + Fixed.mul(s, distFixed);
            t[i] = tt + delta[i];

            if (stageFlag[i] == 0) {
                int spd = delta[i] - 8;
                if (spd < 0) {
                    spd = savedDelta[i];
                    x0[i] = x[i];
                    y0[i] = y[i];
                    t[i] = 0;
                    angleDeg[i] = globalDir;
                    stageFlag[i] = 1;
                }
                delta[i] = spd;
            }
            return;
        }

        int dist = tt << 16;
        x[i] = x0[i] + Fixed.mul(c, dist);
        y[i] = y0[i] + Fixed.mul(s, dist);

        if (mt == 27) {
            t[i] = tt + delta[i];
            if (stageFlag[i] == 0) {
                delta[i] -= 8;
                if (delta[i] < 0) {
                    delta[i] = savedDelta[i];
                    x0[i] = x[i];
                    y0[i] = y[i];
                    t[i] = 0;
                    angleDeg[i] = globalDir;
                    stageFlag[i] = 1;
                }
            }
            return;
        }

        t[i] = tt + delta[i];
    }

    private static int c(final int n, final int n2, final int n3, final int n4) {
        long term1 = (long) n2 * (long) n4;
        long term2 = ((long) n3 * (long) n4 * (long) n4) / 10L;
        term2 >>= 1;
        return (int) (term1 + term2 + n);
    }

    private static int normalizeDeg(int deg) {
        deg %= 360;
        if (deg < 0) {
            deg += 360;
        }
        return deg;
    }

    private static final byte[] DIR_MODE_TABLE = new byte[1024];
    static {
        // Directional sprites (mode 16).
        DIR_MODE_TABLE[32] = 16;
        DIR_MODE_TABLE[239] = 16;
        DIR_MODE_TABLE[255] = 16;
        DIR_MODE_TABLE[271] = 16;
        DIR_MODE_TABLE[287] = 16;
        DIR_MODE_TABLE[303] = 16;
        DIR_MODE_TABLE[319] = 16;
        DIR_MODE_TABLE[335] = 16;
        DIR_MODE_TABLE[521] = 16;
        DIR_MODE_TABLE[537] = 16;
        DIR_MODE_TABLE[553] = 16;
        DIR_MODE_TABLE[569] = 16;

        // Directional sprites (mode 36).
        DIR_MODE_TABLE[585] = 36;
        DIR_MODE_TABLE[603] = 36;
        DIR_MODE_TABLE[621] = 36;
        DIR_MODE_TABLE[639] = 36;
        DIR_MODE_TABLE[657] = 36;
        DIR_MODE_TABLE[675] = 36;
        DIR_MODE_TABLE[693] = 36;
    }

    private static int resolveDirectionMode(int bulletId) {
        if (bulletId < 0 || bulletId >= DIR_MODE_TABLE.length) {
            return 0;
        }
        return DIR_MODE_TABLE[bulletId];
    }

    private static int resolveDirectionFrame(int bulletId, int angleDeg, int speed) {
        int mode = resolveDirectionMode(bulletId);
        if (mode == 0) {
            return 0;
        }

        int ang = angleDeg;
        if (ang < 0 || ang >= 360) {
            ang = normalizeDeg(ang);
        }
        if (speed < 0) {
            ang += 180;
            if (ang >= 360) {
                ang -= 360;
            }
        }

        if (mode == 16) {
            return 15 - (((ang / 22) + 11) & 0xF);
        }

        int d = ang + 90;
        if (d >= 360) {
            d -= 360;
        }
        int d0 = 360 - d;
        return (d0 / 10) % 18;
    }

    private static int mul16(final int a, final int b) {
        return (int) ((a * (long) b) >> 16);
    }

    private static int div16(final int a, final int b) {
        if (b == 0) {
            return 0;
        }
        return (int) (((long) a << 16) / b);
    }

    private static int arcTan16(final int x) {
        final int xx = mul16(x, x);
        int t = mul16(1365, xx);
        t = mul16(t - 5579, xx);
        t = mul16(t + 11805, xx);
        t = mul16(t - 21646, xx);
        t = mul16(t + 65527, x);
        return t;
    }

    private static int arcTan2Deg(int yFixed, int xFixed) {
        int x = xFixed >> 16;
        int y = yFixed >> 16;

        final int div = div16((y << 16), (x << 16));
        int arcTan;

        if (x == 0 && y == 0) {
            arcTan = 0;
        } else if (x == 0 && y > 0) {
            arcTan = 102943;
        } else if (x < 0 && y == 0) {
            arcTan = 205887;
        } else if (x == 0 && y < 0) {
            arcTan = 308830;
        } else if (x > 0 && y == 0) {
            arcTan = 0;
        } else {
            if (div > 65536) {
                arcTan = 102943 - arcTan16(div16(65536, div));
            } else if (div < -65536) {
                arcTan = -102943 - arcTan16(div16(65536, div));
            } else {
                arcTan = arcTan16(div);
            }

            if (x < 0 && y > 0) {
                arcTan += 205887;
            } else if (x < 0 && y < 0) {
                arcTan += 205887;
            } else if (x > 0 && y < 0) {
                arcTan += 411774;
            }
        }

        int i = mul16(arcTan, div16(11796480, 205887)) >> 16;
        for (; i < 0; i = (i + 360) % 360) {
        }
        return i;
    }

    public void renderPlayerBullets(Graphics g) {
        renderInternal(g, 1);
    }

    public void renderEnemyBullets(Graphics g) {
        renderInternal(g, 0);
    }

    public void render(Graphics g) {
        renderInternal(g, -1);
    }

    // mode: -1=all, 0=enemy, 1=player.
    private void renderInternal(Graphics g, int mode) {
        // Cache global clip once (BattleRenderer sets playfield clip before calling this).
        int clipX = 0;
        int clipY = 0;
        int clipW = 0;
        int clipH = 0;
        try {
            clipX = g.getClipX();
            clipY = g.getClipY();
            clipW = g.getClipWidth();
            clipH = g.getClipHeight();
        } catch (Throwable ignore) {
        }

        // DoJa ec(): two-pass draw order.
        // Pass1 draws bulletId 87..96 (large bullets) first so small bullets render on top.
        // Player bullets never use 87..96, so skip pass1 for mode==1.
        if (mode != 1) {
            for (int i = 0; i < max; i++) {
                if (!active[i]) {
                    continue;
                }
                if (mode == 0 && fromPlayer[i]) {
                    continue;
                }
                int baseId = bulletId[i];
                if (baseId < 87 || baseId > 96) {
                    continue;
                }
                renderSlot(g, i, clipX, clipY, clipW, clipH);
            }
        }
        for (int i = 0; i < max; i++) {
            if (!active[i]) {
                continue;
            }
            if (mode == 0 && fromPlayer[i]) {
                continue;
            }
            if (mode == 1 && !fromPlayer[i]) {
                continue;
            }
            int baseId = bulletId[i];
            if (baseId < 0 || (baseId >= 87 && baseId <= 96)) {
                continue;
            }
            renderSlot(g, i, clipX, clipY, clipW, clipH);
        }
    }

    private void renderSlot(Graphics g, int i, int clipX, int clipY, int clipW, int clipH) {
        if (bulletId[i] < 0) {
            return;
        }

        int px = Fixed.toInt(x[i]);
        int py = Fixed.toInt(y[i]);

        int drawId = bulletId[i];
        int frames = animFrames[i];
        if (frames > 1) {
            int frame;
            if (!fromPlayer[i] && moveType[i] == 2 && tag[i] != 0) {
                frame = (animTick[i] >> 1);
                if (frame >= frames) {
                    frame = frames - 1;
                }
            } else {
                frame = (animTick[i] % frames);
            }
            drawId += frame;
        }

        int dirFrame = resolveDirectionFrame(bulletId[i], angleDeg[i], delta[i]);
        if (dirFrame != 0) {
            drawId = bulletId[i] + dirFrame;
        }

        if (sprites != null) {
            if (sprites.drawFast(g, drawId, px, py, fromPlayer[i], clipX, clipY, clipW, clipH)) {
                return;
            }
            if (sprites.draw(g, drawId, px, py, fromPlayer[i])) {
                return;
            }
        }

        if (drawId == 21) {
            g.setColor(0xFF0000);
            g.fillRect(px - 2, py - 2, 4, 4);
            return;
        }

        int c = (drawId * 97) & 0xFF;
        int color = (c << 16) | (c << 8) | c;
        g.setColor(color);
        g.fillRect(px - 1, py - 1, 3, 3);
    }

    private int alloc() {
        int start = allocHint;
        for (int j = 0; j < max; j++) {
            int i = start + j;
            if (i >= max) {
                i -= max;
            }
            if (!active[i]) {
                if (!updateRunning || slotLockStamp[i] != updateStamp) {
                    keepAlive[i] = 0;
                    perfSpawnCount++;
                    allocHint = i + 1;
                    if (allocHint >= max) {
                        allocHint = 0;
                    }
                    return i;
                }
            }
        }
        perfAllocFailCount++;
        return -1;
    }
}
