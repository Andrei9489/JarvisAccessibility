package com.example.jarvisaccessibility

import android.content.Context

class CustomAllowedAppsManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_custom_allowed_apps", Context.MODE_PRIVATE)

    fun allowApp(appNameRaw: String): String {
        val appName = normalize(appNameRaw)

        if (appName.isBlank()) {
            return "Numele aplicației este gol."
        }

        prefs.edit()
            .putString(appName, appNameRaw.trim())
            .apply()

        return "Aplicație permisă custom: ${appNameRaw.trim()}"
    }

    fun removeAllowedApp(appNameRaw: String): String {
        val appName = normalize(appNameRaw)

        if (!prefs.contains(appName)) {
            return "Aplicația nu era permisă custom: ${appNameRaw.trim()}"
        }

        prefs.edit()
            .remove(appName)
            .apply()

        return "Aplicație ștearsă din lista permisă custom: ${appNameRaw.trim()}"
    }

    fun isCustomAllowed(appNameRaw: String): Boolean {
        val appName = normalize(appNameRaw)
        if (appName.isBlank()) return false

        return prefs.all.keys.any { key ->
            appName.contains(key) || key.contains(appName)
        }
    }

    fun listCustomAllowedApps(): String {
        val all = prefs.all

        if (all.isEmpty()) {
            return "Nu există aplicații permise custom."
        }

        val builder = StringBuilder()
        builder.append("Aplicații permise custom:\n")

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
