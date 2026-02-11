package touhou.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import touhou.font.BitmapFont;

public final class UiDraw {
    private static final Font DEFAULT_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private static final Image[] cachedGradImg = new Image[8];
    private static final int[] cachedGradW = new int[8];
    private static final int[] cachedGradH = new int[8];
    private static final int[] cachedGradFrom = new int[8];
    private static final int[] cachedGradTo = new int[8];
    private static final int[] cachedGradSteps = new int[8];
    private static final int[] cachedGradAlpha = new int[8];
    private static int gradCacheNext;

     private static final Image[] cachedSolidImg = new Image[32];
     private static final int[] cachedSolidW = new int[32];
     private static final int[] cachedSolidH = new int[32];
     private static final int[] cachedSolidArgb = new int[32];
     private static int solidCacheNext;

    private UiDraw() {
    }

    public static int stringWidth(Font font, String s) {
        BitmapFont bf = BitmapFont.get();
        if (bf != null) {
            if (s == null) {
                s = "";
            }
            return bf.stringWidth(s);
        }
        if (font == null) {
            font = DEFAULT_FONT;
        }
        if (s == null) {
            s = "";
        }
        return font.stringWidth(s);
    }

    public static int charWidth(Font font, char c) {
        BitmapFont bf = BitmapFont.get();
        if (bf != null) {
            return bf.charWidth(c);
        }
        if (font == null) {
            font = DEFAULT_FONT;
        }
        try {
            return font.charWidth(c);
        } catch (Throwable t) {
            return font.stringWidth(String.valueOf(c));
        }
    }

    public static int fontHeight(Font font) {
        BitmapFont bf = BitmapFont.get();
        if (bf != null) {
            return bf.getCellH();
        }
        if (font == null) {
            font = DEFAULT_FONT;
        }
        return font.getHeight();
    }

    public static int fontBaseline(Font font) {
        BitmapFont bf = BitmapFont.get();
        if (bf != null) {
            return bf.getBaseline();
        }
        if (font == null) {
            font = DEFAULT_FONT;
        }
        return font.getBaselinePosition();
    }

    // Ellipsize text to fit in maxWidth pixels (no scrolling).
    public static String ellipsize(Font font, String s, int maxWidth) {
        if (s == null) {
            s = "";
        }
        if (maxWidth <= 0) {
            return "";
        }
        if (stringWidth(font, s) <= maxWidth) {
            return s;
        }

        String ell = "...";
        int ellW = stringWidth(font, ell);
        if (ellW <= 0) {
            return "";
        }
        if (maxWidth <= ellW) {
            // Fit as many dots as possible.
            StringBuffer dots = new StringBuffer();
            for (int i = 0; i < ell.length(); i++) {
                dots.append('.');
                if (stringWidth(font, dots.toString()) > maxWidth) {
                    dots.setLength(dots.length() - 1);
                    break;
                }
            }
            return dots.toString();
        }

        int avail = maxWidth - ellW;
        int w = 0;
        int end = 0;
        while (end < s.length()) {
            char c = s.charAt(end);
            int cw = charWidth(font, c);
            if (cw < 0) {
                cw = 0;
            }
            if (w + cw > avail) {
                break;
            }
            w += cw;
            end++;
        }
        if (end <= 0) {
            return ell;
        }
        return s.substring(0, end) + ell;
    }

    public static void drawStringPlain(Graphics g, Font font, String s, int x, int y, int align, int color) {
        BitmapFont bf = BitmapFont.get();
        if (bf != null) {
            if (s == null) {
                s = "";
            }
            int drawX = x;
            if (align == 1) {
                drawX -= bf.stringWidth(s) >> 1;
            } else if (align == 2) {
                drawX -= bf.stringWidth(s);
            }
            bf.drawString(g, s, drawX, y, color);
            return;
        }

        if (font == null) {
            font = DEFAULT_FONT;
        }
        if (s == null) {
            s = "";
        }

        int drawX = x;
        if (align == 1) {
            drawX -= font.stringWidth(s) >> 1;
        } else if (align == 2) {
            drawX -= font.stringWidth(s);
        }

        g.setFont(font);
        g.setColor(color);
        g.drawString(s, drawX, y, Graphics.BASELINE | Graphics.LEFT);
    }

    // 1px outline text (8-direction) for specific UI elements.
    public static void drawStringOutline(Graphics g, Font font, String s, int x, int y, int align, int colorMain, int colorOutline) {
        if (g == null) {
            return;
        }
        if (s == null) {
            s = "";
        }
        for (int oy = -1; oy <= 1; oy++) {
            for (int ox = -1; ox <= 1; ox++) {
                if (ox == 0 && oy == 0) {
                    continue;
                }
                drawStringPlain(g, font, s, x + ox, y + oy, align, colorOutline);
            }
        }
        drawStringPlain(g, font, s, x, y, align, colorMain);
    }

