package touhou.replay;

import javax.microedition.rms.RecordStore;

public final class ReplayRmsStore {
    private static final String STORE = "th_rep2";

    public static final int SLOT_COUNT = 9;

    public static final int HEADER_SIZE = ReplayHeader.SIZE;
    public static final int NAME_SIZE = 32;

    public static final int DATA_SIZE = 7168;
    private static final int DATA_SEG_SIZE = 1024;
    private static final int DATA_SEG_COUNT = DATA_SIZE / DATA_SEG_SIZE;

    private static final int FULL_DATA_OFFSET = 2;
    private static final int BOSS_DATA_OFFSET = FULL_DATA_OFFSET + DATA_SEG_COUNT;

    private static final int RECORDS_PER_SLOT = 2 + DATA_SEG_COUNT + DATA_SEG_COUNT;

    private ReplayRmsStore() {
    }

    // One-time warmup for first launch: creates required RMS records.
    public static boolean warmup() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static void saveSlot(int slot, ReplayHeader header, byte[] name32, byte[] encoded7168) {
        saveSlot2(slot, header, name32, encoded7168, null);
    }

    public static void saveSlot2(int slot, ReplayHeader header, byte[] name32, byte[] fullEncoded7168, byte[] bossEncoded7168) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        if (header == null) {
            return;
        }
        if (fullEncoded7168 == null || fullEncoded7168.length < DATA_SIZE) {
            return;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            int base = baseRecordId(slot);

            byte[] headerBytes = header.bytes();
            if (headerBytes == null || headerBytes.length < HEADER_SIZE) {
                return;
            }
            rs.setRecord(base, headerBytes, 0, HEADER_SIZE);

            byte[] name = new byte[NAME_SIZE];
            if (name32 != null) {
                int n = name32.length;
                if (n > NAME_SIZE) {
                    n = NAME_SIZE;
                }
                for (int i = 0; i < n; i++) {
                    name[i] = name32[i];
                }
            }
            rs.setRecord(base + 1, name, 0, NAME_SIZE);

            int p = 0;
            for (int seg = 0; seg < DATA_SEG_COUNT; seg++) {
                rs.setRecord(base + FULL_DATA_OFFSET + seg, fullEncoded7168, p, DATA_SEG_SIZE);
                p += DATA_SEG_SIZE;
            }

            byte[] zSeg = new byte[DATA_SEG_SIZE];
            if (bossEncoded7168 != null && bossEncoded7168.length >= DATA_SIZE) {
                p = 0;
                for (int seg = 0; seg < DATA_SEG_COUNT; seg++) {
                    rs.setRecord(base + BOSS_DATA_OFFSET + seg, bossEncoded7168, p, DATA_SEG_SIZE);
                    p += DATA_SEG_SIZE;
                }
            } else {
                for (int seg = 0; seg < DATA_SEG_COUNT; seg++) {
                    rs.setRecord(base + BOSS_DATA_OFFSET + seg, zSeg, 0, 1);
                }
            }
        } catch (Throwable t) {
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static void loadAllSlotHeadersAndNames(ReplayHeader[] slotHeaders, byte[][] slotNames) {
        if (slotHeaders == null || slotNames == null) {
            return;
        }
        if (slotHeaders.length < SLOT_COUNT || slotNames.length < SLOT_COUNT) {
            return;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                ReplayHeader headerOut = slotHeaders[slot];
                byte[] nameOut32 = slotNames[slot];
                if (headerOut == null || nameOut32 == null || nameOut32.length < NAME_SIZE) {
                    continue;
                }

                int base = baseRecordId(slot);
                byte[] hb = rs.getRecord(base);
                headerOut.clear();
                headerOut.load(hb);

                byte[] nb = rs.getRecord(base + 1);
                fillFixed(nb, nameOut32, NAME_SIZE);
            }
        } catch (Throwable t) {
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                ReplayHeader headerOut = slotHeaders[slot];
                byte[] nameOut32 = slotNames[slot];
                if (headerOut != null) {
                    headerOut.clear();
                }
                if (nameOut32 != null && nameOut32.length >= NAME_SIZE) {
                    for (int i = 0; i < NAME_SIZE; i++) {
                        nameOut32[i] = 0;
                    }
                }
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

    public static void loadSlotHeaderAndName(int slot, ReplayHeader headerOut, byte[] nameOut32) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        if (headerOut == null || nameOut32 == null || nameOut32.length < NAME_SIZE) {
            return;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            int base = baseRecordId(slot);

            byte[] hb = rs.getRecord(base);
            headerOut.clear();
            headerOut.load(hb);

            byte[] nb = rs.getRecord(base + 1);
            fillFixed(nb, nameOut32, NAME_SIZE);
        } catch (Throwable t) {
            headerOut.clear();
            for (int i = 0; i < NAME_SIZE; i++) {
                nameOut32[i] = 0;
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

    public static void loadSlot(int slot, ReplayHeader headerOut, byte[] nameOut32, byte[] encodedOut7168) {
        loadSlot2(slot, headerOut, nameOut32, encodedOut7168, null);
    }

    public static void loadSlot2(int slot, ReplayHeader headerOut, byte[] nameOut32, byte[] fullEncodedOut7168, byte[] bossEncodedOut7168) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        if (headerOut == null || nameOut32 == null || fullEncodedOut7168 == null) {
            return;
        }
        if (nameOut32.length < NAME_SIZE || fullEncodedOut7168.length < DATA_SIZE) {
            return;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            int base = baseRecordId(slot);

            byte[] hb = rs.getRecord(base);
            headerOut.clear();
            headerOut.load(hb);

            byte[] nb = rs.getRecord(base + 1);
            fillFixed(nb, nameOut32, NAME_SIZE);

            int p = 0;
            for (int seg = 0; seg < DATA_SEG_COUNT; seg++) {
                byte[] b = rs.getRecord(base + FULL_DATA_OFFSET + seg);
                int n = (b != null) ? b.length : 0;
                if (n > DATA_SEG_SIZE) {
                    n = DATA_SEG_SIZE;
                }
                if (n > 0) {
                    System.arraycopy(b, 0, fullEncodedOut7168, p, n);
                }
                while (n < DATA_SEG_SIZE) {
                    fullEncodedOut7168[p + n] = 0;
                    n++;
                }
                p += DATA_SEG_SIZE;
            }

            if (bossEncodedOut7168 != null && bossEncodedOut7168.length >= DATA_SIZE) {
                p = 0;
                for (int seg = 0; seg < DATA_SEG_COUNT; seg++) {
                    byte[] b = rs.getRecord(base + BOSS_DATA_OFFSET + seg);
                    int n = (b != null) ? b.length : 0;
                    if (n > DATA_SEG_SIZE) {
                        n = DATA_SEG_SIZE;
                    }
                    if (n > 0) {
                        System.arraycopy(b, 0, bossEncodedOut7168, p, n);
                    }
                    while (n < DATA_SEG_SIZE) {
                        bossEncodedOut7168[p + n] = 0;
                        n++;
                    }
                    p += DATA_SEG_SIZE;
                }
            }
        } catch (Throwable t) {
            headerOut.clear();
            for (int i = 0; i < NAME_SIZE; i++) {
                nameOut32[i] = 0;
            }
            for (int i = 0; i < DATA_SIZE; i++) {
                fullEncodedOut7168[i] = 0;
            }
            if (bossEncodedOut7168 != null && bossEncodedOut7168.length >= DATA_SIZE) {
                for (int i = 0; i < DATA_SIZE; i++) {
                    bossEncodedOut7168[i] = 0;
                }
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

    public static void clearSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            int base = baseRecordId(slot);

            byte[] zHeader = new byte[HEADER_SIZE];
            byte[] zName = new byte[NAME_SIZE];
            byte[] zSeg = new byte[1];

            rs.setRecord(base, zHeader, 0, zHeader.length);
            rs.setRecord(base + 1, zName, 0, zName.length);
            for (int seg = 0; seg < DATA_SEG_COUNT; seg++) {
                rs.setRecord(base + FULL_DATA_OFFSET + seg, zSeg, 0, zSeg.length);
            }
            for (int seg = 0; seg < DATA_SEG_COUNT; seg++) {
                rs.setRecord(base + BOSS_DATA_OFFSET + seg, zSeg, 0, zSeg.length);
            }
        } catch (Throwable t) {
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static int loadSlotFlag(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return 0;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            int base = baseRecordId(slot);
            byte[] hb = rs.getRecord(base);
            if (hb == null || hb.length <= 0) {
                return 0;
            }
            return hb[0] & 0xFF;
        } catch (Throwable t) {
            return 0;
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private static int baseRecordId(int slot) {
        return slot * RECORDS_PER_SLOT + 1;
    }

    private static void ensureRecordCount(RecordStore rs) throws Exception {
        if (rs == null) {
            return;
        }
        int need = SLOT_COUNT * RECORDS_PER_SLOT;
        int n = rs.getNumRecords();
        while (n < need) {
            int idx = n;
            byte[] z;
            int mod = idx % RECORDS_PER_SLOT;
            if (mod == 0) {
                z = new byte[HEADER_SIZE];
            } else if (mod == 1) {
                z = new byte[NAME_SIZE];
            } else {
                z = new byte[1];
            }
            rs.addRecord(z, 0, z.length);
            n++;
        }
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
