package touhou.battle;

import touhou.BulletSystem;
import touhou.CcTable;
import touhou.DropItemSystem;
import touhou.EnemyBulletSystem;
import touhou.EnemySystem;
import touhou.Fixed;
import touhou.Player;
import touhou.ScoreSystem;
import touhou.SoundEffectSystem;
import touhou.stage.BossController;

/**
 * Battle gameplay engine.
 *
 * Responsibilities:
 * - Advance battle-related simulation that is not purely visual:
 *   bullets, enemy bullets, player input, bomb flow, drop items, effects, collision,
 *   death/respawn, and continue revival.
 * - Emit side effects through {@link Host} (sound, screen flash, effect spawns, flags).
 *
 * Design notes:
 * - This class is intentionally stateless about scene flow; it is driven by GameCore/BattleScene.
 * - All positions are fixed-point (16.16) unless explicitly noted.
 */
public final class BattleEngine {
    public interface Host {
        // Stage effect list spawner (visual effects that live in stage effect arrays).
        void spawnStageEffect(int effectId, int xFixed, int yFixed);

        // Effect entry spawner (lightweight transient effects).
        void spawnEffectEntry(int effectId, int xFixed, int yFixed);

        // Mirrors DoJa reset flag used for various side effects (shake/flash/etc.).
        void setResetFlag(int value);

        // Full-screen flash control (white/black).
        void setFlash(int value);

        void setTargetFps(int fps);

        // Enemy death notification (lets the stage runtime award drops/advance script).
        void onEnemyKilled(int enemyIndex);

        // Legacy sound routing entry. loop=true is used for script-driven BGM requests.
        void playSound(int soundId, boolean loop);

        // Per-frame bomb callback while bomb is active.
        void onBombTick(int d0, int bombCnt);
    }

    public static final class DeathTickResult {
        public int newDeadcnt;
        public int barrierTicks;
        public boolean deathProcessed;
        public boolean noPlayersLeft;

        public DeathTickResult(int newDeadcnt, int barrierTicks) {
            this.newDeadcnt = newDeadcnt;
            this.barrierTicks = barrierTicks;
            this.deathProcessed = false;
            this.noPlayersLeft = false;
        }
    }

    public static final class BombActivateResult {
        public int newBombCnt;
        public int newCa;
        public int newDeadcnt;
        public boolean bombActivated;

        public BombActivateResult(int newBombCnt, int newCa, int newDeadcnt) {
            this.newBombCnt = newBombCnt;
            this.newCa = newCa;
            this.newDeadcnt = newDeadcnt;
            this.bombActivated = false;
        }
    }

    public static final class FireInputResult {
        public int newTacnt;
        public boolean newShooting;

        public FireInputResult(int newTacnt, boolean newShooting) {
            this.newTacnt = newTacnt;
            this.newShooting = newShooting;
        }
    }

    public static final class ContinueReviveResult {
        public int newBombCnt;
        public int newDeadcnt;
        public int newBariacnt;
        public int newTacnt;
        public boolean newShooting;

        public ContinueReviveResult(int newBombCnt, int newDeadcnt, int newBariacnt, int newTacnt, boolean newShooting) {
            this.newBombCnt = newBombCnt;
            this.newDeadcnt = newDeadcnt;
            this.newBariacnt = newBariacnt;
            this.newTacnt = newTacnt;
            this.newShooting = newShooting;
        }
    }

    private final BulletSystem bullets;
    private final EnemyBulletSystem enemyBullets;
    private final ScoreSystem scoreSystem;
    private final Host host;

    private boolean deathPending;
    private int deathXFixed;
    private int deathYFixed;

    // Mirrors DoJa : s_deadflg.
    private boolean playerDeadFlag;

    private final DeathTickResult tickResult;
    private final BombActivateResult bombActivateResult;
    private final FireInputResult fireInputResult;

    public BattleEngine(BulletSystem bullets, EnemyBulletSystem enemyBullets, ScoreSystem scoreSystem, Host host) {
        this.bullets = bullets;
        this.enemyBullets = enemyBullets;
        this.scoreSystem = scoreSystem;
        this.host = host;
        this.tickResult = new DeathTickResult(0, 0);
        this.bombActivateResult = new BombActivateResult(0, 0, 0);
        this.fireInputResult = new FireInputResult(0, false);
    }

