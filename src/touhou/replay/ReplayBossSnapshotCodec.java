package touhou.replay;

import touhou.CcTable;
import touhou.Player;
import touhou.ScoreSystem;
import touhou.battle.BattleContinueSystem;
import touhou.battle.BattleHudData;
import touhou.battle.BattleHudModel;

public final class ReplayBossSnapshotCodec {
    // Magic/version for boss-only replay snapshot.
    public static final int MAGIC = 0x42534E50; // 'BSNP'
    public static final int VERSION = 3;

    public static final class Decoded {
        public int fi;
        public int bombCnt;
        public int sBariacnt;
        public int sDeadcnt;
        public int mTacnt;
        public boolean shooting;
        public int ca;
        public int timeStop;
        public int mResetf;

        public int stageMissCount;
        public int stageBombUsedCount;
        public int stageSpellSeenCount;
        public int stageSpellBonusCount;
        public int stageLastSpellId;
        public int stageInitialContinueCount;

        public int remainingContinues;

        public boolean hasHud;
        public int hudLevel;
        public int hudPower;

        public boolean hasPlayer;
    }

    public static final class PlayerBox {
        public Player value;

        public PlayerBox(Player value) {
            this.value = value;
        }
    }

    private ReplayBossSnapshotCodec() {
    }

    // Encode boss-only replay snapshot (includes runtime counters needed for deterministic boss start).
    public static byte[] encode(ReplayRng random,
            int fi, int bombCnt, int sBariacnt, int sDeadcnt, int mTacnt, boolean shooting, int ca, int timeStop, int mResetf,
            int stageMissCount, int stageBombUsedCount, int stageSpellSeenCount, int stageSpellBonusCount, int stageLastSpellId,
            int stageInitialContinueCount, BattleContinueSystem continueSystem, BattleHudData hud, ScoreSystem scoreSystem, Player player) {
        if (random == null || continueSystem == null || scoreSystem == null || player == null) {
            return null;
        }
        ReplaySnapshotWriter w = new ReplaySnapshotWriter(1024);

        w.writeI32LE(MAGIC);
        w.writeI32LE(VERSION);

        w.writeI32LE(random.getState());

        w.writeI32LE(fi);
        w.writeI32LE(bombCnt);
        w.writeI32LE(sBariacnt);
        w.writeI32LE(sDeadcnt);
        w.writeI32LE(mTacnt);
        w.writeBool(shooting);
        w.writeI32LE(ca);
        w.writeI32LE(timeStop);
        w.writeI32LE(mResetf);

        w.writeI32LE(stageMissCount);
        w.writeI32LE(stageBombUsedCount);
        w.writeI32LE(stageSpellSeenCount);
        w.writeI32LE(stageSpellBonusCount);
        w.writeI32LE(stageLastSpellId);
        w.writeI32LE(stageInitialContinueCount);

        w.writeI32LE(continueSystem.getRemainingContinues());

        boolean hasHud = hud instanceof BattleHudModel;
        w.writeBool(hasHud);
        if (hasHud) {
            BattleHudModel m = (BattleHudModel) hud;
            w.writeI32LE(m.getLevel());
            w.writeI32LE(m.getPower());
        }

        scoreSystem.writeSnapshot(w);

        w.writeBool(true);
        player.writeSnapshot(w);

        return w.toByteArray();
    }

    // Decode snapshot and apply state. Returns null if data is invalid.
    public static Decoded decodeAndApply(byte[] snap, ReplayRng random, BattleContinueSystem continueSystem, BattleHudData hud, ScoreSystem scoreSystem, PlayerBox player,
            CcTable cc, int currentChara, int currentType) {
        if (snap == null || snap.length < 8) {
            return null;
        }

        if (random == null || continueSystem == null || scoreSystem == null || player == null) {
            return null;
        }

        ReplaySnapshotReader r = new ReplaySnapshotReader(snap);
        int magic = r.readI32LE();
        int ver = r.readI32LE();
        if (magic != MAGIC || ver != VERSION) {
            return null;
        }

        random.setState(r.readI32LE());

        Decoded d = new Decoded();
        d.fi = r.readI32LE();
        d.bombCnt = r.readI32LE();
        d.sBariacnt = r.readI32LE();
        d.sDeadcnt = r.readI32LE();
        d.mTacnt = r.readI32LE();
        d.shooting = r.readBool();
        d.ca = r.readI32LE();
        d.timeStop = r.readI32LE();
        d.mResetf = r.readI32LE();

        d.stageMissCount = r.readI32LE();
        d.stageBombUsedCount = r.readI32LE();
        d.stageSpellSeenCount = r.readI32LE();
        d.stageSpellBonusCount = r.readI32LE();
        d.stageLastSpellId = r.readI32LE();
        d.stageInitialContinueCount = r.readI32LE();

        d.remainingContinues = r.readI32LE();
        continueSystem.setRemainingContinues(d.remainingContinues);

        d.hasHud = r.readBool();
        d.hudLevel = 0;
        d.hudPower = 0;
        if (d.hasHud) {
            d.hudLevel = r.readI32LE();
            d.hudPower = r.readI32LE();
        }

        scoreSystem.readSnapshot(r);

        if (d.hasHud && hud instanceof BattleHudModel) {
            BattleHudModel m = (BattleHudModel) hud;
            m.setLevel(d.hudLevel);
            m.setPower(d.hudPower);
        }

        d.hasPlayer = r.readBool();
        if (!d.hasPlayer) {
            return null;
        }
        if (player.value == null) {
            return null;
        }
        player.value.readSnapshot(r);

        return d;
    }
}
