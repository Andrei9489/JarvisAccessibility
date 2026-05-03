package com.example.jarvisaccessibility

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(
    private val context: Context
) {

    data class UpdateInfo(
        val hasUpdate: Boolean,
        val currentVersionCode: Int,
        val latestVersionCode: Int,
        val latestVersionName: String,
        val notes: String,
        val releaseUrl: String
    )

    fun checkForUpdates(callback: (UpdateInfo?, String?) -> Unit) {
        Thread {
            try {
                val jsonUrl =
                    "https://raw.githubusercontent.com/Andrei9489/JarvisAccessibility/main/version.json"

                val connection = URL(jsonUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val latestVersionCode = json.getInt("versionCode")
                val latestVersionName = json.getString("versionName")
                val notes = json.optString("notes", "")
                val releaseUrl = json.getString("releaseUrl")

                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = packageInfo.versionCode

                val info = UpdateInfo(
                    hasUpdate = latestVersionCode > currentVersionCode,
                    currentVersionCode = currentVersionCode,
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    notes = notes,
                    releaseUrl = releaseUrl
                )

                callback(info, null)
            } catch (e: Exception) {
                callback(null, e.message ?: "Eroare necunoscută la verificarea update-ului.")
            }
        }.start()
    }

    fun openReleasePage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
