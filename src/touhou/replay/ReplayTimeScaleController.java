package touhou.replay;

import javax.microedition.lcdui.game.GameCanvas;

public final class ReplayTimeScaleController {
    private static final int MAX_STEPS_PER_UPDATE = 32;

    private int slowCounter;
    private int autoFastForwardTargetFrame;

    public ReplayTimeScaleController() {
        reset();
    }

    public void reset() {
        slowCounter = 0;
        autoFastForwardTargetFrame = -1;
    }

    public void startAutoFastForwardToFrame(int frameIndex) {
        if (frameIndex < 0) {
            autoFastForwardTargetFrame = -1;
            return;
        }
        autoFastForwardTargetFrame = frameIndex;
    }

    public void stopAutoFastForward() {
        autoFastForwardTargetFrame = -1;
    }

    public boolean isAutoFastForwardActive(ReplayPlaybackController playback) {
        if (playback == null || !playback.isActive()) {
            return false;
        }
        int target = autoFastForwardTargetFrame;
        if (target < 0) {
            return false;
        }
        return playback.getFramePos() < target;
    }

    // Returns how many replay simulation steps should run in this render frame.
    public int computeSteps(int liveKeys, ReplayPlaybackController playback) {
        if (playback == null || !playback.isActive()) {
            return 1;
        }

        int target = autoFastForwardTargetFrame;
        if (target >= 0) {
            int pos = playback.getFramePos();
            int remaining = target - pos;
            if (remaining > 0) {
                int steps = 16;
                if (steps > remaining) {
                    steps = remaining;
                }
                if (steps > MAX_STEPS_PER_UPDATE) {
                    steps = MAX_STEPS_PER_UPDATE;
                }
                if (steps < 1) {
                    steps = 1;
                }
                return steps;
            }

            autoFastForwardTargetFrame = -1;
        }

        if ((liveKeys & GameCanvas.RIGHT_PRESSED) != 0) {
            slowCounter = 0;
            return 8;
        }

        if ((liveKeys & GameCanvas.LEFT_PRESSED) != 0) {
            slowCounter++;
            return ((slowCounter & 1) == 0) ? 1 : 0;
        }

        slowCounter = 0;
        return 1;
    }
}
