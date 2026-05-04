package com.example.jarvisaccessibility

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiDebugLogger(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_ai_debug_log", Context.MODE_PRIVATE)
    private val key = "log_text"

    fun addLog(title: String, details: String) {
        val oldLog = prefs.getString(key, "") ?: ""
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val entry = """
            [$time] $title
            $details
            ------------------------------
        """.trimIndent()

        val newLog = if (oldLog.isBlank()) {
            entry
        } else {
            "$entry\n$oldLog"
        }

        prefs.edit()
            .putString(key, newLog.take(12000))
            .apply()
    }

    fun getLog(): String {
        val log = prefs.getString(key, "") ?: ""
        return if (log.isBlank()) {
            "AI debug log este gol."
        } else {
            log
        }
    }

    fun clearLog(): String {
        prefs.edit()
            .remove(key)
            .apply()

        return "AI debug log șters."
    }
}
