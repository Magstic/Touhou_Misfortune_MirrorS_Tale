package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import touhou.GameCore;
import touhou.ImageBank;
import touhou.i18n.I18n;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class EndingScreen {

    public static final class Result {
        public static final int KIND_DONE = 1;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static final String[] ENDING_TEXT_FILES = new String[] { "ending_reimu.txt", "ending_marisa.txt", "ending_alice.txt" };

    private static final Font FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    private static final int BG_MODE_RESET = 0;
    private static final int BG_MODE_BLACK = 1;
    private static final int BG_MODE_WHITE = 2;

    private static final int PIC_X = 20;
    private static final int PIC_Y = 24;
    private static final int PIC_W = 200;
    private static final int PIC_H = 150;

    private static final int TEXT_X = 10;
    private static final int TEXT_Y = 198;
    private static final int TEXT_LINE_H = 14;

    // Extracted from DoJa a.ul (ending scripts).
    // Index 0..2 = Reimu/Marisa/Alice.
    private static final String[][] ENDING_SCRIPTS = new String[][] {
            // Reimu
            new String[] {
                    "BG:WHITE",
                    "BG:CHANGE",
                    "IMG:REIMU_1",
                    "BG:RESET",
                    "ここは幻想郷の境にある博麗神社。",
                    "IMG:REIMU_2",
                    "BG:RESET",
                    "魔理沙「で、どうしてみんなここに集ま|ってるんだ。」",
                    "アリス「あんたが騒ぎを起こしたからで|しょ。」",
                    "霊夢「で、さっきのアレは何だったの？|」",
                    "魔理沙「あー、あれはだ。話すと長くな|るから後で…じゃあダメか？」",
                    "霊夢「ダメよ、お茶を淹れてくるから長|くなっても全部話しなさい。」",
                    "忘れ去られたものが辿り着く幻想郷|古くなり、使われなくなった道具は|いつしか意思を持つことがある。",
                    "一連の騒動は、魔理沙が紅魔館に安置さ|れていたはずの不思議な力の宿った鏡を|無断で持ち出し、",
                    "それを覗き込んでしまったことが原因だ|った。",
                    "他者の姿を映すのみで自らの姿を持つこ|との無い鏡。",
                    "鏡に宿った意思は鏡に秘められた願いを|叶えようとしたのかもしれない。",
                    "魔理沙「無断で持ち出したというか借り|たんだけどな。」",
                    "咲夜「それで、その鏡はどこにあるの？|」",
                    "魔理沙「あれならまだうちにあると思う|が。」",
                    "IMG:REIMU_3",
                    "こうして鏡は紅魔館に戻され、騒動は|ひと段落すると思われたが…。",

                    "EDEND:1"
            },

            // Marisa
            new String[] {
                    "BG:WHITE",
                    "BG:CHANGE",
                    "IMG:MARISA_1",
                    "BG:RESET",
                    "ここは魔法の森にある魔理沙の家。",
                    "もう１人の魔理沙は消え去り、魔理沙に|かかっていた呪いは消えつつあった。",
                    "IMG:MARISA_2",
                    "BG:RESET",
                    "魔理沙「何とかアイツにばれる前に何と|かなったな。」",
                    "霊夢「アイツって誰のこと？」",
                    "アリス「あ、魔理沙。戻ってきたのね。|」",
                    "騒動のきっかけは魔理沙が紅魔館から持|ち出した鏡にあった。",
                    "自身の姿を持たないまま年月を重ねた鏡|はいつしか自分の姿を持つことを願う。",
                    "魔力の篭った物は魔法使いにとっては|強く好奇心が刺激される。",
                    "それゆえに不思議な力の宿った鏡を覗い|てしまったことが始まりだった。",
                    "アリス「で、あのもう１人の魔理沙は何|だったの？」",
                    "魔理沙「紅魔館から",
                    "きた鏡を覗|き込んだら出てきたんだ。」",
                    "霊夢「どうせ勝手に持ち出したんでしょ。|何の鏡だか持ち主に相談したほうが良いわ|ね。」",
                    "アリス「それで、その鏡はどこにあるの？|」",
                    "魔理沙「その鏡ならそこにあるぜ。」",
                    "IMG:MARISA_3",
                    "BG:RESET",
                    "霊夢「ってあんた達何やってんのよ！」",
                    "鏡の騒動はもう少し続く。",

                    "EDEND:2"
            },

            // Alice
            new String[] {
                    "BG:WHITE",
                    "BG:CHANGE",
                    "IMG:ALICE_1",
                    "BG:RESET",
                    "アリス「魔理沙、今の魔理沙は何だった|の？」",
                    "魔理沙「魔理沙魔理沙言われると変な感|じだな。」",
                    "魔理沙「あれは紅魔館で見つけた鏡を調|べてたら出てきたんだ。」",
                    "魔理沙「どういった原理なのかは調べて|みないとわからないが。」",
                    "アリス「あの館で見つけた物が原因なら|詳しそうなのがいるわね。」",
                    "アリス「というかメイドが探してたみた|いよ？」",
                    "アリス「今回の件ももう知ってるんじゃ|ないかしら。」",
                    "一連の騒動の原因は魔理沙が持ち出した|鏡にあった。",
                    "本来なら同一の人物が同時に存在するこ|とは出来ない。",
                    "その鏡によって姿を奪われたものは存在|が曖昧になり、",
                    "放っておけば鏡の中の自分と入れ替わっ|てしまうことになる。",
                    "IMG:ALICE_2",
                    "BG:RESET",
                    "アリス「実はさっきまで何だか魔理沙の|記憶が変だったのよね。」",
                    "アリス「今は落ち着いたみたいだけど、|本当はすごく危険な状況だったんじゃな|いかしら？」",
                    "魔理沙「そうだったのか。あの鏡を調べ|れば何かわかるかもしれないな。」",
                    "IMG:ALICE_3",
                    "こうして鏡を調べることにした二人だが|それが再び騒動を引き起こすことになる|ことをまだ知らない。",

                    "EDEND:3"
            }
    };

    private int chara;
    private boolean qualifiesNextExtra;

    private int line;
    private int reveal;

    private int bgMode;
    private int bgFade;
    private int bgFadeMode;

    private int currImgIndex;
    private int nextImgIndex;

    private int imgFade;

    private int[] currRgb;
    private int[] nextRgb;
    private int[] blendRgb;

    // I18N overlay: store localized visible lines (non-directives) and map script line -> localized index.
    private String[] i18nTextLines;
    private int[] i18nTextIndexByScriptLine;

    private ImageBank imgs;

    // Enter ending playback.
    public void enter(int chara, boolean qualifiesNextExtra, ImageBank imgs) {
        if (chara < 0) {
            chara = 0;
        }
        if (chara >= ENDING_SCRIPTS.length) {
            chara = ENDING_SCRIPTS.length - 1;
        }

        this.chara = chara;
        this.qualifiesNextExtra = qualifiesNextExtra;
        this.imgs = imgs;

        i18nTextLines = loadEndingTextLines(chara);
        i18nTextIndexByScriptLine = buildTextIndexMap(ENDING_SCRIPTS[chara]);

        line = 0;
        reveal = 0;

        bgMode = BG_MODE_RESET;
        bgFade = 0;
        bgFadeMode = BG_MODE_RESET;

        currImgIndex = -1;
        nextImgIndex = -1;
        imgFade = 0;

        currRgb = null;
        nextRgb = null;
        blendRgb = null;

        consumeDirectives();
    }

    private static String[] loadEndingTextLines(int chara) {
        if (chara < 0 || chara >= ENDING_TEXT_FILES.length) {
            return null;
        }
        String text = I18n.readUtf8TextResource(I18n.path(ENDING_TEXT_FILES[chara]));
        if (text == null) {
            return null;
        }
        String[] lines = I18n.splitLinesSimple(text);
        return lines;
    }

    private static int[] buildTextIndexMap(String[] script) {
        if (script == null) {
            return null;
        }
        int[] out = new int[script.length];
        int n = 0;
        for (int i = 0; i < script.length; i++) {
            String s = script[i];
            if (startsWith(s, "IMG:") || startsWith(s, "BG:") || startsWith(s, "EDEND:")) {
                out[i] = -1;
            } else {
                out[i] = n++;
            }
        }
        return out;
    }

    // Update ending playback. FIRE advances.
    public Result update(int pressed) {
        consumeDirectives();

        if (imgFade > 0) {
            imgFade--;
            if (imgFade == 0 && nextImgIndex >= 0) {
                currImgIndex = nextImgIndex;
                currRgb = nextRgb;
                nextImgIndex = -1;
                nextRgb = null;
            }
        }

        if (bgFade > 0) {
            bgFade--;
        }

        String s = currentLine();
        if (s == null) {
            return new Result(Result.KIND_DONE);
        }

        if (reveal < s.length()) {
            reveal++;
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                reveal = s.length();
            }
        } else {
            if ((pressed & GameCore.FIRE_PRESSED) != 0) {
                if (line >= ENDING_SCRIPTS[chara].length - 1) {
                    return new Result(Result.KIND_DONE);
                }
                line++;
                reveal = 0;
                consumeDirectives();
            }
        }

        // Allow quick exit with left softkey (GAME_A).
        if ((pressed & GameCore.GAME_A_PRESSED) != 0) {
            return new Result(Result.KIND_DONE);
        }

        return null;
    }

    public void render(Graphics g, ImageBank imgs) {
        g.setColor(0x000000);
        g.fillRect(0, 0, 240, 240);

        if (bgMode == BG_MODE_WHITE || imgFade > 0) {
            g.setColor(0xFFFFFF);
            g.fillRect(PIC_X, PIC_Y, PIC_W, PIC_H);
        }

        if (imgs != null) {
            this.imgs = imgs;
        }

        renderPicture(g, this.imgs);
        renderBgFade(g);
        renderText(g);
    }

    private void renderPicture(Graphics g, ImageBank imgs) {
        if (currImgIndex < 0) {
            return;
        }

        if (imgFade <= 0 || nextImgIndex < 0 || currRgb == null || nextRgb == null) {
            Image img = (imgs != null) ? imgs.get(currImgIndex) : null;
            if (img != null) {
                UiDraw.drawRegion(g, img, PIC_X, PIC_Y, 0, 0, PIC_W, PIC_H);
            }
            return;
        }

        int w = PIC_W;
        int h = PIC_H;

        int total = 30;
        int t = total - imgFade;
        if (t < 0) {
            t = 0;
        }
        if (t > total) {
            t = total;
        }

        if (blendRgb == null || blendRgb.length != w * h) {
            try {
                blendRgb = new int[w * h];
            } catch (Throwable t2) {
                blendRgb = null;
                return;
            }
        }

        int aNew = (t * 255) / total;
        int aOld = 255 - aNew;

        for (int i = 0; i < blendRgb.length; i++) {
            int p0 = currRgb[i];
            int p1 = nextRgb[i];

            int r0 = (p0 >> 16) & 0xFF;
            int g0 = (p0 >> 8) & 0xFF;
            int b0 = (p0) & 0xFF;

            int r1 = (p1 >> 16) & 0xFF;
            int g1 = (p1 >> 8) & 0xFF;
            int b1 = (p1) & 0xFF;

            int r = (r0 * aOld + r1 * aNew) / 255;
            int gg = (g0 * aOld + g1 * aNew) / 255;
            int bb = (b0 * aOld + b1 * aNew) / 255;

            blendRgb[i] = (0xFF << 24) | (r << 16) | (gg << 8) | bb;
        }

        try {
            g.drawRGB(blendRgb, 0, w, PIC_X, PIC_Y, w, h, true);
        } catch (Throwable ignore) {
        }
    }

    private void renderBgFade(Graphics g) {
        if (bgFade <= 0) {
            return;
        }
        if (bgFadeMode == BG_MODE_RESET) {
            return;
        }

        int t = 30 - bgFade;
        if (t < 0) {
            t = 0;
        }
        if (t > 30) {
            t = 30;
        }

        int u = (t <= 15) ? t : (30 - t);

        int w = (u * PIC_W) / 15;
        int h = (u * PIC_H) / 15;
        if (w <= 0 || h <= 0) {
            return;
        }
        int x = PIC_X + ((PIC_W - w) >> 1);
        int y = PIC_Y + ((PIC_H - h) >> 1);

        int rgb = (bgFadeMode == BG_MODE_BLACK) ? 0x000000 : 0xFFFFFF;
        g.setColor(rgb);
        g.fillRect(x, y, w, h);
    }

    private void renderText(Graphics g) {
        String s = currentLine();
        if (s == null) {
            return;
        }

        if (startsWith(s, "EDEND:")) {
            String suffix = substringAfterColon(s);
            String[] out = new String[3];

            if (qualifiesNextExtra) {
                out[0] = "";
                out[1] = UiText.get(TextId.ENDING_NUMBER) + suffix;
                out[2] = UiText.get(TextId.ENDING_EXTRA_UNLOCK);
            } else {
                out[0] = UiText.get(TextId.ENDING_NUMBER) + suffix;
                out[1] = UiText.get(TextId.ENDING_LINE_1);
                out[2] = UiText.get(TextId.ENDING_LINE_2);
            }

            g.setColor(0xFF0000);
            for (int i = 0; i < 3; i++) {
                UiDraw.drawStringPlain(g, FONT, out[i], TEXT_X, TEXT_Y + TEXT_LINE_H * i, 0, 0xFF0000);
            }
            return;
        }

        if (startsWith(s, "IMG:") || startsWith(s, "BG:")) {
            return;
        }

        String shown = safeSubstring(s, 0, reveal);
        g.setColor(0xFFFFFF);

        for (int i = 0; i < 4; i++) {
            int bar = shown.indexOf('|');
            String lineText;
            if (bar >= 0) {
                lineText = shown.substring(0, bar);
                shown = shown.substring(bar + 1);
            } else {
                lineText = shown;
                shown = "";
            }
            UiDraw.drawStringPlain(g, FONT, lineText, TEXT_X, TEXT_Y + TEXT_LINE_H * i, 0, 0xFFFFFF);
        }
    }

    private void consumeDirectives() {
        for (;;) {
            String s = currentLine();
            if (s == null) {
                return;
            }

            if (startsWith(s, "IMG:")) {
                String key = substringAfterColon(s);
                int idx = imgIndexForKey(key);
                if (idx >= 0) {
                    setImage(idx);
                }
                line++;
                continue;
            }

            if (startsWith(s, "BG:")) {
                String key = substringAfterColon(s);
                if (equals(key, "RESET")) {
                    bgMode = BG_MODE_RESET;
                } else if (equals(key, "BLACK")) {
                    bgMode = BG_MODE_BLACK;
                } else if (equals(key, "WHITE")) {
                    bgMode = BG_MODE_WHITE;
                } else if (equals(key, "CHANGE")) {
                    bgFade = 0;
                    bgFadeMode = BG_MODE_RESET;
                }
                line++;
                continue;
            }

            return;
        }
    }

    private void setImage(int idx) {
        if (idx < 0) {
            return;
        }

        if (currImgIndex < 0) {
            currImgIndex = idx;
            currRgb = loadRgb(idx);
            return;
        }

        if (idx == currImgIndex) {
            return;
        }

        nextImgIndex = idx;
        nextRgb = loadRgb(idx);
        if (nextRgb != null) {
            imgFade = 30;
        } else {
            currImgIndex = idx;
            currRgb = null;
            nextImgIndex = -1;
            nextRgb = null;
            imgFade = 0;
        }
    }

    private int[] loadRgb(int imgIndex) {
        if (imgs == null) {
            return null;
        }

        Image img = imgs.get(imgIndex);
        if (img == null) {
            return null;
        }

        int w;
        int h;
        try {
            w = img.getWidth();
            h = img.getHeight();
        } catch (Throwable t) {
            return null;
        }

        if (w < PIC_W || h < PIC_H) {
            return null;
        }

        int[] rgb;
        try {
            rgb = new int[PIC_W * PIC_H];
            img.getRGB(rgb, 0, PIC_W, 0, 0, PIC_W, PIC_H);
        } catch (Throwable t) {
            return null;
        }

        for (int i = 0; i < rgb.length; i++) {
            rgb[i] |= (0xFF << 24);
        }
        return rgb;
    }

    private static int imgIndexForKey(String key) {
        if (equals(key, "REIMU_1")) {
            return 86;
        }
        if (equals(key, "REIMU_2")) {
            return 87;
        }
        if (equals(key, "REIMU_3")) {
            return 88;
        }
        if (equals(key, "MARISA_1")) {
            return 89;
        }
        if (equals(key, "MARISA_2")) {
            return 90;
        }
        if (equals(key, "MARISA_3")) {
            return 91;
        }
        if (equals(key, "ALICE_1")) {
            return 92;
        }
        if (equals(key, "ALICE_2")) {
            return 93;
        }
        if (equals(key, "ALICE_3")) {
            return 94;
        }
        return -1;
    }

    private String currentLine() {
        if (line < 0) {
            return null;
        }
        String[] s = ENDING_SCRIPTS[chara];
        if (line >= s.length) {
            return null;
        }
        String raw = s[line];
        if (i18nTextLines != null && i18nTextIndexByScriptLine != null && line < i18nTextIndexByScriptLine.length) {
            int idx = i18nTextIndexByScriptLine[line];
            if (idx >= 0 && idx < i18nTextLines.length) {
                return i18nTextLines[idx];
            }
        }
        return raw;
    }

    private static boolean startsWith(String s, String prefix) {
        if (s == null || prefix == null) {
            return false;
        }
        if (s.length() < prefix.length()) {
            return false;
        }
        return s.substring(0, prefix.length()).equals(prefix);
    }

    private static String substringAfterColon(String s) {
        if (s == null) {
            return "";
        }
        int p = s.indexOf(':');
        if (p < 0) {
            return "";
        }
        if (p + 1 >= s.length()) {
            return "";
        }
        return s.substring(p + 1);
    }

    private static String safeSubstring(String s, int from, int to) {
        if (s == null) {
            return "";
        }
        if (from < 0) {
            from = 0;
        }
        if (to < from) {
            to = from;
        }
        if (to > s.length()) {
            to = s.length();
        }
        return s.substring(from, to);
    }

    private static boolean equals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}