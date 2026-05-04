package com.example.jarvisaccessibility

class AppSafetyManager {

    private val blockedAppNames = listOf(
        "mobile banking",
        "mobile bank",
        "banking",
        "bank",
        "banca",
        "bancă",
        "my brd",
        "brd",
        "bt pay",
        "bt24",
        "neo bt",
        "banca transilvania",
        "george",
        "bcr",
        "raiffeisen",
        "smart mobile",
        "ing homebank",
        "homebank",
        "revolut",
        "paypal business",
        "paypal",
        "up mobil",
        "up mobil+",
        "samsung pass",
        "pass",
        "portofel",
        "wallet",
        "metamask",
        "crypto",
        "binance"
    )

    private val blockedPackageKeywords = listOf(
        "bank",
        "banking",
        "mobilebanking",
        "mobile.banking",
        "banca",
        "brd",
        "bcr",
        "bt24",
        "bancatransilvania",
        "transilvania",
        "raiffeisen",
        "ing",
        "homebank",
        "george",
        "revolut",
        "paypal",
        "wallet",
        "portofel",
        "metamask",
        "binance",
        "crypto",
        "samsungpass",
        "samsung.android.samsungpass",
        "upromania",
        "upmobil"
    )

    fun isBlockedAppName(appNameRaw: String): Boolean {
        val appName = normalize(appNameRaw)

        return blockedAppNames.any { blocked ->
            appName.contains(normalize(blocked))
        }
    }

    fun isBlockedPackage(packageNameRaw: String?): Boolean {
        if (packageNameRaw.isNullOrBlank()) return false

        val packageName = normalize(packageNameRaw)

        return blockedPackageKeywords.any { blocked ->
            packageName.contains(normalize(blocked))
        }
    }

    fun blockedMessage(appNameRaw: String): String {
        return "Blocat pentru siguranță: Jarvis nu controlează aplicații bancare, portofele, parole sau plăți: $appNameRaw"
    }

    fun blockedCurrentAppMessage(packageNameRaw: String?): String {
        return "Blocat pentru siguranță: Jarvis nu execută acțiuni în aplicația curentă: ${packageNameRaw ?: "necunoscută"}"
    }

    fun listBlockedApps(): String {
        return buildString {
            append("Aplicații blocate pentru siguranță:\n")
            blockedAppNames.distinct().sorted().forEach {
                append("- ").append(it).append("\n")
            }
        }.trim()
    }

    fun checkBlockedApp(appNameRaw: String): String {
        return if (isBlockedAppName(appNameRaw)) {
            "Da, aplicația este blocată: $appNameRaw"
        } else {
            "Nu, aplicația nu este blocată: $appNameRaw"
        }
    }

    fun checkBlockedPackage(packageNameRaw: String): String {
        return if (isBlockedPackage(packageNameRaw)) {
            "Da, pachetul este blocat: $packageNameRaw"
        } else {
            "Nu, pachetul nu este blocat: $packageNameRaw"
        }
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
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "")
            .replace(".", "")
    }
}
