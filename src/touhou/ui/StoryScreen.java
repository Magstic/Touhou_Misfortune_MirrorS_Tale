package touhou.ui;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.ImageBank;
import touhou.i18n.I18n;
import touhou.i18n.TextId;
import touhou.i18n.UiText;

public final class StoryScreen {
    public static final class Result {
        public static final int KIND_NONE = 0;
        public static final int KIND_BACK_TO_TITLE = 1;

        public final int kind;

        public Result(int kind) {
            this.kind = kind;
        }
    }

    private static String[] loadStoryLines() {
        String text = I18n.readUtf8TextResource(I18n.path("story.txt"));
        if (text == null) {
            return STORY_LINES;
        }
        String[] lines = I18n.splitLinesSimple(text);
        return (lines != null) ? lines : STORY_LINES;
    }

    private static final int BG_IMG_INDEX = 54;

    private static final int W = 240;
    private static final int H = 240;

    private static final int TEXT_X = 12;
    private static final int TEXT_Y = 32;
    private static final int TEXT_W = 216;
    private static final int TEXT_H = 196;

    private static final String[] STORY_LINES = new String[] {
            "幻想郷は平和だった。",
            "",
            "博麗神社の巫女、博麗霊夢もまた、平和にお茶を飲んでいた。",
            "相変わらず参拝客の居ない神社ではすることもない。",
            "ここ数日は大結界の様子を思い出したように眺めつつ、お茶をすするのが日課となっていた。",
            "その寂れた境内に見慣れた姿が一つ。黒い魔法帽を被って手を振る姿は、確か、",
            "",
            "霊夢「魔理沙……よね？　うん、魔理沙だわ」",
            "魔理沙「うん？　私は私だぜ？」",
            "霊夢「うーん、ぼけたのかしら。一瞬名前が出てこなくて」",
            "魔理沙「怠けてるから脳が回ってないんじゃないのか？　最近は何もなくて平和だからな」",
            "霊夢「何もないほうがいいに決まってるわ。それとも厄介ごとが多いほうがいいっていうの？」",
            "魔理沙「いや、私も忙しいのは御免被るぜ」",
            "霊夢「ほら、だから今のままでいいの。ただでさえ最近忙しかったんだから」",
            "",
            "巫女は本来神社に務めるものであり、便利屋ではない。ここのところの事件の多さから",
            "本業と副業が逆転していたが、これこそが本来の形なのである。",
            "もっとも、戻したから参拝客が増えるわけではない。相変わらず巫女の仕事は開店休業だった。",
            "",
            "霊夢「ところで、何か用があってきたんじゃないの？」",
            "魔理沙「いや、しいて言うなら帰りに茶を飲みたくなって立ち寄っただけだぜ？」",
            "霊夢「ここなら飲めるとでも？」",
            "魔理沙「そう思うから来たんじゃないか。生憎、外したようだけどな」",
            "霊夢「まったく……茶菓子まで期待してたら追い出してたところだけどね、はい」",
            "魔理沙「お、すまんな」",
            "",
            "注がれたばかりのお茶に口をつけながら、魔理沙が取り出した一冊の本。",
            "霊夢が呆れてため息をついた。年季の入った見慣れないグリモアを見れば、",
            "魔理沙が何をしてどこから帰ってきたかは明白だった。",
            "おそらく今頃図書館では、喘息持ちの魔女が不機嫌に紅茶でも飲んでいるのだろう。",
            "やがて魔理沙が立ち上がった。湯飲みはすっかり底を見せている。",
            "",
            "魔理沙「邪魔した」",
            "霊夢「そう思ってるならお賽銭でも入れてってよ」",
            "魔理沙「できればツケで頼むぜ」",
            "",
            "使い込まれた箒で魔理沙が空へと浮かぶ。後ろから小言でも言われると思ったのか、",
            "そのまま全速力で魔法の森へと飛んでいってしまった。その姿がかすむ。",
            "霊夢が目をこすっている間に魔理沙は見えなくなっていた。足の速さを少しねたみながら、",
            "湯飲みを片付けようとして。",
            "グリモアがおきっぱなしになっていることに霊夢は気がついた。",
            "",
            "翌日、霊夢は気乗りしないまま紅魔館へと足を運んだ。魔理沙の置き土産を片手に、",
            "大図書館の重い扉を開く。",
            "",
            "パチュリー「あら、珍しいわね。巫女がこんなところになんの――」",
            "霊夢「魔理沙が神社に忘れていったわ。ここの本でしょ？」",
            "",
            "興味もない、という風に霊夢が本を差し出す。パチュリーは訝しげに受け取り、",
            "テーブルの上の本の山へ積んだ。",
            "",
            "パチュリー「……泥棒に対してありがとう、と言う必要はないわよね」",
            "霊夢「ちょっと待ってよ、泥棒？　私が？」",
            "パチュリー「そうでしょう？　確かにこれは図書館の本で、しかもここは貸し出しなんかしてないわ。",
            "にもかかわらず、外のあなたが本を持ってきた。あなたが泥棒じゃなくてなんなのかしら」",
            "霊夢「失礼ね、ただの巫女よ。大体最初に魔理沙が忘れていったって言ったじゃない」",
            "パチュリー「魔理沙？　えーと……ああ、あの黒い魔法使いね。",
            "確かにたまに図書館で見かけるとは思ってたけど……」",
            "",
            "その言い方には違和感があった。なぜこうももやもやとした言い回しをするのか、",
            "そのときの霊夢には分からなかった。",
            "",
            "霊夢「大体、私はグリモアなんか読まないでしょ。盗むなら精々祈祷書ってところね」",
            "パチュリー「……ふう、まあいいわ。ここにこうして戻ってきてるのだから」",
            "霊夢「まったく、そのまま香霖堂にでも売り払っちゃったほうがよかったかしら」",
            "",
            "文句を言いながら、霊夢は図書館を後にした。神社に戻ったら魔理沙が来ているかもしれない。",
            "グリモアを返したと知ったら、どんな反応をするのだろうか。少しだけ興味を引かれる。",
            "",
            "しかし結局、魔理沙はその日姿を見せなかった。次も、その次も見せなかった。",
            "不思議に思い、霊夢が魔法の森に向かうも魔理沙に会うことはできなかった。",
            "それどころか家にたどり着くこともできない。永遠亭の廊下に居るようだ、と霊夢は思った。",
            "しかし歪な感じはまったくない。それが逆に不気味だった。",
            "",
            "不意にパチュリーの反応を思い出す。",
            "あの反応は、魔理沙のことを思い出せなかったのではないだろうか。",
            "まさか、と言い聞かせながら自らも思い出そうとして。",
            "霊夢は自分も魔理沙を忘れかけていることにようやく気がついた。",
            "",
            "",
            "",
            "",
            "霊夢「魔理沙が見つからない……ううん、もう『魔理沙』を認識できているのが少ないんだわ」",
            "",
            "さらに数日をかけ、霊夢は念入りに聞き込みも行った。",
            "そして自分の考えが間違っていないことを確信した。",
            "付き合いが多いはずのパチュリーやアリスですら、ああそんなやつもいたかしらね、",
            "程度にしか思っておらず、誰それ？　と返されることも多々あった。",
            "",
            "霊夢「付き合いが長いからかなあ、私は何とか名前と見た目は思い出せるけど」",
            "",
            "その記憶もどんどんぼやけてきているのが分かる。",
            "もっとも、分かったところでどうすればいいかの検討など全くついていないのだが。",
            "",
            "霊夢「……ま、とりあえず本人を捕まえるところからかしら」",
            "",
            "幻想郷は平和だった。",
            "",
            "大半の妖怪たちにとって、過半の人間たちにとって平和だった。",
            "だが霊夢の幻想郷は違う。彼女の幻想郷から、何か大切なものが欠けようとしている。",
            "退屈な時間は終わりを告げ、今巫女は何度目かの空へ飛び出した。"
    };

