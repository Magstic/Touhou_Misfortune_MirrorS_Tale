package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import touhou.BulletSprites;
import touhou.GameCore;
import touhou.GameOptions;
import touhou.ImageBank;
import touhou.i18n.TextId;
import touhou.i18n.UiText;
import touhou.replay.ReplayHeader;
import touhou.replay.ReplayRmsStore;
import touhou.replay.ReplaySaveService;
import touhou.stage.StagePreloader;

public final class StageClearResultPanel {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_ADVANCE_STAGE = 1;
        public static final int KIND_BACK_TO_TITLE = 2;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static final int SCENE_MAIN = 0;
    private static final int SCENE_REPLAY_SLOT = 1;
    private static final int SCENE_REPLAY_SAVE = 2;
    private static final int SCENE_REPLAY_OK = 3;
    private static final int SCENE_REPLAY_FAIL = 4;

    // Final result replay save flow (DoJa  jg()/kg()/lg()).
    private static final int SCENE_FINAL_ASK_SAVE = 10;
    private static final int SCENE_FINAL_REPLAY_SLOT = 11;
    private static final int SCENE_FINAL_REPLAY_SAVE = 12;
    private static final int SCENE_FINAL_REPLAY_OK = 13;
    private static final int SCENE_FINAL_REPLAY_FAIL = 14;

    // Panel mode:
    // - MODE_STAGE_CLEAR: shown after each stage, can optionally save replay.
    // - MODE_FINAL: game-end result screen shown after staff roll. It has its own layout and input rules.
    private static final int MODE_STAGE_CLEAR = 0;
    private static final int MODE_FINAL = 1;

    private static final int PHOTO_SRC_X = 0;
    private static final int PHOTO_SRC_Y = 8;
    private static final int PHOTO_SRC_W = 194;
    private static final int PHOTO_SRC_H = 226;

    private static final int SLIDE_MAX = 480;

    private static final Font FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    private int mode;
    private int scene;
    private int subsecCount;
    private int slide;
    private boolean leaving;
    private int chara;

    private Image photo;

    private int hiScore;
    private int score;
    private int continuesUsed;
    private int missCount;
    private int bombUsedCount;
    private int spellBonusCount;
    private int spellSeenCount;

    private boolean allowReplaySave;

    private int replayCursor;

    private final ReplayHeader[] slotHeaders = new ReplayHeader[ReplayRmsStore.SLOT_COUNT];
    private final byte[][] slotNames = new byte[ReplayRmsStore.SLOT_COUNT][];
    private final byte[] tmpFull = new byte[ReplayRmsStore.DATA_SIZE];
    private final byte[] tmpBoss = new byte[ReplayRmsStore.DATA_SIZE];

    private int[] photoRgb;
    private int[] scaledRgb;
    private int scaledW;
    private int scaledH;

    // DoJa  (game-end result): the OK key is accepted only after count > 10.
    private int finalCount;

    // Stage-to-stage preloading (no separate loading screen).
    private final StagePreloader stagePreloader = new StagePreloader();

    public StageClearResultPanel() {
        for (int i = 0; i < ReplayRmsStore.SLOT_COUNT; i++) {
            slotHeaders[i] = new ReplayHeader();
            slotNames[i] = new byte[ReplayRmsStore.NAME_SIZE];
        }
    }

    public void enter(Image photo, int hiScore, int score, int continuesUsed, int missCount, int bombUsedCount, int spellBonusCount, int spellSeenCount,
            boolean allowReplaySave, int chara) {
        // Stage clear panel: show photo/portrait and allow replay save depending on run state.
        this.mode = MODE_STAGE_CLEAR;
        this.scene = SCENE_MAIN;
        this.subsecCount = 0;
        this.slide = 0;
        this.leaving = false;
        this.chara = chara;

        this.photo = photo;
        this.hiScore = hiScore;
        this.score = score;
        this.continuesUsed = continuesUsed;
        this.missCount = missCount;
        this.bombUsedCount = bombUsedCount;
        this.spellBonusCount = spellBonusCount;
        this.spellSeenCount = spellSeenCount;

        this.allowReplaySave = allowReplaySave;
        this.replayCursor = 0;

        photoRgb = null;
        scaledRgb = null;
        scaledW = 0;
        scaledH = 0;
        initPhotoRgb();
    }

