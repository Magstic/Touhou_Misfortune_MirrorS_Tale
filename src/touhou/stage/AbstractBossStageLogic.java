package touhou.stage;

abstract class AbstractBossStageLogic implements BossStageLogic {
    BossController core;
    BossController.Host host;

    int[] bwave;
    int bspellcnt;
    boolean batkf;
    int bspellstep;

    int k;
    int l;
    int m;
    int n;
    int[] o;
    int[] p;
    int j;
    int spellId;

    public final void tickBoss(BossController core, int enemyIdx) {
        begin(core);
        tickBossImpl(enemyIdx);
        end();
    }

    public final void tickMidboss(BossController core, int enemyIdx) {
        begin(core);
        tickMidbossImpl(enemyIdx);
        end();
    }

    abstract void tickBossImpl(int enemyIdx);

    abstract void tickMidbossImpl(int enemyIdx);

    final void begin(BossController core) {
        this.core = core;
        this.host = core.host();
        pullFromCore();
    }

    final void end() {
        pushToCore();
        this.core = null;
        this.host = null;
        this.bwave = null;
        this.o = null;
        this.p = null;
    }

    final void pullFromCore() {
        bwave = core.bwave;
        bspellcnt = core.bspellcnt;
        batkf = core.batkf;
        bspellstep = core.bspellstep;

        k = core.k;
        l = core.l;
        m = core.m;
        n = core.n;
        o = core.o;
        p = core.p;
        j = core.j;
        spellId = core.spellId;
    }

    final void pushToCore() {
        core.bspellcnt = bspellcnt;
        core.batkf = batkf;
        core.bspellstep = bspellstep;

        core.k = k;
        core.l = l;
        core.m = m;
        core.n = n;
        core.j = j;
        core.spellId = spellId;
    }

    void vb(int enemyIdx, int state) {
        host.setMGCnt(-1);
        host.getEnemyList()[enemyIdx][7] = state;
    }

    void wb(int bossf) {
        host.setMGCnt(-1);
        host.setBossF(bossf);
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
}
