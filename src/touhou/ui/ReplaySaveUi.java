package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import touhou.i18n.I18n;
import touhou.i18n.TextId;
import touhou.i18n.UiText;
import touhou.replay.ReplayBossSnapshotStore;
import touhou.replay.ReplayHeader;
import touhou.replay.ReplayRmsStore;
import touhou.replay.ReplayStageSnapshotStore;
import touhou.replay.ReplayBossOnlyPolicy;

final class ReplaySaveUi {
    // Shared slot list renderer for both save overlays and replay playback menus.

    private static boolean spellTextLoaded;
    private static String[] spellNames;

    // Marquee tick for spell card names in list.
    private static int marqueeTick;

    private static boolean[] slotHasBossOnly;
    private static boolean[] slotBossSnapshotExists;
    private static boolean[] slotStageSnapshotExists;

    private ReplaySaveUi() {
    }

    static void loadAllSlots(ReplayHeader[] slotHeaders, byte[][] slotNames, byte[] tmpFull, byte[] tmpBoss) {
        ensureSlotBossOnlyCache();
        ensureSlotBossSnapshotCache();
        ensureSlotStageSnapshotCache();

        ReplayRmsStore.loadAllSlotHeadersAndNames(slotHeaders, slotNames);
        ReplayBossSnapshotStore.loadHasSlotFlags(slotBossSnapshotExists);
        ReplayStageSnapshotStore.loadHasSlotFlags(slotStageSnapshotExists);

        for (int i = 0; i < ReplayRmsStore.SLOT_COUNT; i++) {
            ReplayHeader h = slotHeaders[i];
            slotHasBossOnly[i] = computeHasBossOnlyForSlot(i, h);
        }
    }

    private static void ensureSlotBossSnapshotCache() {
        if (slotBossSnapshotExists == null || slotBossSnapshotExists.length != ReplayRmsStore.SLOT_COUNT) {
            slotBossSnapshotExists = new boolean[ReplayRmsStore.SLOT_COUNT];
        }
    }

    private static void ensureSlotStageSnapshotCache() {
        if (slotStageSnapshotExists == null || slotStageSnapshotExists.length != ReplayRmsStore.SLOT_COUNT) {
            slotStageSnapshotExists = new boolean[ReplayRmsStore.SLOT_COUNT];
        }
    }

    private static boolean computeHasBossOnlyForSlot(int slot, ReplayHeader h) {
        if (h == null || h.getFlag() == 0) {
            return false;
        }
        if (h.getBossStartFrame() < 0) {
            return false;
        }

        int stage = h.getStage();
        if (ReplayBossOnlyPolicy.isFastForwardBossOnlyStage(stage)) {
            return slotStageSnapshotExists != null
                    && slot >= 0
                    && slot < slotStageSnapshotExists.length
                    && slotStageSnapshotExists[slot];
        }

        return slotBossSnapshotExists != null
                && slot >= 0
                && slot < slotBossSnapshotExists.length
                && slotBossSnapshotExists[slot];
    }

    static void renderSlotListCompact(Graphics g, Font font, int cursor, ReplayHeader[] slotHeaders, String slotPrefix) {
        renderSlotListCompact(g, font, cursor, slotHeaders, slotPrefix, true);
    }

