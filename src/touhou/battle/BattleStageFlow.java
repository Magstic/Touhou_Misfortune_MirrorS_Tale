package touhou.battle;

import touhou.GameCore;
import touhou.stage.StageRuntime;

public final class BattleStageFlow implements StageRuntime.StageFlow {
    private final GameCore core;

    public BattleStageFlow(GameCore core) {
        this.core = core;
    }

    public boolean shouldEnterEndingAfterStageClear() {
        return core.stageFlowShouldEnterEndingAfterStageClear();
    }

    public void beginEndingTransition() {
        core.stageFlowBeginEndingTransition();
    }

    public void enterStageClearResultPanel() {
        core.stageFlowEnterStageClearResultPanel();
    }
}