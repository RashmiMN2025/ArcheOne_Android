package com.archeGlobal.one

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.controller.LoginController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.ResponsiveLoginScreen
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.CustomToast

class LoginActivity : AppCompatActivity() {
    private var showUpdateDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val navigator = AndroidNavigator(this)
        val loginController = LoginController(this, navigator)

        // Check if we should force original login form
        val forceOriginalLogin = intent.getBooleanExtra("forceOriginalLogin", false)
        val forceDifferentUserMode = intent.getBooleanExtra("forceDifferentUserMode", false)
        val shouldShowUpdateDialog = intent.getBooleanExtra("showUpdateDialog", false)
        val sessionExpired = intent.getBooleanExtra("session_expired", false)
        val clearFields = intent.getBooleanExtra("clearFields", false)

        val forceOriginalLoginFinal = if (sessionExpired) false else forceOriginalLogin

        // Handle back press in login screen - exit app instead of going back
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Exit the app when back is pressed at login
                    finish()
                }
            },
        )

        // If launched with intent to show update dialog, show it immediately
        if (shouldShowUpdateDialog) {
            showUpdateDialog = true
        }

        // Show session expired message if needed
        if (sessionExpired) {
            CustomToast.show(this, "Session expired. Please log in again.", android.widget.Toast.LENGTH_LONG)
        }

        setContent {
            XOneTheme {
                ResponsiveLoginScreen(
                    controller = loginController,
                    navigator = navigator,
                    forceOriginalLogin = forceOriginalLoginFinal,
                    forceDifferentUserMode = forceDifferentUserMode,
                    clearFields = clearFields,
                )

                // Update Required Dialog
                if (showUpdateDialog) {
                    UpdateRequiredDialog(
                        onUpdateClick = {
                            // Open Play Store
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                            startActivity(intent)
                        },
                        onDismiss = { showUpdateDialog = false },
                    )
                }
            }
        }
    }

    fun showUpdateDialog() {
        showUpdateDialog = true
    }
}

@Composable
fun UpdateRequiredDialog(
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF6F4EE),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Download icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_download),
                    contentDescription = "Update Required",
                    tint = Color(0xFFDD3825),
                    modifier = Modifier.size(48.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Update Required",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "A new version of ArcheOne is available. You must update to continue using the app.",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Update button
                Button(
                    onClick = onUpdateClick,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            contentColor = Color.White,
                        ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Update Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                    )
                }
            }
        }
    }
}
