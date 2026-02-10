package touhou.ui;

import javax.microedition.lcdui.game.GameCanvas;

import touhou.GameCore;
import touhou.GameOptions;

public final class InputSystem {
    public static final class FrameInput {
        public int keys;
        public int pressed;
        public int fireActionPressed;
    }

    private final GameCanvas canvas;
    private final FrameInput out = new FrameInput();

    private int prevKeyStates;
    private int prevFireActionKeyStates;

    private int extraKeyStates;
    private int softKeyStates;

    private int dpadKeyStates;
    private int heldDpadMask;

    private int fireActionKeyStates;

    private int tenkeyKeyStates;
    private int heldTenkeyMask;

    private int optShotKey;
    private int optSlowKey;
    private int optSpellKey;
    private int optTenkeyMove;

    public InputSystem(GameCanvas canvas) {
        this.canvas = canvas;
        prevKeyStates = 0;
        prevFireActionKeyStates = 0;
        refreshKeyOptions();
    }

    public void refreshKeyOptions() {
        int[] opt = GameOptions.load();
        if (opt == null) {
            opt = GameOptions.defaults();
        }
        optShotKey = (opt.length > 6) ? opt[6] : 13;
        optSlowKey = (opt.length > 7) ? opt[7] : 13;
        optSpellKey = (opt.length > 8) ? opt[8] : 13;
        optTenkeyMove = (opt.length > 5) ? opt[5] : 0;
    }

    public boolean onKeyPressed(int keyCode,
            boolean inGame,
            boolean battlePaused,
            boolean stageClearPanelActive,
            boolean startSpellPractice,
            boolean spellPracticeEndFlg,
            boolean spellPracticeEndActive) {

        if (inGame && !battlePaused && keyCode == 35) {
            extraKeyStates |= GameCore.KEY_POUND_PRESSED;
        }

        if (inGame && !battlePaused && optTenkeyMove != 0 && isTenkeyMoveDirectionKeyCode(keyCode)) {
            applyTenkeyMoveDown(keyCode, inGame);
            return false; // keep legacy behavior: skip super.keyPressed
        }

        applyOptionKeyDown(keyCode, inGame, battlePaused, stageClearPanelActive, startSpellPractice, spellPracticeEndFlg, spellPracticeEndActive);
        applyFireActionDown(keyCode);
        applyDpadMoveDown(keyCode, inGame);
        applyTenkeyMoveDown(keyCode, inGame);

        if (keyCode == -6 || keyCode == -21 || keyCode == -24) {
            softKeyStates |= GameCanvas.GAME_A_PRESSED;
        } else if (keyCode == -7 || keyCode == -22 || keyCode == -25) {
            if (inGame && !battlePaused) {
                softKeyStates |= GameCore.SOFTKEY_R_PRESSED;
            } else {
                softKeyStates |= GameCanvas.GAME_B_PRESSED;
            }
        }

        return true;
    }

    public boolean onKeyReleased(int keyCode,
            boolean inGame,
            boolean battlePaused) {

        if (inGame && !battlePaused && optTenkeyMove != 0 && isTenkeyMoveDirectionKeyCode(keyCode)) {
            applyTenkeyMoveUp(keyCode, inGame);
            return false; // keep legacy behavior: skip super.keyReleased
        }

        applyOptionKeyUp(keyCode, inGame, battlePaused);
        applyFireActionUp(keyCode);
        applyDpadMoveUp(keyCode, inGame);
        applyTenkeyMoveUp(keyCode, inGame);

        if (keyCode == -6 || keyCode == -21 || keyCode == -24) {
            softKeyStates &= ~GameCanvas.GAME_A_PRESSED;
        } else if (keyCode == -7 || keyCode == -22 || keyCode == -25) {
            softKeyStates &= ~(GameCanvas.GAME_B_PRESSED | GameCore.SOFTKEY_R_PRESSED);
        } else if (keyCode == 35) {
            extraKeyStates &= ~GameCore.KEY_POUND_PRESSED;
        }

        return true;
    }

