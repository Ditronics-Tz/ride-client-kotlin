package com.example.ridepassenger2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ridepassenger2.ui.screens.RidaSplashScreen
import com.example.ridepassenger2.ui.navigation.AppNavGraph
import com.example.ridepassenger2.ui.navigation.Routes
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.data.repository.RideRepository
import com.example.ridepassenger2.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RidaApiClient.init(applicationContext)
        setContent {
            RIDEPASSENGER2Theme {
                var startDestination by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    startDestination = try {
                        withContext(Dispatchers.IO) {
                            val loggedIn = SessionManager.isLoggedIn(this@MainActivity)
                            if (loggedIn) {
                                UserRepository.refreshSessionFromApi(this@MainActivity)
                                RideRepository.refreshHistory()
                                RideRepository.restoreActiveRideIfAny()
                                RideRepository.refreshScheduled()
                            }
                            if (loggedIn) Routes.HOME_MAP else Routes.ONBOARDING
                        }
                    } catch (_: Exception) {
                        Routes.ONBOARDING
                    }
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    val destination = startDestination
                    if (destination == null) RidaSplashScreen()
                    else AppNavGraph(startDestination = destination)
                }
            }
        }
    }
}
