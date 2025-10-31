package com.archeGlobal.one.model

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun ChatBottomNavigationBar(
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onHeadsUpClick: () -> Unit,
    onSOSClick: () -> Unit,
    onProfileClick: () -> Unit,
    isUsingPrideIcon: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val homeSelectedColor = Color(0xFF000000) // Black for home
    val selectedColor = Color(0xFFDD3825) // Red for other items
    val unselectedColor = Color(0xFF808080) // Gray for unselected

    NavigationBar(
        modifier = modifier.height(70.dp),
        containerColor = Color(0xFFF6F4EE),
        contentColor = selectedColor,
        tonalElevation = 4.dp,
    ) {
        // Home item - always unselected in Chat screen
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides homeSelectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        val homeIconRes =
                            if (isUsingPrideIcon) {
                                R.drawable.homepride
                            } else {
                                R.drawable.arche_black2
                            }
                        Image(
                            painter = painterResource(id = homeIconRes),
                            contentDescription = "Home",
                            modifier = Modifier.size(24.dp),
                            colorFilter =
                                if (isUsingPrideIcon) {
                                    null
                                } else {
                                    androidx.compose.ui.graphics.ColorFilter
                                        .tint(homeSelectedColor)
                                },
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Home",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = homeSelectedColor,
                        )
                    }
                }
            },
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = homeSelectedColor, // Black for home when selected
                    unselectedIconColor = homeSelectedColor,
                    selectedTextColor = homeSelectedColor, // Black for home when selected
                    unselectedTextColor = homeSelectedColor,
                    indicatorColor = Color(0xFFF6F4EE),
                ),
            alwaysShowLabel = false,
        )

        // Chat item - always selected in Chat screen
        NavigationBarItem(
            selected = true,
            onClick = onChatClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides selectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.chat),
                            contentDescription = "Chat",
                            modifier = Modifier.size(24.dp),
                            tint = selectedColor,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chat",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = selectedColor,
                        )
                    }
                }
            },
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    selectedTextColor = selectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color(0xFFF6F4EE),
                ),
            alwaysShowLabel = false,
        )

        // Heads Up item - always unselected in Chat screen
        NavigationBarItem(
            selected = false,
            onClick = onHeadsUpClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.headsup),
                            contentDescription = "Heads Up",
                            modifier = Modifier.size(24.dp),
                            tint = unselectedColor,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Heads Up",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = unselectedColor,
                        )
                    }
                }
            },
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    selectedTextColor = selectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color(0xFFF6F4EE),
                ),
            alwaysShowLabel = false,
        )

        // SOS item - always unselected in Chat screen
        NavigationBarItem(
            selected = false,
            onClick = onSOSClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.sostab),
                            contentDescription = "SOS",
                            modifier = Modifier.size(24.dp),
                            tint = unselectedColor,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SOS",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = unselectedColor,
                        )
                    }
                }
            },
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor, // Red for SOS when selected
                    unselectedIconColor = unselectedColor,
                    selectedTextColor = selectedColor, // Red for SOS when selected
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color(0xFFF6F4EE),
                ),
            alwaysShowLabel = false,
        )

        // Profile item - always unselected in Chat screen
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = {
                CompositionLocalProvider(LocalContentColor provides unselectedColor) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.footerprofile),
                            contentDescription = "Profile",
                            modifier = Modifier.size(24.dp),
                            tint = unselectedColor,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Profile",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = unselectedColor,
                        )
                    }
                }
            },
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor, // Red for Profile when selected
                    unselectedIconColor = unselectedColor,
                    selectedTextColor = selectedColor, // Red for Profile when selected
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color(0xFFF6F4EE),
                ),
            alwaysShowLabel = false,
        )
    }
}
