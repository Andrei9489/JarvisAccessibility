package com.example.jarvisaccessibility

import android.content.Context
import org.json.JSONObject

class CustomCommandManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_custom_commands", Context.MODE_PRIVATE)

    fun saveCommand(nameRaw: String, commandRaw: String): String {
        val name = normalizeName(nameRaw)
        val command = commandRaw.trim()

        if (name.isBlank()) {
            return "Numele comenzii este gol."
        }

        if (command.isBlank()) {
            return "Comanda este goală."
        }

        prefs.edit()
            .putString(name, command)
            .apply()

        return "Am salvat comanda rapidă: $name = $command"
    }

    fun deleteCommand(nameRaw: String): String {
        val name = normalizeName(nameRaw)

        if (!prefs.contains(name)) {
            return "Nu există comanda rapidă: $name"
        }

        prefs.edit()
            .remove(name)
            .apply()

        return "Am șters comanda rapidă: $name"
    }

    fun getCommand(nameRaw: String): String? {
        val name = normalizeName(nameRaw)
        return prefs.getString(name, null)
    }

    fun listCommands(): String {
        val all = prefs.all

        if (all.isEmpty()) {
            return "Nu ai comenzi rapide salvate."
        }

        val builder = StringBuilder()
        builder.append("Comenzi rapide salvate:\n")

        all.keys.sorted().forEach { key ->
            val value = prefs.getString(key, "") ?: ""
            builder.append("- ")
                .append(key)
                .append(" = ")
                .append(value)
                .append("\n")
        }

        return builder.toString().trim()
    }

    private fun normalizeName(value: String): String {
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
