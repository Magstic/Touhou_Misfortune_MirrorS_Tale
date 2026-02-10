package touhou.battle;

import touhou.Trig;

public final class BattleMath {
    private BattleMath() {
    }

    // Fixed-point speed X component.
    public static int speedCos(int n, int dirDeg) {
        return (int) ((((long) Trig.cos(dirDeg)) * (long) n) >> 3);
    }

    // Fixed-point speed Y component.
    public static int speedSin(int n, int dirDeg) {
        return (int) ((((long) Trig.sin(dirDeg)) * (long) n) >> 3);
    }

    // Normalize degrees to [0, 360).
    public static int normalizeDeg(int deg) {
        deg %= 360;
        if (deg < 0) {
            deg += 360;
        }
        return deg;
    }

    // Fixed-point multiply helper.
    private static int mul16(final int a, final int b) {
        return (int) ((a * (long) b) >> 16);
    }

    // Fixed-point divide helper.
    private static int div16(final int a, final int b) {
        if (b == 0) {
            return 0;
        }
        return (int) (((long) a << 16) / b);
    }

    // Fixed-point arctan approximation.
    private static int arcTan16(final int x) {
        final int xx = mul16(x, x);
        int t = mul16(1365, xx);
        t = mul16(t - 5579, xx);
        t = mul16(t + 11805, xx);
        t = mul16(t - 21646, xx);
        t = mul16(t + 65527, x);
        return t;
    }

    // Fixed-point atan2 to degrees.
    public static int arcTan2Deg(int yFixed, int xFixed) {
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
}
