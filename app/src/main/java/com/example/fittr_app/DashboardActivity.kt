package com.example.fittr_app

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.databinding.ActivityDashboardBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch


class DashboardActivity : AppCompatActivity() {
    private lateinit var DashboardBinding : ActivityDashboardBinding
    private lateinit var api_client : ApiClient
    private lateinit var user: ApiClient.User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DashboardBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(DashboardBinding.root)
        api_client = ApiClient()
        val intent = intent
        if(intent.hasExtra("user_id")){
            val user_id = intent.getIntExtra("user_id",0)
            lifecycleScope.launch {
                getUserInformation(user_id)
            }
        }
        /**
         * Exercise Session logic
         * Select exercise
         * Start the camera fragment from the main activity
         * Backend model should know which exercise was selected
         * Send the landmark data to the exercise specific model
         * Get the results from it and display the results on the screen
         * **/
        val exerciseStartButton = findViewById<ImageButton>(R.id.exercise_squat)
        styliseButton(exerciseStartButton)

        val selectedExercise = "SQUAT";
        exerciseStartButton.setOnClickListener {
            // navigate to main activity that handles the core media pipe logic
            navigateToMain(selectedExercise)
        }

        val barChart = DashboardBinding.dashboardBarChart
        val barEntries = ArrayList<BarEntry>()
        // API to get data on the weekly exercises being done
        barEntries.add(BarEntry(0f, 5f)) // Monday - 5
        barEntries.add(BarEntry(1f, 8f)) // Tuesday - 8
        barEntries.add(BarEntry(2f, 6f)) // Wednesday - 6
        barEntries.add(BarEntry(3f, 7f)) // Thursday - 7
        barEntries.add(BarEntry(4f, 4f)) // Friday - 4
        val barDataSet = BarDataSet(barEntries, "Activity Progress")
        barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS) // Select color templates
        barDataSet.valueTextSize = 14f
        barDataSet.setDrawValues(true) // Show values on top of bars


        // Set data to the BarChart
        val barData = BarData(barDataSet)
        val progressLayout = findViewById<FrameLayout>(R.id.dashboard_progress_layout)
        barChart.setData(barData)

        // Customize BarChart appearance
        barChart.getDescription().setEnabled(false) // Remove description label
        barChart.setDrawGridBackground(false)
        barChart.getAxisRight().setEnabled(false) // Hide right Y-axis


        // Customize X-Axis
        val xAxis: XAxis = barChart.getXAxis()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 5
        xAxis.textSize = 12f
        xAxis.textColor = resources.getColor(android.R.color.black)

        val maxYValue = barEntries.maxOf { it.y }
        val minHeightInDp = 155 // Minimum height for FrameLayout
        val dynamicHeightInDp = minHeightInDp + (maxYValue * 10).toInt() // Adjust height based on Y-value

        // Convert dp to pixels for consistent size across devices
        val displayMetrics = resources.displayMetrics
        val dynamicHeightInPx = (dynamicHeightInDp * displayMetrics.density).toInt()

        // Animate the height change of FrameLayout
        val currentLayoutParams = progressLayout.layoutParams
        val startHeight = currentLayoutParams.height
        val endHeight = dynamicHeightInPx

        val valueAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        valueAnimator.duration = 1000 // Match BarChart animation duration
        valueAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            currentLayoutParams.height = animatedValue
            progressLayout.layoutParams = currentLayoutParams
        }
        valueAnimator.start()

        // Refresh BarChart with animation
        barChart.animateY(1000) // Smooth animation for BarChart
        barChart.invalidate()

        // Refresh BarChart
        barChart.animateY(1000) // Add animation
        barChart.invalidate()

    }
    private fun navigateToMain(selectedExercise:String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("selectedExercise", selectedExercise)
        startActivity(intent)
    }
    private suspend fun getUserInformation(user_id:Int){
        try {
            val response = api_client.getUser(ApiPaths.GetUser(user_id),null)
            if(response.isSuccess){
                Log.i("DashboardActivity","User information retrieved successfully")
                user = response.getOrNull()?.user!!
                runOnUiThread {
                    DashboardBinding.dashboardUserNameText.text =
                        "${user.first_name.replaceFirstChar { it.uppercase() }} " +
                                "${user.last_name.replaceFirstChar { it.uppercase() }}"
                }
            }
        }catch (e:Exception){
            Log.e("DashboardActivity","Error getting user information: $e")
        }
    }
    private fun styliseButton(btn:ImageButton){
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, // Direction of the gradient
            intArrayOf(0xFFE91E63.toInt(), 0xFFFFC107.toInt()) // Colors (Pink to Yellow)
        )
        gradientDrawable.cornerRadius = 1000f // Optional: Rounded corners
        btn.background = gradientDrawable
        val size = resources.getDimensionPixelSize(R.dimen.round_button_medium) // e.g., 48dp or any value you prefer
        val layoutParams = btn.layoutParams
        layoutParams.width = size
        layoutParams.height = size
        btn.layoutParams = layoutParams
    }
}