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

    fun tap(x: Int, y: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
            .build()

        dispatchGesture(gesture, null, null)
    }

    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 500) {
        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(endX.toFloat(), endY.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        dispatchGesture(gesture, null, null)
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

        return focusedNode.performAction(AccessibilityNodeInfo.ACTION_IME_ENTER)
    }

    fun clickNodeByText(text: String): Boolean {
        val node = findNodeByText(text) ?: return false
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun clickNodeByViewId(viewId: String): Boolean {
        val node = findNodeByViewId(viewId) ?: return false
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
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

    fun openAppByName(appName: String): Boolean {
        val packageManager = packageManager
        val apps = packageManager.getInstalledApplications(0)

        val app = apps.firstOrNull {
            val label = packageManager.getApplicationLabel(it).toString()
            label.contains(appName, ignoreCase = true)
        } ?: return false

        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            ?: return false

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
        return true
    }

    fun scrollDown() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        swipe(
            startX = width / 2,
            startY = (height * 0.75).toInt(),
            endX = width / 2,
            endY = (height * 0.25).toInt(),
            duration = 600
        )
    }

    fun scrollUp() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        swipe(
            startX = width / 2,
            startY = (height * 0.25).toInt(),
            endX = width / 2,
            endY = (height * 0.75).toInt(),
            duration = 600
        )
    }

    fun pressBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun pressHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun openRecents() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
}
