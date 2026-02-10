package touhou;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

public final class AudioSystem implements PlayerListener {

    private static final int STATE_UNREALIZED = 100;
    private static final int STATE_PREFETCHED = 300;

    private static final int TRACK_CACHE_SIZE = 32;

    // MIDI only: load BGM from one packed resource with indexed segments.
    private static final String PACK_PREFIX = "pack:";
    private static final String PACK_PATH_MIDI = "/res/snd/bgm.dat";

    private static final int PACK_PART_COUNT = 3;
    private static final int PACK_PART_INTRO = 0;
    private static final int PACK_PART_LOOP = 1;
    private static final int PACK_PART_SINGLE = 2;

    // Offsets and lengths are read lazily from the BGMP file header.
    // Both full and simplified bgm.dat share the same code path.
    private static final BgmPack PACK_MIDI = new BgmPack(PACK_PATH_MIDI);

    // Some real devices have expensive JSR-135 Player.getState().
    // When native looping is unavailable, we only need to poll state periodically
    // to detect end-of-media and restart looping tracks.
    private static final int BGM_STATE_POLL_INTERVAL_FRAMES = 8;
    // When PlayerListener is active, getState() is only a safety net;
    // END_OF_MEDIA event drives transitions. Poll very infrequently.
    private static final int BGM_STATE_POLL_INTERVAL_FRAMES_WITH_LISTENER = 120;

    // Runtime toggle (Battle Pause screen). When disabled, never create Player.
    private boolean bgmEnabled = true;

    private int bgmVol = 50;
    private int seVol = 50;

    // Music Room can temporarily override BGM volume without touching Options.
    private boolean bgmVolOverrideActive;
    private int bgmVolOverride = 50;

    private final SoundEffectSystem se = new SoundEffectSystem();

    private int desiredTrack = -1;
    private boolean desiredLoop = true;

    private int currentTrack = -1;
    private boolean currentLoop = true;

    private boolean phaseIntro;
    private boolean phaseStarted;

    private String introPath;
    private String loopPath;
    private String singlePath;

    private final boolean[] trackResolved = new boolean[TRACK_CACHE_SIZE];
    private final String[] cachedIntroPath = new String[TRACK_CACHE_SIZE];
    private final String[] cachedLoopPath = new String[TRACK_CACHE_SIZE];
    private final String[] cachedSinglePath = new String[TRACK_CACHE_SIZE];

    private Player player;

    // Some JSR-135 implementations require keeping the InputStream open for the lifetime of Player.
    private InputStream playerStream;

    private boolean nativeLoopActive;

    private boolean bgmPaused;
    private long bgmPausedMediaTime;
    private boolean bgmPausedMediaTimeValid;

    private int bgmStatePollCnt;

    private boolean playerListenerActive;
    private boolean bgmEndOfMediaPending;

    // Pre-created loop Player: built during intro playback to avoid mid-gameplay stutter.
    private Player pendingLoopPlayer;
    private InputStream pendingLoopStream;
    private boolean pendingLoopNativeLoop;
    private boolean pendingLoopListenerActive;

    private boolean packAvailabilityChecked;
    private boolean midiPackAvailable;

    public AudioSystem() {
    }

    public void toggleBgmEnabled() {
        setBgmEnabled(!bgmEnabled);
    }

    public void setBgmEnabled(boolean enabled) {
        if (bgmEnabled == enabled) {
            return;
        }
        bgmEnabled = enabled;
        if (!bgmEnabled) {
            stopBgm();
        }
    }

    public void setVolumes(int bgmVol, int seVol) {
        int nb = clamp100(bgmVol);
        int ns = clamp100(seVol);
        if (this.bgmVol == nb && this.seVol == ns) {
            return;
        }

        this.bgmVol = nb;
        this.seVol = ns;

        se.setVolume(this.seVol);

        int eff = effectiveBgmVol();

        if (eff == 0) {
            stopBgm();
        } else {
            applyVolumeSafe(player, eff);
        }
    }

