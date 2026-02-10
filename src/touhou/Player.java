package touhou;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import touhou.replay.ReplaySnapshotReader;
import touhou.replay.ReplaySnapshotWriter;

public final class Player {
    // Movement constants and hitbox tuning.
    private static final int SPEED_MARISA_BOMB = 131072;
    private static final int CLAMP_MARGIN = 10;

    // Player hitbox radius (fixed-point).
    private static final int PLAYER_HITBOX_DEFAULT = Fixed.fromInt(1);
    private static final int REIMU_PLAYER_HITBOX = (PLAYER_HITBOX_DEFAULT * 3) >> 2;

    // Alice hitbox and scan bounds constants.
    private static final int ALICE_PLAYER_HITBOX = PLAYER_HITBOX_DEFAULT;
    private static final int ALICE_RETURN_BOX_HALF = Fixed.fromInt(5);
    private static final int ALICE_DOLL_BOX_HALF = Fixed.fromInt(3);
    private static final int ALICE_TARGET_BOX_HALF = Fixed.fromInt(2);

    // Alice target scan bounds (fixed-point).
    private static final int ALICE_SCAN_X_MIN = 0;
    private static final int ALICE_SCAN_X_MAX = 12713984;
    private static final int ALICE_SCAN_Y_MIN = 524288;
    private static final int ALICE_SCAN_Y_MAX = 15335424;

    // Character identity and sprite configuration.
    private final int chara;
    private final int type;

    private final int spriteId;
    private final int shotBulletId;
    private final int shotBulletId2;

    // Current position (fixed-point).
    private int x;
    private int y;

    // Player state flags.
    private boolean slow;

    private boolean bombActive;

    // Visible flag for death hide / invincibility blink.
    private boolean visible;

    // Animation state.
    private int animeId;
    private int animeNo;
    private int animeCnt;

    private int animeMove;
    private int tick;

    private int prevMoveDir;

    private int lastMoveDir;

    // Option device layout state.
    private int optLv;
    private int optSpriteLv;
    private int optbdir;
    private int optroll;
    private int optlr;

    // Option positions and directions.
    private final int[] optcx = new int[4];
    private final int[] optcy = new int[4];
    private final int[] optdir = new int[4];
    private final int[] optrad = new int[4];

    // Scratch buffers for option layout.
    private final int[] optTmpWork = new int[4];
    private final int[] optTmpR = new int[4];

    // Alice targeting and doll state.
    private int aliceTarget;

    private final int[] opttgt = new int[4];
    private final int[] optmcx = new int[4];
    private final int[] optmcy = new int[4];
    private final int[] optmode = new int[4];

    // Alice aiming output for option bullets.
    private final int[] optAimArc = new int[4];
    private final int[] optAimPower = new int[4];

    // Initialize player state and option defaults.
    public Player(CcTable cc, int chara, int type, int startX, int startY) {
        this.chara = chara;
        this.type = type;
        this.x = Fixed.fromInt(startX);
        this.y = Fixed.fromInt(startY);

        this.visible = true;

        this.animeId = 0;
        this.animeNo = 0;
        this.animeCnt = 0;
        this.animeMove = 0;
        this.tick = 0;
        this.prevMoveDir = 0;
        this.lastMoveDir = 0;

        for (int i = 0; i < 4; i++) {
            optcx[i] = this.x;
            optcy[i] = this.y;
            optdir[i] = (chara == 2) ? 180 : 0;
            optrad[i] = 0;

            opttgt[i] = -1;
            optmcx[i] = this.x;
            optmcy[i] = this.y;
            optmode[i] = 0;

            optAimArc[i] = 90;
            optAimPower[i] = 4;
        }
        if (chara != 2) {
            optdir[0] = 90;
            optdir[1] = 270;
        }
        optLv = 0;
        optSpriteLv = 0;
        optbdir = 0;
        optroll = 0;
        optlr = 0;

        aliceTarget = -1;

        int imgIndex = imgIndexForChara(chara);
        this.spriteId = findSpriteId(cc, imgIndex, 12, 40, 0, 64);
        this.shotBulletId = findSpriteId(cc, imgIndex, 3, 10, 96, 10000);
        this.shotBulletId2 = findSpriteId(cc, imgIndex, 8, 24, 96, 10000);
    }

    // Slow mode toggles.
    public void setSlow(boolean slow) {
        this.slow = slow;
    }

    public boolean isSlow() {
        return slow;
    }

    public void toggleSlow() {
        slow = !slow;
    }

    // Bomb state and Alice targeting reset.
    public void setBombActive(boolean bombActive) {
        this.bombActive = bombActive;
        if (bombActive && chara == 2) {
            aliceTarget = -1;
        }
    }

    // Set Alice target once when a homing hit is detected.
    public void setAliceTargetIfUnset(int targetIndex) {
        if (chara != 2 || type != 0 || aliceTarget != -1) {
            return;
        }
        aliceTarget = targetIndex;
    }

    public int getXFixed() {
        return x;
    }

    public int getYFixed() {
        return y;
    }

    // Player collision hitbox radius.
    public int getHitboxRadiusFixed() {
        // Reimu special ability: smaller hitbox. 
        // NOTE: In the orig.ver., the hitboxes of 3 player are the same size, which I think is a bug!
        // Fix: 0.75
        return (chara == 0) ? REIMU_PLAYER_HITBOX : PLAYER_HITBOX_DEFAULT;
    }

    // Replay snapshot I/O.
    public void writeSnapshot(ReplaySnapshotWriter w) {
        if (w == null) {
            return;
        }
        w.writeI32LE(x);
        w.writeI32LE(y);
        w.writeBool(slow);
        w.writeBool(bombActive);
        w.writeBool(visible);

        w.writeI32LE(animeId);
        w.writeI32LE(animeNo);
        w.writeI32LE(animeCnt);
        w.writeI32LE(animeMove);
        w.writeI32LE(tick);

        w.writeI32LE(prevMoveDir);
        w.writeI32LE(lastMoveDir);

        w.writeI32LE(optLv);
        w.writeI32LE(optSpriteLv);
        w.writeI32LE(optbdir);
        w.writeI32LE(optroll);
        w.writeI32LE(optlr);

        for (int i = 0; i < 4; i++) {
            w.writeI32LE(optcx[i]);
            w.writeI32LE(optcy[i]);
            w.writeI32LE(optdir[i]);
            w.writeI32LE(optrad[i]);
        }

        w.writeI32LE(aliceTarget);
        for (int i = 0; i < 4; i++) {
            w.writeI32LE(opttgt[i]);
            w.writeI32LE(optmcx[i]);
            w.writeI32LE(optmcy[i]);
            w.writeI32LE(optmode[i]);
        }

        for (int i = 0; i < 4; i++) {
            w.writeI32LE(optAimArc[i]);
            w.writeI32LE(optAimPower[i]);
        }
    }