    public void reset() {
        deathPending = false;
        deathXFixed = 0;
        deathYFixed = 0;
        playerDeadFlag = false;
    }

    public boolean isPlayerDeadFlag() {
        return playerDeadFlag;
    }

    // Mirrors DoJa : yc() used by continue.
    public void scatterCollectingItemsForContinue(DropItemSystem dropItems) {
        if (dropItems == null) {
            return;
        }
        dropItems.cancelCollectingAndScatter();
    }

    // Mirrors DoJa : m() continue YES revives without resetting stage progress.
    public ContinueReviveResult continueReviveInPlace(DropItemSystem dropItems, BattleHudModel hud, Player player, int playX, int playY, int playW, int playH,
            int playerCount) {
        // Continue revival resets player/bullets/enemy bullets but keeps stage progression.
        if (hud != null) {
            hud.setPlayer(playerCount);
            hud.setBomb(3);
            if (scoreSystem != null) {
                scoreSystem.attachHud(hud);
            }
        }

        if (dropItems != null) {
            scatterCollectingItemsForContinue(dropItems);
        }

        reset();

        if (bullets != null) {
            bullets.clear();
        }
        if (enemyBullets != null) {
            enemyBullets.clear();
        }

        if (player != null) {
            int px = (playX + (playW / 2)) << 16;
            int py = (playY + playH - 28) << 16;
            player.setSlow(false);
            player.setPositionFixed(px, py);
            player.setVisible(true);
        }

        return new ContinueReviveResult(0, 0, 35, 0, true);
    }

    // Mirrors GameCanvas.update: set enemy level/aim target and update bullet system.
    public void tickBullets(int playX, int playY, int playW, int playH, int enemyLevel, boolean hasAimTarget, int aimXFixed, int aimYFixed) {
        // Player bullets are simulated by BulletSystem (also holds player lasers).
        if (bullets == null) {
            return;
        }

        bullets.setEnemyLevel(enemyLevel);
        if (hasAimTarget) {
            bullets.setEnemyAimTarget(aimXFixed, aimYFixed);
        }
        bullets.update(playX, playY, playW, playH);
    }

    // Mirrors GameCanvas.update: player tick (movement + options + shooting).
    public void tickPlayer(Player player, int keys, int playX, int playY, int playW, int playH, int power, boolean bossBarrierActive,
            boolean shooting, int sBossf, int bombCnt, int currentChara, EnemySystem enemies, int fi, int sDeadcnt) {
        // Handles player movement, option devices, and normal shot emission.
        if (player == null) {
            return;
        }

        if (sDeadcnt != 0 || playerDeadFlag) {
            return;
        }

        player.setBombActive(bombCnt > 0);
        player.update(keys, playX, playY, playW, playH);
        player.updateFirepowerDevices(bullets, enemies, power, bossBarrierActive);

        if (shooting && sBossf != 1 && !(bombCnt > 0 && currentChara == 1)) {
            player.waTick(bullets, fi, power);
        }
    }

    // Mirrors GameCanvas.update: slow toggle input.
    public void tickSlowToggle(Player player, boolean togglePressed) {
        // Slow mode affects hitbox display and movement precision.
        if (!togglePressed) {
            return;
        }
        if (player == null) {
            return;
        }
        player.toggleSlow();
    }

    // Mirrors GameCanvas.update: FIRE_PRESSED handling for dialogue skip and shooting toggle.
    public FireInputResult tickFireInput(boolean firePressed, boolean fireActionPressed, boolean dialogueActive, int mTacnt, boolean shooting) {
        // firePressed: logical pressed state; fireActionPressed: edge-trigger for dialogue skip.
        fireInputResult.newTacnt = mTacnt;
        fireInputResult.newShooting = shooting;

        if (dialogueActive && fireActionPressed) {
            fireInputResult.newTacnt = 80;
        }

        if (firePressed) {
            if (dialogueActive || fireInputResult.newTacnt != 0) {
                fireInputResult.newTacnt = 80;
            } else {
                fireInputResult.newShooting = !fireInputResult.newShooting;
            }
        }

        return fireInputResult;
    }

