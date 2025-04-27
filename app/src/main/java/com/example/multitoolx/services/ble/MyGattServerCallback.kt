package com.example.multitoolx.services.ble

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.UUID

/**
 * Custom GATT server callback to handle Bluetooth GATT operations.
 *
 * This class handles the following operations:
 * - Managing connection state changes (device connect/disconnect).
 * - Responding to characteristic read requests.
 * - Responding to characteristic write requests, including dynamic responses based on mappings.
 * - Handling descriptor write requests, including enabling/disabling notifications.
 *
 * @param context The context from which the callback is initiated. This is used for permission checks.
 * @param bundle A bundle containing data used to handle requests such as read responses and mappings.
 *
 * @property bluetoothGattServer The Bluetooth GATT server used to send responses to the client.
 */
class MyGattServerCallback(
    private val context: Context,
    private val bundle: Bundle
) : BluetoothGattServerCallback() {

    // Bluetooth GATT Server instance
    private var bluetoothGattServer: BluetoothGattServer? = null

    /**
     * Called when a Bluetooth device connects or disconnects.
     *
     * @param device The Bluetooth device.
     * @param status The connection status.
     * @param newState The new connection state.
     */
    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        println("Device connected: ${device.address}")
    }

    /**
     * Set the Bluetooth GATT server instance to be used for responding to requests.
     *
     * @param server The BluetoothGattServer to be used.
     */
    fun setGattServer(server: BluetoothGattServer?) {
        bluetoothGattServer = server
    }

    /**
     * Called when a read request for a characteristic is received.
     *
     * @param device The Bluetooth device that initiated the request.
     * @param requestId The request ID used to send the response.
     * @param offset The offset into the characteristic value where reading should start.
     * @param characteristic The characteristic that is being read.
     */
    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        Log.i("BLE", "Read request received. Sending response...")

        // Get the response from the bundle or use a default response
        val response = bundle.getString("readresponse") ?: "hi from server"
        val responseValue = response.toByteArray(Charsets.UTF_8)
        characteristic.value = responseValue

        // Check permissions before sending the response
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // Permission not granted, return early
        }

        // Send the response to the device
        bluetoothGattServer?.sendResponse(
            device,
            requestId,
            BluetoothGatt.GATT_SUCCESS,
            offset,
            responseValue
        )

        Log.i("BLE", "Response sent successfully.")
    }

    /**
     * Called when a write request for a characteristic is received.
     *
     * @param device The Bluetooth device that initiated the request.
     * @param requestId The request ID used to send the response.
     * @param characteristic The characteristic that is being written.
     * @param preparedWrite True if the write is prepared, false if it is executed immediately.
     * @param responseNeeded True if a response is required.
     * @param offset The offset into the characteristic value where the write should start.
     * @param value The value to be written to the characteristic.
     */
    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?
    ) {
        // Extract the mappings from the bundle and map them
        val mappingsList = bundle.getStringArrayList("mappings")
        val mappings = mappingsList?.mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        }?.toMap() ?: emptyMap()

        // Convert the written value to a string and log the message
        val message = value?.toString(Charsets.UTF_8) ?: ""
        Log.i("BLE", "Write request received: $message")

        // Send GATT response (ACK) if needed
        if (responseNeeded && bluetoothGattServer != null && device != null) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return // Permission not granted, return early
            }
            bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
        }

        // Handle the request based on the mappings
        val response = mappings[message]
        if (response != null) {
            characteristic?.value = response.toByteArray(Charsets.UTF_8)
            bluetoothGattServer?.notifyCharacteristicChanged(device, characteristic, false)
            Log.i("BLE", "Sent dynamic notification response: $response")
        } else {
            Log.w("BLE", "No mapping found for message: $message")
        }
    }

    /**
     * Called when a descriptor write request is received (e.g., for notifications).
     *
     * @param device The Bluetooth device that initiated the request.
     * @param requestId The request ID used to send the response.
     * @param descriptor The descriptor that is being written.
     * @param preparedWrite True if the write is prepared, false if it is executed immediately.
     * @param responseNeeded True if a response is required.
     * @param offset The offset into the descriptor value where the write should start.
     * @param value The value to be written to the descriptor.
     */
    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?, requestId: Int,
        descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean,
        responseNeeded: Boolean, offset: Int, value: ByteArray?
    ) {
        // Check if the descriptor is the one used for notifications
        if (descriptor?.uuid == UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")) {
            val char = descriptor?.characteristic
            val enabled = value?.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == true
            if (char != null) {
                Log.i("BLE", "Notifications ${if (enabled) "enabled" else "disabled"} for ${char.uuid}")
            }

            // Send a response if needed
            if (responseNeeded && bluetoothGattServer != null && device != null) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return // Permission not granted, return early
                }
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }
        } else {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
        }
    }
}
