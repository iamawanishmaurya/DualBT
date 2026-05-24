package com.xpwnit.dualbt.bt

import android.bluetooth.BluetoothDevice
import android.media.AudioDeviceInfo

data class BTDevice(
    val name: String,
    val address: String,
    val bluetoothDevice: BluetoothDevice? = null,
    val audioDeviceInfo: AudioDeviceInfo? = null,
    val isMock: Boolean = false
) {
    companion object {
        val MOCK_DEVICE_1 = BTDevice(
            name = "Mock Speaker 1",
            address = "AA:BB:CC:DD:EE:01",
            isMock = true
        )
        val MOCK_DEVICE_2 = BTDevice(
            name = "Mock Speaker 2",
            address = "AA:BB:CC:DD:EE:02",
            isMock = true
        )
    }
}
