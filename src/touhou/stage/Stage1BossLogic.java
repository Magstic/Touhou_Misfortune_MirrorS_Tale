package touhou.stage;

import touhou.EnemyBulletSystem;
import touhou.Trig;

final class Stage1BossLogic extends AbstractBossStageLogic {
    void tickBossImpl(int enemyIdx) {
        nb(enemyIdx);
    }

    void tickMidbossImpl(int enemyIdx) {
        kb(enemyIdx);
    }

    //boss
    void nb(int targetIdx) {
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
                if (host.getMGCnt() < 30) {
                    bossx[0] = 97;
                    bossy[0] = 56;
                    bossx[1] = 77;
                    bossy[1] = 44;
                    bossx[2] = 117;
                    bossy[2] = 44;
                }
                bspellcnt = 255;
                batkf = false;
                host.setResetFlag(4);
                if (mt == 102) {
                    if (host.getMGCnt() == 30) {
                        enemylist[targetIdx][14] = 131072;
                        enemylist[targetIdx][15] = 131072;
                        bossx[1] = 37;
                    } else if (host.getMGCnt() == 45) {
                        enemylist[targetIdx][14] = 458752;
                        enemylist[targetIdx][15] = 458752;
                        bossx[1] = -30;
                    }
                }
                enemylist[targetIdx][16] = 1;
                return;
            case 2:
                if (mt != 101) {
                    return;
                }
                if (host.getMGCnt() == 0) {
                    host.setBbaria(1);
                }
                if (host.getGameMode() != 3) {
                    bossx[0] = 97;
                    bossy[0] = 44;
                    bossx[2] = 30;
                    bossy[2] = 80;
                }

                bwave[idx] = 120 * idx;
                bspellcnt = 255;
                batkf = false;
                if (host.getMGCnt() <= 10) {
                    return;
                }
                if (host.getGameMode() == 3) {
                    bspellcnt = 120;

                    int step = host.getSpellPracticeBossStep();
                    wb(step);

                    int d0 = 3;
                    j = 1800;
                    switch (step) {
                        case 10:
                            j = 700;
                            d0 = 1;
                            break;
                        case 13:
                            j = 800;
                            d0 = 2;
                            break;
                        case 16:
                            j = 1800;
                            break;
                        case 19:
                            j = 1100;
                            d0 = 0;
                            break;
                        case 37:
                            j = 4800;
                            d0 = 0;
                            break;
                        case 40:
                            j = 4400;
                            d0 = 1;
                            break;
                        case 43:
                            j = 5200;
                            d0 = 2;
                            break;
                    }
                    yb(targetIdx, d0, 3);
                    return;
                }
                if (host.getBstock() == 1) {
                    j = 900;
                    yb(targetIdx, 3, 1);
                    bspellcnt = 39;
                    wb(3);
                    host.getBossWrk()[24] = 0;
                    return;
                }
                if (host.getBstock() == 0) {
                    j = 1800;
                    if (host.getPower() >= 96) {
                        j += 450;
                    }
                    yb(targetIdx, 3, 3);
                    bspellcnt = 39;
                    wb(16);
                    host.setBbaria(2);
                    return;
                }
                return;
            case 3:
                if (mt == 101) {
                    if (m == 2 || bspellcnt < 0) {
                        host.setResetFlag(4);
                        j = 65535;
                        yb(l, n, 2);
                        int t = host.getBossWrk()[24];
                        if (t > 36) {
                            wb(10);
                            return;
                        }
                        if (t < -36) {
                            wb(13);
                            return;
                        }
                        wb(19);
                        return;
                    }

                    host.setBbaria(0);
                    batkf = true;
                    enemylist[targetIdx][14] = 131072;
                    enemylist[targetIdx][15] = 196608;

                    int gc = host.getMGCnt();
                    if (gc == 10 || gc == 85 || gc == 185 || gc == 285 || gc == 385) {
                        bossx[0] = 144;
                        bossy[0] = 80;
                    } else if (gc == 35 || gc == 135 || gc == 235 || gc == 335) {
                        bossx[0] = 50;
                        bossy[0] = 80;
                    }

                    if (gc == 0 || gc == 90 || gc == 190 || gc == 290 || gc == 390) {
                        bossx[2] = 97;
                        bossy[2] = 48;
                    } else if (gc == 40 || gc == 240) {
                        bossx[2] = 147;
                        bossy[2] = 28;
                    } else if (gc == 140 || gc == 340) {
                        bossx[2] = 47;
                        bossy[2] = 28;
                    }

                    int px = host.getPlayerXFixed();
                    int[] bossWrk = host.getBossWrk();
                    if (px < (73 << 16)) {
                        bossWrk[24] -= 1;
                    } else if (px > (121 << 16)) {
                        bossWrk[24] += 1;
                    } else {
                        if (bossWrk[24] > 5) {
                            bossWrk[24] -= 1;
                        }
                        if (bossWrk[24] < -5) {
                            bossWrk[24] += 1;
                        }
                    }
                }

                int gc3 = host.getMGCnt();
                if (gc3 > 10) {
                    int interval;
                    int bulletMoveType;
                    switch (host.getLevel()) {
                        case 0:
                            interval = 8;
                            bulletMoveType = 0;
                            break;
                        case 1:
                            interval = 6;
                            bulletMoveType = 0;
                            break;
                        case 2:
                            interval = 7;
                            bulletMoveType = 3;
                            break;
                        case 3:
                        default:
                            interval = 4;
                            bulletMoveType = 3;
                            break;
                    }

                    if (mt == 101) {
                        if ((gc3 % interval) == 0) {
                            int ang0 = gc3 << 3;
                            int se0 = (int) (((long) Trig.cos(ang0) * (long) 160) >> 3);
                            int te0 = (int) (((long) Trig.sin(ang0) * (long) 160) >> 3);
                            int sx0 = enemylist[targetIdx][5] + (se0 << 1);
                            int sy0 = enemylist[targetIdx][6] + te0;
                            host.spawnEnemyBullet(81, bulletMoveType, ang0 + 270, 32, sx0, sy0);

                            int ang1 = (gc3 << 3) + 180;
                            int se1 = (int) (((long) Trig.cos(ang1) * (long) 160) >> 3);
                            int te1 = (int) (((long) Trig.sin(ang1) * (long) 160) >> 3);
                            int sx1 = enemylist[targetIdx][5] + (se1 << 1);
                            int sy1 = enemylist[targetIdx][6] + te1;
                            host.spawnEnemyBullet(81, bulletMoveType, (gc3 << 3) + 270, 32, sx1, sy1);
                        }
                    }

                    if (mt == 103) {
                        enemylist[targetIdx][14] = 262144;
                        enemylist[targetIdx][15] = 196608;

                        if (!(gc3 >= 40 && (gc3 <= 60 || gc3 >= 90) && (gc3 <= 110 || gc3 >= 140) && (gc3 <= 160 || gc3 >= 190) && (gc3 <= 210 || gc3 >= 240) && (gc3 <= 260 || gc3 >= 290))) {
                            int d0 = (gc3 - 10) % 60;
                            if (!(d0 >= 20 && (d0 < 30 || d0 >= 50))) {
                                d0 = (gc3 - 10) % 20;
                                if (d0 > 10) {
                                    d0 = 20 - d0;
                                }
                                int dir = 45 + d0 * 9;
                                host.spawnEnemyBullet(86, 2, dir, 96, enemylist[targetIdx][5], enemylist[targetIdx][6]);
                                if (host.getLevel() > 2) {
                                    host.spawnEnemyBullet(84, 0, dir, 24, enemylist[targetIdx][5], enemylist[targetIdx][6]);
                                }
                            }
                        }
                    }
                }

                if (host.getMGCnt() > 400) {
                    host.setMGCnt(11);
                }
                return;

            case 10:
                bossx[2] = 97;
                bossy[2] = 68;
                bossx[0] = 164;
                bossy[0] = -32;
                bossx[1] = 30;
                bossy[1] = -32;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 6 + host.getLevel();
                    wb(11);
                }
                return;

