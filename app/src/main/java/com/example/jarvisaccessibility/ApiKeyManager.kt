package com.example.jarvisaccessibility

import android.content.Context

class ApiKeyManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_api_keys", Context.MODE_PRIVATE)

    fun saveOpenAiKey(apiKey: String): String {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) return "OpenAI API key este gol."

        prefs.edit().putString("openai_api_key", cleanKey).apply()
        return "OpenAI API key salvat."
    }

    fun saveGeminiKey(apiKey: String): String {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) return "Gemini API key este gol."

        prefs.edit().putString("gemini_api_key", cleanKey).apply()
        return "Gemini API key salvat."
    }

    fun saveOpenRouterKey(apiKey: String): String {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) return "OpenRouter API key este gol."

        prefs.edit().putString("openrouter_api_key", cleanKey).apply()
        return "OpenRouter API key salvat."
    }

    fun getOpenAiKey(): String {
        return prefs.getString("openai_api_key", "") ?: ""
    }

    fun getGeminiKey(): String {
        return prefs.getString("gemini_api_key", "") ?: ""
    }

    fun getOpenRouterKey(): String {
        return prefs.getString("openrouter_api_key", "") ?: ""
    }

    fun hasOpenAiKey(): Boolean = getOpenAiKey().isNotBlank()
    fun hasGeminiKey(): Boolean = getGeminiKey().isNotBlank()
    fun hasOpenRouterKey(): Boolean = getOpenRouterKey().isNotBlank()

    fun saveAiProvider(provider: String): String {
        val cleanProvider = provider.trim().lowercase()

        if (cleanProvider != "openai" && cleanProvider != "gemini" && cleanProvider != "openrouter") {
            return "Provider invalid. Folosește openai, gemini sau openrouter."
        }

        prefs.edit().putString("ai_provider", cleanProvider).apply()
        return "AI provider setat: $cleanProvider"
    }

    fun getAiProvider(): String {
        return prefs.getString("ai_provider", "openrouter") ?: "openrouter"
    }

    fun clearOpenAiKey(): String {
        prefs.edit().remove("openai_api_key").apply()
        return "OpenAI API key șters."
    }

    fun clearGeminiKey(): String {
        prefs.edit().remove("gemini_api_key").apply()
        return "Gemini API key șters."
    }

    fun clearOpenRouterKey(): String {
        prefs.edit().remove("openrouter_api_key").apply()
        return "OpenRouter API key șters."
    }

    fun getStatusText(): String {
        return """
            AI provider: ${getAiProvider()}
            OpenAI key: ${if (hasOpenAiKey()) "salvat" else "nesalvat"}
            Gemini key: ${if (hasGeminiKey()) "salvat" else "nesalvat"}
            OpenRouter key: ${if (hasOpenRouterKey()) "salvat" else "nesalvat"}
        """.trimIndent()
    }
}
