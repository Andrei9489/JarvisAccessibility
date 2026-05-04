package com.example.jarvisaccessibility

import android.content.Context

class AiPendingActionManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_ai_pending_action", Context.MODE_PRIVATE)

    fun savePendingAction(actionLine: String): String {
        prefs.edit()
            .putString("pending_action", actionLine)
            .apply()

        return "Acțiune AI sensibilă salvată pentru confirmare."
    }

    fun getPendingAction(): String {
        return prefs.getString("pending_action", "") ?: ""
    }

    fun clearPendingAction(): String {
        prefs.edit()
            .remove("pending_action")
            .apply()

        return "Acțiunea AI pending a fost ștearsă."
    }

    fun hasPendingAction(): Boolean {
        return getPendingAction().isNotBlank()
    }
}
