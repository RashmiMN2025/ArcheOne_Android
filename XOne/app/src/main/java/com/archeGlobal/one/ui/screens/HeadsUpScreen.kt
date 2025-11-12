package com.archeGlobal.one.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.ui.components.FooterScaffold
import com.archeGlobal.one.ui.components.NewPostDialog
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.UserDataManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun HeadsUpScreen(
    footerNavigation: FooterNavigationModel,
    isUsingPrideIcon: Boolean = false,
    onFooterHomeClick: () -> Unit,
    onFooterChatClick: () -> Unit,
    onFooterHeadsUpClick: () -> Unit,
    onFooterSOSClick: () -> Unit,
    onFooterProfileClick: () -> Unit,
    onHistoryClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val userDataManager = UserDataManager.getInstance(context)
    val userData = userDataManager.getUserData()
    val profilePicUrl = userData?.profilePic
    val userName = userData?.name ?: "User"

    var showNewPostDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        FooterScaffold(
            footerNavigation = footerNavigation.copy(
                showHome = false,
                showChat = false,
                showHeadsUp = true,
                showSOS = false,
                showProfile = false
            ),
            isUsingPrideIcon = isUsingPrideIcon,
            onFooterHomeClick = onFooterHomeClick,
            onFooterChatClick = onFooterChatClick,
            onFooterHeadsUpClick = onFooterHeadsUpClick,
            onFooterSOSClick = onFooterSOSClick,
            onFooterProfileClick = onFooterProfileClick,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F4EE)),
                contentAlignment = Alignment.Center
            ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Icon
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFE8E3D9)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.headsup),
                            contentDescription = "No Posts",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF999999)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "No Posts Found",
                    fontSize = 24.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                Text(
                    text = "Create a post by clicking on New Post!",
                    fontSize = 16.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(200.dp))
            }

            // Floating Action Button at bottom right
            FloatingActionButton(
                onClick = { showNewPostDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 40.dp, end = 24.dp),
                containerColor = Color(0xFFDD3825),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "New Post",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Post",
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            }
        }

        // Show New Post Dialog
        if (showNewPostDialog) {
            NewPostDialog(
                profilePicUrl = profilePicUrl,
                userName = userName,
                onDismiss = { showNewPostDialog = false },
                onSubmit = { title, description, category ->
                    // TODO: Handle post submission
                    showNewPostDialog = false
                },
                onHistoryClick = onHistoryClick
            )
        }
    }
}
