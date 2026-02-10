package touhou.stage;

import touhou.EnemyBulletSystem;
import touhou.Trig;

final class Stage6BossLogic extends AbstractBossStageLogic {
    private final int[] work = new int[16];

    void tickBossImpl(int enemyIdx) {
        sb(enemyIdx);
    }

    void tickMidbossImpl(int enemyIdx) {
    }

    // Boss(stub)
    void sb(int targetIdx) {
        int[][] enemylist = host.getEnemyList();
        int[] bossx = host.getBossX();
        int[] bossy = host.getBossY();
        int[] bossWrk = host.getBossWrk();
        int bossf = host.getBossF();
        int mt = enemylist[targetIdx][11];

        int idx = 0;
        if (mt == 102) {
            idx = 1;
        } else if (mt == 103) {
            idx = 2;
        }

        if (mt == 102) {
            if (enemylist[targetIdx][5] < -2621440) {
                enemylist[targetIdx][0] = 0;
            }
            if (bossf != 1) {
                return;
            }
        }

        switch (bossf) {
            case 1:
                if (host.getChara() != 0) {
                    bossx[0] = 97;
                    bossy[0] = 56;
                } else if (mt == 102) {
                    int gc = host.getMGCnt();
                    switch (gc) {
                        case 3:
                            bossx[0] = 127;
                            bossy[0] = 56;
                            bossx[1] = 27;
                            bossy[1] = 88;
                            break;
                        case 55:
                            enemylist[targetIdx][14] = 458752;
                            enemylist[targetIdx][15] = 458752;
                            bossx[1] = -50;
                            break;
                    }
                }
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
                    j = 3000;
                    switch (step) {
                        case 10:
                            j = 1800;
                            break;
                        case 13:
                            j = 2100;
                            break;
                        case 16:
                            j = 2600;
                            break;
                        case 19:
                            j = 3000;
                            break;
                        case 22:
                            j = 4200;
                            break;
                        case 37:
                            j = 5000;
                            break;
                    }
                    yb(targetIdx, 8, 3);
                    return;
                }

                switch (host.getBstock()) {
                    case 4:
                        j = 1000;
                        yb(targetIdx, 8, 1);
                        bspellcnt = 49;
                        wb(3);
                        return;
                    case 3:
                        j = 1000;
                        yb(targetIdx, 8, 1);
                        bspellcnt = 49;
                        wb(4);
                        return;
                    case 2:
                        j = 2600;
                        yb(targetIdx, 8, 3);
                        bspellcnt = 119;
                        wb(16);
                        return;
                    case 1:
                        j = 1000;
                        yb(targetIdx, 8, 1);
                        bspellcnt = 49;
                        wb(5);
                        return;
                    case 0:
                        j = 4200;
                        yb(targetIdx, 8, 3);
                        bspellcnt = 119;
                        wb(22);
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

                int d0 = host.getMGCnt() % 180;
                int bossXFixed = enemylist[targetIdx][5];
                int bossYFixed = enemylist[targetIdx][6];
                if (d0 == 10) {
                    if (host.getMGCnt() == 10) {
                        if (host.getPlayerXFixed() < enemylist[targetIdx][5]) {
                            bossx[0] = 127;
                        } else {
                            bossx[0] = 67;
                        }
                    } else if (enemylist[targetIdx][5] > 6356992) {
                        bossx[0] = 67;
                    } else {
                        bossx[0] = 127;
                    }
                    bossy[0] = 68;
                }

                if (d0 >= 30 && d0 <= 90 && (d0 & 0xF) == 0) {
                    int px = host.getPlayerXFixed();
                    int py = host.getPlayerYFixed();
                    int aim = EnemyBulletSystem.arcTan2Deg(py - bossYFixed, px - bossXFixed);
                    if ((d0 & 0x1F) == 0) {
                        aim += 36;
                    }

                    for (int base = 0; base < 360; base += 72) {
                        int dir = aim + base;
                        host.spawnEnemyBullet(319, 0, dir, 48, bossXFixed, bossYFixed);

                        switch (host.getLevel()) {
                            case 0:
                                for (int i = 1; i < 7; i++) {
                                    int spd = 48 - (i << 2);
                                    int off = i * 5;
                                    host.spawnEnemyBullet(319, 0, dir + off, spd, bossXFixed, bossYFixed);
                                    host.spawnEnemyBullet(319, 0, dir - off, spd, bossXFixed, bossYFixed);
                                }
                                break;
                            case 1:
                                for (int i = 1; i < 9; i++) {
                                    int spd = 48 - (i << 2);
                                    int off = i << 2;
                                    host.spawnEnemyBullet(319, 0, dir + off, spd, bossXFixed, bossYFixed);
                                    host.spawnEnemyBullet(319, 0, dir - off, spd, bossXFixed, bossYFixed);
                                }
                                break;
                            case 2:
                                for (int i = 1; i < 12; i++) {
                                    int spd = 48 - (i << 1);
                                    int off = i << 2;
                                    host.spawnEnemyBullet(319, 0, dir + off, spd, bossXFixed, bossYFixed);
                                    host.spawnEnemyBullet(319, 0, dir - off, spd, bossXFixed, bossYFixed);
                                }
                                break;
                            case 3:
                                for (int i = 1; i < 16; i++) {
                                    int spd = 48 - (i << 1);
                                    int off = i << 2;
                                    host.spawnEnemyBullet(319, 0, dir + off, spd, bossXFixed, bossYFixed);
                                    host.spawnEnemyBullet(319, 0, dir - off, spd, bossXFixed, bossYFixed);
                                }
                                break;
                        }
                    }
                }

                if (d0 == 100 || d0 == 140) {
                    if (enemylist[targetIdx][5] > 6356992) {
                        bossx[0] = 20;
                        bossy[0] = 58;
                    } else {
                        bossx[0] = 174;
                        bossy[0] = 58;
                    }
                }

                if (d0 >= 100 && d0 <= 180 && (d0 & 0x11) == 0) {
                    int px = host.getPlayerXFixed();
                    int py = host.getPlayerYFixed();
                    int aim = EnemyBulletSystem.arcTan2Deg(py - bossYFixed, px - bossXFixed);
                    host.spawnEnemyBullet(89, 0, aim, 48, bossXFixed, bossYFixed);
                    switch (host.getLevel()) {
                        case 0:
                            host.spawnEnemyBullet(89, 0, aim + 50, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim - 50, 48, bossXFixed, bossYFixed);
                            break;
                        case 1:
                            host.spawnEnemyBullet(89, 0, aim + 30, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim - 30, 48, bossXFixed, bossYFixed);
                            break;
                        case 2:
                            host.spawnEnemyBullet(89, 0, aim + 50, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim + 25, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim - 25, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim - 50, 48, bossXFixed, bossYFixed);
                            break;
                        case 3:
                            host.spawnEnemyBullet(89, 0, aim + 60, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim + 40, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim + 20, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim - 20, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim - 40, 48, bossXFixed, bossYFixed);
                            host.spawnEnemyBullet(89, 0, aim - 60, 48, bossXFixed, bossYFixed);
                            return;
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

                int d0 = host.getMGCnt() % 180;
                int bossXFixed = enemylist[targetIdx][5];
                int bossYFixed = enemylist[targetIdx][6];
                if (d0 == 10 || d0 == 60) {
                    if (host.getMGCnt() == 10) {
                        if (host.getPlayerXFixed() < enemylist[targetIdx][5]) {
                            bossx[0] = 127;
                        } else {
                            bossx[0] = 67;
                        }
                    } else if (enemylist[targetIdx][5] > 6356992) {
                        bossx[0] = 20;
                    } else {
                        bossx[0] = 174;
                    }
                    bossy[0] = 68;
                }

                if (d0 == 100 || d0 == 140) {
                    if (enemylist[targetIdx][5] > 6356992) {
                        bossx[0] = 20;
                    } else {
                        bossx[0] = 174;
                    }
                    bossy[0] = 58;
                }

                if (d0 >= 10 && d0 <= 90 && (d0 & 0x3) == 0) {
                    int base = d0 << 5;
                    host.spawnEnemyBullet(319, 0, base, 40, bossXFixed, bossYFixed);
                    int lvl = host.getLevel();
                    int maxI;
                    if (lvl == 0) {
                        maxI = 3;
                    } else if (lvl == 1) {
                        maxI = 4;
                    } else if (lvl == 2) {
                        maxI = 5;
                    } else {
                        maxI = 7;
                    }
                    for (int i = 1; i <= maxI; i++) {
                        int angOff = (lvl == 0) ? (i * 5) : (i << 2);
                        int spd = (lvl <= 1) ? (40 - (i << 2)) : (40 - (i << 1));
                        host.spawnEnemyBullet(319, 26, base + angOff, spd, bossXFixed, bossYFixed);
                        host.spawnEnemyBullet(319, 26, base - angOff, spd, bossXFixed, bossYFixed);
                    }
                }

                if (d0 >= 110 && d0 <= 180 && (d0 & 0x3) == 0) {
                    int base = 360 - ((d0 << 5) % 360);
                    host.spawnEnemyBullet(319, 0, base, 40, bossXFixed, bossYFixed);
                    int lvl = host.getLevel();
                    int maxI;
                    if (lvl == 0) {
                        maxI = 3;
                    } else if (lvl == 1) {
                        maxI = 4;
                    } else if (lvl == 2) {
                        maxI = 5;
                    } else {
                        maxI = 7;
                    }
                    for (int i = 1; i <= maxI; i++) {
                        int angOff = (lvl == 0) ? (i * 5) : (i << 2);
                        int spd = (lvl <= 1) ? (40 - (i << 2)) : (40 - (i << 1));
                        host.spawnEnemyBullet(319, 26, base + angOff, spd, bossXFixed, bossYFixed);
                        host.spawnEnemyBullet(319, 26, base - angOff, spd, bossXFixed, bossYFixed);
                    }
                }

                if (d0 % 60 == 0) {
                    int step;
                    switch (host.getLevel()) {
                        case 0:
                            step = 40;
                            break;
                        case 1:
                            step = 30;
                            break;
                        case 2:
                            step = 24;
                            break;
                        default:
                            step = 20;
                            break;
                    }
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                    for (int a = -120; a <= 120; a += step) {
                        host.spawnEnemyBullet(639, 7, aim + a, 24, bossXFixed, bossYFixed);
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

                int d0_5 = host.getMGCnt() % 400;
                int bossXFixed = enemylist[targetIdx][5];
                int bossYFixed = enemylist[targetIdx][6];

                int wob = 0;
                if (d0_5 > 50) {
                    wob = (((d0_5 - 50) >> 1) + 60) % 80;
                    if (wob > 40) {
                        wob = 80 - wob;
                    }
                    wob -= 20;
                }
                int baseAng = 450 + wob;

                if (d0_5 > 330 && d0_5 < 360 && (d0_5 & 0x3) == 0) {
                    if (enemylist[targetIdx][10] == 0) {
                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                        enemylist[targetIdx][10] = aim;
                    }

                    int mt92 = 22;
                    if ((d0_5 & 0x7) == 0) {
                        mt92 = 21;
                    }

                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 12;
                            break;
                        case 1:
                            div = 18;
                            break;
                        case 2:
                            div = 20;
                            break;
                        default:
                            div = 24;
                            break;
                    }
                    int step = 360 / div;
                    int start = 0;
                    if ((enemylist[targetIdx][9] & 0x1) == 0) {
                        start = step >> 1;
                    }
                    int base = enemylist[targetIdx][10];
                    for (int a = start; a < 360; a += step) {
                        host.spawnEnemyBullet(92, mt92, base + a, 60, bossXFixed, bossYFixed);
                    }
                }

                if (d0_5 >= 300) {
                    return;
                }

                if ((d0_5 & 0x1) == 0) {
                    host.spawnEnemyBullet(603, 58, baseAng - 60, 80, bossXFixed, bossYFixed);
                    host.spawnEnemyBullet(603, 58, baseAng + 60, 80, bossXFixed, bossYFixed);
                }

                if (d0_5 <= 40) {
                    return;
                }

                if ((d0_5 & 0x3F) == 0) {
                    int step;
                    int spread;
                    switch (host.getLevel()) {
                        case 0:
                            step = 30;
                            spread = 1;
                            break;
                        case 1:
                            step = 20;
                            spread = 2;
                            break;
                        case 2:
                            step = 16;
                            spread = 3;
                            break;
                        default:
                            step = 14;
                            spread = 4;
                            break;
                    }
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                    for (int a = -(step * spread); a <= step * spread; a += step) {
                        host.spawnEnemyBullet(83, 0, aim + a, 16, bossXFixed, bossYFixed);
                        host.spawnEnemyBullet(89, 0, aim + a, 24, bossXFixed, bossYFixed);
                        host.spawnEnemyBullet(95, 0, aim + a, 32, bossXFixed, bossYFixed);
                    }
                }

                if ((d0_5 & 0x3F) == 0x1F) {
                    int step;
                    int spread;
                    switch (host.getLevel()) {
                        case 0:
                            step = 26;
                            spread = 2;
                            break;
                        case 1:
                            step = 20;
                            spread = 3;
                            break;
                        case 2:
                            step = 14;
                            spread = 4;
                            break;
                        default:
                            step = 10;
                            spread = 5;
                            break;
                    }
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                    for (int a = -(step * spread); a <= step * spread; a += step) {
                        host.spawnEnemyBullet(81, 0, aim + a, 30, bossXFixed, bossYFixed);
                        host.spawnEnemyBullet(87, 0, aim + a, 40, bossXFixed, bossYFixed);
                        host.spawnEnemyBullet(93, 0, aim + a, 50, bossXFixed, bossYFixed);
                    }
                }
                return;
            }

            case 10:
                bossx[0] = 97;
                bossy[0] = 121;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 78 + host.getLevel();
                    wb(11);
                    return;
                }
                return;

            case 11:
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
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(12);
                    j = 1800;
                    yb(targetIdx, 8, 2);
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

                host.setBbaria(0);
                batkf = true;

                int d0_12 = host.getMGCnt();
                if (d0_12 == 0) {
                    bossWrk[5] = 0;
                }

                int dc;
                int d9 = 1;
                switch (host.getLevel()) {
                    case 0:
                        dc = 127;
                        break;
                    case 1:
                        dc = 63;
                        break;
                    case 2:
                        dc = 31;
                        break;
                    case 3:
                        dc = 15;
                        break;
                    default:
                        dc = 63;
                        break;
                }

                if (d9 <= 0) {
                    d9 = 1;
                }

                if ( (d0_12 & dc) == 0) {
                    int d8 = d0_12 >> 4;
                    if (d8 > 30) {
                        d8 = 30;
                    }
                    d8 += 30;

                    for (int j = 0; j < d9; j++) {
                        int da = d0_12 >> 3;
                        int mix = (host.getPlayerXFixed() + host.getPlayerYFixed()) >> 16;
                        int db = ((da << 6) + (d0_12 << 4) + mix + (646 * j) / d9) % 646;

                        int sx;
                        int sy;
                        if (db < 194) {
                            sx = db << 16;
                            sy = 524288;
                        } else if (db < 420) {
                            sx = 12713984;
                            sy = (8 + (db - 194)) << 16;
                        } else {
                            sx = 0;
                            sy = (234 - (db - 420)) << 16;
                        }

                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - sy, host.getPlayerXFixed() - sx);
                        host.spawnEnemyBullet(89, 66, aim, d8, sx, sy);
                    }
                }

                int d8 = 32 + bossWrk[5];
                if (d8 > 48) {
                    d8 = 48;
                }
                if (d0_12 >= 30) {
                    int d1 = enemylist[targetIdx][5];
                    int d2 = enemylist[targetIdx][6];
                    int t0 = d0_12 - 30;

                    int t8 = t0 & 0xFF;
                    if (t8 == 0 || t8 == 2 || t8 == 5 || t8 == 8 || t8 == 11) {
                        int ang = bossWrk[5] * 72;
                        int grp = bossWrk[5] / 5;
                        if ( (grp & 1) != 0) {
                            ang += 36;
                        }
                        host.spawnEnemyBullet(89, 79 + (grp & 1), ang, d8, d1, d2);
                        bossWrk[5]++;
                    }

                    if (host.getLevel() >= 1 && (t8 == 0x7F)) {
                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - d2, host.getPlayerXFixed() - d1);
                        int mtSel = 79 + (bossWrk[5] & 1);
                        for (int k = 0; k < 5; k++) {
                            int ang = aim + k * 72 + 180;
                            host.spawnEnemyBullet(89, mtSel, ang, d8, d1, d2);
                        }
                    }

                    if (host.getLevel() >= 2 && ((t0 & 0x7F) == 0x2F)) {
                        int dirSel = (t0 >> 5) & 1;
                        for (int l = 0; l < 8; l++) {
                            for (int n3 = 0; n3 < 5; n3++) {
                                int ang = l * 9;
                                if (dirSel == 0) {
                                    ang = 360 - l * 9;
                                }
                                ang += n3 * 72;
                                host.spawnEnemyBullet(83, 0, ang, d8 - (l << 1), d1, d2);
                            }
                        }
                    }
                }
                return;

            case 13:
                bossx[0] = 97;
                bossy[0] = 68;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 82 + host.getLevel();
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
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(15);
                    j = 2100;
                    yb(targetIdx, 8, 2);
                    return;
                }
                return;