    private static final int FLAKE_COUNT = 18;

    private final int[] flakeXfp = new int[FLAKE_COUNT];
    private final int[] flakeYfp = new int[FLAKE_COUNT];
    private final int[] flakeVYfp = new int[FLAKE_COUNT];
    private final int[] flakeVXfp = new int[FLAKE_COUNT];
    private final byte[] flakeSize = new byte[FLAKE_COUNT];
    private final byte[] flakeAlphaLevel = new byte[FLAKE_COUNT];
    private final int[] flakeSeed = new int[FLAKE_COUNT];
    private int flakeTick;

    private Font font;
    private int lineH;

    private Vector wrapped;
    private int maxTopLine;

    // Smooth scroll state (line fixed-point, 8 bits fraction).
    private int scrollPosFp;
    private int scrollVelFp;
    private int scrollTargetFp;
    private boolean scrollTargetActive;

    // Hold-to-scroll (UP/DOWN) state.
    private int holdUpTicks;
    private int holdDownTicks;

    // Edge detection for non-repeat actions.
    private int prevPressed;

    public void enter() {
        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        lineH = UiDraw.fontHeight(font) + 1;

        wrapped = new Vector();
        String[] lines = loadStoryLines();
        for (int i = 0; i < lines.length; i++) {
            wrapLine(wrapped, font, lines[i], TEXT_W);
        }

        maxTopLine = 0;
        int visible = TEXT_H / lineH;
        if (visible < 1) {
            visible = 1;
        }
        int total = wrapped.size();
        int max = total - visible;
        if (max > 0) {
            maxTopLine = max;
        }

        scrollPosFp = 0;
        scrollVelFp = 0;
        scrollTargetFp = 0;
        scrollTargetActive = false;
        holdUpTicks = 0;
        holdDownTicks = 0;
        prevPressed = 0;

        flakeTick = 0;
        for (int i = 0; i < FLAKE_COUNT; i++) {
            flakeSeed[i] = 0x1F123BB5 ^ (i * 0x9E3779B9);
            initFlake(i, false);
        }
    }

