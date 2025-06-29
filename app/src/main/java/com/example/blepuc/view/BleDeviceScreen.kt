package com.example.blepuc.view

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.blepuc.vm.MainViewmodel
import kotlinx.coroutines.delay

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    requestLocationPermissions: () -> Unit,
    requestPermissionBluetooth: () -> Unit,
    viewmodel: MainViewmodel
) {
    val bleDevices = viewmodel.bleDeviceList.observeAsState(null)


    LifecycleResumeEffect(Unit) {
        Log.d("asdf", "sadf")

        onPauseOrDispose {
            // Do something on pause or dispose effect
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { requestLocationPermissions() }) {
                Text("Requests Location")
            }
            Button(onClick = { requestPermissionBluetooth() }) {
                Text("Requests BLE")
            }
        }
        Text(
            text = "Scan Devices",
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )

        // Add 5 items
        Text(text = "Device: ${bleDevices.value} ")
    }

}