    public void enterFinal(int hiScore, int score, int continuesUsed, int missCount, int bombUsedCount, int spellBonusCount, int spellSeenCount) {
        // Final result screen (DoJa  jf()/if()): full-screen gradation and OK to leave.
        this.mode = MODE_FINAL;
        this.scene = SCENE_MAIN;
        this.subsecCount = 0;
        this.slide = 0;
        this.leaving = false;
        this.chara = 0;
        this.finalCount = 0;

        this.photo = null;
        this.hiScore = hiScore;
        this.score = score;
        this.continuesUsed = continuesUsed;
        this.missCount = missCount;
        this.bombUsedCount = bombUsedCount;
        this.spellBonusCount = spellBonusCount;
        this.spellSeenCount = spellSeenCount;

        this.allowReplaySave = false;
        this.replayCursor = 0;

        photoRgb = null;
        scaledRgb = null;
        scaledW = 0;
        scaledH = 0;
    }

    public Result update(int pressed) {
        if (mode == MODE_FINAL) {
            return updateFinal(pressed);
        }
        return updateStageClear(pressed);
    }

    // Update stage clear panel with preloading support.
    // nextStage: 0..5 for Stage1..Stage6, or -1 if unavailable.
    public Result updateWithStagePreload(int pressed, int nextStage, int chara, int type, ImageBank imgs, BulletSprites sprites) {
        if (mode == MODE_FINAL) {
            return updateFinal(pressed);
        }
        return updateStageClearWithPreload(pressed, nextStage, chara, type, imgs, sprites);
    }

    private Result updateFinal(int pressed) {
        // DoJa  if():
        // - After count > 10, OK triggers either replay save flow (if enabled) or return to title.
        if (scene == SCENE_MAIN) {
            if (finalCount > 10 && (pressed & GameCore.FIRE_PRESSED) != 0) {
                if (ReplaySaveService.hasPending()) {
                    scene = SCENE_FINAL_ASK_SAVE;
                    replayCursor = 0;
                    return null;
                }
                return new Result(Result.KIND_BACK_TO_TITLE);
            }
            finalCount++;
            return null;
        }

        if (scene == SCENE_FINAL_ASK_SAVE) {
            if ((pressed & (GameCore.UP_PRESSED | GameCore.DOWN_PRESSED)) != 0) {
                replayCursor ^= 1;
                return null;
            }
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                if (replayCursor == 0) {
                    return new Result(Result.KIND_BACK_TO_TITLE);
                }
                enterFinalReplaySlotScene();
            }
            return null;
        }

