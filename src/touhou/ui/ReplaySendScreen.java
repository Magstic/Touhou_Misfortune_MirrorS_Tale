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
import touhou.replay.ReplayShareService;

public final class ReplaySendScreen {
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

    private static final int SCENE_SENDING = 0;
    private static final int SCENE_MESSAGE = 1;

    // Worker thread reports results through these volatile fields.
    private volatile int pendingScene;
    private volatile String pendingMsg;

    // Progress is reported as written bytes/total bytes.
    private volatile int sendDone;
    private volatile int sendTotal;

    // Status information from BtSppLink.
    private volatile int btStatus;
    private volatile String btRemoteAddr;
    private volatile String btRemoteName;
    private volatile boolean cancelRequested;

    private int scene;
    private String msg;

    private int decoTick;

    private int slot;
    private ReplayHeader header;
    private byte[] name32;

    private String previewName;
    private String previewUnit;
    private String previewDetail;
    private String previewScore;

    private ImageBank imgs;

    private BtSppLink.CancelRef cancelRef;

    // Used to ignore stale callbacks from previous worker threads.
    private int sessionId;
    private long speedLastMs;
    private int speedLastDone;
    private int speedBps;

    public void enter(int slot) {
        sessionId++;
        this.slot = slot;
        this.scene = SCENE_SENDING;

        msg = null;
        pendingScene = -1;
        pendingMsg = null;

        sendDone = 0;
        sendTotal = 0;

        btStatus = BtSppLink.STATUS_IDLE;
        btRemoteAddr = null;
        btRemoteName = null;
        cancelRequested = false;

        speedLastMs = 0;
        speedLastDone = 0;
        speedBps = 0;

        header = new ReplayHeader();
        name32 = new byte[ReplayRmsStore.NAME_SIZE];
        ReplayRmsStore.loadSlotHeaderAndName(slot, header, name32);
        buildPreviewStrings();

        startWorker();
    }

    public Result update(int pressed) {
        decoTick++;
        applyPendingFromWorker();

        if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            if (scene == SCENE_SENDING) {
                requestCancel();
            }
            return new Result(Result.KIND_BACK_TO_REPLAY);
        }