    public Result update(int pressed) {
        flakeTick++;

        if ((pressed & (GameCanvas.GAME_A_PRESSED | GameCanvas.GAME_B_PRESSED)) != 0) {
            return new Result(Result.KIND_BACK_TO_TITLE);
        }

        boolean up = (pressed & GameCanvas.UP_PRESSED) != 0;
        boolean down = (pressed & GameCanvas.DOWN_PRESSED) != 0;
        boolean left = (pressed & GameCanvas.LEFT_PRESSED) != 0;
        boolean right = (pressed & GameCanvas.RIGHT_PRESSED) != 0;

        boolean edgeUp = up && ((prevPressed & GameCanvas.UP_PRESSED) == 0);
        boolean edgeDown = down && ((prevPressed & GameCanvas.DOWN_PRESSED) == 0);
        boolean edgeLeft = left && ((prevPressed & GameCanvas.LEFT_PRESSED) == 0);
        boolean edgeRight = right && ((prevPressed & GameCanvas.RIGHT_PRESSED) == 0);

        if (edgeUp) {
            requestScrollByLines(-1);
        } else if (edgeDown) {
            requestScrollByLines(1);
        }
        if (edgeLeft) {
            requestScrollByLines(-6);
        } else if (edgeRight) {
            requestScrollByLines(6);
        }

        int targetVel = 0;
        boolean holdActive = false;
        if (up && !down) {
            holdUpTicks++;
            holdDownTicks = 0;
            targetVel = -calcHoldScrollVelFp(holdUpTicks);
            holdActive = true;
        } else if (down && !up) {
            holdDownTicks++;
            holdUpTicks = 0;
            targetVel = calcHoldScrollVelFp(holdDownTicks);
            holdActive = true;
        } else {
            holdUpTicks = 0;
            holdDownTicks = 0;
        }

        if (holdActive) {
            scrollTargetActive = false;
            scrollVelFp = targetVel;
        } else if (scrollTargetActive) {
            int err = scrollTargetFp - scrollPosFp;
            scrollVelFp += err >> 3;
            scrollVelFp -= scrollVelFp >> 1;

            if (err < 32 && err > -32 && scrollVelFp < 16 && scrollVelFp > -16) {
                scrollPosFp = scrollTargetFp;
                scrollVelFp = 0;
                scrollTargetActive = false;
            }
        } else {
            scrollVelFp -= scrollVelFp >> 1;
        }

        scrollPosFp += scrollVelFp;
        int minFp = 0;
        int maxFp = maxTopLine << 8;
        if (scrollPosFp < minFp) {
            scrollPosFp = minFp;
            scrollVelFp = 0;
            scrollTargetActive = false;
        } else if (scrollPosFp > maxFp) {
            scrollPosFp = maxFp;
            scrollVelFp = 0;
            scrollTargetActive = false;
        }

        prevPressed = pressed;

        return null;
    }

