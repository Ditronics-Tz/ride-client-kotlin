package com.example.ridepassenger2.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import com.example.ridepassenger2.ui.navigation.openHomeAfterAuthentication
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController


/** Device-executable UI regressions; no server/provider or real account is required. */
class ScreenStateRegressionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun resetEmailCannotClaimItWasSentWithoutAnEndpoint() {
        var claimedSent = false
        compose.setContent { ForgotPasswordScreen({}, { claimedSent = true }, {}) }
        compose.onNode(hasSetTextAction()).performTextInput("rider@example.test")
        compose.onNodeWithText("Send Reset Link").performClick()
        compose.runOnIdle { assertFalse(claimedSent) }
        compose.onNodeWithText("Reset link sent").assertDoesNotExist()
    }

    @Test fun languagePickerCannotClaimAnUnimplementedLocaleChange() {
        var claimedSaved = false
        compose.setContent { LanguageSettingsScreen(onSaveLanguage = { claimedSaved = true }) }
        compose.onNodeWithText("Kiswahili").performClick()
        compose.onNodeWithText("Save language").performClick()
        compose.runOnIdle { assertFalse(claimedSaved) }
    }
    @Test fun validatingPromotionCannotClaimItWasAppliedToABooking() {
        var claimedApplied = false
        val api = java.lang.reflect.Proxy.newProxyInstance(
            com.example.ridepassenger2.data.remote.RidaApi::class.java.classLoader,
            arrayOf(com.example.ridepassenger2.data.remote.RidaApi::class.java)
        ) { _, method, _ -> when (method.name) {
            "getAvailablePromos" -> com.example.ridepassenger2.data.remote.PromoListResponse()
            "validatePromo" -> com.example.ridepassenger2.data.remote.PromoValidationResponse(valid = true, code = "SAVE", discountAmount = 200, finalFare = 800)
            else -> error(method.name)
        } } as com.example.ridepassenger2.data.remote.RidaApi
        compose.setContent { PromotionDetailsScreen(onApply = { claimedApplied = true }, fare = 1000, api = api) }
        compose.onNode(hasSetTextAction()).performScrollTo().performTextInput("SAVE")
        compose.onNodeWithText("Check promo eligibility").performClick()
        compose.onNodeWithText("Code eligible (preview)").assertExists()
        compose.runOnIdle { assertFalse(claimedApplied) }
        compose.onNodeWithText("Applied to Account").assertDoesNotExist()
    }

    @Test fun signInExplainsActualPersistenceInsteadOfAnIgnoredRememberChoice() {
        compose.setContent { SignInScreen(onSignIn = {}, onForgotPassword = {}, onSignUp = {}, onGoogle = {}) }
        compose.onNodeWithText("You stay signed in until you log out.").assertExists()
        compose.onNodeWithText("Remember me").assertDoesNotExist()
    }

    @Test fun successfulSignupCannotGoBackToAnEarlierLoginScreen() {
        lateinit var controller: NavHostController
        compose.setContent {
            controller = rememberNavController()
            NavHost(controller, startDestination = com.example.ridepassenger2.ui.navigation.Routes.SIGN_IN) {
                composable(com.example.ridepassenger2.ui.navigation.Routes.SIGN_IN) { androidx.compose.material3.Text("Login fixture") }
                composable(com.example.ridepassenger2.ui.navigation.Routes.CREATE_ACCOUNT) { androidx.compose.material3.Text("Signup fixture") }
                composable(com.example.ridepassenger2.ui.navigation.Routes.HOME_MAP) { androidx.compose.material3.Text("Home fixture") }
            }
        }
        compose.runOnIdle {
            controller.navigate(com.example.ridepassenger2.ui.navigation.Routes.CREATE_ACCOUNT)
            controller.openHomeAfterAuthentication()
            org.junit.Assert.assertEquals(com.example.ridepassenger2.ui.navigation.Routes.HOME_MAP, controller.currentDestination?.route)
            org.junit.Assert.assertNull(controller.previousBackStackEntry)
        }
    }

}