        if (scene == SCENE_MESSAGE) {
            if ((pressed & (GameCanvas.FIRE_PRESSED | GameCanvas.GAME_A_PRESSED)) != 0) {
                return new Result(Result.KIND_BACK_TO_REPLAY);
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

        UiDraw.drawString2(g, FONT, UiText.get(TextId.BT_SEND_TITLE), 120, 25, 1, 0xFFFFFF, 0x777777);

        renderPreviewCard(g);

        if (scene == SCENE_SENDING) {
            renderSending(g);
        } else {
            renderMessage(g);
        }
    }

    private void renderPreviewCard(Graphics g) {
        int cardX = 16;
        int cardY = 32;
        int cardW = 208;
        int cardH = 78;

        UiDraw.fillRectAlpha(g, cardX, cardY, cardW, cardH, 0x000000, 160);
        g.setColor(0x00FF00);
        g.drawRect(cardX, cardY, cardW - 1, cardH - 1);

        if (header == null || header.getFlag() == 0) {
            UiDraw.drawString2(g, FONT_M, UiText.get(TextId.REPLAY_MSG_EMPTY_SLOT), 120, cardY + 26, 1, 0xBBBBBB, 0x333333);
            return;
        }

        boolean sp = (header != null && header.getMode() == 3);

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

    private void renderSending(Graphics g) {
        int boxX = 16;
        int boxY = 116;
        int boxW = 208;
        int boxH = 106;

        UiDraw.fillRectAlpha(g, boxX, boxY, boxW, boxH, 0x000000, 160);
        g.setColor(0x00AA00);
        g.drawRect(boxX, boxY, boxW - 1, boxH - 1);

        // Status text with animated dots
        String st = buildStatusText();
        UiDraw.drawString2(g, FONT_M, st, 120, boxY + 18, 1, 0xFFFFFF, 0x222222);

        // Device name (marquee if long)
        String dev = buildDeviceText();
        if (dev != null && dev.length() != 0) {
            UiDraw.drawStringMarquee2(g, FONT, dev, boxX + 8, boxY + 34, boxW - 16, 1, 0xDDDDDD, 0x222222, decoTick);
        }

        // Separator line
        g.setColor(0x005500);
        g.drawLine(boxX + 8, boxY + 44, boxX + boxW - 8, boxY + 44);

        // Progress bar
        int barX = boxX + 12;
        int barY = boxY + 50;
        int barW = boxW - 24;
        int barH = 12;

        UiDraw.fillRectAlpha(g, barX, barY, barW, barH, 0x001A00, 200);
        g.setColor(0x005500);
        g.drawRect(barX, barY, barW - 1, barH - 1);

        int total = sendTotal;
        int done = sendDone;
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

        UiDraw.drawString2(g, FONT, bytesStr, boxX + 12, boxY + 78, 0, 0xAAAAAA, 0x222222);
        UiDraw.drawString2(g, FONT, speedStr, boxX + boxW - 12, boxY + 78, 2, 0xAAAAAA, 0x222222);

        // Action hint
        UiDraw.drawString2(g, FONT, "[LS] " + UiText.get(TextId.BT_HINT_CANCEL), 120, boxY + 96, 1, 0x558855, 0x112211);
    }

    private void renderMessage(Graphics g) {
        int msgBoxX = 16;
        int msgBoxY = 118;
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

    private void startWorker() {
        if (!BtSppLink.isSupported()) {
            msg = UiText.get(TextId.BT_MSG_NOT_SUPPORTED);
            scene = SCENE_MESSAGE;
            return;
        }

        byte[] packet = null;
        try {
            packet = ReplayShareService.exportSlotPacket(slot);
        } catch (Throwable t) {
            packet = null;
        }

        if (packet == null) {
            msg = UiText.get(TextId.REPLAY_MSG_EMPTY_SLOT);
            scene = SCENE_MESSAGE;
            return;
        }

        cancelRef = new BtSppLink.CancelRef();

        final int sid = sessionId;
        final byte[] sendPacket = packet;
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    BtSppLink.sendPacket(sendPacket, new BtSppLink.ProgressListener() {
                        public void onProgress(int done, int total) {
                            if (sessionId != sid) {
                                return;
                            }
                            sendDone = done;
                            sendTotal = total;
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

                    pendingMsg = UiText.get(TextId.BT_MSG_SENT);
                    pendingScene = SCENE_MESSAGE;
                } catch (Throwable t) {
                    String s = String.valueOf(t);
                    if (s != null && (s.indexOf("no service") >= 0 || s.indexOf("canceled") >= 0 || s.indexOf("cancel") >= 0)) {
                        pendingMsg = UiText.get(TextId.BT_MSG_CANCELED);
                    } else {
                        pendingMsg = UiText.get(TextId.BT_MSG_ERROR) + " " + s;
                    }
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

            scene = ns;
        }
    }

    private void buildPreviewStrings() {
        previewName = ReplaySaveUi.decodeSlotName(name32);
        previewUnit = ReplaySaveUi.formatUnit(header);
        previewDetail = ReplaySaveUi.formatDetail(header);
        previewScore = String.valueOf(header.getScore());
    }

    private String buildStatusText() {
        if (cancelRequested && scene == SCENE_SENDING) {
            return UiText.get(TextId.BT_MSG_CANCELING);
        }
        int st = btStatus;
        if (st == BtSppLink.STATUS_SELECT_SERVICE) return UiText.get(TextId.BT_STATUS_SELECT_SERVICE);
        if (st == BtSppLink.STATUS_CONNECTING) return UiText.get(TextId.BT_STATUS_CONNECTING);
        if (st == BtSppLink.STATUS_CONNECTED) return UiText.get(TextId.BT_STATUS_CONNECTED);
        if (st == BtSppLink.STATUS_SENDING) return UiText.get(TextId.BT_STATUS_SENDING);
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