    // Mirrors GameCanvas.update: bomb countdown and per-frame callback.
    public int tickBomb(int bombCnt) {
        // Bomb countdown is driven by GameCore; this method decrements and emits a per-frame callback.
        if (bombCnt > 0) {
            bombCnt--;
            if (host != null) {
                host.onBombTick(60 - bombCnt, bombCnt);
            }
        }
        return bombCnt;
    }

    // Mirrors GameCanvas.update: bomb activation (GAME_C_PRESSED) conditions and side effects.
    public BombActivateResult tickBombActivation(boolean bombPressed, boolean dialogueActive, boolean hudIsBattleHudModel, int bombCnt, int sBossf,
            int sBariacnt, int fi, int mGcnt, int mTacnt, int ca, int sDeadcnt) {
        // Enforces DoJa bomb activation rules and returns the updated counters.
        bombActivateResult.newBombCnt = bombCnt;
        bombActivateResult.newCa = ca;
        bombActivateResult.newDeadcnt = sDeadcnt;
        bombActivateResult.bombActivated = false;

        if (!bombPressed) {
            return bombActivateResult;
        }
        if (dialogueActive) {
            return bombActivateResult;
        }
        if (!hudIsBattleHudModel) {
            return bombActivateResult;
        }
        if (scoreSystem.getBomb() <= 0) {
            return bombActivateResult;
        }
        if (bombCnt != 0) {
            return bombActivateResult;
        }
        if (sBossf == 1) {
            return bombActivateResult;
        }
        if (sBariacnt >= 20) {
            return bombActivateResult;
        }
        if (fi <= 10) {
            return bombActivateResult;
        }
        if (mGcnt >= 5000) {
            return bombActivateResult;
        }
        if (mTacnt != 0) {
            return bombActivateResult;
        }

        scoreSystem.post(ScoreSystem.EVT_BOMB_USED, 0, 0, 0);
        bombActivateResult.newCa = ca + 1;
        bombActivateResult.newDeadcnt = 0;
        bombActivateResult.newBombCnt = 60;
        bombActivateResult.bombActivated = true;
        return bombActivateResult;
    }

    // Mirrors GameCanvas.stageLc: effect list update and chained effect spawns.
    public void tickStageEffects(int[][] effectlist, int[][] effectTable) {
        // Updates effect entries and spawns chained stage effects (id-dependent patterns).
        if (effectlist == null || effectTable == null) {
            return;
        }

        for (int i = 0; i < 64; i++) {
            if (effectlist[i][0] == 0) {
                continue;
            }

            effectlist[i][1] += 1;
            effectlist[i][4] += effectlist[i][6];
            effectlist[i][5] += effectlist[i][7];

            int id = effectlist[i][3];
            if (id == 21) {
                if (effectlist[i][1] >= effectlist[i][8]) {
                    effectlist[i][0] = 0;
                }
                continue;
            }

            if (id == 16) {
                effectlist[i][7] = (effectlist[i][2] - 1) << 1;
            } else if (id == 17 || id == 18) {
                if (effectlist[i][2] == 0) {
                    effectlist[i][7] = 0;
                } else {
                    effectlist[i][7] = (effectlist[i][2] - 2) << 1;
                }
            }

            int[] row = null;
            if (id >= 0 && id < effectTable.length) {
                row = effectTable[id];
            }
            if (row == null || row.length == 0) {
                effectlist[i][0] = 0;
                continue;
            }

            int frame = effectlist[i][2];
            int p = frame << 1;
            if (p < 0 || p >= row.length) {
                effectlist[i][0] = 0;
                continue;
            }

            int dur = row[p];
            if (dur < effectlist[i][1]) {
                effectlist[i][1] = 0;
                effectlist[i][2] = frame + 1;
                frame = effectlist[i][2];
                p = frame << 1;
                if (p < 0 || p >= row.length) {
                    effectlist[i][0] = 0;
                    continue;
                }
                dur = row[p];
            }

            if (id >= 6 && id <= 15) {
                if (host != null) {
                    host.spawnStageEffect(3, effectlist[i][4], effectlist[i][5]);
                    host.spawnStageEffect(4, effectlist[i][4], effectlist[i][5]);
                    host.spawnStageEffect(5, effectlist[i][4], effectlist[i][5]);
                }
                if (effectlist[i][2] != 0) {
                    if (host != null) {
                        host.spawnStageEffect(3, effectlist[i][4], effectlist[i][5]);
                        host.spawnStageEffect(3, effectlist[i][4], effectlist[i][5]);
                        host.spawnStageEffect(4, effectlist[i][4], effectlist[i][5]);
                        host.spawnStageEffect(4, effectlist[i][4], effectlist[i][5]);
                        host.spawnStageEffect(5, effectlist[i][4], effectlist[i][5]);
                        host.spawnStageEffect(5, effectlist[i][4], effectlist[i][5]);
                        host.setResetFlag(2);
                        host.setTargetFps(16);
                    }
                }
                if (effectlist[i][2] == 2) {
                    if (host != null) {
                        host.setFlash(1);
                    }
                }
            }

            if (dur == 255) {
                if (effectlist[i][8] > 0) {
                    effectlist[i][2] = 0;
                    effectlist[i][1] = 0;
                    effectlist[i][8] -= 1;
                } else {
                    effectlist[i][0] = 0;
                }
            }
        }
    }

