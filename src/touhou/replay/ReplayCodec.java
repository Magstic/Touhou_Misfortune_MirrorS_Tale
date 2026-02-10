package touhou.replay;

public final class ReplayCodec {
    public static final int FRAME_COUNT = 30720;
    public static final int ENCODED_SIZE = 7168;

    private ReplayCodec() {
    }

    // Fixed-size RLE: (len,value) pairs with len in 1..127.
    public static byte[] encode(byte[] frames) {
        byte[] out = new byte[ENCODED_SIZE];
        if (frames == null || frames.length < FRAME_COUNT) {
            return out;
        }

        int outPos = 0;
        int i = 0;
        while (i < FRAME_COUNT && (outPos + 1) < ENCODED_SIZE) {
            byte v = frames[i];
            int run = 1;
            while ((i + run) < FRAME_COUNT && run < 127 && frames[i + run] == v) {
                run++;
            }
            out[outPos++] = (byte) (run & 0xFF);
            out[outPos++] = v;
            i += run;
        }
        return out;
    }

    public static byte[] decode(byte[] encoded) {
        byte[] out = new byte[FRAME_COUNT];
        if (encoded == null || encoded.length < ENCODED_SIZE) {
            return out;
        }

        int outPos = 0;
        for (int inPos = 0; (inPos + 1) < ENCODED_SIZE && outPos < FRAME_COUNT; inPos += 2) {
            int run = encoded[inPos] & 0xFF;
            if (run <= 0) {
                break;
            }
            if (run > 127) {
                run = 127;
            }
            byte v = encoded[inPos + 1];
            int end = outPos + run;
            if (end > FRAME_COUNT) {
                end = FRAME_COUNT;
            }
            while (outPos < end) {
                out[outPos++] = v;
            }
        }
        return out;
    }
}
