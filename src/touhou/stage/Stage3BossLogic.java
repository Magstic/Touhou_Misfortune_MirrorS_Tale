package touhou.stage;

import touhou.EnemyBulletSystem;
import touhou.Trig;

final class Stage3BossLogic extends AbstractBossStageLogic {
    void tickBossImpl(int enemyIdx) {
        pb(enemyIdx);
    }

    void tickMidbossImpl(int enemyIdx) {
        mb(enemyIdx);
    }

    // Boss
    void pb(int enemyIdx) {
        int[][] enemylist = host.getEnemyList();
        int[] bossx = host.getBossX();
        int[] bossy = host.getBossY();
        int bossf = host.getBossF();

        int mt = enemylist[enemyIdx][11];
        int idx = 0;
        if (mt == 102) {
            idx = 1;
        } else if (mt == 103) {
            idx = 2;
        }

        int gc = host.getMGCnt();
        switch (bossf) {
            case 1:
                bossx[idx] = 97;
                bossy[idx] = 56;
                bspellcnt = 255;
                batkf = false;
                host.setResetFlag(4);
                return;

            case 2:
                if (gc == 0) {
                    host.setBbaria(1);
                }
                if (host.getGameMode() != 3) {
                    bossx[idx] = 97;
                    bossy[idx] = 44;
                }
                bwave[idx] = 120 * idx;
                bspellcnt = 255;
                enemylist[enemyIdx][14] = 393216;
                enemylist[enemyIdx][15] = 393216;
                batkf = false;
                if (gc <= 10) {
                    return;
                }

                if (host.getGameMode() == 3) {
                    bspellcnt = 120;
                    int step = host.getSpellPracticeBossStep();
                    wb(step);
                    switch (step) {
                        case 10:
                            j = 1200;
                            break;
                        case 13:
                            j = 2000;
                            break;
                        case 16:
                            j = 1800;
                            break;
                        case 37:
                            j = 5100;
                            break;
                    }
                    yb(enemyIdx, 5, 3);
                    return;
                }

                switch (host.getBstock()) {
                    case 2:
                        j = 1000;
                        yb(enemyIdx, 5, 1);
                        bspellcnt = 34;
                        wb(3);
                        return;
                    case 1:
                        j = 1000;
                        yb(enemyIdx, 5, 1);
                        bspellcnt = 39;
                        wb(4);
                        return;
                    case 0:
                        j = 1800;
                        yb(enemyIdx, 5, 3);
                        bspellcnt = 59;
                        wb(16);
                        host.setBbaria(2);
                        return;
                }
                return;

            case 3:
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    j = 65535;
                    yb(l, n, 2);
                    wb(10);
                    return;
                }
                host.setBbaria(0);
                batkf = true;
                int d0 = gc % 200;
                if ((d0 % 50) == 0) {
                    if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                        bossx[idx] += 30;
                    } else {
                        bossx[idx] -= 30;
                    }
                    if (bossx[idx] < 30) {
                        bossx[idx] = 147;
                    }
                    if (bossx[idx] > 164) {
                        bossx[idx] = 47;
                    }
                    if (bossy[idx] < 60) {
                        bossy[idx] = 60;
                    } else {
                        bossy[idx] = 40;
                    }
                }

