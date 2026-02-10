package touhou;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import touhou.battle.BattleContinueSystem;
import touhou.battle.BattleEngine;
import touhou.battle.BattleHudData;
import touhou.battle.BattleHudModel;
import touhou.battle.BattleMath;
import touhou.battle.BattleRenderer;
import touhou.battle.BattleScene;
import touhou.battle.BattleStageFlow;
import touhou.battle.BattleStageHost;
import touhou.replay.ReplayBossSnapshotCodec;
import touhou.replay.ReplayBossSnapshotService;
import touhou.replay.ReplayBossOnlyPolicy;
import touhou.replay.ReplayHeader;
import touhou.replay.ReplayBtShareManager;
import touhou.replay.ReplayPlaybackController;
import touhou.replay.ReplayRecordingController;
import touhou.replay.ReplayRng;
import touhou.replay.ReplayStageSnapshotService;
import touhou.replay.ReplayTimeScaleController;
import touhou.stage.BossController;
import touhou.stage.StageDochu;
import touhou.stage.StageRuntime;
import touhou.i18n.I18n;
import touhou.i18n.I18nBootstrap;
import touhou.ui.BattlePauseScreen;
import touhou.ui.EndingScreen;
import touhou.ui.InputSystem;
import touhou.ui.SpellPracticeEndScreen;
import touhou.ui.StaffRollScreen;
import touhou.ui.StageClearResultPanel;
import touhou.ui.UiController;

public final class GameCore extends javax.microedition.lcdui.game.GameCanvas implements Runnable {
    // Game loop timing and logical screen size.
    private static final int TARGET_FPS = 16;
    private static final int TARGET_FRAME_MS = 1000 / TARGET_FPS;
    private static final int LOGICAL_W = 240;
    private static final int LOGICAL_H = 240;

    private int targetFps = TARGET_FPS;

    // Animation/effect tables.
    private static final int[][] EFFECT_TABLE = new int[][] { { 1, 392, 0, 393, 0, 394, 255 }, { 1, 395, 0, 396, 0, 397, 0, 398, 2, 399, 255 }, { 1, 442, 0, 443, 0, 444, 255 }, { 2, 445, 2, 446, 2, 447, 255 }, { 2, 448, 2, 449, 2, 450, 255 }, { 2, 451, 2, 452, 2, 453, 255 }, { 5, 166, 5, -1, 255 }, { 5, 173, 5, -1, 255 }, { 5, 179, 5, -1, 255 }, { 5, 196, 5, -1, 255 }, { 5, 216, 5, -1, 255 }, { 5, 356, 5, -1, 255 }, { 5, 370, 5, -1, 255 }, { 5, 504, 5, -1, 255 }, { 5, 711, 5, -1, 255 }, { 5, 719, 5, -1, 255 }, { 2, 454, 15, 454, 2, 454, 255 }, { 2, -1, 2, 455, 15, 455, 2, 455, 255 }, { 4, -1, 2, 456, 15, 456, 2, 456, 255 }, { 1, 457, 1, 458, 1, 459, 1, 460, 1, 461, 255 }, { 1, 462, 1, 463, 1, 464, 1, 465, 1, 466, 255 }, { 255, -1 }, { 1, 500, 1, 501, 1, 502, 1, 503, 255 }, { 3, 401, 20, 400, 2, 401, 2, 402, 255 }, { 2, 404, 20, 403, 2, 404, 2, 405, 255 }, { 3, 407, 20, 406, 2, 407, 2, 408, 255 }, { 2, 410, 20, 409, 2, 410, 2, 411, 255 }, { 3, 413, 20, 412, 2, 413, 2, 414, 255 }, { 2, 416, 20, 415, 2, 416, 2, 417, 255 }, { 3, 419, 20, 418, 2, 419, 2, 420, 255 }, { 2, 422, 20, 421, 2, 422, 2, 423, 255 }, { 3, 425, 20, 424, 2, 425, 2, 426, 255 }, { 2, 428, 20, 427, 2, 428, 2, 429, 255 }, { 3, 431, 20, 430, 2, 431, 2, 432, 255 }, { 2, 434, 20, 433, 2, 434, 2, 435, 255 }, { 3, 437, 20, 436, 2, 437, 2, 438, 255 }, { 2, 440, 20, 439, 2, 440, 2, 441, 255 } };

    // Softkey input mask.
    public static final int SOFTKEY_R_PRESSED = 1 << 28;

    // Key bitmask for tenkey '#'.
    public static final int KEY_POUND_PRESSED = 1 << 29;

    // Boss spell cut-in fade in/out.
    private static final int BOSS_SPELL_CUTIN_PHASE_FADE_IN = 1;
    private static final int BOSS_SPELL_CUTIN_PHASE_HOLD = 2;
    private static final int BOSS_SPELL_CUTIN_PHASE_FADE_OUT = 3;
    private static final int BOSS_SPELL_CUTIN_FADE_IN_FRAMES = 4;
    private static final int BOSS_SPELL_CUTIN_FADE_OUT_FRAMES = 8;

    // Playfield bounds in logical coordinates.
    private static final int PLAY_X = 0;
    private static final int PLAY_Y = 8;
    private static final int PLAY_W = 194;
    private static final int PLAY_H = 226;

    // Spell card name cache.
    private static boolean spellCardTextLoaded;
    private static String[] spellCardNames;

    // Spell practice metadata table.
    private static final int[][] SPELLPRACTICE_INFO = new int[][] {
            { 0, 2, 130, 8 },
            { 0, 3, 130, 8 },
            { 0, 0, 108, 19 },
            { 0, 1, 108, 19 },
            { 0, 2, 108, 19 },
            { 0, 3, 108, 19 },
            { 0, 0, 110, 10 },
            { 0, 1, 110, 10 },
            { 0, 2, 110, 10 },
            { 0, 3, 110, 10 },
            { 0, 0, 109, 13 },
            { 0, 1, 109, 13 },
            { 0, 2, 109, 13 },
            { 0, 3, 109, 13 },
            { 0, 0, 111, 16 },
            { 0, 1, 111, 16 },
            { 0, 2, 111, 16 },
            { 0, 3, 111, 16 },
            { 1, 0, 112, 10 },
            { 1, 1, 112, 10 },
            { 1, 2, 112, 10 },
            { 1, 3, 112, 10 },
            { 1, 0, 112, 13 },
            { 1, 1, 112, 13 },
            { 1, 2, 112, 13 },
            { 1, 3, 112, 13 },
            { 1, 0, 112, 16 },
            { 1, 1, 112, 16 },
            { 1, 2, 112, 16 },
            { 1, 3, 112, 16 },
            { 2, 0, 132, 8 },
            { 2, 1, 132, 8 },
            { 2, 2, 132, 8 },
            { 2, 3, 132, 8 },
            { 2, 0, 113, 10 },
            { 2, 1, 113, 10 },
            { 2, 2, 113, 10 },
            { 2, 3, 113, 10 },
            { 2, 0, 113, 13 },
            { 2, 1, 113, 13 },
            { 2, 2, 113, 13 },
            { 2, 3, 113, 13 },
            { 2, 0, 113, 16 },
            { 2, 1, 113, 16 },
            { 2, 2, 113, 16 },
            { 2, 3, 113, 16 },
            { 3, 0, 114, 10 },
            { 3, 1, 114, 10 },
            { 3, 2, 114, 10 },
            { 3, 3, 114, 10 },
            { 3, 0, 114, 13 },
            { 3, 1, 114, 13 },
            { 3, 2, 114, 13 },
            { 3, 3, 114, 13 },
            { 3, 0, 114, 16 },
            { 3, 1, 114, 16 },
            { 3, 2, 114, 16 },
            { 3, 3, 114, 16 },
            { 3, 0, 114, 19 },
            { 3, 1, 114, 19 },
            { 3, 2, 114, 19 },
            { 3, 3, 114, 19 },
            { 4, 0, 115, 10 },
            { 4, 1, 115, 10 },
            { 4, 2, 115, 10 },
            { 4, 3, 115, 10 },
            { 4, 0, 115, 13 },
            { 4, 1, 115, 13 },
            { 4, 2, 115, 13 },
            { 4, 3, 115, 13 },
            { 4, 0, 115, 16 },
            { 4, 1, 115, 16 },
            { 4, 2, 115, 16 },
            { 4, 3, 115, 16 },
            { 4, 0, 115, 19 },
            { 4, 1, 115, 19 },
            { 4, 2, 115, 19 },
            { 4, 3, 115, 19 },
            { 5, 0, 116, 10 },
            { 5, 1, 116, 10 },
            { 5, 2, 116, 10 },
            { 5, 3, 116, 10 },
            { 5, 0, 116, 13 },
            { 5, 1, 116, 13 },
            { 5, 2, 116, 13 },
            { 5, 3, 116, 13 },
            { 5, 0, 116, 16 },
            { 5, 1, 116, 16 },
            { 5, 2, 116, 16 },
            { 5, 3, 116, 16 },
            { 5, 0, 116, 19 },
            { 5, 1, 116, 19 },
            { 5, 2, 116, 19 },
            { 5, 3, 116, 19 },
            { 5, 0, 116, 22 },
            { 5, 1, 116, 22 },
            { 5, 2, 116, 22 },
            { 5, 3, 116, 22 },
            { 6, 4, 117, 10 },
            { 6, 4, 117, 13 },
            { 6, 4, 117, 16 },
            { 6, 4, 117, 19 },
            { 6, 4, 117, 22 },
            { 6, 4, 117, 25 },
            { 6, 4, 117, 28 },
            { 6, 4, 117, 31 },
            { 6, 4, 117, 34 },
            { 0, 5, 108, 37 },
            { 0, 5, 110, 40 },
            { 0, 5, 109, 43 },
            { 1, 5, 112, 37 },
            { 2, 5, 113, 37 },
            { 3, 5, 114, 37 },
            { 4, 5, 115, 37 },
            { 5, 5, 116, 37 },
            { 6, 5, 117, 37 },
    };

    // Main loop control.
    private Thread loop;
    private volatile boolean running;
    private volatile boolean paused;
    private final ReplayRng random;

    // Core gameplay systems.
    private final InputSystem input;
    private final SceneController sceneController;
    private final BattleScene battleScene;
    private final BattleRenderer battleRenderer;
    private final BattleRenderAccess battleRenderAccess;
    private final BulletSystem bullets;
    private final EnemyBulletSystem enemyBullets;
    private final EnemySystem enemies;

    private final BattleEngine battleEngine;

    private final StageDochu stageDochu;

    private final StageRuntime stageRuntime;

    private final BattleContinueSystem continueSystem;

    private final DropItemSystem dropItems;

    private final VibrationController vibration;

    private final DropItemSystem.IconDrawer dropItemDrawer = new DropItemSystem.IconDrawer() {
        public void draw(Graphics g, int ccIndex, int x, int y) {
            battleRenderer.drawCcIconSafe(battleRenderAccess, g, ccIndex, x, y);
        }
    };

    private boolean shouldEnterEndingAfterStageClear() {
        if (startSpellPractice) {
            return false;
        }

        // Stage6 clear (story) skips stage clear panel and proceeds to ending.
        // Extra clear proceeds directly to the final result panel (no ending/staff roll).
        if (mGamemode == 0) {
            return mStage >= 5; // Stage index: 5 == Stage6.
        }
        return false;
    }

    private void beginEndingTransition() {
        if (endingActive) {
            return;
        }
        if (endingFadeToWhite > 0) {
            return;
        }

        replayRecording.stop();

        // DoJa : replay save is disabled after using continue (ci=false).
        // For story clear (Stage6), there is no stage clear panel, so we arm pending here to allow
        // the final result screen to prompt for replay saving.
        replayRecording.clearPending();
        if (!replayPlayback.isActive() && !startSpellPractice && (mGamemode == 0 || mGamemode == 1)) {
            int continuesUsed = runInitialContinueCount - continueSystem.getRemainingContinues();
            if (continuesUsed < 0) {
                continuesUsed = 0;
            }
            if (continuesUsed == 0) {
                int lvl = (hud != null) ? hud.getLevel() : startLevel;
                int score = scoreSystem.getScore();
                int spellId = stageLastSpellId;
                if (spellId < 0) {
                    spellId = 255;
                }

                replayRecording.armPendingForStageClear(null, ReplayHeader.FLAG_LOCAL, lvl, currentChara, mStage, score, currentType, mGamemode, spellId);
            }
        }

        battlePaused = false;
        stageClearPanelActive = false;
        stageClearPhoto = null;
        endingFadeToWhite = 1;
    }

    public void enterEnding() {
        if (replayPlayback.isActive()) {
            endReplayToMenu(); // If playing a replay, do not enter ending for now.
            return;
        }

        resultStats.onEnterEnding(replayPlayback.isActive());

        // Persist unlock/score state first.
        UnlockFlags.tryUnlockExtraOnStoryClear(mStage, mGamemode, startLevel, continueSystem.getRemainingContinues(), runStartStage, startPracBoss);
        scoreSystem.commitScoreToProgress();

        // Stop gameplay and show ending.
        battlePaused = false;
        inGame = false;
        stageClearPanelActive = false;
        stageClearPhoto = null;

        replayRecording.stop();
        // Keep pending replay data (if any) for the final result replay-save prompt.

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        player = null;

        boolean qualifiesNextExtra = UnlockFlags.qualifiesNextExtraOnStoryClear(
                mStage, mGamemode, startLevel, continueSystem.getRemainingContinues(), runStartStage, startPracBoss);
        endingScreen.enter(currentChara, qualifiesNextExtra, imgs);
        endingActive = true;
    }

    private void convertEnemyBulletsToCrisisItems() {
        int power = (hud != null) ? hud.getPower() : 0;
        boolean bossActive = sBossf != 0;
        int max = bullets.getMax();
        for (int i = 0; i < max; i++) {
            if (!bullets.isActive(i) || bullets.isFromPlayer(i)) {
                continue;
            }
            int bx = bullets.getXFixed(i);
            int by = bullets.getYFixed(i);
            dropItems.spawn(DropItemSystem.TYPE_CRISIS, 0, 0, bx, by, mStage, power, bossActive);
            bullets.deactivate(i);
        }
    }

    private final BossController bossController;

    // Sprite resources.
    private BulletSprites bulletSprites;

    // Debug output strings.
    private String debugLine;
    private String debugLine2;
    private String debugLine3;
    private String debugLine4;
    private String debugLine5;

    // Global resource tables.
    private CcTable cc;
    private ImageBank imgs;

    private final ScoreSystem scoreSystem;

    private final ResultStats resultStats = ResultStats.INSTANCE;

    private final AudioSystem audioSystem = new AudioSystem();

    // UI/game state flags.
    private boolean inGame;
    private boolean battlePaused;

    // Ending screen state.
    private final EndingScreen endingScreen = new EndingScreen();
    private boolean endingActive;
    private int endingFadeToWhite;

    // Staff roll state.
    private final StaffRollScreen staffRollScreen = new StaffRollScreen();
    private boolean staffRollActive;

    private UiController ui;

    // Current player and pause UI.
    private Player player;
    private BattlePauseScreen pauseScreen;

    // Stage clear result panel state.
    private final StageClearResultPanel stageClearResultPanel = new StageClearResultPanel();
    private boolean stageClearPanelActive;
    private Image stageClearPhoto;

    // Spell practice end flow state.
    private final SpellPracticeEndScreen spellPracticeEndScreen = new SpellPracticeEndScreen();
    private boolean spellPracticeEndFlg;
    private int spellPracticeEndCnt;
    private boolean spellPracticeEndActive;
    private boolean spellPracticeEndWon;

    private int stageMissCount;
    private int stageBombUsedCount;
    private int stageSpellSeenCount;
    private int stageSpellBonusCount;
    private int stageLastSpellId;
    private int stageInitialContinueCount;

    // Run-wide result counters (used by final result panel after staff roll).
    private int runMissCount;
    private int runBombUsedCount;
    private int runSpellSeenCount;
    private int runSpellBonusCount;
    private int runInitialContinueCount;

    // Frame counters and input tracking.
    private int fi;
    private boolean shooting;

    private final ReplayRecordingController replayRecording = new ReplayRecordingController();
    private final ReplayPlaybackController replayPlayback = new ReplayPlaybackController();
    private final ReplayBtShareManager replayBtShare = new ReplayBtShareManager();
    private final ReplayTimeScaleController replayTimeScale = new ReplayTimeScaleController();

    private int timeStop;

    // Intro shatter effect state.
    private javax.microedition.lcdui.Image introShatterImg;
    private int introShatterCnt;
    private int introShatterParity;

    // Bomb and spell cut-in state.
    private int bombCnt;

    private int bossSpellCutInCnt;
    private int bossSpellCutInPrevBspellstep;
    private int bossSpellCutInStage;
    private int bossSpellCutInSpellId;
    private int bossSpellCutInChara;
    private int bossSpellCutInStartFi;
    private int bossSpellCutInPhaseStartFi;

    // Compat option: if disabled, cut-in overlay skips fade-in/fade-out.
    private boolean optDynamicCutInEnabled;

    // Compat options: alpha rendering toggles.
    private boolean optBombOverlayAlphaEnabled;
    private boolean optBossSpellCutInAlphaEnabled;
    private boolean optPlayerShotAlphaEnabled;

    // Compat option: if disabled, Stage2 blind mask switches instantly (no fade).
    private boolean optBlindMaskFadeEnabled;

    // Compat option: if disabled, background decorations and alpha overlays are skipped.
    private boolean optHeavyBgEnabled;

    // Compat option: render red hit spark rectangles (DoJa effectId 21).
    private boolean optHitSparkEnabled;

    // Debug options.
    private boolean optDebugFpsEnabled;
    private boolean optDebugPerfEnabled;
    private boolean optDebugResourceEnabled;

