package com.archeGlobal.one

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.archeGlobal.one.controller.HomeController
import com.archeGlobal.one.model.EventResponse
import com.archeGlobal.one.model.GreetingSubcategory
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.components.EventPopup
import com.archeGlobal.one.ui.theme.XOneTheme
import java.util.Calendar

/**
 * Test activity to verify the Pride Month feature works correctly.
 * This allows testing both June and non-June behavior without waiting for the actual month to change.
 */
class PrideMonthTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XOneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrideMonthTestScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrideMonthTestScreen() {
    // Mock data for testing
    val mockNavigator = remember {
        object : Navigator {
            override fun openPulseLogin() {}
            override fun navigateToLoginScreen() {}
            override fun navigateToOtpVerification(email: String, mobile: String, employeeId: String, stayLoggedIn: Boolean) {}
            override fun navigateToHome(
                fromOtp: Boolean,
                showBiometricSetup: Boolean,
                email: String,
                mobile: String,
                employeeId: String,
                stayLoggedIn: Boolean
            ) {}
            override fun getHomeIntent(): android.content.Intent = android.content.Intent()
            override fun navigateToID() {}
            override fun navigateToAsset() {}
            override fun navigateToDeskCart() {}
            override fun navigateToTimesheet() {}
            override fun navigateToLeave() {}
            override fun navigateToMyDocuments() {}
            override fun navigateToUserDocuments() {}
            override fun navigateToMyCareer() {}
            override fun navigateToELearning() {}
            override fun navigateToGoalSetting() {}
            override fun navigateToXCard() {}
            override fun navigateToMedical() {}
            override fun navigateToFinance() {}
            override fun navigateToMyPay() {}
            override fun navigateToAdmin() {}
            override fun navigateToHR() {}
            override fun navigateToHolidayOptions() {}
            override fun navigateToMpinSetup(email: String, mobile: String, employeeId: String, token: String) {}
            override fun showUpdateDialog() {}

            // Methods for services with dynamic URLs
            override fun navigateToSAPWithUrl(url: String) {}
            override fun navigateToAmpleWithUrl(url: String) {}
            override fun navigateToZingHRWithUrl(url: String) {}
            override fun navigateToMyPayWithUrl(url: String) {}
            override fun navigateToMedicalWithUrl(url: String) {}
            override fun navigateToAboutUsWithUrl(url: String) {}
            override fun openPulseLoginWithUrl(url: String) {}
            override fun navigateToHolidayCalendar() {}
            override fun navigateToClientCalendar() {}
            override fun navigateToGreetings() {}
            override fun navigateToGlobalCelebration() {}
            override fun navigateToXConnect(initialTab: String) {}
            override fun navigateToLocations(showHeader: Boolean) {}
            override fun navigateToHelpdesk() {}
            override fun navigateToTrackTickets(category: String) {}
            override fun navigateToAnnouncements() {}
            override fun navigateToXProfile() {}
            override fun navigateToPasswordReset() {}
            override fun navigateToPolicy() {}
            override fun navigateToSOS(showHeader: Boolean) {}
            override fun navigateToTravel() {}
            override fun navigateToTravelHistory() {}
            override fun navigateToTravelExpenses() {}
            override fun navigateToTravelRequestDetail() {}
            override fun navigateToTravelHistoryDetail() {}
            override fun navigateToTravelApprovals() {}
            override fun navigateToTravelApprovalDetail() {}
            override fun navigateToTravelApprovalDetails() {}
            override fun navigateToTravelApprovalConfirm() {}
            override fun navigateToTravelApprove() {}
            override fun navigateToTravelReject() {}
            override fun navigateToSAP() {}
            override fun navigateToAmple() {}
            override fun navigateToZingHR() {}
            override fun navigateToChat() {}
            override fun navigateToBusinessCard() {}
            override fun navigateToProfile() {}
            override fun navigateToAboutMe() {}
            override fun navigateToAddressDetails() {}
            override fun navigateToEmergencyContact() {}
            override fun navigateToPDFViewer(pdfUrl: String, title: String) {}
            override fun navigateToArcheOdyssey() {}
            override fun navigateToCommunique() {}
            override fun navigateToVision() {}
            override fun navigateToCoreValues() {}
            override fun navigateToAboutUs() {}
            override fun navigateToTodo() {}
            override fun navigateToIdeaVault() {}
            override fun navigateToAdminDashboard() {}
            override fun navigateToInventory() {}
            override fun navigateToOrderReceived() {}
            override fun navigateToOrderDetails(orderId: String) {}
            override fun navigateToOrderHistoryDetail(orderId: String) {}
            override fun navigateToOrderHistory() {}
            override fun navigateToConsumptionReport() {}
            override fun getCurrentRoute(): String? = null
            override fun refreshCurrentScreen() {}
            override fun popBackStack() {}
            override fun navigateToGreetingDetail(
                selectedGreetingUrl: String,
                allGreetings: List<String>,
                message: String,
                category: String
            ) {}
            override fun navigateToGlobalCelebrationDetail(subcategory: com.archeGlobal.one.model.GreetingSubcategory) {}
            override fun navigateToSmartCollateral() {}
        }
    }
    val mockEventResponse = remember {
        EventResponse(
            title = "Test Event",
            date = "2025-06-19",
            description = "This is a test event to verify regular event popup functionality",
            image = null
        )
    }

