package com.example.jarvisaccessibility

class JarvisController(
    private val service: JarvisAccessibilityService
) {

    fun executeCommand(commandRaw: String): String {
        val command = commandRaw.trim()

        if (command.isBlank()) {
            return "Comandă goală."
        }

        val lower = command.lowercase()

        return when {
            lower.startsWith("deschide ") -> {
                val appName = command.removePrefixIgnoreCase("deschide ").trim()
                val ok = service.openAppByName(appName)

                if (ok) "Am deschis $appName."
                else "Nu am găsit aplicația $appName."
            }

            lower.startsWith("apasă pe ") -> {
                val target = command.removePrefixIgnoreCase("apasă pe ").trim()
                val ok = service.clickNodeByText(target)

                if (ok) "Am apăsat pe $target."
                else "Nu am găsit pe ecran: $target."
            }

            lower.startsWith("apasa pe ") -> {
                val target = command.removePrefixIgnoreCase("apasa pe ").trim()
                val ok = service.clickNodeByText(target)

                if (ok) "Am apăsat pe $target."
                else "Nu am găsit pe ecran: $target."
            }

            lower.startsWith("scrie ") -> {
                val text = command.removePrefixIgnoreCase("scrie ").trim()
                val ok = service.writeText(text)

                if (ok) "Am scris: $text."
                else "Nu există un câmp text selectat."
            }

            lower == "scroll jos" -> {
                service.scrollDown()
                "Am făcut scroll jos."
            }

            lower == "scroll sus" -> {
                service.scrollUp()
                "Am făcut scroll sus."
            }

            lower.startsWith("caută ") -> {
                val query = command.removePrefixIgnoreCase("caută ").trim()
                val wrote = service.writeText(query)

                if (wrote) {
                    service.tapEnterLikeAction()
                    "Am căutat: $query."
                } else {
                    "Nu există un câmp de căutare selectat."
                }
            }

            lower.startsWith("cauta ") -> {
                val query = command.removePrefixIgnoreCase("cauta ").trim()
                val wrote = service.writeText(query)

                if (wrote) {
                    service.tapEnterLikeAction()
                    "Am căutat: $query."
                } else {
                    "Nu există un câmp de căutare selectat."
                }
            }

            lower == "citește ecranul" || lower == "citeste ecranul" -> {
                val text = service.readScreenText()

                if (text.isBlank()) "Nu am găsit text pe ecran."
                else text
            }

            lower == "înapoi" || lower == "inapoi" -> {
                service.pressBack()
                "Înapoi."
            }

            lower == "home" || lower == "acasă" || lower == "acasa" -> {
                service.pressHome()
                "Home."
            }

            lower == "recente" -> {
                service.openRecents()
                "Am deschis aplicațiile recente."
            }

            lower.startsWith("tap ") -> {
                val parts = lower.removePrefix("tap ").split(" ")

                if (parts.size >= 2) {
                    val x = parts[0].toIntOrNull()
                    val y = parts[1].toIntOrNull()

                    if (x != null && y != null) {
                        service.tap(x, y)
                        "Tap la coordonatele $x, $y."
                    } else {
                        "Coordonate invalide."
                    }
                } else {
                    "Format corect: tap 500 800"
                }
            }

            lower.startsWith("swipe ") -> {
                val parts = lower.removePrefix("swipe ").split(" ")

                if (parts.size >= 4) {
                    val x1 = parts[0].toIntOrNull()
                    val y1 = parts[1].toIntOrNull()
                    val x2 = parts[2].toIntOrNull()
                    val y2 = parts[3].toIntOrNull()

                    if (x1 != null && y1 != null && x2 != null && y2 != null) {
                        service.swipe(x1, y1, x2, y2)
                        "Swipe de la $x1,$y1 la $x2,$y2."
                    } else {
                        "Coordonate invalide."
                    }
                } else {
                    "Format corect: swipe 500 1200 500 300"
                }
            }

            else -> "Comandă necunoscută: $command"
        }
    }

    private fun String.removePrefixIgnoreCase(prefix: String): String {
        return if (this.lowercase().startsWith(prefix.lowercase())) {
            this.substring(prefix.length)
        } else {
            this
        }
    }
}
