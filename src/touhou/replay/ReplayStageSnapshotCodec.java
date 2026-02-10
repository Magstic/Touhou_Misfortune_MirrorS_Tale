package touhou.replay;

import touhou.Player;
import touhou.ScoreSystem;
import touhou.battle.BattleContinueSystem;
import touhou.battle.BattleHudData;
import touhou.battle.BattleHudModel;

public final class ReplayStageSnapshotCodec {
    // Magic/version for stage-start snapshot.
    public static final int MAGIC = 0x53534E50; // 'SSNP'
    public static final int VERSION = 1;

    private ReplayStageSnapshotCodec() {
    }

    // Encode minimal stage-start snapshot required for deterministic stage replay.
    public static byte[] encode(ReplayRng random, BattleContinueSystem continueSystem, BattleHudData hud, ScoreSystem scoreSystem, Player player) {
        if (random == null || continueSystem == null || scoreSystem == null || player == null) {
            return null;
        }

        ReplaySnapshotWriter w = new ReplaySnapshotWriter(1024);

        w.writeI32LE(MAGIC);
        w.writeI32LE(VERSION);

        w.writeI32LE(random.getState());
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

    // Decode snapshot and apply state. Returns false if data is invalid.
    public static boolean decodeAndApply(byte[] snap, ReplayRng random, BattleContinueSystem continueSystem, BattleHudData hud, ScoreSystem scoreSystem, Player player) {
        if (snap == null || snap.length < 8) {
            return false;
        }
        if (random == null || continueSystem == null || scoreSystem == null || player == null) {
            return false;
        }

        ReplaySnapshotReader r = new ReplaySnapshotReader(snap);
        int magic = r.readI32LE();
        int ver = r.readI32LE();
        if (magic != MAGIC || ver != VERSION) {
            return false;
        }

        random.setState(r.readI32LE());

        int remainingContinues = r.readI32LE();
        continueSystem.setRemainingContinues(remainingContinues);

        boolean hasHud = r.readBool();
        int hudLevel = 0;
        int hudPower = 0;
        if (hasHud) {
            hudLevel = r.readI32LE();
            hudPower = r.readI32LE();
        }

        if (hud instanceof BattleHudModel) {
            scoreSystem.attachHud((BattleHudModel) hud);
        }

        scoreSystem.readSnapshot(r);

        if (hasHud && hud instanceof BattleHudModel) {
            BattleHudModel m = (BattleHudModel) hud;
            m.setLevel(hudLevel);
            m.setPower(hudPower);
        }

        boolean hasPlayer = r.readBool();
        if (!hasPlayer) {
            return false;
        }
        player.readSnapshot(r);

        return true;
    }
}
