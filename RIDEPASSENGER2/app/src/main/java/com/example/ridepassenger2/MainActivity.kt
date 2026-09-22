package com.example.ridepassenger2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ridepassenger2.ui.navigation.AppNavGraph
import com.example.ridepassenger2.ui.navigation.Routes
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme
import com.example.ridepassenger2.data.local.SessionManager
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Mock session: signed-in riders skip straight to Home.
        val start = try {
            runBlocking { SessionManager.isLoggedIn(this@MainActivity) }
        } catch (_: Exception) { false }
        setContent {
            RIDEPASSENGER2Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        startDestination = if (start) Routes.HOME_MAP else Routes.ONBOARDING
                    )
                }
            }
        }
    }
}
