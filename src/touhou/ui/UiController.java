package touhou.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.BulletSprites;
import touhou.GameCore;
import touhou.GameOptions;
import touhou.ImageBank;
import touhou.ResultStats;
import touhou.SoundEffectSystem;
import touhou.stage.StagePreloader;
import touhou.UnlockFlags;

public final class UiController {
    public static final int STATE_TITLE = 0;
    public static final int STATE_LOADING = 9;
    public static final int STATE_START_SETUP = 1;
    public static final int STATE_HELP = 2;
    public static final int STATE_SPELL_PRACTICE = 3;
    public static final int STATE_REPLAY = 4;
    public static final int STATE_REPLAY_RECEIVE = 11;
    public static final int STATE_REPLAY_SEND = 12;
    public static final int STATE_RESULT = 5;
    public static final int STATE_MUSIC_ROOM = 6;
    public static final int STATE_OPTION = 7;
    public static final int STATE_QUIT = 8;
    public static final int STATE_STORY = 10;
    // Shown right after LoadingScreen ("少女祈祷中") to apply i18n before entering title.
    public static final int STATE_STARTUP_LANGUAGE = 13;

    public interface Listener {
        void onStartGameRequested(int gamemode, int level, int chara, int type, int startStage, boolean pracBoss);

        // Start replay playback from a saved slot.
        void onStartReplayRequested(int slot, boolean bossOnly);

        void onReplayBtShareRequested(int slot, boolean receive);

        void onStartSpellPracticeRequested(int chara, int type, int stageIndex, int spellId);

        void onQuitRequested();

        void onPlayBgmRequested(int trackId);

        void onPlaySeTypeRequested(int seType);

        void onVolumesChanged(int bgmVol, int seVol);

        void onSeMaskChanged(int mask);

        void onMusicRoomVolumeChanged(int bgmVol);
    }

    private final ImageBank images;
    private final BulletSprites sprites;
    private final Listener listener;

    private TitleScreen title;
    private final LoadingScreen loading;
    private final StartupLanguageMenu startupLanguage;
    private StartSetupScreen setup;
    private HelpScreen help;
    private StoryScreen story;
    private SpellPracticeScreen spellPractice;
    private ReplayScreen replay;
    private ReplayReceiveScreen replayReceive;
    private ReplaySendScreen replaySend;
    private ResultScreen result;
    private MusicRoomScreen musicRoom;
    private OptionScreen option;
    private QuitScreen quit;

    private final StagePreloader stagePreloader = new StagePreloader();

    private int state;

    // Cross-thread UI messaging for BT share worker.
    private volatile String pendingReplayMessage;

    public UiController(ImageBank images, BulletSprites sprites, Listener listener) {
        this.images = images;
        this.sprites = sprites;
        this.listener = listener;

        this.loading = new LoadingScreen();
        this.startupLanguage = new StartupLanguageMenu();

        this.loading.enter();
        this.state = STATE_LOADING;
    }

    // Defer creating language-dependent screens until after startup language selection.
    private void ensureMainScreens() {
        if (title != null) {
            return;
        }
        title = new TitleScreen();
        setup = new StartSetupScreen();
        help = new HelpScreen();
        story = new StoryScreen();
        spellPractice = new SpellPracticeScreen();
        replay = new ReplayScreen();
        replayReceive = new ReplayReceiveScreen();
        replaySend = new ReplaySendScreen();
        result = new ResultScreen();
        musicRoom = new MusicRoomScreen();
        option = new OptionScreen(new OptionScreen.Listener() {
            public void onVolumesChanged(int bgmVol, int seVol) {
                if (UiController.this.listener != null) {
                    UiController.this.listener.onVolumesChanged(bgmVol, seVol);
                }
            }

            public void onSeMaskChanged(int seMask) {
                if (UiController.this.listener != null) {
                    UiController.this.listener.onSeMaskChanged(seMask);
                }
            }
        });
        quit = new QuitScreen();
    }

