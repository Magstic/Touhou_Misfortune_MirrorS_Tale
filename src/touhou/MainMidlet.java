package touhou;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public final class MainMidlet extends MIDlet {
    private static final String BUILD_STAMP = "2026-01-07-1";

    private GameCore canvas;

    protected void startApp() {
        System.out.println("TouhouMIDPPort BUILD " + BUILD_STAMP);
        ResultStats.INSTANCE.onAppStart();
        if (canvas == null) {
            canvas = new GameCore(this);
        }
        Display.getDisplay(this).setCurrent(canvas);
        canvas.start();
    }

    public void startGameAfterLanguageSelect() {
        if (canvas == null) {
            canvas = new GameCore(this);
        }
        Display.getDisplay(this).setCurrent(canvas);
        canvas.start();
    }

    protected void pauseApp() {
        ResultStats.INSTANCE.onAppPauseOrStop();
        if (canvas != null) {
            canvas.pause();
        }
    }

    protected void destroyApp(boolean unconditional) {
        ResultStats.INSTANCE.onAppPauseOrStop();
        if (canvas != null) {
            canvas.stop();
        }
    }
}
