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

        val location = intent.getStringExtra("location") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val fromTime = intent.getStringExtra("fromTime") ?: ""
        val toTime = intent.getStringExtra("toTime") ?: ""
        val startDate = intent.getStringExtra("startDate") ?: ""
        val endDate = intent.getStringExtra("endDate") ?: ""
        val numberOfAttendeesStr = intent.getStringExtra("noOfAttendees") ?: ""
        val meetingType = intent.getStringExtra("meetingType") ?: ""
        val noOfAttendees = numberOfAttendeesStr.toIntOrNull() ?: 0
        if (noOfAttendees <= 0) {
            // Optionally, handle invalid input (e.g., show toast and finish)
            android.widget.Toast.makeText(
                this,
                "Invalid number of attendees",
                android.widget.Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        controller = MeetingRoomListController(this, location, startDate, endDate, noOfAttendees)
        setContent {
            XOneTheme {
                MeetingRoomListScreen(
                    controller = controller,
                    numberOfAttendees = numberOfAttendeesStr,
                    meetingType = meetingType,
                    onBackPressed = { finish() },
                    onRoomClick = { room: MeetingRoom ->
                        val intent = Intent(this@MeetingRoomListActivity, MeetingRoomActivity::class.java).apply {
                            putExtra("roomName", room.name)
                            putExtra("roomType", room.room_type)
                            putExtra("roomCapacity", room.capacity)
                            putExtra("roomEquipment", room.equipment)
                            putExtra("roomImageUrl", room.imageUrl)
                            putExtra("roomId", room.room_id)
                            putExtra("roomLocation", room.location)
                            putExtra("noOfAttendees", numberOfAttendeesStr)
                            putExtra("meetingType", meetingType)
                            putExtra("date", date)
                            putExtra("fromTime", fromTime)
                            putExtra("toTime", toTime)
                            putExtra("location", location)
                            putExtra("startDate", startDate)
                            putExtra("endDate", endDate)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}