package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.MeetingRoomController
import com.archeGlobal.one.model.MeetingRoom
import com.archeGlobal.one.ui.screens.MeetingRoomScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class MeetingRoomActivity : ComponentActivity() {
    private lateinit var controller: MeetingRoomController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = MeetingRoomController(this)
        val roomName = intent.getStringExtra("roomName") ?: ""
        val roomCapacity = intent.getIntExtra("roomCapacity", 0)
        val roomEquipment = intent.getStringExtra("roomEquipment") ?: ""
        val roomImageRes = intent.getIntExtra("roomImageRes", R.drawable.header_home)
        val numberOfAttendees = intent.getStringExtra("numberOfAttendees") ?: ""
        val meetingType = intent.getStringExtra("meetingType") ?: ""
        val room = MeetingRoom(roomName, roomCapacity, roomEquipment, roomImageRes)
        setContent {
            XOneTheme {
                MeetingRoomScreen(
                    room = room,
                    numberOfAttendees = numberOfAttendees,
                    meetingType = meetingType,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}