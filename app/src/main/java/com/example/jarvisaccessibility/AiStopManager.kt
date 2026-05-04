package com.example.jarvisaccessibility

import android.content.Context

class AiStopManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_ai_stop", Context.MODE_PRIVATE)

    fun requestStop(): String {
        prefs.edit()
            .putBoolean("stop_requested", true)
            .apply()

        return "Oprire AI cerută."
    }

    fun clearStop() {
        prefs.edit()
            .putBoolean("stop_requested", false)
            .apply()
    }

    fun isStopRequested(): Boolean {
        return prefs.getBoolean("stop_requested", false)
    }
}
