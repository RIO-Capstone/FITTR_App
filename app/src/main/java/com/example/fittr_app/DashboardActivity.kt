package com.example.fittr_app

import android.animation.ValueAnimator
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
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
                getUserHistory()
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

        val selectedExercise = "LEFT_BICEP_CURLS";
        exerciseStartButton.setOnClickListener {
            // navigate to main activity that handles the core media pipe logic (if connected to device)
//            if(checkBluetoothConnection()) {
//                navigateToMain(selectedExercise)
//            }
            navigateToMain(selectedExercise)
        }
        val bluetoothButton = findViewById<ImageButton>(R.id.dashboard_bluetooth_status_button)
        bluetoothButton.setOnClickListener{
            val isConnected = checkBluetoothConnection()
            if(isConnected){
                bluetoothButton.setImageResource(R.drawable.bluetooth_green)
            }else {
                bluetoothButton.setImageResource(R.drawable.bluetooth_red)
            }
        }

    }

    // Function responsible for starting the Exercise Session from the dashboard
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
    private suspend fun getUserHistory(){
        try {
            if (!::user.isInitialized) {
                Log.w("DashboardActivity", "User not found when calling get history")
                return
            }
            val response = api_client.getUserHistory(ApiPaths.GetUserHistory(user.user_id),null)
            if(response.isSuccess){
                Log.i("DashboardActivity","User history retrieved successfully")
                val history = response.getOrNull()?.session_data // history will have max length of 5 from backend
                val streak = response.getOrNull()?.streak
                val barEntries = ArrayList<BarEntry>()
                if(history == null || history.isEmpty()){
                    return
                }
                history.forEachIndexed{index,session->
                    val duration = session.duration.toFloat() // y-axis value in minutes
                    val date = session.date // x-axis value (e.g., "4th July")

                    // Map the date to the x-axis (index-based for simplicity)
                    barEntries.add(BarEntry(index.toFloat(), duration))
                }
                runOnUiThread {
                    DashboardBinding.streakNumber.text = streak.toString()
                    updateBarChart(barEntries)
                }
            }
        }catch (e:Exception){
            Log.e("DashboardActivity","Error getting user history: ${e}")
            e.printStackTrace()
        }
    }
    private fun updateBarChart(barEntries: ArrayList<BarEntry>) {
        // Create a new BarDataSet with the updated data
        val barDataSet = BarDataSet(barEntries, "Activity Progress")
        barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS) // Use color templates
        barDataSet.valueTextSize = 14f
        barDataSet.setDrawValues(true) // Show values on bars

        // Create new BarData and set it to the BarChart
        val barData = BarData(barDataSet)
        val barChart = DashboardBinding.dashboardBarChart
        val progressLayout = findViewById<FrameLayout>(R.id.dashboard_progress_layout)
        barChart.data = barData

        // Customize BarChart appearance (if needed)
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.axisRight.isEnabled = false

        // Refresh X-Axis labels if needed
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = barEntries.size
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

        // Animate and refresh the chart
        barChart.animateY(1000)
        barChart.invalidate()
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
    private fun checkBluetoothConnection(): Boolean {
        // Check if the app has the BLUETOOTH_CONNECT permission (required for Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if it hasn't been granted
                requestPermissions(
                    arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                    1
                )
                return false // Cannot proceed without permission
            }
        }

        // Get the BluetoothManager and BluetoothAdapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Check if Bluetooth is enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return false // Bluetooth is either not supported or not enabled
        }

        // Check connected devices
        val connectedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

        // Iterate through paired device
        // TODO: Logic needs refining as we only consider a connection to be valid
        // if its with our FITTR device
        for (device in connectedDevices) {
            val deviceName = device.name ?: "Unknown Device"
            val deviceAddress = device.address

            // Log information about the device
            Log.d("BluetoothConnection", "Device Name: $deviceName, Address: $deviceAddress")

            // Check if the device is connected (bonded)
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                Log.d("BluetoothConnection", "Connected to $deviceName")
                return true
            }
        }

        // No connected devices were found
        Toast.makeText(this, "No connected devices found", Toast.LENGTH_SHORT).show()
        return false
    }


}
