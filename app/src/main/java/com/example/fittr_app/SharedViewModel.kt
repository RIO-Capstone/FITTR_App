package com.example.fittr_app

import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fittr_app.types.Exercise
import java.text.SimpleDateFormat
import kotlin.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// TODO: Refactor the class to get rid of calibration and to UUIDS do not need to be Mutable

class SharedViewModel : ViewModel() {
    private val _selectedExercise = MutableLiveData<Exercise>()
    private val _isCalibrating = MutableLiveData<Boolean>()
    private val _deviceServiceUUID = MutableLiveData<String>()
    private val _deviceLeftResistanceUUID = MutableLiveData<String>()
    private val _deviceRightResistanceUUID = MutableLiveData<String>()
    private var _deviceExerciseInitializeUUID = "";
    private val _deviceStopUUID = MutableLiveData<String>()
    private var _userId = 0;
    private var _productId = 0;
    private val _timerValue = MutableLiveData<Long>().apply { value = 0L }
    val timerValue: LiveData<Long> get() = _timerValue
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var startTimeMillis: Long = 0L
    private var _totalRepCount: Int = 0;

    val isCalibrating: LiveData<Boolean> get() = _isCalibrating
    val selectedExercise: LiveData<Exercise> get() = _selectedExercise
    val deviceServiceUUID: String get() = _deviceServiceUUID.value.toString()
    val deviceLeftResistanceUUID: String get() = _deviceLeftResistanceUUID.value.toString()
    val deviceRightResistanceUUID:String get() = _deviceRightResistanceUUID.value.toString()
    val deviceExerciseInitializeUUID: String get() = _deviceExerciseInitializeUUID
    val deviceStopUUID: String get() = _deviceStopUUID.value.toString()
    val user_id: Int get() = _userId
    val product_id: Int get() = _productId
    private val _repCount = MutableLiveData<Int>()
    val repCount: LiveData<Int> = _repCount
    val totalRepCount: Int get() = _totalRepCount

    val displayText: MediatorLiveData<String> = MediatorLiveData<String>().apply {
        addSource(_repCount) { updateDisplayText() }
    }

    fun updateDisplayText() {
        displayText.value = "${selectedExercise.value.toString()} Count: ${_repCount.value ?: 0} \n Target: $totalRepCount"
    }

    fun setSelectedExercise(exercise: Exercise) {
        _selectedExercise.value = exercise
    }

    fun updateRepCount(count: Int) {
        _repCount.postValue(count)
    }

    fun setDeviceServiceUUID(uuid: String) {
        _deviceServiceUUID.value = uuid
    }

    fun setDeviceLeftResistanceUUID(uuid: String) {
        _deviceLeftResistanceUUID.value = uuid
    }
    fun setDeviceRightResistanceUUID(uuid: String) {
        _deviceRightResistanceUUID.value = uuid
    }

    fun setDeviceStopUUID(uuid: String) {
        _deviceStopUUID.value = uuid
    }

    fun setUserId(id: Int) {
        _userId = id
    }

    fun setProductId(id: Int) {
        _productId = id
    }

    fun setDeviceExerciseInitializeUUID(uuid: String) {
        _deviceExerciseInitializeUUID = uuid
    }

    fun setTotalRepCount(count: Int) {
        _totalRepCount = count
    }


    fun getExerciseSessionData():Map<String, Any>{
        val now = Date() // Get current date and time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC") // Set to UTC timezone
        val formattedDateTime = formatter.format(now)
        return mapOf(
            "exercise_type" to (selectedExercise.value?.toString() ?: ""),
            "rep_count" to (repCount.value ?: 0),
            "created_at" to formattedDateTime, // following the same format as the Django model created_at field
            "duration" to (timerValue.value?.div(1000)).toString(), // In seconds
            "errors" to 0, // TODO: Fix this hardcoded implementation
            "user_id" to (user_id),
        )
    }

    fun startTimer() {
        if (isTimerRunning) return // Avoid multiple starts

        startTimeMillis = SystemClock.elapsedRealtime() // Record start time
        isTimerRunning = true

        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 100) { // 100ms interval
            override fun onTick(millisUntilFinished: Long) {
                val elapsedTime = SystemClock.elapsedRealtime() - startTimeMillis
                _timerValue.postValue(elapsedTime)
            }

            override fun onFinish() {
                // Should never finish since it's running indefinitely
            }
        }.start()
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
    }

}