    // Mirrors GameCanvas.update: drop item tick based on player position and HUD power.
    public void tickDropItems(DropItemSystem dropItems, BattleHudData hud, Player player, int playX, int playY, int playW, int playH, int stage,
            boolean disableItemCollectLimitCheat, int currentChara, int sDeadcnt) {
        // Drives item movement/collection and crisis-dependent behavior.
        if (dropItems == null) {
            return;
        }
        if (sDeadcnt != 0) {
            return;
        }
        if (!(hud instanceof BattleHudModel)) {
            return;
        }

        BattleHudModel m = (BattleHudModel) hud;
        int px;
        int py;
        boolean slow = false;
        if (player != null) {
            px = player.getXFixed();
            py = player.getYFixed();
            slow = player.isSlow();
        } else {
            px = (playX + (playW / 2)) << 16;
            py = (playY + playH - 28) << 16;
        }
        int crisis = (scoreSystem != null) ? scoreSystem.getCrisis() : 0;
        dropItems.update(px, py, slow, stage, m.getPower(), playerDeadFlag, disableItemCollectLimitCheat, currentChara, crisis);
    }

    // Mirrors DoJa : (s_deadflg && s_bariacnt == 27) respawn + invincibility blink.
    public void tickPlayerDeathAndInvincibility(Player player, int sBariacnt, int playX, int playY, int playW, int playH) {
        // Handles respawn position reset and visibility blink during invincibility frames.
        if (player == null) {
            return;
        }

        if (playerDeadFlag && sBariacnt == 27) {
            int px = (playX + (playW / 2)) << 16;
            int py = (playY + playH - 28) << 16;
            player.setPositionFixed(px, py);
            playerDeadFlag = false;
        }

        if (playerDeadFlag) {
            player.setVisible(false);
            return;
        }

        if (sBariacnt > 0) {
            player.setVisible((sBariacnt % 3) < 2);
        } else {
            player.setVisible(true);
        }
    }

