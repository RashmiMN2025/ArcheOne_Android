package com.archeGlobal.one.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.archeGlobal.one.model.SecurityQuestion
import com.archeGlobal.one.ui.theme.XOneTheme

class MpinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isReset = intent.getBooleanExtra("resetMpin", false)
        setContent {
            XOneTheme {
                MpinScreen(
                    isReset = isReset,
                    onMpinSet = { mpin, questions ->
                        com.archeGlobal.one.utils.MpinManager.saveMpin(this, mpin)
                        if (!isReset) {
                            com.archeGlobal.one.utils.MpinManager.saveSecurityQuestions(this, questions)
                        }
                        // Prompt user to enable biometric
                        android.app.AlertDialog.Builder(this)
                            .setTitle("Enable Fingerprint Login")
                            .setMessage("Would you like to use fingerprint for faster login next time?")
                            .setPositiveButton("Yes") { _, _ ->
                                val biometricHelper = com.archeGlobal.one.utils.BiometricHelper(this)
                                biometricHelper.showBiometricPrompt(
                                    activity = this as FragmentActivity,
                                    title = "Setup Fingerprint",
                                    subtitle = "Verify your fingerprint to enable quick login",
                                    onSuccess = {
                                        // Save biometric enabled flag
                                        com.archeGlobal.one.utils.UserDataManager.getInstance(this).preferencesManager.setBiometricEnabled(true)
                                        // Set locked flag so HomeActivity will prompt for biometric
                                        com.archeGlobal.one.utils.UserDataManager.getInstance(this).preferencesManager.setLocked(true)
                                        // Now navigate to Home
                                        val intent = android.content.Intent(this, com.archeGlobal.one.HomeActivity::class.java)
                                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    },
                                    onError = { error ->
                                        // Even if biometric fails, go to Home
                                        com.archeGlobal.one.utils.UserDataManager.getInstance(this).preferencesManager.setLocked(true)
                                        val intent = android.content.Intent(this, com.archeGlobal.one.HomeActivity::class.java)
                                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                )
                            }
                            .setNegativeButton("No") { _, _ ->
                                // User declined biometric, just go to Home
                                com.archeGlobal.one.utils.UserDataManager.getInstance(this).preferencesManager.setLocked(true)
                                val intent = android.content.Intent(this, com.archeGlobal.one.HomeActivity::class.java)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .setCancelable(false)
                            .show()
                    },
                    onForgotMpin = {
                        // Optionally, navigate to login or show a dialog
                        finish()
                    }
                )
            }
        }
    }
}