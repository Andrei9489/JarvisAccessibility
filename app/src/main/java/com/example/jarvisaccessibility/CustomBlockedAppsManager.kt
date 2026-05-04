package com.example.jarvisaccessibility

import android.content.Context

class CustomBlockedAppsManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_custom_blocked_apps", Context.MODE_PRIVATE)

    fun blockApp(appNameRaw: String): String {
        val appName = normalize(appNameRaw)

        if (appName.isBlank()) {
            return "Numele aplicației este gol."
        }

        prefs.edit()
            .putString(appName, appNameRaw.trim())
            .apply()

        return "Aplicație blocată custom: ${appNameRaw.trim()}"
    }

    fun unblockApp(appNameRaw: String): String {
        val appName = normalize(appNameRaw)

        if (!prefs.contains(appName)) {
            return "Aplicația nu era blocată custom: ${appNameRaw.trim()}"
        }

        prefs.edit()
            .remove(appName)
            .apply()

        return "Aplicație deblocată custom: ${appNameRaw.trim()}"
    }

    fun isCustomBlocked(appNameRaw: String): Boolean {
        val appName = normalize(appNameRaw)
        if (appName.isBlank()) return false

        return prefs.all.keys.any { key ->
            appName.contains(key) || key.contains(appName)
        }
    }

    fun listCustomBlockedApps(): String {
        val all = prefs.all

        if (all.isEmpty()) {
            return "Nu există aplicații blocate custom."
        }

        val builder = StringBuilder()
        builder.append("Aplicații blocate custom:\n")

        all.values
            .map { it.toString() }
            .sorted()
            .forEach {
                builder.append("- ").append(it).append("\n")
            }

        return builder.toString().trim()
    }

    private fun normalize(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace("ă", "a")
            .replace("â", "a")
            .replace("î", "i")
            .replace("ș", "s")
            .replace("ş", "s")
            .replace("ț", "t")
            .replace("ţ", "t")
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "")
            .replace(".", "")
    }
}
