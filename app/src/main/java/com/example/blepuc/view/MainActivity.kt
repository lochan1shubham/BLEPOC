package com.example.blepuc.view


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blepuc.service.BluetoothLeService
import com.example.blepuc.ui.theme.BlePucTheme
import com.example.blepuc.util.BleBroadcastReceiver
import com.example.blepuc.util.Permission
import com.example.blepuc.vm.MainViewmodel

private const val TAG = "MainActivity"

class MainActivity : BaseActivity() {
    val gattUpdateReceiver = BleBroadcastReceiver()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewmodel: MainViewmodel = viewModel()
//            val bleDevices = viewmodel.bleDeviceList.observeAsState(null)

            BlePucTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    if (Permission.checkBluetoothEnabled(this)) {
                        bleSupportAndRequestPermission()
//                        if (Permission.checkBlePermissions(this)) {
//                            viewmodel.scanLeDevice()
                        Greeting(
                            name = "BLE",
                            modifier = Modifier.padding(innerPadding),
                            requestLocationPermissions = {
                                Permission.requestPermissionLocation(
                                    blePermissionsRequest
                                )
                            },
                            requestPermissionBluetooth = {
                                Permission.requestPermissionBluetooth(
                                    blePermissionsRequest
                                )
                            },
                            viewmodel = viewmodel,
                            gattUpdateReceiver = gattUpdateReceiver,
                            onDeviceSelected = { address -> onDeviceSelected(address) })
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Please enable the bluetooth"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onDeviceSelected(address: String) {
        if (gattUpdateReceiver.isOrderedBroadcast) {
            unregisterReceiver(gattUpdateReceiver)
        }
        registerBroadcast(address, gattUpdateReceiver)
        val serviceConnection = setupServiceConnection(address)
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
            bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
        }, 3000)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun bleSupportAndRequestPermission() {
        when {
            !Permission.checkBleSupport(this) -> {
                Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show()
            }

            !Permission.checkBlePermissions(this) -> {
                Permission.requestAllBlePermissions(blePermissionsRequest)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        if (gattUpdateReceiver.isOrderedBroadcast) {
            unregisterReceiver(gattUpdateReceiver)
        }
    }
}