            case 15: {
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int gc = host.getMGCnt();
                if (gc < 10) {
                    return;
                }
                int d0_15 = gc - 10;

                if ((d0_15 & 0x7F) < 63) {
                    int cnt;
                    int mask;
                    switch (host.getLevel()) {
                        case 0:
                            cnt = 2;
                            mask = 7;
                            break;
                        case 1:
                            cnt = 2;
                            mask = 3;
                            break;
                        case 2:
                            cnt = 3;
                            mask = 3;
                            break;
                        default:
                            cnt = 4;
                            mask = 3;
                            break;
                    }

                    if ((d0_15 & mask) == 0) {
                        int px = host.getPlayerXFixed();
                        int py = host.getPlayerYFixed();
                        for (int i = 0; i < cnt; i++) {
                            int dir = (d0_15 + (d0_15 << 4)) % 360;
                            dir += (360 * i) / cnt;
                            int dist = 240 + (((d0_15 << 4) + (i << 8)) % 600);
                            int se = (int) ((((long) Trig.cos(dir)) * (long) dist) >> 3);
                            int te = (int) ((((long) Trig.sin(dir)) * (long) dist) >> 3);
                            host.spawnEnemyBullet(83, 60, dir, 24, px + se, py + te);

                            dir = 360 - ((d0_15 + (d0_15 << 5)) % 360);
                            dir += (360 * i) / cnt;
                            dist = 240 + (((d0_15 << 4) + (i << 8)) % 600);
                            se = (int) ((((long) Trig.cos(dir)) * (long) dist) >> 3);
                            te = (int) ((((long) Trig.sin(dir)) * (long) dist) >> 3);
                            host.spawnEnemyBullet(83, 60, dir, 24, px + se, py + te);
                        }
                    }
                }

                int div;
                switch (host.getLevel()) {
                    case 0:
                        div = 3;
                        break;
                    case 1:
                    case 2:
                        div = 4;
                        break;
                    default:
                        div = 5;
                        break;
                }
                if ((d0_15 & 0x3) == 0) {
                    int d3 = (d0_15 << 3) - 194;
                    for (int i = 0; i < div; i++) {
                        int d4 = d3 + (i * 194) / div;
                        if (d4 >= 0) {
                            int d5 = d3 % 388;
                            if (d5 > 194) {
                                d5 = 388 - d5;
                            }
                            d5 = 194 - d5;
                            host.spawnEnemyBullet(82, 0, 450, 20, d5 << 16, -131072);
                        }
                    }
                }
                return;
            }

