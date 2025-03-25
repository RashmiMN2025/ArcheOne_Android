package com.archeGlobal.one.controller

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.ProfileModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.LogoutRequest
import com.archeGlobal.one.network.LogoutResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class ProfileController(
    private val context: Context,
    private val navigator: Navigator
) {
    private val userDataManager = UserDataManager.getInstance(context)
    
    var model by mutableStateOf(
        ProfileModel(
            name = OtpVerificationController.getUserData()?.name ?: "",
            email = OtpVerificationController.getUserData()?.email ?: ""
        )
    )
        internal set

    fun onAboutMeClick() {
        // Navigate to About Me screen
        navigator.navigateToAboutMe()
    }

    fun onAddressClick() {
        // Navigate to Address screen
        navigator.navigateToAddressDetails()
    }

    fun onEmergencyContactClick() {
        // Navigate to Emergency Contact screen
        navigator.navigateToEmergencyContact()
    }

    fun onDocumentsClick() {
        navigator.navigateToMyDocuments()
    }

    fun onLogoutClick() {
        // Get the employeeId from the stored user data
        val employeeId = OtpVerificationController.getUserData()?.employeeId ?: ""
        
        if (employeeId.isEmpty()) {
            // If no employeeId is available, simply navigate to login screen
            Toast.makeText(context, "No user session found. Logging out...", Toast.LENGTH_SHORT).show()
            // Clear all user data
            userDataManager.clearUserData()
            navigator.navigateToLoginScreen()
            return
        }
        
        // Create the logout request
        val request = LogoutRequest(employeeId = employeeId)
        
        // Show a loading message
        Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show()
        
        // Make the API call
        RetrofitClient.apiService.logout(request).enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.status == 200) {
                        // Successful logout
                        Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()
                        
                        // Clear user data from central manager
                        userDataManager.clearUserData()
                    } else {
                        // Server returned non-200 status
                        Toast.makeText(context, "Logout failed: ${responseBody.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileController", "Logout failed with status: ${responseBody.status}, message: ${responseBody.message}")
                    }
                } else {
                    // HTTP error response
                    val errorMsg = "Logout failed: ${response.code()} ${response.message()}"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("ProfileController", errorMsg)
                }
                
                // Clear user data regardless of the response
                userDataManager.clearUserData()
                
                // Navigate to login screen regardless of the result
                // This ensures the user can log in again even if the logout API call fails
                navigator.navigateToLoginScreen()
            }
            
            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                // Network error
                val errorMsg = "Network error during logout: ${t.message}"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                Log.e("ProfileController", errorMsg, t)
                
                // Clear user data even on failure
                userDataManager.clearUserData()
                
                // Navigate to login screen anyway
                navigator.navigateToLoginScreen()
            }
        })
    }

    fun onBackPressed() {
        navigator.navigateToHome()
    }
} 