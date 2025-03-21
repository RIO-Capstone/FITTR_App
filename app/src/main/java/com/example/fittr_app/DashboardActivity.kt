package com.example.fittr_app

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
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
import kotlinx.coroutines.launch
import android.os.Handler
import android.text.InputFilter
import android.text.TextWatcher
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.ui.auth.AuthActivity
import com.example.fittr_app.ui.profile.SwitchUserActivity
import com.example.fittr_app.utils.TextToSpeechHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext

interface BluetoothReadCallback {
    fun onValueRead(value: String){}
    fun onError(message: String)
    fun onBluetoothConnectionChange(isConnected: Boolean){} // default implementation to do nothing
}

class DashboardActivity : AppCompatActivity(), BluetoothReadCallback {

    companion object {
       private const val TAG = "DashboardActivity"
    }

    private lateinit var DashboardBinding : ActivityDashboardBinding
    private val apiClient: ApiClient by lazy { ApiClientProvider.apiClient }
    lateinit var user: User // public user object for testing purposes
    private lateinit var productData: ProductData
    private var isBluetoothConnected = false
    private val exerciseReps: MutableMap<Exercise, Int> = mutableMapOf()
    private lateinit var textToSpeech:TextToSpeechHelper

    override fun onError(message: String) {
        Log.e(TAG,"Bluetooth error : $message")
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
        textToSpeech = TextToSpeechHelper.initialize(this) // Singleton object initialization
        if(intent.hasExtra("user_id")){ // getting the user_id from the login session
            val user_id = intent.getIntExtra("user_id",0)
            lifecycleScope.launch {
                getUserInformation(user_id)
                getFITTRAIinformation(user_id)
                getProductData(user.product_id)
            }
        }
        val backButton = findViewById<View>(R.id.dashboard_back_btn)
        backButton.setOnClickListener{
            // navigate back to the Login page
            val authIntent = Intent(this,AuthActivity::class.java)
            startActivity(authIntent)
        }
        val squatStartButton = findViewById<View>(R.id.dashboard_exercise_squats)
        val squatExerciseRep: EditText = findViewById(R.id.squat_exercise_rep)
        squatStartButton.setOnClickListener{
            navigateToMain(Exercise.SQUATS)
        }
        applyIntegerFilterAndTypeMapping(squatExerciseRep,Exercise.SQUATS)

        val rightBicepCurlStartButton = findViewById<View>(R.id.dashboard_exercises_bicep_curl_right)
        val rightBicepCurlExerciseRep: EditText = findViewById(R.id.right_bicep_curl_exercise_rep)
        rightBicepCurlStartButton.setOnClickListener {
            navigateToMain(Exercise.RIGHT_BICEP_CURLS)
        }
        applyIntegerFilterAndTypeMapping(rightBicepCurlExerciseRep,Exercise.RIGHT_BICEP_CURLS)

        val leftBicepCurlStartButton = findViewById<View>(R.id.dashboard_exercises_bicep_curl_left)
        val leftBicepCurlExerciseRep: EditText = findViewById(R.id.left_bicep_curl_exercise_rep)
        leftBicepCurlStartButton.setOnClickListener{
            navigateToMain(Exercise.LEFT_BICEP_CURLS)
        }
        applyIntegerFilterAndTypeMapping(leftBicepCurlExerciseRep,Exercise.LEFT_BICEP_CURLS)

        val bluetoothButton = findViewById<ImageButton>(R.id.dashboard_bluetooth_status_button)
        bluetoothButton.setOnClickListener{
            checkBluetoothConnection()
        }
        val aiExercisePlanButton = findViewById<View>(R.id.dashboard_ai_button)
        val loadingComponent = findViewById<View>(R.id.dashboard_loading_progress)
        aiExercisePlanButton.setOnClickListener{
            aiExercisePlanButton.visibility = View.GONE
            loadingComponent.visibility = View.VISIBLE
            lifecycleScope.launch {
                getAIExercisePlan()
                loadingComponent.visibility = View.GONE
                aiExercisePlanButton.visibility = View.VISIBLE
            }.invokeOnCompletion { updateUIWithExerciseReps() }
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
    private fun navigateToMain(selectedExercise:Exercise){
        if(::textToSpeech.isInitialized){
            textToSpeech.speak("Start")
        }
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
            intent.putExtra("total_session_reps", exerciseReps[selectedExercise])
            BluetoothHelper.queueWriteOperation(
                message = "true",
                characteristicUUID = productData.exercise_initialize_uuid,
                callback = this)
            startActivity(intent)
        } else {
            Toast.makeText(this,"Establish Bluetooth connection first",Toast.LENGTH_LONG).show()
            Log.e(TAG, "Bluetooth connection not established")
        }
    }

    private suspend fun getFITTRAIinformation(user_id: Int) {
        val aiMessageTextView: TextView = findViewById(R.id.ai_message)
        Log.i(TAG, "Getting FITTR AI information")

        // Start the strobing effect on the TextView
        startEllipsisAnimation(aiMessageTextView)

        // Call the API and get the result
        val result = apiClient.getUserAIReply(user_id)

        // Check if the result is successful
        result.onSuccess { aiReply ->
            // Stop the strobing animation and set the solid text

            stopEllipsisAnimation(aiMessageTextView, aiReply.feedback_message.summary_analysis)

            aiMessageTextView.alpha = 1f // Ensure it's fully visible before setting text
            aiMessageTextView.setTextColor(Color.parseColor("#8C52FD")) // Set text color to purple
            aiMessageTextView.text = aiReply.feedback_message.summary_analysis

            form_score = aiReply.feedback_message.form_score
            stability_score = aiReply.feedback_message.stability_score
            range_of_motion_score = aiReply.feedback_message.range_of_motion_score

            summary_analysis = aiReply.feedback_message.summary_analysis
            future_advice = aiReply.feedback_message.future_advice

            Log.i(TAG, "Form Score: $form_score")
            Log.i(TAG, "Stability Score: $stability_score")
            Log.i(TAG, "Range of Motion Score: $range_of_motion_score")


            // Make sure the TextView is fully visible after animation ends
            aiMessageTextView.visibility = View.VISIBLE

        }.onFailure { exception ->

            aiMessageTextView.alpha = 1f // Ensure it's fully visible
            aiMessageTextView.setTextColor(Color.RED) // Set color to red in case of failure
            aiMessageTextView.text = "Failed to load AI information: ${exception.localizedMessage}"

            // Make sure the TextView is fully visible after animation ends
            aiMessageTextView.visibility = View.VISIBLE
        }
    }

    suspend fun getAIExercisePlan(){
        val result = apiClient.getUserAIExercisePlan(userId = user.user_id)
        result.onSuccess { exercisePlan ->
            if(!exercisePlan.error.isNullOrEmpty()){
                Log.e(TAG,"Error from backend failing to load AI exercise plan: ${exercisePlan.error}")
                return
            }
            Log.i(TAG, "Exercise Plan: $exercisePlan")
            val animationJobs = exercisePlan.feedback_message.map { (exercise:Exercise, reps:Int?) ->
                exerciseReps[exercise] = reps ?: 0 // Store reps immediately
                lifecycleScope.launch { triggerLayoutAnimation(exercise) } // Launch animation
            }

            animationJobs.joinAll()
        }.onFailure { exception ->
            Log.e(TAG, "Exception to load AI exercise plan: ${exception}")
        }
    }

    private fun updateUIWithExerciseReps() {
        exerciseReps[Exercise.SQUATS]?.let { this.findViewById<EditText>(R.id.squat_exercise_rep).setText(it.toString()) }
        exerciseReps[Exercise.RIGHT_BICEP_CURLS]?.let { this.findViewById<EditText>(R.id.right_bicep_curl_exercise_rep).setText(it.toString()) }
        exerciseReps[Exercise.LEFT_BICEP_CURLS]?.let { this.findViewById<EditText>(R.id.left_bicep_curl_exercise_rep).setText(it.toString()) }
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
            val response = apiClient.getUser(ApiPaths.GetUser(userId),null)
            if(response.isSuccess){
                Log.i(TAG,"User information retrieved successfully")
                user = response.getOrNull()?.user!!
                runOnUiThread {
                    DashboardBinding.dashboardUserNameText.text =
                        "${user.first_name.replaceFirstChar { it.uppercase() }} " +
                                "${user.last_name.replaceFirstChar { it.uppercase() }}"
                }
            }
        }catch (e:Exception){
            Log.e(TAG,"Error getting user information: $e")
        }
    }

