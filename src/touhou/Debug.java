package touhou;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import touhou.ui.UiDraw;

public final class Debug {
    // FPS overlay shown at the bottom-left corner.
    private static int fpsValue;
    private static int fpsFrameCount;
    private static long fpsLastSampleMs;

    private static int perfBulletUpdateMs;
    private static int perfPlayerCollisionMs;
    private static int perfBulletRenderMs;
    private static int perfBlindOverlayMs;

    private static int perfSpawnRate;
    private static int perfKillRate;
    private static int perfAllocFailRate;

    private static int perfAlphaImgReqRate;
    private static int perfAlphaImgHitRate;
    private static int perfAlphaImgCreateRate;
    private static int perfAlphaRegReqRate;
    private static int perfAlphaRegHitRate;
    private static int perfAlphaRegCreateRate;
    private static int perfAlphaKeyReqRate;
    private static int perfAlphaKeyHitRate;
    private static int perfAlphaKeyCreateRate;

    // Frame spike detector: captures timing breakdown when a frame exceeds threshold.
    private static final int SPIKE_THRESHOLD_MS = 100;
    private static int spikeUpdateMs;
    private static int spikeRenderMs;
    private static int spikeCutInMs;
    private static int spikeAudioMs;
    private static int spikeTotalMs;
    private static long spikeDisplayUntil;
    private static int spikeCount;

    // Update-internal breakdown: pending values written every frame.
    private static int pendingWldGbMs;
    private static int pendingWldMaMs;
    private static int pendingWldResetMs;
    private static int pendingWldOtherMs;
    private static int pendingBattleSceneMs;
    // Latched display values (copied on spike).
    private static int spikeWldGbMs;
    private static int spikeWldMaMs;
    private static int spikeWldResetMs;
    private static int spikeWldOtherMs;
    private static int spikeBattleSceneMs;

    private static int perfSpawnAccum;
    private static int perfKillAccum;
    private static int perfAllocFailAccum;

    private static int perfAlphaImgReqAccum;
    private static int perfAlphaImgHitAccum;
    private static int perfAlphaImgCreateAccum;
    private static int perfAlphaRegReqAccum;
    private static int perfAlphaRegHitAccum;
    private static int perfAlphaRegCreateAccum;
    private static int perfAlphaKeyReqAccum;
    private static int perfAlphaKeyHitAccum;
    private static int perfAlphaKeyCreateAccum;
    private static long perfRateLastSampleMs;

    private static final String[] wrapLinesBuf = new String[48];

    private Debug() {
    }

    public static void perfBeginFrame() {
        perfBulletUpdateMs = 0;
        perfPlayerCollisionMs = 0;
        perfBulletRenderMs = 0;
        perfBlindOverlayMs = 0;
    }

    // Record a frame spike with timing breakdown; latch pending tick values.
    public static void recordFrameSpike(int totalMs, int updateMs, int renderMs, int cutInMs, int audioMs) {
        if (totalMs < SPIKE_THRESHOLD_MS) {
            return;
        }
        spikeTotalMs = totalMs;
        spikeUpdateMs = updateMs;
        spikeRenderMs = renderMs;
        if (cutInMs > 0) {
            spikeCutInMs = cutInMs;
        }
        spikeAudioMs = audioMs;
        // Latch pending tick breakdown.
        spikeWldGbMs = pendingWldGbMs;
        spikeWldMaMs = pendingWldMaMs;
        spikeWldResetMs = pendingWldResetMs;
        spikeWldOtherMs = pendingWldOtherMs;
        spikeBattleSceneMs = pendingBattleSceneMs;
        spikeCount++;
        spikeDisplayUntil = nowMs() + 5000;
    }

    public static void perfSetCutInRenderMs(int ms) {
        spikeCutInMs = ms;
    }

    // Record worldAndStage-internal breakdown (pending, latched on spike).
    public static void recordWorldBreakdown(int gbMs, int maMs, int resetMs, int otherMs) {
        pendingWldGbMs = gbMs;
        pendingWldMaMs = maMs;
        pendingWldResetMs = resetMs;
        pendingWldOtherMs = otherMs;
    }

    // Record total battleScene.update() time (pending, latched on spike).
    public static void recordBattleSceneMs(int ms) {
        pendingBattleSceneMs = ms;
    }