    public FrameInput poll(boolean inGame,
            boolean battlePaused,
            boolean stageClearPanelActive,
            boolean startSpellPractice,
            boolean spellPracticeEndActive) {

        if (!inGame) {
            dpadKeyStates = 0;
            heldDpadMask = 0;
            softKeyStates &= ~GameCore.SOFTKEY_R_PRESSED;
            extraKeyStates &= ~GameCore.KEY_POUND_PRESSED;
        }

        if (!inGame || optTenkeyMove == 0) {
            tenkeyKeyStates = 0;
            heldTenkeyMask = 0;
        }

        int rawKeys = canvas.getKeyStates();
        if (inGame) {
            rawKeys &= ~(GameCanvas.UP_PRESSED | GameCanvas.DOWN_PRESSED | GameCanvas.LEFT_PRESSED | GameCanvas.RIGHT_PRESSED | GameCanvas.GAME_A_PRESSED);
        }

        if (inGame && !battlePaused && optTenkeyMove != 0 && heldTenkeyMask != 0) {
            rawKeys &= ~GameCanvas.GAME_C_PRESSED;
        }

        int fireKeys = 0;
        if (!inGame || battlePaused || stageClearPanelActive || (startSpellPractice && spellPracticeEndActive) || optShotKey == 0) {
            fireKeys = fireActionKeyStates;
        }

        int baseKeys = rawKeys | softKeyStates | dpadKeyStates | tenkeyKeyStates | fireKeys;
        if (inGame && !battlePaused) {
            if (optShotKey != 0 && !stageClearPanelActive && !(startSpellPractice && spellPracticeEndActive)) {
                baseKeys &= ~GameCanvas.FIRE_PRESSED;
            }
            if (optSlowKey != 0) {
                baseKeys &= ~GameCanvas.GAME_B_PRESSED;
            }
            baseKeys &= ~GameCanvas.GAME_C_PRESSED;
        }

        int keys = baseKeys | extraKeyStates;
        int pressed = keys & (~prevKeyStates);
        prevKeyStates = keys;

        int fireAction = fireActionKeyStates;
        int fireActionPressed = fireAction & (~prevFireActionKeyStates);
        prevFireActionKeyStates = fireAction;

        out.keys = keys;
        out.pressed = pressed;
        out.fireActionPressed = fireActionPressed;
        return out;
    }

    private int safeGameAction(int keyCode) {
        try {
            return canvas.getGameAction(keyCode);
        } catch (Throwable t) {
            return Integer.MIN_VALUE;
        }
    }

    private void applyFireActionDown(int keyCode) {
        int ga = safeGameAction(keyCode);
        if (ga == GameCanvas.FIRE) {
            fireActionKeyStates = GameCanvas.FIRE_PRESSED;
        }
    }

    private void applyFireActionUp(int keyCode) {
        int ga = safeGameAction(keyCode);
        if (ga == GameCanvas.FIRE) {
            fireActionKeyStates = 0;
        }
    }

    private static boolean isTenkeyNumericKeyCode(int keyCode) {
        return (keyCode >= 48 && keyCode <= 57) || keyCode == 42 || keyCode == 35;
    }

    private static boolean isTenkeyMoveDirectionKeyCode(int keyCode) {
        switch (keyCode) {
            case 49:
            case 50:
            case 51:
            case 52:
            case 54:
            case 55:
            case 56:
            case 57:
                return true;
            default:
                return false;
        }
    }

    private void applyDpadMoveDown(int keyCode, boolean inGame) {
        if (!inGame) {
            return;
        }
        if (isTenkeyNumericKeyCode(keyCode)) {
            return;
        }

        int ga = safeGameAction(keyCode);
        int bit = 0;
        if (ga == GameCanvas.UP) {
            bit = 1;
        } else if (ga == GameCanvas.DOWN) {
            bit = 2;
        } else if (ga == GameCanvas.LEFT) {
            bit = 4;
        } else if (ga == GameCanvas.RIGHT) {
            bit = 8;
        } else {
            return;
        }

        heldDpadMask |= bit;
        recalcDpadKeyStates();
    }

