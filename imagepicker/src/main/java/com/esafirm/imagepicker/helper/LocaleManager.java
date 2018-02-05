package com.esafirm.imagepicker.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleManager {

    private static String language;

    public static void setLanguange(String newLanguage) {
        language = newLanguage;
    }

    private static String getLanguage() {
        return language != null && !language.isEmpty()
                ? language
                : Locale.getDefault().getLanguage();
    }

    public static Context updateResources(Context context) {
        Locale locale = new Locale(getLanguage());
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
}
