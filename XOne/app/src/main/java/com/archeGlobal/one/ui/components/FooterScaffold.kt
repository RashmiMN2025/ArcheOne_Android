package com.archeGlobal.one.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.archeGlobal.one.model.FooterNavigationModel

@Composable
fun FooterScaffold(
    footerNavigation: FooterNavigationModel,
    onFooterHomeClick: () -> Unit,
    onFooterChatClick: () -> Unit,
    onFooterSOSClick: () -> Unit,
    onFooterProfileClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                model = footerNavigation,
                onHomeClick = onFooterHomeClick,
                onChatClick = onFooterChatClick,
                onSOSClick = onFooterSOSClick,
                onProfileClick = onFooterProfileClick,
                modifier = Modifier.zIndex(1f) // Ensure navigation bar is always on top
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
} 