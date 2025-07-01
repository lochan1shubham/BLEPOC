package com.example.blepuc.view

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.blepuc.util.BleBroadcastReceiver
import com.example.blepuc.vm.MainViewmodel

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    requestLocationPermissions: () -> Unit,
    requestPermissionBluetooth: () -> Unit,
    viewmodel: MainViewmodel,
    gattUpdateReceiver: BleBroadcastReceiver,
    onDeviceSelected: (String) -> Unit
) {
    val bleDevices = viewmodel.bleDeviceList.observeAsState().value
    val gattData = gattUpdateReceiver.getData.observeAsState().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Hello $name!", modifier = modifier
        )
        Row(
            modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { requestLocationPermissions() }) {
                Text("Location")
            }
            Button(onClick = { requestPermissionBluetooth() }) {
                Text("Bluetooth")
            }
            Button(onClick = {
                viewmodel.scanLeDevice()
            }) {
                Text("Scan")
            }
        }
        Text(
            text = "Devices",
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )

        bleDevices?.let {

            if (it.isNotEmpty()) {
                if (gattData != null) {
                    Text(text = "Heart Rate: $gattData")
                }
                DeviceList(
                    bleDevices = bleDevices,
                    onDeviceSelected = onDeviceSelected
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceList(
    bleDevices: HashSet<BluetoothDevice>,
    onDeviceSelected: (String) -> Unit
) {
    LazyColumn {
        items(bleDevices.size) { it ->
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    contentColor = MaterialTheme.colorScheme.surfaceDim
                ),
                onClick = { onDeviceSelected(bleDevices.elementAt(it).address) }

            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Name: ${bleDevices.elementAt(it).name ?: "Unknown"}",
                        color = Color.Gray
                    )
                    Text(text = "Device: ${bleDevices.elementAt(it).address}", color = Color.Gray)
                }
            }
        }
    }
}


