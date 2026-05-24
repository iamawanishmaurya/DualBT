package com.xpwnit.dualbt

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.xpwnit.dualbt.logging.AppLogger
import com.xpwnit.dualbt.service.DualBTService
import com.xpwnit.dualbt.ui.MainScreen
import com.xpwnit.dualbt.ui.theme.DualBTTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val projectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            AppLogger.i("MainActivity", "MediaProjection permission granted")
            val intent = Intent(this, DualBTService::class.java).apply {
                putExtra(DualBTService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(DualBTService.EXTRA_RESULT_DATA, result.data)
            }
            startForegroundService(intent)
        } else {
            AppLogger.w("MainActivity", "MediaProjection permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i("MainActivity", "Activity created")

        setContent {
            DualBTTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        onRequestProjection = {
                            AppLogger.i("MainActivity", "Requesting MediaProjection")
                            projectionLauncher.launch(
                                projectionManager.createScreenCaptureIntent()
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        AppLogger.i("MainActivity", "Activity destroyed")
        super.onDestroy()
    }
}
