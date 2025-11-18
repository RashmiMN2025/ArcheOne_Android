
package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.archeGlobal.one.model.SelfTagItem
import com.archeGlobal.one.model.SelfTagRequest
import com.archeGlobal.one.model.SelfTagResponse
import com.archeGlobal.one.model.TagActionResponse
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelfTagController(
    private val context: Context
) {
    private val _model = MutableStateFlow(SelfTagModel())
    val model: StateFlow<SelfTagModel> = _model.asStateFlow()

    private val apiService = RetrofitClient.apiService

    init {
        loadSelfTagRequests()
    }

    fun loadSelfTagRequests() {
        _model.value = _model.value.copy(isLoading = true, error = null, items = emptyList())

        val request = SelfTagRequest()
        apiService.fetchSelfTagRequests(request).enqueue(
            object : Callback<SelfTagResponse> {
                override fun onResponse(
                    call: Call<SelfTagResponse>,
                    response: Response<SelfTagResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val resp = response.body()!!
                        _model.value = _model.value.copy(
                            items = resp.data,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _model.value = _model.value.copy(
                            isLoading = false,
                            error = "Failed to load: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<SelfTagResponse>, t: Throwable) {
                    _model.value = _model.value.copy(
                        isLoading = false,
                        error = "Network error: ${t.message}"
                    )
                }
            }
        )
    }

    fun approveTag(employeeCode: String, serialNumber: String) {
        apiService.approveTag(employeeCode, serialNumber).enqueue(
            object : Callback<TagActionResponse> {
                override fun onResponse(call: Call<TagActionResponse>, response: Response<TagActionResponse>) {
                    val msg = response.body()?.message ?: "Approved"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                    if (response.isSuccessful) {
                        loadSelfTagRequests() // Refresh list
                    }
                }

                override fun onFailure(call: Call<TagActionResponse>, t: Throwable) {
                    Toast.makeText(context, "Approve failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    fun rejectTag(employeeCode: String, serialNumber: String) {
        apiService.rejectTag(employeeCode, serialNumber).enqueue(
            object : Callback<TagActionResponse> {
                override fun onResponse(call: Call<TagActionResponse>, response: Response<TagActionResponse>) {
                    val msg = response.body()?.message ?: "Rejected"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                    if (response.isSuccessful) {
                        loadSelfTagRequests() // Refresh list
                    }
                }

                override fun onFailure(call: Call<TagActionResponse>, t: Throwable) {
                    Toast.makeText(context, "Reject failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

}

data class SelfTagModel(
    val items: List<SelfTagItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)