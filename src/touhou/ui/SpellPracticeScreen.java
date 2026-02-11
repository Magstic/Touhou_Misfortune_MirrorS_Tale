package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.BulletSprites;
import touhou.GameProgress;
import touhou.ImageBank;
import touhou.UnlockFlags;
import touhou.i18n.I18n;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class SpellPracticeScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_TITLE = 1;
        public static final int KIND_START_PRACTICE = 2;

        public final int kind;
        public final int chara;
        public final int type;
        public final int stageIndex;
        public final int spellId;

        public Result(int kind) {
            this.kind = kind;
            this.chara = 0;
            this.type = 0;
            this.stageIndex = 0;
            this.spellId = 0;
        }

        public Result(int kind, int chara, int type, int stageIndex, int spellId) {
            this.kind = kind;
            this.chara = chara;
            this.type = type;
            this.stageIndex = stageIndex;
            this.spellId = spellId;
        }
    }

    private static final int[][] STAGE_RANGES = new int[][] { { 0, 18 }, { 18, 30 }, { 30, 46 }, { 46, 62 }, { 62, 78 }, { 78, 98 }, { 98, 107 }, { 107, 116 } };
    private static final String[] STAGE_NAMES = new String[] { "Stage1", "Stage2", "Stage3", "Stage4", "Stage5", "Stage6", "ExtraStage", "LastWord" };
    private static final String[] CHARA_EN = new String[] { "Reimu", "Marisa", "Alice" };
    private static final String[][] CHARA_INFO = new String[][] {
            { 
                UiText.get(TextId.CHARACTER_INFO_REIMU_TITLE),
                UiText.get(TextId.CHARACTER_INFO_REIMU_NAME),
                UiText.get(TextId.CHARACTER_INFO_REIMU_TYPE_A_TITLE),
                UiText.get(TextId.CHARACTER_INFO_REIMU_TYPE_B_TITLE),
            },
            { 
                UiText.get(TextId.CHARACTER_INFO_MARISA_TITLE),
                UiText.get(TextId.CHARACTER_INFO_MARISA_NAME),
                UiText.get(TextId.CHARACTER_INFO_MARISA_TYPE_A_TITLE),
                UiText.get(TextId.CHARACTER_INFO_MARISA_TYPE_B_TITLE),
            },
            { 
                UiText.get(TextId.CHARACTER_INFO_ALICE_TITLE),
                UiText.get(TextId.CHARACTER_INFO_ALICE_NAME),
                UiText.get(TextId.CHARACTER_INFO_ALICE_TYPE_A_TITLE),
                UiText.get(TextId.CHARACTER_INFO_ALICE_TYPE_B_TITLE),
            } };

    private static boolean spellTextLoaded;
    private static String[] spellNames;
    private static String[] spellComments;

    private static final int SPELL_COUNT = 116;

    private static final String UNKNOWN_NAME = UiText.get(TextId.UNLOCK);

    private final int[] sumBonusAll = new int[SPELL_COUNT];
    private final int[] sumChareAll = new int[SPELL_COUNT];
    private boolean progressLoaded;

    // Throttled progress refresh for on-screen updates (RMS load can be expensive).
    private int refreshCooldown;
    private static final int REFRESH_PERIOD = 4;

    private int scene;
    private int cursorUD;
    private int cursorLR;
    private int cursor2;

    // Marquee tick for spell card names in list (selected row only).
    private int spellNameMarqueeTick;

    // Vertical auto-scroll tick for spell card comment (bottom area).
    private int spellCommentScrollTick;
    private int spellCommentCacheSpellId = -1;
    private boolean spellCommentCacheVisible;
    private String spellCommentCacheText;
    private String[] spellCommentCacheLines;

    private int chara;
    private int type;

    private int effect1;
    private int effect5;
    private int effect6;
    private int effect8;
    private int effect9;
    private int effectA;
    private int effectB;

    public void enter(boolean reset) {
        ensureSpellTextsLoaded();
        progressLoaded = false;
        ensureProgressLoaded();
        refreshCooldown = 0;

        spellNameMarqueeTick = 0;
        if (reset) {
            cursorLR = 0;
            cursorUD = 0;
            scene = 0;
            chara = 0;
            type = 0;
            effect1 = 255;
            effect5 = 0;
            effect6 = 255;
            effect8 = 0xFFFFFF;
            effect9 = effect8;
            effectA = 0xFF0000;
            effectB = effectA;
        }
    }

    private void ensureProgressLoaded() {
        if (progressLoaded) {
            return;
        }
        GameProgress.INSTANCE.loadFromSp();
        buildSpellTotals(sumBonusAll, sumChareAll);
        progressLoaded = true;
    }

    private void refreshProgressIfNeeded(boolean force) {
        if (force || refreshCooldown <= 0) {
            GameProgress.INSTANCE.loadFromSp();
            buildSpellTotals(sumBonusAll, sumChareAll);
            progressLoaded = true;
            refreshCooldown = REFRESH_PERIOD;
            return;
        }
        refreshCooldown--;
    }

    private static void buildSpellTotals(int[] outBonus, int[] outChare) {
        int[][] bonusSrc = GameProgress.INSTANCE.scBonusCnt;
        int[][] seenSrc = GameProgress.INSTANCE.scChareCnt;

        for (int i = 0; i < SPELL_COUNT; i++) {
            int b = 0;
            int s = 0;
            for (int u = 0; u < GameProgress.UNIT_COUNT; u++) {
                b += bonusSrc[u][i];
                s += seenSrc[u][i];
            }
            outBonus[i] = b;
            outChare[i] = s;
        }
    }

    // Custom LastWord unlock rules.
    // - 107..109: require capturing specific spell IDs.
    // - 110..115: require capturing all spell cards in the corresponding stage range.
    private boolean isLastWordUnlockedCustom(int lwSpellId) {
        if (lwSpellId < 107 || lwSpellId > 115) {
            return false;
        }

        ensureProgressLoaded();

        if (lwSpellId == 107) {
            return sumBonusAll[0] > 0 && sumBonusAll[2] > 0 && sumBonusAll[3] > 0 && sumBonusAll[4] > 0;
        }
        if (lwSpellId == 108) {
            return sumBonusAll[6] > 0 && sumBonusAll[7] > 0 && sumBonusAll[8] > 0;
        }
        if (lwSpellId == 109) {
            return sumBonusAll[10] > 0 && sumBonusAll[11] > 0 && sumBonusAll[12] > 0;
        }

        int stageIndex = -1;
        if (lwSpellId == 110) {
            // Stage2
            stageIndex = 1;
        } else if (lwSpellId == 111) {
            // Stage3
            stageIndex = 2;
        } else if (lwSpellId == 112) {
            // Stage4
            stageIndex = 3;
        } else if (lwSpellId == 113) {
            // Stage5
            stageIndex = 4;
        } else if (lwSpellId == 114) {
            // Stage6
            stageIndex = 5;
        } else if (lwSpellId == 115) {
            // ExtraStage
            stageIndex = 6;
        }

        if (stageIndex < 0 || stageIndex >= STAGE_RANGES.length) {
            return false;
        }

        int start = STAGE_RANGES[stageIndex][0];
        int end = STAGE_RANGES[stageIndex][1];
        if (start < 0) {
            start = 0;
        }
        if (end > SPELL_COUNT) {
            end = SPELL_COUNT;
        }
        for (int id = start; id < end; id++) {
            if (sumBonusAll[id] == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isSelectableInPracticeList(int spellId) {
        if (spellId < 0 || spellId >= SPELL_COUNT) {
            return false;
        }
        ensureProgressLoaded();
        if (sumChareAll[spellId] != 0) {
            return true;
        }
        if (spellId >= 107 && spellId <= 115) {
            return isLastWordUnlockedCustom(spellId);
        }
        return false;
    }

    public Result update(int pressed) {
        refreshProgressIfNeeded(pressed != 0);

        effect1 >>= 1;
        if (effect6 > 0) {
            effect6 -= 1;
        } else if (effect5 < 5) {
            effect5++;
        }

        if (scene == 1) {
            spellNameMarqueeTick += 2;
            spellCommentScrollTick += 2;
        } else {
            spellCommentScrollTick = 0;
        }

        if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            if (scene == 0) {
                return new Result(Result.KIND_BACK_TO_TITLE);
            }
            scene = 0;
            effect1 = 255;
            spellNameMarqueeTick = 0;
            return null;
        }

        if ((pressed & GameCanvas.GAME_B_PRESSED) != 0) {
            type ^= 1;
            return null;
        }

        if (scene == 0) {
            if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                scene = 1;
                cursor2 = 0;
                cursorLR = 0;
                effect1 = 255;
                spellNameMarqueeTick = 0;
                return null;
            }

            if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
                cursorUD = (cursorUD + 1) % STAGE_RANGES.length;
            } else if ((pressed & GameCanvas.UP_PRESSED) != 0) {
                cursorUD = (cursorUD - 1 + STAGE_RANGES.length) % STAGE_RANGES.length;
            }

            if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
                chara = (chara - 1 + 3) % 3;
                if (chara == 2 && !UnlockFlags.isAliceUnlocked()) {
                    chara = 1;
                }
                applyCharaChangeEffects();
            } else if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
                chara = (chara + 1) % 3;
                if (chara == 2 && !UnlockFlags.isAliceUnlocked()) {
                    chara = 0;
                }
                applyCharaChangeEffects();
            }

            return null;
        }

        int start = STAGE_RANGES[cursorUD][0];
        int end = STAGE_RANGES[cursorUD][1];
        int total = end - start;
        int perPage = 6;
        int pageCount = (total + perPage - 1) / perPage;
        if (pageCount <= 0) {
            pageCount = 1;
        }
        cursorLR %= pageCount;
        if (cursorLR < 0) {
            cursorLR += pageCount;
        }
        int remain = total - cursorLR * perPage;
        int n19 = perPage;
        if (n19 > remain) {
            n19 = remain;
        }
        if (n19 <= 0) {
            n19 = 1;
        }

        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursor2 = (cursor2 + 1) % n19;
            spellNameMarqueeTick = 0;
            spellCommentScrollTick = 0;
        } else if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            cursor2 = (cursor2 - 1 + n19) % n19;
            spellNameMarqueeTick = 0;
            spellCommentScrollTick = 0;
        } else if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
            cursorLR = (cursorLR - 1 + pageCount) % pageCount;
            remain = total - cursorLR * perPage;
            n19 = perPage;
            if (n19 > remain) {
                n19 = remain;
            }
            if (cursor2 >= n19) {
                cursor2 = n19 - 1;
            }
            spellNameMarqueeTick = 0;
            spellCommentScrollTick = 0;
        } else if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
            cursorLR = (cursorLR + 1) % pageCount;
            remain = total - cursorLR * perPage;
            n19 = perPage;
            if (n19 > remain) {
                n19 = remain;
            }
            if (cursor2 >= n19) {
                cursor2 = n19 - 1;
            }
            spellNameMarqueeTick = 0;
            spellCommentScrollTick = 0;
        } else if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
            int spellId = start + cursor2 + cursorLR * perPage;
            if (spellId < start) {
                spellId = start;
            }
            if (spellId >= end) {
                spellId = end - 1;
            }
            if (!isSelectableInPracticeList(spellId)) {
                return null;
            }
            if (chara == 2 && !UnlockFlags.isAliceUnlocked()) {
                return null;
            }
            return new Result(Result.KIND_START_PRACTICE, chara, type, cursorUD, spellId);
        }

        return null;
    }

    public void render(Graphics g, ImageBank imgs, BulletSprites sprites) {
        if (g == null) {
            return;
        }

        int bg0 = UiDraw.mixColor(effect8, effect9, effect5, 5);
        int bg1 = UiDraw.mixColor(effectA, effectB, effect5, 5);
        UiDraw.drawGradation(g, 0, 0, 240, 240, bg0, bg1, 32);

        int n10 = 70;
        if (sprites != null) {
            if (chara == 0) {
                sprites.draw(g, 480, n10 + 6, 180);
            } else if (chara == 1) {
                sprites.draw(g, 481, n10 + 10, 180);
            } else {
                sprites.draw(g, 482, n10 + 2, 180);
            }
        }

        Font font = null;

        drawCharaInfo(g, font);

        if (scene == 0) {
            UiDraw.fillRectAlpha(g, 0, 10 - effect1, 240, 28, 0x000000, 128);
        } else {
            UiDraw.fillRectAlpha(g, 0, 0, 240, 240, 0x000000, 128);
        }

        UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_TITLE), 120, 22 - effect1, 1, 0xFF4F4F, 0x771F1F);

        if (scene == 0) {
            UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_HINT), 130, 34 - effect1, 1, 0xFF4F4F, 0x771F1F);

            UiDraw.fillRectAlpha(g, 97 + effect1, 58, 134, 145, 0x000000, 128);

            int y = 70;
            for (int i = 0; i < STAGE_NAMES.length; i++) {
                int cMain;
                int cShadow;
                if (i == cursorUD) {
                    cMain = 0xFFFFFF;
                    cShadow = 0x7F7F7F;
                } else {
                    cMain = 0xB0B0B0;
                    cShadow = 0x404040;
                }

                UiDraw.drawString2(g, font, STAGE_NAMES[i], 100 + effect1, y, 0, cMain, cShadow);

                int unit = (chara << 1) + type;
                int captured = 0;
                int start = STAGE_RANGES[i][0];
                int end = STAGE_RANGES[i][1];
                for (int id = start; id < end; id++) {
                    if (unit >= 0 && unit < GameProgress.UNIT_COUNT && GameProgress.INSTANCE.scBonusCnt[unit][id] != 0) {
                        captured++;
                    }
                }

                int total = end - start;
                String s;
                if (total < 10) {
                    s = captured + "/ " + total;
                } else {
                    s = captured + "/" + total;
                }
                UiDraw.drawString2(g, font, s, 228 + effect1, y, 2, cMain, cShadow);

                if (captured == total) {
                    UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_STAR), 85 + effect1, y, 0, cMain, cShadow);
                }
                y += 14;
            }

            int cMain = 0xFF0000;
            int cShadow = 0x7F0000;
            UiDraw.drawString2(g, font, CHARA_EN[chara], 100 + effect1, y, 0, cMain, cShadow);
            int unit = (chara << 1) + type;
            int capturedUnit = 0;
            if (unit >= 0 && unit < GameProgress.UNIT_COUNT) {
                int[] bonusArr = GameProgress.INSTANCE.scBonusCnt[unit];
                for (int i = 0; i < SPELL_COUNT; i++) {
                    if (bonusArr[i] != 0) {
                        capturedUnit++;
                    }
                }
            }
            UiDraw.drawString2(g, font, capturedUnit + "/" + SPELL_COUNT, 228 + effect1, y, 2, cMain, cShadow);
            if (capturedUnit == SPELL_COUNT) {
                UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_STAR), 85 + effect1, y, 0, cMain, cShadow);
            }
            y += 16;
            UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_ALL), 100 + effect1, y, 0, cMain, cShadow);
            int capturedAll = 0;
            for (int i = 0; i < SPELL_COUNT; i++) {
                if (sumBonusAll[i] != 0) {
                    capturedAll++;
                }
            }
            UiDraw.drawString2(g, font, capturedAll + "/" + SPELL_COUNT, 228 + effect1, y, 2, cMain, cShadow);
            if (capturedAll == SPELL_COUNT) {
                UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_STAR), 85 + effect1, y, 0, cMain, cShadow);
            }
            return;
        }

        UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_CHOOSE_CARD), 130, 34 - effect1, 1, 0xFF4F4F, 0x771F1F);

        int start = STAGE_RANGES[cursorUD][0];
        int end = STAGE_RANGES[cursorUD][1];
        int perPage = 6;
        int pageCount = (end - start + perPage - 1) / perPage;
        if (cursorLR < 0) {
            cursorLR = 0;
        }
        if (cursorLR >= pageCount) {
            cursorLR = pageCount - 1;
        }

        UiDraw.drawString2(g, font, String.valueOf(cursorLR + 1) + "/" + pageCount, 220 + effect1, 40, 2, 0xFF3332, 0x772222);
        int unit = (chara << 1) + type;
        int capturedStage = 0;
        for (int id = start; id < end; id++) {
            if (unit >= 0 && unit < GameProgress.UNIT_COUNT && GameProgress.INSTANCE.scBonusCnt[unit][id] != 0) {
                capturedStage++;
            }
        }
        UiDraw.drawString2(g, font, capturedStage + "/" + (end - start), 20 - effect1, 60, 0, 0xDDDDDD, 0x444444);

        int y = 96;
        for (int i = 0; i < 6; i++) {
            int id = start + i + cursorLR * 6;
            if (id >= end) {
                break;
            }

            int cMain;
            int cShadow;
            if (i == cursor2) {
                cMain = 0xFFFFFF;
                cShadow = 0x7F7F7F;
            } else {
                cMain = 0xB0B0B0;
                cShadow = 0x404040;
            }

            boolean showName = false;
            if (id >= 0 && id < SPELL_COUNT) {
                showName = (sumChareAll[id] != 0);
                if (!showName && id >= 107 && id <= 115) {
                    showName = isLastWordUnlockedCustom(id);
                }
            }
            String name = showName ? getSpellName(id) : UNKNOWN_NAME;

            int b = 0;
            int s = 0;
            if (unit >= 0 && unit < GameProgress.UNIT_COUNT) {
                b = GameProgress.INSTANCE.scBonusCnt[unit][id];
                s = GameProgress.INSTANCE.scChareCnt[unit][id];
            }

            String bsText = b + "/" + s;
            int bsW = UiDraw.stringWidth(font, bsText);
            int nameX = 20 + effect1;
            int numX = 235 + effect1;
            int clipW = (numX - bsW - 6) - nameX;
            if (clipW < 1) {
                clipW = 1;
            }

            if (i == cursor2) {
                UiDraw.drawStringMarquee2(g, font, name, nameX, y, clipW, 0, cMain, cShadow, spellNameMarqueeTick);
            } else {
                UiDraw.drawString2(g, font, ellipsizeToWidth(font, name, clipW), nameX, y, 0, cMain, cShadow);
            }
            UiDraw.drawString2(g, font, bsText, 235 + effect1, y, 2, cMain, cShadow);
            if (b != 0) {
                UiDraw.drawString2(g, font, UiText.get(TextId.SPELL_PRACTICE_STAR), 5 - effect1, y, 0, cMain, cShadow);
            }

            g.setColor(cShadow);
            g.drawLine(20 + effect1, y + 4, 235 + effect1, y + 4);

            y += 18;
        }

        int commentY = 208 + effect1;
        int selectedId = start + cursor2 + cursorLR * 6;
        if (selectedId < start) {
            selectedId = start;
        }
        if (selectedId >= end) {
            selectedId = end - 1;
        }
        String comment = (selectedId >= 0 && selectedId < SPELL_COUNT) ? getSpellComment(selectedId) : "";
        if (comment == null) {
            comment = "";
        }

        boolean showComment = (selectedId >= 0 && selectedId < SPELL_COUNT && sumBonusAll[selectedId] != 0);
        if (selectedId != spellCommentCacheSpellId || showComment != spellCommentCacheVisible || comment != spellCommentCacheText) {
            spellCommentCacheSpellId = selectedId;
            spellCommentCacheVisible = showComment;
            spellCommentCacheText = comment;
            spellCommentCacheLines = null;
            spellCommentScrollTick = 0;
        }

        final int commentX = 12;
        final int commentW = 216;
        final int lineH = 16;
        final int visibleLines = 2;
        final int visibleH = visibleLines * lineH;

        if (showComment) {
            if (spellCommentCacheLines == null) {
                spellCommentCacheLines = wrapTextToWidth(font, comment, commentW);
            }

            int offY = calcPingPongScrollOffsetPx(spellCommentScrollTick, (spellCommentCacheLines.length - visibleLines) * lineH);

            int oldClipX = g.getClipX();
            int oldClipY = g.getClipY();
            int oldClipW = g.getClipWidth();
            int oldClipH = g.getClipHeight();
            try {
                g.setClip(commentX, commentY - UiDraw.fontBaseline(font), commentW, visibleH);
                for (int i = 0; i < spellCommentCacheLines.length; i++) {
                    UiDraw.drawString2(g, font, spellCommentCacheLines[i], commentX, commentY + i * lineH - offY, 0, 0xFF0000, 0x7F0000);
                }
            } catch (Throwable t) {
            } finally {
                try {
                    g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
                } catch (Throwable ignore) {
                }
            }
        } else {
            int n48 = comment.length();
            if (n48 < 0) {
                n48 = 0;
            }
            String mask = repeatFullWidthQ(n48);
            String[] lines = wrapTextToWidth(font, mask, commentW);
            if (lines.length > 0) {
                UiDraw.drawString2(g, font, lines[0], commentX, commentY + 0, 0, 0xFF0000, 0x7F0000);
            }
            if (lines.length > 1) {
                UiDraw.drawString2(g, font, lines[1], commentX, commentY + 16, 0, 0xFF0000, 0x7F0000);
            }
        }
    }

    // Auto-wrap comment text by pixel width (not fixed character count).
    private static String[] wrapTextToWidth(Font font, String s, int maxW) {
        if (s == null) {
            s = "";
        }
        if (maxW <= 0) {
            return new String[] { "" };
        }
        if (s.length() == 0) {
            return new String[] { "" };
        }

        String[] out = new String[0];
        StringBuffer line = new StringBuffer();
        int lineW = 0;
        int i = 0;

        while (i < s.length()) {
            char c = s.charAt(i);
            boolean isWs = (c == ' ' || c == '\t');
            int j = i;

            if (isWs) {
                while (j < s.length()) {
                    char wc = s.charAt(j);
                    if (wc != ' ' && wc != '\t') {
                        break;
                    }
                    j++;
                }
                if (lineW == 0) {
                    i = j;
                    continue;
                }
                String token = s.substring(i, j);
                int tokenW = UiDraw.stringWidth(font, token);
                if (lineW + tokenW > maxW) {
                    out = appendLine(out, line.toString());
                    line.setLength(0);
                    lineW = 0;
                    i = j;
                    continue;
                }
                line.append(token);
                lineW += tokenW;
                i = j;
                continue;
            }

            while (j < s.length()) {
                char wc = s.charAt(j);
                if (wc == ' ' || wc == '\t') {
                    break;
                }
                j++;
            }

            String word = s.substring(i, j);
            int wordW = UiDraw.stringWidth(font, word);
            if (lineW > 0 && lineW + wordW > maxW) {
                out = appendLine(out, line.toString());
                line.setLength(0);
                lineW = 0;
            }

            if (wordW <= maxW) {
                line.append(word);
                lineW += wordW;
                i = j;
                continue;
            }

            for (int k = 0; k < word.length(); k++) {
                char ch = word.charAt(k);
                int chW = UiDraw.charWidth(font, ch);
                if (lineW > 0 && lineW + chW > maxW) {
                    out = appendLine(out, line.toString());
                    line.setLength(0);
                    lineW = 0;
                }
                line.append(ch);
                lineW += chW;
            }
            i = j;
        }

        if (line.length() > 0) {
            out = appendLine(out, line.toString());
        }
        if (out.length == 0) {
            return new String[] { "" };
        }
        return out;
    }

    private static String[] appendLine(String[] arr, String s) {
        int n = (arr != null) ? arr.length : 0;
        String[] out = new String[n + 1];
        for (int i = 0; i < n; i++) {
            out[i] = arr[i];
        }
        out[n] = (s != null) ? s : "";
        return out;
    }

    private static int calcPingPongScrollOffsetPx(int tick, int rangePx) {
        if (rangePx <= 0) {
            return 0;
        }
        int pause = 18;
        int step = (tick >= 0) ? (tick >> 1) : 0;
        int period = pause + rangePx + pause + rangePx;
        int m = (period > 0) ? (step % period) : 0;
        if (m < pause) {
            return 0;
        }
        if (m < pause + rangePx) {
            return m - pause;
        }
        if (m < pause + rangePx + pause) {
            return rangePx;
        }
        return rangePx - (m - (pause + rangePx + pause));
    }

    private static String repeatFullWidthQ(int count) {
        if (count <= 0) {
            return "";
        }
        String s = "";
        for (int i = 0; i < count; i++) {
            s += "\uff1f";
        }
        return s;
    }

    private static String ellipsizeToWidth(Font font, String s, int maxW) {
        if (s == null) {
            return "";
        }
        if (maxW <= 0) {
            return "";
        }
        if (UiDraw.stringWidth(font, s) <= maxW) {
            return s;
        }

        String ell = "...";
        int ellW = UiDraw.stringWidth(font, ell);
        if (ellW >= maxW) {
            return "";
        }

        int limitW = maxW - ellW;
        int w = 0;
        int cut = 0;
        for (int i = 0; i < s.length(); i++) {
            int cw = UiDraw.charWidth(font, s.charAt(i));
            if (w + cw > limitW) {
                break;
            }
            w += cw;
            cut = i + 1;
        }
        if (cut <= 0) {
            return "";
        }
        return s.substring(0, cut) + ell;
    }

    private static void ensureSpellTextsLoaded() {
        if (spellTextLoaded) {
            return;
        }

        // Load localized spell texts first; fallback to the original built-in tables.
        String nameText = I18n.readUtf8TextResource(I18n.path("spcard.dat"));
        spellNames = (nameText != null) ? I18n.splitLinesSimple(nameText) : null;
        String commentText = I18n.readUtf8TextResource(I18n.path("sprac.dat"));
        spellComments = (commentText != null) ? I18n.splitLinesSimple(commentText) : null;
        spellTextLoaded = true;
    }

    private static String getSpellName(int id) {
        if (spellNames != null && id >= 0 && id < spellNames.length) {
            String s = spellNames[id];
            if (s != null && s.length() > 0) {
                return s;
            }
        }
        return UiText.get(TextId.REPLAY_SPELLNO_PREFIX) + (id + 1);
    }

    private static String getSpellComment(int id) {
        if (spellComments != null && id >= 0 && id < spellComments.length) {
            return spellComments[id];
        }
        return "";
    }

    private void drawCharaInfo(Graphics g, Font font) {
        if (chara < 0 || chara >= CHARA_INFO.length) {
            return;
        }

        int main0 = 0xFFCFCF;
        int sh0 = 0x000000;
        int main1 = 0xFFAFAF;
        int sh1 = 0x000000;
        int main2 = 0x7FFF7F;
        int sh2 = 0x003F00;

        if (chara == 1) {
            main0 = 0x7F7F7F;
            sh0 = 0x000000;
            main1 = 0x3F3F3F;
            sh1 = 0x000000;
            main2 = 0x007F00;
            sh2 = 0x000000;
        } else if (chara == 2) {
            main0 = 0xBFBFFF;
            sh0 = 0x00001F;
            main1 = 0x8F8FFF;
            sh1 = 0x00002F;
        }

        int y = 200;
        UiDraw.drawStringOutline(g, font, CHARA_INFO[chara][0], 10, y + 0, 0, main0, sh0);
        UiDraw.drawStringOutline(g, font, CHARA_INFO[chara][1], 10, y + 15, 0, main1, sh1);
        UiDraw.drawStringOutline(g, font, CHARA_INFO[chara][2 + type], 10, y + 30, 0, main2, sh2);
    }

    // Shared effect update when chara selection changes (LEFT/RIGHT).
    private void applyCharaChangeEffects() {
        effect6 = 0;
        effect5 = 0;
        effect9 = effect8;
        effectB = effectA;
        switch (chara) {
            case 0:
                effect8 = 0xFFFFFF;
                effectA = 0xFF0000;
                break;
            case 1:
                effect8 = 0x000000;
                effectA = 0xFFFFFF;
                break;
            default:
                effect8 = 0x0000FF;
                effectA = 0x000000;
                break;
        }
    }

}
