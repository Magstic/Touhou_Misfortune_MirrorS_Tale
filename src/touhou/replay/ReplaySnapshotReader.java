package touhou.replay;

public final class ReplaySnapshotReader {
    private final byte[] data;
    private int pos;

    public ReplaySnapshotReader(byte[] data) {
        this.data = (data != null) ? data : new byte[0];
        this.pos = 0;
    }

    public int remaining() {
        return data.length - pos;
    }

    public int position() {
        return pos;
    }

    public void skip(int n) {
        if (n <= 0) {
            return;
        }
        pos += n;
        if (pos < 0) {
            pos = 0;
        }
        if (pos > data.length) {
            pos = data.length;
        }
    }

    public int readU8() {
        if (pos >= data.length) {
            return 0;
        }
        return data[pos++] & 0xFF;
    }

    public boolean readBool() {
        return readU8() != 0;
    }

    public int readI16LE() {
        int b0 = readU8();
        int b1 = readU8();
        return (short) ((b0 & 0xFF) | ((b1 & 0xFF) << 8));
    }

    public int readI32LE() {
        int b0 = readU8();
        int b1 = readU8();
        int b2 = readU8();
        int b3 = readU8();
        return (b0 & 0xFF) | ((b1 & 0xFF) << 8) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 24);
    }

    public long readI64LE() {
        long lo = readI32LE() & 0xFFFFFFFFL;
        long hi = readI32LE() & 0xFFFFFFFFL;
        return lo | (hi << 32);
    }

    public void readBytes(byte[] dst, int off, int len) {
        if (dst == null || len <= 0) {
            return;
        }
        if (off < 0) {
            len += off;
            off = 0;
        }
        if (off >= dst.length) {
            return;
        }
        if (off + len > dst.length) {
            len = dst.length - off;
        }

        for (int i = 0; i < len; i++) {
            dst[off + i] = (byte) (readU8() & 0xFF);
        }
    }
}
