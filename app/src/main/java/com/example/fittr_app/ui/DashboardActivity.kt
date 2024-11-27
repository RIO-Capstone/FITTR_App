package com.example.fittr_app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.fittr_app.MainActivity
import com.example.fittr_app.data_classes.ExerciseType
import com.example.fittr_app.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var DashboardBinding : ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DashboardBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(DashboardBinding.root)
        /**
         * Select exercise
         * Start the camera fragment from the main activity
         * Backend model should know which exercise was selected
         * Send the landmark data to the exercise specific model
         * Get the results from it and display the results on the screen
         * **/
        val selectedExercise = "SQUAT";
        val exerciseBarButton : ImageButton = DashboardBinding.exerciseSquats
        exerciseBarButton.setOnClickListener {
            // navigate to main activity that handles the core media pipe logic
            navigateToMain(selectedExercise)
        }

    }
    private fun navigateToMain(selectedExercise:String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("selectedExercise", selectedExercise)
        startActivity(intent)

    }
}