package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.network.CreateTripRequest
import com.archeGlobal.one.network.ExpenseRetrofitClient
import com.archeGlobal.one.network.TravelRequestItemUi
import com.archeGlobal.one.network.toTravelRequestItemUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TravelExpenseController(
    private val context: Context,
) {
    private val tag = "TravelExpenseController"

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var trips = mutableStateOf<List<TravelRequestItemUi>>(emptyList())
    var projectOptions = mutableStateOf<List<com.archeGlobal.one.network.ProjectOption>>(emptyList())
    var projectOptionsLoading = mutableStateOf(false)
    var projectOptionsError = mutableStateOf<String?>(null)
    var createRequestLoading = mutableStateOf(false)

    fun fetchTrips() {
        isLoading.value = true
        errorMessage.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        val refreshToken = ExpenseRetrofitClient.cookieJar.getRefreshToken()
        Log.d(tag, "Fetching trips — refresh token available: ${!refreshToken.isNullOrBlank()}")
        if (!refreshToken.isNullOrBlank()) {
            Log.d(tag, "Refresh token from CookieJar: $refreshToken")
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getTrips()

                if (response.isSuccessful) {
                    val items = response.body()?.data?.map { it.toTravelRequestItemUi() }.orEmpty()
                    Log.d(tag, "Fetched ${items.size} travel requests")
                    Log.d(tag, "Refresh token in CookieJar: ${ExpenseRetrofitClient.cookieJar.getRefreshToken()}")
                    withContext(Dispatchers.Main) {
                        trips.value = items
                        errorMessage.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch trips: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        trips.value = emptyList()
                        errorMessage.value = "Failed to load travel requests (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching trips: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    trips.value = emptyList()
                    errorMessage.value = e.message ?: "Failed to load travel requests"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }

    fun fetchProjectOptions() {
        projectOptionsLoading.value = true
        projectOptionsError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getProjectOptions()
                if (response.isSuccessful) {
                    val options = response.body()?.data.orEmpty()
                    withContext(Dispatchers.Main) {
                        projectOptions.value = options
                        projectOptionsError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch project options: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        projectOptions.value = emptyList()
                        projectOptionsError.value = "Failed to load projects (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching project options: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    projectOptions.value = emptyList()
                    projectOptionsError.value = e.message ?: "Failed to load projects"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    projectOptionsLoading.value = false
                }
            }
        }
    }
    fun createTrip(
        request: CreateTripRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createRequestLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.createTrip(request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Travel request submitted successfully", Toast.LENGTH_SHORT).show()
                        fetchTrips()
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to create trip: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError("Failed to submit travel request (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception creating trip: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to submit travel request")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    createRequestLoading.value = false
                }
            }
        }
    }

    fun updateTrip(
        tripId: String,
        request: CreateTripRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createRequestLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.updateTrip(tripId, request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Travel request updated successfully", Toast.LENGTH_SHORT).show()
                        fetchTrips()
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to update trip: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError("Failed to update travel request (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception updating trip: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to update travel request")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    createRequestLoading.value = false
                }
            }
        }
    }

    fun fetchTrip(
        tripId: String,
        onSuccess: (TravelRequestItemUi) -> Unit,
        onError: (String) -> Unit,
    ) {
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getTrip(tripId)
                if (response.isSuccessful) {
                    val trip = response.body()
                    if (trip != null) {
                        withContext(Dispatchers.Main) {
                            onSuccess(trip.toTravelRequestItemUi())
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onError("Failed to load travel request details")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch trip: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError("Failed to load travel request details (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching trip: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to load travel request details")
                }
            }
        }
    }

    fun deleteTrip(
        tripId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.deleteTrip(tripId)
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Trip deleted successfully"
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        fetchTrips()
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to delete trip: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError("Failed to delete travel request (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception deleting trip: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to delete travel request")
                }
            }
        }
    }
}