    private void applyDpadMoveUp(int keyCode, boolean inGame) {
        if (!inGame) {
            return;
        }
        if (isTenkeyNumericKeyCode(keyCode)) {
            return;
        }

        int ga = safeGameAction(keyCode);
        int bit = 0;
        if (ga == GameCanvas.UP) {
            bit = 1;
        } else if (ga == GameCanvas.DOWN) {
            bit = 2;
        } else if (ga == GameCanvas.LEFT) {
            bit = 4;
        } else if (ga == GameCanvas.RIGHT) {
            bit = 8;
        } else {
            return;
        }

        heldDpadMask &= ~bit;
        recalcDpadKeyStates();
    }

    private void recalcDpadKeyStates() {
        int m = heldDpadMask;
        int s = 0;
        if ((m & 1) != 0) {
            s |= GameCanvas.UP_PRESSED;
        }
        if ((m & 2) != 0) {
            s |= GameCanvas.DOWN_PRESSED;
        }
        if ((m & 4) != 0) {
            s |= GameCanvas.LEFT_PRESSED;
        }
        if ((m & 8) != 0) {
            s |= GameCanvas.RIGHT_PRESSED;
        }
        dpadKeyStates = s;
    }

    private static int keyCodeForOptionKey(int idx) {
        if (idx >= 1 && idx <= 9) {
            return 48 + idx;
        }
        if (idx == 11) {
            return 48;
        }
        if (idx == 10) {
            return 42;
        }
        if (idx == 12) {
            return 35;
        }
        return Integer.MIN_VALUE;
    }

    private void applyOptionKeyDown(int keyCode,
            boolean inGame,
            boolean battlePaused,
            boolean stageClearPanelActive,
            boolean startSpellPractice,
            boolean spellPracticeEndFlg,
            boolean spellPracticeEndActive) {

        if (!inGame || battlePaused || stageClearPanelActive || (startSpellPractice && spellPracticeEndActive)) {
            return;
        }
        if (optTenkeyMove != 0) {
            switch (keyCode) {
                case 49:
                case 50:
                case 51:
                case 52:
                case 54:
                case 55:
                case 56:
                case 57:
                    return;
            }
        }

        int ga = Integer.MIN_VALUE;
        if (optShotKey == 0 || optSlowKey == 0 || optSpellKey == 0) {
            ga = safeGameAction(keyCode);
        }

        if (optShotKey == 0 && ga == GameCanvas.FIRE && !isTenkeyNumericKeyCode(keyCode)) {
            extraKeyStates |= GameCanvas.FIRE_PRESSED;
        }
        int shotCode = keyCodeForOptionKey(optShotKey);
        if (shotCode != Integer.MIN_VALUE && keyCode == shotCode) {
            extraKeyStates |= GameCanvas.FIRE_PRESSED;
        }

        if (optSlowKey == 0 && ga == GameCanvas.FIRE && !isTenkeyNumericKeyCode(keyCode)) {
            extraKeyStates |= GameCanvas.GAME_B_PRESSED;
        }
        int slowCode = keyCodeForOptionKey(optSlowKey);
        if (slowCode != Integer.MIN_VALUE && keyCode == slowCode) {
            extraKeyStates |= GameCanvas.GAME_B_PRESSED;
        }

        if (optSpellKey == 0 && ga == GameCanvas.FIRE && !isTenkeyNumericKeyCode(keyCode)) {
            extraKeyStates |= GameCanvas.GAME_C_PRESSED;
        }
        int spellCode = keyCodeForOptionKey(optSpellKey);
        if (spellCode != Integer.MIN_VALUE && keyCode == spellCode) {
            extraKeyStates |= GameCanvas.GAME_C_PRESSED;
        }
    }