            case 11:
                if (mt != 103) {
                    return;
                }
                bspellcnt = 34;
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
                    wb(12);
                    j = 700;
                    if (host.getPower() >= 96) {
                        j += 175;
                    }
                    yb(targetIdx, 3, 2);
                }
                return;

            case 12:
                if (mt != 103) {
                    return;
                }
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int gc12 = host.getMGCnt();

                int n12;
                switch (host.getLevel()) {
                    case 0:
                        n12 = 6;
                        break;
                    case 1:
                        n12 = 8;
                        break;
                    case 2:
                        n12 = 12;
                        break;
                    case 3:
                    default:
                        n12 = 18;
                        break;
                }

                switch (gc12) {
                    case 2:
                        enemylist[targetIdx][3] = 0;
                        bossx[2] = 154;
                        bossy[2] = 38 + ((bspellcnt % 10) << 2);
                        break;
                    case 102:
                        enemylist[targetIdx][3] = 0;
                        bossx[2] = 40;
                        bossy[2] = 38 + ((bspellcnt % 10) << 2);
                        break;

                    case 15:
                    case 115:
                        for (int a = 0; a < 360; a += 360 / n12) {
                            host.spawnEnemyBullet(86, 10, a, 32, enemylist[targetIdx][5], enemylist[targetIdx][6]);
                        }
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 31:
                    case 131:
                        enemylist[targetIdx][3] = 1;
                        break;

                    case 18:
                    case 118:
                        for (int a = 0; a < 360; a += 360 / n12) {
                            host.spawnEnemyBullet(86, 11, a, 40, enemylist[targetIdx][5], enemylist[targetIdx][6]);
                        }
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 28:
                    case 128:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 21:
                    case 121:
                        enemylist[targetIdx][3] = 3;

                        for (int a = 0; a < 360; a += 360 / n12) {
                            host.spawnEnemyBullet(86, 10, a, 56, enemylist[targetIdx][5], enemylist[targetIdx][6]);
                        }
                        break;

                    case 30:
                    case 130: {
                        int lvl12 = host.getLevel();
                        if (lvl12 >= 2) {
                            host.spawnEnemyBullet(-1, 4, 0, 12, enemylist[targetIdx][5], enemylist[targetIdx][6]);
                            break;
                        }

                        int nAim = (lvl12 == 0) ? 8 : 12;
                        int dx = host.getPlayerXFixed() - enemylist[targetIdx][5];
                        int dy = host.getPlayerYFixed() - enemylist[targetIdx][6];
                        int baseAng = EnemyBulletSystem.arcTan2Deg(dy, dx);

                        for (int l = 0; l < 360; l += 360 / nAim) {
                            for (int d3 = 0; d3 < 360; d3 += 20) {
                                int se = (int) (((long) Trig.cos(d3) * (long) 160) >> 3);
                                int te = (int) (((long) Trig.sin(d3) * (long) 160) >> 3);
                                int sx = enemylist[targetIdx][5] + se;
                                int sy = enemylist[targetIdx][6] + te;
                                host.spawnEnemyBullet(83, 0, baseAng + l, 40, sx, sy);
                            }
                        }
                        break;
                    }
                }
                if (gc12 > 190) {
                    host.setMGCnt(0);
                }
                return;

            case 13:
                bossx[1] = 97;
                bossy[1] = 68;
                bossx[0] = 164;
                bossy[0] = -42;
                bossx[2] = 30;
                bossy[2] = -42;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 10 + host.getLevel();
                    wb(14);
                }
                return;

            case 14:
                if (mt != 102) {
                    return;
                }
                bspellcnt = 34;
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
                    wb(15);
                    j = 800;
                    if (host.getPower() >= 96) {
                        j += 200;
                    }
                    yb(targetIdx, 3, 2);
                }
                return;

            case 15:
                if (mt != 102) {
                    return;
                }
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int gc15 = host.getMGCnt();
                int lvl15 = host.getLevel();
                int ex15 = enemylist[targetIdx][5];
                int ey15 = enemylist[targetIdx][6];

                if (gc15 > 20 && gc15 < 80) {
                    int radiusParam = (gc15 - 20) << 4;
                    int ang = (gc15 - 20) * 20;

                    int se = (int) (((long) Trig.cos(ang) * (long) radiusParam) >> 3);
                    int te = (int) (((long) Trig.sin(ang) * (long) radiusParam) >> 3);
                    int sx = ex15 + (se << 1);
                    int sy = ey15 + te;
                    if (sy < 7929856) {
                        if (lvl15 != 0 || (gc15 & 0x1) == 0) {
                            host.spawnEnemyBullet(83, 3, 0, 40, sx, sy);
                        }
                    }

                    if (lvl15 == 2) {
                        int ang2 = ang + 180;
                        int se2 = (int) (((long) Trig.cos(ang2) * (long) radiusParam) >> 3);
                        int te2 = (int) (((long) Trig.sin(ang2) * (long) radiusParam) >> 3);
                        int sx2 = ex15 + (se2 << 1);
                        int sy2 = ey15 + te2;
                        if (sy2 < 7929856) {
                            host.spawnEnemyBullet(83, 3, 0, 40, sx2, sy2);
                        }
                    }
                    if (lvl15 == 3) {
                        int ang2 = ang + 120;
                        int se2 = (int) (((long) Trig.cos(ang2) * (long) radiusParam) >> 3);
                        int te2 = (int) (((long) Trig.sin(ang2) * (long) radiusParam) >> 3);
                        int sx2 = ex15 + (se2 << 1);
                        int sy2 = ey15 + te2;
                        if (sy2 < 7929856) {
                            host.spawnEnemyBullet(83, 3, 0, 40, sx2, sy2);
                        }

                        int ang3 = ang + 240;
                        int se3 = (int) (((long) Trig.cos(ang3) * (long) radiusParam) >> 3);
                        int te3 = (int) (((long) Trig.sin(ang3) * (long) radiusParam) >> 3);
                        int sx3 = ex15 + (se3 << 1);
                        int sy3 = ey15 + te3;
                        if (sy3 < 7929856) {
                            host.spawnEnemyBullet(83, 3, 0, 40, sx3, sy3);
                        }
                    }
                }

                if (gc15 > 90 && gc15 < 140 && (gc15 % 6) == 0) {
                    int div;
                    switch (lvl15) {
                        case 0:
                            div = 3;
                            break;
                        case 1:
                            div = 4;
                            break;
                        case 2:
                            div = 5;
                            break;
                        case 3:
                        default:
                            div = 6;
                            break;
                    }

                    int baseAng = (gc15 / 6) * 15;
                    for (int i = 0; i < 10; i++) {
                        int radiusParam = (8 + (i & 0x1) * 10) << 3;
                        int ang = gc15 * 10 + i * 36;
                        int se = (int) (((long) Trig.cos(ang) * (long) radiusParam) >> 3);
                        int te = (int) (((long) Trig.sin(ang) * (long) radiusParam) >> 3);
                        int sx = ex15 + se;
                        int sy = ey15 + te;
                        for (int a = 0; a < 360; a += 360 / div) {
                            host.spawnEnemyBullet(83, 0, baseAng + a, 56, sx, sy);
                        }
                    }
                }

                if (gc15 == 90) {
                    if (bossx[1] < 20) {
                        bossx[1] = 134;
                    }
                    if (bossx[1] > 174) {
                        bossx[1] = 60;
                    }
                }
                if (gc15 == 155) {
                    int px15 = host.getPlayerXFixed();
                    int mt15;
                    if (px15 < enemylist[targetIdx][5]) {
                        bossx[1] += 40;
                        mt15 = 11;
                    } else {
                        bossx[1] -= 40;
                        mt15 = 10;
                    }

                    int n;
                    switch (lvl15) {
                        case 0:
                            n = 2;
                            break;
                        case 1:
                            n = 3;
                            break;
                        case 2:
                            n = 4;
                            break;
                        case 3:
                        default:
                            n = 6;
                            break;
                    }

                    for (int v = 0; v < 36; v += 36 / n) {
                        int d5 = v >> 2;
                        int sp1 = (12 - d5) << 3;
                        int sp2 = (3 + d5) << 3;
                        for (int k = 0; k < 12; k++) {
                            int a = v + (k * 36);
                            int sp = ((k & 0x1) == 0) ? sp1 : sp2;
                            host.spawnEnemyBullet(83, mt15, a, sp, ex15, ey15);
                        }
                    }
                }
                if (gc15 > 185) {
                    host.setMGCnt(0);
                }
                return;

            case 16:
                bossx[0] = 97;
                bossy[0] = 68;
                bossx[1] = 30;
                bossy[1] = 48;
                bossx[2] = 164;
                bossy[2] = 48;
                enemylist[targetIdx][14] = 327680;
                enemylist[targetIdx][15] = 327680;
                batkf = false;
                enemylist[targetIdx][3] = 0;
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 14 + host.getLevel();
                    wb(17);
                }
                return;

            case 17:
                bspellcnt = 39;
                if (host.getMGCnt() > 30) {
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(18);
                }
                return;

            case 18:
                int gc18 = host.getMGCnt();
                int lvl18 = host.getLevel();
                int[] bossWrk18 = host.getBossWrk();

                host.setBbaria(0);
                batkf = true;

                if (mt == 101) {
                    bossWrk18[25] = targetIdx;
                    if (bspellcnt < 0) {
                        j = 0;
                        host.setQ(false);
                        host.stageJb(targetIdx);
                        return;
                    }

                    switch (gc18) {
                        case 2:
                            bossx[1] = 10;
                            bossx[2] = 184;
                            break;
                        case 5:
                            enemylist[targetIdx][3] = 1;
                            enemylist[targetIdx][14] = 131072;
                            enemylist[targetIdx][15] = 131072;
                            break;
                        case 10:
                            enemylist[targetIdx][3] = 2;
                            bossy[0] = 28;
                            bossx[1] = 184;
                            bossy[1] = 78;
                            bossx[2] = 10;
                            bossy[2] = 78;
                            break;
                        case 40:
                            bossx[1] = 10;
                            bossy[1] = 108;
                            bossx[2] = 184;
                            bossy[2] = 108;
                            break;
                        case 70:
                            enemylist[targetIdx][3] = 4;
                            bossy[0] = 128;
                            enemylist[targetIdx][14] = 524288;
                            enemylist[targetIdx][15] = 524288;
                            break;
                        case 80:
                            bossy[1] = 48;
                            bossy[2] = 48;
                            break;
                        case 110:
                            enemylist[targetIdx][3] = 0;
                            enemylist[targetIdx][14] = 131072;
                            enemylist[targetIdx][15] = 131072;
                            break;
                        case 115:
                            enemylist[targetIdx][3] = 1;
                            bossy[0] = 28;
                            bossy[1] = 98;
                            bossy[2] = 98;
                            break;
                        case 120:
                            enemylist[targetIdx][3] = 2;
                            break;
                        case 145:
                            enemylist[targetIdx][3] = 1;
                            break;
                        case 150:
                            enemylist[targetIdx][3] = 0;
                            break;
                        case 155:
                            bossx[1] = 224;
                            bossy[1] = -22;
                            bossx[2] = -30;
                            bossy[2] = -22;
                            break;
                        case 185:
                            enemylist[targetIdx][3] = 4;
                            bossy[0] = 158;
                            enemylist[targetIdx][14] = 524288;
                            enemylist[targetIdx][15] = 524288;
                            bossx[1] = 30;
                            bossx[2] = 164;
                            break;
                        case 200:
                            enemylist[targetIdx][3] = 1;
                            enemylist[targetIdx][15] = 196608;
                            bossy[0] = 68;
                            break;
                        case 203:
                            enemylist[targetIdx][3] = 2;
                            break;
                        case 206:
                            enemylist[targetIdx][3] = 3;
                            break;
                        case 215:
                            enemylist[targetIdx][3] = 2;
                            bossy[1] = 48;
                            bossy[2] = 48;
                            break;
                        case 220:
                            enemylist[targetIdx][3] = 1;
                            break;
                        case 225:
                            enemylist[targetIdx][3] = 0;
                            break;
                        case 230:
                            bossy[0] = 38;
                            bossy[1] = 68;
                            bossy[2] = 68;
                            break;
                    }

                    if ((gc18 > 70 && gc18 < 90) || (gc18 > 185 && gc18 < 200) || gc18 > 230) {
                        host.convertEnemyBulletsTo81Rain(enemylist[targetIdx][6], gc18);
                    }
                } else if (mt == 102 || mt == 103) {
                    switch (gc18) {
                        case 5:
                            enemylist[targetIdx][15] = 65536;
                            break;
                        case 70:
                            enemylist[targetIdx][15] = 327680;
                            break;
                        case 130:
                            enemylist[targetIdx][14] = 327680;
                            enemylist[targetIdx][15] = 196608;
                            break;
                        case 170:
                            enemylist[targetIdx][15] = 327680;
                            break;
                    }

                    int mainIdx18 = bossWrk18[25];
                    if (mainIdx18 < 0 || mainIdx18 >= enemylist.length || enemylist[mainIdx18][11] != 101) {
                        mainIdx18 = -1;
                        for (int i = 0; i < enemylist.length; i++) {
                            if (enemylist[i][11] == 101) {
                                mainIdx18 = i;
                                break;
                            }
                        }
                    }

                    int ex18 = enemylist[targetIdx][5];
                    int ey18 = enemylist[targetIdx][6];
                    int bid18 = (mt == 102) ? 82 : 83;

                    if (mainIdx18 >= 0 && (gc18 & 0x1) == 0 && ((gc18 > 10 && gc18 < 70) || (gc18 > 115 && gc18 < 140))) {
                        int dx18 = enemylist[mainIdx18][5] - ex18;
                        int dy18 = enemylist[mainIdx18][6] - ey18;
                        int base18 = EnemyBulletSystem.arcTan2Deg(dy18, dx18);
                        switch (lvl18) {
                            case 0:
                                host.spawnEnemyBullet(bid18, 0, base18, 8, ex18, ey18);
                                break;
                            case 1:
                                host.spawnEnemyBullet(bid18, 0, base18 + 30, 8, ex18, ey18);
                                host.spawnEnemyBullet(bid18, 0, base18 - 30, 8, ex18, ey18);
                                break;
                            case 2:
                                host.spawnEnemyBullet(bid18, 0, base18, 8, ex18, ey18);
                                host.spawnEnemyBullet(bid18, 0, base18 + 30, 8, ex18, ey18);
                                host.spawnEnemyBullet(bid18, 0, base18 - 30, 8, ex18, ey18);
                                break;
                            case 3:
                            default:
                                host.spawnEnemyBullet(bid18, 0, base18, 8, ex18, ey18);
                                host.spawnEnemyBullet(bid18, 0, base18 + 30, 8, ex18, ey18);
                                host.spawnEnemyBullet(bid18, 0, base18 - 30, 8, ex18, ey18);
                                host.spawnEnemyBullet(bid18, 0, base18 + 60, 8, ex18, ey18);
                                host.spawnEnemyBullet(bid18, 0, base18 - 60, 8, ex18, ey18);
                                break;
                        }
                    }

                    if ((gc18 % 3) == 0 && gc18 > 155 && gc18 < 180) {
                        if (mt == 102) {
                            int dx18 = (bossx[1] << 16) - ex18;
                            int dy18 = (bossy[1] << 16) - ey18;
                            int base18 = EnemyBulletSystem.arcTan2Deg(dy18, dx18);
                            host.spawnEnemyBullet(82, 0, base18 + 160, 8, ex18, ey18);
                            host.spawnEnemyBullet(82, 0, base18 + 200, 8, ex18, ey18);
                        } else {
                            int dx18 = (bossx[2] << 16) - ex18;
                            int dy18 = (bossy[2] << 16) - ey18;
                            int base18 = EnemyBulletSystem.arcTan2Deg(dy18, dx18);
                            host.spawnEnemyBullet(83, 0, base18 + 160, 8, ex18, ey18);
                            host.spawnEnemyBullet(83, 0, base18 + 200, 8, ex18, ey18);
                        }
                    }

                    if ((gc18 % 5) == 0 && gc18 > 240 && gc18 < 250) {
                        int n18;
                        switch (lvl18) {
                            case 0:
                                n18 = 8;
                                break;
                            case 1:
                                n18 = 10;
                                break;
                            case 2:
                                n18 = 12;
                                break;
                            case 3:
                            default:
                                n18 = 18;
                                break;
                        }

                        if (mt == 102) {
                            for (int a = 0; a < 360; a += 360 / n18) {
                                host.spawnEnemyBullet(82, 10, a + gc18, 24, ex18, ey18);
                            }
                        } else {
                            for (int a = 0; a < 360; a += 360 / n18) {
                                host.spawnEnemyBullet(83, 11, a - gc18, 24, ex18, ey18);
                            }
                        }

                        for (int a = 0; a < 360; a += 360 / n18) {
                            host.spawnEnemyBullet(bid18, 0, a + gc18, 24, ex18, ey18);
                        }
                    }
                }

                if (gc18 > 320) {
                    host.setMGCnt(0);
                }
                return;

            case 19:
                bossx[0] = 97;
                bossy[0] = 68;
                bossx[1] = -30;
                bossx[2] = 30;
                bossy[2] = -42;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 2 + host.getLevel();
                    wb(20);
                }
                return;

            case 20:
                if (mt != 101) {
                    return;
                }
                bspellcnt = 34;
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
                    wb(21);
                    j = 1100;
                    if (host.getPower() >= 96) {
                        j += 275;
                    }
                    yb(targetIdx, 3, 2);
                }
                return;

            case 21:
                if (mt != 101) {
                    return;
                }
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int gc21 = host.getMGCnt();
                int spawnX = enemylist[targetIdx][5];
                int spawnY = enemylist[targetIdx][6];

                int lvl = host.getLevel();
                int d6 = 0;
                int d7 = 0;
                switch (lvl) {
                    case 0:
                        d6 = 3;
                        d7 = 2;
                        break;
                    case 1:
                        d6 = 4;
                        break;
                    case 2:
                        d6 = 6;
                        break;
                    case 3:
                    default:
                        d6 = 8;
                        d7 = 3;
                        break;
                }

                int d5 = 360 / d6;

                if (lvl == 0 || lvl == 3) {
                    if (gc21 != 0 && (gc21 % 60) == 0) {
                        int dx = host.getPlayerXFixed() - enemylist[targetIdx][5];
                        int dy = host.getPlayerYFixed() - enemylist[targetIdx][6];
                        host.setEnemyBulletGlobalDir(EnemyBulletSystem.arcTan2Deg(dy, dx));

                        for (int a = 0; a < 360; a += 36) {
                            for (int k = 0; k < d7; k++) {
                                int spd = (4 + (k << 1)) << 3;
                                host.spawnEnemyBullet(83, 27, a, spd, spawnX, spawnY);
                            }
                        }
                    }

                    if ((gc21 & 7) == 0) {
                        for (int a = 0; a < 360; a += d5) {
                            host.spawnEnemyBullet(239, 26, gc21 + a, 48, spawnX, spawnY);
                            host.spawnEnemyBullet(239, 26, gc21 + a + (d5 >> 2), 40, spawnX, spawnY);
                            host.spawnEnemyBullet(239, 26, gc21 + a + (d5 >> 1), 32, spawnX, spawnY);
                            host.spawnEnemyBullet(239, 26, gc21 + a + (d5 - (d5 >> 2)), 40, spawnX, spawnY);
                        }
                    }
                    return;
                }

                if (gc21 == 0) {
                    host.spawnEnemyBullet(93, 1, 360, 16, spawnX, spawnY);
                    host.spawnEnemyBullet(93, 1, 540, 16, spawnX, spawnY);
                }

                if (gc21 > 70 && (gc21 % 60) == 0) {
                    int edgeX;
                    int dx;
                    if (((gc21 / 60) & 1) == 0) {
                        edgeX = 0;
                        dx = host.getPlayerXFixed();
                    } else {
                        edgeX = 194 << 16;
                        dx = host.getPlayerXFixed() - edgeX;
                    }
                    int dy = host.getPlayerYFixed() - spawnY;
                    host.setEnemyBulletGlobalDir(EnemyBulletSystem.arcTan2Deg(dy, dx));

                    for (int a = 0; a < 360; a += d5) {
                        for (int k = 0; k < 3; k++) {
                            int spd = (4 + (k << 1)) << 3;
                            host.spawnEnemyBullet(83, 27, a, spd, edgeX, spawnY);
                        }
                    }
                }

                if (gc21 > 20 && (gc21 & 0xF) == 0) {
                    for (int a = 0; a < 360; a += d5) {
                        int edgeX0 = 0;
                        host.spawnEnemyBullet(239, 26, gc21 + a, 40, edgeX0, spawnY);
                        host.spawnEnemyBullet(239, 26, gc21 + a + (d5 >> 2), 32, edgeX0, spawnY);
                        host.spawnEnemyBullet(239, 26, gc21 + a + (d5 >> 1), 24, edgeX0, spawnY);
                        host.spawnEnemyBullet(239, 26, gc21 + a + (d5 - (d5 >> 2)), 32, edgeX0, spawnY);

                        int edgeX1 = 194 << 16;
                        host.spawnEnemyBullet(239, 26, gc21 + a, 40, edgeX1, spawnY);
                        host.spawnEnemyBullet(239, 26, gc21 + a + (d5 >> 2), 32, edgeX1, spawnY);
                        host.spawnEnemyBullet(239, 26, gc21 + a + (d5 >> 1), 24, edgeX1, spawnY);
                        host.spawnEnemyBullet(239, 26, gc21 + a + (d5 - (d5 >> 2)), 32, edgeX1, spawnY);
                    }
                }
                return;

            case 37:
                bossx[0] = 97;
                bossy[0] = 68;
                bossx[1] = -30;
                bossx[2] = 30;
                bossy[2] = -42;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 107;
                    wb(38);
                }
                return;

            case 38:
                if (mt != 101) {
                    return;
                }
                bspellcnt = 70;
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
                }
                return;

            case 39:
                if (mt != 101) {
                    return;
                }
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int d0_39 = host.getMGCnt() % 150;
                int bossXFixed39 = enemylist[targetIdx][5];
                int bossYFixed39 = enemylist[targetIdx][6];

                int d6_39 = 4;
                if (o[1] < (o[2] * 66 / 100)) {
                    d6_39 = 5;
                }
                if (o[1] < (o[2] * 33 / 100)) {
                    d6_39 = 6;
                }

                if (d0_39 < 120) {
                    if ((d0_39 & 0x1) == 0) {
                        int stepAng39 = 360 / d6_39;
                        for (int ang = 0; ang < 360; ang += stepAng39) {
                            host.spawnEnemyBullet(239, 28, (d0_39 << 3) + ang, -60 + (d0_39 >> 1), bossXFixed39, bossYFixed39);
                        }
                    }

                    if ((d0_39 & 0x3) == 0) {
                        int stepAng39 = 360 / ((d6_39 << 1) + 8);
                        for (int ang = 0; ang < 360; ang += stepAng39) {
                            host.spawnEnemyBullet(585, 7, (d0_39 << 1) + ang, 40, bossXFixed39, bossYFixed39);
                        }
                    }

                    if (d0_39 >= 31 && (d0_39 & 0x7) == 0) {
                        int stepAng39 = 360 / d6_39;
                        int d7_39 = d0_39 & 0x7;
                        for (int ang = 0; ang < 360; ang += stepAng39) {
                            host.spawnEnemyBullet(81, 0, ang - (d0_39 << 2), 24 + (d7_39 << 3), bossXFixed39, bossYFixed39);
                        }
                    }

                    if (d0_39 >= 15 && (d0_39 & 0x3F) == 0) {
                        int stepAng39 = 360 / ((d6_39 << 1) + 4);
                        int dxAim39 = host.getPlayerXFixed() - bossXFixed39;
                        int dyAim39 = host.getPlayerYFixed() - bossYFixed39;
                        int baseAng39 = EnemyBulletSystem.arcTan2Deg(dyAim39, dxAim39);
                        for (int ang = 0; ang < 360; ang += stepAng39) {
                            host.spawnEnemyBullet(87, 0, baseAng39 + ang, 24, bossXFixed39, bossYFixed39);
                        }
                    }
                }

                if (d0_39 == 120) {
                    int px39 = host.getPlayerXFixed();
                    if (px39 < bossXFixed39) {
                        bossx[0] += 30;
                    } else {
                        bossx[0] -= 30;
                    }
                    if (bossx[0] < 30) {
                        bossx[0] += 80;
                    }
                    if (bossx[0] > 164) {
                        bossx[0] -= 80;
                    }
                    if (bossy[0] == 68) {
                        bossy[0] = 88;
                        return;
                    }
                    bossy[0] = 68;
                }
                return;

            case 40:
                bossx[2] = 97;
                bossy[2] = 68;
                bossx[0] = 164;
                bossy[0] = -32;
                bossx[1] = 30;
                bossy[1] = -32;
                enemylist[targetIdx][14] = 458752;
                enemylist[targetIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 108;
                    wb(41);
                }
                return;

            case 41:
                if (mt != 103) {
                    return;
                }
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
                    wb(42);
                    host.getBossWrk()[6] = 0;
                }
                return;

            case 42:
                if (mt != 103) {
                    return;
                }
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int d0_42 = host.getMGCnt() % 180;
                int bossXFixed42 = enemylist[targetIdx][5];
                int bossYFixed42 = enemylist[targetIdx][6];

                int[] bossWrk42 = host.getBossWrk();
                if (d0_42 == 0) {
                    bossWrk42[6] = 0;
                    if (bossx[2] > 97) {
                        bossWrk42[6] = 1;
                    }
                }

                int d7_42 = 5;
                if ((d0_42 & 0x3) == 0) {
                    int d3_42 = (d0_42 << 3) - 194;
                    for (int n32 = 0; n32 < d7_42; n32++) {
                        int d4_42 = d3_42 + (n32 * 194 / d7_42);
                        if (d4_42 >= 0) {
                            int d5_42 = d3_42 % 388;
                            if (d5_42 > 194) {
                                d5_42 = 388 - d5_42;
                            }
                            d5_42 = 194 - d5_42;
                            host.spawnEnemyBullet(621, 0, 450, 16, d5_42 << 16, -131072);
                            d5_42 = 194 - d5_42;
                            host.spawnEnemyBullet(621, 0, 450, 16, d5_42 << 16, -131072);
                        }
                    }
                }

                int edgeXFixed42 = 0;
                int baseAng42 = 90;
                if (bossWrk42[6] != 0) {
                    edgeXFixed42 = 12713984;
                    baseAng42 = 270;
                }

                int baseYFixed42 = 7929856;
                int offYFixed42 = ((d0_42 << 2) % 113) << 16;
                if ((d0_42 & 0x7) == 0) {
                    host.spawnEnemyBullet(89, 0, baseAng42 + 270, 20, edgeXFixed42, baseYFixed42 + offYFixed42);
                }

                int yRaw42 = ((d0_42 << 3) % 226);
                if (yRaw42 > 113) {
                    yRaw42 = 226 - yRaw42;
                }
                offYFixed42 = yRaw42 << 16;
                if ((d0_42 & 0xF) == 0) {
                    host.spawnEnemyBullet(89, 0, baseAng42 + 270, 24, edgeXFixed42, baseYFixed42 + offYFixed42);
                }

                yRaw42 = 226 - (((d0_42 << 1) % 113));
                offYFixed42 = yRaw42 << 16;
                if ((d0_42 & 0x7) == 0) {
                    host.spawnEnemyBullet(89, 0, baseAng42 + 270, 36, edgeXFixed42, baseYFixed42 + offYFixed42);
                }

                if (d0_42 == 80) {
                    bossx[2] = host.getPlayerXFixed() >> 16;
                }
                if (d0_42 == 100) {
                    int dxAim42 = host.getPlayerXFixed() - bossXFixed42;
                    int dyAim42 = host.getPlayerYFixed() - bossYFixed42;
                    enemylist[targetIdx][10] = EnemyBulletSystem.arcTan2Deg(dyAim42, dxAim42);
                }

                if (d0_42 == 160) {
                    enemylist[targetIdx][3] = 0;
                }
                if (d0_42 == 100 || d0_42 == 158) {
                    enemylist[targetIdx][3] = 1;
                }
                if (d0_42 == 105 || d0_42 == 156) {
                    enemylist[targetIdx][3] = 2;
                }
                if (d0_42 == 110) {
                    enemylist[targetIdx][3] = 3;
                }

                int ringXFixed42 = bossXFixed42;
                int ringYFixed42 = 524288;
                if (d0_42 > 120 && d0_42 < 160 && (d0_42 & 0x3) == 0) {
                    int d6_42 = 10;
                    if (o[1] < (o[2] * 66 / 100)) {
                        d6_42 = 12;
                    }
                    if (o[1] < (o[2] * 33 / 100)) {
                        d6_42 = 15;
                    }

                    int stepAng42 = 360 / d6_42;
                    int startOff42 = 0;
                    if ((d0_42 & 0x7) == 0) {
                        startOff42 = stepAng42 >> 1;
                    }

                    for (int a = 0; a < 360; a += stepAng42) {
                        host.spawnEnemyBullet(83, 0, enemylist[targetIdx][10] + startOff42 + a, 24, ringXFixed42, ringYFixed42);
                    }
                }

                if (d0_42 == 160) {
                    int dxAim42 = host.getPlayerXFixed() - ringXFixed42;
                    int dyAim42 = host.getPlayerYFixed() - ringYFixed42;
                    enemylist[targetIdx][10] = EnemyBulletSystem.arcTan2Deg(dyAim42, dxAim42);
                    host.spawnEnemyBullet(95, 85, enemylist[targetIdx][10], 32, ringXFixed42, ringYFixed42);
                    return;
                }
                return;

            case 43:
                bossx[1] = 97;
                bossy[1] = 68;
                bossx[0] = 164;
                bossy[0] = -42;
                bossx[2] = 30;
                bossy[2] = -42;
                enemylist[targetIdx][14] = 196608;
                enemylist[targetIdx][15] = 196608;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 109;
                    wb(44);
                }
                return;

            case 44:
                if (mt != 102) {
                    return;
                }
                bspellcnt = 60;
                switch (host.getMGCnt()) {

                    case 1:
                        enemylist[targetIdx][3] = 1;
                        break;
                    case 5:
                        enemylist[targetIdx][3] = 2;
                        break;
                    case 10:
                        enemylist[targetIdx][3] = 3;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(45);
                }
                return;


            case 45:
                if (mt != 102) {
                    return;
                }
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(targetIdx);
                    return;
                }

                int d0_45 = host.getMGCnt();
                int bossXFixed45 = enemylist[targetIdx][5];
                int bossYFixed45 = enemylist[targetIdx][6];

                int d6_45 = 4;
                int d7_45 = 4;
                int da_45 = 1;
                if (bspellcnt < 50 || o[1] < (o[2] * 80 / 100)) {
                    d6_45 = 5;
                    d7_45 = 4;
                    da_45++;
                }
                if (bspellcnt < 40 || o[1] < (o[2] * 60 / 100)) {
                    d6_45 = 6;
                    d7_45 = 3;
                    da_45++;
                }
                if (bspellcnt < 30 || o[1] < (o[2] * 40 / 100)) {
                    d6_45 = 8;
                    d7_45 = 3;
                    da_45++;
                }
                if (bspellcnt < 20 || o[1] < (o[2] * 20 / 100)) {
                    d6_45 = 9;
                    d7_45 = 3;
                    da_45++;
                }

                if ((d0_45 % d7_45) == 0) {
                    int stepAng45 = 360 / d6_45;
                    int d8_45 = d0_45 % 100;
                    if (d8_45 > 50) {
                        d8_45 = 100 - d8_45;
                    }
                    int d9_45 = 360 - ((d0_45 << 3) % 360);
                    for (int n34 = 0; n34 < 360; n34 += stepAng45) {
                        int sp45 = 160 + (d8_45 << 3);
                        int offAng45 = d9_45 + n34;
                        int se45 = (int) (((long) Trig.cos(offAng45) * (long) sp45) >> 3);
                        int te45 = (int) (((long) Trig.sin(offAng45) * (long) sp45) >> 3);

                        d8_45 = (d0_45 + n34) % 140;
                        if (d8_45 > 70) {
                            d8_45 = 140 - d8_45;
                        }
                        host.spawnEnemyBullet(83, 28, d8_45 - 35 + 270, 0, bossXFixed45 + se45, bossYFixed45 + (te45 >> 1));
                    }
                }

                if (d0_45 > 50) {
                    for (int n35 = 0; n35 < da_45; n35++) {
                        if ((((d0_45 + (n35 << 2)) & 0x7) == 0)) {
                            int sx45;
                            if ((n35 & 0x1) == 0) {
                                sx45 = ((((d0_45 << 3) + (n35 << 4)) % 194) << 16);
                            } else {
                                sx45 = ((194 - (((d0_45 << 3) + (n35 << 4)) % 194)) << 16);
                            }
                            int sy45 = 524288;

                            int a8_45 = (d0_45 + (n35 << 3)) % 100;
                            if (a8_45 > 50) {
                                a8_45 = 100 - a8_45;
                            }
                            int ang45 = a8_45 - 25 + 180 + 270;
                            for (int n36 = 0; n36 < 5; n36++) {
                                host.spawnEnemyBullet(585, 40, ang45, 30 + (n36 << 2), sx45, sy45);
                            }
                        }
                    }
                }

                if (d0_45 > 150) {
                    if ((d0_45 & 0x3) == 0) {
                        int sx45 = ((194 - ((d0_45 << 2) % 194)) << 16);
                        int sy45 = 524288;
                        int a8_45 = (((d0_45 << 3) + ((host.getPlayerXFixed() >> 16) << 1)) % 80);
                        if (a8_45 > 40) {
                            a8_45 = 80 - a8_45;
                        }
                        host.spawnEnemyBullet(89, 40, a8_45 - 20 + 180 + 270, 40, sx45, sy45);
                    }
                    if ((((d0_45 + 2) & 0x7) == 0)) {
                        int sx45 = (((d0_45 << 3) % 194) << 16);
                        int sy45 = 524288;
                        int a8_45 = 120 - ((((d0_45 << 1) + ((host.getPlayerYFixed() >> 16) << 1)) % 70));
                        if (a8_45 > 35) {
                            a8_45 = 70 - a8_45;
                        }
                        host.spawnEnemyBullet(89, 40, a8_45 - 17 + 180 + 270, 44, sx45, sy45);
                    }
                    if ((((d0_45 + 8) & 0xF) == 0)) {
                        int sx45 = (((d0_45 << 3) % 194) << 16);
                        int sy45 = 524288;
                        int px4 = (((host.getPlayerXFixed() + host.getPlayerXFixed()) >> 16) << 1);
                        int a8_45 = (((d0_45 << 2) + px4) % 120);
                        if (a8_45 > 60) {
                            a8_45 = 60 - a8_45;
                        }
                        host.spawnEnemyBullet(89, 40, a8_45 - 30 + 180 + 270, 48, sx45, sy45);
                    }
                }

                if (bspellcnt < 5) {
                    int d0b_45 = d0_45 + 40;
                    if ((d0b_45 & 0x2) == 0) {
                        int sx45 = ((194 - ((d0b_45 << 2) % 194)) << 16);
                        int sy45 = 524288;
                        int a8_45 = (((d0b_45 << 3) + ((host.getPlayerXFixed() >> 16) << 1)) % 80);
                        if (a8_45 > 40) {
                            a8_45 = 80 - a8_45;
                        }
                        host.spawnEnemyBullet(93, 40, a8_45 - 20 + 180 + 270, 60, sx45, sy45);
                    }
                    if ((((d0b_45 + 2) & 0x7) == 0)) {
                        int sx45 = (((d0b_45 << 3) % 194) << 16);
                        int sy45 = 524288;
                        int a8_45 = 120 - ((((d0b_45 << 1) + ((host.getPlayerYFixed() >> 16) << 1)) % 70));
                        if (a8_45 > 35) {
                            a8_45 = 70 - a8_45;
                        }
                        host.spawnEnemyBullet(93, 40, a8_45 - 17 + 180 + 270, 60, sx45, sy45);
                    }
                    if ((((d0b_45 + 8) & 0xF) == 0)) {
                        int sx45 = (((d0b_45 << 3) % 194) << 16);
                        int sy45 = 524288;
                        int px4 = (((host.getPlayerXFixed() + host.getPlayerXFixed()) >> 16) << 1);
                        int a8_45 = (((d0b_45 << 2) + px4) % 120);
                        if (a8_45 > 60) {
                            a8_45 = 60 - a8_45;
                        }
                        host.spawnEnemyBullet(93, 40, a8_45 - 30 + 180 + 270, 60, sx45, sy45);
                    }
                }

                return;
        }
    }

    //midboss
    void kb(int enemyIdx) {
        int[][] enemylist = host.getEnemyList();
        int[] bossx = host.getBossX();
        int[] bossy = host.getBossY();

        int ex = enemylist[enemyIdx][5];
        int ey = enemylist[enemyIdx][6];

        switch (enemylist[enemyIdx][7]) {
            case 0:
                bossx[0] = 97;
                bossy[0] = 44;
                bspellcnt = 255;
                batkf = false;
                vb(enemyIdx, 1);
                j = enemylist[enemyIdx][4];
                host.setResetFlag(4);
                if (host.getLevel() <= 1) {
                    yb(enemyIdx, 0, 0);
                    return;
                }
                yb(enemyIdx, 0, 1);
                return;

            case 1:
                if (host.getMGCnt() == 0) {
                    enemylist[enemyIdx][16] = 0;
                }
                if (host.getGameMode() != 3) {
                    bossx[0] = 97;
                    bossy[0] = 44;
                }
                bspellcnt = 29;
                batkf = false;
                enemylist[enemyIdx][3] = 0;
                if (host.getGameMode() == 3) {
                    bspellcnt = 120;
                    enemylist[enemyIdx][4] = 800;
                    j = enemylist[enemyIdx][4];
                    yb(enemyIdx, 0, 3);
                    vb(enemyIdx, 8);
                    return;
                }
                if (host.getMGCnt() > 30) {
                    vb(enemyIdx, 3);
                    return;
                }
                return;

            case 3:
                enemylist[enemyIdx][16] = 1;
                batkf = true;

                int n2;
                switch (host.getLevel()) {
                    case 0:
                        n2 = 12;
                        break;
                    case 1:
                        n2 = 18;
                        break;
                    case 2:
                        n2 = 24;
                        break;
                    case 3:
                    default:
                        n2 = 36;
                        break;
                }

                switch (host.getMGCnt()) {
                    case 10:
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[0] = 137;
                            bossy[0] = 68;
                        } else {
                            bossx[0] = 47;
                            bossy[0] = 80;
                        }
                        break;

                    case 30:
                        // Sunny: ring burst ( kb case=3)
                        for (int a = 0; a < 360; a += 360 / n2) {
                            host.spawnEnemyBullet(81, 2, a, 80, ex, ey);
                        }
                        break;

                    case 31:
                    case 32:
                    case 33:
                    case 34:
                    case 35: {
                        // Sunny: aimed burst ( kb case=3)
                        int spd = (host.getMGCnt() - 30 + 3) << 3;
                        host.spawnEnemyBullet(81, 3, 0, spd, ex, ey);
                        break;
                    }

                    case 50:
                        if (enemylist[enemyIdx][5] > (97 << 16)) {
                            bossx[0] = 57;
                            bossy[0] = 38;
                        } else {
                            bossx[0] = 147;
                            bossy[0] = 38;
                        }
                        break;

                    case 60:
                    case 100:
                        // Sunny: slow ring ( kb case=3)
                        for (int a = 0; a < 360; a += 360 / n2) {
                            host.spawnEnemyBullet(81, 0, a, 24, ex, ey);
                        }
                        break;

                    case 62:
                    case 102:
                        // Sunny: offset ring ( kb case=3)
                        for (int a = (360 / n2) >> 1; a < 360; a += 360 / n2) {
                            host.spawnEnemyBullet(81, 0, a, 40, ex, ey);
                        }
                        break;

                    case 64:
                    case 104:
                        // Sunny: fast ring ( kb case=3)
                        for (int a = 0; a < 360; a += 360 / n2) {
                            host.spawnEnemyBullet(81, 0, a, 64, ex, ey);
                        }
                        break;

                    case 90:
                        bossx[0] = 97;
                        bossy[0] = 68;
                        break;
                }

                if (host.getMGCnt() > 120) {
                    host.setMGCnt(0);
                }
                if (bspellcnt < 0) {
                    vb(enemyIdx, 2);
                    return;
                }
                return;

            case 8:
                bossx[0] = 97;
                bossy[0] = 68;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                enemylist[enemyIdx][16] = 0;
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = host.getLevel() - 2;
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
                    case 10:
                        enemylist[enemyIdx][3] = 3;
                        break;
                }
                if (host.getMGCnt() > 30) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    enemylist[enemyIdx][16] = 1;
                    vb(enemyIdx, 10);
                    enemylist[enemyIdx][4] = 800;
                    if (host.getPower() >= 96) {
                        enemylist[enemyIdx][4] <<= 1;
                    }
                    j = enemylist[enemyIdx][4];
                    yb(enemyIdx, 0, 2);
                    bossy[0] = 88;
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

                switch (host.getMGCnt()) {
                    case 10:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 13:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 16:
                        enemylist[enemyIdx][3] = 3;
                        break;

                    case 20:
                        // Sunny spell: single drop shot ( kb case=10)
                        host.spawnEnemyBullet(93, 1, 270, 16, ex, ey);
                        break;

                    case 50: {
                        // Sunny spell: rain/grid ( kb case=10)
                        int div50;
                        if (host.getLevel() == 2) {
                            div50 = 6;
                        } else {
                            div50 = 8;
                        }
                        int baseDeg = 180;
                        int bx0 = bossx[0];
                        if (bx0 < 77) {
                            baseDeg = 170;
                        }
                        if (bx0 > 117) {
                            baseDeg = 190;
                        }
                        int spawnY = 524288;
                        for (int x = -20; x < 214; x += 234 / div50) {
                            for (int i = 0; i < 10; i++) {
                                int s = (3 + i) << 3;
                                int spawnX = ((x - 20) << 16) + ((enemylist[enemyIdx][5] % 20) - 655360);
                                host.spawnEnemyBullet(81, 0, baseDeg + 270, s, spawnX, spawnY);
                            }
                        }
                        break;
                    }

                    case 60:
                        enemylist[enemyIdx][3] = 2;
                        break;
                    case 63:
                        enemylist[enemyIdx][3] = 1;
                        break;
                    case 65:
                        enemylist[enemyIdx][3] = 0;
                        enemylist[enemyIdx][14] = 262144;
                        enemylist[enemyIdx][15] = 262144;
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[0] += 60;
                        } else {
                            bossx[0] -= 60;
                        }
                        if (bossx[0] < 20) {
                            bossx[0] = 134;
                        }
                        if (bossx[0] > 174) {
                            bossx[0] = 60;
                        }
                        break;
                }

                int gc = host.getMGCnt();
                if (gc > 60 && gc < 80) {
                    // Sunny spell: rotating accel ring ( kb case=10)
                    int div = (host.getLevel() == 2) ? 5 : 8;
                    for (int a = 0; a < 360; a += 360 / div) {
                        host.spawnEnemyBullet(83, 2, (gc << 3) + a, 96, ex, ey);
                    }
                }
                if (host.getMGCnt() > 100) {
                    host.setMGCnt(0);
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