    private void requestScrollByLines(int deltaLines) {
        if (maxTopLine <= 0) {
            return;
        }

        int maxFp = maxTopLine << 8;
        int start = scrollTargetActive ? scrollTargetFp : scrollPosFp;
        int dst = start + (deltaLines << 8);
        if (dst < 0) {
            dst = 0;
        } else if (dst > maxFp) {
            dst = maxFp;
        }

        scrollTargetFp = dst;
        scrollTargetActive = true;
    }

    private static int calcHoldScrollVelFp(int holdTicks) {
        if (holdTicks <= 0) {
            return 0;
        }
        if (holdTicks < 4) {
            return 256;
        }
        if (holdTicks < 10) {
            return 384;
        }
        if (holdTicks < 20) {
            return 640;
        }
        if (holdTicks < 35) {
            return 1024;
        }
        if (holdTicks < 55) {
            return 1536;
        }
        return 2048;
    }

    public void render(Graphics g, ImageBank imgs) {
        if (g == null) {
            return;
        }

        Image bg = (imgs != null) ? imgs.get(BG_IMG_INDEX) : null;
        if (bg != null) {
            UiDraw.drawRegion(g, bg, 0, 0, 0, 0, W, H);
        } else {
            g.setColor(0x000000);
            g.fillRect(0, 0, W, H);
        }

        UiDraw.drawGradationAlpha(g, 0, 0, W, H, 0x203060, 0x000000, 32, 120);

        UiDraw.fillRectAlpha(g, 8, 22, 224, 210, 0x000000, 120);
        g.setColor(0xAFCFFF);
        g.drawRect(8, 22, 224, 210);

        UiDraw.drawString2(g, font, UiText.get(TextId.STORY_TITLE), 120, 18, 1, 0xFFFFFF, 0x334455);

        drawSnow(g);
        drawText(g);
        drawScrollBar(g);
    }

    private void drawText(Graphics g) {
        if (wrapped == null || font == null) {
            return;
        }

        int oldClipX = g.getClipX();
        int oldClipY = g.getClipY();
        int oldClipW = g.getClipWidth();
        int oldClipH = g.getClipHeight();

        try {
            g.setClip(TEXT_X, TEXT_Y, TEXT_W, TEXT_H);
            g.setFont(font);

            int idx = scrollPosFp >> 8;
            int sub = scrollPosFp & 0xFF;
            int y = (TEXT_Y + lineH) - ((sub * lineH) >> 8);
            int maxLines = (TEXT_H / lineH) + 3;

            for (int i = 0; i < maxLines; i++) {
                if (idx >= wrapped.size()) {
                    break;
                }
                String s = (String) wrapped.elementAt(idx);
                if (s == null) {
                    s = "";
                }
                UiDraw.drawStringPlain(g, font, s, TEXT_X + 1, y + 1, 0, 0x001820);
                UiDraw.drawStringPlain(g, font, s, TEXT_X, y, 0, 0xFFFFFF);

                y += lineH;
                idx++;
            }
        } catch (Throwable t) {
        } finally {
            try {
                g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
            } catch (Throwable ignore) {
            }
        }
    }

    private void drawScrollBar(Graphics g) {
        if (wrapped == null || font == null) {
            return;
        }

        int total = wrapped.size();
        if (total <= 0) {
            return;
        }

        int visible = TEXT_H / lineH;
        if (visible < 1) {
            visible = 1;
        }
        if (total <= visible) {
            return;
        }

        int barX = TEXT_X + TEXT_W + 2;
        int barY = TEXT_Y;
        int barW = 4;
        int barH = TEXT_H;

        UiDraw.fillRectAlpha(g, barX, barY, barW, barH, 0x000000, 100);

        int knobH = (barH * visible) / total;
        if (knobH < 8) {
            knobH = 8;
        }
        int denom = maxTopLine << 8;
        int knobY;
        if (denom <= 0) {
            knobY = barY;
        } else {
            long num = (long) (barH - knobH) * (long) scrollPosFp;
            knobY = barY + (int) (num / denom);
        }
        UiDraw.fillRectAlpha(g, barX, knobY, barW, knobH, 0xAFCFFF, 180);
    }

