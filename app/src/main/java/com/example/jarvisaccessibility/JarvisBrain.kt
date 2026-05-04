package com.example.jarvisaccessibility

class JarvisBrain {

    data class BrainResult(
        val type: String,
        val command: String,
        val reply: String
    )

    fun think(rawText: String): BrainResult {
        val original = rawText.trim()
        val text = normalize(original)

        if (original.isBlank()) {
            return BrainResult(
                type = "empty",
                command = "",
                reply = "Nu am primit nicio comandă, sir."
            )
        }

        val youtubeIntent = detectYoutubeIntent(original, text)
        if (youtubeIntent != null) return youtubeIntent

        val chromeIntent = detectChromeIntent(original, text)
        if (chromeIntent != null) return chromeIntent

        val appIntent = detectOpenAppIntent(original, text)
        if (appIntent != null) return appIntent

        return BrainResult(
            type = "command",
            command = original,
            reply = "Am înțeles, sir."
        )
    }

    private fun detectYoutubeIntent(original: String, text: String): BrainResult? {
        val mentionsYoutube =
            text.contains("youtube") ||
                text.contains("yootube") ||
                text.contains("you tube") ||
                text.contains("iutub") ||
                text.contains("yt")

        if (!mentionsYoutube) return null

        val wantsChrome =
            text.contains("chrome") ||
                text.contains("google chrome") ||
                text.contains("browser") ||
                text.contains("navigator")

        val wantsSearch =
            text.contains("cauta") ||
                text.contains("caută") ||
                text.contains("search") ||
                text.contains("pune") ||
                text.contains("play") ||
                text.contains("gaseste") ||
                text.contains("găsește") ||
                text.contains("melodie") ||
                text.contains("muzica") ||
                text.contains("muzică") ||
                text.contains("stire") ||
                text.contains("știre") ||
                text.contains("stiri") ||
                text.contains("știri") ||
                text.contains("content")

        if (wantsSearch) {
            val query = extractYoutubeQuery(original)

            val finalQuery = if (query.isBlank()) {
                "YouTube"
            } else {
                query
            }

            return if (wantsChrome) {
                BrainResult(
                    type = "command",
                    command = "caută site:youtube.com $finalQuery",
                    reply = "Caut pe YouTube în Google Chrome: $finalQuery, sir."
                )
            } else {
                BrainResult(
                    type = "command",
                    command = "caută YouTube $finalQuery",
                    reply = "Caut pe YouTube: $finalQuery, sir."
                )
            }
        }

        return if (wantsChrome) {
            BrainResult(
                type = "command",
                command = "caută youtube.com",
                reply = "Deschid YouTube în Google Chrome, sir."
            )
        } else {
            BrainResult(
                type = "command",
                command = "deschide YouTube",
                reply = "Deschid YouTube, sir."
            )
        }
    }

    private fun detectChromeIntent(original: String, text: String): BrainResult? {
        val mentionsChrome =
            text.contains("chrome") ||
                text.contains("google chrome") ||
                text.contains("browser") ||
                text.contains("navigator")

        if (!mentionsChrome) return null

        val wantsSearch =
            text.contains("cauta") ||
                text.contains("caută") ||
                text.contains("search") ||
                text.contains("gaseste") ||
                text.contains("găsește")

        if (wantsSearch) {
            val query = extractSearchQuery(original)

            if (query.isNotBlank()) {
                return BrainResult(
                    type = "command",
                    command = "caută $query",
                    reply = "Caut în Chrome: $query, sir."
                )
            }
        }

        return BrainResult(
            type = "command",
            command = "deschide Chrome",
            reply = "Deschid Google Chrome, sir."
        )
    }

    private fun detectOpenAppIntent(original: String, text: String): BrainResult? {
        val startsOpen =
            text.startsWith("deschide ") ||
                text.startsWith("open ") ||
                text.startsWith("porneste ") ||
                text.startsWith("pornește ")

        if (!startsOpen) return null

        val appName = original
            .replaceFirst("deschide", "", ignoreCase = true)
            .replaceFirst("open", "", ignoreCase = true)
            .replaceFirst("pornește", "", ignoreCase = true)
            .replaceFirst("porneste", "", ignoreCase = true)
            .trim()

        if (appName.isBlank()) return null

        return BrainResult(
            type = "command",
            command = "deschide $appName",
            reply = "Deschid $appName, sir."
        )
    }

    private fun extractYoutubeQuery(original: String): String {
        var value = original.trim()

        val phrases = listOf(
            "caută în youtube",
            "cauta in youtube",
            "caută pe youtube",
            "cauta pe youtube",
            "search youtube for",
            "search on youtube",
            "pune pe youtube",
            "play on youtube",
            "deschide youtube si cauta",
            "deschide youtube și caută",
            "youtube",
            "yootube",
            "you tube",
            "iutub",
            "melodie",
            "muzică",
            "muzica",
            "știre",
            "stire",
            "știri",
            "stiri",
            "content"
        )

        phrases.forEach {
            value = value.replace(it, "", ignoreCase = true).trim()
        }

        return cleanQuery(value)
    }

    private fun extractSearchQuery(original: String): String {
        var value = original.trim()

        val phrases = listOf(
            "caută în chrome",
            "cauta in chrome",
            "caută pe chrome",
            "cauta pe chrome",
            "caută în google chrome",
            "cauta in google chrome",
            "search chrome for",
            "search google chrome for",
            "chrome",
            "google chrome",
            "browser",
            "navigator",
            "caută",
            "cauta",
            "search",
            "găsește",
            "gaseste"
        )

        phrases.forEach {
            value = value.replace(it, "", ignoreCase = true).trim()
        }

        return cleanQuery(value)
    }

    private fun cleanQuery(value: String): String {
        return value
            .replace("te rog", "", ignoreCase = true)
            .replace("please", "", ignoreCase = true)
            .replace("pentru mine", "", ignoreCase = true)
            .replace("for me", "", ignoreCase = true)
            .replace("?", "")
            .replace(".", "")
            .replace(",", "")
            .trim()
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
