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
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var inputCommand: EditText
    private lateinit var inputApiKey: EditText
    private lateinit var resultText: TextView
    private lateinit var statusText: TextView
    private lateinit var versionText: TextView
    private lateinit var historyText: TextView
    private lateinit var updateManager: UpdateManager
    private lateinit var apiKeyManager: ApiKeyManager

    private var lastReleaseUrl: String = "https://github.com/Andrei9489/JarvisAccessibility/releases"
    private var lastApkUrl: String = ""

    private val speechRequestCode = 1001
    private val commandHistory = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateManager = UpdateManager(this)
        apiKeyManager = ApiKeyManager(this)

        val scrollView = ScrollView(this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER_HORIZONTAL
        layout.setPadding(36, 55, 36, 40)

        val title = titleText("Jarvis Accessibility")
        versionText = normalText("")
        statusText = normalText("")

        inputCommand = EditText(this)
        inputCommand.hint = "Scrie comanda aici..."
        inputCommand.setSingleLine(false)
        inputCommand.minLines = 2
        inputCommand.setPadding(20, 20, 20, 20)

        inputApiKey = EditText(this)
        inputApiKey.hint = "OpenAI API Key"
        inputApiKey.setSingleLine(true)
        inputApiKey.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        inputApiKey.setPadding(20, 20, 20, 20)

        resultText = normalText("Rezultat:")
        resultText.setPadding(0, 25, 0, 25)

        historyText = normalText("Istoric comenzi:\nGol")
        historyText.setPadding(0, 20, 0, 80)

        layout.addView(title)
        layout.addView(versionText)
        layout.addView(statusText)

        addSection(layout, "Setări")
        layout.addView(button("Deschide Accessibility Settings") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })

        addSection(layout, "AI API Key")
        layout.addView(inputApiKey)
        layout.addView(button("Salvează API Key") {
            val message = apiKeyManager.saveOpenAiKey(inputApiKey.text.toString())
            inputApiKey.setText("")
            resultText.text = message
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
        layout.addView(button("Șterge API Key") {
            val message = apiKeyManager.clearOpenAiKey()
            resultText.text = message
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
        layout.addView(button("Verifică API Key") {
            resultText.text = if (apiKeyManager.hasOpenAiKey()) {
                "API key este salvat."
            } else {
                "Nu există API key salvat."
            }
        })

        addSection(layout, "Update aplicație")
        layout.addView(button("Verifică update") { checkUpdate() })
        layout.addView(button("Descarcă update") { downloadUpdate() })
        layout.addView(button("Deschide pagina update") {
            updateManager.openReleasePage(lastReleaseUrl)
        })

        addSection(layout, "Buton flotant")
        layout.addView(button("Permite buton flotant") { requestOverlayPermission() })
        layout.addView(button("Pornește buton flotant") { startFloatingButton() })
        layout.addView(button("Oprește buton flotant") {
            stopService(Intent(this, JarvisFloatingService::class.java))
            Toast.makeText(this, "Buton flotant oprit", Toast.LENGTH_SHORT).show()
        })

        addSection(layout, "Comandă manuală / AI / vocală")
        layout.addView(inputCommand)
        layout.addView(button("Vorbește") { startVoiceInput() })
        layout.addView(button("Execută comanda") { executeJarvisCommand() })
        layout.addView(button("Execută cu AI") { executeWithAi() })
        layout.addView(button("Clear rezultat") {
            resultText.text = "Rezultat:"
            inputCommand.setText("")
            Toast.makeText(this, "Rezultat șters", Toast.LENGTH_SHORT).show()
        })
        layout.addView(button("Clear istoric") {
            commandHistory.clear()
            updateHistory()
            Toast.makeText(this, "Istoric șters", Toast.LENGTH_SHORT).show()
        })

        addSection(layout, "Comenzi rapide")
        layout.addView(button("Deschide Chrome") { executeDirectCommand("deschide Chrome") })
        layout.addView(button("Deschide YouTube") { executeDirectCommand("deschide YouTube") })
        layout.addView(button("Deschide Termux") { executeDirectCommand("deschide Termux") })
        layout.addView(button("Caută vremea azi") { executeDirectCommand("caută vremea azi") })
        layout.addView(button("Citește ecranul") { executeDirectCommand("citește ecranul") })
        layout.addView(button("Scroll jos") { executeDirectCommand("scroll jos") })
        layout.addView(button("Home") { executeDirectCommand("home") })
        layout.addView(button("Înapoi") { executeDirectCommand("înapoi") })
        layout.addView(button("Recente") { executeDirectCommand("recente") })
        layout.addView(button("Despre aplicație") { showAbout() })

        addSection(layout, "Exemple AI")
        layout.addView(normalText("""
            deschide browserul
            caută vremea de azi
            apasă pe butonul OK
            scrie salut în câmpul selectat
            fă scroll în jos
            citește ecranul
            mergi acasă
            deschide aplicațiile recente
        """.trimIndent()))

        addSection(layout, "Rezultat")
        layout.addView(resultText)

        addSection(layout, "Istoric")
        layout.addView(historyText)

        scrollView.addView(layout)
        setContentView(scrollView)

        updateVersionText()
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
        updateVersionText()
    }

    private fun executeWithAi() {
        val command = inputCommand.text.toString().trim()

        if (command.isBlank()) {
            resultText.text = "Scrie o comandă pentru AI."
            return
        }

        val apiKey = apiKeyManager.getOpenAiKey()

        if (apiKey.isBlank()) {
            resultText.text = "Lipsește API key. Pune cheia în câmpul OpenAI API Key și apasă Salvează API Key."
            return
        }

        val service = JarvisAccessibilityService.instance

        if (service == null) {
            resultText.text = "Serviciul Accessibility nu este activ."
            return
        }

        resultText.text = "AI procesează comanda..."
        Toast.makeText(this, "Trimit la AI...", Toast.LENGTH_SHORT).show()

        val aiClient = AiClient(apiKey)
        val orchestrator = AiOrchestrator(aiClient, service.controller)

        orchestrator.executeWithAi(command) { result ->
            runOnUiThread {
                resultText.text = result
                addToHistory("AI: $command")
            }
        }
    }

    private fun titleText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 15)
        }
    }

    private fun sectionText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 35, 0, 12)
        }
    }

    private fun normalText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setPadding(0, 8, 0, 8)
        }
    }

    private fun button(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setAllCaps(false)
            setOnClickListener { action() }
        }
    }

    private fun addSection(layout: LinearLayout, title: String) {
        layout.addView(sectionText(title))
    }

    private fun updateVersionText() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toString()
        }

        versionText.text = "Versiune instalată: ${packageInfo.versionName} ($versionCode)"
    }

    private fun showAbout() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toString()
        }

        resultText.text = """
            Jarvis Accessibility

            Versiune instalată:
            ${packageInfo.versionName} ($versionCode)

            Funcții:
            - comenzi scrise
            - comenzi vocale
            - AI actions
            - API key salvat local
            - citire ecran
            - control prin Accessibility
            - buton flotant
            - verificare update
            - download update din aplicație
        """.trimIndent()
    }

    private fun checkUpdate() {
        Toast.makeText(this, "Verific update...", Toast.LENGTH_SHORT).show()

        updateManager.checkForUpdates { info, error ->
            runOnUiThread {
                if (error != null) {
                    resultText.text = "Update error:\n$error"
                    return@runOnUiThread
                }

                if (info == null) {
                    resultText.text = "Nu am putut verifica update-ul."
                    return@runOnUiThread
                }

                lastReleaseUrl = info.releaseUrl
                lastApkUrl = info.apkUrl

                resultText.text = if (info.hasUpdate) {
                    """
                    Update disponibil!

                    Versiune curentă: ${info.currentVersionCode}
                    Versiune nouă: ${info.latestVersionCode}
                    Nume versiune: ${info.latestVersionName}

                    Notes:
                    ${info.notes}

                    Apasă „Descarcă update”.
                    """.trimIndent()
                } else {
                    """
                    Aplicația este la zi.

                    Versiune curentă: ${info.currentVersionCode}
                    Ultima versiune: ${info.latestVersionCode}
                    """.trimIndent()
                }
            }
        }
    }

    private fun downloadUpdate() {
        if (lastApkUrl.isBlank()) {
            resultText.text = "Mai întâi apasă „Verifică update”."
            Toast.makeText(this, "Mai întâi verifică update-ul", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Descarc update...", Toast.LENGTH_LONG).show()
        resultText.text = "Descarc update...\nTe rog așteaptă."

        updateManager.downloadAndInstallApk(lastApkUrl) { error ->
            runOnUiThread {
                if (error != null) {
                    resultText.text = "Eroare download update:\n$error"
                } else {
                    resultText.text = "Update descărcat. Confirmă instalarea în ecranul Android."
                }
            }
        }
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
            historyText.text = "Gol"
            return
        }

        val builder = StringBuilder()

        commandHistory.forEachIndexed { index, command ->
            builder.append(index + 1)
                .append(". ")
                .append(command)
                .append("\n")
        }

        historyText.text = builder.toString().trim()
    }
}
