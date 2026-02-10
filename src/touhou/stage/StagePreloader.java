package touhou.stage;

import touhou.BulletSprites;
import touhou.ImageBank;
import touhou.ui.UiDraw;

public final class StagePreloader {
    private int stage;
    private int chara;
    private int type;
    private boolean playerShotAlphaEnabled = true;
    private boolean bossSpellCutInAlphaEnabled = true;
    private boolean bombOverlayAlphaEnabled = true;
    private int step;
    private int stepMax;
    private boolean done;

    public void setPlayerShotAlphaEnabled(boolean enabled) {
        playerShotAlphaEnabled = enabled;
    }

    public void setBossSpellCutInAlphaEnabled(boolean enabled) {
        bossSpellCutInAlphaEnabled = enabled;
    }

    public void setBombOverlayAlphaEnabled(boolean enabled) {
        bombOverlayAlphaEnabled = enabled;
    }

    public void enter(int nextStage) {
        enter(nextStage, 0, 0);
    }

    public void enter(int nextStage, int chara, int type) {
        enter(nextStage, chara, type, playerShotAlphaEnabled);
    }

    public void enter(int nextStage, int chara, int type, boolean playerShotAlphaEnabled) {
        stage = nextStage;
        this.chara = chara;
        this.type = type;
        this.playerShotAlphaEnabled = playerShotAlphaEnabled;
        step = 0;
        done = false;
        // Keep this in sync with tickOneStep().
        stepMax = 12;
    }

    public boolean isDone() {
        return done;
    }

    public int getStep() {
        return step;
    }

    public int getStepMax() {
        return stepMax;
    }

    public void update(ImageBank imgs, BulletSprites sprites) {
        if (done) {
            return;
        }
        if (imgs == null || sprites == null) {
            done = true;
            return;
        }
        tickOneStep(imgs, sprites);
    }

    private void tickOneStep(ImageBank imgs, BulletSprites sprites) {
        switch (step) {
            case 0:
                // Common HUD/frame images and always-present sprite sheets.
                imgs.get(20);
                imgs.get(21);
                imgs.get(22);
                imgs.get(23);

                // Effects / enemies / bullets+items.
                imgs.get(5);
                imgs.get(6);
                imgs.get(7);
                imgs.get(19);
                break;
            case 1:
                // Common base background texture (stage 0/1/2).
                imgs.get(25);
                break;
            case 2:
                // Stage background images.
                preloadStageBackground(imgs, stage);
                break;
            case 3:
                // Stage background decoration sprites (cc icons).
                preloadStageDecorationSprites(imgs, sprites, stage);
                break;
            case 4:
                // Common small enemy sprites.
                preloadSpriteRange(imgs, sprites, 0, 27, 255);
                break;
            case 5:
                // Common bullet sprites (heuristic).
                preloadSpriteRange(imgs, sprites, 80, 160, 255);

                // Drop items use sprite ids 111..165; 161..165 are not covered by the heuristic range.
                preloadSpriteRange(imgs, sprites, 161, 165, 255);
                break;
            case 6:
                // Boss/enemy sprite ids commonly used by stage renderer.
                preloadSpriteRange(imgs, sprites, 166, 185, 255);

                // Boss sprites (enemy kinds 13..19 in BattleRenderer).
                // kind13
                preloadSpriteRange(imgs, sprites, 196, 215, 255);
                // kind14
                preloadSpriteRange(imgs, sprites, 216, 238, 255);
                // kind15 (includes +14 offset frames)
                preloadSpriteRange(imgs, sprites, 356, 383, 255);
                // kind16
                preloadSpriteRange(imgs, sprites, 504, 520, 255);

                // kind17/18/19 (boss main body parts)
                preloadSpriteRange(imgs, sprites, 711, 727, 255);
                // Common boss bullet sprites seen in stage boss patterns.
                preloadSpriteIds(imgs, sprites, new int[] { 81, 82, 84, 89, 585, 603, 639 }, 255);
                break;
            case 7:
                // Boss dialogue portraits (ImageBank indices) used by BattleRenderer.renderDialogue.
                preloadBossDialoguePortraitImages(imgs);
                break;
            case 8:
                // Boss spell cut-in overlay images.
                preloadBossSpellCutInImages(imgs, stage, bossSpellCutInAlphaEnabled);
                break;
            case 9:
                // Player character image sheets + player bullets (alpha=128) for the selected chara/type.
                preloadPlayerAssets(imgs, sprites, chara, type, playerShotAlphaEnabled);
                break;
            case 10:
                // Bomb overlay image + waTick bullet sprites + darkness overlay precache.
                preloadBombAndMiscAssets(imgs, sprites, chara,
                        bossSpellCutInAlphaEnabled || bombOverlayAlphaEnabled);
                break;
            case 11:
                // Final step: proactive GC to start stage with a clean heap.
                System.gc();
                break;
            default:
                done = true;
                return;
        }

        step++;
        if (step >= stepMax) {
            done = true;
        }
    }