    public static void drawRegion(Graphics g, Image img, int dstX, int dstY, int srcX, int srcY, int w, int h) {
        if (img == null || w <= 0 || h <= 0) {
            return;
        }

        int oldClipX = g.getClipX();
        int oldClipY = g.getClipY();
        int oldClipW = g.getClipWidth();
        int oldClipH = g.getClipHeight();

        int newClipX = dstX;
        int newClipY = dstY;
        int newClipW = w;
        int newClipH = h;
        if (oldClipW > 0 && oldClipH > 0) {
            int ix0 = (newClipX > oldClipX) ? newClipX : oldClipX;
            int iy0 = (newClipY > oldClipY) ? newClipY : oldClipY;
            int ix1 = ((newClipX + newClipW) < (oldClipX + oldClipW)) ? (newClipX + newClipW) : (oldClipX + oldClipW);
            int iy1 = ((newClipY + newClipH) < (oldClipY + oldClipH)) ? (newClipY + newClipH) : (oldClipY + oldClipH);
            int iw = ix1 - ix0;
            int ih = iy1 - iy0;
            if (iw <= 0 || ih <= 0) {
                return;
            }
            newClipX = ix0;
            newClipY = iy0;
            newClipW = iw;
            newClipH = ih;
        }

        try {
            g.setClip(newClipX, newClipY, newClipW, newClipH);
            g.drawImage(img, dstX - srcX, dstY - srcY, Graphics.TOP | Graphics.LEFT);
        } catch (Throwable t) {
        } finally {
            try {
                g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
            } catch (Throwable ignore) {
            }
        }
    }

    public static void drawString2(Graphics g, Font font, String s, int x, int y, int align, int colorMain, int colorShadow) {
        BitmapFont bf = BitmapFont.get();
        if (bf != null) {
            if (s == null) {
                s = "";
            }

            int drawX = x;
            if (align == 1) {
                drawX -= bf.stringWidth(s) >> 1;
            } else if (align == 2) {
                drawX -= bf.stringWidth(s);
            }

            bf.drawStringShadow(g, s, drawX, y, colorMain, colorShadow);
            return;
        }

        if (font == null) {
            font = DEFAULT_FONT;
        }
        if (s == null) {
            s = "";
        }

        int drawX = x;
        if (align == 1) {
            drawX -= font.stringWidth(s) >> 1;
        } else if (align == 2) {
            drawX -= font.stringWidth(s);
        }

        g.setFont(font);
        g.setColor(colorShadow);
        g.drawString(s, drawX + 1, y, Graphics.BASELINE | Graphics.LEFT);
        g.setColor(colorMain);
        g.drawString(s, drawX, y, Graphics.BASELINE | Graphics.LEFT);
    }

    // Horizontal marquee text (ping-pong). Starts from left, scrolls to right end, then back.
    public static void drawStringMarquee2(Graphics g, Font font, String s,
            int clipX, int baselineY, int clipW, int align,
            int colorMain, int colorShadow, int tick) {
        if (g == null || clipW <= 0) {
            return;
        }
        if (font == null) {
            font = DEFAULT_FONT;
        }
        if (s == null) {
            s = "";
        }

        int pad = 2;
        int textW = stringWidth(font, s);
        int avail = clipW - (pad << 1);
        if (avail <= 0 || textW <= avail) {
            int x = clipX;
            if (align == 1) {
                x += (clipW >> 1);
            } else if (align == 2) {
                x += clipW;
            }
            drawString2(g, font, s, x, baselineY, align, colorMain, colorShadow);
            return;
        }

        int range = textW - avail;
        int pause = 18;
        int step = (tick >= 0) ? (tick >> 1) : 0;
        int period = pause + range + pause + range;
        int m = (period > 0) ? (step % period) : 0;
        int off;
        if (m < pause) {
            off = 0;
        } else if (m < pause + range) {
            off = m - pause;
        } else if (m < pause + range + pause) {
            off = range;
        } else {
            off = range - (m - (pause + range + pause));
        }

        int clipY = baselineY - fontBaseline(font);
        int clipH = fontHeight(font);

        int oldClipX = g.getClipX();
        int oldClipY = g.getClipY();
        int oldClipW = g.getClipWidth();
        int oldClipH = g.getClipHeight();
        try {
            g.setClip(clipX, clipY, clipW, clipH);
            drawString2(g, font, s, clipX + pad - off, baselineY, 0, colorMain, colorShadow);
        } catch (Throwable t) {
        } finally {
            try {
                g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
            } catch (Throwable ignore) {
            }
        }
    }

