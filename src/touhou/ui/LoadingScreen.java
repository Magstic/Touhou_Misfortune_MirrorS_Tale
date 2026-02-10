package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.BulletSprites;
import touhou.ImageBank;
import touhou.UnlockFlags;
import touhou.i18n.TextId;
import touhou.i18n.UiText;
import touhou.replay.ReplayBossSnapshotStore;
import touhou.replay.ReplayRmsStore;
import touhou.replay.ReplayStageSnapshotStore;

public final class LoadingScreen {
    private int step;
    private int stepMax;
    private boolean done;

    public void enter() {
        step = 0;
        done = false;

        // Keep this in sync with tickOneStep().
        stepMax = 9;
    }

    public boolean isDone() {
        return done;
    }

    public void update(int pressed, ImageBank imgs, BulletSprites sprites) {
        if (done) {
            return;
        }

        // Allow skipping on emulator/testing.
        if ((pressed & GameCanvas.FIRE_PRESSED) != 0 && UnlockFlags.isReplayRmsWarmed()) {
            done = true;
            return;
        }

        tickOneStep(imgs, sprites);
    }

    private void tickOneStep(ImageBank imgs, BulletSprites sprites) {
        if (imgs == null || sprites == null) {
            done = true;
            return;
        }

        switch (step) {
            case 0:
                // Title background/UI.
                imgs.get(0);
                imgs.get(1);
                imgs.get(2);
                break;
            case 1:
                // Start setup backgrounds.
                imgs.get(18);
                imgs.get(11);
                break;
            case 2:
                // Title menu sprites.
                preloadSpriteRange(imgs, sprites, 766, 783, 255);
                break;
            case 3:
                // Difficulty and character selection sprites.
                preloadSpriteRange(imgs, sprites, 467, 482, 255);
                break;
            case 4:
                // Character thumbnails are drawn with alpha=128 (player-origin draw).
                preloadSpriteRange(imgs, sprites, 477, 479, 128);
                break;
            case 5:
                // Start setup gradation background variants.
                UiDraw.precacheGradationAlpha(240, 240, 0x303030, 0xFFFFA0, 32, 128);
                UiDraw.precacheGradationAlpha(240, 240, 0x303030, 0xFFB000, 32, 128);
                UiDraw.precacheGradationAlpha(240, 240, 0x303030, 0xA10010, 32, 128);
                break;
            case 6:
                // Common UI overlay rectangles.
                UiDraw.precacheSolidAlpha(105, 26, 0x000000, 96);
                UiDraw.precacheSolidAlpha(52, 26, 0x000000, 96);
                break;
            case 7:
                // Full-screen fade (first-touch cost on devices).
                UiDraw.precacheSolidAlpha(240, 240, 0xFFFFFF, 128);
                break;
            case 8:
                // One-time RMS warmup for Replay menu on first launch.
                if (!UnlockFlags.isReplayRmsWarmed()) {
                    boolean ok = ReplayRmsStore.warmup();
                    ok = ok && ReplayBossSnapshotStore.warmup();
                    ok = ok && ReplayStageSnapshotStore.warmup();
                    if (ok) {
                        UnlockFlags.setReplayRmsWarmed(true);
                    }
                }
                break;
            default:
                done = true;
                return;
        }

        step++;
        if (step >= stepMax) {
            done = true;
        }
    }

    private static void preloadSpriteRange(ImageBank imgs, BulletSprites sprites, int idFrom, int idTo, int alpha) {
        if (imgs == null || sprites == null) {
            return;
        }
        for (int id = idFrom; id <= idTo; id++) {
            int imgIndex = sprites.getImageIndex(id);
            if (imgIndex < 0) {
                continue;
            }
            imgs.get(imgIndex);
            if (alpha != 255) {
                // Precreate the most common alpha region for this sprite.
                sprites.precacheAlphaRegion(id, alpha);
            }
        }
    }

    public void render(Graphics g) {
        if (g == null) {
            return;
        }

        g.setColor(0x000000);
        g.fillRect(0, 0, 240, 240);

        Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(f);
        g.setColor(0xFFFFFF);

        String title = UiText.get(TextId.LOADING_TITLE);
        int w = UiDraw.stringWidth(f, title);
        int x = (240 - w) >> 1;
        int y = 110;
        UiDraw.drawStringPlain(g, f, title, x, y, 0, 0xFFFFFF);

        int barW = 160;
        int barH = 10;
        int bx = (240 - barW) >> 1;
        int by = 125;

        g.setColor(0xFFFFFF);
        g.drawRect(bx - 1, by - 1, barW + 1, barH + 1);

        int p = step;
        if (p < 0) {
            p = 0;
        }
        if (p > stepMax) {
            p = stepMax;
        }
        int fillW = (stepMax <= 0) ? barW : (barW * p) / stepMax;
        if (fillW < 0) {
            fillW = 0;
        }
        if (fillW > barW) {
            fillW = barW;
        }

        g.setColor(0x0000FF);
        g.fillRect(bx, by, fillW, barH);
    }
}