        if (scene == SCENE_FINAL_REPLAY_SLOT) {
            int max = ReplayRmsStore.SLOT_COUNT;
            if ((pressed & GameCore.DOWN_PRESSED) != 0) {
                replayCursor = (replayCursor + 1) % max;
            } else if ((pressed & GameCore.UP_PRESSED) != 0) {
                replayCursor = (replayCursor - 1 + max) % max;
            } else if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                scene = SCENE_FINAL_REPLAY_SAVE;
            } else if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
                return new Result(Result.KIND_BACK_TO_TITLE);
            }
        } else if (scene == SCENE_FINAL_REPLAY_OK || scene == SCENE_FINAL_REPLAY_FAIL) {
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                enterFinalReplaySlotScene();
            }
            if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
                enterFinalReplaySlotScene();
            }
        }

        if (scene == SCENE_FINAL_REPLAY_SAVE) {
            boolean ok = ReplaySaveService.savePendingToSlot(replayCursor);
            scene = ok ? SCENE_FINAL_REPLAY_OK : SCENE_FINAL_REPLAY_FAIL;
        }

        return null;
    }

    private Result updateStageClear(int pressed) {
        return updateStageClearWithPreload(pressed, -1, 0, 0, null, null);
    }

    private Result updateStageClearWithPreload(int pressed, int nextStage, int chara, int type, ImageBank imgs, BulletSprites sprites) {
        if (scene == SCENE_MAIN) {
            if (!leaving && slide >= SLIDE_MAX) {
                if ((pressed & (GameCore.FIRE_PRESSED | GameCore.GAME_A_PRESSED)) != 0) {
                    if (nextStage < 0) {
                        // Practice mode: no next stage, skip slide-out animation (DoJa: u()).
                        return new Result(Result.KIND_ADVANCE_STAGE);
                    }
                    if (imgs != null && sprites != null) {
                        // Synchronous preload: keep the panel frozen and allow a short stall.
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
                        stagePreloader.enter(nextStage, chara, type);
                        int guard = stagePreloader.getStepMax() + 4;
                        for (int i = 0; i < guard && !stagePreloader.isDone(); i++) {
                            stagePreloader.update(imgs, sprites);
                        }
                    }
                    leaving = true;
                } else if (allowReplaySave && (pressed & GameCore.KEY_POUND_PRESSED) != 0) {
                    enterReplaySlotScene();
                }
            }
        } else if (scene == SCENE_REPLAY_SLOT) {
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                scene = SCENE_REPLAY_SAVE;
            } else if ((pressed & GameCore.DOWN_PRESSED) != 0) {
                replayCursor = (replayCursor + 1) % ReplayRmsStore.SLOT_COUNT;
            } else if ((pressed & GameCore.UP_PRESSED) != 0) {
                replayCursor = (replayCursor - 1 + ReplayRmsStore.SLOT_COUNT) % ReplayRmsStore.SLOT_COUNT;
            } else if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
                scene = SCENE_MAIN;
            }
        } else if (scene == SCENE_REPLAY_OK || scene == SCENE_REPLAY_FAIL) {
            if ((pressed & (GameCore.FIRE_PRESSED | GameCore.GAME_A_PRESSED)) != 0) {
                enterReplaySlotScene();
            }
        }

        if (scene == SCENE_REPLAY_SAVE) {
            boolean ok = ReplaySaveService.savePendingToSlot(replayCursor);
            scene = ok ? SCENE_REPLAY_OK : SCENE_REPLAY_FAIL;
        }

        if (!leaving) {
            if (slide < SLIDE_MAX) {
                slide += 30;
                if (slide > SLIDE_MAX) {
                    slide = SLIDE_MAX;
                }
            }
        } else {
            if (slide > 0) {
                slide -= 60;
                if (slide < 0) {
                    slide = 0;
                }
            } else {
                return new Result(Result.KIND_ADVANCE_STAGE);
            }
        }

        subsecCount++;
        if (subsecCount > 10) {
            subsecCount = 10;
        }

        return null;
    }

    public void render(Graphics g, BulletSprites sprites) {
        if (g == null) {
            return;
        }

        if (mode == MODE_FINAL) {
            renderFinal(g, sprites);
        } else {
            renderStageClear(g, sprites);
        }
    }

    private void renderFinal(Graphics g, BulletSprites sprites) {
        // DoJa  jf(): full screen gradation + centered layout.
        renderReplayListBackgroundOrFallback(g, sprites);
        UiDraw.drawGradation(g, 0, 0, 240, 240, 0, 16736609, 32);
        UiDraw.drawStringPlain(g, FONT, UiText.get(TextId.STAGECLEAR_TITLE), 120, 22, 1, 0xFFFFFF);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_HISCORE), 30, 52, 0, 11513775, 4144959);
        UiDraw.drawString2(g, FONT, String.valueOf(hiScore), 210, 52, 2, 11513775, 4144959);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SCORE), 30, 72, 0, 11513775, 4144959);
        UiDraw.drawString2(g, FONT, String.valueOf(score), 210, 72, 2, 11513775, 4144959);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_CONTINUES), 30, 92, 0, 11513775, 4144959);
        UiDraw.drawString2(g, FONT, String.valueOf(continuesUsed), 210, 92, 2, 11513775, 4144959);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_MISS), 30, 112, 0, 11513775, 4144959);
        UiDraw.drawString2(g, FONT, String.valueOf(missCount), 210, 112, 2, 11513775, 4144959);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_BOMB), 30, 132, 0, 11513775, 4144959);
        UiDraw.drawString2(g, FONT, String.valueOf(bombUsedCount), 210, 132, 2, 11513775, 4144959);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SPELL_BONUS), 30, 152, 0, 11513775, 4144959);
        UiDraw.drawString2(g, FONT, spellBonusCount + "/" + spellSeenCount, 210, 152, 2, 11513775, 4144959);

        if (finalCount > 10) {
            UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_PUSH_ENTER), 120, 222, 1, 16756655, 11513775);
        }

        if (scene != SCENE_MAIN) {
            renderFinalReplayOverlay(g, sprites);
        }
    }

    private void enterFinalReplaySlotScene() {
        scene = SCENE_FINAL_REPLAY_SLOT;

        if (replayCursor < 0) {
            replayCursor = 0;
        }
        if (replayCursor >= ReplayRmsStore.SLOT_COUNT) {
            replayCursor = ReplayRmsStore.SLOT_COUNT - 1;
        }

        ReplaySaveUi.loadAllSlots(slotHeaders, slotNames, tmpFull, tmpBoss);
    }

    private void renderFinalReplayOverlay(Graphics g, BulletSprites sprites) {
        // Replay overlay for final result.
        // Keep the replay list visible even when showing save result popup.
        renderReplayListBackgroundOrFallback(g, sprites);

        if (scene == SCENE_FINAL_ASK_SAVE) {
            int baseY = 80;
            UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_ASK_SAVE_REPLAY), 120, baseY + 20, 1, 0xFFFFFF, 0x777777);
            if (replayCursor == 0) {
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SAVE_NO), 120, baseY + 40, 1, 0xFFFFFF, 0x444444);
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SAVE_YES), 120, baseY + 60, 1, 0x777777, 0x222222);
            } else {
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SAVE_NO), 120, baseY + 40, 1, 0x777777, 0x222222);
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SAVE_YES), 120, baseY + 60, 1, 0xFFFFFF, 0x444444);
            }
            // Removed bottom key hint text.
            return;
        }

        if (scene == SCENE_FINAL_REPLAY_SLOT || scene == SCENE_FINAL_REPLAY_OK || scene == SCENE_FINAL_REPLAY_FAIL) {
            ReplaySaveUi.renderSlotListFinal(g, FONT, replayCursor, slotHeaders, UiText.get(TextId.COMMON_SLOT_PREFIX));
            // Removed bottom key hint text.
        }

        if (scene == SCENE_FINAL_REPLAY_OK || scene == SCENE_FINAL_REPLAY_FAIL) {
            ReplaySaveUi.renderSaveResultBox(g, FONT, scene == SCENE_FINAL_REPLAY_OK, 104);
        }
    }

    private void renderStageClear(Graphics g, BulletSprites sprites) {
        renderBackgroundSweep(g);

        if (!leaving) {
            renderPhotoEmbed(g);
            renderPortrait(g, sprites);
        }

        if (!leaving && slide >= 120) {
            renderStats(g);
        }

        if (slide >= SLIDE_MAX) {
            UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_PUSH_ENTER), 97, 202, 1, 0xAAAAAA, 0x333333);
            if (allowReplaySave) {
                UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_REPLAY_SAVE_HINT), 97, 217, 1, 0xAAAAAA, 0x333333);
            }
        }

        if (scene != SCENE_MAIN) {
            renderReplayOverlay(g, sprites);
        }
    }

    // Draw character portrait overlay (cc.dat id 193/194/195) with fade-in.
    private void renderPortrait(Graphics g, BulletSprites sprites) {
        if (sprites == null) {
            return;
        }

        int id;
        switch (chara) {
            case 1:
                id = 194;
                break;
            case 2:
                id = 195;
                break;
            case 0:
            default:
                id = 193;
                break;
        }

        int x = 56 - subsecCount;
        int y = 206;
        int alpha = 255 - (128 * subsecCount) / 10;

        sprites.drawAlpha(g, id, x, y, alpha);
        sprites.drawAlpha(g, id, x, y, alpha);
    }

    // DoJa : a(Graphics, 0, 8, 194, 234) driven by slide (a.b).
    private void renderBackgroundSweep(Graphics g) {
        int oldClipX = g.getClipX();
        int oldClipY = g.getClipY();
        int oldClipW = g.getClipWidth();
        int oldClipH = g.getClipHeight();
        try {
            g.setClip(0, 0, 240, 240);
            g.clipRect(0, 8, 194, 226);
            renderBackgroundSweepImpl(g, 0, 8, 194, 234);
        } finally {
            g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
        }
    }

    private void renderBackgroundSweepImpl(Graphics g, int n, int n2, int n3, int n4) {
        for (int i = n2; i <= n4; i += 2) {
            int n5 = i;
            int n6 = n + slide - i;
            int n7 = i;
            if (n6 >= n && n7 >= n5) {
                if (n6 > n3) {
                    n6 = n3;
                }
                if (n7 <= n4) {
                    g.setColor(rgb((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF));
                    g.drawLine(n, n5, n6, n7);
                }
            }
        }
        for (int j = n2; j <= n4; j += 2) {
            int n8 = j + 1;
            int n9 = n + slide - j;
            int n10 = j + 1;
            if (n9 >= n && n10 >= n8) {
                if (n9 > n3) {
                    n9 = n3;
                }
                if (n10 <= n4) {
                    int v = n4 - j;
                    g.setColor(rgb((v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF));
                    g.drawLine(n3, n2 + n4 - n8, n3 - n9, n2 + n4 - n10);
                }
            }
        }
        for (int k = n; k <= n3; k += 2) {
            int n11 = k;
            int n12 = k;
            int n13 = n2 + slide - k;
            if (n12 >= n11 && n13 >= n2 && n12 <= n3) {
                if (n13 > n4) {
                    n13 = n4;
                }
                int v = n3 - k;
                g.setColor(rgb((v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF));
                g.drawLine(n + n3 - n11, n2, n + n3 - n12, n13);
            }
        }
        for (int l = n; l <= n3; l += 2) {
            int n14 = l + 1;
            int n15 = l + 1;
            int n16 = n2 + slide - l;
            if (n15 >= n14 && n16 >= n2 && n15 <= n3) {
                if (n16 > n4) {
                    n16 = n4;
                }
                g.setColor(rgb((l >> 16) & 0xFF, (l >> 8) & 0xFF, l & 0xFF));
                g.drawLine(n14, n2 + n4 - n2, n15, n2 + n4 - n16);
            }
        }
    }

    private static int rgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    private void initPhotoRgb() {
        if (photo == null) {
            return;
        }
        try {
            int pw = photo.getWidth();
            int ph = photo.getHeight();
            int[] rgb = new int[pw * ph];
            photo.getRGB(rgb, 0, pw, 0, 0, pw, ph);
            photoRgb = rgb;
        } catch (Throwable t) {
            photoRgb = null;
        }
    }

    private void ensureScaledBuffer(int w, int h) {
        if (scaledRgb != null && scaledW == w && scaledH == h) {
            return;
        }
        scaledW = w;
        scaledH = h;
        try {
            scaledRgb = new int[w * h];
        } catch (Throwable t) {
            scaledRgb = null;
            scaledW = 0;
            scaledH = 0;
        }
    }

    private void renderPhotoEmbed(Graphics g) {
        if (photoRgb == null) {
            return;
        }

        int d0 = 10 - subsecCount;
        int dstW = (PHOTO_SRC_W * (d0 + 10)) / 20;
        int dstH = (PHOTO_SRC_H * (d0 + 10)) / 20;
        int dstX = (PHOTO_SRC_W * subsecCount) / 20;
        int dstY = PHOTO_SRC_Y + (PHOTO_SRC_H * subsecCount) / 30;

        if (dstW <= 0 || dstH <= 0) {
            return;
        }
        ensureScaledBuffer(dstW, dstH);
        if (scaledRgb == null) {
            return;
        }

        int pw = 240;
        for (int y = 0; y < dstH; y++) {
            int sy = (y * PHOTO_SRC_H) / dstH;
            int srcRow = (PHOTO_SRC_Y + sy) * pw;
            int dstRow = y * dstW;
            for (int x = 0; x < dstW; x++) {
                int sx = (x * PHOTO_SRC_W) / dstW;
                scaledRgb[dstRow + x] = photoRgb[srcRow + (PHOTO_SRC_X + sx)];
            }
        }

        int oldClipX = g.getClipX();
        int oldClipY = g.getClipY();
        int oldClipW = g.getClipWidth();
        int oldClipH = g.getClipHeight();
        try {
            g.setClip(0, 0, 240, 240);
            g.clipRect(0, 8, 194, 226);
            try {
                g.drawRGB(scaledRgb, 0, dstW, dstX, dstY, dstW, dstH, true);
            } catch (Throwable t) {
            }
        } finally {
            g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
        }
    }

    private void renderStats(Graphics g) {
        UiDraw.drawStringPlain(g, FONT, UiText.get(TextId.STAGECLEAR_TITLE), 97, 22, 1, 0xFFFFFF);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_HISCORE), 20, 52, 0, 0xAAAAAA, 0x333333);
        UiDraw.drawString2(g, FONT, String.valueOf(hiScore), 174, 52, 2, 0xAAAAAA, 0x333333);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SCORE), 20, 72, 0, 0xAAAAAA, 0x333333);
        UiDraw.drawString2(g, FONT, String.valueOf(score), 174, 72, 2, 0xAAAAAA, 0x333333);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_CONTINUES), 20, 92, 0, 0xAAAAAA, 0x333333);
        UiDraw.drawString2(g, FONT, String.valueOf(continuesUsed), 174, 92, 2, 0xAAAAAA, 0x333333);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_MISS), 20, 112, 0, 0xAAAAAA, 0x333333);
        UiDraw.drawString2(g, FONT, String.valueOf(missCount), 174, 112, 2, 0xAAAAAA, 0x333333);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_BOMB), 20, 132, 0, 0xAAAAAA, 0x333333);
        UiDraw.drawString2(g, FONT, String.valueOf(bombUsedCount), 174, 132, 2, 0xAAAAAA, 0x333333);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.STAGECLEAR_SPELL_BONUS), 20, 152, 0, 0xAAAAAA, 0x333333);
        UiDraw.drawString2(g, FONT, spellBonusCount + "/" + spellSeenCount, 174, 152, 2, 0xAAAAAA, 0x333333);
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

        renderReplayListBackgroundOrFallback(g, sprites);
        ReplaySaveUi.renderSlotListCompact(g, FONT, replayCursor, slotHeaders, UiText.get(TextId.COMMON_SLOT_PREFIX));
        // Removed bottom key hint text.

        if (scene == SCENE_REPLAY_OK || scene == SCENE_REPLAY_FAIL) {
            ReplaySaveUi.renderSaveResultBox(g, FONT, scene == SCENE_REPLAY_OK, 104);
        }
    }

    static void renderTechnoGreenOverlayBackground(Graphics g) {
        if (g == null) {
            return;
        }
        g.setColor(0x000000);
        g.fillRect(0, 0, 240, 240);

        g.setColor(0x005500);
        g.drawRect(3, 3, 234, 234);
        g.setColor(0x00AA00);
        g.drawRect(6, 6, 228, 228);
        g.setColor(0x00FF00);
        g.drawRect(9, 9, 222, 222);
    }

    static void renderReplayListBackgroundOrFallback(Graphics g, BulletSprites sprites) {
        Image bg = null;
        if (sprites != null) {
            ImageBank imgs = sprites.getImages();
            if (imgs != null) {
                bg = imgs.get(18);
            }
        }
        if (bg != null) {
            UiDraw.drawRegion(g, bg, 0, 0, 0, 0, 240, 240);
            UiDraw.fillRectAlpha(g, 0, 0, 240, 240, 0x000000, 96);
        } else {
            renderTechnoGreenOverlayBackground(g);
        }

        g.setColor(0x005500);
        g.drawRect(3, 3, 234, 234);
        g.setColor(0x00AA00);
        g.drawRect(6, 6, 228, 228);
        g.setColor(0x00FF00);
        g.drawRect(9, 9, 222, 222);
    }

    // Expose leaving state so GameCanvas can pre-initialize the next stage and render it under the exit animation.
    public boolean isLeaving() {
        return leaving;
    }

}