    private void preloadStageSync(int stage, int chara, int type) {
        if (images == null || sprites == null) {
            return;
        }

        // Avoid first-time hitch when the boss spell name is first shown.
        GameCore.warmupSpellCardTexts();

        int[] opt = GameOptions.load();
        if (opt == null) {
            opt = GameOptions.defaults();
        }
        boolean playerShotAlphaEnabled = GameOptions.isPlayerShotAlphaEnabled(opt);
        boolean bossSpellCutInAlphaEnabled = GameOptions.isBossSpellCutInAlphaEnabled(opt);
        boolean bombOverlayAlphaEnabled = GameOptions.isBombOverlayAlphaEnabled(opt);

        stagePreloader.setPlayerShotAlphaEnabled(playerShotAlphaEnabled);
        stagePreloader.setBossSpellCutInAlphaEnabled(bossSpellCutInAlphaEnabled);
        stagePreloader.setBombOverlayAlphaEnabled(bombOverlayAlphaEnabled);
        stagePreloader.enter(stage, chara, type);
        int guard = stagePreloader.getStepMax() + 8;
        for (int i = 0; i < guard && !stagePreloader.isDone(); i++) {
            stagePreloader.update(images, sprites);
        }
    }

    public void resetToTitle() {
        ensureMainScreens();
        state = STATE_TITLE;
        title.reset();
        ResultStats.INSTANCE.onReturnToTitle();
    }

    public void returnToTitleKeepCursor() {
        ensureMainScreens();
        state = STATE_TITLE;
        title.onReturnToTitle();
        ResultStats.INSTANCE.onReturnToTitle();
    }

    public void returnToSpellPracticeKeepCursor() {
        ensureMainScreens();
        spellPractice.enter(false);
        state = STATE_SPELL_PRACTICE;
    }

    // Return from gameplay to replay menu.
    public void returnToReplayKeepCursor() {
        ensureMainScreens();
        replay.enter();
        state = STATE_REPLAY;
    }

    public void returnToReplayKeepCursorWithMessage(String msg) {
        ensureMainScreens();
        replay.enter();
        replay.showMessageExternal(msg);
        state = STATE_REPLAY;
    }

    public void showReplayMessage(String msg) {
        pendingReplayMessage = msg;
    }

