package com.example.xone.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.xone.model.FooterNavigationModel
import com.example.xone.ui.screens.FooterNavigation

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
            FooterNavigation(
                model = footerNavigation,
                onHomeClick = onFooterHomeClick,
                onChatClick = onFooterChatClick,
                onSOSClick = onFooterSOSClick,
                onProfileClick = onFooterProfileClick
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