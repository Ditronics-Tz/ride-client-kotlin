package com.example.ridepassenger2.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ridepassenger2.data.local.AppPrefs
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.repository.SupportRepository
import com.example.ridepassenger2.ui.common.OtpState
import com.example.ridepassenger2.ui.common.otpTick
import com.example.ridepassenger2.ui.payment.AddMethodScreen
import com.example.ridepassenger2.ui.payment.MethodsUiState
import com.example.ridepassenger2.ui.payment.MmEvent
import com.example.ridepassenger2.ui.payment.MmSetupState
import com.example.ridepassenger2.ui.payment.MmVerifyPhase
import com.example.ridepassenger2.ui.payment.PayDetailScreen
import com.example.ridepassenger2.ui.payment.PayFilter
import com.example.ridepassenger2.ui.payment.PayHistoryScreen
import com.example.ridepassenger2.ui.payment.PayMethod
import com.example.ridepassenger2.ui.payment.PayMethodsFlowScreen
import com.example.ridepassenger2.ui.payment.PayTripPhase
import com.example.ridepassenger2.ui.payment.PayTripScreen
import com.example.ridepassenger2.ui.payment.PayTripState
import com.example.ridepassenger2.ui.payment.mmReduce
import com.example.ridepassenger2.ui.preview.FakePayments
import com.example.ridepassenger2.ui.preview.FakeSupport
import com.example.ridepassenger2.ui.profile.ChangeEmailScreen
import com.example.ridepassenger2.ui.profile.ChangePhoneScreen
import com.example.ridepassenger2.ui.profile.DeleteAccountScreen
import com.example.ridepassenger2.ui.profile.EditNameScreen
import com.example.ridepassenger2.ui.profile.EmailChangeState
import com.example.ridepassenger2.ui.profile.EmailStep
import com.example.ridepassenger2.ui.profile.NameEditState
import com.example.ridepassenger2.ui.profile.PhoneChangeState
import com.example.ridepassenger2.ui.profile.PhoneStep
import com.example.ridepassenger2.ui.profile.PhotoChangeScreen
import com.example.ridepassenger2.ui.profile.ProfileVerifyRows
import com.example.ridepassenger2.ui.profile.SecurityScreen
import com.example.ridepassenger2.ui.profile.emailValidate
import com.example.ridepassenger2.ui.profile.phoneEnterNew
import com.example.ridepassenger2.ui.profile.phoneSubmitOtp
import com.example.ridepassenger2.ui.profile.phoneValidateNew
import com.example.ridepassenger2.ui.common.VerifyState
import com.example.ridepassenger2.ui.support.ConfirmScreen
import com.example.ridepassenger2.ui.support.MyRequestsScreen
import com.example.ridepassenger2.ui.support.RequestDetailScreen
import com.example.ridepassenger2.ui.support.RequestFormScreen
import com.example.ridepassenger2.ui.support.RequestFormState
import com.example.ridepassenger2.ui.support.SelfHelpScreen
import com.example.ridepassenger2.ui.support.SupportHomeScreen
import com.example.ridepassenger2.ui.support.TicketMsg
import com.example.ridepassenger2.ui.support.TicketStatus
import com.example.ridepassenger2.ui.support.Topic
import com.example.ridepassenger2.ui.support.TopicScreen
import com.example.ridepassenger2.ui.support.addAttachment
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme
import kotlinx.coroutines.launch

object FlowRoutes {
    // Payment
    const val PAY_METHODS = "flow_pay_methods"
    const val PAY_ADD = "flow_pay_add"
    const val PAY_HISTORY = "flow_pay_history"
    const val PAY_DETAIL = "flow_pay_detail/{id}"
    const val PAY_TRIP = "flow_pay_trip"
    // Profile
    const val EDIT_NAME = "flow_name"
    const val CHANGE_PHONE = "flow_phone"
    const val CHANGE_EMAIL = "flow_email"
    const val PHOTO = "flow_photo"
    const val SECURITY = "flow_security"
    const val DELETE = "flow_delete"
    const val VERIFY_ROWS = "flow_verify"
    // Support
    const val SUPPORT_HOME = "flow_support"
    const val SUPPORT_TOPIC = "flow_topic/{topic}"
    const val SUPPORT_SELFHELP = "flow_selfhelp"
    const val SUPPORT_FORM = "flow_form?trip={trip}"
    const val SUPPORT_CONFIRM = "flow_confirm/{id}"
    const val SUPPORT_MINE = "flow_mine"
    const val SUPPORT_DETAIL = "flow_ticket/{id}"
    fun payDetail(id: String) = "flow_pay_detail/$id"
    fun topic(t: Topic) = "flow_topic/${t.name}"
    fun ticket(id: String) = "flow_ticket/$id"
}

