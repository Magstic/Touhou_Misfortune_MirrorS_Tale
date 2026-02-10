package touhou.replay;

public final class ReplayBossOnlyPolicy {
    private ReplayBossOnlyPolicy() {
    }

    public static boolean isFastForwardBossOnlyStage(int stage) {
        // Stage index: 5 = Stage6, 6 = StageEX.
        return stage == 5 || stage == 6;
    }
}
