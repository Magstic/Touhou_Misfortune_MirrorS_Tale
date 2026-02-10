package touhou.stage;

import touhou.EnemyBulletSystem;
import touhou.Trig;

final class Stage4BossLogic extends AbstractBossStageLogic {
    private final int[] work = new int[16];

    void tickBossImpl(int enemyIdx) {
        qb(enemyIdx);
    }

    void tickMidbossImpl(int enemyIdx) {
    }

    // Boss(stub)
    void qb(int enemyIdx) {
        int[][] enemylist = host.getEnemyList();
        int[] bossx = host.getBossX();
        int[] bossy = host.getBossY();
        int[] bossWrk = host.getBossWrk();
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
                    bossy[idx] = 44;
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
                            j = 2800;
                            break;
                        case 13:
                            j = 2000;
                            break;
                        case 16:
                            j = 2000;
                            break;
                        case 19:
                            j = 2400;
                            break;
                        case 37:
                            j = 3000;
                            break;
                    }
                    yb(enemyIdx, 6, 3);
                    return;
                }

                switch (host.getBstock()) {
                    case 3:
                        j = 1100;
                        yb(enemyIdx, 6, 1);
                        bspellcnt = 39;
                        wb(3);
                        return;
                    case 2:
                        j = 1200;
                        yb(enemyIdx, 6, 1);
                        bspellcnt = 39;
                        wb(4);
                        return;
                    case 1:
                        j = 2000;
                        yb(enemyIdx, 6, 3);
                        bspellcnt = 39;
                        wb(16);
                        return;
                    case 0:
                        j = 2400;
                        yb(enemyIdx, 6, 3);
                        bspellcnt = 49;
                        wb(19);
                        return;
                }
                return;

            case 3:
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    bc();
                    j = 65535;
                    yb(l, n, 2);
                    wb(10);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                {
                    if (gc == 0) {
                        int d5_3 = 2;
                        switch (host.getLevel()) {
                            case 0:
                                d5_3 = 2;
                                break;
                            case 1:
                                d5_3 = 4;
                                break;
                            case 2:
                                d5_3 = 6;
                                break;
                            case 3:
                                d5_3 = 8;
                                break;
                        }
                        for (int i = 0; i < 8; ++i) {
                            if (i < d5_3) {
                                bossWrk[8 + i] = host.stageFb(20 + (i & 0x1), 0, 500, 0, 0, 0, 0, enemylist[enemyIdx][5], enemylist[enemyIdx][6]);
                            } else {
                                bossWrk[8 + i] = 0;
                            }
                        }
                    }

                    int d1_3 = gc << 5;
                    if (d1_3 > 240) {
                        d1_3 = 240;
                    }
                    int d2_3 = (gc - 20) << 1;
                    if (gc < 20) {
                        d2_3 = 0;
                    }

                    for (int i = 0; i < 8; ++i) {
                        work[i] = -1;
                    }
                    switch (host.getLevel()) {
                        case 0:
                            work[0] = 90;
                            work[1] = 270;
                            break;
                        case 1:
                            work[0] = 45;
                            work[1] = 135;
                            work[2] = 225;
                            work[3] = 315;
                            break;
                        case 2:
                            work[0] = 0;
                            work[1] = 60;
                            work[2] = 120;
                            work[3] = 180;
                            work[4] = 240;
                            work[5] = 300;
                            break;
                        case 3:
                            work[0] = 0;
                            work[1] = 45;
                            work[2] = 90;
                            work[3] = 135;
                            work[4] = 180;
                            work[5] = 225;
                            work[6] = 270;
                            work[7] = 315;
                            break;
                    }

                    for (int i = 0; i < 8; ++i) {
                        if (work[i] == -1) {
                            continue;
                        }
                        int si = bossWrk[8 + i];
                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                            continue;
                        }

                        int dir;
                        if ((i & 0x1) == 0) {
                            dir = (work[i] + 270 + d2_3) % 360;
                        } else {
                            dir = (work[i] + 270 - d2_3 + 360) % 360;
                        }
                        int se = (int) ((((long) Trig.cos(dir)) * (long) d1_3) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) d1_3) >> 3);
                        enemylist[si][5] = enemylist[enemyIdx][5] + se;
                        enemylist[si][6] = enemylist[enemyIdx][6] + te;
                    }

                    if (gc > 15 && (((gc + 1) & 0x3) == 0)) {
                        int d0b = gc + 1;
                        for (int i = 0; i < 8; ++i) {
                            if (work[i] == -1) {
                                continue;
                            }
                            int si = bossWrk[8 + i];
                            if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                continue;
                            }
                            int sx = enemylist[si][5];
                            int sy = enemylist[si][6];
                            int a0 = (d0b << 3) % 360;
                            if ((i & 0x1) != 0) {
                                a0 = 360 - ((d0b << 3) % 360);
                            }
                            for (int a = 0; a < 360; a += 180) {
                                host.spawnEnemyBullet(271, 0, a0 + a, 32, sx, sy);
                            }
                        }
                    }
                }

                int d0 = (gc + 1) % 320;
                if (d0 == 0 || d0 == 80 || d0 == 160) {
                    int d8;
                    if (host.getPlayerXFixed() > enemylist[enemyIdx][5]) {
                        bossx[idx] += 30;
                        d8 = -1;
                    } else {
                        bossx[idx] -= 30;
                        d8 = 1;
                    }

                    if (bossx[idx] < 30) {
                        bossx[idx] += 50;
                        d8 = -1;
                    }
                    if (bossx[idx] > 164) {
                        bossx[idx] -= 50;
                        d8 = 1;
                    }

                    if (bossy[idx] < 60) {
                        bossy[idx] = 80;
                    } else if (bossy[idx] > 60) {
                        bossy[idx] = 60;
                    } else {
                        bossy[idx] = 40;
                    }

                    int d5;
                    int d6;
                    switch (host.getLevel()) {
                        case 0:
                            d5 = 4;
                            d6 = 8;
                            break;
                        case 1:
                            d5 = 6;
                            d6 = 10;
                            break;
                        case 2:
                            d5 = 8;
                            d6 = 12;
                            break;
                        case 3:
                        default:
                            d5 = 8;
                            d6 = 14;
                            break;
                    }

                    int bx = enemylist[enemyIdx][5];
                    int by = enemylist[enemyIdx][6];
                    int d0b = gc + 1;
                    for (int a = 0; a < 360; a += 360 / d5) {
                        for (int n8 = 0; n8 < d6; ++n8) {
                            int ang = (d0b + a + ((n8 << 2) * d8)) % 360;
                            if (ang < 0) {
                                ang += 360;
                            }
                            int sp = 40 + (n8 << 2);
                            host.spawnEnemyBullet(82, 0, ang, sp, bx, by);
                        }
                    }
                }

                if (host.getLevel() >= 2) {
                    int bx = enemylist[enemyIdx][5];
                    int by = enemylist[enemyIdx][6];
                    if (d0 == 40 || d0 == 120 || d0 == 200 || d0 == 260) {
                        host.spawnEnemyBullet(89, 31, 390, 32, bx, by);
                        host.spawnEnemyBullet(89, 31, 510, 32, bx, by);
                    }
                    if (d0 == 240) {
                        host.spawnEnemyBullet(89, 31, 330, 32, bx, by);
                        host.spawnEnemyBullet(89, 31, 570, 32, bx, by);
                    }
                    if (d0 == 250) {
                        host.spawnEnemyBullet(89, 31, 360, 32, bx, by);
                        host.spawnEnemyBullet(89, 31, 540, 32, bx, by);
                    }
                }
                return;

            case 4:
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    bc();
                    j = 65535;
                    yb(l, n, 2);
                    wb(13);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                {
                    if (gc == 0) {
                        int d5_4 = 4;
                        switch (host.getLevel()) {
                            case 0:
                                d5_4 = 4;
                                break;
                            case 1:
                                d5_4 = 6;
                                break;
                            case 2:
                                d5_4 = 6;
                                break;
                            case 3:
                                d5_4 = 8;
                                break;
                        }
                        for (int i = 0; i < 8; ++i) {
                            if (i < d5_4) {
                                bossWrk[8 + i] = host.stageFb(20 + (i & 0x1), 0, 500, 0, 0, 0, 0, enemylist[enemyIdx][5], enemylist[enemyIdx][6]);
                            } else {
                                bossWrk[8 + i] = 0;
                            }
                        }
                    }

                    int d1_4 = gc;
                    if (d1_4 > 8) {
                        d1_4 = 8;
                    }

                    for (int i = 0; i < 8; ++i) {
                        work[(i << 1) + 0] = -1;
                    }

                    int d3_4 = 0;
                    int d4_4 = 0;
                    switch (host.getLevel()) {
                        case 0:
                            work[0] = 45;
                            work[1] = 30;
                            work[2] = 315;
                            work[3] = 30;
                            work[4] = 45;
                            work[5] = 60;
                            work[6] = 315;
                            work[7] = 60;
                            d3_4 = 0;
                            d4_4 = 3276800;
                            break;
                        case 1:
                            work[0] = 30;
                            work[1] = 30;
                            work[2] = 330;
                            work[3] = 30;
                            work[4] = 30;
                            work[5] = 60;
                            work[6] = 330;
                            work[7] = 60;
                            work[8] = 30;
                            work[9] = 90;
                            work[10] = 330;
                            work[11] = 90;
                            d3_4 = 0;
                            d4_4 = 3932160;
                            break;
                        case 2:
                            work[0] = 270;
                            work[1] = 0;
                            work[2] = 90;
                            work[3] = 0;
                            work[4] = 315;
                            work[5] = 50;
                            work[6] = 45;
                            work[7] = 50;
                            work[8] = 225;
                            work[9] = 50;
                            work[10] = 135;
                            work[11] = 50;
                            d3_4 = 3932160;
                            d4_4 = 0;
                            break;
                        case 3:
                            work[0] = 45;
                            work[1] = 30;
                            work[2] = 315;
                            work[3] = 30;
                            work[4] = 90;
                            work[5] = 50;
                            work[6] = 270;
                            work[7] = 50;
                            work[8] = 135;
                            work[9] = 30;
                            work[10] = 225;
                            work[11] = 30;
                            work[12] = 0;
                            work[13] = 50;
                            work[14] = 180;
                            work[15] = 50;
                            d3_4 = 0;
                            d4_4 = 0;
                            break;
                    }

                    for (int i = 0; i < 8; ++i) {
                        if (work[(i << 1) + 0] == -1) {
                            continue;
                        }
                        int si = bossWrk[8 + i];
                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                            continue;
                        }

                        int sp = work[(i << 1) + 1] * d1_4;
                        int dir = work[(i << 1) + 0] + 270;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        if (work[(i << 1) + 0] > 180) {
                            enemylist[si][5] = enemylist[enemyIdx][5] + d3_4 + se;
                        } else {
                            enemylist[si][5] = enemylist[enemyIdx][5] - d3_4 + se;
                        }
                        enemylist[si][6] = enemylist[enemyIdx][6] + d4_4 + te;
                    }
                }

                if (gc > 15) {
                    int stepAng = 120;
                    if (host.getLevel() <= 1) {
                        stepAng = 180;
                    }
                    int d0b = gc + 1;
                    for (int i = 0; i < 8; ++i) {
                        if ((((d0b + (i >> 1)) & 0x12) != 0) || work[(i << 1) + 0] == -1) {
                            continue;
                        }
                        int si = bossWrk[8 + i];
                        if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                            continue;
                        }

                        int sx = enemylist[si][5];
                        int sy = enemylist[si][6];
                        int a0 = (d0b << 3) % 360;
                        if ((i & 0x1) != 0) {
                            a0 = 360 - ((d0b << 3) % 360);
                        }
                        for (int a = 0; a < 360; a += stepAng) {
                            host.spawnEnemyBullet(239, 0, a0 + a, 24, sx, sy);
                        }

                        if (i >= 6) {
                            int a1 = (d0b << 3) % 360;
                            if ((((i + 1) & 0x1) != 0)) {
                                a1 = 360 - ((d0b << 3) % 360);
                            }
                            for (int a = 0; a < 360; a += 120) {
                                host.spawnEnemyBullet(239, 0, a1 + a, 24, sx, sy);
                            }
                        }
                    }
                }

                d0 = (gc + 1) % 320;
                if (d0 == 0 || d0 == 80 || d0 == 160) {
                    if (host.getPlayerXFixed() > enemylist[enemyIdx][5]) {
                        bossx[idx] -= 40;
                    } else {
                        bossx[idx] += 40;
                    }

                    if (bossx[idx] < 30) {
                        bossx[idx] += 50;
                    }
                    if (bossx[idx] > 164) {
                        bossx[idx] -= 50;
                    }

                    if (bossy[idx] < 60) {
                        bossy[idx] = 80;
                        return;
                    }
                    if (bossy[idx] > 60) {
                        bossy[idx] = 60;
                        return;
                    }
                    bossy[idx] = 40;
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
                    spellId = 46 + host.getLevel();
                    wb(11);
                }
                return;

            case 11:
                bspellcnt = 39;
                switch (gc) {
                    case 1:
                    case 29:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 4:
                    case 26:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 7:
                    case 23:
                        enemylist[enemyIdx][3] = 3;
                        break;
                    case 10:
                        enemylist[enemyIdx][3] = 4;
                        break;
                }
                if (gc > 30) {
                    bossWrk[3] = 0;
                    bossWrk[4] = 0;
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(12);
                    j = 2800;
                    yb(enemyIdx, 6, 2);
                }
                return;

            case 12:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }
                if (gc == 5) {
                    for (int i = 0; i < 4; i++) {
                        bossWrk[8 + i] = host.stageFb(20 + (i & 0x1), 0, 65535, 0, 0, 0, 0, enemylist[enemyIdx][5], enemylist[enemyIdx][6]);
                    }
                }

                if (gc > 23) {
                    int d0_12 = gc;
                    if ((d0_12 % 24) == 0) {
                        int bx = enemylist[enemyIdx][5];
                        int by = enemylist[enemyIdx][6];

                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - by, host.getPlayerXFixed() - bx);
                        int phase = bossWrk[4] & 0x3;
                        if (phase == 1) {
                            aim = (aim + 45 + bossWrk[3] * 10) % 360;
                        } else if (phase == 3) {
                            aim = (aim + 315 - bossWrk[3] * 10) % 360;
                        }
                        if (aim < 0) {
                            aim += 360;
                        }

                        int step;
                        switch (host.getLevel()) {
                            case 0:
                                step = 24;
                                break;
                            case 1:
                                step = 16;
                                break;
                            case 2:
                                step = 14;
                                break;
                            case 3:
                            default:
                                step = 12;
                                break;
                        }

                        int width = bossWrk[3];
                        int maxOff = step * width;
                        for (int off = -maxOff; off <= maxOff; off += step) {
                            host.spawnEnemyBullet(335, 24, aim + off, 28, bx, by);
                        }

                        bossWrk[4] += 1;
                        if ((bossWrk[4] & 0x3) == 0) {
                            bossWrk[3] += 1;
                        }
                    }

                    if ((d0_12 & 0x3) == 0) {
                        for (int i = 0; i < 4; ++i) {
                            int si = bossWrk[8 + i];
                            if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                continue;
                            }
                            int sx = enemylist[si][5];
                            int sy = enemylist[si][6];
                            for (int a = 0; a < 360; a += 90) {
                                host.spawnEnemyBullet(271, 0, a, 40, sx, sy);
                            }
                        }
                    }

                    if (host.getLevel() >= 2 && (d0_12 & 0x3) == 0) {
                        for (int i = 0; i < 4; ++i) {
                            int si = bossWrk[8 + i];
                            if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                continue;
                            }
                            int sx = enemylist[si][5];
                            int sy = enemylist[si][6];

                            int ang = (d0_12 << 3) % 360;
                            if ((i & 0x1) == 0) {
                                ang = 360 - ((d0_12 << 4) % 360);
                            }
                            for (int a = 0; a < 360; a += 180) {
                                host.spawnEnemyBullet(675, 0, ang + a, 30, sx, sy);
                                if (host.getLevel() == 3) {
                                    host.spawnEnemyBullet(675, 0, ang + a + 8, 30, sx, sy);
                                    host.spawnEnemyBullet(675, 0, ang + a - 8, 30, sx, sy);
                                }
                            }
                        }
                    }
                    return;
                }

                if (gc >= 5 && gc <= 23) {
                    int d1 = (gc - 5) << 2;
                    int sp = d1 << 3;

                    int baseX = enemylist[enemyIdx][5];
                    int baseY = enemylist[enemyIdx][6];

                    int si8 = bossWrk[8];
                    if (si8 >= 0 && si8 < enemylist.length && enemylist[si8][0] != 0) {
                        int dir = 565;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        enemylist[si8][5] = baseX + se;
                        enemylist[si8][6] = baseY + te;
                    }

                    int si9 = bossWrk[9];
                    if (si9 >= 0 && si9 < enemylist.length && enemylist[si9][0] != 0) {
                        int dir = 335;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        enemylist[si9][5] = baseX + se;
                        enemylist[si9][6] = baseY + te;
                    }

                    int si10 = bossWrk[10];
                    if (si10 >= 0 && si10 < enemylist.length && enemylist[si10][0] != 0) {
                        int dir = 515;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        enemylist[si10][5] = baseX + se;
                        enemylist[si10][6] = baseY + te;
                    }

                    int si11 = bossWrk[11];
                    if (si11 >= 0 && si11 < enemylist.length && enemylist[si11][0] != 0) {
                        int dir = 385;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                        enemylist[si11][5] = baseX + se;
                        enemylist[si11][6] = baseY + te;
                    }
                }
                return;

            case 13:
                bossx[idx] = 97;
                bossy[idx] = 78;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (gc > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 50 + host.getLevel();
                    wb(14);
                }
                return;

            case 14:
                bspellcnt = 39;
                switch (gc) {
                    case 1:
                    case 29:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 4:
                    case 26:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 7:
                    case 23:
                        enemylist[enemyIdx][3] = 3;
                        break;
                    case 10:
                        enemylist[enemyIdx][3] = 4;
                        break;
                }
                if (gc > 30) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(15);
                    j = 2000;
                    yb(enemyIdx, 6, 2);
                }
                return;

            case 15:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }
                if (gc == 0) {
                    bossWrk[3] = 0;
                }
                if (gc == 5) {
                    int cnt = 6;
                    if (host.getLevel() == 0) {
                        cnt = 4;
                    }
                    for (int i = 0; i < cnt; i++) {
                        bossWrk[8 + i] = host.stageFb(20 + (i & 0x1), 0, 65535, 0, 0, 0, 0, enemylist[enemyIdx][5], enemylist[enemyIdx][6]);
                    }
                }

                if (gc > 23) {
                    int d0_15 = gc;
                    if ((d0_15 % 24) == 0) {
                        int bx = enemylist[enemyIdx][5];
                        int by = enemylist[enemyIdx][6];

                        int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - by, host.getPlayerXFixed() - bx);

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
                        int step = 360 / div;

                        if (host.getLevel() <= 1) {
                            for (int a = 0; a < 360; a += step) {
                                host.spawnEnemyBullet(87, 0, aim + a, 16, bx, by);
                            }
                        } else {
                            int parity = bossWrk[3] & 0x1;
                            for (int a = 0; a < 360; a += step) {
                                if ((((a / step) & 0x1) == parity)) {
                                    host.spawnEnemyBullet(87, 0, aim + a, 48, bx, by);
                                } else {
                                    host.spawnEnemyBullet(87, 26, aim + a, 32, bx, by);
                                }
                            }
                        }
                        bossWrk[3] += 1;
                    }

                    if ((d0_15 & 0x1F) == 0) {
                        int idxSel;
                        if (host.getLevel() == 0) {
                            idxSel = (d0_15 % 128) / 32;
                        } else {
                            idxSel = (d0_15 % 192) / 32;
                        }

                        int si = bossWrk[8 + idxSel];
                        if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                            int sx = enemylist[si][5];
                            int sy = enemylist[si][6];

                            int aim = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - sy, host.getPlayerXFixed() - sx);
                            if (host.getLevel() <= 1) {
                                aim = 450;
                            }
                            host.spawnEnemyBullet(239, 35, aim, 16, sx, sy);
                        }
                        return;
                    }
                    return;
                }
                if (gc >= 5 && gc <= 23) {
                    int d1 = (gc - 5) << 2;
                    int baseX = enemylist[enemyIdx][5];
                    int baseY = enemylist[enemyIdx][6];

                    int sp0 = d1 << 3;

                    int si8 = bossWrk[8];
                    if (si8 >= 0 && si8 < enemylist.length && enemylist[si8][0] != 0) {
                        int dir = 540;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp0) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp0) >> 3);
                        enemylist[si8][5] = baseX + se;
                        enemylist[si8][6] = baseY + te;
                    }

                    int si9 = bossWrk[9];
                    if (si9 >= 0 && si9 < enemylist.length && enemylist[si9][0] != 0) {
                        int dir = 360;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp0) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp0) >> 3);
                        enemylist[si9][5] = baseX + se;
                        enemylist[si9][6] = baseY + te;
                    }

                    if (host.getLevel() == 0) {
                        int sp1 = d1 << 2;

                        int si10 = bossWrk[10];
                        if (si10 >= 0 && si10 < enemylist.length && enemylist[si10][0] != 0) {
                            int dir = 570;
                            int se = (int) ((((long) Trig.cos(dir)) * (long) sp1) >> 3);
                            int te = (int) ((((long) Trig.sin(dir)) * (long) sp1) >> 3);
                            enemylist[si10][5] = baseX + se;
                            enemylist[si10][6] = baseY + te;
                        }

                        int si11 = bossWrk[11];
                        if (si11 >= 0 && si11 < enemylist.length && enemylist[si11][0] != 0) {
                            int dir = 330;
                            int se = (int) ((((long) Trig.cos(dir)) * (long) sp1) >> 3);
                            int te = (int) ((((long) Trig.sin(dir)) * (long) sp1) >> 3);
                            enemylist[si11][5] = baseX + se;
                            enemylist[si11][6] = baseY + te;
                        }

                        return;
                    }

                    int sp1 = (d1 << 3) - d1;
                    int sp2 = d1 << 2;

                    int si10 = bossWrk[10];
                    if (si10 >= 0 && si10 < enemylist.length && enemylist[si10][0] != 0) {
                        int dir = 570;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp1) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp1) >> 3);
                        enemylist[si10][5] = baseX + se;
                        enemylist[si10][6] = baseY + te;
                    }

                    int si11 = bossWrk[11];
                    if (si11 >= 0 && si11 < enemylist.length && enemylist[si11][0] != 0) {
                        int dir = 330;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp1) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp1) >> 3);
                        enemylist[si11][5] = baseX + se;
                        enemylist[si11][6] = baseY + te;
                    }

                    int si12 = bossWrk[12];
                    if (si12 >= 0 && si12 < enemylist.length && enemylist[si12][0] != 0) {
                        int dir = 600;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp2) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp2) >> 3);
                        enemylist[si12][5] = baseX + se;
                        enemylist[si12][6] = baseY + te;
                    }

                    int si13 = bossWrk[13];
                    if (si13 >= 0 && si13 < enemylist.length && enemylist[si13][0] != 0) {
                        int dir = 300;
                        int se = (int) ((((long) Trig.cos(dir)) * (long) sp2) >> 3);
                        int te = (int) ((((long) Trig.sin(dir)) * (long) sp2) >> 3);
                        enemylist[si13][5] = baseX + se;
                        enemylist[si13][6] = baseY + te;
                    }

                    return;
                }
                return;

            case 16:
                bossx[idx] = 97;
                bossy[idx] = 48;
                enemylist[enemyIdx][14] = 393216;
                enemylist[enemyIdx][15] = 393216;
                batkf = false;
                host.setBbaria(2);
                if (gc > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 54 + host.getLevel();
                    wb(17);
                }
                return;

            case 17:
                bspellcnt = 39;
                switch (gc) {
                    case 1:
                    case 29:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 4:
                    case 26:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 7:
                    case 23:
                        enemylist[enemyIdx][3] = 3;
                        break;
                    case 10:
                        enemylist[enemyIdx][3] = 4;
                        break;
                }
                if (gc > 30) {
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
                {
                    int d0_18 = gc % 140;
                    if (d0_18 == 130) {
                        if (host.getPlayerXFixed() > enemylist[enemyIdx][5]) {
                            bossx[idx] -= 30;
                            if (bossx[idx] < 20) {
                                bossx[idx] = 20;
                            }
                        } else {
                            bossx[idx] += 30;
                            if (bossx[idx] > 174) {
                                bossx[idx] = 174;
                            }
                        }
                    }

                    int bx = enemylist[enemyIdx][5];
                    int by = enemylist[enemyIdx][6];

                    if (d0_18 == 0) {
                        int dx = host.getPlayerXFixed() - bx;
                        int dy = host.getPlayerYFixed() - by;
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(dy, dx);
                    }

                    if (d0_18 >= 0 && d0_18 <= 46) {
                        int d5 = ((d0_18 * 5) - 100) << 1;
                        int d6 = enemylist[enemyIdx][10];

                        int dist0 = d5 << 3;
                        int se0 = (int) ((((long) Trig.cos(d6)) * (long) dist0) >> 3);
                        int te0 = (int) ((((long) Trig.sin(d6)) * (long) dist0) >> 3);
                        int x0 = bx + se0;
                        int y0 = by + te0;

                        int se1 = (int) ((((long) Trig.cos(d6 + 90)) * 240L) >> 3);
                        int te1 = (int) ((((long) Trig.sin(d6 + 90)) * 240L) >> 3);
                        host.spawnEnemyBullet(81, 38, 540, 0, x0 + se1, y0 + te1);

                        int se2 = (int) ((((long) Trig.cos(d6 + 270)) * 240L) >> 3);
                        int te2 = (int) ((((long) Trig.sin(d6 + 270)) * 240L) >> 3);
                        host.spawnEnemyBullet(81, 38, 360, 0, x0 + se2, y0 + te2);
                    }

                    if (d0_18 == 50) {
                        int d6 = enemylist[enemyIdx][10];

                        int se0 = (int) ((((long) Trig.cos(d6)) * -800L) >> 3);
                        int te0 = (int) ((((long) Trig.sin(d6)) * -800L) >> 3);
                        int x0 = bx + se0;
                        int y0 = by + te0;

                        host.spawnEnemyBullet(94, 39, enemylist[enemyIdx][10], 20, x0, y0);

                        int div;
                        switch (host.getLevel()) {
                            case 0:
                            case 1:
                                div = 8;
                                break;
                            case 2:
                                div = 10;
                                break;
                            case 3:
                            default:
                                div = 12;
                                break;
                        }
                        int step = 360 / div;
                        for (int a = 0; a < 360; a += step) {
                            int se = (int) ((((long) Trig.cos(a)) * 320L) >> 3);
                            int te = (int) ((((long) Trig.sin(a)) * 320L) >> 3);
                            host.spawnEnemyBullet(88, 39, enemylist[enemyIdx][10], 20, x0 + se, y0 + te);
                        }
                    }

                    if (d0_18 >= 60 && d0_18 <= 130) {
                        int div;
                        int period;
                        switch (host.getLevel()) {
                            case 0:
                                div = 3;
                                period = 4;
                                break;
                            case 1:
                                div = 5;
                                period = 3;
                                break;
                            case 2:
                                div = 8;
                                period = 3;
                                break;
                            case 3:
                            default:
                                div = 8;
                                period = 2;
                                break;
                        }
                        if ((d0_18 % period) == 0) {
                            int step = 360 / div;
                            int baseAng = d0_18 << 2;
                            for (int a = 0; a < 360; a += step) {
                                host.spawnEnemyBullet(271, 0, baseAng + a, 24, bx, by);
                            }
                        }
                    }
                }
                return;

            case 19:
                bossx[idx] = 97;
                bossy[idx] = 64;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (gc > 30) {
                    host.setQ(true);
                    enemylist[enemyIdx][14] = 65536;
                    enemylist[enemyIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 58 + host.getLevel();
                    wb(20);
                }
                return;

            case 20:
                bspellcnt = 49;
                switch (gc) {
                    case 1:
                    case 29:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 4:
                    case 26:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 7:
                    case 23:
                        enemylist[enemyIdx][3] = 3;
                        break;
                    case 10:
                        enemylist[enemyIdx][3] = 4;
                        break;
                }
                if (gc > 30) {
                    enemylist[enemyIdx][14] = 32768;
                    enemylist[enemyIdx][15] = 32768;
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(21);
                }
                return;

            case 21:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }
                if (gc == 5) {
                    bossWrk[8] = host.stageFb(20, 0, 65535, 0, 0, 0, 0, enemylist[enemyIdx][5], enemylist[enemyIdx][6]);
                    bossWrk[9] = host.stageFb(21, 0, 65535, 0, 0, 0, 0, enemylist[enemyIdx][5], enemylist[enemyIdx][6]);
                }
                if (gc > 15) {
                    int bx = enemylist[enemyIdx][5] >> 16;

                    int si8 = bossWrk[8];
                    if (si8 >= 0 && si8 < enemylist.length && enemylist[si8][0] != 0) {
                        enemylist[si8][5] = (bx - 48) << 16;
                        if (enemylist[si8][5] < 655360) {
                            enemylist[si8][5] = 655360;
                        }
                    }

                    int si9 = bossWrk[9];
                    if (si9 >= 0 && si9 < enemylist.length && enemylist[si9][0] != 0) {
                        enemylist[si9][5] = (bx + 48) << 16;
                        if (enemylist[si9][5] > 12058624) {
                            enemylist[si9][5] = 12058624;
                        }
                    }

                    int d0_21 = gc;
                    int bossXFixed = enemylist[enemyIdx][5];
                    int bossYFixed = enemylist[enemyIdx][6];

                    int mod36 = d0_21 % 36;
                    if (mod36 == 0 || mod36 == 18) {
                        host.spawnEnemyBullet(-1, 29, 450, 32, bossXFixed, bossYFixed);
                    }
                    if (mod36 == 9) {
                        if (si8 >= 0 && si8 < enemylist.length && enemylist[si8][0] != 0) {
                            host.spawnEnemyBullet(-1, 29, 450, 32, enemylist[si8][5], bossYFixed);
                        }
                    }
                    if (mod36 == 27) {
                        if (si9 >= 0 && si9 < enemylist.length && enemylist[si9][0] != 0) {
                            host.spawnEnemyBullet(-1, 29, 450, 32, enemylist[si9][5], bossYFixed);
                        }
                    }

                    if (d0_21 % 100 == 0) {
                        int px = host.getPlayerXFixed();
                        int py = host.getPlayerYFixed();

                        if (si8 >= 0 && si8 < enemylist.length && enemylist[si8][0] != 0) {
                            int sx = enemylist[si8][5];
                            int sy = enemylist[si8][6];
                            int aim = 470;
                            if (py < 7929856) {
                                aim = EnemyBulletSystem.arcTan2Deg(py - sy, px - sx);
                            }
                            for (int k = 0; k < 10; k++) {
                                int sp = 40 + (k << 2);
                                host.spawnEnemyBullet(335, 0, aim - 20, sp, sx, sy);
                                host.spawnEnemyBullet(335, 0, aim, sp, sx, sy);
                                host.spawnEnemyBullet(335, 0, aim + 20, sp, sx, sy);
                            }
                        }

                        if (si9 >= 0 && si9 < enemylist.length && enemylist[si9][0] != 0) {
                            int sx = enemylist[si9][5];
                            int sy = enemylist[si9][6];
                            int aim = 430;
                            if (py < 7929856) {
                                aim = EnemyBulletSystem.arcTan2Deg(py - sy, px - sx);
                            }
                            for (int k = 0; k < 10; k++) {
                                int sp = 40 + (k << 2);
                                host.spawnEnemyBullet(335, 0, aim + 20, sp, sx, sy);
                                host.spawnEnemyBullet(335, 0, aim, sp, sx, sy);
                                host.spawnEnemyBullet(335, 0, aim - 20, sp, sx, sy);
                            }
                        }
                    }

                    if ((d0_21 & 1) == 0) {
                        if (si8 >= 0 && si8 < enemylist.length && enemylist[si8][0] != 0) {
                            int sx = enemylist[si8][5];
                            int sy = enemylist[si8][6];
                            int aim = EnemyBulletSystem.arcTan2Deg(bossYFixed - sy, bossXFixed - sx);

                            host.spawnEnemyBullet(239, 0, aim + 30, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim - 30, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim + 180 + 30, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim + 180, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim + 180 - 30, 40, sx, sy);
                        }

                        if (si9 >= 0 && si9 < enemylist.length && enemylist[si9][0] != 0) {
                            int sx = enemylist[si9][5];
                            int sy = enemylist[si9][6];
                            int aim = EnemyBulletSystem.arcTan2Deg(bossYFixed - sy, bossXFixed - sx);

                            host.spawnEnemyBullet(239, 0, aim + 30, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim - 30, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim + 180 + 30, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim + 180, 40, sx, sy);
                            host.spawnEnemyBullet(239, 0, aim + 180 - 30, 40, sx, sy);
                        }
                    }

                    if (gc > 100) {
                        bossx[idx] = host.getPlayerXFixed() >> 16;
                    }
                    return;
                }

                if (gc >= 5) {
                    int t = gc - 5;

                    int si8 = bossWrk[8];
                    if (si8 >= 0 && si8 < enemylist.length && enemylist[si8][0] != 0) {
                        enemylist[si8][5] = (97 - (48 * t / 10)) << 16;
                        enemylist[si8][6] += 196608;
                    }

                    int si9 = bossWrk[9];
                    if (si9 >= 0 && si9 < enemylist.length && enemylist[si9][0] != 0) {
                        enemylist[si9][5] = (97 + (48 * t / 10)) << 16;
                        enemylist[si9][6] += 196608;
                    }
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
                switch (gc) {
                    case 5:
                        enemylist[enemyIdx][3] = 5;
                        break;
                    case 10:
                        enemylist[enemyIdx][3] = 6;
                        break;
                    case 15:
                        enemylist[enemyIdx][3] = 7;
                        break;
                    case 20:
                        enemylist[enemyIdx][3] = 8;
                        break;
                    case 25:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }
                if (gc > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 112;
                    wb(38);
                }
                return;

            case 38:
                bspellcnt = 60;
                switch (gc) {
                    case 1:
                    case 28:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 5:
                    case 25:
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
            {
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }

                int bossXFixed_39 = enemylist[enemyIdx][5];
                int bossYFixed_39 = enemylist[enemyIdx][6];

                if (gc == 5) {
                    for (int i = 0; i < 8; ++i) {
                        bossWrk[8 + i] = host.stageFb(20 + (i & 0x1), 0, 65535, 0, 0, 0, 0, bossXFixed_39, bossYFixed_39);
                    }
                }

                if (gc > 23) {
                    int d0_39 = gc - 24;

                    if (d0_39 > 31) {
                        int d7_39 = 69;
                        if (bossWrk[3] > 63) {
                            d7_39 = 33;
                        }
                        if (bossWrk[3] > 126) {
                            d7_39 = 17;
                        }
                        if (bossWrk[3] > 189) {
                            d7_39 = 9;
                        }
                        if (bossWrk[3] > 252) {
                            d7_39 = 5;
                        }
                        if (bossWrk[3] > 315) {
                            d7_39 = 3;
                        }
                        if (bossWrk[3] > 378) {
                            d7_39 = 1;
                        }

                        if ((d0_39 & d7_39) == 0) {
                            int dx = host.getPlayerXFixed() - bossXFixed_39;
                            int dy = host.getPlayerYFixed() - bossYFixed_39;
                            int aim = EnemyBulletSystem.arcTan2Deg(dy, dx);
                            enemylist[enemyIdx][10] = aim;

                            int d6_39 = 24;
                            int step_39 = 180 / d6_39;
                            for (int off = -90; off < 90; off += step_39) {
                                int idxOff = ((off + 90) / step_39) & 0x1;
                                if (idxOff == (bossWrk[3] & 0x1)) {
                                    host.spawnEnemyBullet(239, 0, aim + off, 48, bossXFixed_39, bossYFixed_39);
                                } else {
                                    host.spawnEnemyBullet(239, 26, aim + off, 32, bossXFixed_39, bossYFixed_39);
                                }
                            }
                            bossWrk[3] += 1;
                        }
                    }

                    if ((d0_39 & 0x3) == 0) {
                        int d5_39 = 0;
                        if (d0_39 > 15) {
                            d5_39 += (((d0_39 - 15) << 1) % 360);
                        }
                        if (d0_39 > 31) {
                            d5_39 += (((d0_39 - 22) << 2) % 360);
                        }

                        for (int i = 0; i < 8; ++i) {
                            int si = bossWrk[8 + i];
                            if (si < 0 || si >= enemylist.length || enemylist[si][0] == 0) {
                                continue;
                            }

                            int sx = enemylist[si][5];
                            int sy = enemylist[si][6];
                            int a = d5_39;
                            if ((i & 0x1) == 0) {
                                a = 360 - d5_39;
                            }
                            for (int add = 0; add < 360; add += 90) {
                                a += add;
                                host.spawnEnemyBullet(603, 0, a, 16, sx, sy);
                            }
                        }
                    }
                    return;
                }

                if (gc >= 5) {
                    int d1_39 = (gc - 5) << 2;

                    int sp;
                    int dir;
                    int se;
                    int te;
                    int si;

                    sp = d1_39 << 2;
                    dir = 545;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    si = bossWrk[8];
                    if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                        enemylist[si][5] = bossXFixed_39 + se;
                        enemylist[si][6] = bossYFixed_39 + te;
                    }

                    sp = d1_39 << 2;
                    dir = 355;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    si = bossWrk[9];
                    if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                        enemylist[si][5] = bossXFixed_39 + se;
                        enemylist[si][6] = bossYFixed_39 + te;
                    }

                    sp = (d1_39 << 2) - 5;
                    dir = 600;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    si = bossWrk[10];
                    if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                        enemylist[si][5] = bossXFixed_39 + se;
                        enemylist[si][6] = bossYFixed_39 + te;
                    }

                    sp = (d1_39 << 2) - 5;
                    dir = 300;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    si = bossWrk[11];
                    if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                        enemylist[si][5] = bossXFixed_39 + se;
                        enemylist[si][6] = bossYFixed_39 + te;
                    }

                    sp = d1_39 << 3;
                    dir = 550;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    si = bossWrk[12];
                    if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                        enemylist[si][5] = bossXFixed_39 + se;
                        enemylist[si][6] = bossYFixed_39 + te;
                    }

                    sp = d1_39 << 3;
                    dir = 350;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    si = bossWrk[13];
                    if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                        enemylist[si][5] = bossXFixed_39 + se;
                        enemylist[si][6] = bossYFixed_39 + te;
                    }

                    sp = d1_39 * 10;
                    dir = 520;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    si = bossWrk[14];
                    if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                        enemylist[si][5] = bossXFixed_39 + se;
                        enemylist[si][6] = bossYFixed_39 + te;
                    }

                    sp = d1_39 * 10;
                    dir = 380;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp) >> 3);
                    si = bossWrk[15];
                    if (si >= 0 && si < enemylist.length && enemylist[si][0] != 0) {
                        enemylist[si][5] = bossXFixed_39 + se;
                        enemylist[si][6] = bossYFixed_39 + te;
                    }
                    return;
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
