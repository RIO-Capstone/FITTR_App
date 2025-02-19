package com.example.fittr_app.media_pipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fittr_app.R
import com.example.fittr_app.SharedViewModel
import com.example.fittr_app.databinding.FragmentExerciseSuccessBinding

class ExerciseSuccessFragment : Fragment() {

    private var _binding: FragmentExerciseSuccessBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         val exerciseSummarTextView = binding.exerciseSuccessSummary
        // TODO: Use the exercise session data from the view model to create an AI response
        exerciseSummarTextView.text = "You just completed ${viewModel.repCount.value.toString()} reps of ${viewModel.selectedExercise.value.toString()}"

        binding.exerciseSuccessDoneButton.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}