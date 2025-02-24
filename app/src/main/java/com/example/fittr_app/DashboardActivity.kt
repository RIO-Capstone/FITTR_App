package com.example.fittr_app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.connections.BluetoothHelper
import com.example.fittr_app.databinding.ActivityDashboardBinding
import com.example.fittr_app.types.Exercise
import com.example.fittr_app.types.ProductData
import com.example.fittr_app.types.User
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import okhttp3.internal.concurrent.Task
import android.os.Handler

interface BluetoothReadCallback {
    fun onValueRead(value: String){}
    fun onError(message: String)
    fun onBluetoothConnectionChange(isConnected: Boolean){} // default implementation to do nothing
}

class DashboardActivity : AppCompatActivity(), BluetoothReadCallback {
    private lateinit var DashboardBinding : ActivityDashboardBinding
    private lateinit var api_client : ApiClient
    private lateinit var user: User
    private lateinit var productData: ProductData
    private var isBluetoothConnected = false

    override fun onError(message: String) {
        Log.e("DashboardActivity","Bluetooth error : $message")
        Toast.makeText(this,"Unstable bluetooth connection",Toast.LENGTH_LONG).show()
        this.onResume()
    }

    override fun onBluetoothConnectionChange(isConnected: Boolean) {
        val bluetoothButton = findViewById<ImageButton>(R.id.dashboard_bluetooth_status_button)
        if (isConnected) {
            bluetoothButton.setImageResource(R.drawable.bluetooth_green)
        } else {
            bluetoothButton.setImageResource(R.drawable.bluetooth_red)
        }
        isBluetoothConnected = isConnected
    }

    private var form_score: Int = 0
    private var stability_score: Int = 0
    private var range_of_motion_score: Int = 0
    private var summary_analysis: String = ""
    private var future_advice: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DashboardBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(DashboardBinding.root)
        api_client = ApiClient()

        if(intent.hasExtra("user_id")){ // getting the user_id from the login session
            val user_id = intent.getIntExtra("user_id",0)
            lifecycleScope.launch {
                getUserInformation(user_id)
                getFITTRAIinformation(user_id)
                getUserHistory()
                getProductData(user.product_id)
            }
        }
        val backButton = findViewById<View>(R.id.dashboard_back_btn)
        backButton.setOnClickListener{
            finish()
        }
        val squatStartButton = findViewById<View>(R.id.dashboard_exercise_squats)
        squatStartButton.setOnClickListener{
            navigateToMain(Exercise.SQUATS)
        }
        val rightBicepCurlStartButton = findViewById<View>(R.id.dashboard_exercises_bicep_curl_right)
        rightBicepCurlStartButton.setOnClickListener {
            navigateToMain(Exercise.RIGHT_BICEP_CURLS)
        }


        val bluetoothButton = findViewById<ImageButton>(R.id.dashboard_bluetooth_status_button)
        bluetoothButton.setOnClickListener{
            checkBluetoothConnection()
        }



