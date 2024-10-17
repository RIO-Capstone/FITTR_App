package com.example.fittr_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fittr_app.databinding.ActivityHomeBinding
import com.example.fittr_app.ui.auth.AuthActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var homeActivityBinding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeActivityBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(homeActivityBinding.root)
        homeActivityBinding.homeNextButton.setOnClickListener {
            // navigate to login
            navigateToAuth()
        }

    }
    private fun navigateToAuth(){
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
    }

}