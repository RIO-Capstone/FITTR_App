package com.example.fittr_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fittr_app.media_pipe.PoseLandmarkerHelper


class MainViewModel : ViewModel() {

    private var _model = PoseLandmarkerHelper.MODEL_POSE_LANDMARKER_FULL
    private var _delegate: Int = PoseLandmarkerHelper.DELEGATE_CPU
    private var _minPoseDetectionConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE
    private var _minPoseTrackingConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_TRACKING_CONFIDENCE
    private var _minPosePresenceConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_PRESENCE_CONFIDENCE

    // LiveData for Motor State
    private val _motorState = MutableLiveData(PoseLandmarkerHelper.DEFAULT_MOTOR_STATE)
    val currentMotorState: LiveData<Boolean> get() = _motorState

    // LiveData for Resistance Value
    private val _leftResistance = MutableLiveData(PoseLandmarkerHelper.DEFAULT_RESISTANCE_VALUE)
    private val _rightResistance = MutableLiveData(PoseLandmarkerHelper.DEFAULT_RESISTANCE_VALUE)
    val leftCurrentResistance: LiveData<Float> get() = _leftResistance
    val rightCurrentResistance: LiveData<Float> get() = _rightResistance

    val currentDelegate: Int get() = _delegate
    val currentModel: Int get() = _model
    val currentMinPoseDetectionConfidence: Float get() = _minPoseDetectionConfidence
    val currentMinPoseTrackingConfidence: Float get() = _minPoseTrackingConfidence
    val currentMinPosePresenceConfidence: Float get() = _minPosePresenceConfidence

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

    fun setMinPoseDetectionConfidence(confidence: Float) {
        _minPoseDetectionConfidence = confidence
    }

    fun setMinPoseTrackingConfidence(confidence: Float) {
        _minPoseTrackingConfidence = confidence
    }

    fun setMinPosePresenceConfidence(confidence: Float) {
        _minPosePresenceConfidence = confidence
    }

    fun setMotorStateValue(state: Boolean) {
        _motorState.postValue(state)
    }

    fun setLeftResistanceValue(resistance: Float) {
        _leftResistance.postValue(resistance)
    }
    fun setRightResistanceValue(resistance: Float) {
        _rightResistance.postValue(resistance)
    }

    fun setModel(model: Int) {
        _model = model
    }
}
