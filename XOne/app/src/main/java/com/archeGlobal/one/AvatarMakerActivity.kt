package com.archeGlobal.one

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import java.io.File
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager
import org.json.JSONObject
import java.net.URLEncoder

class AvatarMakerActivity : ComponentActivity() {
    companion object {
        const val EXTRA_FILE_PATH = "filePath"
        const val EXTRA_IMAGE_PATH = "imagePath"
        const val EXTRA_MESSAGE = "message"
        private const val TAG = "AvatarMakerActivity"
        private const val BASE_URL = "https://archeforever.archelabs.com"

        // Injected after page load to detect successful upload-style requests the page
        // makes via fetch / XMLHttpRequest. On a 2xx response to a URL matching
        // upload/avatar/profile/save, calls AndroidBridge.uploadSuccess() so the activity
        // closes automatically — the page itself doesn't notify us today.
        private val AVATAR_NETWORK_HOOK_JS = """
            (function() {
              if (window.__archeAvatarHook) return;
              window.__archeAvatarHook = true;
              function looksLikeUpload(url) {
                if (!url) return false;
                var u = String(url).toLowerCase();
                return u.indexOf('upload') >= 0 ||
                       u.indexOf('avatar') >= 0 ||
                       u.indexOf('profile') >= 0 ||
                       u.indexOf('save') >= 0;
              }
              function notifySuccess() {
                try {
                  if (window.AndroidBridge && window.AndroidBridge.uploadSuccess) {
                    window.AndroidBridge.uploadSuccess();
                  }
                } catch (e) {}
              }
              if (window.fetch) {
                var origFetch = window.fetch.bind(window);
                window.fetch = function(input, init) {
                  var url = (typeof input === 'string') ? input : (input && input.url) || '';
                  var method = (init && init.method) || (input && input.method) || 'GET';
                  var p = origFetch(input, init);
                  if (looksLikeUpload(url) && String(method).toUpperCase() !== 'GET') {
                    p.then(function(res) { if (res && res.ok) notifySuccess(); }).catch(function(){});
                  }
                  return p;
                };
              }
              try {
                var origOpen = XMLHttpRequest.prototype.open;
                var origSend = XMLHttpRequest.prototype.send;
                XMLHttpRequest.prototype.open = function(method, url) {
                  this.__avMethod = method;
                  this.__avUrl = url;
                  return origOpen.apply(this, arguments);
                };
                XMLHttpRequest.prototype.send = function() {
                  var xhr = this;
                  if (looksLikeUpload(xhr.__avUrl) && String(xhr.__avMethod || 'GET').toUpperCase() !== 'GET') {
                    xhr.addEventListener('load', function() {
                      if (xhr.status >= 200 && xhr.status < 300) notifySuccess();
                    });
                  }
                  return origSend.apply(this, arguments);
                };
              } catch (e) {}
            })();
        """.trimIndent()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userDataManager = UserDataManager.getInstance(this)
        val userData = userDataManager.getUserData()
        val email = intent.getStringExtra("email") ?: userData?.email.orEmpty()
        val employeeId = intent.getStringExtra("employeeId") ?: userData?.employeeId.orEmpty()

        val avatarUrl = buildString {
            append(BASE_URL)
            append("?email=").append(URLEncoder.encode(email, "UTF-8"))
            append("&employeeId=").append(URLEncoder.encode(employeeId, "UTF-8"))
        }
        Log.d(TAG, "Loading avatar maker URL: $avatarUrl")

        setContent {
            XOneTheme(darkTheme = false, dynamicColor = false) {
                var isLoading by remember { mutableStateOf(true) }

                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Top tap-to-dismiss strip - profile screen peeks through here.
                    // Dimmed slightly so the sheet has visual focus, matching iOS.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color.Black.copy(alpha = 0.20f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { finish() },
                    )

                    // Sheet body
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                        ) {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 8.dp),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                                setBackgroundColor(android.graphics.Color.WHITE)

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    loadsImagesAutomatically = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                    mediaPlaybackRequiresUserGesture = false
                                    javaScriptCanOpenWindowsAutomatically = true
                                    setSupportMultipleWindows(true)
                                    setSupportZoom(false)
                                    builtInZoomControls = false
                                    displayZoomControls = false
                                }

