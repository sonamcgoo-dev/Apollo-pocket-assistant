package com.appolopocket.ui.screens.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.appolopocket.ui.theme.AppoloPocketTheme
import com.appolopocket.ui.theme.VaporwaveColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            AppoloPocketTheme {
                SettingsScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}
