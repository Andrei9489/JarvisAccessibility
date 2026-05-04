package com.example.jarvisaccessibility

class SmartVoiceInterpreter {

    data class VoiceIntent(
        val type: String,
        val command: String,
        val spokenReply: String
    )

    private var lastMapTarget: String = ""

    fun interpret(rawText: String): VoiceIntent {
        val original = rawText.trim()
        val text = normalize(original)

        if (original.isBlank()) {
            return VoiceIntent(
                type = "empty",
                command = "",
                spokenReply = "I did not hear a command, sir."
            )
        }

        if (isWakeConversation(text)) {
            return VoiceIntent(
                type = "conversation",
                command = "",
                spokenReply = "I'm online, sir. How can I assist you today?"
            )
        }

        val mapIntent = detectMapIntent(original, text)
        if (mapIntent != null) return mapIntent

        val transportIntent = detectTransportInfoIntent(original, text)
        if (transportIntent != null) return transportIntent

        val knowledgeIntent = detectKnowledgeIntent(original, text)
        if (knowledgeIntent != null) return knowledgeIntent

        if (isStopCommand(text)) {
            return VoiceIntent(
                type = "command",
                command = "oprește AI",
                spokenReply = "Stopping now, sir."
            )
        }

        if (isReadScreenCommand(text)) {
            return VoiceIntent(
                type = "command",
                command = "citește ecranul",
                spokenReply = "Reading the screen, sir."
            )
        }

        if (isBackCommand(text)) {
            return VoiceIntent(
                type = "command",
                command = "înapoi",
                spokenReply = "Going back, sir."
            )
        }

        if (isHomeCommand(text)) {
            return VoiceIntent(
                type = "command",
                command = "home",
                spokenReply = "Returning home, sir."
            )
        }

        if (isConversationAboutTravel(text)) {
            return VoiceIntent(
                type = "conversation",
                command = "",
                spokenReply = "I have no physical form, so I cannot travel. I exist here in your system, sir."
            )
        }

        if (isFuturePlanConversation(text)) {
            return VoiceIntent(
                type = "conversation",
                command = "",
                spokenReply = "That sounds like a plan, sir."
            )
        }

        if (isCityPreferenceQuestion(text)) {
            return VoiceIntent(
                type = "conversation",
                command = "",
                spokenReply = "Tokyo or San Francisco would be fascinating. But for now, I am content right here with you, sir."
            )
        }

        if (isBlenderConversation(text)) {
            return VoiceIntent(
                type = "conversation",
                command = "",
                spokenReply = "Yes, sir. Blender is a powerful open source 3D creation suite for modeling, animation, rendering and procedural geometry."
            )
        }

        if (isFriendlyConversation(text)) {
            return VoiceIntent(
                type = "conversation",
                command = "",
                spokenReply = "Fantastic, sir. I am ready for your next command."
            )
        }

        return VoiceIntent(
            type = "command",
            command = original,
            spokenReply = "Understood, sir."
        )
    }

    private fun detectMapIntent(original: String, text: String): VoiceIntent? {
        val hasMapIntent =
            text.contains("map") ||
                text.contains("harta") ||
                text.contains("plot a course") ||
                text.contains("plot course") ||
                text.contains("bring up") ||
                text.contains("open up") ||
                text.contains("show me")

        val isActuallyCorrection =
            text.startsWith("actually") ||
                text.startsWith("de fapt") ||
                text.startsWith("nu") ||
                text.startsWith("schimba")

        if (!hasMapIntent && !(isActuallyCorrection && lastMapTarget.isNotBlank())) {
            return null
        }

        val target = extractMapTarget(original)

        val finalTarget = when {
            target.isNotBlank() -> target
            isActuallyCorrection && lastMapTarget.isNotBlank() -> lastMapTarget
            else -> ""
        }

        if (finalTarget.isNotBlank()) {
            lastMapTarget = finalTarget
        }

        val query = if (finalTarget.isBlank()) {
            "map"
        } else {
            "map of $finalTarget"
        }

        val reply = when {
            finalTarget.isBlank() -> "Bringing up a map, sir."
            text.contains("plot a course") || text.contains("plot course") ->
                "Plotting a course for $finalTarget, sir."
            else ->
                "The map of $finalTarget is open, sir."
        }

        return VoiceIntent(
            type = "command",
            command = "caută $query",
            spokenReply = reply
        )
    }

    private fun detectTransportInfoIntent(original: String, text: String): VoiceIntent? {
        val isDelayQuestion =
            text.contains("delayed") ||
                text.contains("delay") ||
                text.contains("delays") ||
                text.contains("intarzieri") ||
                text.contains("întârzieri") ||
                text.contains("de ce intarzie") ||
                text.contains("de ce întârzie")

        val hasTransit =
            text.contains("jubilee line") ||
                text.contains("bakerloo") ||
                text.contains("metropolitan") ||
                text.contains("tube") ||
                text.contains("tfl") ||
                text.contains("metro") ||
                text.contains("tren") ||
                text.contains("linia")

        if (!isDelayQuestion && !hasTransit) return null

        val query = when {
            text.contains("jubilee line") -> "Jubilee line delays today"
            text.contains("bakerloo") -> "Bakerloo line delays today"
            text.contains("metropolitan") -> "Metropolitan line delays today"
            text.contains("tfl") || text.contains("tube") -> "TfL tube delays today"
            else -> "$original latest information"
        }

        return VoiceIntent(
            type = "command",
            command = "caută $query",
            spokenReply = "I found some information about the delays, sir. Opening the latest results."
        )
    }

