package com.xpwnit.dualbt.bt

import com.xpwnit.dualbt.logging.AppLogger
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BTRepository @Inject constructor(
    private val scanner: BTScanner
) {
    val devices: StateFlow<List<BTDevice>> = scanner.devices

    private var selectedDevices = mutableListOf<BTDevice>()

    fun refresh() {
        AppLogger.i("BTRepository", "Refreshing device list")
        scanner.scanConnectedDevices()
    }

    fun selectDevice(device: BTDevice) {
        if (selectedDevices.size < 2 && device !in selectedDevices) {
            selectedDevices.add(device)
            AppLogger.i("BTRepository", "Selected device: ${device.name} (${selectedDevices.size}/2)")
        } else {
            AppLogger.w("BTRepository", "Cannot select device: ${device.name} — already at max or duplicate")
        }
    }

    fun deselectDevice(device: BTDevice) {
        selectedDevices.remove(device)
        AppLogger.i("BTRepository", "Deselected device: ${device.name} (${selectedDevices.size}/2)")
    }

    fun getSelectedDevices(): List<BTDevice> = selectedDevices.toList()

    fun isSelected(device: BTDevice) = device in selectedDevices
}
