package com.archeGlobal.one
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import com.archeGlobal.one.model.SosBlogModel
import androidx.activity.compose.setContent
import com.archeGlobal.one.ui.screens.SOSDetailScreen
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.HomeActivity
import com.archeGlobal.one.SOSActivity
import android.os.Build

class SOSDetailActivity : ComponentActivity() {
    private lateinit var navigator: AndroidNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the version-compatible way to get parcelable extra
        val blog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("blog", SosBlogModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("blog") as? SosBlogModel
        }
        
        navigator = AndroidNavigator(this)

        setContent {
            blog?.let {
                SOSDetailScreen(
                    blog = it, 
                    onBackPressed = { finish() },
                    onFooterHomeClick = { 
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onFooterChatClick = {
                        // Navigate to Chat screen when available
                    },
                    onFooterSOSClick = {
                        // Already in SOS section, go back to main SOS screen
                        val intent = Intent(this, SOSActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onFooterProfileClick = {
                        navigator.navigateToXProfile()
                    }
                )
            }
        }
    }
}
