package com.example.jarvisaccessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    lateinit var controller: JarvisController
        private set

    var currentPackageName: String? = null
        private set

    data class InstalledAppInfo(
        val label: String,
        val packageName: String,
        val isBlocked: Boolean
    )

    private val appSafetyManager = AppSafetyManager(this)

    override fun onServiceConnected() {
        try {
            super.onServiceConnected()
            instance = this
            controller = JarvisController(this)
            Log.d("JarvisAccessibility", "Service connected safely")
        } catch (e: Exception) {
            Log.e("JarvisAccessibility", "Crash prevented in onServiceConnected", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val packageName = event?.packageName?.toString()
            if (!packageName.isNullOrBlank()) {
                currentPackageName = packageName
            }
        } catch (e: Exception) {
            Log.e("JarvisAccessibility", "Crash prevented in onAccessibilityEvent", e)
        }
    }

    override fun onInterrupt() {
        Log.d("JarvisAccessibility", "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    fun getInstalledLaunchableApps(): List<InstalledAppInfo> {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val result = mutableListOf<InstalledAppInfo>()

        for (app in apps) {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName) ?: continue
            val label = packageManager.getApplicationLabel(app).toString()

            result.add(
                InstalledAppInfo(
                    label = label,
                    packageName = app.packageName,
                    isBlocked = appSafetyManager.isBlockedAppName(label) ||
                        appSafetyManager.isBlockedPackage(app.packageName)
                )
            )
        }

        return result.sortedBy { normalize(it.label) }
    }

    fun searchInstalledApps(query: String): List<InstalledAppInfo> {
        val normalizedQuery = normalize(query)

        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        return getInstalledLaunchableApps().filter { app ->
            normalize(app.label).contains(normalizedQuery) ||
                normalize(app.packageName).contains(normalizedQuery)
        }
    }

    fun openAppByName(appName: String): Boolean {
        val normalizedSearch = normalize(appName)

        val apps = getInstalledLaunchableApps()
            .filter { !it.isBlocked }

        val exact = apps.firstOrNull {
            normalize(it.label) == normalizedSearch
        }

        val contains = apps.firstOrNull {
            normalize(it.label).contains(normalizedSearch)
        }

        val match = exact ?: contains ?: return false

        val launchIntent = packageManager.getLaunchIntentForPackage(match.packageName)
            ?: return false

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
        return true
    }


    fun clickNodeByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)

        val node = nodes.firstOrNull { it.isVisibleToUser && it.isEnabled }
            ?: nodes.firstOrNull()
            ?: return false

        var clickable: AccessibilityNodeInfo? = node
        while (clickable != null && !clickable.isClickable) {
            clickable = clickable.parent
        }

        return clickable?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }

    fun scrollDown(): Boolean {
        val root = rootInActiveWindow ?: return false
        return performScroll(root, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    fun scrollUp(): Boolean {
        val root = rootInActiveWindow ?: return false
        return performScroll(root, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }

    private fun performScroll(node: AccessibilityNodeInfo?, action: Int): Boolean {
        if (node == null) return false

        if (node.isScrollable && node.performAction(action)) {
            return true
        }

        for (i in 0 until node.childCount) {
            if (performScroll(node.getChild(i), action)) {
                return true
            }
        }

        return false
    }

    fun readScreenText(): String {
        val root = rootInActiveWindow ?: return "Nu pot citi ecranul acum."
        val builder = StringBuilder()
        collectText(root, builder)

        return builder.toString()
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString("\n")
            .ifBlank { "Nu am găsit text pe ecran." }
    }

    private fun collectText(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return

        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            builder.append(text).append("\n")
        }

        val description = node.contentDescription?.toString()
        if (!description.isNullOrBlank()) {
            builder.append(description).append("\n")
        }

        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), builder)
        }
    }

    fun pressBack(): Boolean {
        return performBack()
    }

    fun pressHome(): Boolean {
        return performHome()
    }

    fun openRecents(): Boolean {
        return performRecents()
    }

    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 350): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    fun performBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun performRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }


    fun tap(x: Int, y: Int): Boolean {
        return tap(x.toFloat(), y.toFloat())
    }

    fun tap(x: Float, y: Float): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    fun writeText(text: String): Boolean {
        val node = rootInActiveWindow ?: return false
        val focused = findFocusedEditable(node) ?: return false

        val args = Bundle()
        args.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )

        return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun findFocusedEditable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null

        if (node.isFocused && node.isEditable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val result = findFocusedEditable(node.getChild(i))
            if (result != null) {
                return result
            }
        }

        return null
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

    companion object {
        var instance: JarvisAccessibilityService? = null
            private set
    }
}
