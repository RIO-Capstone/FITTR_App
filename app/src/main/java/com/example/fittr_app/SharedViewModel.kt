package com.example.fittr_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fittr_app.data_classes.ExerciseType

class SharedViewModel : ViewModel() {
    private val _selectedExercise = MutableLiveData<String>()
    private val _isCalibrating = MutableLiveData<Boolean>(true)
    val isCalibrating: LiveData<Boolean> get() = _isCalibrating
    val selectedExercise: LiveData<String> get() = _selectedExercise
    private val _repCount = MutableLiveData<Int>()
    val repCount: LiveData<Int> = _repCount

    fun setSelectedExercise(exercise: String) {
        _selectedExercise.value = exercise
    }

    fun updateRepCount(count: Int) {
        _repCount.value = count
    }


    fun updateCalibration(calibrating:Boolean){
        _isCalibrating.postValue(calibrating)
    }
}