    public void readSnapshot(ReplaySnapshotReader r) {
        if (r == null) {
            return;
        }
        x = r.readI32LE();
        y = r.readI32LE();
        slow = r.readBool();
        bombActive = r.readBool();
        visible = r.readBool();

        animeId = r.readI32LE();
        animeNo = r.readI32LE();
        animeCnt = r.readI32LE();
        animeMove = r.readI32LE();
        tick = r.readI32LE();

        prevMoveDir = r.readI32LE();
        lastMoveDir = r.readI32LE();

        optLv = r.readI32LE();
        optSpriteLv = r.readI32LE();
        optbdir = r.readI32LE();
        optroll = r.readI32LE();
        optlr = r.readI32LE();

        for (int i = 0; i < 4; i++) {
            optcx[i] = r.readI32LE();
            optcy[i] = r.readI32LE();
            optdir[i] = r.readI32LE();
            optrad[i] = r.readI32LE();
        }

        aliceTarget = r.readI32LE();
        for (int i = 0; i < 4; i++) {
            opttgt[i] = r.readI32LE();
            optmcx[i] = r.readI32LE();
            optmcy[i] = r.readI32LE();
            optmode[i] = r.readI32LE();
        }

        for (int i = 0; i < 4; i++) {
            optAimArc[i] = r.readI32LE();
            optAimPower[i] = r.readI32LE();
        }
    }

    // Used by BattleEngine for death/respawn.
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    // Used by BattleEngine for death/respawn.
    public void setPositionFixed(int xFixed, int yFixed) {
        this.x = xFixed;
        this.y = yFixed;
    }

    // Mirrors DoJa : cb() clears option/laser related state.
    public void clearOptionsAndTargets() {
        optLv = 0;
        optSpriteLv = 0;
        optbdir = 0;
        optroll = 0;
        optlr = 0;

        aliceTarget = -1;
        for (int i = 0; i < 4; i++) {
            opttgt[i] = -1;
            optmode[i] = 0;
            optcx[i] = x;
            optcy[i] = y;
            optmcx[i] = x;
            optmcy[i] = y;
        }
    }

    // Movement, input handling, and clamp to playfield bounds.
    public void update(int keys, int playX, int playY, int playW, int playH) {
        int slowSpd;
        if (chara == 0) {
            slowSpd = 196608;
        } else if (chara == 1) {
            slowSpd = 327680;
        } else {
            slowSpd = 262144;
        }

        int spd = slow ? slowSpd : (slowSpd + 196608);
        if (bombActive && chara == 1) {
            spd = SPEED_MARISA_BOMB;
        }

        boolean up = (keys & GameCanvas.UP_PRESSED) != 0;
        boolean down = (keys & GameCanvas.DOWN_PRESSED) != 0;
        boolean left = (keys & GameCanvas.LEFT_PRESSED) != 0;
        boolean right = (keys & GameCanvas.RIGHT_PRESSED) != 0;

        int moveDir = 0;

        if (down) {
            y += spd;
            spd--;
        } else if (up) {
            y -= spd;
            spd--;
        }

        if (left) {
            x -= spd;
            moveDir = -1;
        } else if (right) {
            x += spd;
            moveDir = 1;
        }

        if (chara == 2 && type != 0) {
            if (left) {
                if (optbdir > -8) {
                    optbdir--;
                }
            } else if (right) {
                if (optbdir < 8) {
                    optbdir++;
                }
            }
        }

        int minX = Fixed.fromInt(playX + CLAMP_MARGIN);
        int maxX = Fixed.fromInt(playX + playW - CLAMP_MARGIN);
        int minY = Fixed.fromInt(playY + CLAMP_MARGIN);
        int maxY = Fixed.fromInt(playY + playH - CLAMP_MARGIN);

        if (x < minX) {
            x = minX;
        }
        if (x > maxX) {
            x = maxX;
        }
        if (y < minY) {
            y = minY;
        }
        if (y > maxY) {
            y = maxY;
        }

        lastMoveDir = moveDir;

        updateAnime(moveDir);
    }

    // Convenience overloads for option updates.
    public void updateFirepowerDevices(int power) {
        updateFirepowerDevices(null, null, power);
    }

    public void updateFirepowerDevices(BulletSystem bullets, int power) {
        updateFirepowerDevices(bullets, null, power);
    }

    public void updateFirepowerDevices(BulletSystem bullets, EnemySystem enemies, int power) {
        updateFirepowerDevices(bullets, enemies, power, false);
    }

