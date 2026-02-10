package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.BulletSprites;
import touhou.ImageBank;
import touhou.i18n.TextId;
import touhou.i18n.UiText;
import touhou.replay.ReplayBossSnapshotStore;
import touhou.replay.ReplayHeader;
import touhou.replay.ReplayRmsStore;
import touhou.replay.ReplayStageSnapshotStore;

public final class ReplayScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_TITLE = 1;
        public static final int KIND_START_REPLAY = 2;
        public static final int KIND_BT_SHARE = 3;
        public static final int KIND_OPEN_BT_RECEIVE = 4;
        public static final int KIND_OPEN_BT_SEND = 5;

        public final int kind;
        public final int slot;
        public final boolean bossOnly;
        public final boolean btReceive;

        public Result(int kind) {
            this.kind = kind;
            this.slot = -1;
            this.bossOnly = false;
            this.btReceive = false;
        }

        public Result(int kind, int slot, boolean bossOnly) {
            this.kind = kind;
            this.slot = slot;
            this.bossOnly = bossOnly;
            this.btReceive = false;
        }

        public Result(int kind, int slot, boolean bossOnly, boolean btReceive) {
            this.kind = kind;
            this.slot = slot;
            this.bossOnly = bossOnly;
            this.btReceive = btReceive;
        }
    }

    // Cache fonts to avoid per-frame allocations.
    private static final Font FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private static final Font FONT_M = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);

    private static final int SCENE_LIST = 0;
    private static final int SCENE_ACTION = 1;
    private static final int SCENE_DELETE_CONFIRM = 2;
    private static final int SCENE_MESSAGE = 3;

    public void enter() {
        scene = SCENE_LIST;
        cursor = 0;
        actionCursor = 0;
        msgCounter = 0;
        msg = null;
        decoTick = 0;
        reloadSlots();
    }

    public void refreshSlotsKeepCursor() {
        int oldCursor = cursor;
        reloadSlots();
        if (oldCursor < 0) {
            oldCursor = 0;
        }
        if (oldCursor >= ReplayRmsStore.SLOT_COUNT) {
            oldCursor = ReplayRmsStore.SLOT_COUNT - 1;
        }
        cursor = oldCursor;
    }

    public Result update(int pressed) {
        decoTick++;
        if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            if (scene == SCENE_LIST) {
                return new Result(Result.KIND_BACK_TO_TITLE);
            }
            scene = SCENE_LIST;
            msgCounter = 0;
            msg = null;
            return null;
        }

        if (scene == SCENE_LIST) {
            if ((pressed & GameCanvas.GAME_B_PRESSED) != 0) {
                return new Result(Result.KIND_OPEN_BT_RECEIVE);
            }
        }

        if (scene == SCENE_MESSAGE) {
            if ((pressed & (GameCanvas.FIRE_PRESSED | GameCanvas.GAME_A_PRESSED)) != 0) {
                scene = SCENE_ACTION;
                msgCounter = 0;
                msg = null;
                updateActionCache();
            }
            return null;
        }

        if (scene == SCENE_DELETE_CONFIRM) {
            if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                ReplayRmsStore.clearSlot(cursor);
                ReplayBossSnapshotStore.clearSlot(cursor);
                ReplayStageSnapshotStore.clearSlot(cursor);
                actionCursor = ACTION_BACK;
                reloadSlots();
                showMessage(UiText.get(TextId.REPLAY_MSG_DELETED));
            }
            return null;
        }

        if (scene == SCENE_ACTION) {
            boolean moved = false;
            int moveDir = 1;
            if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
                // Grid navigation: Down (wrap within column 0-2 or 3-5)
                int col = actionCursor / 3;
                int row = actionCursor % 3;
                row = (row + 1) % 3;
                actionCursor = col * 3 + row;
                moved = true;
                moveDir = 1;
            } else if ((pressed & GameCanvas.UP_PRESSED) != 0) {
                // Grid navigation: Up
                int col = actionCursor / 3;
                int row = actionCursor % 3;
                row = (row + 2) % 3; // -1 mod 3
                actionCursor = col * 3 + row;
                moved = true;
                moveDir = -1;
            } else if ((pressed & GameCanvas.LEFT_PRESSED) != 0 || (pressed & GameCanvas.RIGHT_PRESSED) != 0) {
                // Switch column
                actionCursor = (actionCursor + 3) % 6;
                moved = true;
                moveDir = 1;
            } else if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                Result r = handleAction();
                if (r != null) {
                    return r;
                }
            }

            if (moved) {
                // Skip disabled actions (e.g., Play (Boss) when unavailable).
                ensureActionCursorEnabled(moveDir);
            }
            return null;
        }

        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursor = (cursor + 1) % ReplayRmsStore.SLOT_COUNT;
        } else if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            cursor = (cursor - 1 + ReplayRmsStore.SLOT_COUNT) % ReplayRmsStore.SLOT_COUNT;
        } else if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
            scene = SCENE_ACTION;
            actionCursor = 0;
            updateActionCache();
            // Place cursor on the first enabled action.
            ensureActionCursorEnabled(1);
        }
        return null;
    }

    public void render(Graphics g, ImageBank imgs, BulletSprites sprites) {
        if (g == null) {
            return;
        }

        if (imgs != null) {
            this.imgs = imgs;
        }
        if (sprites != null) {
            this.sprites = sprites;
        }

        Image bg = (this.imgs != null) ? this.imgs.get(18) : null;
        if (bg != null) {
            UiDraw.drawRegion(g, bg, 0, 0, 0, 0, 240, 240);
            UiDraw.fillRectAlpha(g, 0, 0, 240, 240, 0x000000, 96);
        } else {
            g.setColor(0x000000);
            g.fillRect(0, 0, 240, 240);
        }

        g.setColor(0x005500);
        g.drawRect(3, 3, 234, 234);
        g.setColor(0x00AA00);
        g.drawRect(6, 6, 228, 228);
        g.setColor(0x00FF00);
        g.drawRect(9, 9, 222, 222);

        if (scene != SCENE_ACTION) {
            UiDraw.drawString2(g, FONT, UiText.get(TextId.REPLAY_TITLE), 120, 25, 1, 0xFFFFFF, 0x777777);
        }

        if (scene == SCENE_LIST) {
            renderList(g, FONT);
        } else if (scene == SCENE_ACTION) {
            renderAction(g, FONT);
        } else if (scene == SCENE_DELETE_CONFIRM) {
            renderDeleteConfirm(g, FONT);
        } else {
            renderMessage(g, FONT);
        }
    }

    private static final int ACTION_PLAY = 0;
    private static final int ACTION_PLAY_BOSS = 1;
    private static final int ACTION_DELETE = 2;
    private static final int ACTION_NET = 3;
    private static final int ACTION_BT = 4;
    private static final int ACTION_BACK = 5;

    private int scene;
    private int cursor;
    private int actionCursor;

    // UI effect counter for lightweight decorations.
    private int decoTick;

    private ImageBank imgs;
    private BulletSprites sprites;

    // Cache derived strings for the selected slot to avoid per-frame allocations.
    private int cachedSlot;
    private boolean cachedHasData;
    private String cachedName;
    private String cachedUnit;
    private String cachedDetail;
    private String cachedScore;
    private boolean cachedSpellPractice;
    private boolean cachedHasBoss;
    private boolean cachedImported;

    private final ReplayHeader[] slotHeaders = new ReplayHeader[ReplayRmsStore.SLOT_COUNT];
    private final byte[][] slotNames = new byte[ReplayRmsStore.SLOT_COUNT][ReplayRmsStore.NAME_SIZE];
    private final byte[] tmpFull = new byte[ReplayRmsStore.DATA_SIZE];
    private final byte[] tmpBoss = new byte[ReplayRmsStore.DATA_SIZE];

    private String msg;
    private int msgCounter;

    private final int[] tmpSpriteMetrics = new int[4];

    private void reloadSlots() {
        for (int i = 0; i < ReplayRmsStore.SLOT_COUNT; i++) {
            if (slotHeaders[i] == null) {
                slotHeaders[i] = new ReplayHeader();
            }
        }

        ReplaySaveUi.loadAllSlots(slotHeaders, slotNames, tmpFull, tmpBoss);

        cachedSlot = -1;
    }

    private void updateActionCache() {
        if (cachedSlot == cursor) {
            return;
        }
        cachedSlot = cursor;

        ReplayHeader h = slotHeaders[cursor];
        cachedHasData = (h != null && h.getFlag() != 0);

        cachedName = ReplaySaveUi.decodeSlotName(slotNames[cursor]);
        cachedSpellPractice = (h != null && h.getMode() == 3);

        if (!cachedHasData || h == null) {
            cachedUnit = "";
            cachedDetail = "";
            cachedScore = "";
            cachedHasBoss = false;
            cachedImported = false;
            return;
        }

        cachedUnit = ReplaySaveUi.formatUnit(h);
        cachedDetail = ReplaySaveUi.formatDetail(h);
        cachedScore = String.valueOf(h.getScore());
        cachedHasBoss = (!cachedSpellPractice && ReplaySaveUi.hasBossOnly(cursor, h));
        cachedImported = (h.getFlag() == ReplayHeader.FLAG_IMPORTED);
    }

    private Result handleAction() {
        int act = actionCursor;

        if (act == ACTION_BACK) {
            scene = SCENE_LIST;
            return null;
        }

        if (act == ACTION_DELETE) {
            if (!isActionEnabled(ACTION_DELETE)) return null;
            scene = SCENE_DELETE_CONFIRM;
            return null;
        }

        if (act == ACTION_NET) {
            if (!isActionEnabled(act)) return null;
            showMessage(UiText.get(TextId.REPLAY_MSG_NOT_IMPLEMENTED));
            return null;
        }

        if (act == ACTION_BT) {
            if (!isActionEnabled(act)) return null;
            return new Result(Result.KIND_OPEN_BT_SEND, cursor, false, false);
        }

        ReplayHeader h = slotHeaders[cursor];
        boolean hasData = h != null && h.getFlag() != 0;
        if (!hasData) {
            showMessage(UiText.get(TextId.REPLAY_MSG_EMPTY_SLOT));
            return null;
        }

        if (act == ACTION_PLAY_BOSS) {
            if (!isActionEnabled(ACTION_PLAY_BOSS)) return null;
            if (h.getBossStartFrame() < 0) {
                showMessage(UiText.get(TextId.REPLAY_MSG_NO_BOSS_SEGMENT));
                return null;
            }
            if (!ReplayBossSnapshotStore.hasSlot(cursor)) {
                showMessage(UiText.get(TextId.REPLAY_MSG_NO_BOSS_SNAPSHOT));
                return null;
            }
            return new Result(Result.KIND_START_REPLAY, cursor, true);
        }
        if (act == ACTION_PLAY) {
            return new Result(Result.KIND_START_REPLAY, cursor, false);
        }

        showMessage(UiText.get(TextId.REPLAY_MSG_UNKNOWN_ACTION));
        return null;
    }

    private void showMessage(String s) {
        msg = s;
        msgCounter = 30;
        scene = SCENE_MESSAGE;
    }

    public void showMessageExternal(String s) {
        showMessage(s);
    }

    private void renderList(Graphics g, Font font) {
        ReplaySaveUi.renderSlotListCompact(g, font, cursor, slotHeaders, UiText.get(TextId.COMMON_SLOT_PREFIX), false);
    }

    private void renderAction(Graphics g, Font font) {
        updateActionCache();

        int cardX = 16;
        int cardY = 28;
        int cardW = 208;
        int cardH = 120;

        UiDraw.fillRectAlpha(g, cardX, cardY, cardW, cardH, 0x000000, 160);
        g.setColor(0x005500);
        g.drawRect(cardX, cardY, cardW - 1, cardH - 1);
        g.setColor(0x00FF00);
        g.drawRect(cardX + 1, cardY + 1, cardW - 3, cardH - 3);

        UiDraw.fillRectAlpha(g, cardX + 2, cardY + 2, 4, cardH - 4, 0x00AA00, 128);

        // Decorative snowflakes (use sprite frames to avoid per-frame allocations).
        drawSnow(g, cardX + cardW - 16, cardY + 18, 96);
        drawSnow(g, cardX + 18, cardY + cardH - 10, 72);

        int leftX = cardX + 12;
        int rightX = cardX + cardW - 12;

        int y0 = cardY + 16;

        // Draw individual tag badges at top-right (no redundant "ST" mode label).
        int badgeX = rightX;
        if (cachedSpellPractice) {
            badgeX -= drawModeBadge(g, font, UiText.get(TextId.REPLAY_TAG_SP), badgeX, y0) + 4;
        }
        if (cachedHasBoss) {
            badgeX -= drawModeBadge(g, font, UiText.get(TextId.REPLAY_TAG_ST), badgeX, y0) + 4;
        }
        if (cachedImported) {
            badgeX -= drawModeBadge(g, font, UiText.get(TextId.REPLAY_TAG_IMPORTED), badgeX, y0) + 4;
        }

        if (cachedHasData) {
            ReplayHeader h = slotHeaders[cursor];
            if (!cachedSpellPractice) {
                // Draw difficulty icon at top-left, clamp inside card bounds.
                drawIconInCard(g, difficultyToBulletId(h.getDifficulty()), 0, cardX, cardY, cardW, cardH);
            } else {
                // Spell practice replay: draw replay kind icon (LastWord vs normal).
                int cropR = isLastWordSpell(h.getSpellId()) ? 0 : 10;
                drawIconInCard(g, 190, cropR, cardX, cardY, cardW, cardH);
            }

            // Name / Detail line
            String nameLine = cachedName;
            if (nameLine == null || nameLine.length() == 0) {
                if (!cachedSpellPractice && h != null) {
                    nameLine = ReplaySaveUi.stageName(h.getStage());
                } else {
                    nameLine = cachedDetail;
                }
            }
            int titleClipX = cardX + 16;
            int titleClipW = cardW - 32;
            if (!cachedSpellPractice) {
                titleClipX = cardX + 34;
                titleClipW = cardW - 52;
            }
            UiDraw.drawStringMarquee2(g, FONT_M, nameLine, titleClipX, y0 + 24, titleClipW, 1, 0xFFFFFF, 0x222222, decoTick);

            // Unit line (removed duplicate cachedDetail)
            UiDraw.drawString2(g, font, cachedUnit, leftX, y0 + 46, 0, 0xDDDDDD, 0x333333);

            g.setColor(0x005500);
            g.drawLine(cardX + 8, cardY + 84, cardX + cardW - 8, cardY + 84);

            UiDraw.drawString2(g, font, UiText.get(TextId.REPLAY_LABEL_SCORE), leftX, y0 + 80, 0, 0xAAAAAA, 0x333333);
            UiDraw.drawString2(g, FONT_M, cachedScore, rightX, y0 + 80, 2, 0xFFFFFF, 0x222222);
        } else {
            UiDraw.drawString2(g, FONT_M, UiText.get(TextId.REPLAY_LABEL_EMPTY), cardX + (cardW >> 1), y0 + 34, 1, 0xBBBBBB, 0x333333);
        }

        // Action buttons (3x2 Grid).
        int btnW = 96;
        int btnH = 20;
        int btnYBase = 168;
        int btnGapY = 22;
        int col1X = 22;
        int col2X = 124;

        for (int i = 0; i < 6; i++) {
            boolean sel = (i == actionCursor);
            int col = (i >= 3) ? 1 : 0;
            int row = i % 3;
            int bx = (col == 0) ? col1X : col2X;
            int by = btnYBase + row * btnGapY;

            boolean enabled = isActionEnabled(i);
            int mainColor = enabled ? (sel ? 0xFFFFFF : 0xCCCCCC) : 0x555555;
            int shadowColor = enabled ? (sel ? 0x224422 : 0x222222) : 0x111111;
            int borderColor = enabled ? (sel ? 0x00FF00 : 0x005500) : 0x333333;

            UiDraw.fillRectAlpha(g, bx, by - 12, btnW, btnH, sel ? 0x001A00 : 0x000000, sel ? 224 : 128);
            g.setColor(borderColor);
            g.drawRect(bx, by - 12, btnW - 1, btnH - 1);

            String label = actionLabel(i);
            UiDraw.drawString2(g, font, label, bx + (btnW >> 1), by, 1, mainColor, shadowColor);
        }
    }

    private boolean isActionEnabled(int act) {
        if (act == ACTION_BACK) {
            return true;
        }
        if (act == ACTION_BT) {
            return cachedHasData;
        }
        if (!cachedHasData) {
            return false;
        }
        if (act == ACTION_PLAY_BOSS) {
            if (cachedSpellPractice) {
                return false;
            }
            ReplayHeader h = slotHeaders[cursor];
            return h != null && ReplaySaveUi.hasBossOnly(cursor, h);
        }
        return true;
    }

    private void ensureActionCursorEnabled(int dir) {
        if (isActionEnabled(actionCursor)) {
            return;
        }

        int step = (dir >= 0) ? 1 : -1;
        int start = actionCursor;
        for (int i = 0; i < 6; i++) {
            actionCursor = (actionCursor + step + 6) % 6;
            if (isActionEnabled(actionCursor)) {
                return;
            }
            if (actionCursor == start) {
                break;
            }
        }
        actionCursor = ACTION_BACK;
    }

    private String actionLabel(int act) {
        if (act == ACTION_PLAY) {
            return UiText.get(TextId.REPLAY_ACTION_PLAY);
        }
        if (act == ACTION_PLAY_BOSS) {
            return UiText.get(TextId.REPLAY_ACTION_PLAY_BOSS);
        }
        if (act == ACTION_DELETE) {
            return UiText.get(TextId.REPLAY_ACTION_DELETE);
        }
        if (act == ACTION_NET) {
            return UiText.get(TextId.REPLAY_ACTION_NET_SHARE);
        }
        if (act == ACTION_BT) {
            return UiText.get(TextId.REPLAY_ACTION_BT_SHARE);
        }
        return UiText.get(TextId.REPLAY_ACTION_BACK);
    }

    private static int difficultyToBulletId(int d) {
        if (d >= 0 && d <= 3) return 185 + d;
        if (d == 4) return 189;
        return -1;
    }

    private static boolean isLastWordSpell(int spellId) {
        return spellId >= 107 && spellId <= 115;
    }

    // Draw a sprite icon inside the replay detail card, clamped within card bounds.
    private void drawIconInCard(Graphics g, int bulletId, int cropRight, int cardX, int cardY, int cardW, int cardH) {
        if (sprites == null || g == null || bulletId < 0) return;

        int[] m = tmpSpriteMetrics;
        if (!sprites.getSizeAndAnchor(bulletId, m)) {
            sprites.drawAlphaCropRight(g, bulletId, cardX + 10, cardY + 10, 200, cropRight);
            return;
        }

        int w = m[0] - (cropRight > 0 ? cropRight : 0);
        int hh = m[1];
        int ax = m[2];
        int ay = m[3];
        if (w <= 0 || hh <= 0) return;

        int dx = cardX + 10;
        int dy = cardY + 10;
        if (dx < cardX + 2) dx = cardX + 2;
        if (dy < cardY + 2) dy = cardY + 2;
        if (dx > cardX + cardW - 2 - w) dx = cardX + cardW - 2 - w;
        if (dy > cardY + cardH - 2 - hh) dy = cardY + cardH - 2 - hh;

        sprites.drawAlphaCropRight(g, bulletId, dx + ax, dy + ay, 200, cropRight);
    }

    // Returns the badge width so callers can stack badges right-to-left.
    private int drawModeBadge(Graphics g, Font font, String s, int xRight, int y) {
        if (font == null || s == null) {
            return 0;
        }
        int w = UiDraw.stringWidth(font, s) + 10;
        int h = UiDraw.fontHeight(font) + 2;
        int x = xRight - w;
        int yTop = y - UiDraw.fontBaseline(font);

        UiDraw.fillRectAlpha(g, x, yTop, w, h, 0x001A00, 200);
        g.setColor(0x00FF00);
        g.drawRect(x, yTop, w - 1, h - 1);
        UiDraw.drawString2(g, font, s, x + (w >> 1), y, 1, 0xFFFFFF, 0x224422);
        return w;
    }

    private void drawSnow(Graphics g, int x, int y, int alpha) {
        if (sprites == null) {
            return;
        }
        int frame = (decoTick >> 2) % 9;
        sprites.drawAlpha(g, 445 + frame, x, y, alpha);
    }

    private void renderDeleteConfirm(Graphics g, Font font) {
        g.setColor(0x000000);
        g.fillRect(24, 94, 192, 52);
        g.setColor(0x00FF00);
        g.drawRect(25, 95, 190, 50);

        int boxY = 94;
        int boxH = 52;
        int baseY = boxY + ((boxH - UiDraw.fontHeight(font)) >> 1) + UiDraw.fontBaseline(font);
        UiDraw.drawString2(g, font,
                UiText.get(TextId.REPLAY_DELETE_CONFIRM_PREFIX) + (cursor + 1) + UiText.get(TextId.REPLAY_DELETE_CONFIRM_SUFFIX),
                120, baseY, 1, 0xFFFFFF, 0x444444);
    }

    private void renderMessage(Graphics g, Font font) {
        if (msgCounter > 0) {
            msgCounter--;
        }

        g.setColor(0x000000);
        g.fillRect(24, 104, 192, 36);
        g.setColor(0x00FF00);
        g.drawRect(25, 105, 190, 34);

        String s = (msg != null) ? msg : "";
        int boxY = 104;
        int boxH = 36;
        int baseY = boxY + ((boxH - UiDraw.fontHeight(font)) >> 1) + UiDraw.fontBaseline(font);
        UiDraw.drawString2(g, font, s, 120, baseY, 1, 0xFFFFFF, 0x444444);
    }

    // Use ReplaySaveUi for list rendering and header formatting.
}