    // State for controlling the current test month (5 = June, since Calendar months are 0-indexed)
    var currentMonth by remember { mutableStateOf(Calendar.JUNE) }
    var showRegularEvent by remember { mutableStateOf(false) }
    var isPrideMonth by remember { mutableStateOf(currentMonth == Calendar.JUNE) }
    var showPrideDialog by remember { mutableStateOf(false) }
    var usingPrideIcon by remember { mutableStateOf(false) }

    // Create a test controller
    val context = LocalContext.current
    val controller = remember { HomeController(mockNavigator, context) }

    // Main test UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pride Month Feature Test",
            style = MaterialTheme.typography.headlineMedium
        )

        Divider()

        // Current month display
        Text(
            text = "Current Test Month: ${if (currentMonth == Calendar.JUNE) "June (Pride Month)" else "Not June"}"
        )
        Text(
            text = "Pride Month Active: ${if (isPrideMonth) "Yes" else "No"}"
        )

        // Month toggle button
        Button(
            onClick = {
                currentMonth = if (currentMonth == Calendar.JUNE) Calendar.JULY else Calendar.JUNE
                isPrideMonth = currentMonth == Calendar.JUNE
                showPrideDialog = false
            }
        ) {
            Text("Toggle Month (June/Not June)")
        }

        Divider()

        // Pride Month Dialog controls
        Button(
            onClick = { showPrideDialog = true },
            enabled = isPrideMonth
        ) {
            Text("Show Pride Month Dialog")
        }

        // Regular Event Popup controls
        Button(
            onClick = { showRegularEvent = true },
            enabled = !isPrideMonth
        ) {
            Text("Show Regular Event Popup")
        }

        Divider()

        // Status information
        Text(
            text = "Pride Icon Active: ${if (usingPrideIcon) "Yes" else "No"}"
        )
    }

    // Show Pride Month Dialog if needed
    if (isPrideMonth && showPrideDialog) {
        com.archeGlobal.one.ui.components.PrideMonthDialog(
            isUsingPrideIcon = usingPrideIcon,
            onDismiss = { showPrideDialog = false },
            onToggleIcon = {
                usingPrideIcon = !usingPrideIcon
                Toast.makeText(
                    context,
                    if (usingPrideIcon) "Changed to Pride icon" else "Changed to regular icon",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Show regular event popup if needed
    if (!isPrideMonth && showRegularEvent) {
        EventPopup(
            event = mockEventResponse,
            onDismiss = { showRegularEvent = false }
        )
    }
}

// Using the actual PrideMonthDialog component from ui.components package instead
