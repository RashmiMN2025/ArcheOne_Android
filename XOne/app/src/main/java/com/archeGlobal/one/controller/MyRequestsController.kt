package com.archeGlobal.one.controller

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    companion object {
        var selectedRequest: MyRequestItem? = null
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
}