    public static void drawGradation(Graphics g, int x, int y, int w, int h, int colorFrom, int colorTo, int steps) {
        if (steps <= 0 || h <= 0 || w <= 0) {
            return;
        }

        int stepH = h / steps;
        int currStepH = stepH;

        int r0 = (colorFrom >> 16) & 0xFF;
        int g0 = (colorFrom >> 8) & 0xFF;
        int b0 = (colorFrom) & 0xFF;

        int r1 = (colorTo >> 16) & 0xFF;
        int g1 = (colorTo >> 8) & 0xFF;
        int b1 = (colorTo) & 0xFF;

        int dr = Math.abs(r0 - r1) / steps;
        int dg = Math.abs(g0 - g1) / steps;
        int db = Math.abs(b0 - b1) / steps;

        int r = r0;
        int gg = g0;
        int bb = b0;
        int color = colorFrom;

        for (int i = 0; i < steps; i++) {
            if (i == steps - 1 && stepH * i + stepH < h) {
                currStepH += h - (stepH * i + stepH);
            }

            g.setColor(color);
            g.fillRect(x, y + stepH * i, w, currStepH);

            int nr = r + ((r0 < r1) ? dr : (-dr));
            int ng = gg + ((g0 < g1) ? dg : (-dg));
            int nb = bb + ((b0 < b1) ? db : (-db));

            r = clampByte(nr);
            gg = clampByte(ng);
            bb = clampByte(nb);

            color = (r << 16) | (gg << 8) | bb;
            currStepH = stepH;
        }
    }

    private static int clampByte(int v) {
        if (v < 0) {
            return 0;
        }
        if (v > 255) {
            return 255;
        }
        return v;
    }

     public static void drawGradationAlpha(Graphics g, int x, int y, int w, int h, int colorFrom, int colorTo, int steps, int alpha) {
         if (steps <= 0 || h <= 0 || w <= 0) {
             return;
         }
         if (alpha <= 0) {
             return;
         }
         if (alpha > 255) {
             alpha = 255;
         }

         Image img = getOrBuildGradationImage(w, h, colorFrom, colorTo, steps, alpha);

         if (img != null) {
             g.drawImage(img, x, y, Graphics.TOP | Graphics.LEFT);
             return;
         }

         drawGradation(g, x, y, w, h, colorFrom, colorTo, steps);
    }

     public static void fillRectAlpha(Graphics g, int x, int y, int w, int h, int rgb, int alpha) {
         if (w <= 0 || h <= 0) {
             return;
         }
         if (alpha <= 0) {
             return;
         }
         if (alpha >= 255) {
             g.setColor(rgb & 0x00FFFFFF);
             g.fillRect(x, y, w, h);
             return;
         }

         int argb = (alpha << 24) | (rgb & 0x00FFFFFF);
         Image img = getOrBuildSolidImage(w, h, argb);

         if (img != null) {
             g.drawImage(img, x, y, Graphics.TOP | Graphics.LEFT);
             return;
         }

         g.setColor(rgb & 0x00FFFFFF);
         g.fillRect(x, y, w, h);
    }

     // Pre-build a solid alpha image into internal cache.
     public static void precacheSolidAlpha(int w, int h, int rgb, int alpha) {
         if (w <= 0 || h <= 0) {
             return;
         }
         if (alpha <= 0 || alpha >= 255) {
             return;
         }
         int argb = (alpha << 24) | (rgb & 0x00FFFFFF);
         getOrBuildSolidImage(w, h, argb);
     }

     // Pre-build a gradation alpha image into internal cache.
     public static void precacheGradationAlpha(int w, int h, int colorFrom, int colorTo, int steps, int alpha) {
         if (steps <= 0 || h <= 0 || w <= 0) {
             return;
         }
         if (alpha <= 0) {
             return;
         }
         if (alpha > 255) {
             alpha = 255;
         }
         getOrBuildGradationImage(w, h, colorFrom, colorTo, steps, alpha);
     }

     private static Image getOrBuildSolidImage(int w, int h, int argb) {
         for (int i = 0; i < cachedSolidImg.length; i++) {
             if (cachedSolidImg[i] != null && cachedSolidW[i] == w && cachedSolidH[i] == h && cachedSolidArgb[i] == argb) {
                 return cachedSolidImg[i];
             }
         }

         int[] pixels;
         try {
             pixels = new int[w * h];
             for (int i = 0; i < pixels.length; i++) {
                 pixels[i] = argb;
             }
         } catch (Throwable t) {
             return null;
         }

         Image img;
         try {
             img = Image.createRGBImage(pixels, w, h, true);
         } catch (Throwable t) {
             img = null;
         }

         if (img != null) {
             int slot = -1;
             for (int i = 0; i < cachedSolidImg.length; i++) {
                 if (cachedSolidImg[i] == null) {
                     slot = i;
                     break;
                 }
             }
             if (slot < 0) {
                 if (solidCacheNext < 0 || solidCacheNext >= cachedSolidImg.length) {
                     solidCacheNext = 0;
                 }
                 slot = solidCacheNext;
                 solidCacheNext = (solidCacheNext + 1) % cachedSolidImg.length;
             }
             cachedSolidImg[slot] = img;
             cachedSolidW[slot] = w;
             cachedSolidH[slot] = h;
             cachedSolidArgb[slot] = argb;
         }
         return img;
     }