    public void updateFirepowerDevices(BulletSystem bullets, EnemySystem enemies, int power, boolean barrierActive) {
        // Convert power to shot level (0-4).
        int shotLevel = 0;
        if (power >= 32) {
            shotLevel++;
        }
        if (power >= 64) {
            shotLevel++;
        }
        if (power >= 96) {
            shotLevel++;
        }
        if (power >= 128) {
            shotLevel++;
        }
        if (shotLevel < 0) {
            shotLevel = 0;
        }
        if (shotLevel > 4) {
            shotLevel = 4;
        }

        // Mode: 0=fast, 1=slow.
        int mode = slow ? 1 : 0;

        // Marisa A option sprite level depends on laser stage.
        if (chara == 1 && type == 0) {
            int s0 = 0;
            int s1 = 0;
            if (bullets != null) {
                s0 = bullets.getLaserStageFlag(103, 2, 0, true);
                s1 = bullets.getLaserStageFlag(103, 2, 1, true);
            }

            int s = (s0 > s1) ? s0 : s1;
            if (s <= 0) {
                optSpriteLv = 0;
            } else if (s < 5) {
                optSpriteLv = 1;
            } else {
                optSpriteLv = 2;
            }
        } else {
            optSpriteLv = 0;
        }

        // Alice option control and doll targeting.
        if (chara == 2) {
            if (bombActive) {
                aliceTarget = -1;
            }
            if (type == 0) {
                // Alice type A: orbiting options with optional aiming.
                if (mode != 0 && aliceTarget != -1 && (enemies == null || !enemies.isTargetable(aliceTarget))) {
                    aliceTarget = -1;
                }
                if (lastMoveDir < 0) {
                    if (mode == 0) {
                        optbdir -= 8;
                    }
                } else if (lastMoveDir > 0) {
                    if (mode == 0) {
                        optbdir += 8;
                    }
                }

                if (optbdir < 0) {
                    optbdir += 4 + (mode << 2);
                }
                if (optbdir > 0) {
                    optbdir -= 4 + (mode << 2);
                }
                if (optbdir > 40) {
                    optbdir = 40;
                }
                if (optbdir < -40) {
                    optbdir = -40;
                }

                optLv = shotLevel + 1;
                if (optLv > 4) {
                    optLv = 4;
                }

                int n;
                int[] work = optTmpWork;
                if (mode == 0) {
                    n = 20;
                    switch (optLv) {
                        case 1:
                            work[0] = 0;
                            break;
                        case 2:
                            work[0] = 270;
                            work[1] = 90;
                            break;
                        case 3:
                            work[0] = 270;
                            work[1] = 90;
                            work[2] = 0;
                            break;
                        default:
                            work[0] = 270;
                            work[1] = 90;
                            work[2] = 315;
                            work[3] = 45;
                            break;
                    }
                } else {
                    n = 28;
                    switch (optLv) {
                        case 1:
                            work[0] = 180;
                            break;
                        case 2:
                            work[0] = 270;
                            work[1] = 90;
                            break;
                        case 3:
                            work[0] = 270;
                            work[1] = 180;
                            work[2] = 90;
                            break;
                        default:
                            work[0] = 225;
                            work[1] = 135;
                            work[2] = 270;
                            work[3] = 90;
                            break;
                    }
                }

                for (int j = 0; j < optLv; j++) {
                    int target = (work[j] + 360) % 360;
                    for (int k = 0; k < 2; k++) {
                        if (optdir[j] < target) {
                            if (target - optdir[j] > 180) {
                                optdir[j] = (optdir[j] - 5 + 360) % 360;
                            } else {
                                optdir[j] = (optdir[j] + 5 + 360) % 360;
                            }
                        }
                        if (optdir[j] > target) {
                            if (optdir[j] - target > 180) {
                                optdir[j] = (optdir[j] + 5 + 360) % 360;
                            } else {
                                optdir[j] = (optdir[j] - 5 + 360) % 360;
                            }
                            optdir[j] = (optdir[j] - 5 + 360) % 360;
                        }
                    }

                    int want = n << 3;
                    if (bombActive) {
                        want = 0;
                    }
                    if (optrad[j] < want) {
                        optrad[j] += 16;
                    }
                    if (optrad[j] > want) {
                        optrad[j] -= 16;
                    }
                    if (bombActive && optrad[j] < 0) {
                        optrad[j] = 0;
                    }

                    int distPx = optrad[j] >> 3;
                    int ang = (optbdir + optdir[j] + 360) % 360 + 270;
                    int se = Fixed.mul(Trig.cos(ang), Fixed.fromInt(distPx));
                    int te = Fixed.mul(Trig.sin(ang), Fixed.fromInt(distPx));
                    optcx[j] = x + se;
                    optcy[j] = y + te;

                    int aim = 90;
                    int aimPow = 4;
                    if (bombActive) {
                        aliceTarget = -1;
                    }
                    if (mode != 0 && aliceTarget != -1 && enemies != null && enemies.isTargetable(aliceTarget)) {
                        int ex = enemies.getXFixed(aliceTarget);
                        int ey = enemies.getYFixed(aliceTarget);
                        int arc = arcTan2Deg(optcy[j] - ey - Fixed.fromInt(8), optcx[j] - ex);
                        if (arc >= 0 && arc <= 180) {
                            aim = arc;
                            aimPow = 3;
                        } else {
                            aliceTarget = -1;
                        }
                    }
                    optAimArc[j] = aim;
                    optAimPower[j] = aimPow;
                }

                for (int j = optLv; j < 4; j++) {
                    optAimArc[j] = 90;
                    optAimPower[j] = 4;
                }
                return;
            }

            // Alice type B: dolls orbit, chase targets, and aim on lock.
            optLv = shotLevel;
            if (optLv > 4) {
                optLv = 4;
            }
            if (optbdir != 0) {
                if (optbdir > 0) {
                    optlr = 1;
                } else {
                    optlr = 2;
                }
                optbdir = 0;
            }
            if (optlr == 1) {
                optroll = (8 + optroll) % 360;
            }
            if (optlr == 2) {
                optroll = (352 + optroll) % 360;
            }

            int[] work = optTmpWork;
            int[] r = optTmpR;
            for (int i = 0; i < 4; i++) {
                work[i] = 0;
                r[i] = 20;
                optAimArc[i] = 90;
                optAimPower[i] = 3;
            }
            switch (optLv) {
                case 1:
                    work[0] = 0;
                    break;
                case 2:
                    work[0] = 270;
                    work[1] = 90;
                    break;
                case 3:
                    work[0] = 300;
                    work[1] = 60;
                    work[2] = 180;
                    break;
                case 4:
                    work[0] = 315;
                    work[1] = 45;
                    work[2] = 135;
                    work[3] = 225;
                    break;
                default:
                    break;
            }
            for (int j = 0; j < optLv; j++) {
                if (mode == 0) {
                    if (optmode[j] >= 2) {
                        optmode[j] = 1;
                    }
                    opttgt[j] = -1;
                } else {
                    if (bombActive && optmode[j] >= 2) {
                        optmode[j] = 1;
                        opttgt[j] = -1;
                    }

                    if (optmode[j] >= 2) {
                        boolean lost = bombActive || (enemies == null || opttgt[j] < 0 || !enemies.isTargetable(opttgt[j]));
                        if (!lost && enemies != null && opttgt[j] >= 0) {
                            int ex = enemies.getXFixed(opttgt[j]);
                            int ey = enemies.getYFixed(opttgt[j]);
                            if (ex < -1966080 || ex > 14680064 || ey < -1441792 || ey > 17301504) {
                                lost = true;
                            }
                        }
                        if (lost) {
                            optmode[j] = 1;
                            opttgt[j] = -1;
                        }
                    }
                    if (!bombActive && optmode[j] <= 1 && opttgt[j] == -1 && enemies != null) {
                        int t = findAliceDollTargetByX(enemies, optcx[j], barrierActive);
                        opttgt[j] = t;
                        if (t != -1) {
                            optmode[j] = 2;
                        }
                    }
                }

                switch (optmode[j]) {
                    case 3:
                        if (enemies != null && opttgt[j] >= 0 && enemies.isTargetable(opttgt[j])) {
                            optmcx[j] = enemies.getXFixed(opttgt[j]);
                            optmcy[j] = enemies.getYFixed(opttgt[j]);
                            r[j] = 40;
                        } else {
                            optmode[j] = 1;
                            opttgt[j] = -1;
                        }
                        break;
                    case 2:
                        if (enemies != null && opttgt[j] >= 0 && enemies.isTargetable(opttgt[j])) {
                            int ex = enemies.getXFixed(opttgt[j]);
                            int ey = enemies.getYFixed(opttgt[j]);
                            int dx = ex - optmcx[j];
                            int dy = ey - optmcy[j];
                            int arc = arcTan2Deg(dy, dx);
                            int ang = normalizeDeg(arc);
                            int spd = 8 + j;
                            int se = Fixed.mul(Trig.cos(ang), Fixed.fromInt(spd));
                            int te = Fixed.mul(Trig.sin(ang), Fixed.fromInt(spd));
                            optmcx[j] += se;
                            optmcy[j] += te;
                            if (rectsOverlapFixed(
                                    optmcx[j] - ALICE_DOLL_BOX_HALF,
                                    optmcy[j] - ALICE_DOLL_BOX_HALF,
                                    ALICE_DOLL_BOX_HALF << 1,
                                    ALICE_DOLL_BOX_HALF << 1,
                                    ex - ALICE_TARGET_BOX_HALF,
                                    ey - ALICE_TARGET_BOX_HALF,
                                    ALICE_TARGET_BOX_HALF << 1,
                                    ALICE_TARGET_BOX_HALF << 1)) {
                                optmode[j] = 3;
                            }
                        } else {
                            optmode[j] = 1;
                            opttgt[j] = -1;
                        }
                        break;
                    case 1: {
                        int dx = x - optmcx[j];
                        int dy = y - optmcy[j];
                        int arc = arcTan2Deg(dy, dx);
                        int ang = normalizeDeg(arc);
                        int spd = 12 - j;
                        int se = Fixed.mul(Trig.cos(ang), Fixed.fromInt(spd));
                        int te = Fixed.mul(Trig.sin(ang), Fixed.fromInt(spd));
                        optmcx[j] += se;
                        optmcy[j] += te;
                        if (rectsOverlapFixed(
                                optmcx[j] - ALICE_RETURN_BOX_HALF,
                                optmcy[j] - ALICE_RETURN_BOX_HALF,
                                ALICE_RETURN_BOX_HALF << 1,
                                ALICE_RETURN_BOX_HALF << 1,
                                x - ALICE_PLAYER_HITBOX,
                                y - ALICE_PLAYER_HITBOX,
                                (ALICE_PLAYER_HITBOX << 1) + 1,
                                (ALICE_PLAYER_HITBOX << 1) + 1)) {
                            optmode[j] = 0;
                        }
                        break;
                    }
                    default:
                        optmcx[j] = x;
                        optmcy[j] = y;
                        optmode[j] = 0;
                        break;
                }

                int target = (work[j] + 360) % 360;
                for (int k = 0; k < 2; k++) {
                    if (optdir[j] < target) {
                        if (target - optdir[j] > 180) {
                            optdir[j] = (optdir[j] - 5 + 360) % 360;
                        } else {
                            optdir[j] = (optdir[j] + 5 + 360) % 360;
                        }
                    }
                    if (optdir[j] > target) {
                        if (optdir[j] - target > 180) {
                            optdir[j] = (optdir[j] + 5 + 360) % 360;
                        } else {
                            optdir[j] = (optdir[j] - 5 + 360) % 360;
                        }
                        optdir[j] = (optdir[j] - 5 + 360) % 360;
                    }
                }

                int want = r[j] << 3;
                if (bombActive) {
                    want = 0;
                }
                if (optrad[j] < want) {
                    optrad[j] += 16;
                }
                if (optrad[j] > want) {
                    optrad[j] -= 16;
                }
                if (bombActive && optrad[j] < 0) {
                    optrad[j] = 0;
                }

                int dist = optrad[j];
                int ang = (optroll + optdir[j] + 360) % 360 + 270;
                if (dist < 0) {
                    dist = -dist;
                    ang = normalizeDeg(ang + 180);
                }
                int distPx = dist >> 3;
                int se = Fixed.mul(Trig.cos(ang), Fixed.fromInt(distPx));
                int te = Fixed.mul(Trig.sin(ang), Fixed.fromInt(distPx));
                optcx[j] = optmcx[j] + se;
                optcy[j] = optmcy[j] + te;

                if (mode != 0 && optmode[j] == 3 && enemies != null && opttgt[j] >= 0 && enemies.isTargetable(opttgt[j])) {
                    int ex = enemies.getXFixed(opttgt[j]);
                    int ey = enemies.getYFixed(opttgt[j]);
                    optAimArc[j] = arcTan2Deg(optcy[j] - ey, optcx[j] - ex);
                }
            }

            for (int j = optLv; j < 4; j++) {
                opttgt[j] = -1;
                optmode[j] = 0;
                optmcx[j] = x;
                optmcy[j] = y;
                optAimArc[j] = 90;
                optAimPower[j] = 3;
            }
            return;
        }

        // Reimu/Marisa option orbit positions.
        optLv = 2;
        if (mode == 0) {
            if (optdir[0] < 90) {
                optdir[0] += 9;
            } else {
                optdir[0] = 90;
            }
            if (optdir[1] > 270) {
                optdir[1] -= 9;
            } else {
                optdir[1] = 270;
            }
            for (int i = 0; i < 2; i++) {
                if (optrad[i] < 160) {
                    optrad[i] += 16;
                }
            }
        } else {
            if (optdir[0] > 18) {
                optdir[0] -= 9;
            } else {
                optdir[0] = 18;
            }
            if (optdir[1] < 342) {
                optdir[1] += 9;
            } else {
                optdir[1] = 342;
            }
            for (int i = 0; i < 2; i++) {
                if (optrad[i] > 128) {
                    optrad[i] -= 8;
                }
                if (optrad[i] < 128) {
                    optrad[i] += 16;
                }
            }
        }
        for (int i = 0; i < 2; i++) {
            int distPx = optrad[i] >> 3;
            int ang = optdir[i] + 270;
            int se = Fixed.mul(Trig.cos(ang), Fixed.fromInt(distPx));
            int te = Fixed.mul(Trig.sin(ang), Fixed.fromInt(distPx));
            optcx[i] = x + se + Fixed.fromInt(1);
            optcy[i] = y + te;
        }
    }

