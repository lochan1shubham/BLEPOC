package com.example.blepuc.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.UUID


private const val TAG = "BluetoothLeService"

open class BluetoothLeService : Service() {
    private val binder = LocalBinder()
    private var bluetoothGatt: BluetoothGatt? = null
    private var mBluetoothGattService: BluetoothGattService? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    companion object {
        const val STATE_DISCONNECTED: Int = 0

        const val STATE_CONNECTING: Int = 1

        const val STATE_CONNECTED: Int = 2

        const val ACTION_GATT_CONNECTED: String =
            "riz.com.bletest.BluetoothLeServices.ACTION_GATT_CONNECTED"

        const val ACTION_GATT_DISCONNECTED: String =
            "riz.com.bletest.BluetoothLeServices.ACTION_GATT_DISCONNECTED"

        const val ACTION_GATT_SERVICES_DISCOVERED: String =
            "riz.com.bletest.BluetoothLeServices.ACTION_GATT_SERVICES_DISCOVERED"

        const val ACTION_DATA_AVAILABLE: String =
            "riz.com.bletest.BluetoothLeServices.ACTION_DATA_AVAILABLE"

        const val EXTRA_DATA: String = "riz.com.bletest.BluetoothLeServices.EXTRA_DATA"


        val uuid_service: UUID = UUID.fromString("00002220-0000-1000-8000-00805f9b34fb")

        val uuid_recieve: UUID = UUID.fromString("00002221-0000-1000-8000-00805f9b34fb")

        val uuid_config: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun start(device: BluetoothDevice, bta: BluetoothAdapter?) {
        bluetoothAdapter = bta
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
    }

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // connect to the GATT server on the device
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                return true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.  Unable to connect.")
                return false
            }
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }

    private var connectionState = STATE_DISCONNECTED

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }

    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED
                broadcastUpdate(ACTION_GATT_CONNECTED)
                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE)
                //  mBluetoothGattService = gatt!!.getService(uuid_service)
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status)
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    @SuppressLint("MissingPermission")
    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(
        action: String?,
        characteristic: BluetoothGattCharacteristic
    ) {
        val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (uuid_service.equals(characteristic.getUuid())) {
            val flag = characteristic.getProperties()
            var format = -1
            if ((flag and 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16
                Log.d(TAG, "Heart rate format UINT16.")
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8
                Log.d(TAG, "Heart rate format UINT8.")
            }
            val heartRate = characteristic.getIntValue(format, 1)
            Log.d(TAG, String.format("Received heart rate: %d", heartRate))
            intent.putExtra(EXTRA_DATA, heartRate.toString())
        } else {
            // For all other profiles, writes the data formatted in HEX.
            val data = characteristic.getValue()
            if (data != null && data.size > 0) {
                val stringBuilder = StringBuilder(data.size)
                for (byteChar in data) stringBuilder.append(String.format("%02X ", byteChar))
                intent.putExtra(
                    EXTRA_DATA, String(data) + "\n" +
                            stringBuilder.toString()
                )
            }
        }
        sendBroadcast(intent, Manifest.permission.BLUETOOTH)
    }
}