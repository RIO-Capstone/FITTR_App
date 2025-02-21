package com.example.fittr_app.ui.registration

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fittr_app.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {
    private lateinit var registrationBinding: ActivityRegistrationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registrationBinding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(registrationBinding.root)

        if (savedInstanceState == null) {
            // Start with the first fragment
            supportFragmentManager.beginTransaction()
                .replace(registrationBinding.registrationFragmentContainer.id, RegistrationPageOne())
                .commit()
        }
    }

    fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(registrationBinding.registrationFragmentContainer.id, fragment)
            .addToBackStack(null)
            .commit()
    }
}