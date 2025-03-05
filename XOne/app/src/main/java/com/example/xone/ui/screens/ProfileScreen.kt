package com.example.xone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.R
import com.example.xone.controller.ProfileController
import com.example.xone.model.ProfileModel
import com.example.xone.model.ProfileMenuItem
import com.example.xone.ui.preview.PreviewNavigator
import androidx.compose.ui.layout.ContentScale
import com.example.xone.model.FooterNavigationModel

@Composable
fun ProfileScreen(
    controller: ProfileController,
    modifier: Modifier = Modifier,
    footerNavigation: FooterNavigationModel = FooterNavigationModel(showProfile = true),
    onFooterHomeClick: () -> Unit = { controller.onBackPressed() },
    onFooterChatClick: () -> Unit = {},
    onFooterSOSClick: () -> Unit = {},
    onFooterProfileClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1),  // Light Beige/Grey
                        Color(0xFFC8C8CA),  // Light Grey
                        Color(0xFF474749)   // Dark Grey
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            ProfileHeader(
                name = controller.model.name,
                email = controller.model.email
            )

            // Rest of the content with padding
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Menu Items
                MenuItems(
                    items = controller.model.menuItems,
                    onItemClick = { title ->
                        when (title) {
                            "About Me" -> controller.onAboutMeClick()
                            "Address/Coordinates" -> controller.onAddressClick()
                            "Emergency Contact" -> controller.onEmergencyContactClick()
                            "Documents" -> controller.onDocumentsClick()
                            "Log out" -> controller.onLogoutClick()
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Version
                Text(
                    text = controller.model.version,
                    color = Color.White,
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Footer navigation
            FooterNavigation(
                model = footerNavigation,
                onHomeClick = onFooterHomeClick,
                onChatClick = onFooterChatClick,
                onSOSClick = onFooterSOSClick,
                onProfileClick = onFooterProfileClick
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.header_home),
            contentDescription = "Header Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
        ) {
            // Profile Icon
            Surface(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(80.dp),
                shape = CircleShape,
                color = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = email,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MenuItems(
    items: List<ProfileMenuItem>,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            MenuItem(
                title = item.title,
                icon = getIconForMenuItem(item.icon),
                onClick = { onItemClick(item.title) }
            )
        }
    }
}

@Composable
private fun MenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

private fun getIconForMenuItem(icon: String): ImageVector {
    return when (icon) {
        "person" -> Icons.Default.Person
        "home" -> Icons.Default.Home
        "phone" -> Icons.Default.Phone
        "document" -> Icons.Default.Menu
        "logout" -> Icons.Default.ExitToApp
        else -> Icons.Default.KeyboardArrowRight
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val previewController = ProfileController(
        context = LocalContext.current,
        navigator = PreviewNavigator()
    )
    
    ProfileScreen(controller = previewController)
} 