    // Stage/boss counters and status.
    private int sBossf;
    private int sBariacnt;
    private int mGcnt;
    private int mTacnt;
    private int ca;
    private int sDeadcnt;
    private int mResetf;

    // Stage script state.
    private int mStage;
    private int mStep;
    private boolean mLatterf;
    private boolean mBossmodef;

    // Game mode and barrier counters.
    private int mGamemode;
    private int sBbaria;
    private int sBstock;
    private int hFlash;
    private int iFlash;

    // Boss helper buffers.
    private final int[] sBossx = new int[3];
    private final int[] sBossy = new int[3];

    // Enemy/effect storage.
    private int[][] enemylist;
    private int[] mBossWrk;

    private boolean disableBossBombShieldCheat;
    private boolean disableItemCollectLimitCheat;

    private int[][] effectlist;

    // Per-frame camera origin offset (reset in BattleScene).
    int mOriginx;
    int mOriginy;

    private int currentChara;
    private int currentType;

    private int startGamemode;
    private int startLevel;
    private int startStage;
    // Stage index where this run started (used for unlock checks).
    private int runStartStage;
    private boolean startPracBoss;
    private boolean startSpellPractice;
    private int startSpellId;

    private int reimuBombExplosionId;

    private int bgMoveSpd;

    private BattleHudData hud;

    private String resourceError;

    private final MainMidlet midlet;

    // Convenience constructor.
    public GameCore() {
        this(null);
    }

    // Update boss spell cut-in state.
    private void bossSpellCutInTick() {
        int bspellstep = bossController.getBspellstep();

        if (!optDynamicCutInEnabled) {
            if (bossSpellCutInCnt != 0) {
                if (bspellstep != 1) {
                    bossSpellCutInCnt = 0;
                }
                bossSpellCutInPrevBspellstep = bspellstep;
                return;
            }

            if (bossSpellCutInPrevBspellstep == 0 && bspellstep == 1) {
                int mt = resolveBossMtForCutIn();
                bossSpellCutInCnt = BOSS_SPELL_CUTIN_PHASE_HOLD;
                bossSpellCutInStage = mStage;
                bossSpellCutInSpellId = bossController.getSpellId();
                bossSpellCutInChara = resolveBossCharaForCutIn(mStage, mt, bossSpellCutInSpellId);
                bossSpellCutInStartFi = fi;
                bossSpellCutInPhaseStartFi = fi;
            }

            bossSpellCutInPrevBspellstep = bspellstep;
            return;
        }

        if (bossSpellCutInCnt != 0) {
            int dt = fi - bossSpellCutInPhaseStartFi;
            if (dt < 0) {
                dt = 0;
            }

            if (bossSpellCutInCnt == BOSS_SPELL_CUTIN_PHASE_FADE_IN) {
                if (dt >= BOSS_SPELL_CUTIN_FADE_IN_FRAMES) {
                    bossSpellCutInCnt = BOSS_SPELL_CUTIN_PHASE_HOLD;
                }
            } else if (bossSpellCutInCnt == BOSS_SPELL_CUTIN_PHASE_FADE_OUT) {
                if (dt >= BOSS_SPELL_CUTIN_FADE_OUT_FRAMES) {
                    bossSpellCutInCnt = 0;
                }
            }

            if (bossSpellCutInCnt != 0 && bossSpellCutInCnt != BOSS_SPELL_CUTIN_PHASE_FADE_OUT && bspellstep != 1) {
                bossSpellCutInCnt = BOSS_SPELL_CUTIN_PHASE_FADE_OUT;
                bossSpellCutInPhaseStartFi = fi;
            }

            bossSpellCutInPrevBspellstep = bspellstep;
            return;
        }

        if (bossSpellCutInPrevBspellstep == 0 && bspellstep == 1) {
            int mt = resolveBossMtForCutIn();
            bossSpellCutInCnt = BOSS_SPELL_CUTIN_PHASE_FADE_IN;
            bossSpellCutInStage = mStage;
            bossSpellCutInSpellId = bossController.getSpellId();
            bossSpellCutInChara = resolveBossCharaForCutIn(mStage, mt, bossSpellCutInSpellId);
            bossSpellCutInStartFi = fi;
            bossSpellCutInPhaseStartFi = fi;
        }

        bossSpellCutInPrevBspellstep = bspellstep;
    }

    // Resolve cut-in portrait based on boss/spell.
    private static int resolveBossCharaForCutIn(int stage, int mt, int spellId) {
        if (stage != 0) {
            return 0;
        }

        if (spellId == 107) {
            return 108;
        }
        if (spellId == 108) {
            return 110;
        }
        if (spellId == 109) {
            return 109;
        }

        if (spellId >= 14 && spellId <= 17) {
            return 111;
        }

        if (spellId >= 6 && spellId <= 9) {
            return 110;
        }
        if (spellId >= 10 && spellId <= 13) {
            return 109;
        }
        if (spellId >= -2 && spellId <= 5) {
            return 108;
        }

        if (mt == 100 || mt == 101) {
            return 108;
        }
        if (mt == 102) {
            return 109;
        }
        if (mt == 103) {
            return 110;
        }
        return 0;
    }

    // Find boss enemy type for cut-in overlay.
    private int resolveBossMtForCutIn() {
        int idx = bossController.getHpBarTargetIndex();
        if (idx >= 0 && idx < 32 && enemylist[idx][0] != 0) {
            int mt = enemylist[idx][11];
            if (mt >= 100 && mt <= 103) {
                return mt;
            }
        }

        for (int i = 0; i < 32; i++) {
            if (enemylist[i][0] == 0) {
                continue;
            }
            int mt = enemylist[i][11];
            if (mt >= 100 && mt <= 103) {
                return mt;
            }
        }
        return 0;
    }

    // Initialize canvas resources and core systems.
    public GameCore(MainMidlet midlet) {
        super(false);
        setFullScreenMode(true);
        input = new InputSystem(this);
        battleRenderer = new BattleRenderer();
        battleRenderAccess = new BattleRenderAccess(this);
        battleScene = new BattleScene(battleRenderer, battleRenderAccess, audioSystem);
        sceneController = new SceneController(battleScene, endingScreen, staffRollScreen, stageClearResultPanel, audioSystem);
        this.midlet = midlet;
        vibration = new VibrationController(midlet);
        scoreSystem = new ScoreSystem();
        scoreSystem.setProgress(GameProgress.INSTANCE);
        scoreSystem.setListener(new ScoreSystem.Listener() {
            public void onLifeOrBombIncreased() {
                audioSystem.playSeType(SoundEffectSystem.SE_LIFE);
            }
        });
        continueSystem = new BattleContinueSystem(GameProgress.INSTANCE);
        random = new ReplayRng();
        random.setState((int) System.currentTimeMillis());
        bullets = new BulletSystem(1024);
        enemyBullets = new EnemyBulletSystem(bullets);
        enemies = new EnemySystem();

        stageDochu = new StageDochu(enemyBullets);

        StageRuntime.StageFlow stageFlow = new BattleStageFlow(this);

        stageRuntime = new StageRuntime(new BattleStageHost(this, stageFlow, audioSystem));

        enemylist = stageRuntime.getEnemyList();
        mBossWrk = stageRuntime.getBossWrk();
        effectlist = stageRuntime.getEffectList();

        battleEngine = new BattleEngine(bullets, enemyBullets, scoreSystem, new BattleEngine.Host() {
            public void spawnStageEffect(int effectId, int xFixed, int yFixed) {
                stageKcEffect(effectId, xFixed, yFixed);
            }

            public void spawnEffectEntry(int effectId, int xFixed, int yFixed) {
                stageKc(effectId, xFixed, yFixed);
            }

            public void setResetFlag(int value) {
                mResetf = value;
            }

            public void setFlash(int value) {
                hFlash = value;
            }

            public void setTargetFps(int fps) {
                stageHostSetTargetFps(fps);
            }

            public void onEnemyKilled(int enemyIndex) {
                GameCore.this.stageJb(enemyIndex);
            }

            public void playSound(int soundId, boolean loop) {
                if (loop) {
                    if (stageClearPanelActive) {
                        return;
                    }
                    audioSystem.requestBgm(soundId, true);
                    return;
                }
                if (soundId == SoundEffectSystem.SE_CRASH) {
                    // Player hit: vibrate on death.
                    GameCore.this.vibration.requestFrames(5);
                }
                audioSystem.playSeType(soundId);
            }

            public void onBombTick(int d0, int bombCntRemaining) {
                GameCore.this.bombCnt = bombCntRemaining;
                GameCore.this.bombTick(d0);
            }
        });

        dropItems = new DropItemSystem();
        dropItems.setListener(new DropItemSystem.Listener() {
            public void addPower(int amount) {
                if (!(hud instanceof BattleHudModel)) {
                    return;
                }
                BattleHudModel m = (BattleHudModel) hud;
                int old = m.getPower();
                int p = old + amount;
                if (p > 128) {
                    p = 128;
                }
                if (p < 0) {
                    p = 0;
                }

                int oldStage = old >> 5;
                int newStage = p >> 5;
                if (newStage > oldStage && p < 128) {
                    audioSystem.playSeType(SoundEffectSystem.SE_POWER);
                }
                if (p >= 128) {
                    if (old < 128) {
                        audioSystem.playSeType(SoundEffectSystem.SE_POWER);
                        if (amount != 128) {
                            mResetf = 2;
                        } else {
                            mResetf = 3;
                        }
                        stageKcEffect(16, 3342336, 7274496);
                        stageKcEffect(17, 5963776, 7274496);
                        stageKcEffect(18, 9109504, 7274496);
                    }
                    p = 128;
                    scoreSystem.post(ScoreSystem.EVT_ADD_SCORE, 300, 0, 0);
                }
                m.setPower(p);
            }

            public void addBomb(int amount) {
                if (!(hud instanceof BattleHudModel)) {
                    return;
                }
                scoreSystem.post(ScoreSystem.EVT_ADD_BOMB, amount, 0, 0);
            }

            public void addPlayer(int amount) {
                if (!(hud instanceof BattleHudModel)) {
                    return;
                }
                scoreSystem.post(ScoreSystem.EVT_ADD_PLAYER, amount, 0, 0);
            }

            public void addScore(int amount) {
                scoreSystem.post(ScoreSystem.EVT_ADD_SCORE, amount, 0, 0);
            }

            public void onScoreItemCollected() {
                scoreSystem.post(ScoreSystem.EVT_SCORE_ITEM_COLLECTED, 0, 0, 0);
            }
        });

        bossController = new BossController(battleScene.createBossHost(this, stageRuntime));
        bossController.reset();

        try {
            CcTable cc;
            try {
                cc = CcTable.loadFromResource("/res/cc.dat");
            } catch (Exception e) {
                cc = CcTable.loadFromResource("/cc.dat");
            }
            ImageBank imgs = new ImageBank(95);
            bulletSprites = new BulletSprites(cc, imgs);
            bullets.setSprites(bulletSprites);
            bullets.setEnemies(enemies);
            bullets.setEffectSpawner(new BulletSystem.EffectSpawner() {
                public void spawnEffect(int effectId, int xFixed, int yFixed) {
                    stageKc(effectId, xFixed, yFixed);
                }
            });

            this.cc = cc;
            this.imgs = imgs;

            reimuBombExplosionId = 457;

            inGame = false;

            int[] opt = GameOptions.load();
            if (opt == null) {
                opt = GameOptions.defaults();
            }

            I18nBootstrap.initFromOptions(opt);

            audioSystem.setVolumes(opt[1], opt[2]);
            if (opt.length > GameOptions.IDX_SE_MASK_HI) {
                int seMask = (opt[GameOptions.IDX_SE_MASK] & 0xFF) | ((opt[GameOptions.IDX_SE_MASK_HI] & 0xFF) << 8);
                audioSystem.setSeEnabledMask14(seMask);
            }
            // Title BGM (00): request once; actual start is driven by polling tick().
            audioSystem.requestBgm(0, true);

            ui = new UiController(imgs, bulletSprites, new UiController.Listener() {
                public void onStartGameRequested(int gamemode, int level, int chara, int type, int startStage, boolean pracBoss) {
                    startGame(gamemode, level, chara, type, startStage, pracBoss);
                }

                public void onStartReplayRequested(int slot, boolean bossOnly) {
                    startReplay(slot, bossOnly);
                }

                public void onReplayBtShareRequested(int slot, boolean receive) {
                    replayBtShare.start(slot, receive, ui);
                }

                public void onStartSpellPracticeRequested(int chara, int type, int stageIndex, int spellId) {
                    startSpellPractice(chara, type, stageIndex, spellId);
                }

                public void onQuitRequested() {
                    requestQuit();
                }

                public void onPlayBgmRequested(int trackId) {
                    audioSystem.requestBgm(trackId, true);
                }

                public void onPlaySeTypeRequested(int seType) {
                    audioSystem.playSeType(seType);
                }

                public void onVolumesChanged(int bgmVol, int seVol) {
                    audioSystem.setVolumes(bgmVol, seVol);
                }

                public void onSeMaskChanged(int mask) {
                    audioSystem.setSeEnabledMask14(mask);
                }

                public void onMusicRoomVolumeChanged(int bgmVol) {
                    audioSystem.setBgmVolumeOverride(bgmVol);
                }
            });

            pauseScreen = new BattlePauseScreen();
            battlePaused = false;

            mOriginx = 0;
            mOriginy = 0;

            input.refreshKeyOptions();

            StringBuffer sb = new StringBuffer();
            sb.append("test bulletId:");
            debugLine = sb.toString();

            debugLine2 = "exp=" + reimuBombExplosionId + " " + formatCcRow(cc, reimuBombExplosionId);
            debugLine3 = formatCcRow(cc, 83);
            debugLine4 = formatCcRow(cc, 93);
            debugLine5 = formatCcRow(cc, 0);
        } catch (Exception e) {
            resourceError = e.toString();
        }
    }

    // Inject HUD implementation from UI layer.
    public void setBattleHudData(BattleHudData hud) {
        this.hud = hud;
    }

    // Entry point for new game start.
    private void startGame(int gamemode, int level, int chara, int type, int startStage, boolean pracBoss) {
        audioSystem.requestBgm(-1, false);
        beginIntroShatterFromUi();
        startGameInternal(gamemode, level, chara, type, startStage, pracBoss);
    }

    // Entry point for replay playback.
    private void startReplay(int slot, boolean bossOnly) {
        audioSystem.requestBgm(-1, false);
        beginIntroShatterFromUi();

        replayTimeScale.reset();

        // DoJa: disable progress writes during replay playback (!di guards).
        scoreSystem.setProgressWriteEnabled(false);

        ReplayPlaybackController.StartParams sp = replayPlayback.startFromSlot(slot, bossOnly);
        if (sp == null) {
            scoreSystem.setProgressWriteEnabled(true);
            if (ui != null) {
                ui.returnToReplayKeepCursor();
            }
            return;
        }

        if (sp.gamemode == 3) {
            startSpellPracticeInternal(sp.chara, sp.type, sp.startStage, sp.spellId);
        } else {
            startGameInternal(sp.gamemode, sp.level, sp.chara, sp.type, sp.startStage, false);
        }

        // Disable recording during playback.
        replayRecording.stop();

        if (!bossOnly) {
            boolean ok = ReplayStageSnapshotService.loadAndApplyFromSlot(slot, random, continueSystem, hud, scoreSystem, player);
            if (!ok) {
                abortReplayToMenu("Invalid stage snapshot.");
                return;
            }
        }

        if (bossOnly) {
            int stageIndex = sp.startStage;
            if (ReplayBossOnlyPolicy.isFastForwardBossOnlyStage(stageIndex)) {
                boolean ok = ReplayStageSnapshotService.loadAndApplyFromSlot(slot, random, continueSystem, hud, scoreSystem, player);
                if (!ok) {
                    abortReplayToMenu("Invalid stage snapshot.");
                    return;
                }

                replayTimeScale.startAutoFastForwardToFrame(replayPlayback.getBossStartFrame());
                return;
            }

            stageCe();
            ensureTalkTableLoaded();

            ReplayBossSnapshotCodec.Decoded d = ReplayBossSnapshotService.loadAndApplyFromSlot(
                    slot, random, continueSystem, hud, scoreSystem, player, cc, currentChara, currentType);
            if (d == null) {
                abortReplayToMenu("Invalid boss snapshot.");
                return;
            }

            fi = d.fi;
            bombCnt = d.bombCnt;
            sBariacnt = d.sBariacnt;
            sDeadcnt = d.sDeadcnt;
            mTacnt = d.mTacnt;
            shooting = d.shooting;
            ca = d.ca;
            timeStop = d.timeStop;
            mResetf = d.mResetf;

            stageMissCount = d.stageMissCount;
            stageBombUsedCount = d.stageBombUsedCount;
            stageSpellSeenCount = d.stageSpellSeenCount;
            stageSpellBonusCount = d.stageSpellBonusCount;
            stageLastSpellId = d.stageLastSpellId;
            stageInitialContinueCount = d.stageInitialContinueCount;
        }
    }

    private void endReplayToMenu() {
        replayPlayback.stop();
        replayTimeScale.reset();

        // Restore progress writes after playback.
        scoreSystem.setProgressWriteEnabled(true);

        battlePaused = false;
        inGame = false;

        stageClearPanelActive = false;
        stageClearPhoto = null;
        replayRecording.clearPending();

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        player = null;
        if (ui != null) {
            ui.returnToReplayKeepCursor();
        }
    }

