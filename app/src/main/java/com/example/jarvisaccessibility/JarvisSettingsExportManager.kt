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

            AI_PROVIDER=${apiKeyManager.getAiProvider()}
            OPENROUTER_MODEL=${apiKeyManager.getOpenRouterModel()}
            LAST_AI_COMMAND=${aiCommandMemory.getLastCommand()}

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

    fun importSettings(text: String): String {
        if (text.isBlank()) {
            return "Textul pentru import este gol."
        }

        var importedCount = 0
        val messages = mutableListOf<String>()

        text.lines().forEach { rawLine ->
            val line = rawLine.trim()

            when {
                line.startsWith("AI_PROVIDER=") -> {
                    val provider = line.substringAfter("AI_PROVIDER=").trim()
                    val result = apiKeyManager.saveAiProvider(provider)
                    messages.add(result)
                    importedCount++
                }

                line.startsWith("OPENROUTER_MODEL=") -> {
                    val model = line.substringAfter("OPENROUTER_MODEL=").trim()
                    if (model.isNotBlank()) {
                        val result = apiKeyManager.saveOpenRouterModel(model)
                        messages.add(result)
                        importedCount++
                    }
                }

                line.startsWith("LAST_AI_COMMAND=") -> {
                    val command = line.substringAfter("LAST_AI_COMMAND=").trim()
                    if (command.isNotBlank()) {
                        aiCommandMemory.saveLastCommand(command)
                        messages.add("Ultima comandă AI importată.")
                        importedCount++
                    }
                }
            }
        }

        if (importedCount == 0) {
            return "Nu am găsit setări compatibile pentru import."
        }

        return buildString {
            append("Import setări finalizat.\n")
            append("Elemente importate: ").append(importedCount).append("\n\n")
            messages.forEach {
                append("- ").append(it).append("\n")
            }
        }.trim()
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
