package com.example.blepuc.util


object SampleGattAttributes {


        val HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"

        val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

        val attributes = hashMapOf<String, String>(
            "0000180d-0000-1000-8000-00805f9b34fb" to "Heart Rate Service",
            "0000180a-0000-1000-8000-00805f9b34fb" to "Device Information Service",
            HEART_RATE_MEASUREMENT to "Heart Rate Measurement",
            "00002a29-0000-1000-8000-00805f9b34fb" to "Manufacturer Name String"
        )


    fun lookup(uuid: String?, defaultName: String?): String? {
        val name = attributes.get(uuid)
        return if (name == null) defaultName else name
    }
}