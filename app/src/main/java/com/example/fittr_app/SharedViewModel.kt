package com.example.fittr_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// TODO: Refactor the class to get rid of calibration and to UUIDS do not need to be Mutable

class SharedViewModel : ViewModel() {
    private val _selectedExercise = MutableLiveData<String>()
    private val _isCalibrating = MutableLiveData<Boolean>()
    private val _deviceServiceUUID = MutableLiveData<String>()
    private val _deviceResistanceUUID = MutableLiveData<String>()
    private val _deviceStopUUID = MutableLiveData<String>()
    private var _userId = 0;
    private var _productId = 0;

    val isCalibrating: LiveData<Boolean> get() = _isCalibrating
    val selectedExercise: LiveData<String> get() = _selectedExercise
    val deviceServiceUUID: String get() = _deviceServiceUUID.value.toString()
    val deviceResistanceUUID: String get() = _deviceResistanceUUID.value.toString()
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
            "Rep Count: ${_repCount.value ?: 0}"
        }
    }

    fun setSelectedExercise(exercise: String) {
        _selectedExercise.value = exercise
    }

    fun updateRepCount(count: Int) {
        _repCount.postValue(count)
    }

    fun setDeviceServiceUUID(uuid: String) {
        _deviceServiceUUID.value = uuid
    }

    fun setDeviceResistanceUUID(uuid: String) {
        _deviceResistanceUUID.value = uuid
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

    fun updateCalibration(calibrating:Boolean){
        _isCalibrating.postValue(calibrating)
    }
}
