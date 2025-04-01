package com.example.fittr_app.media_pipe

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fittr_app.SharedViewModel
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.databinding.FragmentExerciseSuccessBinding
import com.example.fittr_app.types.Exercise
import kotlinx.coroutines.launch


class ExerciseSuccessFragment : Fragment() {

    private var _binding: FragmentExerciseSuccessBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: SharedViewModel
    private val apiClient: ApiClient by lazy { ApiClientProvider.apiClient }
    private var exerciseFeedback = "No feedback at the moment"

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
        val exerciseSummaryTextView = binding.exerciseSuccessSummary
        val progressBar = binding.exerciseSummaryLoading
        progressBar.visibility = View.VISIBLE
        exerciseSummaryTextView.visibility = View.GONE
        lifecycleScope.launch {
            getAISessionFeedback()
            val exerciseToDescription = mapOf(Exercise.RIGHT_BICEP_CURLS to "Right bicep curls",Exercise.SQUATS to "Squats",Exercise.LEFT_BICEP_CURLS to "Left bicep curls")
            exerciseSummaryTextView.text = "Completed ${sharedViewModel.repCount.value.toString()} reps of \n " +
                    "${exerciseToDescription.get(sharedViewModel.selectedExercise.value)} \n" +
                    "FITTR AI says: $exerciseFeedback"
            progressBar.visibility = View.GONE
            exerciseSummaryTextView.visibility = View.VISIBLE
        }

        binding.exerciseSuccessDoneButton.setOnClickListener {
            activity?.finish()
        }
    }

    private suspend fun getAISessionFeedback(){
        val sessionData = sharedViewModel.getExerciseSessionData()
        val result = apiClient.getUserExerciseSessionFeedback(sessionData)
        if(result.isSuccess){
            exerciseFeedback = result.getOrNull()?.feedback_message ?: exerciseFeedback
        }else{
            Log.e("ExerciseSuccessFragment", "Error getting AI session feedback ${result.getOrNull()}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}