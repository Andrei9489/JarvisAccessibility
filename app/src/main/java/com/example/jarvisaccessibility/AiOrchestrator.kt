package com.example.jarvisaccessibility

class AiOrchestrator(
    private val aiClient: AiClient,
    private val controller: JarvisController,
    private val stopManager: AiStopManager,
    private val pendingActionManager: AiPendingActionManager
) {

    fun executeWithAi(
        userText: String,
        callback: (String) -> Unit
    ) {
        if (userText.isBlank()) {
            callback("Scrie o comandă pentru AI.")
            return
        }

        stopManager.clearStop()

        aiClient.sendCommandToAi(userText) { aiResponse, error ->
            if (error != null) {
                callback("Eroare AI:\n$error")
                return@sendCommandToAi
            }

            if (aiResponse == null || aiResponse.action.isBlank()) {
                callback("AI nu a returnat nicio acțiune.")
                return@sendCommandToAi
            }

            val results = executeAiActions(aiResponse.action)

            callback(
                """
                Provider:
                ${aiResponse.provider}

                Model folosit:
                ${aiResponse.model}

                AI:
                ${aiResponse.action}

                Rezultat:
                $results
                """.trimIndent()
            )
        }
    }

    fun executePendingAction(): String {
        val pending = pendingActionManager.getPendingAction()

        if (pending.isBlank()) {
            return "Nu există acțiune AI pending."
        }

        pendingActionManager.clearPendingAction()
        return interpretAiAction(pending)
    }

    private fun executeAiActions(aiActionsRaw: String): String {
        val lines = aiActionsRaw
            .lines()
            .map { it.trim() }
            .filter { it.startsWith("ACTION:", ignoreCase = true) }
            .take(5)

        if (lines.isEmpty()) {
            return "Răspuns AI invalid. Nu există ACTION."
        }

        var tapCount = 0
        var swipeCount = 0
        var writeCount = 0

        val resultBuilder = StringBuilder()

        val totalSteps = lines.size

        for ((index, line) in lines.withIndex()) {
            if (stopManager.isStopRequested()) {
                resultBuilder.append("Execuția AI a fost oprită manual.")
                    .append("\n\n")
                break
            }

            val actionName = getActionName(line)

            resultBuilder.append("Status pas AI: ")
                .append(index + 1)
                .append("/")
                .append(totalSteps)
                .append(" - ")
                .append(actionName)
                .append("\n")

            when (actionName) {
                "tap" -> {
                    tapCount++
                    if (tapCount > 2) {
                        resultBuilder.append(index + 1)
                            .append(". ")
                            .append(line)
                            .append("\n")
                            .append("Oprit pentru siguranță: prea multe tap-uri într-o singură comandă AI.")
                            .append("\n\n")
                        break
                    }
                }

                "swipe" -> {
                    swipeCount++
                    if (swipeCount > 2) {
                        resultBuilder.append(index + 1)
                            .append(". ")
                            .append(line)
                            .append("\n")
                            .append("Oprit pentru siguranță: prea multe swipe-uri într-o singură comandă AI.")
                            .append("\n\n")
                        break
                    }
                }

                "write" -> {
                    writeCount++
                    if (writeCount > 1) {
                        resultBuilder.append(index + 1)
                            .append(". ")
                            .append(line)
                            .append("\n")
                            .append("Oprit pentru siguranță: prea multe acțiuni de scriere într-o singură comandă AI.")
                            .append("\n\n")
                        break
                    }
                }
            }

            if (requiresConfirmation(actionName)) {
                pendingActionManager.savePendingAction(line)

                resultBuilder.append(index + 1)
                    .append(". ")
                    .append(line)
                    .append("\n")
                    .append("Acțiune sensibilă. Apasă „Confirmă AI Action” ca să o execuți.")
                    .append("\n\n")
                    .append("Execuția AI a fost oprită până la confirmare.")
                    .append("\n\n")

                break
            }

            val result = interpretAiAction(line)

            resultBuilder.append(index + 1)
                .append(". ")
                .append(line)
                .append("\n")
                .append(result)
                .append("\n\n")

            if (
                actionName == "blocked" ||
                result.contains("Blocat pentru siguranță", ignoreCase = true) ||
                result.contains("Acțiune AI necunoscută", ignoreCase = true) ||
                result.contains("Format AI invalid", ignoreCase = true)
            ) {
                resultBuilder.append("Execuția AI a fost oprită pentru siguranță.")
                    .append("\n\n")
                break
            }

            autoWaitAfterAction(line)
        }

        return resultBuilder.toString().trim()
    }

    private fun requiresConfirmation(actionName: String): Boolean {
        return actionName == "tap" || actionName == "swipe" || actionName == "write"
    }

    private fun getActionName(line: String): String {
        return line
            .removePrefix("ACTION:")
            .trim()
            .substringBefore("(")
            .trim()
            .lowercase()
    }

    private fun autoWaitAfterAction(line: String) {
        val clean = line.lowercase()

        val waitMs = when {
            clean.contains("open_app") -> 1800
            clean.contains("search") -> 1300
            clean.contains("home") -> 700
            clean.contains("back") -> 700
            clean.contains("recents") -> 900
            clean.contains("click_text") -> 700
            clean.contains("tap") -> 700
            clean.contains("swipe") -> 900
            clean.contains("scroll") -> 700
            else -> 250
        }

        try {
            var waited = 0
            while (waited < waitMs) {
                if (stopManager.isStopRequested()) return
                Thread.sleep(100)
                waited += 100
            }
        } catch (_: Exception) {
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
                if (appName.isBlank()) "AI nu a specificat aplicația."
                else controller.executeCommand("deschide $appName")
            }

            action.startsWith("tap", ignoreCase = true) -> {
                val numbers = extractNumbers(action)
                if (numbers.size < 2) "Format AI invalid pentru tap."
                else controller.executeCommand("tap ${numbers[0]} ${numbers[1]}")
            }

            action.startsWith("swipe", ignoreCase = true) -> {
                val numbers = extractNumbers(action)
                if (numbers.size < 4) "Format AI invalid pentru swipe."
                else controller.executeCommand("swipe ${numbers[0]} ${numbers[1]} ${numbers[2]} ${numbers[3]}")
            }

            action.startsWith("write", ignoreCase = true) -> {
                val text = extractStringArg(action)
                if (text.length > 500) {
                    "Oprit pentru siguranță: textul de scris este prea lung."
                } else {
                    controller.executeCommand("scrie $text")
                }
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

            action.startsWith("wait", ignoreCase = true) -> {
                val numbers = extractNumbers(action)
                val ms = numbers.firstOrNull() ?: 700
                val safeMs = ms.coerceIn(100, 5000)

                try {
                    var waited = 0
                    while (waited < safeMs) {
                        if (stopManager.isStopRequested()) {
                            return "Așteptarea a fost oprită manual."
                        }
                        Thread.sleep(100)
                        waited += 100
                    }

                    "Am așteptat $safeMs ms."
                } catch (e: Exception) {
                    "Nu am putut aștepta."
                }
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
