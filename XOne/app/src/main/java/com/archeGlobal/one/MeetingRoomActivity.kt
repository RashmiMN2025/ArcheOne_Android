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
        val roomType = intent.getStringExtra("roomType") ?: ""
        val roomCapacity = intent.getIntExtra("roomCapacity", 0)
        val roomEquipment = intent.getStringExtra("roomEquipment") ?: ""
        val roomImageUrl = intent.getStringExtra("roomImageUrl") ?: ""
        val roomId = intent.getStringExtra("roomId") ?: ""
        val roomLocation = intent.getStringExtra("roomLocation") ?: ""
        val noOfAttendees = intent.getStringExtra("noOfAttendees") ?: ""
        val meetingType = intent.getStringExtra("meetingType") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val fromTime = intent.getStringExtra("fromTime") ?: ""
        val toTime = intent.getStringExtra("toTime") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val startDate = intent.getStringExtra("startDate") ?: ""
        val endDate = intent.getStringExtra("endDate") ?: ""

        val room = MeetingRoom(
            room_type = roomType,
            name = roomName,
            capacity = roomCapacity,
            equipment = roomEquipment,
            imageUrl = roomImageUrl,
            room_id = roomId,
            location = roomLocation,
            location_name = location
        )

        setContent {
            XOneTheme {
                MeetingRoomScreen(
                    controller = controller,
                    room = room,
                    location = location,
                    date = date,
                    fromTime = fromTime,
                    toTime = toTime,
                    noOfAttendees = noOfAttendees,
                    meetingType = meetingType,
                    startDate = startDate,
                    endDate = endDate,
                    onBackPressed = { finish() },
                    onSubmit = { }
                )
            }
        }
    }
}