                if ((d0 % 100) > 20 && (d0 & 0x7) == 0) {
                    int lvl3 = host.getLevel();
                    int divPos;
                    int divRing;
                    switch (lvl3) {
                        case 0:
                            divPos = 4;
                            divRing = 4;
                            break;
                        case 1:
                            divPos = 6;
                            divRing = 5;
                            break;
                        case 2:
                            divPos = 8;
                            divRing = 8;
                            break;
                        case 3:
                        default:
                            divPos = 8;
                            divRing = 12;
                            break;
                    }

                    int baseDir = d0 << 2;
                    int stepPos = 360 / divPos;
                    int stepRing = 360 / divRing;
                    int halfRing = stepRing >> 1;

                    for (int pos = 0; pos < 360; pos += stepPos) {
                        int dir = baseDir + pos;
                        int se = (int) ((((long) Trig.cos(dir)) * 400L) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * 400L) >> 3);
                        int sx = enemylist[enemyIdx][5] + se;
                        int sy = enemylist[enemyIdx][6] + te;

                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - sy, host.getPlayerXFixed() - sx);
                        boolean even = ((((d0 / 8) % divPos) & 0x1) == 0);
                        int bulletId = even ? 82 : 81;
                        int offset = even ? 0 : halfRing;
                        for (int a = 0; a < 360; a += stepRing) {
                            host.spawnEnemyBullet(bulletId, 0, aim + offset + a, 40, sx, sy);
                        }
                    }
                }
                return;

            case 4:
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    j = 65535;
                    yb(l, n, 2);
                    wb(13);
                    return;
                }
                host.setBbaria(0);
                batkf = true;
                d0 = gc % 110;
                if (d0 == 95) {
                    if ((gc / 100) == 0) {
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[idx] = 147;
                            bossy[idx] = 68;
                        } else {
                            bossx[idx] = 47;
                            bossy[idx] = 68;
                        }
                    } else if (enemylist[enemyIdx][5] > (97 << 16)) {
                        bossx[idx] = 67;
                        bossy[idx] = 38;
                    } else {
                        bossx[idx] = 127;
                        bossy[idx] = 38;
                    }
                }

                int ex4 = enemylist[enemyIdx][5];
                int ey4 = enemylist[enemyIdx][6];

                if (d0 >= 30) {
                    int d3 = d0 - 30;
                    if (d3 == 0) {
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey4, host.getPlayerXFixed() - ex4);
                    }

                    int step;
                    switch (host.getLevel()) {
                        case 0:
                            step = 12;
                            break;
                        case 1:
                            step = 8;
                            break;
                        case 2:
                        case 3:
                        default:
                            step = 4;
                            break;
                    }

                    if (((d3 << 1) * step) < 180) {
                        int dir = enemylist[enemyIdx][10] + 90 - ((d3 << 1) * step);
                        int se = (int) ((((long) Trig.cos(dir)) * 400L) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * 400L) >> 3);
                        int sx = ex4 + se;
                        int sy = ey4 + (te >> 1);

                        host.spawnEnemyBullet(287, 0, dir, 48, sx, sy);
                        if (host.getLevel() == 0) {
                            host.spawnEnemyBullet(287, 0, dir - 15, 48, sx, sy);
                            host.spawnEnemyBullet(287, 0, dir + 15, 48, sx, sy);
                        } else {
                            for (int i = 1; i < (host.getLevel() << 1); i++) {
                                host.spawnEnemyBullet(287, 0, dir - 10 * i, 48, sx, sy);
                                host.spawnEnemyBullet(287, 0, dir + 10 * i, 48, sx, sy);
                            }
                        }
                    }
                }

                if (d0 >= 45) {
                    int d3 = d0 - 45;
                    if (d3 == 0) {
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey4, host.getPlayerXFixed() - ex4);
                    }

                    int step;
                    switch (host.getLevel()) {
                        case 0:
                        case 1:
                            step = 8;
                            break;
                        case 2:
                        case 3:
                        default:
                            step = 4;
                            break;
                    }

                    if (((d3 << 1) * step) < 180) {
                        int dir = enemylist[enemyIdx][10] - 90 + ((d3 << 1) * step);
                        int se = (int) ((((long) Trig.cos(dir)) * 400L) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * 400L) >> 3);
                        int sx = ex4 + se;
                        int sy = ey4 + (te >> 1);

                        host.spawnEnemyBullet(239, 0, dir, 48, sx, sy);
                        if (host.getLevel() == 0) {
                            host.spawnEnemyBullet(239, 0, dir - 15, 48, sx, sy);
                            host.spawnEnemyBullet(239, 0, dir + 15, 48, sx, sy);
                        } else {
                            for (int i = 1; i < (host.getLevel() << 1); i++) {
                                host.spawnEnemyBullet(239, 0, dir - 10 * i, 48, sx, sy);
                                host.spawnEnemyBullet(239, 0, dir + 10 * i, 48, sx, sy);
                            }
                        }
                    }
                }

                if (d0 == 91) {
                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 10;
                            break;
                        case 1:
                        case 2:
                            div = 15;
                            break;
                        case 3:
                        default:
                            div = 18;
                            break;
                    }

                    enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey4, host.getPlayerXFixed() - ex4);
                    int step = 360 / div;
                    for (int a = 0; a < 360; a += step) {
                        int ang = enemylist[enemyIdx][10] + a;
                        host.spawnEnemyBullet(86, 9, ang, 72, ex4, ey4);
                        if (host.getLevel() >= 2) {
                            host.spawnEnemyBullet(86, 9, ang, 88, ex4, ey4);
                            host.spawnEnemyBullet(86, 9, ang, 104, ex4, ey4);
                        }
                        if (host.getLevel() >= 3) {
                            host.spawnEnemyBullet(86, 9, ang, 56, ex4, ey4);
                            host.spawnEnemyBullet(86, 9, ang, 120, ex4, ey4);
                        }
                    }
                }
                return;

            case 10:
                bossx[idx] = 97;
                bossy[idx] = 68;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (gc > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 34 + host.getLevel();
                    wb(11);
                }
                return;

            case 11:
                bspellcnt = 39;
                switch (gc) {
                    case 1:
                    case 25:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[enemyIdx][3] = 2;
                        break;
                }
                if (gc > 30) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(12);
                    j = 1200;
                    yb(enemyIdx, 5, 2);
                }
                return;

            case 12:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }
                enemylist[enemyIdx][3] = 0;
                d0 = gc % 300;
                switch (d0) {
                    case 10:
                    case 160: {
                        int div;
                        switch (host.getLevel()) {
                            case 0:
                                div = 8;
                                break;
                            case 1:
                                div = 12;
                                break;
                            case 2:
                                div = 18;
                                break;
                            case 3:
                            default:
                                div = 24;
                                break;
                        }
                        int ex = enemylist[enemyIdx][5];
                        int ey = enemylist[enemyIdx][6];
                        int step = 360 / div;
                        for (int a = 0; a < 360; a += step) {
                            host.spawnEnemyBullet(86, 0, a, 8, ex, ey);
                            host.spawnEnemyBullet(86, 0, a, 16, ex, ey);
                            host.spawnEnemyBullet(86, 0, a, 24, ex, ey);
                            host.spawnEnemyBullet(86, 0, a, 32, ex, ey);
                            host.spawnEnemyBullet(86, 0, a, 40, ex, ey);
                            host.spawnEnemyBullet(86, 0, a, 48, ex, ey);
                        }
                        break;
                    }
                    case 50:
                        bossx[idx] = -30;
                        bossy[idx] = 90;
                        enemylist[enemyIdx][14] = 65536;
                        enemylist[enemyIdx][15] = 262144;
                        break;
                    case 200:
                        bossx[idx] = 224;
                        bossy[idx] = 90;
                        enemylist[enemyIdx][14] = 65536;
                        enemylist[enemyIdx][15] = 262144;
                        break;
                    case 51:
                    case 52:
                    case 53:
                    case 54:
                    case 55:
                    case 56:
                    case 57:
                    case 58:
                    case 59:
                    case 60:
                    case 201:
                    case 202:
                    case 203:
                    case 204:
                    case 205:
                    case 206:
                    case 207:
                    case 208:
                    case 209:
                    case 210:
                        enemylist[enemyIdx][14] += 65536;
                        break;
                    case 70:
                    case 240:
                        bossx[idx] = 224;
                        bossy[idx] = 90;
                        enemylist[enemyIdx][6] = 30 << 16;
                        enemylist[enemyIdx][14] += 131072;
                        break;
                    case 90:
                    case 220:
                        bossx[idx] = -30;
                        bossy[idx] = 90;
                        enemylist[enemyIdx][6] = 30 << 16;
                        enemylist[enemyIdx][14] += 131072;
                        break;
                    case 110:
                        bossx[idx] = 164;
                        bossy[idx] = 90;
                        enemylist[enemyIdx][6] = 30 << 16;
                        enemylist[enemyIdx][14] = 1048576;
                        break;
                    case 260:
                        bossx[idx] = 30;
                        bossy[idx] = 90;
                        enemylist[enemyIdx][6] = 30 << 16;
                        enemylist[enemyIdx][14] = 1048576;
                        break;
                    case 111:
                    case 112:
                    case 113:
                    case 114:
                    case 115:
                    case 116:
                    case 117:
                    case 118:
                    case 119:
                    case 120:
                    case 261:
                    case 262:
                    case 263:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    case 268:
                    case 269:
                    case 270:
                        enemylist[enemyIdx][14] -= 65536;
                        break;
                }

                if ((d0 <= 50 || d0 >= 110) && (d0 <= 200 || d0 >= 260)) {
                    return;
                }

                int ex12 = enemylist[enemyIdx][5];
                int ey12 = enemylist[enemyIdx][6];

                if ((d0 % 3) == 0) {
                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 8;
                            break;
                        case 1:
                            div = 10;
                            break;
                        case 2:
                            div = 15;
                            break;
                        case 3:
                        default:
                            div = 18;
                            break;
                    }
                    int step = 360 / div;
                    for (int a = 0; a < 360; a += step) {
                        host.spawnEnemyBullet(82, 2, (d0 << 2) + a, 80, ex12, ey12);
                    }
                }

                if (host.getLevel() == 3 && (d0 & 0x7) == 0) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey12, host.getPlayerXFixed() - ex12);
                    enemylist[enemyIdx][10] = aim;
                    for (int sp = 40; sp < 72; sp += 4) {
                        host.spawnEnemyBullet(239, 0, aim, sp, ex12, ey12);
                    }
                }
                return;

            case 13:
                bossx[idx] = 97;
                bossy[idx] = 68;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (gc > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 38 + host.getLevel();
                    wb(14);
                }
                return;

            case 14:
                bspellcnt = 39;
                switch (gc) {
                    case 1:
                    case 25:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[enemyIdx][3] = 2;
                        break;
                }
                if (gc > 30) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(15);
                    j = 2000;
                    yb(enemyIdx, 5, 2);
                }
                return;

            case 15:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }
                if (gc == 15) {
                    enemylist[enemyIdx][3] = 3;
                }
                if (gc == 20) {
                    enemylist[enemyIdx][3] = 4;
                }

                d0 = gc % 360;
                if (gc <= 25) {
                    return;
                }

                if ((d0 % 14) == 0) {
                    int div;
                    int groups;
                    switch (host.getLevel()) {
                        case 0:
                            div = 8;
                            groups = 0;
                            break;
                        case 1:
                            div = 18;
                            groups = 0;
                            break;
                        case 2:
                            div = 8;
                            groups = 2;
                            break;
                        case 3:
                        default:
                            div = 6;
                            groups = 3;
                            break;
                    }

                    int baseAng = d0 / 7;
                    int step = 360 / div;
                    int ex15 = enemylist[enemyIdx][5];
                    int ey15 = enemylist[enemyIdx][6];

                    if (host.getLevel() <= 1) {
                        for (int a = 0; a < 360; a += step) {
                            host.spawnEnemyBullet(255, 23, baseAng + a, 24, ex15, ey15);
                        }
                    } else {
                        for (int g = 0; g < groups; g++) {
                            int dir = d0 + (360 * g) / groups;
                            int se = (int) ((((long) Trig.cos(dir)) * 480L) >> 3);
                            int te = (int) ((((long) Trig.sin(dir)) * 480L) >> 3);
                            int sx = ex15 + se;
                            int sy = ey15 + te;
                            for (int a = 0; a < 360; a += step) {
                                host.spawnEnemyBullet(255, 23, baseAng + a, 24, sx, sy);
                            }
                        }
                    }
                }

                if ((d0 % 80) == 0) {
                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 8;
                            break;
                        case 1:
                            div = 10;
                            break;
                        case 2:
                            div = 12;
                            break;
                        case 3:
                        default:
                            div = 15;
                            break;
                    }

                    int ex15 = enemylist[enemyIdx][5];
                    int ey15 = enemylist[enemyIdx][6];
                    enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey15, host.getPlayerXFixed() - ex15);

                    int step = 360 / div;
                    for (int a = 0; a < 360; a += step) {
                        host.spawnEnemyBullet(93, 0, enemylist[enemyIdx][10] + a, 24, ex15, ey15);
                    }
                }
                return;

            case 16:
                bossx[idx] = 97;
                bossy[idx] = 121;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                if (gc > 30) {
                    host.setQ(true);
                    enemylist[enemyIdx][14] = 65536;
                    enemylist[enemyIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 42 + host.getLevel();
                    wb(17);
                }
                return;

            case 17:
                bspellcnt = 59;
                switch (gc) {
                    case 1:
                    case 25:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[enemyIdx][3] = 2;
                        break;
                }
                if (gc > 30) {
                    enemylist[enemyIdx][14] = 0;
                    enemylist[enemyIdx][15] = 0;
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(18);
                }
                return;

            case 18:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }
                if (gc == 5) {
                    enemylist[enemyIdx][3] = 3;
                }
                if (gc == 15) {
                    enemylist[enemyIdx][3] = 4;
                }
                d0 = gc % 500;
                final int baseX = 97 << 16;
                final int baseY = 121 << 16;

                if (d0 == 20) {
                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 8;
                            break;
                        case 1:
                            div = 10;
                            break;
                        case 2:
                            div = 12;
                            break;
                        case 3:
                        default:
                            div = 18;
                            break;
                    }

                    int ex = enemylist[enemyIdx][5];
                    int ey = enemylist[enemyIdx][6];
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey, host.getPlayerXFixed() - ex);

                    int step = 360 / div;
                    int half = step >> 1;
                    for (int a = 0; a < 360; a += step) {
                        host.spawnEnemyBullet(86, 25, aim + a, 24, ex, ey);
                        host.spawnEnemyBullet(86, 25, aim + a + half, 40, ex, ey);
                        host.spawnEnemyBullet(86, 25, aim + a, 56, ex, ey);
                        host.spawnEnemyBullet(86, 25, aim + a + half, 72, ex, ey);
                        if (a >= step && a <= 360 - step) {
                            host.spawnEnemyBullet(86, 25, aim + a, 88, ex, ey);
                            host.spawnEnemyBullet(86, 25, aim + a + half, 104, ex, ey);
                            host.spawnEnemyBullet(86, 25, aim + a, 120, ex, ey);
                            host.spawnEnemyBullet(86, 25, aim + a + half, 136, ex, ey);
                        }
                    }
                }

                if (d0 >= 30 && d0 < 50) {
                    enemylist[enemyIdx][3] = 5;
                    int d3 = (d0 - 30) << 2;
                    int sp = d3 << 3;
                    int dir = 360;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    enemylist[enemyIdx][5] = baseX + se;
                    enemylist[enemyIdx][6] = baseY + te;
                    bossx[idx] = enemylist[enemyIdx][5] >> 16;
                    bossy[idx] = enemylist[enemyIdx][6] >> 16;
                }
                if (d0 >= 50 && d0 < 130) {
                    int d3 = 80 - (d0 - 50);
                    int d4 = 90 + ((d0 - 50) << 2);
                    int sp = d3 << 3;
                    int dir = d4 + 270;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    enemylist[enemyIdx][5] = baseX + se;
                    enemylist[enemyIdx][6] = baseY + te;
                    bossx[idx] = enemylist[enemyIdx][5] >> 16;
                    bossy[idx] = enemylist[enemyIdx][6] >> 16;

                    if (d0 == 50) {
                        host.setEnemyBulletsDeltaForIdAndMoveType(86, 10, 16);
                    }

                    if ((d0 % 3) == 0) {
                        int ex = enemylist[enemyIdx][5];
                        int ey = enemylist[enemyIdx][6];
                        int ang = d4 + 180;
                        host.spawnEnemyBullet(239, 0, ang, 32, ex, ey);
                        if (host.getLevel() >= 1) {
                            host.spawnEnemyBullet(239, 0, ang + 20, 32, ex, ey);
                            host.spawnEnemyBullet(239, 0, ang - 20, 32, ex, ey);
                            if (host.getLevel() >= 2) {
                                host.spawnEnemyBullet(239, 0, ang + 10, 32, ex, ey);
                                host.spawnEnemyBullet(239, 0, ang - 10, 32, ex, ey);
                            }
                        }
                    }
                }
                if (d0 >= 130 && d0 < 210) {
                    int d3 = d0 - 130;
                    int d4 = 90 + ((d0 - 50) << 2);
                    int sp = d3 << 3;
                    int dir = d4 + 270;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    enemylist[enemyIdx][5] = baseX + se;
                    enemylist[enemyIdx][6] = baseY + te;
                    bossx[idx] = enemylist[enemyIdx][5] >> 16;
                    bossy[idx] = enemylist[enemyIdx][6] >> 16;

                    if ((d0 % 20) == 0) {
                        int div;
                        switch (host.getLevel()) {
                            case 0:
                                div = 8;
                                break;
                            case 1:
                                div = 10;
                                break;
                            case 2:
                                div = 12;
                                break;
                            case 3:
                            default:
                                div = 18;
                                break;
                        }
                        int ex = enemylist[enemyIdx][5];
                        int ey = enemylist[enemyIdx][6];
                        int step = 360 / div;
                        for (int a = 0; a < 360; a += step) {
                            host.spawnEnemyBullet(84, 11, a, 16, ex, ey);
                        }
                    }

                    if ((d0 % 3) == 0) {
                        int ex = enemylist[enemyIdx][5];
                        int ey = enemylist[enemyIdx][6];
                        int ang = d4 + 180;
                        host.spawnEnemyBullet(239, 0, ang, 32, ex, ey);
                        if (host.getLevel() >= 1) {
                            host.spawnEnemyBullet(239, 0, ang + 20, 32, ex, ey);
                            host.spawnEnemyBullet(239, 0, ang - 20, 32, ex, ey);
                            if (host.getLevel() >= 2) {
                                host.spawnEnemyBullet(239, 0, ang + 10, 32, ex, ey);
                                host.spawnEnemyBullet(239, 0, ang - 10, 32, ex, ey);
                            }
                        }
                    }
                }
                if (d0 >= 210 && d0 < 230) {
                    int d3 = 80 - ((d0 - 210) << 2);
                    int d4 = 0;
                    int sp = d3 << 3;
                    int dir = d4 + 270;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    enemylist[enemyIdx][5] = baseX + se;
                    enemylist[enemyIdx][6] = baseY + te;
                    bossx[idx] = enemylist[enemyIdx][5] >> 16;
                    bossy[idx] = enemylist[enemyIdx][6] >> 16;
                }
                if (d0 == 230) {
                    enemylist[enemyIdx][3] = 3;
                }
                if (d0 == 233) {
                    enemylist[enemyIdx][3] = 4;

                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 8;
                            break;
                        case 1:
                            div = 10;
                            break;
                        case 2:
                            div = 12;
                            break;
                        case 3:
                        default:
                            div = 18;
                            break;
                    }
                    int ex = enemylist[enemyIdx][5];
                    int ey = enemylist[enemyIdx][6];
                    int step = 360 / div;
                    int half = step >> 1;
                    for (int a = 0; a < 360; a += step) {
                        host.spawnEnemyBullet(239, 2, a, 96, ex, ey);
                        host.spawnEnemyBullet(239, 2, a + half, 80, ex, ey);
                    }
                }
                if (d0 == 240) {
                    enemylist[enemyIdx][3] = 0;
                }

                if (d0 == 270) {
                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 8;
                            break;
                        case 1:
                            div = 10;
                            break;
                        case 2:
                            div = 12;
                            break;
                        case 3:
                        default:
                            div = 18;
                            break;
                    }

                    int ex = enemylist[enemyIdx][5];
                    int ey = enemylist[enemyIdx][6];
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey, host.getPlayerXFixed() - ex);

                    int step = 360 / div;
                    int half = step >> 1;
                    for (int a = 0; a < 360; a += step) {
                        host.spawnEnemyBullet(86, 33, aim + a, 24, ex, ey);
                        host.spawnEnemyBullet(86, 33, aim + a + half, 40, ex, ey);
                        host.spawnEnemyBullet(86, 33, aim + a, 56, ex, ey);
                        host.spawnEnemyBullet(86, 33, aim + a + half, 72, ex, ey);
                        if (a >= step && a <= 360 - step) {
                            host.spawnEnemyBullet(86, 33, aim + a, 88, ex, ey);
                            host.spawnEnemyBullet(86, 33, aim + a + half, 104, ex, ey);
                            host.spawnEnemyBullet(86, 33, aim + a, 120, ex, ey);
                            host.spawnEnemyBullet(86, 33, aim + a + half, 136, ex, ey);
                        }
                    }
                }

                if (d0 >= 280 && d0 < 300) {
                    enemylist[enemyIdx][3] = 5;
                    int d3 = (d0 - 250 - 30) << 2;
                    int sp = d3 << 3;
                    int dir = 540;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    enemylist[enemyIdx][5] = baseX + se;
                    enemylist[enemyIdx][6] = baseY + te;
                    bossx[idx] = enemylist[enemyIdx][5] >> 16;
                    bossy[idx] = enemylist[enemyIdx][6] >> 16;
                }
                if (d0 >= 300 && d0 < 380) {
                    int d3 = 80 - (d0 - 250 - 50);
                    int d4 = 270 - ((d0 - 250 - 50) << 2);
                    int sp = d3 << 3;
                    int dir = d4 + 270;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    enemylist[enemyIdx][5] = baseX + se;
                    enemylist[enemyIdx][6] = baseY + te;
                    bossx[idx] = enemylist[enemyIdx][5] >> 16;
                    bossy[idx] = enemylist[enemyIdx][6] >> 16;

                    if (d0 == 300) {
                        host.setEnemyBulletsDeltaForIdAndMoveType(86, 11, 16);
                    }

                    if ((d0 % 3) == 0) {
                        int ex = enemylist[enemyIdx][5];
                        int ey = enemylist[enemyIdx][6];
                        host.spawnEnemyBullet(239, 0, d4, 32, ex, ey);
                        if (host.getLevel() >= 1) {
                            host.spawnEnemyBullet(239, 0, d4 + 20, 32, ex, ey);
                            host.spawnEnemyBullet(239, 0, d4 - 20, 32, ex, ey);
                            if (host.getLevel() >= 2) {
                                host.spawnEnemyBullet(239, 0, d4 + 10, 32, ex, ey);
                                host.spawnEnemyBullet(239, 0, d4 - 10, 32, ex, ey);
                            }
                        }
                    }
                }
                if (d0 >= 380 && d0 < 460) {
                    int d3 = d0 - 250 - 130;
                    int d4 = 270 - ((d0 - 250 - 50) << 2);
                    int sp = d3 << 3;
                    int dir = d4 + 270;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    enemylist[enemyIdx][5] = baseX + se;
                    enemylist[enemyIdx][6] = baseY + te;
                    bossx[idx] = enemylist[enemyIdx][5] >> 16;
                    bossy[idx] = enemylist[enemyIdx][6] >> 16;

                    if ((d0 % 20) == 0) {
                        int div;
                        switch (host.getLevel()) {
                            case 0:
                                div = 8;
                                break;
                            case 1:
                                div = 10;
                                break;
                            case 2:
                                div = 12;
                                break;
                            case 3:
                            default:
                                div = 18;
                                break;
                        }
                        int ex = enemylist[enemyIdx][5];
                        int ey = enemylist[enemyIdx][6];
                        int step = 360 / div;
                        for (int a = 0; a < 360; a += step) {
                            host.spawnEnemyBullet(84, 11, a, 16, ex, ey);
                        }
                    }

                    if ((d0 % 3) == 0) {
                        int ex = enemylist[enemyIdx][5];
                        int ey = enemylist[enemyIdx][6];
                        host.spawnEnemyBullet(239, 0, d4, 32, ex, ey);
                        if (host.getLevel() >= 1) {
                            host.spawnEnemyBullet(239, 0, d4 + 20, 32, ex, ey);
                            host.spawnEnemyBullet(239, 0, d4 - 20, 32, ex, ey);
                            if (host.getLevel() >= 2) {
                                host.spawnEnemyBullet(239, 0, d4 + 10, 32, ex, ey);
                                host.spawnEnemyBullet(239, 0, d4 - 10, 32, ex, ey);
                            }
                        }
                    }
                }
                if (d0 >= 460 && d0 < 480) {
                    int d3 = 80 - ((d0 - 250 - 210) << 2);
                    int d4 = 0;
                    int sp = d3 << 3;
                    int dir = d4 + 270;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    enemylist[enemyIdx][5] = baseX + se;
                    enemylist[enemyIdx][6] = baseY + te;
                    bossx[idx] = enemylist[enemyIdx][5] >> 16;
                    bossy[idx] = enemylist[enemyIdx][6] >> 16;
                }
                if (d0 == 480) {
                    enemylist[enemyIdx][3] = 3;
                }
                if (d0 == 483) {
                    enemylist[enemyIdx][3] = 4;

                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 8;
                            break;
                        case 1:
                            div = 10;
                            break;
                        case 2:
                            div = 12;
                            break;
                        case 3:
                        default:
                            div = 18;
                            break;
                    }
                    int ex = enemylist[enemyIdx][5];
                    int ey = enemylist[enemyIdx][6];
                    int step = 360 / div;
                    int half = step >> 1;
                    for (int a = 0; a < 360; a += step) {
                        host.spawnEnemyBullet(239, 2, a, 96, ex, ey);
                        host.spawnEnemyBullet(239, 2, a + half, 80, ex, ey);
                    }
                }
                if (d0 == 490) {
                    enemylist[enemyIdx][3] = 0;
                    return;
                }
                return;

            case 37:
                bossx[idx] = 97;
                bossy[idx] = 68;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (gc > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 111;
                    wb(38);
                }
                return;

            case 38:
                bspellcnt = 80;
                switch (gc) {
                    case 1:
                    case 25:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[enemyIdx][3] = 2;
                        break;
                }
                if (gc > 30) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(39);
                }
                return;

            case 39:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }

                int[] bossWrk39 = host.getBossWrk();
                int ex39 = enemylist[enemyIdx][5];
                int ey39 = enemylist[enemyIdx][6];

                if (gc == 5) {
                    for (int i = 0; i < 4; ++i) {
                        bossWrk39[8 + i] = host.stageFb(20 + (i & 0x1), 0, 65535, 0, 50, 0, 0, ex39, ey39);
                    }
                }

                if (gc > 23) {
                    int d0_39 = (gc - 24) % 600;

                    if ((d0_39 % 120) == 115) {
                        int dx = host.getPlayerXFixed() - ex39;
                        int dy = host.getPlayerYFixed() - ey39;
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(dy, dx);
                        for (int n27 = 0; n27 < 36; ++n27) {
                            int d3 = enemylist[enemyIdx][10] + n27 * 10;
                            host.spawnEnemyBullet(239, 0, d3, 48, ex39, ey39);
                            if ((n27 & 0x1) == 0) {
                                host.spawnEnemyBullet(239, 26, d3, 40, ex39, ey39);
                            } else {
                                host.spawnEnemyBullet(239, 26, d3, 56, ex39, ey39);
                            }
                            if (d0_39 > 300) {
                                host.spawnEnemyBullet(239, 26, d3, 16, ex39, ey39);
                            }
                            if (d0_39 > 500) {
                                host.spawnEnemyBullet(239, 26, d3 + 5, 8, ex39, ey39);
                            }
                        }
                    }

                    if (d0_39 < 120) {
                        int da_39 = d0_39;
                        if (da_39 <= 90) {
                            if (da_39 == 0) {
                                host.setEnemyBulletLock(0, 0);
                                host.setEnemyBulletLock(1, 0);
                                host.setEnemyBulletLock(2, 0);
                            }

                            int d9_39 = 576;
                            if (da_39 > 30) {
                                d9_39 -= (da_39 - 30) << 3;
                            }
                            if (da_39 > 60) {
                                d9_39 += (da_39 - 60) << 1;
                            }

                            for (int i = 0; i < 4; ++i) {
                                int si = bossWrk39[8 + i];
                                if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                    continue;
                                }
                                int d4_39 = 360 + i * 90 - da_39 * 3;
                                int dir_39 = d4_39 + 270;
                                int se_39 = (int) ((((long) Trig.cos(dir_39)) * (long) d9_39) >> 3);
                                int te_39 = (int) ((((long) Trig.sin(dir_39)) * (long) d9_39) >> 3);
                                enemylist[si][13] = d4_39;
                                enemylist[si][5] = ex39 + se_39;
                                enemylist[si][6] = ey39 + te_39;
                            }

                            if (da_39 < 30) {
                                if ((d0_39 & 0x1) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13] + 180;
                                        host.spawnEnemyBullet(239, 86, a + 10, 16, sx, sy);
                                    }
                                }
                            } else if (da_39 < 60) {
                                for (int i = 0; i < 4; ++i) {
                                    int si = bossWrk39[8 + i];
                                    if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                        continue;
                                    }
                                    int sx = enemylist[si][5];
                                    int sy = enemylist[si][6];
                                    int a = enemylist[si][13] + 90;
                                    host.spawnEnemyBullet(84, 87, a, 24, sx, sy);
                                }
                            } else {
                                if ((d0_39 & 0x3) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13] + 90;
                                        host.spawnEnemyBullet(603, 88, a, 32, sx, sy);
                                        host.spawnEnemyBullet(603, 88, a + 180, 32, sx, sy);
                                    }
                                }
                            }
                        } else {
                            if (da_39 == 100) {
                                host.setEnemyBulletLock(0, 1);
                                return;
                            }
                            if (da_39 == 105) {
                                host.setEnemyBulletLock(2, 1);
                                return;
                            }
                            if (da_39 == 110) {
                                host.setEnemyBulletLock(1, 1);
                                return;
                            }
                        }
                    } else if (d0_39 < 240) {
                        int da_39 = d0_39 - 120;
                        if (da_39 <= 90) {
                            if (da_39 == 0) {
                                host.setEnemyBulletLock(0, 0);
                                host.setEnemyBulletLock(1, 0);
                                host.setEnemyBulletLock(2, 0);
                            }

                            int d9_39 = 156;
                            d9_39 += da_39 * 5;
                            if (da_39 > 30) {
                                d9_39 -= (da_39 - 30) << 3;
                            }

                            for (int i = 0; i < 4; ++i) {
                                int si = bossWrk39[8 + i];
                                if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                    continue;
                                }
                                int d4_39 = 360 + i * 90;
                                if (da_39 >= 30) {
                                    d4_39 += da_39 * 3;
                                }
                                int dir_39 = d4_39 + 270;
                                int se_39 = (int) ((((long) Trig.cos(dir_39)) * (long) d9_39) >> 3);
                                int te_39 = (int) ((((long) Trig.sin(dir_39)) * (long) d9_39) >> 3);
                                enemylist[si][13] = d4_39;
                                enemylist[si][5] = ex39 + se_39;
                                enemylist[si][6] = ey39 + te_39;
                            }

                            if (da_39 < 30) {
                                if ((d0_39 & 0x3) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13];
                                        for (int n34 = 0; n34 < 360; n34 += 45) {
                                            a += n34;
                                            host.spawnEnemyBullet(585, 13, a, 16, sx, sy);
                                        }
                                    }
                                }
                            } else {
                                if ((d0_39 & 0x3) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13];
                                        int mt_39;
                                        if ((d0_39 & 0x7) == 0) {
                                            a += 180;
                                            mt_39 = 87;
                                        } else {
                                            mt_39 = 86;
                                        }
                                        host.spawnEnemyBullet(84, mt_39, a, 24, sx, sy);
                                    }
                                }
                                if ((d0_39 & 0xF) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13];
                                        for (int n37 = 0; n37 < 360; n37 += 45) {
                                            a += n37;
                                            host.spawnEnemyBullet(255, 88, a, 16, sx, sy);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (da_39 == 100) {
                                host.setEnemyBulletLock(0, 1);
                                return;
                            }
                            if (da_39 == 105) {
                                host.setEnemyBulletLock(2, 1);
                                return;
                            }
                            if (da_39 == 110) {
                                host.setEnemyBulletLock(1, 1);
                                return;
                            }
                        }
                    } else if (d0_39 < 360) {
                        int da_39 = d0_39 - 240;
                        if (da_39 <= 90) {
                            if (da_39 == 0) {
                                host.setEnemyBulletLock(0, 0);
                                host.setEnemyBulletLock(1, 0);
                                host.setEnemyBulletLock(2, 0);
                            }

                            int d9_39 = 126;
                            if (da_39 <= 45) {
                                d9_39 += da_39 * 10;
                            } else {
                                d9_39 += 450;
                            }

                            for (int i = 0; i < 4; ++i) {
                                int si = bossWrk39[8 + i];
                                if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                    continue;
                                }
                                int d4_39 = 360 + i * 90 - da_39 * 6;
                                int dir_39 = d4_39 + 270;
                                int se_39 = (int) ((((long) Trig.cos(dir_39)) * (long) d9_39) >> 3);
                                int te_39 = (int) ((((long) Trig.sin(dir_39)) * (long) d9_39) >> 3);
                                enemylist[si][13] = d4_39;
                                enemylist[si][5] = ex39 + se_39;
                                enemylist[si][6] = ey39 + te_39;
                            }

                            if (da_39 < 45) {
                                if ((d0_39 & 0x3) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13] + 180;
                                        a -= da_39 << 2;
                                        host.spawnEnemyBullet(335, 86, a, 16, sx, sy);
                                    }
                                }
                            } else {
                                if ((d0_39 & 0x7) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13];
                                        for (int n41 = 0; n41 < 360; n41 += 120) {
                                            a += n41;
                                            host.spawnEnemyBullet(84, 87, a, 24, sx, sy);
                                        }
                                    }
                                }
                                if ((d0_39 & 0x1) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13] + 180;
                                        host.spawnEnemyBullet(255, 13, a, 30, sx, sy);
                                    }
                                }
                            }
                        } else {
                            if (da_39 == 100) {
                                host.setEnemyBulletLock(0, 1);
                                return;
                            }
                            if (da_39 == 105) {
                                host.setEnemyBulletLock(2, 1);
                                return;
                            }
                            if (da_39 == 110) {
                                host.setEnemyBulletLock(1, 1);
                                return;
                            }
                        }
                    } else if (d0_39 < 480) {
                        int da_39 = d0_39 - 360;
                        if (da_39 <= 90) {
                            if (da_39 == 0) {
                                host.setEnemyBulletLock(0, 0);
                                host.setEnemyBulletLock(1, 0);
                                host.setEnemyBulletLock(2, 0);
                            }

                            int d9_39 = 576;
                            d9_39 -= da_39 * 7;
                            if (da_39 > 45) {
                                d9_39 += (da_39 - 45) << 4;
                            }

                            for (int i = 0; i < 4; ++i) {
                                int si = bossWrk39[8 + i];
                                if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                    continue;
                                }
                                int d4_39 = 360 + i * 90 + da_39 * 12;
                                int dir_39 = d4_39 + 270;
                                int se_39 = (int) ((((long) Trig.cos(dir_39)) * (long) d9_39) >> 3);
                                int te_39 = (int) ((((long) Trig.sin(dir_39)) * (long) d9_39) >> 3);
                                enemylist[si][13] = d4_39;
                                enemylist[si][5] = ex39 + se_39;
                                enemylist[si][6] = ey39 + te_39;
                            }

                            if ((d0_39 & 0x1) == 0) {
                                if (da_39 < 45) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13];
                                        host.spawnEnemyBullet(239, 86, a, 16, sx, sy);
                                        host.spawnEnemyBullet(239, 87, a + 180, 16, sx, sy);
                                    }
                                } else {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13] + 90;
                                        host.spawnEnemyBullet(239, 86, a, 16, sx, sy);
                                        host.spawnEnemyBullet(239, 87, a + 180, 16, sx, sy);
                                    }
                                }
                            }
                        } else {
                            if (da_39 == 100) {
                                host.setEnemyBulletLock(0, 1);
                                return;
                            }
                            if (da_39 == 105) {
                                host.setEnemyBulletLock(2, 1);
                                return;
                            }
                            if (da_39 == 110) {
                                host.setEnemyBulletLock(1, 1);
                                return;
                            }
                        }
                    } else {
                        int da_39 = d0_39 - 480;
                        if (da_39 <= 90) {
                            if (da_39 == 0) {
                                host.setEnemyBulletLock(0, 0);
                                host.setEnemyBulletLock(1, 0);
                                host.setEnemyBulletLock(2, 0);
                            }

                            int d9_39 = 666;
                            d9_39 -= da_39 << 1;
                            if (da_39 > 30) {
                                d9_39 += (da_39 - 30) * 3;
                            }
                            if (da_39 > 60) {
                                d9_39 -= (da_39 - 60) * 3;
                            }

                            for (int i = 0; i < 4; ++i) {
                                int si = bossWrk39[8 + i];
                                if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                    continue;
                                }
                                int d4_39 = 360 + i * 90 - da_39 * 3;
                                int dir_39 = d4_39 + 270;
                                int se_39 = (int) ((((long) Trig.cos(dir_39)) * (long) d9_39) >> 3);
                                int te_39 = (int) ((((long) Trig.sin(dir_39)) * (long) d9_39) >> 3);
                                enemylist[si][13] = d4_39;
                                enemylist[si][5] = ex39 + se_39;
                                enemylist[si][6] = ey39 + te_39;
                            }

                            if (da_39 < 30) {
                                if ((d0_39 & 0x1) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13] + 180;
                                        host.spawnEnemyBullet(81, 86, a + 10, 16, sx, sy);
                                    }
                                }
                            } else if (da_39 < 60) {
                                for (int i = 0; i < 4; ++i) {
                                    int si = bossWrk39[8 + i];
                                    if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                        continue;
                                    }
                                    int sx = enemylist[si][5];
                                    int sy = enemylist[si][6];
                                    int a = enemylist[si][13] + 90;
                                    host.spawnEnemyBullet(639, 87, a, 24, sx, sy);
                                    host.spawnEnemyBullet(639, 39, a, 24, sx, sy);
                                }
                            } else {
                                if ((d0_39 & 0x3) == 0) {
                                    for (int i = 0; i < 4; ++i) {
                                        int si = bossWrk39[8 + i];
                                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                            continue;
                                        }
                                        int sx = enemylist[si][5];
                                        int sy = enemylist[si][6];
                                        int a = enemylist[si][13] + 90;
                                        host.spawnEnemyBullet(255, 88, a, 32, sx, sy);
                                        host.spawnEnemyBullet(255, 88, a + 180, 32, sx, sy);
                                    }
                                }
                            }
                        } else {
                            if (da_39 == 100) {
                                host.setEnemyBulletLock(0, 1);
                                return;
                            }
                            if (da_39 == 105) {
                                host.setEnemyBulletLock(2, 1);
                                return;
                            }
                            if (da_39 == 110) {
                                host.setEnemyBulletLock(1, 1);
                                return;
                            }
                        }
                    }
                } else {
                    if (gc >= 5) {
                        int d1 = (gc - 5) << 2;
                        int sp = d1 << 3;

                        int dir;
                        int se;
                        int te;

                        dir = 270;
                        se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        int si0 = bossWrk39[8];
                        if (si0 >= 0 && si0 < enemylist.length && enemylist[si0][0] != 0) {
                            enemylist[si0][5] = ex39 + se;
                            enemylist[si0][6] = ey39 + te;
                        }

                        dir = 360;
                        se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        int si1 = bossWrk39[9];
                        if (si1 >= 0 && si1 < enemylist.length && enemylist[si1][0] != 0) {
                            enemylist[si1][5] = ex39 + se;
                            enemylist[si1][6] = ey39 + te;
                        }

                        dir = 450;
                        se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        int si2 = bossWrk39[10];
                        if (si2 >= 0 && si2 < enemylist.length && enemylist[si2][0] != 0) {
                            enemylist[si2][5] = ex39 + se;
                            enemylist[si2][6] = ey39 + te;
                        }

                        dir = 540;
                        se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        int si3 = bossWrk39[11];
                        if (si3 >= 0 && si3 < enemylist.length && enemylist[si3][0] != 0) {
                            enemylist[si3][5] = ex39 + se;
                            enemylist[si3][6] = ey39 + te;
                        }
                    }
                }

                return;
        }
    }

    // midboss
    void mb(int enemyIdx) {
        int[][] enemylist = host.getEnemyList();
        int[] bossx = host.getBossX();
        int[] bossy = host.getBossY();

        switch (enemylist[enemyIdx][7]) {
            case 0:
                bossx[0] = 97;
                bossy[0] = 68;
                bspellcnt = 255;
                batkf = false;
                host.setResetFlag(4);
                enemylist[enemyIdx][16] = 0;
                if (host.getMGCnt() > 250) {
                    enemylist[enemyIdx][16] = 1;
                    j = enemylist[enemyIdx][4];
                    yb(enemyIdx, 5, 1);
                    vb(enemyIdx, 1);
                    return;
                }
                return;

            case 1:
                if (host.getMGCnt() == 0) {
                    enemylist[enemyIdx][16] = 0;
                }
                if (host.getGameMode() != 3) {
                    bossx[0] = 97;
                    bossy[0] = 68;
                }
                batkf = false;
                enemylist[enemyIdx][3] = 0;
                if (host.getGameMode() == 3) {
                    bspellcnt = 120;
                    enemylist[enemyIdx][4] = 1200;
                    j = enemylist[enemyIdx][4];
                    yb(enemyIdx, 5, 3);
                    vb(enemyIdx, 8);
                } else {
                    bspellcnt = 29;
                    vb(enemyIdx, 3);
                }
                host.getBossWrk()[1] = 1;
                return;

            case 3:
                enemylist[enemyIdx][16] = 1;
                batkf = true;
                enemylist[enemyIdx][3] = 0;

                int mg3 = host.getMGCnt();
                int lvl3 = host.getLevel();
                int ex3 = enemylist[enemyIdx][5];
                int ey3 = enemylist[enemyIdx][6];
                int d0 = (mg3 + 20) % 300;
                switch (d0) {
                    case 30: {
                        int dx = host.getPlayerXFixed() - ex3;
                        int dy = host.getPlayerYFixed() - ey3;
                        int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);
                        spawnJc0(239, aim, 12 << 3, ex3, ey3, lvl3);
                        break;
                    }
                    case 40: {
                        int dx = host.getPlayerXFixed() - ex3;
                        int dy = host.getPlayerYFixed() - ey3;
                        int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);
                        spawnJc0(239, aim + 25, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim - 25, 12 << 3, ex3, ey3, lvl3);
                        break;
                    }
                    case 50: {
                        int dx = host.getPlayerXFixed() - ex3;
                        int dy = host.getPlayerYFixed() - ey3;
                        int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);
                        spawnJc0(239, aim + 40, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim - 40, 12 << 3, ex3, ey3, lvl3);
                        break;
                    }
                    case 60: {
                        int dx = host.getPlayerXFixed() - ex3;
                        int dy = host.getPlayerYFixed() - ey3;
                        int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);
                        spawnJc0(239, aim + 60, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim + 20, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim - 20, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim - 60, 12 << 3, ex3, ey3, lvl3);
                        break;
                    }
                    case 70: {
                        int dx = host.getPlayerXFixed() - ex3;
                        int dy = host.getPlayerYFixed() - ey3;
                        int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);
                        spawnJc0(239, aim + 80, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim + 40, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim - 40, 12 << 3, ex3, ey3, lvl3);
                        spawnJc0(239, aim - 80, 12 << 3, ex3, ey3, lvl3);
                        break;
                    }

                    case 150:
                    case 250: {
                        int dx = host.getPlayerXFixed() - ex3;
                        int dy = host.getPlayerYFixed() - ey3;
                        int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);
                        spawnJc0(255, aim, 10 << 3, ex3, ey3, lvl3);
                        break;
                    }

                    case 100:
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[0] = 157;
                            bossy[0] = 68;
                        } else {
                            bossx[0] = 37;
                            bossy[0] = 68;
                        }
                        break;
                    case 200:
                        if (enemylist[enemyIdx][5] > 6356992) {
                            bossx[0] = 67;
                            bossy[0] = 28;
                        } else {
                            bossx[0] = 127;
                            bossy[0] = 28;
                        }
                        break;
                    case 290:
                        bossx[0] = 97;
                        bossy[0] = 58;
                        break;
                }

                if ((d0 > 130 && d0 < 180) || (d0 > 230 && d0 < 280)) {
                    int base;
                    if (ex3 > 6356992) {
                        base = d0 << 3;
                    } else {
                        base = 360 - ((d0 << 3) % 360);
                    }

                    int step;
                    switch (lvl3) {
                        case 0:
                            step = 180;
                            break;
                        case 1:
                            step = 120;
                            break;
                        case 2:
                            step = 72;
                            break;
                        case 3:
                        default:
                            step = 60;
                            break;
                    }

                    for (int a = 0; a < 360; a += step) {
                        int dir = (base + a) % 360;
                        int sp = 160;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        host.spawnEnemyBullet(239, 2, dir, 11 << 3, ex3 + se, ey3 + te);
                    }
                }

                if (bspellcnt < 0) {
                    vb(enemyIdx, 2);
                    return;
                }
                return;

            case 8:
                bossx[0] = 97;
                bossy[0] = 58;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                enemylist[enemyIdx][16] = 0;
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 30 + host.getLevel();
                    vb(enemyIdx, 9);
                    return;
                }
                return;

            case 9:
                bspellcnt = 34;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[enemyIdx][3] = 2;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    enemylist[enemyIdx][16] = 1;
                    vb(enemyIdx, 10);
                    enemylist[enemyIdx][4] = 1200;
                    j = enemylist[enemyIdx][4];
                    yb(enemyIdx, 5, 2);
                    return;
                }
                return;

            case 10:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    if (host.getGameMode() == 3) {
                        host.stageJb(enemyIdx);
                        return;
                    }
                    host.setResetFlag(4);
                    vb(enemyIdx, 2);
                    return;
                }

                int mg10 = host.getMGCnt();
                int d1 = mg10 % 100;
                int lvl10 = host.getLevel();
                int ex10 = enemylist[enemyIdx][5];
                int ey10 = enemylist[enemyIdx][6];
                if (d1 == 90) {
                    if (enemylist[enemyIdx][5] > 6356992) {
                        bossx[0] = 67;
                    } else {
                        bossx[0] = 127;
                    }
                }

                if (d1 > 30 && d1 < 80) {
                    int base;
                    if (((mg10 / 100) & 0x1) == 0) {
                        base = d1 << 3;
                    } else {
                        base = 360 - ((d1 << 3) % 360);
                    }

                    int step;
                    switch (lvl10) {
                        case 0:
                            step = 180;
                            break;
                        case 1:
                            step = 120;
                            break;
                        case 2:
                            step = 72;
                            break;
                        case 3:
                        default:
                            step = 45;
                            break;
                    }

                    int sp = (d1 - 20) << 3;
                    for (int a = 0; a < 360; a += step) {
                        int dir = (base + a) % 360;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        host.spawnEnemyBullet(86, 13, dir + 180, 6 << 3, ex10 + se, ey10 + te);
                    }

                    if ((d1 & 0x3) == 0) {
                        int dx = host.getPlayerXFixed() - ex10;
                        int dy = host.getPlayerYFixed() - ey10;
                        int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);

                        int sp2 = (d1 - 30) << 5;
                        int dirA = (aim + 40) % 360;
                        int seA = (int) ((((long) Trig.cos(dirA)) * (long) sp2) >> 3);
                        int teA = (int) ((((long) Trig.sin(dirA)) * (long) sp2) >> 3);
                        int sxA = ex10 + seA;
                        int syA = ey10 + teA;
                        int aimA = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - syA, host.getPlayerXFixed() - sxA);
                        host.spawnEnemyBullet(81, 13, aimA, 6 << 3, sxA, syA);
                        if (lvl10 >= 2) {
                            host.spawnEnemyBullet(81, 13, 180, 6 << 3, sxA, syA);
                        } else {
                            host.spawnEnemyBullet(81, 13, aimA + 120, 6 << 3, sxA, syA);
                            host.spawnEnemyBullet(81, 13, aimA + 240, 6 << 3, sxA, syA);
                        }

                        int dirB = aim - 40;
                        while (dirB < 0) {
                            dirB += 360;
                        }
                        dirB %= 360;
                        int seB = (int) ((((long) Trig.cos(dirB)) * (long) sp2) >> 3);
                        int teB = (int) ((((long) Trig.sin(dirB)) * (long) sp2) >> 3);
                        int sxB = ex10 + seB;
                        int syB = ey10 + teB;
                        int aimB = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - syB, host.getPlayerXFixed() - sxB);
                        host.spawnEnemyBullet(81, 13, aimB, 6 << 3, sxB, syB);
                        if (lvl10 >= 2) {
                            host.spawnEnemyBullet(81, 13, 180, 6 << 3, sxB, syB);
                        } else {
                            host.spawnEnemyBullet(81, 13, aimB + 120, 6 << 3, sxB, syB);
                            host.spawnEnemyBullet(81, 13, aimB + 240, 6 << 3, sxB, syB);
                        }
                    }
                }
                return;

            case 2:
                k = 0;
                enemylist[enemyIdx][3] = 0;
                enemylist[enemyIdx][14] = 524288;
                enemylist[enemyIdx][15] = 524288;
                bossx[0] = -20;
                bossy[0] = -32;
                bspellstep = 0;
                enemylist[enemyIdx][16] = 0;
                if (host.getMGCnt() > 50) {
                    host.stageJb(enemyIdx);
                    return;
                }
                return;
        }
    }

    private void spawnJc0(int bulletId, int ang, int spd, int ex, int ey, int lvl) {
        int s = spd;
        switch (lvl) {
            case 0:
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                return;

            case 1:
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 20, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 20, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                return;

            case 2:
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 20, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 20, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 30, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 30, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 20, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 20, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                return;

            case 3:
            default:
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 20, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 20, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 30, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 30, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 40, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang + 20, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 20, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 40, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 30, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 30, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 20, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 20, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang + 10, s, ex, ey);
                host.spawnEnemyBullet(bulletId, 20, ang - 10, s, ex, ey);
                s -= 8;
                host.spawnEnemyBullet(bulletId, 20, ang, s, ex, ey);
                return;
        }
    }
}
