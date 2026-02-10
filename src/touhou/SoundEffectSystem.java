package touhou;

import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

public final class SoundEffectSystem {
    public static final int SE_NAV = 0;
    public static final int SE_SELECT = 1;
    public static final int SE_BACK = 2;
    public static final int SE_CD = 3;
    public static final int SE_CRASH = 4;
    public static final int SE_GRAZE = 5;
    public static final int SE_PAUSE = 6;
    public static final int SE_CRASH2 = 7;

    public static final int SE_POWER = 8;
    public static final int SE_LIFE = 9;
    public static final int SE_SHOT = 10;
    public static final int SE_ALICE_BOMB = 11;
    public static final int SE_REIMU_BOMB = 12;
    public static final int SE_MARISA_BOMB = 13;

    private static final int SE_COUNT = 14;
    private static final int STATE_STARTED = 400;

    private int volume = 50;
    private int enabledMask = 0xFF;

    // Each SE has its own independent cached Player. No shared "active player" tracking.
    private final Player[] cache = new Player[SE_COUNT];
    private final InputStream[] cacheStreams = new InputStream[SE_COUNT];
    private boolean cacheEnabled = true;

    // Fallback shared player (used when caching is disabled on incompatible devices).
    private Player sharedPlayer;
    private InputStream sharedStream;

    // CD exclusive: suppresses other SEs while the CD countdown sound plays.
    private boolean cdExclusive;

    // Per-SE debounce timestamps to reduce rapid re-triggering.
    private final long[] lastPlayMs = new long[SE_COUNT];

    // Resource existence cache to avoid repeated failed lookups.
    private final boolean[] resourceChecked = new boolean[SE_COUNT];
    private final boolean[] resourceExists = new boolean[SE_COUNT];

    public void setVolume(int vol) {
        volume = clamp100(vol);
        for (int i = 0; i < SE_COUNT; i++) {
            applyVolumeSafe(cache[i], volume);
        }
        applyVolumeSafe(sharedPlayer, volume);
    }

    public void setEnabledMask(int mask) {
        enabledMask = mask;
    }

    public void stop() {
        cdExclusive = false;
        closeAll();
    }

    public void play(int seId) {
        if (seId < 0 || seId >= SE_COUNT) {
            return;
        }
        if (volume == 0) {
            return;
        }
        if ((enabledMask & (1 << seId)) == 0) {
            return;
        }

        // Per-SE debounce to reduce rapid re-triggering.
        long now = System.currentTimeMillis();
        int interval = minIntervalMs(seId);
        if (interval > 0 && now - lastPlayMs[seId] < interval) {
            return;
        }
        lastPlayMs[seId] = now;

        // CD exclusive: only SE_CD itself can play while it's active.
        if (seId != SE_CD && isCdExclusivePlaying()) {
            return;
        }

        if (cacheEnabled) {
            if (startCached(seId)) {
                return;
            }
            // Caching failed on this device; fall back to shared player.
            cacheEnabled = false;
            closeAll();
        }

        startShared(seId);
    }

    // ---- Cached path: each SE has its own independent Player ----

    private boolean startCached(int seId) {
        if (!ensureResourceExists(seId)) {
            return true; // No resource file; skip silently.
        }

        Player p = cache[seId];
        if (p == null) {
            p = createAndCache(seId);
            if (p == null) {
                return false; // Creation failed; caller should try shared path.
            }
        }

        // Safe restart: stop → seek → start. Always use this sequence;
        // setMediaTime(0) on a STARTED Clip can deadlock in freej2me.
        try {
            p.stop();
        } catch (Throwable ignore) {
        }
        try {
            p.setMediaTime(0);
        } catch (Throwable ignore) {
        }
        try {
            p.start();
        } catch (Throwable t) {
            return false;
        }

        if (seId == SE_CD) {
            cdExclusive = true;
        }
        return true;
    }

