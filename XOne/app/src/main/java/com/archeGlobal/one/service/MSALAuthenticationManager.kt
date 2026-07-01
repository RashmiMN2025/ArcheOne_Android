package com.archeGlobal.one.service

import android.content.Context
import android.util.Log
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

/**
 * MSALAuthenticationManager - Handles Azure AD B2C authentication using Microsoft Authentication Library
 * 
 * WORKFLOW:
 * 1. Initialize - Creates SingleAccountPublicClientApplication from auth_config_single_account.json
 * 2. Sign In - Call acquireToken() with user interaction for first-time login
 * 3. Get Token - Use acquireTokenSilently() to get cached token for API calls (no user interaction)
 * 4. API Calls - Use token in Authorization header: "Authorization: Bearer {accessToken}"
 * 5. Sign Out - Call signOut() to remove cached account when user logs out
 */
class MSALAuthenticationManager(private val context: Context) {
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private var currentAccount: IAccount? = null
    private var initializationComplete = false
    private val TAG = "MSALAuthenticationManager"

    interface AuthenticationCallback {
        fun onAuthenticationSuccess(result: IAuthenticationResult)
        fun onAuthenticationError(exception: MsalException)
        fun onCancel()
    }

    private val AUTHORITY = "https://login.microsoftonline.com/3865b44b-651f-4df8-a0c8-2625494f6198"
    private fun getScopes(): Array<String> {
        val scopes = arrayOf("User.Read")
        Log.d(TAG, "Requesting scopes: ${scopes.joinToString(",")}")
        return scopes
    }

    /**
     * Initialize MSAL - Creates SingleAccountPublicClientApplication using auth_config_single_account.json
     * Must be called before any authentication operations
     * 
     * IMPORTANT: This method is asynchronous. The callback(true) is called when:
     * 1. ISingleAccountPublicClientApplication is created
     * 2. CurrentAccountAsync callback completes
     * 
     * Only then is it safe to call signIn() or other auth methods.
     */
    fun initialize(callback: (Boolean) -> Unit) {
        initializationComplete = false
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            com.archeGlobal.one.R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mSingleAccountApp = application
                    Log.d(TAG, "ISingleAccountPublicClientApplication created, loading current account...")
                    application.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
                        override fun onAccountLoaded(activeAccount: IAccount?) {
                            // Check if there's a cached account but user is logged out locally
                            // If so, remove the orphaned account from MSAL cache
                            if (activeAccount != null && !isUserLoggedInLocally()) {
                                Log.d(TAG, "Found orphaned MSAL account (${activeAccount.username}) after logout, clearing it...")
                                clearOrphanedAccount(application) { cleared ->
                                    currentAccount = null
                                    initializationComplete = true
                                    Log.d(TAG, "✓ MSAL initialized with orphaned account cleared")
                                    callback(true)
                                }
                            } else {
                                currentAccount = activeAccount
                                initializationComplete = true
                                Log.d(TAG, "✓ MSAL fully initialized, current account: ${activeAccount?.username ?: "none"}")
                                callback(true)
                            }
                        }

                        override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                            this@MSALAuthenticationManager.currentAccount = currentAccount
                            initializationComplete = true
                            Log.d(TAG, "✓ MSAL account changed: ${priorAccount?.username} → ${currentAccount?.username}")
                            callback(true)
                        }

