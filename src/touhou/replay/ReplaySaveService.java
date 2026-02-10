package touhou.replay;

public final class ReplaySaveService {
    public static final int NAME_SIZE = ReplayRmsStore.NAME_SIZE;

    private static final class Pending {
        private boolean armed;
        private final ReplayHeader header = new ReplayHeader();
        private final byte[] name32 = new byte[NAME_SIZE];
        private final byte[] full7168 = new byte[ReplayRmsStore.DATA_SIZE];
        private final byte[] boss7168 = new byte[ReplayRmsStore.DATA_SIZE];
        private byte[] bossSnapshot;
        private byte[] stageSnapshot;

        private void clear() {
            armed = false;
            header.clear();
            for (int i = 0; i < name32.length; i++) {
                name32[i] = 0;
            }
            for (int i = 0; i < full7168.length; i++) {
                full7168[i] = 0;
            }
            for (int i = 0; i < boss7168.length; i++) {
                boss7168[i] = 0;
            }
            bossSnapshot = null;
            stageSnapshot = null;
        }
    }

    private static final Pending pending = new Pending();

    private ReplaySaveService() {
    }

    public static boolean hasPending() {
        return pending.armed;
    }

    public static void clearPending() {
        pending.clear();
    }

    public static boolean armPending(ReplayHeader header, byte[] name32, ReplayStageRecorder recorder) {
        return armPending(header, name32, recorder, null);
    }

    public static boolean armPending(ReplayHeader header, byte[] name32, ReplayStageRecorder recorder, byte[] bossSnapshot) {
        return armPending(header, name32, recorder, bossSnapshot, null);
    }

    public static boolean armPending(ReplayHeader header, byte[] name32, ReplayStageRecorder recorder, byte[] bossSnapshot, byte[] stageSnapshot) {
        if (header == null || recorder == null) {
            return false;
        }

        byte[] full = recorder.encodeFull();
        byte[] boss = recorder.encodeBossOnly();
        if (full == null || full.length < ReplayRmsStore.DATA_SIZE) {
            return false;
        }
        if (boss == null || boss.length < ReplayRmsStore.DATA_SIZE) {
            return false;
        }

        pending.header.clear();
        pending.header.load(header.bytes());

        fillFixed(name32, pending.name32, NAME_SIZE);
        fillFixed(full, pending.full7168, ReplayRmsStore.DATA_SIZE);
        fillFixed(boss, pending.boss7168, ReplayRmsStore.DATA_SIZE);

        pending.bossSnapshot = bossSnapshot;
        pending.stageSnapshot = stageSnapshot;

        pending.armed = true;
        return true;
    }

    public static boolean savePendingToSlot(int slot) {
        if (!pending.armed) {
            return false;
        }
        try {
            ReplayRmsStore.saveSlot2(slot, pending.header, pending.name32, pending.full7168, pending.boss7168);
            ReplayBossSnapshotStore.saveSlot(slot, pending.bossSnapshot);
            ReplayStageSnapshotStore.saveSlot(slot, pending.stageSnapshot);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean saveToSlot2(int slot, ReplayHeader header, byte[] name32, ReplayStageRecorder recorder) {
        if (header == null || recorder == null) {
            return false;
        }
        try {
            byte[] full = recorder.encodeFull();
            byte[] boss = recorder.encodeBossOnly();
            if (full == null || full.length < ReplayRmsStore.DATA_SIZE) {
                return false;
            }
            if (boss == null || boss.length < ReplayRmsStore.DATA_SIZE) {
                return false;
            }
            ReplayRmsStore.saveSlot2(slot, header, name32, full, boss);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean saveToSlot2(int slot, ReplayStageRecorder recorder, int flag, int difficulty, int chara, int stage, int score, int type, int mode, int spellId,
            byte[] name32) {
        if (recorder == null) {
            return false;
        }
        ReplayHeader h = new ReplayHeader();
        recorder.fillHeaderForSave(h, flag, difficulty, chara, stage, score, type, mode, spellId);
        return saveToSlot2(slot, h, name32, recorder);
    }

    private static void fillFixed(byte[] src, byte[] dst, int len) {
        if (dst == null || len <= 0) {
            return;
        }
        int n = 0;
        if (src != null) {
            n = src.length;
            if (n > len) {
                n = len;
            }
            if (n > 0) {
                System.arraycopy(src, 0, dst, 0, n);
            }
        }
        while (n < len) {
            dst[n++] = 0;
        }
    }
}
