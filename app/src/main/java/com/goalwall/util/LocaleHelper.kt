package com.goalwall.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.goalwall.data.UserPreferences

object LocaleHelper {
    fun applyLanguage(language: String) {
        val locales =
            when (language) {
                UserPreferences.LANGUAGE_ZH -> LocaleListCompat.forLanguageTags("zh-CN")
                UserPreferences.LANGUAGE_EN -> LocaleListCompat.forLanguageTags("en")
                else -> LocaleListCompat.getEmptyLocaleList()
            }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
