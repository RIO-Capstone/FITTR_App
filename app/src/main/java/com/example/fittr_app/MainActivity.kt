package com.example.fittr_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.setupWithNavController
import com.example.fittr_app.databinding.ActivityMainBinding
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.fittr_app.data_classes.ExerciseType


class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var sharedViewModel: SharedViewModel
    private val viewModel : MainViewModel by viewModels()
    private lateinit var navController:NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val selectedExercise = intent.getStringExtra("selectedExercise")
        // initialise the shared view model which shares data between the different fragments in main activity
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        if (selectedExercise != null) {
            sharedViewModel.setSelectedExercise(selectedExercise)
        };

        val repCountTextView = findViewById<TextView>(R.id.rep_count)
        sharedViewModel.repCount.observe(this) { repCount ->
            if(sharedViewModel.isCalibrating.value == true){
                repCountTextView.text = "Calibrating..."
            }else{
                repCountTextView.text = "Rep Count: $repCount"
            }
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        // VERY IMPORTANT!
        // entry fragment is the permissions fragment, which automatically navigates to the camera fragment
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }

        // Get the fragment to load from the intent extras
//        val fragmentToLoad:String = intent.getStringExtra("FRAGMENT_TO_LOAD").toString()
//
//        // Load the appropriate fragment
//        when (fragmentToLoad) {
//            "CameraFragment" -> navigateToCameraFragment()
//            "GalleryFragment" -> navigateToGalleryFragment()
//        }

    }

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