    private void abortReplayToMenu(String msg) {
        replayPlayback.stop();
        replayTimeScale.reset();

        // Restore progress writes after playback.
        scoreSystem.setProgressWriteEnabled(true);

        battlePaused = false;
        inGame = false;

        stageClearPanelActive = false;
        stageClearPhoto = null;
        replayRecording.clearPending();

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        player = null;

        if (ui != null) {
            ui.returnToReplayKeepCursorWithMessage(msg);
        }
    }

    // Initialize game state for standard play.
    private void startGameInternal(int gamemode, int level, int chara, int type, int startStage, boolean pracBoss) {
        targetFps = TARGET_FPS;
        //Upon defeating the boss, the game enters a 5 FPS slow-motion (bullet time) state. But if the player's ship is destroyed simultaneously, the frame rate gets stuck and won't return to normal.
        startGamemode = gamemode;
        startLevel = level;
        this.startStage = startStage;
        runStartStage = startStage;
        startPracBoss = pracBoss;
        startSpellPractice = false;
        startSpellId = 0;

        resultStats.onRunStart(replayPlayback.isActive(), gamemode, level);

        currentChara = chara;
        currentType = type;

        inGame = true;
        battleEngine.reset();
        continueSystem.resetForNewRun(gamemode, false);

        resetRunResultCounters();

        stageInitialContinueCount = continueSystem.getRemainingContinues();
        resetStageResultCounters();
        stageClearPanelActive = false;
        stageClearPhoto = null;
        replayRecording.clearPending();

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        hud = createHudForStart(gamemode, level, chara, type, startStage, pracBoss);
        syncScoreSystemForHud();
        battlePaused = false;
        bgMoveSpd = 0;
        fi = 0;
        shooting = true;

        replayRecording.startForNewRun();

        bombCnt = 0;
        bossSpellCutInCnt = 0;
        bossSpellCutInPrevBspellstep = 0;
        bossSpellCutInStage = 0;
        bossSpellCutInSpellId = -1;
        bossSpellCutInChara = 0;
        bossSpellCutInStartFi = 0;
        mOriginx = 0;
        mOriginy = 0;
        sBossf = 0;
        sBariacnt = 0;
        mGcnt = 0;
        mTacnt = 0;
        ca = 0;
        sDeadcnt = 0;
        mResetf = 0;

        mGamemode = gamemode;
        mStage = startStage;
        mStep = 0;
        mLatterf = false;
        mBossmodef = false;
        stageRuntime.setTalkCnt(3);
        sBbaria = 0;
        sBstock = 0;
        for (int i = 0; i < 3; i++) {
            sBossx[i] = 0;
            sBossy[i] = 0;
        }
        stageIb();
        ensureStageDatLoaded();
        ensureTalkTableLoaded();

        if (startSpellPractice) {
            initSpellPracticeBoss();
        }

        if (gamemode == 2 && pracBoss) {
            stageCe();
            ensureTalkTableLoaded();
        }

        input.refreshKeyOptions();
        if (cc != null) {
            player = new Player(cc, currentChara, currentType, PLAY_X + (PLAY_W / 2), PLAY_Y + PLAY_H - 28);
        }

        if (!replayPlayback.isActive()) {
            replayRecording.captureStageStartSnapshot(random, continueSystem, hud, scoreSystem, player);
        }
    }

    // Resolve spell practice boss BGM track id.
    private static int resolveSpellPracticeBossBgmTrack(int spellId) {
        if (spellId < 0 || spellId >= SPELLPRACTICE_INFO.length) {
            return -1;
        }

        // SpellPracticeScreen.LastWord range is [107, 116).
        if (spellId >= 107) {
            return 17;
        }

        int stage = SPELLPRACTICE_INFO[spellId][0];
        if (stage < 0) {
            return -1;
        }
        if (stage > 6) {
            stage = 6;
        }
        return 2 + (stage * 2);
    }

    // Reset battle state after a loss or retry (also clears transient battle engine state).
    public void restartBattle() {
        resultStats.onRetry(replayPlayback.isActive());
        introShatterImg = null;
        introShatterCnt = 0;

        // Spell practice end flow: reset state on retry.
        spellPracticeEndFlg = false;
        spellPracticeEndCnt = 0;
        spellPracticeEndActive = false;
        spellPracticeEndWon = false;

        stageInitialContinueCount = continueSystem.getRemainingContinues();
        resetStageResultCounters();
        stageClearPanelActive = false;
        stageClearPhoto = null;
        replayRecording.clearPending();

        if (startSpellPractice) {
            hud = createHudForSpellPractice(currentChara, currentType, startSpellId);
        } else {
            hud = createHudForStart(startGamemode, startLevel, currentChara, currentType, startStage, startPracBoss);
        }

        syncScoreSystemForHud();
        battleEngine.reset();

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        bgMoveSpd = 0;
        fi = 0;
        shooting = true;
        battlePaused = false;

        replayRecording.startForRetry(startSpellPractice);

        bombCnt = 0;
        bossSpellCutInCnt = 0;
        bossSpellCutInPrevBspellstep = 0;
        bossSpellCutInStage = 0;
        bossSpellCutInSpellId = -1;
        bossSpellCutInChara = 0;
        bossSpellCutInStartFi = 0;
        mOriginx = 0;
        mOriginy = 0;
        sBossf = 0;
        sBariacnt = 0;
        mGcnt = 0;
        mTacnt = 0;
        ca = 0;
        sDeadcnt = 0;
        mResetf = 0;

        mGamemode = startSpellPractice ? 3 : startGamemode;
        mStage = startStage;
        mStep = 0;
        mLatterf = false;
        mBossmodef = false;
        stageRuntime.setTalkCnt(3);
        sBbaria = 0;
        sBstock = 0;
        for (int i = 0; i < 3; i++) {
            sBossx[i] = 0;
            sBossy[i] = 0;
        }
        stageIb();
        ensureStageDatLoaded();
        ensureTalkTableLoaded();

        if (startSpellPractice) {
            initSpellPracticeBoss();
        }

        if (!startSpellPractice && startGamemode == 2 && startPracBoss) {
            stageCe();
            ensureTalkTableLoaded();
        }

        if (player != null) {
            player.setSlow(false);
        }

        if (cc != null) {
            player = new Player(cc, currentChara, currentType, PLAY_X + (PLAY_W / 2), PLAY_Y + PLAY_H - 28);
        }

        if (!replayPlayback.isActive()) {
            replayRecording.captureStageStartSnapshot(random, continueSystem, hud, scoreSystem, player);
        }
    }

    // Entry point for spell practice mode.
    private void startSpellPractice(int chara, int type, int stageIndex, int spellId) {
        audioSystem.requestBgm(-1, false);
        beginIntroShatterFromUi();

        startSpellPracticeInternal(chara, type, stageIndex, spellId);
    }

    private void startSpellPracticeInternal(int chara, int type, int stageIndex, int spellId) {
        targetFps = TARGET_FPS;

        // Spell practice end flow: reset state on new run.
        spellPracticeEndFlg = false;
        spellPracticeEndCnt = 0;
        spellPracticeEndActive = false;
        spellPracticeEndWon = false;

        startGamemode = 0;
        startLevel = 0;
        int realStage = stageIndex;
        if (spellId >= 0 && spellId < SPELLPRACTICE_INFO.length) {
            realStage = SPELLPRACTICE_INFO[spellId][0];
        }
        startStage = realStage;
        startPracBoss = true;
        startSpellPractice = true;
        startSpellId = spellId;

        resultStats.onSpellPracticeStart(replayPlayback.isActive());

        currentChara = chara;
        currentType = type;

        inGame = true;
        battleEngine.reset();
        continueSystem.resetForNewRun(0, true);

        stageInitialContinueCount = continueSystem.getRemainingContinues();
        resetStageResultCounters();
        stageClearPanelActive = false;
        stageClearPhoto = null;
        replayRecording.clearPending();

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        hud = createHudForSpellPractice(chara, type, spellId);
        syncScoreSystemForHud();
        battlePaused = false;
        bgMoveSpd = 0;
        fi = 0;
        shooting = true;

        replayRecording.startForSpellPractice();

        bombCnt = 0;
        bossSpellCutInCnt = 0;
        bossSpellCutInPrevBspellstep = 0;
        bossSpellCutInStage = 0;
        bossSpellCutInSpellId = -1;
        bossSpellCutInChara = 0;
        bossSpellCutInStartFi = 0;
        mOriginx = 0;
        mOriginy = 0;
        sBossf = 0;
        sBariacnt = 0;
        mGcnt = 0;
        mTacnt = 0;
        ca = 0;
        sDeadcnt = 0;
        mResetf = 0;

        mGamemode = 3;
        mStage = realStage;
        mStep = 0;
        mLatterf = false;
        mBossmodef = false;
        stageRuntime.setTalkCnt(3);
        sBbaria = 0;
        sBstock = 0;
        for (int i = 0; i < 3; i++) {
            sBossx[i] = 0;
            sBossy[i] = 0;
        }
        stageIb();
        ensureStageDatLoaded();
        ensureTalkTableLoaded();

        initSpellPracticeBoss();

        // Spell practice uses the boss BGM for the selected stage; LastWord uses track 17.
        int bossBgm = resolveSpellPracticeBossBgmTrack(startSpellId);
        if (bossBgm >= 0) {
            GameProgress.unlockMusicPlayed(bossBgm);
            audioSystem.requestBgm(bossBgm, true);
        }

        input.refreshKeyOptions();
        if (cc != null) {
            player = new Player(cc, currentChara, currentType, PLAY_X + (PLAY_W / 2), PLAY_Y + PLAY_H - 28);
        }

        if (!replayPlayback.isActive()) {
            replayRecording.captureStageStartSnapshot(random, continueSystem, hud, scoreSystem, player);
        }
    }