    private void drawSnow(Graphics g) {
        for (int i = 0; i < FLAKE_COUNT; i++) {
            int seed = flakeSeed[i];

            if ((flakeTick & 15) == (i & 15)) {
                seed = nextRand(seed);
                int n = (seed >> 28) & 7;
                int dvx = (n - 3) << 3;

                int vx = flakeVXfp[i];
                vx += dvx;
                vx -= (vx >> 5);
                if (vx < -128) {
                    vx = -128;
                } else if (vx > 128) {
                    vx = 128;
                }
                flakeVXfp[i] = vx;
            }

            int xfp = flakeXfp[i] + flakeVXfp[i];
            int yfp = flakeYfp[i] + flakeVYfp[i];

            if (xfp < (-4 << 8)) {
                xfp = (W + 3) << 8;
            } else if (xfp > ((W + 3) << 8)) {
                xfp = (-3) << 8;
            }

            if (yfp > ((H + 6) << 8)) {
                flakeSeed[i] = nextRand(seed);
                initFlake(i, true);
                continue;
            }

            flakeSeed[i] = seed;
            flakeXfp[i] = xfp;
            flakeYfp[i] = yfp;

            int x = xfp >> 8;
            int y = yfp >> 8;

            int size = flakeSize[i] & 0xFF;
            int alpha = 96 + (flakeAlphaLevel[i] & 3) * 32;
            if (size <= 1) {
                UiDraw.fillRectAlpha(g, x, y, 1, 1, 0xFFFFFF, alpha);
            } else if (size == 2) {
                UiDraw.fillRectAlpha(g, x, y, 2, 2, 0xFFFFFF, alpha);
            } else {
                UiDraw.fillRectAlpha(g, x, y, 3, 1, 0xFFFFFF, alpha);
                UiDraw.fillRectAlpha(g, x + 1, y - 1, 1, 3, 0xFFFFFF, alpha);
            }
        }
    }

    private void initFlake(int i, boolean respawn) {
        int seed = flakeSeed[i];
        seed = nextRand(seed);
        int x = (seed >>> 16) % W;

        seed = nextRand(seed);
        int y;
        if (respawn) {
            y = -((seed >>> 16) % 32) - 2;
        } else {
            y = (seed >>> 16) % H;
        }

        seed = nextRand(seed);
        int vy = 40 + ((seed >>> 16) % 96);

        seed = nextRand(seed);
        int vx = ((seed >>> 16) % 81) - 40;

        seed = nextRand(seed);
        int size = 1 + ((seed >>> 16) % 3);

        seed = nextRand(seed);
        int a = (seed >>> 16) & 3;

        flakeXfp[i] = x << 8;
        flakeYfp[i] = y << 8;
        flakeVYfp[i] = vy;
        flakeVXfp[i] = vx;
        flakeSize[i] = (byte) size;
        flakeAlphaLevel[i] = (byte) a;
        flakeSeed[i] = seed;
    }

    private static int nextRand(int s) {
        return s * 1103515245 + 12345;
    }

    private static void wrapLine(Vector out, Font font, String s, int maxWidth) {
        if (out == null || font == null) {
            return;
        }
        if (s == null) {
            out.addElement("");
            return;
        }
        if (s.length() == 0) {
            out.addElement("");
            return;
        }

        int lineStart = 0;
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int cw = UiDraw.charWidth(font, c);
            if (w + cw > maxWidth && i > lineStart) {
                out.addElement(s.substring(lineStart, i));
                lineStart = i;
                w = 0;
            }
            w += cw;
        }
        if (lineStart < s.length()) {
            out.addElement(s.substring(lineStart));
        }
    }
}
