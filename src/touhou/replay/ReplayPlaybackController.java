package touhou.replay;

public final class ReplayPlaybackController {
    public static final class StartParams {
        public int gamemode;
        public int level;
        public int chara;
        public int type;
        public int startStage;
        public int spellId;
    }

    public static final class FrameInput {
        public int keys;
        public int pressed;
        public int fireActionPressed;
    }

    private boolean active;
    private boolean bossOnly;
    private int framePos;
    private byte[] frames;

    private final byte[] dialoguePageCounts = new byte[ReplayHeader.DIALOGUE_MAX];
    private int dialoguePageCountLen;

    private final ReplayHeader header = new ReplayHeader();
    private final byte[] name32 = new byte[ReplayRmsStore.NAME_SIZE];
    private final byte[] full7168 = new byte[ReplayRmsStore.DATA_SIZE];
    private final byte[] boss7168 = new byte[ReplayRmsStore.DATA_SIZE];
    private final FrameInput frameInput = new FrameInput();

    public ReplayPlaybackController() {
        stop();
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBossOnly() {
        return bossOnly;
    }

    public int getFramePos() {
        return framePos;
    }

    public int getBossStartFrame() {
        return header.getBossStartFrame();
    }

    public void stop() {
        active = false;
        bossOnly = false;
        framePos = 0;
        frames = null;

        for (int i = 0; i < dialoguePageCounts.length; i++) {
            dialoguePageCounts[i] = 0;
        }
        dialoguePageCountLen = 0;
    }

    public StartParams startFromSlot(int slot, boolean bossOnly) {
        header.clear();
        ReplayRmsStore.loadSlot2(slot, header, name32, full7168, boss7168);
        if (header.getFlag() == ReplayHeader.FLAG_NONE) {
            stop();
            return null;
        }

        dialoguePageCountLen = header.getDialoguePageCountLen();
        for (int i = 0; i < dialoguePageCounts.length; i++) {
            int v = header.getDialoguePageCountAt(i);
            dialoguePageCounts[i] = (byte) ((v < 0) ? 0 : (v & 0xFF));
        }

        byte[] encoded = bossOnly ? boss7168 : full7168;
        if (bossOnly && ReplayBossOnlyPolicy.isFastForwardBossOnlyStage(header.getStage())) {
            encoded = full7168;
        }
        frames = ReplayCodec.decode(encoded);

        active = true;
        this.bossOnly = bossOnly;
        framePos = 0;

        StartParams sp = new StartParams();
        sp.gamemode = header.getMode();
        sp.level = header.getDifficulty();
        sp.chara = header.getChara();
        sp.type = header.getType();
        sp.startStage = header.getStage();
        sp.spellId = header.getSpellId();
        return sp;
    }

    public int getRecordedDialoguePageCount(int encounterIndex) {
        if (!active) {
            return -1;
        }
        if (encounterIndex < 0 || encounterIndex >= ReplayHeader.DIALOGUE_MAX) {
            return -1;
        }
        if (encounterIndex >= dialoguePageCountLen) {
            return -1;
        }
        return dialoguePageCounts[encounterIndex] & 0xFF;
    }

    public boolean wantsQuitToMenu(int pressed) {
        return active && ((pressed & javax.microedition.lcdui.game.GameCanvas.GAME_A_PRESSED) != 0);
    }

    public FrameInput nextFrame() {
        if (!active) {
            return null;
        }

        if (frames == null || framePos >= ReplayCodec.FRAME_COUNT) {
            return null;
        }

        byte b = frames[framePos++];

        FrameInput out = frameInput;

        int ks = ReplayKeyCodec.decodeToKeyState(b);
        out.keys = ks & (javax.microedition.lcdui.game.GameCanvas.UP_PRESSED
                | javax.microedition.lcdui.game.GameCanvas.DOWN_PRESSED
                | javax.microedition.lcdui.game.GameCanvas.LEFT_PRESSED
                | javax.microedition.lcdui.game.GameCanvas.RIGHT_PRESSED);

        int p = 0;
        if (ReplayKeyCodec.isToggleShot(b)) {
            p |= javax.microedition.lcdui.game.GameCanvas.FIRE_PRESSED;
        }
        if (ReplayKeyCodec.isToggleSlow(b)) {
            p |= javax.microedition.lcdui.game.GameCanvas.GAME_B_PRESSED;
        }
        if (ReplayKeyCodec.isBomb(b)) {
            p |= javax.microedition.lcdui.game.GameCanvas.GAME_C_PRESSED;
        }
        out.pressed = p;

        out.fireActionPressed = ReplayKeyCodec.isFireAction(b)
                ? javax.microedition.lcdui.game.GameCanvas.FIRE_PRESSED
                : 0;

        return out;
    }
}