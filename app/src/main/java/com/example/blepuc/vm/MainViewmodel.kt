package com.example.blepuc.vm

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class MainViewmodel : ViewModel() {
    private val _bleDeviceList  = MutableLiveData<BluetoothDevice>()
    val bleDeviceList: LiveData<BluetoothDevice> get() = _bleDeviceList
//    companion object{
//        lateinit var bluetoothDevice : BluetoothDevice
//    }

//    init {
//        scanLeDevice()
//    }

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
          //  bluetoothDevice = result.device
            _bleDeviceList.value = result.device
            bluetoothLeScanner.stopScan(leScanCallback)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("VM", "$errorCode")
        }
    }

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    private val SCAN_PERIOD: Long = 10000


    @SuppressLint("MissingPermission")
    fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed(@RequiresPermission(Manifest.permission.BLUETOOTH_SCAN) {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }


}