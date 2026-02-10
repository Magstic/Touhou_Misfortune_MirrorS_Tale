package touhou.battle;

import touhou.AudioSystem;
import touhou.GameCore;
import touhou.stage.BossController;
import touhou.stage.StageRuntime;

final class BattleBossHost implements BossController.Host {
    private final GameCore core;
    private final StageRuntime stageRuntime;
    private final AudioSystem audioSystem;

    BattleBossHost(GameCore core, StageRuntime stageRuntime, AudioSystem audioSystem) {
        this.core = core;
        this.stageRuntime = stageRuntime;
        this.audioSystem = audioSystem;
    }

    public boolean isBossMode() {
        return core.bossHostIsBossMode();
    }

    public int getStage() {
        return core.bossHostGetStage();
    }

    public int getGameMode() {
        return core.bossHostGetGameMode();
    }

    public int getChara() {
        return core.bossHostGetChara();
    }

    public int getLevel() {
        return core.bossHostGetLevel();
    }

    public int getPower() {
        return core.bossHostGetPower();
    }

    public int getFi() {
        return core.bossHostGetFi();
    }

    public int getMGCnt() {
        return core.bossHostGetMGCnt();
    }

    public void setMGCnt(int v) {
        core.bossHostSetMGCnt(v);
    }

    public int getBossF() {
        return core.bossHostGetBossF();
    }

    public void setBossF(int v) {
        core.bossHostSetBossF(v);
    }

    public int getBbaria() {
        return core.bossHostGetBbaria();
    }

    public void setBbaria(int v) {
        core.bossHostSetBbaria(v);
    }

    public int getBstock() {
        return stageRuntime.getBstock();
    }

    public void setResetFlag(int v) {
        core.bossHostSetResetFlag(v);
    }

    public int getPlayerXFixed() {
        return core.bossHostGetPlayerXFixed();
    }

    public int getPlayerYFixed() {
        return core.bossHostGetPlayerYFixed();
    }

    public int getSpellPracticeBossStep() {
        return core.bossHostGetSpellPracticeBossStep();
    }

    public void setQ(boolean v) {
        core.bossHostSetSpellBonusArmed(v);
    }

    public void stageJb(int enemyIdx) {
        core.bossHostStageJb(enemyIdx);
    }

    public int stageFb(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9) {
        return stageRuntime.stageFb(n, n2, n3, n4, n5, n6, n7, n8, n9);
    }

    public int[] getBossX() {
        return stageRuntime.getBossX();
    }

    public int[] getBossY() {
        return stageRuntime.getBossY();
    }

    public int[] getBossWrk() {
        return stageRuntime.getBossWrk();
    }

    public int[][] getEnemyList() {
        return stageRuntime.getEnemyList();
    }

    public void playSound(int id, boolean loop) {
        // Legacy sound routing: loop -> BGM, else -> SE.
        if (loop) {
            if (core.battleFlowIsStageClearPanelActive()) {
                return;
            }
            audioSystem.requestBgm(id, true);
            return;
        }
        audioSystem.playSeType(id);
    }

    public int getTimeStop() {
        return core.bossHostGetTimeStop();
    }

    public void setTimeStop(int v) {
        core.bossHostSetTimeStop(v);
    }

    public void aimEnemyBulletsToPlayer() {
        core.bossHostAimEnemyBulletsToPlayer();
    }

    public void setEnemyBulletGlobalDir(int deg) {
        core.bossHostSetEnemyBulletGlobalDir(deg);
    }

    public void setEnemyBulletLock(int idx, int value) {
        core.bossHostSetEnemyBulletLock(idx, value);
    }

    public void spawnStageEffect(int effectId, int xFixed, int yFixed) {
        stageRuntime.stageKc(effectId, xFixed, yFixed);
    }

    public void spawnEnemyBullet(int bulletId, int moveType, int angleDeg, int speedParam, int xFixed, int yFixed) {
        core.bossHostSpawnEnemyBullet(bulletId, moveType, angleDeg, speedParam, xFixed, yFixed);
    }

    public void convertEnemyBulletsTo81Rain(int bossYFixed, int gc) {
        core.bossHostConvertEnemyBulletsTo81Rain(bossYFixed, gc);
    }

    public void setEnemyBulletsDeltaForIdAndMoveType(int bulletId, int moveType, int newDelta) {
        core.bossHostSetEnemyBulletsDeltaForIdAndMoveType(bulletId, moveType, newDelta);
    }

    public void setEnemyBulletsDeltaForIdIfDeltaZero(int bulletId, int baseDelta, int deltaStep) {
        core.bossHostSetEnemyBulletsDeltaForIdIfDeltaZero(bulletId, baseDelta, deltaStep);
    }
}