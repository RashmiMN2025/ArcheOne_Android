package com.archeGlobal.one.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun FingerprintEnableDialog(
    onEnable: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismiss on outside touch if needed */ },
        title = { Text("Enable Fingerprint Authentication") },
        text = { Text("Would you like to enable fingerprint authentication for faster and secure logins in the future?") },
        confirmButton = {
            TextButton(onClick = onEnable) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Not Now")
            }
        }
    )
}
