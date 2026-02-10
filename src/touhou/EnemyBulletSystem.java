package touhou;

import touhou.replay.ReplaySnapshotReader;
import touhou.replay.ReplaySnapshotWriter;

public final class EnemyBulletSystem {
    private static final int GRAZE_MARGIN_FIXED = Fixed.fromInt(8);

    private final BulletSystem bullets;
    private final int max;

    private final boolean[] wasActive;
    private final boolean[] canGraze;

    private int grazeCount;
    private boolean playerHit;

    public EnemyBulletSystem(BulletSystem bullets) {
        this.bullets = bullets;
        this.max = (bullets != null) ? bullets.getMax() : 0;
        wasActive = new boolean[max];
        canGraze = new boolean[max];
    }

    public void clear() {
        for (int i = 0; i < max; i++) {
            wasActive[i] = false;
            canGraze[i] = false;
        }
        grazeCount = 0;
        playerHit = false;
    }

    // Replay snapshot I/O.
    public void writeSnapshot(ReplaySnapshotWriter w) {
        if (w == null) {
            return;
        }

        w.writeI32LE(max);
        for (int i = 0; i < max; i++) {
            w.writeBool(wasActive[i]);
            w.writeBool(canGraze[i]);
        }
        w.writeI32LE(grazeCount);
        w.writeBool(playerHit);
    }

    public void readSnapshot(ReplaySnapshotReader r) {
        if (r == null) {
            return;
        }

        int storedMax = r.readI32LE();
        int readCount = storedMax;
        if (readCount < 0) {
            readCount = 0;
        }

        for (int i = 0; i < readCount; i++) {
            boolean wa = r.readBool();
            boolean cg = r.readBool();
            if (i < max) {
                wasActive[i] = wa;
                canGraze[i] = cg;
            }
        }

        for (int i = readCount; i < max; i++) {
            wasActive[i] = false;
            canGraze[i] = false;
        }

        grazeCount = r.readI32LE();
        playerHit = r.readBool();
    }

    public void cc(int bulletId, int moveType, int angleDeg, int speedParam, int spawnXFixed, int spawnYFixed, int playerXFixed, int playerYFixed) {
        if (bullets == null) {
            return;
        }

        if (moveType == 1 || moveType == 28) {
            bullets.spawnEnemyAccel(bulletId, moveType, angleDeg, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        if (moveType == 6 || moveType == 7 || moveType == 8 || moveType == 9) {
            bullets.spawnEnemyStopTurnAccel(bulletId, moveType, angleDeg, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        if (moveType == 26) {
            bullets.spawn(bulletId, moveType, angleDeg, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        if (moveType == 3) {
            int ang = arcTan2Deg(playerYFixed - spawnYFixed, playerXFixed - spawnXFixed) % 360;
            bullets.spawn(bulletId, 0, ang, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        if (moveType == 2) {
            bullets.spawnEnemyDecel(bulletId, angleDeg, speedParam >> 3, 1, 4, spawnXFixed, spawnYFixed);
            return;
        }
        // Stage EXTRA fodder: spiral curve bullets that transition to straight flight.
        if (moveType == 21 || moveType == 22) {
            bullets.spawnEnemyCurve(bulletId, moveType, angleDeg, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        // Stage EXTRA fodder: accelerating burst and spiral split bullets.
        if (moveType == 40) {
            bullets.spawnEnemyAccel40(bulletId, angleDeg, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        if (moveType == 43 || moveType == 44) {
            bullets.spawnEnemySpiral43_44(bulletId, moveType, angleDeg, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        if (moveType == 41) {
            bullets.spawnEnemyRamp41(bulletId, angleDeg, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        if (moveType == 0) {
            bullets.spawn(bulletId, 0, angleDeg, speedParam, spawnXFixed, spawnYFixed);
            return;
        }
        bullets.spawn(bulletId, moveType, angleDeg, speedParam, spawnXFixed, spawnYFixed);
        return;
    }

    public int getGrazeCount() {
        return grazeCount;
    }

    public void clearGrazeCount() {
        grazeCount = 0;
    }

    public boolean isPlayerHit() {
        return playerHit;
    }

    public void clearPlayerHit() {
        playerHit = false;
    }

    public void applyPlayerCollision(int playerXFixed, int playerYFixed, int playerRadiusFixed, boolean allowGraze, boolean allowHit) {
        if (bullets == null) {
            return;
        }

        grazeCount = 0;
        playerHit = false;

        int playerSize = (playerRadiusFixed << 1) + Fixed.fromInt(1);
        int playerLeft = playerXFixed - playerRadiusFixed;
        int playerTop = playerYFixed - playerRadiusFixed;

        // Direct array access avoids per-slot getter + bounds-check overhead.
        final boolean[] bActive = bullets.activeArray();
        final boolean[] bFromPlayer = bullets.fromPlayerArray();
        final int[] bx = bullets.xArray();
        final int[] by = bullets.yArray();
        final int[] bBulletId = bullets.bulletIdArray();

        for (int i = 0; i < max; i++) {
            if (!bActive[i] || bFromPlayer[i]) {
                wasActive[i] = false;
                canGraze[i] = false;
                continue;
            }

            if (!wasActive[i]) {
                wasActive[i] = true;
                canGraze[i] = true;
            }

            int ex = bx[i];
            int ey = by[i];

            int bId = bBulletId[i];
            if (bId < 0) {
                continue;
            }

            int br = resolveEnemyBulletRadiusFixed(bId);
            int bl = ex - br;
            int bt = ey - br;
            int bw = (br << 1);
            int bh = (br << 1);

            if (allowGraze && canGraze[i]) {
                if (rectOverlapFixed(bl - GRAZE_MARGIN_FIXED, bt - GRAZE_MARGIN_FIXED, bw + (GRAZE_MARGIN_FIXED << 1), bh + (GRAZE_MARGIN_FIXED << 1),
                        playerLeft, playerTop, playerSize, playerSize)) {
                    canGraze[i] = false;
                    grazeCount++;
                }
            }

            if (allowHit) {
                if (rectOverlapFixed(bl, bt, bw, bh, playerLeft, playerTop, playerSize, playerSize)) {
                    playerHit = true;
                    bullets.deactivate(i);
                }
            }
        }
    }

    private static int resolveEnemyBulletRadiusFixed(int bulletId) {
        if (bulletId >= 87 && bulletId <= 92) {
            return 393216;
        }
        if (bulletId >= 93 && bulletId <= 96) {
            return 655360;
        }
        return 131072;
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
