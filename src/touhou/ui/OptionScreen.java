package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.GameOptions;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class OptionScreen {
    public interface Listener {
        void onVolumesChanged(int bgmVol, int seVol);

        void onSeMaskChanged(int seMask);
    }

    private void renderCompatible(Graphics g, Font font) {
        g.setColor(0x00FF00);
        g.drawRect(10, 30, 220, 200);
        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_COMPAT_TITLE), 120, 22, 1, 16733525, 7820629);

        for (int i = 0; i < MENU_COMPAT.length; i++) {
            int main;
            int sh;
            if (i == cursorUD) {
                main = 0xFFFFFF;
                sh = 7829367;
            } else {
                main = 6710886;
                sh = 3355443;
            }
            UiDraw.drawString2(g, font, UiText.get(COMPAT_MENU_TEXT_IDS[i]), 40, 50 + i * 20, 0, main, sh);

            String v = compatValueText(i);
            if (v != null) {
                UiDraw.drawString2(g, font, v, 210, 50 + i * 20, 2, main, sh);
            }
        }

        UiDraw.drawString2(g, font, UiText.get(COMPAT_DESC_TEXT_IDS[cursorUD]), 120, 220, 1, 16711680, 7798784);
        g.setColor(colorPulse());
        g.drawLine(30, 54 + cursorUD * 20, 210, 54 + cursorUD * 20);
    }

    private void renderDebug(Graphics g, Font font) {
        g.setColor(0x00FF00);
        g.drawRect(10, 30, 220, 200);
        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_DEBUG_TITLE), 120, 22, 1, 16733525, 7820629);

        for (int i = 0; i < MENU_DEBUG.length; i++) {
            int main;
            int sh;
            if (i == cursorUD) {
                main = 0xFFFFFF;
                sh = 7829367;
            } else {
                main = 6710886;
                sh = 3355443;
            }
            UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_DEBUG_MENU_0 + i), 40, 50 + i * 20, 0, main, sh);

            String v = debugValueText(i);
            if (v != null) {
                UiDraw.drawString2(g, font, v, 210, 50 + i * 20, 2, main, sh);
            }
        }

        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_DEBUG_DESC_0 + cursorUD), 120, 220, 1, 16711680, 7798784);
        g.setColor(colorPulse());
        g.drawLine(30, 54 + cursorUD * 20, 210, 54 + cursorUD * 20);
    }

    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_TITLE = 1;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static final int SCENE_OPTION = 0;
    private static final int SCENE_GAME_CONFIG = 1;
    private static final int SCENE_SE_MENU = 2;
    private static final int SCENE_CHEATING = 3;
    private static final int SCENE_DEBUG = 4;
    private static final int SCENE_COMPATIBLE = 5;

    private static final int MENU_OPTION_COUNT = 8;

    private static final String[] MENU_SE = new String[] { "Nav", "Select", "Back", "CD", "Crash", "Graze", "Pause", "Crash2", "Power", "Life", "Shot", "Alice", "Reimu", "Marisa", "Back" };

    private static final String[] MENU_GAME = new String[] { "PlayerStock", "TenkeyMove", "ShotKey", "SlowKey", "SpellKey", "Compatible", "Cheating", "Back" };

    private static final String[] MENU_CHEAT = new String[] { "Boss Shield", "Item Collection Limit", "Back" };

    private static final String[] MENU_COMPAT = new String[] { "Close-up", "HeavyBG", "Mystia Fade", "Hit Spark", "Alpha: BOMB Close-up", "Alpha: BOSS Close-up", "Alpha: Player Bullets", "Back" };

    private static final int[] COMPAT_MENU_TEXT_IDS = new int[] {
            TextId.OPTION_COMPAT_MENU_0,
            TextId.OPTION_COMPAT_MENU_1,
            TextId.OPTION_COMPAT_MENU_2,
            TextId.OPTION_COMPAT_HIT_SPARK_MENU,
            TextId.OPTION_COMPAT_MENU_3,
            TextId.OPTION_COMPAT_MENU_4,
            TextId.OPTION_COMPAT_MENU_5,
            TextId.OPTION_COMPAT_MENU_6
    };

    private static final int[] COMPAT_DESC_TEXT_IDS = new int[] {
            TextId.OPTION_COMPAT_DESC_0,
            TextId.OPTION_COMPAT_DESC_1,
            TextId.OPTION_COMPAT_DESC_2,
            TextId.OPTION_COMPAT_HIT_SPARK_DESC,
            TextId.OPTION_COMPAT_DESC_3,
            TextId.OPTION_COMPAT_DESC_4,
            TextId.OPTION_COMPAT_DESC_5,
            TextId.OPTION_COMPAT_DESC_6
    };

    private static final String[] MENU_DEBUG = new String[] { "FPS", "Profiler", "Resource", "Back" };

    private static final String[] KEY_LABELS = new String[] { "[OK]", "[1]", "[2]", "[3]", "[4]", "[5]", "[6]", "[7]", "[8]", "[9]", "[*]", "[0]", "[#]", "OFF" };

    private int scene;
    private int cursorUD;
    private int count;

    private int[] opt;

    // Cheat UI: show a warning every time user enters Cheating scene.
    private boolean cheatWarnActive;

    private final Listener listener;

    public OptionScreen() {
        this(null);
    }

    public OptionScreen(Listener listener) {
        this.listener = listener;
    }

    public void enter() {
        opt = GameOptions.load();
        scene = SCENE_OPTION;
        cursorUD = 0;
        count = 0;

        notifyAudioOptionsChanged();
    }

    private void notifyAudioOptionsChanged() {
        if (listener == null || opt == null) {
            return;
        }
        listener.onVolumesChanged(opt[1], opt[2]);
        if (opt.length > GameOptions.IDX_SE_MASK_HI) {
            int mask = (opt[GameOptions.IDX_SE_MASK] & 0xFF) | ((opt[GameOptions.IDX_SE_MASK_HI] & 0xFF) << 8);
            listener.onSeMaskChanged(mask);
        }
    }

    public Result update(int pressed) {
        count = (count + 8) & 0xFF;
        if (opt == null) {
            opt = GameOptions.defaults();
        }

        if (scene == SCENE_CHEATING && cheatWarnActive) {
            if (pressed != 0) {
                cheatWarnActive = false;
            }
            return null;
        }

        if ((pressed & GameCanvas.GAME_A_PRESSED) != 0) {
            if (scene == SCENE_OPTION) {
                GameOptions.save(opt);
                return new Result(Result.KIND_BACK_TO_TITLE);
            }
            if (scene == SCENE_SE_MENU) {
                GameOptions.save(opt);
                scene = SCENE_OPTION;
                cursorUD = 2;
                return null;
            }
            if (scene == SCENE_CHEATING) {
                scene = SCENE_GAME_CONFIG;
                cursorUD = 6;
                return null;
            }
            if (scene == SCENE_DEBUG) {
                GameOptions.save(opt);
                scene = SCENE_OPTION;
                cursorUD = 5;
                return null;
            }
            if (scene == SCENE_COMPATIBLE) {
                GameOptions.save(opt);
                scene = SCENE_GAME_CONFIG;
                cursorUD = 5;
                return null;
            }
            scene = SCENE_OPTION;
            cursorUD = 4;
            return null;
        }

        if (scene == SCENE_OPTION) {
            return updateOption(pressed);
        }
        if (scene == SCENE_SE_MENU) {
            return updateSeMenu(pressed);
        }
        if (scene == SCENE_GAME_CONFIG) {
            return updateGameConfig(pressed);
        }
        if (scene == SCENE_CHEATING) {
            return updateCheating(pressed);
        }
        if (scene == SCENE_DEBUG) {
            return updateDebug(pressed);
        }
        if (scene == SCENE_COMPATIBLE) {
            return updateCompatible(pressed);
        }
        return null;
    }

    public void render(Graphics g) {
        if (g == null) {
            return;
        }

        g.setColor(0x000000);
        g.fillRect(0, 0, 240, 240);

        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        if (scene == SCENE_OPTION) {
            renderOption(g, font);
        } else if (scene == SCENE_GAME_CONFIG) {
            renderGameConfig(g, font);
        } else if (scene == SCENE_SE_MENU) {
            renderSeMenu(g, font);
        } else if (scene == SCENE_CHEATING) {
            renderCheating(g, font);
        } else if (scene == SCENE_DEBUG) {
            renderDebug(g, font);
        } else if (scene == SCENE_COMPATIBLE) {
            renderCompatible(g, font);
        } else {
            renderOption(g, font);
        }
    }

    private Result updateOption(int pressed) {
        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursorUD = (cursorUD + 1) % MENU_OPTION_COUNT;
            return null;
        }
        if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            cursorUD = (cursorUD - 1 + MENU_OPTION_COUNT) % MENU_OPTION_COUNT;
            return null;
        }

        if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
            adjustOptionValue(-1);
            return null;
        }
        if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
            adjustOptionValue(1);
            return null;
        }

        if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
            if (cursorUD == 2) {
                scene = SCENE_SE_MENU;
                cursorUD = 0;
                return null;
            }
            if (cursorUD == 4) {
                scene = SCENE_GAME_CONFIG;
                cursorUD = 0;
                return null;
            }
            if (cursorUD == 5) {
                scene = SCENE_DEBUG;
                cursorUD = 0;
                return null;
            }
            if (cursorUD == 6) {
                opt = GameOptions.defaults();
                GameOptions.save(opt);
                return null;
            }
            if (cursorUD == 7) {
                GameOptions.save(opt);
                return new Result(Result.KIND_BACK_TO_TITLE);
            }
        }
        return null;
    }

    private Result updateCompatible(int pressed) {
        if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            cursorUD = (cursorUD - 1 + MENU_COMPAT.length) % MENU_COMPAT.length;
            return null;
        }
        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursorUD = (cursorUD + 1) % MENU_COMPAT.length;
            return null;
        }

        if ((pressed & (GameCanvas.LEFT_PRESSED | GameCanvas.RIGHT_PRESSED | GameCanvas.FIRE_PRESSED)) != 0) {
            if (cursorUD == MENU_COMPAT.length - 1) {
                GameOptions.save(opt);
                scene = SCENE_GAME_CONFIG;
                cursorUD = 5;
                return null;
            }

            if (cursorUD == 0) {
                if (opt != null && opt.length > GameOptions.IDX_DYNAMIC_CUTIN) {
                    opt[GameOptions.IDX_DYNAMIC_CUTIN] ^= 1;
                }
            } else if (cursorUD == 1) {
                GameOptions.toggleHeavyBg(opt);
            } else if (cursorUD == 2) {
                GameOptions.toggleBlindMaskFade(opt);
            } else if (cursorUD == 3) {
                GameOptions.toggleHitSpark(opt);
            } else if (cursorUD == 4) {
                GameOptions.toggleBombOverlayAlpha(opt);
            } else if (cursorUD == 5) {
                GameOptions.toggleBossSpellCutInAlpha(opt);
            } else if (cursorUD == 6) {
                GameOptions.togglePlayerShotAlpha(opt);
            }
            GameOptions.saveFast(opt);
            return null;
        }

        return null;
    }

    private String seValueText(int idx) {
        if (idx == MENU_SE.length - 1) {
            return null;
        }
        if (opt == null || opt.length <= GameOptions.IDX_SE_MASK_HI) {
            return null;
        }
        int mask = (opt[GameOptions.IDX_SE_MASK] & 0xFF) | ((opt[GameOptions.IDX_SE_MASK_HI] & 0xFF) << 8);
        return ((mask & (1 << idx)) == 0) ? UiText.get(TextId.COMMON_OFF) : UiText.get(TextId.COMMON_ON);
    }

    private Result updateGameConfig(int pressed) {
        if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            cursorUD = (cursorUD - 1 + MENU_GAME.length) % MENU_GAME.length;
            return null;
        }
        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursorUD = (cursorUD + 1) % MENU_GAME.length;
            return null;
        }

        if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
            adjustGameValue(-1);
            return null;
        }
        if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
            adjustGameValue(1);
            return null;
        }

        if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
            if (cursorUD == MENU_GAME.length - 1) {
                scene = SCENE_OPTION;
                cursorUD = 4;
                GameOptions.save(opt);
                return null;
            }
            if (cursorUD == 0 || cursorUD == 1) {
                adjustGameValue(1);
                return null;
            }
            if (cursorUD == 2 || cursorUD == 3 || cursorUD == 4) {
                adjustGameValue(1);
                return null;
            }
            if (cursorUD == 5) {
                scene = SCENE_COMPATIBLE;
                cursorUD = 0;
                return null;
            }
            if (cursorUD == 6) {
                scene = SCENE_CHEATING;
                cursorUD = 0;
                cheatWarnActive = true;
                return null;
            }
        }

        return null;
    }

    private void adjustOptionValue(int dir) {
        if (cursorUD == 0) {
            opt[1] = clamp(opt[1] + dir * 10, 0, 100);
            GameOptions.saveFast(opt);
            if (listener != null) {
                listener.onVolumesChanged(opt[1], opt[2]);
            }
        } else if (cursorUD == 1) {
            opt[2] = clamp(opt[2] + dir * 10, 0, 100);
            GameOptions.saveFast(opt);
            if (listener != null) {
                listener.onVolumesChanged(opt[1], opt[2]);
            }
        } else if (cursorUD == 2) {
            // S.E. config entry (no value).
        } else if (cursorUD == 3) {
            opt[4] ^= 1;
            GameOptions.saveFast(opt);
        }
    }

    private Result updateCheating(int pressed) {
        if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            cursorUD = (cursorUD - 1 + MENU_CHEAT.length) % MENU_CHEAT.length;
            return null;
        }
        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursorUD = (cursorUD + 1) % MENU_CHEAT.length;
            return null;
        }

        if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
            adjustCheatValue(1);
            return null;
        }
        if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
            adjustCheatValue(1);
            return null;
        }

        if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
            if (cursorUD == 0) {
                adjustCheatValue(1);
                return null;
            }
            if (cursorUD == MENU_CHEAT.length - 1) {
                scene = SCENE_GAME_CONFIG;
                cursorUD = 6;
                return null;
            }
        }
        return null;
    }

    private void adjustCheatValue(int dir) {
        if (cursorUD == 0 && opt != null && opt.length > GameOptions.IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD) {
            opt[GameOptions.IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD] ^= 1;
            GameOptions.saveFast(opt);
            return;
        }
        if (cursorUD == 1 && opt != null && opt.length > GameOptions.IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT) {
            opt[GameOptions.IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT] ^= 1;
            GameOptions.saveFast(opt);
        }
    }

    private Result updateSeMenu(int pressed) {
        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            if (cursorUD == MENU_SE.length - 1) {
                return null;
            }
            int row = cursorUD % 7;
            int col = cursorUD / 7;
            if (row == 6) {
                cursorUD = MENU_SE.length - 1;
            } else {
                row = row + 1;
                cursorUD = row + col * 7;
            }
            return null;
        }
        if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            if (cursorUD == MENU_SE.length - 1) {
                cursorUD = 6;
                return null;
            }
            int row = cursorUD % 7;
            int col = cursorUD / 7;
            row = (row - 1 + 7) % 7;
            cursorUD = row + col * 7;
            return null;
        }
        if ((pressed & GameCanvas.LEFT_PRESSED) != 0) {
            if (cursorUD == MENU_SE.length - 1) {
                return null;
            }
            if (cursorUD >= 7) {
                cursorUD -= 7;
            }
            return null;
        }
        if ((pressed & GameCanvas.RIGHT_PRESSED) != 0) {
            if (cursorUD == MENU_SE.length - 1) {
                return null;
            }
            if (cursorUD < 7) {
                cursorUD += 7;
            }
            return null;
        }

        if ((pressed & GameCanvas.FIRE_PRESSED) != 0) {
            if (cursorUD == MENU_SE.length - 1) {
                GameOptions.save(opt);
                scene = SCENE_OPTION;
                cursorUD = 2;
                return null;
            }
            toggleSeBit(cursorUD);
            GameOptions.saveFast(opt);
            return null;
        }

        return null;
    }

    private void toggleSeBit(int seIdx) {
        if (seIdx < 0 || seIdx >= 14) {
            return;
        }
        if (opt == null || opt.length <= GameOptions.IDX_SE_MASK_HI) {
            return;
        }
        int mask = (opt[GameOptions.IDX_SE_MASK] & 0xFF) | ((opt[GameOptions.IDX_SE_MASK_HI] & 0xFF) << 8);
        mask ^= (1 << seIdx);
        opt[GameOptions.IDX_SE_MASK] = mask & 0xFF;
        opt[GameOptions.IDX_SE_MASK_HI] = (mask >> 8) & 0xFF;

        if (listener != null) {
            listener.onSeMaskChanged(mask);
        }
    }

    private void adjustGameValue(int dir) {
        if (cursorUD == 0) {
            opt[0] = clamp(opt[0] + dir, 1, 6);
        } else if (cursorUD == 1) {
            opt[5] ^= 1;
        } else if (cursorUD == 2) {
            opt[6] = (opt[6] + dir + 14) % 14;
        } else if (cursorUD == 3) {
            opt[7] = (opt[7] + dir + 14) % 14;
        } else if (cursorUD == 4) {
            opt[8] = (opt[8] + dir + 14) % 14;
        }
    }

    private Result updateDebug(int pressed) {
        if ((pressed & GameCanvas.UP_PRESSED) != 0) {
            cursorUD = (cursorUD - 1 + MENU_DEBUG.length) % MENU_DEBUG.length;
            return null;
        }
        if ((pressed & GameCanvas.DOWN_PRESSED) != 0) {
            cursorUD = (cursorUD + 1) % MENU_DEBUG.length;
            return null;
        }

        if ((pressed & (GameCanvas.LEFT_PRESSED | GameCanvas.RIGHT_PRESSED | GameCanvas.FIRE_PRESSED)) != 0) {
            if (cursorUD == MENU_DEBUG.length - 1) {
                GameOptions.save(opt);
                scene = SCENE_OPTION;
                cursorUD = 5;
                return null;
            }

            if (cursorUD == 0) {
                GameOptions.toggleDebugFps(opt);
            } else if (cursorUD == 1) {
                GameOptions.toggleDebugPerf(opt);
            } else if (cursorUD == 2) {
                GameOptions.toggleDebugResource(opt);
            }
            GameOptions.saveFast(opt);
            return null;
        }

        return null;
    }

    private void renderOption(Graphics g, Font font) {
        g.setColor(0x0000FF);
        g.drawRect(10, 30, 220, 200);
        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_TITLE), 120, 22, 1, 16733525, 7820629);

        for (int i = 0; i < MENU_OPTION_COUNT; i++) {
            int main;
            int sh;
            if (i == cursorUD) {
                main = 0xFFFFFF;
                sh = 7829367;
            } else {
                main = 6710886;
                sh = 3355443;
            }
            UiDraw.drawString2(g, font, optionMenuText(i), 40, 50 + i * 20, 0, main, sh);

            String v = optionValueText(i);
            if (v != null) {
                UiDraw.drawString2(g, font, v, 210, 50 + i * 20, 2, main, sh);
            }
        }

        UiDraw.drawString2(g, font, optionDescText(cursorUD), 120, 220, 1, 16711680, 7798784);
        g.setColor(colorPulse());
        g.drawLine(30, 54 + cursorUD * 20, 210, 54 + cursorUD * 20);
    }

    private static String optionMenuText(int idx) {
        switch (idx) {
            case 0:
                return UiText.get(TextId.OPTION_MENU_BGM_VOL);
            case 1:
                return UiText.get(TextId.OPTION_MENU_SE_VOL);
            case 2:
                return UiText.get(TextId.OPTION_MENU_SE_CONFIG);
            case 3:
                return UiText.get(TextId.OPTION_MENU_VIBRATION);
            case 4:
                return UiText.get(TextId.OPTION_MENU_GAME_CONFIG);
            case 5:
                return UiText.get(TextId.OPTION_MENU_DEBUG);
            case 6:
                return UiText.get(TextId.OPTION_MENU_DEFAULT);
            case 7:
                return UiText.get(TextId.OPTION_MENU_BACK);
            default:
                return "";
        }
    }

    private static String optionDescText(int idx) {
        switch (idx) {
            case 0:
                return UiText.get(TextId.OPTION_DESC_BGM_VOL);
            case 1:
                return UiText.get(TextId.OPTION_DESC_SE_VOL);
            case 2:
                return UiText.get(TextId.OPTION_DESC_SE_CONFIG);
            case 3:
                return UiText.get(TextId.OPTION_DESC_VIBRATION);
            case 4:
                return UiText.get(TextId.OPTION_DESC_GAME_CONFIG);
            case 5:
                return UiText.get(TextId.OPTION_DESC_DEBUG);
            case 6:
                return UiText.get(TextId.OPTION_DESC_DEFAULT);
            case 7:
                return UiText.get(TextId.OPTION_DESC_BACK);
            default:
                return "";
        }
    }

    private void renderSeMenu(Graphics g, Font font) {
        g.setColor(0xFF00FF);
        g.drawRect(10, 30, 220, 200);
        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_SE_TITLE), 120, 22, 1, 16733525, 7820629);

        for (int i = 0; i < MENU_SE.length - 1; i++) {
            int main;
            int sh;
            if (i == cursorUD) {
                main = 0xFFFFFF;
                sh = 7829367;
            } else {
                main = 6710886;
                sh = 3355443;
            }

            int row = i % 7;
            int col = i / 7;
            int x = (col == 0) ? 25 : 125;
            int y = 50 + row * 20;
            UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_SE_MENU_0 + i), x, y, 0, main, sh);

            String v = seValueText(i);
            if (v != null) {
                UiDraw.drawString2(g, font, v, x + 80, y, 2, main, sh);
            }
        }

        int backIdx = MENU_SE.length - 1;
        int backMain;
        int backSh;
        if (cursorUD == backIdx) {
            backMain = 0xFFFFFF;
            backSh = 7829367;
        } else {
            backMain = 6710886;
            backSh = 3355443;
        }
        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_SE_MENU_0 + backIdx), 120, 192, 1, backMain, backSh);

        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_SE_DESC_0 + cursorUD), 120, 220, 1, 16711680, 7798784);
        g.setColor(colorPulse());
        if (cursorUD == backIdx) {
            g.drawLine(30, 196, 210, 196);
        } else {
            int row = cursorUD % 7;
            int col = cursorUD / 7;
            int y = 54 + row * 20;
            if (col == 0) {
                g.drawLine(15, y, 110, y);
            } else {
                g.drawLine(115, y, 225, y);
            }
        }
    }

    private void renderGameConfig(Graphics g, Font font) {
        g.setColor(0x00FF00);
        g.drawRect(10, 30, 220, 200);
        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_GAME_TITLE), 120, 22, 1, 16733525, 7820629);

        for (int i = 0; i < MENU_GAME.length; i++) {
            int main;
            int sh;
            if (i == cursorUD) {
                main = 0xFFFFFF;
                sh = 7829367;
            } else {
                main = 6710886;
                sh = 3355443;
            }
            UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_GAME_MENU_0 + i), 40, 50 + i * 20, 0, main, sh);

            String v = gameValueText(i);
            if (v != null) {
                UiDraw.drawString2(g, font, v, 210, 50 + i * 20, 2, main, sh);
            }
        }

        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_GAME_DESC_0 + cursorUD), 120, 220, 1, 16711680, 7798784);
        g.setColor(colorPulse());
        g.drawLine(30, 54 + cursorUD * 20, 210, 54 + cursorUD * 20);
    }

    private void renderCheating(Graphics g, Font font) {
        g.setColor(0x00FF00);
        g.drawRect(10, 30, 220, 200);
        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_CHEAT_TITLE), 120, 22, 1, 16733525, 7820629);

        for (int i = 0; i < MENU_CHEAT.length; i++) {
            int main;
            int sh;
            if (i == cursorUD) {
                main = 0xFFFFFF;
                sh = 7829367;
            } else {
                main = 6710886;
                sh = 3355443;
            }
            UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_CHEAT_MENU_0 + i), 40, 50 + i * 20, 0, main, sh);

            String v = cheatValueText(i);
            if (v != null) {
                UiDraw.drawString2(g, font, v, 210, 50 + i * 20, 2, main, sh);
            }
        }

        UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_CHEAT_DESC_0 + cursorUD), 120, 220, 1, 16711680, 7798784);
        g.setColor(colorPulse());
        g.drawLine(30, 54 + cursorUD * 20, 210, 54 + cursorUD * 20);

        if (cheatWarnActive) {
            g.setColor(0x000000);
            g.fillRect(20, 80, 200, 80);
            g.setColor(0xFFFFFF);
            g.drawRect(20, 80, 200, 80);

            // Warning text: keep short using half-width to fit.
            int boxY = 80;
            int boxH = 80;
            int lineH = UiDraw.fontHeight(font);
            int baseline = UiDraw.fontBaseline(font);
            int gapY = 2;
            int totalTextH = lineH * 3 + gapY * 2;
            int padY = (boxH - totalTextH) >> 1;
            if (padY < 0) {
                padY = 0;
            }

            int y1 = boxY + padY + baseline;
            int y2 = y1 + lineH + gapY;
            int y3 = y2 + lineH + gapY;

            UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_CHEAT_WARN_TITLE), 120, y1, 1, 0xFF0000, 0x000000);
            UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_CHEAT_WARN_LINE1), 120, y2, 1, 0xFFFFFF, 0x000000);
            UiDraw.drawString2(g, font, UiText.get(TextId.OPTION_CHEAT_WARN_LINE2), 120, y3, 1, 0xFFFFFF, 0x000000);
        }
    }

    private String cheatValueText(int idx) {
        if (idx == 0 && opt != null && opt.length > GameOptions.IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD) {
            // UI semantics: ON = boss shield enabled (normal); OFF = shield disabled (cheat active).
            return (opt[GameOptions.IDX_CHEAT_DISABLE_BOSS_BOMB_SHIELD] == 0) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == 1 && opt != null && opt.length > GameOptions.IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT) {
            // UI semantics: ON = power-gated top-line collection (normal); OFF = no limit (cheat active).
            return (opt[GameOptions.IDX_CHEAT_DISABLE_ITEM_COLLECT_LIMIT] == 0) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        return null;
    }

    private String optionValueText(int idx) {
        if (idx == 0) {
            if (opt[1] == 0) {
                return UiText.get(TextId.COMMON_OFF);
            }
            return String.valueOf(opt[1]);
        }
        if (idx == 1) {
            return String.valueOf(opt[2]);
        }
        if (idx == 2) {
            return UiText.get(TextId.COMMON_NEXT);
        }
        if (idx == 3) {
            return (opt[4] == 0) ? UiText.get(TextId.COMMON_OFF) : UiText.get(TextId.COMMON_ON);
        }
        if (idx == 4) {
            return UiText.get(TextId.COMMON_NEXT);
        }
        if (idx == 5) {
            return UiText.get(TextId.COMMON_NEXT);
        }
        return null;
    }

    private String compatValueText(int idx) {
        if (idx == 0 && opt != null && opt.length > GameOptions.IDX_DYNAMIC_CUTIN) {
            return (opt[GameOptions.IDX_DYNAMIC_CUTIN] == 0) ? UiText.get(TextId.COMMON_OFF) : UiText.get(TextId.COMMON_ON);
        }
        if (idx == 1) {
            return GameOptions.isHeavyBgEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == 2) {
            return GameOptions.isBlindMaskFadeEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == 3) {
            return GameOptions.isHitSparkEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == 4) {
            return GameOptions.isBombOverlayAlphaEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == 5) {
            return GameOptions.isBossSpellCutInAlphaEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == 6) {
            return GameOptions.isPlayerShotAlphaEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == MENU_COMPAT.length - 1) {
            return null;
        }
        return null;
    }

    private String debugValueText(int idx) {
        if (idx == 0) {
            return GameOptions.isDebugFpsEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == 1) {
            return GameOptions.isDebugPerfEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        if (idx == 2) {
            return GameOptions.isDebugResourceEnabled(opt) ? UiText.get(TextId.COMMON_ON) : UiText.get(TextId.COMMON_OFF);
        }
        return null;
    }

    private String gameValueText(int idx) {
        if (idx == 0) {
            return String.valueOf(opt[0]);
        }
        if (idx == 1) {
            return (opt[5] == 0) ? UiText.get(TextId.COMMON_OFF) : UiText.get(TextId.COMMON_ON);
        }
        if (idx == 2) {
            return keyLabel(opt[6]);
        }
        if (idx == 3) {
            return keyLabel(opt[7]);
        }
        if (idx == 4) {
            return keyLabel(opt[8]);
        }
        if (idx == 5) {
            return UiText.get(TextId.COMMON_NEXT);
        }
        if (idx == 6) {
            return UiText.get(TextId.COMMON_NEXT);
        }
        return null;
    }

    private static String keyLabel(int idx) {
        if (idx < 0 || idx >= KEY_LABELS.length) {
            return UiText.get(TextId.COMMON_OFF);
        }
        return KEY_LABELS[idx];
    }

    private int colorPulse() {
        int v = count % 127;
        int base = (count < 127) ? (127 + v) : (127 + (127 - v));
        int r = base & 0xFF;
        int gg = base & 0xFF;
        int bb = base & 0xFF;
        return (r << 16) | (gg << 8) | bb;
    }

    private static int clamp(int v, int lo, int hi) {
        if (v < lo) {
            return lo;
        }
        if (v > hi) {
            return hi;
        }
        return v;
    }
}
