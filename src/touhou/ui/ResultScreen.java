package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.ResultStats;
import touhou.i18n.I18n;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class ResultScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_TITLE = 1;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static final int SCENE_MENU = 0;
    private static final int SCENE_SPELLCARD = 1;
    private static final int SCENE_SCORE = 2;
    private static final int SCENE_OTHER = 4;

    private static final int PAGE_SIZE = 10;

    private static String menuText(int idx) {
        switch (idx) {
            case 0:
                return UiText.get(TextId.RESULT_MENU_0);
            case 1:
                return UiText.get(TextId.RESULT_MENU_1);
            case 2:
                return UiText.get(TextId.RESULT_MENU_2);
            case 3:
                return UiText.get(TextId.RESULT_MENU_3);
            case 4:
                return UiText.get(TextId.RESULT_MENU_4);
            default:
                return "";
        }
    }

    private static String charaText(int idx) {
        switch (idx) {
            case 0:
                return UiText.get(TextId.RESULT_CHARA_0);
            case 1:
                return UiText.get(TextId.RESULT_CHARA_1);
            case 2:
                return UiText.get(TextId.RESULT_CHARA_2);
            case 3:
                return UiText.get(TextId.RESULT_CHARA_3);
            case 4:
                return UiText.get(TextId.RESULT_CHARA_4);
            case 5:
                return UiText.get(TextId.RESULT_CHARA_5);
            case 6:
                return UiText.get(TextId.RESULT_CHARA_6);
            default:
                return "";
        }
    }

    private int scene;
    private int cursorUD;
    private int cursorLR;
    private int cursor2;

    private int effect1;
    private int effect2;

    public void enter() {
        ResultStats.INSTANCE.onResultScreenEnter();
        scene = SCENE_MENU;
        cursorUD = 0;
        cursorLR = 0;
        cursor2 = 0;
        effect1 = 255;
        effect2 = 0;
    }

    public Result update(int pressed) {
        if (scene == SCENE_MENU) {
            effect1 = (effect1 << 1) + 1;
            if (effect1 > 255) {
                effect1 = 255;
            }
            effect2 >>= 1;
        } else {
            effect1 >>= 1;
            effect2 = (effect2 << 1) + 1;
            if (effect2 > 255) {
                effect2 = 255;
            }
        }

        if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            if (scene == SCENE_MENU) {
                return new Result(Result.KIND_BACK_TO_TITLE);
            }
            scene = SCENE_MENU;
            cursorLR = 0;
            cursor2 = 0;
            return null;
        }

        if (scene == SCENE_MENU) {
            if ((pressed & GameCanvas.UP_PRESSED) != 0) {
                cursorUD = (cursorUD - 1 + 5) % 5;
            } else if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
                cursorUD = (cursorUD + 1) % 5;
            } else if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                if (cursorUD == 0) {
                    scene = SCENE_SPELLCARD;
                    cursorLR = 0;
                    cursor2 = 0;
                } else if (cursorUD == 1) {
                    scene = SCENE_SCORE;
                    cursorLR = 0;
                    cursor2 = 0;
                } else if (cursorUD == 2) {
                    scene = SCENE_SCORE;
                    cursorLR = 0;
                    cursor2 = 0;
                } else if (cursorUD == 3) {
                    scene = SCENE_OTHER;
                } else {
                    return new Result(Result.KIND_BACK_TO_TITLE);
                }
            }
            return null;
        }

        if (scene == SCENE_SPELLCARD) {
            int pageCount = (ResultStats.SPELL_COUNT - 1) / PAGE_SIZE + 1;
            if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
                if (cursorLR > 0) {
                    cursorLR--;
                }
            } else if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
                if (cursorLR < pageCount - 1) {
                    cursorLR++;
                }
            } else if ((pressed & GameCanvas.UP_PRESSED) != 0) {
                cursor2 = (cursor2 - 1 + 7) % 7;
            } else if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
                cursor2 = (cursor2 + 1) % 7;
            }
            return null;
        }

        if (scene == SCENE_SCORE) {
            if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
                if (cursorLR != 0) {
                    cursorLR = 0;
                }
            } else if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
                if (cursorLR != 1) {
                    cursorLR = 1;
                }
            } else if ((pressed & GameCanvas.UP_PRESSED) != 0) {
                cursor2 = (cursor2 - 1 + 6) % 6;
            } else if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
                cursor2 = (cursor2 + 1) % 6;
            }
            return null;
        }

        if (scene == SCENE_OTHER) {
            return null;
        }

        return null;
    }

    public void render(Graphics g) {
        if (g == null) {
            return;
        }

        UiDraw.drawGradation(g, 0, 0, 240, 240, 0x000000, 9540095, 32);

        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

        UiDraw.drawStringPlain(g, font, UiText.get(TextId.RESULT_TITLE), 120 - effect2, 22, 1, 16733525);

        for (int i = 0; i < 5; i++) {
            int main;
            int sh;
            if (i == cursorUD && scene == SCENE_MENU) {
                main = 0xFFFFFF;
                sh = 7829367;
            } else {
                main = 6710886;
                sh = 3355443;
            }
            UiDraw.drawString2(g, font, menuText(i), 30 - effect2, 50 + i * 20, 0, main, sh);
        }

        if (scene == SCENE_MENU) {
            renderPreview(g, font);
            return;
        }

        if (scene == SCENE_SPELLCARD) {
            renderSpellCard(g, font);
            return;
        }

        if (scene == SCENE_SCORE) {
            renderScore(g, font);
            return;
        }

        if (scene == SCENE_OTHER) {
            renderOther(g, font);
        }
    }

    private void renderPreview(Graphics g, Font font) {
        if (cursorUD == 0) {
            renderSpellCard(g, font);
        } else if (cursorUD == 1) {
            renderScore(g, font);
        } else if (cursorUD == 3) {
            renderOther(g, font);
        } else if (cursorUD == 2) {
            UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_PREVIEW_NET_NOT_IMPLEMENTED), 120, 120, 1, 0xFFFFFF, 0x777777);
        }
    }

    private void renderSpellCard(Graphics g, Font font) {
        int spellTotal = ResultStats.SPELL_COUNT;
        int pageCount = (spellTotal - 1) / PAGE_SIZE + 1;

        UiDraw.drawStringPlain(g, font, UiText.get(TextId.RESULT_SPELLCARD_TITLE_PREFIX) + charaText(cursor2) + UiText.get(TextId.RESULT_SPELLCARD_TITLE_SUFFIX), 120 + effect1, 22, 1, 16711680);
        UiDraw.drawString2(g, font, String.valueOf(cursorLR + 1) + "/" + pageCount, 220 + effect1, 40, 2, 16720418, 7807522);

        int[] bonus = new int[ResultStats.SPELL_COUNT];
        int[] seen = new int[ResultStats.SPELL_COUNT];
        ResultStats.INSTANCE.buildSpellArrays(bonus, seen, cursor2);

        int captured = 0;
        for (int i = 0; i < ResultStats.SPELL_COUNT; i++) {
            if (bonus[i] != 0) {
                captured++;
            }
        }

        UiDraw.drawStringOutline(g, font, UiText.get(TextId.RESULT_SPELLCARD_CAPTURED_PREFIX) + captured + "/" + spellTotal, 20 + effect1, 40, 0, 2237183, 7829503);

        int start = cursorLR * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && start + i < spellTotal; i++) {
            int id = start + i;
            String name = ResultStats.INSTANCE.getSpellDisplayName(id, seen[id] != 0);

            // Only ellipsize spell names for languages that can overflow horizontally.
            boolean ellipsize = (I18n.getLanguageId() == I18n.LANG_EN);
            if (ellipsize) {
                int colW = 235 - 20;
                int maxW = (colW * 4) / 5;
                if (maxW > 0) {
                    name = UiDraw.ellipsize(font, name, maxW);
                }
            }
            UiDraw.drawString2(g, font, name, 20 + effect1, 60 + i * 18, 0, 0xFFFFFF, 7829367);
            UiDraw.drawString2(g, font, String.valueOf(bonus[id]) + "/" + String.valueOf(seen[id]), 235 + effect1, 60 + i * 18, 2, 0xFFFFFF, 7829367);
            if (bonus[id] != 0) {
                UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_STAR), 5 + effect1, 60 + i * 18, 0, 0xFFFFFF, 7829367);
            }
            g.setColor(0x777777);
            g.drawLine(20 + effect1, 60 + i * 18 + 4, 235 + effect1, 60 + i * 18 + 4);
        }
    }

    private void renderScore(Graphics g, Font font) {
        int unit = cursor2;
        if (unit < 0) {
            unit = 0;
        }
        if (unit > 5) {
            unit = 5;
        }

        UiDraw.drawStringPlain(g, font, UiText.get(TextId.RESULT_SCORE_TITLE_PREFIX) + charaText(unit + 1) + UiText.get(TextId.RESULT_SCORE_TITLE_SUFFIX), 120 + effect1, 22, 1, 16733525);

        int[] scores = ResultStats.INSTANCE.getScoreList(unit);
        int baseY = 40;

        if (cursorLR == 0) {
            drawScoreBlock(g, font, UiText.get(TextId.RESULT_SCORE_EASY), 30 - effect1, baseY + 0, scores, 0, 7829503, 10066329);
            drawScoreBlock(g, font, UiText.get(TextId.RESULT_SCORE_NORMAL), 70 + effect1, baseY + 48, scores, 3, 7864183, 10066329);
            drawScoreBlock(g, font, UiText.get(TextId.RESULT_SCORE_HARD), 110 - effect1, baseY + 96, scores, 6, 16742263, 10066329);
            drawScoreBlock(g, font, UiText.get(TextId.RESULT_SCORE_LUNATIC), 150 + effect1, baseY + 144, scores, 9, 16777079, 10066329);
        } else {
            UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_SCORE_EXTRA), 75 - effect1, baseY + 84, 0, 10448895, 11513775);
            UiDraw.drawString2(g, font, "1: " + String.valueOf(scores[12]), 75 - effect1, baseY + 96, 0, 11501567, 11513775);
            UiDraw.drawString2(g, font, "2: " + String.valueOf(scores[13]), 75 - effect1, baseY + 108, 0, 11501567, 11513775);
            UiDraw.drawString2(g, font, "3: " + String.valueOf(scores[14]), 75 - effect1, baseY + 120, 0, 11501567, 11513775);
            UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_SCORE_UPLOAD_NOT_IMPLEMENTED), 120, 230, 1, 0xFFFFFF, 0x777777);
        }
    }

    private void renderOther(Graphics g, Font font) {
        UiDraw.drawStringPlain(g, font, UiText.get(TextId.RESULT_OTHER_TITLE), 120 + effect1, 22, 1, 16711680);

        int[] ud = ResultStats.INSTANCE.getUserData();

        g.setColor(0xFFFFFF);
        g.drawRect(30 + effect1, 35, 180, 80);
        g.drawRect(30 + effect1, 125, 180, 96);
        g.drawLine(120 + effect1, 35, 120 + effect1, 115);
        g.drawLine(90 + effect1, 125, 90 + effect1, 221);
        g.drawLine(150 + effect1, 125, 150 + effect1, 221);

        for (int i = 0; i < 4; i++) {
            g.drawLine(30 + effect1, 51 + i * 16, 210 + effect1, 51 + i * 16);
        }
        for (int i = 0; i < 5; i++) {
            g.drawLine(30 + effect1, 141 + i * 16, 210 + effect1, 141 + i * 16);
        }

        int main = 0xFFFFFF;
        int sh = 8355711;

        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_BOOT_TIME), 115 + effect1, 48, 2, main, sh);
        UiDraw.drawString2(g, font, ResultStats.INSTANCE.formatBootTimeSecondsNow(ud[0]), 205 + effect1, 48, 2, main, sh);

        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_PLAY_TIME), 115 + effect1, 64, 2, main, sh);
        UiDraw.drawString2(g, font, ResultStats.INSTANCE.formatTimeSeconds(ud[1]), 205 + effect1, 64, 2, main, sh);

        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_TOTAL_CLEAR), 115 + effect1, 80, 2, main, sh);
        UiDraw.drawString2(g, font, String.valueOf(ud[2]), 205 + effect1, 80, 2, main, sh);

        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_CONTINUE_COUNT), 115 + effect1, 96, 2, main, sh);
        UiDraw.drawString2(g, font, String.valueOf(ud[3]), 205 + effect1, 96, 2, main, sh);

        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_RETRY_COUNT), 115 + effect1, 112, 2, main, sh);
        UiDraw.drawString2(g, font, String.valueOf(ud[4]), 205 + effect1, 112, 2, main, sh);

        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_PLAY_COUNT), 60 + effect1, 138, 1, main, sh);
        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_NORMAL), 120 + effect1, 138, 1, main, sh);
        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_PRACTICE), 180 + effect1, 138, 1, main, sh);

        drawPlayCountRow(g, font, UiText.get(TextId.REPLAY_DIFFICULTY_EASY), 154, ud[9], ud[5], main, sh);
        drawPlayCountRow(g, font, UiText.get(TextId.REPLAY_DIFFICULTY_NORMAL), 170, ud[10], ud[6], main, sh);
        drawPlayCountRow(g, font, UiText.get(TextId.REPLAY_DIFFICULTY_HARD), 186, ud[11], ud[7], main, sh);
        drawPlayCountRow(g, font, UiText.get(TextId.REPLAY_DIFFICULTY_LUNATIC), 202, ud[12], ud[8], main, sh);
        UiDraw.drawString2(g, font, UiText.get(TextId.RESULT_OTHER_EXTRA), 85 + effect1, 218, 2, main, sh);
        UiDraw.drawString2(g, font, String.valueOf(ud[13]), 145 + effect1, 218, 2, main, sh);
        UiDraw.drawString2(g, font, "-", 205 + effect1, 218, 2, main, sh);
    }

    private void drawPlayCountRow(Graphics g, Font font, String label, int y, int normal, int practice, int main, int sh) {
        UiDraw.drawString2(g, font, label, 85 + effect1, y, 2, main, sh);
        UiDraw.drawString2(g, font, String.valueOf(normal), 145 + effect1, y, 2, main, sh);
        UiDraw.drawString2(g, font, String.valueOf(practice), 205 + effect1, y, 2, main, sh);
    }

    private void drawScoreBlock(Graphics g, Font font, String label, int x, int y, int[] scores, int baseIndex, int cMain, int cShadow) {
        UiDraw.drawString2(g, font, label, x, y + 0, 0, cMain, cShadow);
        UiDraw.drawString2(g, font, "1: " + String.valueOf(scores[baseIndex + 0]), x, y + 12, 0, cMain, cShadow);
        UiDraw.drawString2(g, font, "2: " + String.valueOf(scores[baseIndex + 1]), x, y + 24, 0, cMain, cShadow);
        UiDraw.drawString2(g, font, "3: " + String.valueOf(scores[baseIndex + 2]), x, y + 36, 0, cMain, cShadow);
    }

    
}
