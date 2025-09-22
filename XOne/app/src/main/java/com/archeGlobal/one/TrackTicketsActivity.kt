package com.archeGlobal.one

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.archeGlobal.one.controller.HelpDeskController
import com.archeGlobal.one.ui.screens.TicketTrackingScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager

class TrackTicketsActivity : AppCompatActivity() {

    private lateinit var helpDeskController: HelpDeskController
    private lateinit var userDataManager: UserDataManager
    private lateinit var userDataReadyCallback: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("TrackTicketsActivity", "Activity created")

        userDataManager = UserDataManager.getInstance(this)

        // Initialize HelpDeskController
        helpDeskController = HelpDeskController(this)

        // Get the ticket category from intent (if provided)
        val ticketCategory = intent.getStringExtra("ticketCategory") ?: "Helpdesk"
        val source = intent.getStringExtra("source")

        Log.d("TrackTicketsActivity", "Loading tickets with category: $ticketCategory, source: $source")

        // Set navigation source if provided
        if (source != null) {
            helpDeskController.setNavigationSource(source)
        }

        // Register callback to load tickets when user data becomes ready (for fresh installs)
        userDataReadyCallback = {
            Log.d("TrackTicketsActivity", "User data ready callback triggered, loading tickets (connection should be warmed)")
            helpDeskController.loadTicketsData(ticketCategory)
        }
        userDataManager.addUserDataReadyCallback(userDataReadyCallback)

        // Also try to load tickets immediately if data is already ready
        if (userDataManager.isUserDataReady()) {
            Log.d("TrackTicketsActivity", "User data already ready, loading tickets immediately")
            helpDeskController.loadTicketsData(ticketCategory) // No delay needed if data is already ready (app restart case)
        }

        setContent {
            XOneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicketTrackingScreen(
                        controller = helpDeskController
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up callback to prevent memory leaks
        if (::userDataManager.isInitialized && ::userDataReadyCallback.isInitialized) {
            userDataManager.removeUserDataReadyCallback(userDataReadyCallback)
            Log.d("TrackTicketsActivity", "Cleaned up user data ready callback")
        }
        Log.d("TrackTicketsActivity", "Activity destroyed")
    }

    fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
}
