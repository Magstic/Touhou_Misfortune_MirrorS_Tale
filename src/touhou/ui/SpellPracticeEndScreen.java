package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import touhou.BulletSprites;
import touhou.GameCore;
import touhou.i18n.TextId;
import touhou.i18n.UiText;
import touhou.replay.ReplayHeader;
import touhou.replay.ReplayRmsStore;
import touhou.replay.ReplaySaveService;

public final class SpellPracticeEndScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_RETRY = 1;
        public static final int KIND_EXIT = 2;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static final int SCENE_RETRY_ASK = 0;
    private static final int SCENE_REPLAY_ASK_SAVE = 1;
    private static final int SCENE_REPLAY_SLOT = 2;
    private static final int SCENE_REPLAY_SAVE = 3;
    private static final int SCENE_REPLAY_OK = 4;
    private static final int SCENE_REPLAY_FAIL = 5;

    private static final Font FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    private int scene;
    private int cursor;

    private int replayCursor;

    private final ReplayHeader[] slotHeaders = new ReplayHeader[ReplayRmsStore.SLOT_COUNT];
    private final byte[][] slotNames = new byte[ReplayRmsStore.SLOT_COUNT][];
    private final byte[] tmpFull = new byte[ReplayRmsStore.DATA_SIZE];
    private final byte[] tmpBoss = new byte[ReplayRmsStore.DATA_SIZE];

    public SpellPracticeEndScreen() {
        for (int i = 0; i < ReplayRmsStore.SLOT_COUNT; i++) {
            slotHeaders[i] = new ReplayHeader();
            slotNames[i] = new byte[ReplayRmsStore.NAME_SIZE];
        }
    }

    public void enter(boolean won) {
        scene = SCENE_RETRY_ASK;
        // DoJa behavior: default cursor differs by win/loss.
        // - win: default "No"
        // - loss: default "Yes"
        cursor = won ? 1 : 0;
        replayCursor = 0;
    }

    public Result update(int pressed) {
        if (scene == SCENE_RETRY_ASK) {
            if ((pressed & (GameCore.UP_PRESSED | GameCore.DOWN_PRESSED)) != 0) {
                cursor ^= 1;
                return null;
            }
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                if (cursor == 0) {
                    return new Result(Result.KIND_RETRY);
                }
                // If replay recording is disabled (e.g. cheat mode), there is no pending replay to save.
                if (!ReplaySaveService.hasPending()) {
                    return new Result(Result.KIND_EXIT);
                }
                scene = SCENE_REPLAY_ASK_SAVE;
                cursor = 0;
            }
            return null;
        }

        if (scene == SCENE_REPLAY_ASK_SAVE) {
            if (!ReplaySaveService.hasPending()) {
                return new Result(Result.KIND_EXIT);
            }
            if ((pressed & (GameCore.UP_PRESSED | GameCore.DOWN_PRESSED)) != 0) {
                cursor ^= 1;
                return null;
            }
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                if (cursor == 0) {
                    return new Result(Result.KIND_EXIT);
                }
                enterReplaySlotScene();
            }
            if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
                return new Result(Result.KIND_EXIT);
            }
            return null;
        }

        if (scene == SCENE_REPLAY_SLOT) {
            int max = ReplayRmsStore.SLOT_COUNT;
            if ((pressed & GameCore.DOWN_PRESSED) != 0) {
                replayCursor = (replayCursor + 1) % max;
            } else if ((pressed & GameCore.UP_PRESSED) != 0) {
                replayCursor = (replayCursor - 1 + max) % max;
            } else if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                scene = SCENE_REPLAY_SAVE;
            } else if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
                return new Result(Result.KIND_EXIT);
            }
        } else if (scene == SCENE_REPLAY_OK) {
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                enterReplaySlotScene();
            }
            if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
                return new Result(Result.KIND_EXIT);
            }
        } else if (scene == SCENE_REPLAY_FAIL) {
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                enterReplaySlotScene();
            }
            if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
                enterReplaySlotScene();
            }
        }

        if (scene == SCENE_REPLAY_SAVE) {
            boolean ok = ReplaySaveService.savePendingToSlot(replayCursor);
            scene = ok ? SCENE_REPLAY_OK : SCENE_REPLAY_FAIL;
        }

        return null;
    }

    public void render(Graphics g) {
        render(g, null);
    }

    public void render(Graphics g, BulletSprites sprites) {
        if (g == null) {
            return;
        }

        if (scene == SCENE_RETRY_ASK) {
            // semi-transparent black background (DoJa style)
            UiDraw.fillRectAlpha(g, 0, 8, 194, 226, 0x000000, 128);
            UiDraw.drawString2(g, FONT, UiText.get(TextId.SPELL_PRACTICE_RETRY_ASK), 97, 105, 1, 0xFF0000, 0x770000);
            String s0 = UiText.get(TextId.SPELL_PRACTICE_RETRY_YES);
            String s1 = UiText.get(TextId.SPELL_PRACTICE_RETRY_NO);
            if (cursor == 0) {
                UiDraw.drawString2(g, FONT, s0, 97, 130, 1, 0xFFFFFF, 0x777777);
                UiDraw.drawString2(g, FONT, s1, 97, 150, 1, 0x777777, 0x333333);
            } else {
                UiDraw.drawString2(g, FONT, s0, 97, 130, 1, 0x777777, 0x333333);
                UiDraw.drawString2(g, FONT, s1, 97, 150, 1, 0xFFFFFF, 0x777777);
            }
            return;
        }

        if (scene == SCENE_REPLAY_ASK_SAVE) {
            StageClearResultPanel.renderReplayListBackgroundOrFallback(g, sprites);
            UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_ASK_SAVE_REPLAY), 120, 105, 1, 0xFFFFFF, 0x777777);
            if (cursor == 0) {
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SAVE_NO), 120, 130, 1, 0xFFFFFF, 0x777777);
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SAVE_YES), 120, 150, 1, 0x777777, 0x333333);
            } else {
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SAVE_NO), 120, 130, 1, 0x777777, 0x333333);
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SAVE_YES), 120, 150, 1, 0xFFFFFF, 0x777777);
            }
            return;
        }

        renderReplayOverlay(g, sprites);
    }

    private void enterReplaySlotScene() {
        scene = SCENE_REPLAY_SLOT;

        if (replayCursor < 0) {
            replayCursor = 0;
        }
        if (replayCursor >= ReplayRmsStore.SLOT_COUNT) {
            replayCursor = ReplayRmsStore.SLOT_COUNT - 1;
        }

        ReplaySaveUi.loadAllSlots(slotHeaders, slotNames, tmpFull, tmpBoss);
    }

    private void renderReplayOverlay(Graphics g, BulletSprites sprites) {
        boolean showList = (scene == SCENE_REPLAY_SLOT || scene == SCENE_REPLAY_OK || scene == SCENE_REPLAY_FAIL);
        if (!showList) {
            return;
        }

        StageClearResultPanel.renderReplayListBackgroundOrFallback(g, sprites);
        ReplaySaveUi.renderSlotListCompact(g, FONT, replayCursor, slotHeaders, UiText.get(TextId.COMMON_SLOT_PREFIX));
        // Removed bottom key hint text.

        if (scene == SCENE_REPLAY_OK || scene == SCENE_REPLAY_FAIL) {
            ReplaySaveUi.renderSaveResultBox(g, FONT, scene == SCENE_REPLAY_OK, 104);
        }
    }

}
