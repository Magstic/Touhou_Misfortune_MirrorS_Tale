package touhou.stage;

import touhou.EnemyBulletSystem;
import touhou.Trig;

final class Stage5BossLogic extends AbstractBossStageLogic {
    void tickBossImpl(int enemyIdx) {
        rb(enemyIdx);
    }

    void tickMidbossImpl(int enemyIdx) {
    }

    // Boss(stub)
    void rb(int enemyIdx) {
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
                if (host.getMGCnt() == 0) {
                    host.setBbaria(1);
                }
                if (host.getGameMode() != 3) {
                    bossx[idx] = 97;
                    bossy[idx] = 56;
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
                    switch (step) {
                        case 10:
                            j = 1400;
                            break;
                        case 13:
                            j = 2000;
                            break;
                        case 16:
                            j = 2000;
                            break;
                        case 19:
                            j = 1800;
                            break;
                        case 37:
                            j = 4200;
                            break;
                    }
                    yb(enemyIdx, 7, 3);
                    return;
                }

                switch (host.getBstock()) {
                    case 3:
                        j = 1000;
                        yb(enemyIdx, 7, 1);
                        bspellcnt = 39;
                        wb(3);
                        return;
                    case 2:
                        j = 1000;
                        yb(enemyIdx, 7, 1);
                        bspellcnt = 49;
                        wb(4);
                        return;
                    case 1:
                        j = 2000;
                        yb(enemyIdx, 7, 3);
                        bspellcnt = 49;
                        wb(16);
                        return;
                    case 0:
                        j = 1800;
                        yb(enemyIdx, 7, 3);
                        bspellcnt = 49;
                        wb(19);
                        return;
                }
                return;

            case 3:
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    j = 65535;
                    yb(l, n, 2);
                    enemylist[enemyIdx][3] = 0;
                    wb(10);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d03 = (host.getMGCnt() + 1) % 240;
                if (d03 % 120 == 40) {
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
                }

                switch (d03) {
                    case 16:
                        enemylist[enemyIdx][3] = 9;
                        break;
                    case 17:
                        enemylist[enemyIdx][3] = 10;
                        break;
                    case 18:
                        enemylist[enemyIdx][3] = 11;
                        break;
                    case 20:
                        enemylist[enemyIdx][3] = 12;
                        break;
                    case 40:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d03 >= 20 && d03 < 40 && (d03 & 0x1) == 0) {
                    int ex = enemylist[enemyIdx][5];
                    int ey = enemylist[enemyIdx][6];
                    if (d03 == 20) {
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey, host.getPlayerXFixed() - ex);
                    }

                    int d4;
                    int d5;
                    switch (host.getLevel()) {
                        case 0:
                            d4 = 64;
                            d5 = 2;
                            break;
                        case 1:
                            d4 = 51;
                            d5 = 3;
                            break;
                        case 2:
                            d4 = 40;
                            d5 = 5;
                            break;
                        case 3:
                        default:
                            d4 = 32;
                            d5 = 6;
                            break;
                    }

                    int span = d4 * d5;
                    int aim = enemylist[enemyIdx][10];
                    for (int v = 0; v <= span; v += d4) {
                        int off = (v + (d03 << 3)) % span - (span >> 1);
                        host.spawnEnemyBullet(537, 0, aim + off, 40, ex, ey);

                        off = (span - v + (d03 << 2)) % span - (span >> 1);
                        host.spawnEnemyBullet(537, 0, aim + off, 40, ex, ey);
                    }
                }

                switch (d03) {
                    case 136:
                        enemylist[enemyIdx][3] = 5;
                        break;
                    case 137:
                        enemylist[enemyIdx][3] = 6;
                        break;
                    case 138:
                        enemylist[enemyIdx][3] = 7;
                        break;
                    case 140:
                        enemylist[enemyIdx][3] = 8;
                        break;
                    case 160:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d03 >= 140 && d03 < 160 && (d03 & 0x1) == 0) {
                    int ex = enemylist[enemyIdx][5];
                    int ey = enemylist[enemyIdx][6];
                    if (d03 == 140) {
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey, host.getPlayerXFixed() - ex);
                    }
                    int d8 = 160 - d03;

                    int d4;
                    int d5;
                    switch (host.getLevel()) {
                        case 0:
                            d4 = 64;
                            d5 = 2;
                            break;
                        case 1:
                            d4 = 52;
                            d5 = 3;
                            break;
                        case 2:
                            d4 = 40;
                            d5 = 5;
                            break;
                        case 3:
                        default:
                            d4 = 32;
                            d5 = 6;
                            break;
                    }

