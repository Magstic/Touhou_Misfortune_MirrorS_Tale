package touhou.replay;

import java.io.ByteArrayOutputStream;

public final class ReplaySnapshotWriter {
    private final ByteArrayOutputStream out;

    public ReplaySnapshotWriter() {
        this(1024);
    }

    public ReplaySnapshotWriter(int initialCapacity) {
        out = new ByteArrayOutputStream(initialCapacity);
    }

    public int size() {
        return out.size();
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }

    public void writeU8(int v) {
        out.write(v & 0xFF);
    }

    public void writeBool(boolean v) {
        out.write(v ? 1 : 0);
    }

    public void writeI16LE(int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
    }

    public void writeI32LE(int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
        out.write((v >> 16) & 0xFF);
        out.write((v >> 24) & 0xFF);
    }

    public void writeI64LE(long v) {
        writeI32LE((int) (v & 0xFFFFFFFFL));
        writeI32LE((int) ((v >> 32) & 0xFFFFFFFFL));
    }

    public void writeBytes(byte[] b) {
        if (b == null) {
            return;
        }
        out.write(b, 0, b.length);
    }

    public void writeBytes(byte[] b, int off, int len) {
        if (b == null || len <= 0) {
            return;
        }
        out.write(b, off, len);
    }
}