    public static void perfSetBulletUpdateMs(int ms) {
        perfBulletUpdateMs = ms;
    }

    public static void perfSetPlayerCollisionMs(int ms) {
        perfPlayerCollisionMs = ms;
    }

    public static void perfSetBulletRenderMs(int ms) {
        perfBulletRenderMs = ms;
    }

    public static void perfSetBlindOverlayMs(int ms) {
        perfBlindOverlayMs = ms;
    }

    public static void renderBattleDebugText(Graphics g,
            boolean fpsEnabled, boolean perfEnabled, boolean resourceEnabled,
            String resourceError,
            int playX, int playY, int playW,
            BulletSystem bullets, int[][] enemylist, int[][] effectlist,
            CcTable cc, int[][] effectTable, ImageBank imgs,
            String debugLine1, String debugLine2, String debugLine3, String debugLine4, String debugLine5) {
        if (g == null) {
            return;
        }

        renderFpsOverlay(g, fpsEnabled);

        if (!perfEnabled && !resourceEnabled) {
            return;
        }

        if (resourceEnabled && resourceError != null) {
            int hudTextX = playX + playW + 2;
            int y = 2 + UiDraw.fontBaseline(null);
            UiDraw.drawStringPlain(g, null, resourceError, hudTextX, y, 0, 0xFFFFFF);
            return;
        }

        int hudTextX = playX + 2;
        int c = 0x00FFFF;
        int yBase = 16;
        if (resourceEnabled) {
            int active = (bullets != null) ? bullets.activeCount() : 0;
            drawDebugString(g, "active=" + active, hudTextX, 2, c);
        }
        if (perfEnabled) {
            yBase = 16 + renderPerfOverlay(g, playX + 2, 16, playW - 4, bullets, imgs);
        }

        if (!resourceEnabled) {
            return;
        }

        if (debugLine1 != null) {
            drawDebugString(g, debugLine1, hudTextX, yBase, c);
        }
        if (debugLine2 != null) {
            drawDebugString(g, debugLine2, hudTextX, yBase + 14, c);
        }
        if (debugLine3 != null) {
            drawDebugString(g, debugLine3, hudTextX, yBase + 28, c);
        }
        if (debugLine4 != null) {
            drawDebugString(g, debugLine4, hudTextX, yBase + 42, c);
        }
        if (debugLine5 != null) {
            drawDebugString(g, debugLine5, hudTextX, yBase + 56, c);
        }

        int y = yBase + 70;
        String ghostSummary = buildGhostSummaryLine(bullets, enemylist, effectlist, cc, effectTable);
        if (ghostSummary != null) {
            drawDebugString(g, ghostSummary, hudTextX, y, c);
            y += 14;
        }
        String ghostFirst = buildGhostFirstLine(bullets, enemylist, effectlist, cc, effectTable);
        if (ghostFirst != null) {
            drawDebugString(g, ghostFirst, hudTextX, y, c);
            y += 14;
        }
        String ghostSecond = buildGhostSecondLine(bullets, enemylist, effectlist, cc, effectTable);
        if (ghostSecond != null) {
            drawDebugString(g, ghostSecond, hudTextX, y, c);
        }
    }

