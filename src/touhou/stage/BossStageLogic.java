package touhou.stage;

interface BossStageLogic {
    void tickBoss(BossController core, int enemyIdx);

    void tickMidboss(BossController core, int enemyIdx);
}