    public void update(int pressed) {
        // Play UI S.E. after the screen update so Options volume changes take effect immediately.
        int stateBefore = state;

        if (state == STATE_LOADING) {
            loading.update(pressed, images, sprites);
            if (loading.isDone()) {
                startupLanguage.enter();
                state = STATE_STARTUP_LANGUAGE;
            }
            return;
        }

        if (state == STATE_STARTUP_LANGUAGE) {
            StartupLanguageMenu.Result r = startupLanguage.update(pressed);
            if (r != null && r.kind == StartupLanguageMenu.Result.KIND_DONE) {
                ensureMainScreens();
                resetToTitle();
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_TITLE) {
            int action = title.update(pressed);
            if (action == TitleScreen.ACTION_START) {
                setup.enter(0);
                state = STATE_START_SETUP;
            } else if (action == TitleScreen.ACTION_EXTRA_START) {
                setup.enter(1);
                state = STATE_START_SETUP;
            } else if (action == TitleScreen.ACTION_PRACTICE) {
                setup.enter(2);
                state = STATE_START_SETUP;
            } else if (action == TitleScreen.ACTION_HELP) {
                help.enter();
                state = STATE_HELP;
            } else if (action == TitleScreen.ACTION_STORY) {
                story.enter();
                state = STATE_STORY;
            } else if (action == TitleScreen.ACTION_SPELL_PRACTICE) {
                spellPractice.enter(true);
                state = STATE_SPELL_PRACTICE;
            } else if (action == TitleScreen.ACTION_REPLAY) {
                replay.enter();
                state = STATE_REPLAY;
            } else if (action == TitleScreen.ACTION_RESULT) {
                result.enter();
                state = STATE_RESULT;
            } else if (action == TitleScreen.ACTION_MUSIC_ROOM) {
                musicRoom.enter();
                state = STATE_MUSIC_ROOM;
                if (listener != null) {
                    listener.onMusicRoomVolumeChanged(musicRoom.getVolume());
                }
            } else if (action == TitleScreen.ACTION_OPTION) {
                option.enter();
                state = STATE_OPTION;
            } else if (action == TitleScreen.ACTION_QUIT) {
                quit.enter();
                state = STATE_QUIT;
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_START_SETUP) {
            StartSetupScreen.Result r = setup.update(pressed);
            if (r != null) {
                if (r.kind == StartSetupScreen.Result.KIND_BACK_TO_TITLE) {
                    returnToTitleKeepCursor();
                } else if (r.kind == StartSetupScreen.Result.KIND_START_GAME) {
                    preloadStageSync(r.startStage, r.chara, r.type);
                    if (listener != null) {
                        listener.onStartGameRequested(r.gamemode, r.level, r.chara, r.type, r.startStage, r.pracBoss);
                    }
                    return;
                }
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_HELP) {
            HelpScreen.Result r = help.update(pressed);
            if (r != null && r.kind == HelpScreen.Result.KIND_BACK_TO_TITLE) {
                returnToTitleKeepCursor();
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_STORY) {
            StoryScreen.Result r = story.update(pressed);
            if (r != null && r.kind == StoryScreen.Result.KIND_BACK_TO_TITLE) {
                returnToTitleKeepCursor();
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_SPELL_PRACTICE) {
            SpellPracticeScreen.Result r = spellPractice.update(pressed);
            if (r != null) {
                if (r.kind == SpellPracticeScreen.Result.KIND_BACK_TO_TITLE) {
                    returnToTitleKeepCursor();
                } else if (r.kind == SpellPracticeScreen.Result.KIND_START_PRACTICE) {
                    if (r.chara == 2 && !UnlockFlags.isAliceUnlocked()) {
                        return;
                    }
                    preloadStageSync(r.stageIndex, r.chara, r.type);
                    if (listener != null) {
                        listener.onStartSpellPracticeRequested(r.chara, r.type, r.stageIndex, r.spellId);
                    }
                    return;
                }
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_REPLAY) {
            String msg = pendingReplayMessage;
            if (msg != null) {
                pendingReplayMessage = null;
                replay.showMessageExternal(msg);
            }
            ReplayScreen.Result r = replay.update(pressed);
            if (r != null) {
                if (r.kind == ReplayScreen.Result.KIND_BACK_TO_TITLE) {
                    returnToTitleKeepCursor();
                } else if (r.kind == ReplayScreen.Result.KIND_START_REPLAY) {
                    if (listener != null) {
                        listener.onStartReplayRequested(r.slot, r.bossOnly);
                    }
                } else if (r.kind == ReplayScreen.Result.KIND_BT_SHARE) {
                    if (listener != null) {
                        listener.onReplayBtShareRequested(r.slot, r.btReceive);
                    }
                } else if (r.kind == ReplayScreen.Result.KIND_OPEN_BT_RECEIVE) {
                    replayReceive.enter();
                    state = STATE_REPLAY_RECEIVE;
                } else if (r.kind == ReplayScreen.Result.KIND_OPEN_BT_SEND) {
                    replaySend.enter(r.slot);
                    state = STATE_REPLAY_SEND;
                }
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_REPLAY_RECEIVE) {
            ReplayReceiveScreen.Result r = replayReceive.update(pressed);
            if (r != null && r.kind == ReplayReceiveScreen.Result.KIND_BACK_TO_REPLAY) {
                state = STATE_REPLAY;
                // Refresh replay list so newly received data shows up immediately.
                if (replay != null) {
                    replay.refreshSlotsKeepCursor();
                }
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_REPLAY_SEND) {
            ReplaySendScreen.Result r = replaySend.update(pressed);
            if (r != null && r.kind == ReplaySendScreen.Result.KIND_BACK_TO_REPLAY) {
                state = STATE_REPLAY;
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_RESULT) {
            ResultScreen.Result r = result.update(pressed);
            if (r != null && r.kind == ResultScreen.Result.KIND_BACK_TO_TITLE) {
                returnToTitleKeepCursor();
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_MUSIC_ROOM) {
            MusicRoomScreen.Result r = musicRoom.update(pressed);
            if (r != null) {
                if (r.kind == MusicRoomScreen.Result.KIND_BACK_TO_TITLE) {
                    returnToTitleKeepCursor();
                } else if (r.kind == MusicRoomScreen.Result.KIND_PLAY_TRACK) {
                    if (listener != null) {
                        listener.onPlayBgmRequested(r.trackId);
                    }
                } else if (r.kind == MusicRoomScreen.Result.KIND_VOLUME_CHANGED) {
                    if (listener != null) {
                        listener.onMusicRoomVolumeChanged(r.trackId);
                    }
                }
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_OPTION) {
            OptionScreen.Result r = option.update(pressed);
            if (r != null && r.kind == OptionScreen.Result.KIND_BACK_TO_TITLE) {
                returnToTitleKeepCursor();
            }
            playUiSe(stateBefore, pressed);
            return;
        }

        if (state == STATE_QUIT) {
            QuitScreen.Result r = quit.update(pressed);
            if (r != null) {
                if (r.kind == QuitScreen.Result.KIND_BACK_TO_TITLE) {
                    returnToTitleKeepCursor();
                } else if (r.kind == QuitScreen.Result.KIND_QUIT) {
                    if (listener != null) {
                        listener.onQuitRequested();
                    }
                }
            }
        }

        playUiSe(stateBefore, pressed);
    }

    private void playUiSe(int stateBefore, int pressed) {
        if ((pressed & (GameCanvas.UP_PRESSED | GameCanvas.DOWN_PRESSED | GameCanvas.LEFT_PRESSED | GameCanvas.RIGHT_PRESSED)) != 0) {
            playSe(SoundEffectSystem.SE_NAV);
        }
        if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
            playSe(SoundEffectSystem.SE_SELECT);
        }
        if (stateBefore == STATE_TITLE && (pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            playSe(SoundEffectSystem.SE_SELECT);
        }
        if (stateBefore != STATE_TITLE && (pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            playSe(SoundEffectSystem.SE_BACK);
        }
    }

    private void playSe(int seType) {
        if (listener == null) {
            return;
        }
        listener.onPlaySeTypeRequested(seType);
    }

    public void render(Graphics g) {
        if (state == STATE_LOADING) {
            loading.render(g);
            return;
        }
        if (state == STATE_STARTUP_LANGUAGE) {
            startupLanguage.render(g, images);
            return;
        }
        if (state == STATE_TITLE) {
            title.render(g, images, sprites);
            return;
        }
        if (state == STATE_START_SETUP) {
            setup.render(g, images, sprites);
            return;
        }
        if (state == STATE_HELP) {
            help.render(g, images);
            return;
        }
        if (state == STATE_STORY) {
            story.render(g, images);
            return;
        }
        if (state == STATE_SPELL_PRACTICE) {
            spellPractice.render(g, images, sprites);
            return;
        }
        if (state == STATE_REPLAY) {
            replay.render(g, images, sprites);
            return;
        }
        if (state == STATE_REPLAY_RECEIVE) {
            replayReceive.render(g, images, sprites);
            return;
        }
        if (state == STATE_REPLAY_SEND) {
            replaySend.render(g, images, sprites);
            return;
        }
        if (state == STATE_RESULT) {
            result.render(g);
            return;
        }
        if (state == STATE_MUSIC_ROOM) {
            musicRoom.render(g);
            return;
        }
        if (state == STATE_OPTION) {
            option.render(g);
            return;
        }
        if (state == STATE_QUIT) {
            title.render(g, images, sprites);
            quit.render(g);
        }
    }
    
    public int getState() {
        return state;
    }
}