    // Update movement animation frames.
    private void updateAnime(int moveDir) {
        tick++;
        ed(0);

        if (moveDir < 0) {
            animeMove += 2;
            if (animeMove < 3) {
                ed(1);
            } else if (animeMove < 6) {
                ed(2);
            } else {
                ed(3);
            }
        } else if (moveDir > 0) {
            animeMove += 2;
            if (animeMove < 3) {
                ed(4);
            } else if (animeMove < 6) {
                ed(5);
            } else {
                ed(6);
            }
        } else {
            animeMove = 0;
        }

        if (animeId == 0) {
            animeMove = 0;
        }
        if (animeMove != 0) {
            animeMove--;
        }
        prevMoveDir = moveDir;
        fd();
    }

    // Switch animation sequence when state changes.
    private void ed(int newAnimeId) {
        if (animeId != newAnimeId) {
            animeId = newAnimeId;
            animeNo = 0;
            animeCnt = 0;
        }
    }

    // Advance animation frame timing.
    private void fd() {
        animeCnt++;
        short[] list = ANIMELIST[animeId];
        int idx = animeNo * 3;
        if (idx < 0 || idx >= list.length) {
            animeNo = 0;
            idx = 0;
            animeCnt = 0;
        }
        if ((list[idx] & 0xFF) < animeCnt) {
            animeNo++;
            animeCnt = 0;
            idx = animeNo * 3;
            if (idx < 0 || idx >= list.length) {
                animeNo = 0;
                idx = 0;
            }
        }
        if (idx < list.length && (list[idx] & 0xFF) == 254) {
            animeNo = 0;
        }
        if (idx < list.length && (list[idx] & 0xFF) == 255) {
            int next = 0;
            if (idx + 1 < list.length) {
                next = list[idx + 1] & 0xFFFF;
            }
            if (next >= 0 && next < ANIMELIST.length) {
                animeId = next;
            }
            animeNo = 0;
        }
    }

