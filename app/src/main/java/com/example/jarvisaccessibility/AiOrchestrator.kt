package com.example.jarvisaccessibility

class AiOrchestrator(
    private val aiClient: AiClient,
    private val controller: JarvisController
) {

    fun executeWithAi(
        userText: String,
        callback: (String) -> Unit
    ) {
        if (userText.isBlank()) {
            callback("Scrie o comandă pentru AI.")
            return
        }

        aiClient.sendCommandToAi(userText) { aiAction, error ->
            if (error != null) {
                callback("Eroare AI:\n$error")
                return@sendCommandToAi
            }

            if (aiAction.isNullOrBlank()) {
                callback("AI nu a returnat nicio acțiune.")
                return@sendCommandToAi
            }

            val result = interpretAiAction(aiAction)

            callback(
                """
                AI:
                $aiAction

                Rezultat:
                $result
                """.trimIndent()
            )
        }
    }

    private fun interpretAiAction(aiActionRaw: String): String {
        val aiAction = aiActionRaw.trim()

        if (!aiAction.startsWith("ACTION:", ignoreCase = true)) {
            return "Răspuns AI invalid. Trebuia să înceapă cu ACTION:"
        }

        val action = aiAction
            .removePrefix("ACTION:")
            .trim()

        return when {
            action.startsWith("open_app", ignoreCase = true) -> {
                val appName = extractStringArg(action)
                if (appName.isBlank()) {
                    "AI nu a specificat aplicația."
                } else {
                    controller.executeCommand("deschide $appName")
                }
            }

            action.startsWith("tap", ignoreCase = true) -> {
                val numbers = extractNumbers(action)
                if (numbers.size < 2) {
                    "Format AI invalid pentru tap."
                } else {
                    controller.executeCommand("tap ${numbers[0]} ${numbers[1]}")
                }
            }

            action.startsWith("swipe", ignoreCase = true) -> {
                val numbers = extractNumbers(action)
                if (numbers.size < 4) {
                    "Format AI invalid pentru swipe."
                } else {
                    controller.executeCommand(
                        "swipe ${numbers[0]} ${numbers[1]} ${numbers[2]} ${numbers[3]}"
                    )
                }
            }

            action.startsWith("write", ignoreCase = true) -> {
                val text = extractStringArg(action)
                controller.executeCommand("scrie $text")
            }

            action.startsWith("scroll_down", ignoreCase = true) -> {
                controller.executeCommand("scroll jos")
            }

            action.startsWith("scroll_up", ignoreCase = true) -> {
                controller.executeCommand("scroll sus")
            }

            action.startsWith("read_screen", ignoreCase = true) -> {
                controller.executeCommand("citește ecranul")
            }

            action.startsWith("back", ignoreCase = true) -> {
                controller.executeCommand("înapoi")
            }

            action.startsWith("home", ignoreCase = true) -> {
                controller.executeCommand("home")
            }

            action.startsWith("recents", ignoreCase = true) -> {
                controller.executeCommand("recente")
            }

            action.startsWith("search", ignoreCase = true) -> {
                val query = extractStringArg(action)
                controller.executeCommand("caută $query")
            }

            action.startsWith("click_text", ignoreCase = true) -> {
                val text = extractStringArg(action)
                controller.executeCommand("apasă pe $text")
            }

            action.startsWith("blocked", ignoreCase = true) -> {
                "Blocat pentru siguranță. AI a refuzat o acțiune sensibilă."
            }

            else -> {
                "Acțiune AI necunoscută: $action"
            }
        }
    }

    private fun extractStringArg(action: String): String {
        val start = action.indexOf("\"")
        val end = action.lastIndexOf("\"")

        if (start == -1 || end == -1 || end <= start) {
            return ""
        }

        return action.substring(start + 1, end)
    }

    private fun extractNumbers(action: String): List<Int> {
        val inside = action.substringAfter("(").substringBefore(")")
        return inside
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }
}
