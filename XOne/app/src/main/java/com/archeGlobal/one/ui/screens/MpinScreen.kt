package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.model.SecurityQuestion
import com.archeGlobal.one.ui.components.CompanyLogo
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveMpinScreen(
    isReset: Boolean,
    onMpinSet: (String, List<SecurityQuestion>) -> Unit,
    onForgotMpin: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val contentPadding = when (windowSizeClass?.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 16.dp // Phone
        WindowWidthSizeClass.Medium -> 48.dp // Large phone/small tablet
        WindowWidthSizeClass.Expanded -> 120.dp // Tablet
        else -> 16.dp
    }
    MpinScreen(
        isReset = isReset,
        onMpinSet = onMpinSet,
        onForgotMpin = onForgotMpin,
        contentPadding = contentPadding
    )
}

private val securityQuestionsList = listOf(
    "What is the name of your first school?",
    "What is your mother's maiden name?",
    "What was the name of your first company?",
    "What is the name of your childhood best friend?",
    "What is the name of the street you grew up on?",
    "What is the name of your favorite teacher in school?",
    "What is your favorite book or author?",
    "What was the model of your first vehicle?"
)

@Composable
fun OutlinedDropdownField(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = if (selectedIndex in options.indices) options[selectedIndex] else ""

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                    "Select a question",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, // your down arrow icon
                    contentDescription = "Dropdown",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .clickable { expanded = true },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(
                color = if (selectedText.isEmpty()) Color.DarkGray else Color.Black,
                fontSize = 18.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal
            ),
            shape = MaterialTheme.shapes.medium,
            enabled = false // disables keyboard
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White)
        ) {
            options.forEachIndexed { idx, option ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelected(idx)
                            expanded = false
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (idx != options.lastIndex) {
                        Divider(
                            color = Color(0xFFE0E0E0),
                            thickness = 1.dp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MpinScreen(
    isReset: Boolean,
    onMpinSet: (String, List<SecurityQuestion>) -> Unit,
    contentPadding: Dp = 16.dp,
    onForgotMpin: () -> Unit
) {
    // State
    val context = LocalContext.current
    var step by remember { mutableStateOf(0) } // 0: security, 1: mpin
    var error by remember { mutableStateOf<String?>(null) }

    // For reset: get the two questions set previously
    val savedQuestions = remember {
        if (isReset) {
            com.archeGlobal.one.utils.MpinManager.getSecurityQuestions(context)
        } else {
            emptyList()
        }
    }

    // For reset: only allow selection from saved questions
    var selectedResetQuestionIndex by remember { mutableStateOf(0) }
    var resetAnswer by remember { mutableStateOf("") }
    var resetVerified by remember { mutableStateOf(false) }

    // For set: normal logic
    var selectedQuestions by remember { mutableStateOf(listOf("", "")) }
    var answers by remember { mutableStateOf(listOf("", "")) }
    var selectedQuestionIndices by remember { mutableStateOf(listOf(-1, -1)) }
    var showAnswer = remember { mutableStateListOf(false, false) }

    // MPIN state
    var mpinDigits by remember { mutableStateOf(List(4) { "" }) }
    var confirmMpinDigits by remember { mutableStateOf(List(4) { "" }) }
    val focusRequesters = List(4) { remember { FocusRequester() } }
    val confirmFocusRequesters = List(4) { remember { FocusRequester() } }
    var focusedMpinIndex by remember { mutableStateOf(-1) }
    var focusedConfirmIndex by remember { mutableStateOf(-1) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val archeGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
    )

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(archeGradient)
            .padding(horizontal = contentPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            CompanyLogo(modifier = Modifier.height(120.dp))

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Security",
                    tint = Color(0xFFDD3825),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isReset) "Reset Your MPIN" else "Set Your MPIN",
                    fontSize = 22.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 20.dp, 32.dp, 32.dp)
            ) {
                Text(
                    text = if (isReset) {
                        "Verify your identity to reset your MPIN"
                    } else {
                        "Set security questions and a 4-digit PIN for secure access"
                    },
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isReset) {
                // --- RESET FLOW ---
                if (!resetVerified) {
                    // Step 1: Show only one dropdown with the two saved questions
                    Text(
                        "Answer Security Question",
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedDropdownField(
                            options = savedQuestions.map { it.question },
                            selectedIndex = selectedResetQuestionIndex,
                            onSelected = { idx -> selectedResetQuestionIndex = idx }
                        )
                    }
                    Spacer(modifier = Modifier.height(11.dp))
                    OutlinedTextField(
                        value = resetAnswer,
                        onValueChange = { resetAnswer = it },
                        placeholder = {
                            Text(
                                "Answer",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .padding(bottom = 16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        keyboardOptions = KeyboardOptions.Default,
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = if (showAnswer.getOrNull(0) == true) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                showAnswer[0] = !showAnswer[0]
                            }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (showAnswer.getOrNull(0) == true) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                                    ),
                                    contentDescription = if (showAnswer.getOrNull(0) == true) "Hide" else "Show",
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val correctAnswer = savedQuestions[selectedResetQuestionIndex].answer
                            if (resetAnswer.isBlank()) {
                                error = "Please enter the answer!"
                            } else if (resetAnswer.trim() != correctAnswer.trim()) {
                                error = "Incorrect answer. Please try again."
                            } else {
                                error = "Verified Successfully"
                                resetVerified = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(65.dp)
                            .padding(top = 18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            "Verify Answer",
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    if (isReset && !resetVerified) {
                        Spacer(modifier = Modifier.height(18.dp))
                        val annotatedText = buildAnnotatedString {
                            append("Not remember? then ")
                            val start = length
                            append("login again")
                            addStyle(
                                style = SpanStyle(
                                    color = Color(0xFFDD3825),
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Medium
                                ),
                                start = start,
                                end = length
                            )
                            addStringAnnotation(
                                tag = "login_again",
                                annotation = "login_again",
                                start = start,
                                end = length
                            )
                        }
                        ClickableText(
                            text = annotatedText,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(
                                    tag = "login_again",
                                    start = offset,
                                    end = offset
                                )
                                    .firstOrNull()?.let {
                                        // Navigate to LoginActivity
                                        val activity = context as? android.app.Activity
                                        activity?.let {
                                            val intent = android.content.Intent(
                                                context,
                                                com.archeGlobal.one.LoginActivity::class.java
                                            )
                                            intent.flags =
                                                android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            intent.putExtra("forceDifferentUserMode", true)
                                            context.startActivity(intent)
                                            activity.finish()
                                        }
                                    }
                            }
                        )
                    }
                } else {
                    // Step 2: Allow user to set new MPIN
                    Text(
                        "Enter New MPIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until 4) {
                            OutlinedTextField(
                                value = mpinDigits[i],
                                onValueChange = { value ->
                                    if (value.length <= 1 && value.all { it.isDigit() }) {
                                        mpinDigits =
                                            mpinDigits.toMutableList().also { it[i] = value }
                                        if (value.isNotEmpty() && i < 3) {
                                            focusRequesters[i + 1].requestFocus()
                                        }
                                    }
                                    if (value.isEmpty() && i > 0) {
                                        mpinDigits = mpinDigits.toMutableList().also { it[i] = "" }
                                        focusRequesters[i - 1].requestFocus()
                                    }
                                },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(60.dp)
                                    .focusRequester(focusRequesters[i])
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            focusedMpinIndex = i
                                        }
                                    }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (focusedMpinIndex == i) Color(0xFFDD3825) else Color.White,
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = if (i == 3) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next
                                ),
                                shape = MaterialTheme.shapes.medium,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                visualTransformation = PasswordVisualTransformation()
                            )
                            if (i < 3) Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        "Confirm New MPIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until 4) {
                            OutlinedTextField(
                                value = confirmMpinDigits[i],
                                onValueChange = { value ->
                                    if (value.length <= 1 && value.all { it.isDigit() }) {
                                        confirmMpinDigits =
                                            confirmMpinDigits.toMutableList().also { it[i] = value }
                                        if (value.isNotEmpty() && i < 3) {
                                            confirmFocusRequesters[i + 1].requestFocus()
                                        }
                                    }
                                    if (value.isEmpty() && i > 0) {
                                        confirmMpinDigits =
                                            confirmMpinDigits.toMutableList().also { it[i] = "" }
                                        confirmFocusRequesters[i - 1].requestFocus()
                                    }
                                },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(60.dp)
                                    .focusRequester(confirmFocusRequesters[i])
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            focusedConfirmIndex = i
                                        }
                                    }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (focusedConfirmIndex == i) Color(0xFFDD3825) else Color.White,
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = if (i == 3) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next
                                ),
                                shape = MaterialTheme.shapes.medium,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                visualTransformation = PasswordVisualTransformation()
                            )
                            if (i < 3) Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = {
                            val mpin = mpinDigits.joinToString("")
                            val confirmMpin = confirmMpinDigits.joinToString("")
                            if (mpin.length != 4 || confirmMpin.length != 4) {
                                error = "MPIN must be 4 digits!"
                            } else if (mpin != confirmMpin) {
                                error = "MPINs do not match!"
                            } else {
                                error = null
                                com.archeGlobal.one.utils.MpinManager.saveMpin(context, mpin)
                                Toast.makeText(
                                    context,
                                    "MPIN reset successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onMpinSet(mpin, savedQuestions)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(65.dp)
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            "Set MPIN",
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            } else {
                if (step == 0) {
                    // Security Questions Step
                    Text(
                        text = "Select Security Questions",
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    for (i in 0..1) {
                        val filteredQuestions = securityQuestionsList.filterIndexed { idx, _ ->
                            idx == selectedQuestionIndices[i] || idx !in selectedQuestionIndices
                        }
                        val selectedIdxInFiltered = filteredQuestions.indexOf(
                            selectedQuestionIndices.getOrNull(i)?.let { idx ->
                                securityQuestionsList.getOrNull(idx)
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedDropdownField(
                                options = filteredQuestions,
                                selectedIndex = selectedIdxInFiltered,
                                onSelected = { filteredIdx ->
                                    val originalIdx =
                                        securityQuestionsList.indexOf(filteredQuestions[filteredIdx])
                                    selectedQuestionIndices =
                                        selectedQuestionIndices.toMutableList()
                                            .also { it[i] = originalIdx }
                                    selectedQuestions = selectedQuestions.toMutableList()
                                        .also { it[i] = securityQuestionsList[originalIdx] }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(11.dp))
                        OutlinedTextField(
                            value = answers[i],
                            onValueChange = { newValue ->
                                answers = answers.toMutableList().also { it[i] = newValue }
                            },
                            placeholder = {
                                Text(
                                    "Answer ${i + 1}",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.97f)
                                .padding(bottom = if (i == 0) 8.dp else 16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal
                            ),
                            keyboardOptions = KeyboardOptions.Default,
                            shape = MaterialTheme.shapes.medium,
                            visualTransformation = if (showAnswer.getOrNull(i) == true) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (showAnswer.size <= i) {
                                        repeat(i - showAnswer.size + 1) { showAnswer.add(false) }
                                    }
                                    showAnswer[i] = !showAnswer[i]
                                }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (showAnswer.getOrNull(i) == true) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                                        ),
                                        contentDescription = if (showAnswer.getOrNull(i) == true) "Hide" else "Show",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(if (i == 0) 8.dp else 14.dp))
                    }

                    // Info message with red icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "Info",
                            tint = Color(0xFFDD3825),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Answer must be up to 20 characters, can include letters or numbers, and is case sensitive.",
                            fontSize = 12.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (selectedQuestions.any { it.isBlank() } || answers.any { it.isBlank() }) {
                                error = "Please select and answer both security questions!"
                            } else if (selectedQuestions[0] == selectedQuestions[1]) {
                                error = "Please select different security questions!"
                            } else {
                                error = null
                                step = 1
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(65.dp)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825) // Keep same color when disabled
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            "Continue",
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                } else {
                    // MPIN Step
                    Text(
                        "Enter MPIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until 4) {
                            OutlinedTextField(
                                value = mpinDigits[i],
                                onValueChange = { value ->
                                    if (value.length <= 1 && value.all { it.isDigit() }) {
                                        mpinDigits =
                                            mpinDigits.toMutableList().also { it[i] = value }
                                        if (value.isNotEmpty() && i < 3) {
                                            focusRequesters[i + 1].requestFocus()
                                        }
                                    }
                                    if (value.isEmpty() && i > 0) {
                                        mpinDigits =
                                            mpinDigits.toMutableList().also { it[i] = "" }
                                        focusRequesters[i - 1].requestFocus()
                                    }
                                },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(60.dp)
                                    .focusRequester(focusRequesters[i])
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            focusedMpinIndex = i
                                        }
                                    }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (focusedMpinIndex == i) Color(0xFFDD3825) else Color.White,
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = if (i == 3) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next
                                ),
                                shape = MaterialTheme.shapes.medium,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                visualTransformation = PasswordVisualTransformation()
                            )
                            if (i < 3) Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        "Confirm MPIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until 4) {
                            OutlinedTextField(
                                value = confirmMpinDigits[i],
                                onValueChange = { value ->
                                    if (value.length <= 1 && value.all { it.isDigit() }) {
                                        confirmMpinDigits = confirmMpinDigits.toMutableList()
                                            .also { it[i] = value }
                                        if (value.isNotEmpty() && i < 3) {
                                            confirmFocusRequesters[i + 1].requestFocus()
                                        }
                                    }
                                    if (value.isEmpty() && i > 0) {
                                        confirmMpinDigits = confirmMpinDigits.toMutableList()
                                            .also { it[i] = "" }
                                        confirmFocusRequesters[i - 1].requestFocus()
                                    }
                                },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(60.dp)
                                    .focusRequester(confirmFocusRequesters[i])
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            focusedConfirmIndex = i
                                        }
                                    }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (focusedConfirmIndex == i) Color(0xFFDD3825) else Color.White,
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = if (i == 3) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next
                                ),
                                shape = MaterialTheme.shapes.medium,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    disabledTextColor = Color.Black,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                visualTransformation = PasswordVisualTransformation()
                            )
                            if (i < 3) Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = {
                            val mpin = mpinDigits.joinToString("")
                            val confirmMpin = confirmMpinDigits.joinToString("")
                            if (mpin.length != 4 || confirmMpin.length != 4) {
                                error = "MPIN must be 4 digits!"
                            } else if (mpin != confirmMpin) {
                                error = "MPINs do not match!"
                            } else {
                                error = null
                                onMpinSet(
                                    mpin,
                                    listOf(
                                        SecurityQuestion(selectedQuestions[0], answers[0]),
                                        SecurityQuestion(selectedQuestions[1], answers[1])
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .height(65.dp)
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825),
                            disabledContainerColor = Color(0xFFDD3825) // Keep same color when disabled
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            "Set MPIN",
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}