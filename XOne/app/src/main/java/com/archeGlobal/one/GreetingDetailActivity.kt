package com.archeGlobal.one

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.GreetingDetailScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.ImageLoader
import coil.request.ImageRequest
import android.util.Log

class GreetingDetailActivity : ComponentActivity() {
    private lateinit var navigator: AndroidNavigator
    private var allGreetings by mutableStateOf<List<String>>(emptyList())
    private var editableMessage by mutableStateOf("")
    private var selectedGreetingUrl by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val message = intent.getStringExtra("message") ?: ""
        val category = intent.getStringExtra("category") ?: "Greeting"

        selectedGreetingUrl = imageUrl
        editableMessage = message

        navigator = AndroidNavigator(this)
        val userDataManager = UserDataManager.getInstance(this)
        val greetingsData = userDataManager.getGreetingsData()
        val categoryMessages = userDataManager.getGreetingCategoriesData()

        allGreetings = greetingsData?.get(category) ?: listOf(imageUrl)

        if (editableMessage.isEmpty()) {
            categoryMessages?.find { it.name == category }?.let { categoryData ->
                if (categoryData.message.isNotEmpty()) {
                    editableMessage = categoryData.message
                }
            }
        }

        setContent {
            XOneTheme {
                GreetingDetailScreen(
                    imageUrl = imageUrl,
                    categoryGreetings = allGreetings,
                    selectedGreetingUrl = selectedGreetingUrl,
                    message = editableMessage,
                    category = category,
                    onMessageChanged = { newMessage -> editableMessage = newMessage },
                    onGreetingSelected = { newGreetingUrl -> selectedGreetingUrl = newGreetingUrl },
                    onBackPressed = { finish() },
                    onSendGreeting = { sendGreeting(selectedGreetingUrl, editableMessage, category) },
                    onSendInOutlook = { greetingUrl, msg -> sendGreetingInOutlook(greetingUrl, msg, category) }
                )
            }
        }
    }

    private fun sendGreeting(imageUrl: String, message: String, category: String) {
        android.widget.Toast.makeText(this, "Preparing greeting to send...", android.widget.Toast.LENGTH_SHORT).show()
        shareLinkOnly(imageUrl, message, category)
    }

    private fun shareLinkOnly(imageUrl: String, message: String, category: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, category)
            val shareText = if (message.isNotEmpty()) {
                "$message\n\n$imageUrl"
            } else {
                imageUrl
            }
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Send Greeting"))
    }

    // --- Send via Outlook with image URL and small display in body ---
    private fun sendGreetingInOutlook(imageUrl: String, message: String, category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get user data for signature
                val userDataManager = UserDataManager.getInstance(this@GreetingDetailActivity)
                val userData = userDataManager.getUserData()
                val userName = userData?.name ?: "Your Name"
                val userDesignation = userData?.designation ?: "Your Designation"
                val userMobile = userData?.mobile ?: ""

                // Build HTML email with image URL (width 300px) and signature
                val htmlEmailContent = createHtmlEmailWithImageUrl(
                    message, imageUrl, userName, userDesignation, userMobile
                )

                withContext(Dispatchers.Main) {
                    val emailIntent = Intent(Intent.ACTION_SEND).apply {
                        setPackage("com.microsoft.office.outlook")
                        type = "text/html"
                        putExtra(Intent.EXTRA_SUBJECT, category)
                        putExtra(Intent.EXTRA_HTML_TEXT, htmlEmailContent)
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    try {
                        startActivity(emailIntent)
                    } catch (e: Exception) {
                        Log.e("GreetingDetailActivity", "Outlook HTML intent failed: ${e.message}")
                        shareLinkOnly(imageUrl, message, category)
                    }
                }
            } catch (e: Exception) {
                Log.e("GreetingDetailActivity", "Error in sendGreetingInOutlook: ${e.message}")
                withContext(Dispatchers.Main) {
                    shareLinkOnly(imageUrl, message, category)
                }
            }
        }
    }

    // Create HTML email with image URL (width 300px) and signature
    private fun createHtmlEmailWithImageUrl(
        message: String,
        imageUrl: String,
        userName: String,
        userDesignation: String,
        userMobile: String
    ): String {
        val sanitizedMessage = message.replace("\n", "<br>")
        return """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <p>$sanitizedMessage</p>
                <img src="$imageUrl" width="300" style="display:block; margin-top:10px;" />
                <p style="margin-top: 20px;">Best Regards,</p>
                <table style="margin-top: 10px;">
                    <tr>
                        <td>
                            <!-- Optionally add your icon here if it's a URL -->
                        </td>
                        <td style="padding-left: 8px;">
                            <strong>$userName</strong><br/>
                            $userDesignation<br/>
                            $userMobile
                        </td>
                    </tr>
                </table>
            </body>
            </html>
        """.trimIndent()
    }
}