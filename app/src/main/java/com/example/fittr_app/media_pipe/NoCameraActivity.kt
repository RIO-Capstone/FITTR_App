package com.example.fittr_app.media_pipe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fittr_app.BluetoothReadCallback
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.connections.BluetoothHelper
import com.example.fittr_app.databinding.ActivityNoCameraBinding
import com.example.fittr_app.media_pipe.PoseLandmarkerHelper.Companion.MIN_RESISTANCE_VALUE

class NoCameraActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "NoCameraActivity"
    }
    private lateinit var binding: ActivityNoCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val user_id = intent.getStringExtra("user_id")
        val leftDeviceResistanceUUID = intent.getStringExtra("leftResistanceUUID")
        val rightDeviceResistanceUUID = intent.getStringExtra("rightResistanceUUID")

        binding.noCameraExerciseStopButton.setOnClickListener {
            val dashboardIntent = Intent(this, DashboardActivity::class.java)
            dashboardIntent.putExtra("user_id",user_id)
            startActivity(dashboardIntent)
        }

        binding.noCameraResistancePlus.setOnClickListener {
            val currentResistance = binding.noCameraResistanceValue.text.toString().toFloat()
            handleResistanceUpdate(currentResistance+1.0f,rightDeviceResistanceUUID!!,leftDeviceResistanceUUID!!)
        }
        binding.noCameraResistanceMinus.setOnClickListener {
            val currentResistance = binding.noCameraResistanceValue.text.toString().toFloat()
            handleResistanceUpdate(currentResistance-1.0f,rightDeviceResistanceUUID!!,leftDeviceResistanceUUID!!)
        }
        // reset the resistance value to the minimum before the free flow exercise
        handleResistanceUpdate(MIN_RESISTANCE_VALUE,rightDeviceResistanceUUID!!,leftDeviceResistanceUUID!!)
        binding.noCameraResistanceValue.text = MIN_RESISTANCE_VALUE.toString()
    }

    /**
     * Handles updating the resistance values for both the left and right devices via Bluetooth.
     * @param newResistance The new resistance value (Float) to be set on both devices.
     * @param rightDeviceResistanceUUID The UUID for the right motor.
     * @param leftDeviceResistanceUUID The UUID for the left motor.
     */
    private fun handleResistanceUpdate(newResistance: Float,rightDeviceResistanceUUID: String,leftDeviceResistanceUUID: String) {
        if(newResistance < MIN_RESISTANCE_VALUE){
            Toast.makeText(this,"Cannot set resistance value lower than the minimum", Toast.LENGTH_LONG).show()
            return
        }
        // Right resistance
        BluetoothHelper.queueWriteOperation(
            newResistance.toString(),
            rightDeviceResistanceUUID,
            object : BluetoothReadCallback {
                override fun onValueRead(value: String) {
                    Log.i(TAG,"Handling right resistance value: $value")
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
            leftDeviceResistanceUUID,
            object : BluetoothReadCallback {
                override fun onValueRead(value: String) {
                    Log.i(TAG,"Handling left resistance value: $value")
                    handleUIUpdateResistance(newResistance)
                }

                override fun onError(message: String) {
                    Log.e(TAG, "Error updating left resistance: $message")
                }
            }
        )
    }

    private fun handleUIUpdateResistance(newResistance: Float){
        this.runOnUiThread {
            binding.noCameraResistanceValue.text = newResistance.toString()
        }
    }
}