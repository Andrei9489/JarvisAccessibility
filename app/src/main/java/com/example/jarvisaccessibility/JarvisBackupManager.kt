package com.example.jarvisaccessibility

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisBackupManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_local_backup", Context.MODE_PRIVATE)
    private val settingsExportManager = JarvisSettingsExportManager(context)

    fun saveBackup(): String {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val export = settingsExportManager.exportSettings()

        val backupText = """
            Jarvis Local Backup
            BACKUP_TIME=$time

            $export
        """.trimIndent()

        prefs.edit()
            .putString("backup_text", backupText)
            .putString("backup_time", time)
            .apply()

        return "Backup local salvat la: $time"
    }

    fun restoreBackup(): String {
        val backup = prefs.getString("backup_text", "") ?: ""

        if (backup.isBlank()) {
            return "Nu există backup local pentru restaurare."
        }

        return settingsExportManager.importSettings(backup)
    }

    fun getBackup(): String {
        val backup = prefs.getString("backup_text", "") ?: ""

        return if (backup.isBlank()) {
            "Nu există backup local salvat."
        } else {
            backup
        }
    }

    fun clearBackup(): String {
        prefs.edit()
            .clear()
            .apply()

        return "Backup local șters."
    }

    fun getBackupStatus(): String {
        val time = prefs.getString("backup_time", "") ?: ""

        return if (time.isBlank()) {
            "Backup local: nesalvat."
        } else {
            "Backup local salvat la: $time"
        }
    }
}