     private static Image getOrBuildGradationImage(int w, int h, int colorFrom, int colorTo, int steps, int alpha) {
        boolean cacheable = (w == 240 && ((steps == 32 && h == 240) || (steps == 24 && h > 0 && h <= 96)));

        if (cacheable) {
            for (int i = 0; i < cachedGradImg.length; i++) {
                if (cachedGradImg[i] != null && cachedGradW[i] == w && cachedGradH[i] == h && cachedGradFrom[i] == colorFrom
                        && cachedGradTo[i] == colorTo && cachedGradSteps[i] == steps && cachedGradAlpha[i] == alpha) {
                    return cachedGradImg[i];
                }
            }
        }

         int[] pixels;
         try {
             pixels = buildGradationPixels(w, h, colorFrom, colorTo, steps, alpha);
         } catch (Throwable t) {
             return null;
         }

         Image img;
         try {
             img = Image.createRGBImage(pixels, w, h, true);
         } catch (Throwable t) {
             img = null;
         }

         if (img != null && cacheable) {
            int slot = -1;
            for (int i = 0; i < cachedGradImg.length; i++) {
                if (cachedGradImg[i] == null) {
                    slot = i;
                    break;
                }
            }
            if (slot < 0) {
                slot = (gradCacheNext++) % cachedGradImg.length;
            }
            cachedGradImg[slot] = img;
            cachedGradW[slot] = w;
            cachedGradH[slot] = h;
            cachedGradFrom[slot] = colorFrom;
            cachedGradTo[slot] = colorTo;
            cachedGradSteps[slot] = steps;
            cachedGradAlpha[slot] = alpha;
        }
        return img;
    }

     private static int[] buildGradationPixels(int w, int h, int colorFrom, int colorTo, int steps, int alpha) {
         int[] pixels = new int[w * h];

         int stepH = h / steps;
         int currStepH = stepH;

         int r0 = (colorFrom >> 16) & 0xFF;
         int g0 = (colorFrom >> 8) & 0xFF;
         int b0 = (colorFrom) & 0xFF;

         int r1 = (colorTo >> 16) & 0xFF;
         int g1 = (colorTo >> 8) & 0xFF;
         int b1 = (colorTo) & 0xFF;

         int dr = Math.abs(r0 - r1) / steps;
         int dg = Math.abs(g0 - g1) / steps;
         int db = Math.abs(b0 - b1) / steps;

         int r = r0;
         int gg = g0;
         int bb = b0;
         int color = (alpha << 24) | (colorFrom & 0x00FFFFFF);

         int y = 0;
         for (int i = 0; i < steps; i++) {
             if (i == steps - 1 && stepH * i + stepH < h) {
                 currStepH += h - (stepH * i + stepH);
             }

             for (int yy = 0; yy < currStepH && y < h; yy++, y++) {
                 int rowStart = y * w;
                 for (int x = 0; x < w; x++) {
                     pixels[rowStart + x] = color;
                 }
             }

             int nr = r + ((r0 < r1) ? dr : (-dr));
             int ng = gg + ((g0 < g1) ? dg : (-dg));
             int nb = bb + ((b0 < b1) ? db : (-db));

             r = clampByte(nr);
             gg = clampByte(ng);
             bb = clampByte(nb);
             color = (alpha << 24) | (r << 16) | (gg << 8) | bb;
             currStepH = stepH;
         }

         return pixels;
     }

    // Linear color interpolation: returns blend of c0 and c1 at position t/tMax.
    public static int mixColor(int c0, int c1, int t, int tMax) {
        if (t <= 0) {
            return c1;
        }
        if (t >= tMax) {
            return c0;
        }

        int r0 = (c0 >> 16) & 0xFF;
        int g0 = (c0 >> 8) & 0xFF;
        int b0 = (c0) & 0xFF;

        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = (c1) & 0xFF;

        int inv = tMax - t;
        int r = (r0 * t + r1 * inv) / tMax;
        int gg = (g0 * t + g1 * inv) / tMax;
        int bb = (b0 * t + b1 * inv) / tMax;
        return (r << 16) | (gg << 8) | bb;
    }
}
