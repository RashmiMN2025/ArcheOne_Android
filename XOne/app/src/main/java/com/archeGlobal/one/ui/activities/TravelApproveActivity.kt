package com.archeGlobal.one.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.ui.screens.TravelApproveScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.google.gson.Gson

class TravelApproveActivity : ComponentActivity() {
    companion object {
        // Shared controller instance passed from HomeActivity
        var sharedTravelController: TravelController? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use shared controller instance from HomeActivity
        val travelController = sharedTravelController
        if (travelController == null) {
            android.util.Log.e("TravelApproveActivity", "Shared controller not available")
            finish()
            return
        }

        // Get travel request from intent
        val travelRequestJson = intent.getStringExtra("travel_request")
        val travelRequest =
            if (travelRequestJson != null) {
                try {
                    Gson().fromJson(travelRequestJson, TravelRequest::class.java)
                } catch (e: Exception) {
                    android.util.Log.e("TravelApproveActivity", "Error parsing travel request: ${e.message}")
                    null
                }
            } else {
                null
            }

        travelRequest?.let {
            // Register this request in the controller so approveTravelRequest can locate it
            travelController.selectTravelRequest(it)
        }

        setContent {
            XOneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    if (travelRequest != null) {
                        // Show the approval screen with the travel request
                        // We pass the travel request directly to the screen instead of using selectedTravelRequest
                        TravelApproveScreen(
                            controller = travelController,
                            travelRequest = travelRequest,
                        )
                    } else {
                        // Show error state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                        ) {
                            Text("Error: Travel request data not found")
                        }
                    }
                }
            }
        }
    }
}
