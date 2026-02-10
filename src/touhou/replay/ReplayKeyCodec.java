package touhou.replay;

import touhou.GameCore;

public final class ReplayKeyCodec {
    public static final int BIT_UP = 0x01;
    public static final int BIT_DOWN = 0x02;
    public static final int BIT_LEFT = 0x04;
    public static final int BIT_RIGHT = 0x08;

    public static final int BIT_FIRE_ACTION = 0x10;
    public static final int BIT_TOGGLE_SHOT = 0x20;
    public static final int BIT_TOGGLE_SLOW = 0x40;
    public static final int BIT_BOMB = 0x80;

    private ReplayKeyCodec() {
    }

    public static byte encode(int keys, int pressed) {
        return encode(keys, pressed, 0);
    }

    public static byte encode(int keys, int pressed, int fireActionPressed) {
        int r = 0;

        if ((keys & GameCore.UP_PRESSED) != 0) {
            r |= BIT_UP;
        }
        if ((keys & GameCore.DOWN_PRESSED) != 0) {
            r |= BIT_DOWN;
        }
        if ((keys & GameCore.LEFT_PRESSED) != 0) {
            r |= BIT_LEFT;
        }
        if ((keys & GameCore.RIGHT_PRESSED) != 0) {
            r |= BIT_RIGHT;
        }

        if ((pressed & GameCore.FIRE_PRESSED) != 0) {
            r |= BIT_TOGGLE_SHOT;
        }
        if ((fireActionPressed & GameCore.FIRE_PRESSED) != 0) {
            r |= BIT_FIRE_ACTION;
        }
        // Slow toggle can be triggered either by GAME_B or the in-game right softkey.
        if ((pressed & (GameCore.GAME_B_PRESSED | GameCore.SOFTKEY_R_PRESSED)) != 0) {
            r |= BIT_TOGGLE_SLOW;
        }
        if ((pressed & GameCore.GAME_C_PRESSED) != 0) {
            r |= BIT_BOMB;
        }

        return (byte) r;
    }

    public static int decodeToKeyState(byte replayByte) {
        int r = replayByte & 0xFF;
        int keys = 0;

        if ((r & BIT_UP) != 0) {
            keys |= GameCore.UP_PRESSED;
        }
        if ((r & BIT_DOWN) != 0) {
            keys |= GameCore.DOWN_PRESSED;
        }
        if ((r & BIT_LEFT) != 0) {
            keys |= GameCore.LEFT_PRESSED;
        }
        if ((r & BIT_RIGHT) != 0) {
            keys |= GameCore.RIGHT_PRESSED;
        }

        if ((r & BIT_TOGGLE_SHOT) != 0) {
            keys |= GameCore.FIRE_PRESSED;
        }
        if ((r & BIT_TOGGLE_SLOW) != 0) {
            keys |= GameCore.GAME_B_PRESSED;
        }
        if ((r & BIT_BOMB) != 0) {
            keys |= GameCore.GAME_C_PRESSED;
        }

        return keys;
    }

    public static boolean isFireAction(byte replayByte) {
        return ((replayByte & 0xFF) & BIT_FIRE_ACTION) != 0;
    }

    public static boolean isToggleShot(byte replayByte) {
        return ((replayByte & 0xFF) & BIT_TOGGLE_SHOT) != 0;
    }

    public static boolean isToggleSlow(byte replayByte) {
        return ((replayByte & 0xFF) & BIT_TOGGLE_SLOW) != 0;
    }

    public static boolean isBomb(byte replayByte) {
        return ((replayByte & 0xFF) & BIT_BOMB) != 0;
    }
}
