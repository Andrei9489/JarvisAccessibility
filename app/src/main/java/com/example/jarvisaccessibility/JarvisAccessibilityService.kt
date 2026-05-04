package com.example.jarvisaccessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.os.Bundle
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

    private val appSafetyManager = AppSafetyManager()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        controller = JarvisController(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString()
        if (!packageName.isNullOrBlank()) {
            currentPackageName = packageName
        }
    }

    override fun onInterrupt() {}

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

        val startsWith = apps.firstOrNull {
            normalize(it.label).startsWith(normalizedSearch)
        }

        val contains = apps.firstOrNull {
            normalize(it.label).contains(normalizedSearch) ||
                normalizedSearch.contains(normalize(it.label))
        }

        val selected = exact ?: startsWith ?: contains ?: return false

        val launchIntent = packageManager.getLaunchIntentForPackage(selected.packageName)
            ?: return false

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
        return true
    }

    fun clickNodeByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)

        for (node in nodes) {
            var clickableNode: AccessibilityNodeInfo? = node

            while (clickableNode != null) {
                if (clickableNode.isClickable) {
                    clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
                clickableNode = clickableNode.parent
            }
        }

        return false
    }

    fun writeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focusedNode = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false

        val arguments = Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )

        return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun scrollDown(): Boolean {
        return performScroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    fun scrollUp(): Boolean {
        return performScroll(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }

    private fun performScroll(action: Int): Boolean {
        val root = rootInActiveWindow ?: return false
        return scrollNode(root, action)
    }

    private fun scrollNode(node: AccessibilityNodeInfo, action: Int): Boolean {
        if (node.isScrollable) {
            if (node.performAction(action)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (scrollNode(child, action)) {
                return true
            }
        }

        return false
    }

    fun readScreenText(): String {
        val root = rootInActiveWindow ?: return ""
        val result = StringBuilder()
        collectText(root, result)
        return result.toString().trim()
    }

    private fun collectText(node: AccessibilityNodeInfo, result: StringBuilder) {
        val text = node.text?.toString()
        val description = node.contentDescription?.toString()

        if (!text.isNullOrBlank()) {
            result.append(text).append("\n")
        }

        if (!description.isNullOrBlank()) {
            result.append(description).append("\n")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectText(child, result)
        }
    }

    fun pressBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun pressHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun openRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    fun tap(x: Int, y: Int): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(endX.toFloat(), endY.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 400))
            .build()

        return dispatchGesture(gesture, null, null)
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