    static void renderSlotListCompact(Graphics g, Font font, int cursor, ReplayHeader[] slotHeaders, String slotPrefix, boolean drawTitle) {
        marqueeTick++;
        if (drawTitle) {
            UiDraw.drawString2(g, font, UiText.get(TextId.REPLAY_SLOTLIST_TITLE), 120, 25, 1, 0xFFFFFF, 0x777777);
        }

        int slotCount = ReplayRmsStore.SLOT_COUNT;
        int visible = (slotCount <= 4) ? slotCount : 4;
        int start = cursor - (visible / 2);
        if (start < 0) {
            start = 0;
        }
        if (start > slotCount - visible) {
            start = slotCount - visible;
        }

        int lineH = font.getHeight();
        int baselinePos = font.getBaselinePosition();
        int lineGap = 2;

        int firstBoxY = 49;
        int boxStepY = 40;
        for (int row = 0; row < visible; row++) {
            int i = start + row;
            boolean sel = (i == cursor);
            int main = sel ? 0xFFFFFF : 0x777777;
            int sh = sel ? 0x444444 : 0x222222;

            int boxX = 12;
            int boxY = firstBoxY + row * boxStepY;
            int boxW = 216;
            int boxH = 34;
            if (sel) {
                g.setColor(0x001A00);
                g.fillRect(boxX, boxY, boxW, boxH);
                g.setColor(0x00AA00);
                g.drawRect(boxX, boxY, boxW, boxH);
            }

            int totalTextH = lineH + lineGap + lineH;
            int padY = (boxH - totalTextH) >> 1;
            if (padY < 0) {
                padY = 0;
            }

            int line1BaseY = boxY + padY + baselinePos;
            int line2BaseY = line1BaseY + lineH + lineGap;

            UiDraw.drawString2(g, font, slotPrefix + (i + 1), 18, line1BaseY, 0, main, sh);

            ReplayHeader h = slotHeaders[i];
            if (h != null && h.getFlag() != 0) {
                // Line 1: unit + score (no tags, avoids overlap).
                UiDraw.drawString2(g, font, formatUnit(h), 80, line1BaseY, 0, main, sh);
                UiDraw.drawString2(g, font, String.valueOf(h.getScore()), boxX + boxW - 6, line1BaseY, 2, main, sh);

                // Line 2 right: compact tags (Boss/Imported/SP only).
                int tagMain = sel ? 0xA0FFA0 : 0x558855;
                int tagSh = sel ? 0x224422 : 0x112211;
                String listTags = formatListTags(i, h);
                int tagsW = 0;
                if (listTags.length() > 0) {
                    UiDraw.drawString2(g, font, listTags, boxX + boxW - 6, line2BaseY, 2, tagMain, tagSh);
                    tagsW = UiDraw.stringWidth(font, listTags) + 6;
                }

                // Line 2 left: detail or spell card name.
                int detailClipW = boxW - 12 - tagsW;
                if (detailClipW < 20) { detailClipW = 20; }

                if (h.getMode() == 3) {
                    int spellId = h.getSpellId();
                    String name = getSpellName(spellId);

                    int clipX = boxX + 6;
                    int nameX = clipX;
                    int nameW = detailClipW;
                    if (nameW > 0) {
                        int fastTick = marqueeTick + (marqueeTick >> 1);
                        UiDraw.drawStringMarquee2(g, font, name, nameX, line2BaseY, nameW, 0, main, sh, fastTick);
                    }
                } else {
                    String detail = formatDetail(h);
                    UiDraw.drawStringMarquee2(g, font, detail, boxX + 6, line2BaseY, detailClipW, 0, main, sh, marqueeTick);
                }
            } else {
                UiDraw.drawString2(g, font, "-", boxX + boxW - 6, line1BaseY, 2, main, sh);
            }
        }

        if (slotCount > visible) {
            int trackX = 232;
            int trackY = firstBoxY;
            int trackH = (visible - 1) * boxStepY + 34;

            g.setColor(0x00AA00);
            g.drawLine(trackX, trackY, trackX, trackY + trackH - 1);

            int denom = slotCount - visible;
            int knobH = (trackH * visible) / slotCount;
            if (knobH < 10) {
                knobH = 10;
            }
            if (knobH > trackH) {
                knobH = trackH;
            }
            int knobY = trackY;
            if (denom > 0) {
                knobY = trackY + ((trackH - knobH) * start) / denom;
            }

            g.setColor(0x001A00);
            g.fillRect(trackX - 2, knobY, 5, knobH);
            g.setColor(0x00FF00);
            g.drawRect(trackX - 2, knobY, 4, knobH - 1);
        }
    }

    static void renderSlotListFinal(Graphics g, Font font, int cursor, ReplayHeader[] slotHeaders, String slotPrefix) {
        renderSlotListCompact(g, font, cursor, slotHeaders, slotPrefix);
    }

    static void renderSaveResultBox(Graphics g, Font font, boolean ok, int boxY) {
        g.setColor(0x000000);
        g.fillRect(24, boxY, 192, 36);
        g.setColor(0x00FF00);
        g.drawRect(25, boxY + 1, 190, 34);

        String msg = ok ? UiText.get(TextId.REPLAY_SAVE_OK) : UiText.get(TextId.REPLAY_SAVE_FAIL);
        UiDraw.drawString2(g, font, msg, 120, boxY + 20, 1, 0xFFFFFF, 0x444444);
    }

    static String difficultyName(int v) {
        switch (v) {
            case 0:
                return UiText.get(TextId.REPLAY_DIFFICULTY_EASY);
            case 1:
                return UiText.get(TextId.REPLAY_DIFFICULTY_NORMAL);
            case 2:
                return UiText.get(TextId.REPLAY_DIFFICULTY_HARD);
            case 3:
                return UiText.get(TextId.REPLAY_DIFFICULTY_LUNATIC);
            case 4:
                return UiText.get(TextId.REPLAY_DIFFICULTY_EXTRA);
            default:
                return "-";
        }
    }

