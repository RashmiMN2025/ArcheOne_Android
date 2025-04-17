package com.archeGlobal.one.model

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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

/**
 * A custom bottom navigation bar for the Chat screen that shows all tabs
 * but only highlights the Chat tab as selected.
 */
@Composable
fun ChatBottomNavigationBar(
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
        // Home item - always unselected in Chat screen
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.arche_black2),
                            contentDescription = "Home",
                            modifier = Modifier.size(20.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(unselectedColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = unselectedColor
                        )
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = homeSelectedColor,  // Black for home when selected
                unselectedIconColor = unselectedColor,
                selectedTextColor = homeSelectedColor,  // Black for home when selected
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.White
            ),
            alwaysShowLabel = false
        )
        
        // Chat item - always selected in Chat screen
        NavigationBarItem(
            selected = true,
            onClick = onChatClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides selectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.chat),
                            contentDescription = "Chat",
                            modifier = Modifier.size(20.dp),
                            tint = selectedColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chat",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = selectedColor
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
        
        // SOS item - always unselected in Chat screen
        NavigationBarItem(
            selected = false,
            onClick = onSOSClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.sostab),
                            contentDescription = "SOS",
                            modifier = Modifier.size(20.dp),
                            tint = unselectedColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SOS",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = unselectedColor
                        )
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,  // Red for SOS when selected
                unselectedIconColor = unselectedColor,
                selectedTextColor = selectedColor,  // Red for SOS when selected
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.White
            ),
            alwaysShowLabel = false
        )
        
        // Profile item - always unselected in Chat screen
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(20.dp),
                            tint = unselectedColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = unselectedColor
                        )
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,  // Red for Profile when selected
                unselectedIconColor = unselectedColor,
                selectedTextColor = selectedColor,  // Red for Profile when selected
                unselectedTextColor = unselectedColor,
                indicatorColor = Color.White
            ),
            alwaysShowLabel = false
        )
    }
} 