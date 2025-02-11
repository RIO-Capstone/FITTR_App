package com.example.fittr_app.connections

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import com.example.fittr_app.BluetoothReadCallback

@SuppressLint("MissingPermission")
object BluetoothHelper{
    private var bluetoothGatt: BluetoothGatt? = null
    private var serviceUUID = "4c72c9a7-69af-4a0b-8630-fab8f513fb9e" // TODO: Retrieve dynamically
    private var characteristicUUID = "" // To be specified dynamically in the read/send function
    private var readMode = false
    private var sendMode = false
    private var messageToSend: String? = null
    private var currentCallback: BluetoothReadCallback? = null
    private val connectionTimeoutHandler = Handler(Looper.getMainLooper())
    private var device: BluetoothDevice? = null
    private val connectionTimeoutRunnable = Runnable {
        if (bluetoothGatt == null) {
            Log.e("BluetoothHelper", "Connection timed out")
            currentCallback?.onError("Connection timed out")
        }
    }
    fun initialize(context: Context,device: BluetoothDevice,initCallback: BluetoothReadCallback){
        val deviceName = device.name ?: "Unknown Device"
        val deviceAddress = device.address
        Log.d("BluetoothHelper", "Initializing with Device Name: $deviceName, Address: $deviceAddress")
        this.device = device
        this.currentCallback = initCallback
        handleConnection(context)
    }
    private fun startConnectionTimeout() {
        connectionTimeoutHandler.postDelayed(connectionTimeoutRunnable, 10000) // 10-second timeout
    }

    private fun clearConnectionTimeout() {
        connectionTimeoutHandler.removeCallbacks(connectionTimeoutRunnable)
    }

    // Reading characteristics dynamically
    fun connectAndRead(context: Context, characteristicUUID: String, callback: BluetoothReadCallback): Boolean {
        this.characteristicUUID = characteristicUUID
        this.currentCallback = callback
        readMode = true
        sendMode = false

        return handleConnection(context)
    }

    // Function to write a characteristic value
    fun connectAndSendMessage(context: Context, message: String, characteristicUUID: String, callback: BluetoothReadCallback): Boolean {
        this.messageToSend = message
        this.characteristicUUID = characteristicUUID
        this.currentCallback = callback
        readMode = false
        sendMode = true
        Log.d("BluetoothHelper", "Connecting and sending message: $message")
        return handleConnection(context)
    }

    // Handles connection logic
    private fun handleConnection(context: Context): Boolean {
        if(device == null){
            Log.e("BluetoothHelper", "Unable to complete bluetooth operation as device is null")
            return false
        }
        // Check if the device is already connected
        if (bluetoothGatt != null) {
            Log.d("BluetoothHelper", "Device already connected. Proceeding with operation.")
            bluetoothGatt?.discoverServices()
            return true
        }
        bluetoothGatt = device!!.connectGatt(context, true, gattCallback)
        Log.d("BluetoothHelper", "Connecting to device: ${device?.name}}")
        // retry connection until timeout
        startConnectionTimeout()
        if (bluetoothGatt == null) {
            Log.e("BluetoothHelper", "Failed to connect to ${device!!.name}")
            currentCallback?.onError("Failed to connect to ${device!!.name}")
            clearConnectionTimeout()
            return false
        }
        return true
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.d("BluetoothHelper", "Device onConnectionStateChange: status: $status, newState: $newState")
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("BluetoothHelper", "Successfully connected to GATT server")
                currentCallback?.onBluetoothConnectionChange(true)
                gatt.discoverServices()
            } else{
                Log.e("BluetoothHelper", "Disconnected from GATT server")
                disconnect()
                currentCallback?.onBluetoothConnectionChange(false)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothHelper", "GATT services discovered")
                if (readMode) {
                    readCharacteristic(gatt)
                } else if (sendMode) {
                    sendMessage(gatt)
                }
            } else {
                Log.e("BluetoothHelper", "Service discovery failed with status $status")
                currentCallback?.onError("Service discovery failed with status $status")
            }
        }

        private fun readCharacteristic(gatt: BluetoothGatt) {
            val service = gatt.getService(java.util.UUID.fromString(serviceUUID))
            if (service != null) {
                Log.d("BluetoothHelper", "Reading from device ${service.uuid}")
                val characteristic = service.getCharacteristic(java.util.UUID.fromString(characteristicUUID))
                if (characteristic != null && isCharacteristicReadable(characteristic)) {
                    val success = gatt.readCharacteristic(characteristic)
                    if (success) {
                        Log.d("BluetoothHelper", "Read request sent successfully")
                    } else {
                        Log.e("BluetoothHelper", "Failed to send read request")
                        currentCallback?.onError("Failed to send read request")
                    }
                } else {
                    Log.e("BluetoothHelper", "Characteristic $characteristicUUID not found or not readable")
                    currentCallback?.onError("Characteristic not found or not readable")
                }
            } else {
                Log.e("BluetoothHelper", "Service not found")
                currentCallback?.onError("Service not found")
            }
        }

        private fun sendMessage(gatt: BluetoothGatt) {
            val service = gatt.getService(java.util.UUID.fromString(serviceUUID))
            if (service != null) {
                Log.d("BluetoothHelper", "Sending message to ${service.uuid}")
                val characteristic = service.getCharacteristic(java.util.UUID.fromString(characteristicUUID))
                if(characteristic == null){
                    Log.e("BluetoothHelper", "Characteristic $characteristicUUID not found")
                    currentCallback?.onError("Characteristic not found")
                    return
                }
                if (isCharacteristicWritable(characteristic)) {
                    characteristic.value = messageToSend?.toByteArray(Charsets.UTF_8)
                    val success = gatt.writeCharacteristic(characteristic)
                    if (success) {
                        Log.d("BluetoothHelper", "Message sent successfully: $messageToSend")
                        currentCallback?.onValueRead(messageToSend?:"")
                    } else {
                        Log.e("BluetoothHelper", "Failed to send message")
                        currentCallback?.onError("Failed to send message")
                    }
                } else {
                    Log.e("BluetoothHelper", "Characteristic not writable")
                    currentCallback?.onError("Characteristic $characteristicUUID not found or not writable")
                }
            } else {
                Log.e("BluetoothHelper", "Service not found")
                currentCallback?.onError("Service $serviceUUID not found")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic.value.toString(Charsets.UTF_8)
                Log.d("BluetoothHelper", "Characteristic read successfully: $value")
                currentCallback?.onValueRead(value)
            } else {
                Log.e("BluetoothHelper", "Failed to read characteristic with status $status")
                currentCallback?.onError("Failed to read characteristic with status $status")
            }
        }

        private fun isCharacteristicWritable(characteristic: BluetoothGattCharacteristic): Boolean {
            return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 ||
                    (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0
        }

        private fun isCharacteristicReadable(characteristic: BluetoothGattCharacteristic): Boolean {
            return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) > 0
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt = null
    }
}