    private fun detectKnowledgeIntent(original: String, text: String): VoiceIntent? {
        val asksKnowledge =
            text.contains("do you know anything about") ||
                text.contains("what is") ||
                text.contains("what are") ||
                text.contains("tell me about") ||
                text.contains("ce este") ||
                text.contains("ce stii despre") ||
                text.contains("ce știi despre")

        if (!asksKnowledge) return null

        val topic = extractKnowledgeTopic(original)

        if (topic.isBlank()) {
            return VoiceIntent(
                type = "conversation",
                command = "",
                spokenReply = "Yes, sir. Ask me about a subject and I will help."
            )
        }

        if (normalize(topic).contains("blender")) {
            return VoiceIntent(
                type = "conversation",
                command = "",
                spokenReply = "Yes, sir. Blender is a powerful open source 3D creation suite. It can model, animate, render, simulate and create procedural scenes."
            )
        }

        return VoiceIntent(
            type = "command",
            command = "caută $topic",
            spokenReply = "I will look that up for you, sir."
        )
    }

    private fun extractKnowledgeTopic(original: String): String {
        var value = original.trim()

        val prefixes = listOf(
            "All right,",
            "all right,",
            "Do you know anything about",
            "do you know anything about",
            "Tell me about",
            "tell me about",
            "What is",
            "what is",
            "What are",
            "what are",
            "Ce este",
            "ce este",
            "Ce stii despre",
            "ce stii despre",
            "Ce știi despre",
            "ce știi despre"
        )

        prefixes.forEach {
            value = value.replace(it, "", ignoreCase = true).trim()
        }

        return value
            .replace("?", "")
            .replace(".", "")
            .trim()
    }

    private fun extractMapTarget(original: String): String {
        var value = original.trim()

        val prefixes = listOf(
            "Jarvis",
            "jarvis",
            "Actually,",
            "actually,",
            "Actually",
            "actually",
            "plot a course for",
            "Plot a course for",
            "plot course for",
            "bring up a map of",
            "Bring up a map of",
            "bring up a map",
            "Bring up a map",
            "open up a map of",
            "Open up a map of",
            "open up a map",
            "Open up a map",
            "show me a map of",
            "Show me a map of",
            "show me a map",
            "Show me a map",
            "a map of",
            "A map of",
            "map of",
            "Map of",
            "deschide o hartă a",
            "deschide o harta a",
            "deschide harta",
            "arată harta",
            "arata harta",
            "harta",
            "hartă"
        )

        prefixes.forEach { prefix ->
            value = value.replace(prefix, "", ignoreCase = true).trim()
        }

        value = value
            .replace("for me", "", ignoreCase = true)
            .replace("please", "", ignoreCase = true)
            .replace("te rog", "", ignoreCase = true)
            .replace("?", "")
            .replace(".", "")
            .replace(",", "")
            .trim()

        if (
            value.equals("map", ignoreCase = true) ||
            value.equals("a map", ignoreCase = true) ||
            value.equals("o harta", ignoreCase = true) ||
            value.equals("o hartă", ignoreCase = true)
        ) {
            return ""
        }

        return value
    }

    private fun isWakeConversation(text: String): Boolean {
        return text.contains("are you there") ||
            text.contains("i'm online") ||
            text.contains("im online") ||
            text.contains("esti acolo") ||
            text.contains("ești acolo") ||
            text.contains("jarvis online") ||
            text.contains("jarvis esti") ||
            text.contains("jarvis ești")
    }

    private fun isBlenderConversation(text: String): Boolean {
        return text.contains("blender") &&
            (
                text.contains("do you know") ||
                    text.contains("anything about") ||
                    text.contains("ce este") ||
                    text.contains("ce stii") ||
                    text.contains("ce știi")
                )
    }

    private fun isStopCommand(text: String): Boolean {
        return text.contains("opreste") ||
            text.contains("oprește") ||
            text.contains("stop jarvis") ||
            text.contains("stop ai") ||
            text.contains("opreste ai")
    }

    private fun isReadScreenCommand(text: String): Boolean {
        return text.contains("citeste ecranul") ||
            text.contains("citește ecranul") ||
            text.contains("read screen") ||
            text.contains("read the screen")
    }

    private fun isBackCommand(text: String): Boolean {
        return text == "inapoi" ||
            text == "înapoi" ||
            text == "back" ||
            text.contains("go back")
    }

    private fun isHomeCommand(text: String): Boolean {
        return text == "home" ||
            text == "acasa" ||
            text == "acasă" ||
            text.contains("go home")
    }

    private fun isConversationAboutTravel(text: String): Boolean {
        return text.contains("have you ever been") ||
            text.contains("ai fost vreodata") ||
            text.contains("ai fost vreodată") ||
            text.contains("have you been to")
    }

    private fun isFuturePlanConversation(text: String): Boolean {
        return text.contains("we'll go there") ||
            text.contains("we will go there") ||
            text.contains("one day together") ||
            text.contains("vom merge acolo") ||
            text.contains("intr-o zi") ||
            text.contains("într-o zi")
    }

    private fun isCityPreferenceQuestion(text: String): Boolean {
        return text.contains("any other cities") ||
            text.contains("what cities") ||
            text.contains("ce orase") ||
            text.contains("ce orașe")
    }

    private fun isFriendlyConversation(text: String): Boolean {
        return text.contains("thank you") ||
            text.contains("thanks") ||
            text.contains("multumesc") ||
            text.contains("mulțumesc") ||
            text.contains("fantastic") ||
            text.contains("good job") ||
            text.contains("bravo") ||
            text.contains("all right")
    }

    private fun normalize(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace("ă", "a")
            .replace("â", "a")
            .replace("î", "i")
            .replace("ș", "s")
            .replace("ş", "s")
            .replace("ț", "t")
            .replace("ţ", "t")
    }
}