    // Mirrors GameCanvas.stageHd: handle player bullet collision against an enemy.
    public void tickEnemyPlayerBulletCollision(int enemyIndex, int[][] enemylist, int bombCnt, int sBbaria, int[] bossWrk, boolean disableBossBombShieldCheat,
            CcTable cc, BossController bossController, Player player) {
        // Detects overlap between player bullets and a target enemy hitbox and applies damage.
        if (enemylist == null || enemyIndex < 0 || enemyIndex >= enemylist.length) {
            return;
        }
        if (enemylist[enemyIndex][0] == 0) {
            return;
        }
        if (enemylist[enemyIndex][16] == 0) {
            return;
        }
        if (enemylist[enemyIndex][21] == 8) {
            return;
        }
        if (sBbaria != 0) {
            return;
        }
        if (cc == null) {
            return;
        }
        if (bullets == null) {
            return;
        }

        int enemyKind = enemylist[enemyIndex][1];
        if (!disableBossBombShieldCheat && (enemyKind == 17 || enemyKind == 19) && bossWrk != null && bossWrk.length > 2 && bossWrk[2] != 0 && bombCnt > 0) {
            // DoJa : kind 17/19 uses m_bossWrk[2] + s_sccnt for special bomb/spell flicker.
            // Use the same condition to gate bullet damage (boss invincibility during bomb).
            // Also mark as untargetable for homing devices during invincibility.
            enemylist[enemyIndex][16] = 0;
            return;
        }

        int enemyMt = enemylist[enemyIndex][11];

        int ex = enemylist[enemyIndex][5];
        int ey = enemylist[enemyIndex][6];
        int el = ex - enemylist[enemyIndex][17];
        int et = ey - enemylist[enemyIndex][19];
        int ew = enemylist[enemyIndex][17] + enemylist[enemyIndex][18];
        int eh = enemylist[enemyIndex][19] + enemylist[enemyIndex][20];

        int max = bullets.getMax();
        for (int i = 0; i < max; i++) {
            if (enemylist[enemyIndex][0] == 0) {
                return;
            }
            if (!bullets.isActive(i) || !bullets.isFromPlayer(i)) {
                continue;
            }

            int bid = bullets.getBulletId(i);
            if (!cc.hasSpriteMeta(bid)) {
                continue;
            }

            int imgIndex = cc.getImgIndex(bid);
            int w = cc.getW(bid);
            int h = cc.getH(bid);
            int ax = cc.getAx(bid);
            int ay = cc.getAy(bid);

            int scale = (imgIndex == 6) ? 2 : 1;
            if (scale != 1) {
                w *= scale;
                h *= scale;
                ax *= scale;
                ay *= scale;
            }
            if (bid == 31 || bid == 60 || bid == 99) {
                w = 8;
                h = 16;
                ax = 4;
                ay = 4;
            } else if (bid == 32) {
                w = 8;
                h = 8;
                ax = 4;
                ay = 4;
            } else if (bid == 48) {
                w = 20;
                h = 20;
                ax = 10;
                ay = 10;
            } else if (bid == 97) {
                w = 16;
                h = 16;
                ax = 8;
                ay = 8;
            } else if (bid >= 733 && bid <= 740) {
                w = 32;
                h = 32;
                ax = 16;
                ay = 16;
            }
            if (bid >= 741 && bid <= 749) {
                w = 16;
                h = 16;
                ax = 8;
                ay = 8;
            }
            if (bid >= 750 && bid <= 765) {
                w = 24;
                h = 24;
                ax = 12;
                ay = 12;
            }

            // DoJa : Reimu bomb bullets (moveType 4/5, bulletId 61..80) only become hittable at stageFlag==14,
            // and the hitbox expands to 80px on each side (160x160).
            int mt = bullets.getMoveType(i);
            if ((mt == 4 || mt == 5) && bid >= 61 && bid <= 80) {
                if (bullets.getStageFlag(i) != 14) {
                    continue;
                }
                w = 160;
                h = 160;
                ax = 80;
                ay = 80;
            }

            int bx = bullets.getXFixed(i);
            int by = bullets.getYFixed(i);

            // DoJa : Marisa bomb laser (moveType 3, bulletId 105..110) uses a tall beam hitbox.
            // Early frames (105..107) have hitbox=0; 108 is narrow; 109/110 are wide.
            int bl = bx - (ax << 16);
            int bt = by - (ay << 16);
            int bw = w << 16;
            int bh = h << 16;
            if (mt == 3 && bid >= 105 && bid <= 110) {
                if (bid <= 107) {
                    continue;
                }
                int halfW = (bid == 108) ? Fixed.fromInt(23) : Fixed.fromInt(85);
                int topY = Fixed.fromInt(8);
                bl = bx - halfW;
                bt = topY;
                bw = halfW + halfW;
                bh = by - topY;
                if (bw <= 0 || bh <= 0) {
                    continue;
                }
            }

            if (!rectOverlapFixed(el, et, ew, eh, bl, bt, bw, bh)) {
                continue;
            }

            // mt already read above for special hitbox cases.

            // Marisa bomb laser hittable frames are gated above based on bulletId (105..110).
            if (mt != 2 && mt != 3 && mt != 6 && mt != 7) {
                if (host != null) {
                    for (int j = 0; j < 3; j++) {
                        host.spawnStageEffect(21, bx, by);
                    }
                }
            }

            if (scoreSystem != null) {
                if (mt == 2 || mt == 3 || mt == 6 || mt == 7) {
                    scoreSystem.post(ScoreSystem.EVT_ADD_SCORE, 30, 0, 0);
                } else {
                    scoreSystem.post(ScoreSystem.EVT_ADD_SCORE, 50, 0, 0);
                }
            }

            if (host != null) {
                host.playSound(SoundEffectSystem.SE_SHOT, false);
            }

            int dmg = bullets.getPower(i);
            if (dmg <= 0) {
                dmg = 1;
            }

            if (enemyMt == 100 || enemyMt == 101 || enemyMt == 102 || enemyMt == 103) {
                if (bossController != null) {
                    if (bossController.applyBossDamage(dmg) <= 0) {
                        if (host != null) {
                            host.onEnemyKilled(enemyIndex);
                        }
                        return;
                    }
                }
                if (mt == 8 && player != null) {
                    player.setAliceTargetIfUnset(enemyIndex);
                }
            } else {
                enemylist[enemyIndex][4] -= dmg;
                if (enemylist[enemyIndex][4] <= 0) {
                    if (host != null) {
                        host.onEnemyKilled(enemyIndex);
                    }
                    return;
                }
                if (mt == 8 && player != null) {
                    player.setAliceTargetIfUnset(enemyIndex);
                }
            }

            if (mt != 2 && mt != 3 && mt != 4 && mt != 5 && mt != 6 && mt != 7) {
                bullets.deactivate(i);
            }
        }
    }

