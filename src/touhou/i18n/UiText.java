package touhou.i18n;

public final class UiText {
    private static String[] table;
    private static int warnCount;

    private UiText() {
    }

    public static void load() {
        table = I18n.splitLinesSimple(I18n.readUtf8TextResource(I18n.path("ui.txt")));
    }

    public static String get(int id) {
        String[] t = table;
        if (t == null) {
            if (warnCount < 8) {
                warnCount++;
                System.out.println("WARN: UiText.get before load: id=" + id);
            }
            return "";
        }
        if (id < 0 || id >= t.length) {
            if (warnCount < 8) {
                warnCount++;
                System.out.println("WARN: UiText.get out of range: id=" + id + " table=" + t.length + " (check TextId vs ui.txt lines)");
            }
            return "";
        }
        String s = t[id];
        return (s != null) ? s : "";
    }
}