        val aiLayout = findViewById<View>(R.id.ai_layout)
        aiLayout.setOnClickListener {
            val intent = Intent(this, AIDashboardActivity::class.java)
            intent.putExtra("user_id", user.user_id)
            intent.putExtra("summary_analysis", summary_analysis)
            intent.putExtra("future_advice", future_advice)
            intent.putExtra("form_score", form_score)
            intent.putExtra("stability_score", stability_score)
            intent.putExtra("range_of_motion_score", range_of_motion_score)
            startActivity(intent)
        }
    }

    // Function responsible for starting the Exercise Session from the dashboard
    private fun navigateToMain(selectedExercise:Exercise){
        // Disabled bluetooth check for testing purposes
        if(isBluetoothConnected){
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("selectedExercise", selectedExercise)
            intent.putExtra("deviceServiceUUID",productData.service_uuid)
            intent.putExtra("deviceStopUUID",productData.stop_uuid)
            intent.putExtra("leftResistanceUUID",productData.left_resistance_uuid)
            intent.putExtra("rightResistanceUUID",productData.right_resistance_uuid)
            intent.putExtra("exercise_initialize_uuid",productData.exercise_initialize_uuid)
            intent.putExtra("user_id",user.user_id)
            intent.putExtra("product_id",user.product_id)
            BluetoothHelper.queueWriteOperation(
                message = "true",
                characteristicUUID = productData.exercise_initialize_uuid,
                callback = this)
            startActivity(intent)
        } else {
            Toast.makeText(this,"Establish Bluetooth connection first",Toast.LENGTH_LONG).show()
            Log.e("DashboardActivity", "Bluetooth connection not established")
        }
    }

    data class Feedback(
        val summary_advice: String,
        val summary_analysis: String,
        val future_advice: String,
        val form_score: Int,
        val stability_score: Int,
        val range_of_motion_score: Int
    )

    data class ApiResponse(
        val user_id: Int,
        val feedback: Feedback
    )

    private suspend fun getFITTRAIinformation(user_id: Int) {
        val aiMessageTextView: TextView = findViewById(R.id.ai_message)
        Log.i("DashboardActivity", "Getting FITTR AI information")

        // Start the strobing effect on the TextView
        startEllipsisAnimation(aiMessageTextView)

        // Call the API and get the result
        val result = api_client.getUserAIReply(user_id)

        // Check if the result is successful
        result.onSuccess { aiReply ->
            // Stop the strobing animation and set the solid text

            stopEllipsisAnimation(aiMessageTextView, aiReply.message?.summary_analysis!!)

            aiMessageTextView.alpha = 1f // Ensure it's fully visible before setting text
            aiMessageTextView.setTextColor(android.graphics.Color.parseColor("#8C52FD")) // Set text color to purple
            aiMessageTextView.text = aiReply.message.summary_analysis!!.substring(1)

            form_score = aiReply.message.form_score.toInt()
            stability_score = aiReply.message.stability_score.toInt()
            range_of_motion_score = aiReply.message.range_of_motion_score.toInt()

            summary_analysis = aiReply.message.summary_analysis
            future_advice = aiReply.message.future_advice

            Log.i("DashboardActivity", "Form Score: $form_score")
            Log.i("DashboardActivity", "Stability Score: $stability_score")
            Log.i("DashboardActivity", "Range of Motion Score: $range_of_motion_score")


            // Make sure the TextView is fully visible after animation ends
            aiMessageTextView.visibility = View.VISIBLE

        }.onFailure { exception ->

            aiMessageTextView.alpha = 1f // Ensure it's fully visible
            aiMessageTextView.setTextColor(android.graphics.Color.RED) // Set color to red in case of failure
            aiMessageTextView.text = "Failed to load AI information: ${exception.localizedMessage}"

            // Make sure the TextView is fully visible after animation ends
            aiMessageTextView.visibility = View.VISIBLE
        }
    }



    private var aiAnimationHandler: Handler? = null
    private var aiAnimationRunnable: Runnable? = null


    private fun startEllipsisAnimation(aiMessageTextView: TextView) {
        val loadingTexts = arrayOf("",".", "..", "...")
        var index = 0

        aiAnimationHandler = Handler(Looper.getMainLooper())
        aiAnimationRunnable = object : Runnable {
            override fun run() {
                aiMessageTextView.text = "Loading feedback from FITTR AI${loadingTexts[index]}"
                index = (index + 1) % loadingTexts.size
                aiAnimationHandler?.postDelayed(this, 100) // Update every 500ms
            }
        }
        aiAnimationHandler?.post(aiAnimationRunnable!!)
    }

    private fun stopEllipsisAnimation(aiMessageTextView: TextView, finalText: String) {
        aiAnimationHandler?.removeCallbacks(aiAnimationRunnable!!)
        aiMessageTextView.text = finalText // Set final AI response text
    }


    private suspend fun getUserInformation(userId:Int){
        try {
            val response = api_client.getUser(ApiPaths.GetUser(userId),null)
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
                if(history.isNullOrEmpty()){
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
            Log.e("DashboardActivity","Error getting user history: $e")
            e.printStackTrace()
        }
    }

    private suspend fun getProductData(productId:Int){
        if (!::user.isInitialized) {
            Log.e("DashboardActivity", "User not initialised when calling get product")
            return
        }
        try{
            val response = api_client.getProductData(ApiPaths.GetProduct(productId),null)
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
                Toast.makeText(this,"Bluetooth permission not granted",Toast.LENGTH_SHORT).show()
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

            // Check if the device is connected (bonded)
            val state_check = device.bondState == BluetoothDevice.BOND_BONDED
//            val id_check = device.uuids[0].toString() // TODO: Identify that the device is a FITTR device using serviceUUID
            if (state_check) {
                BluetoothHelper.initialize(this,device,this)
                return true
            }
        }
        Toast.makeText(this,"None of the connected devices passed the state check",Toast.LENGTH_LONG).show()
        return false
    }




}