    // Apply Music Room BGM volume override.
    public void setBgmVolumeOverride(int bgmVol) {
        bgmVolOverrideActive = true;
        bgmVolOverride = clamp100(bgmVol);
        applyVolumeSafe(player, effectiveBgmVol());
    }

    public void clearBgmVolumeOverride() {
        if (!bgmVolOverrideActive) {
            return;
        }
        bgmVolOverrideActive = false;
        applyVolumeSafe(player, effectiveBgmVol());
    }

    public void setSeEnabledMask14(int mask) {
        se.setEnabledMask(mask & 0x3FFF);
    }

    public void requestBgm(int trackId, boolean loop) {
        if (trackId < 0) {
            if (desiredTrack < 0 && currentTrack < 0 && player == null && !bgmPaused) {
                return;
            }

            desiredTrack = -1;
            desiredLoop = loop;
            stopBgm();
            return;
        }
        if (desiredTrack == trackId && desiredLoop == loop) {
            return;
        }
        desiredTrack = trackId;
        desiredLoop = loop;
    }

    public void stopBgm() {
        closePendingLoopPlayer();
        closePlayer();

        currentTrack = -1;
        phaseIntro = false;
        phaseStarted = false;
        introPath = null;
        loopPath = null;
        singlePath = null;
        bgmPaused = false;
        bgmPausedMediaTimeValid = false;
        bgmStatePollCnt = 0;
        playerListenerActive = false;
        bgmEndOfMediaPending = false;
    }

    public void stopAll() {
        stopBgm();
        se.stop();
    }

    public void playSeType(int seId) {
        se.play(seId);
    }

    // Call once per frame to drive polling-based playback and intro->loop transitions.
    public void tick() {
        if (effectiveBgmVol() == 0) {
            return;
        }
        if (desiredTrack < 0) {
            return;
        }

        if (desiredTrack != currentTrack || desiredLoop != currentLoop) {
            switchToTrack(desiredTrack, desiredLoop);
        }

        if (player == null) {
            ensurePlayerCreated();
            if (player == null) {
                return;
            }
        }

        if (bgmPaused) {
            return;
        }

        // Prefer end-of-media event when supported; fall back to low-frequency polling.
        if (bgmEndOfMediaPending) {
            bgmEndOfMediaPending = false;
            bgmStatePollCnt = 0;

            if (phaseStarted) {
                if (phaseIntro) {
                    if (!switchFromIntroToLoopOrSingle()) {
                        stopBgm();
                        return;
                    }
                    return;
                }
                if (!nativeLoopActive && currentLoop) {
                    tryStart();
                    return;
                }
            }
        }

        if (nativeLoopActive && phaseStarted && !phaseIntro) {
            return;
        }

        // Throttle expensive getState() polling during BGM playback.
        // Applies to both intro and loop phases (except native loop which returns above).
        if (phaseStarted && !nativeLoopActive) {
            int interval = playerListenerActive ? BGM_STATE_POLL_INTERVAL_FRAMES_WITH_LISTENER
                    : BGM_STATE_POLL_INTERVAL_FRAMES;
            bgmStatePollCnt++;
            if (bgmStatePollCnt < interval) {
                return;
            }
            bgmStatePollCnt = 0;
        } else {
            bgmStatePollCnt = 0;
        }

        int st;
        try {
            st = player.getState();
        } catch (Throwable t) {
            closePlayer();
            return;
        }

        if (st == STATE_UNREALIZED || st == STATE_PREFETCHED) {
            // If intro was started before and we are back to PREFETCHED, treat as end-of-media.
            if (phaseIntro && phaseStarted && st == STATE_PREFETCHED) {
                // Intro ended -> switch to loop/single.
                if (!switchFromIntroToLoopOrSingle()) {
                    // No loop/single available, stop.
                    stopBgm();
                    return;
                }
                // Re-evaluate state after switching.
                return;
            }

            // Loop ended -> restart only when looping is enabled.
            if (!phaseIntro && phaseStarted && st == STATE_PREFETCHED) {
                if (nativeLoopActive) {
                    return;
                }
                if (!currentLoop) {
                    return;
                }
            }

            tryStart();
        }
    }

