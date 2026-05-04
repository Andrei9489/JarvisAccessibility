package com.example.jarvisaccessibility

import android.content.Context

class AiCommandMemory(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_ai_command_memory", Context.MODE_PRIVATE)

    fun saveLastCommand(command: String) {
        prefs.edit()
            .putString("last_ai_command", command.trim())
            .apply()
    }

    fun getLastCommand(): String {
        return prefs.getString("last_ai_command", "") ?: ""
    }

    fun clearLastCommand(): String {
        prefs.edit()
            .remove("last_ai_command")
            .apply()

        return "Ultima comandă AI a fost ștearsă."
    }

    fun getStatus(): String {
        val last = getLastCommand()

        return if (last.isBlank()) {
            "Nu există comandă AI salvată."
        } else {
            "Ultima comandă AI:\n$last"
        }
    }
}
