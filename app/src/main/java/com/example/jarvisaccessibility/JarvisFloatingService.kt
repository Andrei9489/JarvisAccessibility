package com.example.jarvisaccessibility

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import kotlin.math.abs

class JarvisFloatingService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingButton: Button? = null
    private var buttonParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var moved = false

    override fun onCreate() {
        super.onCreate()
        showFloatingButton()
    }

    private fun showFloatingButton() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        buttonParams = WindowManager.LayoutParams(
            150,
            150,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 250
        }

        floatingButton = Button(this).apply {
            text = "J"
            textSize = 18f

            setOnTouchListener { _, event ->
                val params = buttonParams
                val button = floatingButton

                if (params == null || button == null) {
                    return@setOnTouchListener false
                }

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        moved = false
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()

                        if (abs(deltaX) > 8 || abs(deltaY) > 8) {
                            moved = true
                        }

                        params.x = initialX + deltaX
                        params.y = initialY + deltaY

                        windowManager?.updateViewLayout(button, params)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!moved) {
                            openJarvis()
                        }
                        true
                    }

                    else -> false
                }
            }
        }

        windowManager?.addView(floatingButton, buttonParams)
    }

    private fun openJarvis() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        val button = floatingButton
        if (button != null) {
            windowManager?.removeView(button)
            floatingButton = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