                    int span = d4 * d5;
                    int aim = enemylist[enemyIdx][10];
                    for (int v = 0; v <= span; v += d4) {
                        int off = (v + (d8 << 3)) % span - (span >> 1);
                        host.spawnEnemyBullet(569, 0, aim + off, 40, ex, ey);

                        off = (span - v + (d8 << 2)) % span - (span >> 1);
                        host.spawnEnemyBullet(569, 0, aim + off, 40, ex, ey);
                    }
                }

                switch (d03) {
                    case 76:
                    case 196:
                        enemylist[enemyIdx][3] = 5;
                        break;
                    case 77:
                    case 197:
                        enemylist[enemyIdx][3] = 6;
                        break;
                    case 78:
                    case 198:
                        enemylist[enemyIdx][3] = 7;
                        break;
                    case 79:
                    case 199:
                        enemylist[enemyIdx][3] = 8;
                        break;
                    case 81:
                    case 201:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d03 == 80 || d03 == 200) {
                    int ex = enemylist[enemyIdx][5];
                    int ey = enemylist[enemyIdx][6];
                    enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey, host.getPlayerXFixed() - ex);

                    int div;
                    int count;
                    switch (host.getLevel()) {
                        case 0:
                            div = 12;
                            count = 1;
                            break;
                        case 1:
                            div = 18;
                            count = 3;
                            break;
                        case 2:
                            div = 20;
                            count = 3;
                            break;
                        case 3:
                        default:
                            div = 24;
                            count = 5;
                            break;
                    }

                    int step = 360 / div;
                    int aim = enemylist[enemyIdx][10];
                    for (int a = 0; a < 360; a += step) {
                        for (int k = 0; k < count; k++) {
                            host.spawnEnemyBullet(537, 37, aim + a, 28 + (k << 2), ex, ey);
                        }
                    }
                }
                return;

            case 4:
                if (m == 2 || bspellcnt < 0) {
                    host.setResetFlag(4);
                    j = 65535;
                    yb(l, n, 2);
                    enemylist[enemyIdx][3] = 0;
                    wb(13);
                    return;
                }
                host.setBbaria(0);
                batkf = true;

                int d04 = (host.getMGCnt() + 1) % 300;
                int ex04 = enemylist[enemyIdx][5];
                int ey04 = enemylist[enemyIdx][6];
                if (d04 % 150 == 0) {
                    if (host.getPlayerXFixed() > enemylist[enemyIdx][5]) {
                        bossx[idx] += 40;
                    } else {
                        bossx[idx] -= 40;
                    }
                    if (bossx[idx] < 30) {
                        bossx[idx] = 107;
                    }
                    if (bossx[idx] > 164) {
                        bossx[idx] = 87;
                    }
                }

                switch (d04) {
                    case 16:
                        enemylist[enemyIdx][3] = 9;
                        break;
                    case 17:
                        enemylist[enemyIdx][3] = 10;
                        break;
                    case 18:
                        enemylist[enemyIdx][3] = 11;
                        break;
                    case 20:
                        enemylist[enemyIdx][3] = 12;
                        break;
                    case 50:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d04 >= 20 && d04 < 50 && ((d04 - 20) & 0x7) == 0) {
                    if (d04 == 20) {
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey04, host.getPlayerXFixed() - ex04);
                    }
                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 5;
                            break;
                        case 1:
                            div = 6;
                            break;
                        case 2:
                            div = 8;
                            break;
                        case 3:
                        default:
                            div = 10;
                            break;
                    }
                    int step = 360 / div;
                    int aim = enemylist[enemyIdx][10];
                    for (int a = 0; a < 360; a += step) {
                        int ang = aim + a + (d04 << 2);
                        for (int o = -2; o <= 2; o++) {
                            host.spawnEnemyBullet(537, 37, ang + o, 32, ex04, ey04);
                        }
                    }
                }

                switch (d04) {
                    case 136:
                        enemylist[enemyIdx][3] = 5;
                        break;
                    case 137:
                        enemylist[enemyIdx][3] = 6;
                        break;
                    case 138:
                        enemylist[enemyIdx][3] = 7;
                        break;
                    case 140:
                        enemylist[enemyIdx][3] = 8;
                        break;
                    case 160:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d04 >= 140 && d04 < 160 && ((d04 - 140) & 0x7) == 0) {
                    if (d04 == 140) {
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey04, host.getPlayerXFixed() - ex04);
                    }
                    int d8 = 160 - d04;
                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 5;
                            break;
                        case 1:
                            div = 6;
                            break;
                        case 2:
                            div = 8;
                            break;
                        case 3:
                        default:
                            div = 10;
                            break;
                    }
                    int step = 360 / div;
                    int aim = enemylist[enemyIdx][10];
                    for (int a = 0; a < 360; a += step) {
                        int ang = aim + a + (d8 << 2);
                        for (int o = -2; o <= 2; o++) {
                            host.spawnEnemyBullet(537, 37, ang + o, 32, ex04, ey04);
                        }
                    }
                }

                switch (d04) {
                    case 76:
                    case 196:
                        enemylist[enemyIdx][3] = 5;
                        break;
                    case 77:
                    case 197:
                        enemylist[enemyIdx][3] = 6;
                        break;
                    case 78:
                    case 198:
                        enemylist[enemyIdx][3] = 7;
                        break;
                    case 79:
                    case 199:
                        enemylist[enemyIdx][3] = 8;
                        break;
                    case 81:
                    case 201:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d04 == 80 || d04 == 200) {
                    enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey04, host.getPlayerXFixed() - ex04);
                    int div;
                    switch (host.getLevel()) {
                        case 0:
                            div = 5;
                            break;
                        case 1:
                            div = 6;
                            break;
                        case 2:
                            div = 8;
                            break;
                        case 3:
                        default:
                            div = 10;
                            break;
                    }
                    int step = 70 / div;
                    int aim = enemylist[enemyIdx][10];
                    for (int a = -70; a <= 70; a += step) {
                        for (int k = 0; k < 5; k++) {
                            host.spawnEnemyBullet(569, 0, aim + a, 28 + (k << 2), ex04, ey04);
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
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 62 + host.getLevel();
                    wb(11);
                }
                return;

            case 11:
                bspellcnt = 59;
                switch (host.getMGCnt()) {
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
                enemylist[enemyIdx][14] = 262144;
                enemylist[enemyIdx][15] = 262144;
                if (host.getMGCnt() > 30) {
                    bossWrk[3] = 0;
                    bossWrk[4] = 0;
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(12);
                    j = 1400;
                    yb(enemyIdx, 7, 2);
                }
                return;

            case 12:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }

                int d12 = host.getMGCnt() % 330;
                int ex12 = enemylist[enemyIdx][5];
                int ey12 = enemylist[enemyIdx][6];

                switch (d12) {
                    case 16:
                        enemylist[enemyIdx][3] = 5;
                        break;
                    case 17:
                        enemylist[enemyIdx][3] = 6;
                        break;
                    case 18:
                        enemylist[enemyIdx][3] = 7;
                        break;
                    case 20:
                        enemylist[enemyIdx][3] = 8;
                        break;
                    case 40:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d12 >= 20 && d12 < 40) {
                    if (d12 == 20) {
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey12, host.getPlayerXFixed() - ex12);
                    }

                    int d4;
                    int d5;
                    int d6;
                    switch (host.getLevel()) {
                        case 0:
                            d4 = 40;
                            d5 = 2;
                            d6 = 4;
                            break;
                        case 1:
                            d4 = 32;
                            d5 = 3;
                            d6 = 3;
                            break;
                        case 2:
                            d4 = 28;
                            d5 = 4;
                            d6 = 2;
                            break;
                        case 3:
                        default:
                            d4 = 24;
                            d5 = 5;
                            d6 = 1;
                            break;
                    }

                    if (d12 % d6 == 0) {
                        int span = d4 * d5;
                        int aim = enemylist[enemyIdx][10];
                        int sp = (d12 - 20) << 5;

                        for (int n4 = 0; n4 <= span; n4 += d4) {
                            int off = (n4 + (d12 << 2)) % span - (span >> 1);
                            int ang = aim + off;
                            int se = (int) ((((long) Trig.cos(ang)) * (long) sp) >> 3);
                            int te = (int) ((((long) Trig.sin(ang)) * (long) sp) >> 3);
                            host.spawnEnemyBullet(521, 1, ang, 12, ex12 + se, ey12 + te);

                            off = (span - n4 + (d12 << 2)) % span - (span >> 1);
                            ang = aim + off;
                            se = (int) ((((long) Trig.cos(ang)) * (long) sp) >> 3);
                            te = (int) ((((long) Trig.sin(ang)) * (long) sp) >> 3);
                            host.spawnEnemyBullet(521, 1, ang, 12, ex12 + se, ey12 + te);
                        }
                    }
                }

                if (d12 >= 40 && d12 < 70) {
                    enemylist[enemyIdx][10] = 450;

                    int d6;
                    int d7;
                    int d8;
                    switch (host.getLevel()) {
                        case 0:
                            d6 = 30;
                            d7 = 2;
                            d8 = 6;
                            break;
                        case 1:
                            d6 = 24;
                            d7 = 3;
                            d8 = 5;
                            break;
                        case 2:
                            d6 = 18;
                            d7 = 4;
                            d8 = 4;
                            break;
                        case 3:
                        default:
                            d6 = 14;
                            d7 = 5;
                            d8 = 3;
                            break;
                    }

                    int d9 = (d12 - 40) << 3;
                    if (d12 % d8 == 0) {
                        for (int n5 = 0; n5 < d7; n5++) {
                            int base = 450;
                            host.spawnEnemyBullet(569, 0, base + n5 * d6 + d9 - 20 + 360, 42, ex12, ey12);
                            host.spawnEnemyBullet(569, 0, base - n5 * d6 + d9 - 20 + 360, 42, ex12, ey12);
                            host.spawnEnemyBullet(569, 0, base + n5 * d6 - d9 + 20 + 360, 42, ex12, ey12);
                            host.spawnEnemyBullet(569, 0, base - n5 * d6 - d9 + 20 + 360, 42, ex12, ey12);
                        }
                    }
                }

                switch (d12) {
                    case 156:
                        enemylist[enemyIdx][3] = 9;
                        break;
                    case 157:
                        enemylist[enemyIdx][3] = 10;
                        break;
                    case 158:
                        enemylist[enemyIdx][3] = 11;
                        break;
                    case 159:
                        enemylist[enemyIdx][3] = 12;
                        break;
                    case 161:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d12 == 160) {
                    enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey12, host.getPlayerXFixed() - ex12);

                    int d4;
                    int d5;
                    switch (host.getLevel()) {
                        case 0:
                            d4 = 3;
                            d5 = 3;
                            break;
                        case 1:
                            d4 = 4;
                            d5 = 4;
                            break;
                        case 2:
                            d4 = 5;
                            d5 = 5;
                            break;
                        case 3:
                        default:
                            d4 = 6;
                            d5 = 6;
                            break;
                    }

                    int aim = enemylist[enemyIdx][10];
                    for (int n6 = -d4; n6 <= d4; n6++) {
                        for (int n7 = 0; n7 < d5; n7++) {
                            host.spawnEnemyBullet(521, 26, aim + (n6 << 4), 24 + (n7 << 3), ex12, ey12);
                            host.spawnEnemyBullet(537, 37, aim + (n6 << 5) + 180, 18 + (n7 << 2), ex12, ey12);
                        }
                    }
                }

                if (d12 == 60 || d12 == 250) {
                    host.setTimeStop(60);
                }

                if (d12 == 80 || d12 == 190 || d12 == 220 || d12 == 270) {
                    if (host.getMGCnt() == 80) {
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[idx] = 127;
                            bossy[idx] = 48;
                        } else {
                            bossx[idx] = 67;
                            bossy[idx] = 48;
                        }
                    } else if (enemylist[enemyIdx][5] < 6356992) {
                        bossx[idx] = 147;
                        bossy[idx] = 68;
                    } else {
                        bossx[idx] = 47;
                        bossy[idx] = 68;
                    }
                }

                if ((d12 > 80 && d12 < 110) || (d12 > 270 && d12 < 300)) {
                    int d8;
                    switch (host.getLevel()) {
                        case 0:
                            d8 = 32;
                            break;
                        case 1:
                            d8 = 28;
                            break;
                        case 2:
                            d8 = 24;
                            break;
                        case 3:
                        default:
                            d8 = 20;
                            break;
                    }

                    int px = host.getPlayerXFixed();
                    int py = host.getPlayerYFixed();

                    int dir = (d12 << 5) % 360;
                    int sp0 = (40 + ((d12 & 0xD) << 2)) << 3;
                    int se = (int) ((((long) Trig.cos(dir)) * (long) sp0) >> 3);
                    int te = (int) ((((long) Trig.sin(dir)) * (long) sp0) >> 3);
                    int sx = px + se;
                    int sy = py + te;
                    int base = (dir + 180) % 360;
                    int v0 = 24 + ((d12 & 0xD) << 2);
                    host.spawnEnemyBullet(537, 0, base - d8 + 360, v0, sx, sy);
                    host.spawnEnemyBullet(537, 0, base + d8, v0, sx, sy);

                    dir = (360 - ((d12 << 5) % 360)) % 360;
                    sp0 = (50 + ((d12 & 0x15) << 2)) << 3;
                    se = (int) ((((long) Trig.cos(dir)) * (long) sp0) >> 3);
                    te = (int) ((((long) Trig.sin(dir)) * (long) sp0) >> 3);
                    sx = px + se;
                    sy = py + te;
                    base = (dir + 180) % 360;
                    v0 = 16 + ((d12 & 0x15) << 2);
                    host.spawnEnemyBullet(537, 0, base - d8 + 360, v0, sx, sy);
                    host.spawnEnemyBullet(537, 0, base + d8, v0, sx, sy);
                }

                if (d12 >= 190 && d12 < 240) {
                    enemylist[enemyIdx][10] = 450;

                    int d6;
                    int d7;
                    int d8;
                    switch (host.getLevel()) {
                        case 0:
                            d6 = 30;
                            d7 = 2;
                            d8 = 6;
                            break;
                        case 1:
                            d6 = 24;
                            d7 = 3;
                            d8 = 5;
                            break;
                        case 2:
                            d6 = 18;
                            d7 = 4;
                            d8 = 4;
                            break;
                        case 3:
                        default:
                            d6 = 14;
                            d7 = 5;
                            d8 = 3;
                            break;
                    }

                    int d9 = (d12 - 190) << 3;
                    if (d12 % d8 == 0) {
                        for (int n8 = 0; n8 < d7; n8++) {
                            int base = 450;
                            host.spawnEnemyBullet(569, 0, base + n8 * d6 + d9 - 20 + 360, 42, ex12, ey12);
                            host.spawnEnemyBullet(569, 0, base - n8 * d6 + d9 - 20 + 360, 42, ex12, ey12);
                            host.spawnEnemyBullet(569, 0, base + n8 * d6 - d9 + 20 + 360, 42, ex12, ey12);
                            host.spawnEnemyBullet(569, 0, base - n8 * d6 - d9 + 20 + 360, 42, ex12, ey12);
                        }
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
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 66 + host.getLevel();
                    wb(14);
                }
                return;

            case 14:
                bspellcnt = 59;
                switch (host.getMGCnt()) {
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
                if (host.getMGCnt() > 30) {
                    enemylist[enemyIdx][3] = 0;
                    bspellstep = 2;
                    batkf = true;
                    host.setBbaria(0);
                    wb(15);
                    j = 2000;
                    yb(enemyIdx, 7, 2);
                }
                return;

            case 15:
                if (bspellcnt < 0) {
                    j = 0;
                    host.setQ(false);
                    host.stageJb(enemyIdx);
                    return;
                }
                int d15 = (host.getMGCnt() + 1) % 140;
                int ex15 = enemylist[enemyIdx][5];
                int ey15 = enemylist[enemyIdx][6];
                if (d15 == 60) {
                    host.setTimeStop(10);
                    host.aimEnemyBulletsToPlayer();
                }
                switch (d15) {
                    case 16:
                        enemylist[enemyIdx][3] = 9;
                        break;
                    case 17:
                        enemylist[enemyIdx][3] = 10;
                        break;
                    case 18:
                        enemylist[enemyIdx][3] = 11;
                        break;
                    case 20:
                        enemylist[enemyIdx][3] = 12;
                        break;
                    case 30:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d15 >= 20 && d15 < 30) {
                    if (d15 == 20) {
                        enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey15, host.getPlayerXFixed() - ex15);
                    }

                    if ((d15 & 0x1) == 0) {
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
                            case 3:
                            default:
                                div = 8;
                                break;
                        }
                        int step = 60 / div;
                        int aim = enemylist[enemyIdx][10];
                        for (int a = 0; a < 60; a += step) {
                            for (int k = 0; k < 3; k++) {
                                int angR = aim + a + (step >> 1) + 12;
                                int angL = aim - a - (step >> 1) - 12;
                                int sp = 20 + (k << 4);
                                host.spawnEnemyBullet(569, 0, angR, sp, ex15, ey15);
                                host.spawnEnemyBullet(569, 0, angL, sp, ex15, ey15);
                            }
                        }
                    }

                    if (((d15 & 0x3) == 0x1) && host.getLevel() > 0) {
                        int div;
                        switch (host.getLevel()) {
                            case 1:
                                div = 10;
                                break;
                            case 2:
                                div = 15;
                                break;
                            case 3:
                            default:
                                div = 20;
                                break;
                        }
                        int step = 360 / div;
                        int aim = enemylist[enemyIdx][10];
                        for (int a = 0; a < 360; a += step) {
                            host.spawnEnemyBullet(537, 0, aim + a, 24, ex15, ey15);
                        }
                    }
                }

                if (d15 == 70) {
                    bossy[idx] = 40;
                    enemylist[enemyIdx][5] = bossx[idx] << 16;
                    enemylist[enemyIdx][6] = bossy[idx] << 16;

                    int step;
                    switch (host.getLevel()) {
                        case 0:
                            step = 15;
                            break;
                        case 1:
                            step = 10;
                            break;
                        case 2:
                            step = 8;
                            break;
                        case 3:
                        default:
                            step = 6;
                            break;
                    }

                    int toggle = 0;
                    int mod = (host.getMGCnt() + 1) % 280;
                    if (mod < 130) {
                        if (host.getPlayerXFixed() > enemylist[enemyIdx][5]) {
                            bossx[idx] = 37;
                        } else {
                            bossx[idx] = 157;
                        }

                        for (int x = 0; x < 194; x += step) {
                            if (host.getLevel() >= 2) {
                                host.spawnEnemyBullet(521, 0, 450, 24 - (toggle << 3), x << 16, 524288);
                            }
                            host.spawnEnemyBullet(521, 0, 450, 20 + (toggle << 3), x << 16, 524288);
                            toggle ^= 0x1;
                        }
                    } else if (host.getPlayerXFixed() > enemylist[enemyIdx][5]) {
                        bossx[idx] = 57;
                    } else {
                        bossx[idx] = 137;
                    }

                    if (mod >= 130) {
                        step += 2;
                        for (int y = 8; y < 234; y += step) {
                            if (host.getLevel() >= 2) {
                                host.spawnEnemyBullet(521, 0, 360, 18 - (toggle << 3), 0, y << 16);
                            }
                            host.spawnEnemyBullet(521, 0, 360, 14 + (toggle << 3), 0, y << 16);
                            toggle ^= 0x1;

                            if (host.getLevel() >= 2) {
                                host.spawnEnemyBullet(521, 0, 540, 18 - (toggle << 3), 12713984, y << 16);
                            }
                            host.spawnEnemyBullet(521, 0, 540, 14 + (toggle << 3), 12713984, y << 16);
                        }
                    }
                }
                return;

            case 16:
                bossx[idx] = 97;
                bossy[idx] = 64;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[enemyIdx][14] = 65536;
                    enemylist[enemyIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 70 + host.getLevel();
                    wb(17);
                }
                return;

            case 17:
                bspellcnt = 59;
                switch (host.getMGCnt()) {
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
                if (host.getMGCnt() > 30) {
                    enemylist[enemyIdx][14] = 327680;
                    enemylist[enemyIdx][15] = 327680;
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
                int d18 = host.getMGCnt() % 260;

                int ex18 = enemylist[enemyIdx][5];
                int ey18 = enemylist[enemyIdx][6];

                if (d18 < 100) {
                    int count;
                    switch (host.getLevel()) {
                        case 0:
                            count = 3;
                            break;
                        case 1:
                            count = 4;
                            break;
                        case 2:
                            count = 5;
                            break;
                        case 3:
                        default:
                            count = 6;
                            break;
                    }

                    int base0 = (j + (d18 << 4) + (count << 5)) % 360;
                    int angA = base0;
                    int angB = 360 - base0;
                    if ((d18 & 0x3) == 0) {
                        angA += 100;
                        angB += 100;
                    }

                    for (int i = 0; i < count; i++) {
                        int sp = 52 - (i << 2);
                        host.spawnEnemyBullet(537, 7, angA, sp, ex18, ey18);
                    }
                    for (int i = 0; i < count; i++) {
                        int sp = 52 - (i << 2);
                        host.spawnEnemyBullet(537, 7, angB, sp, ex18, ey18);
                    }
                }

                if (d18 == 130) {
                    host.setTimeStop(50);
                }

                if (d18 > 140 && d18 < 170) {
                    int count;
                    int baseSp;
                    int interval;
                    switch (host.getLevel()) {
                        case 0:
                            count = 1;
                            baseSp = 30;
                            interval = 3;
                            break;
                        case 1:
                            count = 3;
                            baseSp = 36;
                            interval = 2;
                            break;
                        case 2:
                            count = 4;
                            baseSp = 40;
                            interval = 1;
                            break;
                        case 3:
                        default:
                            count = 5;
                            baseSp = 44;
                            interval = 1;
                            break;
                    }

                    if (d18 % interval == 0) {
                        int step = d18 - 140;
                        int px = host.getPlayerXFixed();
                        int py = host.getPlayerYFixed();

                        int base0 = (j + (d18 << 4) + (count << 5)) % 360;
                        int angA = base0;
                        int angB = 360 - base0;
                        if ((d18 & 0x3) == 0) {
                            angA += 100;
                            angB += 100;
                        }

                        int dist = 500 + step * 25;
                        for (int i = 0; i < count; i++) {
                            int sp = baseSp - (i << 1);

                            int se = (int) ((((long) Trig.cos(angA)) * (long) dist) >> 3);
                            int te = (int) ((((long) Trig.sin(angA)) * (long) dist) >> 3);
                            host.spawnEnemyBullet(521, 39, angA + 180, sp, px + se, py + te);
                        }
                        for (int i = 0; i < count; i++) {
                            int sp = baseSp - (i << 1);

                            int se = (int) ((((long) Trig.cos(angB)) * (long) dist) >> 3);
                            int te = (int) ((((long) Trig.sin(angB)) * (long) dist) >> 3);
                            host.spawnEnemyBullet(521, 39, angB + 180, sp, px + se, py + te);
                        }
                    }
                }

                if (d18 == 200) {
                    enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey18, host.getPlayerXFixed() - ex18);

                    int div;
                    int count;
                    switch (host.getLevel()) {
                        case 0:
                            div = 12;
                            count = 2;
                            break;
                        case 1:
                            div = 18;
                            count = 3;
                            break;
                        case 2:
                            div = 20;
                            count = 3;
                            break;
                        case 3:
                        default:
                            div = 24;
                            count = 5;
                            break;
                    }

                    int step = 360 / div;
                    int aim = enemylist[enemyIdx][10];
                    for (int a = 0; a < 360; a += step) {
                        for (int k = 0; k < count; k++) {
                            host.spawnEnemyBullet(537, 37, aim + a, 28 + (k << 2), ex18, ey18);
                        }
                    }
                }

                if (d18 == 210) {
                    if (host.getMGCnt() == 210) {
                        if (host.getPlayerXFixed() < enemylist[enemyIdx][5]) {
                            bossx[idx] = 137;
                        } else {
                            bossx[idx] = 57;
                        }
                        bossy[idx] = 54;
                        return;
                    }
                    if (enemylist[enemyIdx][5] < 6356992) {
                        bossx[idx] = 117;
                    } else {
                        bossx[idx] = 77;
                    }
                    bossy[idx] = 64;
                    return;
                }
                return;

            case 19:
                bossx[idx] = 97;
                bossy[idx] = 121;
                enemylist[enemyIdx][14] = 458752;
                enemylist[enemyIdx][15] = 458752;
                batkf = false;
                host.setBbaria(2);
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    enemylist[enemyIdx][14] = 65536;
                    enemylist[enemyIdx][15] = 65536;
                    bspellstep = 1;
                    spellId = 74 + host.getLevel();
                    wb(20);
                }
                return;

            case 20:
                bspellcnt = 59;
                switch (host.getMGCnt()) {
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
                if (host.getMGCnt() > 30) {
                    enemylist[enemyIdx][14] = 0;
                    enemylist[enemyIdx][15] = 0;
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

                int ex21 = enemylist[enemyIdx][5];
                int ey21 = enemylist[enemyIdx][6];

                if (host.getLevel() <= 1) {
                    int d0 = (host.getMGCnt() + 10) % 360;
                    if (host.getMGCnt() <= 10) {
                        return;
                    }

                    if (host.getLevel() >= 1) {
                        int ang = (360 - ((d0 * 15) % 360)) + 270;
                        host.spawnEnemyBullet(537, 0, ang, 40, ex21, ey21);
                    }
                    if (d0 % 3 == 0) {
                        int ang = (360 - ((d0 * 6) % 360)) + 270;
                        host.spawnEnemyBullet(553, 0, ang, 36, ex21, ey21);
                    }
                    if (d0 % 5 == 0) {
                        int ang = (360 - (d0 % 360)) + 270;
                        host.spawnEnemyBullet(569, 0, ang, 32, ex21, ey21);
                    }
                    if ((d0 & 0xF) == 0) {
                        int step = 360 >> (3 + host.getLevel());
                        for (int a = 0; a < 360; a += step) {
                            host.spawnEnemyBullet(84, 11, (d0 << 2) + a, 24, ex21, ey21);
                        }
                        return;
                    }
                    return;
                }

                int d0 = host.getMGCnt() + 10;
                int mult = d0 / 5;
                if (mult > 12) {
                    mult = 12;
                }
                if (host.getMGCnt() > 10) {
                    int ang;
                    ang = (360 - ((d0 * 15 * mult / 5) % 360)) + 270;
                    host.spawnEnemyBullet(537, 0, ang, 40, ex21, ey21);

                    if ((d0 & 0x1) == 0) {
                        ang = (360 - ((d0 * 6 * mult / 5) % 360)) + 270;
                        host.spawnEnemyBullet(553, 0, ang, 36, ex21, ey21);
                    }
                    if ((d0 & 0x3) == 0) {
                        ang = (360 - ((d0 * mult / 5) % 360)) + 270;
                        host.spawnEnemyBullet(569, 0, ang, 32, ex21, ey21);
                    }

                    int moveType = 11;
                    int bulletId = 84;
                    if (d0 > 100) {
                        moveType = 10;
                        bulletId = 81;
                    }
                    if ((d0 & 0xF) == 0) {
                        int step = 360 >> (2 + host.getLevel());
                        for (int a = 0; a < 360; a += step) {
                            host.spawnEnemyBullet(bulletId, moveType, (d0 << 2) + a, 24, ex21, ey21);
                        }
                    }
                }

                if (d0 > 100 && (d0 & 0x3) == 0) {
                    int ang = ((d0 * 5) % 360) + 270;
                    int dist = 1000;
                    int se = (int) ((((long) Trig.cos(ang)) * (long) dist) >> 3);
                    int te = (int) ((((long) Trig.sin(ang)) * (long) dist) >> 3);
                    host.spawnEnemyBullet(675, 39, ang + 180, 24, ex21 + se, ey21 + te);

                    if (host.getLevel() == 3) {
                        ang = ((d0 << 2) % 360) + 180;
                        se = (int) ((((long) Trig.cos(ang)) * (long) dist) >> 3);
                        te = (int) ((((long) Trig.sin(ang)) * (long) dist) >> 3);
                        host.spawnEnemyBullet(675, 39, ang + 180, 24, ex21 + se, ey21 + te);
                        return;
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
                if (host.getMGCnt() > 30) {
                    host.setQ(true);
                    bspellstep = 1;
                    spellId = 113;
                    wb(38);
                }
                return;

            case 38:
                bspellcnt = 60;
                switch (host.getMGCnt()) {
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
                if (host.getMGCnt() > 30) {
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

                int d0_39 = host.getMGCnt() % 480;
                if (d0_39 <= 0) {
                    return;
                }

                int ex_39 = enemylist[enemyIdx][5];
                int ey_39 = enemylist[enemyIdx][6];

                if ((d0_39 & 0x1F) == 0) {
                    enemylist[enemyIdx][10] = EnemyBulletSystem.arcTan2Deg(host.getPlayerYFixed() - ey_39, host.getPlayerXFixed() - ex_39);
                }
                int aim_39 = enemylist[enemyIdx][10];

                switch (d0_39) {
                    case 16:
                        enemylist[enemyIdx][3] = 9;
                        break;
                    case 17:
                        enemylist[enemyIdx][3] = 10;
                        break;
                    case 18:
                        enemylist[enemyIdx][3] = 11;
                        break;
                    case 20:
                        enemylist[enemyIdx][3] = 12;
                        break;
                    case 120:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (d0_39 >= 20 && d0_39 < 130) {
                    if ((d0_39 & 0x1) == 0) {
                        int d4_39 = 48;
                        int d5_39 = 6;
                        int span_39 = d4_39 * d5_39;
                        for (int v_39 = 0; v_39 <= span_39; v_39 += d4_39) {
                            int off_39 = (v_39 + (d0_39 << 3)) % span_39 - (span_39 >> 1);
                            host.spawnEnemyBullet(569, 7, aim_39 + off_39, 40, ex_39, ey_39);

                            off_39 = (span_39 - v_39 + (d0_39 << 2)) % span_39 - (span_39 >> 1);
                            host.spawnEnemyBullet(569, 7, aim_39 + off_39, 40, ex_39, ey_39);
                        }
                    }
                    if ((d0_39 & 0x7) == 0) {
                        int ang_39 = aim_39;
                        ang_39 += ang_39 * 40;
                        ang_39 += d0_39 << 3;
                        int spd_39 = 16 + (((d0_39 >> 3) & 0x3) << 3);
                        host.spawnEnemyBullet(92, 83, ang_39, spd_39, ex_39, ey_39);
                    }
                }

                int da_39 = d0_39 - 160;
                switch (da_39) {
                    case 166:
                        enemylist[enemyIdx][3] = 5;
                        break;
                    case 167:
                        enemylist[enemyIdx][3] = 6;
                        break;
                    case 168:
                        enemylist[enemyIdx][3] = 7;
                        break;
                    case 170:
                        enemylist[enemyIdx][3] = 8;
                        break;
                    case 270:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (da_39 >= 20 && da_39 < 130) {
                    if ((da_39 & 0x1) == 0) {
                        int d8_39 = 120 - da_39;
                        int d4_39 = 64;
                        int d5_39 = 8;
                        int span_39 = d4_39 * d5_39;
                        for (int v_39 = 0; v_39 <= span_39; v_39 += d4_39) {
                            int off_39 = (v_39 + (d8_39 << 3)) % span_39 - (span_39 >> 1);
                            host.spawnEnemyBullet(569, 7, aim_39 + off_39, 40, ex_39, ey_39);

                            off_39 = (span_39 - v_39 + (d8_39 << 2)) % span_39 - (span_39 >> 1);
                            host.spawnEnemyBullet(569, 7, aim_39 + off_39, 40, ex_39, ey_39);
                        }
                    }
                    if ((da_39 & 0x3) == 0) {
                        int d3_39 = (da_39 << 3) - 194;
                        for (int n_39 = 0; n_39 < 6; ++n_39) {
                            int d4_39 = d3_39 + (n_39 * 194) / 6;
                            if (d4_39 >= 0) {
                                int d5_39 = d3_39 % 388;
                                if (d5_39 > 194) {
                                    d5_39 = 388 - d5_39;
                                }
                                host.spawnEnemyBullet(521, 0, 420, 20, d5_39 << 16, -131072);
                                host.spawnEnemyBullet(521, 0, 450, 20, d5_39 << 16, -131072);
                                host.spawnEnemyBullet(521, 0, 480, 20, d5_39 << 16, -131072);

                                d5_39 = 194 - d5_39;
                                host.spawnEnemyBullet(537, 0, 270, 20, d5_39 << 16, 15990784);
                                host.spawnEnemyBullet(537, 0, 270, 20, d5_39 << 16, 16646144);
                                host.spawnEnemyBullet(537, 0, 270, 20, d5_39 << 16, 17301504);
                            }
                        }
                    }
                }

                da_39 = d0_39 - 360;
                switch (da_39) {
                    case 16:
                        enemylist[enemyIdx][3] = 9;
                        break;
                    case 17:
                        enemylist[enemyIdx][3] = 10;
                        break;
                    case 18:
                        enemylist[enemyIdx][3] = 11;
                        break;
                    case 20:
                        enemylist[enemyIdx][3] = 12;
                        break;
                    case 120:
                        enemylist[enemyIdx][3] = 0;
                        break;
                }

                if (da_39 >= 20 && da_39 < 130) {
                    if ((da_39 & 0x1) == 0) {
                        int d4_39 = 32;
                        int d5_39 = 3;
                        int span_39 = d4_39 * d5_39;
                        for (int v_39 = 0; v_39 <= span_39; v_39 += d4_39) {
                            int off_39 = (v_39 + (da_39 << 3)) % span_39 - (span_39 >> 1);
                            host.spawnEnemyBullet(569, 7, aim_39 + off_39, 40, ex_39, ey_39);

                            off_39 = (span_39 - v_39 + (da_39 << 2)) % span_39 - (span_39 >> 1);
                            host.spawnEnemyBullet(569, 7, aim_39 + off_39, 40, ex_39, ey_39);
                        }
                    }
                    if ((da_39 & 0x7) == 0) {
                        int d3_39 = (da_39 << 3) % 360;
                        host.spawnEnemyBullet(553, 82, 540 + d3_39, 34, ex_39, ey_39);
                        d3_39 = 360 - d3_39;
                        host.spawnEnemyBullet(553, 81, 360 + d3_39, 34, ex_39, ey_39);
                    }
                }
                return;
            }

            default:
                return;
        }
    }
}
