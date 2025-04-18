package com.example.fittr_app.ui.registration

import RegistrationViewModel
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fittr_app.R
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.databinding.RegistrationPageTwoBinding
import com.example.fittr_app.DashboardActivity
import kotlinx.coroutines.launch
import java.util.Calendar


class RegistrationPageTwo : Fragment() {
    private lateinit var viewModel: RegistrationViewModel
    private var _binding: RegistrationPageTwoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegistrationPageTwoBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        // initialise api client
        // Access views using binding instead of findViewById
        val genderField = binding.spinnerGender
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderField.adapter = adapter
        }
        val dobField = binding.etDateOfBirth
        // set up the calendar interface for the dobField
        setupDatePicker(dobField)
        val weightField = binding.etWeight
        val heightField = binding.etHeight
        val nextButton = binding.nxtBtnPageTwo

        nextButton.setOnClickListener { v: View? ->
            // Save data to ViewModel
            if(isValidInput()){
                viewModel.setGender(genderField.selectedItem.toString())
                viewModel.setDateOfBirth(dobField.text.toString())
                viewModel.setWeight(weightField.text.toString().toInt())
                viewModel.setHeight(heightField.text.toString().toInt())
                Log.i("RegistrationPageTwo", "Page two data saved to ViewModel: $viewModel")
                // start a coroutine for the suspend function
                Log.i("RegistrationPageTwo","Current product id = ${viewModel.product_id.value}")
                viewLifecycleOwner.lifecycleScope.launch {
                    (requireActivity() as RegistrationActivity).navigateToFragment(RegistrationPageThree())
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Clean up binding to avoid memory leaks
    }

    private fun isValidInput(): Boolean {
        val dobField = binding.etDateOfBirth
        val weightField = binding.etWeight
        val heightField = binding.etHeight
        val genderField = binding.spinnerGender

        if(genderField.selectedItem == resources.getStringArray(R.array.gender_options).first()){
            Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
            return false
        }
        // Validate Date of Birth
        if (dobField.text.isNullOrEmpty()) {
            dobField.error = "Date of birth is required"
            Toast.makeText(requireContext(),"Date of birth is required",Toast.LENGTH_SHORT).show()
            return false
        } else if (!isValidDate(dobField.text.toString())) {
            dobField.error = "Please enter a valid date of birth (YYYY-MM-DD)"
            return false
        }

        // Validate Weight
        if (weightField.text.isEmpty()) {
            weightField.error = "Weight is required"
            return false
        }

        // Validate Height
        if (heightField.text.isEmpty()) {
            heightField.error = "Height is required"
            return false
        }

        return true
    }

    private fun isValidDate(date: String): Boolean {
        // Basic validation for date format (DD-MM-YYYY)
        val datePattern = "^\\d{2}-\\d{2}-\\d{4}\$"
        return date.matches(datePattern.toRegex())
    }

    private fun setupDatePicker(editText: TextView) {
        val calendar = Calendar.getInstance()

        // When EditText is clicked, show DatePickerDialog
        editText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                editText.context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Update EditText with selected date in DD-MM-YYYY format
                    val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                    editText.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePicker.show()
        }
    }

}
