package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.ApprovalRequestItem
import com.archeGlobal.one.model.ManagerDashboardRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApprovalRequestsController(private val context: Context) {

    var approvalRequests by mutableStateOf<List<ApprovalRequestItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchManagerApprovals() {
        val approverEmail = "brindha.a@arche.global"

//        val userData = UserDataManager.getInstance(context).getUserData()
//        val approverEmail = userData?.email ?: ""
//
//        if (approverEmail.isEmpty()) {
//            errorMessage = "Approver email not found"
//            return
//        }

        isLoading = true
        errorMessage = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("ApprovalController", "Fetching approvals for: $approverEmail")
                val response = RetrofitClient.apiService.getManagerDashboard(
                    ManagerDashboardRequest(approverEmail)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    approvalRequests = response.body()?.data?.requests ?: emptyList()
                    Log.d("ApprovalController", "Fetched ${approvalRequests.size} requests")
                } else {
                    errorMessage = response.body()?.message ?: "Failed to fetch approvals"
                    Log.e("ApprovalController", "API Error: $errorMessage")
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                Log.e("ApprovalController", "Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
}
