package touhou.replay;

public final class ReplayShareCodec {
    public static final int MAGIC_THRP = 0x50524854; // 'THRP' little-endian
    public static final int VERSION_1 = 1;
    public static final int VERSION_2 = 2;

    public static final int FLAG_HAS_BOSS_DATA = 0x0001;
    public static final int FLAG_HAS_STAGE_SNAPSHOT = 0x0002;
    public static final int FLAG_HAS_BOSS_SNAPSHOT = 0x0004;

    private ReplayShareCodec() {
    }

    public static final class Decoded {
        public final int version;
        public final int flags;
        public final ReplayHeader header;
        public final byte[] name32;
        public final byte[] full7168;
        public final byte[] boss7168;
        public final byte[] stageSnapshot;
        public final byte[] bossSnapshot;

        public Decoded(int version, int flags, ReplayHeader header, byte[] name32, byte[] full7168, byte[] boss7168, byte[] stageSnapshot, byte[] bossSnapshot) {
            this.version = version;
            this.flags = flags;
            this.header = header;
            this.name32 = name32;
            this.full7168 = full7168;
            this.boss7168 = boss7168;
            this.stageSnapshot = stageSnapshot;
            this.bossSnapshot = bossSnapshot;
        }
    }

    public static byte[] encodeV2(ReplayHeader header, byte[] name32, byte[] full7168, byte[] boss7168, byte[] stageSnapshot, byte[] bossSnapshot) {
        if (header == null || name32 == null || full7168 == null) {
            return null;
        }
        if (name32.length < ReplayRmsStore.NAME_SIZE) {
            return null;
        }
        if (full7168.length < ReplayRmsStore.DATA_SIZE) {
            return null;
        }

        int flags = 0;
        int bossLen = 0;
        if (boss7168 != null && boss7168.length >= ReplayRmsStore.DATA_SIZE) {
            flags |= FLAG_HAS_BOSS_DATA;
            bossLen = ReplayRmsStore.DATA_SIZE;
        }

        int stageLen = 0;
        if (stageSnapshot != null && stageSnapshot.length > 0) {
            flags |= FLAG_HAS_STAGE_SNAPSHOT;
            stageLen = stageSnapshot.length;
        }

        int bossSnapLen = 0;
        if (bossSnapshot != null && bossSnapshot.length > 0) {
            flags |= FLAG_HAS_BOSS_SNAPSHOT;
            bossSnapLen = bossSnapshot.length;
        }

        int headerLen = ReplayHeader.SIZE;
        int nameLen = ReplayRmsStore.NAME_SIZE;
        int fullLen = ReplayRmsStore.DATA_SIZE;

        int fixed = 4 + 2 + 2 + 6 * 4;
        int total = fixed + headerLen + nameLen + fullLen + bossLen + stageLen + bossSnapLen;
        byte[] out = new byte[total];

        int p = 0;
        writeI32LE(out, p, MAGIC_THRP);
        p += 4;
        writeI16LE(out, p, VERSION_2);
        p += 2;
        writeI16LE(out, p, flags);
        p += 2;

        writeI32LE(out, p, headerLen);
        p += 4;
        writeI32LE(out, p, nameLen);
        p += 4;
        writeI32LE(out, p, fullLen);
        p += 4;
        writeI32LE(out, p, bossLen);
        p += 4;
        writeI32LE(out, p, stageLen);
        p += 4;
        writeI32LE(out, p, bossSnapLen);
        p += 4;

        byte[] hb = header.bytes();
        if (hb == null || hb.length < ReplayHeader.SIZE) {
            return null;
        }
        System.arraycopy(hb, 0, out, p, headerLen);
        p += headerLen;

        System.arraycopy(name32, 0, out, p, nameLen);
        p += nameLen;

        System.arraycopy(full7168, 0, out, p, fullLen);
        p += fullLen;

        if (bossLen > 0) {
            System.arraycopy(boss7168, 0, out, p, bossLen);
            p += bossLen;
        }

        if (stageLen > 0) {
            System.arraycopy(stageSnapshot, 0, out, p, stageLen);
            p += stageLen;
        }

        if (bossSnapLen > 0) {
            System.arraycopy(bossSnapshot, 0, out, p, bossSnapLen);
            p += bossSnapLen;
        }

        return out;
    }

