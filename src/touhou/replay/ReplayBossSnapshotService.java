package touhou.replay;

import touhou.CcTable;
import touhou.Player;
import touhou.ScoreSystem;
import touhou.battle.BattleContinueSystem;
import touhou.battle.BattleHudData;

public final class ReplayBossSnapshotService {
    // Boss snapshot capture + playback helpers.
    // Keeps GameCanvas focused on orchestration.

    private ReplayBossSnapshotService() {
    }

    public static byte[] captureIfNeeded(ReplayStageRecorder recorder, byte[] existingSnapshot, ReplayRng random,
            int fi, int bombCnt, int sBariacnt, int sDeadcnt, int mTacnt, boolean shooting, int ca, int timeStop, int mResetf,
            int stageMissCount, int stageBombUsedCount, int stageSpellSeenCount, int stageSpellBonusCount, int stageLastSpellId,
            int stageInitialContinueCount, BattleContinueSystem continueSystem, BattleHudData hud, ScoreSystem scoreSystem, Player player) {

        if (existingSnapshot != null) {
            return existingSnapshot;
        }
        if (recorder == null || !recorder.isRecording()) {
            return existingSnapshot;
        }
        if (recorder.getBossStartFrame() < 0) {
            return existingSnapshot;
        }

        return ReplayBossSnapshotCodec.encode(random,
                fi, bombCnt, sBariacnt, sDeadcnt, mTacnt, shooting, ca, timeStop, mResetf,
                stageMissCount, stageBombUsedCount, stageSpellSeenCount,
                stageSpellBonusCount, stageLastSpellId, stageInitialContinueCount, continueSystem, hud, scoreSystem, player);
    }

    public static ReplayBossSnapshotCodec.Decoded loadAndApplyFromSlot(int slot, ReplayRng random, BattleContinueSystem continueSystem,
            BattleHudData hud, ScoreSystem scoreSystem, Player player, CcTable cc, int currentChara, int currentType) {

        if (player == null) {
            return null;
        }

        byte[] snap = ReplayBossSnapshotStore.loadSlot(slot);
        ReplayBossSnapshotCodec.PlayerBox pb = new ReplayBossSnapshotCodec.PlayerBox(player);
        return ReplayBossSnapshotCodec.decodeAndApply(snap, random, continueSystem, hud, scoreSystem, pb, cc, currentChara, currentType);
    }
}