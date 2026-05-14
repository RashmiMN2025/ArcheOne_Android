package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.DeletionRequestCreateBody
import com.archeGlobal.one.model.MyRequestItem
import com.archeGlobal.one.model.MyRequestsRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyRequestsController(private val context: Context) {

    var requests by mutableStateOf<List<MyRequestItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var fetchJob: Job? = null

    fun fetchRequests() {
        val employeeCode = UserDataManager.getInstance(context).getUserData()?.employeeId ?: return

        fetchJob?.cancel()
        fetchJob = CoroutineScope(Dispatchers.Main).launch {
            requests = emptyList()
            isLoading = true
            errorMessage = null
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMyRequests(MyRequestsRequest(employeeCode))
                }
                ensureActive()
                if (response.isSuccessful && response.body()?.success == true) {
                    requests = response.body()?.data?.requests ?: emptyList()
                } else {
                    errorMessage = response.body()?.message ?: "Failed to load requests"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    fun cancelRequest(eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.deleteRequest(eventId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Request cancelled successfully", Toast.LENGTH_SHORT).show()
                    }
                    delay(2000)
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    val msg = "Failed to cancel request (${response.code()})"
                    withContext(Dispatchers.Main) { onError(msg) }
                }
            } catch (e: Exception) {
                val msg = e.message ?: "An error occurred"
                withContext(Dispatchers.Main) { onError(msg) }
            }
        }
    }

    /**
     * Raise a deletion request for an already-approved item. The manager will
     * see this in their approval dashboard under the "deletion request" category
     * and either approve (which removes the original item) or reject (keeping it).
     */
    fun requestDeletion(
        eventId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val employeeCode = UserDataManager.getInstance(context).getUserData()?.employeeId.orEmpty()
            val body = DeletionRequestCreateBody(eventId = eventId, reason = reason)
            Log.d(TAG, "requestDeletion → POST /api/v1/timesheet/delete-approvals/create | X-Employee-Code=$employeeCode | body=$body")
            if (employeeCode.isBlank()) {
                Log.e(TAG, "requestDeletion aborted — no employee code in session")
                withContext(Dispatchers.Main) { onError("Your employee code is missing. Please log out and log in again.") }
                return@launch
            }
            try {
                val response = RetrofitClient.apiService.createDeletionRequest(employeeCode, body)
                Log.d(TAG, "requestDeletion ← HTTP ${response.code()} successful=${response.isSuccessful}")
                Log.d(TAG, "requestDeletion ← headers=${response.headers()}")
                val rawBody = response.body()
                val rawErrorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                Log.d(TAG, "requestDeletion ← body=$rawBody")
                if (rawErrorBody != null) {
                    Log.e(TAG, "requestDeletion ← errorBody=$rawErrorBody")
                }

                if (response.isSuccessful && rawBody?.success == true) {
                    val msg = rawBody.message.ifBlank { "Deletion request sent to approver" }
                    Log.d(TAG, "requestDeletion success: $msg | data=${rawBody.data}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        onSuccess()
                    }
                } else {
                    // Try to parse a message out of either body or errorBody
                    val parsedErrMsg = parseErrorMessage(rawErrorBody)
                    val msg = rawBody?.message?.takeIf { it.isNotBlank() }
                        ?: parsedErrMsg
                        ?: "Failed to raise deletion request (HTTP ${response.code()})"
                    Log.e(TAG, "requestDeletion failed | code=${response.code()} | msg=$msg")
                    withContext(Dispatchers.Main) { onError(msg) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "requestDeletion exception | ${e.javaClass.simpleName}: ${e.message}", e)
                val msg = e.message ?: "An error occurred"
                withContext(Dispatchers.Main) { onError(msg) }
            }
        }
    }

    private fun parseErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val json = org.json.JSONObject(raw)
            json.optString("message").takeIf { it.isNotBlank() }
                ?: json.optString("detail").takeIf { it.isNotBlank() }
                ?: json.optString("error").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            raw.take(200)
        }
    }

    companion object {
        private const val TAG = "MyRequestsCtrl"
        var selectedRequest: MyRequestItem? = null
    }
}
