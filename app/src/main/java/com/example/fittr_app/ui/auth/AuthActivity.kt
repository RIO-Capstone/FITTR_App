package com.example.fittr_app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fittr_app.MainActivity
import com.example.fittr_app.databinding.ActivityAuthBinding
import com.example.fittr_app.ui.DashboardActivity
import com.example.fittr_app.ui.registration.RegistrationActivity

class AuthActivity:AppCompatActivity() {
    lateinit private var AuthBinding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(AuthBinding.root)
        val userNameField = AuthBinding.authIdField
        val passwordField = AuthBinding.authPasswordField
        val loginButton = AuthBinding.authLoginButton
        val registrationButton = AuthBinding.authRegistrationButton

        registrationButton.setOnClickListener {
            startRegistration()
        }
        loginButton.setOnClickListener(){
            if(login(userNameField.text.toString(),passwordField.text.toString())){
                navigateToDashboard()
            }
        }
    }

    private fun login(username: String,password: String):Boolean{
        return true
    }

    private fun startRegistration(){
        // Navigate to registration
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDashboard(){
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}