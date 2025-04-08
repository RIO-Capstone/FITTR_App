package com.example.fittr_app.media_pipe

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Camera
import androidx.camera.core.AspectRatio
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.example.fittr_app.BluetoothReadCallback
import com.example.fittr_app.MainViewModel
import com.example.fittr_app.R
import com.example.fittr_app.SharedViewModel
import com.example.fittr_app.connections.BluetoothHelper
import com.example.fittr_app.databinding.FragmentCameraBinding
import com.example.fittr_app.connections.WebSocketClient
import com.example.fittr_app.types.Exercise
import com.example.fittr_app.utils.TextToSpeechHelper
import com.google.gson.Gson
import com.google.mediapipe.tasks.vision.core.RunningMode
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.Request
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.TimeSource

interface BackendResponseHandler {
    fun handleBackendResponse(response: BackendResponse)
}

data class BackendResponse (
    val rep_count: Int? = null,
    val error: String? = null,
    val message: String? = null
)
data class ClientMessage(
   val results: PoseLandmarkerHelper.ResultBundle,
    val is_calibrated: Boolean? = false
 )

class CameraFragment : Fragment(), PoseLandmarkerHelper.LandmarkerListener, BackendResponseHandler {

    companion object {
        private const val TAG = "CameraFragment"
    }

    private lateinit var webSocket: WebSocket
    private lateinit var sharedViewModel: SharedViewModel
    private var textToSpeech = TextToSpeechHelper
    private val client = OkHttpClient()
    private val IP_ADDRESS = "172.20.10.9";

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private var isPaused = false

