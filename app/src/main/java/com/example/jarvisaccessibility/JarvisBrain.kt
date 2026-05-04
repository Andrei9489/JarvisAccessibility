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

        detectOpenAppAndSearchIntent(original, text)?.let { return it }
        detectInstalledAppsIntent(original, text)?.let { return it }
        detectWebsiteIntent(original, text)?.let { return it }
        detectPlatformSearchIntent(original, text)?.let { return it }
        detectYoutubeIntent(original, text)?.let { return it }
        detectChromeIntent(original, text)?.let { return it }
        detectUniversalOpenAppIntent(original, text)?.let { return it }

        return BrainResult(
            type = "command",
            command = original,
            reply = "Am înțeles, sir."
        )
    }



    private fun detectOpenAppAndSearchIntent(original: String, text: String): BrainResult? {
        val connectors = listOf(
            " si cauta ",
            " și caută ",
            " si caută ",
            " și cauta ",
            " and search ",
            " and look for "
        )

        val connector = connectors.firstOrNull { text.contains(it.trim()) || text.contains(it) }
            ?: return null

        val parts = original.split(
            " și caută",
            " si cauta",
            " si caută",
            " și cauta",
            " and search",
            " and look for",
            ignoreCase = true,
            limit = 2
        )

        if (parts.size < 2) return null

        val openPart = parts[0].trim()
        val searchPart = cleanQuery(parts[1])

        if (searchPart.isBlank()) return null

        val appName = openPart
            .replaceFirst("deschide aplicația", "", ignoreCase = true)
            .replaceFirst("deschide aplicatia", "", ignoreCase = true)
            .replaceFirst("deschide", "", ignoreCase = true)
            .replaceFirst("intră pe", "", ignoreCase = true)
            .replaceFirst("intra pe", "", ignoreCase = true)
            .replaceFirst("intră în", "", ignoreCase = true)
            .replaceFirst("intra in", "", ignoreCase = true)
            .replaceFirst("open", "", ignoreCase = true)
            .replaceFirst("launch", "", ignoreCase = true)
            .trim()

        if (appName.isBlank()) return null

        val canonicalApp = canonicalPlatformName(appName)

        return BrainResult(
            type = "command",
            command = "deschide aplicația $canonicalApp și caută $searchPart",
            reply = "Deschid $canonicalApp și caut $searchPart, sir."
        )
    }

    private fun canonicalPlatformName(value: String): String {
        val text = normalize(value)

        return when {
            text.contains("yootube") || text.contains("you tube") || text.contains("youtube") || text.contains("iutub") -> "YouTube"
            text.contains("tik tok") || text.contains("tiktok") -> "TikTok"
            text.contains("insta") || text.contains("instagram") -> "Instagram"
            text.contains("netflix") -> "Netflix"
            text.contains("spotify") -> "Spotify"
            text.contains("chrome") || text.contains("google chrome") || text.contains("browser") -> "Chrome"
            text.contains("facebook") || text.contains("face book") -> "Facebook"
            else -> value.trim()
        }
    }


    private fun detectInstalledAppsIntent(original: String, text: String): BrainResult? {
        if (
            text.contains("listeaza aplicatiile") ||
            text.contains("listeaza aplicațiile") ||
            text.contains("lista aplicatii") ||
            text.contains("listă aplicații") ||
            text.contains("lista de aplicatii") ||
            text.contains("listă de aplicații") ||
            text.contains("lista de placați") ||
            text.contains("lista de placati") ||
            text.contains("deschide lista de aplicatii") ||
            text.contains("deschide lista de aplicații") ||
            text.contains("deschide lista de placați") ||
            text.contains("deschide lista de placati") ||
            text.contains("ce aplicatii am") ||
            text.contains("ce aplicații am") ||
            text.contains("aplicatii instalate") ||
            text.contains("aplicații instalate")
        ) {
            return BrainResult(
                type = "command",
                command = "listează aplicațiile instalate",
                reply = "Listez aplicațiile instalate, sir."
            )
        }

        if (
            text.startsWith("cauta aplicatia ") ||
            text.startsWith("cauta aplicația ") ||
            text.startsWith("caută aplicatia ") ||
            text.startsWith("caută aplicația ") ||
            text.startsWith("gaseste aplicatia ") ||
            text.startsWith("găsește aplicația ")
        ) {
            val appName = original
                .replaceFirst("caută aplicația", "", ignoreCase = true)
                .replaceFirst("caută aplicatia", "", ignoreCase = true)
                .replaceFirst("cauta aplicația", "", ignoreCase = true)
                .replaceFirst("cauta aplicatia", "", ignoreCase = true)
                .replaceFirst("găsește aplicația", "", ignoreCase = true)
                .replaceFirst("gaseste aplicatia", "", ignoreCase = true)
                .trim()

            return BrainResult(
                type = "command",
                command = "caută aplicația $appName",
                reply = "Caut aplicația $appName, sir."
            )
        }

        return null
    }

    private fun detectWebsiteIntent(original: String, text: String): BrainResult? {
        val wantsWebsite =
            text.contains("site") ||
                text.contains("website") ||
                text.contains("web site") ||
                text.contains(".com") ||
                text.contains(".ro") ||
                text.contains(".net") ||
                text.contains(".org")

        if (!wantsWebsite) return null

        var target = original
            .replace("intră pe site-ul", "", ignoreCase = true)
            .replace("intra pe site-ul", "", ignoreCase = true)
            .replace("intră pe site", "", ignoreCase = true)
            .replace("intra pe site", "", ignoreCase = true)
            .replace("deschide site-ul", "", ignoreCase = true)
            .replace("deschide site", "", ignoreCase = true)
            .replace("deschide website-ul", "", ignoreCase = true)
            .replace("deschide website", "", ignoreCase = true)
            .replace("open website", "", ignoreCase = true)
            .replace("open site", "", ignoreCase = true)
            .trim()
            .trim('.', ',', '?', ' ')

        if (target.isBlank()) {
            return BrainResult(
                type = "conversation",
                command = "",
                reply = "Spune-mi ce website vrei să deschid, sir."
            )
        }

        if (!target.startsWith("http://", ignoreCase = true) &&
            !target.startsWith("https://", ignoreCase = true)
        ) {
            target = "https://$target"
        }

        return BrainResult(
            type = "command",
            command = "caută $target",
            reply = "Deschid website-ul $target, sir."
        )
    }

    private fun detectPlatformSearchIntent(original: String, text: String): BrainResult? {
        val platforms = mapOf(
            "youtube" to "YouTube",
            "yootube" to "YouTube",
            "you tube" to "YouTube",
            "tiktok" to "TikTok",
            "tik tok" to "TikTok",
            "instagram" to "Instagram",
            "facebook" to "Facebook",
            "netflix" to "Netflix",
            "prime video" to "Prime Video",
            "disney" to "Disney",
            "spotify" to "Spotify",
            "google" to "Google"
        )

        val platform = platforms.entries.firstOrNull { text.contains(it.key) }?.value ?: return null

        val wantsSearch =
            text.contains("cauta") ||
                text.contains("caută") ||
                text.contains("search") ||
                text.contains("gaseste") ||
                text.contains("găsește") ||
                text.contains("pune") ||
                text.contains("play") ||
                text.contains("video") ||
                text.contains("film") ||
                text.contains("serial") ||
                text.contains("melodie") ||
                text.contains("muzica") ||
                text.contains("muzică") ||
                text.contains("stire") ||
                text.contains("știre") ||
                text.contains("stiri") ||
                text.contains("știri") ||
                text.contains("content")

        if (!wantsSearch) return null

        val query = extractPlatformQuery(original, platform).ifBlank {
            return BrainResult(
                type = "command",
                command = "deschide $platform",
                reply = "Deschid $platform, sir."
            )
        }

        val command = when (platform) {
            "YouTube" -> "caută YouTube $query"
            "TikTok" -> "caută TikTok $query"
            "Instagram" -> "caută Instagram $query"
            "Facebook" -> "caută Facebook $query"
            "Netflix" -> "caută Netflix $query"
            "Prime Video" -> "caută Prime Video $query"
            "Disney" -> "caută Disney Plus $query"
            "Spotify" -> "caută Spotify $query"
            else -> "caută $platform $query"
        }

        return BrainResult(
            type = "command",
            command = command,
            reply = "Caut în $platform: $query, sir."
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
                text.contains("video") ||
                text.contains("film") ||
                text.contains("serial") ||
                text.contains("content")

        if (wantsSearch) {
            val query = extractYoutubeQuery(original).ifBlank { "YouTube" }

            return if (wantsChrome) {
                BrainResult(
                    type = "command",
                    command = "caută site:youtube.com $query",
                    reply = "Caut pe YouTube în Google Chrome: $query, sir."
                )
            } else {
                BrainResult(
                    type = "command",
                    command = "caută YouTube $query",
                    reply = "Caut pe YouTube: $query, sir."
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

    private fun detectUniversalOpenAppIntent(original: String, text: String): BrainResult? {
        val startsOpen =
            text.startsWith("deschide ") ||
                text.startsWith("open ") ||
                text.startsWith("porneste ") ||
                text.startsWith("pornește ") ||
                text.startsWith("intra pe ") ||
                text.startsWith("intră pe ") ||
                text.startsWith("intra in ") ||
                text.startsWith("intră în ") ||
                text.startsWith("lanseaza ") ||
                text.startsWith("lansează ")

        if (!startsOpen) return null

        val appName = original
            .replaceFirst("deschide aplicația", "", ignoreCase = true)
            .replaceFirst("deschide aplicatia", "", ignoreCase = true)
            .replaceFirst("deschide", "", ignoreCase = true)
            .replaceFirst("open", "", ignoreCase = true)
            .replaceFirst("pornește", "", ignoreCase = true)
            .replaceFirst("porneste", "", ignoreCase = true)
            .replaceFirst("intră pe", "", ignoreCase = true)
            .replaceFirst("intra pe", "", ignoreCase = true)
            .replaceFirst("intră în", "", ignoreCase = true)
            .replaceFirst("intra in", "", ignoreCase = true)
            .replaceFirst("lansează", "", ignoreCase = true)
            .replaceFirst("lanseaza", "", ignoreCase = true)
            .trim()
            .trim('.', ',', '?', ' ')

        if (appName.isBlank()) return null

        return BrainResult(
            type = "command",
            command = "deschide $appName",
            reply = "Deschid $appName, sir."
        )
    }

    private fun extractPlatformQuery(original: String, platform: String): String {
        var value = original.trim()

        val platformWords = when (platform) {
            "YouTube" -> listOf("youtube", "yootube", "you tube", "iutub", "yt")
            "TikTok" -> listOf("tiktok", "tik tok")
            "Instagram" -> listOf("instagram")
            "Facebook" -> listOf("facebook")
            "Netflix" -> listOf("netflix")
            "Prime Video" -> listOf("prime video")
            "Disney" -> listOf("disney plus", "disney")
            "Spotify" -> listOf("spotify")
            else -> listOf(platform)
        }

        val phrases = mutableListOf(
            "caută în",
            "cauta in",
            "caută pe",
            "cauta pe",
            "search in",
            "search on",
            "search",
            "pune pe",
            "pune",
            "play on",
            "play",
            "găsește",
            "gaseste",
            "video",
            "film",
            "serial",
            "melodie",
            "muzică",
            "muzica",
            "știre",
            "stire",
            "știri",
            "stiri",
            "content"
        )

        phrases.addAll(platformWords)

        phrases.forEach {
            value = value.replace(it, "", ignoreCase = true).trim()
        }

        return cleanQuery(value)
    }

    private fun extractYoutubeQuery(original: String): String {
        return extractPlatformQuery(original, "YouTube")
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