    private void switchToTrack(int trackId, boolean loop) {
        closePendingLoopPlayer();
        closePlayer();

        currentTrack = trackId;
        currentLoop = loop;

        resolveTrackPaths(trackId);
        phaseIntro = (introPath != null);
        phaseStarted = false;

        bgmStatePollCnt = 0;
        bgmEndOfMediaPending = false;
    }

    private void resolveTrackPaths(int trackId) {
        introPath = null;
        loopPath = null;
        singlePath = null;

        if (trackId >= 0 && trackId < TRACK_CACHE_SIZE && trackResolved[trackId]) {
            introPath = cachedIntroPath[trackId];
            loopPath = cachedLoopPath[trackId];
            singlePath = cachedSinglePath[trackId];
            return;
        }

        ensurePackAvailabilityChecked();
        if (midiPackAvailable) {
            int base = trackId * PACK_PART_COUNT;
            introPath = resolvePackEntryPath(base + PACK_PART_INTRO);
            loopPath = resolvePackEntryPath(base + PACK_PART_LOOP);
            singlePath = resolvePackEntryPath(base + PACK_PART_SINGLE);
        }

        if (trackId >= 0 && trackId < TRACK_CACHE_SIZE) {
            cachedIntroPath[trackId] = introPath;
            cachedLoopPath[trackId] = loopPath;
            cachedSinglePath[trackId] = singlePath;
            trackResolved[trackId] = true;
        }
    }

    private boolean switchFromIntroToLoopOrSingle() {
        closePlayer();
        phaseIntro = false;
        phaseStarted = false;

        // Use pre-created loop player if available (avoids mid-gameplay stutter).
        if (pendingLoopPlayer != null) {
            player = pendingLoopPlayer;
            playerStream = pendingLoopStream;
            nativeLoopActive = pendingLoopNativeLoop;
            playerListenerActive = pendingLoopListenerActive;
            pendingLoopPlayer = null;
            pendingLoopStream = null;
            pendingLoopNativeLoop = false;
            pendingLoopListenerActive = false;
            return true;
        }

        // Fallback: create player synchronously.
        if (!currentLoop && singlePath != null) {
            createPlayerFromPath(singlePath);
            return player != null;
        }
        if (loopPath != null) {
            createPlayerFromPath(loopPath);
            return player != null;
        }
        if (singlePath != null) {
            createPlayerFromPath(singlePath);
            return player != null;
        }
        return false;
    }

    private void ensurePlayerCreated() {
        if (player != null) {
            return;
        }

        // When BGM is OFF, do not create Player (avoid unnecessary overhead/stutters).
        if (effectiveBgmVol() == 0) {
            return;
        }

        String path;
        if (phaseIntro) {
            path = introPath;
        } else {
            if (!currentLoop && singlePath != null) {
                path = singlePath;
            } else if (loopPath != null) {
                path = loopPath;
            } else {
                path = singlePath;
            }
        }

        if (path == null) {
            return;
        }
        createPlayerFromPath(path);

        // Pre-create loop player while intro plays, so intro->loop switch is instant.
        if (player != null && phaseIntro) {
            precreateLoopPlayer();
        }
    }

    private void createPlayerFromPath(String path) {
        if (effectiveBgmVol() == 0) {
            return;
        }
        if (path == null) {
            return;
        }
        createPlayerFromPackPath(path);
    }

    private boolean shouldNativeLoopPath(String path) {
        if (path == null) {
            return false;
        }
        if (loopPath != null) {
            return path.equals(loopPath);
        }
        return path.equals(singlePath);
    }

    private void ensurePackAvailabilityChecked() {
        if (packAvailabilityChecked) {
            return;
        }
        packAvailabilityChecked = true;
        midiPackAvailable = resourceExists(PACK_PATH_MIDI);
    }

