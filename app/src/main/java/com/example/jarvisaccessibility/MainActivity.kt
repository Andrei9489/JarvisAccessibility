package com.example.jarvisaccessibility

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var inputCommand: EditText
    private lateinit var resultText: TextView
    private lateinit var statusText: TextView
    private lateinit var historyText: TextView

    private val speechRequestCode = 1001
    private val commandHistory = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER_HORIZONTAL
        layout.setPadding(40, 60, 40, 40)

        val title = TextView(this)
        title.text = "Jarvis Accessibility"
        title.textSize = 26f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER

        statusText = TextView(this)
        statusText.textSize = 16f
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 25, 0, 25)

        val btnAccessibility = Button(this)
        btnAccessibility.text = "Deschide Accessibility Settings"
        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        val btnOverlayPermission = Button(this)
        btnOverlayPermission.text = "Permite buton flotant"
        btnOverlayPermission.setOnClickListener {
            requestOverlayPermission()
        }

        val btnStartFloating = Button(this)
        btnStartFloating.text = "Pornește buton flotant"
        btnStartFloating.setOnClickListener {
            startFloatingButton()
        }

        val btnStopFloating = Button(this)
        btnStopFloating.text = "Oprește buton flotant"
        btnStopFloating.setOnClickListener {
            stopService(Intent(this, JarvisFloatingService::class.java))
            Toast.makeText(this, "Buton flotant oprit", Toast.LENGTH_SHORT).show()
        }

        inputCommand = EditText(this)
        inputCommand.hint = "Ex: deschide Chrome"
        inputCommand.setSingleLine(false)
        inputCommand.minLines = 2
        inputCommand.setPadding(20, 20, 20, 20)

        val btnVoice = Button(this)
        btnVoice.text = "Vorbește"
        btnVoice.setOnClickListener {
            startVoiceInput()
        }

        val btnExecute = Button(this)
        btnExecute.text = "Execută comanda"
        btnExecute.setOnClickListener {
            executeJarvisCommand()
        }

        val btnClear = Button(this)
        btnClear.text = "Clear rezultat"
        btnClear.setOnClickListener {
            resultText.text = "Rezultat:"
            inputCommand.setText("")
            Toast.makeText(this, "Rezultat șters", Toast.LENGTH_SHORT).show()
        }

        val btnClearHistory = Button(this)
        btnClearHistory.text = "Clear istoric"
        btnClearHistory.setOnClickListener {
            commandHistory.clear()
            updateHistory()
            Toast.makeText(this, "Istoric șters", Toast.LENGTH_SHORT).show()
        }

        val btnChrome = Button(this)
        btnChrome.text = "Deschide Chrome"
        btnChrome.setOnClickListener {
            executeDirectCommand("deschide Chrome")
        }

        val btnYoutube = Button(this)
        btnYoutube.text = "Deschide YouTube"
        btnYoutube.setOnClickListener {
            executeDirectCommand("deschide YouTube")
        }

        val btnTermux = Button(this)
        btnTermux.text = "Deschide Termux"
        btnTermux.setOnClickListener {
            executeDirectCommand("deschide Termux")
        }

        val btnSearch = Button(this)
        btnSearch.text = "Caută vremea azi"
        btnSearch.setOnClickListener {
            executeDirectCommand("caută vremea azi")
        }

        val btnReadScreen = Button(this)
        btnReadScreen.text = "Citește ecranul"
        btnReadScreen.setOnClickListener {
            executeDirectCommand("citește ecranul")
        }

        val btnScrollDown = Button(this)
        btnScrollDown.text = "Scroll jos"
        btnScrollDown.setOnClickListener {
            executeDirectCommand("scroll jos")
        }

        val btnHome = Button(this)
        btnHome.text = "Home"
        btnHome.setOnClickListener {
            executeDirectCommand("home")
        }

        val btnBack = Button(this)
        btnBack.text = "Înapoi"
        btnBack.setOnClickListener {
            executeDirectCommand("înapoi")
        }

        val btnRecents = Button(this)
        btnRecents.text = "Recente"
        btnRecents.setOnClickListener {
            executeDirectCommand("recente")
        }

        val examples = TextView(this)
        examples.text = """
            Exemple comenzi:

            deschide Chrome
            deschide YouTube
            deschide Termux
            caută vremea azi
            apasă pe OK
            scrie salut
            scroll jos
            scroll sus
            citește ecranul
            tap 500 800
            swipe 500 1200 500 300
            home
            înapoi
            recente
        """.trimIndent()
        examples.textSize = 14f
        examples.setPadding(0, 30, 0, 20)

        resultText = TextView(this)
        resultText.text = "Rezultat:"
        resultText.textSize = 16f
        resultText.setPadding(0, 30, 0, 30)

        historyText = TextView(this)
        historyText.text = "Istoric comenzi:\nGol"
        historyText.textSize = 15f
        historyText.setPadding(0, 20, 0, 80)

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(btnAccessibility)
        layout.addView(btnOverlayPermission)
        layout.addView(btnStartFloating)
        layout.addView(btnStopFloating)
        layout.addView(inputCommand)
        layout.addView(btnVoice)
        layout.addView(btnExecute)
        layout.addView(btnClear)
        layout.addView(btnClearHistory)
        layout.addView(btnChrome)
        layout.addView(btnYoutube)
        layout.addView(btnTermux)
        layout.addView(btnSearch)
        layout.addView(btnReadScreen)
        layout.addView(btnScrollDown)
        layout.addView(btnHome)
        layout.addView(btnBack)
        layout.addView(btnRecents)
        layout.addView(examples)
        layout.addView(resultText)
        layout.addView(historyText)

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        val service = JarvisAccessibilityService.instance

        if (service == null) {
            statusText.text = "Status: Jarvis Accessibility NU este activ."
        } else {
            statusText.text = "Status: Jarvis Accessibility este ACTIV."
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permisiunea pentru buton flotant există deja", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startFloatingButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Mai întâi permite butonul flotant", Toast.LENGTH_LONG).show()
            requestOverlayPermission()
            return
        }

        startService(Intent(this, JarvisFloatingService::class.java))
        Toast.makeText(this, "Buton flotant pornit", Toast.LENGTH_SHORT).show()
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Spune comanda pentru Jarvis")

        try {
            startActivityForResult(intent, speechRequestCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Recunoașterea vocală nu este disponibilă pe telefon.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Deprecated("Deprecated Android API, dar funcționează pentru acest proiect simplu.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == speechRequestCode && resultCode == RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenCommand = results?.firstOrNull()?.trim()

            if (!spokenCommand.isNullOrBlank()) {
                inputCommand.setText(spokenCommand)
                inputCommand.setSelection(inputCommand.text.length)
                executeDirectCommand(spokenCommand)
            } else {
                Toast.makeText(this, "Nu am detectat nicio comandă.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun executeJarvisCommand() {
        val command = inputCommand.text.toString().trim()

        if (command.isBlank()) {
            resultText.text = "Rezultat: Scrie sau rostește o comandă."
            Toast.makeText(this, "Scrie sau rostește o comandă", Toast.LENGTH_SHORT).show()
            return
        }

        executeDirectCommand(command)
    }

    private fun executeDirectCommand(command: String) {
        Toast.makeText(this, "Execut: $command", Toast.LENGTH_SHORT).show()
        addToHistory(command)

        val service = JarvisAccessibilityService.instance

        if (service == null) {
            resultText.text =
                "Comandă: $command\n\nRezultat:\nServiciul Accessibility nu este activ. Activează Jarvis Accessibility din setări."
            updateServiceStatus()
            return
        }

        try {
            val result = service.controller.executeCommand(command)
            resultText.text = "Comandă: $command\n\nRezultat:\n$result"
        } catch (e: Exception) {
            resultText.text = "Comandă: $command\n\nEroare:\n${e.message}"
        }

        updateServiceStatus()
    }

    private fun addToHistory(command: String) {
        commandHistory.add(0, command)

        if (commandHistory.size > 10) {
            commandHistory.removeAt(commandHistory.lastIndex)
        }

        updateHistory()
    }

    private fun updateHistory() {
        if (commandHistory.isEmpty()) {
            historyText.text = "Istoric comenzi:\nGol"
            return
        }

        val builder = StringBuilder()
        builder.append("Istoric comenzi:\n")

        commandHistory.forEachIndexed { index, command ->
            builder.append(index + 1)
                .append(". ")
                .append(command)
                .append("\n")
        }

        historyText.text = builder.toString().trim()
    }
}
