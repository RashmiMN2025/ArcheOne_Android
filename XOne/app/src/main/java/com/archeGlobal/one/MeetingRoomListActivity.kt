package com.archeGlobal.one

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.MeetingRoomListController
import com.archeGlobal.one.model.MeetingRoom
import com.archeGlobal.one.ui.screens.MeetingRoomListScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class MeetingRoomListActivity : ComponentActivity() {
    private lateinit var controller: MeetingRoomListController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = MeetingRoomListController(this)
        val numberOfAttendees = intent.getStringExtra("numberOfAttendees") ?: ""
        val meetingType = intent.getStringExtra("meetingType") ?: ""
        setContent {
            XOneTheme {
                MeetingRoomListScreen(
                    controller = controller,
                    numberOfAttendees = numberOfAttendees,
                    meetingType = meetingType,
                    onBackPressed = { finish() },
                    onRoomClick = { room: MeetingRoom ->
                        val intent =
                            Intent(this@MeetingRoomListActivity, MeetingRoomActivity::class.java).apply {
                                putExtra("roomName", room.name)
                                putExtra("roomCapacity", room.capacity)
                                putExtra("roomEquipment", room.equipment)
                                putExtra("roomImageRes", room.imageRes)
                                putExtra("numberOfAttendees", numberOfAttendees)
                                putExtra("meetingType", meetingType)
                            }
                        startActivity(intent)
                    },
                )
            }
        }
    }
}