@Composable
fun PaymentFlowNav(nav: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val paymentLabel by AppPrefs.payment(context).collectAsState(initial = "Cash")
    val payMethodsState = remember(paymentLabel) {
        MethodsUiState.Content(
            listOf(
                PayMethod("cash", "cash", "Cash", "Pay the driver after your ride", true)
            )
        )
    }
    var mm by remember { mutableStateOf(MmSetupState()) }
    var trip by remember { mutableStateOf(PayTripState()) }
    var histFilter by remember { mutableStateOf(PayFilter.ALL) }
    NavHost(navController = nav, startDestination = FlowRoutes.PAY_METHODS) {
        composable(FlowRoutes.PAY_METHODS) {
            PayMethodsFlowScreen(payMethodsState, { nav.popBackStack() }, { nav.navigate(FlowRoutes.PAY_ADD) }, {}, {})
        }
        composable(FlowRoutes.PAY_ADD) {
            AddMethodScreen(mm, { mm = mmReduce(mm, it) }, { nav.popBackStack() }, { nav.popBackStack() })
        }
        composable(FlowRoutes.PAY_HISTORY) {
            PayHistoryScreen(emptyList(), histFilter, { histFilter = it }, { nav.navigate(FlowRoutes.payDetail(it)) }, { nav.popBackStack() })
        }
        composable(FlowRoutes.PAY_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            PayDetailScreen(null, { nav.popBackStack() }, { nav.navigate("${FlowRoutes.SUPPORT_FORM}?trip=$it") })
        }
        composable(FlowRoutes.PAY_TRIP) {
            PayTripScreen(trip, "—", { trip = trip.copy(methodId = it) }, { trip = trip.copy(phase = PayTripPhase.WAITING) }, { trip = trip.copy(phase = PayTripPhase.WAITING) }, { trip = trip.copy(methodId = "cash", phase = PayTripPhase.PICK) }, { nav.popBackStack() })
        }
    }
}

@Composable
fun ProfileFlowNav(nav: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val session by SessionManager.observe(context).collectAsState(initial = SessionManager.Session())
    var name by remember(session.name) { mutableStateOf(NameEditState(session.name)) }
    var phone by remember { mutableStateOf(PhoneChangeState()) }
    var email by remember { mutableStateOf(EmailChangeState()) }
    var sessions by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val resp = com.example.ridepassenger2.data.remote.RidaApiClient.api.getSessions()
            val list = resp["sessions"] as? List<*> ?: emptyList<Any>()
            sessions = list.mapNotNull { row ->
                (row as? Map<*, *>)?.let { m ->
                    val device = m["device_name"] ?: m["user_agent"] ?: "Device"
                    val city = m["city"] ?: m["location"] ?: ""
                    "$device • $city"
                }
            }
        } catch (_: Exception) {
            sessions = emptyList()
        }
    }

    NavHost(navController = nav, startDestination = FlowRoutes.EDIT_NAME) {
        composable(FlowRoutes.EDIT_NAME) {
            EditNameScreen(name, { name = name.copy(name = it, dirty = true) }, {}, { nav.popBackStack() })
        }
        composable(FlowRoutes.CHANGE_PHONE) {
            ChangePhoneScreen(phone, { phone = phone.copy(password = it) }, { phone = phone.copy(step = PhoneStep.ENTER_NEW) },
                { phone = phoneEnterNew(phone, it) }, { phone = phoneValidateNew(phone) },
                { phone = phone.copy(otp = phone.otp.copy(code = it)) }, { phone = phoneSubmitOtp(phone, phone.otp.code) }, { phone = phone.copy(otp = phone.otp.copy(resendInSec = 30)) }, { nav.popBackStack() })
        }
        composable(FlowRoutes.CHANGE_EMAIL) {
            ChangeEmailScreen(email, { email = email.copy(password = it) }, { email = email.copy(step = EmailStep.ENTER_NEW) },
                { email = email.copy(newEmail = it) }, { email = emailValidate(email) }, {}, {}, { nav.popBackStack() })
        }
        composable(FlowRoutes.PHOTO) { PhotoChangeScreen(null, null, {}, {}, { nav.popBackStack() }) }
        composable(FlowRoutes.SECURITY) {
            var pw by remember { mutableStateOf("") }; var cf by remember { mutableStateOf("") }
            SecurityScreen(pw, cf, { pw = it }, { cf = it }, sessions, {}, {}, {}, { nav.popBackStack() })
        }
        composable(FlowRoutes.DELETE) {
            var step by remember { mutableStateOf(0) }
            DeleteAccountScreen(step, { step = 1 }, {}, { nav.popBackStack() })
        }
        composable(FlowRoutes.VERIFY_ROWS) {
            Column(Modifier.padding(16.dp)) { ProfileVerifyRows(VerifyState.VERIFIED, VerifyState.PENDING, { nav.navigate(FlowRoutes.CHANGE_PHONE) }, { nav.navigate(FlowRoutes.CHANGE_EMAIL) }) }
        }
    }
}

