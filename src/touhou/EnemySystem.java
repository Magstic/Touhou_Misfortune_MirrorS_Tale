package touhou;

import touhou.replay.ReplaySnapshotReader;
import touhou.replay.ReplaySnapshotWriter;

public final class EnemySystem {
    public static final int MAX = 32;

    // Target scan bounds in fixed-point (playfield 0..194, 8..234).
    private static final int TARGET_SCAN_X_MIN = 0;
    private static final int TARGET_SCAN_X_MAX = 12713984;
    private static final int TARGET_SCAN_Y_MIN = 524288;
    private static final int TARGET_SCAN_Y_MAX = 15335424;

    private final boolean[] active = new boolean[MAX];
    private final boolean[] targetable = new boolean[MAX];

    private final int[] x = new int[MAX];
    private final int[] y = new int[MAX];

    private final int[] r = new int[MAX];

    private final int[] aabbL = new int[MAX];
    private final int[] aabbR = new int[MAX];
    private final int[] aabbT = new int[MAX];
    private final int[] aabbB = new int[MAX];

    public EnemySystem() {
        clear();
    }

    public void clear() {
        for (int i = 0; i < MAX; i++) {
            active[i] = false;
            targetable[i] = false;
            x[i] = 0;
            y[i] = 0;
            r[i] = 0;

            aabbL[i] = 0;
            aabbR[i] = 0;
            aabbT[i] = 0;
            aabbB[i] = 0;
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
            w.writeBool(targetable[i]);
            w.writeI32LE(x[i]);
            w.writeI32LE(y[i]);
            w.writeI32LE(r[i]);
            w.writeI32LE(aabbL[i]);
            w.writeI32LE(aabbR[i]);
            w.writeI32LE(aabbT[i]);
            w.writeI32LE(aabbB[i]);
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
            boolean t = reader.readBool();
            int xx = reader.readI32LE();
            int yy = reader.readI32LE();
            int rr = reader.readI32LE();
            int l = reader.readI32LE();
            int rr2 = reader.readI32LE();
            int tt = reader.readI32LE();
            int bb = reader.readI32LE();

            if (i >= MAX) {
                continue;
            }
            active[i] = a;
            targetable[i] = t;
            x[i] = xx;
            y[i] = yy;
            r[i] = rr;
            aabbL[i] = l;
            aabbR[i] = rr2;
            aabbT[i] = tt;
            aabbB[i] = bb;
        }

        for (int i = readCount; i < MAX; i++) {
            active[i] = false;
            targetable[i] = false;
            x[i] = 0;
            y[i] = 0;
            r[i] = 0;
            aabbL[i] = 0;
            aabbR[i] = 0;
            aabbT[i] = 0;
            aabbB[i] = 0;
        }
    }

    public void update() {
    }

    public int alloc() {
        for (int i = 0; i < MAX; i++) {
            if (!active[i]) {
                active[i] = true;
                return i;
            }
        }
        return -1;
    }

    public void free(int idx) {
        if (idx < 0 || idx >= MAX) {
            return;
        }
        active[idx] = false;
        targetable[idx] = false;
        x[idx] = 0;
        y[idx] = 0;
        r[idx] = 0;

        aabbL[idx] = 0;
        aabbR[idx] = 0;
        aabbT[idx] = 0;
        aabbB[idx] = 0;
    }

    public boolean isActive(int idx) {
        return idx >= 0 && idx < MAX && active[idx];
    }

    public boolean isTargetable(int idx) {
        return idx >= 0 && idx < MAX && active[idx] && targetable[idx];
    }

    public int getXFixed(int idx) {
        if (idx < 0 || idx >= MAX) {
            return 0;
        }
        return x[idx];
    }

    public int getYFixed(int idx) {
        if (idx < 0 || idx >= MAX) {
            return 0;
        }
        return y[idx];
    }

    public int getRadiusFixed(int idx) {
        if (idx < 0 || idx >= MAX) {
            return 0;
        }
        return r[idx];
    }

    public int getAabbLeftFixed(int idx) {
        if (idx < 0 || idx >= MAX) {
            return 0;
        }
        return aabbL[idx];
    }

    public int getAabbRightFixed(int idx) {
        if (idx < 0 || idx >= MAX) {
            return 0;
        }
        return aabbR[idx];
    }

    public int getAabbTopFixed(int idx) {
        if (idx < 0 || idx >= MAX) {
            return 0;
        }
        return aabbT[idx];
    }

    public int getAabbBottomFixed(int idx) {
        if (idx < 0 || idx >= MAX) {
            return 0;
        }
        return aabbB[idx];
    }

    public void set(int idx, int xFixed, int yFixed, int radiusFixed, boolean canTarget) {
        if (idx < 0 || idx >= MAX) {
            return;
        }
        active[idx] = true;
        x[idx] = xFixed;
        y[idx] = yFixed;
        r[idx] = radiusFixed;
        targetable[idx] = canTarget;

        aabbL[idx] = xFixed - radiusFixed;
        aabbR[idx] = xFixed + radiusFixed;
        aabbT[idx] = yFixed - radiusFixed;
        aabbB[idx] = yFixed + radiusFixed;
    }

    public void set(int idx, int xFixed, int yFixed, int radiusFixed, int leftExtentFixed, int rightExtentFixed, int topExtentFixed, int bottomExtentFixed, boolean canTarget) {
        if (idx < 0 || idx >= MAX) {
            return;
        }
        active[idx] = true;
        x[idx] = xFixed;
        y[idx] = yFixed;
        r[idx] = radiusFixed;
        targetable[idx] = canTarget;

        aabbL[idx] = xFixed - leftExtentFixed;
        aabbR[idx] = xFixed + rightExtentFixed;
        aabbT[idx] = yFixed - topExtentFixed;
        aabbB[idx] = yFixed + bottomExtentFixed;
    }

    public void setTargetable(int idx, boolean canTarget) {
        if (idx < 0 || idx >= MAX) {
            return;
        }
        if (!active[idx]) {
            return;
        }
        targetable[idx] = canTarget;
    }

    public int findNearestTargetByX(int xFixed) {
        int best = -1;
        int bestAbsDx = 0;
        for (int i = 0; i < MAX; i++) {
            if (!isTargetable(i)) {
                continue;
            }
            int l = aabbL[i];
            int r = aabbR[i];
            int t = aabbT[i];
            int b = aabbB[i];
            if (!(l < TARGET_SCAN_X_MAX && t < TARGET_SCAN_Y_MAX && r > TARGET_SCAN_X_MIN && b > TARGET_SCAN_Y_MIN)) {
                continue;
            }
            int dx = xFixed - x[i];
            if (dx < 0) {
                dx = -dx;
            }
            if (best == -1 || dx < bestAbsDx) {
                best = i;
                bestAbsDx = dx;
            }
        }
        return best;
    }
}
