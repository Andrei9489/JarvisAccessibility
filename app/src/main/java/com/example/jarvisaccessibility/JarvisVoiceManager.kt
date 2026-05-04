package com.example.jarvisaccessibility

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

class JarvisVoiceManager(
    private val activity: Activity
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

    init {
        textToSpeech = TextToSpeech(activity, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val roResult = textToSpeech?.setLanguage(Locale("ro", "RO"))

            if (
                roResult == TextToSpeech.LANG_MISSING_DATA ||
                roResult == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                textToSpeech?.language = Locale.US
            }

            textToSpeech?.setSpeechRate(0.92f)
            textToSpeech?.setPitch(0.85f)
            ttsReady = true
        }
    }

    fun speak(text: String) {
        val cleanText = text
            .replace("✅", "")
            .replace("❌", "")
            .replace("ACTION:", "Acțiune:")
            .replace("\n", ". ")
            .take(450)

        if (!ttsReady) {
            Toast.makeText(activity, cleanText, Toast.LENGTH_SHORT).show()
            return
        }

        textToSpeech?.speak(
            cleanText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis_voice_response"
        )
    }

    fun listen(requestCode: Int) {
        speak("Jarvis te ascult.")

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Spune comanda pentru Jarvis")

        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            speak("Recunoașterea vocală nu este disponibilă pe acest telefon.")
            Toast.makeText(
                activity,
                "Recunoașterea vocală nu este disponibilă.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
