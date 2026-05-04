package com.example.jarvisaccessibility

import android.content.Context

class JarvisSettingsExportManager(
    private val context: Context
) {

    private val apiKeyManager = ApiKeyManager(context)
    private val customBlockedAppsManager = CustomBlockedAppsManager(context)
    private val customAllowedAppsManager = CustomAllowedAppsManager(context)
    private val aiCommandMemory = AiCommandMemory(context)

    fun exportSettings(): String {
        return """
            Jarvis Settings Export

            AI:
            ${apiKeyManager.getStatusText()}

            Ultima comandă AI:
            ${aiCommandMemory.getLastCommand().ifBlank { "Nu există comandă AI salvată." }}

            Aplicații blocate custom:
            ${customBlockedAppsManager.listCustomBlockedApps()}

            Aplicații permise custom:
            ${customAllowedAppsManager.listCustomAllowedApps()}
        """.trimIndent()
    }

    fun clearCustomLists(): String {
        clearPrefs("jarvis_custom_blocked_apps")
        clearPrefs("jarvis_custom_allowed_apps")

        return "Listele custom blocate/permise au fost șterse."
    }

    fun clearAiMemory(): String {
        aiCommandMemory.clearLastCommand()
        return "Memoria ultimei comenzi AI a fost ștearsă."
    }

    private fun clearPrefs(name: String) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
