package touhou;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class CcTable {
    private final short[][] cc;

    private final int[] imgIndex;
    private final int[] srcX;
    private final int[] srcY;
    private final int[] w;
    private final int[] h;
    private final short[] ax;
    private final short[] ay;

    private final boolean[] hasSpriteMeta;

    private CcTable(short[][] cc) {
        this.cc = cc;

        int count = (cc != null) ? cc.length : 0;
        imgIndex = new int[count];
        srcX = new int[count];
        srcY = new int[count];
        w = new int[count];
        h = new int[count];
        ax = new short[count];
        ay = new short[count];
        hasSpriteMeta = new boolean[count];

        for (int i = 0; i < count; i++) {
            short[] row = cc[i];
            int ii = -1;
            if (row != null && row.length >= 1) {
                ii = row[0] & 0xFFFF;
            }
            imgIndex[i] = ii;

            if (row != null && row.length >= 7) {
                srcX[i] = row[1] & 0xFFFF;
                srcY[i] = row[2] & 0xFFFF;
                w[i] = row[3] & 0xFFFF;
                h[i] = row[4] & 0xFFFF;
                ax[i] = row[5];
                ay[i] = row[6];
                hasSpriteMeta[i] = true;
            } else {
                srcX[i] = 0;
                srcY[i] = 0;
                w[i] = 0;
                h[i] = 0;
                ax[i] = 0;
                ay[i] = 0;
                hasSpriteMeta[i] = false;
            }
        }
    }

    public int size() {
        return cc.length;
    }

    public short[] get(int bulletId) {
        if (bulletId < 0 || bulletId >= cc.length) {
            return null;
        }
        return cc[bulletId];
    }

    public int getImgIndex(int ccIndex) {
        if (ccIndex < 0 || ccIndex >= imgIndex.length) {
            return -1;
        }
        return imgIndex[ccIndex];
    }

    public boolean hasSpriteMeta(int ccIndex) {
        if (ccIndex < 0 || ccIndex >= hasSpriteMeta.length) {
            return false;
        }
        return hasSpriteMeta[ccIndex];
    }

    public int getSrcX(int ccIndex) {
        if (ccIndex < 0 || ccIndex >= srcX.length) {
            return 0;
        }
        return srcX[ccIndex];
    }

    public int getSrcY(int ccIndex) {
        if (ccIndex < 0 || ccIndex >= srcY.length) {
            return 0;
        }
        return srcY[ccIndex];
    }

    public int getW(int ccIndex) {
        if (ccIndex < 0 || ccIndex >= w.length) {
            return 0;
        }
        return w[ccIndex];
    }

    public int getH(int ccIndex) {
        if (ccIndex < 0 || ccIndex >= h.length) {
            return 0;
        }
        return h[ccIndex];
    }

    public int getAx(int ccIndex) {
        if (ccIndex < 0 || ccIndex >= ax.length) {
            return 0;
        }
        return ax[ccIndex];
    }

    public int getAy(int ccIndex) {
        if (ccIndex < 0 || ccIndex >= ay.length) {
            return 0;
        }
        return ay[ccIndex];
    }

    public static CcTable loadFromResource(String resourcePath) throws IOException {
        InputStream in = CcTable.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("Missing resource: " + resourcePath);
        }
        try {
            byte[] data = readAll(in);
            short[][] cc = parse(data);
            return new CcTable(cc);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
        }
        return out.toByteArray();
    }

    private static short[][] parse(byte[] data) throws IOException {
        if (data.length < 2) {
            throw new IOException("Invalid cc.dat");
        }
        int count = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        int n = 2;

        short[][] cc = new short[count][];
        int[] lens = new int[count];

        for (int i = 0; i < count; i++) {
            if (n >= data.length) {
                throw new IOException("Invalid cc.dat (length table)");
            }
            int len = data[n] & 0xFF;
            n++;
            lens[i] = len;
            cc[i] = new short[len];
        }

        for (int j = 0; j < count; j++) {
            for (int k = 0; k < lens[j]; k++) {
                if (n + 1 >= data.length) {
                    throw new IOException("Invalid cc.dat (payload)");
                }
                int v = ((data[n] & 0xFF) << 8) | (data[n + 1] & 0xFF);
                n += 2;
                cc[j][k] = (short) v;
            }
        }

        return cc;
    }
}
