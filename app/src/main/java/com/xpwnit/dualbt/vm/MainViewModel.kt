package com.xpwnit.dualbt.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpwnit.dualbt.bt.BTDevice
import com.xpwnit.dualbt.bt.BTRepository
import com.xpwnit.dualbt.logging.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val devices: List<BTDevice> = emptyList(),
    val selectedDevices: List<BTDevice> = emptyList(),
    val isStreaming: Boolean = false,
    val isEmulatorMode: Boolean = false,
    val statusMessage: String = "Select 2 speakers",
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val btRepository: BTRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        AppLogger.i("MainViewModel", "ViewModel initialized")
        viewModelScope.launch {
            btRepository.devices.collect { devices ->
                val isEmulator = devices.any { it.isMock }
                _uiState.value = _uiState.value.copy(
                    devices = devices,
                    isEmulatorMode = isEmulator,
                    statusMessage = if (isEmulator)
                        "Emulator mode — mock devices loaded"
                    else
                        "Select up to 2 speakers"
                )
                AppLogger.d("MainViewModel", "Devices updated: ${devices.size}, emulator=$isEmulator")
            }
        }
        refresh()
    }

    fun refresh() {
        AppLogger.i("MainViewModel", "Refresh requested")
        btRepository.refresh()
    }

    fun toggleDevice(device: BTDevice) {
        if (btRepository.isSelected(device)) {
            btRepository.deselectDevice(device)
        } else if (btRepository.getSelectedDevices().size < 2) {
            btRepository.selectDevice(device)
        }
        _uiState.value = _uiState.value.copy(
            selectedDevices = btRepository.getSelectedDevices(),
            statusMessage = when (btRepository.getSelectedDevices().size) {
                0 -> "Select 2 speakers"
                1 -> "Select 1 more speaker"
                2 -> "Ready to stream"
                else -> ""
            }
        )
    }

    fun startStreaming() {
        val devices = btRepository.getSelectedDevices()
        if (devices.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Select at least 1 device"
            )
            return
        }
        AppLogger.i("MainViewModel", "Starting streaming to ${devices.size} device(s)")
        _uiState.value = _uiState.value.copy(
            isStreaming = true,
            statusMessage = "Streaming to ${devices.size} speaker(s)",
            error = null
        )
    }

    fun stopStreaming() {
        AppLogger.i("MainViewModel", "Stopping streaming")
        _uiState.value = _uiState.value.copy(
            isStreaming = false,
            statusMessage = "Stopped"
        )
    }

    fun getSelectedDevices() = btRepository.getSelectedDevices()
    fun isSelected(device: BTDevice) = btRepository.isSelected(device)

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