    private static void preloadStageBackground(ImageBank imgs, int stage) {
        // Stage index: 0..6 (Stage1..Stage6 + StageEX).
        switch (stage) {
            case 0:
                imgs.get(25);
                break;
            case 1:
                imgs.get(25);
                imgs.get(32);
                imgs.getAlphaImage(32, 128);
                break;
            case 2:
                imgs.get(25);
                imgs.get(44);
                imgs.get(45);
                imgs.getAlphaImage(44, 128);
                imgs.getAlphaImage(45, 128);
                break;
            case 3:
                imgs.get(59);
                imgs.get(54);
                imgs.getAlphaImage(54, 127);
                break;
            case 4:
                imgs.get(62);
                imgs.get(63);
                imgs.get(64);
                imgs.get(66);
                imgs.getAlphaImage(64, 128);
                break;
            case 5:
                imgs.get(70);
                break;
            case 6:
                // StageEX base/overlay phases.
                imgs.get(77);
                imgs.get(78);
                imgs.get(79);
                imgs.get(80);
                imgs.get(81);
                imgs.get(82);
                imgs.get(83);
                imgs.get(84);
                break;
            default:
                break;
        }
    }

    private static void preloadBossDialoguePortraitImages(ImageBank imgs) {
        // Keep in sync with BattleRenderer.renderDialogue portraitType mappings.
        imgs.get(8);
        imgs.get(9);
        imgs.get(10);
        imgs.get(28);
        imgs.get(39);
        imgs.get(50);
        imgs.get(53);
        imgs.get(60);
        imgs.get(72);
        imgs.get(74);
    }

    private static void preloadBossSpellCutInImages(ImageBank imgs, int stage, boolean alphaEnabled) {
        // Keep in sync with BattleRenderer.bossSpellCutInImgIndexForStage().
        int imgIndex = -1;
        switch (stage) {
            case 0:
                imgIndex = 30;
                break;
            case 1:
                imgIndex = 40;
                break;
            case 2:
                imgIndex = 51;
                break;
            case 3:
                imgIndex = 56;
                imgs.get(58);
                break;
            case 4:
                imgIndex = 65;
                break;
            case 5:
                imgIndex = 69;
                break;
            case 6:
                imgIndex = 76;
                break;
            default:
                imgIndex = -1;
                break;
        }
        if (imgIndex >= 0) {
            imgs.get(imgIndex);
            if (alphaEnabled) {
                // Prebuild a common alpha variant to avoid first-time alpha generation cost.
                imgs.getAlphaImage(imgIndex, 128);
                if (stage == 3) {
                    imgs.getAlphaImage(58, 128);
                }
            }
        }
    }

    private static void preloadPlayerAssets(ImageBank imgs, BulletSprites sprites, int chara, int type, boolean playerShotAlphaEnabled) {
        if (imgs == null || sprites == null) {
            return;
        }

        // Player sprite sheet used by Player.render.
        imgs.get(8 + chara);

        // Slow aura alpha regions used by Player.render.
        // The region is (auraFrame*32,117,32,31) on sheet 19 with alpha=128.
        imgs.get(19);
        imgs.getAlphaRegion(19, 0, 117, 32, 31, 128);
        imgs.getAlphaRegion(19, 32, 117, 32, 31, 128);
        imgs.getAlphaRegion(19, 64, 117, 32, 31, 128);
        imgs.getAlphaRegion(19, 96, 117, 32, 31, 128);

        // Precache the exact player shot sprites (alpha=128 draw path).
        if (playerShotAlphaEnabled) {
            int shot1 = findPlayerShotSpriteId(sprites, chara, type, false);
            int shot2 = findPlayerShotSpriteId(sprites, chara, type, true);
            int imgIndex = -1;
            if (shot1 >= 0) {
                imgIndex = sprites.getImageIndex(shot1);
            } else if (shot2 >= 0) {
                imgIndex = sprites.getImageIndex(shot2);
            }
            if (imgIndex >= 0) {
                imgs.get(imgIndex);
                imgs.getAlphaImage(imgIndex, 128);
            }
        }
    }

