package com.example.ridepassenger2.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.ui.components.RidaBottomBar
import com.example.ridepassenger2.ui.screens.*
import kotlinx.coroutines.launch

object Routes {
    const val ONBOARDING = "onboarding"
    const val SIGN_IN = "sign_in"
    const val CREATE_ACCOUNT = "create_account"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME_MAP = "home_map"
    const val RIDE_DETAILS = "ride_details"
    const val RIDE_HISTORY = "ride_history"
    // Alias: Activity is the user-facing name for ride history
    const val ACTIVITY = RIDE_HISTORY
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val PAYMENT = "payment"
    const val SAVED_PLACES = "saved_places"
    const val SCHEDULE = "schedule"
    const val PACKAGE = "package"
    const val SAFETY = "safety"
    const val PERSONAL_INFO = "personal_info"
    const val RIDE_PREFS = "ride_prefs"
    const val HELP_FAQ = "help_faq"
    const val CONTACT_US = "contact_us"
}

// Bottom bar is shown only on the three primary tabs
private val BottomBarRoutes = setOf(Routes.HOME_MAP, Routes.RIDE_HISTORY, Routes.PROFILE)

@Composable
fun AppNavGraph(
    startDestination: String = Routes.ONBOARDING,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in BottomBarRoutes
    val context = LocalContext.current
    val ioScope = rememberCoroutineScope()

    fun logOut() {
        ioScope.launch {
            try { SessionManager.clear(context) } catch (_: Exception) { }
            navController.navigate(Routes.SIGN_IN) {
                popUpTo(Routes.HOME_MAP) { inclusive = true }
            }
        }
    }

    val selectedTab = when (currentRoute) {
        Routes.HOME_MAP -> "Home"
        Routes.RIDE_HISTORY -> "Activity"
        Routes.PROFILE -> "Profile"
        else -> "Home"
    }

    // Helpers for bottom-bar navigation — singleTop + restoreState, no duplicate stacks
    fun navigateBottomBar(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            // Keep HOME_MAP as the logical root for bottom-bar tabs
            popUpTo(Routes.HOME_MAP) {
                saveState = true
                inclusive = false
            }
            // If we are on onboarding/auth and jump to a tab, pop onboarding
            // (fallback: popUpTo onboarding inclusive is handled by auth flow)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                RidaBottomBar(
                    selected = selectedTab,
                    onHome = { navigateBottomBar(Routes.HOME_MAP) },
                    onActivity = { navigateBottomBar(Routes.RIDE_HISTORY) },
                    onCenter = {
                        // Center car button → live request flow. Clear any history selection
                        // so details shows the live ride (or empty), not a stale trip.
                        if (currentRoute != Routes.RIDE_DETAILS) {
                            com.example.ridepassenger2.data.mock.RideRepository.selectHistory(null)
                            navController.navigate(Routes.RIDE_DETAILS) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onProfile = { navigateBottomBar(Routes.PROFILE) }
                )
            }
        }
    ) { innerPadding ->
        // NavHost must respect Scaffold innerPadding so content isn't behind the bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onGetStarted = { navController.navigate(Routes.CREATE_ACCOUNT) },
                        onAlreadyHaveAccount = { navController.navigate(Routes.SIGN_IN) }
                    )
                }
                composable(Routes.SIGN_IN) {
                    SignInScreen(
                        onSignIn = {
                            navController.navigate(Routes.HOME_MAP) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        },
                        onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                        onSignUp = { navController.navigate(Routes.CREATE_ACCOUNT) },
                        onGoogle = {
                            navController.navigate(Routes.HOME_MAP) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.CREATE_ACCOUNT) {
                    CreateAccountScreen(
                        onBack = { navController.popBackStack() },
                        onCreateAccount = {
                            navController.navigate(Routes.HOME_MAP) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.FORGOT_PASSWORD) {
                    ForgotPasswordScreen(
                        onBack = { navController.popBackStack() },
                        onSendReset = { navController.popBackStack() },
                        onBackToSignIn = {
                            navController.navigate(Routes.SIGN_IN) {
                                popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.HOME_MAP) {
                    HomeMapScreen(
                        onProfile = { navigateBottomBar(Routes.PROFILE) },
                        onRequestRide = { navController.navigate(Routes.RIDE_DETAILS) },
                        onSchedule = { navController.navigate(Routes.SCHEDULE) },
                        onPackage = { navController.navigate(Routes.PACKAGE) },
                        onSafety = { navController.navigate(Routes.SAFETY) },
                        onSavedPlaces = { navController.navigate(Routes.SAVED_PLACES) },
                        onPayment = { navController.navigate(Routes.PAYMENT) }
                    )
                }
                composable(Routes.RIDE_DETAILS) {
                    RideDetailsScreen(
                        onBack = { navController.popBackStack() },
                        onCancel = { navController.popBackStack() }
                    )
                }
                composable(Routes.RIDE_HISTORY) {
                    RideHistoryScreen(
                        onRideClick = {
                            com.example.ridepassenger2.data.mock.RideRepository.selectHistory(it)
                            navController.navigate(Routes.RIDE_DETAILS)
                        }
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onSettings = { navController.navigate(Routes.SETTINGS) },
                        onMenuClick = { key ->
                            when (key) {
                                "personal" -> navController.navigate(Routes.PERSONAL_INFO)
                                "payment" -> navController.navigate(Routes.PAYMENT)
                                "addresses" -> navController.navigate(Routes.SAVED_PLACES)
                                "prefs" -> navController.navigate(Routes.RIDE_PREFS)
                                else -> navController.navigate(Routes.SETTINGS)
                            }
                        },
                        onLogOut = { logOut() }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onLogOut = { logOut() },
                        onPersonal = { navController.navigate(Routes.PERSONAL_INFO) },
                        onPayment = { navController.navigate(Routes.PAYMENT) },
                        onAddresses = { navController.navigate(Routes.SAVED_PLACES) },
                        onNotifications = { navController.navigate(Routes.RIDE_PREFS) },
                        onHelp = { navController.navigate(Routes.HELP_FAQ) },
                        onContact = { navController.navigate(Routes.CONTACT_US) }
                    )
                }
                composable(Routes.PAYMENT) {
                    PaymentMethodsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.SAVED_PLACES) {
                    SavedPlacesScreen(
                        onBack = { navController.popBackStack() },
                        onUsePlace = { q ->
                            PendingSearch.query = q
                            navController.popBackStack()
                        }
                    )
                }
                composable(Routes.SCHEDULE) { ScheduleRideScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.PACKAGE) {
                    PackageRideScreen(
                        onBack = { navController.popBackStack() },
                        onSent = { navController.navigate(Routes.RIDE_DETAILS) }
                    )
                }
                composable(Routes.SAFETY) { SafetyScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.PERSONAL_INFO) { PersonalInfoScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.RIDE_PREFS) { RidePreferencesScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.HELP_FAQ) { HelpFaqScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.CONTACT_US) { ContactUsScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}
