package com.example.simple_world.services.ble



import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.UUID

class MyGattServerCallback(
    private val context: Context,
    private val bundle: Bundle

) : BluetoothGattServerCallback() {
    private var bluetoothGattServer: BluetoothGattServer? = null

    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        println("Device connected: ${device.address}")
    }
    fun setGattServer(server: BluetoothGattServer?) {
        bluetoothGattServer = server
    }
    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        Log.i("BLE", "Read request received. Sending response...")
        val response=bundle.getString("readresponse")?:"hi from server"
        val responseValue = response.toByteArray(Charsets.UTF_8)
        characteristic.value = responseValue

        val success = if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        } else {
            //println("failed to have permission...")
        }
        if (bluetoothGattServer!=null){
            bluetoothGattServer!!.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                responseValue
            )
        }
        else{

        }


        Log.i("BLE", "sendResponse returned: $success")
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?
    ) {

        val mappingsList = bundle.getStringArrayList("mappings")
        val mappings = mappingsList?.mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        }?.toMap() ?: emptyMap()

        val message = value?.toString(Charsets.UTF_8) ?: ""
        Log.i("BLE", "Write request received: $message")

        // Send GATT response (ACK) if needed
        if (responseNeeded && bluetoothGattServer != null && device != null) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
        }

        // For now, handle hardcoded logic
        val response = mappings[message] // `mappings` should be passed or accessible here

        if (response != null) {
            characteristic?.value = response.toByteArray(Charsets.UTF_8)
            bluetoothGattServer?.notifyCharacteristicChanged(device, characteristic, false)
            Log.i("BLE", "Sent dynamic notification response: $response")
        } else {
            Log.w("BLE", "No mapping found for message: $message")
        }
    }
    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?, requestId: Int,
        descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean,
        responseNeeded: Boolean, offset: Int, value: ByteArray?
    ) {
        if (descriptor?.uuid == UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")) {
            val char = descriptor?.characteristic
            val enabled = value?.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == true
            if (char != null) {
                Log.i("BLE", "Notifications ${if (enabled) "enabled" else "disabled"} for ${char.uuid}")
            }

            if (responseNeeded && bluetoothGattServer != null && device != null) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    return
                }
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }
        } else {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
        }
    }

}
