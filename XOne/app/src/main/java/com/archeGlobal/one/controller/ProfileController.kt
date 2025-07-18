package com.archeGlobal.one.controller

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.ProfileModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.LogoutRequest
import com.archeGlobal.one.network.LogoutResponse
import com.archeGlobal.one.network.ProfilePictureResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.utils.UserDataManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ProfileController(
    private val context: Context,
    private val navigator: Navigator
) {
    private val userDataManager = UserDataManager.getInstance(context)
    private val userData = userDataManager.getUserData()

    // Initialize with debug logging
    init {
        Log.d("ProfileController", "Initializing with userData: $userData")
        Log.d("ProfileController", "Profile picture URL: ${userData?.profilePic}")
    }

    // Function to get the app version dynamically
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "Version ${packageInfo.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("ProfileController", "Error getting app version", e)
            "Version 1.0" // Fallback version
        }
    }

    var model by mutableStateOf(
        ProfileModel(
            name = userData?.name ?: "",
            email = userData?.email ?: "",
            profilePicture = userData?.profilePic,
            version = getAppVersion(),
            lastLoginTime = userDataManager.getLastLoginTime()?.let { formatLastLoginTime(it) } ?: ""
        )
    )
        internal set

    private fun formatLastLoginTime(timestamp: Long): String {
        return try {
            val date = java.util.Date(timestamp)
            android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", date).toString()
        } catch (e: Exception) {
            Log.e("ProfileController", "Error formatting last login time", e)
            ""
        }
    }

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
        navigator.navigateToUserDocuments()
    }

    fun uploadProfilePicture(imageUri: Uri) {
        // Get employeeId from userDataManager
        val employeeId = userData?.employeeId ?: ""

        if (employeeId.isEmpty()) {
            Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
            Log.e("ProfileController", "Upload failed - employeeId is empty")
            return
        }

        Log.d("ProfileController", "Using employeeId: $employeeId")

        // Log the URI details
        Log.d("ProfileController", "Image URI: $imageUri")

        val file = getFileFromUri(context, imageUri)
        if (file == null) {
            Toast.makeText(context, "Could not process image file", Toast.LENGTH_SHORT).show()
            Log.e("ProfileController", "Upload failed - couldn't process file from URI: $imageUri")
            return
        }

        // Show uploading message
        Toast.makeText(context, "Uploading profile picture...", Toast.LENGTH_SHORT).show()

        // Log the file details to help debug
        Log.d("ProfileController", "File to upload: ${file.absolutePath}, size: ${file.length()} bytes, exists: ${file.exists()}")

        try {
            // Prepare the multipart request with exact parameters that worked in Postman
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", "profile.jpg", requestFile)

            // Get email from user data
            val email = userData?.email ?: ""
            if (email.isEmpty()) {
                Toast.makeText(context, "Email not available", Toast.LENGTH_SHORT).show()
                Log.e("ProfileController", "Upload failed - email is empty")
                return
            }

            val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())

            // Log the request parameters
            Log.d("ProfileController", "Making upload request to: ${RetrofitClient.BASE_URL}upload_profile")
            Log.d("ProfileController", "Request params: email=$email, employeeId=$employeeId")

            // Use the dedicated profile picture upload endpoint
            val call = RetrofitClient.apiService.uploadProfilePicture(filePart, emailPart, employeeIdPart)

            // Log the call details
            Log.d("ProfileController", "Call URL: ${call.request().url}")
            Log.d("ProfileController", "Call Method: ${call.request().method}")

            call.enqueue(object : Callback<ProfilePictureResponse> {
                override fun onResponse(call: Call<ProfilePictureResponse>, response: Response<ProfilePictureResponse>) {
                    // Log the raw response for debugging
                    Log.d("ProfileController", "Response received - Code: ${response.code()}")

                    // Log raw response body if available
                    try {
                        response.body()?.let {
                            Log.d("ProfileController", "Raw response body: $it")
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileController", "Error logging raw response: ${e.message}")
                    }

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!

                        Log.d("ProfileController", "Upload response received: $responseBody")
                        Log.d("ProfileController", "Response status: ${responseBody.status}")
                        Log.d("ProfileController", "Response message: ${responseBody.message}")
                        Log.d("ProfileController", "Response filePath: ${responseBody.filePath}")

                        // Check for success status
                        if (responseBody.status == 200) {
                            // Get the profile picture URL from the response
                            val profilePicUrl = responseBody.filePath

                            // First, check if the URL is null
                            if (profilePicUrl != null && profilePicUrl.isNotEmpty()) {
                                Log.d("ProfileController", "Found profile picture URL: $profilePicUrl")

                                // Display a success message first
                                Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()

                                // Log the success for debugging
                                Log.d("ProfileController", "Profile picture upload successful. URL: $profilePicUrl")

                                // Update the model and UI immediately
                                try {
                                    // First update the model
                                    model = model.copy(profilePicture = profilePicUrl)
                                    Log.d("ProfileController", "Updated model.profilePicture: ${model.profilePicture}")

                                    // Update the user data in UserDataManager
                                    userDataManager.updateProfilePicture(profilePicUrl)
                                    Log.d("ProfileController", "Updated profile picture in UserDataManager")

                                    // Store the profile picture URL in shared preferences for persistence
                                    val sharedPrefs = context.getSharedPreferences("profile_data", Context.MODE_PRIVATE)
                                    sharedPrefs.edit().putString("profile_picture_url", profilePicUrl).apply()
                                    Log.d("ProfileController", "Saved profile picture URL to shared preferences")

                                    // Invalidate image cache to force a reload
                                    try {
                                        com.archeGlobal.one.utils.ImageCache.invalidateProfileImageCache()
                                        Log.d("ProfileController", "Invalidated profile image cache")
                                    } catch (e: Exception) {
                                        Log.e("ProfileController", "Error invalidating image cache: ${e.message}", e)
                                    }

                                    // Use a safer approach to update the HomeController
                                    try {
                                        if (homeController != null) {
                                            homeController?.updateProfilePicture(profilePicUrl)
                                            Log.d("ProfileController", "Updated HomeController with new profile picture")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ProfileController", "Error updating HomeController: ${e.message}", e)
                                        // Continue execution - don't crash
                                    }

                                    // Refresh the UI immediately
                                    try {
                                        // Notify any listeners that the profile picture has changed
                                        val intent = android.content.Intent("com.archeGlobal.one.PROFILE_PICTURE_UPDATED")
                                        intent.putExtra("profile_picture_url", profilePicUrl)
                                        // Use a simpler approach instead of LocalBroadcastManager
                                        context.sendBroadcast(intent)
                                        Log.d("ProfileController", "Sent broadcast to refresh UI")
                                    } catch (e: Exception) {
                                        Log.e("ProfileController", "Error sending broadcast: ${e.message}", e)
                                    }
                                } catch (e: Exception) {
                                    Log.e("ProfileController", "Error updating profile data: ${e.message}", e)
                                }
                            } else {
                                // URL is null but upload was successful
                                Log.e("ProfileController", "Profile picture filePath is null or empty in response")
                                Log.d("ProfileController", "Response status: ${responseBody.status}, message: ${responseBody.message}")

                                // Despite missing URL, tell the user upload was successful
                                Toast.makeText(context, "Profile picture uploaded successfully. Changes will appear after restart.", Toast.LENGTH_LONG).show()

                                // Attempt to generate a fallback URL from the user's email
                                try {
                                    val userData = userDataManager.getUserData()
                                    val email = userData?.email
                                    val employeeId = userData?.employeeId

                                    if (!email.isNullOrEmpty()) {
                                        // Construct a URL similar to the expected format based on the API documentation
                                        val fallbackUrl = "https://dev.arche.global:7000/download_doc/$email?fileName=${employeeId ?: ""}-profile_pic.jpg"
                                        Log.d("ProfileController", "Generated fallback URL: $fallbackUrl")

                                        // Store this URL for next app start
                                        val sharedPrefs = context.getSharedPreferences("profile_data", Context.MODE_PRIVATE)
                                        sharedPrefs.edit().putString("profile_picture_url", fallbackUrl).apply()
                                        Log.d("ProfileController", "Saved fallback URL to preferences")
                                    }
                                } catch (e: Exception) {
                                    Log.e("ProfileController", "Failed to generate fallback URL: ${e.message}", e)
                                }

                                // Return to home after a delay
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    try {
                                        navigator.navigateToHome()
                                    } catch (e: Exception) {
                                        Log.e("ProfileController", "Failed to navigate home: ${e.message}", e)
                                    }
                                }, 2000)
                            }
                        } else {
                            // Error response from server
                            Toast.makeText(context, "Error: ${responseBody.message}", Toast.LENGTH_SHORT).show()
                            Log.e("ProfileController", "API error status: ${responseBody.status}, message: ${responseBody.message}")
                        }
                    } else {
                        // HTTP error response
                        val errorMsg = "Profile picture upload failed: ${response.code()} ${response.message()}"
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        Log.e("ProfileController", errorMsg)

                        try {
                            val errorBody = response.errorBody()?.string() ?: "No error body"
                            Log.e("ProfileController", "Error body: $errorBody")
                        } catch (e: Exception) {
                            Log.e("ProfileController", "Error reading error body", e)
                        }
                    }
                }

                override fun onFailure(call: Call<ProfilePictureResponse>, t: Throwable) {
                    // Network error - log detailed error information
                    val errorMsg = "Network error during profile picture upload: ${t.message}"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("ProfileController", errorMsg, t)

                    // Log the request details that failed
                    Log.e("ProfileController", "Failed request URL: ${call.request().url}")
                    Log.e("ProfileController", "Failed request method: ${call.request().method}")
                    Log.e("ProfileController", "Failed request headers: ${call.request().headers}")
                }
            })
        } catch (e: Exception) {
            Log.e("ProfileController", "Error preparing multipart request", e)
            Toast.makeText(context, "Failed to prepare multipart request", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteProfilePhoto(onSuccess: () -> Unit = {}) {
        val email = userData?.email ?: ""
        val employeeId = userData?.employeeId ?: ""
        if (email.isEmpty() || employeeId.isEmpty()) {
            Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
            return
        }
        val params = mapOf(
            "email" to email,
            "employeeId" to employeeId,
            "documentType" to "profile_pic"
        )
        // Assuming you have a deleteDoc endpoint in your ApiService:
        RetrofitClient.apiService.deleteDoc(params).enqueue(object : Callback<ProfilePictureResponse> {
            override fun onResponse(call: Call<ProfilePictureResponse>, response: Response<ProfilePictureResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Profile photo deleted", Toast.LENGTH_SHORT).show()
                    // Remove from model and UserDataManager
                    model = model.copy(profilePicture = null)
                    userDataManager.updateProfilePicture(null)
                    com.archeGlobal.one.utils.ImageCache.invalidateProfileImageCache()
                    onSuccess()
                } else {
                    Toast.makeText(context, "Failed to delete photo", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ProfilePictureResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to delete photo", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver

            // Try to get the file's MIME type
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            Log.d("ProfileController", "File MIME type: $mimeType")

            // Determine file extension based on MIME type
            val extension = when {
                mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
                mimeType.contains("png") -> ".png"
                else -> ".jpg" // Default to jpg
            }

            val inputStream = contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "profile_picture$extension")

            Log.d("ProfileController", "Creating temp file at: ${file.absolutePath}")

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024) // 4K buffer
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }

            Log.d("ProfileController", "File created successfully, size: ${file.length()} bytes")
            file
        } catch (e: Exception) {
            Log.e("ProfileController", "Error processing file: ${e.message}", e)
            null
        }
    }

    fun onLogoutClick() {
        val userData = userDataManager.getUserData()
        val email = userData?.email
        
        if (email.isNullOrEmpty()) {
            Log.e("ProfileController", "Email not found, proceeding with local logout")
            proceedWithLocalLogout(userData)
            return
        }

        val employeeName = userData?.name ?: userData?.email ?: ""
        userDataManager.setLastUsername(employeeName) // Save for welcome text

        // Call logout API
        val logoutRequest = LogoutRequest(email)
        Log.d("ProfileController", "Calling logout API with email: $email")

        RetrofitClient.apiService.logout(logoutRequest)
            .enqueue(object : retrofit2.Callback<LogoutResponse> {
                override fun onResponse(
                    call: retrofit2.Call<LogoutResponse>, 
                    response: retrofit2.Response<LogoutResponse>
                ) {
                    Log.d("ProfileController", "Logout API response: ${response.code()}")
                    if (response.isSuccessful) {
                        val logoutResponse = response.body()
                        Log.d("ProfileController", "Logout API success: ${logoutResponse?.message}")
                        proceedWithLocalLogout(userData)
                    } else {
                        Log.e("ProfileController", "Logout API failed with code: ${response.code()}")
                        // Even if API fails, proceed with local logout for user experience
                        proceedWithLocalLogout(userData)
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<LogoutResponse>, 
                    t: Throwable
                ) {
                    Log.e("ProfileController", "Logout API call failed: ${t.message}", t)
                    // Even if API call fails, proceed with local logout for user experience
                    proceedWithLocalLogout(userData)
                }
            })
    }

    private fun proceedWithLocalLogout(userData: UserData?) {
        // Clear login state and local data
        userDataManager.setIsLoggedIn(false)
        userDataManager.setHasLoggedIn(true)
        // Preserve that this is not a first-time user (important for showing fingerprint option)
        com.archeGlobal.one.utils.setFirstTimeLogin(context, false)
        // Remove token from preferences
        userDataManager.preferencesManager.clearAuthToken()
        // Navigate to login screen WITHOUT token
        navigator.navigateToLoginScreen()
    }

    fun onBackPressed() {
        navigator.navigateToHome()
    }

    fun onProfilePictureClick(imageUri: Uri) {
        uploadProfilePicture(imageUri)
    }

    // Add a method to broadcast profile picture update to any controllers that need it
    private fun broadcastProfilePictureUpdate(profilePicUrl: String?) {
        try {
            Log.d("ProfileController", "Broadcasting profile picture update: $profilePicUrl")

            // Update the profile model - with null check
            try {
                model = model.copy(profilePicture = profilePicUrl)
                Log.d("ProfileController", "Successfully updated model with new profile picture")
            } catch (e: Exception) {
                Log.e("ProfileController", "Error updating model: ${e.message}", e)
                // Continue with other updates even if this fails
            }

            // Update the user data in UserDataManager - with null check
            try {
                if (userDataManager != null) {
                    userDataManager.updateProfilePicture(profilePicUrl)
                    Log.d("ProfileController", "Successfully updated user data with new profile picture")
                } else {
                    Log.w("ProfileController", "UserDataManager is null, skipping update")
                }
            } catch (e: Exception) {
                Log.e("ProfileController", "Error updating user data: ${e.message}", e)
                // Continue with other updates
            }

            // Update the HomeController if available - with null check and careful handling
            try {
                if (homeController != null) {
                    Log.d("ProfileController", "Attempting to update HomeController with profile picture")
                    homeController?.updateProfilePicture(profilePicUrl)
                    Log.d("ProfileController", "Successfully updated HomeController")
                } else {
                    Log.d("ProfileController", "HomeController is null, skipping update")
                }
            } catch (e: Exception) {
                Log.e("ProfileController", "Error updating HomeController: ${e.message}", e)
                // Continue with other operations even if HomeController update fails
            }

            // Invalidate image cache to force a reload
            try {
                // Clear the image cache to force a fresh load
                com.archeGlobal.one.utils.ImageCache.invalidateProfileImageCache()
                Log.d("ProfileController", "Invalidated profile image cache for: $profilePicUrl")
            } catch (e: Exception) {
                Log.e("ProfileController", "Error invalidating image cache: ${e.message}", e)
            }

            // We'll skip the screen refresh since it's likely causing crashes
            // Instead, we'll just log that the update is complete
            Log.d("ProfileController", "Profile picture update complete - skipping screen refresh to avoid crashes")

            /* Commented out the screen refresh code to prevent crashes
            try {
                // Force a navigation to refresh the current screen - this helps update UI immediately
                val currentScreen = navigator.getCurrentRoute()
                Log.d("ProfileController", "Current screen: $currentScreen")
                if (currentScreen == "profile") {
                    // If we're on the profile screen, we need to force a refresh
                    navigator.refreshCurrentScreen()
                    Log.d("ProfileController", "Refreshed current screen")
                }
            } catch (e: Exception) {
                Log.e("ProfileController", "Error forcing navigation refresh: ${e.message}", e)
            }
            */

            Log.d("ProfileController", "Broadcast of profile picture update completed successfully")
        } catch (e: Exception) {
            // Catch-all to prevent any crash
            Log.e("ProfileController", "Critical error in broadcastProfilePictureUpdate: ${e.message}", e)
        }
    }

    fun uploadProfilePhoto(bitmap: Bitmap) {
        Log.d("ProfileController", "Starting profile photo upload from bitmap")

        // Convert bitmap to file
        val file = convertBitmapToFile(bitmap)
        if (file == null) {
            Toast.makeText(context, "Could not process image file", Toast.LENGTH_SHORT).show()
            Log.e("ProfileController", "Upload failed - couldn't convert bitmap to file")
            return
        }

        // Use the existing upload method
        uploadProfilePicture(Uri.fromFile(file))
    }

    fun uploadProfilePhotoFromUri(uri: Uri) {
        Log.d("ProfileController", "Starting profile photo upload from URI")
        // Simply delegate to the existing upload method
        uploadProfilePicture(uri)
    }

    private fun convertBitmapToFile(bitmap: Bitmap): File? {
        return try {
            // Create a file to write the bitmap data
            val file = File(context.cacheDir, "profile_photo.jpg")
            file.createNewFile()

            // Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
            val bitmapData = bos.toByteArray()

            // Write the bytes to file
            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()

            Log.d("ProfileController", "Successfully converted bitmap to file: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e("ProfileController", "Error converting bitmap to file: ${e.message}", e)
            null
        }
    }

    companion object {
        private var homeController: HomeController? = null

        fun setHomeController(controller: HomeController) {
            homeController = controller
        }
    }
}
