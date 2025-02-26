package com.example.fittr_app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fittr_app.types.Exercise
import kotlin.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
    private var _duration: Duration = Duration.ZERO

    val isCalibrating: LiveData<Boolean> get() = _isCalibrating
    val selectedExercise: LiveData<Exercise> get() = _selectedExercise
    val deviceServiceUUID: String get() = _deviceServiceUUID.value.toString()
    val deviceLeftResistanceUUID: String get() = _deviceLeftResistanceUUID.value.toString()
    val deviceRightResistanceUUID:String get() = _deviceRightResistanceUUID.value.toString()
    val deviceExerciseInitializeUUID: String get() = _deviceExerciseInitializeUUID
    val deviceStopUUID: String get() = _deviceStopUUID.value.toString()
    val user_id: Int get() = _userId
    val product_id: Int get() = _productId
    val duration: Duration get() = _duration
    private val _repCount = MutableLiveData<Int>()
    val repCount: LiveData<Int> = _repCount

    val displayText: MediatorLiveData<String> = MediatorLiveData<String>().apply {
        addSource(_repCount) { updateDisplayText() }
        addSource(_isCalibrating) { updateDisplayText() }
    }

    private fun updateDisplayText() {
        displayText.value = if (_isCalibrating.value == true) {
            "Calibrating..."
        } else {
            "${selectedExercise.value.toString()} Count: ${_repCount.value ?: 0}"
        }
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

    fun updateCalibration(calibrating:Boolean){
        _isCalibrating.postValue(calibrating)
    }

    fun setDuration(duration:Duration){
        _duration = duration
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExerciseSessionData():Map<String, Any>{
        val now = LocalDateTime.now(ZoneOffset.UTC) // Get current UTC time
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val formattedDateTime = now.format(formatter) + "Z"
        return mapOf(
            "exercise_type" to (selectedExercise.value?.toString() ?: ""),
            "rep_count" to (repCount.value ?: 0),
            "created_at" to formattedDateTime, // following the same format as the Django model created_at field
            "duration" to (duration.inWholeMilliseconds/1000).toString(), // In seconds
            "errors" to 0, // TODO: Fix this hardcoded implementation
            "user_id" to (user_id),
        )
    }
}