    private static int findPlayerShotSpriteId(BulletSprites sprites, int chara, int type, boolean isAlt) {
        if (sprites == null || sprites.getCc() == null) {
            return -1;
        }
        int imgIndex = 8 + chara;
        int minSize = isAlt ? 8 : 3;
        int maxSize = isAlt ? 24 : 10;
        int minSrcX = 96;
        int maxSrcX = 10000;

        // Mirrors Player.findSpriteId selection criteria without creating a Player instance.
        int bestId = -1;
        int bestY = Integer.MAX_VALUE;
        int bestX = Integer.MAX_VALUE;

        touhou.CcTable cc = sprites.getCc();
        for (int i = 0; i < cc.size(); i++) {
            if (!cc.hasSpriteMeta(i)) {
                continue;
            }
            int idx = cc.getImgIndex(i);
            if (idx != imgIndex) {
                continue;
            }
            int w = cc.getW(i);
            int h = cc.getH(i);
            if (w < minSize || h < minSize || w > maxSize || h > maxSize) {
                continue;
            }
            int srcX = cc.getSrcX(i);
            int srcY = cc.getSrcY(i);
            if (srcX < minSrcX || srcX > maxSrcX) {
                continue;
            }
            if (srcY < bestY || (srcY == bestY && srcX < bestX)) {
                bestY = srcY;
                bestX = srcX;
                bestId = i;
            }
        }

        // Marisa type 0 uses an alternate shot when available.
        if (chara == 1 && type == 0) {
            return bestId;
        }
        // Default: use the same selected id.
        return bestId;
    }

    private static void preloadStageDecorationSprites(ImageBank imgs, BulletSprites sprites, int stage) {
        if (stage == 0) {
            preloadSpriteIds(imgs, sprites, new int[] { 191, 192 }, 255);
            return;
        }
        if (stage == 1) {
            preloadSpriteIds(imgs, sprites, new int[] { 351, 352, 353, 354, 355 }, 255);
            return;
        }
        if (stage == 2) {
            preloadSpriteIds(imgs, sprites, new int[] { 485, 486, 487, 488, 489 }, 255);
            return;
        }
        if (stage == 5) {
            // Stage 5 falling particles.
            preloadSpriteIds(imgs, sprites, new int[] { 728, 729 }, 128);
            return;
        }
    }

    private static void preloadBombAndMiscAssets(ImageBank imgs, BulletSprites sprites, int chara, boolean bossSpellAlphaEnabled) {
        // Bomb overlay image (imgIndex 12+chara). Previously not preloaded, causing first-bomb stutter.
        int bombImgIndex = 12 + chara;
        if (bombImgIndex >= 12 && bombImgIndex <= 14) {
            imgs.get(bombImgIndex);
        }

        // waTick bullet sprites per character (loaded on first slow-mode shot otherwise).
        if (sprites != null) {
            switch (chara) {
                case 0:
                    // Reimu: xaSpawn types 0,1,2,18 -> sprite IDs 31-32, 48-60.
                    preloadSpriteRange(imgs, sprites, 31, 32, 255);
                    preloadSpriteRange(imgs, sprites, 48, 60, 255);
                    break;
                case 1:
                    // Marisa: xaSpawn types 8,19 + lasers 9,10 -> sprite IDs 97-107.
                    preloadSpriteRange(imgs, sprites, 97, 107, 255);
                    break;
                case 2:
                    // Alice: xaSpawn types 11-17 -> sprite IDs 730-766.
                    // 730-740 already covered by step 6 (kind17/18/19 range 711-727 + decorations).
                    preloadSpriteRange(imgs, sprites, 733, 766, 255);
                    break;
            }
        }

        // Precache darkness overlay (194x226 black@128).
        // Shared by boss spells and player bombs; only one layer is rendered.
        if (bossSpellAlphaEnabled) {
            UiDraw.precacheSolidAlpha(194, 226, 0x000000, 128);
        }
    }

    private static void preloadSpriteIds(ImageBank imgs, BulletSprites sprites, int[] ids, int alpha) {
        if (ids == null) {
            return;
        }
        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            int imgIndex = sprites.getImageIndex(id);
            if (imgIndex < 0) {
                continue;
            }
            imgs.get(imgIndex);
            if (alpha != 255) {
                sprites.precacheAlphaRegion(id, alpha);
            }
        }
    }

    private static void preloadSpriteRange(ImageBank imgs, BulletSprites sprites, int idFrom, int idTo, int alpha) {
        for (int id = idFrom; id <= idTo; id++) {
            int imgIndex = sprites.getImageIndex(id);
            if (imgIndex < 0) {
                continue;
            }
            imgs.get(imgIndex);
            if (alpha != 255) {
                sprites.precacheAlphaRegion(id, alpha);
            }
        }
    }
}
