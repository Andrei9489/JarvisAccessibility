package com.example.jarvisaccessibility

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisDashboardActivity : Activity() {

    private lateinit var txtJarvisOnline: TextView
    private lateinit var txtAiStatus: TextView
    private lateinit var txtPhoneStatus: TextView
    private lateinit var txtJarvisSubtitle: TextView
    private lateinit var recentCommandsList: LinearLayout
    private lateinit var jarvisVoiceManager: JarvisVoiceManager
    private val dashboardVoiceRequestCode = 3001
    private val dashboardHandsFreeRequestCode = 3002
    private var dashboardHandsFreeActive = false
    private var dashboardHandsFreeCount = 0
    private val dashboardMaxHandsFreeCommands = 10
    private val smartVoiceInterpreter = SmartVoiceInterpreter()
    private val termuxServerClient = TermuxServerClient()

    private val recentCommands = listOf(
        "deschide Chrome",
        "deschide browserul",
        "deschide Chrome și caută vremea azi",
        "citește ecranul",
        "AI: Test siguranță Banking"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        setContentView(R.layout.activity_jarvis_dashboard)

        txtJarvisOnline = findViewById(R.id.txtJarvisOnline)
        txtAiStatus = findViewById(R.id.txtAiStatus)
        txtPhoneStatus = findViewById(R.id.txtPhoneStatus)
        txtJarvisSubtitle = findViewById(R.id.txtJarvisSubtitle)
        recentCommandsList = findViewById(R.id.recentCommandsList)

        val jarvisRing = findViewById<android.view.View>(R.id.jarvisRing)
        val pulse = AnimationUtils.loadAnimation(this, R.anim.jarvis_pulse)
        jarvisRing.startAnimation(pulse)

        jarvisVoiceManager = JarvisVoiceManager(this)

        setupInitialData()
        setupButtons()
    }

    private fun setupInitialData() {
        txtJarvisOnline.text = "JARVIS ONLINE"
        txtAiStatus.text = "Status AI: pregătit"
        txtPhoneStatus.text = buildPhoneStatusText()

        recentCommandsList.removeAllViews()

        recentCommands.forEach { command ->
            val item = TextView(this)
            item.text = "› $command"
            item.textSize = 13f
            item.setTextColor(android.graphics.Color.parseColor("#0EA5E9"))
            item.typeface = android.graphics.Typeface.MONOSPACE
            item.setPadding(0, 6, 0, 6)
            recentCommandsList.addView(item)
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnOpenChrome).setOnClickListener {
            onOpenChrome()
        }

        findViewById<Button>(R.id.btnOpenYouTube).setOnClickListener {
            onOpenYouTube()
        }

        findViewById<Button>(R.id.btnOpenTermux).setOnClickListener {
            onOpenTermux()
        }

        findViewById<Button>(R.id.btnHandsFreeJarvis).setOnClickListener {
            startDashboardHandsFree()
        }

        findViewById<Button>(R.id.btnStopHandsFreeJarvis).setOnClickListener {
            stopDashboardHandsFree()
        }

        findViewById<Button>(R.id.btnVoiceJarvis).setOnClickListener {
            onJarvisVoice()
        }

        findViewById<Button>(R.id.btnExecuteAI).setOnClickListener {
            onExecuteWithAI()
        }

        findViewById<Button>(R.id.btnStopAI).setOnClickListener {
            onStopAI()
        }

        findViewById<Button>(R.id.btnTermuxServerStatus).setOnClickListener {
            txtAiStatus.text = "Status AI: verific server Termux"
            txtJarvisSubtitle.text = "Checking Termux server, sir."

            termuxServerClient.getStatus { result ->
                runOnUiThread {
                    txtJarvisSubtitle.text = result.take(180)
                    txtAiStatus.text = "Status AI: server verificat"
                }
            }
        }

        findViewById<Button>(R.id.btnTermuxServerUpdate).setOnClickListener {
            txtAiStatus.text = "Status AI: verific update server"
            txtJarvisSubtitle.text = "Checking server update, sir."

            termuxServerClient.checkUpdate { result ->
                runOnUiThread {
                    txtJarvisSubtitle.text = result.take(180)
                    txtAiStatus.text = "Status AI: update server verificat"
                }
            }
        }

        findViewById<Button>(R.id.btnOpenClassicJarvis).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.btnAccessibilitySettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun buildPhoneStatusText(): String {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val batteryIntent = registerReceiver(
            null,
            android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1

        val batteryPercent = if (level >= 0 && scale > 0) {
            (level * 100 / scale).toString() + "%"
        } else {
            "necunoscut"
        }

        val connection = if (isNetworkAvailable()) {
            "online"
        } else {
            "offline"
        }

        return "Telefon: baterie $batteryPercent | ora $time | conexiune $connection"
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager =
                getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    private fun executeDirectCommand(command: String) {
        val service = JarvisAccessibilityService.instance

        if (service == null) {
            txtAiStatus.text = "Status AI: Accessibility inactiv"
            Toast.makeText(this, "Activează Jarvis Accessibility", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }

        txtAiStatus.text = "Status AI: execut $command"

        val result = try {
            service.controller.executeCommand(command)
        } catch (e: Exception) {
            "Eroare: ${e.message}"
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show()
        jarvisVoiceManager.speak(result)
        txtJarvisSubtitle.text = result.take(160)
        txtAiStatus.text = "Status AI: finalizat"
        txtPhoneStatus.text = buildPhoneStatusText()
    }

    private fun onOpenChrome() {
        executeDirectCommand("deschide Chrome")
    }

    private fun onOpenYouTube() {
        executeDirectCommand("deschide YouTube")
    }

    private fun onOpenTermux() {
        executeDirectCommand("deschide Termux")
    }



    private fun startDashboardHandsFree() {
        dashboardHandsFreeActive = true
        dashboardHandsFreeCount = 0

        txtAiStatus.text = "Status AI: Hands Free activ"
        txtJarvisSubtitle.text = "Hands Free active, sir."
        jarvisVoiceManager.speak("Hands Free active, sir.")
        jarvisVoiceManager.listen(dashboardHandsFreeRequestCode)
    }

    private fun stopDashboardHandsFree() {
        dashboardHandsFreeActive = false

        txtAiStatus.text = "Status AI: Hands Free oprit"
        txtJarvisSubtitle.text = "Hands Free stopped, sir."
        jarvisVoiceManager.speak("Hands Free stopped, sir.")
    }

    private fun continueDashboardHandsFreeIfNeeded() {
        if (!dashboardHandsFreeActive) return

        dashboardHandsFreeCount++

        if (dashboardHandsFreeCount >= dashboardMaxHandsFreeCommands) {
            dashboardHandsFreeActive = false
            txtAiStatus.text = "Status AI: Hands Free limită"
            txtJarvisSubtitle.text = "Hands Free stopped for safety, sir."
            jarvisVoiceManager.speak("Hands Free stopped for safety, sir.")
            return
        }

        txtJarvisSubtitle.postDelayed({
            if (dashboardHandsFreeActive) {
                jarvisVoiceManager.listen(dashboardHandsFreeRequestCode)
            }
        }, 1200)
    }

    private fun onJarvisVoice() {
        txtAiStatus.text = "Status AI: Jarvis te ascultă"
        txtJarvisSubtitle.text = "Listening, sir."
        jarvisVoiceManager.listen(dashboardVoiceRequestCode)
    }

    @Deprecated("Deprecated Android API, dar funcționează pentru acest dashboard simplu.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ((requestCode == dashboardVoiceRequestCode || requestCode == dashboardHandsFreeRequestCode) && resultCode == RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenCommand = results?.firstOrNull()?.trim()

            if (!spokenCommand.isNullOrBlank()) {
                val intent = smartVoiceInterpreter.interpret(spokenCommand)
                jarvisVoiceManager.speak(intent.spokenReply)
                txtJarvisSubtitle.text = intent.spokenReply

                if (intent.type == "conversation") {
                    txtAiStatus.text = "Status AI: conversație"
                    Toast.makeText(this, intent.spokenReply, Toast.LENGTH_LONG).show()
                    if (requestCode == dashboardHandsFreeRequestCode) continueDashboardHandsFreeIfNeeded()
                } else {
                    val finalCommand = intent.command.ifBlank { spokenCommand }
                    executeDirectCommand(finalCommand)
                    if (requestCode == dashboardHandsFreeRequestCode) continueDashboardHandsFreeIfNeeded()
                }
            } else {
                jarvisVoiceManager.speak("Nu am detectat nicio comandă.")
                txtJarvisSubtitle.text = "I did not hear a command, sir."
                if (requestCode == dashboardHandsFreeRequestCode) continueDashboardHandsFreeIfNeeded()
            }
        } else if (requestCode == dashboardHandsFreeRequestCode && dashboardHandsFreeActive) {
            txtJarvisSubtitle.text = "I did not hear a command, sir."
            jarvisVoiceManager.speak("I did not hear a command, sir.")
            continueDashboardHandsFreeIfNeeded()
        }
    }

    override fun onDestroy() {
        try {
            jarvisVoiceManager.shutdown()
        } catch (_: Exception) {
        }

        super.onDestroy()
    }

    private fun onExecuteWithAI() {
        txtAiStatus.text = "Status AI: pregătit pentru integrare dashboard"
        Toast.makeText(
            this,
            "Pentru AI complet folosește ecranul clasic Jarvis momentan.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun onStopAI() {
        val stopManager = AiStopManager(this)
        val message = stopManager.requestStop()

        txtAiStatus.text = "Status AI: oprire cerută"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
