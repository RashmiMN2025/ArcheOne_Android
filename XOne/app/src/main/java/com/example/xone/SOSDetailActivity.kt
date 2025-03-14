package com.example.xone
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.xone.model.SosBlogModel
import androidx.activity.compose.setContent
import com.example.xone.ui.screens.SOSDetailScreen

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
