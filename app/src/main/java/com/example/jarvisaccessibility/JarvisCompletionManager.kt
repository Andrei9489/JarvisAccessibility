package com.example.jarvisaccessibility

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisCompletionManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_completion_status", Context.MODE_PRIVATE)

    fun markComplete(): String {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        prefs.edit()
            .putBoolean("completed", true)
            .putString("completed_at", time)
            .apply()

        return "Jarvis a fost marcat 100% funcțional pentru versiunea actuală la: $time"
    }

    fun resetComplete(): String {
        prefs.edit()
            .clear()
            .apply()

        return "Statusul 100% a fost resetat."
    }

    fun isComplete(): Boolean {
        return prefs.getBoolean("completed", false)
    }

    fun getStatus(): String {
        val completed = isComplete()
        val time = prefs.getString("completed_at", "") ?: ""

        return if (completed) {
            "Jarvis status: 100% funcțional pentru versiunea actuală.\nMarcat la: $time"
        } else {
            "Jarvis status: 99.9% dezvoltat. Rulează checklist-ul final înainte de 100%."
        }
    }
}
