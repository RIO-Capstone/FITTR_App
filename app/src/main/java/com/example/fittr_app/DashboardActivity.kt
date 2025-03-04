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
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.connections.BluetoothHelper
import com.example.fittr_app.databinding.ActivityDashboardBinding
import com.example.fittr_app.ui.profile.SwitchUserActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import okhttp3.internal.concurrent.Task

interface BluetoothReadCallback {
    fun onValueRead(value: String)
    fun onError(message: String)
}

class DashboardActivity : AppCompatActivity() {
    private lateinit var DashboardBinding : ActivityDashboardBinding
    private lateinit var api_client : ApiClient
    private lateinit var user: ApiClient.User
    private lateinit var productData: ApiClient.ProductData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DashboardBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(DashboardBinding.root)
        api_client = ApiClient()


        val intent = intent
        if(intent.hasExtra("user_id")){ // getting the user_id from the login session
            val user_id = intent.getIntExtra("user_id",0)
            lifecycleScope.launch {
                getUserInformation(user_id)
                getUserHistory()
                getProductData(user.product_id)
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
        val squatStartButton = findViewById<View>(R.id.dashboard_exercise_squats)
        squatStartButton.setOnClickListener{
            navigateToMain("SQUATS")
        }
        val bicepCurlStartButton = findViewById<View>(R.id.dashboard_exercises_bicep_curl_right)
        bicepCurlStartButton.setOnClickListener {
            navigateToMain("RIGHT_BICEP_CURLS")
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

        val aiButton = findViewById<ImageButton>(R.id.dashboard_ai_button)
        aiButton.setOnClickListener{
            lifecycleScope.launch {
                getAIReply()
            }
        }

        val aiLayout = findViewById<View>(R.id.ai_layout)
        aiLayout.setOnClickListener {
            // Navigate to AIDashboardActivity
            val intent = Intent(this, AIDashboardActivity::class.java)
            intent.putExtra("user_id", user.user_id) // Pass user_id or other data as needed
            startActivity(intent)
        }

        val switchUserButton = findViewById<Button>(R.id.switch_user_button)
        switchUserButton.setOnClickListener{
            navigateToUserProfileActivity()
        }


    }

    private fun navigateToUserProfileActivity() {
        val intent = Intent(this, SwitchUserActivity::class.java)
        startActivity(intent)
    }


    // Function responsible for starting the Exercise Session from the dashboard
    private fun navigateToMain(selectedExercise:String){
        // Disabled bluetooth check for testing purposes
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("selectedExercise", selectedExercise)
        intent.putExtra("deviceServiceUUID",productData.service_uuid)
        intent.putExtra("deviceStopUUID",productData.stop_uuid)
        intent.putExtra("deviceResistanceUUID",productData.resistance_uuid)
        intent.putExtra("user_id",user.user_id)
        intent.putExtra("product_id",user.product_id)
        startActivity(intent)
//        if(checkBluetoothConnection()){
//
//        } else {
//            Log.e("DashboardActivity", "Bluetooth connection not established")
//        }
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
                Log.e("DashboardActivity", "User not initialised when calling get history")
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

            }
        }catch (e:Exception){
            Log.e("DashboardActivity","Error getting user history: ${e}")
            e.printStackTrace()
        }
    }

    private suspend fun getProductData(product_id:Int){
        if (!::user.isInitialized) {
            Log.e("DashboardActivity", "User not initialised when calling get product")
            return
        }
        try{
            val response = api_client.getProductData(ApiPaths.GetProduct(product_id),null)
            if(response.isSuccess){
                productData = response.getOrNull()!!
            }else{
                Log.e("DashboardActivity","Error getting product data: ${response.getOrNull()?.message}")
            }
        }catch (e:Exception) {
            Log.e("DashboardActivity", "Error getting product data: $e")
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
    private fun checkBluetoothConnection(): Boolean {
        // Check if the app has the BLUETOOTH_CONNECT permission (required for Android 12+)
        Log.d("DashboardActivity", "Checking Bluetooth connection")
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
            Toast.makeText(this, "Bluetooth is not enabled!", Toast.LENGTH_SHORT).show()
            return false // Bluetooth is either not supported or not enabled
        }

        // List of paired devices
        val connectedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        if(connectedDevices.isNullOrEmpty()){
            Toast.makeText(this, "No connected devices found", Toast.LENGTH_SHORT).show()
            return false
        }
        // Iterate through paired device
        // TODO: Logic needs refining as we only consider a connection to be valid if its with our FITTR device
        for (device in connectedDevices) {
            val deviceName = device.name ?: "Unknown Device"
            val deviceAddress = device.address

            // Log information about the device
            Log.d("DashboardActivity", "Device Name: $deviceName, Address: $deviceAddress")

            // Check if the device is connected (bonded)
            val state_check = device.bondState == BluetoothDevice.BOND_BONDED
            val id_check = device.uuids[0].toString() // TODO: Identify that the device is a FITTR device using serviceUUID
            Log.d("DashboardActivity", "Device State: $state_check, UUID: $id_check")
            if (state_check) {
                BluetoothHelper.initialize(device)
                return true
            }
        }
        Toast.makeText(this,"None of the connected devices passed the state check",Toast.LENGTH_LONG).show()
        return false
    }
    private suspend fun getAIReply(){
        if(!::user.isInitialized){
            Log.e("DashboardActivity","User not initialised when calling get AI reply")
            return
        }
        val response = api_client.getAIReply(ApiPaths.GetAIReply(user.user_id),data = null)
        if(response.isSuccess){
            Toast.makeText(this,response.getOrNull()?.message,Toast.LENGTH_LONG).show()
            Log.i("DashboardActivity","AI reply retrieved successfully")
        }else{
            Log.e("DashboardActivity","Error getting AI reply: ${response.getOrNull()?.message}")
        }
    }


}