    private static int renderPerfOverlay(Graphics g, int x, int y, int maxW, BulletSystem bullets, ImageBank imgs) {
        if (g == null) {
            return 0;
        }

        int spawn = 0;
        int kill = 0;
        int allocFail = 0;
        if (bullets != null) {
            spawn = bullets.consumePerfSpawnCount();
            kill = bullets.consumePerfKillCount();
            allocFail = bullets.consumePerfAllocFailCount();
        }

        int aImgReq = 0;
        int aImgHit = 0;
        int aImgCreate = 0;
        int aRegReq = 0;
        int aRegHit = 0;
        int aRegCreate = 0;
        int aKeyReq = 0;
        int aKeyHit = 0;
        int aKeyCreate = 0;
        if (imgs != null) {
            aImgReq = imgs.consumePerfAlphaImageReq();
            aImgHit = imgs.consumePerfAlphaImageHit();
            aImgCreate = imgs.consumePerfAlphaImageCreate();
            aRegReq = imgs.consumePerfAlphaRegionReq();
            aRegHit = imgs.consumePerfAlphaRegionHit();
            aRegCreate = imgs.consumePerfAlphaRegionCreate();
            aKeyReq = imgs.consumePerfAlphaKeyReq();
            aKeyHit = imgs.consumePerfAlphaKeyHit();
            aKeyCreate = imgs.consumePerfAlphaKeyCreate();
        }

        perfSpawnAccum += spawn;
        perfKillAccum += kill;
        perfAllocFailAccum += allocFail;

        perfAlphaImgReqAccum += aImgReq;
        perfAlphaImgHitAccum += aImgHit;
        perfAlphaImgCreateAccum += aImgCreate;
        perfAlphaRegReqAccum += aRegReq;
        perfAlphaRegHitAccum += aRegHit;
        perfAlphaRegCreateAccum += aRegCreate;
        perfAlphaKeyReqAccum += aKeyReq;
        perfAlphaKeyHitAccum += aKeyHit;
        perfAlphaKeyCreateAccum += aKeyCreate;
        long now = nowMs();
        if (now != 0) {
            if (perfRateLastSampleMs == 0) {
                perfRateLastSampleMs = now;
            }
            long dt = now - perfRateLastSampleMs;
            if (dt >= 1000) {
                perfSpawnRate = (int) ((perfSpawnAccum * 1000L) / dt);
                perfKillRate = (int) ((perfKillAccum * 1000L) / dt);
                perfAllocFailRate = (int) ((perfAllocFailAccum * 1000L) / dt);
                perfSpawnAccum = 0;
                perfKillAccum = 0;
                perfAllocFailAccum = 0;

                perfAlphaImgReqRate = (int) ((perfAlphaImgReqAccum * 1000L) / dt);
                perfAlphaImgHitRate = (int) ((perfAlphaImgHitAccum * 1000L) / dt);
                perfAlphaImgCreateRate = (int) ((perfAlphaImgCreateAccum * 1000L) / dt);
                perfAlphaRegReqRate = (int) ((perfAlphaRegReqAccum * 1000L) / dt);
                perfAlphaRegHitRate = (int) ((perfAlphaRegHitAccum * 1000L) / dt);
                perfAlphaRegCreateRate = (int) ((perfAlphaRegCreateAccum * 1000L) / dt);
                perfAlphaKeyReqRate = (int) ((perfAlphaKeyReqAccum * 1000L) / dt);
                perfAlphaKeyHitRate = (int) ((perfAlphaKeyHitAccum * 1000L) / dt);
                perfAlphaKeyCreateRate = (int) ((perfAlphaKeyCreateAccum * 1000L) / dt);
                perfAlphaImgReqAccum = 0;
                perfAlphaImgHitAccum = 0;
                perfAlphaImgCreateAccum = 0;
                perfAlphaRegReqAccum = 0;
                perfAlphaRegHitAccum = 0;
                perfAlphaRegCreateAccum = 0;
                perfAlphaKeyReqAccum = 0;
                perfAlphaKeyHitAccum = 0;
                perfAlphaKeyCreateAccum = 0;
                perfRateLastSampleMs = now;
            }
        }

        int act = (bullets != null) ? bullets.activeCount() : 0;

        String l1 = "perf ms u" + perfBulletUpdateMs + " c" + perfPlayerCollisionMs + " r" + perfBulletRenderMs + " b" + perfBlindOverlayMs;
        String l2 = "perf b act" + act + " sp" + perfSpawnRate + " kl" + perfKillRate + " af" + perfAllocFailRate;

        String l3 = "alpha img r" + perfAlphaImgReqRate + " h" + perfAlphaImgHitRate + " c" + perfAlphaImgCreateRate;
        String l4 = "alpha reg r" + perfAlphaRegReqRate + " h" + perfAlphaRegHitRate + " c" + perfAlphaRegCreateRate;
        String l5 = "alpha key r" + perfAlphaKeyReqRate + " h" + perfAlphaKeyHitRate + " c" + perfAlphaKeyCreateRate;

        // Spike lines shown for 5s after detection.
        String spikeStr = null;
        String spikeStr2 = null;
        if (spikeDisplayUntil > 0 && now > 0 && now < spikeDisplayUntil) {
            spikeStr = "SPIKE #" + spikeCount + " tot" + spikeTotalMs + " upd" + spikeUpdateMs + " rnd" + spikeRenderMs + " ci" + spikeCutInMs + " au" + spikeAudioMs;
            spikeStr2 = "bs" + spikeBattleSceneMs + " gb" + spikeWldGbMs + " ma" + spikeWldMaMs + " rst" + spikeWldResetMs + " o" + spikeWldOtherMs;
        }

        Font font;
        try {
            font = g.getFont();
        } catch (Throwable t) {
            font = Font.getDefaultFont();
        }
        if (font == null) {
            font = Font.getDefaultFont();
        }

        int lineH = UiDraw.fontHeight(font);
        if (lineH <= 0) {
            lineH = 14;
        }

        int count = 0;
        count = wrapLineInto(font, l1, maxW, wrapLinesBuf, count);
        count = wrapLineInto(font, l2, maxW, wrapLinesBuf, count);
        count = wrapLineInto(font, l3, maxW, wrapLinesBuf, count);
        count = wrapLineInto(font, l4, maxW, wrapLinesBuf, count);
        count = wrapLineInto(font, l5, maxW, wrapLinesBuf, count);
        if (spikeStr != null) {
            count = wrapLineInto(font, spikeStr, maxW, wrapLinesBuf, count);
        }
        if (spikeStr2 != null) {
            count = wrapLineInto(font, spikeStr2, maxW, wrapLinesBuf, count);
        }
        if (count <= 0) {
            return 0;
        }

        int boxW = maxW;
        int boxH = (count * lineH) + 4;

        try {
            g.setColor(0x000000);
            g.fillRect(x - 1, y - 1, boxW + 2, boxH + 2);
        } catch (Throwable t) {
        }

        int yy = y + 2;
        for (int i = 0; i < count; i++) {
            String s = wrapLinesBuf[i];
            if (s != null) {
                drawDebugString(g, s, x + 2, yy, 0x00FF00);
            }
            yy += lineH;
        }

        return boxH + 2;
    }

