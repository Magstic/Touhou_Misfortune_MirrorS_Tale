package touhou.ui;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.ImageBank;
import touhou.i18n.I18n;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class StoryScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_TITLE = 1;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static String[] loadStoryLines() {
        String text = I18n.readUtf8TextResource(I18n.path("story.txt"));
        if (text == null) {
            return null;
        }
        return I18n.splitLinesSimple(text);
    }

    private static final int BG_IMG_INDEX = 54;

    private static final int W = 240;
    private static final int H = 240;

    private static final int TEXT_X = 12;
    private static final int TEXT_Y = 32;
    private static final int TEXT_W = 216;
    private static final int TEXT_H = 196;

    private static final int FLAKE_COUNT = 18;

    private final int[] flakeXfp = new int[FLAKE_COUNT];
    private final int[] flakeYfp = new int[FLAKE_COUNT];
    private final int[] flakeVYfp = new int[FLAKE_COUNT];
    private final int[] flakeVXfp = new int[FLAKE_COUNT];
    private final byte[] flakeSize = new byte[FLAKE_COUNT];
    private final byte[] flakeAlphaLevel = new byte[FLAKE_COUNT];
    private final int[] flakeSeed = new int[FLAKE_COUNT];
    private int flakeTick;

    private Font font;
    private int lineH;

    private Vector wrapped;
    private int maxTopLine;

    // Smooth scroll state (line fixed-point, 8 bits fraction).
    private int scrollPosFp;
    private int scrollVelFp;
    private int scrollTargetFp;
    private boolean scrollTargetActive;

    // Hold-to-scroll (UP/DOWN) state.
    private int holdUpTicks;
    private int holdDownTicks;

    // Edge detection for non-repeat actions.
    private int prevPressed;

    public void enter() {
        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        lineH = UiDraw.fontHeight(font) + 1;

        wrapped = new Vector();
        String[] lines = loadStoryLines();
        if (lines != null) {
            for (int i = 0; i < lines.length; i++) {
                wrapLine(wrapped, font, lines[i], TEXT_W);
            }
        }

        maxTopLine = 0;
        int visible = TEXT_H / lineH;
        if (visible < 1) {
            visible = 1;
        }
        int total = wrapped.size();
        int max = total - visible;
        if (max > 0) {
            maxTopLine = max;
        }

        scrollPosFp = 0;
        scrollVelFp = 0;
        scrollTargetFp = 0;
        scrollTargetActive = false;
        holdUpTicks = 0;
        holdDownTicks = 0;
        prevPressed = 0;

        flakeTick = 0;
        for (int i = 0; i < FLAKE_COUNT; i++) {
            flakeSeed[i] = 0x1F123BB5 ^ (i * 0x9E3779B9);
            initFlake(i, false);
        }
    }

    public Result update(int pressed) {
        flakeTick++;

        if ((pressed & (GameCanvas.GAME_A_PRESSED | GameCanvas.GAME_B_PRESSED)) != 0) {
            return new Result(Result.KIND_BACK_TO_TITLE);
        }

        boolean up = (pressed & GameCanvas.UP_PRESSED) != 0;
        boolean down = (pressed & GameCanvas.DOWN_PRESSED) != 0;
        boolean left = (pressed & GameCanvas.LEFT_PRESSED) != 0;
        boolean right = (pressed & GameCanvas.RIGHT_PRESSED) != 0;

        boolean edgeUp = up && ((prevPressed & GameCanvas.UP_PRESSED) == 0);
        boolean edgeDown = down && ((prevPressed & GameCanvas.DOWN_PRESSED) == 0);
        boolean edgeLeft = left && ((prevPressed & GameCanvas.LEFT_PRESSED) == 0);
        boolean edgeRight = right && ((prevPressed & GameCanvas.RIGHT_PRESSED) == 0);

        if (edgeUp) {
            requestScrollByLines(-1);
        } else if (edgeDown) {
            requestScrollByLines(1);
        }
        if (edgeLeft) {
            requestScrollByLines(-6);
        } else if (edgeRight) {
            requestScrollByLines(6);
        }

        int targetVel = 0;
        boolean holdActive = false;
        if (up && !down) {
            holdUpTicks++;
            holdDownTicks = 0;
            targetVel = -calcHoldScrollVelFp(holdUpTicks);
            holdActive = true;
        } else if (down && !up) {
            holdDownTicks++;
            holdUpTicks = 0;
            targetVel = calcHoldScrollVelFp(holdDownTicks);
            holdActive = true;
        } else {
            holdUpTicks = 0;
            holdDownTicks = 0;
        }

        if (holdActive) {
            scrollTargetActive = false;
            scrollVelFp = targetVel;
        } else if (scrollTargetActive) {
            int err = scrollTargetFp - scrollPosFp;
            scrollVelFp += err >> 3;
            scrollVelFp -= scrollVelFp >> 1;

            if (err < 32 && err > -32 && scrollVelFp < 16 && scrollVelFp > -16) {
                scrollPosFp = scrollTargetFp;
                scrollVelFp = 0;
                scrollTargetActive = false;
            }
        } else {
            scrollVelFp -= scrollVelFp >> 1;
        }

        scrollPosFp += scrollVelFp;
        int minFp = 0;
        int maxFp = maxTopLine << 8;
        if (scrollPosFp < minFp) {
            scrollPosFp = minFp;
            scrollVelFp = 0;
            scrollTargetActive = false;
        } else if (scrollPosFp > maxFp) {
            scrollPosFp = maxFp;
            scrollVelFp = 0;
            scrollTargetActive = false;
        }

        prevPressed = pressed;

        return null;
    }

    private void requestScrollByLines(int deltaLines) {
        if (maxTopLine <= 0) {
            return;
        }

        int maxFp = maxTopLine << 8;
        int start = scrollTargetActive ? scrollTargetFp : scrollPosFp;
        int dst = start + (deltaLines << 8);
        if (dst < 0) {
            dst = 0;
        } else if (dst > maxFp) {
            dst = maxFp;
        }

        scrollTargetFp = dst;
        scrollTargetActive = true;
    }

    private static int calcHoldScrollVelFp(int holdTicks) {
        if (holdTicks <= 0) {
            return 0;
        }
        if (holdTicks < 4) {
            return 256;
        }
        if (holdTicks < 10) {
            return 384;
        }
        if (holdTicks < 20) {
            return 640;
        }
        if (holdTicks < 35) {
            return 1024;
        }
        if (holdTicks < 55) {
            return 1536;
        }
        return 2048;
    }

    public void render(Graphics g, ImageBank imgs) {
        if (g == null) {
            return;
        }

        Image bg = (imgs != null) ? imgs.get(BG_IMG_INDEX) : null;
        if (bg != null) {
            UiDraw.drawRegion(g, bg, 0, 0, 0, 0, W, H);
        } else {
            g.setColor(0x000000);
            g.fillRect(0, 0, W, H);
        }

        UiDraw.drawGradationAlpha(g, 0, 0, W, H, 0x203060, 0x000000, 32, 120);

        UiDraw.fillRectAlpha(g, 8, 22, 224, 210, 0x000000, 120);
        g.setColor(0xAFCFFF);
        g.drawRect(8, 22, 224, 210);

        UiDraw.drawString2(g, font, UiText.get(TextId.STORY_TITLE), 120, 18, 1, 0xFFFFFF, 0x334455);

        drawSnow(g);
        drawText(g);
        drawScrollBar(g);
    }

    private void drawText(Graphics g) {
        if (wrapped == null || font == null) {
            return;
        }

        int oldClipX = g.getClipX();
        int oldClipY = g.getClipY();
        int oldClipW = g.getClipWidth();
        int oldClipH = g.getClipHeight();

        try {
            g.setClip(TEXT_X, TEXT_Y, TEXT_W, TEXT_H);
            g.setFont(font);

            int idx = scrollPosFp >> 8;
            int sub = scrollPosFp & 0xFF;
            int y = (TEXT_Y + lineH) - ((sub * lineH) >> 8);
            int maxLines = (TEXT_H / lineH) + 3;

            for (int i = 0; i < maxLines; i++) {
                if (idx >= wrapped.size()) {
                    break;
                }
                String s = (String) wrapped.elementAt(idx);
                if (s == null) {
                    s = "";
                }
                UiDraw.drawStringPlain(g, font, s, TEXT_X + 1, y + 1, 0, 0x001820);
                UiDraw.drawStringPlain(g, font, s, TEXT_X, y, 0, 0xFFFFFF);

                y += lineH;
                idx++;
            }
        } catch (Throwable t) {
        } finally {
            try {
                g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
            } catch (Throwable ignore) {
            }
        }
    }

    private void drawScrollBar(Graphics g) {
        if (wrapped == null || font == null) {
            return;
        }

        int total = wrapped.size();
        if (total <= 0) {
            return;
        }

        int visible = TEXT_H / lineH;
        if (visible < 1) {
            visible = 1;
        }
        if (total <= visible) {
            return;
        }

        int barX = TEXT_X + TEXT_W + 2;
        int barY = TEXT_Y;
        int barW = 4;
        int barH = TEXT_H;

        UiDraw.fillRectAlpha(g, barX, barY, barW, barH, 0x000000, 100);

        int knobH = (barH * visible) / total;
        if (knobH < 8) {
            knobH = 8;
        }
        int denom = maxTopLine << 8;
        int knobY;
        if (denom <= 0) {
            knobY = barY;
        } else {
            long num = (long) (barH - knobH) * (long) scrollPosFp;
            knobY = barY + (int) (num / denom);
        }
        UiDraw.fillRectAlpha(g, barX, knobY, barW, knobH, 0xAFCFFF, 180);
    }

    private void drawSnow(Graphics g) {
        for (int i = 0; i < FLAKE_COUNT; i++) {
            int seed = flakeSeed[i];

            if ((flakeTick & 15) == (i & 15)) {
                seed = nextRand(seed);
                int n = (seed >> 28) & 7;
                int dvx = (n - 3) << 3;

                int vx = flakeVXfp[i];
                vx += dvx;
                vx -= (vx >> 5);
                if (vx < -128) {
                    vx = -128;
                } else if (vx > 128) {
                    vx = 128;
                }
                flakeVXfp[i] = vx;
            }

            int xfp = flakeXfp[i] + flakeVXfp[i];
            int yfp = flakeYfp[i] + flakeVYfp[i];

            if (xfp < (-4 << 8)) {
                xfp = (W + 3) << 8;
            } else if (xfp > ((W + 3) << 8)) {
                xfp = (-3) << 8;
            }

            if (yfp > ((H + 6) << 8)) {
                flakeSeed[i] = nextRand(seed);
                initFlake(i, true);
                continue;
            }

            flakeSeed[i] = seed;
            flakeXfp[i] = xfp;
            flakeYfp[i] = yfp;

            int x = xfp >> 8;
            int y = yfp >> 8;

            int size = flakeSize[i] & 0xFF;
            int alpha = 96 + (flakeAlphaLevel[i] & 3) * 32;
            if (size <= 1) {
                UiDraw.fillRectAlpha(g, x, y, 1, 1, 0xFFFFFF, alpha);
            } else if (size == 2) {
                UiDraw.fillRectAlpha(g, x, y, 2, 2, 0xFFFFFF, alpha);
            } else {
                UiDraw.fillRectAlpha(g, x, y, 3, 1, 0xFFFFFF, alpha);
                UiDraw.fillRectAlpha(g, x + 1, y - 1, 1, 3, 0xFFFFFF, alpha);
            }
        }
    }

    private void initFlake(int i, boolean respawn) {
        int seed = flakeSeed[i];
        seed = nextRand(seed);
        int x = (seed >>> 16) % W;

        seed = nextRand(seed);
        int y;
        if (respawn) {
            y = -((seed >>> 16) % 32) - 2;
        } else {
            y = (seed >>> 16) % H;
        }

        seed = nextRand(seed);
        int vy = 40 + ((seed >>> 16) % 96);

        seed = nextRand(seed);
        int vx = ((seed >>> 16) % 81) - 40;

        seed = nextRand(seed);
        int size = 1 + ((seed >>> 16) % 3);

        seed = nextRand(seed);
        int a = (seed >>> 16) & 3;

        flakeXfp[i] = x << 8;
        flakeYfp[i] = y << 8;
        flakeVYfp[i] = vy;
        flakeVXfp[i] = vx;
        flakeSize[i] = (byte) size;
        flakeAlphaLevel[i] = (byte) a;
        flakeSeed[i] = seed;
    }

    private static int nextRand(int s) {
        return s * 1103515245 + 12345;
    }

    private static void wrapLine(Vector out, Font font, String s, int maxWidth) {
        if (out == null || font == null) {
            return;
        }
        if (s == null) {
            out.addElement("");
            return;
        }
        if (s.length() == 0) {
            out.addElement("");
            return;
        }

        int lineStart = 0;
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int cw = UiDraw.charWidth(font, c);
            if (w + cw > maxWidth && i > lineStart) {
                out.addElement(s.substring(lineStart, i));
                lineStart = i;
                w = 0;
            }
            w += cw;
        }
        if (lineStart < s.length()) {
            out.addElement(s.substring(lineStart));
        }
    }
}
