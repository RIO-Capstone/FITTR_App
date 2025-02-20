package com.example.fittr_app.media_pipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fittr_app.SharedViewModel
import com.example.fittr_app.databinding.FragmentExerciseSuccessBinding
import com.example.fittr_app.types.Exercise

class ExerciseSuccessFragment : Fragment() {

    private var _binding: FragmentExerciseSuccessBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseSuccessBinding.inflate(inflater, container, false)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         val exerciseSummarTextView = binding.exerciseSuccessSummary
        // TODO: Use the exercise session data from the view model to create an AI response
        val exerciseToDescription = mapOf(Exercise.RIGHT_BICEP_CURLS to "Right bicep curls",Exercise.SQUATS to "Squats",Exercise.LEFT_BICEP_CURLS to "Left bicep curls")
        exerciseSummarTextView.text = "Completed ${sharedViewModel.repCount.value.toString()} reps of \n ${exerciseToDescription.get(sharedViewModel.selectedExercise.value)}"

        binding.exerciseSuccessDoneButton.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}