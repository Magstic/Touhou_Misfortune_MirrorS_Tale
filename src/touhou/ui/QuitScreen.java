package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class QuitScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_TITLE = 1;
        public static final int KIND_QUIT = 2;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private int cursor;

    public void enter() {
        cursor = 0;
    }

    public Result update(int pressed) {
        if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            return new Result(Result.KIND_BACK_TO_TITLE);
        }

        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursor++;
            if (cursor == 2) {
                cursor = 0;
            }
            return null;
        }

        if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            if (cursor != 0) {
                cursor--;
                return null;
            }
            cursor = 1;
            return null;
        }

        if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
            if (cursor == 0) {
                return new Result(Result.KIND_BACK_TO_TITLE);
            }
            return new Result(Result.KIND_QUIT);
        }

        return null;
    }

    public void render(Graphics g) {
        if (g == null) {
            return;
        }

        g.setColor(0x000000);
        g.fillRect(48, 80, 144, 80);
        g.setColor(0xFF0000);
        g.fillRect(49, 81, 142, 78);
        g.setColor(0x000000);
        g.fillRect(50, 82, 140, 76);

        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        // I18N
        UiDraw.drawString2(g, font, UiText.get(TextId.QUIT_TITLE), 120, 100, 1, 0xFF0000, 0x770000);

        String s0 = UiText.get(TextId.QUIT_OPTION_CONTINUE);
        String s1 = UiText.get(TextId.QUIT_OPTION_QUIT);

        UiDraw.drawString2(g, font, s0, 120, 120, 1, 0x777777, 0x333333);
        UiDraw.drawString2(g, font, s1, 120, 140, 1, 0x777777, 0x333333);

        if (cursor == 0) {
            UiDraw.drawString2(g, font, s0, 120, 120, 1, 0xFFFFFF, 0x777777);
        } else if (cursor == 1) {
            UiDraw.drawString2(g, font, s1, 120, 140, 1, 0xFFFFFF, 0x777777);
        }
    }
}
