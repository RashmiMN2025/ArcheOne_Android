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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("TrackTicketsActivity", "Activity created")
        
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
        
        // Check if user data is ready and load tickets
        val userDataManager = UserDataManager.getInstance(this)
        if (userDataManager.isUserDataReady()) {
            Log.d("TrackTicketsActivity", "User data is ready, loading tickets immediately")
            helpDeskController.loadTicketsData(ticketCategory)
        } else {
            Log.d("TrackTicketsActivity", "User data not ready, tickets will load when available")
            // Tickets will load when user navigates or when data becomes available
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
        Log.d("TrackTicketsActivity", "Activity destroyed")
    }
}