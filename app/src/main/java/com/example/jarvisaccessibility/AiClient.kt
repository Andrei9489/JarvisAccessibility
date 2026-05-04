package com.example.jarvisaccessibility

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AiClient(
    private val apiKey: String
) {

    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    private val model = "gpt-4o-mini"

    fun sendCommandToAi(
        userCommand: String,
        callback: (String?, String?) -> Unit
    ) {
        Thread {
            try {
                val body = buildRequestBody(userCommand)
                val response = postJson(apiUrl, body)
                val actionText = parseResponse(response)

                callback(actionText, null)
            } catch (e: Exception) {
                callback(null, e.message ?: "Eroare AI necunoscută.")
            }
        }.start()
    }

    private fun buildRequestBody(userCommand: String): String {
        val systemPrompt = """
            Ești modulul AI pentru aplicația Android Jarvis Accessibility.

            Răspunzi DOAR cu o singură acțiune.
            Fără explicații.
            Fără Markdown.
            Fără JSON.
            Fără text suplimentar.

            Format obligatoriu:
            ACTION: open_app("Chrome")
            ACTION: tap(500, 800)
            ACTION: swipe(500, 1200, 500, 300)
            ACTION: write("salut")
            ACTION: scroll_down()
            ACTION: scroll_up()
            ACTION: read_screen()
            ACTION: back()
            ACTION: home()
            ACTION: recents()
            ACTION: search("vremea azi")
            ACTION: click_text("OK")

            Reguli de siguranță:
            - Nu controla aplicații bancare, parole, portofele, plăți sau autentificare.
            - Dacă utilizatorul cere banking, bani, card, parole, PIN, wallet sau plăți, răspunde:
              ACTION: blocked("financial_or_sensitive_app")
        """.trimIndent()

        val json = JSONObject()
        json.put("model", model)
        json.put("temperature", 0)

        val messages = JSONArray()

        val system = JSONObject()
        system.put("role", "system")
        system.put("content", systemPrompt)
        messages.put(system)

        val user = JSONObject()
        user.put("role", "user")
        user.put("content", userCommand)
        messages.put(user)

        json.put("messages", messages)

        return json.toString()
    }

    private fun postJson(urlString: String, body: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.doOutput = true

        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")

        connection.outputStream.use { output ->
            output.write(body.toByteArray(Charsets.UTF_8))
        }

        val code = connection.responseCode

        val stream = if (code in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val response = stream.bufferedReader().use { it.readText() }

        if (code !in 200..299) {
            throw Exception("AI API error $code: $response")
        }

        return response
    }

    private fun parseResponse(response: String): String {
        val json = JSONObject(response)
        val choices = json.getJSONArray("choices")
        val first = choices.getJSONObject(0)
        val message = first.getJSONObject("message")
        return message.getString("content").trim()
    }
}