    private void applyOptionKeyUp(int keyCode, boolean inGame, boolean battlePaused) {
        if (!inGame || battlePaused) {
            return;
        }
        if (optTenkeyMove != 0) {
            switch (keyCode) {
                case 49:
                case 50:
                case 51:
                case 52:
                case 54:
                case 55:
                case 56:
                case 57:
                    return;
            }
        }

        int ga = Integer.MIN_VALUE;
        if (optShotKey == 0 || optSlowKey == 0 || optSpellKey == 0) {
            ga = safeGameAction(keyCode);
        }

        if (optShotKey == 0 && ga == GameCanvas.FIRE && !isTenkeyNumericKeyCode(keyCode)) {
            extraKeyStates &= ~GameCanvas.FIRE_PRESSED;
        }
        int shotCode = keyCodeForOptionKey(optShotKey);
        if (shotCode != Integer.MIN_VALUE && keyCode == shotCode) {
            extraKeyStates &= ~GameCanvas.FIRE_PRESSED;
        }

        if (optSlowKey == 0 && ga == GameCanvas.FIRE && !isTenkeyNumericKeyCode(keyCode)) {
            extraKeyStates &= ~GameCanvas.GAME_B_PRESSED;
        }
        int slowCode = keyCodeForOptionKey(optSlowKey);
        if (slowCode != Integer.MIN_VALUE && keyCode == slowCode) {
            extraKeyStates &= ~GameCanvas.GAME_B_PRESSED;
        }

        if (optSpellKey == 0 && ga == GameCanvas.FIRE && !isTenkeyNumericKeyCode(keyCode)) {
            extraKeyStates &= ~GameCanvas.GAME_C_PRESSED;
        }
        int spellCode = keyCodeForOptionKey(optSpellKey);
        if (spellCode != Integer.MIN_VALUE && keyCode == spellCode) {
            extraKeyStates &= ~GameCanvas.GAME_C_PRESSED;
        }
    }

    private void applyTenkeyMoveDown(int keyCode, boolean inGame) {
        if (!inGame || optTenkeyMove == 0) {
            return;
        }
        int bit = 0;
        switch (keyCode) {
            case 49: bit = 1; break;
            case 50: bit = 2; break;
            case 51: bit = 4; break;
            case 52: bit = 8; break;
            case 54: bit = 16; break;
            case 55: bit = 32; break;
            case 56: bit = 64; break;
            case 57: bit = 128; break;
            default: return;
        }
        heldTenkeyMask |= bit;
        recalcTenkeyKeyStates();
    }

    private void applyTenkeyMoveUp(int keyCode, boolean inGame) {
        if (!inGame || optTenkeyMove == 0) {
            return;
        }
        int bit = 0;
        switch (keyCode) {
            case 49: bit = 1; break;
            case 50: bit = 2; break;
            case 51: bit = 4; break;
            case 52: bit = 8; break;
            case 54: bit = 16; break;
            case 55: bit = 32; break;
            case 56: bit = 64; break;
            case 57: bit = 128; break;
            default: return;
        }
        heldTenkeyMask &= ~bit;
        recalcTenkeyKeyStates();
    }

    private void recalcTenkeyKeyStates() {
        int m = heldTenkeyMask;
        int s = 0;
        if ((m & (1 | 2 | 4)) != 0) {
            s |= GameCanvas.UP_PRESSED;
        }
        if ((m & (32 | 64 | 128)) != 0) {
            s |= GameCanvas.DOWN_PRESSED;
        }
        if ((m & (8 | 1 | 32)) != 0) {
            s |= GameCanvas.LEFT_PRESSED;
        }
        if ((m & (16 | 4 | 128)) != 0) {
            s |= GameCanvas.RIGHT_PRESSED;
        }
        tenkeyKeyStates = s;
    }
}