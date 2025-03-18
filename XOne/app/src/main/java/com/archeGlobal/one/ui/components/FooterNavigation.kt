package com.archeGlobal.one.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.model.FooterNavigationModel

@Composable
fun FooterNavigation(
    model: FooterNavigationModel,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onSOSClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home item with arche_tri logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onHomeClick)
                    .padding(horizontal = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arche_tri),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(
                        if (model.showHome) Color(0xFFDD3825) else Color(0xFF808080)
                    )
                )
                Text(
                    text = "Home",
                    color = if (model.showHome) Color(0xFFDD3825) else Color(0xFF808080),
                    fontSize = 12.sp
                )
            }

            // Other footer items
            FooterItem(
                icon = Icons.Default.Email,
                title = "Chat",
                isSelected = model.showChat,
                onClick = onChatClick
            )
            FooterItem(
                icon = Icons.Default.Warning,
                title = "SOS",
                isSelected = model.showSOS,
                onClick = onSOSClick
            )
            FooterItem(
                icon = Icons.Default.Person,
                title = "Profile",
                isSelected = model.showProfile,
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun FooterItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) Color(0xFFDD3825) else Color(0xFF808080),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            color = if (isSelected) Color(0xFFDD3825) else Color(0xFF808080),
            fontSize = 12.sp
        )
    }
} 