    private suspend fun getProductData(productId:Int){
        if (!::user.isInitialized) {
            Log.e(TAG, "User not initialised when calling get product")
            return
        }
        try{
            val response = apiClient.getProductData(ApiPaths.GetProduct(productId),null)
            if(response.isSuccess){
                productData = response.getOrNull()!!
            }else{
                Log.e(TAG,"Error getting product data: ${response.getOrNull()?.message}")
            }
        }catch (e:Exception) {
            Log.e(TAG, "Error getting product data: $e")
        }
    }

    private fun checkBluetoothConnection(): Boolean {
        // Check if the app has the BLUETOOTH_CONNECT permission (required for Android 12+)
        Log.d(TAG, "Checking Bluetooth connection")
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

    private fun applyIntegerFilterAndTypeMapping(editText: EditText, exercise: Exercise) {
        editText.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isDigit(source[i])) {
                    Toast.makeText(this, "Only whole numbers are allowed.", Toast.LENGTH_SHORT).show()
                    return@InputFilter ""
                }
            }
            null
        })
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    val value = input.toInt()
                    if (value > 0) {
                        exerciseReps[exercise] = value
                    } else {
                        editText.setText("")
                        exerciseReps.remove(exercise) // remove the exercise if value is not > 0
                        Toast.makeText(this@DashboardActivity, "Value must be greater than 0.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    exerciseReps.remove(exercise) // remove the exercise if input is empty
                }
            }
        })
    }

    private suspend fun triggerLayoutAnimation(exercise: Exercise) {
        val frameLayout = when (exercise) {
            Exercise.SQUATS -> findViewById<FrameLayout>(R.id.dashboard_squats_frame)
            Exercise.RIGHT_BICEP_CURLS -> findViewById<FrameLayout>(R.id.dashboard_bicep_curl_right_frame)
            Exercise.LEFT_BICEP_CURLS -> findViewById<FrameLayout>(R.id.dashboard_bicep_curl_left_frame)
            else -> return // no animation required
        }
        if(frameLayout != null){
            withContext(Dispatchers.Main) {
                animateBackgroundChange(frameLayout) // Ensures this animation completes before proceeding
            }
        }
    }

    private suspend fun animateBackgroundChange(frameLayout: FrameLayout) {
        withContext(Dispatchers.Main) {
            val startColor = Color.parseColor("#8C52FD") // Purple
            val endColor = Color.parseColor("#FFFFFF")   // White

            // Create a GradientDrawable with the same rounded corners
            val originalDrawable = ContextCompat.getDrawable(this@DashboardActivity, R.drawable.rounded_white_view) as GradientDrawable
            val cornerRadius = originalDrawable.cornerRadius

            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(startColor, endColor)
            )
            gradientDrawable.cornerRadius = cornerRadius

            frameLayout.background = gradientDrawable

            // Create a ValueAnimator for gradient transition
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 1000
            animator.interpolator = AccelerateDecelerateInterpolator()

            animator.addUpdateListener { animation ->
                val progress = animation.animatedFraction
                val newStartColor = blendColors(startColor, endColor, progress)
                val newEndColor = blendColors(endColor, startColor, progress)

                gradientDrawable.colors = intArrayOf(newStartColor, newEndColor)
                frameLayout.background = gradientDrawable
            }

            animator.start()

            // Wait before transitioning back
            delay(1500)

            // Start fade-out animation to transition back to rounded_white_view
            val fadeOutAnimator = ObjectAnimator.ofFloat(frameLayout, "alpha", 1f, 0f)
            fadeOutAnimator.duration = 500
            fadeOutAnimator.interpolator = AccelerateDecelerateInterpolator()
            fadeOutAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    // Reset the background when fade-out completes
                    frameLayout.setBackgroundResource(R.drawable.rounded_white_view)

                    // Start fade-in animation
                    val fadeInAnimator = ObjectAnimator.ofFloat(frameLayout, "alpha", 0f, 1f)
                    fadeInAnimator.duration = 500
                    fadeInAnimator.interpolator = AccelerateDecelerateInterpolator()
                    fadeInAnimator.start()
                }
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })

            fadeOutAnimator.start()
        }
    }

    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = (Color.red(color1) * inverseRatio + Color.red(color2) * ratio).toInt()
        val g = (Color.green(color1) * inverseRatio + Color.green(color2) * ratio).toInt()
        val b = (Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio).toInt()
        return Color.rgb(r, g, b)
    }


}