    private static int wrapLineInto(Font font, String s, int maxW, String[] out, int outCount) {
        if (out == null || font == null) {
            return outCount;
        }
        if (s == null) {
            s = "";
        }
        if (maxW <= 0) {
            if (outCount < out.length) {
                out[outCount++] = s;
            }
            return outCount;
        }

        int len = s.length();
        int start = 0;
        while (start < len && outCount < out.length) {
            int end = start;
            int w = 0;
            while (end < len) {
                char ch = s.charAt(end);
                int cw = UiDraw.charWidth(font, ch);
                if (w + cw > maxW && end > start) {
                    break;
                }
                w += cw;
                end++;
                if (w >= maxW) {
                    break;
                }
            }

            if (end <= start) {
                end = start + 1;
            }

            out[outCount++] = s.substring(start, end);
            start = end;
        }
        return outCount;
    }

    private static long nowMs() {
        try {
            return System.currentTimeMillis();
        } catch (Throwable t) {
            return 0;
        }
    }

    private static void drawDebugString(Graphics g, String s, int x, int y, int color) {
        Font font;
        try {
            font = g.getFont();
        } catch (Throwable t) {
            font = Font.getDefaultFont();
        }
        if (font == null) {
            font = Font.getDefaultFont();
        }

        // Keep TOP alignment behavior while allowing BitmapFont rendering.
        int base = UiDraw.fontBaseline(font);
        UiDraw.drawStringPlain(g, font, s, x + 1, y + 1 + base, 0, 0x000000);
        UiDraw.drawStringPlain(g, font, s, x, y + base, 0, color);
    }

    private static void renderFpsOverlay(Graphics g, boolean enabled) {
        if (!enabled || g == null) {
            return;
        }

        long now = 0;
        try {
            now = System.currentTimeMillis();
        } catch (Throwable t) {
            return;
        }

        if (fpsLastSampleMs == 0) {
            fpsLastSampleMs = now;
            fpsFrameCount = 0;
            fpsValue = 0;
        }

        fpsFrameCount++;
        long dt = now - fpsLastSampleMs;
        if (dt >= 1000) {
            fpsValue = (int) ((fpsFrameCount * 1000L) / dt);
            fpsFrameCount = 0;
            fpsLastSampleMs = now;
        }

        int x = 2;
        int y = 226;
        try {
            x = g.getClipX() + 2;
            y = g.getClipY() + g.getClipHeight() - 14;
        } catch (Throwable ignore) {
        }
        drawDebugString(g, "FPS=" + fpsValue, x, y, 0x00FF00);
    }