    public static byte[] encodeV1(ReplayHeader header, byte[] name32, byte[] full7168, byte[] boss7168, byte[] stageSnapshot, byte[] bossSnapshot) {
        if (header == null || name32 == null || full7168 == null) {
            return null;
        }
        if (name32.length < ReplayRmsStore.NAME_SIZE) {
            return null;
        }
        if (full7168.length < ReplayRmsStore.DATA_SIZE) {
            return null;
        }

        int flags = 0;
        int bossLen = 0;
        if (boss7168 != null && boss7168.length >= ReplayRmsStore.DATA_SIZE) {
            flags |= FLAG_HAS_BOSS_DATA;
            bossLen = ReplayRmsStore.DATA_SIZE;
        }

        int stageLen = 0;
        if (stageSnapshot != null && stageSnapshot.length > 0) {
            flags |= FLAG_HAS_STAGE_SNAPSHOT;
            stageLen = stageSnapshot.length;
        }

        int bossSnapLen = 0;
        if (bossSnapshot != null && bossSnapshot.length > 0) {
            flags |= FLAG_HAS_BOSS_SNAPSHOT;
            bossSnapLen = bossSnapshot.length;
        }

        int headerLen = 32;
        int nameLen = ReplayRmsStore.NAME_SIZE;
        int fullLen = ReplayRmsStore.DATA_SIZE;

        int fixed = 4 + 2 + 2 + 6 * 4;
        int total = fixed + headerLen + nameLen + fullLen + bossLen + stageLen + bossSnapLen;
        byte[] out = new byte[total];

        int p = 0;
        writeI32LE(out, p, MAGIC_THRP);
        p += 4;
        writeI16LE(out, p, VERSION_1);
        p += 2;
        writeI16LE(out, p, flags);
        p += 2;

        writeI32LE(out, p, headerLen);
        p += 4;
        writeI32LE(out, p, nameLen);
        p += 4;
        writeI32LE(out, p, fullLen);
        p += 4;
        writeI32LE(out, p, bossLen);
        p += 4;
        writeI32LE(out, p, stageLen);
        p += 4;
        writeI32LE(out, p, bossSnapLen);
        p += 4;

        byte[] hb = header.bytes();
        if (hb == null || hb.length < headerLen) {
            return null;
        }
        System.arraycopy(hb, 0, out, p, headerLen);
        p += headerLen;

        System.arraycopy(name32, 0, out, p, nameLen);
        p += nameLen;

        System.arraycopy(full7168, 0, out, p, fullLen);
        p += fullLen;

        if (bossLen > 0) {
            System.arraycopy(boss7168, 0, out, p, bossLen);
            p += bossLen;
        }

        if (stageLen > 0) {
            System.arraycopy(stageSnapshot, 0, out, p, stageLen);
            p += stageLen;
        }

        if (bossSnapLen > 0) {
            System.arraycopy(bossSnapshot, 0, out, p, bossSnapLen);
            p += bossSnapLen;
        }

        return out;
    }

    public static final class DecodedV1 {
        public final int flags;
        public final ReplayHeader header;
        public final byte[] name32;
        public final byte[] full7168;
        public final byte[] boss7168;
        public final byte[] stageSnapshot;
        public final byte[] bossSnapshot;

        public DecodedV1(int flags, ReplayHeader header, byte[] name32, byte[] full7168, byte[] boss7168, byte[] stageSnapshot, byte[] bossSnapshot) {
            this.flags = flags;
            this.header = header;
            this.name32 = name32;
            this.full7168 = full7168;
            this.boss7168 = boss7168;
            this.stageSnapshot = stageSnapshot;
            this.bossSnapshot = bossSnapshot;
        }
    }

    public static DecodedV1 decodeV1(byte[] b) {
        if (b == null) {
            return null;
        }
        int p = 0;
        if (b.length < 4 + 2 + 2 + 6 * 4) {
            return null;
        }

        int magic = readI32LE(b, p);
        p += 4;
        if (magic != MAGIC_THRP) {
            return null;
        }

        int ver = readU16LE(b, p);
        p += 2;
        if (ver != VERSION_1) {
            return null;
        }

        int flags = readU16LE(b, p);
        p += 2;

        int headerLen = readI32LE(b, p);
        p += 4;
        int nameLen = readI32LE(b, p);
        p += 4;
        int fullLen = readI32LE(b, p);
        p += 4;
        int bossLen = readI32LE(b, p);
        p += 4;
        int stageLen = readI32LE(b, p);
        p += 4;
        int bossSnapLen = readI32LE(b, p);
        p += 4;

        if (headerLen != 32) {
            return null;
        }
        if (nameLen != ReplayRmsStore.NAME_SIZE) {
            return null;
        }
        if (fullLen != ReplayRmsStore.DATA_SIZE) {
            return null;
        }
        if (bossLen != 0 && bossLen != ReplayRmsStore.DATA_SIZE) {
            return null;
        }
        if (stageLen < 0 || stageLen > ReplayStageSnapshotStore.MAX_SIZE) {
            return null;
        }
        if (bossSnapLen < 0 || bossSnapLen > ReplayBossSnapshotStore.MAX_SIZE) {
            return null;
        }

        int need = (4 + 2 + 2 + 6 * 4) + headerLen + nameLen + fullLen + bossLen + stageLen + bossSnapLen;
        if (b.length < need) {
            return null;
        }

        ReplayHeader header = new ReplayHeader();
        header.load(sliceCopy(b, p, headerLen));
        p += headerLen;

        byte[] name32 = sliceCopy(b, p, nameLen);
        p += nameLen;

        byte[] full7168 = sliceCopy(b, p, fullLen);
        p += fullLen;

        byte[] boss7168 = null;
        if (bossLen > 0) {
            boss7168 = sliceCopy(b, p, bossLen);
            p += bossLen;
        }

        byte[] stageSnapshot = null;
        if (stageLen > 0) {
            stageSnapshot = sliceCopy(b, p, stageLen);
            p += stageLen;
        }

        byte[] bossSnapshot = null;
        if (bossSnapLen > 0) {
            bossSnapshot = sliceCopy(b, p, bossSnapLen);
            p += bossSnapLen;
        }

        return new DecodedV1(flags, header, name32, full7168, boss7168, stageSnapshot, bossSnapshot);
    }

