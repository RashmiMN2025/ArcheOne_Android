package com.archeGlobal.one.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OtpNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != OUTLOOK_PACKAGE) return

        val extras = sbn.notification.extras
        val title = extras?.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val otpMatch = OTP_REGEX.find("$title $text $bigText") ?: return
        _otpFlow.value = otpMatch.value
    }

    companion object {
        private const val OUTLOOK_PACKAGE = "com.microsoft.office.outlook"
        private val OTP_REGEX = Regex("\\b(\\d{6})\\b")

        private val _otpFlow = MutableStateFlow<String?>(null)
        val otpFlow: StateFlow<String?> = _otpFlow.asStateFlow()

        fun clearOtp() {
            _otpFlow.value = null
        }
    }
}
