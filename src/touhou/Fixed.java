package touhou;

public final class Fixed {
    private Fixed() {
    }

    public static int fromInt(int v) {
        return v << 16;
    }

    public static int toInt(int v) {
        return v >> 16;
    }

    public static int mul(int a, int b) {
        return (int) (((long) a * (long) b) >> 16);
    }

    public static int div(int a, int b) {
        return (int) (((long) a << 16) / (long) b);
    }
}