                                // Force light mode so the page does not pick up `prefers-color-scheme: dark`
                                // when the device is in dark mode (page renders as black otherwise).
                                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, false)
                                }
                                @Suppress("DEPRECATION")
                                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                                    WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
                                }

                                addJavascriptInterface(
                                    AvatarBridge(
                                        onResult = { filePath, message ->
                                            handleAvatarResult(filePath, message)
                                        },
                                        onImage = { imagePath ->
                                            handleAvatarImage(imagePath)
                                        },
                                        onClose = { runOnUiThread { finish() } },
                                    ),
                                    "AndroidBridge",
                                )

                                webChromeClient = object : WebChromeClient() {
                                    override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                                        Log.d(
                                            TAG,
                                            "WebView console: ${message?.message()} @ ${message?.sourceId()}:${message?.lineNumber()}",
                                        )
                                        return true
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    private var pageStartMs = 0L

                                    override fun onPageStarted(
                                        view: WebView?,
                                        url: String?,
                                        favicon: android.graphics.Bitmap?,
                                    ) {
                                        super.onPageStarted(view, url, favicon)
                                        pageStartMs = System.currentTimeMillis()
                                        Log.d(TAG, "[AvatarLoad] onPageStarted: $url")
                                        isLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        val elapsed = if (pageStartMs > 0) System.currentTimeMillis() - pageStartMs else -1
                                        Log.d(TAG, "[AvatarLoad] onPageFinished: $url (took ${elapsed}ms)")
                                        isLoading = false
                                        // Inject a network-hook that closes the activity when the
                                        // page completes an upload-style POST/PUT. The avatar page
                                        // uploads via its own fetch/XHR without notifying us, so we
                                        // wrap both APIs and call AndroidBridge.uploadSuccess()
                                        // on a 2xx response to an "upload/avatar/profile/save" URL.
                                        view?.evaluateJavascript(AVATAR_NETWORK_HOOK_JS, null)
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?,
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        val isMainFrame = request?.isForMainFrame == true
                                        Log.e(
                                            TAG,
                                            "[AvatarLoad] onReceivedError mainFrame=$isMainFrame code=${error?.errorCode} desc='${error?.description}' url=${request?.url}",
                                        )
                                        if (isMainFrame) isLoading = false
                                    }

                                    override fun onReceivedHttpError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        errorResponse: android.webkit.WebResourceResponse?,
                                    ) {
                                        super.onReceivedHttpError(view, request, errorResponse)
                                        Log.e(
                                            TAG,
                                            "[AvatarLoad] onReceivedHttpError status=${errorResponse?.statusCode} reason='${errorResponse?.reasonPhrase}' url=${request?.url}",
                                        )
                                    }

                                    override fun onReceivedSslError(
                                        view: WebView?,
                                        handler: SslErrorHandler?,
                                        error: SslError?,
                                    ) {
                                        // ---- Detailed SSL diagnostics for the backend chain-fix verification ----
                                        // If this block fires, the server's TLS chain failed validation.
                                        // The most common cause for archeforever.archelabs.com is a missing
                                        // intermediate certificate (Sectigo CA DV R36). After the backend
                                        // ships the full-chain fix, this method should stop being called.
                                        val errorName = when (error?.primaryError) {
                                            SslError.SSL_NOTYETVALID -> "SSL_NOTYETVALID (cert not yet valid)"
                                            SslError.SSL_EXPIRED -> "SSL_EXPIRED (cert expired)"
                                            SslError.SSL_IDMISMATCH -> "SSL_IDMISMATCH (hostname mismatch)"
                                            SslError.SSL_UNTRUSTED -> "SSL_UNTRUSTED (chain not trusted — likely missing intermediate)"
                                            SslError.SSL_DATE_INVALID -> "SSL_DATE_INVALID (cert date invalid)"
                                            SslError.SSL_INVALID -> "SSL_INVALID (generic)"
                                            else -> "UNKNOWN(${error?.primaryError})"
                                        }
                                        Log.e(TAG, "[AvatarSSL] ============ SSL ERROR ============")
                                        Log.e(TAG, "[AvatarSSL] URL:        ${error?.url}")
                                        Log.e(TAG, "[AvatarSSL] Error:      $errorName")
                                        val cert = error?.certificate
                                        if (cert != null) {
                                            val issued = cert.issuedTo
                                            val issuer = cert.issuedBy
                                            Log.e(TAG, "[AvatarSSL] IssuedTo CN: ${issued?.cName}")
                                            Log.e(TAG, "[AvatarSSL] IssuedTo O:  ${issued?.oName}")
                                            Log.e(TAG, "[AvatarSSL] IssuedTo DN: ${issued?.dName}")
                                            Log.e(TAG, "[AvatarSSL] Issuer CN:   ${issuer?.cName}")
                                            Log.e(TAG, "[AvatarSSL] Issuer O:    ${issuer?.oName}")
                                            Log.e(TAG, "[AvatarSSL] Issuer DN:   ${issuer?.dName}")
                                            Log.e(TAG, "[AvatarSSL] Valid from:  ${cert.validNotBeforeDate}")
                                            Log.e(TAG, "[AvatarSSL] Valid to:    ${cert.validNotAfterDate}")
                                        } else {
                                            Log.e(TAG, "[AvatarSSL] (no certificate object)")
                                        }
                                        Log.e(TAG, "[AvatarSSL] Hint: run `openssl s_client -connect archeforever.archelabs.com:443` —")
                                        Log.e(TAG, "[AvatarSSL]   Verify return code != 0 means the server isn't sending the full chain.")
                                        Log.e(TAG, "[AvatarSSL] ===================================")
                                        handler?.cancel()
                                        isLoading = false
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                    ): Boolean {
                                        val uri: Uri = request?.url ?: return false
                                        return interceptCallbackUrl(uri)
                                    }
                                }

                                loadUrl(avatarUrl)
                            }
                        },
                    )

                            // Close pill in the top-left of the sheet (like the iOS "Close" button).
                            Surface(
                                modifier = Modifier
                                    .padding(start = 14.dp, top = 14.dp)
                                    .size(36.dp)
                                    .align(Alignment.TopStart),
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 3.dp,
                            ) {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.Black,
                                    )
                                }
                            }
                        }
                    }

                    UniversalLoader(isLoading = isLoading)
                }
            }
        }
    }

    private fun interceptCallbackUrl(uri: Uri): Boolean {
        val response = uri.getQueryParameter("response") ?: return false
        return try {
            val json = JSONObject(response)
            parseAndFinish(json)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse callback URL response: ${e.message}")
            false
        }
    }

    private fun handleAvatarResult(filePath: String?, message: String?) {
        runOnUiThread {
            if (!filePath.isNullOrEmpty()) {
                val result = Intent().apply {
                    putExtra(EXTRA_FILE_PATH, filePath)
                    putExtra(EXTRA_MESSAGE, message ?: "Profile picture uploaded successfully")
                }
                setResult(RESULT_OK, result)
            }
            finish()
        }
    }

    private fun handleAvatarImage(imagePath: String) {
        runOnUiThread {
            val result = Intent().apply {
                putExtra(EXTRA_IMAGE_PATH, imagePath)
            }
            setResult(RESULT_OK, result)
            finish()
        }
    }

    /**
     * Decode a base64-encoded avatar image (with or without a `data:` URI prefix)
     * and save it to the cache directory. Returns the absolute file path, or null
     * if decoding failed.
     */
    private fun saveBase64AsImage(base64Image: String): String? {
        val payload = if (base64Image.startsWith("data:") && base64Image.contains(",")) {
            base64Image.substringAfter(",")
        } else {
            base64Image
        }
        return try {
            val bytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT)
            val ext = when {
                base64Image.startsWith("data:image/png") -> "png"
                base64Image.startsWith("data:image/jpeg") -> "jpg"
                else -> "png"
            }
            val file = File(cacheDir, "avatar_${System.currentTimeMillis()}.$ext")
            file.writeBytes(bytes)
            Log.d(TAG, "Saved avatar to ${file.absolutePath} (${bytes.size} bytes)")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode/save base64 avatar: ${e.message}", e)
            null
        }
    }

    private fun parseAndFinish(json: JSONObject) {
        val status = json.optInt("status", 0)
        val message = json.optString("message")
        val filePath = json.optString("filePath").takeIf { it.isNotEmpty() }
        // Match the lenient success detection used by the JS bridge.
        val isSuccess = status == 200 ||
            json.optBoolean("success", false) ||
            message.contains("success", ignoreCase = true) ||
            message.contains("uploaded", ignoreCase = true)
        when {
            !filePath.isNullOrEmpty() && isSuccess -> handleAvatarResult(filePath, message)
            isSuccess -> runOnUiThread { finish() }
            else -> Log.w(TAG, "Avatar response ignored - status=$status filePath=$filePath message=$message")
        }
    }

    inner class AvatarBridge(
        private val onResult: (String?, String?) -> Unit,
        private val onImage: (String) -> Unit,
        private val onClose: () -> Unit,
    ) {
        /**
         * Generic message bridge. Supports two payload shapes:
         *  1. {"status":200,"filePath":"..."} - already uploaded URL (apply directly)
         *  2. {"imageData":"<base64-or-data-url>"} - raw image bytes, native uploads
         *     via the same /upload_profile endpoint Gallery/Camera use.
         * A bare base64 string (no JSON) is also treated as case 2.
         */
        @JavascriptInterface
        fun postMessage(payload: String) {
            Log.d(TAG, "AndroidBridge.postMessage received (${payload.length} chars)")
            val trimmed = payload.trim()
            if (trimmed.isEmpty()) return

            if (trimmed.startsWith("{")) {
                try {
                    val obj = JSONObject(trimmed)
                    val imageData = obj.optString("imageData").takeIf { it.isNotEmpty() }
                        ?: obj.optString("image").takeIf { it.isNotEmpty() }
                        ?: obj.optString("base64").takeIf { it.isNotEmpty() }
                    if (imageData != null) {
                        forwardImage(imageData)
                        return
                    }
                    val status = obj.optInt("status", 0)
                    val filePath = obj.optString("filePath").takeIf { it.isNotEmpty() }
                    val message = obj.optString("message")
                    // Treat any truthy success signal as "upload done → close" — covers
                    // {status:200}, {success:true}, {status:200,filePath:…}, and pages that
                    // upload server-side and just notify with a success message.
                    val isSuccess = status == 200 ||
                        obj.optBoolean("success", false) ||
                        message.contains("success", ignoreCase = true) ||
                        message.contains("uploaded", ignoreCase = true)
                    when {
                        !filePath.isNullOrEmpty() && isSuccess -> onResult(filePath, message)
                        isSuccess -> onClose()
                        else -> Log.w(TAG, "Avatar bridge JSON ignored - status=$status success=${obj.optBoolean("success", false)} filePath=$filePath message=$message")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse bridge JSON: ${e.message}")
                }
            } else {
                forwardImage(trimmed)
            }
        }

        @JavascriptInterface
        fun onProfilePicUploaded(json: String) = postMessage(json)

        /** Direct entry point: webview sends a base64 / data-URL image, native uploads it. */
        @JavascriptInterface
        fun uploadAvatar(base64Image: String) {
            Log.d(TAG, "AndroidBridge.uploadAvatar received (${base64Image.length} chars)")
            forwardImage(base64Image)
        }

        @JavascriptInterface
        fun close() {
            Log.d(TAG, "AndroidBridge.close called")
            onClose()
        }

        /** Explicit "I just uploaded the avatar, close me" signal — pages can call this
         *  after their own server upload completes. ProfileScreen's launcher refreshes
         *  the picture from cache when no extras are attached. */
        @JavascriptInterface
        fun onUploadComplete() {
            Log.d(TAG, "AndroidBridge.onUploadComplete called")
            onClose()
        }

        @JavascriptInterface
        fun uploadSuccess() {
            Log.d(TAG, "AndroidBridge.uploadSuccess called")
            onClose()
        }

        private fun forwardImage(base64Image: String) {
            val path = saveBase64AsImage(base64Image)
            if (path != null) {
                onImage(path)
            } else {
                Log.e(TAG, "Avatar bridge: failed to save image bytes")
            }
        }
    }
}
