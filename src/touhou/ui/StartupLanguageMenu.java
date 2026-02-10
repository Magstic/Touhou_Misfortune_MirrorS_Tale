package touhou.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.GameOptions;
import touhou.ImageBank;
import touhou.i18n.I18nBootstrap;

public final class StartupLanguageMenu {
    // Menu image: 240x240. Left half is unselected (gray), right half is selected (colored).
    private static final int ITEM_W = 120;
    private static final int ITEM_H = 13;
    private static final int ITEM_STRIDE = 15;

    // Match TitleScreen menu area (y=104, 9 rows * 12px) and center our smaller list within it.
    private static final int TITLE_MENU_Y0 = 104;
    private static final int TITLE_MENU_STEP = 12;
    private static final int TITLE_MENU_ROWS = 9;

    // Stair layout: make vertical spacing a bit larger than the baked 13+2=15px stride.
    private static final int ITEM_STEP_Y = 18;

    private static final int TINT_RGB = 0x0A1026;
    private static final int TINT_ALPHA = 26;

    private static final int MENU_SRC_X_OFF = 0;
    private static final int MENU_SRC_X_ON = 120;

    private int cursor;
    private int count;

    private Image menu;

    public StartupLanguageMenu() {
    }

    public void enter() {
        count = 0;

        int[] opt = GameOptions.load();
        if (opt == null) {
            opt = GameOptions.defaults();
        }
        if (opt.length > GameOptions.IDX_LANGUAGE) {
            cursor = opt[GameOptions.IDX_LANGUAGE];
        }
        if (cursor < 0 || cursor >= 3) {
            cursor = 0;
        }
    }

    public Result update(int pressed) {
        if (count < 8) {
            count++;
        }

        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursor = (cursor + 1) % 3;
        } else if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            cursor = (cursor + 2) % 3;
        } else if ((pressed & (GameCanvas.FIRE_PRESSED | GameCanvas.GAME_A_PRESSED | GameCanvas.GAME_B_PRESSED)) != 0) {
            commit();
            return new Result(Result.KIND_DONE);
        }

        return null;
    }

    private void commit() {
        int[] opt = GameOptions.load();
        if (opt == null) {
            opt = GameOptions.defaults();
        }
        if (opt.length > GameOptions.IDX_LANGUAGE) {
            opt[GameOptions.IDX_LANGUAGE] = cursor;
        }
        GameOptions.save(opt);

        I18nBootstrap.initFromOptions(opt);
    }

    public void render(Graphics g, ImageBank imgs) {
        if (g == null || imgs == null) {
            return;
        }

        UiDraw.drawRegion(g, imgs.get(0), 0, 0, 0, 0, 240, 240);

        int wob = 4 - (count >> 1);
        Image ui = imgs.get(1);
        if (ui != null) {
            UiDraw.drawRegion(g, ui, 30 - wob + 0, 33 - wob, 0, 3, 36, 36);
            UiDraw.drawRegion(g, ui, 30 + wob + 36, 33 + wob, 36, 3, 36, 36);
            UiDraw.drawRegion(g, ui, 30 - wob + 72, 33 - wob, 72, 3, 36, 36);
            UiDraw.drawRegion(g, ui, 30 + wob + 108, 33 + wob, 108, 3, 36, 36);
            UiDraw.drawRegion(g, ui, 30 - wob + 144, 33 - wob, 144, 3, 36, 36);

            UiDraw.drawRegion(g, ui, 30 + wob + 0, 33 - wob, 0, 40, 36, 36);
            UiDraw.drawRegion(g, ui, 30 - wob + 36, 33 + wob, 36, 40, 36, 36);
            UiDraw.drawRegion(g, ui, 30 + wob + 72, 33 - wob, 72, 40, 36, 36);
            UiDraw.drawRegion(g, ui, 30 - wob + 108, 33 + wob, 108, 40, 36, 36);
            UiDraw.drawRegion(g, ui, 30 + wob + 144, 33 - wob, 144, 40, 36, 36);

            if (count == 8) {
                UiDraw.drawRegion(g, ui, 59, 73, 2, 79, 121, 8);
            }
        }

        Image charImg = imgs.get(2);
        if (charImg != null) {
            g.drawImage(charImg, 5, 93, Graphics.TOP | Graphics.LEFT);
        }

        UiDraw.fillRectAlpha(g, 0, 0, 240, 240, TINT_RGB, TINT_ALPHA);

        ensureMenuLoaded();
        if (menu == null) {
            return;
        }

        int menuAreaH = TITLE_MENU_STEP * TITLE_MENU_ROWS;
        int listH = ITEM_H + (ITEM_STEP_Y * 2);
        int y0 = TITLE_MENU_Y0 + ((menuAreaH - listH) >> 1);

        for (int i = 0; i < 3; i++) {
            int x;
            if ((i & 1) == 0) {
                x = 143 - i * 3 + wob + 8;
            } else {
                x = 143 - i * 3 - wob + 8;
            }
            int y = y0 + i * ITEM_STEP_Y;

            int srcX = (i == cursor) ? MENU_SRC_X_ON : MENU_SRC_X_OFF;
            int srcY = i * ITEM_STRIDE;

            UiDraw.drawRegion(g, menu, x, y, srcX, srcY, ITEM_W, ITEM_H);
        }
    }

    private void ensureMenuLoaded() {
        if (menu != null) {
            return;
        }
        try {
            // Keep it as a direct resource load to avoid expanding ImageBank capacity.
            menu = Image.createImage("/res/sp/095.gif");
        } catch (Throwable t) {
            menu = null;
        }
    }

    public static final class Result {
        public static final int KIND_DONE = 0;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }
}
