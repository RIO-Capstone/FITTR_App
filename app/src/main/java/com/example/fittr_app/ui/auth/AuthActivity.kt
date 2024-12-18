package com.example.fittr_app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.databinding.ActivityAuthBinding
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.ui.registration.RegistrationActivity
import kotlinx.coroutines.launch

class AuthActivity:AppCompatActivity() {
    private lateinit var _binding: ActivityAuthBinding
    private lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        apiClient = ApiClient()

        val userNameField = _binding.authIdField
        val passwordField = _binding.authPasswordField
        val loginButton = _binding.authLoginButton
        val registrationButton = _binding.authRegistrationButton

        registrationButton.setOnClickListener {
            startRegistration()
        }
        loginButton.setOnClickListener {
            lifecycleScope.launch {
                val response = login(email = userNameField.text.toString(), password = passwordField.text.toString())
                if (response != null) {
                    Toast.makeText(this@AuthActivity, "Login Complete", Toast.LENGTH_LONG).show()
                    navigateToDashboard(response.user.user_id)
                } else {
                    Toast.makeText(this@AuthActivity, "Login Failed. Check your credentials.", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private suspend fun login(email: String, password: String): ApiClient.LoginUserBackendResponse? {
        val loginData = mapOf("email" to email, "password" to password)
        val result = apiClient.loginUser(ApiPaths.LoginUser, loginData)

        // Check if the result was successful and return it
        return if (result.isSuccess) {
            result.getOrNull()
        } else {
            // Show Toast when login is unsuccessful
            runOnUiThread {
                Toast.makeText(this@AuthActivity, "Login Unsuccessful. Try again later.", Toast.LENGTH_LONG).show()
            }
            null
        }
    }


    private fun startRegistration(){
        // Navigate to registration
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDashboard(user_id: Int){
        val intent = Intent(this, DashboardActivity::class.java).apply {
            putExtra("user_id", user_id)
        }
        startActivity(intent)
    }
}