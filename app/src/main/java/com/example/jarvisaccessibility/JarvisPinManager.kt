package com.example.jarvisaccessibility

import android.content.Context

class JarvisPinManager(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("jarvis_pin_settings", Context.MODE_PRIVATE)

    fun setPin(pinRaw: String): String {
        val pin = pinRaw.trim()

        if (pin.length < 4) {
            return "PIN-ul trebuie să aibă minimum 4 caractere."
        }

        prefs.edit()
            .putString("pin", pin)
            .putBoolean("unlocked", true)
            .apply()

        return "PIN Jarvis setat și setările sunt deblocate."
    }

    fun unlock(pinRaw: String): String {
        val savedPin = prefs.getString("pin", "") ?: ""
        val pin = pinRaw.trim()

        if (savedPin.isBlank()) {
            return "Nu există PIN setat. Setările sunt permise."
        }

        if (pin == savedPin) {
            prefs.edit()
                .putBoolean("unlocked", true)
                .apply()

            return "Setări Jarvis deblocate."
        }

        return "PIN greșit."
    }

    fun lock(): String {
        prefs.edit()
            .putBoolean("unlocked", false)
            .apply()

        return "Setări Jarvis blocate."
    }

    fun isUnlocked(): Boolean {
        val savedPin = prefs.getString("pin", "") ?: ""

        if (savedPin.isBlank()) {
            return true
        }

        return prefs.getBoolean("unlocked", false)
    }

    fun requireUnlocked(): String? {
        return if (isUnlocked()) {
            null
        } else {
            "Setări protejate. Introdu PIN-ul și apasă Deblochează setări."
        }
    }

    fun status(): String {
        val savedPin = prefs.getString("pin", "") ?: ""
        val unlocked = isUnlocked()

        return if (savedPin.isBlank()) {
            "PIN Jarvis: nesetat. Setările nu sunt protejate."
        } else {
            "PIN Jarvis: setat. Status: ${if (unlocked) "deblocat" else "blocat"}."
        }
    }
}
