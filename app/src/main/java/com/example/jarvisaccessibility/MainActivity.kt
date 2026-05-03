package com.example.jarvisaccessibility

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = android.view.Gravity.CENTER
        layout.setPadding(40, 40, 40, 40)

        val button = Button(this)
        button.text = "Start Jarvis"

        button.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        layout.addView(button)
        setContentView(layout)
    }
}
