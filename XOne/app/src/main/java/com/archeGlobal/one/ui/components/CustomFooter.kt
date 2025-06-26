package com.archeGlobal.one.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun CustomFooter(
    footerNavigation: FooterNavigationModel,
    onFooterHomeClick: () -> Unit,
    onFooterChatClick: () -> Unit,
    onFooterSOSClick: () -> Unit,
    onFooterProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // Increased footer height
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FooterIcon(
            text = "Home",
            icon = Icons.Default.Home,
            onClick = onFooterHomeClick,
            isSelected = footerNavigation.showHome
        )
        FooterIcon(
            text = "Chat",
            icon = Icons.Default.Person, // Replace with the correct chat icon
            onClick = onFooterChatClick,
            isSelected = footerNavigation.showChat
        )
        FooterIcon(
            text = "SOS",
            icon = Icons.Default.Warning, // Replace with the correct SOS icon
            onClick = onFooterSOSClick,
            isSelected = footerNavigation.showSOS
        )
        FooterIcon(
            text = "Profile",
            icon = Icons.Default.Person,
            onClick = onFooterProfileClick,
            isSelected = footerNavigation.showProfile
        )
    }
}

@Composable
fun FooterIcon(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isSelected) Color.Black else Color.Gray,
            modifier = Modifier.size(32.dp) // Increased icon size
        )
        Text(
            text = text,
            fontSize = 14.sp, // Updated font size
            fontFamily = GraphikFontFamily, // Updated font family
            fontWeight = FontWeight.Medium, // Updated font weight
            color = if (isSelected) Color.Black else Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
