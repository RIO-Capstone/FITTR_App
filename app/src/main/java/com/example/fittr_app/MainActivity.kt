package com.example.fittr_app

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.setupWithNavController
import com.example.fittr_app.databinding.ActivityMainBinding
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.fittr_app.types.Exercise


class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var navController:NavController

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val selectedExercise = intent.getSerializableExtra("selectedExercise") as? Exercise ?: Exercise.UNKNOWN
        val totalSessionReps = intent.getIntExtra("total_session_reps",0)
        val deviceServiceUUID = intent.getStringExtra("deviceServiceUUID")
        val leftDeviceResistanceUUID = intent.getStringExtra("leftResistanceUUID")
        val rightDeviceResistanceUUID = intent.getStringExtra("rightResistanceUUID")
        val exerciseInitializeUUID = intent.getStringExtra("exercise_initialize_uuid")
        val deviceStopUUID = intent.getStringExtra("deviceStopUUID")
        val userId = intent.getIntExtra("user_id",0)
        val productId = intent.getIntExtra("product_id",0)

        // initialise the shared view model which shares data between the different fragments in main activity
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        sharedViewModel.setSelectedExercise(selectedExercise)
        sharedViewModel.setDeviceServiceUUID(deviceServiceUUID!!)
        sharedViewModel.setDeviceStopUUID(deviceStopUUID!!)
        sharedViewModel.setDeviceLeftResistanceUUID(leftDeviceResistanceUUID!!)
        sharedViewModel.setDeviceRightResistanceUUID(rightDeviceResistanceUUID!!)
        sharedViewModel.setDeviceExerciseInitializeUUID(exerciseInitializeUUID!!)
        sharedViewModel.setUserId(userId)
        sharedViewModel.setProductId(productId)
        sharedViewModel.setTotalRepCount(totalSessionReps)
        sharedViewModel.updateDisplayText()

        val repCountTextView = findViewById<TextView>(R.id.rep_count)
        sharedViewModel.displayText.observe(this) { displayText ->
            repCountTextView.text = displayText
        }
        val timerTextView = findViewById<TextView>(R.id.timer_text)
        sharedViewModel.timerValue.observe(this) { timeInMillis ->
            val totalSeconds = timeInMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val milliseconds = (timeInMillis % 1000) / 100 // Get tenths of a second

            timerTextView.text = String.format("%02d:%02d.%d", minutes, seconds, milliseconds)
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        sharedViewModel.startTimer()
    }

    @Deprecated("onBackPressed deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish();
    }

    private fun navigateToCameraFragment() {
        navController.navigate(R.id.action_permissions_to_camera)
    }

}