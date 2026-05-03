package com.example.jarvisaccessibility

import android.content.Intent
import android.net.Uri

class JarvisController(
    private val service: JarvisAccessibilityService
) {

    fun executeCommand(rawCommand: String): String {
        val command = normalize(rawCommand)

        if (command.isBlank()) {
            return "Comandă goală."
        }

        return when {
            command.startsWith("deschide ") -> {
                val appName = rawCommand.substringAfter("deschide", "").trim()
                if (appName.isBlank()) {
                    "Spune ce aplicație să deschid."
                } else {
                    val ok = service.openAppByName(appName)
                    if (ok) "Am deschis: $appName" else "Nu am găsit aplicația: $appName"
                }
            }

            command.startsWith("cauta ") || command.startsWith("căută ") -> {
                val query = rawCommand
                    .replaceFirst("caută", "", ignoreCase = true)
                    .replaceFirst("cauta", "", ignoreCase = true)
                    .trim()

                if (query.isBlank()) {
                    "Spune ce vrei să caut."
                } else {
                    openGoogleSearch(query)
                    "Caut pe Google: $query"
                }
            }

            command.startsWith("apasa pe ") || command.startsWith("apasă pe ") -> {
                val text = rawCommand
                    .replaceFirst("apasă pe", "", ignoreCase = true)
                    .replaceFirst("apasa pe", "", ignoreCase = true)
                    .trim()

                if (text.isBlank()) {
                    "Spune pe ce text să apăs."
                } else {
                    val ok = service.clickNodeByText(text)
                    if (ok) "Am apăsat pe: $text" else "Nu am găsit pe ecran: $text"
                }
            }

            command.startsWith("scrie ") -> {
                val text = rawCommand.substringAfter("scrie", "").trim()

                if (text.isBlank()) {
                    "Spune ce text să scriu."
                } else {
                    val ok = service.writeText(text)
                    if (ok) "Am scris: $text" else "Nu am găsit un câmp text activ."
                }
            }

            command == "scroll jos" || command == "deruleaza jos" || command == "derulează jos" -> {
                val ok = service.scrollDown()
                if (ok) "Am făcut scroll jos." else "Nu am putut face scroll jos."
            }

            command == "scroll sus" || command == "deruleaza sus" || command == "derulează sus" -> {
                val ok = service.scrollUp()
                if (ok) "Am făcut scroll sus." else "Nu am putut face scroll sus."
            }

            command == "citeste ecranul" || command == "citește ecranul" -> {
                val text = service.readScreenText()
                if (text.isBlank()) "Nu am găsit text pe ecran." else text
            }

            command == "inapoi" || command == "înapoi" || command == "back" -> {
                val ok = service.pressBack()
                if (ok) "Am apăsat Înapoi." else "Nu am putut apăsa Înapoi."
            }

            command == "home" || command == "acasă" || command == "acasa" -> {
                val ok = service.pressHome()
                if (ok) "Am mers la Home." else "Nu am putut merge la Home."
            }

            command == "recente" || command == "aplicatii recente" || command == "aplicații recente" -> {
                val ok = service.openRecents()
                if (ok) "Am deschis aplicațiile recente." else "Nu am putut deschide recente."
            }

            command.startsWith("tap ") -> {
                val parts = command.split(" ")
                if (parts.size < 3) {
                    "Format corect: tap 500 800"
                } else {
                    val x = parts[1].toIntOrNull()
                    val y = parts[2].toIntOrNull()

                    if (x == null || y == null) {
                        "Coordonate invalide. Exemplu: tap 500 800"
                    } else {
                        val ok = service.tap(x, y)
                        if (ok) "Am făcut tap la $x, $y." else "Nu am putut face tap."
                    }
                }
            }

            command.startsWith("swipe ") -> {
                val parts = command.split(" ")
                if (parts.size < 5) {
                    "Format corect: swipe 500 1200 500 300"
                } else {
                    val startX = parts[1].toIntOrNull()
                    val startY = parts[2].toIntOrNull()
                    val endX = parts[3].toIntOrNull()
                    val endY = parts[4].toIntOrNull()

                    if (startX == null || startY == null || endX == null || endY == null) {
                        "Coordonate invalide. Exemplu: swipe 500 1200 500 300"
                    } else {
                        val ok = service.swipe(startX, startY, endX, endY)
                        if (ok) "Am făcut swipe." else "Nu am putut face swipe."
                    }
                }
            }

            else -> {
                "Comandă necunoscută: $rawCommand"
            }
        }
    }

    private fun openGoogleSearch(query: String) {
        val encoded = Uri.encode(query)
        val url = "https://www.google.com/search?q=$encoded"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        service.startActivity(intent)
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
