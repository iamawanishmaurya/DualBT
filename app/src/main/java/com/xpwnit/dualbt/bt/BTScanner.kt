package com.xpwnit.dualbt.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.xpwnit.dualbt.logging.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BTScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val audioManager = context.getSystemService(AudioManager::class.java)

    private val _devices = MutableStateFlow<List<BTDevice>>(emptyList())
    val devices: StateFlow<List<BTDevice>> = _devices

    fun scanConnectedDevices() {
        AppLogger.i("BTScanner", "Scanning for connected BT audio devices...")
        val found = mutableListOf<BTDevice>()

        try {
            val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val btTypes = setOf(
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER
            )

            for (device in outputDevices) {
                if (device.type in btTypes) {
                    val pairedDevice = bluetoothAdapter?.bondedDevices
                        ?.find { it.address == device.address }
                    val btDevice = BTDevice(
                        name = device.productName?.toString() ?: "BT Device",
                        address = device.address ?: "unknown",
                        bluetoothDevice = pairedDevice,
                        audioDeviceInfo = device
                    )
                    found.add(btDevice)
                    AppLogger.i("BTScanner", "Found BT device: ${btDevice.name} (${btDevice.address})")
                }
            }
        } catch (e: SecurityException) {
            AppLogger.e("BTScanner", "Bluetooth permission denied", e)
        }

        // Emulator fallback
        if (found.isEmpty()) {
            AppLogger.w("BTScanner", "No BT devices found — loading mock devices (emulator mode)")
            found.add(BTDevice.MOCK_DEVICE_1)
            found.add(BTDevice.MOCK_DEVICE_2)
        }

        AppLogger.i("BTScanner", "Scan complete: ${found.size} device(s) found")
        _devices.value = found
    }
}