    static String stageName(int v) {
        switch (v) {
            case 0:
                return UiText.get(TextId.REPLAY_STAGE_1);
            case 1:
                return UiText.get(TextId.REPLAY_STAGE_2);
            case 2:
                return UiText.get(TextId.REPLAY_STAGE_3);
            case 3:
                return UiText.get(TextId.REPLAY_STAGE_4);
            case 4:
                return UiText.get(TextId.REPLAY_STAGE_5);
            case 5:
                return UiText.get(TextId.REPLAY_STAGE_6);
            case 6:
                return UiText.get(TextId.REPLAY_STAGE_EX);
            default:
                return "-";
        }
    }

    static String formatUnit(ReplayHeader h) {
        if (h == null) {
            return "-";
        }
        int c = h.getChara();
        int t = h.getType();
        String n;
        if (c == 0) {
            n = UiText.get(TextId.REPLAY_CHARA_REIMU);
        } else if (c == 1) {
            n = UiText.get(TextId.REPLAY_CHARA_MARISA);
        } else if (c == 2) {
            n = UiText.get(TextId.REPLAY_CHARA_ALICE);
        } else {
            n = "?";
        }
        String suf = (t == 0) ? "A" : "B";
        return n + suf;
    }

    // Compact tags for list line 2 (omits redundant ST/SP mode indicator).
    static String formatListTags(int slot, ReplayHeader h) {
        if (h == null) {
            return "";
        }
        String tag = "";
        if (h.getMode() == 3) {
            tag = UiText.get(TextId.REPLAY_TAG_SP);
        }
        if (h.getFlag() == ReplayHeader.FLAG_IMPORTED) {
            if (tag.length() > 0) { tag += " "; }
            tag += UiText.get(TextId.REPLAY_TAG_IMPORTED);
        }
        if (h.getMode() != 3 && hasBossOnly(slot, h)) {
            if (tag.length() > 0) { tag += " "; }
            tag += UiText.get(TextId.REPLAY_TAG_ST);
        }
        return tag;
    }

    static boolean hasBossOnly(int slot, ReplayHeader h) {
        if (h == null) {
            return false;
        }
        if (h.getBossStartFrame() < 0) {
            return false;
        }
        if (slotHasBossOnly != null && slot >= 0 && slot < slotHasBossOnly.length) {
            return slotHasBossOnly[slot];
        }

        if (ReplayBossOnlyPolicy.isFastForwardBossOnlyStage(h.getStage())) {
            return ReplayStageSnapshotStore.hasSlot(slot);
        }
        return ReplayBossSnapshotStore.hasSlot(slot);
    }

    private static void ensureSlotBossOnlyCache() {
        if (slotHasBossOnly == null || slotHasBossOnly.length != ReplayRmsStore.SLOT_COUNT) {
            slotHasBossOnly = new boolean[ReplayRmsStore.SLOT_COUNT];
        }
    }

    static String formatDetail(ReplayHeader h) {
        if (h == null) {
            return "-";
        }

        if (h.getMode() == 3) {
            return getSpellName(h.getSpellId());
        }

        return difficultyName(h.getDifficulty()) + " " + stageName(h.getStage());
    }

    // Decode fixed 32-byte zero-terminated UTF-8 slot name.
    static String decodeSlotName(byte[] name32) {
        if (name32 == null) {
            return "";
        }

        int n = 0;
        while (n < name32.length) {
            if (name32[n] == 0) {
                break;
            }
            n++;
        }
        if (n <= 0) {
            return "";
        }

        String s;
        try {
            s = new String(name32, 0, n, "UTF-8");
        } catch (Throwable t) {
            try {
                s = new String(name32, 0, n);
            } catch (Throwable ignore) {
                s = "";
            }
        }

        s = s.trim();
        return s;
    }

    // Lazy-load localized spell card names for spell practice replay display.
    private static String getSpellName(int id) {
        ensureSpellTextsLoaded();
        if (spellNames != null && id >= 0 && id < spellNames.length) {
            String s = spellNames[id];
            if (s != null && s.length() > 0) {
                return s;
            }
        }
        if (id == 255) {
            return "-";
        }
        return UiText.get(TextId.REPLAY_SPELLNO_PREFIX) + (id + 1);
    }

    private static void ensureSpellTextsLoaded() {
        if (spellTextLoaded) {
            return;
        }

        String text = I18n.readUtf8TextResource(I18n.path("spcard.dat"));
        spellNames = (text != null) ? I18n.splitLinesSimple(text) : null;
        spellTextLoaded = true;
    }
}
