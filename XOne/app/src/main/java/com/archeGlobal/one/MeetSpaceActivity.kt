package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.MeetSpaceController
import com.archeGlobal.one.ui.screens.MeetSpaceScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class MeetSpaceActivity: ComponentActivity() {
    private lateinit var controller: MeetSpaceController

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        controller = MeetSpaceController(this)
        setContent {
            XOneTheme {
                MeetSpaceScreen(
                    controller = controller,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}