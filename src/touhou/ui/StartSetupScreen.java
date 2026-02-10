package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.BulletSprites;
import touhou.ImageBank;
import touhou.SpStore;
import touhou.UnlockFlags;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class StartSetupScreen {
    public static final class Result {
        public static final int KIND_BACK_TO_TITLE = 1;
        public static final int KIND_START_GAME = 2;

        public final int kind;
        public final int gamemode;
        public final int level;
        public final int chara;
        public final int type;
        public final int startStage;
        public final boolean pracBoss;

        public Result(int kind, int gamemode, int level, int chara, int type, int startStage, boolean pracBoss) {
            this.kind = kind;
            this.gamemode = gamemode;
            this.level = level;
            this.chara = chara;
            this.type = type;
            this.startStage = startStage;
            this.pracBoss = pracBoss;
        }
    }

    private static final String[] DIFF_DESC = new String[] {
            UiText.get(TextId.DIFFICULTY_DESC_EASY),
            UiText.get(TextId.DIFFICULTY_DESC_NORMAL),
            UiText.get(TextId.DIFFICULTY_DESC_HARD),
            UiText.get(TextId.DIFFICULTY_DESC_LUNATIC) };

    private static final String EXTRA_DESC = UiText.get(TextId.DIFFICULTY_DESC_EXTRA);

private static final String[] CHARA_NAME_0 = new String[] { 
            UiText.get(TextId.CHARACTER_NAME_REIMU), 
            UiText.get(TextId.CHARACTER_NAME_MARISA), 
            UiText.get(TextId.CHARACTER_NAME_ALICE) };

private static final String[][] CHARA_INFO = new String[][] {
            { 
                UiText.get(TextId.CHARACTER_INFO_REIMU_TITLE),
                UiText.get(TextId.CHARACTER_INFO_REIMU_NAME),
                UiText.get(TextId.CHARACTER_INFO_REIMU_TYPE_A_TITLE),
                UiText.get(TextId.CHARACTER_INFO_REIMU_TYPE_A_DESC_1),
                UiText.get(TextId.CHARACTER_INFO_REIMU_TYPE_A_DESC_2),
                UiText.get(TextId.CHARACTER_INFO_REIMU_TYPE_B_TITLE),
                UiText.get(TextId.CHARACTER_INFO_REIMU_TYPE_B_DESC_1),
                UiText.get(TextId.CHARACTER_INFO_REIMU_TYPE_B_DESC_2),
                UiText.get(TextId.CHARACTER_INFO_REIMU_ABILITY_TITLE),
                UiText.get(TextId.CHARACTER_INFO_REIMU_ABILITY_DESC)
            },
            { 
                UiText.get(TextId.CHARACTER_INFO_MARISA_TITLE),
                UiText.get(TextId.CHARACTER_INFO_MARISA_NAME),
                UiText.get(TextId.CHARACTER_INFO_MARISA_TYPE_A_TITLE),
                UiText.get(TextId.CHARACTER_INFO_MARISA_TYPE_A_DESC_1),
                UiText.get(TextId.CHARACTER_INFO_MARISA_TYPE_A_DESC_2),
                UiText.get(TextId.CHARACTER_INFO_MARISA_TYPE_B_TITLE),
                UiText.get(TextId.CHARACTER_INFO_MARISA_TYPE_B_DESC_1),
                UiText.get(TextId.CHARACTER_INFO_MARISA_TYPE_B_DESC_2),
                UiText.get(TextId.CHARACTER_INFO_MARISA_ABILITY_TITLE),
                UiText.get(TextId.CHARACTER_INFO_MARISA_ABILITY_DESC)
            },
            { 
                UiText.get(TextId.CHARACTER_INFO_ALICE_TITLE),
                UiText.get(TextId.CHARACTER_INFO_ALICE_NAME),
                UiText.get(TextId.CHARACTER_INFO_ALICE_TYPE_A_TITLE),
                UiText.get(TextId.CHARACTER_INFO_ALICE_TYPE_A_DESC_1),
                UiText.get(TextId.CHARACTER_INFO_ALICE_TYPE_A_DESC_2),
                UiText.get(TextId.CHARACTER_INFO_ALICE_TYPE_B_TITLE),
                UiText.get(TextId.CHARACTER_INFO_ALICE_TYPE_B_DESC_1),
                UiText.get(TextId.CHARACTER_INFO_ALICE_TYPE_B_DESC_2),
                UiText.get(TextId.CHARACTER_INFO_ALICE_ABILITY_TITLE),
                UiText.get(TextId.CHARACTER_INFO_ALICE_ABILITY_DESC)
            } };

    private int gamemode;

    private int sLevel;
    private int sChara;
    private int sType;
    private int startStage;

    private boolean pracBoss;

    private int charaMax;
    private int typeMax;

    private int scene;

    private int effect1;
    private int effect2;
    private int effect3;
    private int effect4;
    private int effect5;
    private int effect6;
    private int effect7;
    private int effect8;
    private int effect9;
    private int effectA;
    private int effectB;
    private int effectC;
    private int effectD;

    private final int[] work = new int[6];

    private final int[] progressByDifficulty = new int[] { 6, 6, 6, 6 };

    public void enter(int gamemode) {
        this.gamemode = gamemode;

        // Story/Practice start-stage availability (difficulty-isolated), compatible with DoJa :
        // readByteArray(102, 4) -> t[0..3].
        for (int i = 0; i < progressByDifficulty.length; i++) {
            progressByDifficulty[i] = 0;
        }
        try {
            byte[] b = SpStore.read(102, 4);
            if (b != null) {
                for (int i = 0; i < progressByDifficulty.length && i < b.length; i++) {
                    progressByDifficulty[i] = b[i] & 0xFF;
                }
            }
        } catch (Throwable t) {
            // Ignore storage errors.
        }

        sLevel = 0;
        if (gamemode == 1) {
            sLevel = 4;
        }
        sChara = 0;
        sType = 0;
        startStage = 0;

        if (!UnlockFlags.isAliceUnlocked() && sChara == 2) {
            sChara = 0;
        }

        charaMax = 3;
        typeMax = 2;

        effect1 = 5;
        effect2 = 0;
        effect3 = 5;
        effect4 = 5;
        effect5 = 0;
        effect6 = 255;
        effect7 = 255;
        effect8 = 0xFF0000;
        effect9 = 0xFF0000;
        effectA = 0;
        effectB = 0;
        effectC = 255;
        effectD = 0;

        pracBoss = false;
        resetWork();

        scene = 0;
    }

    public Result update(int pressed) {
        switch (scene) {
            case 0:
                tickScene0();
                if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                    scene = 1;
                    effect3 = 0;
                    effect5 = 0;
                    effect9 = effect8;
                    effect8 = charaColor(sChara);
                    return null;
                }
                if ((pressed & GameCanvas.DOWN_PRESSED) != 0 && gamemode != 1) {
                    sLevel = (sLevel + 1) & 3;
                    return null;
                } else if ((pressed & GameCanvas.UP_PRESSED) != 0 && gamemode != 1) {
                    sLevel = (sLevel - 1 + 4) & 3;
                    return null;
                }
                if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
                    return new Result(Result.KIND_BACK_TO_TITLE, gamemode, sLevel, sChara, sType, startStage, pracBoss);
                }
                return null;

            case 1:
                tickScene1();
                if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                    effect4 = 0;
                    effectC = 255;
                    if (gamemode == 1) {
                        scene = 3;
                        startStage = 6;
                        return null;
                    }
                    scene = 2;
                    startStage = 0;
                    pracBoss = false;
                    resetWork();
                    return null;
                }
                if ((pressed & GameCanvas.UP_PRESSED) != 0 && typeMax > 1) {
                    sType = (sType - 1 + typeMax) % typeMax;
                    return null;
                }
                if ((pressed & GameCanvas.DOWN_PRESSED) != 0 && typeMax > 1) {
                    sType = (sType + 1) % typeMax;
                    return null;
                }
                if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
                    int n = 0;
                    do {
                        sChara = (sChara - 1 + charaMax) % charaMax;
                        n++;
                    } while (n < charaMax && sChara == 2 && !UnlockFlags.isAliceUnlocked());
                    effect5 = 0;
                    effect9 = effect8;
                    effect8 = charaColor(sChara);
                    return null;
                }
                if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
                    int n = 0;
                    do {
                        sChara = (sChara + 1) % charaMax;
                        n++;
                    } while (n < charaMax && sChara == 2 && !UnlockFlags.isAliceUnlocked());
                    effect5 = 0;
                    effect9 = effect8;
                    effect8 = charaColor(sChara);
                    return null;
                }
                if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
                    scene = 0;
                    effect2 = 0;
                    effect9 = effect8;
                    effect8 = charaColor(sChara);
                    effectD = 160;
                    return null;
                }
                return null;

            case 2:
                tickScene2();
                if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                    scene = 3;
                    effectB = 0;
                    return null;
                }
                if ((pressed & GameCanvas.UP_PRESSED) != 0) {
                    int n = stageCount();
                    if (n > 0) {
                        startStage = (startStage - 1 + n) % n;
                        effectC = 255;
                    }
                    return null;
                }
                if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
                    int n = stageCount();
                    if (n > 0) {
                        startStage = (startStage + 1) % n;
                        effectC = 255;
                    }
                    return null;
                }
                if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
                    if (gamemode == 2 && bossToggleAvailable()) {
                        pracBoss = false;
                        return null;
                    }
                } else if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
                    if (gamemode == 2 && bossToggleAvailable()) {
                        pracBoss = true;
                        return null;
                    }
                }
                if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
                    scene = 1;
                    effect3 = 0;
                    effect9 = effect8;
                    effect8 = charaColor(sChara);
                    return null;
                }
                return null;

            case 3:
                effectB++;
                if (effectB >= 15) {
                    return new Result(Result.KIND_START_GAME, gamemode, sLevel, sChara, sType, startStage, pracBoss);
                }
                return null;
        }
        return null;
    }

    public void render(Graphics g, ImageBank imgs, BulletSprites sprites) {
        if (imgs == null) {
            return;
        }

        UiDraw.drawRegion(g, imgs.get(18), 0, 0, 0, 0, 240, 240);

        int colorFrom = 0x303030;
        int colorTo = 0xFFFFA0;
        if (gamemode == 1) {
            colorTo = 0xFFB000;
        } else if (gamemode == 2) {
            colorTo = 0xA10010;
        }
        UiDraw.drawGradationAlpha(g, 0, 0, 240, 240, colorFrom, colorTo, 32, 128);

        renderTopHints(g);
        renderDifficulty(g, sprites);
        renderChara(g, sprites);
        renderStageBlocks(g, imgs);
        renderPracticeLabel(g);
        renderFadeOut(g);
    }

    private void renderPracticeLabel(Graphics g) {
        if (gamemode != 2) {
            return;
        }
        UiDraw.drawString2(g, null, UiText.get(TextId.PRACTICE_MODE_LABEL), 140, 36, 0, 16777120, 3158064);
    }

