package com.example.jarvisaccessibility

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class TermuxServerClient {

    private val baseUrl = "http://127.0.0.1:8765"

    fun getStatus(callback: (String) -> Unit) {
        getAsync("$baseUrl/status", callback)
    }

    fun checkUpdate(callback: (String) -> Unit) {
        getAsync("$baseUrl/update/check", callback)
    }

    fun installUpdate(callback: (String) -> Unit) {
        getAsync("$baseUrl/update/install", callback)
    }

    fun sendCommand(command: String, callback: (String) -> Unit) {
        val encoded = URLEncoder.encode(command, "UTF-8")
        getAsync("$baseUrl/command?text=$encoded", callback)
    }

    fun say(text: String, callback: (String) -> Unit) {
        val encoded = URLEncoder.encode(text, "UTF-8")
        getAsync("$baseUrl/say?text=$encoded", callback)
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
}
