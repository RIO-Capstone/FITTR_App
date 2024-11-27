package com.example.fittr_app

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Apply gradient background to the title
        val titleTextView = findViewById<TextView>(R.id.gradient_title)
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, // Direction of the gradient
            intArrayOf(0xFFE91E63.toInt(), 0xFFFFC107.toInt()) // Colors (Pink to Yellow)
        )
        gradientDrawable.cornerRadius = 16f // Optional: Rounded corners
        titleTextView.background = gradientDrawable
    }
}


