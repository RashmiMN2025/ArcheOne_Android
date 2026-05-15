package com.archeGlobal.one.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.ProfileController
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.model.ProfileMenuItem
import com.archeGlobal.one.ui.components.FooterScaffold
import com.archeGlobal.one.ui.preview.PreviewNavigator
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.ImageCache

@Composable
fun ProfileScreen(
    controller: ProfileController,
    modifier: Modifier = Modifier,
    footerNavigation: FooterNavigationModel = FooterNavigationModel(showProfile = true),
    onFooterHomeClick: () -> Unit = { controller.onBackPressed() },
    onFooterChatClick: () -> Unit = {},
    onFooterHeadsUpClick: () -> Unit = {},
    onFooterSOSClick: () -> Unit = {},
    onFooterProfileClick: () -> Unit = {},
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userDataManager = com.archeGlobal.one.utils.UserDataManager.getInstance(context)

    // Ensure profile data is loaded when screen is displayed
    LaunchedEffect(Unit) {
        Log.d("ProfileScreen", "ProfileScreen composed - ensuring data is loaded")
        controller.onServiceAccessed()
    }

    BackHandler(enabled = true) {
        // Handle back press manually
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview(),
        ) { bitmap ->
            bitmap?.let {
                controller.uploadProfilePhoto(bitmap)
                showUploadDialog = false
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasCameraPermission = isGranted
            if (isGranted) {
                cameraLauncher.launch(null)
            } else {
                Toast
                    .makeText(
                        context,
                        "Camera permission is required to use camera",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                controller.uploadProfilePhotoFromUri(uri)
                showUploadDialog = false
            }
        }

    val avatarMakerLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val data = result.data
            val imagePath = data?.getStringExtra(
                com.archeGlobal.one.AvatarMakerActivity.EXTRA_IMAGE_PATH,
            )
            val filePath = data?.getStringExtra(
                com.archeGlobal.one.AvatarMakerActivity.EXTRA_FILE_PATH,
            )
            when {
                result.resultCode == Activity.RESULT_OK && !imagePath.isNullOrEmpty() -> {
                    controller.uploadProfilePhotoFromUri(Uri.fromFile(java.io.File(imagePath)))
                }
                result.resultCode == Activity.RESULT_OK && !filePath.isNullOrEmpty() -> {
                    controller.updateProfilePictureFromUrl(filePath)
                }
                else -> {
                    // Webview may have uploaded directly to the same backend URL.
                    // Force-refresh so Coil re-fetches the image content.
                    controller.refreshProfilePicture()
                }
            }
            showUploadDialog = false
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
    ) {
        FooterScaffold(
            footerNavigation = footerNavigation,
            onFooterHomeClick = onFooterHomeClick,
            onFooterChatClick = onFooterChatClick,
            onFooterHeadsUpClick = onFooterHeadsUpClick,
            onFooterSOSClick = onFooterSOSClick,
            onFooterProfileClick = onFooterProfileClick,
            headsUpCount = userDataManager.getHeadsUpCount(),
        ) {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color(0xFFE0DCD1),
                                            Color(0xFFC8C8CA),
                                            Color(0xFF474749),
                                        ),
                                ),
                        ),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfileHeader(
                        name = controller.model.name,
                        email = controller.model.email,
                        profilePicture = controller.model.profilePicture,
                        onProfilePictureClick = { uri ->
                            controller.onProfilePictureClick(uri)
                        },
                        onCameraCapture = { bitmap ->
                            controller.uploadProfilePhoto(bitmap)
                        },
                        onDeleteProfilePhoto = {
                            controller.deleteProfilePhoto()
                            ImageCache.invalidateProfileImageCache() // Invalidate cache
                        },
                        onAvatarCreated = { filePath ->
                            controller.updateProfilePictureFromUrl(filePath)
                        },
                        onAvatarMakerClosed = {
                            controller.refreshProfilePicture()
                        },
                    )

                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        MenuItems(
                            items = controller.model.menuItems,
                            onItemClick = { title ->
                                when (title) {
                                    "About Me" -> controller.onAboutMeClick()
                                    "Address/Coordinates" -> controller.onAddressClick()
                                    "Emergency Contact" -> controller.onEmergencyContactClick()
                                    "Documents" -> controller.onDocumentsClick()
                                    "Log out" -> showLogoutDialog = true
                                }
                            },
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 16.dp),
                        ) {
                            Text(
                                text = controller.model.version,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                            )

                            if (controller.model.lastLoginTime.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Last Login: ${controller.model.lastLoginTime}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                        }
                    }

                    if (showLogoutDialog) {
                        LogoutConfirmationDialog(
                            onConfirm = {
                                showLogoutDialog = false
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                controller.onLogoutClick()
                            },
                            onDismiss = { showLogoutDialog = false },
                        )
                    }
                }

                if (showUploadDialog) {
                    ProfilePictureUploadDialog(
                        profilePicture = controller.model.profilePicture,
                        onCameraClick = {
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onGalleryClick = { galleryLauncher.launch("image/*") },
                        onCreateAvatarClick = {
                            val userData = userDataManager.getUserData()
                            val intent = Intent(context, com.archeGlobal.one.AvatarMakerActivity::class.java).apply {
                                putExtra("email", userData?.email.orEmpty())
                                putExtra("employeeId", userData?.employeeId.orEmpty())
                            }
                            avatarMakerLauncher.launch(intent)
                        },
                        onDeleteClick = {
                            controller.deleteProfilePhoto()
                            ImageCache.invalidateProfileImageCache() // Invalidate cache
                            showUploadDialog = false
                        },
                        onDismiss = { showUploadDialog = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(1f),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF6F4EE),
                tonalElevation = 8.dp,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 30.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout1),
                        contentDescription = "Logout",
                        tint = Color(0xFFDD3825),
                        modifier = Modifier.size(32.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Log Out",
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Are you sure you want to log out of your account?",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onConfirm,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(48.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825),
                                ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = "Log Out",
                                color = Color.White,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                            )
                        }

                        Button(
                            onClick = onDismiss,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(48.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFABABAB),
                                ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
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
    onCameraCapture: ((Bitmap) -> Unit)? = null,
    onDeleteProfilePhoto: (() -> Unit)? = null,
    onAvatarCreated: ((String) -> Unit)? = null,
    onAvatarMakerClosed: (() -> Unit)? = null,
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userDataManager = com.archeGlobal.one.utils.UserDataManager.getInstance(context)

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview(),
        ) { bitmap ->
            bitmap?.let {
                onCameraCapture?.invoke(it)
                showUploadDialog = false
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasCameraPermission = isGranted
            if (isGranted) {
                cameraLauncher.launch(null)
            } else {
                Toast.makeText(context, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show()
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            if (uri != null && onProfilePictureClick != null) {
                onProfilePictureClick(uri)
                showUploadDialog = false
            }
        }

    val avatarMakerLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val data = result.data
            val imagePath = data?.getStringExtra(
                com.archeGlobal.one.AvatarMakerActivity.EXTRA_IMAGE_PATH,
            )
            val filePath = data?.getStringExtra(
                com.archeGlobal.one.AvatarMakerActivity.EXTRA_FILE_PATH,
            )
            when {
                result.resultCode == Activity.RESULT_OK && !imagePath.isNullOrEmpty() -> {
                    // Webview returned raw image bytes - upload through the same
                    // /upload_profile endpoint Gallery/Camera use.
                    onProfilePictureClick?.invoke(Uri.fromFile(java.io.File(imagePath)))
                }
                result.resultCode == Activity.RESULT_OK && !filePath.isNullOrEmpty() -> {
                    // Webview already uploaded - just apply the resulting URL.
                    onAvatarCreated?.invoke(filePath)
                }
                else -> {
                    // Webview may have uploaded directly without notifying the bridge.
                    // Invalidate the Coil cache so the (possibly same) URL is re-fetched.
                    onAvatarMakerClosed?.invoke()
                }
            }
            showUploadDialog = false
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)),
    ) {
        Image(
            painter = painterResource(id = R.drawable.header_home),
            contentDescription = "Header Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(top = 24.dp)
                        .size(110.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = Color.LightGray,
                ) {
                    if (profilePicture.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = Color.DarkGray,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                        )
                    } else {
                        val cacheVersion = ImageCache.profileImageVersion.value
                        key(profilePicture, cacheVersion) {
                            Image(
                                painter =
                                    rememberAsyncImagePainter(
                                        model =
                                            ImageCache.createProfileImageRequest(
                                                context = LocalContext.current,
                                                url = profilePicture,
                                            ),
                                        onSuccess = { Log.d("ProfileHeader", "Profile image loaded successfully: $profilePicture") },
                                        onError = { Log.e("ProfileHeader", "Failed to load profile image: $profilePicture") },
                                    ),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }

                if (onProfilePictureClick != null) {
                    Surface(
                        modifier =
                            Modifier
                                .size(30.dp)
                                .align(Alignment.BottomEnd)
                                .clickable { showUploadDialog = true },
                        shape = CircleShape,
                        color = Color.Black,
                        border = BorderStroke(1.5.dp, Color.White),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.padding(5.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                fontSize = 22.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )

            Text(
                text = email,
                fontSize = 16.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color.White,
            )
        }
    }

    if (showUploadDialog) {
        ProfilePictureUploadDialog(
            profilePicture = profilePicture,
            onCameraClick = {
                if (hasCameraPermission) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGalleryClick = { galleryLauncher.launch("image/*") },
            onCreateAvatarClick = {
                val userData = userDataManager.getUserData()
                val intent = Intent(context, com.archeGlobal.one.AvatarMakerActivity::class.java).apply {
                    putExtra("email", userData?.email.orEmpty())
                    putExtra("employeeId", userData?.employeeId.orEmpty())
                }
                avatarMakerLauncher.launch(intent)
            },
            onDeleteClick = {
                onDeleteProfilePhoto?.invoke()
                ImageCache.invalidateProfileImageCache() // Invalidate cache
                showUploadDialog = false
            },
            onDismiss = { showUploadDialog = false },
        )
    }
}

@Composable
fun ProfilePictureUploadDialog(
    profilePicture: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
    onDismiss: () -> Unit,
    onCreateAvatarClick: (() -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 14.dp),
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                elevation = CardDefaults.cardElevation(8.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (!profilePicture.isNullOrEmpty()) "Edit Profile Photo" else "Upload Profile Photo",
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center,
                    )

                    Box(
                        modifier =
                            Modifier
                                .size(110.dp)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (profilePicture.isNullOrEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                tint = Color.DarkGray,
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                        .clip(CircleShape),
                            )
                        } else {
                            Image(
                                painter =
                                    rememberAsyncImagePainter(
                                        model =
                                            ImageCache.createProfileImageRequest(
                                                context = LocalContext.current,
                                                url = profilePicture,
                                            ),
                                        onSuccess = {
                                            Log.d(
                                                "ProfilePictureUploadDialog",
                                                "Profile image loaded successfully: $profilePicture",
                                            )
                                        },
                                        onError = { Log.e("ProfilePictureUploadDialog", "Failed to load profile image: $profilePicture") },
                                    ),
                                contentDescription = "Profile Picture",
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }

                    Text(
                        text = if (!profilePicture.isNullOrEmpty()) "Choose a method to edit your profile picture" else "Choose a method to upload your profile picture",
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = onGalleryClick,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_gallery),
                                contentDescription = "Gallery",
                                modifier = Modifier.padding(end = 8.dp),
                                tint = Color.White,
                            )
                            Text(
                                text = "Gallery",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Button(
                            onClick = onCameraClick,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "Camera",
                                modifier = Modifier.padding(end = 8.dp),
                                tint = Color.White,
                            )
                            Text(
                                text = "Camera",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    if (onCreateAvatarClick != null) {
                        Button(
                            onClick = onCreateAvatarClick,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Create Avatar",
                                modifier = Modifier.padding(end = 8.dp),
                                tint = Color.White,
                            )
                            Text(
                                text = "Create Avatar",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    if (!profilePicture.isNullOrEmpty()) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Button(
                                onClick = { onDeleteClick?.invoke() },
                                modifier =
                                    Modifier
                                        .width(125.dp)
                                        .height(40.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE0B4AA),
                                        contentColor = Color(0xFFDD3825),
                                        disabledContainerColor = Color(0xFFE0B4AA),
                                        disabledContentColor = Color(0xFFDD3825),
                                    ),
                                border = BorderStroke(1.dp, Color(0xFFDD3825)),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete),
                                    contentDescription = "Delete",
                                    tint = Color(0xFFDD3825),
                                    modifier = Modifier.size(22.dp),
                                )
                                Text(
                                    text = "Delete",
                                    fontSize = 11.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFDD3825),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier =
    composed {
        this.then(
            Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = enabled,
                onClick = onClick,
            ),
        )
    }

@Composable
private fun MenuItems(
    items: List<ProfileMenuItem>,
    onItemClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items.forEach { item ->
            MenuItem(
                title = item.title,
                icon = getIconForMenuItem(item.icon),
                onClick = { onItemClick(item.title) },
            )
        }
    }
}

@Composable
private fun MenuItem(
    title: String,
    icon: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
        color = Color.White,
    ) {
        Row(
            modifier =
                Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = Color.Black,
                    modifier = Modifier.size(25.dp),
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(25.dp),
            )
        }
    }
}

fun getIconForMenuItem(icon: String): Int =
    when (icon) {
        "person" -> R.drawable.ic_user
        "home" -> R.drawable.ic_home1
        "phone" -> R.drawable.ic_call
        "document" -> R.drawable.ic_doc
        "logout" -> R.drawable.ic_logout1
        else -> R.drawable.arche_black2
    }

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val previewController =
        ProfileController(
            context = LocalContext.current,
            navigator = PreviewNavigator(),
        )

    ProfileScreen(controller = previewController)
}