    private static String buildGhostSummaryLine(BulletSystem bullets, int[][] enemylist, int[][] effectlist,
            CcTable cc, int[][] effectTable) {
        if (cc == null || bullets == null || enemylist == null || effectlist == null || effectTable == null) {
            return null;
        }

        int ghostBullets = 0;
        int ghostEnemies = 0;
        int ghostEffects = 0;

        for (int i = 0; i < bullets.getMax(); i++) {
            if (!bullets.isActive(i)) {
                continue;
            }
            int bId = bullets.getBulletId(i);
            int imgIndex = cc.getImgIndex(bId);
            if (imgIndex < 0) {
                continue;
            }
            if (imgIndex == 7 || imgIndex == 8) {
                ghostBullets++;
            }
        }

        for (int i = 0; i != 32; ++i) {
            if (enemylist[i][0] == 0) {
                continue;
            }
            int spriteMain = resolveEnemySpriteId(enemylist, i);
            int spriteAlt = resolveEnemyAltSpriteId(enemylist, i);
            if (spriteMain >= 0 && spriteUsesGhostImage(cc, spriteMain)) {
                ghostEnemies++;
            } else if (spriteAlt >= 0 && spriteUsesGhostImage(cc, spriteAlt)) {
                ghostEnemies++;
            }
        }

        for (int i = 0; i < 64; i++) {
            if (effectlist[i][0] == 0) {
                continue;
            }
            int id = effectlist[i][3];
            if (id == 21) {
                continue;
            }
            int[] row = null;
            if (id >= 0 && id < effectTable.length) {
                row = effectTable[id];
            }
            if (row == null || row.length == 0) {
                continue;
            }

            int frame = effectlist[i][2];
            int spriteIdx = (frame << 1) + 1;
            if (spriteIdx < 0 || spriteIdx >= row.length) {
                continue;
            }
            int spriteId = row[spriteIdx];
            if (spriteId >= 0 && spriteUsesGhostImage(cc, spriteId)) {
                ghostEffects++;
            }
        }

        return "gh b" + ghostBullets + " e" + ghostEnemies + " f" + ghostEffects;
    }

    private static String buildGhostFirstLine(BulletSystem bullets, int[][] enemylist, int[][] effectlist,
            CcTable cc, int[][] effectTable) {
        return buildGhostNthLine(0, bullets, enemylist, effectlist, cc, effectTable);
    }

    private static String buildGhostSecondLine(BulletSystem bullets, int[][] enemylist, int[][] effectlist,
            CcTable cc, int[][] effectTable) {
        return buildGhostNthLine(1, bullets, enemylist, effectlist, cc, effectTable);
    }

