package com.example.fittr_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fittr_app.types.Exercise

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
}
