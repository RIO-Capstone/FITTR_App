package com.example.fittr_app

import android.os.Build
import android.os.Bundle
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
    private val viewModel : MainViewModel by viewModels()
    private lateinit var navController:NavController

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val selectedExercise = intent.getSerializableExtra("selectedExercise") as? Exercise ?: Exercise.UNKNOWN
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

        val repCountTextView = findViewById<TextView>(R.id.rep_count)
        sharedViewModel.displayText.observe(this) { displayText ->
            repCountTextView.text = displayText
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        // VERY IMPORTANT!
        // entry fragment is the permissions fragment, which automatically navigates to the camera fragment
        // camera fragment is the one where HTTP and WebSocket connections are made
//        activityMainBinding.navigation.setupWithNavController(navController)
//        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
//            // ignore the reselection
//        }

    }

    @Deprecated("onBackPressed deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish();
    }

    private fun navigateToCameraFragment() {
        navController.navigate(R.id.action_permissions_to_camera)
    }

    private fun navigateToGalleryFragment() {
        //navController.navigate(R.id.) // Assuming you have an action defined for this
    }


}