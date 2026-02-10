package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import touhou.CcTable;
import touhou.GameCore;
import touhou.ImageBank;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class StaffRollScreen {
    public static final class Result {
        public static final int KIND_DONE = 1;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static final Font FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    private static final int STAFF_MAX = 1125;

    private static final int[][] WIPE_EVENTS = new int[][] {
            // { start, orient(0=h,1=v), side(0=l,1=r), pos, backIndex, srcX, srcY }
            { 100, 0, 0, 140, 0, 50, 38 },
            { 225, 1, 0, 20, 0, 130, 58 },
            { 350, 0, 1, 20, 0, 50, 98 },
            { 475, 1, 1, 140, 1, 100, 108 },
            { 600, 0, 0, 10, 2, 100, 108 },
            { 725, 0, 1, 140, 3, 100, 108 },
            { 850, 0, 0, 20, 4, 100, 108 },
            { 975, 0, 1, 140, 5, 100, 108 }
    };

    private static final int[][] STAFF_TEXT = new int[][] {
            { 10, 120, 120, TextId.STAFF_TITLE },
            { 125, 120, 60, TextId.STAFF_PLANNING_TITLE, TextId.STAFF_PLANNING_1 },
            { 250, 180, 100, TextId.STAFF_PROGRAM_TITLE, TextId.STAFF_PROGRAM_1 },
            { 375, 120, 130, TextId.STAFF_GRAPHIC_TITLE, TextId.STAFF_GRAPHIC_1, TextId.STAFF_GRAPHIC_2 },
            { 500, 70, 100, TextId.STAFF_SOUND_TITLE, TextId.STAFF_SOUND_1 },
            { 625, 120, 120, TextId.STAFF_SPECIAL_THANKS_TITLE, TextId.STAFF_SPECIAL_THANKS_1, TextId.STAFF_SPECIAL_THANKS_2,
                    TextId.STAFF_SPECIAL_THANKS_3 },
            { 750, 120, 40, TextId.STAFF_TEST_PLAY_TITLE, TextId.STAFF_TEST_PLAY_1, TextId.STAFF_TEST_PLAY_2, TextId.STAFF_TEST_PLAY_3,
                    TextId.STAFF_TEST_PLAY_4 },
            { 875, 120, 130, TextId.STAFF_ORIGINAL_TITLE, TextId.STAFF_ORIGINAL_1, TextId.STAFF_ORIGINAL_URL },
            { 1000, 120, 40, TextId.STAFF_DEVELOPMENT_TITLE, TextId.STAFF_DEVELOPMENT_1, TextId.STAFF_DEVELOPMENT_URL_1,
                    TextId.STAFF_DEVELOPMENT_2, TextId.STAFF_DEVELOPMENT_URL_2 }
    };

    private static CcTable cc;
    private static boolean ccLoaded;

    private static void ensureCcLoaded() {
        if (ccLoaded) {
            return;
        }
        ccLoaded = true;
        try {
            try {
                cc = CcTable.loadFromResource("/res/cc.dat");
            } catch (Exception e) {
                cc = CcTable.loadFromResource("/cc.dat");
            }
        } catch (Throwable t) {
            cc = null;
        }
    }

    private ImageBank imgs;

    private Image[] back;

    private int count;

    public void enter(ImageBank imgs) {
        this.imgs = imgs;
        count = 0;
        buildBackImages();
    }

    public void leave() {
        imgs = null;
        back = null;
    }

    public Result update(int pressed) {
        return update(pressed, pressed);
    }

    public Result update(int keys, int pressed) {
        if (count < STAFF_MAX) {
            if ((keys & GameCore.FIRE_PRESSED) != 0) {
                count += 3;
            } else if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
                count = STAFF_MAX;
            }
            count++;
        } else {
            if ((pressed & GameCore.FIRE_PRESSED) != 0 || (pressed & GameCore.GAME_A_PRESSED) != 0) {
                return new Result(Result.KIND_DONE);
            }
        }
        return null;
    }

    public void render(Graphics g, ImageBank imgs) {
        if (g == null) {
            return;
        }
        if (imgs != null) {
            this.imgs = imgs;
        }

        Image bg = (this.imgs != null) ? this.imgs.get(18) : null;
        if (bg != null) {
            UiDraw.drawRegion(g, bg, 0, 0, 0, 0, 240, 240);
        } else {
            g.setColor(0x000000);
            g.fillRect(0, 0, 240, 240);
        }

        renderWipes(g);
        renderStaffText(g);

        if (count >= STAFF_MAX) {
            UiDraw.drawString2(g, FONT, "Thank You For Playing", 120, 120, 1, 0xFF0000, 0x7F0000);
            UiDraw.drawString2(g, FONT, "2008-2009 L-Garden", 120, 136, 1, 0xFF0000, 0x7F0000);
        }
    }

    private void renderWipes(Graphics g) {
        for (int i = 0; i < WIPE_EVENTS.length; i++) {
            int start = WIPE_EVENTS[i][0];
            int orient = WIPE_EVENTS[i][1];
            int side = WIPE_EVENTS[i][2];
            int pos = WIPE_EVENTS[i][3];
            int backIndex = WIPE_EVENTS[i][4];
            int srcCx = WIPE_EVENTS[i][5];
            int srcCy = WIPE_EVENTS[i][6];

            if (!(count > start && count < start + 100)) {
                continue;
            }

            int n = count - start;

            if (orient == 0) {
                renderWipeHorizontal(g, n, side, pos, backIndex, srcCx, srcCy);
            } else {
                renderWipeVertical(g, n, side, pos, backIndex, srcCx, srcCy);
            }
        }
    }

    private void renderWipeHorizontal(Graphics g, int n, int side, int y, int backIndex, int srcCx, int srcCy) {
        final int x = 0;
        final int w = 240;
        final int h = 80;

        g.setColor(0x7F0000);

        if (n < 10) {
            for (int j = 0; j < h; j++) {
                if (((10 - j) & n) == 0) {
                    g.drawLine(x, y + j, x + w, y + j);
                }
            }
            return;
        }

        if (n > 90) {
            int n6 = n - 90;
            for (int k = 0; k < h; k++) {
                int n7 = (h - k) << n6;
                if (n7 < 240) {
                    g.drawLine(x + n7, y + k, x + w, y + k);
                }
            }
            return;
        }

        UiDraw.fillRectAlpha(g, x, y, w, h, 0x7F0000, 128);

        int dstX;
        if (side == 0) {
            dstX = x + (n >> 1);
        } else {
            dstX = x + w - (n >> 1) - 100;
        }

        drawBackWindow(g, backIndex, dstX, y + 10, srcCx - 50, srcCy - 30, 100, 60);
        UiDraw.fillRectAlpha(g, dstX, y + 10, 100, 60, 0x000000, 128);
    }

    private void renderWipeVertical(Graphics g, int n, int side, int x, int backIndex, int srcCx, int srcCy) {
        final int y = 0;
        final int w = 80;
        final int h = 240;

        g.setColor(0x7F0000);

        if (n < 10) {
            for (int j = 0; j < w; j++) {
                if (((10 - j) & n) == 0) {
                    g.drawLine(x + j, y, x + j, y + h);
                }
            }
            return;
        }

        if (n > 90) {
            int n14 = n - 90;
            for (int k = 0; k < w; k++) {
                int n16 = (w - k) << n14;
                if (n16 < 240) {
                    g.drawLine(x + k, y + n16, x + k, y + h);
                }
            }
            return;
        }

        UiDraw.fillRectAlpha(g, x, y, w, h, 0x7F0000, 128);

        int dstY;
        if (side == 0) {
            dstY = y + (n >> 1);
        } else {
            dstY = y + h - (n >> 1) - 100;
        }

        drawBackWindow(g, backIndex, x + 10, dstY, srcCx - 30, srcCy - 50, 60, 100);
        UiDraw.fillRectAlpha(g, x + 10, dstY, 60, 100, 0x000000, 128);
    }

    private void drawBackWindow(Graphics g, int backIndex, int dstX, int dstY, int srcX, int srcY, int w, int h) {
        if (back == null || backIndex < 0 || backIndex >= back.length) {
            return;
        }
        Image img = back[backIndex];
        if (img == null) {
            return;
        }
        UiDraw.drawRegion(g, img, dstX, dstY, srcX, srcY, w, h);
    }

    private void renderStaffText(Graphics g) {
        for (int i = 0; i < STAFF_TEXT.length; i++) {
            int start = STAFF_TEXT[i][0];
            if (!(count > start && count < start + 100)) {
                continue;
            }

            int n = count - start;
            int x = STAFF_TEXT[i][1];
            int y = STAFF_TEXT[i][2];

            int a = 255;
            if (n < 10) {
                a -= (10 - n) << 4;
            } else if (n > 90) {
                a -= (n - 90) << 4;
            }
            if (a < 0) {
                a = 0;
            }
            if (a > 255) {
                a = 255;
            }

            int main = scaleRgb(0xFFFFFF, a);
            int shadow = scaleRgb(0x777777, a);

            for (int j = 3; j < STAFF_TEXT[i].length; j++) {
                String s = UiText.get(STAFF_TEXT[i][j]);
                UiDraw.drawString2(g, FONT, s, x, y, 1, main, shadow);
                y += 18;
            }
        }
    }

    private void buildBackImages() {
        back = null;
        try {
            back = new Image[6];
        } catch (Throwable t) {
            return;
        }

        ensureCcLoaded();

        buildBack0();
        buildBack1();
        buildBack2();
        buildBack3();
        buildBack4();
        buildBack5();
    }

    private void buildBack0() {
        Image img = createBackImage();
        if (img == null) {
            return;
        }
        Graphics g = img.getGraphics();

        // DoJa builds _m_backImage[] by rendering the scene and then drawing cc icons.
        // Do not draw sprite-sheet images full-frame here, or the wipe windows will expose multiple frames.
        drawFull(g, 25);

        drawCcIcon(g, 169, 50, 38);
        drawCcIcon(g, 174, 130, 58);
        drawCcIcon(g, 181, 50, 98);

        back[0] = img;
    }

    private void buildBack1() {
        Image img = createBackImage();
        if (img == null) {
            return;
        }
        Graphics g = img.getGraphics();

        // Base background (avoid drawing sprite sheets full-frame).
        drawFull(g, 25);

        drawCcIcon(g, 205, 100, 108);

        back[1] = img;
    }

    private void buildBack2() {
        Image img = createBackImage();
        if (img == null) {
            return;
        }
        Graphics g = img.getGraphics();

        // Base background (avoid drawing sprite sheets full-frame).
        drawFull(g, 25);

        drawCcIcon(g, 227, 100, 108);

        back[2] = img;
    }

    private void buildBack3() {
        Image img = createBackImage();
        if (img == null) {
            return;
        }
        Graphics g = img.getGraphics();

        // DoJa uses 59 as the primary scene background here.
        drawFull(g, 59);

        drawCcIcon(g, 365, 100, 108);

        back[3] = img;
    }

    private void buildBack4() {
        Image img = createBackImage();
        if (img == null) {
            return;
        }
        Graphics g = img.getGraphics();

        // DoJa uses 62/63 as the base scene background here.
        drawFull(g, 62);
        drawFull(g, 63);

        drawCcIcon(g, 516, 100, 108);

        back[4] = img;
    }

    private void buildBack5() {
        Image img = createBackImage();
        if (img == null) {
            return;
        }
        Graphics g = img.getGraphics();

        // DoJa uses 70 as the base scene background here.
        drawFull(g, 70);

        drawCcIcon(g, 711, 100, 108);

        back[5] = img;
    }

    private Image createBackImage() {
        try {
            return Image.createImage(240, 240);
        } catch (Throwable t) {
            return null;
        }
    }

    private void drawFull(Graphics g, int imgIndex) {
        if (imgs == null) {
            return;
        }
        Image i = imgs.get(imgIndex);
        if (i == null) {
            return;
        }
        try {
            g.drawImage(i, 0, 0, Graphics.TOP | Graphics.LEFT);
        } catch (Throwable t) {
        }
    }

    // Backdrop builder helper: use current screen ImageBank.
    private void drawCcIcon(Graphics g, int ccIndex, int x, int y) {
        drawCcIcon(g, this.imgs, ccIndex, x, y);
    }

    private static void drawCcIcon(Graphics g, ImageBank imgs, int ccIndex, int x, int y) {
        if (g == null || imgs == null) {
            return;
        }
        ensureCcLoaded();
        if (cc == null) {
            return;
        }
        if (!cc.hasSpriteMeta(ccIndex)) {
            return;
        }

        int imgIndex = cc.getImgIndex(ccIndex);
        int srcX = cc.getSrcX(ccIndex);
        int srcY = cc.getSrcY(ccIndex);
        int w = cc.getW(ccIndex);
        int h = cc.getH(ccIndex);
        int ax = cc.getAx(ccIndex);
        int ay = cc.getAy(ccIndex);

        Image img = imgs.get(imgIndex);
        if (img == null) {
            return;
        }

        // Match original behavior: some sprite sheets (imgIndex==6) may require a 2x scale fallback.
        if (imgIndex == 6) {
            if (drawRegionSafe(g, img, x, y, srcX, srcY, w, h, ax, ay, 1)) {
                return;
            }
            drawRegionSafe(g, img, x, y, srcX, srcY, w, h, ax, ay, 2);
            return;
        }
        drawRegionSafe(g, img, x, y, srcX, srcY, w, h, ax, ay, 1);
    }

    private static boolean drawRegionSafe(Graphics g, Image img, int x, int y, int srcX, int srcY, int w, int h, int ax, int ay, int scale) {
        if (img == null) {
            return false;
        }
        if (scale != 1) {
            srcX *= scale;
            srcY *= scale;
            w *= scale;
            h *= scale;
            ax *= scale;
            ay *= scale;
        }

        int imgW = img.getWidth();
        int imgH = img.getHeight();
        if (w <= 0 || h <= 0) {
            return false;
        }

        int dx = x - ax;
        int dy = y - ay;

        if (srcX < 0) {
            int cut = -srcX;
            srcX = 0;
            dx += cut;
            w -= cut;
        }
        if (srcY < 0) {
            int cut = -srcY;
            srcY = 0;
            dy += cut;
            h -= cut;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }

        if (srcX >= imgW || srcY >= imgH) {
            return false;
        }
        if (srcX + w > imgW) {
            w = imgW - srcX;
        }
        if (srcY + h > imgH) {
            h = imgH - srcY;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }

        UiDraw.drawRegion(g, img, dx, dy, srcX, srcY, w, h);
        return true;
    }

    private static int scaleRgb(int rgb, int alpha) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        r = (r * alpha) / 255;
        g = (g * alpha) / 255;
        b = (b * alpha) / 255;

        return (r << 16) | (g << 8) | b;
    }
}
