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
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.data.repository.RideRepository
import com.example.ridepassenger2.data.repository.UserRepository
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RidaApiClient.init(applicationContext)
        val start = try {
            runBlocking {
                val loggedIn = SessionManager.isLoggedIn(this@MainActivity)
                if (loggedIn) {
                    UserRepository.refreshSessionFromApi(this@MainActivity)
                    RideRepository.refreshHistory()
                    RideRepository.restoreActiveRideIfAny()
                    RideRepository.refreshScheduled()
                }
                loggedIn
            }
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
