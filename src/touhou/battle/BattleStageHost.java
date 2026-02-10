package touhou.battle;

import javax.microedition.lcdui.Graphics;

import touhou.AudioSystem;
import touhou.GameCore;
import touhou.GameProgress;
import touhou.stage.StageRuntime;

public final class BattleStageHost implements StageRuntime.Host {
    private final GameCore core;
    private final StageRuntime.StageFlow flow;
    private final AudioSystem audioSystem;

    public BattleStageHost(GameCore core, StageRuntime.StageFlow flow, AudioSystem audioSystem) {
        this.core = core;
        this.flow = flow;
        this.audioSystem = audioSystem;
    }

    public String readUtf8TextResource(String path) {
        return GameCore.readUtf8TextResource(path);
    }

    public String[] splitLinesAndWrap(String text, int wrapByteLen) {
        return GameCore.splitLinesAndWrap(text, wrapByteLen);
    }

    public void clearDropItems() {
        core.stageHostClearDropItems();
    }

    public void clearEnemies() {
        core.stageHostClearEnemies();
    }

    public void resetBossController() {
        core.stageHostResetBossController();
    }

    public int getStage() {
        return core.bossHostGetStage();
    }

    public int getGameMode() {
        return core.bossHostGetGameMode();
    }

    public int getBstock() {
        return core.stageHostGetBstock();
    }

    public void setBstock(int v) {
        core.stageHostSetBstock(v);
    }

    public void setEnemyAabb(int idx, int xFixed, int yFixed, int radiusFixed,
            int leftExtentFixed, int rightExtentFixed, int topExtentFixed, int bottomExtentFixed,
            boolean canTarget) {
        core.stageHostSetEnemyAabb(idx, xFixed, yFixed, radiusFixed, leftExtentFixed, rightExtentFixed, topExtentFixed, bottomExtentFixed, canTarget);
    }

    public int[][] getEffectTable() {
        return core.stageHostGetEffectTable();
    }

    public boolean isHitSparkEnabled() {
        return core.stageHostIsHitSparkEnabled();
    }

    public int stageNa() {
        return core.stageHostStageNa();
    }

    public int getStep() {
        return core.stageHostGetStep();
    }

    public void setStep(int v) {
        core.stageHostSetStep(v);
    }

    public int getTaCnt() {
        return core.stageHostGetTaCnt();
    }

    public void setTaCnt(int v) {
        core.stageHostSetTaCnt(v);
    }

    public void setIFlash(int v) {
        core.stageHostSetIFlash(v);
    }

    public void playSound(int id, boolean loop) {
        // Legacy sound routing: loop -> BGM, else -> SE.
        if (loop) {
            if (core.battleFlowIsStageClearPanelActive()) {
                return;
            }
            GameProgress.unlockMusicPlayed(id);
            audioSystem.requestBgm(id, true);
            return;
        }
        audioSystem.playSeType(id);
    }

    public boolean shouldEnterEndingAfterStageClear() {
        return flow.shouldEnterEndingAfterStageClear();
    }

    public void beginEndingTransition() {
        flow.beginEndingTransition();
    }

    public void enterStageClearResultPanel() {
        flow.enterStageClearResultPanel();
    }

    public void stageHd(int enemyIdx) {
        core.stageHostStageHd(enemyIdx);
    }

    public void stageUb(int enemyIdx) {
        core.stageHostStageUb(enemyIdx);
    }

    public void stageDb(int enemyIdx) {
        core.stageHostStageDb(enemyIdx);
    }

    public void stageEb(int enemyIdx) {
        core.stageHostStageEb(enemyIdx);
    }

    public void freeEnemy(int enemyIdx) {
        core.stageHostFreeEnemy(enemyIdx);
    }

    public void spawnBulletEffect(int effectId, int xFixed, int yFixed) {
        core.stageHostSpawnBulletEffect(effectId, xFixed, yFixed);
    }

    public int randomNextInt() {
        return core.stageHostRandomNextInt();
    }

    public int getOriginX() {
        return core.stageHostGetOriginX();
    }

    public int getOriginY() {
        return core.stageHostGetOriginY();
    }

    public int getPlayX() {
        return core.stageHostGetPlayX();
    }

    public int getPlayY() {
        return core.stageHostGetPlayY();
    }

    public int getPlayW() {
        return core.stageHostGetPlayW();
    }

    public int getPlayH() {
        return core.stageHostGetPlayH();
    }

    public int getBossF() {
        return core.bossHostGetBossF();
    }

    public void setBossF(int v) {
        core.bossHostSetBossF(v);
    }

    public int getMGCnt() {
        return core.bossHostGetMGCnt();
    }

    public void setMGCnt(int v) {
        core.bossHostSetMGCnt(v);
    }

    public void setBbaria(int v) {
        core.bossHostSetBbaria(v);
    }

    public void setLatterFlag(boolean v) {
        core.stageHostSetLatterFlag(v);
    }

    public void setBossMode(boolean v) {
        core.stageHostSetBossMode(v);
    }

    public int[] getBossX() {
        return core.stageHostGetBossX();
    }

    public int[] getBossY() {
        return core.stageHostGetBossY();
    }

    public int getStartLevel() {
        return core.stageHostGetStartLevel();
    }

    public boolean isReplayPlaybackActive() {
        return core.battleFlowIsReplayPlaybackActive();
    }

    public boolean isReplayRecordingActive() {
        return core.stageHostIsReplayRecordingActive();
    }

    public void recordDialoguePageCount(int encounterIndex, int pageCount) {
        core.stageHostRecordDialoguePageCount(encounterIndex, pageCount);
    }

    public int getRecordedDialoguePageCount(int encounterIndex) {
        return core.stageHostGetRecordedDialoguePageCount(encounterIndex);
    }

    public void markBossStartAndCaptureCurrentFrame() {
        core.stageHostMarkBossStartAndCaptureCurrentFrame();
    }

    public void setResetFlag(int v) {
        core.bossHostSetResetFlag(v);
    }

    public void setHFlash(int v) {
        core.stageHostSetHFlash(v);
    }

    public void setTargetFps(int fps) {
        core.stageHostSetTargetFps(fps);
    }

    public void drawCcIconSafe(Graphics g, int spriteId, int x, int y) {
        core.stageHostDrawCcIconSafe(g, spriteId, x, y);
    }
}