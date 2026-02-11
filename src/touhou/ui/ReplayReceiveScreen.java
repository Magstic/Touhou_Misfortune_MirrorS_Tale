package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.BulletSprites;
import touhou.ImageBank;
import touhou.bt.BtSppLink;
import touhou.i18n.TextId;
import touhou.i18n.UiText;
import touhou.replay.ReplayHeader;
import touhou.replay.ReplayRmsStore;
import touhou.replay.ReplayShareCodec;
import touhou.replay.ReplayShareService;

public final class ReplayReceiveScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_REPLAY = 1;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static final Font FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private static final Font FONT_M = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);

    private static final int SCENE_RECEIVING = 0;
    private static final int SCENE_SLOT_SELECT = 1;
    private static final int SCENE_OVERWRITE_CONFIRM = 2;
    private static final int SCENE_MESSAGE = 3;
    private static final int SCENE_RECV_CONFIRM = 4;

    // Worker thread reports results through these volatile fields.
    private volatile int pendingScene;
    private volatile String pendingMsg;
    private volatile byte[] pendingPacket;
    private volatile ReplayShareCodec.Decoded pendingDecoded;
    private volatile boolean pendingReloadSlots;

    // Progress is reported as read bytes/total bytes.
    private volatile int recvDone;
    private volatile int recvTotal;

    // Status information from BtSppLink.
    private volatile int btStatus;
    private volatile String btRemoteAddr;
    private volatile String btRemoteName;
    private volatile boolean cancelRequested;

    // Used to ignore stale callbacks from previous worker threads.
    private int sessionId;
    private long speedLastMs;
    private int speedLastDone;
    private int speedBps;

    private int scene;
    private int cursor;

    private int decoTick;

    private ImageBank imgs;

    private final ReplayHeader[] slotHeaders = new ReplayHeader[ReplayRmsStore.SLOT_COUNT];
    private final byte[][] slotNames = new byte[ReplayRmsStore.SLOT_COUNT][ReplayRmsStore.NAME_SIZE];
    private final byte[] tmpFull = new byte[ReplayRmsStore.DATA_SIZE];
    private final byte[] tmpBoss = new byte[ReplayRmsStore.DATA_SIZE];

    private String msg;

    private byte[] receivedPacket;
    private ReplayShareCodec.Decoded decoded;

    private String previewName;
    private String previewUnit;
    private String previewDetail;
    private String previewScore;

    private BtSppLink.CancelRef cancelRef;

    public void enter() {
        sessionId++;
        scene = SCENE_RECEIVING;
        cursor = 0;
        msg = null;
        receivedPacket = null;
        decoded = null;

        pendingScene = -1;
        pendingMsg = null;
        pendingPacket = null;
        pendingDecoded = null;
        pendingReloadSlots = false;

        previewName = null;
        previewUnit = null;
        previewDetail = null;
        previewScore = null;

        recvDone = 0;
        recvTotal = 0;

        btStatus = BtSppLink.STATUS_IDLE;
        btRemoteAddr = null;
        btRemoteName = null;
        cancelRequested = false;
        speedLastMs = 0;
        speedLastDone = 0;
        speedBps = 0;

        reloadSlots();
        startWorker();
    }

    public Result update(int pressed) {
        decoTick++;
        applyPendingFromWorker();

        if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            if (scene == SCENE_RECEIVING) {
                requestCancel();
            } else if (scene == SCENE_RECV_CONFIRM) {
                // Reject received data, go back
                receivedPacket = null;
                decoded = null;
            }
            return new Result(Result.KIND_BACK_TO_REPLAY);
        }

        if (scene == SCENE_MESSAGE) {
            if ((pressed & (GameCanvas.FIRE_PRESSED | GameCanvas.GAME_A_PRESSED)) != 0) {
                return new Result(Result.KIND_BACK_TO_REPLAY);
            }
            return null;
        }

        if (scene == SCENE_RECV_CONFIRM) {
            if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                // Accept: proceed to slot selection
                reloadSlots();
                scene = SCENE_SLOT_SELECT;
            }
            return null;
        }

        if (scene == SCENE_OVERWRITE_CONFIRM) {
            if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                doSaveToSlot(cursor);
                return null;
            }
            return null;
        }

        if (scene == SCENE_SLOT_SELECT) {
            if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
                cursor = (cursor + 1) % ReplayRmsStore.SLOT_COUNT;
            } else if ((pressed & GameCanvas.UP_PRESSED) != 0) {
                cursor = (cursor - 1 + ReplayRmsStore.SLOT_COUNT) % ReplayRmsStore.SLOT_COUNT;
            } else if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
                ReplayHeader h = slotHeaders[cursor];
                boolean hasData = (h != null && h.getFlag() != 0);
                if (hasData) {
                    scene = SCENE_OVERWRITE_CONFIRM;
                } else {
                    doSaveToSlot(cursor);
                }
            }
            return null;
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

        StageClearResultPanel.renderReplayListBackgroundOrFallback(g, this.imgs);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.BT_RECEIVE_TITLE), 120, 25, 1, 0xFFFFFF, 0x777777);

        if (scene == SCENE_RECEIVING) {
            renderReceiving(g);
        } else if (scene == SCENE_RECV_CONFIRM) {
            renderRecvConfirm(g);
        } else if (scene == SCENE_SLOT_SELECT) {
            renderSlotSelect(g);
        } else if (scene == SCENE_OVERWRITE_CONFIRM) {
            renderOverwriteConfirm(g);
        } else {
            renderMessage(g);
        }
    }

    private void renderReceiving(Graphics g) {
        int boxX = 16;
        int boxY = 42;
        int boxW = 208;
        int boxH = 112;

        UiDraw.fillRectAlpha(g, boxX, boxY, boxW, boxH, 0x000000, 160);
        g.setColor(0x00AA00);
        g.drawRect(boxX, boxY, boxW - 1, boxH - 1);

        // Status text
        String st = buildStatusText();
        UiDraw.drawString2(g, FONT_M, st, 120, boxY + 18, 1, 0xFFFFFF, 0x222222);

        // Device name (marquee if long)
        String dev = buildDeviceText();
        if (dev != null && dev.length() != 0) {
            UiDraw.drawStringMarquee2(g, FONT, dev, boxX + 8, boxY + 36, boxW - 16, 1, 0xDDDDDD, 0x222222, decoTick);
        }

        // Separator line
        g.setColor(0x005500);
        g.drawLine(boxX + 8, boxY + 48, boxX + boxW - 8, boxY + 48);

        // Progress bar
        int barX = boxX + 12;
        int barY = boxY + 54;
        int barW = boxW - 24;
        int barH = 12;

        UiDraw.fillRectAlpha(g, barX, barY, barW, barH, 0x001A00, 200);
        g.setColor(0x005500);
        g.drawRect(barX, barY, barW - 1, barH - 1);

        int total = recvTotal;
        int done = recvDone;
        int w = 0;
        if (total > 0 && done > 0) {
            if (done > total) done = total;
            w = (barW * done) / total;
            if (w < 0) w = 0;
            if (w > barW) w = barW;
        }
        if (w > 2) {
            UiDraw.fillRectAlpha(g, barX + 1, barY + 1, w - 2, barH - 2, 0x00FF00, 160);
        }

        // Percentage text inside bar
        String pct;
        if (total > 0) {
            int p = (done * 100) / total;
            if (p < 0) p = 0;
            if (p > 100) p = 100;
            pct = p + "%";
        } else {
            pct = "0%";
        }
        int barMidY = barY + ((barH - UiDraw.fontHeight(FONT)) >> 1) + UiDraw.fontBaseline(FONT);
        UiDraw.drawString2(g, FONT, pct, 120, barMidY, 1, 0xFFFFFF, 0x003300);

        // Compact info: bytes left, speed right
        updateSpeed(done);
        String bytesStr = done + "/" + ((total > 0) ? String.valueOf(total) : "?") + "B";
        String speedStr = formatSpeed(speedBps);

        UiDraw.drawString2(g, FONT, bytesStr, boxX + 12, boxY + 82, 0, 0xAAAAAA, 0x222222);
        UiDraw.drawString2(g, FONT, speedStr, boxX + boxW - 12, boxY + 82, 2, 0xAAAAAA, 0x222222);

        // Action hint
        UiDraw.drawString2(g, FONT, "[LS] " + UiText.get(TextId.BT_HINT_CANCEL), 120, boxY + 100, 1, 0x558855, 0x112211);
    }

    private void renderRecvConfirm(Graphics g) {
        // Preview card at top
        renderPreviewCard(g);

        // Confirmation dialog below preview
        int dlgX = 16;
        int dlgY = 118;
        int dlgW = 208;
        int dlgH = 80;

        UiDraw.fillRectAlpha(g, dlgX, dlgY, dlgW, dlgH, 0x000000, 180);
        g.setColor(0x00FF00);
        g.drawRect(dlgX, dlgY, dlgW - 1, dlgH - 1);

        // "收到來自 [DeviceName] 的 Replay 數據"
        String devName = btRemoteName;
        if (devName == null || devName.length() == 0) {
            devName = btRemoteAddr;
        }
        if (devName == null || devName.length() == 0) {
            devName = "???";
        }
        String line1 = UiText.get(TextId.BT_RECV_FROM_PREFIX) + devName;
        String line2 = UiText.get(TextId.BT_RECV_FROM_SUFFIX);
        UiDraw.drawStringMarquee2(g, FONT, line1, dlgX + 8, dlgY + 18, dlgW - 16, 1, 0xDDDDDD, 0x222222, decoTick);
        UiDraw.drawString2(g, FONT, line2, 120, dlgY + 32, 1, 0xDDDDDD, 0x222222);

        // "是否接受？"
        UiDraw.drawString2(g, FONT_M, UiText.get(TextId.BT_RECV_CONFIRM), 120, dlgY + 50, 1, 0xFFFFFF, 0x222222);

        // Action hints
        String hintAccept = "[OK] " + UiText.get(TextId.BT_HINT_ACCEPT);
        String hintReject = "[LS] " + UiText.get(TextId.BT_HINT_REJECT);
        UiDraw.drawString2(g, FONT, hintAccept, 72, dlgY + 70, 1, 0x88FF88, 0x224422);
        UiDraw.drawString2(g, FONT, hintReject, 168, dlgY + 70, 1, 0x558855, 0x112211);
    }

    private void renderSlotSelect(Graphics g) {
        renderPreviewCard(g);
        renderSlotListBelowPreview(g);
    }

    private void renderOverwriteConfirm(Graphics g) {
        renderPreviewCard(g);

        int dlgX = 16;
        int dlgY = 118;
        int dlgW = 208;
        int dlgH = 72;

        UiDraw.fillRectAlpha(g, dlgX, dlgY, dlgW, dlgH, 0x000000, 180);
        g.setColor(0x00FF00);
        g.drawRect(dlgX, dlgY, dlgW - 1, dlgH - 1);

        String q = UiText.get(TextId.BT_CONFIRM_OVERWRITE_PREFIX) + (cursor + 1) + UiText.get(TextId.BT_CONFIRM_OVERWRITE_SUFFIX);
        UiDraw.drawString2(g, FONT_M, q, 120, dlgY + 24, 1, 0xFFFFFF, 0x222222);

        // Action hints
        String hintOk = "[OK] " + UiText.get(TextId.BT_HINT_OK);
        String hintBack = "[LS] " + UiText.get(TextId.BT_HINT_CANCEL);
        UiDraw.drawString2(g, FONT, hintOk, 72, dlgY + 54, 1, 0x88FF88, 0x224422);
        UiDraw.drawString2(g, FONT, hintBack, 168, dlgY + 54, 1, 0x558855, 0x112211);
    }

    private void renderPreviewCard(Graphics g) {
        int cardX = 16;
        int cardY = 32;
        int cardW = 208;
        int cardH = 78;

        UiDraw.fillRectAlpha(g, cardX, cardY, cardW, cardH, 0x000000, 160);
        g.setColor(0x00FF00);
        g.drawRect(cardX, cardY, cardW - 1, cardH - 1);

        if (decoded == null || decoded.header == null) {
            UiDraw.drawString2(g, FONT_M, UiText.get(TextId.BT_MSG_NO_DATA), 120, cardY + 26, 1, 0xBBBBBB, 0x333333);
            return;
        }

        ReplayHeader h = decoded.header;
        boolean sp = (h.getMode() == 3);

        String title = (previewName != null && previewName.length() != 0) ? previewName : previewDetail;

        int titleClipX = cardX + 12;
        int titleClipW = cardW - 24;
        if (sp) {
            titleClipW = cardW - 42;
        }
        UiDraw.drawStringMarquee2(g, FONT_M, title, titleClipX, cardY + 22, titleClipW, 1, 0xFFFFFF, 0x222222, decoTick);

        if (sp) {
            UiDraw.drawString2(g, FONT, UiText.get(TextId.REPLAY_TAG_SP), cardX + cardW - 12, cardY + 22, 2, 0xA0FFA0, 0x224422);
        }

        UiDraw.drawString2(g, FONT, previewUnit, cardX + 12, cardY + 42, 0, 0xDDDDDD, 0x333333);

        g.setColor(0x005500);
        g.drawLine(cardX + 8, cardY + 54, cardX + cardW - 8, cardY + 54);

        UiDraw.drawString2(g, FONT, UiText.get(TextId.REPLAY_LABEL_SCORE), cardX + 12, cardY + 66, 0, 0xAAAAAA, 0x333333);
        UiDraw.drawString2(g, FONT, previewScore, cardX + cardW - 12, cardY + 66, 2, 0xFFFFFF, 0x222222);
    }

    private void renderSlotListBelowPreview(Graphics g) {
        int slotCount = ReplayRmsStore.SLOT_COUNT;
        int visible = 3;
        if (slotCount < visible) {
            visible = slotCount;
        }

        int start = cursor - (visible / 2);
        if (start < 0) {
            start = 0;
        }
        if (start > slotCount - visible) {
            start = slotCount - visible;
        }

        int lineH = FONT.getHeight();
        int baselinePos = FONT.getBaselinePosition();
        int lineGap = 2;

        int boxX = 12;
        int firstBoxY = 118;
        int boxW = 216;
        int boxH = 34;
        int boxStepY = 40;

        for (int row = 0; row < visible; row++) {
            int i = start + row;
            boolean sel = (i == cursor);
            int main = sel ? 0xFFFFFF : 0x777777;
            int sh = sel ? 0x444444 : 0x222222;

            int boxY = firstBoxY + row * boxStepY;
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

            UiDraw.drawString2(g, FONT, UiText.get(TextId.COMMON_SLOT_PREFIX) + (i + 1), boxX + 6, line1BaseY, 0, main, sh);

            ReplayHeader h = slotHeaders[i];
            if (h != null && h.getFlag() != 0) {
                // Line 1: unit + score (no tags, avoids overlap).
                UiDraw.drawString2(g, FONT, ReplaySaveUi.formatUnit(h), boxX + 60, line1BaseY, 0, main, sh);
                UiDraw.drawString2(g, FONT, String.valueOf(h.getScore()), boxX + boxW - 6, line1BaseY, 2, main, sh);

                // Line 2 right: compact tags (Boss/Imported/SP only).
                int tagMain = sel ? 0xA0FFA0 : 0x558855;
                int tagSh = sel ? 0x224422 : 0x112211;
                String listTags = ReplaySaveUi.formatListTags(i, h);
                int tagsW = 0;
                if (listTags.length() > 0) {
                    UiDraw.drawString2(g, FONT, listTags, boxX + boxW - 6, line2BaseY, 2, tagMain, tagSh);
                    tagsW = UiDraw.stringWidth(FONT, listTags) + 6;
                }

                // Line 2 left: detail text.
                int detailClipW = boxW - 12 - tagsW;
                if (detailClipW < 20) { detailClipW = 20; }
                String detail = ReplaySaveUi.formatDetail(h);
                UiDraw.drawStringMarquee2(g, FONT, detail, boxX + 6, line2BaseY, detailClipW, 0, main, sh, decoTick);
            } else {
                UiDraw.drawString2(g, FONT, "-", boxX + boxW - 6, line1BaseY, 2, main, sh);
            }
        }

        if (slotCount > visible) {
            int trackX = 232;
            int trackY = firstBoxY;
            int trackH = (visible - 1) * boxStepY + boxH;

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

    private void renderMessage(Graphics g) {
        int msgBoxX = 16;
        int msgBoxY = 104;
        int msgBoxW = 208;
        int msgBoxH = 60;

        UiDraw.fillRectAlpha(g, msgBoxX, msgBoxY, msgBoxW, msgBoxH, 0x000000, 180);
        g.setColor(0x00FF00);
        g.drawRect(msgBoxX, msgBoxY, msgBoxW - 1, msgBoxH - 1);

        String s = (msg != null) ? msg : "";
        UiDraw.drawStringMarquee2(g, FONT_M, s, msgBoxX + 8, msgBoxY + 22, msgBoxW - 16, 1, 0xFFFFFF, 0x444444, decoTick);

        // Action hint
        String hint = "[OK] " + UiText.get(TextId.BT_HINT_OK);
        UiDraw.drawString2(g, FONT, hint, 120, msgBoxY + 46, 1, 0x88FF88, 0x224422);
    }

    private void reloadSlots() {
        for (int i = 0; i < ReplayRmsStore.SLOT_COUNT; i++) {
            if (slotHeaders[i] == null) {
                slotHeaders[i] = new ReplayHeader();
            }
        }
        ReplaySaveUi.loadAllSlots(slotHeaders, slotNames, tmpFull, tmpBoss);
    }

    private void startWorker() {
        if (!BtSppLink.isSupported()) {
            msg = UiText.get(TextId.BT_MSG_NOT_SUPPORTED);
            scene = SCENE_MESSAGE;
            return;
        }

        cancelRef = new BtSppLink.CancelRef();

        final int sid = sessionId;
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] p = BtSppLink.waitAndReceivePacket(new BtSppLink.ProgressListener() {
                        public void onProgress(int done, int total) {
                            if (sessionId != sid) {
                                return;
                            }
                            recvDone = done;
                            recvTotal = total;
                        }
                    }, cancelRef, new BtSppLink.StatusListener() {
                        public void onStatus(int status) {
                            if (sessionId != sid) {
                                return;
                            }
                            btStatus = status;
                        }

                        public void onRemoteDevice(String addr, String name) {
                            if (sessionId != sid) {
                                return;
                            }
                            btRemoteAddr = addr;
                            btRemoteName = name;
                        }
                    });

                    if (p == null) {
                        pendingMsg = UiText.get(TextId.BT_MSG_CANCELED);
                        pendingScene = SCENE_MESSAGE;
                        return;
                    }

                    ReplayShareCodec.Decoded d = ReplayShareCodec.decode(p);
                    if (d == null || d.header == null) {
                        pendingMsg = UiText.get(TextId.BT_MSG_INVALID_DATA);
                        pendingScene = SCENE_MESSAGE;
                        return;
                    }

                    pendingPacket = p;
                    pendingDecoded = d;
                    pendingScene = SCENE_RECV_CONFIRM;
                } catch (Throwable t) {
                    pendingMsg = UiText.get(TextId.BT_MSG_ERROR) + " " + String.valueOf(t);
                    pendingScene = SCENE_MESSAGE;
                }
            }
        });
        t.start();
    }

    private void requestCancel() {
        BtSppLink.CancelRef r = cancelRef;
        if (r != null) {
            cancelRequested = true;
            r.cancel();
        }
    }

    private void applyPendingFromWorker() {
        int ns = pendingScene;
        if (ns != -1) {
            pendingScene = -1;

            String m = pendingMsg;
            if (m != null) {
                pendingMsg = null;
                msg = m;
            }

            ReplayShareCodec.Decoded d = pendingDecoded;
            if (d != null) {
                pendingDecoded = null;
                decoded = d;
                buildPreviewStrings(d);
            }

            byte[] p = pendingPacket;
            if (p != null) {
                pendingPacket = null;
                receivedPacket = p;
            }

            if (pendingReloadSlots) {
                pendingReloadSlots = false;
                reloadSlots();
            }

            scene = ns;
        }
    }

    private void doSaveToSlot(int slot) {
        if (receivedPacket == null) {
            msg = UiText.get(TextId.BT_MSG_NO_DATA);
            scene = SCENE_MESSAGE;
            return;
        }
        boolean ok;
        try {
            ok = ReplayShareService.importToSlotFromPacket(slot, receivedPacket);
        } catch (Throwable t) {
            ok = false;
        }
        reloadSlots();
        msg = ok ? (UiText.get(TextId.BT_MSG_SAVED_PREFIX) + (slot + 1) + UiText.get(TextId.BT_MSG_SAVED_SUFFIX)) : UiText.get(TextId.BT_MSG_SAVE_FAILED);
        scene = SCENE_MESSAGE;
    }

    private void buildPreviewStrings(ReplayShareCodec.Decoded d) {
        previewName = ReplaySaveUi.decodeSlotName(d.name32);
        previewUnit = ReplaySaveUi.formatUnit(d.header);
        previewDetail = ReplaySaveUi.formatDetail(d.header);
        previewScore = String.valueOf(d.header.getScore());
    }

    private String buildStatusText() {
        if (cancelRequested && scene == SCENE_RECEIVING) {
            return UiText.get(TextId.BT_MSG_CANCELING);
        }
        int st = btStatus;
        if (st == BtSppLink.STATUS_SELECT_SERVICE) return UiText.get(TextId.BT_STATUS_SELECT_SERVICE);
        if (st == BtSppLink.STATUS_CONNECTING) return UiText.get(TextId.BT_STATUS_CONNECTING);
        if (st == BtSppLink.STATUS_CONNECTED) return UiText.get(TextId.BT_STATUS_CONNECTED);
        if (st == BtSppLink.STATUS_RECEIVING) return UiText.get(TextId.BT_STATUS_RECEIVING);
        if (st == BtSppLink.STATUS_DONE) return UiText.get(TextId.BT_STATUS_DONE);
        if (st == BtSppLink.STATUS_CANCELED) return UiText.get(TextId.BT_STATUS_CANCELED);
        return UiText.get(TextId.BT_STATUS_WAITING);
    }

    private String buildDeviceText() {
        String a = btRemoteAddr;
        String n = btRemoteName;
        if ((a == null || a.length() == 0) && (n == null || n.length() == 0)) {
            return "";
        }
        String s = UiText.get(TextId.BT_LABEL_DEVICE) + ": ";
        if (n != null && n.length() != 0) {
            s += n;
            if (a != null && a.length() != 0) {
                s += " (" + a + ")";
            }
        } else {
            s += a;
        }
        return s;
    }

    private void updateSpeed(int done) {
        long now = System.currentTimeMillis();
        if (speedLastMs == 0) {
            speedLastMs = now;
            speedLastDone = done;
            speedBps = 0;
            return;
        }
        long dt = now - speedLastMs;
        if (dt < 400) {
            return;
        }
        int dd = done - speedLastDone;
        if (dd < 0) dd = 0;
        speedBps = (int) ((dd * 1000L) / dt);
        speedLastMs = now;
        speedLastDone = done;
    }

    private static String formatSpeed(int bps) {
        if (bps <= 0) {
            return "0 B/s";
        }
        int kb10 = (bps * 10) / 1024;
        if (kb10 < 10) {
            return "0." + kb10 + " KB/s";
        }
        if (kb10 < 1000) {
            return (kb10 / 10) + "." + (kb10 % 10) + " KB/s";
        }
        int mb10 = (kb10 * 10) / 1024;
        return (mb10 / 10) + "." + (mb10 % 10) + " MB/s";
    }
}
