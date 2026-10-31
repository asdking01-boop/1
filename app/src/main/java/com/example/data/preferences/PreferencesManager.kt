package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_iptv_prefs", Context.MODE_PRIVATE)

    // Language state: "ar" (default) or "en"
    private val _appLanguage = MutableStateFlow(
        prefs.getString(KEY_LANGUAGE, "ar") ?: "ar"
    )
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Aspect ratio: "FIT", "FILL", "ZOOM", "STRETCH", "ORIGINAL"
    private val _defaultAspectRatio = MutableStateFlow(
        prefs.getString(KEY_ASPECT_RATIO, "FIT") ?: "FIT"
    )
    val defaultAspectRatio: StateFlow<String> = _defaultAspectRatio.asStateFlow()

    // Auto-play next episode
    private val _autoPlayNextEpisode = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_PLAY_NEXT, true)
    )
    val autoPlayNextEpisode: StateFlow<Boolean> = _autoPlayNextEpisode.asStateFlow()

    fun setAppLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _appLanguage.value = lang
    }

    fun setDefaultAspectRatio(ratio: String) {
        prefs.edit().putString(KEY_ASPECT_RATIO, ratio).apply()
        _defaultAspectRatio.value = ratio
    }

    fun setAutoPlayNextEpisode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY_NEXT, enabled).apply()
        _autoPlayNextEpisode.value = enabled
    }

    companion object {
        private const val KEY_LANGUAGE = "key_app_language"
        private const val KEY_ASPECT_RATIO = "key_aspect_ratio"
        private const val KEY_AUTO_PLAY_NEXT = "key_auto_play_next"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
