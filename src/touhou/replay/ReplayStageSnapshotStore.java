package touhou.replay;

import javax.microedition.rms.RecordStore;

public final class ReplayStageSnapshotStore {
    private static final String STORE = "th_rep2_stage";

    public static final int MAX_SIZE = 64 * 1024;

    private ReplayStageSnapshotStore() {
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

    public static void loadHasSlotFlags(boolean[] hasSlotOut) {
        if (hasSlotOut == null || hasSlotOut.length < ReplayRmsStore.SLOT_COUNT) {
            return;
        }

        for (int i = 0; i < ReplayRmsStore.SLOT_COUNT; i++) {
            hasSlotOut[i] = false;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            for (int slot = 0; slot < ReplayRmsStore.SLOT_COUNT; slot++) {
                int id = recordId(slot);
                byte[] b = rs.getRecord(id);
                if (b == null || b.length < 4) {
                    continue;
                }
                int len = readI32LE(b, 0);
                hasSlotOut[slot] = (len > 0);
            }
        } catch (Throwable t) {
            for (int i = 0; i < ReplayRmsStore.SLOT_COUNT; i++) {
                hasSlotOut[i] = false;
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

    public static boolean hasSlot(int slot) {
        if (slot < 0 || slot >= ReplayRmsStore.SLOT_COUNT) {
            return false;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            int id = recordId(slot);
            byte[] b = rs.getRecord(id);
            if (b == null || b.length < 4) {
                return false;
            }
            int len = readI32LE(b, 0);
            return len > 0;
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

    public static byte[] loadSlot(int slot) {
        if (slot < 0 || slot >= ReplayRmsStore.SLOT_COUNT) {
            return null;
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            int id = recordId(slot);
            byte[] b = rs.getRecord(id);
            if (b == null || b.length < 4) {
                return null;
            }
            int len = readI32LE(b, 0);
            if (len <= 0) {
                return null;
            }
            if (len > MAX_SIZE) {
                len = MAX_SIZE;
            }
            if (b.length < 4 + len) {
                len = b.length - 4;
                if (len <= 0) {
                    return null;
                }
            }

            byte[] out = new byte[len];
            System.arraycopy(b, 4, out, 0, len);
            return out;
        } catch (Throwable t) {
            return null;
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
        saveSlot(slot, null);
    }

    public static void saveSlot(int slot, byte[] data) {
        if (slot < 0 || slot >= ReplayRmsStore.SLOT_COUNT) {
            return;
        }

        int len = 0;
        if (data != null) {
            len = data.length;
            if (len > MAX_SIZE) {
                len = MAX_SIZE;
            }
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(STORE, true);
            ensureRecordCount(rs);

            int id = recordId(slot);
            byte[] out = new byte[4 + len];
            writeI32LE(out, 0, len);
            if (data != null && len > 0) {
                System.arraycopy(data, 0, out, 4, len);
            }
            rs.setRecord(id, out, 0, out.length);
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

    private static int recordId(int slot) {
        return slot + 1;
    }

    private static void ensureRecordCount(RecordStore rs) throws Exception {
        if (rs == null) {
            return;
        }

        int need = ReplayRmsStore.SLOT_COUNT;
        int n = rs.getNumRecords();
        while (n < need) {
            byte[] z = new byte[4];
            rs.addRecord(z, 0, z.length);
            n++;
        }
    }

    private static int readI32LE(byte[] b, int p) {
        return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8) | ((b[p + 2] & 0xFF) << 16) | ((b[p + 3] & 0xFF) << 24);
    }

    private static void writeI32LE(byte[] b, int p, int v) {
        b[p] = (byte) (v & 0xFF);
        b[p + 1] = (byte) ((v >> 8) & 0xFF);
        b[p + 2] = (byte) ((v >> 16) & 0xFF);
        b[p + 3] = (byte) ((v >> 24) & 0xFF);
    }
}