            case 16:
                bossx[0] = 97;
                bossy[0] = 121;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[targetIdx][14] = 65536;
                    enemylist[targetIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 86 + host.getLevel();
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
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][14] = 327680;
                    enemylist[targetIdx][15] = 327680;
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
                    bc();
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                host.setBbaria(0);
                batkf = true;

                int gc = host.getMGCnt();
                if (gc == 0) {
                    int d5_18 = 6;
                    switch (host.getLevel()) {
                        case 0:
                            d5_18 = 6;
                            break;
                        case 1:
                            d5_18 = 8;
                            break;
                        case 2:
                            d5_18 = 8;
                            break;
                        case 3:
                            d5_18 = 10;
                            break;
                    }
                    for (int i = 0; i < 10; ++i) {
                        if (i < d5_18) {
                            bossWrk[8 + i] = host.stageFb(20 + (i & 0x1), 0, 500, 0, 0, 0, 0, enemylist[targetIdx][5], enemylist[targetIdx][6]);
                        } else {
                            bossWrk[8 + i] = 0;
                        }
                    }
                }
                if (gc == 20) {
                    bossy[0] = 58;
                }

                int d1_18 = gc << 5;
                if (d1_18 > 900) {
                    d1_18 = 900;
                }
                int d2_18 = (gc - 20) << 1;
                if (gc < 20) {
                    d2_18 = 0;
                }

