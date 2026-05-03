package com.example.jarvisaccessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        var instance: JarvisAccessibilityService? = null
            private set
    }

    lateinit var controller: JarvisController
        private set

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        controller = JarvisController(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun tap(x: Int, y: Int): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    fun swipe(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long = 500
    ): Boolean {
        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(endX.toFloat(), endY.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    fun writeText(text: String): Boolean {
        val focusedNode = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: return false

        val args = Bundle()
        args.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )

        return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    fun tapEnterLikeAction(): Boolean {
        val focusedNode = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: return false

        return focusedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun clickNodeByText(text: String): Boolean {
        val node = findNodeByText(text) ?: return false

        return if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            clickFirstClickableParent(node)
        }
    }

    fun clickNodeByViewId(viewId: String): Boolean {
        val node = findNodeByViewId(viewId) ?: return false

        return if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            clickFirstClickableParent(node)
        }
    }

    private fun clickFirstClickableParent(node: AccessibilityNodeInfo?): Boolean {
        var current = node?.parent

        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }

            current = current.parent
        }

        return false
    }

    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByText(text)

        return nodes.firstOrNull { node ->
            node.isVisibleToUser
        }
    }

    fun findNodeByViewId(viewId: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)

        return nodes.firstOrNull { node ->
            node.isVisibleToUser
        }
    }

    fun readScreenText(): String {
        val root = rootInActiveWindow ?: return ""
        val builder = StringBuilder()
        collectText(root, builder)
        return builder.toString().trim()
    }

    private fun collectText(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return

        val text = node.text?.toString()
        val contentDescription = node.contentDescription?.toString()

        if (!text.isNullOrBlank()) {
            builder.append(text).append("\n")
        }

        if (!contentDescription.isNullOrBlank()) {
            builder.append(contentDescription).append("\n")
        }

        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), builder)
        }
    }

    fun openAppByName(appNameRaw: String): Boolean {
        val appName = normalizeAppName(appNameRaw)
        val pm = packageManager

        val knownPackages = mapOf(
            "chrome" to "com.android.chrome",
            "google chrome" to "com.android.chrome",
            "youtube" to "com.google.android.youtube",
            "gmail" to "com.google.android.gm",
            "whatsapp" to "com.whatsapp",
            "facebook" to "com.facebook.katana",
            "messenger" to "com.facebook.orca",
            "tiktok" to "com.zhiliaoapp.musically",
            "instagram" to "com.instagram.android",
            "telegram" to "org.telegram.messenger",
            "settings" to "com.android.settings",
            "setari" to "com.android.settings",
            "setări" to "com.android.settings",
            "termux" to "com.termux"
        )

        val directPackage = knownPackages[appName]
        if (directPackage != null) {
            val directIntent = pm.getLaunchIntentForPackage(directPackage)
            if (directIntent != null) {
                directIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(directIntent)
                return true
            }
        }

        val launchIntent = Intent(Intent.ACTION_MAIN, null)
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = pm.queryIntentActivities(launchIntent, 0)

        val match = apps.firstOrNull { info ->
            val label = info.loadLabel(pm).toString()
            normalizeAppName(label).contains(appName) ||
                appName.contains(normalizeAppName(label)) ||
                info.activityInfo.packageName.contains(appName, ignoreCase = true)
        } ?: return false

        val intent = pm.getLaunchIntentForPackage(match.activityInfo.packageName)
            ?: return false

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        return true
    }

    private fun normalizeAppName(value: String): String {
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

    fun scrollDown(): Boolean {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        return swipe(
            startX = width / 2,
            startY = (height * 0.75).toInt(),
            endX = width / 2,
            endY = (height * 0.25).toInt(),
            duration = 600
        )
    }

    fun scrollUp(): Boolean {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        return swipe(
            startX = width / 2,
            startY = (height * 0.25).toInt(),
            endX = width / 2,
            endY = (height * 0.75).toInt(),
            duration = 600
        )
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
}
