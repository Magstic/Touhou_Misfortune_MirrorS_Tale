package touhou;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import touhou.ui.UiDraw;

public final class BulletSprites {
    private boolean debugDrawBounds = false;

    // Compat option: if disabled, player bullets are drawn without runtime alpha.
    private boolean playerShotAlphaEnabled = true;

    private final CcTable cc;
    private final ImageBank images;

    public BulletSprites(CcTable cc, ImageBank images) {
        this.cc = cc;
        this.images = images;
    }

    public void setDebugDrawBounds(boolean enabled) {
        debugDrawBounds = enabled;
    }

    public void setPlayerShotAlphaEnabled(boolean enabled) {
        playerShotAlphaEnabled = enabled;
    }

    public ImageBank getImages() {
        return images;
    }

    public CcTable getCc() {
        return cc;
    }

    // Returns backing sheet image index for a sprite id.
    public int getImageIndex(int bulletId) {
        return cc.getImgIndex(bulletId);
    }

    // Pre-build commonly used alpha region for a sprite.
    public void precacheAlphaRegion(int bulletId, int alpha) {
        if (!cc.hasSpriteMeta(bulletId)) {
            return;
        }

        int imgIndex = cc.getImgIndex(bulletId);
        int srcX = cc.getSrcX(bulletId);
        int srcY = cc.getSrcY(bulletId);
        int w = cc.getW(bulletId);
        int h = cc.getH(bulletId);

        if (images != null) {
            images.getAlphaRegion(imgIndex, srcX, srcY, w, h, alpha);
        }
    }

    // Returns sprite size and anchor (w,h,ax,ay) from cc.dat for UI layout.
    public boolean getSizeAndAnchor(int bulletId, int[] out) {
        if (out == null || out.length < 4) {
            return false;
        }
        if (!cc.hasSpriteMeta(bulletId)) {
            return false;
        }
        out[0] = cc.getW(bulletId);
        out[1] = cc.getH(bulletId);
        out[2] = cc.getAx(bulletId);
        out[3] = cc.getAy(bulletId);
        return true;
    }

    public boolean draw(Graphics g, int bulletId, int x, int y) {
        return draw(g, bulletId, x, y, false);
    }

