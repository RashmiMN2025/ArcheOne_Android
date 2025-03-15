package com.archeGlobal.one
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.archeGlobal.one.model.SosBlogModel
import androidx.activity.compose.setContent
import com.archeGlobal.one.ui.screens.SOSDetailScreen

class SOSDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blog: SosBlogModel? = intent.getParcelableExtra("blog")

        setContent {
            blog?.let {
                SOSDetailScreen(blog = it, onBackPressed = { finish() })
            }
        }
    }
}
