package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.CommuniqueActivity
import com.archeGlobal.one.model.CommuniqueModel
import com.archeGlobal.one.navigation.Navigator
import kotlinx.coroutines.*

class CommuniqueController(
    private val context: Context,
    private val navigator: Navigator,
) {
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _communiques = mutableStateOf<List<CommuniqueModel.Communique>>(emptyList())

    val model: CommuniqueModel
        get() = CommuniqueModel(communiques = _communiques.value)

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        loadCommuniques()
    }

    private fun loadCommuniques() {
        _isLoading.value = true

        coroutineScope.launch {
            try {
                // Get communiques data from user data (already loaded during login)
                val communiqueData = OtpVerificationController.getCommuniquesData()
                if (communiqueData != null) {
                    _communiques.value = communiqueData
                    Log.d("CommuniqueController", "Loaded ${communiqueData.size} communiques")
                } else {
                    Log.e("CommuniqueController", "No communiques data available")
                    Toast.makeText(context, "Failed to load communiques", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CommuniqueController", "Error loading communiques: ${e.message}", e)
                Toast.makeText(context, "Failed to load communiques", Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onCommuniqueClick(communique: CommuniqueModel.Communique) {
        // Native PDF rendering - much faster than the previous PDF.js WebView path.
        val intent =
            Intent(context, com.archeGlobal.one.PdfViewerActivity::class.java).apply {
                putExtra(com.archeGlobal.one.PdfViewerActivity.EXTRA_FILE_URL, communique.filePath)
                putExtra(com.archeGlobal.one.PdfViewerActivity.EXTRA_TITLE, communique.communiqueName)
            }
        context.startActivity(intent)
    }

    fun onBackPressed() {
        (context as? CommuniqueActivity)?.finishWithAnimation()
    }

    // Clean up resources when no longer needed
    fun onCleared() {
        coroutineScope.cancel()
    }
}
