package touhou.replay;

import touhou.Player;
import touhou.ScoreSystem;
import touhou.battle.BattleContinueSystem;
import touhou.battle.BattleHudData;

public final class ReplayStageSnapshotService {
    // Stage snapshot capture + playback helpers.
    // Keeps GameCanvas focused on orchestration.

    private ReplayStageSnapshotService() {
    }

    public static byte[] captureIfNeeded(byte[] existingSnapshot, ReplayRng random, BattleContinueSystem continueSystem, BattleHudData hud, ScoreSystem scoreSystem,
            Player player) {
        if (existingSnapshot != null) {
            return existingSnapshot;
        }
        return ReplayStageSnapshotCodec.encode(random, continueSystem, hud, scoreSystem, player);
    }

    public static boolean loadAndApplyFromSlot(int slot, ReplayRng random, BattleContinueSystem continueSystem, BattleHudData hud, ScoreSystem scoreSystem, Player player) {
        if (player == null) {
            return false;
        }
        byte[] snap = ReplayStageSnapshotStore.loadSlot(slot);
        return ReplayStageSnapshotCodec.decodeAndApply(snap, random, continueSystem, hud, scoreSystem, player);
    }
}