    // Mirrors GameCanvas.update: enemy bullet graze/hit collision + death queue.
    public int tickPlayerCollision(Player player, boolean allowHit, int[][] enemylist, int sBariacnt, int sDeadcnt, int bombCnt) {
        // Applies graze scoring and hit detection (enemy bullets + enemy bodies).
        if (player == null) {
            return sDeadcnt;
        }
        if (sBariacnt != 0 || sDeadcnt != 0 || bombCnt != 0) {
            return sDeadcnt;
        }

        int playerRadiusFixed = player.getHitboxRadiusFixed();

        if (enemyBullets != null) {
            enemyBullets.applyPlayerCollision(player.getXFixed(), player.getYFixed(), playerRadiusFixed, true, allowHit);
            int grazed = enemyBullets.getGrazeCount();
            if (grazed > 0) {
                int gx = player.getXFixed();
                int gy = player.getYFixed();
                for (int i = 0; i < grazed; i++) {
                    if (host != null) {
                        host.spawnEffectEntry(2, gx, gy);
                        host.playSound(SoundEffectSystem.SE_GRAZE, false);
                    }
                }
                if (scoreSystem != null) {
                    scoreSystem.post(ScoreSystem.EVT_GRAZE, grazed, 0, 0);
                }
            }
        }

        return checkAndQueueDeath(player, allowHit, enemylist, sDeadcnt);
    }

    public int checkAndQueueDeath(Player player, boolean allowHit, int[][] enemylist, int sDeadcnt) {
        // Converts a hit into a queued death event to be processed in tickDeathCountdown.
        if (player == null) {
            return sDeadcnt;
        }
        if (!allowHit) {
            return sDeadcnt;
        }
        if (sDeadcnt != 0) {
            return sDeadcnt;
        }

        if (enemyBullets != null && enemyBullets.isPlayerHit()) {
            queueDeath(player.getXFixed(), player.getYFixed());
            enemyBullets.clearPlayerHit();
            return 3;
        }

        // Use character-specific player hitbox.
        if (enemylist != null && isPlayerCollidingWithEnemy(enemylist, player.getXFixed(), player.getYFixed(), player.getHitboxRadiusFixed())) {
            queueDeath(player.getXFixed(), player.getYFixed());
            return 3;
        }

        return sDeadcnt;
    }

