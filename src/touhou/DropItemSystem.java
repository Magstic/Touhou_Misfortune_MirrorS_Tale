package touhou;

import javax.microedition.lcdui.Graphics;

import touhou.replay.ReplaySnapshotReader;
import touhou.replay.ReplaySnapshotWriter;

public final class DropItemSystem {
    public static final int MAX = 64;

    public static final int TYPE_NONE = 0;
    public static final int TYPE_SCORE = 1;
    public static final int TYPE_P = 2;
    public static final int TYPE_BIG_P = 3;
    public static final int TYPE_FULL_POWER = 4;
    public static final int TYPE_BOMB = 5;
    public static final int TYPE_1UP = 6;
    public static final int TYPE_CRISIS = 7;

    public interface Listener {
        void addPower(int amount);

        void addBomb(int amount);

        void addPlayer(int amount);

        void addScore(int amount);

        void onScoreItemCollected();
    }

    public interface IconDrawer {
        void draw(Graphics g, int ccIndex, int x, int y);
    }

    private final boolean[] active = new boolean[MAX];
    private final int[] timer = new int[MAX];
    private final int[] spriteBase = new int[MAX];
    private final int[] type = new int[MAX];
    private final int[] state = new int[MAX];
    private final int[] x = new int[MAX];
    private final int[] y = new int[MAX];
    private final int[] r = new int[MAX];
    private final int[] vel = new int[MAX];
    private final int[] dir = new int[MAX];
    private final int[] x0 = new int[MAX];
    private final int[] y0 = new int[MAX];

    private Listener listener;

