package com.esafirm.imagepicker.helper

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleManager {

    var language: String = Locale.getDefault().language

    fun updateResources(context: Context): Context {
        var resultContext = context
        val locale = normalizeLocale(Locale(language))

        Locale.setDefault(locale)

        val res = resultContext.resources
        val config = Configuration(res.configuration)

        config.setLocale(locale)
        resultContext = resultContext.createConfigurationContext(config)

        return resultContext
    }

    private fun normalizeLocale(localeLanguage: Locale): Locale {
        val ZH = "zh"
        val TW = "TW"
        val CN = "CN"
        val locale: Locale
        val newLocaleLanguage = localeLanguage.toString()
        return when {
            newLocaleLanguage.length == 5 -> {
                locale = Locale(
                    newLocaleLanguage.substring(0, 2),
                    newLocaleLanguage.substring(3, 5).toUpperCase(Locale.getDefault())
                )
                locale
            }

            newLocaleLanguage == ZH -> {
                locale = if (Locale.getDefault().country == TW) {
                    Locale(ZH, TW)
                } else {
                    Locale(ZH, CN)
                }
                locale
            }

            else -> {
                localeLanguage
            }
        }
    }
}
