package com.example.blepuc.view


import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blepuc.ui.theme.BlePucTheme
import com.example.blepuc.util.Permission
import com.example.blepuc.vm.MainViewmodel

private const val TAG = "MainActivity"

class MainActivity : BaseActivity() {


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewmodel: MainViewmodel = viewModel()
            val bleDevices = viewmodel.bleDeviceList.observeAsState(null)

            LaunchedEffect(Unit) {
                bleSupportAndRequestPermission(viewmodel)
            }
            BlePucTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    if (Permission.checkBluetoothEnabled(this)) {

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
                            viewmodel = viewmodel
                        )
//                        } else {
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .padding(16.dp)
//                            ) {
//                                Text(
//                                    text = "No permission"
//                                )
//                            }
//                        }
                    } else {
                        Column(
                            modifier = Modifier
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

            if (bleDevices.value != null) {
                // Code to manage Service lifecycle.
                setupServiceConnection(bleDevices.value!!.address)
                registerBroadcast(bleDevices.value!!.address)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun bleSupportAndRequestPermission(viewmodel: MainViewmodel) {
        when {
            !Permission.checkBleSupport(this) -> {
                Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show()
            }

            !Permission.checkBlePermissions(this) -> {
                Permission.requestAllBlePermissions(blePermissionsRequest)
            }

            else -> {
                viewmodel.scanLeDevice()
                //registerBroadcast(bleDevices.value!!.address)
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.S)
//    override fun onResume() {
//        super.onResume()
//        bleSupportAndRequestPermission()
////        if (Permission.checkBlePermissions(this)) {
////            android.os.Handler(Looper.getMainLooper()).postDelayed({
////                val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
////                bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
////            }, 3000)
////        }
//
//    }

    override fun onPause() {
        super.onPause()
        if (gattUpdateReceiver.isOrderedBroadcast) {
            unregisterReceiver(gattUpdateReceiver)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BlePucTheme {
        Greeting(
            "Android",
            requestLocationPermissions = {},
            requestPermissionBluetooth = {},
            viewmodel = MainViewmodel()
        )
    }
}

