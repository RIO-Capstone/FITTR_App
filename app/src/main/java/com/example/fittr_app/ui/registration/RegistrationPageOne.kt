package com.example.fittr_app.ui.registration

import RegistrationViewModel
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fittr_app.databinding.RegistrationPageOneBinding

class RegistrationPageOne : Fragment() {
    private lateinit var viewModel: RegistrationViewModel
    private var _binding: RegistrationPageOneBinding? = null
    private val binding get() = _binding!!  // Only access after binding is initialized

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = RegistrationPageOneBinding.inflate(inflater, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]

        // Bind the views directly using the binding object
        val firstNameField = binding.etFirstName
        val lastNameField = binding.etLastName
        val phoneNumberField = binding.etPhoneNumber
        val emailField = binding.etEmail
        val productIdField = binding.etProductId
        val registerButton = binding.nxtButtonPageOne
        val passwordField = binding.etPassword
        val tc_checkbox = binding.cbAcceptTerms

        registerButton.setOnClickListener {
            // Save data to ViewModel
            if(tc_checkbox.isChecked && isValidInput()){
                viewModel.setEmail(emailField.text.toString())
                viewModel.setFirstName(firstNameField.text.toString())
                viewModel.setLastName(lastNameField.text.toString())
                viewModel.setPhoneNumber(phoneNumberField.text.toString())
                viewModel.setProductId(productIdField.text.toString().toInt())
                viewModel.setPassword(passwordField.text.toString())
                Log.i("RegistrationPageOne", "Page one data saved to ViewModel: $viewModel")
                // Navigate to the next fragment
                (requireActivity() as RegistrationActivity)
                    .navigateToFragment(RegistrationPageTwo())
            }else{
                Toast.makeText(activity, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
            }

        }

        return binding.root
    }

    private fun isValidInput(): Boolean {
        val firstNameField = binding.etFirstName
        val lastNameField = binding.etLastName
        val phoneNumberField = binding.etPhoneNumber
        val emailField = binding.etEmail
        val productIdField = binding.etProductId
        val passwordField = binding.etPassword

        // Check if all fields are filled
        if (firstNameField.text.isNullOrEmpty()) {
            firstNameField.error = "First name is required"
            return false
        }

        if (lastNameField.text.isNullOrEmpty()) {
            lastNameField.error = "Last name is required"
            return false
        }

        if(passwordField.text.isNullOrEmpty()){
            passwordField.error = "Password is required"
            return false
        }

        if (phoneNumberField.text.isNullOrEmpty()) {
            phoneNumberField.error = "Phone number is required"
            return false
        }

        if (emailField.text.isNullOrEmpty()) {
            emailField.error = "Email is required"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailField.text).matches()) {
            emailField.error = "Please enter a valid email address"
            return false
        }

        if (productIdField.text.isNullOrEmpty()) {
            productIdField.error = "Product ID is required"
            return false
        }
        return true;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set the binding to null to avoid memory leaks
        _binding = null
    }
}
