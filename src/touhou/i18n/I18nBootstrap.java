package touhou.i18n;

import touhou.GameOptions;
import touhou.font.BitmapFont;

public final class I18nBootstrap {
    private I18nBootstrap() {
    }

    public static void initFromOptions(int[] opt) {
        int lang = GameOptions.LANG_JA;
        if (opt != null && opt.length > GameOptions.IDX_LANGUAGE) {
            lang = opt[GameOptions.IDX_LANGUAGE];
        }

        I18n.setLanguageId(lang);
        I18nPack.loadForCurrentLanguage();
        UiText.load();
        BitmapFont.loadForCurrentLanguage();
    }
}
