
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.archeGlobal.one.model.AuthResponse

@Composable
fun MicrosoftLoginWebView(
    url: String,
    onReceiveAuth: (AuthResponse) -> Unit,
    onClose: () -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val uri = request?.url ?: return false
                        val urlString = uri.toString()
                        Log.d("MicrosoftLoginWebView", "Intercepted URL: $urlString")
                        if (urlString.startsWith("https://archeone.arche.global/mfaCallback")) {
                            val params = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) ?: "" }
                            if (
                                params["message"] == "Authenticated" &&
                                params["token"] != null &&
                                params["email"] != null &&
                                params["employeeId"] != null &&
                                params["mobilePhone"] != null
                            ) {
                                val authResponse = AuthResponse(
                                    message = params["message"]!!,
                                    token = params["token"]!!,
                                    email = params["email"]!!,
                                    employeeId = params["employeeId"]!!,
                                    mobilePhone = params["mobilePhone"]!!
                                )
                                onReceiveAuth(authResponse)
                                onClose()
                                return true // Prevent WebView from loading this URL
                            }
                        }
                        return false
                    }
                }
                loadUrl(url)
            }
        }
    )
}