    // Spawn shot patterns based on character, type, and shot level.
    public void waTick(BulletSystem bullets, int fi, int power) {
        if (bullets == null) {
            return;
        }

        int opt0x = optcx[0];
        int opt1x = optcx[1];
        int opt0y = optcy[0];
        int opt1y = optcy[1];

        int shotLevel = 0;
        if (power >= 32) {
            shotLevel++;
        }
        if (power >= 64) {
            shotLevel++;
        }
        if (power >= 96) {
            shotLevel++;
        }
        if (power >= 128) {
            shotLevel++;
        }
        if (shotLevel < 0) {
            shotLevel = 0;
        }
        if (shotLevel > 4) {
            shotLevel = 4;
        }

        int mode = slow ? 1 : 0;
        int cx = x;
        int cy = y;

        if (opt0x == 0 && opt1x == 0) {
            opt0x = cx - Fixed.fromInt(12);
            opt1x = cx + Fixed.fromInt(12);
            opt0y = cy - Fixed.fromInt(8);
            opt1y = cy - Fixed.fromInt(8);
        }

        // Reimu shot tables.
        if (chara == 0) {
            if (type == 0) {
                if (mode == 0) {
                    switch (shotLevel) {
                        case 0:
                            if ((fi & 1) == 0) {
                                xaSpawn(bullets, 0, 0, 6, 16, cx, cy - Fixed.fromInt(10));
                            }
                            if (fi % 12 == 0) {
                                xaSpawn(bullets, 1, 0, 2, 10, opt1x, opt1y - Fixed.fromInt(10));
                                xaSpawn(bullets, 1, 0, 2, 10, opt0x, opt0y - Fixed.fromInt(10));
                            }
                            return;
                        case 1:
                            if ((fi & 1) == 0) {
                                xaSpawn(bullets, 0, 0, 4, 16, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 0, 4, 16, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                            }
                            if ((fi & 7) == 0) {
                                xaSpawn(bullets, 1, 0, 2, 10, opt1x, opt1y - Fixed.fromInt(10));
                                xaSpawn(bullets, 1, 0, 2, 10, opt0x, opt0y - Fixed.fromInt(10));
                            }
                            return;
                        case 2:
                            if ((fi & 1) == 0) {
                                xaSpawn(bullets, 0, 355, 2, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 0, 7, 16, cx, cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 5, 2, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            }
                            if ((fi & 3) == 0) {
                                xaSpawn(bullets, 1, 0, 2, 10, opt1x, opt1y - Fixed.fromInt(10));
                                xaSpawn(bullets, 1, 0, 2, 10, opt0x, opt0y - Fixed.fromInt(10));
                            }
                            return;
                        case 3:
                            if ((fi & 1) == 0) {
                                xaSpawn(bullets, 0, 355, 3, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 0, 7, 16, cx, cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 5, 3, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            }
                            switch (fi % 6) {
                                case 0:
                                    xaSpawn(bullets, 1, 350, 2, 10, opt1x, opt1y);
                                    xaSpawn(bullets, 1, 10, 2, 10, opt0x, opt0y);
                                    return;
                                case 2:
                                    xaSpawn(bullets, 1, 320, 2, 10, opt1x, opt1y);
                                    xaSpawn(bullets, 1, 40, 2, 10, opt0x, opt0y);
                                    return;
                                case 4:
                                    xaSpawn(bullets, 1, 290, 2, 10, opt1x, opt1y);
                                    xaSpawn(bullets, 1, 70, 2, 10, opt0x, opt0y);
                                    return;
                                default:
                                    return;
                            }
                        case 4:
                            if ((fi & 1) == 0) {
                                xaSpawn(bullets, 0, 350, 1, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 355, 3, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 0, 7, 16, cx, cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 5, 3, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 0, 10, 1, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            }
                            switch (fi % 6) {
                                case 0:
                                    xaSpawn(bullets, 1, 350, 2, 10, opt1x, opt1y);
                                    xaSpawn(bullets, 1, 10, 2, 10, opt0x, opt0y);
                                    return;
                                case 2:
                                    xaSpawn(bullets, 1, 320, 2, 10, opt1x, opt1y);
                                    xaSpawn(bullets, 1, 40, 2, 10, opt0x, opt0y);
                                    return;
                                case 4:
                                    xaSpawn(bullets, 1, 290, 2, 10, opt1x, opt1y);
                                    xaSpawn(bullets, 1, 70, 2, 10, opt0x, opt0y);
                                    return;
                                default:
                                    return;
                            }
                        default:
                            return;
                    }
                }

                switch (shotLevel) {
                    case 0:
                    case 1:
                        if (fi % 3 == 0) {
                            xaSpawn(bullets, 0, 0, 7 + (shotLevel << 1), 16, cx, cy - Fixed.fromInt(10));
                        }
                        if (fi % 6 == 0) {
                            xaSpawn(bullets, 2, 0, 7 + (shotLevel << 1), 10, cx, cy);
                        }
                        return;
                    case 2:
                        if (fi % 3 == 0) {
                            xaSpawn(bullets, 0, 0, 5, 16, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 0, 5, 16, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                        }
                        if (fi % 6 == 0) {
                            xaSpawn(bullets, 2, 340, 7, 10, cx, cy);
                            xaSpawn(bullets, 2, 20, 7, 10, cx, cy);
                        }
                        return;
                    case 3:
                        if (fi % 3 == 0) {
                            xaSpawn(bullets, 0, 355, 3, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 0, 8, 16, cx, cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 5, 3, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        }
                        if (fi % 6 == 0) {
                            xaSpawn(bullets, 2, 350, 4, 10, cx, cy);
                            xaSpawn(bullets, 2, 10, 4, 10, cx, cy);
                        }
                        if (fi % 10 == 0) {
                            xaSpawn(bullets, 2, 340, 5, 10, cx, cy);
                            xaSpawn(bullets, 2, 20, 5, 10, cx, cy);
                        }
                        return;
                    case 4:
                        if (fi % 3 == 0) {
                            xaSpawn(bullets, 0, 355, 3, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 0, 8, 16, cx, cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 5, 3, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        }
                        if (fi % 6 == 0) {
                            xaSpawn(bullets, 2, 350, 5, 10, cx, cy);
                            xaSpawn(bullets, 2, 340, 5, 10, cx, cy);
                            xaSpawn(bullets, 2, 10, 5, 10, cx, cy);
                            xaSpawn(bullets, 2, 20, 5, 10, cx, cy);
                        }
                        return;
                    default:
                        return;
                }
            }

            if (mode == 0) {
                switch (shotLevel) {
                    case 0:
                        if ((fi & 1) == 0) {
                            xaSpawn(bullets, 0, 0, 5, 16, cx, cy - Fixed.fromInt(10));
                        }
                        if ((fi & 7) == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                        }
                        return;
                    case 1:
                        if ((fi & 1) == 0) {
                            xaSpawn(bullets, 0, 0, 3, 16, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 0, 3, 16, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                        }
                        if (fi % 6 == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                        }
                        if (fi % 3 == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, cx, cy - Fixed.fromInt(10));
                        }
                        return;
                    case 2:
                        if ((fi & 1) == 0) {
                            xaSpawn(bullets, 0, 352, 2, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 0, 5, 16, cx, cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 8, 2, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        }
                        if (fi % 6 == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                        }
                        if (fi % 3 == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, cx, cy - Fixed.fromInt(10));
                        }
                        return;
                    case 3:
                        if ((fi & 1) == 0) {
                            xaSpawn(bullets, 0, 352, 3, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 0, 5, 16, cx, cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 8, 3, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        }
                        if ((fi & 1) == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, cx, cy - Fixed.fromInt(10));
                        }
                        if ((fi & 3) == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, cx - Fixed.fromInt(8), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 4, 10, cx + Fixed.fromInt(8), cy - Fixed.fromInt(10));
                        }
                        if (fi % 6 == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                        }
                        return;
                    case 4:
                        if ((fi & 1) == 0) {
                            xaSpawn(bullets, 0, 344, 1, 16, cx - Fixed.fromInt(8), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 352, 2, 16, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 0, 5, 16, cx, cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 8, 2, 16, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 0, 16, 1, 16, cx + Fixed.fromInt(8), cy - Fixed.fromInt(10));
                        }
                        if ((fi & 1) == 0) {
                            xaSpawn(bullets, 18, 0, 3, 10, cx, cy - Fixed.fromInt(10));
                        }
                        if ((fi & 3) == 0) {
                            xaSpawn(bullets, 18, 0, 3, 10, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 3, 10, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 3, 10, cx - Fixed.fromInt(12), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 3, 10, cx + Fixed.fromInt(12), cy - Fixed.fromInt(10));
                        }
                        if (fi % 6 == 0) {
                            xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                            xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                        }
                        return;
                    default:
                        return;
                }
            }

            switch (shotLevel) {
                case 0:
                    if ((fi & 1) == 0) {
                        xaSpawn(bullets, 0, 0, 5, 16, cx, cy - Fixed.fromInt(10));
                    }
                    if ((fi & 7) == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                    }
                    return;
                case 1:
                    if ((fi & 1) == 0) {
                        xaSpawn(bullets, 0, 0, 3, 16, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 0, 3, 16, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                    }
                    if (fi % 6 == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                    }
                    if (fi % 3 == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, cx, cy - Fixed.fromInt(10));
                    }
                    return;
                case 2:
                    if ((fi & 1) == 0) {
                        xaSpawn(bullets, 0, 355, 2, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 0, 5, 16, cx, cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 5, 2, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                    }
                    if (fi % 6 == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                    }
                    if (fi % 3 == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, cx, cy - Fixed.fromInt(10));
                    }
                    return;
                case 3:
                    if ((fi & 1) == 0) {
                        xaSpawn(bullets, 0, 355, 3, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 0, 5, 16, cx, cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 5, 3, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                    }
                    if ((fi & 1) == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, cx, cy - Fixed.fromInt(10));
                    }
                    if ((fi & 3) == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, cx - Fixed.fromInt(8), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 4, 10, cx + Fixed.fromInt(8), cy - Fixed.fromInt(10));
                    }
                    if (fi % 6 == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                    }
                    return;
                case 4:
                    if ((fi & 1) == 0) {
                        xaSpawn(bullets, 0, 350, 1, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 355, 2, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 0, 5, 16, cx, cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 5, 2, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 0, 10, 1, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                    }
                    if ((fi & 1) == 0) {
                        xaSpawn(bullets, 18, 0, 3, 10, cx, cy - Fixed.fromInt(10));
                    }
                    if ((fi & 3) == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 4, 10, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 3, 10, cx - Fixed.fromInt(12), cy - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 3, 10, cx + Fixed.fromInt(12), cy - Fixed.fromInt(10));
                    }
                    if (fi % 6 == 0) {
                        xaSpawn(bullets, 18, 0, 4, 10, opt1x, opt1y - Fixed.fromInt(10));
                        xaSpawn(bullets, 18, 0, 4, 10, opt0x, opt0y - Fixed.fromInt(10));
                    }
                    return;
                default:
                    return;
            }
        }

        // Marisa shot tables.
        if (chara == 1) {
            if (type == 0) {
                if (mode == 0) {
                    if ((fi & 1) == 0) {
                        switch (shotLevel) {
                            case 0:
                                xaSpawn(bullets, 8, 0, 2, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 0, 2, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                                break;
                            case 1:
                            case 2:
                                xaSpawn(bullets, 8, -5, shotLevel, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 0, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 0, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 5, shotLevel, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                break;
                            case 3:
                                xaSpawn(bullets, 8, -5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, -5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 0, 3, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 0, 3, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                break;
                            case 4:
                                xaSpawn(bullets, 8, -10, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, -10, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, -5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, -5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 0, 3, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 0, 3, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 10, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                xaSpawn(bullets, 8, 10, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                                break;
                            default:
                                break;
                        }
                    }
                } else if ((fi & 1) == 0) {
                    switch (shotLevel) {
                        case 0:
                            xaSpawn(bullets, 8, 0, 4, 16, cx, cy - Fixed.fromInt(10));
                            break;
                        case 1:
                            xaSpawn(bullets, 8, 0, 3, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 3, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            break;
                        case 2:
                        case 3:
                            xaSpawn(bullets, 8, -5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, shotLevel, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, shotLevel, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            break;
                        case 4:
                            xaSpawn(bullets, 8, -5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, -5, 3, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 3, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 3, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 5, 3, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            break;
                        default:
                            break;
                    }
                }

                yaSpawn(bullets, 9, 0, 1, opt1x, opt1y - Fixed.fromInt(10));
                yaSpawn(bullets, 9, 1, 1, opt0x, opt0y - Fixed.fromInt(10));
                return;
            }

            if (mode == 0) {
                if ((fi & 1) == 0) {
                    switch (shotLevel) {
                        case 0:
                            xaSpawn(bullets, 8, 0, 2, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 2, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                            break;
                        case 1:
                        case 2:
                            xaSpawn(bullets, 8, -5, shotLevel, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 5, shotLevel, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            break;
                        case 3:
                            xaSpawn(bullets, 8, -5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, -5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            break;
                        case 4:
                            xaSpawn(bullets, 8, -10, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, -10, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, -5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, -5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 0, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 10, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            xaSpawn(bullets, 8, 10, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                            break;
                        default:
                            break;
                    }
                }

                if (fi % 3 == 0) {
                    xaSpawn(bullets, 19, 0, 3 + (shotLevel >> 1), 4, opt1x, opt1y - Fixed.fromInt(10));
                    xaSpawn(bullets, 19, 0, 3 + (shotLevel >> 1), 4, opt0x, opt0y - Fixed.fromInt(10));
                }
                return;
            }

            if ((fi & 1) == 0) {
                if (shotLevel == 0) {
                    xaSpawn(bullets, 8, 0, 4, 16, cx, cy - Fixed.fromInt(10));
                } else if (shotLevel == 1) {
                    xaSpawn(bullets, 8, 0, 3, 16, cx - Fixed.fromInt(6), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, 0, 3, 16, cx + Fixed.fromInt(6), cy - Fixed.fromInt(10));
                } else if (shotLevel == 2 || shotLevel == 3) {
                    xaSpawn(bullets, 8, -5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, 0, shotLevel, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, 0, shotLevel, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, 5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                } else {
                    xaSpawn(bullets, 8, -5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, -5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, 0, 3, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, 0, 3, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, 5, 2, 16, cx - Fixed.fromInt(5), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, 8, 5, 2, 16, cx + Fixed.fromInt(5), cy - Fixed.fromInt(10));
                }
            }

            if ((fi & 1) == 0) {
                xaSpawn(bullets, 19, 0, 1 + (shotLevel >> 1), 4, opt1x, opt1y - Fixed.fromInt(10));
                xaSpawn(bullets, 19, 0, 1 + (shotLevel >> 1), 4, opt0x, opt0y - Fixed.fromInt(10));
            }
            return;
        }

        // Alice shot tables.
        int optLv = this.optLv;

        if (type == 0) {
            if ((fi & 1) != 0) {
                return;
            }

            if (mode == 0) {
                for (int i = 0; i < optLv; i++) {
                    xaSpawn(bullets, 13, optbdir, 3, 24, optcx[i], optcy[i] - Fixed.fromInt(8));
                }
                aliceTarget = -1;
            } else {
                int optBullet = (aliceTarget != -1) ? 13 : 14;
                for (int i = 0; i < optLv; i++) {
                    xaSpawn(bullets, optBullet, optAimArc[i] + 270, optAimPower[i], 24, optcx[i], optcy[i] - Fixed.fromInt(8));
                }
            }

            int n = (mode == 0) ? 11 : 12;
            switch (shotLevel) {
                case 0:
                    xaSpawn(bullets, n, 0, 5, 26, cx, cy - Fixed.fromInt(10));
                    return;
                case 1:
                    xaSpawn(bullets, n, 0, 2, 26, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, n, 0, 2, 26, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                    return;
                case 2:
                case 3:
                    xaSpawn(bullets, n, 0, 3, 26, cx, cy - Fixed.fromInt(10));
                    xaSpawn(bullets, n, 0, 1, 26, cx + Fixed.fromInt(8), cy - Fixed.fromInt(5));
                    xaSpawn(bullets, n, 0, 1, 26, cx - Fixed.fromInt(8), cy - Fixed.fromInt(5));
                    return;
                case 4:
                    xaSpawn(bullets, n, 0, 1, 26, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, n, 0, 2, 26, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                    xaSpawn(bullets, n, 0, 2, 26, cx + Fixed.fromInt(9), cy - Fixed.fromInt(5));
                    xaSpawn(bullets, n, 0, 1, 26, cx - Fixed.fromInt(9), cy - Fixed.fromInt(5));
                    return;
                default:
                    return;
            }
        }

        if ((fi & 1) != 0) {
            return;
        }

        if (mode == 0) {
            for (int i = 0; i < optLv; i++) {
                xaSpawn(bullets, 13, 0, 3, 24, optcx[i], optcy[i] - Fixed.fromInt(8));
            }
        } else {
            int speed = 68;
            for (int i = 0; i < optLv; i++) {
                if (optmode[i] == 3 && opttgt[i] != -1) {
                    int arc = optAimArc[i];
                    int se = (int) (((long) Trig.cos(arc) * (long) speed) >> 3);
                    int te = (int) (((long) Trig.sin(arc) * (long) speed) >> 3);
                    xaSpawn(bullets, 15, arc + 270, 3, 20, optcx[i] - se, optcy[i] - te);
                }
            }
        }

        switch (shotLevel) {
            case 0:
                xaSpawn(bullets, 11, 0, 5, 26, cx, cy - Fixed.fromInt(10));
                return;
            case 1:
                xaSpawn(bullets, 11, 0, 2, 26, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                xaSpawn(bullets, 11, 0, 2, 26, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                return;
            case 2:
            case 3:
                xaSpawn(bullets, 11, 0, 3, 26, cx, cy - Fixed.fromInt(10));
                xaSpawn(bullets, 11, 0, 1, 26, cx + Fixed.fromInt(8), cy - Fixed.fromInt(5));
                xaSpawn(bullets, 11, 0, 1, 26, cx - Fixed.fromInt(8), cy - Fixed.fromInt(5));
                return;
            case 4:
                xaSpawn(bullets, 11, 0, 1, 26, cx + Fixed.fromInt(4), cy - Fixed.fromInt(10));
                xaSpawn(bullets, 11, 0, 2, 26, cx - Fixed.fromInt(4), cy - Fixed.fromInt(10));
                xaSpawn(bullets, 11, 0, 2, 26, cx + Fixed.fromInt(9), cy - Fixed.fromInt(5));
                xaSpawn(bullets, 11, 0, 1, 26, cx - Fixed.fromInt(9), cy - Fixed.fromInt(5));
                return;
            default:
                return;
        }
    }

    // Draw options, player sprite, and slow aura.
    public void render(Graphics g, BulletSprites sprites) {
        if (g == null) {
            return;
        }

        if (!visible) {
            return;
        }
        int px = Fixed.toInt(x);
        int py = Fixed.toInt(y);

        if (sprites != null) {
            if (chara == 0) {
                int x1 = Fixed.toInt(optcx[1]);
                int y1 = Fixed.toInt(optcy[1]);
                int x0 = Fixed.toInt(optcx[0]);
                int y0 = Fixed.toInt(optcy[0]);
                sprites.draw(g, 27 + (tick & 0x3), x1, y1, false);
                sprites.draw(g, 27 + (3 - ((tick + 1) & 0x3)), x0, y0, false);
            } else if (chara == 1) {
                int id = 100 + optSpriteLv;
                int x1 = Fixed.toInt(optcx[1]);
                int y1 = Fixed.toInt(optcy[1]);
                int x0 = Fixed.toInt(optcx[0]);
                int y0 = Fixed.toInt(optcy[0]);
                sprites.draw(g, id, x1, y1, true);
                sprites.draw(g, id, x0, y0, true);
            } else {
                int id = 731;
                if (type == 0) {
                    if (optbdir > 20) {
                        id = 732;
                    }
                    if (optbdir < -20) {
                        id = 730;
                    }
                }
                for (int i = 0; i < optLv; i++) {
                    if (optrad[i] > 0) {
                        sprites.draw(g, id, Fixed.toInt(optcx[i]), Fixed.toInt(optcy[i]), false);
                    }
                }
            }
        }

        boolean drawn = false;
        if (sprites != null) {
            ImageBank imgs = sprites.getImages();
            if (imgs != null) {
                int imgIndex = 8 + chara;
                short[] list = ANIMELIST[animeId];
                int idx = animeNo * 3;
                int srcX = 0;
                int srcY = 0;
                if (idx + 2 < list.length) {
                    srcX = list[idx + 1] & 0xFFFF;
                    srcY = list[idx + 2] & 0xFFFF;
                }

                if (slow) {
                    int auraFrame = tick & 0x3;
                    int auraSrcX = auraFrame * 32;
                    Image aura = imgs.getAlphaRegion(19, auraSrcX, 117, 32, 31, 128);
                    if (aura != null) {
                        g.drawImage(aura, px - 15, py - 15, Graphics.TOP | Graphics.LEFT);
                    } else {
                        javax.microedition.lcdui.Image sheet = imgs.get(19);
                        if (sheet != null) {
                            touhou.ui.UiDraw.drawRegion(g, sheet, px - 15, py - 15, auraSrcX, 117, 32, 31);
                        }
                    }
                }

                javax.microedition.lcdui.Image playerImg = imgs.get(imgIndex);
                if (playerImg != null) {
                    touhou.ui.UiDraw.drawRegion(g, playerImg, px - 11, py - 11, srcX, srcY, 24, 24);
                    drawn = true;
                }
            }
        }

        if (!drawn) {
            boolean fallback = false;
            if (sprites != null && spriteId >= 0) {
                fallback = sprites.draw(g, spriteId, px, py);
            }
            if (!fallback) {
                g.setColor(0xFFFFFF);
                g.fillRect(px - 2, py - 2, 5, 5);
            }
        }

        if (slow) {
            g.setColor(0xCCCCCC);
            g.fillRect(px - 1, py - 1, 3, 3);
            g.setColor(0xFF0000);
            g.drawRect(px - 1, py - 1, 2, 2);
        }
    }

    // Fire interval in frames per character/type.
    public int shotInterval() {
        if (chara == 0) {
            return (type == 0) ? 4 : 3;
        }
        if (chara == 1) {
            return (type == 0) ? 2 : 4;
        }
        if (type == 0) {
            return slow ? 3 : 5;
        }
        return 4;
    }

    // Spawn basic player shots (non-waTick patterns).
    public void shoot(BulletSystem bullets) {
        if (bullets == null) {
            return;
        }
        int px = x;
        int py = y;

        if (chara == 0) {
            if (type == 0) {
                spawnShot(bullets, shotBulletId, 262, 6, px - Fixed.fromInt(3), py);
                spawnShot(bullets, shotBulletId, 278, 6, px + Fixed.fromInt(3), py);
                return;
            }
            spawnShot(bullets, shotBulletId, 270, 7, px - Fixed.fromInt(4), py);
            spawnShot(bullets, shotBulletId, 270, 7, px + Fixed.fromInt(4), py);
            return;
        }

        if (chara == 1) {
            int bid = (type == 0 && shotBulletId2 >= 0) ? shotBulletId2 : shotBulletId;
            if (type == 0) {
                spawnShot(bullets, bid, 270, 10, px, py);
                return;
            }
            spawnShot(bullets, bid, 268, 6, px - Fixed.fromInt(4), py);
            spawnShot(bullets, bid, 272, 6, px + Fixed.fromInt(4), py);
            return;
        }

        if (type == 0) {
            if (slow) {
                spawnShot(bullets, shotBulletId, 264, 6, px - Fixed.fromInt(7), py);
                spawnShot(bullets, shotBulletId, 276, 6, px + Fixed.fromInt(7), py);
                spawnShot(bullets, shotBulletId, 270, 7, px - Fixed.fromInt(2), py);
                spawnShot(bullets, shotBulletId, 270, 7, px + Fixed.fromInt(2), py);
                return;
            }
            int bid = (shotBulletId2 >= 0) ? shotBulletId2 : shotBulletId;
            spawnShot(bullets, bid, 266, 6, px - Fixed.fromInt(6), py);
            spawnShot(bullets, bid, 274, 6, px + Fixed.fromInt(6), py);
            return;
        }

        spawnShot(bullets, shotBulletId, 270, 6, px - Fixed.fromInt(5), py);
        spawnShot(bullets, shotBulletId, 270, 6, px + Fixed.fromInt(5), py);
        if (slow) {
            spawnShot(bullets, shotBulletId, 270, 6, px - Fixed.fromInt(12), py);
            spawnShot(bullets, shotBulletId, 270, 6, px + Fixed.fromInt(12), py);
        }
    }

    // Basic bullet spawn helper.
    private static void spawnShot(BulletSystem bullets, int bulletId, int ang, int speed, int fx, int fy) {
        if (bulletId < 0) {
            return;
        }
        bullets.spawnPlayer(bulletId, 0, ang, speed, fx, fy);
    }

    // Player bullet table spawn helper.
    private static void xaSpawn(BulletSystem bullets, int n, int i, int n2, int n3, int fx, int fy) {
        int baseBulletId;
        int frames;
        int moveType;
        int shotPower = n2;
        switch (n) {
            case 0:
                baseBulletId = 31;
                frames = 1;
                moveType = 0;
                break;
            case 1:
                baseBulletId = 32;
                frames = 1;
                moveType = 1;
                break;
            case 2:
                baseBulletId = 48;
                frames = 12;
                moveType = 1;
                break;
            case 18:
                baseBulletId = 60;
                frames = 1;
                moveType = 9;
                shotPower = n2 + 1;
                break;
            case 8:
                baseBulletId = 97;
                frames = 2;
                moveType = 0;
                break;
            case 19:
                baseBulletId = 99;
                frames = 1;
                moveType = 10;
                break;
            case 11:
                baseBulletId = 741;
                frames = 1;
                moveType = 0;
                break;
            case 12:
                baseBulletId = 741;
                frames = 1;
                moveType = 8;
                break;
            case 13:
                baseBulletId = 746;
                frames = 1;
                moveType = 0;
                break;
            case 14:
                baseBulletId = 742;
                frames = 1;
                moveType = 0;
                break;
            case 15:
                baseBulletId = 750;
                frames = 1;
                moveType = 11;
                break;
            case 16:
                baseBulletId = 733;
                frames = 4;
                moveType = 6;
                break;
            case 17:
                baseBulletId = 737;
                frames = 4;
                moveType = 7;
                break;
            default:
                return;
        }

        int ang = normalizeDeg(i + 270);
        if (n == 15) {
            int d0 = ang % 360;
            int frame = 15 - (((d0 / 22) + 11) & 0xF);
            baseBulletId += frame;
        }
        bullets.spawnPlayerEx(baseBulletId, frames, moveType, ang, n3, fx, fy, shotPower);
    }

    // Player laser spawn helper.
    private static void yaSpawn(BulletSystem bullets, int n, int laserSlot, int shotPower, int fx, int fy) {
        if (n == 9) {
            bullets.spawnOrUpdatePlayerLaser(103, 2, laserSlot, fx, fy, shotPower);
        } else if (n == 10) {
            bullets.spawnOrUpdatePlayerLaser(105, 3, laserSlot, fx, fy, shotPower);
        }
    }

    // Normalize degree angles to [0, 360).
    private static int normalizeDeg(int deg) {
        deg %= 360;
        if (deg < 0) {
            deg += 360;
        }
        return deg;
    }

    // Fixed-point AABB overlap check.
    private static boolean rectsOverlapFixed(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        long r1 = (long) x1 + (long) w1;
        long b1 = (long) y1 + (long) h1;
        long r2 = (long) x2 + (long) w2;
        long b2 = (long) y2 + (long) h2;
        return (long) x1 < r2 && r1 > (long) x2 && (long) y1 < b2 && b1 > (long) y2;
    }

    // Find the closest valid Alice target by X distance.
    private static int findAliceDollTargetByX(EnemySystem enemies, int xFixed, boolean barrierActive) {
        if (enemies == null || barrierActive) {
            return -1;
        }
        int best = -1;
        int bestAbsDx = Fixed.fromInt(240);
        for (int i = 0; i < EnemySystem.MAX; i++) {
            if (!enemies.isTargetable(i)) {
                continue;
            }

            int l = enemies.getAabbLeftFixed(i);
            int r = enemies.getAabbRightFixed(i);
            int t = enemies.getAabbTopFixed(i);
            int b = enemies.getAabbBottomFixed(i);
            if (!(l < ALICE_SCAN_X_MAX && t < ALICE_SCAN_Y_MAX && r > ALICE_SCAN_X_MIN && b > ALICE_SCAN_Y_MIN)) {
                continue;
            }

            int ex = enemies.getXFixed(i);
            int dx = xFixed - ex;
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

    // Fixed-point math helpers.
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

    // Animation frame table.
    private static final short[][] ANIMELIST = new short[][] {
            { 1, 0, 0, 1, 24, 0, 1, 48, 0, 1, 24, 0, (short) 254 },
            { 3, 0, 24, (short) 254 },
            { 3, 24, 24, (short) 254 },
            { 3, 48, 24, (short) 254 },
            { 3, 0, 48, (short) 254 },
            { 3, 24, 48, (short) 254 },
            { 3, 48, 48, (short) 254 },
            { 1, 0, 72, 1, 24, 72, (short) 255, 6 },
            { 3, 48, 72, (short) 254 },
    };

    // Image index lookup by character.
    private static int imgIndexForChara(int chara) {
        if (chara == 0) {
            return 8;
        }
        if (chara == 1) {
            return 9;
        }
        return 10;
    }

    // Pick a sprite entry matching the requested image constraints.
    private static int findSpriteId(CcTable cc, int imgIndex, int minSize, int maxSize, int minSrcX, int maxSrcX) {
        if (cc == null) {
            return -1;
        }

        int bestId = -1;
        int bestY = Integer.MAX_VALUE;
        int bestX = Integer.MAX_VALUE;

        for (int i = 0; i < cc.size(); i++) {
            if (!cc.hasSpriteMeta(i)) {
                continue;
            }
            int idx = cc.getImgIndex(i);
            if (idx != imgIndex) {
                continue;
            }
            int w = cc.getW(i);
            int h = cc.getH(i);
            if (w < minSize || h < minSize || w > maxSize || h > maxSize) {
                continue;
            }
            int srcX = cc.getSrcX(i);
            int srcY = cc.getSrcY(i);
            if (srcX < minSrcX || srcX > maxSrcX) {
                continue;
            }
            if (srcY < bestY || (srcY == bestY && srcX < bestX)) {
                bestY = srcY;
                bestX = srcX;
                bestId = i;
            }
        }
        return bestId;
    }
}
