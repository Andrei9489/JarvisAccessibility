package com.example.jarvisaccessibility

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button

class JarvisFloatingService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingButton: Button? = null

    override fun onCreate() {
        super.onCreate()
        showFloatingButton()
    }

    private fun showFloatingButton() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        floatingButton = Button(this).apply {
            text = "J"
            textSize = 18f
            setOnClickListener {
                val intent = Intent(this@JarvisFloatingService, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            150,
            150,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 20
        params.y = 250

        windowManager?.addView(floatingButton, params)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (floatingButton != null) {
            windowManager?.removeView(floatingButton)
            floatingButton = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