    /** Blocking backend operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            )
            .navigate(R.id.action_camera_to_permissions)
        }

        // Start the PoseLandmarkerHelper again when users come back
        // to the foreground.
        backgroundExecutor.execute {
            if(this::poseLandmarkerHelper.isInitialized) {
                if (poseLandmarkerHelper.isClose()) {
                    poseLandmarkerHelper.setupPoseLandmarker()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(this::poseLandmarkerHelper.isInitialized) {
            viewModel.setMinPoseDetectionConfidence(poseLandmarkerHelper.minPoseDetectionConfidence)
            viewModel.setMinPoseTrackingConfidence(poseLandmarkerHelper.minPoseTrackingConfidence)
            viewModel.setMinPosePresenceConfidence(poseLandmarkerHelper.minPosePresenceConfidence)
            viewModel.setDelegate(poseLandmarkerHelper.currentDelegate)

            // Close the PoseLandmarkerHelper and release resources
            backgroundExecutor.execute { poseLandmarkerHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        BluetoothHelper.queueWriteOperation(message = "false",
            characteristicUUID = sharedViewModel.deviceExerciseInitializeUUID,
            object : BluetoothReadCallback  {
                override fun onValueRead(value: String) {
                    Log.i(TAG,"Successfully disabled exercise initialization characteristic")
                }
                override fun onError(message: String) {
                    Log.e(TAG,"Error disabling exercise initialization characteristic: $message")
                }
            })
        sharedViewModel.stopTimer()
        requireActivity().findViewById<TextView>(R.id.timer_text).visibility = View.INVISIBLE
        super.onDestroyView()

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
        // close the web socket connection
        if (::webSocket.isInitialized) {
            webSocket.close(1000, "Closing WebSocket connection")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)
        // initialise the shared view model to get the exercise being performed
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        Log.i(TAG,"Currently performing exercise: ${sharedViewModel.selectedExercise.value}")
        connectWebSocket() // start the web socket connection now
        return fragmentCameraBinding.root
    }

    private fun connectWebSocket() {
        val backend_address = "ws://${IP_ADDRESS}:8000/ws/exercise/" +
                "${sharedViewModel.user_id}/${sharedViewModel.product_id}/${sharedViewModel.selectedExercise.value}"
        val request = Request.Builder()
            .url(backend_address)  // IP and Port using secure websocket connection
            .build()
        Log.i(TAG,"Connecting to $backend_address")
        val listener = WebSocketClient(this)
        webSocket = client.newWebSocket(request, listener)

        // Shutdown client when the activity/fragment is destroyed
        client.dispatcher.executorService.shutdown()
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set up live data state listeners
//        #viewModel.currentMotorState.observe(viewLifecycleOwner) { motorState ->
//            #fragmentCameraBinding.bottomSheetLayout.motorStateToggle.isChecked = motorState
//        }

        val updateFunction = exerciseToUpdateMap(sharedViewModel.selectedExercise.value!!)

        // When clicked, send a bluetooth feedback_message to FITTR to decrease resistance on the motors
        // once a successful response is received, then update the UI
        when(sharedViewModel.selectedExercise.value){
            Exercise.RIGHT_BICEP_CURLS -> initializeRightMotor(updateFunction)
            Exercise.LEFT_BICEP_CURLS -> initializeLeftMotor(updateFunction)
            else -> initializeLeftMotor(updateFunction)
        }

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Create the PoseLandmarkerHelper that will handle the inference
        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = viewModel.currentMinPoseDetectionConfidence,
                minPoseTrackingConfidence = viewModel.currentMinPoseTrackingConfidence,
                minPosePresenceConfidence = viewModel.currentMinPosePresenceConfidence,
                currentDelegate = viewModel.currentDelegate,
                poseLandmarkerHelperListener = this
            )
        }

        // Attach listeners to UI control widgets and update default UI values
        initBottomSheetControls()
    }

    private fun initializeRightMotor(updateFunction: (Float) -> Unit){
        viewModel.rightCurrentResistance.observe(viewLifecycleOwner){resistanceValue->
            fragmentCameraBinding.bottomSheetLayout.resistanceValue.text =
                String.format(Locale.US,"%.2f",resistanceValue)
        }
        BluetoothHelper.queueReadOperation(sharedViewModel.deviceRightResistanceUUID,
            object : BluetoothReadCallback{
                override fun onValueRead(value: String) {
                    handleRightUIUpdateResistance(value.toFloat())
                }
                override fun onError(message: String) {
                    Log.e(TAG, "Error reading right resistance value from Bluetooth: $message")
                }
            }
        )

        // Setting the left motor resistance to 0, using only right motor for now
        BluetoothHelper.queueWriteOperation(
            "0",
            sharedViewModel.deviceLeftResistanceUUID,
            object : BluetoothReadCallback{
                override fun onValueRead(value: String) = Unit // no UI update required

                override fun onError(message: String) {
                    Log.e(TAG, "Error updating the left resistance value $message")
                }
            }
        )

        fragmentCameraBinding.bottomSheetLayout.resistanceMinus.setOnClickListener {
            val currentResistance = viewModel.rightCurrentResistance.value ?: PoseLandmarkerHelper.DEFAULT_RESISTANCE_VALUE
            if (currentResistance > PoseLandmarkerHelper.MIN_RESISTANCE_VALUE) {
                val newResistance = currentResistance - 1
                updateFunction(newResistance)
            } else {
                Log.w(TAG, "Cannot set resistance value lower than the minimum")
            }
        }

        fragmentCameraBinding.bottomSheetLayout.resistancePlus.setOnClickListener {
            val currentResistance = viewModel.rightCurrentResistance.value ?: PoseLandmarkerHelper.DEFAULT_RESISTANCE_VALUE
            if (currentResistance < PoseLandmarkerHelper.MAX_RESISTANCE_VALUE) {
                val newResistance = currentResistance + 1
                updateFunction(newResistance)
            } else {
                Log.w(TAG, "Cannot set left resistance value greater than the maximum")
            }
        }

    }

    private fun initializeLeftMotor(updateFunction: (Float) -> Unit){
        viewModel.leftCurrentResistance.observe(viewLifecycleOwner) { resistanceValue ->
            fragmentCameraBinding.bottomSheetLayout.resistanceValue.text =
                String.format(Locale.US,"%.2f",resistanceValue)
        }
        BluetoothHelper.queueReadOperation(sharedViewModel.deviceLeftResistanceUUID,
            object : BluetoothReadCallback{
                override fun onValueRead(value: String) {
                    handleLeftUIUpdateResistance(value.toFloat())
                }
                override fun onError(message: String) {
                    Log.e(TAG, "Error reading left resistance value from Bluetooth: $message")
                }})
        // Setting the right motor resistance to 0, using only left motor for now
        BluetoothHelper.queueWriteOperation(
            "0",
            sharedViewModel.deviceRightResistanceUUID,
            object : BluetoothReadCallback{
                override fun onValueRead(value: String) = Unit

                override fun onError(message: String) {
                    Log.e(TAG, "Error updating the right resistance value $message")
                }
            }
        )
        fragmentCameraBinding.bottomSheetLayout.resistanceMinus.setOnClickListener {
            val currentResistance = viewModel.leftCurrentResistance.value ?: PoseLandmarkerHelper.MIN_RESISTANCE_VALUE
            if (currentResistance > PoseLandmarkerHelper.MIN_RESISTANCE_VALUE) {
                val newResistance = currentResistance - 1
                updateFunction(newResistance)
            } else {
                Log.w(TAG, "Cannot set resistance value lower than the minimum")
            }
        }

        fragmentCameraBinding.bottomSheetLayout.resistancePlus.setOnClickListener {
            val currentResistance = viewModel.leftCurrentResistance.value ?: PoseLandmarkerHelper.MIN_RESISTANCE_VALUE
            if (currentResistance < PoseLandmarkerHelper.MAX_RESISTANCE_VALUE) {
                val newResistance = currentResistance + 1
                updateFunction(newResistance)
            } else {
                Log.w(TAG, "Cannot set left resistance value greater than the maximum")
            }
        }
    }

    private fun initBottomSheetControls() {
        val exerciseStopButton = fragmentCameraBinding.bottomSheetLayout.exerciseStopButton
        val pauseAndPlayButton = fragmentCameraBinding.bottomSheetLayout.exercisePauseAndPlayButton
        exerciseStopButton.setOnClickListener{
            showStopConfirmationDialog()
        }
        pauseAndPlayButton.setOnClickListener{
            togglePausePlay()
        }
        // init bottom sheet settings (should not start without already establishing a bluetooth connection in the parent activity)
        BluetoothHelper.queueReadOperation(sharedViewModel.deviceStopUUID,
            object : BluetoothReadCallback {
            override fun onValueRead(value: String) {
                handleUpdateMotorState(value.toBoolean())
            }
            override fun onError(message: String) {
                Log.e(TAG, "Error reading motor value from Bluetooth: $message")
            }
        })

//        fragmentCameraBinding.bottomSheetLayout.motorStateToggle.setOnClickListener {
//            val currentState = viewModel.currentMotorState.value ?: PoseLandmarkerHelper.DEFAULT_MOTOR_STATE
//            val newState = !currentState
//            sendMotorStateUpdate(newState)
//        }
    }

    private fun exerciseToUpdateMap(exercise: Exercise): (Float) -> Unit {
        return when (exercise) {
            Exercise.SQUATS -> this::handleResistanceUpdate
            Exercise.RIGHT_BICEP_CURLS -> this::handleRightResistanceUpdate
            Exercise.LEFT_BICEP_CURLS -> this::handleLeftResistanceUpdate
            else -> this::handleResistanceUpdate // Handle UNKNOWN case
        }
    }

    private fun handleRightResistanceUpdate(newResistance: Float){
        BluetoothHelper.queueWriteOperation(
            newResistance.toString(),
            sharedViewModel.deviceRightResistanceUUID,
            object : BluetoothReadCallback{
                override fun onValueRead(value: String) {
                    handleRightUIUpdateResistance(newResistance)
                }

                override fun onError(message: String) {
                    Log.e(TAG, "Error updating the right resistance value $message")
                }
            }
        )
    }

    private fun handleLeftResistanceUpdate(newResistance: Float){
        BluetoothHelper.queueWriteOperation(
            newResistance.toString(),
            sharedViewModel.deviceLeftResistanceUUID,
            object : BluetoothReadCallback{
                override fun onValueRead(value: String) {
                    handleLeftUIUpdateResistance(newResistance)
                }

                override fun onError(message: String) {
                    Log.e(TAG, "Error updating the right resistance value $message")
                }
            }
        )
    }

    private fun handleResistanceUpdate(newResistance: Float) {
        // Right resistance
        BluetoothHelper.queueWriteOperation(
            newResistance.toString(),
            sharedViewModel.deviceRightResistanceUUID,
            object : BluetoothReadCallback {
                override fun onValueRead(value: String) {
                    //handleRightUIUpdateResistance(newResistance)
                }

                override fun onError(message: String) {
                    Log.e(TAG, "Error updating right resistance: $message")
                }
            }
        )

        // Left resistance
        BluetoothHelper.queueWriteOperation(
            newResistance.toString(),
            sharedViewModel.deviceLeftResistanceUUID,
            object : BluetoothReadCallback {
                override fun onValueRead(value: String) {
                    handleLeftUIUpdateResistance(newResistance)
                }

                override fun onError(message: String) {
                    Log.e(TAG, "Error updating left resistance: $message")
                }
            }
        )
    }

    private fun sendMotorStateUpdate(newState: Boolean) {
        BluetoothHelper.queueWriteOperation(
            newState.toString(),
            sharedViewModel.deviceStopUUID,
            object : BluetoothReadCallback {
                override fun onValueRead(value: String) {
                    handleUpdateMotorState(newState)
                }
                override fun onError(message: String) {
                    Log.e("CameraFragment", message)
                }
            })
    }

    private fun handleLeftUIUpdateResistance(value:Float){
        try{
            Log.i(TAG,"Handling left resistance value: $value")
            activity?.runOnUiThread{
                viewModel.setLeftResistanceValue(value)
            }
        }catch (e:NumberFormatException){
            Log.e(TAG, "Invalid resistance value: $value")
        }catch (e:Exception){
            Log.e(TAG, "Error updating resistance: $e")
        }
    }

    private fun handleRightUIUpdateResistance(value:Float){
        try {
            Log.i(TAG, "Handling right resistance value: $value")
            activity?.runOnUiThread {
                viewModel.setRightResistanceValue(value)
            }
        }catch (e:NumberFormatException){
            Log.e(TAG, "Invalid resistance value: $value")
        }catch (e:Exception){
            Log.e(TAG, "Error updating resistance: $e")
        }
    }

    private fun handleUpdateMotorState(value:Boolean){
        try{
            Log.i(TAG,"Handling motor state value: $value")
            activity?.runOnUiThread{
                viewModel.setMotorStateValue(value)
            }
        }catch (e:NumberFormatException){
            Log.e(TAG, "Invalid resistance value: $value")
        }catch (e:Exception){
            Log.e(TAG, "Error updating resistance: $e")
        }
    }

    private fun stopExercise() {
        Log.i(TAG, "Stopping exercise")
        requireActivity().finish() // finish the main activity, onDestroyView called automatically
    }

    private fun showStopConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Stop Exercise")
            .setMessage("Are you sure you want to stop the exercise?")
            .setPositiveButton("Yes") { _, _ ->
                stopExercise()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun togglePausePlay() {
        isPaused = !isPaused
        if (isPaused) {
            pauseCamera()
            fragmentCameraBinding.bottomSheetLayout.exercisePauseAndPlayButton.setImageResource(R.drawable.play_button_foreground)
        } else {
            resumeCamera()
            fragmentCameraBinding.bottomSheetLayout.exercisePauseAndPlayButton.setImageResource(R.drawable.pause_button_foreground)
        }
    }

    private fun pauseCamera() {
        imageAnalyzer?.clearAnalyzer() // Stop image analysis
    }

    private fun resumeCamera() {
        imageAnalyzer?.setAnalyzer(backgroundExecutor) { image ->
            detectPose(image) // Resume image analysis
        }
    }

    private fun navigateToExerciseSuccess() {
        activity?.runOnUiThread {
            textToSpeech.speak("Congratulations")
            val navController = try {
                NavHostFragment.findNavController(this)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Navigation failed: ${e.message}")
                return@runOnUiThread
            }
            requireActivity().findViewById<View>(R.id.rep_count).visibility = View.INVISIBLE
            navController.navigate(R.id.action_camera_to_exercise_success)
        }
    }


    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectPose(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectPose(imageProxy: ImageProxy) {
        if(this::poseLandmarkerHelper.isInitialized) {
            poseLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after pose have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView
    override fun onResults(
        resultBundle: PoseLandmarkerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {

                val message = ClientMessage(results=resultBundle,
                    is_calibrated = sharedViewModel.isCalibrating.value == false)
                val jsonResult: String = Gson().toJson(message)
                webSocket.send(jsonResult) // sends results per frame to backend

                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun handleBackendResponse(response: BackendResponse) {
        activity?.runOnUiThread {
            response.rep_count?.let { repCount ->
                sharedViewModel.updateRepCount(repCount);
                if (repCount >= sharedViewModel.totalRepCount) {
                    navigateToExerciseSuccess()
                }
            }

            response.error?.let { error ->
                // Show an error message
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }

        }
    }


    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }
}