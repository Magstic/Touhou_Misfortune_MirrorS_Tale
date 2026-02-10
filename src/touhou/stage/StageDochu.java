package touhou.stage;

import touhou.EnemyBulletSystem;
import touhou.battle.BattleMath;

public class StageDochu {
    private final EnemyBulletSystem enemyBullets;

    // Mid-stage (dochu) enemy shooting logic.
    // NOTE: This class exists to keep BattleEngine focused on generic battle systems.
    // The methods below mirror the original GameCanvas.stageEb() behavior.
    public StageDochu(EnemyBulletSystem enemyBullets) {
        this.enemyBullets = enemyBullets;
    }

    // Mirrors GameCanvas.stageEb (partial): stage 1/2 fodder shooting patterns (enemyType 10/20/21/22/23/24).
    public void tickEnemyShootingStageA(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        // Stage 1/2: enemyType 10 simple aimed shots.
        if (enemyType == 10 && ey < 14680064 && (enemylist[enemyIndex][2] == 10 || enemylist[enemyIndex][2] == 25)) {
            if (level != 0) {
                enemyBullets.cc(81, 3, 0, 48, ex, ey, px, py);
            }
            if (level >= 2) {
                enemyBullets.cc(81, 3, 0, 56, ex + 196608, ey, px, py);
                enemyBullets.cc(81, 3, 0, 64, ex, ey + 196608, px, py);
                enemyBullets.cc(81, 3, 0, 72, ex - 196608, ey, px, py);
                enemyBullets.cc(81, 3, 0, 80, ex, ey - 196608, px, py);
            }
        }

        // Stage 1/2: enemyType 20 aimed spread.
        if (enemyType == 20 && enemylist[enemyIndex][2] > 15 && ey < 7929856) {
            int ang = BattleMath.arcTan2Deg(py - ey, px - ex);
            if (enemylist[enemyIndex][2] == 20) {
                if (level == 1) {
                    enemyBullets.cc(81, 0, ang, 40, ex, ey, px, py);
                } else if (level == 2) {
                    enemyBullets.cc(81, 0, ang, 40, ex, ey, px, py);
                    enemyBullets.cc(81, 0, ang + 15, 40, ex, ey, px, py);
                    enemyBullets.cc(81, 0, ang - 15, 40, ex, ey, px, py);
                } else if (level >= 3) {
                    enemyBullets.cc(81, 0, ang, 40, ex, ey, px, py);
                    enemyBullets.cc(81, 0, ang + 10, 40, ex, ey, px, py);
                    enemyBullets.cc(81, 0, ang - 10, 40, ex, ey, px, py);
                    enemyBullets.cc(81, 0, ang + 20, 40, ex, ey, px, py);
                    enemyBullets.cc(81, 0, ang - 20, 40, ex, ey, px, py);
                }
            }
        }

        // Stage 1/2: enemyType 21 accelerating ring.
        if (enemyType == 21 && enemylist[enemyIndex][2] > 15 && ey < 7929856
                && (enemylist[enemyIndex][2] == 20 || (level > 1 && enemylist[enemyIndex][2] == 25) || (level > 1 && enemylist[enemyIndex][2] == 30))) {
            int div;
            if (level == 0) {
                div = 12;
            } else if (level == 1) {
                div = 18;
            } else if (level == 2) {
                div = 24;
            } else {
                div = 36;
            }
            for (int i = 0; i < 360; i += 360 / div) {
                enemyBullets.cc(81, 2, i, 80, ex, ey, px, py);
            }
        }

        if (enemyType == 22 && (level >= 2 || (enemylist[enemyIndex][2] & 1) == 0)) {
            if (enemylist[enemyIndex][2] > 15 && enemylist[enemyIndex][2] < 25) {
                int a = enemylist[enemyIndex][2] * 10;
                enemyBullets.cc(81, 1, a, 8, ex, ey, px, py);
                enemyBullets.cc(81, 1, a + 90, 8, ex, ey, px, py);
                enemyBullets.cc(81, 1, a + 180, 8, ex, ey, px, py);
                enemyBullets.cc(81, 1, a + 270, 8, ex, ey, px, py);
            } else if (enemylist[enemyIndex][2] > 25 && enemylist[enemyIndex][2] < 35) {
                int a = (enemylist[enemyIndex][2] * 10) % 360;
                enemyBullets.cc(81, 1, 360 - a, 24, ex, ey, px, py);
                enemyBullets.cc(81, 1, 360 - (a + 90), 24, ex, ey, px, py);
                enemyBullets.cc(81, 1, 360 - (a + 180), 24, ex, ey, px, py);
                enemyBullets.cc(81, 1, 360 - (a + 270), 24, ex, ey, px, py);
            }
        }

        if (enemyType == 23 && (level >= 2 || (enemylist[enemyIndex][2] & 1) == 0) && enemylist[enemyIndex][2] > 15 && enemylist[enemyIndex][2] < 25) {
            int a = enemylist[enemyIndex][2] * 10;
            enemyBullets.cc(82, 1, a, 24, ex, ey, px, py);
            enemyBullets.cc(82, 1, a + 90, 24, ex, ey, px, py);
            enemyBullets.cc(82, 1, a + 180, 24, ex, ey, px, py);
            enemyBullets.cc(82, 1, a + 270, 24, ex, ey, px, py);
        }

        if (enemyType == 24 && (level >= 2 || (enemylist[enemyIndex][2] & 1) == 0) && enemylist[enemyIndex][2] > 15 && enemylist[enemyIndex][2] < 25) {
            int a = (enemylist[enemyIndex][2] * 10) % 360;
            enemyBullets.cc(82, 1, 360 - a, 24, ex, ey, px, py);
            enemyBullets.cc(82, 1, 360 - (a + 90), 24, ex, ey, px, py);
            enemyBullets.cc(82, 1, 360 - (a + 180), 24, ex, ey, px, py);
            enemyBullets.cc(82, 1, 360 - (a + 270), 24, ex, ey, px, py);
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 1/2 fodder shooting patterns (enemyType 25/27).
    public void tickEnemyShootingStageB(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        if (enemyType == 25) {
            if (enemylist[enemyIndex][7] == 1 && enemylist[enemyIndex][2] % 20 == 0) {
                if (level == 4) {
                    for (int j = 0; j < 360; j += 15) {
                        enemyBullets.cc(82, 0, j, 40, ex, ey + 524288, px, py);
                        enemyBullets.cc(82, 0, j, 56, ex, ey + 262144, px, py);
                        enemyBullets.cc(82, 0, j, 72, ex, ey, px, py);
                    }
                } else {
                    enemyBullets.cc(82, 0, 270, 40, ex, ey + 524288, px, py);
                    enemyBullets.cc(82, 0, 270, 48, ex, ey + 393216, px, py);
                    enemyBullets.cc(82, 0, 270, 56, ex, ey + 262144, px, py);
                    enemyBullets.cc(82, 0, 270, 64, ex, ey + 131072, px, py);
                    enemyBullets.cc(82, 0, 270, 72, ex, ey, px, py);
                }
            }
            enemylist[enemyIndex][15] -= 1;
        }

        // Stage 1/2: enemyType 27 paired curve spread.
        if (enemyType == 27 && enemylist[enemyIndex][7] == 1) {
            if (enemylist[enemyIndex][9] == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            }
            if (enemylist[enemyIndex][9] % 3 == 0) {
                int d3 = 7;
                int base = enemylist[enemyIndex][10];
                int step = (enemylist[enemyIndex][9] << 2);
                enemyBullets.cc(84, 0, base + 180 + step, d3 << 3, ex, ey, px, py);
                enemyBullets.cc(84, 0, base + 180 - step, d3 << 3, ex, ey, px, py);
                if (level > 0) {
                    d3--;
                    enemyBullets.cc(84, 0, base + 180 + step + 5, d3 << 3, ex, ey, px, py);
                    enemyBullets.cc(84, 0, base + 180 - step - 5, d3 << 3, ex, ey, px, py);
                    if (level > 1) {
                        d3--;
                        enemyBullets.cc(84, 0, base + 180 + step + 10, d3 << 3, ex, ey, px, py);
                        enemyBullets.cc(84, 0, base + 180 - step - 10, d3 << 3, ex, ey, px, py);
                        if (level > 2) {
                            d3--;
                            enemyBullets.cc(84, 0, base + 180 + step + 15, d3 << 3, ex, ey, px, py);
                            enemyBullets.cc(84, 0, base + 180 - step - 15, d3 << 3, ex, ey, px, py);
                        }
                    }
                }
            }
            enemylist[enemyIndex][9] += 1;
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 5 fodder shooting patterns (enemyType 34/35).
    public void tickEnemyShootingStageC(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        // Stage 5: enemyType 34 spread with delayed accel shots.
        if (enemyType == 34 && enemylist[enemyIndex][7] == 1 && enemylist[enemyIndex][9] == 0) {
            enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);

            int div;
            if (level == 0) {
                div = 6;
            } else if (level == 1) {
                div = 8;
            } else if (level == 2) {
                div = 10;
            } else {
                div = 12;
            }

            int slot = 0;
            for (int ang = -60; ang <= 60; ang += 120 / div) {
                int aim = (enemylist[enemyIndex][10] + 360 + ang) % 360;
                enemyBullets.cc(603, 0, aim, 48, ex, ey, px, py);
                if ((slot & 1) == 0) {
                    enemyBullets.cc(603, 26, aim, 32, ex, ey, px, py);
                    enemyBullets.cc(603, 26, aim, 40, ex, ey, px, py);
                    enemyBullets.cc(603, 26, aim, 56, ex, ey, px, py);
                    enemyBullets.cc(603, 26, aim, 64, ex, ey, px, py);
                }
                slot++;
            }
            enemylist[enemyIndex][9] += 1;
        }

        // Stage 5: enemyType 35 negative accel fan.
        if (enemyType == 35 && enemylist[enemyIndex][7] == 1) {
            enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            if (enemylist[enemyIndex][9] < 10 && (enemylist[enemyIndex][9] & 1) == 0) {
                int spd = -8;
                int base = enemylist[enemyIndex][10];
                enemyBullets.cc(639, 1, base, spd << 3, ex, ey, px, py);
                enemyBullets.cc(639, 1, base + 20, spd << 3, ex, ey, px, py);
                enemyBullets.cc(639, 1, base - 20, spd << 3, ex, ey, px, py);
                if (level > 1) {
                    enemyBullets.cc(639, 1, base + 40, spd << 3, ex, ey, px, py);
                    enemyBullets.cc(639, 1, base - 40, spd << 3, ex, ey, px, py);
                    if (level > 2) {
                        enemyBullets.cc(639, 1, base + 60, spd << 3, ex, ey, px, py);
                        enemyBullets.cc(639, 1, base - 60, spd << 3, ex, ey, px, py);
                    }
                }
            }
            enemylist[enemyIndex][9] += 1;
        }
    }

    // Mirrors GameCanvas.stageEb (partial): fodder shooting patterns (enemyType 36/37/38/39).
    public void tickEnemyShootingStageD(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        if (enemyType == 36) {
            int d1;
            if (level == 0) {
                d1 = 3;
            } else if (level == 1) {
                d1 = 4;
            } else if (level == 2) {
                d1 = 6;
            } else {
                d1 = 8;
            }
            if ((enemylist[enemyIndex][9] & 1) == 0 && enemylist[enemyIndex][9] % 20 < (d1 << 1)) {
                enemyBullets.cc(86, 0, 450, 56, ex, ey, px, py);
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 37 && enemylist[enemyIndex][7] == 1) {
            int d1;
            if (level == 0) {
                d1 = 2;
            } else if (level == 1) {
                d1 = 3;
            } else if (level == 2) {
                d1 = 4;
            } else {
                d1 = 5;
            }
            if ((enemylist[enemyIndex][9] & 1) == 0) {
                for (int a = 0; a < 360; a += 360 / d1) {
                    int shift = (enemylist[enemyIndex][9] << 2);
                    enemyBullets.cc(86, 41, a - shift, 0, ex, ey, px, py);
                    enemyBullets.cc(86, 41, a + shift + 180, 0, ex, ey, px, py);
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 38 && enemylist[enemyIndex][7] == 1) {
            int d1;
            int d4;
            int d5;
            if (level == 0) {
                d1 = 2;
                d4 = 7;
                d5 = 4;
            } else if (level == 1) {
                d1 = 3;
                d4 = 6;
                d5 = 3;
            } else if (level == 2) {
                d1 = 4;
                d4 = 5;
                d5 = 2;
            } else {
                d1 = 5;
                d4 = 4;
                d5 = 2;
            }
            if (enemylist[enemyIndex][9] % d5 == 0) {
                for (int a = 0; a < 360; a += 360 / d1) {
                    int ang = a + (enemylist[enemyIndex][9] * d4) + 270;
                    if (level != 1) {
                        enemyBullets.cc(585, 0, ang, 56, ex, ey, px, py);
                    }
                    if (level >= 1) {
                        enemyBullets.cc(585, 0, ang + 8, 56, ex, ey, px, py);
                        enemyBullets.cc(585, 0, ang - 8, 56, ex, ey, px, py);
                    }
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 39 && enemylist[enemyIndex][7] == 1) {
            int d1;
            int d4;
            int d5;
            if (level == 0) {
                d1 = 2;
                d4 = 7;
                d5 = 4;
            } else if (level == 1) {
                d1 = 3;
                d4 = 6;
                d5 = 3;
            } else if (level == 2) {
                d1 = 4;
                d4 = 5;
                d5 = 2;
            } else {
                d1 = 5;
                d4 = 4;
                d5 = 2;
            }
            if (enemylist[enemyIndex][9] % d5 == 0) {
                for (int a = 0; a < 360; a += 360 / d1) {
                    int ang = a - (enemylist[enemyIndex][9] * d4) + 270;
                    if (level != 1) {
                        enemyBullets.cc(585, 0, ang, 56, ex, ey, px, py);
                    }
                    if (level >= 1) {
                        enemyBullets.cc(585, 0, ang + 8, 56, ex, ey, px, py);
                        enemyBullets.cc(585, 0, ang - 8, 56, ex, ey, px, py);
                    }
                }
            }
            enemylist[enemyIndex][9] += 1;
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 5 fodder shooting patterns (enemyType 54/55).
    public void tickEnemyShootingStageE(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        // Stage 5: enemyType 54 staggered sprays with follow-up ring.
        if (enemyType == 54) {
            int fireStep;
            int angleStep;
            if (level == 0) {
                fireStep = 4;
                angleStep = 18;
            } else if (level == 1) {
                fireStep = 3;
                angleStep = 15;
            } else if (level == 2) {
                fireStep = 2;
                angleStep = 10;
            } else {
                fireStep = 1;
                angleStep = 8;
            }

            if (enemylist[enemyIndex][7] == 1 && enemylist[enemyIndex][2] % fireStep == 0) {
                int offset = enemylist[enemyIndex][9] * angleStep;
                for (int i = 0; i < 6; i++) {
                    int spd = 32 + (i << 2);
                    if (enemylist[enemyIndex][9] == 0) {
                        enemyBullets.cc(335, 0, 450, spd, ex, ey, px, py);
                    } else {
                        enemyBullets.cc(335, 0, (180 + offset + 360) % 360 + 270, spd, ex, ey, px, py);
                        enemyBullets.cc(335, 0, (180 - offset + 360) % 360 + 270, spd, ex, ey, px, py);
                    }
                }
                enemylist[enemyIndex][9] += 1;
            }

            if (enemylist[enemyIndex][7] != 1 && enemylist[enemyIndex][9] > 0 && (enemylist[enemyIndex][2] & 1) == 0) {
                int div;
                if (level == 0) {
                    div = 10;
                } else if (level == 1) {
                    div = 12;
                } else if (level == 2) {
                    div = 15;
                } else {
                    div = 18;
                }
                for (int ang = 0; ang < 360; ang += 360 / div) {
                    int offset = enemylist[enemyIndex][9] << 1;
                    enemyBullets.cc(81, 2, (ang + offset + 360) % 360, 64, ex, ey, px, py);
                    enemyBullets.cc(81, 2, (ang - offset + 360) % 360, 64, ex, ey, px, py);
                }
                enemylist[enemyIndex][9] += 1;
            }
        }

        // Stage 5: enemyType 55 alternating ring shots.
        if (enemyType == 55) {
            int fireStep;
            if (level == 0) {
                fireStep = 16;
            } else if (level == 1) {
                fireStep = 12;
            } else if (level == 2) {
                fireStep = 10;
            } else {
                fireStep = 8;
            }

            if (enemylist[enemyIndex][7] == 1 && enemylist[enemyIndex][2] % fireStep == 0) {
                int div;
                if (level == 0) {
                    div = 10;
                } else if (level == 1) {
                    div = 15;
                } else if (level == 2) {
                    div = 15;
                } else {
                    div = 18;
                }
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
                for (int ang = 0; ang < 360; ang += 360 / div) {
                    if ((enemylist[enemyIndex][9] & 1) == 0) {
                        enemyBullets.cc(86, 9, enemylist[enemyIndex][10] + ang, 64 + (enemylist[enemyIndex][9] << 1), ex, ey, px, py);
                    } else {
                        enemyBullets.cc(86, 0, enemylist[enemyIndex][10] + ang, 48 + (enemylist[enemyIndex][9] << 1), ex, ey, px, py);
                    }
                }
                enemylist[enemyIndex][9] += 1;
            }
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 1/2/3 fodder shooting patterns (enemyType 28/26).
    public void tickEnemyShootingStageF(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        // Stage 1/2: enemyType 28 burst aimed shots.
        if (enemyType == 28 && enemylist[enemyIndex][7] == 1) {
            if (enemylist[enemyIndex][9] == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            }
            if (enemylist[enemyIndex][9] < 10 && (enemylist[enemyIndex][9] & 1) == 0) {
                int d3 = 3 + enemylist[enemyIndex][9];
                int base = enemylist[enemyIndex][10];
                enemyBullets.cc(86, 0, base, d3 << 3, ex, ey, px, py);
                if (level > 1) {
                    enemyBullets.cc(86, 0, base + 20, d3 << 3, ex, ey, px, py);
                    enemyBullets.cc(86, 0, base - 20, d3 << 3, ex, ey, px, py);
                    if (level > 2) {
                        enemyBullets.cc(86, 0, base + 40, d3 << 3, ex, ey, px, py);
                        enemyBullets.cc(86, 0, base - 40, d3 << 3, ex, ey, px, py);
                    }
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        // Stage 2/3: enemyType 26 aimed fan shots.
        if (enemyType == 26 && enemylist[enemyIndex][7] == 1 && enemylist[enemyIndex][2] % 20 == 0) {
            int ang = BattleMath.arcTan2Deg(py - ey, px - ex);
            int d3 = 7;

            if (level == 0) {
                enemyBullets.cc(82, 0, ang + 10, d3 << 3, ex, ey, px, py);
                enemyBullets.cc(82, 0, ang - 10, d3 << 3, ex, ey, px, py);
            } else {
                enemyBullets.cc(82, 0, ang, d3 << 3, ex, ey, px, py);
                d3--;
                enemyBullets.cc(82, 0, ang + 10, d3 << 3, ex, ey, px, py);
                enemyBullets.cc(82, 0, ang - 10, d3 << 3, ex, ey, px, py);
                if (level > 1) {
                    d3--;
                    enemyBullets.cc(82, 0, ang + 20, d3 << 3, ex, ey, px, py);
                    enemyBullets.cc(82, 0, ang, d3 << 3, ex, ey, px, py);
                    enemyBullets.cc(82, 0, ang - 20, d3 << 3, ex, ey, px, py);
                    if (level > 2) {
                        d3--;
                        enemyBullets.cc(82, 0, ang + 30, d3 << 3, ex, ey, px, py);
                        enemyBullets.cc(82, 0, ang + 10, d3 << 3, ex, ey, px, py);
                        enemyBullets.cc(82, 0, ang - 10, d3 << 3, ex, ey, px, py);
                        enemyBullets.cc(82, 0, ang - 30, d3 << 3, ex, ey, px, py);
                    }
                }
            }
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 2/3 fodder shooting patterns (enemyType 50/51/52).
    public void tickEnemyShootingStageG(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        if (enemyType == 50) {
            int step;
            if (level == 0 || level == 1) {
                step = 5;
            } else {
                step = 4;
            }
            if (enemylist[enemyIndex][2] % step == 0 && enemylist[enemyIndex][2] > 20) {
                enemyBullets.cc(82, 0, 0, 48, ex, ey, px, py);
            }
        }

        if (enemyType == 51 && enemylist[enemyIndex][2] % 5 == 0 && enemylist[enemyIndex][2] > 15 && ey > 4194304 && ey < 8585216) {
            int angStep;
            if (level == 0) {
                angStep = 60;
            } else if (level == 1) {
                angStep = 40;
            } else if (level == 2) {
                angStep = 30;
            } else {
                angStep = 20;
            }
            for (int a = 0; a < 360; a += angStep) {
                enemyBullets.cc(82, 0, a, 48, ex, ey, px, py);
            }
        }

        // Stage 2/3: enemyType 52 rotating accelerating double ring.
        if (enemyType == 52 && enemylist[enemyIndex][7] == 1) {
            if (enemylist[enemyIndex][9] == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            }

            if ((enemylist[enemyIndex][2] & 0x7) == 0) {
                int div;
                if (level == 0) {
                    div = 9;
                } else if (level == 1) {
                    div = 10;
                } else if (level == 2) {
                    div = 12;
                } else {
                    div = 18;
                }

                int rot = (enemylist[enemyIndex][9] << 2);
                for (int angBase = 0; angBase < 360; angBase += 360 / div) {
                    enemyBullets.cc(82, 2, angBase + rot, 88, ex, ey, px, py);
                    enemyBullets.cc(82, 2, angBase - rot, 88, ex, ey, px, py);
                }
                enemylist[enemyIndex][9] += 1;
            }
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 3 fodder shooting patterns (enemyType 29/30).
    public void tickEnemyShootingStageH(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        // Stage 3: enemyType 29 alternating spiral ring.
        if (enemyType == 29 && enemylist[enemyIndex][7] == 1 && enemylist[enemyIndex][2] % 20 == 0) {
            if (enemylist[enemyIndex][9] == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            }
            int base = enemylist[enemyIndex][10];

            int moveType = 22;
            if (ex < 6356992) {
                moveType = 21;
            }

            int d3 = 7;
            int div;
            if (level == 0) {
                div = 8;
            } else if (level == 1) {
                div = 12;
            } else if (level == 2) {
                div = 18;
            } else {
                div = 24;
            }

            int ang = 0;
            if ((enemylist[enemyIndex][9] & 1) == 0) {
                ang = (360 / div >> 1);
            }

            while (ang < 360) {
                enemyBullets.cc(82, moveType, base + ang, d3 << 3, ex, ey, px, py);
                ang += 360 / div;
            }
            enemylist[enemyIndex][9] += 1;
        }

        // Stage 3: enemyType 30 staggered spiral ring burst.
        if (enemyType == 30 && enemylist[enemyIndex][7] == 1 && (enemylist[enemyIndex][2] & 0x3) == 0 && enemylist[enemyIndex][9] < 6) {
            if (enemylist[enemyIndex][9] == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            }
            int base = enemylist[enemyIndex][10];

            int moveType = 0;
            int d3 = 3 + enemylist[enemyIndex][9];

            int div;
            if (level == 0) {
                div = 8;
            } else if (level == 1) {
                div = 12;
            } else if (level == 2) {
                div = 18;
            } else {
                div = 24;
            }

            int ang = 0;
            if ((enemylist[enemyIndex][9] & 1) == 0) {
                ang = (360 / div >> 1) + (enemylist[enemyIndex][9] << 3);
            }
            while (ang < 360) {
                enemyBullets.cc(82, moveType, base + ang, d3 << 3, ex, ey, px, py);
                ang += 360 / div;
            }
            enemylist[enemyIndex][9] += 1;
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 4 fodder shooting patterns (enemyType 32/33/53).
    public void tickEnemyShootingStageI(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        // Stage 4: enemyType 32 ring + stop/aim shots.
        if (enemyType == 32 && enemylist[enemyIndex][7] == 1) {
            if (enemylist[enemyIndex][10] == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            }
            if ((enemylist[enemyIndex][2] & 1) == 0) {
                int div;
                if (level == 0) {
                    div = 8;
                } else if (level == 1) {
                    div = 9;
                } else if (level == 2) {
                    div = 10;
                } else {
                    div = 12;
                }

                int base = enemylist[enemyIndex][10] + ((360 / div) >> 1);
                for (int ang = 0; ang < 360; ang += 360 / div) {
                    enemyBullets.cc(82, 1, base + ang, 24, ex, ey, px, py);
                }
            }
            if (enemylist[enemyIndex][2] % 10 == 0) {
                int base = (BattleMath.arcTan2Deg(py - ey, px - ex) + 180) % 360;
                if (level == 0) {
                    enemyBullets.cc(86, 9, base + 30, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base - 30, 64, ex, ey, px, py);
                } else if (level == 1) {
                    enemyBullets.cc(86, 9, base + 40, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base - 40, 64, ex, ey, px, py);
                } else if (level == 2) {
                    enemyBullets.cc(86, 9, base + 50, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base + 25, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base - 25, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base - 50, 64, ex, ey, px, py);
                } else {
                    enemyBullets.cc(86, 9, base + 60, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base + 30, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base - 30, 64, ex, ey, px, py);
                    enemyBullets.cc(86, 9, base - 60, 64, ex, ey, px, py);
                }
            }
        }

        // Stage 4: enemyType 33 short burst acceleration shots.
        if (enemyType == 33 && enemylist[enemyIndex][7] == 1 && (enemylist[enemyIndex][2] & 1) == 0) {
            if ((enemylist[enemyIndex][9] & 7) == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            }

            int limit;
            if (level == 0) {
                limit = 1;
            } else if (level == 1) {
                limit = 3;
            } else if (level == 2) {
                limit = 5;
            } else {
                limit = 8;
            }

            if (enemylist[enemyIndex][9] <= limit) {
                enemyBullets.cc(90, 1, enemylist[enemyIndex][10], 24, ex, ey, px, py);
            }
            enemylist[enemyIndex][9] += 1;
        }

        // Stage 4: enemyType 53 dense accelerating ring.
        if (enemyType == 53 && enemylist[enemyIndex][7] == 1 && (enemylist[enemyIndex][2] & 7) == 0) {
            if (enemylist[enemyIndex][9] == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
            }

            int div;
            int mod;
            if (level == 0) {
                div = 30;
                mod = 5;
            } else if (level == 1) {
                div = 36;
                mod = 6;
            } else if (level == 2) {
                div = 40;
                mod = 8;
            } else {
                div = 72;
                mod = 12;
            }

            int baseSpeed = 4;
            int phase = 0;
            for (int ang = 0; ang < 360; ang += 360 / div) {
                int spd = baseSpeed + (enemylist[enemyIndex][9] << 2) + (phase << 2);
                enemyBullets.cc(81, 28, enemylist[enemyIndex][10] + ang, spd, ex, ey, px, py);

                if ((enemylist[enemyIndex][9] & 1) == 0) {
                    phase = (phase + 1) % mod;
                } else {
                    phase = (phase - 1 + mod) % mod;
                }
            }
            enemylist[enemyIndex][9] += 1;
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 4 fodder shooting patterns (enemyType 70).
    public void tickEnemyShootingStageJ(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        if (enemyType == 70 && (enemylist[enemyIndex][2] & 1) == 0 && enemylist[enemyIndex][2] > 5 && enemylist[enemyIndex][2] < 15) {
            enemyBullets.cc(81, 3, 0, 56, ex, ey, px, py);
            enemyBullets.cc(81, 3, 0, 64, ex, ey, px, py);
            enemyBullets.cc(81, 3, 0, 72, ex, ey, px, py);
            enemyBullets.cc(81, 3, 0, 80, ex, ey, px, py);
            enemyBullets.cc(81, 3, 0, 88, ex, ey, px, py);
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage EXTRA fodder enemy bullet patterns (enemyType 90-97).
    public void tickEnemyShootingStageK(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        if (enemyType == 90 && enemylist[enemyIndex][7] == 1) {
            if ((enemylist[enemyIndex][9] & 3) == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
                for (int a = 0; a < 360; a += 45) {
                    int base = enemylist[enemyIndex][10] + a;
                    for (int k = 0; k < 5; k++) {
                        enemyBullets.cc(319, 0, base, 40 + (k << 3), ex, ey, px, py);
                    }
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 91 && enemylist[enemyIndex][7] == 1) {
            if ((enemylist[enemyIndex][9] & 1) == 0) {
                enemylist[enemyIndex][10] = 450;
                for (int off = -80; off <= 80; off += 20) {
                    enemyBullets.cc(84, 0, enemylist[enemyIndex][10] + off, 46, ex, ey, px, py);
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 92 && enemylist[enemyIndex][7] == 1) {
            if ((enemylist[enemyIndex][9] & 1) == 0) {
                int d5 = enemylist[enemyIndex][9];
                enemylist[enemyIndex][10] = d5 << 4;
                for (int k = 0; k < 6; k++) {
                    int ang = ((d5 << 3) + (k * 90)) % 360;
                    int sx = ex + BattleMath.speedCos(100, ang);
                    int sy = ey + BattleMath.speedSin(100, ang);
                    enemyBullets.cc(82, 0, enemylist[enemyIndex][10] + (k * 60), 46, sx, sy, px, py);
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 93 && enemylist[enemyIndex][7] == 1) {
            if ((enemylist[enemyIndex][9] & 1) == 0) {
                int d5 = enemylist[enemyIndex][9];
                enemylist[enemyIndex][10] = 360 - ((d5 << 4) % 360);
                for (int k = 0; k < 6; k++) {
                    int ang = 360 - (((d5 << 3) + (k * 90)) % 360);
                    int sx = ex + BattleMath.speedCos(100, ang);
                    int sy = ey + BattleMath.speedSin(100, ang);
                    enemyBullets.cc(82, 0, enemylist[enemyIndex][10] + (k * 60), 46, sx, sy, px, py);
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 94 && ex > 0 && ex < 12713984) {
            if ((enemylist[enemyIndex][9] & 1) == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
                for (int off = -60; off <= 20; off += 60) {
                    enemyBullets.cc(81, 40, enemylist[enemyIndex][10] + off, 48, ex, ey, px, py);
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 95 && enemylist[enemyIndex][7] == 1) {
            if (enemylist[enemyIndex][9] == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
                int mt = 22;
                if (ex < 6356992) {
                    mt = 21;
                }
                int div = 24;
                int ang = 0;
                if ((enemylist[enemyIndex][9] & 1) == 0) {
                    ang = (360 / div) >> 1;
                }
                while (ang < 360) {
                    enemyBullets.cc(86, mt, enemylist[enemyIndex][10] + ang, 40, ex, ey, px, py);
                    ang += 360 / div;
                }
            } else if (enemylist[enemyIndex][9] % 10 == 0) {
                enemylist[enemyIndex][10] = BattleMath.arcTan2Deg(py - ey, px - ex);
                for (int off = -20; off <= 20; off += 20) {
                    int base = enemylist[enemyIndex][10] + off;
                    for (int k = 0; k < 3; k++) {
                        enemyBullets.cc(81, 0, base, 40 + (k << 2), ex, ey, px, py);
                    }
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 96 && enemylist[enemyIndex][7] == 1) {
            if ((enemylist[enemyIndex][9] & 1) == 0) {
                int shift = enemylist[enemyIndex][9] << 2;
                for (int a = 0; a < 360; a += 60) {
                    enemyBullets.cc(83, 43, a - shift, 0, ex, ey, px, py);
                    enemyBullets.cc(83, 44, a - shift, 0, ex, ey, px, py);
                    enemyBullets.cc(83, 43, a + shift + 180, 0, ex, ey, px, py);
                    enemyBullets.cc(83, 44, a + shift + 180, 0, ex, ey, px, py);
                }
            }
            enemylist[enemyIndex][9] += 1;
        }

        if (enemyType == 97 && ex > 0 && ex < 12713984 && ey > 524288 && ey < 15335424) {
            if (enemylist[enemyIndex][9] < 15) {
                for (int a = 0; a < 360; a += 45) {
                    for (int k = 4; k <= 9; k++) {
                        enemyBullets.cc(603, 0, a, k << 3, ex, ey, px, py);
                    }
                }
            }
            enemylist[enemyIndex][9] += 1;
        }
    }

    // Mirrors GameCanvas.stageEb (partial): stage 1 fodder shooting patterns (enemyType 31).
    public void tickEnemyShootingStageL(int enemyIndex, int[][] enemylist, int enemyType, int ex, int ey, int px, int py, int level) {
        if (enemyBullets == null) {
            return;
        }
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }

        // Stage 1: enemyType 31 accelerating aimed petals.
        if (enemyType == 31 && enemylist[enemyIndex][2] == 20) {
            int div;
            if (level == 0) {
                div = 3;
            } else if (level == 1) {
                div = 5;
            } else if (level == 2) {
                div = 6;
            } else {
                div = 8;
            }

            int ang = BattleMath.arcTan2Deg(py - ey, px - ex);
            for (int n8 = 0; n8 < 360; n8 += 360 / div) {
                for (int n9 = 8; n9 <= 11; n9++) {
                    enemyBullets.cc(82, 2, ang + n8 + 2, n9 << 3, ex, ey, px, py);
                    enemyBullets.cc(82, 2, ang + n8 - 2, n9 << 3, ex, ey, px, py);
                }
            }
        }
    }
}
