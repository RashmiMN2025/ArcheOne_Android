package com.archeGlobal.one.model

import com.archeGlobal.one.network.TicketItem

data class AssetTicketsResponse(
    val status: Int,
    val counts: Int,
    val tickets: List<TicketItem>
)

data class AssetTicketsModel(
    val tickets: List<SupportTicket> = emptyList(),
    val count: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)