package touhou;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.battle.BattleScene;
import touhou.ui.EndingScreen;
import touhou.ui.StageClearResultPanel;
import touhou.ui.StaffRollScreen;
import touhou.ui.UiController;
import touhou.ui.UiDraw;

public final class SceneController {

    private final BattleScene battleScene;
    private final EndingScreen endingScreen;
    private final StaffRollScreen staffRollScreen;
    private final StageClearResultPanel stageClearResultPanel;
    private final AudioSystem audioSystem;

    SceneController(BattleScene battleScene, EndingScreen endingScreen, StaffRollScreen staffRollScreen, StageClearResultPanel stageClearResultPanel, AudioSystem audioSystem) {
        this.battleScene = battleScene;
        this.endingScreen = endingScreen;
        this.staffRollScreen = staffRollScreen;
        this.stageClearResultPanel = stageClearResultPanel;
        this.audioSystem = audioSystem;
    }

    public void update(GameCore core, int keys, int pressed) {
        if (core.sceneIsEndingActive()) {
            EndingScreen.Result r = endingScreen.update(pressed);
            if (r != null && r.kind == EndingScreen.Result.KIND_DONE) {
                core.sceneSetEndingActive(false);
                staffRollScreen.enter(core.sceneImgs());
                core.sceneSetStaffRollActive(true);
            }
            return;
        }

        if (core.sceneIsStaffRollActive()) {
            StaffRollScreen.Result r = staffRollScreen.update(keys, pressed);
            if (r != null && r.kind == StaffRollScreen.Result.KIND_DONE) {
                core.sceneSetStaffRollActive(false);
                staffRollScreen.leave();
                core.sceneEnterFinalResultPanelAfterStaffRoll();
            }
            return;
        }

        if (core.battleFlowIsStageClearPanelActive() && !core.sceneIsInGame()) {
            if ((pressed & (GameCanvas.UP_PRESSED | GameCanvas.DOWN_PRESSED)) != 0) {
                audioSystem.playSeType(SoundEffectSystem.SE_NAV);
            }
            if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                audioSystem.playSeType(SoundEffectSystem.SE_SELECT);
            }
            if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
                audioSystem.playSeType(SoundEffectSystem.SE_BACK);
            }
            if ((pressed & GameCore.KEY_POUND_PRESSED) != 0) {
                audioSystem.playSeType(SoundEffectSystem.SE_SELECT);
            }

            StageClearResultPanel.Result r = stageClearResultPanel.update(pressed);
            if (r != null && r.kind == StageClearResultPanel.Result.KIND_BACK_TO_TITLE) {
                core.battleFlowSetStageClearPanelActive(false);
                core.battleFlowClearStageClearPhoto();
                core.battleFlowClearReplayPending();

                UiController ui = core.sceneUi();
                if (ui != null) {
                    ui.returnToTitleKeepCursor();
                }
            }
            return;
        }

        UiController ui = core.sceneUi();
        if (ui != null) {
            ui.update(pressed);
        }
    }

    public void render(GameCore core, Graphics g) {
        if (core.sceneIsEndingActive()) {
            endingScreen.render(g, core.sceneImgs());
            return;
        }

        if (core.sceneIsStaffRollActive()) {
            staffRollScreen.render(g, core.sceneImgs());
            return;
        }

        if (core.battleFlowIsStageClearPanelActive() && !core.sceneIsInGame()) {
            stageClearResultPanel.render(g, core.sceneBulletSprites());
            return;
        }

        if (!core.sceneIsInGame()) {
            UiController ui = core.sceneUi();
            if (ui != null) {
                ui.render(g);
            }
            String err = core.sceneResourceError();
            if (err != null) {
                int y = 2 + UiDraw.fontBaseline(null);
                UiDraw.drawStringPlain(g, null, err, 2, y, 0, 0xFFFFFF);
            }
            return;
        }

        battleScene.render(core, g);
    }
}