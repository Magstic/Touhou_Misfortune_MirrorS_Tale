package touhou.stage;

import touhou.EnemyBulletSystem;
import touhou.Trig;

final class StageExtraBossLogic extends AbstractBossStageLogic {
    void tickBossImpl(int enemyIdx) {
        tb(enemyIdx);
    }

    void tickMidbossImpl(int enemyIdx) {
    }

    //boss(stub)
    void tb(int targetIdx) {
        int[][] enemylist = host.getEnemyList();
        int[] bossx = host.getBossX();
        int[] bossy = host.getBossY();
        int bossf = host.getBossF();

        int mt = enemylist[targetIdx][11];
        int idx = 0;
        if (mt == 102) {
            idx = 1;
        } else if (mt == 103) {
            idx = 2;
        }

        switch (bossf) {
            case 1:
                bossx[idx] = 97;
                bossy[idx] = 56;
                bspellcnt = 255;
                batkf = false;
                host.setResetFlag(4);
                enemylist[targetIdx][16] = 1;
                return;

            case 2:
                if (host.getMGCnt() == 0) {
                    host.setBbaria(1);
                }
                if (host.getGameMode() != 3) {
                    bossx[idx] = 97;
                    bossy[idx] = 44;
                }
                bwave[idx] = 120 * idx;
                bspellcnt = 255;
                enemylist[targetIdx][14] = 393216;
                enemylist[targetIdx][15] = 393216;
                batkf = false;
                host.getBossWrk()[2] = 1;
                if (host.getMGCnt() <= 10) {
                    return;
                }
                if (host.getGameMode() == 3) {
                    bspellcnt = 120;
                    int step = host.getSpellPracticeBossStep();
                    wb(step);
                    j = 5000;
                    switch (step) {
                        case 10:
                            j = 2000;
                            break;
                        case 13:
                            j = 2200;
                            break;
                        case 16:
                            j = 1900;
                            break;
                        case 19:
                            j = 2200;
                            break;
                        case 22:
                            j = 2700;
                            break;
                        case 25:
                            j = 1900;
                            break;
                        case 28:
                            j = 1800;
                            break;
                        case 31:
                            j = 1600;
                            break;
                        case 34:
                            j = 6000;
                            break;
                        case 37:
                            j = 5000;
                            break;
                    }
                    yb(targetIdx, 9, 3);
                    return;
                }

                switch (host.getBstock()) {
                    case 8:
                        j = 1100;
                        yb(targetIdx, 9, 1);
                        bspellcnt = 49;
                        wb(3);
                        return;
                    case 7:
                        j = 1100;
                        yb(targetIdx, 9, 1);
                        bspellcnt = 49;
                        wb(4);
                        return;
                    case 6:
                        j = 1900;
                        yb(targetIdx, 9, 3);
                        bspellcnt = 79;
                        wb(16);
                        return;
                    case 5:
                        j = 1000;
                        yb(targetIdx, 9, 1);
                        bspellcnt = 49;
                        wb(5);
                        return;
                    case 4:
                        j = 2700;
                        yb(targetIdx, 9, 3);
                        bspellcnt = 49;
                        wb(22);
                        return;
                    case 3:
                        j = 1000;
                        yb(targetIdx, 9, 1);
                        bspellcnt = 49;
                        wb(6);
                        return;
                    case 2:
                        j = 1800;
                        yb(targetIdx, 9, 3);
                        bspellcnt = 59;
                        wb(28);
                        return;
                    case 1:
                        j = 1600;
                        yb(targetIdx, 9, 3);
                        bspellcnt = 63;
                        wb(31);
                        return;
                    case 0:
                        j = 6000;
                        yb(targetIdx, 9, 3);
                        bspellcnt = 119;
                        wb(34);
                        return;
                }
                return;

            case 3: {
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    j = 65535;
                    yb(l, n, 2);
                    wb(10);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d0_3 = host.getMGCnt() + 1;
                if (d0_3 % 100 == 0) {
                    if (host.getPlayerXFixed() < enemylist[targetIdx][5]) {
                        bossx[0] += 20;
                    } else {
                        bossx[0] -= 20;
                    }
                    if (bossx[0] < 30) {
                        bossx[0] = 127;
                    }
                    if (bossx[0] > 164) {
                        bossx[0] = 67;
                    }
                    if (bossy[0] < 60) {
                        bossy[0] = 60;
                    } else {
                        bossy[0] = 40;
                    }
                }

                if (host.getMGCnt() <= 20) {
                    return;
                }

                int bossXFixed = enemylist[targetIdx][5];
                int bossYFixed = enemylist[targetIdx][6];

                if ((d0_3 & 0x1) == 0) {
                    int angBase = d0_3 << 2;
                    for (int a = 0; a < 360; a += 45) {
                        host.spawnEnemyBullet(603, 0, angBase + a, 40, bossXFixed, bossYFixed);
                    }
                }

                if ((d0_3 % 100) > 50 && (d0_3 & 0x1) == 0) {
                    int angBase = d0_3 << 2;
                    for (int a = 0; a < 360; a += 60) {
                        host.spawnEnemyBullet(585, 0, angBase + a, 48, bossXFixed, bossYFixed);
                    }
                }
                return;
            }

            case 4: {
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    j = 65535;
                    yb(l, n, 2);
                    wb(13);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d0_4 = host.getMGCnt() + 1;
                if (d0_4 % 50 == 0) {
                    if (host.getPlayerXFixed() < enemylist[targetIdx][5]) {
                        bossx[0] += 20;
                    } else {
                        bossx[0] -= 20;
                    }
                    if (bossx[0] < 30) {
                        bossx[0] = 147;
                    }
                    if (bossx[0] > 164) {
                        bossx[0] = 47;
                    }
                    if (bossy[0] < 60) {
                        bossy[0] = 60;
                    } else {
                        bossy[0] = 40;
                    }
                }

                if (host.getMGCnt() <= 20) {
                    return;
                }

                int bossXFixed = enemylist[targetIdx][5];
                int bossYFixed = enemylist[targetIdx][6];

                if ((d0_4 & 0x1) == 0) {
                    int angBase = 360 - ((d0_4 << 2) % 360);
                    for (int a = 0; a < 360; a += 45) {
                        host.spawnEnemyBullet(585, 0, angBase + a, 40, bossXFixed, bossYFixed);
                    }
                }

                if ((d0_4 % 100) > 50 && (d0_4 & 0x1) == 0) {
                    int angBase = 360 - ((d0_4 << 1) % 360);
                    for (int a = 0; a < 360; a += 60) {
                        host.spawnEnemyBullet(603, 0, angBase + a, 48, bossXFixed, bossYFixed);
                    }
                }

                if ((d0_4 % 100) >= 80 && (d0_4 & 0x7) == 0) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                    enemylist[targetIdx][10] = aim;
                    for (int sp = 3; sp < 8; sp++) {
                        host.spawnEnemyBullet(92, 0, aim, sp << 3, bossXFixed, bossYFixed);
                    }
                }
                return;
            }

            case 5: {
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    j = 65535;
                    yb(l, n, 2);
                    wb(19);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d0_5 = host.getMGCnt() + 1;
                if (d0_5 % 50 == 0) {
                    if (host.getPlayerXFixed() < enemylist[targetIdx][5]) {
                        bossx[0] += 20;
                    } else {
                        bossx[0] -= 20;
                    }
                    if (bossx[0] < 30) {
                        bossx[0] = 147;
                    }
                    if (bossx[0] > 164) {
                        bossx[0] = 47;
                    }
                    if (bossy[0] < 60) {
                        bossy[0] = 60;
                    } else {
                        bossy[0] = 40;
                    }
                }

                if (host.getMGCnt() <= 20) {
                    return;
                }

                int bossXFixed5 = enemylist[targetIdx][5];
                int bossYFixed5 = enemylist[targetIdx][6];

                if ((d0_5 & 0x3) == 0) {
                    int angBase = 360 - ((d0_5 << 2) % 360);
                    for (int a = 0; a < 360; a += 90) {
                        host.spawnEnemyBullet(585, 43, angBase + a, 32, bossXFixed5, bossYFixed5);
                        host.spawnEnemyBullet(585, 44, angBase + a, 32, bossXFixed5, bossYFixed5);
                    }
                }

                if ((d0_5 & 0x1F) == 0) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed5, host.getPlayerXFixed() - bossXFixed5);
                    enemylist[targetIdx][10] = aim;
                    for (int n0 = -2; n0 <= 2; n0++) {
                        int ang = aim + (30 * n0);
                        for (int s = 0; s < 3; s++) {
                            host.spawnEnemyBullet(96, 0, ang, 28 + (s << 3), bossXFixed5, bossYFixed5);
                        }
                    }
                }
                return;
            }

            case 6:
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    j = 65535;
                    yb(l, n, 2);
                    wb(25);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d6 = (host.getMGCnt() + 1) % 260;
                enemylist[targetIdx][14] = 65536;
                enemylist[targetIdx][15] = 65536;
                if (d6 == 240) {
                    if (host.getPlayerXFixed() < enemylist[targetIdx][5]) {
                        bossx[0] -= 20;
                    } else {
                        bossx[0] += 20;
                    }
                    if (bossx[0] < 30) {
                        bossx[0] += 30;
                    }
                    if (bossx[0] > 164) {
                        bossx[0] -= 30;
                    }
                    bossy[0] = 54;
                }

                int bossXFixed6 = enemylist[targetIdx][5];
                int bossYFixed6 = enemylist[targetIdx][6];

                if (d6 == 20) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed6, host.getPlayerXFixed() - bossXFixed6);
                    enemylist[targetIdx][10] = aim;
                }

                if (d6 <= 20 || d6 >= 240) {
                    return;
                }

                int d5 = 0;
                if (d6 > 50) {
                    d5 = (((d6 - 50) << 1) + 35) % 140;
                    if (d5 > 70) {
                        d5 = 140 - d5;
                    }
                    d5 -= 35;
                }

                int baseAim = enemylist[targetIdx][10];
                for (int n35 = -60; n35 <= 60; n35 += 40) {
                    int ang = baseAim + n35 + d5;
                    host.spawnEnemyBullet(603, 0, ang, 72, bossXFixed6, bossYFixed6);
                    host.spawnEnemyBullet(603, 0, ang, 64, bossXFixed6, bossYFixed6);
                    host.spawnEnemyBullet(603, 0, ang, 56, bossXFixed6, bossYFixed6);
                }

                if (d6 <= 40) {
                    return;
                }

                if ((d6 & 0x3F) == 0) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed6, host.getPlayerXFixed() - bossXFixed6);
                    for (int n36 = -20; n36 <= 20; n36 += 20) {
                        for (int n37 = 0; n37 < 360; n37 += 120) {
                            host.spawnEnemyBullet(91, 0, aim + n36 + n37, 24, bossXFixed6, bossYFixed6);
                        }
                    }
                }

                if ((d6 & 0x3F) == 0x1F) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed6, host.getPlayerXFixed() - bossXFixed6);
                    for (int n38 = -30; n38 <= 30; n38 += 15) {
                        for (int n39 = 0; n39 < 360; n39 += 90) {
                            host.spawnEnemyBullet(87, 0, aim + n38 + n39, 32, bossXFixed6, bossYFixed6);
                        }
                    }
                }

                if ((d6 & 0x3F) == 0x2F) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed6, host.getPlayerXFixed() - bossXFixed6);
                    for (int n40 = 0; n40 < 360; n40 += 72) {
                        host.spawnEnemyBullet(95, 0, aim + n40, 16, bossXFixed6, bossYFixed6);
                    }
                    return;
                }
                return;

            case 10:
                bossx[0] = 97;
                bossy[0] = 68;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 98;
                    wb(11);
                    return;
                }
                return;

            case 11:
                bspellcnt = 39;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(12);
                    j = 2000;
                    yb(targetIdx, 9, 2);
                    return;
                }
                return;

            case 12:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }
                enemylist[targetIdx][3] = 0;

                int d0_12 = host.getMGCnt() % 300;
                if (host.getMGCnt() <= 30) {
                    return;
                }

                int div;
                if (j < 500) {
                    div = 5;
                } else {
                    div = 6;
                }

                if ((d0_12 % div) == 0) {
                    int cnt = 3;
                    int dist = 500 + (j >> 4);
                    int spd = 12;
                    for (int i = 0; i < cnt; i++) {
                        int ang = ((d0_12 << 2) + (i * (360 / cnt))) % 360;
                        int offX = (int) ((((long) Trig.cos(ang)) * (long) dist) >> 3);
                        int offY = (int) ((((long) Trig.sin(ang)) * (long) dist) >> 3);
                        int sx = host.getPlayerXFixed() + offX;
                        int sy = host.getPlayerYFixed() + offY;
                        host.spawnEnemyBullet(85, 39, ang + 180, spd, sx, sy);
                    }
                }

                if ((d0_12 % ((div << 2) + 4)) == 0) {
                    int sx = ((host.getPlayerXFixed() >> 16) + (enemylist[targetIdx][5] >> 16) + (d0_12 << 2) + (j >> 1)) % 194;
                    int spawnXFixed = sx << 16;
                    int spawnYFixed = 16121856;
                    host.spawnEnemyBullet(89, 0, 270, 16 + (d0_12 % 5), spawnXFixed, spawnYFixed);
                }

                if ((d0_12 % ((div << 2) + 10)) == 0) {
                    int sx = ((host.getPlayerXFixed() >> 16) + (enemylist[targetIdx][5] >> 16) + d0_12 + j) % 194;
                    int spawnXFixed = sx << 16;
                    int spawnYFixed = 16646144;
                    host.spawnEnemyBullet(95, 0, 270, 16 + (d0_12 % 5), spawnXFixed, spawnYFixed);
                    return;
                }
                return;

            case 13:
                bossx[0] = 97;
                bossy[0] = 91;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 99;
                    wb(14);
                    return;
                }
                return;

            case 14:
                bspellcnt = 59;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(15);
                    j = 2200;
                    yb(targetIdx, 9, 2);
                    return;
                }
                return;

            case 15:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d0_15 = host.getMGCnt() % 200;
                int bossXFixed = enemylist[targetIdx][5];
                int bossYFixed = enemylist[targetIdx][6];

                int step = 45;
                if (d0_15 < 100) {
                    enemylist[targetIdx][10] = (enemylist[targetIdx][10] + 4 + 360) % 360;
                    if (d0_15 > 50) {
                        enemylist[targetIdx][10] = (enemylist[targetIdx][10] + 4 + 360) % 360;
                    }
                    if (d0_15 > 80) {
                        enemylist[targetIdx][10] = (enemylist[targetIdx][10] + 4 + 360) % 360;
                    }
                } else {
                    enemylist[targetIdx][10] = (enemylist[targetIdx][10] - 4 + 360) % 360;
                    if (d0_15 > 150) {
                        enemylist[targetIdx][10] = (enemylist[targetIdx][10] - 4 + 360) % 360;
                    }
                    if (d0_15 > 180) {
                        enemylist[targetIdx][10] = (enemylist[targetIdx][10] - 4 + 360) % 360;
                    }
                }

                int cnt = 3 + (d0_15 / 100);
                if (cnt > 6) {
                    cnt = 6;
                }

                if ((d0_15 & 0x3) == 0) {
                    int base = enemylist[targetIdx][10];
                    for (int a = 0; a < 360; a += step) {
                        for (int n = 0; n < cnt; n++) {
                            host.spawnEnemyBullet(255, 34, base + a, 30 + (n << 2), bossXFixed, bossYFixed);
                        }
                    }
                }

                for (int i = 0; i < 5; i++) {
                    if (host.getMGCnt() > 50 + (100 * i)) {
                        int d3 = d0_15 + (4 * i);
                        int sx0 = 0;
                        int sy0 = (230 - (8 * i)) << 16;
                        if ((d3 & 0x17) == 0) {
                            host.spawnEnemyBullet(85, 0, 360, 20, sx0, sy0);
                        }

                        d3 = d0_15 + 8 + (4 * i);
                        int sx1 = 12713984;
                        int sy1 = (226 - (8 * i)) << 16;
                        if ((d3 & 0x17) == 0) {
                            host.spawnEnemyBullet(85, 0, 540, 20, sx1, sy1);
                        }
                    }
                }
                return;

            case 16:
                bossx[0] = 97;
                bossy[0] = 81;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[targetIdx][14] = 65536;
                    enemylist[targetIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 100;
                    wb(17);
                    return;
                }
                return;

            case 17:
                bspellcnt = 79;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][14] = 0;
                    enemylist[targetIdx][15] = 0;
                    enemylist[targetIdx][3] = 0;
                    enemylist[targetIdx][10] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(18);
                    return;
                }
                return;

            case 18: {
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d0_18 = host.getMGCnt() + 1;
                int bossXFixed18 = enemylist[targetIdx][5];
                int bossYFixed18 = enemylist[targetIdx][6];

                if ((d0_18 & 0x1) == 0) {
                    int wob = (d0_18 << 1) % 120;
                    if (wob > 30) {
                        wob = 90 - wob;
                    }

                    int base0 = 315 + wob;
                    for (int n0 = -1; n0 <= 1; n0++) {
                        host.spawnEnemyBullet(287, 0, base0 + (20 * n0), 40, bossXFixed18, bossYFixed18);
                    }
                    int base1 = 585 - wob;
                    for (int n1 = -1; n1 <= 1; n1++) {
                        host.spawnEnemyBullet(287, 0, base1 + (20 * n1), 40, bossXFixed18, bossYFixed18);
                    }
                }

                if ((d0_18 & 0x3) == 0) {
                    int wob = (d0_18 << 2) % 120;
                    if (wob > 30) {
                        wob = 90 - wob;
                    }

                    int base0 = 390 + wob;
                    for (int n0 = -1; n0 <= 1; n0++) {
                        host.spawnEnemyBullet(271, 37, base0 + (20 * n0), 32, bossXFixed18, bossYFixed18);
                    }
                    int base1 = 510 - wob;
                    for (int n1 = -1; n1 <= 1; n1++) {
                        host.spawnEnemyBullet(271, 37, base1 + (20 * n1), 32, bossXFixed18, bossYFixed18);
                    }
                }

                if ((d0_18 & 0x1F) == 0) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed18, host.getPlayerXFixed() - bossXFixed18);
                    enemylist[targetIdx][10] = aim;
                    host.spawnEnemyBullet(93, 0, aim, 28, bossXFixed18, bossYFixed18);

                    for (int i = 0; i < 4; i++) {
                        int ang0 = 45 + (360 * i) / 4;
                        int dist = 160;
                        int offX = (int) ((((long) Trig.cos(ang0)) * (long) dist) >> 3);
                        int offY = (int) ((((long) Trig.sin(ang0)) * (long) dist) >> 3);
                        int sx = bossXFixed18 + offX;
                        int sy = bossYFixed18 + offY;

                        int aim2 = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - sy, host.getPlayerXFixed() - sx);
                        enemylist[targetIdx][10] = aim2;
                        for (int n = 0; n < 6; n++) {
                            host.spawnEnemyBullet(81, 45, aim2, 28 + (n << 2), sx, sy);
                        }
                    }
                }
                return;
            }

            case 19:
                bossx[0] = 97;
                bossy[0] = 91;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 101;
                    wb(20);
                    return;
                }
                return;

            case 20:
                bspellcnt = 59;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(21);
                    j = 2200;
                    yb(targetIdx, 9, 2);
                    return;
                }
                return;

            case 21: {
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d0_21 = host.getMGCnt() % 200;
                int bossXFixed21 = enemylist[targetIdx][5];
                int bossYFixed21 = enemylist[targetIdx][6];

                if (d0_21 < 60) {
                    int d3;
                    int offAng;
                    int sx;
                    int sy;

                    d3 = d0_21 * 6;
                    offAng = d3;
                    sx = bossXFixed21 + (int) ((((long) Trig.cos(offAng)) * (long) 360) >> 3);
                    sy = bossYFixed21 + (int) ((((long) Trig.sin(offAng)) * (long) 360) >> 3);
                    for (int n22 = 1; n22 < 4; n22++) {
                        host.spawnEnemyBullet(81, 0, d3 + (n22 * 90), 0, sx, sy);
                    }

                    if (d0_21 >= 20) {
                        d3 = 360 - (d0_21 * 9);
                        offAng = d3 + 90;
                        sx = bossXFixed21 + (int) ((((long) Trig.cos(offAng)) * (long) 300) >> 3);
                        sy = bossYFixed21 + (int) ((((long) Trig.sin(offAng)) * (long) 300) >> 3);
                        host.spawnEnemyBullet(81, 0, d3 + 45, 0, sx, sy);
                    }

                    d3 = 360 - (d0_21 * 6);
                    offAng = d3 + 180;
                    sx = bossXFixed21 + (int) ((((long) Trig.cos(offAng)) * (long) 240) >> 3);
                    sy = bossYFixed21 + (int) ((((long) Trig.sin(offAng)) * (long) 240) >> 3);
                    host.spawnEnemyBullet(81, 0, d3, 0, sx, sy);

                    if (d0_21 >= 30) {
                        d3 = d0_21 * 12;
                        offAng = d3 + 270;
                        sx = bossXFixed21 + (int) ((((long) Trig.cos(offAng)) * (long) 180) >> 3);
                        sy = bossYFixed21 + (int) ((((long) Trig.sin(offAng)) * (long) 180) >> 3);
                        host.spawnEnemyBullet(81, 0, d3 - 45, 0, sx, sy);
                    }
                }

                if (d0_21 == 60) {
                    for (int a = 0; a < 360; a += 45) {
                        for (int s = 0; s < 6; s++) {
                            host.spawnEnemyBullet(693, 0, a, 48 + (s << 2), bossXFixed21, bossYFixed21);
                        }
                    }
                }

                if (d0_21 == 65) {
                    host.setEnemyBulletsDeltaForIdIfDeltaZero(81, 32, 2);
                }

                if (d0_21 >= 70 && d0_21 < 140) {
                    if (d0_21 == 70) {
                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed21, host.getPlayerXFixed() - bossXFixed21);
                        enemylist[targetIdx][10] = aim;
                    }

                    if ((d0_21 & 0x7) == 0) {
                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed21, host.getPlayerXFixed() - bossXFixed21);
                        int div21 = 8;
                        if ((d0_21 & 0xF) == 0) {
                            div21 = 16;
                        }
                        for (int a = 0; a < 360; a += 360 / div21) {
                            host.spawnEnemyBullet(81, 21, aim + a, 64, bossXFixed21, bossYFixed21);
                            host.spawnEnemyBullet(81, 22, aim + a, 64, bossXFixed21, bossYFixed21);
                        }
                    }

                    if ((d0_21 & 0x1) == 0) {
                        int off = (d0_21 - 70) << 2;
                        int base = enemylist[targetIdx][10];
                        host.spawnEnemyBullet(81, 2, base + 60 + off, 90, bossXFixed21, bossYFixed21);
                        host.spawnEnemyBullet(81, 2, base + 60 - off + 360, 90, bossXFixed21, bossYFixed21);
                        host.spawnEnemyBullet(81, 2, base + 180 + off, 90, bossXFixed21, bossYFixed21);
                        host.spawnEnemyBullet(81, 2, base + 180 - off + 360, 90, bossXFixed21, bossYFixed21);
                        host.spawnEnemyBullet(81, 2, base + 300 + off, 90, bossXFixed21, bossYFixed21);
                        host.spawnEnemyBullet(81, 2, base + 300 - off + 360, 90, bossXFixed21, bossYFixed21);
                    }
                }

                if (d0_21 == 145) {
                    if (host.getMGCnt() < 150) {
                        if (host.getPlayerXFixed() < enemylist[targetIdx][5]) {
                            bossx[0] = 67;
                        } else {
                            bossx[0] = 127;
                        }
                    } else if (bossx[0] == 67) {
                        bossx[0] = 137;
                    } else {
                        bossx[0] = 57;
                    }
                    bossy[0] = 81;
                    return;
                }
                return;
            }

            case 22:
                bossx[idx] = 97;
                bossy[idx] = 81;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[targetIdx][14] = 65536;
                    enemylist[targetIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 102;
                    wb(23);
                    return;
                }
                return;

            case 23:
                bspellcnt = 49;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][14] = 0;
                    enemylist[targetIdx][15] = 0;
                    enemylist[targetIdx][3] = 0;
                    enemylist[targetIdx][10] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(24);
                    return;
                }
                return;

            case 24: {
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d8_24 = 0;
                if (bspellcnt < 40) {
                    d8_24 = 1;
                }
                if (j < 1400) {
                    d8_24 = 1;
                }
                if (bspellcnt < 30) {
                    d8_24 = 2;
                }
                if (j < 1000) {
                    d8_24 = 2;
                }
                if (bspellcnt < 15) {
                    d8_24 = 3;
                }
                if (j < 600) {
                    d8_24 = 3;
                }

                int d0_24 = host.getMGCnt() + 1;
                int cx24 = 97 << 16;
                int cy24 = 121 << 16;

                if (d8_24 >= 1 && (d0_24 & 0x1F) == 0x7) {
                    int stepAng = 18;
                    int spd = 20;
                    for (int a = 0; a < 360; a += stepAng) {
                        host.spawnEnemyBullet(86, 0, a + (stepAng >> 1), spd, cx24, cy24);
                    }
                }

                int step24 = 24;
                int bullet24 = 82;
                int spd24 = 12;
                if (d8_24 >= 2) {
                    step24 = 18;
                    bullet24 = 86;
                    spd24 = 14;
                }
                if (d8_24 >= 3) {
                    step24 = 18;
                    bullet24 = 81;
                    spd24 = 18;
                }

                if ((d0_24 & 0xF) == 0) {
                    int d7 = 0;
                    if ((d0_24 & 0x1F) == 0) {
                        // DoJa original ( case 24): d7 intentionally uses 16.16 units here.
                        // This makes the (d0&0x1F)==0 tick skip the spawn loops (even waves), preventing skew.
                        d7 = (step24 >> 1) << 16;
                    }

                    for (int n28 = d7; n28 < 194; n28 += step24) {
                        int sx = n28 << 16;
                        int sy = 524288;
                        int aim = EnemyBulletSystem.arcTan2Deg(cy24 - sy, cx24 - sx);
                        host.spawnEnemyBullet(bullet24, 46, aim, spd24, sx, sy);

                        sx = n28 << 16;
                        sy = 15335424;
                        aim = EnemyBulletSystem.arcTan2Deg(cy24 - sy, cx24 - sx);
                        host.spawnEnemyBullet(bullet24, 47, aim, spd24, sx, sy);
                    }

                    for (int n29 = 8 + d7; n29 < 234; n29 += step24) {
                        int sx = 0;
                        int sy = n29 << 16;
                        int aim = EnemyBulletSystem.arcTan2Deg(cy24 - sy, cx24 - sx);
                        host.spawnEnemyBullet(bullet24, 48, aim, spd24, sx, sy);

                        sx = 12713984;
                        sy = n29 << 16;
                        aim = EnemyBulletSystem.arcTan2Deg(cy24 - sy, cx24 - sx);
                        host.spawnEnemyBullet(bullet24, 49, aim, spd24, sx, sy);
                    }
                }

                if (d8_24 >= 2 && (d0_24 & 0x1F) == 0) {
                    int stepAng = 15;
                    int spd = 20;
                    for (int n30 = 0; n30 <= 90; n30 += stepAng) {
                        int mt24;

                        int sx = 0;
                        int sy = 524288;
                        mt24 = (n30 > 45) ? 46 : 48;
                        host.spawnEnemyBullet(82, mt24, 360 + n30, spd, sx, sy);

                        sx = 12713984;
                        sy = 524288;
                        mt24 = (n30 > 45) ? 49 : 46;
                        host.spawnEnemyBullet(82, mt24, 450 + n30, spd, sx, sy);

                        sx = 0;
                        sy = 15335424;
                        mt24 = (n30 > 45) ? 48 : 47;
                        host.spawnEnemyBullet(82, mt24, 270 + n30, spd, sx, sy);

                        sx = 12713984;
                        sy = 15335424;
                        mt24 = (n30 > 45) ? 47 : 49;
                        host.spawnEnemyBullet(82, mt24, 540 + n30, spd, sx, sy);
                    }
                }
                return;
            }

            case 25:
                bossx[idx] = 97;
                bossy[idx] = 68;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 103;
                    wb(26);
                    return;
                }
                return;

            case 26:
                bspellcnt = 49;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(27);
                    j = 1900;
                    yb(targetIdx, 9, 2);
                    return;
                }
                return;

            case 27:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }
                enemylist[targetIdx][3] = 0;

                int d0_27 = host.getMGCnt();
                int bossXFixed27 = enemylist[targetIdx][5];
                // Y-axis is not needed.
                if ((d0_27 & 0x1F) == 0) {
                    int sx = ((host.getPlayerXFixed() >> 16) + (bossXFixed27 >> 16) + (d0_27 << 2) + (j >> 1)) % 194;
                    int spawnXFixed = sx << 16;
                    int spawnYFixed = -262144;
                    int mt27 = 50;
                    if ((d0_27 & 0x3F) == 0) {
                        mt27 = 51;
                    }
                    host.spawnEnemyBullet(-1, mt27, 450, 40, spawnXFixed, spawnYFixed);
                }
                return;

            case 28:
                bossx[idx] = 97;
                bossy[idx] = 81;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[targetIdx][14] = 65536;
                    enemylist[targetIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 104;
                    wb(29);
                    return;
                }
                return;

            case 29:
                bspellcnt = 59;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][14] = 327680;
                    enemylist[targetIdx][15] = 327680;
                    enemylist[targetIdx][3] = 0;
                    enemylist[targetIdx][10] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(30);
                    host.getBossWrk()[7] = 20;
                    return;
                }
                return;

            case 30:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int[] bossWrk30 = host.getBossWrk();
                int mt30 = 71;
                int bullet30 = 88;
                if (bspellcnt < 40 || o[1] < (o[2] * 70 / 100)) {
                    mt30 = 72;
                    bullet30 = 92;
                }
                if (bspellcnt < 15 || o[1] < (o[2] * 40 / 100)) {
                    mt30 = 73;
                    bullet30 = 87;
                }
                if (bossWrk30[7] == 0) {
                    int bossXFixed30 = enemylist[targetIdx][5];
                    int bossYFixed30 = enemylist[targetIdx][6];
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed30, host.getPlayerXFixed() - bossXFixed30);
                    enemylist[targetIdx][10] = aim;
                    host.spawnEnemyBullet(bullet30, mt30, aim, 20, bossXFixed30, bossYFixed30);

                    int px = host.getPlayerXFixed() >> 16;
                    bossx[idx] = 194 - px;
                    if (bossx[idx] < 20) {
                        bossx[idx] = 20;
                    }
                    if (bossx[idx] > 174) {
                        bossx[idx] = 174;
                    }
                    bossWrk30[7] = 50 + (bspellcnt >> 1);
                }
                --bossWrk30[7];
                return;

            case 31:
                bossx[idx] = 97;
                bossy[idx] = 121;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[targetIdx][14] = 65536;
                    enemylist[targetIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 105;
                    wb(32);
                    return;
                }
                return;

            case 32:
                bspellcnt = 63;
                switch (host.getMGCnt()) {
                    case 1:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][14] = 327680;
                    enemylist[targetIdx][15] = 327680;
                    enemylist[targetIdx][10] = 0;
                    bspellstep = 2;
                    batkf = true;
                    wb(33);
                    return;
                }
                return;

            case 33:
                if (bspellcnt < 0) {
                    j = 0;
                    enemylist[targetIdx][5] = 6356992;
                    enemylist[targetIdx][6] = 7929856;
                    host.stageJb(targetIdx);
                    return;
                }
                int d0_33 = host.getMGCnt();
                int bossXFixed33 = enemylist[targetIdx][5];
                int bossYFixed33 = enemylist[targetIdx][6];

                if (d0_33 == 10) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed33, host.getPlayerXFixed() - bossXFixed33);
                    enemylist[targetIdx][10] = aim;
                    for (int n42 = 0; n42 < 360; n42 += 45) {
                        host.spawnEnemyBullet(81, 70, aim + n42, 20, bossXFixed33, bossYFixed33);
                    }
                }

                if (d0_33 == 50) {
                    enemylist[targetIdx][6] = -3276800;
                    bossy[idx] = -50;
                }

                if (d0_33 <= 80) {
                    return;
                }

                int cx33 = 6356992;
                int cy33 = 7929856;

                if (bspellcnt > 45 && bspellcnt < 60 && (d0_33 % 24) == 0) {
                    int d3 = ((d0_33 << 4) + d0_33) % 360;
                    for (int n43 = 0; n43 < 360; n43 += 120) {
                        for (int n44 = 0; n44 < 6; n44++) {
                            host.spawnEnemyBullet(85, 75, d3 + n43 + (n44 << 2), 12 + n44, cx33, cy33);
                            host.spawnEnemyBullet(85, 75, 360 - d3 + n43 - (n44 << 2), 12 + n44, cx33, cy33);
                        }
                    }
                }

                if (bspellcnt > 30 && bspellcnt < 44 && (d0_33 & 0xD) == 0) {
                    int d3 = ((d0_33 << 3) + d0_33) % 360;
                    for (int n45 = 0; n45 < 360; n45 += 90) {
                        host.spawnEnemyBullet(83, 75, d3 + n45, 12, cx33, cy33);
                        host.spawnEnemyBullet(83, 75, 360 - d3 + n45, 12, cx33, cy33);
                    }
                }

                if (bspellcnt > 15 && bspellcnt < 29 && (d0_33 % 24) == 0) {
                    int d3 = ((d0_33 << 2) + d0_33) % 360;
                    for (int n46 = 0; n46 < 360; n46 += 72) {
                        for (int n47 = 0; n47 < 4; n47++) {
                            host.spawnEnemyBullet(86, 75, d3 + n46 + (n47 << 2), 12 + n47, cx33, cy33);
                            host.spawnEnemyBullet(86, 75, 360 - d3 + n46 - (n47 << 2), 12 + n47, cx33, cy33);
                        }
                    }
                }

                if (bspellcnt < 10 && (d0_33 & 0x9) == 0) {
                    int d3 = ((d0_33 << 1) + d0_33) % 360;
                    for (int n48 = 0; n48 < 360; n48 += 60) {
                        host.spawnEnemyBullet(84, 75, d3 + n48, 12, cx33, cy33);
                        host.spawnEnemyBullet(84, 75, 360 - d3 + n48, 12, cx33, cy33);
                    }
                }
                return;

            case 34:
                bossx[idx] = 97;
                bossy[idx] = 81;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[targetIdx][14] = 65536;
                    enemylist[targetIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 106;
                    wb(35);
                    return;
                }
                return;

            case 35:
                bspellcnt = 119;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][14] = 327680;
                    enemylist[targetIdx][15] = 327680;
                    enemylist[targetIdx][3] = 0;
                    enemylist[targetIdx][10] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(36);
                    int[] bossWrk35 = host.getBossWrk();
                    bossWrk35[6] = 0;
                    bossWrk35[7] = 20;
                    return;
                }
                return;

            case 36: {
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int[] bossWrk36 = host.getBossWrk();
                if (bossWrk36[7] > 0) {
                    --bossWrk36[7];
                    return;
                }

                int bossXFixed36 = enemylist[targetIdx][5];
                int bossYFixed36 = enemylist[targetIdx][6];

                bossWrk36[6] = 0;
                if (bspellcnt < 100 || o[1] < (o[2] * 85 / 100)) {
                    bossWrk36[6] = 1;
                }
                if (bspellcnt < 80 || o[1] < (o[2] * 70 / 100)) {
                    bossWrk36[6] = 2;
                }
                if (bspellcnt < 60 || o[1] < (o[2] * 60 / 100)) {
                    bossWrk36[6] = 3;
                }
                if (bspellcnt < 45 || o[1] < (o[2] * 45 / 100)) {
                    bossWrk36[6] = 4;
                }
                if (bspellcnt < 30 || o[1] < (o[2] * 30 / 100)) {
                    bossWrk36[6] = 5;
                }
                if (bspellcnt < 15 || o[1] < (o[2] * 15 / 100)) {
                    bossWrk36[6] = 6;
                }

                int d0_36;
                int d3_36;
                int d4_36;
                int d5_36;
                int d6_36;
                int d7_36;

                if (bossWrk36[6] >= 0) {
                    d0_36 = host.getMGCnt();
                    if ((d0_36 & 0x3) == 0) {
                        if (bossWrk36[6] == 0) {
                            d6_36 = 8;
                            d7_36 = 6;
                        } else {
                            d6_36 = 3;
                            d7_36 = 4;
                        }
                        d5_36 = 360 / d6_36;
                        for (int n50 = 0; n50 < 360; n50 += d5_36) {
                            d3_36 = (d0_36 << 3) + n50;
                            for (int n51 = 0; n51 < d7_36; n51++) {
                                host.spawnEnemyBullet(603, 0, d3_36, 30 + (n51 << 2), bossXFixed36, bossYFixed36);
                            }
                        }
                    }
                }

                if (bossWrk36[6] >= 1) {
                    d0_36 = host.getMGCnt();
                    if ((d0_36 & 0x7) == 0) {
                        if (bossWrk36[6] == 1) {
                            d6_36 = 4;
                        } else {
                            d6_36 = 2;
                        }
                        d5_36 = 360 / d6_36;
                        for (int n52 = 0; n52 < 360; n52 += d5_36) {
                            d3_36 = 360 - ((d0_36 << 3) % 360) + n52;
                            for (int n53 = -1; n53 <= 1; n53++) {
                                host.spawnEnemyBullet(90, 0, d3_36 + (n53 << 3), 32, bossXFixed36, bossYFixed36);
                            }
                        }
                    }
                }

                if (bossWrk36[6] >= 2) {
                    d0_36 = host.getMGCnt();
                    if ((d0_36 & 0xF) == 0) {
                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed36, host.getPlayerXFixed() - bossXFixed36);
                        enemylist[targetIdx][10] = aim;
                        if (bossWrk36[6] == 2) {
                            d6_36 = 15;
                        } else {
                            d6_36 = 8;
                        }
                        d5_36 = 360 / d6_36;
                        for (int n54 = 0; n54 < 360; n54 += d5_36) {
                            host.spawnEnemyBullet(271, 0, aim + n54, 32, bossXFixed36, bossYFixed36);
                        }
                    }
                }

                if (bossWrk36[6] >= 3) {
                    d0_36 = host.getMGCnt();
                    if ((d0_36 & 0x3) == 0) {
                        if (bossWrk36[6] == 3) {
                            d6_36 = 8;
                        } else {
                            d6_36 = 3;
                        }
                        d5_36 = 360 / d6_36;
                        for (int n55 = 0; n55 < 360; n55 += d5_36) {
                            d3_36 = (host.getMGCnt() << 1) + n55;
                            host.spawnEnemyBullet(657, 11, d3_36, 32, bossXFixed36, bossYFixed36);
                        }
                    }
                }

                if (bossWrk36[6] >= 4) {
                    d0_36 = host.getMGCnt();
                    if ((d0_36 & 0x3) == 0) {
                        if (bossWrk36[6] == 4) {
                            d6_36 = 8;
                        } else {
                            d6_36 = 3;
                        }
                        d5_36 = 360 / d6_36;
                        d7_36 = 360 - ((d0_36 << 1) % 360);
                        for (int n56 = 0; n56 < 360; n56 += d5_36) {
                            d3_36 = d7_36 + n56;
                            host.spawnEnemyBullet(86, 10, d3_36, 32, bossXFixed36, bossYFixed36);
                        }
                    }
                }

                if (bossWrk36[6] < 5) {
                    return;
                }

                d0_36 = host.getMGCnt();
                if ((d0_36 & 0x7) == 0) {
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed36, host.getPlayerXFixed() - bossXFixed36);
                    enemylist[targetIdx][10] = aim;
                    if (bossWrk36[6] == 5) {
                        d6_36 = 10;
                    } else {
                        d6_36 = 3;
                    }
                    d5_36 = 160 / d6_36;
                    d4_36 = ((d0_36 << 2) % 160) - 30;
                    d7_36 = (d0_36 & 0x3F);
                    if (d7_36 > 31) {
                        d7_36 = 63 - d7_36;
                    }
                    for (int n57 = -80; n57 <= 80; n57 += d5_36) {
                        d3_36 = aim + d4_36 + n57;
                        host.spawnEnemyBullet(585, 40, d3_36, -64 + d7_36, bossXFixed36, bossYFixed36);
                    }
                }
                return;
            }

            case 37:
                bossx[0] = 97;
                bossy[0] = 68;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 115;
                    wb(38);
                    return;
                }
                return;

            case 38:
                bspellcnt = 116;
                switch (host.getMGCnt()) {
                    case 1:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                    case 18:
                        enemylist[targetIdx][3] = 3;
                        break;
                    case 11:
                    case 13:
                    case 15:
                    case 17:
                    case 19:
                        enemylist[targetIdx][3] = 4;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(39);
                    return;
                }
                return;

            case 39: {
                if (bspellcnt < 0) {
                    j = 0;
                    enemylist[targetIdx][5] = 6356992;
                    enemylist[targetIdx][6] = 7929856;
                    host.stageJb(targetIdx);
                    return;
                }

                int d0_39 = host.getMGCnt();
                int bossXFixed_39 = enemylist[targetIdx][5];
                int bossYFixed_39 = enemylist[targetIdx][6];

                if (bspellcnt > 10) {
                    int d39MinJ;
                    if (bspellcnt < 100) {
                        d39MinJ = bspellcnt * 5000 / 100;
                    } else {
                        d39MinJ = 5000;
                    }
                    if (j < d39MinJ) {
                        j = d39MinJ;
                    }
                }

                if (d0_39 >= 10 && d0_39 <= 100) {
                    int px_39 = host.getPlayerXFixed();
                    int py_39 = host.getPlayerYFixed();

                    if ((d0_39 & 0x3) == 0) {
                        int sx_39 = bossXFixed_39;
                        int sy_39 = bossYFixed_39 - ((70 + d0_39) << 16);

                        int aim_39 = EnemyBulletSystem.arcTan2Deg(py_39 - sy_39, px_39 - sx_39);
                        enemylist[targetIdx][10] = aim_39;

                        for (int a = -80; a < 80; a += 20) {
                            host.spawnEnemyBullet(239, 84, aim_39 + a, 20, sx_39, sy_39);
                        }
                    }
                    if ((d0_39 & 0x3) == 1) {
                        int sx_39 = bossXFixed_39 - ((80 - d0_39) << 16);
                        int sy_39 = bossYFixed_39 - 5242880;
                        int aim_39 = enemylist[targetIdx][10];
                        for (int a = -80; a < 80; a += 20) {
                            host.spawnEnemyBullet(239, 84, aim_39 + a, 22, sx_39, sy_39);
                        }
                    }
                    if ((d0_39 & 0x3) == 2) {
                        int sx_39 = bossXFixed_39 + ((80 - d0_39) << 16);
                        int sy_39 = bossYFixed_39 - 5242880;
                        int aim_39 = enemylist[targetIdx][10];
                        for (int a = -80; a < 80; a += 20) {
                            host.spawnEnemyBullet(239, 84, aim_39 + a, 24, sx_39, sy_39);
                        }
                    }
                }

                if (d0_39 > 200 && d0_39 < 350 && (d0_39 & 0xF) == 0) {
                    for (int n61 = 0; n61 < 5; ++n61) {
                        int dir_39 = (((d0_39 << 2) + n61 * 72) % 360) + 270;
                        int spd_39 = 24 + (n61 << 1);

                        int ang_39 = dir_39 + 180;
                        int se_39 = (int) ((((long) Trig.cos(ang_39)) * 1400L) >> 3);
                        int te_39 = (int) ((((long) Trig.sin(ang_39)) * 1400L) >> 3);
                        host.spawnEnemyBullet(319, 39, dir_39, spd_39, bossXFixed_39 + se_39, bossYFixed_39 + te_39);

                        ang_39 = dir_39 + 180 + 5;
                        se_39 = (int) ((((long) Trig.cos(ang_39)) * 1430L) >> 3);
                        te_39 = (int) ((((long) Trig.sin(ang_39)) * 1430L) >> 3);
                        host.spawnEnemyBullet(319, 39, dir_39, spd_39, bossXFixed_39 + se_39, bossYFixed_39 + te_39);

                        ang_39 = dir_39 + 180 + 355;
                        se_39 = (int) ((((long) Trig.cos(ang_39)) * 1430L) >> 3);
                        te_39 = (int) ((((long) Trig.sin(ang_39)) * 1430L) >> 3);
                        host.spawnEnemyBullet(319, 39, dir_39, spd_39, bossXFixed_39 + se_39, bossYFixed_39 + te_39);

                        ang_39 = dir_39 + 180 + 10;
                        se_39 = (int) ((((long) Trig.cos(ang_39)) * 1460L) >> 3);
                        te_39 = (int) ((((long) Trig.sin(ang_39)) * 1460L) >> 3);
                        host.spawnEnemyBullet(319, 39, dir_39, spd_39, bossXFixed_39 + se_39, bossYFixed_39 + te_39);

                        ang_39 = dir_39 + 180 + 350;
                        se_39 = (int) ((((long) Trig.cos(ang_39)) * 1460L) >> 3);
                        te_39 = (int) ((((long) Trig.sin(ang_39)) * 1460L) >> 3);
                        host.spawnEnemyBullet(319, 39, dir_39, spd_39, bossXFixed_39 + se_39, bossYFixed_39 + te_39);
                    }
                }

                if (d0_39 > 400 && d0_39 < 600) {
                    int px_39 = host.getPlayerXFixed();
                    int py_39 = host.getPlayerYFixed();

                    if (d0_39 % 24 == 0) {
                        int sx_39 = ((px_39 >> 16) + (d0_39 << 2)) % 194;
                        sx_39 <<= 16;
                        int sy_39 = 16121856;
                        host.spawnEnemyBullet(87, 0, 270, 16 + (d0_39 % 5), sx_39, sy_39);
                    }
                    if (d0_39 % 30 == 0) {
                        int sx_39 = ((px_39 >> 16) + (d0_39 << 1)) % 194;
                        sx_39 <<= 16;
                        int sy_39 = 16646144;
                        host.spawnEnemyBullet(93, 0, 270, 16 + (d0_39 % 5), sx_39, sy_39);
                    }
                    if (d0_39 % 7 == 0) {
                        int t_39 = (((px_39 + py_39) >> 16) + (d0_39 << 2));
                        int baseAng_39 = (t_39 % 360) + 270;

                        int se_39 = (int) ((((long) Trig.cos(baseAng_39)) * 1100L) >> 3);
                        int te_39 = (int) ((((long) Trig.sin(baseAng_39)) * 1100L) >> 3);
                        int sx_39 = 6356992 + se_39;
                        int sy_39 = 7929856 + te_39;

                        int aim_39 = EnemyBulletSystem.arcTan2Deg(py_39 - sy_39, px_39 - sx_39);
                        host.spawnEnemyBullet(521, 9, aim_39, 30, sx_39, sy_39);
                    }
                }

                if (d0_39 == 700) {
                    host.spawnEnemyBullet(-1, 89, 540, 20, 14680064, 3145728);
                    host.spawnEnemyBullet(-1, 89, 360, 20, -1966080, 12713984);
                }
                if (d0_39 == 800) {
                    host.spawnEnemyBullet(-1, 89, 450, 20, 1966080, -1441792);
                    host.spawnEnemyBullet(-1, 89, 270, 20, 10747904, 17301504);
                }
                if (d0_39 == 900) {
                    host.spawnEnemyBullet(-1, 89, 405, 20, -1966080, -1441792);
                    host.spawnEnemyBullet(-1, 89, 495, 20, 14680064, -1441792);
                    host.spawnEnemyBullet(-1, 89, 585, 20, 14680064, 17301504);
                    host.spawnEnemyBullet(-1, 89, 315, 20, -1966080, 17301504);
                }
                if (d0_39 == 1000) {
                    host.spawnEnemyBullet(-1, 89, 450, 20, 6356992, -1441792);
                    host.spawnEnemyBullet(-1, 89, 540, 20, 14680064, 7929856);
                    host.spawnEnemyBullet(-1, 89, 270, 20, 6356992, 17301504);
                    host.spawnEnemyBullet(-1, 89, 360, 20, -1966080, 7929856);
                }

                if (d0_39 >= 1100 && d0_39 <= 1400 && (d0_39 & 0xF) == 0) {
                    int da_39 = (d0_39 - 1100) % 388;
                    if (da_39 > 194) {
                        da_39 = 388 - da_39;
                    }
                    int sx_39 = da_39 << 16;
                    int sy_39 = 524288;
                    for (int n62 = 0; n62 < 16; ++n62) {
                        host.spawnEnemyBullet(603, 74, 450, 16 + (n62 << 3), sx_39, sy_39);
                    }
                }

                if (d0_39 >= 1250 && d0_39 <= 1400) {
                    if ((d0_39 & 0x7) == 0) {
                        int step_39 = 30;
                        int baseX_39 = 0;
                        int baseY_39 = 524288;
                        for (int n63 = 0; n63 < 360; n63 += step_39) {
                            for (int n64 = 0; n64 < 3; ++n64) {
                                int ang_39 = n63 + (n64 << 2) + (d0_39 >> 1);
                                int a2_39 = (ang_39 + 90) % 360;
                                if (a2_39 > 90 && a2_39 < 180) {
                                    host.spawnEnemyBullet(603, 26, ang_39, (3 + n64) << 3, baseX_39, baseY_39);
                                }
                            }
                        }
                    }
                }

                if (d0_39 < 1500) {
                    return;
                }

                if ((d0_39 & 0x7) == 0) {
                    int step_39 = 30;
                    int rotBase_39 = 360 - ((d0_39 << 1) % 360);
                    for (int a = 0; a < 360; a += step_39) {
                        host.spawnEnemyBullet(86, 10, rotBase_39 + a, 32, bossXFixed_39, bossYFixed_39);
                    }
                }

                if ((d0_39 & 0x3) == 0) {
                    for (int a = 0; a < 360; a += 90) {
                        int dir_39 = ((360 - (d0_39 << 2) + a) % 360) + 270;

                        int ang_39 = dir_39 + 180;
                        int se_39 = (int) ((((long) Trig.cos(ang_39)) * 1400L) >> 3);
                        int te_39 = (int) ((((long) Trig.sin(ang_39)) * 1400L) >> 3);

                        host.spawnEnemyBullet(621, 39, dir_39, 20, bossXFixed_39 + se_39, bossYFixed_39 + te_39);
                    }
                    return;
                }

                return;
            }
        }
    }
}
