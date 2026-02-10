package touhou.replay;

import touhou.bt.BtSppLink;
import touhou.ui.UiController;

public final class ReplayBtShareManager {
    // Background worker for BT share to avoid blocking UI.
    private volatile boolean busy;

    public ReplayBtShareManager() {
    }

    public boolean isBusy() {
        return busy;
    }

    public void start(final int slot, final boolean receive, final UiController ui) {
        if (busy) {
            if (ui != null) {
                ui.showReplayMessage("BT busy.");
            }
            return;
        }
        busy = true;

        Thread t = new Thread(new Runnable() {
            public void run() {
                String msg;
                try {
                    if (!BtSppLink.isSupported()) {
                        msg = "BT not supported.";
                    } else if (receive) {
                        msg = doReceive(slot);
                    } else {
                        msg = doSend(slot);
                    }
                } catch (Throwable t) {
                    String s = String.valueOf(t);
                    if (s != null && (s.indexOf("canceled") >= 0 || s.indexOf("cancel") >= 0)) {
                        msg = "Canceled.";
                    } else if (s != null && (s.indexOf("no service") >= 0 || s.indexOf("selectService") >= 0)) {
                        msg = "BT send unsupported.";
                    } else {
                        msg = "BT error: " + s;
                    }
                }

                busy = false;
                if (ui != null) {
                    ui.showReplayMessage(msg);
                }
            }
        });
        t.start();
    }

    private static String doSend(int slot) throws Exception {
        byte[] packet = ReplayShareService.exportSlotPacket(slot);
        if (packet == null) {
            return "Empty slot.";
        }
        BtSppLink.sendPacket(packet);
        return "Sent.";
    }

    private static String doReceive(int slot) throws Exception {
        byte[] packet = BtSppLink.waitAndReceivePacket();
        if (packet == null) {
            return "Canceled.";
        }
        boolean ok = ReplayShareService.importToSlotFromPacket(slot, packet);
        return ok ? "Received." : "Invalid data.";
    }
}
