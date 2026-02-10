package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.CcTable;
import touhou.ImageBank;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class HelpScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_TITLE = 1;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private int page;
    private int pageMax;

    private static CcTable cc;
    private static boolean ccLoaded;

    public void enter() {
        page = 0;
        pageMax = 15;
    }

    public Result update(int pressed) {
        if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            return new Result(Result.KIND_BACK_TO_TITLE);
        }

        if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
            if (page > 0) {
                page--;
            }
        } else if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
            if (page < pageMax - 1) {
                page++;
            }
        }

        return null;
    }

    public void render(Graphics g, ImageBank imgs) {
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

        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        // Title is i18n. Page number is dynamic.
        UiDraw.drawString2(g, font, UiText.get(TextId.HELP_TITLE_PREFIX) + (page + 1) + "/" + pageMax + UiText.get(TextId.HELP_PAGE_SUFFIX), 120, 25, 1, 0xFFFFFF, 0x777777);

        renderPage(g, font, imgs, page);

        Image ui = (imgs != null) ? imgs.get(1) : null;
        if (ui != null) {
            if (page != 0) {
                UiDraw.drawRegion(g, ui, 20, 25, 0, 212, 8, 12);
            }
            if (page != pageMax - 1) {
                UiDraw.drawRegion(g, ui, 214, 25, 8, 212, 8, 12);
            }
        }
    }

    private static void drawPlain(Graphics g, Font font, String s, int x, int y, int rgb) {
        if (s == null) {
            return;
        }
        UiDraw.drawStringPlain(g, font, s, x, y, 0, rgb);
    }

    private static void drawPlainId(Graphics g, Font font, int textId, int x, int y, int rgb) {
        drawPlain(g, font, UiText.get(textId), x, y, rgb);
    }

    private static int pageHeaderId(int page) {
        // Clamp to a valid help page to avoid depending on a special "default" text.
        if (page < 0) {
            page = 0;
        } else if (page > 14) {
            page = 14;
        }
        return TextId.HELP_PAGE_HEADER_0 + page;
    }

    private static void renderPage(Graphics g, Font font, ImageBank imgs, int page) {
        UiDraw.drawString2(g, font, UiText.get(pageHeaderId(page)), 120, 44, 1, 0xFF0000, 0x770000);
        switch (page) {
            case 0:
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_LABEL_START, 20, 58, 0x55FF55);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_LABEL_EXTRA_START, 20, 86, 0x55FF55);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_LABEL_SPELL_PRACTICE, 20, 114, 0x55FF55);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_LABEL_PRACTICE_START, 20, 142, 0x55FF55);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_LABEL_REPLAY, 20, 170, 0x55FF55);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_LABEL_RESULT, 20, 198, 0x55FF55);

                drawPlainId(g, font, TextId.HELP_CONTENT_P0_DESC_START, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_DESC_EXTRA_START, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_DESC_SPELL_PRACTICE, 20, 128, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_DESC_PRACTICE_START, 20, 156, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_DESC_REPLAY, 20, 184, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P0_DESC_RESULT, 20, 212, 0xFFFFFF);
                return;

            case 1:
                drawPlainId(g, font, TextId.HELP_CONTENT_P1_LABEL_MUSIC_ROOM, 20, 58, 0x55FF55);
                drawPlainId(g, font, TextId.HELP_CONTENT_P1_LABEL_OPTION, 20, 86, 0x55FF55);
                drawPlainId(g, font, TextId.HELP_CONTENT_P1_LABEL_QUIT, 20, 114, 0x55FF55);
                drawPlainId(g, font, TextId.HELP_CONTENT_P1_DESC_MUSIC_ROOM, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P1_DESC_OPTION, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P1_DESC_QUIT, 20, 128, 0xFFFFFF);
                return;

            case 2:
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_TITLE_NORMAL, 20, 58, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_MOVE, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_KEY_1_9_UNUSED, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_KEY_OK_UNUSED, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_KEY_STAR_SHOT, 20, 114, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_KEY_0_SPEED, 20, 128, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_KEY_HASH_BOMB, 20, 142, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_NOTE_OPTIONS, 20, 156, 0xFF7777);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_TITLE_TALK, 20, 184, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P2_KEY_OK_MESSAGE, 20, 198, 0xFFFFFF);
                return;

            case 3:
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_LABEL_HISCORE, 20, 58, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_DESC_HISCORE, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_LABEL_SCORE, 20, 86, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_DESC_SCORE, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_LABEL_PLAYER, 20, 114, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_DESC_PLAYER, 20, 128, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_LABEL_SPELL, 20, 142, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_DESC_SPELL, 20, 156, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_LABEL_POWER, 20, 170, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_DESC_POWER, 20, 184, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_LABEL_GRAZE, 20, 198, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P3_DESC_GRAZE, 20, 212, 0xFFFFFF);
                return;

            case 4:
                drawCcIcon(g, imgs, 120, 25, 53);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_LABEL_P_SMALL, 40, 58, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_DESC_P_SMALL_1, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_DESC_P_SMALL_2, 20, 86, 0xFFFFFF);

                drawCcIcon(g, imgs, 111, 25, 102);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_LABEL_POINT, 40, 107, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_DESC_POINT_1, 20, 121, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_DESC_POINT_2, 20, 135, 0xFFFFFF);

                drawCcIcon(g, imgs, 129, 26, 151);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_LABEL_P_BIG, 40, 156, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_DESC_P_BIG, 20, 170, 0xFFFFFF);

                drawCcIcon(g, imgs, 138, 26, 186);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_LABEL_F, 40, 191, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P4_DESC_F, 20, 205, 0xFFFFFF);
                return;

            case 5:
                drawCcIcon(g, imgs, 147, 26, 53);
                drawPlainId(g, font, TextId.HELP_CONTENT_P5_LABEL_B, 40, 58, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P5_DESC_B, 20, 72, 0xFFFFFF);

                drawCcIcon(g, imgs, 156, 26, 87);
                drawPlainId(g, font, TextId.HELP_CONTENT_P5_LABEL_1UP, 40, 93, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P5_DESC_1UP, 20, 107, 0xFFFFFF);

                drawCcIcon(g, imgs, 165, 26, 123);
                drawPlainId(g, font, TextId.HELP_CONTENT_P5_LABEL_STAR, 40, 128, 0x0077FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P5_DESC_STAR_1, 20, 142, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P5_DESC_STAR_2, 20, 156, 0xFFFFFF);
                return;

            case 6:
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_TITLE_SPELL_CARD, 20, 58, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_DESC_SPELL_CARD_1, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_DESC_SPELL_CARD_2, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_DESC_SPELL_CARD_3, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_NOTE_SPELL_CARD, 20, 114, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_TITLE_SPELL_BONUS, 20, 142, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_DESC_SPELL_BONUS_1, 20, 156, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_DESC_SPELL_BONUS_2, 20, 170, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_DESC_SPELL_BONUS_3, 20, 184, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_DESC_SPELL_BONUS_4, 20, 198, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P6_DESC_SPELL_BONUS_5, 20, 212, 0xFFFFFF);
                return;

            case 7:
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_TITLE_EXTEND, 20, 58, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_EXTEND_1, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_EXTEND_2, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_EXTEND_3, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_TITLE_DEATH_BOMB, 20, 121, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_DEATH_BOMB_1, 20, 135, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_DEATH_BOMB_2, 20, 149, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_DEATH_BOMB_3, 20, 163, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_TITLE_AUTO_COLLECT, 20, 184, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_AUTO_COLLECT_1, 20, 198, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_AUTO_COLLECT_2, 20, 212, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P7_DESC_AUTO_COLLECT_3, 20, 226, 0xFFFFFF);
                return;

            case 8:
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_TITLE_SAVE_METHOD, 20, 58, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_DESC_SAVE_METHOD_1, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_DESC_SAVE_METHOD_2, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_DESC_SAVE_METHOD_3, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_NOTE_SAVE_METHOD, 20, 114, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_DESC_SAVE_METHOD_4, 20, 128, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_DESC_SAVE_METHOD_5, 20, 142, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_TITLE_SPECIAL_CONTROLS, 20, 170, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_DESC_SPECIAL_CONTROLS_1, 20, 184, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P8_DESC_SPECIAL_CONTROLS_2, 20, 198, 0xFFFFFF);
                return;

            case 9:
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_TITLE_HOMEPAGE_DOWNLOAD, 20, 58, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_DESC_HOMEPAGE_DOWNLOAD_1, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_DESC_HOMEPAGE_DOWNLOAD_2, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_NOTE_HOMEPAGE_DOWNLOAD, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_TITLE_HOMEPAGE_UPLOAD, 20, 128, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_DESC_HOMEPAGE_UPLOAD_1, 20, 142, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_DESC_HOMEPAGE_UPLOAD_2, 20, 156, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_DESC_HOMEPAGE_UPLOAD_3, 20, 170, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_NOTE_HOMEPAGE_UPLOAD_1, 20, 184, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P9_NOTE_HOMEPAGE_UPLOAD_2, 20, 198, 0xFFFFFF);
                return;

            case 10:
                drawPlainId(g, font, TextId.HELP_CONTENT_P10_TITLE_SD_CARD_SAVE, 20, 58, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P10_DESC_SD_CARD_SAVE_1, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P10_DESC_SD_CARD_SAVE_2, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P10_DESC_SD_CARD_SAVE_3, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P10_NOTE_SD_CARD_SAVE_1, 20, 114, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P10_NOTE_SD_CARD_SAVE_2, 20, 128, 0xFFFFFF);
                return;

            case 11:
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_TITLE_RANKING_JOIN, 20, 58, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_DESC_RANKING_JOIN_1, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_DESC_RANKING_JOIN_2, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_DESC_RANKING_JOIN_3, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_DESC_RANKING_JOIN_4, 20, 114, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_DESC_RANKING_JOIN_5, 20, 128, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_TITLE_REGISTRATION_NOTES, 20, 156, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_DESC_REGISTRATION_NOTES_1, 20, 170, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_DESC_REGISTRATION_NOTES_2, 20, 184, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P11_DESC_REGISTRATION_NOTES_3, 20, 198, 0xFFFFFF);
                return;

            case 12:
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_TITLE_GHOST_MODE, 20, 58, 0x7777FF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_1, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_2, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_3, 20, 109, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_4, 20, 123, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_5, 20, 146, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_6, 20, 169, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_7, 20, 183, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_8, 20, 197, 0xFFFFFF);
                drawPlainId(g, font, TextId.HELP_CONTENT_P12_DESC_GHOST_MODE_9, 20, 211, 0xFFFFFF);
                return;

            case 13:
                drawPlainId(g, font, TextId.CREDITS_CATEGORY_PROGRAM, 20, 58, 0x55FF55);
                drawPlainId(g, font, TextId.CREDITS_CATEGORY_GRAPHIC, 20, 86, 0x55FF55);
                drawPlainId(g, font, TextId.CREDITS_CATEGORY_SOUND, 20, 128, 0x55FF55);
                drawPlainId(g, font, TextId.CREDITS_CATEGORY_SUPPORT, 20, 156, 0x55FF55);
                drawPlainId(g, font, TextId.CREDITS_PERSON_SHIINA, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_PERSON_FU, 20, 100, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_PERSON_RADRAM, 20, 114, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_PERSON_SCIROCCO, 20, 142, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_PERSON_MEGRA, 20, 170, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_PERSON_KAJI, 20, 184, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_PERSON_HONAMI, 20, 198, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_PERSON_KINMACHI, 20, 212, 0xFFFFFF);
                return;

            case 14:
                drawPlainId(g, font, TextId.CREDITS_TITLE_TOUHOU_PROJECT, 20, 58, 0xFFCF0F);
                drawPlainId(g, font, TextId.CREDITS_TITLE_HYOUIBANA_DEV, 20, 114, 0xFFCF0F);
                drawPlainId(g, font, TextId.CREDITS_TOUHOU_TEAM_SHANGHAI, 20, 72, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_TOUHOU_URL, 20, 86, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_HYOUIBANA_L_GARDEN, 20, 128, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_HYOUIBANA_L_GARDEN_URL, 20, 142, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_HYOUIBANA_TOPPATU, 20, 156, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_HYOUIBANA_TOPPATU_URL, 20, 170, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_NOTE_DERIVATIVE_1, 20, 198, 0xFFFFFF);
                drawPlainId(g, font, TextId.CREDITS_NOTE_DERIVATIVE_2, 20, 212, 0xFFFFFF);
                return;
        }
    }

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

        if (w <= 0 || h <= 0) {
            return false;
        }

        int imgW = img.getWidth();
        int imgH = img.getHeight();
        if (srcX >= imgW || srcY >= imgH) {
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
}