private void renderTopHints(Graphics g) {
        int c = 0xFFFFAF;
        Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

        if (effect2 < 5) {
            UiDraw.drawStringPlain(g, f, UiText.get(TextId.TOP_HINT_SELECT_DIFFICULTY), 10, 20 - effect2, 0, c);
        }
        if (effect3 < 5) {
            UiDraw.drawStringPlain(g, f, UiText.get(TextId.TOP_HINT_SELECT_CHARACTER), 10, 20 - effect3, 0, c);
        }
        if (effect4 < 5) {
            UiDraw.drawStringPlain(g, f, UiText.get(TextId.TOP_HINT_SELECT_STAGE), 10, 20 - effect4, 0, c);
        }
    }

    private void renderDifficulty(Graphics g, BulletSprites sprites) {
        if (sprites == null) {
            return;
        }

        if (effect1 <= 5) {
            if (gamemode == 1) {
                sprites.draw(g, 476, 105, 120 - effect1);
            } else {
                for (int j = 0; j < 4; j++) {
                    int id = 467 + j;
                    if (j == sLevel) {
                        id = 472 + j;
                    }
                    int xBase = 75 + 30 * j;
                    int y = 50 + 45 * j;
                    int x;
                    if ((j & 1) == 0) {
                        x = xBase - (effect1 << 1);
                    } else {
                        x = xBase + (effect1 << 1);
                    }
                    sprites.draw(g, id, x, y);
                }
            }

            if (gamemode != 1) {
                UiDraw.drawString2(g, null, String.valueOf(DIFF_DESC[sLevel]), 120, 230, 1, 0xCFE7FF, 0x2F2F2F);
            } else {
                UiDraw.drawString2(g, null, EXTRA_DESC, 120, 230, 1, 0xCFE7FF, 0x2F2F2F);
            }
        }

        if (effectA > 0) {
            renderDifficultyHighlight(g, sprites);
        }

        if (scene == 0 && effectD > 0) {
            int y = 210;
            if (typeMax > 1) {
                y += 10;
            }
            int alpha = effectD;
            if (alpha < 0) {
                alpha = 0;
            }
            if (alpha > 255) {
                alpha = 255;
            }

            int id = 472 + sLevel;
            if (gamemode == 1) {
                id = 476;
            }
            sprites.drawAlpha(g, id, 120, y, alpha);
        }
    }

    private void renderDifficultyHighlight(Graphics g, BulletSprites sprites) {
        int id = 472 + sLevel;
        if (gamemode == 1) {
            id = 476;
        }

        int y = 210;
        if (typeMax > 1) {
            y += 10;
        }

        if (effect1 <= 5) {
            int dx = 5 - effect1;
            sprites.draw(g, id, 120 + dx, y);
            sprites.draw(g, id, 120 - dx, y);
        } else {
            sprites.draw(g, id, 120, y);
        }

        int dist = effectA * ((typeMax == 2) ? 14 : 10);

        int color = mixColor(effect8, effect9, effect5, 5);
        UiDraw.drawGradationAlpha(g, 0, 120 - dist, 240, dist, color, 0xFFFFFF, 24, 96);
        UiDraw.drawGradationAlpha(g, 0, 120, 240, dist, 0xFFFFFF, color, 24, 96);
    }

    private void renderChara(Graphics g, BulletSprites sprites) {
        if (sprites == null) {
            return;
        }

        int offset = 0;
        if (charaMax == 2) {
            offset = 16;
        }

        int baseX0 = 54 - effect6 + offset;
        int baseX1 = 79 - effect6 + offset;
        int baseX2 = 94 - effect6 + offset;

        if (sChara != 2 && charaMax >= 3) {
            sprites.drawAlpha(g, 479, baseX2, 170, 128);
        }
        if (sChara != 1) {
            sprites.drawAlpha(g, 478, baseX1, 170, 128);
        }
        if (sChara != 0) {
            sprites.drawAlpha(g, 477, baseX0, 170, 128);
        }

        int selX = baseX0;
        int selId = 480;
        int titleColor = 16764879;
        int titleShadow = 0;
        int nameColor = 16756655;
        int nameShadow = 0;
        if (sChara == 1) {
            selX = baseX1;
            selId = 481;
            titleColor = 13619151;
            titleShadow = 0;
            nameColor = 11513775;
            nameShadow = 0;
        } else if (sChara == 2) {
            selX = baseX2;
            selId = 482;
            titleColor = 12566527;
            titleShadow = 31;
            nameColor = 9408511;
            nameShadow = 47;
        }
        sprites.draw(g, selId, selX + effect5, 170);

        if (effect6 == 0) {
            int y = 75;
            if (typeMax == 2) {
                y -= 22;
            }
            int nameIdx = sChara;
            if (nameIdx < 0) {
                nameIdx = 0;
            }
            if (nameIdx >= CHARA_NAME_0.length) {
                nameIdx = CHARA_NAME_0.length - 1;
            }

            int infoIdx = nameIdx;
            if (infoIdx < 0) {
                infoIdx = 0;
            }
            if (infoIdx >= CHARA_INFO.length) {
                infoIdx = CHARA_INFO.length - 1;
            }

            UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][0], 170, y + 0, 1, titleColor, titleShadow);
            UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][1], 170, y + 15, 1, nameColor, nameShadow);

            int n14 = y + 35;
            int c0Main;
            int c0Shadow;
            int c1Main;
            int c1Shadow;
            if (sType == 0) {
                c0Main = 16777215;
                c0Shadow = 0;
                c1Main = 8355711;
                c1Shadow = 0;
            } else {
                c0Main = 8355711;
                c0Shadow = 0;
                c1Main = 16777215;
                c1Shadow = 0;
            }

            UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][2], 100, n14 + 0, 0, c0Main, c0Shadow);
            UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][3], 100, n14 + 15, 0, c0Main, c0Shadow);
            UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][4], 100, n14 + 30, 0, c0Main, c0Shadow);
            n14 += 45;

            if (typeMax == 2) {
                UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][5], 100, n14 + 0, 0, c1Main, c1Shadow);
                UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][6], 100, n14 + 15, 0, c1Main, c1Shadow);
                UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][7], 100, n14 + 30, 0, c1Main, c1Shadow);
                n14 += 45;
            }

            UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][8], 100, n14 + 0, 0, 8388479, 40704);
            UiDraw.drawString2(g, null, CHARA_INFO[infoIdx][9], 100, n14 + 15, 0, 8388479, 40704);
        }
    }

    private void renderFadeOut(Graphics g) {
        if (effectB <= 0) {
            return;
        }
        int alpha = 128 + (effectB << 2);
        if (alpha > 255) {
            alpha = 255;
        }
        UiDraw.fillRectAlpha(g, 0, 0, 240, 240, 0xFFFFFF, alpha);
    }

    private void tickScene0() {
        if (effect1 > 0) {
            effect1--;
        }
        if (effect3 < 7) {
            effect3++;
        }
        if (effect4 < 7) {
            effect4++;
        }
        if (effect5 > 0) {
            effect5--;
        }
        if (effect6 < 255) {
            effect6 = (effect6 << 1) + 1;
            if (effect6 > 255) {
                effect6 = 255;
            }
        }
        if (effect7 < 255) {
            effect7 = (effect7 << 1) + 1;
            if (effect7 > 255) {
                effect7 = 255;
            }
        }
        if (effectA > 0) {
            effectA--;
        }
        if (effectD > 0) {
            effectD -= 16;
            if (effectD < 0) {
                effectD = 0;
            }
        }
    }

    private void tickScene1() {
        if (effect1 < 6) {
            effect1++;
        }
        if (effect2 < 7) {
            effect2++;
        }
        if (effect4 < 7) {
            effect4++;
        }
        if (effect6 > 0) {
            effect6 >>= 1;
        } else if (effect5 < 5) {
            effect5++;
        }
        if (effect7 < 255) {
            effect7 = (effect7 << 1) + 1;
            if (effect7 > 255) {
                effect7 = 255;
            }
        }
        if (effectA < 6) {
            effectA++;
        }
    }

    private void tickScene2() {
        if (effect1 < 6) {
            effect1++;
        }
        if (effect2 < 7) {
            effect2++;
        }
        if (effect3 < 7) {
            effect3++;
        }
        if (effect6 > 0) {
            effect6 >>= 1;
        } else if (effect5 < 5) {
            effect5++;
        }
        if (effect7 > 0) {
            effect7 >>= 1;
        }
        if (effectA < 6) {
            effectA++;
        }
        if (effectC > 0) {
            effectC >>= 1;
        }

        int n = stageCount();
        for (int k = 0; k < work.length && k < n; k++) {
            if (k == startStage) {
                if (work[k] < 8) {
                    work[k] += 3;
                }
            } else {
                if (work[k] > 0) {
                    work[k] >>= 1;
                }
            }
        }
    }

    private void renderStageBlocks(Graphics g, ImageBank imgs) {
        if (gamemode == 1) {
            return;
        }
        if (!(scene == 1 || scene == 2 || scene == 3)) {
            return;
        }

        javax.microedition.lcdui.Image img = imgs.get(11);
        if (img == null) {
            return;
        }

        int n = stageCount();
        if (n <= 0) {
            return;
        }

        int m = effect7;
        int n20 = 70 - (n - 1) * 12;
        final int n21 = 110;
        for (int k = 0; k < n; k++) {
            int xBase = 125 - work[k];
            n20 += 28;

            int x = xBase + m;
            int y = n20;

            UiDraw.drawRegion(g, img, x, y, 0, 0, 105, 26);
            UiDraw.drawRegion(g, img, x + 21, y + 6, 0, 52, 50, 14);
            UiDraw.drawRegion(g, img, x + 76, y + 7, 50 + (k << 3), 54, 8, 12);

            if (k != startStage) {
                UiDraw.fillRectAlpha(g, x, y, 105, 26, 0x000000, 96);
            }

            if (gamemode == 2 && k == startStage && bossToggleAvailable()) {
                int bx0 = x + (n21 >> 1) - 23 - 26 + effectC;
                int by = y + 20;
                int bx1 = x + (n21 >> 1) - 23 + 54 - 26 + effectC;

                UiDraw.drawRegion(g, img, bx0, by, 0, 26, 52, 26);
                UiDraw.drawRegion(g, img, bx0 + 9, by + 6, 52, 26, 34, 14);

                UiDraw.drawRegion(g, img, bx1, by, 0, 26, 52, 26);
                UiDraw.drawRegion(g, img, bx1 + 9, by + 6, 52, 40, 34, 14);

                if (pracBoss) {
                    UiDraw.fillRectAlpha(g, bx0, by, 52, 26, 0x000000, 96);
                } else {
                    UiDraw.fillRectAlpha(g, bx1, by, 52, 26, 0x000000, 96);
                }
                n20 += 20;
            }

            m <<= 1;
        }
    }

    private int stageCount() {
        int idx = sLevel;
        if (idx < 0) {
            idx = 0;
        }
        if (idx > 3) {
            idx = 3;
        }

        int t = progressByDifficulty[idx];
        if (gamemode == 2 && t > 0) {
            t--;
        }
        if (t > 5) {
            t = 5;
        }
        return t + 1;
    }

    private boolean bossToggleAvailable() {
        for (int i = 0; i < progressByDifficulty.length; i++) {
            if (progressByDifficulty[i] >= 6) {
                return true;
            }
        }
        return false;
    }

    private void resetWork() {
        for (int i = 0; i < work.length; i++) {
            work[i] = 0;
        }
    }

    private static int charaColor(int chara) {
        if (chara == 0) {
            return 0xFF0000;
        }
        if (chara == 1) {
            return 0x000000;
        }
        return 0x0000FF;
    }

    private static int mixColor(int c0, int c1, int t, int tMax) {
        if (t <= 0) {
            return c1;
        }
        if (t >= tMax) {
            return c0;
        }

        int r0 = (c0 >> 16) & 0xFF;
        int g0 = (c0 >> 8) & 0xFF;
        int b0 = (c0) & 0xFF;

        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = (c1) & 0xFF;

        int inv = tMax - t;
        int r = (r0 * t + r1 * inv) / tMax;
        int gg = (g0 * t + g1 * inv) / tMax;
        int bb = (b0 * t + b1 * inv) / tMax;
        return (r << 16) | (gg << 8) | bb;
    }
}
