package com.rickb.imagepicker.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;

public class LocaleManager {

    private static String language;

    public static void setLanguage(String newLanguage) {
        language = newLanguage;
    }

    private static String getLanguage() {
        return language != null && !language.isEmpty()
                ? language
                : Locale.getDefault().getLanguage();
    }

    public static Context updateResources(Context context) {

        Locale locale = new Locale(getLanguage());
        locale = normalizeLocale(locale);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }

    private static Locale normalizeLocale(Locale localeLanguage) {
        final String ZH = "zh";
        final String TW = "TW";
        final String CN = "CN";

        Locale locale;
        String newLocaleLanguage = String.valueOf(localeLanguage);

        if (newLocaleLanguage.length() == 5) {
            locale = new Locale(
                    newLocaleLanguage.substring(0, 2),
                    newLocaleLanguage.substring(3, 5).toUpperCase()
            );
            return locale;
        } else if (newLocaleLanguage.equals(ZH)) {
            if (Locale.getDefault().getCountry().equals(TW)) {
                locale = new Locale(ZH, TW);
            } else {
                locale = new Locale(ZH, CN);
            }
            return locale;
        } else {
            return localeLanguage;
        }
    }
}