@Composable
fun SupportFlowNav(nav: NavHostController = rememberNavController()) {
    var form by remember { mutableStateOf(RequestFormState()) }
    var filter by remember { mutableStateOf<TicketStatus?>(null) }
    val tickets by SupportRepository.tickets.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { SupportRepository.refresh() }

    NavHost(navController = nav, startDestination = FlowRoutes.SUPPORT_HOME) {
        composable(FlowRoutes.SUPPORT_HOME) {
            SupportHomeScreen("", {}, tickets, { nav.navigate(FlowRoutes.SUPPORT_FORM) }, { nav.navigate(FlowRoutes.topic(it)) }, { nav.navigate(FlowRoutes.ticket(it)) }, {}, { nav.popBackStack() })
        }
        composable(FlowRoutes.SUPPORT_TOPIC, arguments = listOf(navArgument("topic") { type = NavType.StringType })) { e ->
            val t = runCatching { Topic.valueOf(e.arguments?.getString("topic") ?: "OTHER") }.getOrDefault(Topic.OTHER)
            TopicScreen(t, { nav.navigate(FlowRoutes.SUPPORT_SELFHELP) }, { nav.popBackStack() })
        }
        composable(FlowRoutes.SUPPORT_SELFHELP) {
            SelfHelpScreen("Check your connection and try again. If the issue continues, submit a support request.", {}, { nav.navigate(FlowRoutes.SUPPORT_FORM) }, { nav.popBackStack() })
        }
        composable(FlowRoutes.SUPPORT_FORM, arguments = listOf(navArgument("trip") { type = NavType.StringType; nullable = true; defaultValue = null })) {
            RequestFormScreen(form, { form = form.copy(subject = it, draftSaved = true) }, { form = form.copy(description = it, draftSaved = true) },
                { form = addAttachment(form, "picha${form.attachments.size + 1}.jpg") }, { r -> form = form.copy(attachments = form.attachments - r) },
                { form = form.copy(contact = it) }, {
                    form = form.copy(submitting = true)
                    SupportRepository.createTicket(
                        subject = form.subject,
                        description = form.description,
                        category = "ride",
                        onSuccess = { id ->
                            form = form.copy(submitting = false)
                            nav.navigate(FlowRoutes.SUPPORT_CONFIRM.replace("{id}", id))
                        },
                        onError = { msg ->
                            form = form.copy(submitting = false, error = msg)
                        }
                    )
                }, { nav.popBackStack() })
        }
        composable(FlowRoutes.SUPPORT_CONFIRM, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            val id = e.arguments?.getString("id") ?: ""
            ConfirmScreen(id, "Your request was submitted.", { nav.navigate(FlowRoutes.ticket(id)) }, { nav.popBackStack() }, { nav.popBackStack() })
        }
        composable(FlowRoutes.SUPPORT_MINE) {
            MyRequestsScreen(tickets, filter, { filter = it }, { nav.navigate(FlowRoutes.ticket(it)) }, { nav.popBackStack() })
        }
        composable(FlowRoutes.SUPPORT_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            val id = e.arguments?.getString("id")
            val tk = tickets.firstOrNull { it.id == id }
            var reply by remember { mutableStateOf("") }
            var rating by remember { mutableStateOf(0) }
            var thread by remember { mutableStateOf<List<TicketMsg>>(emptyList()) }
            LaunchedEffect(id) {
                thread = if (id != null) SupportRepository.loadMessages(id) else emptyList()
            }
            RequestDetailScreen(tk, thread, reply, { reply = it }, {
                if (id != null && reply.isNotBlank()) {
                    SupportRepository.sendMessage(id, reply,
                        onDone = {
                            reply = ""
                            scope.launch { thread = SupportRepository.loadMessages(id) }
                        },
                        onError = { }
                    )
                }
            }, {}, rating, { rating = it }, { nav.popBackStack() })
        }
    }
}

/** Preview gallery + debug flow runner entry (preview-only click-through of every step). */
@Composable
fun FlowGallery() {
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Flow gallery — Payment", fontWeight = FontWeight.Bold)
        PayMethodsFlowScreen(MethodsUiState.Content(FakePayments.methods()), {}, {}, {}, {})
        Spacer(Modifier.height(16.dp))
        AddMethodScreen(MmSetupState(phase = MmVerifyPhase.WAITING, resendInSec = 9), {}, {}, {})
        Spacer(Modifier.height(16.dp))
        PayHistoryScreen(FakePayments.history(), PayFilter.ALL, {}, {}, {})
        Spacer(Modifier.height(16.dp)); Text("Flow gallery — Profile", fontWeight = FontWeight.Bold)
        EditNameScreen(NameEditState("Juma K."), {}, {}, {})
        Spacer(Modifier.height(16.dp))
        ChangePhoneScreen(PhoneChangeState(step = PhoneStep.OTP, otp = OtpState(code = "123", attemptsLeft = 3)), {}, {}, {}, {}, {}, {}, {}, {})
        Spacer(Modifier.height(16.dp)); Text("Flow gallery — Support", fontWeight = FontWeight.Bold)
        SupportHomeScreen("", {}, FakeSupport.tickets(), {}, {}, {}, {}, {})
        Spacer(Modifier.height(16.dp))
        RequestFormScreen(RequestFormState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable fun PrevGallery() { RIDEPASSENGER2Theme { FlowGallery() } }
