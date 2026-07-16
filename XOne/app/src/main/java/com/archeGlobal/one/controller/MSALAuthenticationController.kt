package com.archeGlobal.one.controller

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.service.MSALAuthenticationManager
import com.archeGlobal.one.utils.PreferencesManager
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.exception.MsalException

class MSALAuthenticationController(
    private val context: Context,
    private val navigator: AndroidNavigator,
) {
    private val authManager = MSALAuthenticationManager(context)
    private val TAG = "MSALAuthenticationController"

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf("")
    var successMessage = mutableStateOf("")
    var userEmail = mutableStateOf("")
    var isAuthenticated = mutableStateOf(false)
    var accessToken = mutableStateOf("")

    /**
     * Initialize MSAL - Creates SingleAccountPublicClientApplication using auth_config_single_account.json
     */
    fun initializeMSAL(callback: (Boolean) -> Unit) {
        authManager.initialize { success ->
            if (success) {
                Log.d(TAG, "MSAL initialized successfully")
                isAuthenticated.value = authManager.isUserSignedIn()
                userEmail.value = authManager.getCurrentAccount() ?: ""
                callback(true)
            } else {
                Log.e(TAG, "Failed to initialize MSAL")
                errorMessage.value = "Failed to initialize authentication"
                callback(false)
            }
        }
    }

    /**
     * Perform Sign In - Interactive Azure AD authentication
     * 
     * CRITICAL: Must wait for initializeMSAL() callback(true) before calling this method.
     * The initialization is ASYNCHRONOUS - just because initializeMSAL returns doesn't mean
     * the internal getCurrentAccountAsync has completed.
     * 
     * IMPORTANT: Following Microsoft Learn pattern, we:
     * 1. Only check if MSAL is fully initialized (NOT if account exists)
     * 2. Call signIn() which calls acquireToken() internally
     * 3. MSAL shows browser for user to enter credentials
     * 4. On success, account is cached for future silent token requests
     * 
     * No email/phone/empid needed - just pure Azure AD authentication
     * 
     * Reference: https://learn.microsoft.com/en-us/entra/identity-platform/tutorial-mobile-app-android-sign-in-sign-out
     */
    fun performSignIn(
        activity: Activity,
        callback: (Boolean, String) -> Unit,
    ) {
        // Step 1: Check only if MSAL is FULLY initialized
        if (!authManager.isInitialized()) {
            Log.e(TAG, "✗ MSAL not fully initialized yet. Wait for initializeMSAL() callback to complete.")
            Log.e(TAG, authManager.diagnoseSetup())
            isLoading.value = false
            errorMessage.value = "Authentication system initializing. Please try again."
            callback(false, "MSAL not ready")
            return
        }

        Log.d(TAG, "===== MSAL Sign-In Attempt =====")
        authManager.diagnoseSetup()
        isLoading.value = true
        errorMessage.value = ""

        // Step 2: Call signIn() - this launches interactive Azure AD auth
        Log.d(TAG, "Launching interactive MSAL sign-in...")
        performSignInInternal(activity, callback, retryCount = 0)
    }

    /**
     * Internal sign-in with retry mechanism for browser redirect issues
     */
    private fun performSignInInternal(
        activity: Activity,
        callback: (Boolean, String) -> Unit,
        retryCount: Int,
    ) {
        authManager.signIn(
            activity,
            object : MSALAuthenticationManager.AuthenticationCallback {
                override fun onAuthenticationSuccess(result: IAuthenticationResult) {
                    Log.d(TAG, "✓ MSAL Authentication successful for: ${result.account?.username}")
                    isAuthenticated.value = true
                    accessToken.value = result.accessToken
                    userEmail.value = result.account?.username ?: ""

                    val idToken = result.account?.idToken
                    Log.d(TAG, "MSAL id token available: ${!idToken.isNullOrBlank()}")
                    if (!idToken.isNullOrBlank()) {
                        PreferencesManager(context).saveMsalIdToken(idToken)
                        Log.d(TAG, "Stored MSAL id token for later expense entra login")
                    }

                    fun continueWithBackendLogin() {
                        // Step 3: Fetch user details from Microsoft Graph API
                        Log.d(TAG, "Fetching user profile from Microsoft Graph...")
                        authManager.fetchUserProfileFromGraph(result.accessToken) { userProfile ->
                        if (userProfile != null) {
                            val email = userProfile["email"] ?: ""
                            val mobile = userProfile["mobilePhone"] ?: ""
                            val employeeId = userProfile["employeeId"] ?: ""
                            
                            Log.d(TAG, "✓ User profile fetched: email=$email, mobile=$mobile, employeeId=$employeeId")
                            
                            // Step 4: Call backend login API with user details and MSAL token
                            Log.d(TAG, "Calling backend login API with MSAL token and user details...")
                            callBackendLogin(result.accessToken, email, mobile, employeeId) { success, message ->
                                isLoading.value = false
                                if (success) {
                                    Log.d(TAG, "✓ Backend login successful")
                                    successMessage.value = "Successfully logged in"
                                    callback(true, "Login successful")
                                } else {
                                    Log.e(TAG, "✗ Backend login failed: $message")
                                    errorMessage.value = message
                                    callback(false, message)
                                }
                            }
                        } else {
                            Log.w(TAG, "⚠ Could not fetch user profile from Microsoft Graph, using email from MSAL")
                            isLoading.value = false
                            successMessage.value = "Signed in but could not fetch profile details"
                            // Still consider it a partial success for MSAL
                            callback(true, "Signed in with MSAL (profile fetch failed)")
                        }
                    }
                    }

                    continueWithBackendLogin()
                }

                override fun onAuthenticationError(exception: MsalException) {
                    isLoading.value = false
                    Log.e(TAG, "✗ MSAL Authentication error: ${exception.message}")
                    Log.e(TAG, "Error code: ${exception.errorCode}")
                    
                    // Detailed error handling
                    val errorMsg = when {
                        exception.message?.contains("no signed in account", ignoreCase = true) == true -> {
                            "Browser redirect issue: Authentication completed but account caching failed. Please try again or check your internet connection."
                        }
                        exception.message?.contains("user_cancelled", ignoreCase = true) == true -> {
                            "Sign-in was cancelled. Please try again."
                        }
                        exception.message?.contains("authorization_pending", ignoreCase = true) == true -> {
                            "Authorization request pending. Please try again."
                        }
                        exception.message?.contains("invalid_grant", ignoreCase = true) == true -> {
                            "Invalid credentials or expired session. Please try again."
                        }
                        else -> exception.message ?: "Authentication failed. Please try again."
                    }
                    
                    errorMessage.value = errorMsg
                    Log.e(TAG, "Final error message: $errorMsg")
                    callback(false, errorMsg)
                }

                override fun onCancel() {
                    isLoading.value = false
                    Log.d(TAG, "⊘ Sign-in cancelled by user")
                    errorMessage.value = "Sign-in was cancelled"
                    callback(false, "Cancelled")
                }
            },
        )
    }

    /**
     * Acquire Token Silently - Get cached token without user interaction
     * 
     * IMPORTANT: Only call AFTER successful sign-in when account is cached.
     * If no cached account exists, this will fail with "There is no signed in account".
     * 
     * Usage: Call this to refresh token for API calls without prompting user
     * 
     * Reference: https://learn.microsoft.com/en-us/entra/identity-platform/tutorial-mobile-app-android-sign-in-sign-out
     */
    fun acquireTokenSilently(callback: (String?) -> Unit) {
        // Check if account is cached - REQUIRED for silent token acquisition
        if (!authManager.isUserSignedIn()) {
            Log.w(TAG, "⚠ No signed-in account found. Call performSignIn() first for interactive authentication.")
            errorMessage.value = "No signed-in account. Please sign in first."
            callback(null)
            return
        }

        Log.d(TAG, "Acquiring token silently for cached account...")
        authManager.acquireTokenSilently(
            object : MSALAuthenticationManager.AuthenticationCallback {
                override fun onAuthenticationSuccess(result: IAuthenticationResult) {
                    Log.d(TAG, "✓ Silent token acquisition successful")
                    accessToken.value = result.accessToken
                    callback(result.accessToken)
                }

                override fun onAuthenticationError(exception: MsalException) {
                    Log.e(TAG, "✗ Silent token acquisition error: ${exception.message}")
                    errorMessage.value = exception.message ?: "Failed to acquire token silently"
                    callback(null)
                }

                override fun onCancel() {
                    Log.d(TAG, "⊘ Silent token acquisition cancelled")
                    callback(null)
                }
            },
        )
    }

    /**
     * Get Access Token - Helper to get token for API calls
     * Call this before making authenticated API requests
     */
    fun getAccessToken(scopes: Array<String> = arrayOf("User.Read"), callback: (String?) -> Unit) {
        authManager.getAccessToken(scopes) { token ->
            if (token != null) {
                accessToken.value = token
                Log.d(TAG, "Access token obtained for API calls")
            }
            callback(token)
        }
    }

    /**
     * Perform Sign Out - Remove account from app
     * Call this when user clicks logout button in profile section
     */
    fun performSignOut(callback: (Boolean) -> Unit) {
        isLoading.value = true
        authManager.signOut { success ->
            isLoading.value = false
            if (success) {
                Log.d(TAG, "MSAL Sign-out successful")
                isAuthenticated.value = false
                userEmail.value = ""
                accessToken.value = ""
                PreferencesManager(context).clearMsalIdToken()
                successMessage.value = "Signed out successfully"
                errorMessage.value = ""
                callback(true)
            } else {
                Log.e(TAG, "MSAL Sign-out failed")
                errorMessage.value = "Failed to sign out"
                callback(false)
            }
        }
    }

    fun checkIfUserIsSignedIn(): Boolean {
        val isSignedIn = authManager.isUserSignedIn()
        isAuthenticated.value = isSignedIn
        if (isSignedIn) {
            userEmail.value = authManager.getCurrentAccount() ?: ""
        }
        return isSignedIn
    }

    fun clearMessages() {
        errorMessage.value = ""
        successMessage.value = ""
    }

    /**
     * Call backend login API with MSAL token and user details
     * 
     * This method:
     * 1. Takes the MSAL access token
     * 2. Takes user details (email, mobile, employeeId) from Microsoft Graph
     * 3. Calls the backend login endpoint with these details
     * 4. The backend validates the Azure AD token and creates/updates user session
     * 
     * Flow:
     * MSAL Sign-In → Fetch from Graph → Call Backend Login → Store Session Token → Navigate to Home
     */
    private fun callBackendLogin(
        msalToken: String,
        email: String,
        mobile: String,
        employeeId: String,
        callback: (Boolean, String) -> Unit,
    ) {
        // Use LoginController to call backend login API
        val loginController = LoginController(context, navigator)
        
        // Validate and log the parameters being sent
        Log.d(TAG, "===== Backend Login Parameters =====")
        Log.d(TAG, "Email length: ${email.length}, value: '$email', isEmpty: ${email.isEmpty()}")
        Log.d(TAG, "Mobile length: ${mobile.length}, value: '$mobile', isEmpty: ${mobile.isEmpty()}")
        Log.d(TAG, "EmployeeId length: ${employeeId.length}, value: '$employeeId', isEmpty: ${employeeId.isEmpty()}")
        Log.d(TAG, "MSAL Token length: ${msalToken.length}")
        Log.d(TAG, "===================================")
        
        // Check for empty fields - these will cause backend to reject the request
        val missingFields = mutableListOf<String>()
        if (email.isEmpty()) missingFields.add("email")
        if (mobile.isEmpty()) missingFields.add("mobile")
        if (employeeId.isEmpty()) missingFields.add("employeeId")
        
        if (missingFields.isNotEmpty()) {
            val fieldList = missingFields.joinToString(", ")
            Log.w(TAG, "⚠ WARNING: The following required fields are empty from Microsoft Graph: $fieldList")
            Log.w(TAG, "⚠ These fields come from Microsoft Graph /v1.0/me endpoint")
            Log.w(TAG, "⚠ Email field: userPrincipalName (Azure AD UPN)")
            Log.w(TAG, "⚠ Mobile field: mobilePhone property")
            Log.w(TAG, "⚠ EmployeeId field: employeeId or jobTitle property")
            Log.w(TAG, "⚠ Backend may reject login if these fields are missing/empty")
        }
        
        Log.d(TAG, "Calling backend login API...")
        loginController.loginWithToken(msalToken, email, mobile, employeeId) { message, isError ->
            if (isError) {
                Log.e(TAG, "Backend login failed: $message")
                callback(false, message)
            } else {
                Log.d(TAG, "Backend login successful: $message")
                callback(true, message)
            }
        }
    }
}