    public DropItemSystem() {
        clear();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void clear() {
        for (int i = 0; i < MAX; i++) {
            active[i] = false;
            timer[i] = 0;
            spriteBase[i] = 0;
            type[i] = 0;
            state[i] = 0;
            x[i] = 0;
            y[i] = 0;
            r[i] = 0;
            vel[i] = 0;
            dir[i] = 0;
            x0[i] = 0;
            y0[i] = 0;
        }
    }

    // Replay snapshot I/O.
    public void writeSnapshot(ReplaySnapshotWriter w) {
        if (w == null) {
            return;
        }

        w.writeI32LE(MAX);
        for (int i = 0; i < MAX; i++) {
            w.writeBool(active[i]);
            w.writeI32LE(timer[i]);
            w.writeI32LE(spriteBase[i]);
            w.writeI32LE(type[i]);
            w.writeI32LE(state[i]);
            w.writeI32LE(x[i]);
            w.writeI32LE(y[i]);
            w.writeI32LE(r[i]);
            w.writeI32LE(vel[i]);
            w.writeI32LE(dir[i]);
            w.writeI32LE(x0[i]);
            w.writeI32LE(y0[i]);
        }
    }

    public void readSnapshot(ReplaySnapshotReader reader) {
        if (reader == null) {
            return;
        }

        int storedMax = reader.readI32LE();
        int readCount = storedMax;
        if (readCount < 0) {
            readCount = 0;
        }

        for (int i = 0; i < readCount; i++) {
            boolean a = reader.readBool();
            int ti = reader.readI32LE();
            int sb = reader.readI32LE();
            int ty = reader.readI32LE();
            int st = reader.readI32LE();
            int xx = reader.readI32LE();
            int yy = reader.readI32LE();
            int rr = reader.readI32LE();
            int vv = reader.readI32LE();
            int dd = reader.readI32LE();
            int xx0 = reader.readI32LE();
            int yy0 = reader.readI32LE();

            if (i >= MAX) {
                continue;
            }
            active[i] = a;
            timer[i] = ti;
            spriteBase[i] = sb;
            type[i] = ty;
            state[i] = st;
            x[i] = xx;
            y[i] = yy;
            r[i] = rr;
            vel[i] = vv;
            dir[i] = dd;
            x0[i] = xx0;
            y0[i] = yy0;
        }

        for (int i = readCount; i < MAX; i++) {
            active[i] = false;
            timer[i] = 0;
            spriteBase[i] = 0;
            type[i] = 0;
            state[i] = 0;
            x[i] = 0;
            y[i] = 0;
            r[i] = 0;
            vel[i] = 0;
            dir[i] = 0;
            x0[i] = 0;
            y0[i] = 0;
        }
    }

    public void spawn(int itemType, int spawnState, int angleOffsetDeg, int xFixed, int yFixed, int stageIndex, int power, boolean bossActive) {
        if (itemType == TYPE_NONE) {
            return;
        }

        int n = itemType;
        if (stageIndex == 0 && power > 64 && !bossActive) {
            if (n == TYPE_P) {
                n = TYPE_SCORE;
            }
            if (n == TYPE_BIG_P) {
                n = TYPE_P;
            }
        }
        if (power == 128 && (n == TYPE_P || n == TYPE_BIG_P)) {
            n = TYPE_SCORE;
        }

        int base;
        switch (n) {
            case TYPE_SCORE:
                base = 111;
                break;
            case TYPE_P:
                base = 120;
                break;
            case TYPE_BIG_P:
                base = 129;
                break;
            case TYPE_FULL_POWER:
                base = 138;
                break;
            case TYPE_BOMB:
                base = 147;
                break;
            case TYPE_1UP:
                base = 156;
                break;
            case TYPE_CRISIS:
                base = 165;
                break;
            default:
                return;
        }

        for (int i = 0; i < MAX; i++) {
            if (active[i]) {
                continue;
            }
            active[i] = true;
            timer[i] = 0;
            type[i] = n;
            spriteBase[i] = base;
            dir[i] = angleOffsetDeg + 270;
            r[i] = 655360;
            x[i] = xFixed;
            y[i] = yFixed;
            x0[i] = xFixed;
            y0[i] = yFixed;
            vel[i] = 0;
            state[i] = spawnState;
            return;
        }
    }

    public void update(int playerXFixed, int playerYFixed, boolean playerSlow, int stageIndex, int power, boolean playerDead,
            boolean disableItemCollectLimitCheat, int chara, int crisis) {
        int figcs = Fixed.fromInt(1);
        int figisqrt = (chara == 1) ? Fixed.fromInt(40) : Fixed.fromInt(30);
        int collectRange = figisqrt;
        if (!playerSlow) {
            collectRange >>= 2;
        }

        if (playerYFixed < 4849664 && (disableItemCollectLimitCheat || power == 128 || stageIndex == 6)) {
            forceCollect(0);
        }

        for (int i = 0; i < MAX; i++) {
            if (!active[i]) {
                continue;
            }

            if (!playerDead && (state[i] == 2 || timer[i] > 3)) {
                int d0 = collectRange;
                if (state[i] == 0 && rectOverlapFixed(x[i] - r[i], y[i] - r[i], r[i] << 1, r[i] << 1, playerXFixed - (figcs + d0), playerYFixed - (figcs + d0), (figcs + d0) << 1, (figcs + d0) << 1)) {
                    timer[i] = 0;
                    state[i] = 2;
                }

                if (rectOverlapFixed(x[i] - r[i], y[i] - r[i], r[i] << 1, r[i] << 1, playerXFixed - (figcs + 131072), playerYFixed - (figcs + 131072), (figcs + 131072) << 1, (figcs + 131072) << 1)) {
                    applyPickup(type[i], playerYFixed, crisis);
                    active[i] = false;
                    continue;
                }
            }

            switch (state[i]) {
                case 0:
                    y[i] += vel[i];
                    if (timer[i] < 8) {
                        if (vel[i] > -327680) {
                            vel[i] -= 65536;
                        }
                    } else {
                        if (vel[i] < 262144) {
                            vel[i] += 65536;
                        }
                    }
                    break;
                case 1:
                    int ang = dir[i];
                    x[i] += speedCos(80, ang);
                    y[i] += speedSin(80, ang);
                    if (y[i] < 3145728 || x[i] < 1179648 || x[i] > 12582912) {
                        state[i] = 0;
                    }
                    break;
                case 2:
                    int arc = arcTan2Deg(playerYFixed - y[i], playerXFixed - x[i]);
                    x0[i] = x[i];
                    y0[i] = y[i];
                    int dist = timer[i] * 3;
                    x[i] = x0[i] + (Trig.cos(arc) * dist);
                    y[i] = y0[i] + (Trig.sin(arc) * dist);
                    if (timer[i] > 3) {
                        timer[i] = 3;
                    }
                    break;
                default:
                    break;
            }

            timer[i]++;
            if (y[i] > 15990784) {
                active[i] = false;
            }
        }
    }

    public void render(Graphics g, IconDrawer drawer, int originX, int originY, int playY) {
        if (g == null || drawer == null) {
            return;
        }
        for (int i = 0; i < MAX; i++) {
            if (!active[i]) {
                continue;
            }

            int frame = 0;
            if (state[i] == 0 && timer[i] < 8) {
                frame = timer[i];
            }

            int drawX = originX + (x[i] >> 16);
            if (type[i] == TYPE_CRISIS) {
                int drawY = originY + (y[i] >> 16);
                drawer.draw(g, spriteBase[i], drawX, drawY);
                continue;
            }

            if (y[i] < 0) {
                drawer.draw(g, spriteBase[i] + 8, drawX, originY + playY + 2);
                continue;
            }

            int drawY = originY + (y[i] >> 16);
            drawer.draw(g, spriteBase[i] + frame, drawX, drawY);
        }
    }

    public void forceCollect(int mode) {
        for (int i = 0; i < MAX; i++) {
            if (!active[i] || state[i] == 2) {
                continue;
            }
            if (mode == 0) {
                timer[i] = 0;
                state[i] = 2;
            } else if (mode == 1) {
                if (type[i] == TYPE_CRISIS) {
                    timer[i] = 0;
                    state[i] = 2;
                }
            } else if (mode == 2) {
                if (type[i] != TYPE_FULL_POWER) {
                    timer[i] = 0;
                    state[i] = 2;
                }
            }
        }
    }

    // Mirrors DoJa : yc() cancels collecting items and gives them a small initial speed.
    public void cancelCollectingAndScatter() {
        for (int i = 0; i < MAX; i++) {
            if (!active[i] || state[i] != 2) {
                continue;
            }
            vel[i] = 196608;
            state[i] = 0;
        }
    }

    private void applyPickup(int itemType, int playerYFixed, int crisis) {
        if (listener == null) {
            return;
        }
        switch (itemType) {
            case TYPE_FULL_POWER:
                listener.addPower(128);
                return;
            case TYPE_BIG_P:
                listener.addPower(8);
                return;
            case TYPE_P:
                listener.addPower(1);
                return;
            case TYPE_SCORE:
                listener.addScore(ScoreSystem.calcScoreItemAdd(playerYFixed));
                listener.onScoreItemCollected();
                return;
            case TYPE_BOMB:
                listener.addBomb(1);
                return;
            case TYPE_1UP:
                listener.addPlayer(1);
                return;
            case TYPE_CRISIS:
                listener.addScore(ScoreSystem.calcCrisisItemAdd(crisis));
                return;
            default:
                return;
        }
    }

    private static int speedCos(int speed, int deg) {
        return (int) ((((long) Trig.cos(deg)) * (long) speed) >> 3);
    }

    private static int speedSin(int speed, int deg) {
        return (int) ((((long) Trig.sin(deg)) * (long) speed) >> 3);
    }

    private static boolean rectOverlapFixed(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        if (aw <= 0 || ah <= 0 || bw <= 0 || bh <= 0) {
            return false;
        }
        if (ax >= bx + bw) {
            return false;
        }
        if (bx >= ax + aw) {
            return false;
        }
        if (ay >= by + bh) {
            return false;
        }
        if (by >= ay + ah) {
            return false;
        }
        return true;
    }

    private static int arcTan2Deg(int yFixed, int xFixed) {
        int x = xFixed >> 16;
        int y = yFixed >> 16;

        final int div = div16((y << 16), (x << 16));
        int arcTan;

        if (x == 0 && y == 0) {
            arcTan = 0;
        } else if (x == 0 && y > 0) {
            arcTan = 102943;
        } else if (x < 0 && y == 0) {
            arcTan = 205887;
        } else if (x == 0 && y < 0) {
            arcTan = 308830;
        } else if (x > 0 && y == 0) {
            arcTan = 0;
        } else {
            if (div > 65536) {
                arcTan = 102943 - arcTan16(div16(65536, div));
            } else if (div < -65536) {
                arcTan = -102943 - arcTan16(div16(65536, div));
            } else {
                arcTan = arcTan16(div);
            }

            if (x < 0 && y > 0) {
                arcTan += 205887;
            } else if (x < 0 && y < 0) {
                arcTan += 205887;
            } else if (x > 0 && y < 0) {
                arcTan += 411774;
            }
        }

        int i = mul16(arcTan, div16(11796480, 205887)) >> 16;
        for (; i < 0; i = (i + 360) % 360) {
        }
        return i;
    }

    private static int mul16(final int a, final int b) {
        return (int) ((a * (long) b) >> 16);
    }

    private static int div16(final int a, final int b) {
        if (b == 0) {
            return 0;
        }
        return (int) (((long) a << 16) / b);
    }

    private static int arcTan16(final int x) {
        final int xx = mul16(x, x);
        int t = mul16(1365, xx);
        t = mul16(t - 5579, xx);
        t = mul16(t + 11805, xx);
        t = mul16(t - 21646, xx);
        t = mul16(t + 65527, x);
        return t;
    }
}
