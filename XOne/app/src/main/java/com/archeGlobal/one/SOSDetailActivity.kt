package com.archeGlobal.one
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.model.SosBlogModel
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.SOSDetailScreen

class SOSDetailActivity : ComponentActivity() {
    private lateinit var navigator: AndroidNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Use the version-compatible way to get parcelable extra
        val blog =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                )
            }
        }
    }
}