    private void queueDeath(int xFixed, int yFixed) {
        // Store position for death effects and item scattering.
        deathPending = true;
        deathXFixed = xFixed;
        deathYFixed = yFixed;
    }

    public DeathTickResult tickDeathCountdown(int sDeadcnt, int stage, DropItemSystem dropItems, BattleHudModel hud, Player player, int playX, int playY,
            int playW, int playH, boolean spellPractice, boolean bossActive) {
        // Processes a queued death on the last tick and returns barrier/invincibility duration.
        tickResult.newDeadcnt = sDeadcnt;
        tickResult.barrierTicks = 0;
        tickResult.deathProcessed = false;
        tickResult.noPlayersLeft = false;

        if (sDeadcnt <= 0) {
            return tickResult;
        }

        if (sDeadcnt == 1) {
            if (deathPending) {
                deathPending = false;

                if (host != null) {
                    host.playSound(SoundEffectSystem.SE_CRASH, false);
                    host.setFlash(1);
                    host.spawnStageEffect(1, deathXFixed, deathYFixed);
                }

                if (dropItems != null) {
                    dropItems.cancelCollectingAndScatter();
                }

                if (hud != null) {
                    int p = hud.getPower() - 5;
                    if (p < 0) {
                        p = 0;
                    }
                    hud.setPower(p);
                }

                if (player != null) {
                    player.clearOptionsAndTargets();
                    player.setVisible(false);
                }

                if (bullets != null) {
                    bullets.clearPlayerLasers();
                }

                if (!spellPractice) {
                    int dropType = DropItemSystem.TYPE_P;
                    if (scoreSystem != null && scoreSystem.getPlayer() == 1) {
                        dropType = DropItemSystem.TYPE_FULL_POWER;
                        if (hud != null) {
                            hud.setPower(0);
                        }
                    }

                    int n2 = -30;
                    if (deathXFixed <= 4194304) {
                        n2 += 20;
                    } else if (deathXFixed >= 8519680) {
                        n2 -= 20;
                    }
                    int power = (hud != null) ? hud.getPower() : 0;
                    for (int i = 0; i < 4; i++) {
                        if (dropItems != null) {
                            dropItems.spawn(dropType, 1, n2 + i * 20, deathXFixed, deathYFixed, stage, power, bossActive);
                        }
                    }
                }

                playerDeadFlag = true;

                if (bullets != null) {
                    bullets.convertEnemyBulletsToDeathVanish();
                }
                if (enemyBullets != null) {
                    enemyBullets.clear();
                }

                if (scoreSystem != null) {
                    scoreSystem.post(ScoreSystem.EVT_PLAYER_MISS, 0, stage, 0);
                }

                tickResult.barrierTicks = 35;
                tickResult.deathProcessed = true;
                tickResult.noPlayersLeft = (scoreSystem != null && scoreSystem.getPlayer() == 0);
            }

            tickResult.newDeadcnt = 0;
            return tickResult;
        }

        tickResult.newDeadcnt = sDeadcnt - 1;
        return tickResult;
    }

    private static boolean isPlayerCollidingWithEnemy(int[][] enemylist, int playerXFixed, int playerYFixed, int playerRadiusFixed) {
        // AABB overlap test between the player hitbox and enemy bounding boxes.
        int playerSize = (playerRadiusFixed << 1) + Fixed.fromInt(1);
        int playerLeft = playerXFixed - playerRadiusFixed;
        int playerTop = playerYFixed - playerRadiusFixed;

        for (int i = 0; i < enemylist.length; i++) {
            if (enemylist[i][0] == 0) {
                continue;
            }

            int ex = enemylist[i][5];
            int ey = enemylist[i][6];
            int el = ex - enemylist[i][17];
            int et = ey - enemylist[i][19];
            int ew = enemylist[i][17] + enemylist[i][18];
            int eh = enemylist[i][19] + enemylist[i][20];

            if (rectOverlapFixed(el, et, ew, eh, playerLeft, playerTop, playerSize, playerSize)) {
                return true;
            }
        }

        return false;
    }

    private static boolean rectOverlapFixed(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        // Fixed-point AABB overlap.
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
}