                for (int i = 0; i < 10; ++i) {
                    work[i] = -1;
                }
                switch (host.getLevel()) {
                    case 0:
                        for (int i = 0; i < 6; ++i) {
                            work[i] = 120 * i;
                        }
                        break;
                    case 1:
                    case 2:
                        for (int i = 0; i < 8; ++i) {
                            work[i] = 90 * i + 45;
                        }
                        break;
                    case 3:
                        for (int i = 0; i < 10; ++i) {
                            work[i] = 72 * i;
                        }
                        break;
                }

                int centerX_18 = 97 << 16;
                int centerY_18 = 121 << 16;
                for (int i = 0; i < 10; ++i) {
                    if (work[i] == -1) {
                        continue;
                    }
                    int si = bossWrk[8 + i];
                    if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                        continue;
                    }

                    int dir;
                    if ((i & 0x1) == 0) {
                        dir = (work[i] + 270 + d2_18) % 360;
                    } else {
                        dir = (work[i] + 270 - d2_18 + 360) % 360;
                    }
                    int se = (int) ((((long) Trig.cos(dir)) * (long) d1_18) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) d1_18) >> 3);
                    enemylist[si][5] = centerX_18 + se;
                    enemylist[si][6] = centerY_18 + te;
                }

                int d0_18 = gc + 1;
                if (gc > 40) {
                    for (int i = 0; i < 10; i++) {
                        if (work[i] == -1) {
                            continue;
                        }
                        int si = bossWrk[8 + i];
                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                            continue;
                        }
                        if (((d0_18 + (i * 5)) & 0x28) != 0) {
                            continue;
                        }

                        int dir;
                        if ((i & 0x1) == 0) {
                            dir = (work[i] + 270 + d2_18) % 360;
                        } else {
                            dir = (work[i] + 270 - d2_18 + 360) % 360;
                        }
                        host.spawnEnemyBullet(83, 0, dir + 180, 20, enemylist[si][5], enemylist[si][6]);
                    }
                }

                if (host.getLevel() >= 2 && gc > 60) {
                    int xBase = (((d0_18 << 4) % 194) << 16);
                    int yBase = ((8 + ((d0_18 << 4) % 226)) << 16);

                    if (((d0_18 + 0) & 0x7) == 0) {
                        for (int n = -2; n <= 2; n++) {
                            host.spawnEnemyBullet(82, 0, 450, 20, xBase + (n << 3), 524288);
                        }
                        for (int n = -2; n <= 2; n++) {
                            host.spawnEnemyBullet(82, 0, 360, 20, 0, yBase + (n << 3));
                        }
                    }
                    if (((d0_18 + 2) & 0x7) == 0) {
                        for (int n = -2; n <= 2; n++) {
                            host.spawnEnemyBullet(82, 0, 270, 20, xBase + (n << 3), 15335424);
                        }
                        for (int n = -2; n <= 2; n++) {
                            host.spawnEnemyBullet(82, 0, 540, 20, 12713984, yBase + (n << 3));
                        }
                    }
                }

                if (gc > 100 && d0_18 % 90 == 0) {
                    if (host.getPlayerXFixed() > enemylist[targetIdx][5]) {
                        bossx[0] += 30;
                    } else {
                        bossx[0] -= 30;
                    }

                    if (bossx[0] < 30) {
                        bossx[0] += 50;
                    }
                    if (bossx[0] > 164) {
                        bossx[0] -= 50;
                    }

                    if (bossy[0] < 60) {
                        bossy[0] = 80;
                        return;
                    }
                    if (bossy[0] > 60) {
                        bossy[0] = 60;
                        return;
                    }
                    bossy[0] = 40;
                    return;
                }
                return;
            }

            case 19:
                bossx[0] = 97;
                bossy[0] = 64;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[targetIdx][14] = 65536;
                    enemylist[targetIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 90 + host.getLevel();
                    wb(20);
                    return;
                }
                return;

            case 20:
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
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][14] = 393216;
                    enemylist[targetIdx][15] = 393216;
                    enemylist[targetIdx][3] = 0;
                    enemylist[targetIdx][10] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(21);
                    j = 3000;
                    yb(targetIdx, 8, 2);

                    for (int i = 0; i < 6; ++i) {
                        bossWrk[8 + i] = host.stageFb(22, 0, 65535, 0, 50, 0, 0, enemylist[targetIdx][5], enemylist[targetIdx][6]);
                        bossWrk[18 + i] = 60 * i;
                    }
                    for (int i = 6; i < 10; ++i) {
                        bossWrk[8 + i] = 0;
                        bossWrk[18 + i] = 0;
                    }
                    bossWrk[24] = 0;
                    bossWrk[25] = 0;
                    bossWrk[26] = 0;
                    return;
                }
                return;

            case 21:
                if (bspellcnt < 0) {
                    j = 0;
                    bc();
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d0_21 = host.getMGCnt() + 1;
                int cnt21 = 6;
                if (o[2] > 0) {
                    cnt21 = 1 + (o[0] * 6) / o[2];
                }
                if (cnt21 > 6) {
                    cnt21 = 6;
                }
                if (cnt21 >= 6 && bspellcnt < 30) {
                    cnt21 = 5;
                }
                if (cnt21 >= 5 && bspellcnt < 25) {
                    cnt21 = 4;
                }
                if (cnt21 >= 4 && bspellcnt < 20) {
                    cnt21 = 3;
                }
                if (cnt21 >= 3 && bspellcnt < 15) {
                    cnt21 = 2;
                }
                if (cnt21 >= 2 && bspellcnt < 10) {
                    cnt21 = 1;
                }
                bossWrk[6] = cnt21;
                if (cnt21 != bossWrk[26]) {
                    bossWrk[26] = cnt21;
                    bossWrk[7] = 15;
                }

                if (cnt21 > 5) {
                    bossWrk[24] += 32;
                    if (bossWrk[24] > 400) {
                        bossWrk[24] = 400;
                    }
                } else {
                    bossWrk[24] -= 4;
                    int minR = (50 - (cnt21 << 1)) << 3;
                    if (bossWrk[24] < minR) {
                        bossWrk[24] = minR;
                    }
                }

                if (host.getMGCnt() > 10) {
                    int rotDelta = 11 - cnt21;
                    if (bossWrk[0] <= 0) {
                        rotDelta *= -1;
                    }
                    bossWrk[25] = (bossWrk[25] + 360 + rotDelta) % 360;
                }

                for (int i = 0; i < 6; ++i) {
                    if (i < cnt21) {
                        int targetAng = (360 / cnt21) * i;
                        if (bossWrk[18 + i] > targetAng) {
                            --bossWrk[18 + i];
                        }
                        if (bossWrk[18 + i] < targetAng) {
                            ++bossWrk[18 + i];
                        }

                        int si = bossWrk[8 + i];
                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                            continue;
                        }

                        int dir = (bossWrk[18 + i] + 270 + bossWrk[25]) % 360;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) bossWrk[24]) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) bossWrk[24]) >> 3);
                        enemylist[si][5] = enemylist[targetIdx][5] + se;
                        enemylist[si][6] = enemylist[targetIdx][6] + te;
                    } else {
                        int si = bossWrk[8 + i];
                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                            continue;
                        }
                        enemylist[si][0] = 0;
                        host.spawnStageEffect(22, enemylist[si][5], enemylist[si][6]);
                    }
                }

                int div21;
                switch (host.getLevel()) {
                    case 0:
                        div21 = 8;
                        break;
                    case 1:
                        div21 = 6;
                        break;
                    case 2:
                        div21 = 5;
                        break;
                    default:
                        div21 = 4;
                        break;
                }
                if (bossWrk[7] == 0 && (d0_21 % div21) == 0) {
                    for (int i = 0; i < 6; i++) {
                        if (i >= cnt21) {
                            continue;
                        }
                        int si = bossWrk[8 + i];
                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                            continue;
                        }

                        int sx = enemylist[si][5];
                        int sy = enemylist[si][6];
                        int dir = (bossWrk[18 + i] + 270 + bossWrk[25]) % 360;

                        switch (cnt21) {
                            case 6:
                                host.spawnEnemyBullet(86, 0, dir, 32, sx, sy);
                                break;
                            case 5:
                                host.spawnEnemyBullet(85, 0, dir, 32, sx, sy);
                                host.spawnEnemyBullet(85, 0, dir + 180, 32, sx, sy);
                                break;
                            case 4: {
                                int step;
                                if (host.getLevel() <= 1) {
                                    step = 120;
                                } else {
                                    step = 90;
                                }
                                int base = bossWrk[25] + 45;
                                for (int a = 0; a < 360; a += step) {
                                    host.spawnEnemyBullet(84, 0, base + a, 32, sx, sy);
                                }
                                break;
                            }
                            case 3: {
                                int step;
                                switch (host.getLevel()) {
                                    case 0:
                                        step = 20;
                                        break;
                                    case 1:
                                        step = 18;
                                        break;
                                    case 2:
                                        step = 15;
                                        break;
                                    default:
                                        step = 10;
                                        break;
                                }
                                for (int a = -60; a <= 60; a += step) {
                                    host.spawnEnemyBullet(271, 0, dir + a, 32, sx, sy);
                                }
                                break;
                            }
                            case 2: {
                                int step;
                                switch (host.getLevel()) {
                                    case 0:
                                        step = 20;
                                        break;
                                    case 1:
                                        step = 18;
                                        break;
                                    case 2:
                                        step = 15;
                                        break;
                                    default:
                                        step = 10;
                                        break;
                                }
                                for (int a = -90; a <= 90; a += step) {
                                    host.spawnEnemyBullet(255, 0, dir + a, 32, sx, sy);
                                }
                                break;
                            }
                            case 1: {
                                int base = (d0_21 << 3) % 360;
                                int step;
                                switch (host.getLevel()) {
                                    case 0:
                                        step = 24;
                                        break;
                                    case 1:
                                        step = 20;
                                        break;
                                    case 2:
                                        step = 18;
                                        break;
                                    default:
                                        step = 15;
                                        break;
                                }
                                for (int a = 0; a < 360; a += step) {
                                    host.spawnEnemyBullet(239, 0, base + a, 32, sx, sy);
                                }
                                break;
                            }
                        }
                    }
                }

                int d21 = host.getMGCnt() + 1;
                if (d21 % 60 == 0) {
                    switch ((d21 / 60) % 5) {
                        case 0:
                            bossx[0] = 37;
                            bossy[0] = 54;
                            break;
                        case 1:
                            bossx[0] = 157;
                            bossy[0] = 54;
                            break;
                        case 2:
                            bossx[0] = 67;
                            bossy[0] = 94;
                            break;
                        case 3:
                            bossx[0] = 97;
                            bossy[0] = 44;
                            break;
                        case 4:
                            bossx[0] = 127;
                            bossy[0] = 94;
                            break;
                    }
                }

                if (bossWrk[7] > 0) {
                    --bossWrk[7];
                }
                return;

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
                    spellId = 114;
                    wb(38);
                    return;
                }
                return;

            case 38:
                bspellcnt = 60;
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
                        enemylist[targetIdx][3] = 3;
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
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int d0_39 = host.getMGCnt() - 20;
                if (d0_39 <= 0) {
                    return;
                }

                int da_39 = d0_39 % 250;
                int db_39 = (d0_39 / 250) % 5;

                int bossXFixed_39 = enemylist[targetIdx][5];
                int bossYFixed_39 = enemylist[targetIdx][6];

                if (da_39 == 0 || da_39 == 200) {
                    enemylist[targetIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed_39, host.getPlayerXFixed() - bossXFixed_39);
                }

                if (da_39 < 50) {
                    if (d0_39 % 3 == 0) {
                        int d6_39 = 90;
                        int d3_39 = (d0_39 << 3) % 360;
                        for (int a = 0; a < 360; a += d6_39) {
                            int ang = (enemylist[targetIdx][10] + a + d3_39 + 360) % 360;
                            host.spawnEnemyBullet(89, 10, ang, 28, bossXFixed_39, bossYFixed_39);

                            ang = (enemylist[targetIdx][10] + a - d3_39 + 360) % 360;
                            host.spawnEnemyBullet(89, 11, ang, 28, bossXFixed_39, bossYFixed_39);
                        }
                    }
                } else if (da_39 > 70 && da_39 < 150) {
                    if (d0_39 % 12 == 0) {
                        int[] work_39 = work;
                        switch (db_39) {
                            case 0:
                                work_39[0] = 0;
                                work_39[1] = 524288;
                                work_39[2] = 393216;
                                work_39[3] = 327680;
                                work_39[4] = 12713984;
                                work_39[5] = 524288;
                                work_39[6] = -393216;
                                work_39[7] = 327680;
                                break;
                            case 1:
                                work_39[0] = 0;
                                work_39[1] = 15335424;
                                work_39[2] = 262144;
                                work_39[3] = -327680;
                                work_39[4] = 12713984;
                                work_39[5] = 524288;
                                work_39[6] = -262144;
                                work_39[7] = 327680;
                                break;
                            case 2:
                                work_39[0] = 10092544;
                                work_39[1] = 524288;
                                work_39[2] = -196608;
                                work_39[3] = 327680;
                                work_39[4] = 12713984;
                                work_39[5] = 1835008;
                                work_39[6] = -196608;
                                work_39[7] = 327680;
                                break;
                            case 3:
                                work_39[0] = 2621440;
                                work_39[1] = 524288;
                                work_39[2] = 196608;
                                work_39[3] = 327680;
                                work_39[4] = 0;
                                work_39[5] = 1835008;
                                work_39[6] = 196608;
                                work_39[7] = 327680;
                                break;
                            case 4:
                                work_39[0] = 0;
                                work_39[1] = 524288;
                                work_39[2] = 262144;
                                work_39[3] = 327680;
                                work_39[4] = 12713984;
                                work_39[5] = 15335424;
                                work_39[6] = -262144;
                                work_39[7] = -327680;
                                break;
                        }

                        int aim_39 = enemylist[targetIdx][10];
                        for (int n63 = 0; n63 < 2; ++n63) {
                            int sx_39 = work_39[0 + (n63 << 2)] + work_39[2 + (n63 << 2)] * (da_39 - 70);
                            int sy_39 = work_39[1 + (n63 << 2)] + work_39[3 + (n63 << 2)] * (da_39 - 70);

                            int step_39 = 45;
                            for (int a = 0; a < 360; a += step_39) {
                                host.spawnEnemyBullet(83, 7, aim_39 + a, 24, sx_39, sy_39);
                            }
                            step_39 = 22;
                            for (int a = 0; a < 360; a += step_39) {
                                host.spawnEnemyBullet(585, 0, aim_39 + a + (step_39 >> 1), 24, sx_39, sy_39);
                            }
                            step_39 = 30;
                            for (int a = 0; a < 360; a += step_39) {
                                host.spawnEnemyBullet(81, 2, aim_39 + a, 32, sx_39, sy_39);
                            }
                            step_39 = 11;
                            for (int a = 0; a < 360; a += step_39) {
                                host.spawnEnemyBullet(83, 7, aim_39 + a, 16, sx_39, sy_39);
                            }
                        }
                    }
                } else if (da_39 == 200) {
                    int cx_39 = 6356992;
                    int cy_39 = 7929856;
                    int aim_39 = enemylist[targetIdx][10];

                    int step_39 = 45;
                    for (int a = 0; a < 360; a += step_39) {
                        host.spawnEnemyBullet(83, 7, aim_39 + a, 24, cx_39, cy_39);
                    }
                    step_39 = 22;
                    for (int a = 0; a < 360; a += step_39) {
                        int ang = aim_39 + a + (step_39 >> 1);
                        host.spawnEnemyBullet(585, 0, ang, 24, cx_39, cy_39);
                        host.spawnEnemyBullet(585, 0, ang, 18, cx_39, cy_39);

                        ang = aim_39 + a;
                        host.spawnEnemyBullet(585, 0, ang, 21, cx_39, cy_39);
                        host.spawnEnemyBullet(585, 0, ang, 15, cx_39, cy_39);
                    }
                    step_39 = 30;
                    for (int a = 0; a < 360; a += step_39) {
                        host.spawnEnemyBullet(81, 2, aim_39 + a, 32, cx_39, cy_39);
                    }
                    step_39 = 11;
                    for (int a = 0; a < 360; a += step_39) {
                        host.spawnEnemyBullet(83, 7, aim_39 + a, 16, cx_39, cy_39);
                    }

                    step_39 = 20;
                    int d9_39 = 0;
                    int d5_39 = 0;
                    for (int a = 0; a < 360; a += step_39) {
                        host.spawnEnemyBullet(85, 28, aim_39 + a, d9_39 - 16, cx_39, cy_39);
                        d9_39 = (d9_39 + 2) % 7;
                        host.spawnEnemyBullet(85, 28, aim_39 + a + (step_39 >> 1), d9_39 - 32, cx_39, cy_39);
                        d5_39 = (d5_39 + 9) % 7;
                    }

                    step_39 = 18;
                    d9_39 = 0;
                    for (int a = 0; a < 360; a += step_39) {
                        host.spawnEnemyBullet(86, 45, aim_39 + a + (step_39 >> 1), d9_39 << 2, cx_39, cy_39);
                        d9_39 = (d9_39 + 9) % 7;
                    }
                }

                if (da_39 == 200) {
                    bossx[0] = 97;
                    bossy[0] = 121;
                    switch (db_39) {
                        case 0:
                            bossx[0] -= 30;
                            break;
                        case 1:
                            bossx[0] += 60;
                            bossy[0] -= 40;
                            break;
                        case 2:
                            bossx[0] -= 60;
                            bossy[0] -= 40;
                            break;
                        case 3:
                            bossx[0] += 30;
                            break;
                        case 4:
                            bossy[0] -= 60;
                            break;
                    }
                }

                if (da_39 > 200 && da_39 < 230) {
                    int tx = bossx[0] << 16;
                    int ty = bossy[0] << 16;
                    int dx = tx - bossXFixed_39;
                    int dy = ty - bossYFixed_39;
                    int ang = EnemyBulletSystem.arcTan2Deg(dy, dx);
                    int sp = 260 - da_39;

                    int se = (int) ((((long) Trig.cos(ang)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(ang)) * (long) sp) >> 3);
                    if (se < 0) {
                        se = -se;
                    }
                    if (te < 0) {
                        te = -te;
                    }
                    if (se < 65536) {
                        se = 65536;
                    }
                    if (te < 65536) {
                        te = 65536;
                    }
                    enemylist[targetIdx][14] = se;
                    enemylist[targetIdx][15] = te;
                }
                return;
            }

            case 22:
                bossx[idx] = 97;
                bossy[idx] = 91;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[targetIdx][14] = 65536;
                    enemylist[targetIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 94 + host.getLevel();
                    wb(23);
                    return;
                }
                return;

            case 23:
                bossy[idx] = 71;
                bspellcnt = 120;
                switch (host.getMGCnt()) {
                    case 1:
                    case 25:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[targetIdx][3] = 2;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[targetIdx][14] = 262144;
                    enemylist[targetIdx][15] = 262144;
                    enemylist[targetIdx][3] = 0;
                    enemylist[targetIdx][10] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(24);
                    host.getBossWrk()[6] = 0;
                    host.getBossWrk()[7] = 0;
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

                int[] bossWrk24 = host.getBossWrk();
                bossWrk24[6] = 0;
                if (bspellcnt < 60 || o[1] < (o[2] * 45 / 100)) {
                    bossWrk24[6] = 1;
                }
                if (bspellcnt < 30 || o[1] < (o[2] * 15 / 100)) {
                    bossWrk24[6] = 2;
                } else {
                    bossWrk24[7] = 30;
                }

                int bossXFixed = enemylist[targetIdx][5];
                int bossYFixed = enemylist[targetIdx][6];
                int mt24;

                if (bossWrk24[6] == 0) {
                    int d0_24 = host.getMGCnt() % 600;

                    if (d0_24 >= 20 && d0_24 < 100) {
                        int da = d0_24 - 20;
                        if (da == 0 || da == 40) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                            int div;
                            switch (host.getLevel()) {
                                case 0:
                                    div = 6;
                                    break;
                                case 1:
                                    div = 8;
                                    break;
                                case 2:
                                    div = 12;
                                    break;
                                default:
                                    div = 15;
                                    break;
                            }
                            int step = 360 / div;
                            for (int a = 0; a < 360; a += step) {
                                host.spawnEnemyBullet(93, 7, enemylist[targetIdx][10] + a, 60, bossXFixed, bossYFixed);
                            }
                        }
                        if ((da > 5 && da < 40) || (da > 45 && da < 80)) {
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
                                default:
                                    div = 18;
                                    break;
                            }
                            int step = 360 / div;
                            if ((da & 0x3) == 0) {
                                mt24 = 6;
                                if (da > 50) {
                                    mt24 = 8;
                                }
                                int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                                enemylist[targetIdx][10] = aim;
                                for (int a = 0; a < 360; a += step) {
                                    int ang = enemylist[targetIdx][10] + a;
                                    if (((da >> 2) & 0x1) != 0) {
                                        ang += step >> 1;
                                    }
                                    host.spawnEnemyBullet(239, mt24, ang, 80, bossXFixed, bossYFixed);
                                }
                            }
                        }
                    }

                    if (d0_24 >= 120 && d0_24 < 200) {
                        int da = d0_24 - 120;
                        if (da == 0 || da == 40) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                            int div;
                            switch (host.getLevel()) {
                                case 0:
                                    div = 6;
                                    break;
                                case 1:
                                    div = 8;
                                    break;
                                case 2:
                                    div = 12;
                                    break;
                                default:
                                    div = 15;
                                    break;
                            }
                            int step = 360 / div;
                            mt24 = 64 - (da / 40);
                            for (int a = 0; a < 360; a += step) {
                                host.spawnEnemyBullet(94, mt24, enemylist[targetIdx][10] + a, 80, bossXFixed, bossYFixed);
                            }
                        }
                    }

                    if (d0_24 >= 220 && d0_24 < 300) {
                        int da = d0_24 - 220;
                        if (da == 0) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                        }
                        if (da <= 63) {
                            int div;
                            switch (host.getLevel()) {
                                case 0:
                                    div = 2;
                                    break;
                                case 1:
                                    div = 3;
                                    break;
                                case 2:
                                    div = 4;
                                    break;
                                default:
                                    div = 5;
                                    break;
                            }
                            int step = 360 / div;
                            if ((da & 0x1) == 0) {
                                int cnt = 12;
                                if ((da & 0x1F) < 15) {
                                    for (int a = 0; a < 360; a += step) {
                                        int base = enemylist[targetIdx][10] + a - 90 + (da * (14 - host.getLevel()));
                                        for (int n = 0; n < cnt; n++) {
                                            host.spawnEnemyBullet(621, 61, base - n, 24 + (n << 3) + (n << 1), bossXFixed, bossYFixed);
                                        }
                                    }
                                } else {
                                    for (int a = 0; a < 360; a += step) {
                                        int base = enemylist[targetIdx][10] + a + 90 - (da * (14 - host.getLevel()));
                                        for (int n = 0; n < cnt; n++) {
                                            host.spawnEnemyBullet(621, 61, base + n, 24 + (n << 3) + (n << 1), bossXFixed, bossYFixed);
                                        }
                                    }
                                }
                            }
                        } else {
                            int div;
                            switch (host.getLevel()) {
                                case 0:
                                    div = 12;
                                    break;
                                case 1:
                                    div = 15;
                                    break;
                                case 2:
                                    div = 20;
                                    break;
                                default:
                                    div = 24;
                                    break;
                            }
                            int step = 360 / div;
                            if ((da & 0x3) == 0) {
                                int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                                enemylist[targetIdx][10] = aim;
                                int ang = enemylist[targetIdx][10] + (da << 1);
                                if (((da >> 2) & 0x1) != 0) {
                                    ang += step >> 1;
                                }
                                for (int a = 0; a < 360; a += step) {
                                    host.spawnEnemyBullet(83, 28, ang + a, -24, bossXFixed, bossYFixed);
                                }
                            }
                        }
                    }

                    if (d0_24 >= 320 && d0_24 < 400) {
                        int da = d0_24 - 320;
                        if (da < 50) {
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
                                default:
                                    div = 18;
                                    break;
                            }
                            int step = 360 / div;
                            if ((da & 0x3) == 0) {
                                int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                                enemylist[targetIdx][10] = aim;
                                for (int a = 0; a < 360; a += step) {
                                    int ang = enemylist[targetIdx][10] + a;
                                    if (((da >> 2) & 0x1) != 0) {
                                        ang += step >> 1;
                                    }
                                    host.spawnEnemyBullet(90, 2, ang, 48, bossXFixed, bossYFixed);
                                }
                            }
                        }
                        if (da == 55) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                            int div;
                            switch (host.getLevel()) {
                                case 0:
                                    div = 4;
                                    break;
                                case 1:
                                    div = 5;
                                    break;
                                case 2:
                                    div = 6;
                                    break;
                                default:
                                    div = 8;
                                    break;
                            }
                            int step = 180 / div;
                            for (int a = -180; a <= 180; a += step) {
                                host.spawnEnemyBullet(90, 62, enemylist[targetIdx][10] + a, 48, bossXFixed, bossYFixed);
                            }
                        }
                    }

                    if (d0_24 >= 420 && d0_24 < 500) {
                        int da = d0_24 - 420;
                        int div;
                        switch (host.getLevel()) {
                            case 0:
                                div = 10;
                                break;
                            case 1:
                                div = 12;
                                break;
                            case 2:
                                div = 15;
                                break;
                            default:
                                div = 18;
                                break;
                        }
                        int step = 360 / div;

                        if ((da & 0x13) == 0) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                            for (int a = 0; a < 360; a += step) {
                                int ang = enemylist[targetIdx][10] + a + da;
                                if (((da >> 2) & 0x1) != 0) {
                                    ang += step >> 1;
                                }
                                host.spawnEnemyBullet(85, 26, ang, 30, bossXFixed, bossYFixed);
                            }
                        }
                        if (((da + 7) & 0x23) == 0) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                            for (int a = 0; a < 360; a += step) {
                                int ang = enemylist[targetIdx][10] + a - da;
                                if (((da >> 2) & 0x1) != 0) {
                                    ang += step >> 1;
                                }
                                host.spawnEnemyBullet(85, 26, ang, 30, bossXFixed, bossYFixed);
                            }
                        }
                    }

                    if (d0_24 >= 520 && d0_24 < 600) {
                        int da = d0_24 - 520;
                        if (da < 60) {
                            int div;
                            int cnt;
                            switch (host.getLevel()) {
                                case 0:
                                    div = 5;
                                    cnt = 3;
                                    break;
                                case 1:
                                    div = 6;
                                    cnt = 4;
                                    break;
                                case 2:
                                    div = 8;
                                    cnt = 5;
                                    break;
                                default:
                                    div = 10;
                                    cnt = 6;
                                    break;
                            }
                            int step = 360 / div;
                            if ((da & 0x3) == 0) {
                                int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                                enemylist[targetIdx][10] = aim;
                                for (int a = 0; a < 360; a += step) {
                                    int ang = enemylist[targetIdx][10] + a + (da << 3);
                                    int dist = 40 + (da << 2);
                                    int offX = (int) ((((long) Trig.cos(ang)) * (long) dist) >> 3);
                                    int offY = (int) ((((long) Trig.sin(ang)) * (long) dist) >> 3);
                                    int sx = bossXFixed + offX;
                                    int sy = bossYFixed + offY;
                                    for (int n = 0; n < cnt; n++) {
                                        host.spawnEnemyBullet(86, 12, ang, 30 + (n << 2), sx, sy);
                                    }
                                }
                            }
                        }
                    }
                    return;
                }

                if (bossWrk24[6] == 1) {
                    int d0_24 = bossWrk24[4] % 300;

                    if (d0_24 >= 20 && d0_24 < 100) {
                        int da = d0_24 - 20;
                        if (da == 0) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                        }
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
                            default:
                                div = 18;
                                break;
                        }
                        int step = 360 / div;
                        if ((da & 0x7) == 0) {
                            int bulletId = 87 + ((da >> 3) % 6);
                            mt24 = 10 + ((da >> 3) & 0x1);
                            for (int a = 0; a < 360; a += step) {
                                int ang = enemylist[targetIdx][10] + a + (da << 3) + (host.getLevel() << 4);
                                host.spawnEnemyBullet(bulletId, mt24, ang, 24, bossXFixed, bossYFixed);
                            }
                        }
                    }

                    if (d0_24 >= 120 && d0_24 < 200) {
                        int da = d0_24 - 120;
                        int div;
                        switch (host.getLevel()) {
                            case 0:
                                div = 3;
                                break;
                            case 1:
                                div = 4;
                                break;
                            case 2:
                                div = 5;
                                break;
                            default:
                                div = 6;
                                break;
                        }
                        int step = 360 / div;
                        if ((da & 0x2D) == 0) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                            for (int a = 0; a < 360; a += step) {
                                int ang = enemylist[targetIdx][10] + a + (da << 4);
                                if (((da >> 2) & 0x1) != 0) {
                                    ang += step >> 1;
                                }
                                host.spawnEnemyBullet(85, 65, ang, 30, bossXFixed, bossYFixed);
                            }
                        }
                        if (((da + 7) & 0x35) == 0) {
                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                            enemylist[targetIdx][10] = aim;
                            for (int a = 0; a < 360; a += step) {
                                int ang = enemylist[targetIdx][10] + a - (da << 5);
                                if (((da >> 2) & 0x1) != 0) {
                                    ang += step >> 1;
                                }
                                host.spawnEnemyBullet(85, 65, ang, 30, bossXFixed, bossYFixed);
                            }
                        }
                    }

                    if (d0_24 >= 220 && d0_24 < 300) {
                        int da = d0_24 - 220;
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
                            default:
                                div = 18;
                                break;
                        }
                        int step = 360 / div;
                        if ((da & 0x3) == 0) {
                            int bulletId = 87 + ((da >> 3) % 6);
                            for (int a = 0; a < 360; a += step) {
                                int ang = enemylist[targetIdx][10] + a + (da << 3) + (host.getLevel() << 2);
                                host.spawnEnemyBullet(bulletId, 0, ang, 24, bossXFixed, bossYFixed);
                            }
                        }
                    }

                    bossWrk24[4]++;
                    return;
                }

                if (bossWrk24[7] > 0) {
                    bossWrk24[7]--;
                    return;
                }

                int d0_24 = host.getMGCnt() % 100;
                int da = d0_24;

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
                    default:
                        div = 18;
                        break;
                }
                int step = 360 / div;
                if ((da & 0x7) == 0) {
                    int bulletId = 87 + ((da >> 3) % 6);
                    mt24 = 10 + ((da >> 3) & 0x1);
                    for (int a = 0; a < 360; a += step) {
                        int ang = a + (da << 3) + (host.getLevel() << 4);
                        host.spawnEnemyBullet(bulletId, mt24, ang, 24, bossXFixed, bossYFixed);
                    }
                }

                switch (host.getLevel()) {
                    case 0:
                        div = 3;
                        break;
                    case 1:
                        div = 5;
                        break;
                    case 2:
                        div = 8;
                        break;
                    default:
                        div = 10;
                        break;
                }
                step = 360 / div;
                if ((da & 0xF) < 7) {
                    int[] ids0 = { 585, 603, 621, 639, 657, 675, 693 };
                    int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - bossYFixed, host.getPlayerXFixed() - bossXFixed);
                    enemylist[targetIdx][10] = aim;
                    for (int a = 0; a < 360; a += step) {
                        int ang = enemylist[targetIdx][10] + a;
                        host.spawnEnemyBullet(ids0[da & 0xF], 0, ang, 30 + ((da & 0xF) << 2), bossXFixed, bossYFixed);
                    }
                }

                switch (host.getLevel()) {
                    case 0:
                        div = 12;
                        break;
                    case 1:
                        div = 15;
                        break;
                    case 2:
                        div = 18;
                        break;
                    default:
                        div = 20;
                        break;
                }
                step = 360 / div;
                if ((da & 0x7) == 0) {
                    int[] ids1 = { 239, 255, 271, 287, 303, 319, 335 };
                    for (int a = 0; a < 360; a += step) {
                        int ang = enemylist[targetIdx][10] + a;
                        host.spawnEnemyBullet(ids1[(da >> 3) % 7], 0, ang, 30 + ((da & 0xF) << 2), bossXFixed, bossYFixed);
                    }
                }
                return;
            }
        }
    }

    private void bc() {
        int[][] enemylist = host.getEnemyList();
        int[] bossWrk = host.getBossWrk();
        for (int i = 0; i < 10; ++i) {
            int si = bossWrk[8 + i];
            if (si < 0 || si >= enemylist.length) {
                continue;
            }
            if (enemylist[si][0] != 0) {
                int mt = enemylist[si][11];
                if (mt != 100 && mt != 101 && mt != 102 && mt != 103) {
                    host.stageJb(si);
                    bossWrk[8 + i] = 0;
                }
            }
        }
    }
}