    private static boolean isPackPath(String path) {
        return path != null && path.startsWith(PACK_PREFIX);
    }

    private static String makePackPath(int entryIndex) {
        return PACK_PREFIX + entryIndex;
    }

    private String resolvePackEntryPath(int entryIndex) {
        if (!midiPackAvailable) {
            return null;
        }
        if (!PACK_MIDI.hasEntry(entryIndex)) {
            return null;
        }
        return makePackPath(entryIndex);
    }

    private void createPlayerFromPackPath(String path) {
        if (!isPackPath(path)) {
            return;
        }

        int entry = entryIndexFromPath(path);
        if (entry < 0) {
            return;
        }

        InputStream is = null;
        try {
            is = PACK_MIDI.openEntry(entry);
            if (is == null) {
                return;
            }

            Player p = Manager.createPlayer(is, "audio/midi");
            player = p;
            playerStream = is;
            is = null;

            playerListenerActive = false;
            try {
                player.addPlayerListener(this);
                playerListenerActive = true;
            } catch (Throwable ignore) {
                playerListenerActive = false;
            }

            nativeLoopActive = false;
            try {
                player.prefetch();
            } catch (Throwable ignore) {
            }

            boolean wantNativeLoop = !phaseIntro && currentLoop && shouldNativeLoopPath(path);
            if (wantNativeLoop) {
                try {
                    player.setLoopCount(-1);
                    nativeLoopActive = true;
                } catch (Throwable ignore) {
                    nativeLoopActive = false;
                }
            } else {
                try {
                    player.setLoopCount(1);
                } catch (Throwable ignore) {
                }
            }

            applyVolumeSafe(player, effectiveBgmVol());
        } catch (Throwable t) {
            closePlayer();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static int entryIndexFromPath(String path) {
        if (!isPackPath(path)) {
            return -1;
        }
        try {
            return Integer.parseInt(path.substring(PACK_PREFIX.length()));
        } catch (Throwable t) {
            return -1;
        }
    }

    private static final class LimitedInputStream extends InputStream {
        private final InputStream in;
        private int remaining;

        LimitedInputStream(InputStream in, int remaining) {
            this.in = in;
            this.remaining = remaining;
        }

        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int b = in.read();
            if (b >= 0) {
                remaining--;
            }
            return b;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int want = len;
            if (want > remaining) {
                want = remaining;
            }
            int n = in.read(b, off, want);
            if (n > 0) {
                remaining -= n;
            }
            return n;
        }

        public long skip(long n) throws IOException {
            if (remaining <= 0) {
                return 0;
            }
            long want = n;
            if (want > remaining) {
                want = remaining;
            }
            long s = in.skip(want);
            if (s > 0) {
                remaining -= (int) s;
            }
            return s;
        }

        public int available() throws IOException {
            int a = in.available();
            if (a > remaining) {
                return remaining;
            }
            return a;
        }

        public void close() throws IOException {
            in.close();
        }
    }

    /**
     * Reads offset/length tables from the BGMP pack file header on first access.
     * Header layout (big-endian):
     *   4B magic "BGMP", 2B version, 1B codec, 1B partCount,
     *   2B trackCount, 2B entryCount, 4B reserved,
     *   entryCount*4B offsets, entryCount*4B lengths.
     */
    private static final class BgmPack {
        final String resourcePath;

        // Lazily populated from file header
        private int trackCount;
        private int[] offsets;
        private int[] lengths;
        private boolean headerLoaded;
        private boolean headerFailed;

        BgmPack(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        private void ensureHeader() {
            if (headerLoaded || headerFailed) {
                return;
            }
            InputStream raw = null;
            try {
                raw = AudioSystem.class.getResourceAsStream(resourcePath);
                if (raw == null) {
                    headerFailed = true;
                    return;
                }
                DataInputStream dis = new DataInputStream(raw);

                // Validate magic
                int m = dis.readInt();
                if (m != 0x42474D50) { // 'B','G','M','P'
                    headerFailed = true;
                    return;
                }

                dis.readShort(); // version
                dis.readByte();  // codec
                dis.readByte();  // partCount (always 3)
                trackCount = dis.readShort() & 0xFFFF;
                int entryCount = dis.readShort() & 0xFFFF;
                dis.readInt();   // reserved

                offsets = new int[entryCount];
                lengths = new int[entryCount];
                for (int i = 0; i < entryCount; i++) {
                    offsets[i] = dis.readInt();
                }
                for (int i = 0; i < entryCount; i++) {
                    lengths[i] = dis.readInt();
                }
                headerLoaded = true;
            } catch (Throwable t) {
                headerFailed = true;
            } finally {
                if (raw != null) {
                    try { raw.close(); } catch (Throwable ignore) {}
                }
            }
        }

        boolean hasEntry(int entryIndex) {
            ensureHeader();
            if (!headerLoaded) {
                return false;
            }
            if (entryIndex < 0) {
                return false;
            }
            if (entryIndex >= trackCount * PACK_PART_COUNT) {
                return false;
            }
            if (entryIndex >= offsets.length || entryIndex >= lengths.length) {
                return false;
            }
            return offsets[entryIndex] >= 0 && lengths[entryIndex] > 0;
        }

        InputStream openEntry(int entryIndex) throws IOException {
            if (!hasEntry(entryIndex)) {
                return null;
            }

            InputStream raw = AudioSystem.class.getResourceAsStream(resourcePath);
            if (raw == null) {
                return null;
            }

            boolean ok = false;
            try {
                skipFully(raw, offsets[entryIndex]);
                InputStream lim = new LimitedInputStream(raw, lengths[entryIndex]);
                ok = true;
                return lim;
            } finally {
                if (!ok) {
                    try {
                        raw.close();
                    } catch (Throwable ignore) {
                    }
                }
            }
        }
    }

    private static void skipFully(InputStream in, int bytes) throws IOException {
        int remaining = bytes;
        while (remaining > 0) {
            long s = in.skip(remaining);
            if (s <= 0) {
                int b = in.read();
                if (b < 0) {
                    throw new IOException("EOF while skipping");
                }
                remaining--;
            } else {
                remaining -= (int) s;
            }
        }
    }

    private void tryStart() {
        if (player == null) {
            return;
        }
        try {
            player.start();
            phaseStarted = true;
            applyVolumeSafe(player, effectiveBgmVol());
        } catch (Throwable t) {
            closePlayer();
        }
    }

    // Pre-create the loop/single Player so intro->loop transition is stutter-free.
    private void precreateLoopPlayer() {
        if (pendingLoopPlayer != null) {
            return;
        }
        String path;
        if (!currentLoop && singlePath != null) {
            path = singlePath;
        } else if (loopPath != null) {
            path = loopPath;
        } else {
            path = singlePath;
        }
        if (path == null || !isPackPath(path)) {
            return;
        }

        int entry = entryIndexFromPath(path);
        if (entry < 0) {
            return;
        }

        InputStream is = null;
        try {
            is = PACK_MIDI.openEntry(entry);
            if (is == null) {
                return;
            }

            Player p = Manager.createPlayer(is, "audio/midi");
            pendingLoopPlayer = p;
            pendingLoopStream = is;
            is = null;

            pendingLoopListenerActive = false;
            try {
                pendingLoopPlayer.addPlayerListener(this);
                pendingLoopListenerActive = true;
            } catch (Throwable ignore) {
            }

            try {
                pendingLoopPlayer.prefetch();
            } catch (Throwable ignore) {
            }

            pendingLoopNativeLoop = false;
            boolean wantNativeLoop = currentLoop && shouldNativeLoopPath(path);
            if (wantNativeLoop) {
                try {
                    pendingLoopPlayer.setLoopCount(-1);
                    pendingLoopNativeLoop = true;
                } catch (Throwable ignore) {
                }
            } else {
                try {
                    pendingLoopPlayer.setLoopCount(1);
                } catch (Throwable ignore) {
                }
            }

            applyVolumeSafe(pendingLoopPlayer, effectiveBgmVol());
        } catch (Throwable t) {
            closePendingLoopPlayer();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private void closePendingLoopPlayer() {
        if (pendingLoopPlayer == null) {
            return;
        }
        InputStream ps = pendingLoopStream;
        pendingLoopStream = null;
        try {
            pendingLoopPlayer.removePlayerListener(this);
        } catch (Throwable ignore) {
        }
        try {
            pendingLoopPlayer.stop();
        } catch (Throwable ignore) {
        }
        try {
            pendingLoopPlayer.close();
        } catch (Throwable ignore) {
        }
        pendingLoopPlayer = null;
        pendingLoopNativeLoop = false;
        pendingLoopListenerActive = false;
        if (ps != null) {
            try {
                ps.close();
            } catch (Throwable ignore) {
            }
        }
    }

    private void closePlayer() {
        if (player == null) {
            return;
        }
        InputStream ps = playerStream;
        playerStream = null;
        try {
            player.removePlayerListener(this);
        } catch (Throwable ignore) {
        }

        try {
            player.stop();
        } catch (Throwable ignore) {
        }
        try {
            player.close();
        } catch (Throwable ignore) {
        }
        player = null;
        nativeLoopActive = false;
        playerListenerActive = false;
        bgmEndOfMediaPending = false;

        if (ps != null) {
            try {
                ps.close();
            } catch (Throwable ignore) {
            }
        }
    }

    private static void applyVolumeSafe(Player p, int vol) {
        if (p == null) {
            return;
        }
        int v = clamp100(vol);
        try {
            VolumeControl vc = (VolumeControl) p.getControl("VolumeControl");
            if (vc != null) {
                vc.setLevel(v);
            }
        } catch (Throwable ignore) {
        }
    }

    private static boolean resourceExists(String path) {
        InputStream is = null;
        try {
            is = AudioSystem.class.getResourceAsStream(path);
            return is != null;
        } catch (Throwable t) {
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

    private static int clamp100(int v) {
        if (v < 0) {
            return 0;
        }
        if (v > 100) {
            return 100;
        }
        return v;
    }

    private int effectiveBgmVol() {
        if (!bgmEnabled) {
            return 0;
        }
        return bgmVolOverrideActive ? bgmVolOverride : bgmVol;
    }

    // Pause BGM without closing player. Best effort resume via mediaTime.
    public void pauseBgm() {
        if (bgmPaused) {
            return;
        }
        if (player == null) {
            bgmPaused = true;
            bgmPausedMediaTimeValid = false;
            return;
        }
        bgmPaused = true;
        bgmPausedMediaTimeValid = false;
        bgmEndOfMediaPending = false;
        try {
            bgmPausedMediaTime = player.getMediaTime();
            bgmPausedMediaTimeValid = true;
        } catch (Throwable ignore) {
        }
        try {
            player.stop();
        } catch (Throwable ignore) {
        }
    }

    // Resume BGM from paused position if supported.
    public void resumeBgm() {
        if (!bgmPaused) {
            return;
        }
        bgmPaused = false;
        bgmEndOfMediaPending = false;

        if (player == null) {
            // Recreate player if needed.
            ensurePlayerCreated();
        }
        if (player == null) {
            return;
        }

        applyVolumeSafe(player, effectiveBgmVol());

        if (bgmPausedMediaTimeValid) {
            try {
                player.setMediaTime(bgmPausedMediaTime);
            } catch (Throwable ignore) {
            }
        }
        tryStart();
    }

    public void pauseAll() {
        pauseBgm();
        se.stop();
    }

    public void resumeAll() {
        resumeBgm();
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        if (player == null || player != this.player) {
            return;
        }
        if (PlayerListener.END_OF_MEDIA.equals(event)) {
            bgmEndOfMediaPending = true;
        }
    }
}