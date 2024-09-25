package com.example.fittr_app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fittr_app.MainActivity
import com.example.fittr_app.databinding.ActivityAuthBinding
import com.example.fittr_app.ui.DashboardActivity

class AuthActivity:AppCompatActivity() {
    lateinit private var AuthBinding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(AuthBinding.root)
        AuthBinding.authNextButton.setOnClickListener {
            navigateToDashboard()
        }
    }
    private fun navigateToDashboard(){
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}