    private static String buildGhostNthLine(int skipCount, BulletSystem bullets, int[][] enemylist, int[][] effectlist,
            CcTable cc, int[][] effectTable) {
        if (cc == null || bullets == null || enemylist == null || effectlist == null || effectTable == null) {
            return null;
        }

        int remaining = skipCount;
        int bestType = 0;
        int bestId = -1;
        int bestX = 0;
        int bestY = 0;

        for (int i = 0; i < bullets.getMax(); i++) {
            if (!bullets.isActive(i)) {
                continue;
            }
            int bId = bullets.getBulletId(i);
            int imgIndex = cc.getImgIndex(bId);
            if (imgIndex < 0) {
                continue;
            }
            if (imgIndex == 7 || imgIndex == 8) {
                if (remaining > 0) {
                    remaining--;
                    continue;
                }
                bestType = 1;
                bestId = bId;
                bestX = bullets.getXFixed(i);
                bestY = bullets.getYFixed(i);
                break;
            }
        }

        if (bestId == -1) {
            for (int i = 0; i != 32; ++i) {
                if (enemylist[i][0] == 0) {
                    continue;
                }
                int spriteMain = resolveEnemySpriteId(enemylist, i);
                int spriteAlt = resolveEnemyAltSpriteId(enemylist, i);
                int useSprite = -1;
                if (spriteMain >= 0 && spriteUsesGhostImage(cc, spriteMain)) {
                    useSprite = spriteMain;
                } else if (spriteAlt >= 0 && spriteUsesGhostImage(cc, spriteAlt)) {
                    useSprite = spriteAlt;
                }
                if (useSprite >= 0) {
                    if (remaining > 0) {
                        remaining--;
                        continue;
                    }
                    bestType = 2;
                    bestId = useSprite;
                    bestX = enemylist[i][5];
                    bestY = enemylist[i][6];
                    break;
                }
            }
        }

        if (bestId == -1) {
            for (int i = 0; i < 64; i++) {
                if (effectlist[i][0] == 0) {
                    continue;
                }
                int id = effectlist[i][3];
                if (id == 21) {
                    continue;
                }
                int[] rowFx = null;
                if (id >= 0 && id < effectTable.length) {
                    rowFx = effectTable[id];
                }
                if (rowFx == null || rowFx.length == 0) {
                    continue;
                }
                int frame = effectlist[i][2];
                int spriteIdx = (frame << 1) + 1;
                if (spriteIdx < 0 || spriteIdx >= rowFx.length) {
                    continue;
                }
                int spriteId = rowFx[spriteIdx];
                if (spriteId >= 0 && spriteUsesGhostImage(cc, spriteId)) {
                    if (remaining > 0) {
                        remaining--;
                        continue;
                    }
                    bestType = 3;
                    bestId = spriteId;
                    bestX = effectlist[i][4];
                    bestY = effectlist[i][5];
                    break;
                }
            }
        }

        if (bestId == -1) {
            return null;
        }

        String t;
        if (bestType == 1) {
            t = "b";
        } else if (bestType == 2) {
            t = "e";
        } else {
            t = "fx";
        }
        return "gh" + (skipCount + 1) + " " + t + " id" + bestId + " x" + (bestX >> 16) + " y" + (bestY >> 16);
    }

    private static boolean spriteUsesGhostImage(CcTable cc, int spriteId) {
        if (cc == null) {
            return false;
        }
        int imgIndex = cc.getImgIndex(spriteId);
        return imgIndex == 7 || imgIndex == 8;
    }

    private static int resolveEnemySpriteId(int[][] enemylist, int i) {
        if (enemylist == null) {
            return -1;
        }
        int spriteId;
        switch (enemylist[i][1]) {
            case 0:
                spriteId = 0 + ((enemylist[i][2] / 3) & 0x3);
                break;
            case 1:
                spriteId = 4 + ((enemylist[i][2] / 3) & 0x3);
                break;
            case 2:
                spriteId = 8 + ((enemylist[i][2] / 3) & 0x3);
                break;
            case 3:
                spriteId = 12 + ((enemylist[i][2] / 3) & 0x3);
                break;
            case 4:
                spriteId = 16 + ((enemylist[i][2] / 3) & 0x1);
                break;
            case 5:
                spriteId = 18 + ((enemylist[i][2] / 3) & 0x1);
                break;
            case 6:
                spriteId = 20 + ((enemylist[i][2] / 3) & 0x1);
                break;
            case 7:
                spriteId = 22 + ((enemylist[i][2] / 3) & 0x1);
                break;
            case 8:
                spriteId = 24 + ((enemylist[i][2] / 3) % 3);
                break;
            case 20:
            case 21: {
                int idx = (enemylist[i][2] / 3) % 12;
                spriteId = 380 + ENEMY20_ANIM[idx];
                break;
            }
            case 22:
                spriteId = 494 + (i % 6);
                break;
            default:
                return -1;
        }
        return spriteId;
    }

    private static int resolveEnemyAltSpriteId(int[][] enemylist, int i) {
        if (enemylist == null) {
            return -1;
        }
        if (enemylist[i][1] == 20 || enemylist[i][1] == 21) {
            if (enemylist[i][1] == 20) {
                return 388 + ((enemylist[i][2] >> 1) & 0x3);
            }
            return 388 + (3 - ((enemylist[i][2] >> 1) & 0x3));
        }
        return -1;
    }

    private static final int[] ENEMY20_ANIM = new int[] { 0, 1, 2, 3, 2, 1, 0, 4, 5, 6, 5, 4 };
}
