package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.ui.screens.RaiseConcernScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class RaiseConcernActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the source from intent extras, default to "helpdesk" if not specified
        val source = intent.getStringExtra("source") ?: "helpdesk"

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            XOneTheme {
                RaiseConcernScreen(
                    onBackPressed = { // Return to the appropriate screen based on source
                        if (source == "asset") {
                            setResult(RESULT_OK)
                        }
                        finish()
                    }
                )
            }
        }
    }
}
