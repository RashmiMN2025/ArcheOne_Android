package com.archeGlobal.one.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.model.FooterNavigationModel

@Composable
fun BottomNavigationBar(
    model: FooterNavigationModel,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onSOSClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeSelectedColor = Color(0xFF000000)  // Black for home
    val selectedColor = Color(0xFFDD3825)      // Red for other items
    val unselectedColor = Color(0xFF808080)    // Gray for unselected

    NavigationBar(
        modifier = modifier.height(56.dp),
        containerColor = Color.White,
        contentColor = selectedColor,
        tonalElevation = 4.dp
    ) {
        // Home item
        NavigationBarItem(
            selected = model.showHome,
            onClick = onHomeClick,
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arche_black2),
                        contentDescription = "Home",
                        modifier = Modifier.size(20.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(homeSelectedColor) // Always black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = homeSelectedColor // Always black
                    )
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = homeSelectedColor,
                unselectedIconColor = homeSelectedColor, // Always black
                selectedTextColor = homeSelectedColor, // Always black
                unselectedTextColor = homeSelectedColor, // Always black
                indicatorColor = Color.White
            ),
            alwaysShowLabel = false
        )

        // Chat item
        NavigationBarItem(
            selected = model.showChat,
            onClick = onChatClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides if (model.showChat) selectedColor else unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.chat),
                            contentDescription = "Chat",
                            modifier = Modifier.size(20.dp),
                            tint = if (model.showChat) selectedColor else unselectedColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chat",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = if (model.showChat) selectedColor else unselectedColor
                        )
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                unselectedIconColor = unselectedColor,
                selectedTextColor = selectedColor,
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.White
            ),
            alwaysShowLabel = false
        )

        // SOS item
        NavigationBarItem(
            selected = model.showSOS,
            onClick = onSOSClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides if (model.showSOS) selectedColor else unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.sostab),
                            contentDescription = "SOS",
                            modifier = Modifier.size(20.dp),
                            tint = if (model.showSOS) selectedColor else unselectedColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SOS",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = if (model.showSOS) selectedColor else unselectedColor
                        )
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                unselectedIconColor = unselectedColor,
                selectedTextColor = selectedColor,
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.White
            ),
            alwaysShowLabel = false
        )

        // Profile item
        NavigationBarItem(
            selected = model.showProfile,
            onClick = onProfileClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides if (model.showProfile) selectedColor else unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(20.dp),
                            tint = if (model.showProfile) selectedColor else unselectedColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = if (model.showProfile) selectedColor else unselectedColor
                        )
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                unselectedIconColor = unselectedColor,
                selectedTextColor = selectedColor,
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.White
            ),
            alwaysShowLabel = false
        )
    }
} 
