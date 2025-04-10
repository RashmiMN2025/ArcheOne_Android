package com.archeGlobal.one.controller

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.ProfileModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.DocumentUploadResponse
import com.archeGlobal.one.network.LogoutRequest
import com.archeGlobal.one.network.LogoutResponse
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

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
    
    var model by mutableStateOf(
        ProfileModel(
            name = userData?.name ?: "",
            email = userData?.email ?: "",
            profilePicture = userData?.profilePic
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
        navigator.navigateToUserDocuments()
    }

    fun uploadProfilePicture(imageUri: Uri) {
        Log.d("ProfileController", "Starting profile picture upload process")
        
        val employeeId = OtpVerificationController.getUserData()?.employeeId ?: ""
        
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
            
            // Prepare other request parts - these must exactly match what the server expects
            val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
            val documentTypePart = "profile_pic".toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Log the request parameters
            Log.d("ProfileController", "Making upload request to: ${RetrofitClient.BASE_URL}upload")
            Log.d("ProfileController", "Request params: employeeId=$employeeId, documentType=profile_pic")
            
            // Make the API call to upload the profile picture
            val call = RetrofitClient.apiService.uploadDocument(filePart, employeeIdPart, documentTypePart)
            
            // Log the call details
            Log.d("ProfileController", "Call URL: ${call.request().url}")
            Log.d("ProfileController", "Call Method: ${call.request().method}")
            
            call.enqueue(object : Callback<DocumentUploadResponse> {
                override fun onResponse(call: Call<DocumentUploadResponse>, response: Response<DocumentUploadResponse>) {
                    // Log the raw response for debugging
                    Log.d("ProfileController", "Response received - Code: ${response.code()}")
                    
                    try {
                        val rawResponse = response.raw().toString()
                        Log.d("ProfileController", "Raw response: $rawResponse")
                    } catch (e: Exception) {
                        Log.e("ProfileController", "Error logging raw response", e)
                    }
                    
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        
                        Log.d("ProfileController", "Upload response received: $responseBody")
                        
                        // Check for success status
                        if (responseBody.status == 200) {
                            // Handle the profile picture update in user data
                            val userData = userDataManager.getUserData()
                            if (userData != null) {
                                // Get the profile picture URL from the response
                                val profilePicUrl = responseBody.filePath
                                
                                Log.d("ProfileController", "Direct file path from response: $profilePicUrl")
                                
                                if (profilePicUrl != null && profilePicUrl.isNotEmpty()) {
                                    // Use central method to broadcast the update
                                    broadcastProfilePictureUpdate(profilePicUrl)
                                    
                                    Log.d("ProfileController", "Current model.profilePicture: ${model.profilePicture}")
                                    
                                    Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Fallback to the old way of getting filePath from the response
                                    // Find the profile picture URL from the response
                                    var filePathFromDocs: String? = null
                                    
                                    try {
                                        // Check both personal and professional docs for profile picture
                                        responseBody.personalDoc?.forEach { doc ->
                                            Log.d("ProfileController", "Checking personal doc: ${doc.docName}, path: ${doc.filePath}")
                                            if (doc.docName == "profile_pic" && !doc.filePath.isNullOrEmpty()) {
                                                filePathFromDocs = doc.filePath
                                                Log.d("ProfileController", "Found profile pic in personal docs: $filePathFromDocs")
                                            }
                                        }
                                        
                                        responseBody.professionalDoc?.forEach { doc ->
                                            Log.d("ProfileController", "Checking professional doc: ${doc.docName}, path: ${doc.filePath}")
                                            if (doc.docName == "profile_pic" && !doc.filePath.isNullOrEmpty()) {
                                                filePathFromDocs = doc.filePath
                                                Log.d("ProfileController", "Found profile pic in professional docs: $filePathFromDocs")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ProfileController", "Error parsing doc arrays: ${e.message}", e)
                                    }
                                    
                                    // Now handle the found file path
                                    filePathFromDocs?.let { path ->
                                        // Use central method to broadcast the update
                                        broadcastProfilePictureUpdate(path)
                                        
                                        Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                                    } ?: run {
                                        Toast.makeText(context, "Profile picture uploaded but URL not found in response", Toast.LENGTH_SHORT).show()
                                        Log.e("ProfileController", "Profile picture URL not found in response")
                                    }
                                }
                            }
                        } else {
                            // Error response from server
                            Toast.makeText(context, "Failed to update profile picture: ${responseBody.message}", Toast.LENGTH_SHORT).show()
                            Log.e("ProfileController", "Upload failed with status: ${responseBody.status}, message: ${responseBody.message}")
                        }
                    } else {
                        // HTTP error - log detailed error information
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
                
                override fun onFailure(call: Call<DocumentUploadResponse>, t: Throwable) {
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

    fun onProfilePictureClick(imageUri: Uri) {
        uploadProfilePicture(imageUri)
    }

    // Add a method to broadcast profile picture update to any controllers that need it
    private fun broadcastProfilePictureUpdate(profilePicUrl: String?) {
        Log.d("ProfileController", "Broadcasting profile picture update: $profilePicUrl")
        
        // Update the profile model
        model = model.copy(profilePicture = profilePicUrl)
        
        // Update the user data in UserDataManager
        userDataManager.updateProfilePicture(profilePicUrl)
        
        // Also update the HomeController if available
        homeController?.updateProfilePicture(profilePicUrl)
        
        // Invalidate image cache to force a reload
        try {
            // Clear the image cache to force a fresh load
            com.archeGlobal.one.utils.ImageCache.invalidateProfileImageCache()
            Log.d("ProfileController", "Invalidated profile image cache for: $profilePicUrl")
        } catch (e: Exception) {
            Log.e("ProfileController", "Error invalidating image cache: ${e.message}", e)
        }
        
        // Force refresh to create immediate visual effect
        try {
            // Force a navigation to refresh the current screen - this helps update UI immediately
            val currentScreen = navigator.getCurrentRoute()
            if (currentScreen == "profile") {
                // If we're on the profile screen, we need to force a refresh
                navigator.refreshCurrentScreen()
            }
        } catch (e: Exception) {
            Log.e("ProfileController", "Error forcing navigation refresh: ${e.message}", e)
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