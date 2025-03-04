package com.example.fittr_app

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fittr_app.databinding.ActivityHomeBinding
import com.example.fittr_app.ui.auth.AuthActivity
import com.example.fittr_app.ui.profile.SwitchUserActivity


class HomeActivity : AppCompatActivity() {
    private lateinit var homeActivityBinding: ActivityHomeBinding

    // Inflating the activity_home xml file

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeActivityBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(homeActivityBinding.root)

        // Apply gradient to the TextView
        applyGradientToTextView()

        homeActivityBinding.homeNextButton.setOnClickListener {
            // Navigate to login
            navigateToUserProfileActivity()
        }
    }

    private fun applyGradientToTextView() {
        val textView = homeActivityBinding.textView // Replace with your TextView's ID

        val text = textView.text.toString()
        val paint = textView.paint
        val width = paint.measureText(text)

        val shader = LinearGradient(
            0f, 0f, width, textView.textSize,
            intArrayOf(
                Color.parseColor("#CC8FED"), // Start color
                Color.parseColor("#6B50F6")  // End color
            ),
            null,
            Shader.TileMode.CLAMP
        )
        textView.paint.shader = shader
    }
/*

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java) // Navigate to AuthActivity
        startActivity(intent)

        //Navigates to AuthActivity
    }
*/

    private fun navigateToUserProfileActivity() {
        val intent = Intent(this, SwitchUserActivity::class.java)
        startActivity(intent)
    }

}
