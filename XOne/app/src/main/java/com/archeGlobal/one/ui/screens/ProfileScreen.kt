package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.ProfileController
import com.archeGlobal.one.model.ProfileMenuItem
import com.archeGlobal.one.ui.preview.PreviewNavigator
import androidx.compose.ui.layout.ContentScale
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.ui.components.FooterScaffold
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.activity.compose.BackHandler
import android.net.Uri
import android.util.Log
import com.archeGlobal.one.utils.ImageCache
import androidx.compose.runtime.collectAsState
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import android.widget.Toast
import android.graphics.Bitmap

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
    // State to control the visibility of the logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Disable back swipe gesture
    BackHandler(enabled = true) {
        // Handle back press manually

    }

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            controller.uploadProfilePhoto(bitmap)
            showUploadDialog = false
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            controller.uploadProfilePhotoFromUri(uri)
            showUploadDialog = false
        }
    }

    FooterScaffold(
        footerNavigation = footerNavigation,
        onFooterHomeClick = onFooterHomeClick,
        onFooterChatClick = onFooterChatClick,
        onFooterSOSClick = onFooterSOSClick,
        onFooterProfileClick = onFooterProfileClick
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
                    email = controller.model.email,
                    profilePicture = controller.model.profilePicture,
                    onProfilePictureClick = { uri ->
                        controller.onProfilePictureClick(uri)
                    },
                    onCameraCapture = { bitmap ->
                        controller.uploadProfilePhoto(bitmap)
                    }
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
                                "Log out" -> showLogoutDialog = true // Show logout dialog instead of direct action
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
            }

            // Logout confirmation dialog
            if (showLogoutDialog) {
                LogoutConfirmationDialog(
                    onConfirm = { 
                        showLogoutDialog = false
                        controller.onLogoutClick()
                    },
                    onDismiss = { 
                        showLogoutDialog = false 
                    }
                )
            }

            // Upload Dialog
            if (showUploadDialog) {
                Dialog(onDismissRequest = { showUploadDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Upload Profile Photo",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                "Choose a method to upload your profile picture",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                textAlign = TextAlign.Center
                            )

                            // Camera Button
                            Button(
                                onClick = {
                                    if (hasCameraPermission) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = "Camera",
                                    modifier = Modifier.padding(end = 8.dp),
                                    tint = Color.White
                                )
                                Text("Camera", fontSize = 16.sp, color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Gallery Button
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_gallery),
                                    contentDescription = "Gallery",
                                    modifier = Modifier.padding(end = 8.dp),
                                    tint = Color.White
                                )
                                Text("Gallery", fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = "Logout",
                    tint = Color(0xFFDD3825),
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = "Log Out",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Message
                Text(
                    text = "Are you sure you want to log out of\nyour account?",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Log Out button
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDD3825)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Log Out",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Cancel button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFABABAB) // Lighter gray color
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    profilePicture: String? = null,
    onProfilePictureClick: ((Uri) -> Unit)? = null,
    onCameraCapture: ((Bitmap) -> Unit)? = null
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            onCameraCapture?.invoke(it)
            showUploadDialog = false
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && onProfilePictureClick != null) {
            onProfilePictureClick(uri)
            showUploadDialog = false
        }
    }

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
            // Profile picture with camera icon overlay
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile picture or default icon
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color.LightGray
                ) {
                    if (profilePicture != null) {
                        // Display the profile picture using Coil with proper caching
                        val cacheVersion = ImageCache.profileImageVersion.collectAsState().value
                        key(profilePicture, cacheVersion) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Always show the person icon first as a placeholder
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.DarkGray,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .align(Alignment.Center)
                                )
                                
                                // Load the actual profile image on top
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageCache.createProfileImageRequest(
                                            context = context, 
                                            url = profilePicture
                                        ),
                                        onSuccess = { Log.d("ProfileHeader", "Profile image loaded successfully: $profilePicture") }
                                    ),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        // Default profile icon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        )
                    }
                }
                
                // Camera icon overlay for changing profile picture
                if (onProfilePictureClick != null) {
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .clickable { showUploadDialog = true },
                        shape = CircleShape,
                        color = Color.Black
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                fontSize = 20.sp,
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

    // Upload Dialog
    if (showUploadDialog) {
        Dialog(onDismissRequest = { showUploadDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Upload Profile Photo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        "Choose a method to upload your profile picture",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    // Camera Button
                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = "Camera",
                            modifier = Modifier.padding(end = 8.dp),
                            tint = Color.White
                        )
                        Text("Camera", fontSize = 16.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gallery Button
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_gallery),
                            contentDescription = "Gallery",
                            modifier = Modifier.padding(end = 8.dp),
                            tint = Color.White
                        )
                        Text("Gallery", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
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