package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.AssetTicketsModel
import com.archeGlobal.one.model.AssetTicketsResponse
import com.archeGlobal.one.model.SupportTicket
import com.archeGlobal.one.model.toSupportTicket
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.network.TicketsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssetTicketsController(
    private val context: Context,
    private val status: String
) {
    private val _model = MutableStateFlow(AssetTicketsModel())
    val model: StateFlow<AssetTicketsModel> = _model.asStateFlow()

    private val apiService = RetrofitClient.apiService

    init {
        loadTicketsData()
    }

    fun loadTicketsData() {
        _model.value = _model.value.copy(
            isLoading = true,
            error = null,
            tickets = emptyList(),
            count = 0
        )

        Log.d("AssetTicketsController", "Starting API call for tickets with status: $status")
        apiService.getAssetTickets(status).enqueue(
            object : Callback<AssetTicketsResponse> {
                override fun onResponse(
                    call: Call<AssetTicketsResponse>,
                    response: Response<AssetTicketsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val ticketsResponse = response.body()!!
                        val supportTickets = ticketsResponse.tickets.map { it.toSupportTicket() }

                        _model.value = _model.value.copy(
                            tickets = supportTickets,
                            count = ticketsResponse.counts,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _model.value = _model.value.copy(
                            isLoading = false,
                            error = "Failed to load tickets: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<AssetTicketsResponse>,
                    t: Throwable
                ) {
                    _model.value = _model.value.copy(
                        isLoading = false,
                        error = "Network error: ${t.message}"
                    )
                }
            }
        )
    }

    fun refreshTickets() {
        loadTicketsData()
    }
}