                        override fun onError(exception: MsalException) {
                            Log.e(TAG, "Error loading current account: ${exception.message}")
                            initializationComplete = true
                            callback(true)
                        }
                    })
                }

                override fun onError(exception: MsalException) {
                    Log.e(TAG, "✗ Error initializing MSAL: ${exception.message}")
                    initializationComplete = false
                    callback(false)
                }
            },
        )
    }

    /**
     * Sign In - Interactive authentication with user
     * Launches Azure AD login prompt for user to enter credentials
     * Returns IAuthenticationResult with accessToken for API calls
     * 
     * IMPORTANT: For ISingleAccountPublicClientApplication, we use signIn() method, NOT acquireToken()
     * - signIn() is for SingleAccount mode (account_mode = "SINGLE")
     * - acquireToken() is for MultiAccount mode
     * 
     * Signature: signIn(Activity activity, String loginHint, String[] scopes, AuthenticationCallback callback)
     * - loginHint: null = user chooses account, or pass email to pre-fill
     * - scopes: array of permissions like ["User.Read"]
     * 
     * IMPORTANT: For FIRST sign-in, do NOT check if account exists.
     * The account is cached AFTER successful sign-in.
     * Only check if MSAL is initialized.
     * 
     * Following Microsoft identity platform pattern:
     * https://learn.microsoft.com/en-us/entra/identity-platform/tutorial-mobile-app-android-sign-in-sign-out
     * 
     * Usage:
     * ```
     * // Step 1: Initialize MSAL first via initialize()
     * msalManager.initialize { success ->
     *     if (success) {
     *         // Step 2: Call signIn() - launches interactive Azure AD authentication
     *         msalManager.signIn(activity, null, object : AuthenticationCallback { ... })
     *     }
     * }
     * 
     * // In onAuthenticationSuccess:
     * // - result.accessToken is available immediately
     * // - Account is now cached in MSAL
     * // - Use result.accessToken in API calls: "Authorization: Bearer $token"
     * ```
     */
    fun signIn(
        activity: android.app.Activity,
        callback: AuthenticationCallback,
    ) {
        if (mSingleAccountApp == null || !initializationComplete) {
            Log.e(TAG, "✗ MSAL not fully initialized. mSingleAccountApp=$mSingleAccountApp, initComplete=$initializationComplete")
            Log.e(TAG, "⚠ Make sure initialize() callback(true) was called before calling signIn()")
            callback.onCancel()
            return
        }

        Log.d(TAG, "Starting interactive sign-in flow via signIn() method...")
        Log.d(TAG, "Activity: ${activity.localClassName}")
        Log.d(TAG, "Scopes: ${getScopes().joinToString(",")}")
        
        try {
            mSingleAccountApp!!.signIn(
                activity,
                null,  // loginHint - null means user can enter any account
                getScopes(),
                object : com.microsoft.identity.client.AuthenticationCallback {
                    override fun onSuccess(authenticationResult: IAuthenticationResult) {
                        currentAccount = authenticationResult.account
                        Log.d(TAG, "✓ Interactive sign-in successful: ${authenticationResult.account?.username}")
                        Log.d(TAG, "✓ Access token obtained: ${authenticationResult.accessToken.substring(0, minOf(20, authenticationResult.accessToken.length))}...")
                        Log.d(TAG, "✓ Account now cached for future silent token requests")
                        callback.onAuthenticationSuccess(authenticationResult)
                    }

                    override fun onError(exception: MsalException) {
                        Log.e(TAG, "✗ Interactive sign-in failed: ${exception.message}")
                        Log.e(TAG, "Exception type: ${exception.javaClass.simpleName}")
                        Log.e(TAG, "Exception error code: ${exception.errorCode}")
                        
                        // Log the full exception stack trace for debugging
                        exception.printStackTrace()
                        
                        // Check if this is a redirect URI issue
                        if (exception.message?.contains("redirect", ignoreCase = true) == true ||
                            exception.message?.contains("no signed in account", ignoreCase = true) == true) {
                            Log.e(TAG, "⚠ POSSIBLE REDIRECT/BROWSER ISSUE:")
                            Log.e(TAG, "  This error typically means the browser redirect didn't complete properly")
                            Log.e(TAG, "  or MSAL couldn't cache the account after authentication")
                            Log.e(TAG, "  Config redirect: msauth://com.archeGlobal.one/Sg%2FUNMZmy1u8kGYkDOSwv44%2FHQs%3D")
                            Log.e(TAG, "  Manifest filter: com.microsoft.identity.client.BrowserTabActivity")
                        }
                        
                        // For debugging, also check current account status
                        try {
                            val currentAcc = mSingleAccountApp?.currentAccount
                            Log.d(TAG, "Current account after error: $currentAcc")
                        } catch (e: Exception) {
                            Log.d(TAG, "Could not check current account: ${e.message}")
                        }
                        
                        callback.onAuthenticationError(exception)
                    }

                    override fun onCancel() {
                        Log.d(TAG, "⊘ Interactive sign-in cancelled by user")
                        callback.onCancel()
                    }
                },
            )
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception calling signIn: ${e.message}")
            e.printStackTrace()
            callback.onCancel()
        }
    }

    /**
     * Check if MSAL SDK is FULLY initialized and ready to use
     * Returns true only after:
     * 1. ISingleAccountPublicClientApplication was created
     * 2. getCurrentAccountAsync callback completed
     * 
     * IMPORTANT: Returns false if initialization is still in progress!
     */
    fun isInitialized(): Boolean {
        val initialized = initializationComplete && mSingleAccountApp != null
        Log.d(TAG, "isInitialized: $initialized (initComplete=$initializationComplete, appCreated=${mSingleAccountApp != null})")
        return initialized
    }

    /**
     * Diagnose MSAL setup - helps debug connection issues
     */
    fun diagnoseSetup(): String {
        val diagnostics = StringBuilder()
        diagnostics.append("MSAL Setup Diagnosis:\n")
        diagnostics.append("- App initialized: ${mSingleAccountApp != null}\n")
        diagnostics.append("- Initialization complete: $initializationComplete\n")
        diagnostics.append("- Current account cached: ${currentAccount != null}\n")
        try {
            diagnostics.append("- Account from SDK: ${mSingleAccountApp?.currentAccount}\n")
        } catch (e: Exception) {
            diagnostics.append("- Account check error: ${e.message}\n")
        }
        Log.d(TAG, diagnostics.toString())
        return diagnostics.toString()
    }

    /**
     * Acquire Token Silently - Get cached token without user interaction
     * 
     * CRITICAL: This method REQUIRES an account to be cached from a prior successful sign-in.
     * If no account exists, this will fail with "There is no signed in account".
     * 
     * MUST be called AFTER successful interactive sign-in via signIn()
     * 
     * Usage flow:
     * ```
     * // Step 1: User signs in interactively (calls acquireToken internally)
     * signIn(activity, callback)  // Account is cached after success
     * 
     * // Step 2: Later, refresh token silently (no user interaction, no UI)
     * acquireTokenSilently(callback)
     * 
     * // Step 3: If token expired, MSAL auto-refreshes using refresh token
     * // If refresh token expired, user must call signIn() again
     * ```
     * 
     * Error handling:
     * - If no cached account: Fail immediately
     * - If token expired: MSAL refreshes automatically
     * - If refresh token expired: User must sign in again interactively
     * 
     * Reference: https://learn.microsoft.com/en-us/entra/identity-platform/tutorial-mobile-app-android-sign-in-sign-out
     */
    fun acquireTokenSilently(
        callback: AuthenticationCallback,
    ) {
        if (mSingleAccountApp == null) {
            Log.e(TAG, "MSAL not initialized")
            return
        }

        mSingleAccountApp!!.acquireTokenSilentAsync(
            getScopes(),
            AUTHORITY,
            object : SilentAuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    Log.d(TAG, "Silent token acquisition successful")
                    callback.onAuthenticationSuccess(authenticationResult)
                }

                override fun onError(exception: MsalException) {
                    Log.e(TAG, "Silent token acquisition error: ${exception.message}")
                    callback.onAuthenticationError(exception)
                }
            },
        )
    }

    /**
     * Refresh MSAL token silently and keep session active until logout.
     * This method attempts to refresh the access token using MSAL's cached account.
     */
    fun refreshTokenSilently(callback: (String?) -> Unit) {
        if (mSingleAccountApp == null) {
            Log.e(TAG, "MSAL not initialized for refreshTokenSilently")
            callback(null)
            return
        }

        if (!isUserSignedIn()) {
            Log.w(TAG, "No signed-in account available for silent refresh")
            callback(null)
            return
        }

        mSingleAccountApp!!.acquireTokenSilentAsync(
            getScopes(),
            AUTHORITY,
            object : SilentAuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    currentAccount = authenticationResult.account
                    Log.d(TAG, "MSAL access token refreshed silently")
                    callback(authenticationResult.accessToken)
                }

                override fun onError(exception: MsalException) {
                    Log.w(TAG, "MSAL silent refresh failed: ${exception.message}")
                    callback(null)
                }
            },
        )
    }

    /**
     * Sign Out - Remove cached account and tokens
     * Call this when user clicks logout button in profile section
     * Clears all cached tokens and account info
     * 
     * Usage:
     * ```
     * msalManager.signOut { success ->
     *     if (success) {
     *         // Navigate to login screen
     *     }
     * }
     * ```
     */
    fun signOut(callback: (Boolean) -> Unit) {
        if (mSingleAccountApp == null) {
            Log.e(TAG, "MSAL not initialized")
            callback(false)
            return
        }

        mSingleAccountApp!!.signOut(
            object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    currentAccount = null
                    Log.d(TAG, "Sign-out successful")
                    callback(true)
                }

                override fun onError(exception: MsalException) {
                    Log.e(TAG, "Sign-out error: ${exception.message}")
                    callback(false)
                }
            },
        )
    }

    /**
     * Get Access Token - Helper to get token for API calls
     * Call this before making authenticated API requests to Microsoft Graph or custom APIs
     * 
     * Usage:
     * ```
     * msalManager.getAccessToken(arrayOf("User.Read")) { token ->
     *     if (token != null) {
     *         // Make API call with: "Authorization: Bearer $token"
     *     }
     * }
     * ```
     */
    fun getAccessToken(
        scopes: Array<String>,
        callback: (String?) -> Unit,
    ) {
        if (mSingleAccountApp == null) {
            Log.e(TAG, "MSAL not initialized")
            callback(null)
            return
        }

        try {
            mSingleAccountApp!!.acquireTokenSilentAsync(
                scopes,
                AUTHORITY,
                object : SilentAuthenticationCallback {
                    override fun onSuccess(authenticationResult: IAuthenticationResult) {
                        Log.d(TAG, "Access token obtained for API calls")
                        callback(authenticationResult.accessToken)
                    }

                    override fun onError(exception: MsalException) {
                        Log.e(TAG, "Error getting access token: ${exception.message}")
                        callback(null)
                    }
                },
            )
        } catch (exception: Exception) {
            Log.e(TAG, "Error getting access token: ${exception.message}")
            callback(null)
        }
    }

    /**
     * Check if user is logged in locally (in app preferences)
     * Used to detect orphaned MSAL cached accounts after logout
     */
    private fun isUserLoggedInLocally(): Boolean {
        return try {
            val userDataManager = com.archeGlobal.one.utils.UserDataManager.getInstance(context)
            userDataManager.isLoggedIn()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking local login status: ${e.message}")
            false
        }
    }

    /**
     * Clear orphaned MSAL cached account when user is logged out locally
     * This prevents "account already signed in" error on re-login after logout
     */
    private fun clearOrphanedAccount(application: ISingleAccountPublicClientApplication, callback: (Boolean) -> Unit) {
        try {
            application.signOut(
                object : ISingleAccountPublicClientApplication.SignOutCallback {
                    override fun onSignOut() {
                        Log.d(TAG, "Orphaned account successfully cleared from MSAL cache")
                        callback(true)
                    }

                    override fun onError(exception: MsalException) {
                        Log.e(TAG, "Error clearing orphaned account: ${exception.message}")
                        callback(false)
                    }
                },
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception clearing orphaned account: ${e.message}")
            callback(false)
        }
    }

    /**
     * Check if user is currently signed in
     * Returns true if a valid cached account exists
     */
    fun isUserSignedIn(): Boolean {
        return currentAccount != null
    }

    /**
     * Get current signed-in account username
     * Returns the UPN (User Principal Name) or email of the current account
     */
    fun getCurrentAccount(): String? {
        return currentAccount?.username
    }

    /**
     * Fetch user profile from Microsoft Graph API
     * 
     * Calls: GET https://graph.microsoft.com/v1.0/me
     * Returns: email, userPrincipalName, mobilePhone, employeeId, etc.
     * 
     * Usage:
     * ```
     * msalManager.fetchUserProfileFromGraph(accessToken) { userProfile ->
     *     if (userProfile != null) {
     *         val email = userProfile["email"]
     *         val mobile = userProfile["mobilePhone"]
     *         val empId = userProfile["employeeId"]
     *     }
     * }
     * ```
     */
    fun fetchUserProfileFromGraph(accessToken: String, callback: (Map<String, String>?) -> Unit) {
        Thread {
            try {
                val client = OkHttpClient()
                // Explicitly request the fields we need
                val graphUrl = "https://graph.microsoft.com/v1.0/me?" +
                    "\$select=id,userPrincipalName,mail,mobilePhone,displayName," +
                    "givenName,surname,jobTitle,officeLocation,employeeId"
                
                val request = Request.Builder()
                    .url(graphUrl)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .build()

                Log.d(TAG, "Fetching Microsoft Graph: $graphUrl")
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        Log.d(TAG, "Microsoft Graph raw response: $body")
                        val jsonObject = JSONObject(body)
                        
                        // Extract user details from Microsoft Graph response
                        val userProfile = mutableMapOf<String, String>()
                        
                        // Email extraction priority: userPrincipalName > mail
                        val upn = jsonObject.optString("userPrincipalName", "")
                        val mail = jsonObject.optString("mail", "")
                        val email = if (upn.isNotEmpty()) upn else mail
                        userProfile["email"] = email
                        
                        // Mobile phone extraction
                        val mobilePhone = jsonObject.optString("mobilePhone", "")
                        userProfile["mobilePhone"] = mobilePhone
                        
                        // Employee ID extraction priority: employeeId > jobTitle
                        val empId = jsonObject.optString("employeeId", "")
                        val jobTitle = jsonObject.optString("jobTitle", "")
                        val employeeId = if (empId.isNotEmpty()) empId else jobTitle
                        userProfile["employeeId"] = employeeId
                        
                        // Additional useful fields
                        userProfile["displayName"] = jsonObject.optString("displayName", "")
                        userProfile["givenName"] = jsonObject.optString("givenName", "")
                        userProfile["surname"] = jsonObject.optString("surname", "")
                        userProfile["id"] = jsonObject.optString("id", "")
                        
                        Log.d(TAG, "✓ Microsoft Graph user profile extracted:")
                        Log.d(TAG, "  UPN: '$upn'")
                        Log.d(TAG, "  Mail: '$mail'")
                        Log.d(TAG, "  Email (final): '$email'")
                        Log.d(TAG, "  Mobile: '$mobilePhone'")
                        Log.d(TAG, "  EmployeeId: '$employeeId'")
                        Log.d(TAG, "  JobTitle: '$jobTitle'")
                        Log.d(TAG, "  DisplayName: '${userProfile["displayName"]}'")
                        
                        // Log empty fields for debugging
                        if (email.isEmpty()) {
                            Log.w(TAG, "⚠ EMAIL IS EMPTY! UPN: '$upn', Mail: '$mail'")
                        }
                        if (mobilePhone.isEmpty()) {
                            Log.w(TAG, "⚠ MOBILE IS EMPTY from Microsoft Graph")
                        }
                        if (employeeId.isEmpty()) {
                            Log.w(TAG, "⚠ EMPLOYEE ID IS EMPTY! EmpId: '$empId', JobTitle: '$jobTitle'")
                        }
                        
                        callback(userProfile)
                    } else {
                        Log.e(TAG, "✗ Empty response body from Microsoft Graph")
                        callback(null)
                    }
                } else {
                    Log.e(TAG, "✗ Failed to fetch user profile from Microsoft Graph: ${response.code}")
                    val errorBody = response.body?.string()
                    Log.e(TAG, "Error response: $errorBody")
                    callback(null)
                }
            } catch (e: IOException) {
                Log.e(TAG, "✗ Network error fetching user profile: ${e.message}")
                e.printStackTrace()
                callback(null)
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error fetching user profile: ${e.message}")
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }
}
