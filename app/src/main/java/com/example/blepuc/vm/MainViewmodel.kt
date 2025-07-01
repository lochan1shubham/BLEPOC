package com.example.blepuc.vm

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blepuc.util.TestInterface
import kotlin.collections.mutableListOf


class MainViewmodel : ViewModel(){
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()
    private val SCAN_PERIOD: Long = 10000
    private val _bleDeviceList = MutableLiveData< HashSet<BluetoothDevice>>()
    val bleDeviceList: LiveData<HashSet<BluetoothDevice>> get() = _bleDeviceList

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            _bleDeviceList.value = (_bleDeviceList.value?.plus(result.device)?: hashSetOf(result.device)) as HashSet<BluetoothDevice>?
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("VM", "$errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun scanLeDevice() {
        _bleDeviceList.value = HashSet<BluetoothDevice> ()
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

    // val getGattData: LiveData<List<BluetoothGattService?>?> get() = IntermediateRepo.repoInstance.getMediatorLiveData()

}