    private Player createAndCache(int seId) {
        String path = resolvePath(seId);
        if (path == null) {
            return null;
        }

        InputStream is = null;
        try {
            is = SoundEffectSystem.class.getResourceAsStream(path);
            if (is == null) {
                return null;
            }

            Player p = createWavPlayer(is);
            if (p == null) {
                return null;
            }

            try {
                p.prefetch();
            } catch (Throwable ignore) {
            }
            applyVolumeSafe(p, volume);
            cache[seId] = p;
            cacheStreams[seId] = is;
            is = null; // Prevent close in finally.
            return p;
        } catch (Throwable t) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    // ---- Shared path: single player for devices that can't cache ----

    private void startShared(int seId) {
        closeShared();

        if (!ensureResourceExists(seId)) {
            return;
        }
        String path = resolvePath(seId);
        if (path == null) {
            return;
        }

        InputStream is = null;
        try {
            is = SoundEffectSystem.class.getResourceAsStream(path);
            if (is == null) {
                return;
            }

            Player p = createWavPlayer(is);
            if (p == null) {
                return;
            }

            sharedPlayer = p;
            sharedStream = is;
            is = null;

            try {
                p.prefetch();
            } catch (Throwable ignore) {
            }
            applyVolumeSafe(p, volume);

            try {
                p.start();
            } catch (Throwable t) {
                closeShared();
                return;
            }

            if (seId == SE_CD) {
                cdExclusive = true;
            }
        } catch (Throwable t) {
            closeShared();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    // ---- CD exclusive ----

    private boolean isCdExclusivePlaying() {
        if (!cdExclusive) {
            return false;
        }

        // Check the cached CD player first, then the shared player.
        Player p = cache[SE_CD];
        if (p == null) {
            p = sharedPlayer;
        }
        if (p == null) {
            cdExclusive = false;
            return false;
        }

        try {
            if (p.getState() == STATE_STARTED) {
                return true;
            }
        } catch (Throwable ignore) {
        }

        cdExclusive = false;
        return false;
    }

    // ---- Debounce ----

    private static int minIntervalMs(int seId) {
        switch (seId) {
            case SE_GRAZE: return 80;
            case SE_NAV:   return 60;
            case SE_SHOT:  return 40;
            default:       return 0;
        }
    }

    // ---- Resource helpers ----

    private static String resolvePath(int seId) {
        switch (seId) {
            case SE_NAV:         return "/res/se/nav.wav";
            case SE_SELECT:      return "/res/se/select.wav";
            case SE_BACK:        return "/res/se/back.wav";
            case SE_CD:          return "/res/se/cd.wav";
            case SE_CRASH:       return "/res/se/crash.wav";
            case SE_GRAZE:       return "/res/se/graze.wav";
            case SE_PAUSE:       return "/res/se/pause.wav";
            case SE_CRASH2:      return "/res/se/crash2.wav";
            case SE_POWER:       return "/res/se/power.wav";
            case SE_LIFE:        return "/res/se/life.wav";
            case SE_SHOT:        return "/res/se/shot.wav";
            case SE_ALICE_BOMB:  return "/res/se/alice.wav";
            case SE_REIMU_BOMB:  return "/res/se/reimu.wav";
            case SE_MARISA_BOMB: return "/res/se/marisa.wav";
            default:             return null;
        }
    }

    private boolean ensureResourceExists(int seId) {
        if (resourceChecked[seId]) {
            return resourceExists[seId];
        }
        resourceChecked[seId] = true;

        String path = resolvePath(seId);
        if (path == null) {
            resourceExists[seId] = false;
            return false;
        }

        InputStream is = null;
        try {
            is = SoundEffectSystem.class.getResourceAsStream(path);
            resourceExists[seId] = (is != null);
            return resourceExists[seId];
        } catch (Throwable t) {
            resourceExists[seId] = false;
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static Player createWavPlayer(InputStream is) {
        try {
            return Manager.createPlayer(is, "audio/x-wav");
        } catch (Throwable t0) {
            try {
                return Manager.createPlayer(is, "audio/wav");
            } catch (Throwable t1) {
                try {
                    return Manager.createPlayer(is, "audio/wave");
                } catch (Throwable t2) {
                    return null;
                }
            }
        }
    }

    // ---- Cleanup ----

    private void closeShared() {
        Player p = sharedPlayer;
        sharedPlayer = null;
        InputStream is = sharedStream;
        sharedStream = null;

        safeClose(p);
        safeCloseStream(is);
    }

    private void closeAll() {
        for (int i = 0; i < SE_COUNT; i++) {
            Player p = cache[i];
            cache[i] = null;
            safeClose(p);

            InputStream is = cacheStreams[i];
            cacheStreams[i] = null;
            safeCloseStream(is);
        }

        closeShared();
    }

    private static void safeClose(Player p) {
        if (p == null) {
            return;
        }
        try {
            p.stop();
        } catch (Throwable ignore) {
        }
        try {
            p.close();
        } catch (Throwable ignore) {
        }
    }

    private static void safeCloseStream(InputStream is) {
        if (is == null) {
            return;
        }
        try {
            is.close();
        } catch (Throwable ignore) {
        }
    }

    private static void applyVolumeSafe(Player p, int vol) {
        if (p == null) {
            return;
        }
        try {
            VolumeControl vc = (VolumeControl) p.getControl("VolumeControl");
            if (vc != null) {
                vc.setLevel(vol);
            }
        } catch (Throwable ignore) {
        }
    }

    private static int clamp100(int v) {
        if (v < 0) {
            return 0;
        }
        if (v > 100) {
            return 100;
        }
        return v;
    }
}
