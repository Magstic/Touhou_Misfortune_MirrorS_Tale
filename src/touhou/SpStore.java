package touhou;

import javax.microedition.rms.RecordStore;

public final class SpStore {
    private static final String STORE = "th_sp";
    private static final int SEG_SIZE = 1024;
    private static final int DEFAULT_SEGMENTS = 32;

    private static byte[] cache;
    private static boolean[] dirty;
    private static int segmentCount;
    private static boolean loaded;

    private SpStore() {
    }

    public static byte[] read(int offset, int length) {
        if (offset < 0 || length <= 0) {
            return new byte[0];
        }

        try {
            ensureLoaded();
            byte[] out = new byte[length];
            if (cache == null) {
                return out;
            }

            if (offset >= cache.length) {
                return out;
            }
            int avail = cache.length - offset;
            int n = (length > avail) ? avail : length;
            if (n > 0) {
                System.arraycopy(cache, offset, out, 0, n);
            }
            return out;
        } catch (Throwable t) {
            return new byte[length];
        }
    }

    public static void write(int offset, byte[] src) {
        if (src == null || src.length == 0 || offset < 0) {
            return;
        }

        try {
            ensureLoaded();
            ensureCapacity(offset + src.length);
            if (cache == null) {
                return;
            }

            System.arraycopy(src, 0, cache, offset, src.length);
            markDirty(offset, src.length);
            flushDirty();
        } catch (Throwable t) {
        } finally {
        }
    }

    private static void ensureLoaded() throws Exception {
        if (loaded) {
            return;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            int n = rs.getNumRecords();
            if (n <= 0) {
                segmentCount = DEFAULT_SEGMENTS;
                cache = new byte[segmentCount * SEG_SIZE];
                dirty = new boolean[segmentCount];
                byte[] z = new byte[SEG_SIZE];
                for (int i = 0; i < segmentCount; i++) {
                    rs.addRecord(z, 0, z.length);
                }
                loaded = true;
                return;
            }

            segmentCount = (n > DEFAULT_SEGMENTS) ? n : DEFAULT_SEGMENTS;
            cache = new byte[segmentCount * SEG_SIZE];
            dirty = new boolean[segmentCount];

            for (int seg = 0; seg < n; seg++) {
                byte[] data = rs.getRecord(seg + 1);
                if (data == null || data.length == 0) {
                    continue;
                }
                int base = seg * SEG_SIZE;
                int len = data.length;
                if (len > SEG_SIZE) {
                    len = SEG_SIZE;
                }
                System.arraycopy(data, 0, cache, base, len);
            }

            if (n < segmentCount) {
                byte[] z = new byte[SEG_SIZE];
                while (n < segmentCount) {
                    rs.addRecord(z, 0, z.length);
                    n++;
                }
            }
            loaded = true;
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static void ensureCapacity(int minSize) {
        if (minSize <= 0) {
            return;
        }
        if (cache == null) {
            int segs = (minSize + SEG_SIZE - 1) / SEG_SIZE;
            segmentCount = (segs > DEFAULT_SEGMENTS) ? segs : DEFAULT_SEGMENTS;
            cache = new byte[segmentCount * SEG_SIZE];
            dirty = new boolean[segmentCount];
            return;
        }

        if (minSize <= cache.length) {
            return;
        }

        byte[] oldCache = cache;
        boolean[] oldDirty = dirty;
        int oldSegs = segmentCount;

        int needSegs = (minSize + SEG_SIZE - 1) / SEG_SIZE;
        int newSegs = segmentCount;
        while (newSegs < needSegs) {
            newSegs <<= 1;
            if (newSegs <= 0) {
                newSegs = needSegs;
                break;
            }
        }

        byte[] nd = new byte[newSegs * SEG_SIZE];
        System.arraycopy(cache, 0, nd, 0, cache.length);
        cache = nd;

        boolean[] ndirty = new boolean[newSegs];
        if (dirty != null) {
            System.arraycopy(dirty, 0, ndirty, 0, dirty.length);
        }
        dirty = ndirty;
        segmentCount = newSegs;

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            int n = rs.getNumRecords();
            if (n < segmentCount) {
                byte[] z = new byte[SEG_SIZE];
                while (n < segmentCount) {
                    rs.addRecord(z, 0, z.length);
                    n++;
                }
            }
        } catch (Throwable ignore) {
            cache = oldCache;
            dirty = oldDirty;
            segmentCount = oldSegs;
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static void markDirty(int offset, int length) {
        if (dirty == null || length <= 0 || offset < 0) {
            return;
        }
        int startSeg = offset / SEG_SIZE;
        int endSeg = (offset + length - 1) / SEG_SIZE;
        if (startSeg < 0) {
            startSeg = 0;
        }
        if (endSeg >= dirty.length) {
            endSeg = dirty.length - 1;
        }
        for (int seg = startSeg; seg <= endSeg; seg++) {
            dirty[seg] = true;
        }
    }

    private static void flushDirty() throws Exception {
        if (cache == null || dirty == null) {
            return;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            int n = rs.getNumRecords();
            if (n < segmentCount) {
                byte[] z = new byte[SEG_SIZE];
                while (n < segmentCount) {
                    rs.addRecord(z, 0, z.length);
                    n++;
                }
            }

            for (int seg = 0; seg < segmentCount; seg++) {
                if (!dirty[seg]) {
                    continue;
                }
                int base = seg * SEG_SIZE;
                rs.setRecord(seg + 1, cache, base, SEG_SIZE);
                dirty[seg] = false;
            }
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }
}
