package com.appolopocket.ui.screens.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.appolopocket.ui.screens.settings.SettingsActivity
import com.appolopocket.ui.theme.AppoloPocketTheme
import com.appolopocket.ui.theme.VaporwaveColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            AppoloPocketTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VaporwaveColors.DeepNight
                ) {
                    MainScreen(
                        onNavigateToSettings = {
                            startActivity(
                                android.content.Intent(
                                    this@MainActivity,
                                    SettingsActivity::class.java
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
