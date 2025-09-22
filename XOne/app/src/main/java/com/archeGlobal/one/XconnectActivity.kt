package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.archeGlobal.one.controller.SocialController
import com.archeGlobal.one.controller.SocialDataProvider
import com.archeGlobal.one.model.SocialArticle
import com.archeGlobal.one.ui.screens.ArticleDetailScreen
import com.archeGlobal.one.ui.screens.XConnectScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import kotlinx.coroutines.delay

class XConnectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Get the initial tab selection from intent, defaulting to "All Posts" if not specified
        val initialTab =
            if (intent.hasExtra("initialTab")) {
                intent.getStringExtra("initialTab")
            } else {
                "All Posts"
            } ?: "All Posts"

        // Start preloading data immediately when activity is created
        SocialDataProvider.getInstance(applicationContext).preloadData()

        setContent {
            XOneTheme {
                var isDataReady by remember { mutableStateOf(false) }
                var showLoading by remember { mutableStateOf(true) }

                // Navigation state
                var currentScreen by remember { mutableStateOf<Screen>(Screen.XConnect) }
                var selectedArticle by remember { mutableStateOf<SocialArticle?>(null) }
                var selectedArticleType by remember { mutableStateOf("") }

                // Social controller for handling web view navigation
                val socialController = remember { SocialController(this) }

                // Check if data is already preloaded
                val dataProvider = SocialDataProvider.getInstance(applicationContext)

                LaunchedEffect(key1 = true) {
                    // If data is already loaded, show content immediately
                    if (dataProvider.isLoaded) {
                        isDataReady = true
                        showLoading = false
                    } else {
                        // Wait for a short time to see if data becomes available quickly
                        delay(500)

                        // Check if data is ready now
                        if (dataProvider.isLoaded) {
                            isDataReady = true
                            showLoading = false
                        } else {
                            // If data is still loading, show the UI anyway after a brief loading indicator
                            delay(1000)
                            showLoading = false
                        }
                    }
                }

                if (showLoading) {
                    // Show loading indicator
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    // Show content based on current screen
                    when (val screen = currentScreen) {
                        is Screen.XConnect -> {
                            XConnectScreen(
                                onBackPressed = { finish() },
                                onArticleSelected = { article, type ->
                                    selectedArticle = article
                                    selectedArticleType = type
                                    currentScreen = Screen.ArticleDetail
                                },
                                initialTab = initialTab,
                            )
                        }
                        is Screen.ArticleDetail -> {
                            selectedArticle?.let { article ->
                                ArticleDetailScreen(
                                    article = article,
                                    type = selectedArticleType,
                                    onBackPressed = {
                                        currentScreen = Screen.XConnect
                                    },
                                    onReadMore = {
                                        socialController.openInBrowser(selectedArticleType, article.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Make sure data is loading when activity is resumed
        SocialDataProvider.getInstance(applicationContext).preloadData()
    }
}

// Sealed class for navigation
sealed class Screen {
    object XConnect : Screen()

    object ArticleDetail : Screen()
}
