package touhou.stage;

import touhou.EnemyBulletSystem;
import touhou.Trig;

final class Stage2BossLogic extends AbstractBossStageLogic {
    void tickBossImpl(int enemyIdx) {
        ob(enemyIdx);
    }

    void tickMidbossImpl(int enemyIdx) {
        lb(enemyIdx);
    }

    // Boss
    void ob(int enemyIdx) {
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
                enemylist[enemyIdx][16] = 1;
                return;

            case 2:
                if (gc == 0) {
                    host.setBbaria(1);
                }
                if (host.getGameMode() != 3) {
                    bossx[idx] = 97;
                    bossy[idx] = 56;
                }
                bwave[idx] = 120 * idx;
                bspellcnt = 255;
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
                        case 13:
                            j = 1100;
                            break;
                        case 16:
                            j = 1600;
                            break;
                        case 37:
                            j = 3000;
                            break;
                    }
                    yb(enemyIdx, 4, 3);
                    return;
                }

                switch (host.getBstock()) {
                    case 2:
                        j = 1000;
                        yb(enemyIdx, 4, 1);
                        bspellcnt = 29;
                        wb(3);
                        return;
                    case 1:
                        j = 1000;
                        yb(enemyIdx, 4, 1);
                        bspellcnt = 29;
                        wb(4);
                        return;
                    case 0:
                        j = 1600;
                        yb(enemyIdx, 4, 3);
                        bspellcnt = 39;
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
                if (gc > 10) {
                    int d0 = gc % 200;
                    int ex3 = enemylist[enemyIdx][5];
                    int ey3 = enemylist[enemyIdx][6];
                    if (d0 == 0 || d0 == 100) {
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[idx] += 60;
                        } else {
                            bossx[idx] -= 60;
                        }
                        if (bossx[idx] < 30) {
                            bossx[idx] = 147;
                        }
                        if (bossx[idx] > 164) {
                            bossx[idx] = 47;
                        }
                    }

                    if (d0 > 20 && d0 < 30) {
                        int d1 = d0 - 20;
                        int ang = (d1 * 24) % 360;
                        for (int i = 0; i < 3; i++) {
                            int sp = (24 + i * 12) << 3;
                            int se = (int) ((((long) Trig.cos(ang)) * (long) sp) >> 3);
                            int te = (int) ((((long) Trig.sin(ang)) * (long) sp) >> 3);
                            int sx = ex3 + se;
                            int sy = ey3 + te;
                            switch (host.getLevel()) {
                                case 0:
                                    host.spawnEnemyBullet(84, 12, ang, 48, sx, sy);
                                    break;
                                case 1:
                                    host.spawnEnemyBullet(84, 12, ang - 10, 48, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang, 56, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 10, 64, sx, sy);
                                    break;
                                case 2:
                                    host.spawnEnemyBullet(84, 12, ang - 10, 32, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 5, 40, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang, 48, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 5, 56, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 10, 64, sx, sy);
                                    break;
                                case 3:
                                default:
                                    host.spawnEnemyBullet(84, 12, ang - 20, 32, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 15, 36, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 10, 40, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 5, 44, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang, 48, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 5, 52, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 10, 56, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 15, 60, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 20, 64, sx, sy);
                                    break;
                            }
                        }
                    }

                    if (d0 > 70 && d0 < 80) {
                        int d1 = d0 - 70;
                        int ang = 180 - d1 * 24;
                        while (ang < 0) {
                            ang += 360;
                        }
                        for (int i = 0; i < 3; i++) {
                            int sp = (24 + i * 12) << 3;
                            int se = (int) ((((long) Trig.cos(ang)) * (long) sp) >> 3);
                            int te = (int) ((((long) Trig.sin(ang)) * (long) sp) >> 3);
                            int sx = ex3 + se;
                            int sy = ey3 + te;
                            switch (host.getLevel()) {
                                case 0:
                                    host.spawnEnemyBullet(84, 12, ang, 48, sx, sy);
                                    break;
                                case 1:
                                    host.spawnEnemyBullet(84, 12, ang - 5, 48, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang, 56, sx, sy);
                                    break;
                                case 2:
                                    host.spawnEnemyBullet(84, 12, ang + 10, 48, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang, 56, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 10, 64, sx, sy);
                                    break;
                                case 3:
                                default:
                                    host.spawnEnemyBullet(84, 12, ang + 20, 32, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 15, 36, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 10, 40, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang + 5, 44, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang, 48, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 5, 52, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 10, 56, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 15, 60, sx, sy);
                                    host.spawnEnemyBullet(84, 12, ang - 20, 64, sx, sy);
                                    break;
                            }
                        }
                    }

                    if (d0 == 15 || d0 == 65) {
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
                                div = 20;
                                break;
                        }
                        for (int a = 0; a < 360; a += 360 / div) {
                            host.spawnEnemyBullet(86, 0, a + 5, 64, ex3, ey3);
                            host.spawnEnemyBullet(86, 0, a, 72, ex3, ey3);
                            host.spawnEnemyBullet(86, 0, a - 5, 64, ex3, ey3);
                        }
                    }

                    if (d0 > 130 && d0 < 160 && (d0 % 6) == 0) {
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
                                div = 20;
                                break;
                        }
                        int step = 360 / div;
                        for (int a = 0; a < 360; a += step) {
                            host.spawnEnemyBullet(319, 0, a, 64, ex3, ey3);
                            host.spawnEnemyBullet(319, 2, a + (step >> 1), 128, ex3, ey3);
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
                if (gc > 10) {
                    int d0 = gc % 100;
                    int ex4 = enemylist[enemyIdx][5];
                    int ey4 = enemylist[enemyIdx][6];
                    if (d0 == 90) {
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[idx] += 60;
                        } else {
                            bossx[idx] -= 60;
                        }
                        if (bossx[idx] < 30) {
                            bossx[idx] = 147;
                        }
                        if (bossx[idx] > 164) {
                            bossx[idx] = 47;
                        }
                    }

                    if (d0 > 40 && d0 < 90) {
                        int ang = (d0 * 26) % 360;
                        int sp = (10 + d0 - 40) << 3;
                        int se = (int) ((((long) Trig.cos(ang)) * (long) sp) >> 3);
                        int te = (int) ((((long) Trig.sin(ang)) * (long) sp) >> 3);
                        host.spawnEnemyBullet(335, 12, ang, 48, ex4 + se, ey4 + te);

                        int ang2 = 180 - d0 * 26;
                        while (ang2 < 0) {
                            ang2 += 360;
                        }
                        se = (int) ((((long) Trig.cos(ang2)) * (long) sp) >> 3);
                        te = (int) ((((long) Trig.sin(ang2)) * (long) sp) >> 3);
                        host.spawnEnemyBullet(335, 12, ang2, 48, ex4 + se, ey4 + te);
                    }

                    if ((d0 % 100) == 20) {
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
                                div = 16;
                                break;
                        }
                        for (int a = 0; a < 360; a += 360 / div) {
                            host.spawnEnemyBullet(87, 17, a, 64, ex4, ey4);
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
                    spellId = 18 + host.getLevel();
                    wb(11);
                }
                return;

            case 11:
                bspellcnt = 34;
                switch (gc) {
                    case 1:
                    case 38:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 4:
                    case 34:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 8:
                    case 30:
                        enemylist[enemyIdx][3] = 3;
                        break;
                    case 12:
                        enemylist[enemyIdx][3] = 4;
                        break;
                }
                if (gc > 40) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(12);
                    j = 1100;
                    yb(enemyIdx, 4, 2);
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
                if (gc > 10) {
                    int d0 = gc % 100;
                    int ex12 = enemylist[enemyIdx][5];
                    int ey12 = enemylist[enemyIdx][6];
                    if (d0 == 90) {
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[idx] += 30;
                        } else {
                            bossx[idx] -= 30;
                        }
                        bossy[idx] = 48;
                        if (bossx[idx] < 30) {
                            bossx[idx] = 147;
                        }
                        if (bossx[idx] > 164) {
                            bossx[idx] = 47;
                        }
                    }
                    if (d0 >= 20 && d0 <= 30) {
                        enemylist[enemyIdx][3] = 5;
                    }

                    switch (d0) {
                        case 20: {
                            int n2;
                            switch (host.getLevel()) {
                                case 0:
                                    n2 = 8;
                                    break;
                                case 1:
                                    n2 = 12;
                                    break;
                                case 2:
                                    n2 = 18;
                                    break;
                                case 3:
                                default:
                                    n2 = 24;
                                    break;
                            }
                            for (int a = 0; a < 360; a += 360 / n2) {
                                host.spawnEnemyBullet(81, 18, a, 32, ex12, ey12);
                                host.spawnEnemyBullet(81, 19, a, 48, ex12, ey12);
                            }
                            break;
                        }

                        case 40:
                        case 45:
                        case 50: {
                            int n2;
                            switch (host.getLevel()) {
                                case 0:
                                    n2 = 8;
                                    break;
                                case 1:
                                    n2 = 12;
                                    break;
                                case 2:
                                    n2 = 18;
                                    break;
                                case 3:
                                default:
                                    n2 = 24;
                                    break;
                            }
                            for (int a = 0; a < 360; a += 360 / n2) {
                                host.spawnEnemyBullet(86, 0, a, 24, ex12, ey12);
                            }
                            break;
                        }

                        case 70:
                        case 76:
                        case 82: {
                            int n2;
                            switch (host.getLevel()) {
                                case 0:
                                    n2 = 8;
                                    break;
                                case 1:
                                    n2 = 12;
                                    break;
                                case 2:
                                    n2 = 18;
                                    break;
                                case 3:
                                default:
                                    n2 = 24;
                                    break;
                            }
                            for (int a = 0; a < 360; a += 360 / n2) {
                                host.spawnEnemyBullet(287, 0, a, 16, ex12, ey12);
                                host.spawnEnemyBullet(287, 0, a, 16, ex12, ey12);
                            }
                            break;
                        }
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
                    spellId = 22 + host.getLevel();
                    wb(14);
                }
                return;

            case 14:
                bspellcnt = 34;
                switch (gc) {
                    case 1:
                    case 38:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 4:
                    case 34:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 8:
                    case 30:
                        enemylist[enemyIdx][3] = 3;
                        break;
                    case 12:
                        enemylist[enemyIdx][3] = 4;
                        break;
                }
                if (gc > 40) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(15);
                    j = 1100;
                    yb(enemyIdx, 4, 2);
                }
                return;

            case 15:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }
                enemylist[enemyIdx][3] = 0;
                switch (gc % 100) {
                    case 50:
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[idx] = 147;
                        } else {
                            bossx[idx] = 47;
                        }
                        bossy[idx] = 68;
                        break;
                    case 80:
                        if (enemylist[enemyIdx][5] > 6356992) {
                            bossx[idx] = 47;
                        } else {
                            bossx[idx] = 147;
                        }
                        bossy[idx] = 68;
                        break;
                }
                if ((gc % 100) >= 20 && (gc % 100) <= 30) {
                    enemylist[enemyIdx][3] = 5;
                }
                if (gc > 10) {
                    int d0 = gc % 100;
                    int ex15 = enemylist[enemyIdx][5];
                    int ey15 = enemylist[enemyIdx][6];
                    switch (d0) {
                        case 20: {
                            int lvl = host.getLevel();
                            if (lvl == 0) {
                                int px = host.getPlayerXFixed();
                                int py = host.getPlayerYFixed();
                                host.spawnEnemyBullet(92, 14, 270, 24, px - (35 << 16), 234 << 16);
                                host.spawnEnemyBullet(92, 14, 450, 24, px + (35 << 16), 8 << 16);
                                host.spawnEnemyBullet(92, 14, 540, 24, 194 << 16, py + (35 << 16));
                                host.spawnEnemyBullet(92, 14, 360, 24, 0, py - (35 << 16));

                                host.spawnEnemyBullet(92, 14, 270, 24, px + (80 << 16), 234 << 16);
                                host.spawnEnemyBullet(92, 14, 450, 24, px - (80 << 16), 8 << 16);
                                host.spawnEnemyBullet(92, 14, 540, 24, 194 << 16, py - (80 << 16));
                                host.spawnEnemyBullet(92, 14, 360, 24, 0, py + (80 << 16));
                                break;
                            }

                            int div;
                            int mt92;
                            if (lvl == 1) {
                                div = 12;
                                mt92 = 14;
                            } else if (lvl == 2) {
                                div = 12;
                                mt92 = 15;
                            } else {
                                div = 12;
                                mt92 = 16;
                            }

                            for (int a = 0; a < 360; a += 360 / div) {
                                host.spawnEnemyBullet(92, mt92, a, 24, ex15, ey15);
                            }
                            break;
                        }

                        case 30:
                        case 33:
                        case 36:
                        case 39:
                        case 42:
                        case 45: {
                            int dx = host.getPlayerXFixed() - ex15;
                            int dy = host.getPlayerYFixed() - ey15;
                            int ang = EnemyBulletSystem.arcTan2Deg(dy, dx);
                            int adjust = 10 + (d0 - 30);
                            if ((gc & 1) == 0) {
                                ang += adjust;
                            } else {
                                ang -= adjust;
                            }
                            ang = ang % 360;
                            for (int n18 = 0; n18 < 5; n18++) {
                                int spd = (10 + (n18 << 1)) << 3;
                                host.spawnEnemyBullet(287, 2, ang, spd, ex15, ey15);
                            }
                            break;
                        }

                        case 60:
                        case 65:
                        case 70: {
                            int dx = host.getPlayerXFixed() - ex15;
                            int dy = host.getPlayerYFixed() - ey15;
                            int base = EnemyBulletSystem.arcTan2Deg(dy, dx) % 360;

                            int step = 30;
                            int offMin = -2;
                            int offMax = 2;
                            switch (host.getLevel()) {
                                case 1:
                                    step = 20;
                                    offMin = -3;
                                    offMax = 3;
                                    break;
                                case 2:
                                    step = 12;
                                    offMin = -3;
                                    offMax = 3;
                                    break;
                                case 3:
                                    step = 10;
                                    offMin = -4;
                                    offMax = 4;
                                    break;
                            }

                            for (int off = offMin; off < offMax; off++) {
                                int spd;
                                int mod = off & 0x3;
                                if (mod == 0) {
                                    spd = 64;
                                } else if (mod == 2) {
                                    spd = 56;
                                } else {
                                    spd = 48;
                                }
                                if (host.getLevel() == 0) {
                                    spd -= 12;
                                }
                                host.spawnEnemyBullet(319, 0, base + step * off, spd, ex15, ey15);
                            }
                            break;
                        }
                    }
                }
                return;

            case 16:
                bossx[idx] = 97;
                bossy[idx] = 68;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                if (gc > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 26 + host.getLevel();
                    wb(17);
                }
                return;

            case 17:
                bspellcnt = 39;
                switch (gc) {
                    case 1:
                    case 38:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 4:
                    case 34:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 8:
                    case 30:
                        enemylist[enemyIdx][3] = 3;
                        break;
                    case 12:
                        enemylist[enemyIdx][3] = 4;
                        break;
                }
                if (gc > 40) {
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
                switch (gc % 190) {
                    case 0:
                        bossx[idx] = 97;
                        break;
                    case 60:
                        bossx[idx] = (host.getPlayerXFixed() >> 16) + 20;
                        break;
                    case 120:
                        bossx[idx] = (host.getPlayerXFixed() >> 16) - 20;
                        break;
                }
                if (bossx[idx] < 20) {
                    bossx[idx] = 20;
                }
                if (bossx[idx] > 174) {
                    bossx[idx] = 174;
                }
                if (gc > 10) {
                    int d0 = gc % 190;
                    int ex18 = enemylist[enemyIdx][5];
                    int ey18 = enemylist[enemyIdx][6];
                    switch (d0) {
                        case 20: {
                            int dx = host.getPlayerXFixed() - ex18;
                            int dy = host.getPlayerYFixed() - ey18;
                            int base = EnemyBulletSystem.arcTan2Deg(dy, dx);
                            host.spawnEnemyBullet(92, 17, base, 24, ex18, ey18);
                            host.spawnEnemyBullet(92, 17, base + 30, 24, ex18, ey18);
                            host.spawnEnemyBullet(92, 17, base - 30, 24, ex18, ey18);
                            host.spawnEnemyBullet(92, 17, base + 60, 24, ex18, ey18);
                            host.spawnEnemyBullet(92, 17, base - 60, 24, ex18, ey18);
                            break;
                        }

                        case 30:
                        case 33:
                        case 36:
                        case 39:
                        case 42:
                        case 45:
                        case 170:
                        case 173:
                        case 176:
                        case 179:
                        case 182:
                        case 185: {
                            int dx = host.getPlayerXFixed() - ex18;
                            int dy = host.getPlayerYFixed() - ey18;
                            int base = EnemyBulletSystem.arcTan2Deg(dy, dx) % 360;

                            int step = 25;
                            int offMin = -3;
                            int offMax = 3;
                            switch (host.getLevel()) {
                                case 1:
                                    step = 20;
                                    offMin = -4;
                                    offMax = 4;
                                    break;
                                case 2:
                                    step = 12;
                                    offMin = -6;
                                    offMax = 6;
                                    break;
                                case 3:
                                    step = 10;
                                    offMin = -8;
                                    offMax = 8;
                                    break;
                            }

                            for (int off = offMin; off < offMax; off++) {
                                int ang = base + step * off;
                                host.spawnEnemyBullet(287, 0, ang, 24, ex18, ey18);
                                host.spawnEnemyBullet(287, 0, ang, 32, ex18, ey18);
                                host.spawnEnemyBullet(287, 0, ang, 40, ex18, ey18);
                            }
                            break;
                        }

                        case 60:
                        case 65:
                        case 70:
                        case 75:
                        case 80: {
                            int dx = host.getPlayerXFixed() - ex18;
                            int dy = host.getPlayerYFixed() - ey18;
                            int base = EnemyBulletSystem.arcTan2Deg(dy, dx) % 360;

                            int n2;
                            int count;
                            switch (host.getLevel()) {
                                case 0:
                                    n2 = 12;
                                    count = 1;
                                    break;
                                case 1:
                                    n2 = 10;
                                    count = 2;
                                    break;
                                case 2:
                                    n2 = 7;
                                    count = 3;
                                    break;
                                case 3:
                                default:
                                    n2 = 4;
                                    count = 5;
                                    break;
                            }

                            for (int i = 0; i < count; i++) {
                                int spd = (n2 + i) << 3;
                                host.spawnEnemyBullet(81, 18, base - 10, spd, ex18, ey18);
                                host.spawnEnemyBullet(81, 19, base + 10, spd, ex18, ey18);
                            }
                            break;
                        }

                        case 140:
                        case 145:
                        case 150:
                        case 155:
                        case 160: {
                            int lvl = host.getLevel();
                            if (lvl < 2) {
                                break;
                            }
                            int div = (lvl == 2) ? 20 : 24;
                            for (int a = 0; a < 360; a += 360 / div) {
                                host.spawnEnemyBullet(86, 0, a, 16, ex18 + (50 << 16), ey18);
                                host.spawnEnemyBullet(86, 0, a, 16, ex18 - (50 << 16), ey18);
                            }
                            break;
                        }
                    }
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
                    spellId = 110;
                    wb(38);
                }
                return;

            case 38:
                bspellcnt = 60;
                switch (gc) {
                    case 1:
                    case 25:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 5:
                    case 20:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 10:
                        enemylist[enemyIdx][3] = 3;
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
                int ex39 = enemylist[enemyIdx][5];
                int ey39 = enemylist[enemyIdx][6];

                int step39 = 48;
                if ((gc & 0xF) == 0) {
                    for (int x = 0; x < 194; x += step39) {
                        host.spawnEnemyBullet(693, 0, 450, 16, x << 16, 524288);
                        host.spawnEnemyBullet(693, 0, 270, 16, (x + (step39 >> 1)) << 16, 15335424);
                    }
                }
                if ((gc & 0xF) == 0x7) {
                    for (int y = 8; y < 234; y += step39) {
                        host.spawnEnemyBullet(693, 0, 360, 16, 0, y << 16);
                        host.spawnEnemyBullet(693, 0, 540, 16, 12713984, (y + (step39 >> 1)) << 16);
                    }
                }

                if (gc <= 30) {
                    return;
                }

                int d6 = 40;
                int d7 = 64;
                if (o[2] != 0 && o[1] < (o[2] * 66 / 100)) {
                    d6 = 50;
                    d7 = 48;
                }
                if (o[2] != 0 && o[1] < (o[2] * 33 / 100)) {
                    d6 = 60;
                    d7 = 32;
                }

                if (d7 != 0 && (gc % d7) == 0) {
                    host.spawnEnemyBullet(87, 77, (gc << 2) % 360, d6, ex39, ey39);
                }
                if ((gc & 1) == 0) {
                    enemylist[enemyIdx][14] >>= 1;
                    enemylist[enemyIdx][15] >>= 1;
                }
                if (d7 != 0 && (gc % d7) == 0) {
                    enemylist[enemyIdx][14] = 458752;
                    enemylist[enemyIdx][15] = 458752;
                    int by = ((host.getPlayerYFixed() >> 16) + (gc << 2)) % 154;
                    int bx = ((host.getPlayerXFixed() >> 16) + (gc << 3)) % 83;
                    bossx[idx] = 20 + by;
                    bossy[idx] = 38 + bx;
                }
                return;
        }
    }

    //midboss
    void lb(int enemyIdx) {
        int[][] enemylist = host.getEnemyList();
        int[] bossx = host.getBossX();
        int[] bossy = host.getBossY();

        switch (enemylist[enemyIdx][7]) {
            case 0:
                bossx[0] = 97;
                bossy[0] = 68;
                bspellcnt = 255;
                batkf = false;
                vb(enemyIdx, 1);
                j = enemylist[enemyIdx][4];
                host.setResetFlag(4);
                yb(enemyIdx, 4, 0);
                return;

            case 1:
                if (host.getMGCnt() == 0) {
                    enemylist[enemyIdx][16] = 0;
                }
                bossx[0] = 97;
                bossy[0] = 68;
                batkf = false;
                enemylist[enemyIdx][3] = 0;
                if (host.getMGCnt() > 30) {
                    host.getBossWrk()[1] = 1;
                    bspellcnt = 29;
                    vb(enemyIdx, 3);
                    return;
                }
                return;

            case 3:
                enemylist[enemyIdx][16] = 1;
                batkf = true;
                enemylist[enemyIdx][3] = 0;

                int mg = host.getMGCnt();
                int lvl = host.getLevel();
                int ex = enemylist[enemyIdx][5];
                int ey = enemylist[enemyIdx][6];

                if (host.getBossWrk()[1] == 1) {
                    host.getBossWrk()[1] = 0;
                    host.getBossWrk()[3] = 30;
                    host.getBossWrk()[4] = 0;
                    host.getBossWrk()[5] = 0;
                    host.getBossWrk()[6] = 0;
                }

                switch (mg) {
                    case 10:
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[0] = 147;
                            bossy[0] = 68;
                        } else {
                            bossx[0] = 47;
                            bossy[0] = 68;
                        }
                        break;
                    case 60:
                        if (enemylist[enemyIdx][5] > 6356992) {
                            bossx[0] = 47;
                            bossy[0] = 28;
                        } else {
                            bossx[0] = 147;
                            bossy[0] = 28;
                        }
                        break;
                    case 110:
                        bossx[0] = 97;
                        bossy[0] = 58;
                        break;
                }

                if ((mg > 30 && mg < 40) || (mg > 80 && mg < 90)) {
                    int d0;
                    if (mg < 40) {
                        d0 = mg - 30;
                    } else {
                        d0 = mg - 80;
                    }

                    int ang = d0 * 24;
                    int sp = 240;
                    int se = (int) ((((long) Trig.cos(ang)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(ang)) * (long) sp) >> 3);
                    int sx = ex + se;
                    int sy = ey + te;

                    switch (lvl) {
                        case 0:
                            host.spawnEnemyBullet(239, 12, ang, 80, sx, sy);
                            break;
                        case 1:
                            host.spawnEnemyBullet(239, 12, ang, 48, sx, sy);
                            host.spawnEnemyBullet(239, 12, ang, 64, sx, sy);
                            host.spawnEnemyBullet(239, 12, ang, 80, sx, sy);
                            break;
                        case 2: {
                            int a = ang;
                            for (int i = 0; i < 3; i++) {
                                int d3 = 10 - (i << 1);
                                a += (i & 0x1) * 20;
                                int spd = d3 << 3;
                                host.spawnEnemyBullet(239, 12, a + 3, spd, sx, sy);
                                host.spawnEnemyBullet(239, 12, a, spd, sx, sy);
                                host.spawnEnemyBullet(239, 12, a - 3, spd, sx, sy);
                            }
                            break;
                        }
                        case 3:
                        default: {
                            int a = ang;
                            for (int i = 0; i < 3; i++) {
                                int d3 = 9 - (i << 1);
                                a += i * 20;
                                int spd = d3 << 3;
                                host.spawnEnemyBullet(239, 12, a + 6, spd, sx, sy);
                                host.spawnEnemyBullet(239, 12, a + 3, spd + 8, sx, sy);
                                host.spawnEnemyBullet(239, 12, a, spd + 16, sx, sy);
                                host.spawnEnemyBullet(239, 12, a - 3, spd + 8, sx, sy);
                                host.spawnEnemyBullet(239, 12, a - 6, spd, sx, sy);
                            }
                            break;
                        }
                    }
                    enemylist[enemyIdx][3] = 5;
                }

                if ((mg > 35 && mg < 45) || (mg > 85 && mg < 95)) {
                    int d0;
                    if (mg < 45) {
                        d0 = mg - 40;
                    } else {
                        d0 = mg - 80;
                    }

                    int ang = 270 - d0 * 24;
                    int sp = 240;
                    int se = (int) ((((long) Trig.cos(ang)) * (long) sp) >> 3);
                    int te = (int) ((((long) Trig.sin(ang)) * (long) sp) >> 3);
                    int sx = ex + se;
                    int sy = ey + te;

                    switch (lvl) {
                        case 0:
                            host.spawnEnemyBullet(335, 12, ang, 88, sx, sy);
                            break;
                        case 1:
                            host.spawnEnemyBullet(335, 12, ang, 56, sx, sy);
                            host.spawnEnemyBullet(335, 12, ang, 72, sx, sy);
                            host.spawnEnemyBullet(335, 12, ang, 88, sx, sy);
                            break;
                        case 2: {
                            int a = ang;
                            for (int i = 0; i < 3; i++) {
                                int d3 = 10 - (i << 1);
                                a += (i & 0x1) * 20;
                                int spd = d3 << 3;
                                host.spawnEnemyBullet(335, 12, a + 6, spd, sx, sy);
                                host.spawnEnemyBullet(335, 12, a, spd, sx, sy);
                                host.spawnEnemyBullet(335, 12, a - 6, spd, sx, sy);
                            }
                            break;
                        }
                        case 3:
                        default: {
                            int a = ang;
                            for (int i = 0; i < 3; i++) {
                                int d3 = 8 - (i << 1);
                                a += (i & 0x1) * 20;
                                int spd = d3 << 3;
                                host.spawnEnemyBullet(335, 12, a + 8, spd, sx, sy);
                                host.spawnEnemyBullet(335, 12, a + 4, spd + 8, sx, sy);
                                host.spawnEnemyBullet(335, 12, a, spd + 16, sx, sy);
                                host.spawnEnemyBullet(335, 12, a - 4, spd + 8, sx, sy);
                                host.spawnEnemyBullet(335, 12, a - 8, spd, sx, sy);
                            }
                            break;
                        }
                    }
                    enemylist[enemyIdx][3] = 5;
                }

                int n2;
                switch (lvl) {
                    case 0:
                        n2 = 12;
                        break;
                    case 1:
                        n2 = 10;
                        break;
                    case 2:
                        n2 = 8;
                        break;
                    case 3:
                    default:
                        n2 = 6;
                        break;
                }
                if (mg >= 130 && mg <= 240 && (mg % n2) == 0) {
                    int dx = host.getPlayerXFixed() - ex;
                    int dy = host.getPlayerYFixed() - ey;
                    int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);

                    int bid;
                    int mt;
                    int baseSp;
                    if ((mg % (n2 << 1)) == 0) {
                        bid = 335;
                        mt = 2;
                        baseSp = 10;
                    } else {
                        bid = 239;
                        mt = 0;
                        baseSp = 3;
                    }

                    for (int i = 0; i < 3; i++) {
                        int spd = (baseSp + (i << 1)) << 3;
                        host.spawnEnemyBullet(bid, mt, aim, spd, ex, ey);
                        host.spawnEnemyBullet(bid, mt, aim + 20, spd, ex, ey);
                        host.spawnEnemyBullet(bid, mt, aim - 20, spd, ex, ey);
                        if (bid == 335) {
                            host.spawnEnemyBullet(bid, mt, aim + 40, spd, ex, ey);
                            host.spawnEnemyBullet(bid, mt, aim - 40, spd, ex, ey);
                        }
                    }
                }

                if (mg > 250) {
                    host.setMGCnt(0);
                }

                if (bspellcnt < 0) {
                    vb(enemyIdx, 2);
                    return;
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
}
