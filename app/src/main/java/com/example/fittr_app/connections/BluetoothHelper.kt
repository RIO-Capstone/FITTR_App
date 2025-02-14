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
import java.util.LinkedList
import java.util.UUID

@SuppressLint("MissingPermission")
object BluetoothHelper {
    private var bluetoothGatt: BluetoothGatt? = null
    private var serviceUUID: String = "4c72c9a7-69af-4a0b-8630-fab8f513fb9e" // Can be set dynamically
    private var characteristicUUID: String = ""
    private var readMode = false
    private var writeMode = false
    private var messageToSend: String? = null
    private var currentCallback: BluetoothReadCallback? = null
    private val connectionTimeoutHandler = Handler(Looper.getMainLooper())
    private var device: BluetoothDevice? = null
    private var operationInProgress = false
    private var operationsQueue = LinkedList<BluetoothOperation>()

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
        writeMode = false
        return handleConnection(context)
    }

    fun connectAndSendMessage(context: Context, message: String, characteristicUUID: String, callback: BluetoothReadCallback): Boolean {
        this.messageToSend = message
        this.characteristicUUID = characteristicUUID
        this.currentCallback = callback
        readMode = false
        writeMode = true
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
        return bluetoothGatt != null
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
                currentCallback?.onBluetoothConnectionChange(true)
            } else {
                Log.e("BluetoothHelper", "Service discovery failed with status $status")
                currentCallback?.onError("Service discovery failed with status $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic.value.toString(Charsets.UTF_8)
                Log.d("BluetoothHelper", "Characteristic read success: $value")
                currentCallback?.onValueRead(value)
            } else {
                Log.e("BluetoothHelper", "Read failed with status $status")
                currentCallback?.onError("Read failed with status $status")
            }
            operationInProgress = false
            processNextOperation()
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothHelper", "Write successful")
                currentCallback?.onValueRead("Write successful")
            } else {
                Log.e("BluetoothHelper", "Write failed with status $status")
                currentCallback?.onError("Write failed with status $status")
            }
            operationInProgress = false
            processNextOperation()
        }
    }

    fun queueReadOperation(characteristicUUID: String, callback: BluetoothReadCallback) {
        operationsQueue.add(BluetoothOperation.Read(characteristicUUID, callback))
        processNextOperation()
    }

    fun queueWriteOperation(
        message: String,
        characteristicUUID: String,
        callback: BluetoothReadCallback
    ) {
        operationsQueue.add(BluetoothOperation.Write(characteristicUUID, message, callback))
        processNextOperation()
    }

    private fun processNextOperation() {
        if (operationInProgress || operationsQueue.isEmpty()) return
        operationInProgress = true

        when (val operation = operationsQueue.poll()) {
            is BluetoothOperation.Read -> {
                currentCallback = operation.callback
                bluetoothGatt?.getService(UUID.fromString(serviceUUID))
                    ?.getCharacteristic(UUID.fromString(operation.characteristicUUID))
                    ?.let { characteristic ->
                        bluetoothGatt?.readCharacteristic(characteristic)
                    } ?: run {
                    Log.e("BluetoothHelper", "Characteristic not found for reading")
                    currentCallback?.onError("Characteristic not found")
                    operationInProgress = false
                    processNextOperation()
                }
            }

            is BluetoothOperation.Write -> {
                currentCallback = operation.callback
                bluetoothGatt?.getService(UUID.fromString(serviceUUID))
                    ?.getCharacteristic(UUID.fromString(operation.characteristicUUID))
                    ?.let { characteristic ->
                        characteristic.value = operation.message.toByteArray(Charsets.UTF_8)
                        val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false
                        if (!success) {
                            Log.e("BluetoothHelper", "Failed to send message")
                            currentCallback?.onError("Failed to send message")
                            operationInProgress = false
                            processNextOperation()
                        }
                    } ?: run {
                    Log.e("BluetoothHelper", "Characteristic not found for writing")
                    currentCallback?.onError("Characteristic not found")
                    operationInProgress = false
                    processNextOperation()
                }
            }
            else -> {
                operationInProgress = false
                processNextOperation()
            }
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    sealed class BluetoothOperation {
        data class Read(val characteristicUUID: String, val callback: BluetoothReadCallback) : BluetoothOperation()
        data class Write(val characteristicUUID: String, val message: String, val callback: BluetoothReadCallback) : BluetoothOperation()
    }
}
