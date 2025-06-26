package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.TodoController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.TodoScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class TodoActivity : ComponentActivity() {
    private lateinit var controller: TodoController
    private lateinit var navigator: AndroidNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigator = AndroidNavigator(this)
        controller = TodoController(navigator, this)

        setContent {
            XOneTheme {
                TodoScreen(
                    controller = controller,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