    public static Decoded decode(byte[] b) {
        if (b == null) {
            return null;
        }
        int p = 0;
        if (b.length < 4 + 2 + 2 + 6 * 4) {
            return null;
        }

        int magic = readI32LE(b, p);
        p += 4;
        if (magic != MAGIC_THRP) {
            return null;
        }

        int ver = readU16LE(b, p);
        p += 2;
        if (ver != VERSION_1 && ver != VERSION_2) {
            return null;
        }

        int flags = readU16LE(b, p);
        p += 2;

        int headerLen = readI32LE(b, p);
        p += 4;
        int nameLen = readI32LE(b, p);
        p += 4;
        int fullLen = readI32LE(b, p);
        p += 4;
        int bossLen = readI32LE(b, p);
        p += 4;
        int stageLen = readI32LE(b, p);
        p += 4;
        int bossSnapLen = readI32LE(b, p);
        p += 4;

        if (headerLen != 32 && headerLen != ReplayHeader.SIZE) {
            return null;
        }
        if (nameLen != ReplayRmsStore.NAME_SIZE) {
            return null;
        }
        if (fullLen != ReplayRmsStore.DATA_SIZE) {
            return null;
        }
        if (bossLen != 0 && bossLen != ReplayRmsStore.DATA_SIZE) {
            return null;
        }
        if (stageLen < 0 || stageLen > ReplayStageSnapshotStore.MAX_SIZE) {
            return null;
        }
        if (bossSnapLen < 0 || bossSnapLen > ReplayBossSnapshotStore.MAX_SIZE) {
            return null;
        }

        int need = (4 + 2 + 2 + 6 * 4) + headerLen + nameLen + fullLen + bossLen + stageLen + bossSnapLen;
        if (b.length < need) {
            return null;
        }

        ReplayHeader header = new ReplayHeader();
        header.load(sliceCopy(b, p, headerLen));
        p += headerLen;

        byte[] name32 = sliceCopy(b, p, nameLen);
        p += nameLen;

        byte[] full7168 = sliceCopy(b, p, fullLen);
        p += fullLen;

        byte[] boss7168 = null;
        if (bossLen > 0) {
            boss7168 = sliceCopy(b, p, bossLen);
            p += bossLen;
        }

        byte[] stageSnapshot = null;
        if (stageLen > 0) {
            stageSnapshot = sliceCopy(b, p, stageLen);
            p += stageLen;
        }

        byte[] bossSnapshot = null;
        if (bossSnapLen > 0) {
            bossSnapshot = sliceCopy(b, p, bossSnapLen);
            p += bossSnapLen;
        }

        return new Decoded(ver, flags, header, name32, full7168, boss7168, stageSnapshot, bossSnapshot);
    }

    private static byte[] sliceCopy(byte[] b, int off, int len) {
        if (len <= 0) {
            return new byte[0];
        }
        byte[] out = new byte[len];
        System.arraycopy(b, off, out, 0, len);
        return out;
    }

    private static int readU16LE(byte[] b, int p) {
        return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8);
    }

    private static int readI32LE(byte[] b, int p) {
        return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8) | ((b[p + 2] & 0xFF) << 16) | ((b[p + 3] & 0xFF) << 24);
    }

    private static void writeI16LE(byte[] b, int p, int v) {
        b[p] = (byte) (v & 0xFF);
        b[p + 1] = (byte) ((v >> 8) & 0xFF);
    }

    private static void writeI32LE(byte[] b, int p, int v) {
        b[p] = (byte) (v & 0xFF);
        b[p + 1] = (byte) ((v >> 8) & 0xFF);
        b[p + 2] = (byte) ((v >> 16) & 0xFF);
        b[p + 3] = (byte) ((v >> 24) & 0xFF);
    }
}
