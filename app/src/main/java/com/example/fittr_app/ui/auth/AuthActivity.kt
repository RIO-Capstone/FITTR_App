package com.example.fittr_app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.databinding.ActivityAuthBinding
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.types.LoginUserBackendResponse
import com.example.fittr_app.ui.profile.SwitchUserActivity
import com.example.fittr_app.ui.registration.RegistrationActivity
import kotlinx.coroutines.launch

class AuthActivity:AppCompatActivity() {
    private lateinit var _binding: ActivityAuthBinding
    private val apiClient: ApiClient by lazy { ApiClientProvider.apiClient }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(_binding.root)

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
                    navigateToUserProfileActivity(response.user.product_id)
                } else {
                    Toast.makeText(this@AuthActivity, "Login Failed. Check your credentials.", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private suspend fun login(email: String, password: String): LoginUserBackendResponse? {
        val loginData = mapOf("email" to email, "password" to password)
        val result = apiClient.loginUser(ApiPaths.LoginUser, loginData)

        // Check if the result was successful and return it
        return if (result.isSuccess) {
            result.getOrNull()
        } else {
            // Show Toast when login is unsuccessful
//            runOnUiThread {
//                Toast.makeText(this@AuthActivity, "Login Unsuccessful. Try again later.", Toast.LENGTH_LONG).show()
//            }
            null // return null
        }
    }


    private fun startRegistration(){
        // Navigate to registration
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToUserProfileActivity(productId:Int) {
        val intent = Intent(this, SwitchUserActivity::class.java)
        intent.putExtra("product_id",productId)
        startActivity(intent)
    }
}