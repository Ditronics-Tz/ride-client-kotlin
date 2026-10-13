package com.example.ridepassenger2.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ridepassenger2.ui.navigation.PaymentFlowNav
import com.example.ridepassenger2.ui.navigation.ProfileFlowNav
import com.example.ridepassenger2.ui.navigation.SupportFlowNav
import com.example.ridepassenger2.ui.navigation.FlowGallery
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

/** Debug-only Screens launcher + per-flow runner with failure toggles. Never in release. */
class FlowDebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RIDEPASSENGER2Theme {
                var screen by remember { mutableStateOf("menu") }
                var forceFail by remember { mutableStateOf("none") }
                when (screen) {
                    "menu" -> Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                        Text("Screens (debug)")
                        Spacer(Modifier.height(8.dp))
                        Text("Failure toggle: $forceFail (wrong OTP / timeout / network / declined)")
                        listOf("none", "wrong OTP", "timeout", "network error", "declined").forEach { f ->
                            Button(onClick = { forceFail = f }) { Text(f) }
                            Spacer(Modifier.height(6.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { screen = "pay" }) { Text("Payment flow runner") }
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = { screen = "profile" }) { Text("Profile flow runner") }
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = { screen = "support" }) { Text("Support flow runner") }
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = { screen = "gallery" }) { Text("Full gallery") }
                    }
                    "pay" -> PaymentFlowNav()
                    "profile" -> ProfileFlowNav()
                    "support" -> SupportFlowNav()
                    else -> FlowGallery()
                }
            }
        }
    }
}
