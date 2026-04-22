package com.archeGlobal.one.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NotificationAccessDialog(
    onAllow: () -> Unit,
    onSkip: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Enable OTP Auto-fill") },
        text = { Text("Allow ArcheOne to read your Outlook notifications so the OTP is filled in automatically when it arrives.") },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Not Now")
            }
        },
    )
}
