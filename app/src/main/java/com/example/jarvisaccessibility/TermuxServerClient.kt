package com.example.jarvisaccessibility

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class TermuxServerClient {

    private val baseUrl = "http://127.0.0.1:8765"

    fun getStatus(callback: (String) -> Unit) {
        getAsync("$baseUrl/status") { raw ->
            callback(prettyStatus(raw))
        }
    }

    fun checkUpdate(callback: (String) -> Unit) {
        getAsync("$baseUrl/update/check") { raw ->
            callback(prettyUpdate(raw))
        }
    }

    fun installUpdate(callback: (String) -> Unit) {
        getAsync("$baseUrl/update/install") { raw ->
            callback(prettyInstall(raw))
        }
    }

    fun sendCommand(command: String, callback: (String) -> Unit) {
        val encoded = URLEncoder.encode(command, "UTF-8")
        getAsync("$baseUrl/command?text=$encoded") { raw ->
            callback(prettyCommand(raw))
        }
    }

    fun sendVoiceCommand(command: String, callback: (String) -> Unit) {
        val encoded = URLEncoder.encode(command, "UTF-8")
        getAsync("$baseUrl/command?text=$encoded&source=voice") { raw ->
            callback(prettyCommand(raw))
        }
    }

    fun say(text: String, callback: (String) -> Unit) {
        val encoded = URLEncoder.encode(text, "UTF-8")
        getAsync("$baseUrl/say?text=$encoded") { raw ->
            callback(prettySay(raw))
        }
    }

    fun getCommandHistory(callback: (String) -> Unit) {
        getAsync("$baseUrl/command-history") { raw ->
            callback(prettyCommandHistory(raw))
        }
    }

    fun clearCommandHistory(callback: (String) -> Unit) {
        getAsync("$baseUrl/clear-history") { raw ->
            callback(prettyClearHistory(raw))
        }
    }

    private fun getAsync(urlString: String, callback: (String) -> Unit) {
        Thread {
            val result = try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 10000

                val code = connection.responseCode
                val stream = if (code in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val text = BufferedReader(InputStreamReader(stream)).use { reader ->
                    reader.readText()
                }

                connection.disconnect()

                "HTTP $code\n$text"
            } catch (e: Exception) {
                "Eroare server Termux:\n${e.message}\n\nVerifică dacă serverul rulează în Termux:\ncd ~/jarvis-server\npython server.py"
            }

            callback(result)
        }.start()
    }

    private fun extractJson(raw: String): JSONObject? {
        return try {
            val jsonStart = raw.indexOf("{")
            if (jsonStart < 0) return null
            JSONObject(raw.substring(jsonStart))
        } catch (_: Exception) {
            null
        }
    }


    private fun prettyCommandHistory(raw: String): String {
        val json = extractJson(raw) ?: return raw

        if (!json.optBoolean("ok", false)) {
            return "Istoric server: eroare\n${json.optString("error", raw)}"
        }

        val items = json.optJSONArray("items")
            ?: return "Istoric server: gol."

        if (items.length() == 0) {
            return "Istoric comenzi server: gol."
        }

        val builder = StringBuilder()
        builder.append("Istoric comenzi server\n\n")

        for (i in 0 until minOf(items.length(), 15)) {
            val item = items.optJSONObject(i) ?: continue
            builder.append(i + 1)
                .append(". ")
                .append(item.optString("time", "-"))
                .append("\n   Comandă: ")
                .append(item.optString("text", "-"))
                .append("\n   Intent: ")
                .append(item.optString("intent", "-"))
                .append("\n   Răspuns: ")
                .append(item.optString("reply", "-"))
                .append("\n\n")
        }

        return builder.toString().trim()
    }

    private fun prettyClearHistory(raw: String): String {
        val json = extractJson(raw) ?: return raw

        if (!json.optBoolean("ok", false)) {
            return "Ștergere istoric server: eroare\n${json.optString("error", raw)}"
        }

        return json.optString("message", "Istoricul a fost șters.")
    }

    private fun prettyStatus(raw: String): String {
        val json = extractJson(raw) ?: return raw

        val status = json.optString("status", "unknown")
        val name = json.optString("name", "Jarvis Termux Server")
        val versionName = json.optString("serverVersionName", "?")
        val versionCode = json.optInt("serverVersionCode", 0)
        val startedAt = json.optString("started_at", "-")
        val lastCommand = json.optString("last_command", "").ifBlank { "-" }

        return """
            $name

            Server Termux: ${status.uppercase()}
            Versiune server: $versionName
            Cod versiune: $versionCode
            Ultima comandă: $lastCommand
            Pornit la: $startedAt
        """.trimIndent()
    }

    private fun prettyUpdate(raw: String): String {
        val json = extractJson(raw) ?: return raw

        if (!json.optBoolean("ok", false)) {
            return "Update server Termux: eroare\n${json.optString("error", raw)}"
        }

        val localName = json.optString("localVersionName", "?")
        val localCode = json.optInt("localVersionCode", 0)
        val remoteName = json.optString("remoteVersionName", "?")
        val remoteCode = json.optInt("remoteVersionCode", 0)
        val available = json.optBoolean("updateAvailable", false)
        val notes = json.optString("notes", "")

        return """
            Update server Termux

            Local: $localName ($localCode)
            Remote: $remoteName ($remoteCode)
            Update disponibil: ${if (available) "DA" else "NU"}

            Note:
            ${notes.ifBlank { "-" }}
        """.trimIndent()
    }

    private fun prettyInstall(raw: String): String {
        val json = extractJson(raw) ?: return raw

        if (!json.optBoolean("ok", false)) {
            return "Instalare update server: eroare\n${json.optString("error", raw)}"
        }

        return """
            Instalare update server

            ${json.optString("message", "Operație finalizată.")}

            Versiune nouă:
            ${json.optString("newVersionName", "-")} (${json.optInt("newVersionCode", 0)})

            Dacă s-a instalat update-ul, repornește serverul Termux:
            CTRL + C
            python server.py
        """.trimIndent()
    }

    private fun prettyCommand(raw: String): String {
        val json = extractJson(raw) ?: return raw

        if (!json.optBoolean("ok", false)) {
            return "Comandă server: eroare\n${json.optString("error", raw)}"
        }

        return """
            Comandă trimisă la server

            Primit:
            ${json.optString("received", "-")}

            Răspuns:
            ${json.optString("reply", "-")}
        """.trimIndent()
    }

    private fun prettySay(raw: String): String {
        val json = extractJson(raw) ?: return raw

        if (!json.optBoolean("ok", false)) {
            return "Voce server: eroare\n${json.optString("error", raw)}"
        }

        return """
            Voce prin server Termux

            Text:
            ${json.optString("spoken", "-")}

            Output:
            ${json.optString("output", "-").ifBlank { "-" }}
        """.trimIndent()
    }
}