    // Spawn boss setup for spell practice.
    private void initSpellPracticeBoss() {
        if (startSpellId < 0 || startSpellId >= SPELLPRACTICE_INFO.length) {
            return;
        }

        int[] r = SPELLPRACTICE_INFO[startSpellId];
        int enemy = r[2];

        boolean b = false;
        int n4 = -1;

        sBossx[0] = 97;
        sBossy[0] = 56;

        switch (enemy) {
            case 130:
                n4 = stageFb(9, 130, 3000, 0, 100, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                b = true;
                break;
            case 108:
            case 109:
            case 110:
            case 111:
                sBossx[0] = 97;
                sBossy[0] = 68;
                sBossx[1] = 30;
                sBossy[1] = 48;
                sBossx[2] = 164;
                sBossy[2] = 48;
                if (enemy == 108) {
                    sBossx[1] = -30;
                    sBossx[2] = 30;
                    sBossy[2] = -42;
                }
                if (enemy == 109) {
                    sBossx[1] = 97;
                    sBossy[1] = 68;
                    sBossx[0] = 164;
                    sBossy[0] = -42;
                    sBossx[2] = 30;
                    sBossy[2] = -42;
                }
                if (enemy == 110) {
                    sBossx[2] = 97;
                    sBossy[2] = 68;
                    sBossx[0] = 164;
                    sBossy[0] = -32;
                    sBossx[1] = 30;
                    sBossy[1] = -32;
                }
                int i0 = stageFb(9, 108, 3000, 0, 101, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                if (i0 >= 0) {
                    enemylist[i0][16] = 1;
                }
                int i1 = stageFb(11, 109, 3000, 0, 102, 180, 5, sBossx[1] << 16, sBossy[1] << 16);
                if (i1 >= 0) {
                    enemylist[i1][16] = 1;
                }
                n4 = stageFb(10, 110, 3000, 0, 103, 180, 5, sBossx[2] << 16, sBossy[2] << 16);
                if (n4 >= 0) {
                    enemylist[n4][16] = 1;
                }
                break;
            case 112:
                n4 = stageFb(13, 112, 3000, 0, 101, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                break;
            case 132:
                n4 = stageFb(14, 132, 3000, 0, 100, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                b = true;
                break;
            case 113:
                n4 = stageFb(14, 113, 3000, 0, 101, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                break;
            case 114:
                n4 = stageFb(15, 114, 3000, 0, 101, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                break;
            case 115:
                n4 = stageFb(16, 115, 3000, 0, 101, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                break;
            case 116:
                n4 = stageFb(17, 116, 3000, 0, 101, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                break;
            case 117:
                n4 = stageFb(19, 117, 3000, 0, 101, 180, 5, sBossx[0] << 16, sBossy[0] << 16);
                break;
        }

        if (n4 >= 0) {
            enemylist[n4][16] = 1;
        }
        if (!b) {
            sBossf = 2;
            sBbaria = 1;
            mBossmodef = true;
        } else if (n4 >= 0) {
            if (mStage == 0 && hud instanceof BattleHudModel) {
                ((BattleHudModel) hud).setPower(28);
            }
            enemylist[n4][7] = 1;
        }
    }

    // Build HUD data for spell practice.
    private static BattleHudModel createHudForSpellPractice(int chara, int type, int spellId) {
        int stage = 0;
        int level = 0;
        int enemy = 0;
        if (spellId >= 0 && spellId < SPELLPRACTICE_INFO.length) {
            int[] r = SPELLPRACTICE_INFO[spellId];
            stage = r[0];
            level = r[1];
            enemy = r[2];
        }

        BattleHudModel m = new BattleHudModel();
        m.setLevel(level);
        m.setPlayer(1);
        m.setBomb(0);
        m.setHiScore(0);
        m.setScore(0);
        m.setGraze(0);

        int power = 128;
        if (stage == 0) {
            power = 64;
        }
        if (level == 5) {
            power = 128;
        }
        if (stage == 0 && enemy == 130) {
            power = 28;
        }
        m.setPower(power);
        return m;
    }

    // Build HUD data for standard game start.
    private static BattleHudModel createHudForStart(int gamemode, int level, int chara, int type, int startStage, boolean pracBoss) {
        GameProgress.INSTANCE.loadFromSp();
        int[] opt = GameOptions.load();
        if (opt == null) {
            opt = GameOptions.defaults();
        }

        BattleHudModel m = new BattleHudModel();
        m.setLevel(level);

        int player;
        int bomb;
        if (gamemode == 0) {
            player = opt[0];
            bomb = 3;
        } else if (gamemode == 1) {
            player = 3;
            bomb = 3;
        } else if (gamemode == 2) {
            player = 7;
            bomb = 3;
        } else {
            player = 1;
            bomb = 0;
        }
        m.setPlayer(player);
        m.setBomb(bomb);

        int unit = (chara << 1) + type;
        int hi = 0;
        if (gamemode != 3) {
            if (unit >= 0 && unit < GameProgress.INSTANCE.scoreList.length) {
                int[] scores = GameProgress.INSTANCE.scoreList[unit];
                int idx = level * 3;
                if (scores != null && idx >= 0 && idx < scores.length) {
                    hi = scores[idx];
                }
            }
        }
        m.setHiScore(hi);
        m.setScore(0);

        int power = 0;
        if (startStage == 0) {
            power = 0;
            if (pracBoss) {
                power = 70;
            }
        } else if (startStage == 1) {
            power = 70;
            if (pracBoss) {
                power = 128;
            }
        } else if (startStage == 6) {
            power = 0;
        } else {
            power = 128;
        }
        m.setPower(power);
        m.setGraze(0);
        return m;
    }

    // Quit to midlet and stop the loop.
    private void requestQuit() {
        stop();
        if (midlet != null) {
            try {
                midlet.notifyDestroyed();
            } catch (Throwable t) {
            }
        }
    }

    // Build debug text for boss summon state.
    private String buildSummonDebugLine(int bossWrkIndex) {
        if (bossWrkIndex < 0 || bossWrkIndex >= mBossWrk.length) {
            return "wrk" + bossWrkIndex + "=<oob>";
        }
        int si = mBossWrk[bossWrkIndex];
        if (si < 0 || si >= enemylist.length) {
            return "wrk" + bossWrkIndex + "=" + si;
        }
        if (enemylist[si][0] == 0) {
            return "wrk" + bossWrkIndex + "=" + si + " dead";
        }
        return "wrk" + bossWrkIndex + "=" + si
                + " id=" + enemylist[si][1]
                + " mt=" + enemylist[si][11]
                + " x=" + (enemylist[si][5] >> 16)
                + " y=" + (enemylist[si][6] >> 16);
    }

    // Format a cc.dat row for debug output.
    private static String formatCcRow(CcTable cc, int bulletId) {
        if (!cc.hasSpriteMeta(bulletId)) {
            return "cc[" + bulletId + "]=<none>";
        }
        int imgIndex = cc.getImgIndex(bulletId);
        int srcX = cc.getSrcX(bulletId);
        int srcY = cc.getSrcY(bulletId);
        int w = cc.getW(bulletId);
        int h = cc.getH(bulletId);
        int ax = cc.getAx(bulletId);
        int ay = cc.getAy(bulletId);

        StringBuffer sb = new StringBuffer();
        sb.append("cc[");
        sb.append(bulletId);
        sb.append("] img=");
        sb.append(imgIndex);
        sb.append(" src=");
        sb.append(srcX);
        sb.append(',');
        sb.append(srcY);
        sb.append(" wh=");
        sb.append(w);
        sb.append('x');
        sb.append(h);
        sb.append(" a=");
        sb.append(ax);
        sb.append(',');
        sb.append(ay);
        return sb.toString();
    }

    // Start the main loop thread.
    public void start() {
        if (running) {
            paused = false;
            audioSystem.resumeAll();
            return;
        }
        running = true;
        paused = false;
        loop = new Thread(this);
        loop.start();
    }

    // Pause the main loop updates.
    public void pause() {
        paused = true;
        audioSystem.pauseAll();
    }

    // Stop the main loop thread.
    public void stop() {
        running = false;
        paused = false;
        audioSystem.stopAll();
    }

    // Main loop timing and render dispatch.
    public void run() {
        long last = System.currentTimeMillis();
        while (running) {
            int frameMs = TARGET_FRAME_MS;
            if (inGame) {
                int fps = targetFps;
                if (fps <= 0) {
                    fps = TARGET_FPS;
                }
                frameMs = 1000 / fps;
                if (frameMs <= 0) {
                    frameMs = 1;
                }
            }

            long now = System.currentTimeMillis();
            long dt = now - last;
            if (dt < frameMs) {
                try {
                    Thread.sleep(frameMs - dt);
                } catch (InterruptedException e) {
                }
                continue;
            }
            // Limit catch-up to 2 steps to avoid CPU bursts that starve
            // the MIDI synthesis thread and cause cascading stutter.
            int steps = 0;
            long frameStart = now;
            long updateTotal = 0;
            while (dt >= frameMs) {
                if (!paused) {
                    try {
                        long u0 = System.currentTimeMillis();
                        update();
                        updateTotal += System.currentTimeMillis() - u0;
                    } catch (Throwable t) {
                        resourceError = String.valueOf(t);
                        paused = true;
                    }
                }
                last += frameMs;
                dt = now - last;
                steps++;
                if (steps >= 2) {
                    last = now;
                    break;
                }
            }

            long r0 = System.currentTimeMillis();
            try {
                render();
            } catch (Throwable t) {
                resourceError = String.valueOf(t);
                running = false;
            }
            long r1 = System.currentTimeMillis();

            // Frame spike detector (always active, lightweight).
            int totalFrameMs = (int) (r1 - frameStart);
            if (totalFrameMs >= 100) {
                Debug.recordFrameSpike(totalFrameMs, (int) updateTotal, (int) (r1 - r0), 0, lastAudioMs);
            }

            // Yield after render to give audio/system threads CPU time,
            // reducing frame-time spikes when MIDI synthesis is active.
            Thread.yield();
        }
    }

    private int audioOptPollCnt;

    private int lastUiState = UiController.STATE_TITLE;

    private void tickAudioSystem() {
        if (stageClearPanelActive) {
            audioSystem.requestBgm(-1, false);
            return;
        }
        // Poll options at low frequency in UI to avoid per-frame RMS overhead.
        if (!inGame) {
            audioOptPollCnt--;
            if (audioOptPollCnt <= 0) {
                audioOptPollCnt = 30;
                int[] opt = GameOptions.load();
                if (opt == null) {
                    opt = GameOptions.defaults();
                }
                audioSystem.setVolumes(opt[1], opt[2]);
                if (opt.length > GameOptions.IDX_SE_MASK_HI) {
                    int seMask = (opt[GameOptions.IDX_SE_MASK] & 0xFF) | ((opt[GameOptions.IDX_SE_MASK_HI] & 0xFF) << 8);
                    audioSystem.setSeEnabledMask14(seMask);
                }
            }
        }

        // Scene-based BGM routing (kept outside core logic).
        if (endingActive) {
            GameProgress.unlockMusicPlayed(15);
            audioSystem.requestBgm(15, false);
        } else if (staffRollActive) {
            GameProgress.unlockMusicPlayed(16);
            audioSystem.requestBgm(16, false);
        } else if (!inGame) {
            int st = (ui != null) ? ui.getState() : UiController.STATE_TITLE;

            if (lastUiState == UiController.STATE_MUSIC_ROOM && st != UiController.STATE_MUSIC_ROOM) {
                audioSystem.clearBgmVolumeOverride();
            }

            if (st == UiController.STATE_MUSIC_ROOM) {
                if (lastUiState != UiController.STATE_MUSIC_ROOM) {
                    audioSystem.requestBgm(-1, false);
                }
                // Music room controls BGM explicitly. (no default playback)
            } else if (st == UiController.STATE_TITLE || st == UiController.STATE_START_SETUP) {
                GameProgress.unlockMusicPlayed(0);
                audioSystem.requestBgm(0, true);
            } else {
                audioSystem.requestBgm(-1, false);
                lastUiState = st;
                return;
            }

            lastUiState = st;
        }
        // In-game: stage scripts (op 251) will request BGM via StageRuntime.Host.

        audioSystem.tick();
    }

    private int lastAudioMs;

    // Per-frame game logic update.
    private void update() {
        long a0 = System.currentTimeMillis();
        tickAudioSystem();
        lastAudioMs = (int) (System.currentTimeMillis() - a0);
        vibration.tick();
        InputSystem.FrameInput frameInput = input.poll(
                inGame,
                battlePaused,
                stageClearPanelActive,
                startSpellPractice,
                spellPracticeEndActive);
        int liveKeys = frameInput.keys;
        int keys = liveKeys;
        int pressed = frameInput.pressed;
        int fireActionPressed = frameInput.fireActionPressed;

        if (introShatterCnt > 0) {
            introShatterCnt -= 2;
            if (introShatterCnt <= 0) {
                introShatterCnt = 0;
                introShatterImg = null;
            }
        }

        if (replayPlayback.wantsQuitToMenu(pressed)) {
            endReplayToMenu();
            return;
        }

        if (replayPlayback.isActive() && inGame && !battlePaused && !stageClearPanelActive) {
            int steps = replayTimeScale.computeSteps(liveKeys, replayPlayback);
            if (steps <= 0) {
                return;
            }

            for (int i = 0; i < steps; i++) {
                ReplayPlaybackController.FrameInput r = replayPlayback.nextFrame();
                if (r == null) {
                    endReplayToMenu();
                    return;
                }

                keys = r.keys;
                pressed = r.pressed;
                fireActionPressed = r.fireActionPressed;

                long bs0 = System.currentTimeMillis();
                if (battleScene.update(this, keys, pressed, fireActionPressed)) {
                    Debug.recordBattleSceneMs((int) (System.currentTimeMillis() - bs0));
                } else {
                    Debug.recordBattleSceneMs((int) (System.currentTimeMillis() - bs0));
                    sceneController.update(this, keys, pressed);
                }

            }
            return;
        }

        if (inGame && !battlePaused) {
            if (endingFadeToWhite == 0 && !stageClearPanelActive && !replayPlayback.isActive() && !endingActive && !staffRollActive ) {
                // Do not record the pause-trigger frame (left softkey / GAME_A).
                // The game returns early and does not advance gameplay state on that frame.
                if ((pressed & GAME_A_PRESSED) == 0) {
                    if (!startSpellPractice || (!spellPracticeEndFlg && !spellPracticeEndActive)) {
                        replayRecording.onFrame(keys, pressed, fireActionPressed);
                    }
                }
            }
        }

        long bs0 = System.currentTimeMillis();
        if (battleScene.update(this, keys, pressed, fireActionPressed)) {
            Debug.recordBattleSceneMs((int) (System.currentTimeMillis() - bs0));
            return;
        }
        Debug.recordBattleSceneMs((int) (System.currentTimeMillis() - bs0));
        sceneController.update(this, keys, pressed);
    }

    public boolean battleFlowIsSpellPractice() {
        return startSpellPractice;
    }

    public int battleFlowGetEndingFadeToWhite() {
        return endingFadeToWhite;
    }

    public void battleFlowSetEndingFadeToWhite(int v) {
        endingFadeToWhite = v;
    }

    public boolean battleFlowIsStageClearPanelActive() {
        return stageClearPanelActive;
    }

    public void battleFlowSetStageClearPanelActive(boolean v) {
        stageClearPanelActive = v;
    }

    public void battleFlowClearStageClearPhoto() {
        stageClearPhoto = null;
    }

    public boolean battleFlowIsSpellPracticeEndActive() {
        return spellPracticeEndActive;
    }

    public boolean battleFlowIsSpellPracticeEndFlg() {
        return spellPracticeEndFlg;
    }

    public boolean battleFlowTickSpellPracticeEndCountdownIfNeeded() {
        if (!startSpellPractice) {
            return false;
        }
        if (!spellPracticeEndFlg) {
            return false;
        }
        if (spellPracticeEndActive) {
            return false;
        }
        if (battlePaused || stageClearPanelActive) {
            return false;
        }

        spellPracticeEndCnt--;
        if (spellPracticeEndCnt > 0) {
            return false;
        }

        stageHostSetTargetFps(16);
        enterSpellPracticeEndScreen();
        return true;
    }

    public boolean battleFlowIsReplayPlaybackActive() {
        return replayPlayback.isActive();
    }

    public boolean stageHostIsReplayRecordingActive() {
        return replayRecording.isActive();
    }

    public void stageHostRecordDialoguePageCount(int encounterIndex, int pageCount) {
        replayRecording.recordDialoguePageCount(encounterIndex, pageCount);
    }

    public int stageHostGetRecordedDialoguePageCount(int encounterIndex) {
        return replayPlayback.getRecordedDialoguePageCount(encounterIndex);
    }

    public void battleFlowClearReplayPending() {
        replayRecording.clearPending();
    }

    public StageClearResultPanel battleFlowStageClearResultPanel() {
        return stageClearResultPanel;
    }

    public ImageBank battleFlowImgs() {
        return imgs;
    }

    public BulletSprites battleFlowBulletSprites() {
        return bulletSprites;
    }

    public SpellPracticeEndScreen battleFlowSpellPracticeEndScreen() {
        return spellPracticeEndScreen;
    }

    // Battle pause bridge helpers (used by BattleScene).
    public boolean battlePauseIsPaused() {
        return battlePaused;
    }

    public void battlePauseSyncPlayerBombActive() {
        if (player != null) {
            player.setBombActive(bombCnt > 0);
        }
    }

    public BattlePauseScreen battlePauseScreen() {
        return pauseScreen;
    }

    public void battlePauseResume() {
        battlePaused = false;
        audioSystem.resumeBgm();
    }

    public void battlePauseQuitToTitle() {
        audioSystem.stopAll();
        resultStats.onQuitToTitle(replayPlayback.isActive());
        battlePaused = false;
        inGame = false;
        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        player = null;
        if (ui != null) {
            if (startSpellPractice) {
                ui.returnToSpellPracticeKeepCursor();
            } else {
                ui.returnToTitleKeepCursor();
            }
        }
    }

    public void battlePauseRestart() {
        audioSystem.stopAll();
        restartBattle();
    }

    // Pause menu action: restart the whole run from the stage selected at run start.
    // (Story/Practice stage-select: should restart back to selected stage, not always Stage 1.)
    public void battlePauseRestartFromBeginning() {
        audioSystem.stopAll();
        resultStats.onRetry(replayPlayback.isActive());
        // Keep current run settings (difficulty/chara/type), and restart from selected start stage.
        startGameInternal(startGamemode, startLevel, currentChara, currentType, runStartStage, false);
    }

    public void battlePauseTryContinueYes() {
        if (continueSystem.consumeContinueAndRecord()) {
            scoreSystem.post(ScoreSystem.EVT_CONTINUE_USED, 0, mStage, 0);
            scoreSystem.post(ScoreSystem.EVT_CONTINUE_RESET, 0, 0, 0);
            int[] opt = GameOptions.load();
            if (opt == null) {
                opt = GameOptions.defaults();
            }
            int playerCount = opt[0];
            BattleEngine.ContinueReviveResult rr = battleEngine.continueReviveInPlace(dropItems, (hud instanceof BattleHudModel) ? (BattleHudModel) hud : null,
                    player, PLAY_X, PLAY_Y, PLAY_W, PLAY_H, playerCount);
            bombCnt = rr.newBombCnt;
            sDeadcnt = rr.newDeadcnt;
            sBariacnt = rr.newBariacnt;
            mTacnt = rr.newTacnt;
            shooting = rr.newShooting;
            battlePaused = false;
            audioSystem.resumeBgm();
        } else {
            enterGameOverFinalResultPanel();
        }
    }

    public void battlePauseContinueNo() {
        enterGameOverFinalResultPanel();
    }

    public void battlePauseEnterFromGame() {
        battlePaused = true;
        audioSystem.playSeType(SoundEffectSystem.SE_PAUSE);
        audioSystem.pauseBgm();
        if (pauseScreen != null) {
            pauseScreen.enterPause(startSpellPractice);
        }
    }

    // StageRuntime.StageFlow bridge helpers (used by BattleStageFlow).
    public boolean stageFlowShouldEnterEndingAfterStageClear() {
        return shouldEnterEndingAfterStageClear();
    }

    public void stageFlowBeginEndingTransition() {
        beginEndingTransition();
    }

    public void stageFlowEnterStageClearResultPanel() {
        enterStageClearResultPanel();
    }

    // StageRuntime.Host bridge helpers (used by BattleStageHost).
    public void stageHostClearDropItems() {
        dropItems.clear();
    }

    public void stageHostClearEnemies() {
        enemies.clear();
    }

    public void stageHostResetBossController() {
        bossController.reset();
    }

    public int stageHostGetBstock() {
        return sBstock;
    }

    public void stageHostSetBstock(int v) {
        sBstock = v;
    }

    public void stageHostSetEnemyAabb(int idx, int xFixed, int yFixed, int radiusFixed,
            int leftExtentFixed, int rightExtentFixed, int topExtentFixed, int bottomExtentFixed,
            boolean canTarget) {
        enemies.set(idx, xFixed, yFixed, radiusFixed, leftExtentFixed, rightExtentFixed, topExtentFixed, bottomExtentFixed, canTarget);
    }

    public int[][] stageHostGetEffectTable() {
        return EFFECT_TABLE;
    }

    public boolean stageHostIsHitSparkEnabled() {
        return optHitSparkEnabled;
    }

    public int stageHostStageNa() {
        return stageNa();
    }

    public int stageHostGetStep() {
        return mStep;
    }

    public void stageHostSetStep(int v) {
        mStep = v;
    }

    public int stageHostGetTaCnt() {
        return mTacnt;
    }

    public void stageHostSetTaCnt(int v) {
        mTacnt = v;
    }

    public void stageHostSetIFlash(int v) {
        iFlash = v;
    }

    public void stageHostStageHd(int enemyIdx) {
        stageHd(enemyIdx);
    }

    public void stageHostStageUb(int enemyIdx) {
        stageUb(enemyIdx);
    }

    public void stageHostStageDb(int enemyIdx) {
        stageDb(enemyIdx);
    }

    public void stageHostStageEb(int enemyIdx) {
        stageEb(enemyIdx);
    }

    public void stageHostFreeEnemy(int enemyIdx) {
        enemies.free(enemyIdx);
    }

    public void stageHostSpawnBulletEffect(int effectId, int xFixed, int yFixed) {
        bullets.spawnEffect(effectId, 1, 10, xFixed, yFixed);
    }

    public int stageHostRandomNextInt() {
        return random.nextInt();
    }

    public int stageHostGetOriginX() {
        return mOriginx;
    }

    public int stageHostGetOriginY() {
        return mOriginy;
    }

    public int stageHostGetPlayX() {
        return PLAY_X;
    }

    public int stageHostGetPlayY() {
        return PLAY_Y;
    }

    public int stageHostGetPlayW() {
        return PLAY_W;
    }

    public int stageHostGetPlayH() {
        return PLAY_H;
    }

    public void stageHostSetLatterFlag(boolean v) {
        mLatterf = v;
    }

    public void stageHostSetBossMode(boolean v) {
        mBossmodef = v;
    }

    public int[] stageHostGetBossX() {
        return sBossx;
    }

    public int[] stageHostGetBossY() {
        return sBossy;
    }

    public int stageHostGetStartLevel() {
        return startLevel;
    }

    public void stageHostMarkBossStartAndCaptureCurrentFrame() {
        replayRecording.markBossStartAndCaptureCurrentFrame(random,
                fi, bombCnt, sBariacnt, sDeadcnt, mTacnt, shooting, ca, timeStop, mResetf,
                stageMissCount, stageBombUsedCount, stageSpellSeenCount, stageSpellBonusCount, stageLastSpellId,
                stageInitialContinueCount, continueSystem, hud, scoreSystem, player);
    }

    public void stageHostSetHFlash(int v) {
        hFlash = v;
    }

    public void stageHostDrawCcIconSafe(Graphics g, int spriteId, int x, int y) {
        // Delegate cc.dat icon drawing to BattleRenderer to keep GameCore render-free.
        battleRenderer.drawCcIconSafe(battleRenderAccess, g, spriteId, x, y);
    }

    // BossController.Host bridge helpers (used by BattleBossHost).
    public boolean bossHostIsBossMode() {
        return mBossmodef;
    }

    public int bossHostGetStage() {
        return mStage;
    }

    public int bossHostGetGameMode() {
        return mGamemode;
    }

    public int bossHostGetChara() {
        return currentChara;
    }

    public int bossHostGetType() {
        return currentType;
    }

    public int bossHostGetLevel() {
        return (hud != null) ? hud.getLevel() : 0;
    }

    public int bossHostGetPower() {
        return (hud != null) ? hud.getPower() : 0;
    }

    public int bossHostGetFi() {
        return fi;
    }

    public int bossHostGetMGCnt() {
        return mGcnt;
    }

    public void bossHostSetMGCnt(int v) {
        mGcnt = v;
    }

    public int bossHostGetBossF() {
        return sBossf;
    }

    public void bossHostSetBossF(int v) {
        sBossf = v;
    }

    public int bossHostGetBbaria() {
        return sBbaria;
    }

    public void bossHostSetBbaria(int v) {
        sBbaria = v;
    }

    public void bossHostSetResetFlag(int v) {
        mResetf = v;
    }

    public int bossHostGetPlayerXFixed() {
        if (player != null) {
            return player.getXFixed();
        }
        return (PLAY_X + (PLAY_W / 2)) << 16;
    }

    public int bossHostGetPlayerYFixed() {
        if (player != null) {
            return player.getYFixed();
        }
        return (PLAY_Y + PLAY_H - 28) << 16;
    }

    public int bossHostGetSpellPracticeBossStep() {
        if (startSpellId >= 0 && startSpellId < SPELLPRACTICE_INFO.length) {
            return SPELLPRACTICE_INFO[startSpellId][3];
        }
        return 0;
    }

    public void bossHostSetSpellBonusArmed(boolean v) {
        scoreSystem.post(ScoreSystem.EVT_SET_SPELL_BONUS_ARMED, v ? 1 : 0, 0, 0);
    }

    public void bossHostStageJb(int enemyIdx) {
        stageJb(enemyIdx);
    }

    public int bossHostGetTimeStop() {
        return timeStop;
    }

    public void bossHostSetTimeStop(int v) {
        timeStop = v;
    }

    public void bossHostAimEnemyBulletsToPlayer() {
        bullets.aimEnemyBulletsTo(bossHostGetPlayerXFixed(), bossHostGetPlayerYFixed());
    }

    public void bossHostSetEnemyBulletGlobalDir(int deg) {
        bullets.setGlobalDir(deg);
    }

    public void bossHostSetEnemyBulletLock(int idx, int value) {
        bullets.setEnemyBulletLock(idx, value);
    }

    public void bossHostSpawnEnemyBullet(int bulletId, int moveType, int angleDeg, int speedParam, int xFixed, int yFixed) {
        int px = bossHostGetPlayerXFixed();
        int py = bossHostGetPlayerYFixed();
        enemyBullets.cc(bulletId, moveType, angleDeg, speedParam, xFixed, yFixed, px, py);
    }

    public void bossHostConvertEnemyBulletsTo81Rain(int bossYFixed, int gc) {
        bullets.convertEnemyBulletsTo81Rain(bossYFixed, gc);
    }

    public void bossHostSetEnemyBulletsDeltaForIdAndMoveType(int bulletId, int moveType, int newDelta) {
        bullets.setEnemyBulletsDeltaForIdAndMoveType(bulletId, moveType, newDelta);
    }

    public void bossHostSetEnemyBulletsDeltaForIdIfDeltaZero(int bulletId, int baseDelta, int deltaStep) {
        bullets.setEnemyBulletsDeltaForIdIfDeltaZero(bulletId, baseDelta, deltaStep);
    }

    // Battle main tick bridge helpers (used by BattleScene).
    public boolean battleMainIsDialogueActiveForInput() {
        return isDialogueActiveForInput(mGcnt + 1);
    }

    // Handle mResetf-driven side effects during update (renderer must remain read-only).
    public void battleMainHandleResetFlagSideEffects() {
        if (mResetf == 0) {
            return;
        }
        convertEnemyBulletsToCrisisItems();
        if (mResetf == 2) {
            dropItems.forceCollect(0);
        } else if (mResetf == 3) {
            dropItems.forceCollect(2);
        } else if (mResetf == 4) {
            dropItems.forceCollect(1);
        }
        mResetf = 0;
    }

    public void battleMainTickInput(int keys, int pressed, int fireActionPressed, boolean dialogueActive) {
        if ((pressed & SOFTKEY_R_PRESSED) != 0) {
            battleEngine.tickSlowToggle(player, true);
        }

        if ((pressed & GAME_B_PRESSED) != 0) {
            battleEngine.tickSlowToggle(player, true);
        }

        if ((pressed & GAME_C_PRESSED) != 0) {
            BattleEngine.BombActivateResult r = battleEngine.tickBombActivation(true, dialogueActive, hud instanceof BattleHudModel, bombCnt, sBossf, sBariacnt, fi,
                    mGcnt, mTacnt, ca, sDeadcnt);
            if (r.bombActivated) {
                stageBombUsedCount++;
                runBombUsedCount++;
                if (currentChara == 0) {
                    audioSystem.playSeType(SoundEffectSystem.SE_REIMU_BOMB);
                } else if (currentChara == 1) {
                    audioSystem.playSeType(SoundEffectSystem.SE_MARISA_BOMB);
                } else {
                    audioSystem.playSeType(SoundEffectSystem.SE_ALICE_BOMB);
                }
                ca = r.newCa;
                sDeadcnt = r.newDeadcnt;
                bombCnt = r.newBombCnt;
            }
        }

        BattleEngine.FireInputResult fr = battleEngine.tickFireInput((pressed & FIRE_PRESSED) != 0, (fireActionPressed & FIRE_PRESSED) != 0, dialogueActive, mTacnt,
                shooting);
        mTacnt = fr.newTacnt;
        shooting = fr.newShooting;
    }

    public int battleMainGetDeadcnt() {
        return sDeadcnt;
    }

    public boolean battleMainAdvanceAndHandleDeath() {
        if (sDeadcnt == 0) {
            if (mStage == 1 || mStage == 2) {
                bgMoveSpd++;
            } else {
                bgMoveSpd += 2;
            }
        }
        fi++;
        mGcnt++;
        scoreSystem.post(ScoreSystem.EVT_TICK, 0, 0, 0);
        if (sBariacnt > 0) {
            sBariacnt--;
        }

        if (player != null) {
            battleEngine.tickPlayerDeathAndInvincibility(player, sBariacnt, PLAY_X, PLAY_Y, PLAY_W, PLAY_H);
        }
        if (sDeadcnt > 0) {
            boolean bossActive = sBossf != 0;
            BattleEngine.DeathTickResult r = battleEngine.tickDeathCountdown(sDeadcnt, mStage, dropItems, (hud instanceof BattleHudModel) ? (BattleHudModel) hud : null,
                    player, PLAY_X, PLAY_Y, PLAY_W, PLAY_H, startSpellPractice, bossActive);
            sDeadcnt = r.newDeadcnt;
            if (r.barrierTicks > 0) {
                sBariacnt = r.barrierTicks;
            }
            if (r.deathProcessed) {
                stageMissCount++;
                runMissCount++;
            }
            if (r.deathProcessed && r.noPlayersLeft) {
                if (startSpellPractice) {
                    startSpellPracticeEnd(false, 10);
                    return true;
                }
                // During replay playback, auto-finish on game over to avoid showing the continue menu.
                if (replayPlayback.isActive()) {
                    enterGameOverFinalResultPanel();
                    return true;
                }
                if (continueSystem.canOfferContinue(mGamemode, startSpellPractice) && pauseScreen != null) {
                    battlePaused = true;
                    audioSystem.pauseBgm();
                    pauseScreen.enterContinue(continueSystem.getRemainingContinues());
                    return true;
                }
                enterGameOverFinalResultPanel();
                return true;
            }
        }

        return false;
    }

    public void battleMainTickBomb() {
        bombCnt = battleEngine.tickBomb(bombCnt);
    }

    public boolean battleMainTickTimeStop() {
        if (timeStop > 0) {
            stageGb();
            bossSpellCutInTick();
            timeStop--;
            bossController.tickHpBarOncePerFrame();
            return true;
        }
        return false;
    }

    public boolean battleMainTickWorldAndStage(int keys) {
        long w0 = System.currentTimeMillis();
        if (player != null) {
            int p = (hud != null) ? hud.getPower() : 0;
            battleEngine.tickPlayer(player, keys, PLAY_X, PLAY_Y, PLAY_W, PLAY_H, p, sBbaria != 0, shooting, sBossf, bombCnt, currentChara, enemies, fi, sDeadcnt);
        }

        long w1 = System.currentTimeMillis();
        stageGb();

        // Proactive GC: after boss state change (wb/vb), clear caches and GC now
        // so the JVM doesn't auto-GC during the spell cut-in animation.
        if (bossController.consumeGcArmed()) {
            imgs.clearAlphaCaches();
            System.gc();
        }

        long w2 = System.currentTimeMillis();

        bossSpellCutInTick();

        battleEngine.tickStageEffects(effectlist, EFFECT_TABLE);

        battleEngine.tickDropItems(dropItems, hud, player, PLAY_X, PLAY_Y, PLAY_W, PLAY_H, mStage, disableItemCollectLimitCheat, currentChara, sDeadcnt);

        long w3 = System.currentTimeMillis();
        if (mGamemode != 3) {
            stageMa();
        }
        long w4 = System.currentTimeMillis();

        // gb = stageGb (boss/enemy update+collision), ma = stageMa (script advance),
        // rst = bossController.tick time, o = player+effects+items+cutIn.
        Debug.recordWorldBreakdown((int)(w2-w1), (int)(w4-w3), lastBossTickMs, (int)(w1-w0) + (int)(w3-w2));

        return stageClearPanelActive;
    }

    public void battleMainTickBulletsAndCollisionAndHpBar() {
        int enemyLevel = 0;
        if (hud instanceof BattleHudData) {
            enemyLevel = ((BattleHudData) hud).getLevel();
        }

        if (optDebugPerfEnabled) {
            Debug.perfBeginFrame();
            if (bullets != null) {
                bullets.perfBeginFrame();
            }
        }

        boolean hasAimTarget = player != null;
        int aimXFixed = 0;
        int aimYFixed = 0;
        if (hasAimTarget) {
            aimXFixed = player.getXFixed();
            aimYFixed = player.getYFixed();
        }

        if (optDebugPerfEnabled) {
            long t0 = System.currentTimeMillis();
            battleEngine.tickBullets(PLAY_X, PLAY_Y, PLAY_W, PLAY_H, enemyLevel, hasAimTarget, aimXFixed, aimYFixed);
            long t1 = System.currentTimeMillis();
            Debug.perfSetBulletUpdateMs((int) (t1 - t0));
        } else {
            battleEngine.tickBullets(PLAY_X, PLAY_Y, PLAY_W, PLAY_H, enemyLevel, hasAimTarget, aimXFixed, aimYFixed);
        }

        if (player != null && sBariacnt == 0 && sDeadcnt == 0 && bombCnt == 0) {
            boolean allowHit = true;
            if (optDebugPerfEnabled) {
                long c0 = System.currentTimeMillis();
                sDeadcnt = battleEngine.tickPlayerCollision(player, allowHit, enemylist, sBariacnt, sDeadcnt, bombCnt);
                long c1 = System.currentTimeMillis();
                Debug.perfSetPlayerCollisionMs((int) (c1 - c0));
            } else {
                sDeadcnt = battleEngine.tickPlayerCollision(player, allowHit, enemylist, sBariacnt, sDeadcnt, bombCnt);
            }
        }

        bossController.tickHpBarOncePerFrame();
    }

    // Check if dialogue input should block gameplay input.
    private boolean isDialogueActiveForInput(int gcntPlusOne) {
        ensureStageDatLoaded();
        return stageRuntime.isDialogueActiveForInput(stageNa(), mStep, gcntPlusOne);
    }

    // Key down handler for input routing.
    protected void keyPressed(int keyCode) {
        boolean callSuper = input.onKeyPressed(
                keyCode,
                inGame,
                battlePaused,
                stageClearPanelActive,
                startSpellPractice,
                spellPracticeEndFlg,
                spellPracticeEndActive);
        if (callSuper) {
            super.keyPressed(keyCode);
        }
    }

    // Key up handler for input routing.
    protected void keyReleased(int keyCode) {
        boolean callSuper = input.onKeyReleased(keyCode, inGame, battlePaused);
        if (callSuper) {
            super.keyReleased(keyCode);
        }
    }

    // Top-level render dispatch.
    private void render() {
        Graphics g = getGraphics();
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());

        int ox = (getWidth() - LOGICAL_W) / 2;
        int oy = (getHeight() - LOGICAL_H) / 2;
        g.translate(ox, oy);

        g.setClip(0, 0, LOGICAL_W, LOGICAL_H);

        sceneController.render(this, g);

        renderIntroShatter(g);

        g.translate(-ox, -oy);

        flushGraphics();
    }

    boolean sceneIsEndingActive() {
        return endingActive;
    }

    void sceneSetEndingActive(boolean v) {
        endingActive = v;
    }

    boolean sceneIsStaffRollActive() {
        return staffRollActive;
    }

    void sceneSetStaffRollActive(boolean v) {
        staffRollActive = v;
    }

    ImageBank sceneImgs() {
        return imgs;
    }

    BulletSprites sceneBulletSprites() {
        return bulletSprites;
    }

    UiController sceneUi() {
        return ui;
    }

    String sceneResourceError() {
        return resourceError;
    }

    void sceneEnterFinalResultPanelAfterStaffRoll() {
        enterFinalResultPanelAfterStaffRoll();
    }

    // Scene hook: query current in-game state.
    public boolean sceneIsInGame() {
        return inGame;
    }

    // Battle hook: per-frame origin reset.
    public void battleFrameResetOrigin() {
        mOriginx = 0;
        mOriginy = 0;
    }

    // Render full battle UI and playfield.
    private void renderBattleUi(Graphics g) {
        battleRenderer.renderBattleUi(battleRenderAccess, g);
    }

    // Capture UI snapshot for intro shatter effect.
    private void beginIntroShatterFromUi() {
        if (ui == null) {
            return;
        }
        try {
            javax.microedition.lcdui.Image img = javax.microedition.lcdui.Image.createImage(256, 256);
            Graphics g = img.getGraphics();
            g.setColor(0x000000);
            g.fillRect(0, 0, 256, 256);
            g.setClip(0, 0, LOGICAL_W, LOGICAL_H);
            ui.render(g);
            introShatterImg = img;
            introShatterCnt = 100;
            introShatterParity = Math.abs(random.nextInt()) & 0x1;
        } catch (Throwable t) {
            introShatterImg = null;
            introShatterCnt = 0;
            introShatterParity = 0;
        }
    }

    public void beginIntroShatterWhite() {
        try {
            javax.microedition.lcdui.Image img = javax.microedition.lcdui.Image.createImage(256, 256);
            Graphics g = img.getGraphics();
            g.setColor(0xFFFFFF);
            g.fillRect(0, 0, 256, 256);
            introShatterImg = img;
            introShatterCnt = 100;
            introShatterParity = Math.abs(random.nextInt()) & 0x1;
        } catch (Throwable t) {
            introShatterImg = null;
            introShatterCnt = 0;
            introShatterParity = 0;
        }
    }

    // Render intro shatter overlay animation.
    private void renderIntroShatter(Graphics g) {
        if (introShatterCnt <= 0) {
            return;
        }
        if (introShatterImg == null) {
            return;
        }

        int oldClipX = g.getClipX();
        int oldClipY = g.getClipY();
        int oldClipW = g.getClipWidth();
        int oldClipH = g.getClipHeight();

        try {
            int n = introShatterParity;
            int n2 = 100 - introShatterCnt;
            for (int i = 0; i <= 120; i += 15) {
                for (int j = 0; j <= 120; j += 15) {
                    int n3 = (n2 << 3) - (240 - (i + j + 30));

                    int n4 = i;
                    int n5 = j;
                    if (n3 > 0) {
                        if (n == 0) {
                            n4 += n3 << 1;
                            n5 += n3;
                        } else {
                            n4 += n3;
                            n5 += n3 << 1;
                        }
                    }
                    g.setClip(n4, n5, 15, 15);
                    g.drawImage(introShatterImg, n4 - i, n5 - j, Graphics.TOP | Graphics.LEFT);

                    int n6 = 240 - i;
                    int n7 = j;
                    if (n3 > 0) {
                        if (n == 0) {
                            n6 -= n3;
                            n7 += n3 << 1;
                        } else {
                            n6 -= n3 << 1;
                            n7 += n3;
                        }
                    }
                    g.setClip(n6, n7, 15, 15);
                    g.drawImage(introShatterImg, n6 - (240 - i), n7 - j, Graphics.TOP | Graphics.LEFT);

                    int n8 = i;
                    int n9 = 240 - j;
                    if (n3 > 0) {
                        if (n == 0) {
                            n8 += n3;
                            n9 -= n3 << 1;
                        } else {
                            n8 += n3 << 1;
                            n9 -= n3;
                        }
                    }
                    g.setClip(n8, n9, 15, 15);
                    g.drawImage(introShatterImg, n8 - i, n9 - (240 - j), Graphics.TOP | Graphics.LEFT);

                    int n10 = 240 - i;
                    int n11 = 240 - j;
                    if (n3 > 0) {
                        if (n == 0) {
                            n10 -= n3 << 1;
                            n11 -= n3;
                        } else {
                            n10 -= n3;
                            n11 -= n3 << 1;
                        }
                    }
                    g.setClip(n10, n11, 15, 15);
                    g.drawImage(introShatterImg, n10 - (240 - i), n11 - (240 - j), Graphics.TOP | Graphics.LEFT);
                }
            }
        } catch (Throwable t) {
        } finally {
            try {
                g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
            } catch (Throwable ignore) {
            }
        }
    }

    // Per-frame bomb effect logic.
    private void bombTick(int d0) {
        if (player == null) {
            return;
        }
        int fx = player.getXFixed();
        int fy = player.getYFixed();

        if (currentChara == 0) {
            // Reimu bomb: explosion effect is spawned when bomb bullets end (see BulletSystem).
            if (d0 == 2) {
                int sx = fx + speedCos(240, 0);
                int sy = fy + speedSin(240, 0);
                bombSpawnXa(3, 0, 45, 15, sx, sy);
            } else if (d0 == 8) {
                int sx = fx + speedCos(240, 72);
                int sy = fy + speedSin(240, 72);
                bombSpawnXa(4, 72, 45, 15, sx, sy);
            } else if (d0 == 14) {
                int sx = fx + speedCos(240, 144);
                int sy = fy + speedSin(240, 144);
                bombSpawnXa(5, 144, 45, 15, sx, sy);
            } else if (d0 == 20) {
                int sx = fx + speedCos(240, 216);
                int sy = fy + speedSin(240, 216);
                bombSpawnXa(6, 216, 45, 15, sx, sy);
            } else if (d0 == 26) {
                int sx = fx + speedCos(240, 288);
                int sy = fy + speedSin(240, 288);
                bombSpawnXa(7, 288, 40, 15, sx, sy);
            }
        }

        if (currentChara == 1) {
            if (d0 < 45) {
                int y = fy - Fixed.fromInt(10);
                bullets.spawnOrUpdatePlayerLaser(105, 3, 2, fx, y, 12);
            }

            int d1;
            if (d0 < 30) {
                d1 = d0;
            } else {
                d1 = 60 - d0;
            }
            d1 >>= 1;
            int d2 = Math.abs(random.nextInt()) % 360;
            int se = speedCos(d1 << 3, d2);
            int te = speedSin(d1 << 3, d2);
            mOriginx = se >> 16;
            mOriginy = te >> 16;
        }

        if (currentChara == 2) {
            if (d0 == 10) {
                for (int i = 0; i < 6; i++) {
                    bombSpawnXa(16, 0 + 60 * i, 20, 4, fx, fy);
                }
            }
            if (d0 == 25) {
                for (int i = 0; i < 6; i++) {
                    bombSpawnXa(17, 30 + 60 * i, 20, 5, fx, fy);
                }
            }
        }

        if (bombCnt > 15) {
            scoreSystem.post(ScoreSystem.EVT_BOMB_TICK, bombCnt, 0, 0);
        }
        if (d0 < 60) {
            mResetf = 2;
        }
    }

    // Load stage script data when needed.
    private void ensureStageDatLoaded() {
        stageRuntime.ensureStageDatLoaded();
    }

    // Load dialogue text table for current stage.
    private void ensureTalkTableLoaded() {
        stageRuntime.ensureTalkTableLoaded(mStage, currentChara);
    }
    // Map stage/boss state to script table index.
    private int stageNa() {
        int n = mStage * 5;
        if (mLatterf) {
            ++n;
            if (mBossmodef) {
                n += currentChara + 1;
            }
        }
        return n;
    }

    // Clear enemy/effect state for a new stage segment.
    private void stageIb() {
        stageRuntime.stageIb();
    }

    // Spawn enemy entry and populate hitbox/motion fields.
    private int stageFb(final int n, final int n2, final int n3, final int n4, final int n5, final int n6, final int n7, final int n8, final int n9) {
        return stageRuntime.stageFb(n, n2, n3, n4, n5, n6, n7, n8, n9);
    }

    // Per-frame enemy/boss update and collision processing.
    private void stageGb() {
        if (optDebugResourceEnabled && mBossmodef) {
            debugLine = "bossf=" + sBossf + " gc=" + mGcnt + " stage=" + mStage + " baria=" + sBbaria;

            int t = bossController.getHpBarTargetIndex();
            int mt = -1;
            int hp = -1;
            int maxHp = -1;
            int e16 = -1;
            int e21 = -1;
            if (t >= 0 && t < 32 && enemylist[t][0] != 0) {
                mt = enemylist[t][11];
                hp = enemylist[t][4];
                maxHp = enemylist[t][9];
                e16 = enemylist[t][16];
                e21 = enemylist[t][21];
            }
            debugLine2 = "t=" + t + " mt=" + mt + " ehp=" + hp + "/" + maxHp + " j=" + bossController.getHpBarAnimJ() + " e16=" + e16 + " e21=" + e21 + " baria=" + sBbaria + " k=" + bossController.getHpBarAnimK() + " m=" + bossController.getHpBarAnimMode();
            debugLine3 = buildSummonDebugLine(9);
            debugLine4 = buildSummonDebugLine(10);
            debugLine5 = buildSummonDebugLine(11);
        }
        stageRuntime.stageGb();
    }

    private int lastBossTickMs;

    // Boss update and movement smoothing.
    private void stageUb(final int n) {
        int mt = enemylist[n][11];
        if (mt == 100 || mt == 101 || mt == 102 || mt == 103) {
            long bt0 = System.currentTimeMillis();
            bossController.tick(n);
            lastBossTickMs = (int) (System.currentTimeMillis() - bt0);
            int sid = bossController.getSpellId();
            scoreSystem.post(ScoreSystem.EVT_SPELL_ID_CHANGED, sid, 0, 0);
            onStageSpellIdChanged(sid);
            if (mt != 100) {
                enemylist[n][16] = (sBbaria == 0) ? 1 : 0;
            }
            return;
        }

        bossTargetsUpdateMinimal();

        int idx = 0;
        if (mt == 102) {
            idx = 1;
        } else if (mt == 103) {
            idx = 2;
        }

        if (mBossmodef && mStage == 0 && sBossf == 1 && mt == 102) {
            if (mGcnt == 30) {
                enemylist[n][14] = 131072;
                enemylist[n][15] = 131072;
            } else if (mGcnt == 45) {
                enemylist[n][14] = 458752;
                enemylist[n][15] = 458752;
            }
        }

        if (mBossmodef && mStage == 2 && sBossf == 2 && mGcnt == 0) {
            enemylist[n][14] = 393216;
            enemylist[n][15] = 393216;
        }

        enemylist[n][16] = (sBbaria == 0) ? 1 : 0;

        if (sBossx[idx] == 0 && sBossy[idx] == 0) {
            sBossx[idx] = enemylist[n][5] >> 16;
            sBossy[idx] = enemylist[n][6] >> 16;
        }

        if (enemylist[n][5] < (sBossx[idx] + 1 << 16)) {
            enemylist[n][5] += enemylist[n][14];
        }
        if (enemylist[n][5] > (sBossx[idx] - 1 << 16)) {
            enemylist[n][5] -= enemylist[n][14];
        }
        if (enemylist[n][6] < (sBossy[idx] + 1 << 16)) {
            enemylist[n][6] += enemylist[n][15];
        }
        if (enemylist[n][6] > (sBossy[idx] - 1 << 16)) {
            enemylist[n][6] -= enemylist[n][15];
        }
    }

    // Update boss target positions and barrier timing.
    private void bossTargetsUpdateMinimal() {
        if (!mBossmodef) {
            return;
        }
        if (sBossf == 0) {
            return;
        }

        if (sBossf == 2 && mGcnt > 10 && mGamemode != 3) {
            sBbaria = 0;
            bossWb(3);
        }

        if (sBossf == 2) {
            if (mGcnt == 0) {
                sBbaria = 1;
            } else if (mGcnt > 10) {
                sBbaria = 0;
            }
        }

        if (mStage == 0) {
            if (sBossf == 1) {
                sBossx[0] = 97;
                sBossy[0] = 56;
                if (mGcnt >= 45) {
                    sBossx[1] = -30;
                } else if (mGcnt >= 30) {
                    sBossx[1] = 37;
                } else {
                    sBossx[1] = 77;
                }
                sBossy[1] = 44;
                sBossx[2] = 117;
                sBossy[2] = 44;
            } else if (sBossf == 2) {
                sBossx[0] = 97;
                sBossy[0] = 44;
                sBossx[2] = 30;
                sBossy[2] = 80;
            } else if (sBossf == 3) {
                if (mGcnt == 10 || mGcnt == 85 || mGcnt == 185 || mGcnt == 285 || mGcnt == 385) {
                    sBossx[0] = 144;
                    sBossy[0] = 80;
                } else if (mGcnt == 35 || mGcnt == 135 || mGcnt == 235 || mGcnt == 335) {
                    sBossx[0] = 50;
                    sBossy[0] = 80;
                }

                if (mGcnt == 0 || mGcnt == 90 || mGcnt == 190 || mGcnt == 290 || mGcnt == 390) {
                    sBossx[2] = 97;
                    sBossy[2] = 48;
                } else if (mGcnt == 40 || mGcnt == 240) {
                    sBossx[2] = 147;
                    sBossy[2] = 28;
                } else if (mGcnt == 140 || mGcnt == 340) {
                    sBossx[2] = 47;
                    sBossy[2] = 28;
                }
            }
            return;
        }

        if (mStage == 1) {
            sBossx[0] = 97;
            sBossy[0] = 56;
            return;
        }

        if (mStage >= 2) {
            if (sBossf == 1) {
                sBossx[0] = 97;
                sBossy[0] = 56;
            } else {
                sBossx[0] = 97;
                sBossy[0] = 44;
            }
        }
    }

    // Handle player bullet collision against an enemy.
    private void stageHd(final int n) {
        battleEngine.tickEnemyPlayerBulletCollision(n, enemylist, bombCnt, sBbaria, mBossWrk, disableBossBombShieldCheat, cc, bossController, player);
    }

    // StageJb helper: spawn a drop item if HUD supports drop context.
    private void stageJbSpawnDropIfAny(int dropType, int ex, int ey) {
        if (dropType != 0 && hud instanceof BattleHudModel) {
            BattleHudModel m = (BattleHudModel) hud;
            dropItems.spawn(dropType, 0, 0, ex, ey, mStage, m.getPower(), sBossf != 0);
        }
    }

    // StageJb helper: post spell finish event and update bonus counters.
    private void stageJbPostSpellFinish(boolean countRunBonus) {
        int lvl = (hud != null) ? hud.getLevel() : 0;
        boolean bonus = scoreSystem.isSpellBonusArmed();
        scoreSystem.post(ScoreSystem.EVT_SPELL_FINISH, bossController.getBspellcnt(), lvl, mStage);
        if (bonus) {
            stageSpellBonusCount++;
            if (countRunBonus) {
                runSpellBonusCount++;
            }
        }
        bossController.resetSpellCountersAfterJb();
    }

    private void stageJbHandleMt100(final int n, final int ex, final int ey, final int dropType) {
        timeStop = 0;
        if (stageRuntime.stageJbMt100PreProcess(n, ex, ey)) {
            return;
        }
        stageJbPostSpellFinish(true);

        if (bossController.getHpBarAnimMode() == 1) {
            stageRuntime.stageJbMt100EnterHpBarAnimMode1Transition(n);
        } else {
            scoreSystem.post(ScoreSystem.EVT_ADD_SCORE, 30000, 0, 0);
            stageJbSpawnDropIfAny(dropType, ex, ey);
            if (startSpellPractice) {
                // Spell practice: boss defeat triggers snow-scatter + end flow (DoJa).
                stageRuntime.stageJbSpellPracticeBossDefeatedEnter(n, ex, ey);
                startSpellPracticeEnd(true, 30);
            } else {
                stageRuntime.stageJbMt100EnterNonPracticeDefeatedState(n);
            }
        }
    }

    public void stageHostSetTargetFps(int fps) {
        if (fps <= 0) {
            fps = TARGET_FPS;
        }
        targetFps = fps;
    }

    private void stageJbHandleMt101to103(final int n, final int ex, final int ey) {
        timeStop = 0;
        stageRuntime.stageJbBossPhaseCleanup();

        stageRuntime.stageJbBossClearStateOnDeath(n);
        // Boss phase clear (mt 101..103) is the common spell finish path in DoJa.
        // Count run-wide spell bonus here so result panels show correct totals.
        stageJbPostSpellFinish(true);

        if (sBstock != 0) {
            if (hud instanceof BattleHudModel) {
                BattleHudModel m = (BattleHudModel) hud;
                int power = m.getPower();
                boolean bossActive = sBossf != 0;
                int gc = mGcnt;
                if (gc < 0) {
                    gc = -gc;
                }
                dropItems.spawn(DropItemSystem.TYPE_P, 0, 0, ex + ((((gc & 0x1) * 23)) << 16), ey + (((gc % 7) * 7) << 16), mStage, power, bossActive);
                dropItems.spawn(DropItemSystem.TYPE_P, 0, 0, ex + (((gc % 3) * 12) << 16), ey - (((gc % 5) * 13) << 16), mStage, power, bossActive);
                dropItems.spawn(DropItemSystem.TYPE_P, 0, 0, ex - (((gc % 5) * 15) << 16), ey + (((gc % 3) * 14) << 16), mStage, power, bossActive);
                dropItems.spawn(DropItemSystem.TYPE_P, 0, 0, ex - (((gc % 7) << 3) << 16), ey - ((((gc & 0x1) * 22)) << 16), mStage, power, bossActive);
            }
            boolean animMode1 = bossController.getHpBarAnimMode() == 1;
            stageRuntime.stageJbApplyBossStockTransition(n, ex, ey, !animMode1);

            if (animMode1) {
                bossController.enterHpBarMode2ForStockTransition();
            }
            return;
        }

        stageRuntime.stageJbBossDefeatedEnter(ex, ey);

        scoreSystem.post(ScoreSystem.EVT_ADD_SCORE, 50000 + 30000 * mStage, 0, 0);

        stageRuntime.stageJbFreeEnemyIfAlive(n);
        if (startSpellPractice) {
            startSpellPracticeEnd(true, 30);
        }
    }
    private void stageJbHandleNormalEnemyDeath(final int n, final int mt, final int ex, final int ey, final int dropType) {
        stageJbSpawnDropIfAny(dropType, ex, ey);

        int enemyType = stageRuntime.stageJbNormalEnemyDeathFreeAndGetEnemyType(n);
        scoreSystem.post(ScoreSystem.EVT_ADD_SCORE, 500, 0, 0);
        audioSystem.playSeType(SoundEffectSystem.SE_CRASH2);
        stageRuntime.stageJbNormalEnemyDeathSpawnEffects(mt, ex, ey);

        int level = startLevel;
        int px;
        int py;
        if (player != null) {
            px = player.getXFixed();
            py = player.getYFixed();
        } else {
            px = (PLAY_X + (PLAY_W / 2)) << 16;
            py = (PLAY_Y + PLAY_H - 28) << 16;
        }

        // Death bursts for specific enemy types.
        if (enemyType == 80) {
            int ang = arcTan2Deg(py - ey, px - ex);
            if (level == 0) {
                enemyBullets.cc(81, 0, ang, 32, ex, ey, px, py);
            } else if (level == 1) {
                enemyBullets.cc(81, 0, ang, 40, ex, ey, px, py);
                enemyBullets.cc(81, 0, ang, 36, ex, ey, px, py);
            } else if (level == 2) {
                enemyBullets.cc(81, 0, ang, 48, ex, ey, px, py);
                enemyBullets.cc(81, 0, ang, 44, ex, ey, px, py);
            } else {
                enemyBullets.cc(81, 0, ang, 28, ex, ey, px, py);
            }
        }
        if (enemyType == 81) {
            int ang = arcTan2Deg(py - ey, px - ex);
            int step;
            int count;
            if (level == 0) {
                step = 30;
                count = 1;
            } else if (level == 1) {
                step = 25;
                count = 2;
            } else if (level == 2) {
                step = 15;
                count = 3;
            } else {
                step = 10;
                count = 4;
            }
            for (int off = -(step * count); off <= step * count; off += step) {
                enemyBullets.cc(81, 0, ang + off, 36, ex, ey, px, py);
            }
        }
        if (enemyType == 82) {
            int ang = arcTan2Deg(py - ey, px - ex);
            int div;
            if (level == 0) {
                div = 8;
            } else if (level == 1) {
                div = 9;
            } else if (level == 2) {
                div = 10;
            } else if (level == 3) {
                div = 15;
            } else {
                div = 12;
            }
            for (int k = 0; k < 360; k += 360 / div) {
                enemyBullets.cc(81, 0, ang + k, 36, ex, ey, px, py);
            }
        }
    }

    // Handle enemy death and boss phase transitions.
    private void stageJb(final int n) {
        if (enemylist[n][0] == 0) {
            return;
        }
        int mt = enemylist[n][11];
        int ex = enemylist[n][5];
        int ey = enemylist[n][6];
        int dropType = enemylist[n][21];

        if (mt == 100) {
            stageJbHandleMt100(n, ex, ey, dropType);
            return;
        }

        if (mt == 101 || mt == 102 || mt == 103) {
            stageJbHandleMt101to103(n, ex, ey);
            return;
        }

        stageJbHandleNormalEnemyDeath(n, mt, ex, ey, dropType);
    }

    private void syncScoreSystemForHud() {
        if (hud == null || !(hud instanceof BattleHudModel)) {
            return;
        }
        BattleHudModel m = (BattleHudModel) hud;

        // Preserve initial HUD hi-score (loaded from persistent score list).
        // attachHud() calls syncHud() and would otherwise overwrite it with ScoreSystem defaults.
        int initialHiScore = m.getHiScore();

        scoreSystem.attachHud(m);
        int[] opt = GameOptions.load();
        if (opt == null) {
            opt = GameOptions.defaults();
        }

        optDynamicCutInEnabled = GameOptions.isDynamicCutInEnabled(opt);

        optBombOverlayAlphaEnabled = GameOptions.isBombOverlayAlphaEnabled(opt);
        optBossSpellCutInAlphaEnabled = GameOptions.isBossSpellCutInAlphaEnabled(opt);
        optPlayerShotAlphaEnabled = GameOptions.isPlayerShotAlphaEnabled(opt);

        optBlindMaskFadeEnabled = GameOptions.isBlindMaskFadeEnabled(opt);

        optHeavyBgEnabled = GameOptions.isHeavyBgEnabled(opt);

        optHitSparkEnabled = GameOptions.isHitSparkEnabled(opt);

        optDebugFpsEnabled = GameOptions.isDebugFpsEnabled(opt);
        optDebugPerfEnabled = GameOptions.isDebugPerfEnabled(opt);
        optDebugResourceEnabled = GameOptions.isDebugResourceEnabled(opt);

        if (bulletSprites != null) {
            bulletSprites.setPlayerShotAlphaEnabled(optPlayerShotAlphaEnabled);
        }

        // Cheat is ignored in spell practice (no bomb usage in this mode).
        boolean optDisableShield = GameOptions.isCheatDisableBossBombShield(opt);
        boolean optDisableItemCollectLimit = GameOptions.isCheatDisableItemCollectLimit(opt);
        disableBossBombShieldCheat = optDisableShield && !startSpellPractice;
        disableItemCollectLimitCheat = optDisableItemCollectLimit;

        boolean anyCheatActive = optDisableShield || optDisableItemCollectLimit;
        scoreSystem.setScoreEnabled(!anyCheatActive);
        replayRecording.setEnabled(!anyCheatActive);

        int optLife = (opt.length > 0) ? opt[0] : 3;
        int gm = startSpellPractice ? 3 : startGamemode;
        scoreSystem.resetForNewRun(initialHiScore, gm, optLife, currentChara, currentType, startLevel);
    }

    // Set boss phase state and reset counters.
    private void bossWb(int bossf) {
        mGcnt = -1;
        sBossf = bossf;
    }

    // Update non-boss enemy movement patterns.
    private void stageDb(final int n) {
        enemylist[n][5] += enemylist[n][14];
        enemylist[n][6] += enemylist[n][15];
        switch (enemylist[n][11]) {
            case 1:
                if (enemylist[n][2] % 3 == 0) {
                    enemylist[n][15] += 65536;
                    return;
                }
                break;
            case 2:
                if (enemylist[n][2] % 5 == 0) {
                    enemylist[n][14] += 65536;
                    return;
                }
                break;
            case 3:
                if (enemylist[n][2] % 5 == 0) {
                    enemylist[n][14] -= 65536;
                    return;
                }
                break;
            case 4:
                if (enemylist[n][2] > 15 && enemylist[n][2] < 50) {
                    enemylist[n][7] = 1;
                    enemylist[n][5] -= enemylist[n][14];
                    enemylist[n][6] -= enemylist[n][15];
                    return;
                }
                enemylist[n][7] = 0;
                return;
            case 8:
                if (enemylist[n][2] > 50) {
                    enemylist[n][7] = 0;
                    if ((enemylist[n][2] & 0x1) == 0) {
                        enemylist[n][12] -= 1;
                    }
                    enemylist[n][13] -= 2;
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 13) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                return;
            case 9:
                if (enemylist[n][2] > 50) {
                    enemylist[n][7] = 0;
                    if ((enemylist[n][2] & 0x1) == 0) {
                        enemylist[n][12] -= 1;
                    }
                    enemylist[n][13] += 2;
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 13) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                return;
            case 5:
                if (enemylist[n][2] > 14 && enemylist[n][2] % 5 == 0) {
                    enemylist[n][14] += 65536;
                    return;
                }
                break;
            case 6:
                if (enemylist[n][2] > 14 && enemylist[n][2] % 5 == 0) {
                    enemylist[n][14] -= 65536;
                    return;
                }
                break;
            case 7:
                if (enemylist[n][2] > 50) {
                    enemylist[n][7] = 0;
                    if ((enemylist[n][2] & 0x1) == 0) {
                        enemylist[n][12] -= 1;
                    }
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 13) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                return;
            case 10:
                if (enemylist[n][2] > 100) {
                    enemylist[n][7] = 0;
                    if ((enemylist[n][2] & 0x1) == 0) {
                        enemylist[n][12] -= 1;
                    }
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 13) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                return;
            case 11:
                if (enemylist[n][2] >= 20 && enemylist[n][2] < 30) {
                    enemylist[n][7] = 1;
                    enemylist[n][13] = (enemylist[n][13] - 18 + 360) % 360;
                    enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                    enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                    return;
                }
                break;
            case 12:
                if (enemylist[n][2] >= 20 && enemylist[n][2] < 30) {
                    enemylist[n][7] = 1;
                    enemylist[n][13] = (enemylist[n][13] + 18 + 360) % 360;
                    enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                    enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                    return;
                }
                break;
            case 13:
                if (enemylist[n][2] >= 30) {
                    enemylist[n][7] = 0;
                    if (enemylist[n][2] == 30) {
                        enemylist[n][13] += 90;
                        enemylist[n][12] = 8;
                    }
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 10) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                return;
            case 14:
                if (enemylist[n][2] >= 30) {
                    enemylist[n][7] = 0;
                    if (enemylist[n][2] == 30) {
                        enemylist[n][13] += 270;
                        enemylist[n][12] = 8;
                    }
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 10) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                return;
            case 15: {
                enemylist[n][13] = (enemylist[n][13] + 2) % 360;
                int a = (enemylist[n][13] + 90) % 360;
                if (enemylist[n][5] < 6356992) {
                    if (a > 90) {
                        enemylist[n][13] = 360;
                    }
                    enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                    enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                } else {
                    if (a < 90) {
                        enemylist[n][13] = 360;
                    }
                    if (a > 135) {
                        enemylist[n][13] = 405;
                    }
                    enemylist[n][7] = 1;
                    enemylist[n][14] = speedCos(enemylist[n][12] << 2, enemylist[n][13]);
                    enemylist[n][15] = speedSin(enemylist[n][12] << 2, enemylist[n][13]);
                }
                return;
            }
            case 16: {
                enemylist[n][13] = (enemylist[n][13] + 360 - 2) % 360;
                int a = (enemylist[n][13] + 90) % 360;
                if (enemylist[n][5] > 6356992) {
                    if (a < 270) {
                        enemylist[n][13] = 540;
                    }
                    enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                    enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                } else {
                    if (a > 270) {
                        enemylist[n][13] = 540;
                    }
                    if (a < 225) {
                        enemylist[n][13] = 495;
                    }
                    enemylist[n][7] = 1;
                    enemylist[n][14] = speedCos(enemylist[n][12] << 2, enemylist[n][13]);
                    enemylist[n][15] = speedSin(enemylist[n][12] << 2, enemylist[n][13]);
                }
                return;
            }
            case 17:
                if (enemylist[n][2] == 50) {
                    if (player != null) {
                        enemylist[n][13] = arcTan2Deg(player.getYFixed() - enemylist[n][6], player.getXFixed() - enemylist[n][5]);
                    }
                } else if (enemylist[n][2] > 50) {
                    enemylist[n][7] = 0;
                    if ((enemylist[n][2] & 0x1) == 0) {
                        enemylist[n][12] += 1;
                    }
                    if (enemylist[n][12] > 4) {
                        enemylist[n][12] = 4;
                    }
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 13) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                return;
            case 18:
                if (enemylist[n][5] < -1966080 || enemylist[n][6] < -1441792 || enemylist[n][5] > 14680064 || enemylist[n][6] > 17301504) {
                    enemylist[n][2] -= 1;
                    return;
                }
                break;
            case 19:
                if (enemylist[n][7] == 1) {
                    break;
                }
                if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                    enemylist[n][13] += 90;
                    enemylist[n][12] = 8;
                    enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                    enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                    return;
                }
                if (enemylist[n][2] > 5) {
                    enemylist[n][12] -= 1;
                    return;
                }
                break;
            case 20:
                if (enemylist[n][7] == 1) {
                    break;
                }
                if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                    enemylist[n][13] += 270;
                    enemylist[n][12] = 8;
                    enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                    enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                    return;
                }
                if (enemylist[n][2] > 5) {
                    enemylist[n][12] -= 1;
                    return;
                }
                break;
            case 21:
                if (enemylist[n][2] >= 30) {
                    enemylist[n][7] = 0;
                    if (enemylist[n][2] == 30) {
                        if (enemylist[n][5] < 6356992) {
                            enemylist[n][13] = 315;
                        } else {
                            enemylist[n][13] = 585;
                        }
                        enemylist[n][12] = 4;
                    }
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 5) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                return;
            case 22:
                if (enemylist[n][2] >= 30) {
                    enemylist[n][7] = 0;
                    if (enemylist[n][2] == 30) {
                        if (enemylist[n][5] < 6356992) {
                            enemylist[n][13] = 405;
                        } else {
                            enemylist[n][13] = 495;
                        }
                        enemylist[n][12] = 4;
                    }
                } else if (enemylist[n][12] == 0) {
                    enemylist[n][7] = 1;
                } else if (enemylist[n][2] > 5) {
                    enemylist[n][12] -= 1;
                }
                enemylist[n][14] = speedCos(enemylist[n][12] << 3, enemylist[n][13]);
                enemylist[n][15] = speedSin(enemylist[n][12] << 3, enemylist[n][13]);
                break;
        }
    }

    private void stageEb(final int n) {
        if (enemylist[n][0] == 0) {
            return;
        }

        int px;
        int py;
        if (player != null) {
            px = player.getXFixed();
            py = player.getYFixed();
        } else {
            px = (PLAY_X + (PLAY_W / 2)) << 16;
            py = (PLAY_Y + PLAY_H - 28) << 16;
        }

        int level = startLevel;
        int enemyType = enemylist[n][8];
        int ex = enemylist[n][5];
        int ey = enemylist[n][6];

        stageDochu.tickEnemyShootingStageA(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageB(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageC(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageD(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageE(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageF(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageG(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageH(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageI(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageJ(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageK(n, enemylist, enemyType, ex, ey, px, py, level);
        stageDochu.tickEnemyShootingStageL(n, enemylist, enemyType, ex, ey, px, py, level);

        // Stage 1/2 fodder shooting patterns are handled by StageDochu.tickEnemyShootingStageA/B().

        // Fodder shooting patterns (enemyType 36/37/38/39) are handled by StageDochu.tickEnemyShootingStageD().

        // Stage 5 fodder shooting patterns are handled by StageDochu.tickEnemyShootingStageC().

        // Stage 5 fodder shooting patterns (enemyType 54/55) are handled by StageDochu.tickEnemyShootingStageE().

        // Stage 1/2/3 fodder shooting patterns (enemyType 28/26) are handled by StageDochu.tickEnemyShootingStageF().
        // Stage 2/3 fodder shooting patterns (enemyType 50/51/52) are handled by StageDochu.tickEnemyShootingStageG().
        // Stage 3 fodder shooting patterns (enemyType 29/30) are handled by StageDochu.tickEnemyShootingStageH().
        // Stage 4 fodder shooting patterns (enemyType 32/33/53) are handled by StageDochu.tickEnemyShootingStageI().
        // Stage 4 fodder shooting patterns (enemyType 70) are handled by StageDochu.tickEnemyShootingStageJ().
        // Stage EXTRA fodder shooting patterns (enemyType 90-97) are handled by StageDochu.tickEnemyShootingStageK().
        // Stage 1 fodder shooting patterns (enemyType 31) are handled by StageDochu.tickEnemyShootingStageL().
    }

    // Read UTF-8 text resources with fallback.
    public static String readUtf8TextResource(String path) {
        return I18n.readUtf8TextResource(path);
    }

    // Lazy-load spell card name table.
    private static void ensureSpellCardTextsLoaded() {
        if (spellCardTextLoaded) {
            return;
        }

        String text = readUtf8TextResource(I18n.path("spcard.dat"));
        if (text == null) {
            text = readUtf8TextResource("/res/spcard.dat");
        }
        if (text != null) {
            spellCardNames = splitLinesSimple(text);
        }
        spellCardTextLoaded = true;
    }

    public static void warmupSpellCardTexts() {
        ensureSpellCardTextsLoaded();
    }

    // Resolve spell card display name.
    private static String getSpellCardName(int id) {
        ensureSpellCardTextsLoaded();
        if (spellCardNames != null && id >= 0 && id < spellCardNames.length) {
            String s = spellCardNames[id];
            if (s != null && s.length() > 0) {
                return s;
            }
        }
        return "SpellNo." + (id + 1);
    }

    // Split lines on newline boundaries.
    private static String[] splitLinesSimple(String text) {
        if (text == null) {
            return new String[0];
        }

        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }

        String[] out = new String[lines];
        int pos = 0;
        int start = 0;
        for (int i = 0; i <= text.length(); i++) {
            boolean end = (i == text.length());
            if (!end && text.charAt(i) != '\n') {
                continue;
            }

            int e = i;
            if (e > start && text.charAt(e - 1) == '\r') {
                e--;
            }
            out[pos++] = text.substring(start, e);
            start = i + 1;
        }

        return out;
    }

    // Split lines and wrap by DoJa byte length (ASCII=1, non-ASCII=2).
    public static String[] splitLinesAndWrap(String text, int wrapByteLen) {
        if (text == null) {
            return new String[0];
        }

        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }

        String[] out = new String[lines];
        int pos = 0;
        int start = 0;
        for (int i = 0; i <= text.length(); i++) {
            boolean end = (i == text.length());
            if (!end && text.charAt(i) != '\n') {
                continue;
            }

            int e = i;
            if (e > start && text.charAt(e - 1) == '\r') {
                e--;
            }
            String line = text.substring(start, e);
            line = replaceBrTag(line);
            out[pos++] = wrapByUtf8ByteLen(line, wrapByteLen);
            start = i + 1;
        }

        return out;
    }

    // Replace HTML line breaks with wrap markers.
    private static String replaceBrTag(String s) {
        if (s == null) {
            return null;
        }
        int idx = s.indexOf("<br>");
        if (idx < 0) {
            idx = s.indexOf("<BR>");
        }
        if (idx < 0) {
            return s;
        }
        StringBuffer out = new StringBuffer();
        int p = 0;
        while (true) {
            int i = s.indexOf("<br>", p);
            int len = 4;
            if (i < 0) {
                i = s.indexOf("<BR>", p);
            }
            if (i < 0) {
                out.append(s.substring(p));
                break;
            }
            out.append(s.substring(p, i));
            out.append('|');
            p = i + len;
            if (p >= s.length()) {
                break;
            }
        }
        return out.toString();
    }

    // Force transition to boss stage script.
    private void stageCe() {
        stageRuntime.stageCe();
    }

    // End battle and return to title UI.
    private void endBattleToTitle() {
        if (replayPlayback.isActive()) {
            endReplayToMenu();
            return;
        }

        resultStats.onBattleEndToTitle(replayPlayback.isActive());

        battlePaused = false;
        inGame = false;

        stageClearPanelActive = false;
        stageClearPhoto = null;
        replayRecording.clearPending();

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        player = null;
        if (ui != null) {
            ui.returnToTitleKeepCursor();
        }
    }

    public void endSpellPracticeToMenu() {
        if (replayPlayback.isActive()) {
            endReplayToMenu();
            return;
        }

        resultStats.onSpellPracticeEndToMenu(replayPlayback.isActive());

        battlePaused = false;
        inGame = false;

        stageClearPanelActive = false;
        stageClearPhoto = null;

        replayRecording.stop();
        replayRecording.clearPending();

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        player = null;

        spellPracticeEndFlg = false;
        spellPracticeEndCnt = 0;
        spellPracticeEndActive = false;
        spellPracticeEndWon = false;

        if (ui != null) {
            ui.returnToSpellPracticeKeepCursor();
        }
    }

    private void startSpellPracticeEnd(boolean won, int endCnt) {
        if (!startSpellPractice) {
            return;
        }
        if (spellPracticeEndFlg || spellPracticeEndActive) {
            return;
        }

        // Spell practice end flow state machine (mirrors DoJa).
        spellPracticeEndWon = won;
        spellPracticeEndFlg = true;
        spellPracticeEndCnt = endCnt;

        battlePaused = false;
        timeStop = 0;

        replayRecording.stop();

        // Clear bullets with vanish animation so effects continue during countdown.
        bullets.convertEnemyBulletsToDeathVanish();
        enemyBullets.clear();
    }

    public void tickSpellPracticeEndCountdown() {
        fi++;
        mGcnt++;
        scoreSystem.post(ScoreSystem.EVT_TICK, 0, 0, 0);

        battleEngine.tickStageEffects(effectlist, EFFECT_TABLE);

        int enemyLevel = 0;
        if (hud instanceof BattleHudData) {
            enemyLevel = ((BattleHudData) hud).getLevel();
        }

        boolean hasAimTarget = player != null;
        int aimXFixed = 0;
        int aimYFixed = 0;
        if (hasAimTarget) {
            aimXFixed = player.getXFixed();
            aimYFixed = player.getYFixed();
        }
        battleEngine.tickBullets(PLAY_X, PLAY_Y, PLAY_W, PLAY_H, enemyLevel, hasAimTarget, aimXFixed, aimYFixed);

        bossController.tickHpBarOncePerFrame();

        spellPracticeEndCnt--;
        if (spellPracticeEndCnt <= 0) {
            enterSpellPracticeEndScreen();
        }
    }

    private void enterSpellPracticeEndScreen() {
        spellPracticeEndFlg = false;
        spellPracticeEndCnt = 0;
        spellPracticeEndActive = true;

        if (replayPlayback.isActive()) {
            endReplayToMenu();
            return;
        }

        // Arm pending replay regardless of win/loss (spell practice).
        replayRecording.clearPending();
        if (!replayPlayback.isActive()) {
            int lvl = (hud != null) ? hud.getLevel() : startLevel;
            int score = scoreSystem.getScore();
            int spellId = startSpellId;
            if (spellId < 0) {
                spellId = 255;
            }
            replayRecording.armPendingForStageClear(null, ReplayHeader.FLAG_LOCAL, lvl, currentChara, mStage, score, currentType, mGamemode, spellId);
        }

        spellPracticeEndScreen.enter(spellPracticeEndWon);
    }

    // Spawn effect entry with optional table lookup.
    private void stageKc(int effectId, int xFixed, int yFixed) {
        stageRuntime.stageKc(effectId, xFixed, yFixed);
    }

    // Allocate and initialize effect slot.
    private void stageKcEffect(int effectId, int xFixed, int yFixed) {
        stageRuntime.stageKcEffect(effectId, xFixed, yFixed);
    }

    // Render effect sprites and special lines.
    private void stageMc(final Graphics g) {
        stageRuntime.stageMc(g);
    }

    // Advance stage script and spawn events.
    private void stageMa() {
        stageRuntime.stageMa();
    }

    private void resetStageResultCounters() {
        stageMissCount = 0;
        stageBombUsedCount = 0;
        stageSpellSeenCount = 0;
        stageSpellBonusCount = 0;
        stageLastSpellId = -1;
    }

    private void resetRunResultCounters() {
        runMissCount = 0;
        runBombUsedCount = 0;
        runSpellSeenCount = 0;
        runSpellBonusCount = 0;
        runInitialContinueCount = continueSystem.getRemainingContinues();
    }

    private void onStageSpellIdChanged(int spellId) {
        if (spellId == stageLastSpellId) {
            return;
        }
        stageLastSpellId = spellId;
        if (spellId >= 0) {
            stageSpellSeenCount++;
            runSpellSeenCount++;
        }
    }

    private void enterFinalResultPanelAfterStaffRoll() {
        resultStats.onFinalResultAfterStaffRoll(replayPlayback.isActive());

        int hiScore = scoreSystem.getHiScore();
        int score = scoreSystem.getScore();

        int continuesUsed = runInitialContinueCount - continueSystem.getRemainingContinues();
        if (continuesUsed < 0) {
            continuesUsed = 0;
        }

        stageClearResultPanel.enterFinal(hiScore, score, continuesUsed, runMissCount, runBombUsedCount, runSpellBonusCount, runSpellSeenCount);
        stageClearPanelActive = true;
        stageClearPhoto = null;
    }

    private void enterGameOverFinalResultPanel() {
        if (replayPlayback.isActive()) {
            endReplayToMenu();
            return;
        }

        resultStats.onGameOver(replayPlayback.isActive());

        battlePaused = false;
        inGame = false;

        replayRecording.stop();

        // DoJa: disable replay save after any continue was used.
        replayRecording.clearPending();
        if (!startSpellPractice && (mGamemode == 0 || mGamemode == 1)) {
            int continuesUsed = runInitialContinueCount - continueSystem.getRemainingContinues();
            if (continuesUsed < 0) {
                continuesUsed = 0;
            }
            if (continuesUsed == 0) {
                int lvl = (hud != null) ? hud.getLevel() : startLevel;
                int score = scoreSystem.getScore();
                int spellId = stageLastSpellId;
                if (spellId < 0) {
                    spellId = 255;
                }
                replayRecording.armPendingForStageClear(null, ReplayHeader.FLAG_LOCAL, lvl, currentChara, mStage, score, currentType, mGamemode, spellId);
            }
        }

        scoreSystem.commitScoreToProgress();

        stageClearPhoto = null;
        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        player = null;

        int hiScore = scoreSystem.getHiScore();
        int score = scoreSystem.getScore();

        int continuesUsed = runInitialContinueCount - continueSystem.getRemainingContinues();
        if (continuesUsed < 0) {
            continuesUsed = 0;
        }

        stageClearResultPanel.enterFinal(hiScore, score, continuesUsed, runMissCount, runBombUsedCount, runSpellBonusCount, runSpellSeenCount);

        stageClearPanelActive = true;
    }

    private Image captureBattlePhoto() {
        try {
            Image img = Image.createImage(LOGICAL_W, LOGICAL_H);
            Graphics g = img.getGraphics();
            g.setClip(0, 0, LOGICAL_W, LOGICAL_H);
            renderBattleUi(g);
            return img;
        } catch (Throwable t) {
            return null;
        }
    }

    private void enterStageClearResultPanel() {
        if (stageClearPanelActive) {
            return;
        }
        if (replayPlayback.isActive() && replayPlayback.isBossOnly()) {
            endReplayToMenu();
            return;
        }

        if (replayPlayback.isActive()) {
            endReplayToMenu();
            return;
        }

        // Extra clear: show final result panel directly.
        if (mGamemode == 1) {
            enterFinalResultPanelAfterExtraClear();
            return;
        }

        replayRecording.stop();
        replayRecording.clearPending();

        stageClearPhoto = captureBattlePhoto();

        int hiScore = scoreSystem.getHiScore();
        int score = scoreSystem.getScore();

        // DoJa: stage-clear result uses run-wide continue usage count ("3 - s_continue").
        int continuesUsed = runInitialContinueCount - continueSystem.getRemainingContinues();
        if (continuesUsed < 0) {
            continuesUsed = 0;
        }

        boolean allowReplaySave = false;
        // DoJa: after any continue, replay save is disabled on stage-clear result (a.ci = false).
        if (continuesUsed == 0 && !replayPlayback.isActive() && !startSpellPractice && mGamemode == 0) {
            int lvl = (hud != null) ? hud.getLevel() : startLevel;
            int spellId = stageLastSpellId;
            if (spellId < 0) {
                spellId = 255;
            }

            allowReplaySave = replayRecording.armPendingForStageClear(
                    null, ReplayHeader.FLAG_LOCAL, lvl, currentChara, mStage, score, currentType, mGamemode, spellId);
        }

        // DoJa: stage-clear result shows run-wide totals (ba/ca/ea/da), not per-stage counters.
        stageClearResultPanel.enter(stageClearPhoto, hiScore, score, continuesUsed, runMissCount, runBombUsedCount, runSpellBonusCount, runSpellSeenCount,
                allowReplaySave, currentChara);
        stageClearPanelActive = true;
    }

    private void enterFinalResultPanelAfterExtraClear() {
        if (stageClearPanelActive) {
            return;
        }
        if (replayPlayback.isActive()) {
            endReplayToMenu();
            return;
        }

        // Keep accounting consistent with final result screens.
        resultStats.onFinalResultAfterStaffRoll(replayPlayback.isActive());

        battlePaused = false;
        inGame = false;

        replayRecording.stop();

        // DoJa : replay save is disabled after using continue (ci=false).
        // Replay recording is also disabled when any cheat is active.
        replayRecording.clearPending();
        int continuesUsed = runInitialContinueCount - continueSystem.getRemainingContinues();
        if (continuesUsed < 0) {
            continuesUsed = 0;
        }
        if (!startSpellPractice && continuesUsed == 0) {
            int lvl = (hud != null) ? hud.getLevel() : startLevel;
            int score = scoreSystem.getScore();
            int spellId = stageLastSpellId;
            if (spellId < 0) {
                spellId = 255;
            }
            replayRecording.armPendingForStageClear(null, ReplayHeader.FLAG_LOCAL, lvl, currentChara, mStage, score, currentType, mGamemode, spellId);
        }

        UnlockFlags.tryUnlockAliceOnExtraClear(mStage, mGamemode);
        scoreSystem.commitScoreToProgress();

        stageClearPhoto = null;
        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        player = null;

        int hiScore = scoreSystem.getHiScore();
        int score = scoreSystem.getScore();

        stageClearResultPanel.enterFinal(hiScore, score, continuesUsed, runMissCount, runBombUsedCount, runSpellBonusCount, runSpellSeenCount);
        stageClearPanelActive = true;
    }

    // Advance to next stage or end the run.
    public void stageAdvanceToNextStageOrEnd() {
        if (replayPlayback.isActive()) {
            endReplayToMenu();
            return;
        }
        mStep = 0;
        mGcnt = 0;
        mTacnt = 0;
        // Keep stage start state consistent with startGameInternal() (replay determinism).
        fi = 0;
        shooting = true;
        battlePaused = false;
        bombCnt = 0;
        bossSpellCutInCnt = 0;
        bossSpellCutInPrevBspellstep = 0;
        bossSpellCutInStage = 0;
        bossSpellCutInSpellId = -1;
        bossSpellCutInChara = 0;
        bossSpellCutInStartFi = 0;
        mOriginx = 0;
        mOriginy = 0;
        ca = 0;
        sDeadcnt = 0;
        mResetf = 0;
        timeStop = 0;
        stageRuntime.setTalkCnt(3);
        mBossmodef = false;
        mLatterf = false;
        sBossf = 0;
        sBariacnt = 0;
        sBbaria = 0;
        sBstock = 0;
        for (int i = 0; i < 3; i++) {
            sBossx[i] = 0;
            sBossy[i] = 0;
        }

        bullets.clear();
        enemyBullets.clear();
        enemies.clear();
        stageIb();

        if (mGamemode == 2 || mGamemode == 3) {
            endBattleToTitle();
            return;
        }

        scoreSystem.post(ScoreSystem.EVT_STAGE_ADVANCED, mStage, 0, 0);

        if (mStage >= 5) {
            UnlockFlags.tryUnlockExtraOnStoryClear(mStage, mGamemode, startLevel, continueSystem.getRemainingContinues(), runStartStage, startPracBoss);
            UnlockFlags.tryUnlockAliceOnExtraClear(mStage, mGamemode);
            scoreSystem.commitScoreToProgress();
            endBattleToTitle();
            return;
        }

        mStage = mStage + 1;
        startStage = mStage;
        bgMoveSpd = 0;

        stageInitialContinueCount = continueSystem.getRemainingContinues();
        resetStageResultCounters();
        stageClearPanelActive = false;
        stageClearPhoto = null;

        if (!replayPlayback.isActive()) {
            replayRecording.clearPending();
            replayRecording.startForNextStage();
        }

        ensureTalkTableLoaded();
        if (cc != null) {
            player = new Player(cc, currentChara, currentType, PLAY_X + (PLAY_W / 2), PLAY_Y + PLAY_H - 28);
        }

        replayRecording.captureStageStartSnapshot(random, continueSystem, hud, scoreSystem, player);
    }

    // Wrap text by UTF-8 byte length.
    private static String wrapByUtf8ByteLen(String s, int maxBytes) {
        if (s == null) {
            return "";
        }
        int byteCount = 0;
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            String ch = s.substring(i, i + 1);
            byteCount += utf8ByteLen(ch);
            if ("|".equals(ch)) {
                byteCount = 0;
            }
            if (byteCount > maxBytes) {
                byteCount = utf8ByteLen(ch);
                out.append('|');
            }
            out.append(ch);
        }
        return out.toString();
    }

    // Compute DoJa byte length for a string (ASCII=1, non-ASCII=2).
    private static int utf8ByteLen(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        // DoJa (original) used 1 byte for ASCII and 2 bytes for non-ASCII in many UI layouts.
        // Using UTF-8 byte length would wrap CJK too early (3 bytes per char).
        char c = s.charAt(0);
        return (c <= 0x7F) ? 1 : 2;
    }

    // Fixed-point speed X component.
    private static int speedCos(int n, int dirDeg) {
        return BattleMath.speedCos(n, dirDeg);
    }

    // Fixed-point speed Y component.
    private static int speedSin(int n, int dirDeg) {
        return BattleMath.speedSin(n, dirDeg);
    }

    // Spawn bomb bullets with character-specific logic.
    private void bombSpawnXa(int n, int i, int shotPower, int speed, int fx, int fy) {
        int baseBulletId;
        int frames;
        int moveType;
        int p = shotPower;
        switch (n) {
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                baseBulletId = 61 + ((n - 3) << 2);
                frames = 1;
                int dx = fx - 6356992;
                int dy = fy - 7929856;
                int dist2 = (dx * dx) + (dy * dy);
                int d2 = gj(dist2);
                speed = d2 >> 3;
                if (speed < 8) {
                    speed = 8;
                }
                if (speed > 14) {
                    speed = 14;
                }

                int ang0 = arcTan2Deg(7929856 - fy, 6356992 - fx);
                if (fx < 6356992) {
                    ang0 = normalizeDeg(ang0 + 290);
                    moveType = 4;
                } else {
                    ang0 = normalizeDeg(ang0 + 70);
                    moveType = 5;
                }
                bullets.spawnPlayerEx(baseBulletId, frames, moveType, ang0, speed, fx, fy, p);
                return;
            case 16:
                baseBulletId = 733;
                frames = 4;
                moveType = 6;
                break;
            case 17:
                baseBulletId = 737;
                frames = 4;
                moveType = 7;
                break;
            default:
                return;
        }

        int ang = normalizeDeg(i + 270);
        bullets.spawnPlayerEx(baseBulletId, frames, moveType, ang, speed, fx, fy, p);
    }

    // Normalize degrees to [0, 360).
    private static int normalizeDeg(int deg) {
        return BattleMath.normalizeDeg(deg);
    }

    // Integer sqrt helper.
    private static int gj(final int n) {
        if (n <= 0) {
            return 0;
        }
        int n2 = 1;
        for (int i = n; i >= n2; n2 <<= 1, i >>= 1) {
        }
        int n3;
        do {
            n3 = n2;
        } while ((n2 = (n / n2 + n2) >> 1) < n3);
        return n3;
    }

    // Fixed-point atan2 to degrees.
    private static int arcTan2Deg(int yFixed, int xFixed) {
        return BattleMath.arcTan2Deg(yFixed, xFixed);
    }

    public static final class BattleRenderAccess {
        private final GameCore core;

        public BattleRenderAccess(GameCore core) {
            this.core = core;
        }

        public CcTable cc() {
            return core.cc;
        }

        public String resourceError() {
            return core.resourceError;
        }

        public boolean debugFpsEnabled() {
            return core.optDebugFpsEnabled;
        }

        public boolean debugPerfEnabled() {
            return core.optDebugPerfEnabled;
        }

        public boolean debugResourceEnabled() {
            return core.optDebugResourceEnabled;
        }

        public ImageBank imgs() {
            return core.imgs;
        }

        public BulletSprites bulletSprites() {
            return core.bulletSprites;
        }

        public BattleHudData hud() {
            return core.hud;
        }

        public BulletSystem bullets() {
            return core.bullets;
        }

        public BossController bossController() {
            return core.bossController;
        }

        public StageRuntime stageRuntime() {
            return core.stageRuntime;
        }

        public int stage() {
            return core.mStage;
        }

        public int stageNa() {
            return core.stageNa();
        }

        public int step() {
            return core.mStep;
        }

        public int gameMode() {
            return core.mGamemode;
        }

        public int currentChara() {
            return core.currentChara;
        }

        public boolean bossMode() {
            return core.mBossmodef;
        }

        public int bossF() {
            return core.sBossf;
        }

        public int bossStock() {
            return core.sBstock;
        }

        public int bossBarrier() {
            return core.sBbaria;
        }

        public int gcnt() {
            return core.mGcnt;
        }

        public int fi() {
            return core.fi;
        }

        public int taCnt() {
            return core.mTacnt;
        }

        public int timeStop() {
            return core.timeStop;
        }

        public int bgMoveSpd() {
            return core.bgMoveSpd;
        }

        public int bombCnt() {
            return core.bombCnt;
        }

        public int deadcnt() {
            return core.sDeadcnt;
        }

        public int[][] enemylist() {
            return core.enemylist;
        }

        public int[] bossWrk() {
            return core.mBossWrk;
        }

        public boolean disableBossBombShieldCheat() {
            return core.disableBossBombShieldCheat;
        }

        public int[] bossX() {
            return core.sBossx;
        }

        public int[] bossY() {
            return core.sBossy;
        }

        public boolean startSpellPractice() {
            return core.startSpellPractice;
        }

        public int startSpellId() {
            return core.startSpellId;
        }

        public String getSpellCardName(int id) {
            return GameCore.getSpellCardName(id);
        }

        public boolean isDialogueActiveForInput(int frame) {
            return core.isDialogueActiveForInput(frame);
        }

        public int bossSpellCutInCnt() {
            return core.bossSpellCutInCnt;
        }

        public int bossSpellCutInStage() {
            return core.bossSpellCutInStage;
        }

        public int bossSpellCutInChara() {
            return core.bossSpellCutInChara;
        }

        public int bossSpellCutInStartFi() {
            return core.bossSpellCutInStartFi;
        }

        public int bossSpellCutInPhaseStartFi() {
            return core.bossSpellCutInPhaseStartFi;
        }

        public boolean dynamicCutInEnabled() {
            return core.optDynamicCutInEnabled;
        }

        public boolean bombOverlayAlphaEnabled() {
            return core.optBombOverlayAlphaEnabled;
        }

        public boolean bossSpellCutInAlphaEnabled() {
            return core.optBossSpellCutInAlphaEnabled;
        }

        public boolean blindMaskFadeEnabled() {
            return core.optBlindMaskFadeEnabled;
        }

        public boolean heavyBgEnabled() {
            return core.optHeavyBgEnabled;
        }

        public DropItemSystem dropItems() {
            return core.dropItems;
        }

        public DropItemSystem.IconDrawer dropItemDrawer() {
            return core.dropItemDrawer;
        }

        public ScoreSystem scoreSystem() {
            return core.scoreSystem;
        }

        public Player player() {
            return core.player;
        }

        public int originX() {
            return core.mOriginx;
        }

        public int originY() {
            return core.mOriginy;
        }

        public boolean battlePaused() {
            return core.battlePaused;
        }

        public boolean stageClearPanelActive() {
            return core.stageClearPanelActive;
        }

        public StageClearResultPanel stageClearResultPanel() {
            return core.stageClearResultPanel;
        }

        public BattlePauseScreen pauseScreen() {
            return core.pauseScreen;
        }

        public boolean spellPracticeEndActive() {
            return core.startSpellPractice && core.spellPracticeEndActive;
        }

        public SpellPracticeEndScreen spellPracticeEndScreen() {
            return core.spellPracticeEndScreen;
        }

        public int endingFadeToWhite() {
            return core.endingFadeToWhite;
        }

        public int hFlash() {
            return core.hFlash;
        }

        public void clearHFlash() {
            core.hFlash = 0;
        }

        public int iFlash() {
            return core.iFlash;
        }

        public void decIFlash() {
            core.iFlash--;
        }

        public void stageMc(Graphics g) {
            core.stageMc(g);
        }

        public int[][] effectlist() {
            return core.effectlist;
        }

        public String debugLine() {
            return core.debugLine;
        }

        public String debugLine2() {
            return core.debugLine2;
        }

        public String debugLine3() {
            return core.debugLine3;
        }

        public String debugLine4() {
            return core.debugLine4;
        }

        public String debugLine5() {
            return core.debugLine5;
        }

        public int[][] effectTable() {
            return GameCore.EFFECT_TABLE;
        }
    }
}
