package com.example.fittr_app.connections

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.fittr_app.BluetoothReadCallback
import java.util.UUID

@SuppressLint("MissingPermission")
object BluetoothHelper {
    private var bluetoothGatt: BluetoothGatt? = null
    private var serviceUUID: String = "4c72c9a7-69af-4a0b-8630-fab8f513fb9e" // Can be set dynamically
    private var characteristicUUID: String = ""
    private var readMode = false
    private var sendMode = false
    private var messageToSend: String? = null
    private var currentCallback: BluetoothReadCallback? = null
    private val connectionTimeoutHandler = Handler(Looper.getMainLooper())
    private var device: BluetoothDevice? = null
    private val connectionTimeoutRunnable = Runnable {
        Log.e("BluetoothHelper", "Connection timed out")
        currentCallback?.onError("Connection timed out")
        disconnect()
    }

    fun initialize(context: Context, device: BluetoothDevice, initCallback: BluetoothReadCallback) {
        Log.d("BluetoothHelper", "Initializing BluetoothHelper for: ${device.name ?: "Unknown"}")
        this.device = device
        this.currentCallback = initCallback
        handleConnection(context)
    }

    private fun startConnectionTimeout() {
        connectionTimeoutHandler.postDelayed(connectionTimeoutRunnable, 10000) // 10 sec timeout
    }

    private fun clearConnectionTimeout() {
        connectionTimeoutHandler.removeCallbacks(connectionTimeoutRunnable)
    }

    fun connectAndRead(context: Context, characteristicUUID: String, callback: BluetoothReadCallback): Boolean {
        this.characteristicUUID = characteristicUUID
        this.currentCallback = callback
        readMode = true
        sendMode = false
        return handleConnection(context)
    }

    fun connectAndSendMessage(context: Context, message: String, characteristicUUID: String, callback: BluetoothReadCallback): Boolean {
        this.messageToSend = message
        this.characteristicUUID = characteristicUUID
        this.currentCallback = callback
        readMode = false
        sendMode = true
        return handleConnection(context)
    }

    private fun handleConnection(context: Context): Boolean {
        if (device == null) {
            Log.e("BluetoothHelper", "No device specified for Bluetooth connection.")
            return false
        }

        if (bluetoothGatt != null) {
            Log.d("BluetoothHelper", "Already connected, discovering services...")
            bluetoothGatt?.discoverServices()
            return true
        }

        bluetoothGatt = device!!.connectGatt(context, false, gattCallback)
        Log.d("BluetoothHelper", "Attempting to connect to device: ${device?.name}")
        startConnectionTimeout()

        return bluetoothGatt != null
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BluetoothHelper", "Connection state changed: $status -> $newState")
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("BluetoothHelper", "Connected to GATT server.")
                clearConnectionTimeout()
                currentCallback?.onBluetoothConnectionChange(true)
                gatt.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.e("BluetoothHelper", "Disconnected from GATT server.")
                disconnect()
                currentCallback?.onBluetoothConnectionChange(false)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothHelper", "Services discovered successfully.")
                if (readMode) {
                    readCharacteristic(gatt)
                } else if (sendMode) {
                    sendMessage(gatt)
                }
            } else {
                Log.e("BluetoothHelper", "Service discovery failed: $status")
                currentCallback?.onError("Service discovery failed with status $status")
            }
        }

        private fun readCharacteristic(gatt: BluetoothGatt) {
            val service = gatt.getService(UUID.fromString(serviceUUID))
            val characteristic = service?.getCharacteristic(UUID.fromString(characteristicUUID))

            if (characteristic != null && isCharacteristicReadable(characteristic)) {
                Log.d("BluetoothHelper", "Reading characteristic: $characteristicUUID")
                val success = gatt.readCharacteristic(characteristic)
                if (!success) {
                    Log.e("BluetoothHelper", "Failed to read characteristic")
                    currentCallback?.onError("Failed to read characteristic")
                }
            } else {
                Log.e("BluetoothHelper", "Characteristic $characteristicUUID not found or not readable")
                currentCallback?.onError("Characteristic not found or not readable")
            }
        }

        private fun sendMessage(gatt: BluetoothGatt) {
            val service = gatt.getService(UUID.fromString(serviceUUID))
            val characteristic = service?.getCharacteristic(UUID.fromString(characteristicUUID))

            if (characteristic != null && isCharacteristicWritable(characteristic)) {
                characteristic.value = messageToSend?.toByteArray(Charsets.UTF_8)
                val success = gatt.writeCharacteristic(characteristic)

                if (success) {
                    Log.d("BluetoothHelper", "Message sent successfully: $messageToSend")
                    currentCallback?.onValueRead(messageToSend ?: "")
                } else {
                    Log.e("BluetoothHelper", "Failed to send message")
                    currentCallback?.onError("Failed to send message")
                }
            } else {
                Log.e("BluetoothHelper", "Characteristic $characteristicUUID not writable")
                currentCallback?.onError("Characteristic not writable")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic.value.toString(Charsets.UTF_8)
                Log.d("BluetoothHelper", "Characteristic read success: $value")
                currentCallback?.onValueRead(value)
            } else {
                Log.e("BluetoothHelper", "Failed to read characteristic with status $status")
                currentCallback?.onError("Read failed with status $status")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothHelper", "Characteristic write successful")
            } else {
                Log.e("BluetoothHelper", "Failed to write characteristic, status: $status")
                currentCallback?.onError("Write failed with status $status")
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
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
