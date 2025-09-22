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
        val prefilledCategory = intent.getStringExtra("prefilledCategory")

        val title = if (source == "asset") "Raise a Ticket" else "Raise a Concern"

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            XOneTheme {
                RaiseConcernScreen(
                    onBackPressed = {
                        if (source == "asset") {
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            finish()
                        }
                    },
                    title = title,
                    source = source,
                    prefilledCategory = prefilledCategory, // ✅ pass prefilled category
                )
            }
        }
    }
}
