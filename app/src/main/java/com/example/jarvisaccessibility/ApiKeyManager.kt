package com.example.jarvisaccessibility

import android.content.Context

class ApiKeyManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_api_keys", Context.MODE_PRIVATE)

    fun saveOpenAiKey(apiKey: String): String {
        val cleanKey = apiKey.trim()

        if (cleanKey.isBlank()) {
            return "API key este gol."
        }

        prefs.edit()
            .putString("openai_api_key", cleanKey)
            .apply()

        return "API key salvat."
    }

    fun getOpenAiKey(): String {
        return prefs.getString("openai_api_key", "") ?: ""
    }

    fun hasOpenAiKey(): Boolean {
        return getOpenAiKey().isNotBlank()
    }

    fun clearOpenAiKey(): String {
        prefs.edit()
            .remove("openai_api_key")
            .apply()

        return "API key șters."
    }
}
