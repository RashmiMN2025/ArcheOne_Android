package com.archeGlobal.one.utils

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val context: Context) {
    private val biometricManager = BiometricManager.from(context)
    private val preferencesManager = PreferencesManager(context)

    fun canUseBiometric(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Fingerprint Authentication",
        subtitle: String = "Log in using your fingerprint",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun saveCredentials(email: String, mobile: String, employeeId: String) {
        preferencesManager.saveBiometricCredentials(email, mobile, employeeId)
        preferencesManager.setBiometricEnabled(true)
    }

    fun getStoredCredentials(): Triple<String, String, String>? {
        return preferencesManager.getBiometricCredentials()
    }

    fun isBiometricEnabled(): Boolean {
        return preferencesManager.isBiometricEnabled()
    }

    fun clearBiometricData() {
        preferencesManager.clearBiometricData()
    }
} 