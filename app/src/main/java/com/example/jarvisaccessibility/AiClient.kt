package com.example.jarvisaccessibility

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AiClient(
    private val apiKey: String,
    private val provider: String = "gemini"
) {

    private val openAiUrl = "https://api.openai.com/v1/chat/completions"
    private val openAiModel = "gpt-4o-mini"

    private val geminiModel = "gemini-1.5-flash"

    fun sendCommandToAi(
        userCommand: String,
        callback: (String?, String?) -> Unit
    ) {
        Thread {
            try {
                val response = if (provider.lowercase() == "openai") {
                    sendToOpenAi(userCommand)
                } else {
                    sendToGemini(userCommand)
                }

                callback(response, null)
            } catch (e: Exception) {
                callback(null, e.message ?: "Eroare AI necunoscută.")
            }
        }.start()
    }

    private fun sendToOpenAi(userCommand: String): String {
        val body = buildOpenAiRequestBody(userCommand)
        val response = postJson(
            urlString = openAiUrl,
            body = body,
            headers = mapOf(
                "Authorization" to "Bearer $apiKey",
                "Content-Type" to "application/json"
            )
        )

        return parseOpenAiResponse(response)
    }

    private fun sendToGemini(userCommand: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$apiKey"
        val body = buildGeminiRequestBody(userCommand)

        val response = postJson(
            urlString = url,
            body = body,
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )

        return parseGeminiResponse(response)
    }

    private fun systemPrompt(): String {
        return """
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
    }

    private fun buildOpenAiRequestBody(userCommand: String): String {
        val json = JSONObject()
        json.put("model", openAiModel)
        json.put("temperature", 0)

        val messages = JSONArray()

        val system = JSONObject()
        system.put("role", "system")
        system.put("content", systemPrompt())
        messages.put(system)

        val user = JSONObject()
        user.put("role", "user")
        user.put("content", userCommand)
        messages.put(user)

        json.put("messages", messages)

        return json.toString()
    }

    private fun buildGeminiRequestBody(userCommand: String): String {
        val fullPrompt = systemPrompt() + "\n\nComanda utilizatorului:\n" + userCommand

        val json = JSONObject()

        val contents = JSONArray()
        val content = JSONObject()
        val parts = JSONArray()
        val part = JSONObject()

        part.put("text", fullPrompt)
        parts.put(part)

        content.put("parts", parts)
        contents.put(content)

        json.put("contents", contents)

        val generationConfig = JSONObject()
        generationConfig.put("temperature", 0)
        generationConfig.put("maxOutputTokens", 80)

        json.put("generationConfig", generationConfig)

        return json.toString()
    }

    private fun postJson(
        urlString: String,
        body: String,
        headers: Map<String, String>
    ): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.doOutput = true

        headers.forEach { entry ->
            connection.setRequestProperty(entry.key, entry.value)
        }

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

    private fun parseOpenAiResponse(response: String): String {
        val json = JSONObject(response)
        val choices = json.getJSONArray("choices")
        val first = choices.getJSONObject(0)
        val message = first.getJSONObject("message")
        return message.getString("content").trim()
    }

    private fun parseGeminiResponse(response: String): String {
        val json = JSONObject(response)
        val candidates = json.getJSONArray("candidates")
        val first = candidates.getJSONObject(0)
        val content = first.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val part = parts.getJSONObject(0)
        return part.getString("text").trim()
    }
}
