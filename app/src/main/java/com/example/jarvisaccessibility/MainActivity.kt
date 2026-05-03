package com.example.jarvisaccessibility

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
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

        inputCommand = EditText(this)
        inputCommand.hint = "Ex: deschide Chrome"
        inputCommand.setSingleLine(false)
        inputCommand.minLines = 2
        inputCommand.setPadding(20, 20, 20, 20)

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

        val btnChrome = Button(this)
        btnChrome.text = "Deschide Chrome"
        btnChrome.setOnClickListener {
            executeDirectCommand("deschide Chrome")
        }

        val btnTermux = Button(this)
        btnTermux.text = "Deschide Termux"
        btnTermux.setOnClickListener {
            executeDirectCommand("deschide Termux")
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
            apasă pe OK
            scrie salut
            caută vremea azi
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
        resultText.setPadding(0, 30, 0, 60)

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(btnAccessibility)
        layout.addView(inputCommand)
        layout.addView(btnExecute)
        layout.addView(btnClear)
        layout.addView(btnChrome)
        layout.addView(btnTermux)
        layout.addView(btnReadScreen)
        layout.addView(btnScrollDown)
        layout.addView(btnHome)
        layout.addView(btnBack)
        layout.addView(btnRecents)
        layout.addView(examples)
        layout.addView(resultText)

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

    private fun executeJarvisCommand() {
        val command = inputCommand.text.toString().trim()

        if (command.isBlank()) {
            resultText.text = "Rezultat: Scrie o comandă."
            Toast.makeText(this, "Scrie o comandă", Toast.LENGTH_SHORT).show()
            return
        }

        executeDirectCommand(command)
    }

    private fun executeDirectCommand(command: String) {
        Toast.makeText(this, "Execut: $command", Toast.LENGTH_SHORT).show()

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
}
