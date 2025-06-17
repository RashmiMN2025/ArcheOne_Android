package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.model.SecurityQuestion

private val securityQuestionsList = listOf(
    "What is the name of your first school?",
    "What is your mother’s maiden name?",
    "What was the name of your first company?",
    "What is the name of your childhood best friend?",
    "What is the name of the street you grew up on?",
    "What is the name of your favorite teacher in school?",
    "What is your favorite book or author?",
    "What was the model of your first vehicle?"
)

@Composable
fun MpinScreen(
    isReset: Boolean,
    onMpinSet: (String, List<SecurityQuestion>) -> Unit,
    onForgotMpin: () -> Unit
) {
    val context = LocalContext.current
    var mpin by remember { mutableStateOf("") }
    var confirmMpin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Security questions state
    var selectedQuestions by remember { mutableStateOf(listOf("", "")) }
    var answers by remember { mutableStateOf(listOf("", "")) }
    var showSecurityQuestions by remember { mutableStateOf(!isReset) }
    var showMpinInput by remember { mutableStateOf(isReset.not()) }
    var resetAnswer by remember { mutableStateOf("") }
    var resetQuestionIndex by remember { mutableStateOf(0) }
    val savedQuestions = com.archeGlobal.one.utils.MpinManager.getSecurityQuestions(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (isReset) "Reset Your MPIN" else "Set Your MPIN", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (isReset && showSecurityQuestions) {
            // Show security question for reset
            Text("Answer Security Question", style = MaterialTheme.typography.bodyLarge)
            DropdownMenuBox(
                options = savedQuestions.map { it.question },
                selectedIndex = resetQuestionIndex,
                onSelected = { resetQuestionIndex = it }
            )
            OutlinedTextField(
                value = resetAnswer,
                onValueChange = { resetAnswer = it },
                label = { Text("Answer") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (resetAnswer == savedQuestions[resetQuestionIndex].answer) {
                    showSecurityQuestions = false
                    showMpinInput = true
                    error = null
                } else {
                    error = "Incorrect answer. Please try again."
                }
            }) { Text("Verify Answer") }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onForgotMpin) { Text("Login Again") }
        }

        if (!isReset && showSecurityQuestions) {
            // Setup security questions
            Text("Select Security Questions", style = MaterialTheme.typography.bodyLarge)
            for (i in 0..1) {
                DropdownMenuBox(
                    options = securityQuestionsList.filter { it !in selectedQuestions || it == selectedQuestions[i] },
                    selectedIndex = securityQuestionsList.indexOf(selectedQuestions[i]).takeIf { it >= 0 } ?: 0,
                    onSelected = { idx ->
                        selectedQuestions = selectedQuestions.toMutableList().also { it[i] = securityQuestionsList[idx] }
                    }
                )
                OutlinedTextField(
                    value = answers[i], // <-- Use the single string
                    onValueChange = { newValue ->
                        answers = answers.toMutableList().also { it[i] = newValue }
                    },
                    label = { Text("Answer ${i + 1}") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(onClick = {
                if (selectedQuestions.any { it.isBlank() } || answers.any { it.isBlank() }) {
                    error = "Please select and answer both security questions!"
                } else if (selectedQuestions[0] == selectedQuestions[1]) {
                    error = "Please select different security questions!"
                } else {
                    showSecurityQuestions = false
                    showMpinInput = true
                    error = null
                }
            }) { Text("Continue") }
        }

        if (showMpinInput) {
            OutlinedTextField(
                value = mpin,
                onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) mpin = it },
                label = { Text("MPIN") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmMpin,
                onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) confirmMpin = it },
                label = { Text("Confirm MPIN") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (mpin.length != 4 || confirmMpin.length != 4) {
                    error = "MPIN must be 4 digits!"
                } else if (mpin != confirmMpin) {
                    error = "MPINs do not match!"
                } else {
                    val questions = if (isReset) savedQuestions else listOf(
                        SecurityQuestion(selectedQuestions[0], answers[0]),
                        SecurityQuestion(selectedQuestions[1], answers[1])
                    )
                    onMpinSet(mpin, questions)
                }
            }) { Text(if (isReset) "Reset MPIN" else "Set MPIN") }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun DropdownMenuBox(options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(options.getOrNull(selectedIndex) ?: "Select a question")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { idx, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(idx)
                        expanded = false
                    }
                )
            }
        }
    }
}