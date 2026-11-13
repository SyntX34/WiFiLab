package com.syntx34.wifilab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.syntx34.wifilab.ui.components.AppBackground
import com.syntx34.wifilab.ui.navigation.WiFiLabApp
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.WiFiLabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Navy900.toArgb()),
        )
        setContent {
            WiFiLabTheme {
                AppBackground {
                    WiFiLabApp()
                }
            }
        }
    }
}
