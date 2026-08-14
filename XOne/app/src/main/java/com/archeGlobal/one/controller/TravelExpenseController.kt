package com.archeGlobal.one.controller

import android.R.attr.tag
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.network.CreateTripRequest
import com.archeGlobal.one.network.ExpenseRetrofitClient
import com.archeGlobal.one.network.MileageExpenseItemUi
import com.archeGlobal.one.network.TripStatusUpdateRequest
import com.archeGlobal.one.network.TeamMileageDashboardMetricsResponse
import com.archeGlobal.one.network.TravelRequestItemUi
import com.archeGlobal.one.network.toMileageExpenseItemUi
import com.archeGlobal.one.network.toTravelRequestItemUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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
    var tripOptions = mutableStateOf<List<com.archeGlobal.one.network.TripOptionResponse>>(emptyList())
    var tripOptionsLoading = mutableStateOf(false)
    var tripOptionsError = mutableStateOf<String?>(null)
    var createRequestLoading = mutableStateOf(false)
    var vehicleAssets = mutableStateOf<List<com.archeGlobal.one.network.VehicleAssetItem>>(emptyList())
    var vehicleAssetsLoading = mutableStateOf(false)
    var vehicleAssetsError = mutableStateOf<String?>(null)
    var personalVehicles = mutableStateOf<List<com.archeGlobal.one.network.VehicleItem>>(emptyList())
    var personalVehiclesLoading = mutableStateOf(false)
    var personalVehiclesError = mutableStateOf<String?>(null)
    var mileageRate = mutableStateOf<String?>(null)
    var mileageRateLoading = mutableStateOf(false)
    var mileageRateError = mutableStateOf<String?>(null)
    var mileageExpenses = mutableStateOf<List<MileageExpenseItemUi>>(emptyList())
    var mileageExpensesLoading = mutableStateOf(false)
    var mileageExpensesError = mutableStateOf<String?>(null)
    var mileageDashboardMetrics = mutableStateOf<TeamMileageDashboardMetricsResponse?>(null)
    var mileageDashboardMetricsLoading = mutableStateOf(false)
    var mileageDashboardMetricsError = mutableStateOf<String?>(null)
    var teamMileageMetrics = mutableStateOf<TeamMileageDashboardMetricsResponse?>(null)
    var teamMileageMetricsLoading = mutableStateOf(false)
    var teamMileageMetricsError = mutableStateOf<String?>(null)
    var teamMileageExpenses = mutableStateOf<List<MileageExpenseItemUi>>(emptyList())
    var teamMileageExpensesLoading = mutableStateOf(false)
    var teamMileageExpensesError = mutableStateOf<String?>(null)
    var teamTrips = mutableStateOf<List<TravelRequestItemUi>>(emptyList())
    var teamTripsLoading = mutableStateOf(false)
    var teamTripsError = mutableStateOf<String?>(null)

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
                        errorMessage.value = parseErrorMessage(errorBody, "Failed to load travel requests (${response.code()})")
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

    fun fetchMileageExpenses() {
        mileageExpensesLoading.value = true
        mileageExpensesError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getMileageExpenses(page = 1, perPage = 10)
                if (response.isSuccessful) {
                    val items = response.body()?.data?.map { it.toMileageExpenseItemUi() }.orEmpty()
                    withContext(Dispatchers.Main) {
                        mileageExpenses.value = items
                        mileageExpensesError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch mileage expenses: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        mileageExpenses.value = emptyList()
                        mileageExpensesError.value = parseErrorMessage(errorBody, "Failed to load mileage expenses (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching mileage expenses: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    mileageExpenses.value = emptyList()
                    mileageExpensesError.value = e.message ?: "Failed to load mileage expenses"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    mileageExpensesLoading.value = false
                }
            }
        }
    }

    fun fetchMileageDashboardMetrics() {
        mileageDashboardMetricsLoading.value = true
        mileageDashboardMetricsError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getMileageDashboardMetrics()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        mileageDashboardMetrics.value = response.body()
                        mileageDashboardMetricsError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch mileage dashboard metrics: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        mileageDashboardMetrics.value = null
                        mileageDashboardMetricsError.value = parseErrorMessage(errorBody, "Failed to load dashboard metrics (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching mileage dashboard metrics: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    mileageDashboardMetrics.value = null
                    mileageDashboardMetricsError.value = e.message ?: "Failed to load dashboard metrics"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    mileageDashboardMetricsLoading.value = false
                }
            }
        }
    }

    fun fetchTeamMileageDashboardMetrics() {
        teamMileageMetricsLoading.value = true
        teamMileageMetricsError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getTeamMileageDashboardMetrics()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        teamMileageMetrics.value = response.body()
                        teamMileageMetricsError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch team mileage metrics: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        teamMileageMetrics.value = null
                        teamMileageMetricsError.value = parseErrorMessage(errorBody, "Failed to load mileage metrics (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching team mileage metrics: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    teamMileageMetrics.value = null
                    teamMileageMetricsError.value = e.message ?: "Failed to load mileage metrics"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    teamMileageMetricsLoading.value = false
                }
            }
        }
    }

    fun fetchTeamMileageExpenses(page: Int = 1, perPage: Int = 10) {
        teamMileageExpensesLoading.value = true
        teamMileageExpensesError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getTeamMileageExpenses(page = page, perPage = perPage)
                if (response.isSuccessful) {
                    val items = response.body()?.data?.map { it.toMileageExpenseItemUi() }.orEmpty()
                    withContext(Dispatchers.Main) {
                        teamMileageExpenses.value = items
                        teamMileageExpensesError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch team mileage expenses: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        teamMileageExpenses.value = emptyList()
                        teamMileageExpensesError.value = parseErrorMessage(errorBody, "Failed to load team mileage expenses (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching team mileage expenses: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    teamMileageExpenses.value = emptyList()
                    teamMileageExpensesError.value = e.message ?: "Failed to load team mileage expenses"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    teamMileageExpensesLoading.value = false
                }
            }
        }
    }

    fun fetchTeamTrips(page: Int = 1, perPage: Int = 10) {
        teamTripsLoading.value = true
        teamTripsError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getTeamTrips(page = page, perPage = perPage)
                if (response.isSuccessful) {
                    val items = response.body()?.data?.map { it.toTravelRequestItemUi() }.orEmpty()
                    withContext(Dispatchers.Main) {
                        teamTrips.value = items
                        teamTripsError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch team trips: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        teamTrips.value = emptyList()
                        teamTripsError.value = parseErrorMessage(errorBody, "Failed to load team travel requests (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching team trips: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    teamTrips.value = emptyList()
                    teamTripsError.value = e.message ?: "Failed to load team travel requests"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    teamTripsLoading.value = false
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
                        projectOptionsError.value = parseErrorMessage(errorBody, "Failed to load projects (${response.code()})")
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

    fun fetchTripOptions() {
        tripOptionsLoading.value = true
        tripOptionsError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getTripOptions()
                if (response.isSuccessful) {
                    val options = response.body().orEmpty()
                    withContext(Dispatchers.Main) {
                        tripOptions.value = options
                        tripOptionsError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch trip options: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        tripOptions.value = emptyList()
                        tripOptionsError.value = parseErrorMessage(errorBody, "Failed to load trip options (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching trip options: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    tripOptions.value = emptyList()
                    tripOptionsError.value = e.message ?: "Failed to load trip options"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    tripOptionsLoading.value = false
                }
            }
        }
    }

    fun fetchVehicleAssets(vehicleType: String) {
        vehicleAssetsLoading.value = true
        vehicleAssetsError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getVehicleAssets(
                    vehicleType = vehicleType.lowercase(Locale.getDefault()),
                )
                if (response.isSuccessful) {
                    val assets = response.body()?.data.orEmpty()
                    withContext(Dispatchers.Main) {
                        vehicleAssets.value = assets
                        vehicleAssetsError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch vehicle assets: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        vehicleAssets.value = emptyList()
                        vehicleAssetsError.value = parseErrorMessage(errorBody, "Failed to load vehicles (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching vehicle assets: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    vehicleAssets.value = emptyList()
                    vehicleAssetsError.value = e.message ?: "Failed to load vehicles"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    vehicleAssetsLoading.value = false
                }
            }
        }
    }

    fun fetchPersonalVehicles(vehicleType: String) {
        personalVehiclesLoading.value = true
        personalVehiclesError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getVehicles(
                    type = vehicleType.lowercase(Locale.getDefault()),
                )
                if (response.isSuccessful) {
                    val vehicles = response.body()?.vehicles.orEmpty()
                    withContext(Dispatchers.Main) {
                        personalVehicles.value = vehicles
                        personalVehiclesError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch personal vehicles: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        personalVehicles.value = emptyList()
                        personalVehiclesError.value = parseErrorMessage(errorBody, "Failed to load vehicles (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching personal vehicles: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    personalVehicles.value = emptyList()
                    personalVehiclesError.value = e.message ?: "Failed to load vehicles"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    personalVehiclesLoading.value = false
                }
            }
        }
    }

    fun createMileageExpense(
        request: com.archeGlobal.one.network.CreateMileageExpenseRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createRequestLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.createMileageExpense(request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Travel expense added successfully.", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to create mileage expense: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to add travel expense (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception creating mileage expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to add travel expense")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    createRequestLoading.value = false
                }
            }
        }
    }

    fun updateMileageExpense(
        expenseId: Int,
        request: com.archeGlobal.one.network.CreateMileageExpenseRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createRequestLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.updateMileageExpense(expenseId, request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Travel expense updated successfully.", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to update mileage expense: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to update travel expense (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception updating mileage expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to update travel expense")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    createRequestLoading.value = false
                }
            }
        }
    }

    fun withdrawMileageExpense(
        expenseId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createRequestLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.withdrawMileageExpense(expenseId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Travel expense notes withdrawn successfully.", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to withdraw mileage expense: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to withdraw travel expense (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception withdrawing mileage expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to withdraw travel expense")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    createRequestLoading.value = false
                }
            }
        }
    }

    fun deleteMileageExpense(
        expenseId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createRequestLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.deleteMileageExpense(expenseId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Travel expense notes deleted successfully.", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to delete mileage expense: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to delete travel expense (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception deleting mileage expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to delete travel expense")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    createRequestLoading.value = false
                }
            }
        }
    }

    fun fetchMileageExpenseDetail(
        expenseId: Int,
        onSuccess: (com.archeGlobal.one.network.MileageExpenseDetailResponse) -> Unit,
        onError: (String) -> Unit,
    ) {
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getMileageExpenseDetail(expenseId)
                if (response.isSuccessful) {
                    val body = response.body()
                    withContext(Dispatchers.Main) {
                        if (body != null) {
                            onSuccess(body)
                        } else {
                            onError("Empty response")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch mileage expense detail: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to load travel expense (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching mileage expense detail: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to load travel expense")
                }
            }
        }
    }

    fun fetchMileageExpenseNotes(
        expenseId: Int,
        onSuccess: (List<com.archeGlobal.one.network.MileageExpenseNoteResponse>) -> Unit,
        onError: (String) -> Unit,
    ) {
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getMileageExpenseNotes(expenseId)
                if (response.isSuccessful) {
                    val notes = response.body().orEmpty()
                    withContext(Dispatchers.Main) {
                        onSuccess(notes)
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch mileage expense notes: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to load notes (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching mileage expense notes: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to load notes")
                }
            }
        }
    }

    fun addMileageExpenseNote(
        expenseId: Int,
        notes: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.addMileageExpenseNote(
                    com.archeGlobal.one.network.AddMileageExpenseNoteRequest(
                        notes = notes,
                        expenseId = expenseId,
                    )
                )
                if (response.isSuccessful) {
                    val message = response.body()?.message?.takeIf { it.isNotBlank() }
                        ?: "Travel expense notes added successfully."
                    withContext(Dispatchers.Main) {
                        onSuccess(message)
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to add mileage expense note: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to add note (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception adding mileage expense note: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to add note")
                }
            }
        }
    }

    fun submitMileageExpense(
        expenseId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.submitMileageExpense(expenseId)
                if (response.isSuccessful) {
                    val message = response.body()?.message?.takeIf { it.isNotBlank() }
                        ?: "Travel expense updated successfully."
                    withContext(Dispatchers.Main) {
                        onSuccess(message)
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to submit mileage expense: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to request reimbursement (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception submitting mileage expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to request reimbursement")
                }
            }
        }
    }

    fun fetchMileageRate(
        vehicleId: Int,
        vehicleOwnershipType: String,
        vehicleType: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        mileageRateLoading.value = true
        mileageRateError.value = null
        mileageRate.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.tripService.getMileageRate(
                    com.archeGlobal.one.network.MileageRateRequest(
                        vehicleId = vehicleId,
                        vehicleOwnershipType = vehicleOwnershipType.lowercase(Locale.getDefault()),
                        vehicleType = vehicleType.lowercase(Locale.getDefault()),
                    )
                )
                if (response.isSuccessful) {
                    val rateValue = response.body()?.mileageRate
                    withContext(Dispatchers.Main) {
                        if (!rateValue.isNullOrBlank()) {
                            mileageRate.value = rateValue
                            mileageRateError.value = null
                            onSuccess(rateValue)
                        } else {
                            mileageRate.value = null
                            mileageRateError.value = "No mileage rate returned"
                            onError("No mileage rate returned")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch mileage rate: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        mileageRate.value = null
                        val message = parseErrorMessage(errorBody, "Failed to load mileage rate (${response.code()})")
                        mileageRateError.value = message
                        onError(message)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching mileage rate: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    mileageRate.value = null
                    mileageRateError.value = e.message ?: "Failed to load mileage rate"
                    onError(e.message ?: "Failed to load mileage rate")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    mileageRateLoading.value = false
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
                    val message = parseErrorMessage(errorBody, "Failed to submit travel request (${response.code()})")
                    Log.e(tag, "Failed to create trip: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(message)
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

    fun updateTripStatus(
        tripId: String,
        approvedAmount: Double?,
        comment: String,
        status: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createRequestLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = TripStatusUpdateRequest(
                    approved_amount = approvedAmount,
                    comment = comment,
                    status = status,
                )
                val response = ExpenseRetrofitClient.tripService.updateTripStatus(tripId, request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        fetchTeamTrips()
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    val message = parseErrorMessage(errorBody, "Failed to update travel request (${response.code()})")
                    Log.e(tag, "Failed to update trip status: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(message)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception updating trip status: ${e.message}", e)
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
                    val message = parseErrorMessage(errorBody, "Failed to update travel request (${response.code()})")
                    Log.e(tag, "Failed to update trip: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(message)
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
                        onError(parseErrorMessage(errorBody, "Failed to load travel request details (${response.code()})"))
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

    private fun parseErrorMessage(errorBody: String?, fallback: String): String =
        com.archeGlobal.one.network.parseApiErrorMessage(errorBody, fallback)

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
                    val message = parseErrorMessage(errorBody, "Failed to delete travel request (${response.code()})")
                    Log.e(tag, "Failed to delete trip: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(message)
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
