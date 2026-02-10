package touhou;

import javax.microedition.lcdui.Display;

public final class VibrationController {
    // Ported from DoJa : ue()/ve(int).

    private final MainMidlet midlet;

    private int vibCnt;
    private int vibOptPollCnt;
    private boolean vibrationEnabled;

    public VibrationController(MainMidlet midlet) {
        this.midlet = midlet;
    }

    public void requestFrames(int frames) {
        if (frames <= 0) {
            return;
        }
        refreshVibrationOptionNow();
        if (!vibrationEnabled) {
            return;
        }
        if (frames > vibCnt) {
            vibCnt = frames;
        }
    }

    public void tick() {
        if (midlet == null) {
            vibCnt = 0;
            return;
        }

        vibOptPollCnt--;
        if (vibOptPollCnt <= 0) {
            vibOptPollCnt = 30;
            refreshVibrationOptionNow();
            if (!vibrationEnabled && vibCnt != 0) {
                vibCnt = 0;
                Display.getDisplay(midlet).vibrate(0);
            }
        }

        if (!vibrationEnabled) {
            return;
        }

        if (vibCnt > 0) {
            Display.getDisplay(midlet).vibrate(100);
            vibCnt--;
            if (vibCnt == 0) {
                Display.getDisplay(midlet).vibrate(0);
            }
        }
    }

    private void refreshVibrationOptionNow() {
        int[] opt = GameOptions.load();
        if (opt == null) {
            opt = GameOptions.defaults();
        }
        vibrationEnabled = (opt.length > 4 && opt[4] != 0);
    }
}
