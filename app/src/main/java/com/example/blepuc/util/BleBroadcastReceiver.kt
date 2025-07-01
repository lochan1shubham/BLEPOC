package com.example.blepuc.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blepuc.service.BluetoothLeService
import com.example.blepuc.service.BluetoothLeService.Companion.EXTRA_DATA
import com.example.blepuc.view.BaseActivity.Companion.LIST_NAME
import com.example.blepuc.view.BaseActivity.Companion.LIST_UUID
import com.example.blepuc.view.BaseActivity.Companion.bluetoothService


const val TAG = "BleBroadsastReceiver"

class BleBroadcastReceiver : BroadcastReceiver() {
    private val mData: MutableLiveData<String> = MutableLiveData<String>()
    val getData: LiveData<String> get() = mData

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothLeService.ACTION_GATT_CONNECTED -> {
                Log.d(TAG, "GATT connected")
            }

            BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                Log.d(TAG, "GATT Disconnected")
            }

            BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                Log.d(TAG, "Data Received: ")
                bluetoothService?.getSupportedGattServices()?.let { displayGattServices(it) }
            }

            BluetoothLeService.ACTION_DATA_AVAILABLE -> {
                val et = intent.getStringExtra(EXTRA_DATA)
                mData.postValue(et!!)
                Log.d(TAG, "Extra data ")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun displayGattServices(gattData: BluetoothGatt) {
        val gattServices: List<BluetoothGattService?> = gattData.services
        //if (gattServices == null) return
        var uuid: String? = null
        val unknownServiceString = "unknownServiceString"
        val unknownCharaString = "unknownCharaString"
        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
            mutableListOf()
        val mGattCharacteristics: MutableList<BluetoothGattCharacteristic> = mutableListOf()

        // Loops through available GATT Services.
        gattServices.forEach { gattService ->
            val currentServiceData = HashMap<String, String>()
            uuid = gattService?.uuid.toString()
            currentServiceData[LIST_NAME] =
                SampleGattAttributes.lookup(uuid, unknownServiceString)!!
            currentServiceData[LIST_UUID] = uuid
            gattServiceData += currentServiceData

            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
            val gattCharacteristics = gattService?.characteristics
            val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()

            // Loops through available Characteristics.
            gattCharacteristics?.forEach { gattCharacteristic ->
                charas += gattCharacteristic
                val currentCharaData: HashMap<String, String> = hashMapOf()
                uuid = gattCharacteristic.uuid.toString()
                currentCharaData[LIST_NAME] =
                    SampleGattAttributes.lookup(uuid, unknownCharaString)!!
                currentCharaData[LIST_UUID] = uuid
                gattCharacteristicGroupData += currentCharaData

                val flag = gattCharacteristic.properties
                var format = -1
                format = if ((flag and 0x01) != 0) {
                    BluetoothGattCharacteristic.FORMAT_UINT16
                } else {
                    BluetoothGattCharacteristic.FORMAT_UINT8
                }
                val isHeartRate =
                    gattData.readCharacteristic(gattCharacteristic) //gattCharacteristic.getIntValue(format, 1)
                Log.d(TAG, "Is reading heart data  $isHeartRate")
            }
            mGattCharacteristics += charas
            gattCharacteristicData += gattCharacteristicGroupData


        }
    }
}