package touhou.replay;

public final class ReplayShareService {
    // Share packet is a portable binary format, not tied to RMS layout.
    private ReplayShareService() {
    }

    public static byte[] exportSlotPacket(int slot) {
        return exportSlotPacketV2(slot);
    }

    public static byte[] exportSlotPacketV2(int slot) {
        if (slot < 0 || slot >= ReplayRmsStore.SLOT_COUNT) {
            return null;
        }

        ReplayHeader h = new ReplayHeader();
        byte[] name32 = new byte[ReplayRmsStore.NAME_SIZE];
        byte[] full = new byte[ReplayRmsStore.DATA_SIZE];
        byte[] boss = new byte[ReplayRmsStore.DATA_SIZE];

        ReplayRmsStore.loadSlot2(slot, h, name32, full, boss);
        if (h.getFlag() == ReplayHeader.FLAG_NONE) {
            return null;
        }

        byte[] stageSnap = ReplayStageSnapshotStore.loadSlot(slot);
        byte[] bossSnap = ReplayBossSnapshotStore.loadSlot(slot);

        boolean hasBossData = hasNonZero(boss);
        byte[] boss7168 = hasBossData ? boss : null;

        return ReplayShareCodec.encodeV2(h, name32, full, boss7168, stageSnap, bossSnap);
    }

    public static byte[] exportSlotPacketV1(int slot) {
        if (slot < 0 || slot >= ReplayRmsStore.SLOT_COUNT) {
            return null;
        }

        ReplayHeader h = new ReplayHeader();
        byte[] name32 = new byte[ReplayRmsStore.NAME_SIZE];
        byte[] full = new byte[ReplayRmsStore.DATA_SIZE];
        byte[] boss = new byte[ReplayRmsStore.DATA_SIZE];

        ReplayRmsStore.loadSlot2(slot, h, name32, full, boss);
        if (h.getFlag() == ReplayHeader.FLAG_NONE) {
            return null;
        }

        byte[] stageSnap = ReplayStageSnapshotStore.loadSlot(slot);
        byte[] bossSnap = ReplayBossSnapshotStore.loadSlot(slot);

        boolean hasBossData = hasNonZero(boss);
        byte[] boss7168 = hasBossData ? boss : null;

        return ReplayShareCodec.encodeV1(h, name32, full, boss7168, stageSnap, bossSnap);
    }

    public static boolean importToSlotFromPacket(int slot, byte[] packet) {
        if (slot < 0 || slot >= ReplayRmsStore.SLOT_COUNT) {
            return false;
        }
        ReplayShareCodec.Decoded d = ReplayShareCodec.decode(packet);
        if (d == null || d.header == null || d.name32 == null || d.full7168 == null) {
            return false;
        }

        // Mark as imported.
        d.header.setFlag(ReplayHeader.FLAG_IMPORTED);

        ReplayRmsStore.saveSlot2(slot, d.header, d.name32, d.full7168, d.boss7168);

        if (d.stageSnapshot != null && d.stageSnapshot.length > 0) {
            ReplayStageSnapshotStore.saveSlot(slot, d.stageSnapshot);
        } else {
            ReplayStageSnapshotStore.clearSlot(slot);
        }

        if (d.bossSnapshot != null && d.bossSnapshot.length > 0) {
            ReplayBossSnapshotStore.saveSlot(slot, d.bossSnapshot);
        } else {
            ReplayBossSnapshotStore.clearSlot(slot);
        }

        return true;
    }

    private static boolean hasNonZero(byte[] b) {
        if (b == null) {
            return false;
        }
        for (int i = 0; i < b.length; i++) {
            if (b[i] != 0) {
                return true;
            }
        }
        return false;
    }
}
