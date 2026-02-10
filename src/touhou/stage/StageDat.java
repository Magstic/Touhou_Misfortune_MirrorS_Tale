package touhou.stage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class StageDat {

    private final int scriptCount;
    private final int[] stepCount;
    private final int[][] rowLen;
    private final short[][][] table;

    private StageDat(int scriptCount, int[] stepCount, int[][] rowLen, short[][][] table) {
        this.scriptCount = scriptCount;
        this.stepCount = stepCount;
        this.rowLen = rowLen;
        this.table = table;
    }

    public int getScriptCount() {
        return scriptCount;
    }

    public int getStepCount(int script) {
        return stepCount[script];
    }

    public int getRowLen(int script, int step) {
        return rowLen[script][step];
    }

    public short[][][] getTable() {
        return table;
    }

    public short[] getRow(int script, int step) {
        return table[script][step];
    }

    public static StageDat loadFromResources() {
        InputStream is = null;
        try {
            is = StageDat.class.getResourceAsStream("/res/stage.dat");
            if (is == null) {
                is = StageDat.class.getResourceAsStream("/stage.dat");
            }
            if (is == null) {
                return null;
            }
            return load(is);
        } catch (Throwable t) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
            }
            System.gc();
        }
    }

    public static StageDat load(InputStream is) throws IOException {
        if (is == null) {
            throw new NullPointerException("is");
        }

        byte[] b = readAllBytes(is);

        int n = 0;
        int scriptCount = b[n++];

        short[][][] table = new short[scriptCount][0][0];
        int[] stepCount = new int[scriptCount];

        for (int i = 0; i < table.length; i++) {
            int steps = b[n++];
            stepCount[i] = steps;
            table[i] = new short[steps][0];
        }

        int[][] rowLen = new int[scriptCount][];
        for (int j = 0; j < table.length; j++) {
            rowLen[j] = new int[table[j].length];
            for (int k = 0; k < table[j].length; k++) {
                int len = b[n++];
                rowLen[j][k] = len;
                table[j][k] = new short[len];
            }
        }

        for (int l = 0; l < table.length; l++) {
            for (int n2 = 0; n2 < table[l].length; n2++) {
                for (int n3 = 0; n3 < table[l][n2].length; n3++) {
                    table[l][n2][n3] = (short) (((b[n] & 0xFF) << 8) + (b[n + 1] & 0xFF));
                    n += 2;
                }
            }
        }

        return new StageDat(scriptCount, stepCount, rowLen, table);
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            int r = is.read();
            if (r == -1) {
                break;
            }
            baos.write(r);
        }
        return baos.toByteArray();
    }
}
