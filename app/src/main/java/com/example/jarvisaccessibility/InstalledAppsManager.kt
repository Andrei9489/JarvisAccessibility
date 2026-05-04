package com.example.jarvisaccessibility

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class InstalledAppsManager(
    private val context: Context
) {

    data class InstalledApp(
        val label: String,
        val packageName: String
    )

    fun listLaunchableApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = packageManager.queryIntentActivities(intent, 0)

        return apps.map {
            val label = it.loadLabel(packageManager).toString()
            val packageName = it.activityInfo.packageName
            InstalledApp(label, packageName)
        }.distinctBy {
            it.packageName
        }.sortedBy {
            it.label.lowercase()
        }
    }

    fun findBestApp(appNameRaw: String): InstalledApp? {
        val query = normalize(appNameRaw)
        if (query.isBlank()) return null

        val aliases = mapOf(
            "yootube" to "youtube",
            "you tube" to "youtube",
            "iutub" to "youtube",
            "google chrome" to "chrome",
            "browser" to "chrome",
            "tik tok" to "tiktok",
            "insta" to "instagram",
            "whats app" to "whatsapp",
            "face book" to "facebook"
        )

        val normalizedQuery = aliases[query] ?: query
        val apps = listLaunchableApps()

        return apps.firstOrNull {
            normalize(it.label) == normalizedQuery
        } ?: apps.firstOrNull {
            normalize(it.label).contains(normalizedQuery)
        } ?: apps.firstOrNull {
            normalizedQuery.contains(normalize(it.label))
        }
    }

    fun openApp(appNameRaw: String): String {
        val app = findBestApp(appNameRaw)
            ?: return "Nu am găsit aplicația instalată: $appNameRaw"

        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            ?: return "Aplicația ${app.label} nu poate fi pornită."

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)

        return "Deschid ${app.label}, sir."
    }

    fun listAppsText(limit: Int = 80): String {
        val apps = listLaunchableApps()

        if (apps.isEmpty()) {
            return "Nu am găsit aplicații instalate."
        }

        return apps.take(limit).joinToString("\n") {
            "• ${it.label} (${it.packageName})"
        }
    }

    fun searchAppsText(query: String): String {
        val app = findBestApp(query)

        return if (app == null) {
            "Nu am găsit aplicația: $query"
        } else {
            "Aplicație găsită: ${app.label}\nPachet: ${app.packageName}"
        }
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
    }
}