    // Fast path for bullet rendering: relies on caller's global clip (playfield).
    // Avoids per-sprite setClip/clipRect which is very slow on real devices.
    public boolean drawFast(Graphics g, int bulletId, int x, int y, boolean isFromPlayer, int clipX, int clipY, int clipW, int clipH) {
        if (!cc.hasSpriteMeta(bulletId)) {
            return false;
        }

        int imgIndex = cc.getImgIndex(bulletId);
        int srcX = cc.getSrcX(bulletId);
        int srcY = cc.getSrcY(bulletId);
        int w = cc.getW(bulletId);
        int h = cc.getH(bulletId);
        int ax = cc.getAx(bulletId);
        int ay = cc.getAy(bulletId);

        if (w <= 0 || h <= 0) {
            return true;
        }

        int dx = x - ax;
        int dy = y - ay;

        // Offscreen cull (no bullet loss: only skipping draw when fully outside clip).
        if (clipW > 0 && clipH > 0) {
            int cx1 = clipX + clipW;
            int cy1 = clipY + clipH;
            if (dx >= cx1 || dy >= cy1 || (dx + w) <= clipX || (dy + h) <= clipY) {
                return true;
            }
        }

        Image img = images.get(imgIndex);
        if (img == null) {
            return false;
        }

        int drawSrcX = srcX;
        int drawSrcY = srcY;
        if (isFromPlayer && playerShotAlphaEnabled) {
            // Player bullets are drawn with alpha=128.
            Image alphaImg = images.getAlphaImage(imgIndex, 128);
            if (alphaImg != null) {
                img = alphaImg;
            }
        }

        try {
            // transform=0 (no rotation/flip), anchor=TOP|LEFT.
            g.drawRegion(img, drawSrcX, drawSrcY, w, h, 0, dx, dy, Graphics.TOP | Graphics.LEFT);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean drawAlpha(Graphics g, int bulletId, int x, int y, int alpha) {
        return drawAlpha(g, bulletId, x, y, false, alpha);
    }

    public boolean drawAlpha(Graphics g, int bulletId, int x, int y, boolean isFromPlayer, int alpha) {
        if (!cc.hasSpriteMeta(bulletId)) {
            return false;
        }

        int imgIndex = cc.getImgIndex(bulletId);
        int srcX = cc.getSrcX(bulletId);
        int srcY = cc.getSrcY(bulletId);
        int w = cc.getW(bulletId);
        int h = cc.getH(bulletId);
        int ax = cc.getAx(bulletId);
        int ay = cc.getAy(bulletId);

        Image img = images.get(imgIndex);
        if (img == null) {
            return false;
        }

        int a = alpha;
        if (isFromPlayer) {
            if (!playerShotAlphaEnabled) {
                a = 255;
            } else {
                a = 128;
            }
        }
        if (a < 0) {
            a = 0;
        }
        if (a > 255) {
            a = 255;
        }
        if (!isFromPlayer && a != 0 && a != 255) {
            // Quantize alpha to reduce distinct cached variants on low-end devices.
            a = (a + 8) & 0xF0;
            if (a <= 0) {
                a = 16;
            }
            if (a >= 255) {
                a = 255;
            }
        }
        int drawSrcX = srcX;
        int drawSrcY = srcY;
        if (a != 255) {
            if (isFromPlayer) {
                // Avoid alpha region cache churn for high-rate player shots.
                Image alphaImg = images.getAlphaImage(imgIndex, a);
                if (alphaImg != null) {
                    img = alphaImg;
                }
            } else {
                // Prefer alpha region to avoid generating a full-sheet alpha image.
                Image alphaRegion = images.getAlphaRegion(imgIndex, srcX, srcY, w, h, a);
                if (alphaRegion != null) {
                    img = alphaRegion;
                    drawSrcX = 0;
                    drawSrcY = 0;
                } else {
                    Image alphaImg = images.getAlphaImage(imgIndex, a);
                    if (alphaImg != null) {
                        img = alphaImg;
                    }
                }
            }
        }

        if (drawRegionSafe(g, img, drawSrcX, drawSrcY, w, h, ax, ay, x, y, 1)) {
            drawDebug(g, bulletId, img, x, y, srcX, srcY, w, h, ax, ay, 1);
            return true;
        }
        return false;
    }

    public boolean draw(Graphics g, int bulletId, int x, int y, boolean isFromPlayer) {
        if (!cc.hasSpriteMeta(bulletId)) {
            return false;
        }

        int imgIndex = cc.getImgIndex(bulletId);
        int srcX = cc.getSrcX(bulletId);
        int srcY = cc.getSrcY(bulletId);
        int w = cc.getW(bulletId);
        int h = cc.getH(bulletId);
        int ax = cc.getAx(bulletId);
        int ay = cc.getAy(bulletId);

        Image img = images.get(imgIndex);
        if (img == null) {
            return false;
        }

        int drawSrcX = srcX;
        int drawSrcY = srcY;
        if (isFromPlayer && playerShotAlphaEnabled) {
            // Avoid alpha region cache churn for high-rate player shots.
            Image alphaImg = images.getAlphaImage(imgIndex, 128);
            if (alphaImg != null) {
                img = alphaImg;
            }
        }

        if (drawRegionSafe(g, img, drawSrcX, drawSrcY, w, h, ax, ay, x, y, 1)) {
            drawDebug(g, bulletId, img, x, y, srcX, srcY, w, h, ax, ay, 1);
            return true;
        }
        return false;
    }

    private void drawDebug(Graphics g, int bulletId, Image img, int x, int y, int srcX, int srcY, int w, int h, int ax, int ay, int scale) {
        if (!debugDrawBounds) {
            return;
        }
        if (scale != 1) {
            w *= scale;
            h *= scale;
            ax *= scale;
            ay *= scale;
            srcX *= scale;
            srcY *= scale;
        }

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        int dx = x - ax;
        int dy = y - ay;

        if (srcX < 0) {
            int cut = -srcX;
            srcX = 0;
            dx += cut;
            w -= cut;
        }
        if (srcY < 0) {
            int cut = -srcY;
            srcY = 0;
            dy += cut;
            h -= cut;
        }
        if (w <= 0 || h <= 0) {
            return;
        }

        if (srcX >= imgW || srcY >= imgH) {
            return;
        }
        if (srcX + w > imgW) {
            w = imgW - srcX;
        }
        if (srcY + h > imgH) {
            h = imgH - srcY;
        }
        if (w <= 0 || h <= 0) {
            return;
        }

        g.setColor(0x00FF00);
        g.drawRect(dx, dy, w - 1, h - 1);
        // Keep TOP alignment behavior while allowing BitmapFont rendering.
        int textY = dy + UiDraw.fontBaseline(null);
        UiDraw.drawStringPlain(g, null, String.valueOf(bulletId), dx, textY, 0, 0x00FF00);
    }

    private static boolean drawRegionSafe(Graphics g, Image img, int srcX, int srcY, int w, int h, int ax, int ay, int x, int y, int scale) {
        if (scale != 1) {
            srcX *= scale;
            srcY *= scale;
            w *= scale;
            h *= scale;
            ax *= scale;
            ay *= scale;
        }

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        if (w <= 0 || h <= 0) {
            return false;
        }

        int dx = x - ax;
        int dy = y - ay;

        if (srcX < 0) {
            int cut = -srcX;
            srcX = 0;
            dx += cut;
            w -= cut;
        }
        if (srcY < 0) {
            int cut = -srcY;
            srcY = 0;
            dy += cut;
            h -= cut;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }

        if (srcX >= imgW || srcY >= imgH) {
            return false;
        }
        if (srcX + w > imgW) {
            w = imgW - srcX;
        }
        if (srcY + h > imgH) {
            h = imgH - srcY;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }

        int oldClipX = g.getClipX();
        int oldClipY = g.getClipY();
        int oldClipW = g.getClipWidth();
        int oldClipH = g.getClipHeight();

        try {
            g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
            g.clipRect(dx, dy, w, h);
            g.drawImage(img, dx - srcX, dy - srcY, Graphics.TOP | Graphics.LEFT);
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            try {
                g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
            } catch (Throwable ignore) {
            }
        }
    }
}
