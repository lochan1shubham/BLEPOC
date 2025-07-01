package com.example.blepuc.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.blepuc.service.BluetoothLeService
import com.example.blepuc.util.BleBroadcastReceiver
import com.example.blepuc.util.Permission
import com.example.blepuc.util.SampleGattAttributes

private const val TAG = "BaseActivity"

abstract class BaseActivity : ComponentActivity() {

    val blePermissionsRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                Toast.makeText(this, "Fine granted", Toast.LENGTH_SHORT).show()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                Toast.makeText(this, "Coarse granted", Toast.LENGTH_SHORT).show()
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH, false) -> {
                // Precise location access granted.
                Toast.makeText(this, "BLUETOOTH", Toast.LENGTH_SHORT).show()
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH_ADMIN, false) -> {
                // Only approximate location access granted.
                Toast.makeText(this, "BLUETOOTH_ADMIN", Toast.LENGTH_SHORT).show()
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false) -> {
                // Precise location access granted.
                Toast.makeText(this, "BLUETOOTH_SCAN", Toast.LENGTH_SHORT).show()
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false) -> {
                // Only approximate location access granted.
                Toast.makeText(this, "BLUETOOTH_CONNECT", Toast.LENGTH_SHORT).show()
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH_ADVERTISE, false) -> {
                // Only approximate location access granted.
                Toast.makeText(this, "BLUETOOTH_ADV", Toast.LENGTH_SHORT).show()
            }

            else -> {
                // No location access granted.
                Toast.makeText(this, "No location Granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var btAdapter: BluetoothAdapter? = null
    var uuid: String = "00002220-0000-1000-8000-00805f9b34fb"

    companion object {
        var bluetoothService: BluetoothLeService? = null
        const val LIST_NAME = "NAME"
        const val LIST_UUID = "UUID"
    }

//    private fun displayGattServices(gattServices: List<BluetoothGattService?>?) {
//        if (gattServices == null) return
//        var uuid: String?
//        val unknownServiceString = "unknownServiceString"
//        val unknownCharaString = "unknownCharaString"
//        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
//        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
//            mutableListOf()
//        //  val mGattCharacteristics = mutableListOf<String>()
//
//        // Loops through available GATT Services.
//        gattServices.forEach { gattService ->
//            val currentServiceData = HashMap<String, String>()
//            uuid = gattService?.uuid.toString()
//            currentServiceData[LIST_NAME] =
//                SampleGattAttributes.lookup(uuid, unknownServiceString)!!
//            currentServiceData[LIST_UUID] = uuid
//            gattServiceData += currentServiceData
//
//            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
//            val gattCharacteristics = gattService?.characteristics
//            val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()
//
//            // Loops through available Characteristics.
//            gattCharacteristics?.forEach { gattCharacteristic ->
//                charas += gattCharacteristic
//                val currentCharaData: HashMap<String, String> = hashMapOf()
//                uuid = gattCharacteristic.uuid.toString()
//                currentCharaData[LIST_NAME] =
//                    SampleGattAttributes.lookup(uuid, unknownCharaString)!!
//                currentCharaData[LIST_UUID] = uuid
//                gattCharacteristicGroupData += currentCharaData
//            }
//            //    mGattCharacteristics += charas
//            gattCharacteristicData += gattCharacteristicGroupData
//        }
//    }

    fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        }
    }


    fun registerBroadcast(address: String, gattUpdateReceiver: BleBroadcastReceiver) {
        if (Permission.checkBlePermissions(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    gattUpdateReceiver,
                    makeGattUpdateIntentFilter(),
                    RECEIVER_EXPORTED
                )
            } else {
                registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
            }
            // IntermediateRepo.repoInstance.addDataSource(gattUpdateReceiver.getData())
            if (bluetoothService != null) {
                val result = bluetoothService!!.connect(address)
                Log.d(TAG, "Connect request result=$result")
            }

        }

    }

    fun setupServiceConnection(address: String) = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName, service: IBinder
        ) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                // call functions on service to check connection and connect to devices
                if (!bluetooth.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")
                    finish()
                }
                bluetooth.connect(address)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }
}