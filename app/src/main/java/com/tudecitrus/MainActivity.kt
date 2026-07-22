package com.tudecitrus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tudecitrus.ui.navigation.CitrusCareApp
import com.tudecitrus.ui.theme.CitrusScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CitrusScanTheme {
                CitrusCareApp()
            }
        }
    }
}
