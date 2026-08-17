package com.archeGlobal.one.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.util.Log
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.archeGlobal.one.controller.TravelExpenseController
import com.archeGlobal.one.network.MileageExpenseDetailResponse
import com.archeGlobal.one.network.MileageExpenseNoteResponse
import com.archeGlobal.one.network.MileageExpenseRoutePoint
import com.archeGlobal.one.network.VehicleAssetItem
import com.archeGlobal.one.network.isTravelExpenseDraft
import com.archeGlobal.one.network.isTravelExpenseSubmitted
import com.archeGlobal.one.network.travelExpenseStatusLabel
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAPPLS_ACCESS_TOKEN = "a5d8515bf86b25045659870561ac1980"
private const val MAPPLS_TILE_BASE_URL = "https://apis.mappls.com/advancedmaps/v1/$MAPPLS_ACCESS_TOKEN/map_sdk/mal_en/"

private fun buildMapplsTileSource(): OnlineTileSourceBase {
    return object : OnlineTileSourceBase(
        "mappls",
        2,
        20,
        256,
        ".png",
        arrayOf(MAPPLS_TILE_BASE_URL)
    ) {
        override fun getTileURLString(pTile: Long): String {
            val zoom = MapTileIndex.getZoom(pTile)
            val x = MapTileIndex.getX(pTile)
            val y = MapTileIndex.getY(pTile)
            return "$MAPPLS_TILE_BASE_URL$zoom/$x/$y.png"
        }
    }
}

private suspend fun probeMapplsAvailability(): Boolean = withContext(Dispatchers.IO) {
    val probeUrl = URL("${MAPPLS_TILE_BASE_URL}2/1/1.png")
    var connection: HttpURLConnection? = null
    return@withContext try {
        connection = probeUrl.openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        val code = connection.responseCode
        val contentType = connection.contentType.orEmpty()
        code in 200..299 && contentType.startsWith("image", ignoreCase = true)
    } catch (_: Exception) {
        false
    } finally {
        connection?.disconnect()
    }
}

private data class MileageTripUi(
    val expenseId: Int,
    val id: String,
    val customerName: String,
    val fromDate: String,
    val toDate: String,
    val date: String,
    val startPoint: String,
    val endPoint: String,
    val type: String,
    val vehicle: String,
    val vehicleName: String,
    val amount: String,
    val distance: String,
    val status: String,
    val routePoints: List<MileageExpenseRoutePoint>,
    val projectId: Int,
    val vehicleOwnershipType: String,
    val vehicleTypeValue: String,
    val vehicleId: Int?
)

private data class RouteStop(
    val name: String = "",
    val point: GeoPoint? = null,
)

internal fun buildVehicleDropdownOptions(
    vehicleOwner: String,
    vehicleAssets: List<VehicleAssetItem>,
    personalVehicles: List<com.archeGlobal.one.network.VehicleItem> = emptyList(),
): List<String> {
    return when {
        vehicleOwner.equals("COMPANY", ignoreCase = true) -> vehicleAssets
            .mapNotNull { it.makeModel?.takeIf(String::isNotBlank) }
            .distinct()
        vehicleOwner.equals("PERSONAL", ignoreCase = true) -> personalVehicles
            .mapNotNull { it.makeModel?.takeIf(String::isNotBlank) }
            .distinct()
        else -> emptyList()
    }
}

private fun buildAddressDisplayName(address: Address?, fallbackPoint: GeoPoint? = null): String {
    val fullAddress = address?.getAddressLine(0)?.takeIf { it.isNotBlank() }
    if (!fullAddress.isNullOrBlank()) {
        return fullAddress
    }

    val parts = listOfNotNull(
        address?.featureName?.takeIf { it.isNotBlank() },
        address?.subThoroughfare?.takeIf { it.isNotBlank() },
        address?.thoroughfare?.takeIf { it.isNotBlank() },
        address?.subLocality?.takeIf { it.isNotBlank() },
        address?.locality?.takeIf { it.isNotBlank() },
        address?.subAdminArea?.takeIf { it.isNotBlank() },
        address?.adminArea?.takeIf { it.isNotBlank() },
        address?.countryName?.takeIf { it.isNotBlank() },
    ).joinToString(separator = ", ")

    return parts.ifBlank {
        if (fallbackPoint != null) {
            "Lat ${"%.4f".format(fallbackPoint.latitude)}, Lng ${"%.4f".format(fallbackPoint.longitude)}"
        } else {
            "Location selected"
        }
    }
}

private fun pickMostAccurateAddress(results: List<Address>?): Address? {
    if (results.isNullOrEmpty()) {
        return null
    }

    return results.maxByOrNull { address ->
        listOf(
            if (address.getAddressLine(0).isNullOrBlank()) 0 else 4,
            if (address.featureName.isNullOrBlank()) 0 else 3,
            if (address.subThoroughfare.isNullOrBlank()) 0 else 3,
            if (address.thoroughfare.isNullOrBlank()) 0 else 3,
            if (address.subLocality.isNullOrBlank()) 0 else 2,
            if (address.locality.isNullOrBlank()) 0 else 2,
            if (address.subAdminArea.isNullOrBlank()) 0 else 1,
            if (address.adminArea.isNullOrBlank()) 0 else 1,
        ).sum()
    }
}

private fun resolveAddressFromPoint(context: Context, point: GeoPoint): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocation(point.latitude, point.longitude, 5)
        val address = pickMostAccurateAddress(results)
        buildAddressDisplayName(address, point)
    } catch (_: Exception) {
        "Lat ${"%.4f".format(point.latitude)}, Lng ${"%.4f".format(point.longitude)}"
    }
}

private fun formatCurrencyValue(value: BigDecimal): String {
    return "Rs ${value.setScale(2, RoundingMode.HALF_UP).toPlainString()}"
}

private fun getCurrentGeoPoint(context: Context, onResult: (GeoPoint?) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
        onResult(null)
        return
    }

    val finePermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!finePermissionGranted) {
        onResult(null)
        return
    }

    val lastKnownLocation = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .firstOrNull { provider -> locationManager.isProviderEnabled(provider) }
        ?.let { provider -> locationManager.getLastKnownLocation(provider) }

    if (lastKnownLocation != null) {
        onResult(GeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude))
        return
    }

    val criteria = Criteria().apply { accuracy = Criteria.ACCURACY_FINE }
    val provider = locationManager.getBestProvider(criteria, true)
    if (provider == null) {
        onResult(null)
        return
    }

    val handler = Handler(Looper.getMainLooper())
    var resolved = false
    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (resolved) return
            resolved = true
            handler.removeCallbacksAndMessages(null)
            locationManager.removeUpdates(this)
            onResult(GeoPoint(location.latitude, location.longitude))
        }

        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    try {
        locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        handler.postDelayed({
            if (!resolved) {
                resolved = true
                locationManager.removeUpdates(listener)
                onResult(null)
            }
        }, 10000)
    } catch (_: SecurityException) {
        onResult(null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun notAfterTodaySelectableDates(minMillis: Long? = null): SelectableDates {
    val maxMillis = System.currentTimeMillis()
    return object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            if (minMillis != null && utcTimeMillis < minMillis) return false
            return utcTimeMillis <= maxMillis
        }
    }
}

private fun haversineKm(startPoint: GeoPoint, endPoint: GeoPoint): Double {
    val earthRadiusKm = 6371.0
    val latDistance = Math.toRadians(endPoint.latitude - startPoint.latitude)
    val lonDistance = Math.toRadians(endPoint.longitude - startPoint.longitude)
    val startLat = Math.toRadians(startPoint.latitude)
    val endLat = Math.toRadians(endPoint.latitude)

    val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
        Math.cos(startLat) * Math.cos(endLat) *
        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadiusKm * c
}

private fun calculateRouteDistanceInKm(points: List<GeoPoint>): String {
    if (points.size < 2) {
        return ""
    }

    val totalKm = points.zipWithNext().sumOf { (a, b) -> haversineKm(a, b) }
    return BigDecimal(totalKm).setScale(1, RoundingMode.HALF_UP).toPlainString()
}

private fun searchGeoPoint(context: Context, query: String, onResult: (GeoPoint?, String) -> Unit) {
    if (query.isBlank()) {
        onResult(null, "")
        return
    }

    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocationName(query, 8)
        val address = pickMostAccurateAddress(results)
        if (address != null) {
            val point = GeoPoint(address.latitude, address.longitude)
            val displayName = buildAddressDisplayName(address)
            onResult(point, displayName.ifBlank { query })
        } else {
            onResult(null, "")
        }
    } catch (_: Exception) {
        onResult(null, "")
    }
}

@Composable
fun MileageCalculatorViewScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val expenseController = remember { TravelExpenseController(context) }
    val mileageExpenses by expenseController.mileageExpenses
    val mileageExpensesLoading by expenseController.mileageExpensesLoading
    val mileageExpensesError by expenseController.mileageExpensesError
    val mileageDashboardMetrics by expenseController.mileageDashboardMetrics
    val mileageDashboardMetricsLoading by expenseController.mileageDashboardMetricsLoading
    val mileageDashboardMetricsError by expenseController.mileageDashboardMetricsError

    var searchText by rememberSaveable { mutableStateOf("") }
    var showAddMileageSheet by rememberSaveable { mutableStateOf(false) }
    var selectedExpenseForEdit by remember { mutableStateOf<MileageTripUi?>(null) }
    var selectedExpenseForAction by remember { mutableStateOf<MileageTripUi?>(null) }
    var showWithdrawConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedExpenseForDetail by remember { mutableStateOf<Int?>(null) }

    if (selectedExpenseForDetail != null) {
        MileageExpenseDetailScreen(
            expenseId = selectedExpenseForDetail!!,
            onBack = {
                selectedExpenseForDetail = null
                expenseController.fetchMileageExpenses()
            },
        )
        return
    }

    LaunchedEffect(Unit) {
        expenseController.fetchMileageExpenses()
        expenseController.fetchMileageDashboardMetrics()
    }

    BackHandler {
        onBack()
    }

    val trips = remember(mileageExpenses) {
        mileageExpenses.map { expense ->
            MileageTripUi(
                expenseId = expense.expenseId,
                id = expense.id,
                customerName = expense.customerName,
                fromDate = expense.fromDate,
                toDate = expense.toDate,
                date = expense.date,
                startPoint = expense.startPoint,
                endPoint = expense.endPoint,
                type = expense.type,
                vehicle = expense.vehicle,
                vehicleName = expense.vehicleName,
                amount = expense.amount,
                distance = expense.distance,
                status = expense.status,
                routePoints = expense.routePoints,
                projectId = expense.projectId,
                vehicleOwnershipType = expense.vehicleOwnershipType,
                vehicleTypeValue = expense.vehicleTypeValue,
                vehicleId = expense.vehicleId,
            )
        }
    }

    val filteredTrips = trips.filter { trip ->
        searchText.isBlank() ||
            trip.id.contains(searchText, ignoreCase = true) ||
            trip.customerName.contains(searchText, ignoreCase = true) ||
            trip.startPoint.contains(searchText, ignoreCase = true) ||
            trip.endPoint.contains(searchText, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,
                        WelcomeBackgroundMiddle,
                        WelcomeBackgroundBottom
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Mileage Overview",
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    actions = { Spacer(modifier = Modifier.size(48.dp)) }
                )
            }

            item {
                Text(
                    text = "Mileage Overview",
                    fontFamily = GraphikFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Track and review all travel-based mileage submissions in one place.",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalDistance = formatMetricValue(mileageDashboardMetrics?.totalDistance)
                    val totalAmount = formatMetricValue(mileageDashboardMetrics?.totalClaimAmount)
                    val totalCarbonEmission = formatMetricValue(mileageDashboardMetrics?.totalCarbonEmission)
                    MileageStatCard(
                        title = "Total distance logged",
                        value = "$totalDistance km",
                        icon = Icons.Default.DirectionsCar,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    MileageStatCard(
                        title = "Total Carbon Emission",
                        value = "$totalCarbonEmission Kg CO₂e",
                        icon = Icons.Default.Eco,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    MileageStatCard(
                        title = "Total claim amount",
                        value = "Rs $totalAmount",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "My recent trips",
                        fontFamily = GraphikFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        },
                        placeholder = {
                            Text(
                                text = "Search trips...",
                                color = Color.Gray,
                                fontFamily = GraphikFontFamily,
                                fontSize = 15.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color(0xFFD4D4D4)
                        )
                    )
                }
            }

            if (mileageExpensesLoading) {
                item {
                    MileageEmptyState(message = "Loading mileage expenses...")
                }
            } else if (!mileageExpensesError.isNullOrBlank()) {
                item {
                    MileageEmptyState(message = mileageExpensesError ?: "Unable to load mileage expenses")
                }
            } else if (filteredTrips.isEmpty()) {
                item {
                    MileageEmptyState(message = "You haven't spent anything yet")
                }
            } else {
                items(filteredTrips) { trip ->
                    MileageTripCard(
                        trip = trip,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        onView = { selectedExpenseForDetail = trip.expenseId },
                        onEdit = {
                            selectedExpenseForEdit = trip
                            showAddMileageSheet = true
                        },
                        onDelete = {
                            selectedExpenseForAction = trip
                            showDeleteConfirm = true
                        },
                        onWithdraw = {
                            selectedExpenseForAction = trip
                            showWithdrawConfirm = true
                        }
                    )
                }
            }
        }

        Button(
            onClick = { showAddMileageSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = PrimaryRed,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Expense",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showAddMileageSheet) {
        AddMileageExpenseBottomSheet(
            expense = selectedExpenseForEdit,
            onDismiss = {
                selectedExpenseForEdit = null
                showAddMileageSheet = false
            },
            onSuccess = {
                expenseController.fetchMileageExpenses()
                expenseController.fetchMileageDashboardMetrics()
                selectedExpenseForEdit = null
                showAddMileageSheet = false
            }
        )
    }

    if (showWithdrawConfirm && selectedExpenseForAction != null) {
        val trip = selectedExpenseForAction!!
        AlertDialog(
            onDismissRequest = {
                showWithdrawConfirm = false
                selectedExpenseForAction = null
            },
            title = {
                Text(
                    text = "Withdraw selected Mileage Expense?",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "You're about to withdraw Mileage Expense-${trip.expenseId}.",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    expenseController.withdrawMileageExpense(
                        trip.expenseId,
                        onSuccess = {
                            showWithdrawConfirm = false
                            selectedExpenseForAction = null
                            expenseController.fetchMileageExpenses()
                            expenseController.fetchMileageDashboardMetrics()
                        },
                        onError = { message ->
                            showWithdrawConfirm = false
                            selectedExpenseForAction = null
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                }) {
                    Text("Withdraw", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWithdrawConfirm = false
                    selectedExpenseForAction = null
                }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        )
    }

    if (showDeleteConfirm && selectedExpenseForAction != null) {
        val trip = selectedExpenseForAction!!
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                selectedExpenseForAction = null
            },
            title = {
                Text(
                    text = "Delete selected Mileage Expense?",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "You're about to delete Mileage Expense-${trip.expenseId}. This action cannot be undone.",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    expenseController.deleteMileageExpense(
                        trip.expenseId,
                        onSuccess = {
                            showDeleteConfirm = false
                            selectedExpenseForAction = null
                            expenseController.fetchMileageExpenses()
                            expenseController.fetchMileageDashboardMetrics()
                        },
                        onError = { message ->
                            showDeleteConfirm = false
                            selectedExpenseForAction = null
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                }) {
                    Text("Delete", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    selectedExpenseForAction = null
                }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        )
    }
}

private fun formatMileageDate(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(value) ?: return value
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        value
    }
}

private fun formatNoteTimestamp(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(value.take(19)) ?: return value
        SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        value
    }
}

@Composable
private fun MileageExpenseDetailScreen(
    expenseId: Int,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val tripController = remember { TravelExpenseController(context) }
    val scrollState = rememberScrollState()

    var detail by remember { mutableStateOf<MileageExpenseDetailResponse?>(null) }
    var notes by remember { mutableStateOf<List<MileageExpenseNoteResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var noteText by rememberSaveable { mutableStateOf("") }
    var isSendingNote by remember { mutableStateOf(false) }
    var isSubmittingReimbursement by remember { mutableStateOf(false) }

    fun loadNotes() {
        tripController.fetchMileageExpenseNotes(
            expenseId = expenseId,
            onSuccess = { notes = it },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() },
        )
    }

    LaunchedEffect(expenseId) {
        isLoading = true
        tripController.fetchMileageExpenseDetail(
            expenseId = expenseId,
            onSuccess = {
                detail = it
                isLoading = false
            },
            onError = { message ->
                isLoading = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
        loadNotes()
    }

    fun sendNote() {
        val text = noteText.trim()
        if (text.isBlank()) {
            Toast.makeText(context, "Please enter a note", Toast.LENGTH_SHORT).show()
            return
        }
        isSendingNote = true
        tripController.addMileageExpenseNote(
            expenseId = expenseId,
            notes = text,
            onSuccess = { message ->
                isSendingNote = false
                noteText = ""
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                loadNotes()
            },
            onError = { message ->
                isSendingNote = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    fun submitReimbursement() {
        isSubmittingReimbursement = true
        tripController.submitMileageExpense(
            expenseId = expenseId,
            onSuccess = { message ->
                isSubmittingReimbursement = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                onBack()
            },
            onError = { message ->
                isSubmittingReimbursement = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,
                        WelcomeBackgroundMiddle,
                        WelcomeBackgroundBottom,
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Mileage Details",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                actions = { Spacer(modifier = Modifier.size(48.dp)) }
            )

            if (isLoading || detail == null) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            } else {
                val record = detail!!
                val routePoints = record.route.filter { it.name.isNotBlank() }
                val startAddress = routePoints.firstOrNull()?.name ?: "-"
                val endAddress = routePoints.lastOrNull()?.name ?: "-"
                val travelDate = if (record.fromDate == record.toDate || record.toDate.isNullOrBlank()) {
                    formatMileageDate(record.fromDate)
                } else {
                    "${formatMileageDate(record.fromDate)} - ${formatMileageDate(record.toDate)}"
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column {
                            Text(
                                text = "MLG-${record.id}",
                                fontFamily = GraphikFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                            )
                            Text(
                                text = record.projectName?.takeIf { it.isNotBlank() } ?: "-",
                                fontFamily = GraphikFontFamily,
                                fontSize = 14.sp,
                                color = Color.Gray,
                            )
                        }
                        val (badgeBg, badgeText) = statusColors(record.status ?: "-")
                        Text(
                            text = travelExpenseStatusLabel(record.status),
                            color = badgeText,
                            fontFamily = GraphikFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    MileageSectionCard(title = "Claim Summary") {
                        MileageDetailRow("Customer", record.customerName?.takeIf { it.isNotBlank() } ?: "-")
                        MileageDetailRow("Project", record.projectName?.takeIf { it.isNotBlank() } ?: "-")
                        MileageDetailRow("Date of Travel", travelDate)
                        MileageDetailRow("Claim Amount", "Rs ${record.amount ?: "0.00"}")
                    }

                    MileageSectionCard(title = "Trip Details") {
                        MileageDetailRow("From Address", startAddress)
                        MileageDetailRow("To Address", endAddress)
                        MileageDetailRow("Travel Distance", "${formatMetricValue(record.distance)} km")
                        MileageDetailRow("Duration", formatMileageDuration(record.durationSeconds))
                        MileageDetailRow("Carbon Emission", "${formatMetricValue(record.carbonEmission)} Kg CO₂e")
                    }

                    val vehicleDetail = record.companyVehicle ?: record.personalVehicle
                    MileageSectionCard(title = "Vehicle") {
                        MileageDetailRow(
                            "Ownership",
                            record.vehicleType?.replaceFirstChar { it.uppercase() } ?: "-",
                        )
                        MileageDetailRow(
                            "Vehicle",
                            record.vehicle?.replaceFirstChar { it.uppercase() } ?: "-",
                        )
                        MileageDetailRow("Make / Model", vehicleDetail?.makeModel ?: "-")
                        MileageDetailRow("Asset Code", vehicleDetail?.assetCode ?: "-")
                        MileageDetailRow(
                            "Fuel Type",
                            vehicleDetail?.fuelType?.replaceFirstChar { it.uppercase() } ?: "-",
                        )
                        MileageDetailRow(
                            "Engine CC",
                            vehicleDetail?.vehicleCc?.let { "$it cc" } ?: "-",
                        )
                    }

                    if (!record.mapImageUrl.isNullOrBlank()) {
                        MileageSectionCard(title = "Route") {
                            AsyncImage(
                                model = record.mapImageUrl,
                                contentDescription = "Route map",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                        }
                    }

                    MileageSectionCard(title = "Notes") {
                        if (notes.isEmpty()) {
                            Text(
                                text = "No notes yet",
                                fontFamily = GraphikFontFamily,
                                fontSize = 13.sp,
                                color = Color.Gray,
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                notes.forEach { note ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF6F4EE), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = note.createdBy?.takeIf { it.isNotBlank() } ?: "-",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color.Black,
                                            )
                                            Text(
                                                text = formatNoteTimestamp(note.createdAt),
                                                fontFamily = GraphikFontFamily,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                            )
                                        }
                                        Text(
                                            text = note.notes?.takeIf { it.isNotBlank() } ?: "-",
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        text = "Enter a note",
                                        color = Color.Gray,
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 14.sp,
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = Color.White,
                                    focusedBorderColor = Color.LightGray,
                                    unfocusedBorderColor = Color(0xFFD4D4D4),
                                ),
                                enabled = !isSendingNote,
                            )
                            IconButton(onClick = { sendNote() }, enabled = !isSendingNote) {
                                if (isSendingNote) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = PrimaryRed,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send note",
                                        tint = PrimaryRed,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val reimbursementAllowed = canRequestReimbursement(record.status)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFF6F4EE),
                                contentColor = Color.Black,
                            ),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            )
                        }
                        if (reimbursementAllowed) {
                            Button(
                                onClick = { submitReimbursement() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = PrimaryRed,
                                    contentColor = Color.White,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isSubmittingReimbursement,
                            ) {
                                if (isSubmittingReimbursement) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(
                                        text = "Request Reimbursement",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MileageSectionCard(
    title: String?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                )
                Divider(color = Color(0xFFEAEAEA))
            }
            content()
        }
    }
}

private fun formatMetricValue(value: String?): String {
    return value?.takeIf { it.isNotBlank() } ?: "0"
}

private fun formatMileageDuration(durationSeconds: String?): String {
    val seconds = durationSeconds?.toDoubleOrNull() ?: return "-"
    val totalMinutes = (seconds / 60).toInt()
    return when {
        totalMinutes < 60 -> "$totalMinutes min"
        totalMinutes % 60 == 0 -> "${totalMinutes / 60} hr"
        else -> "${totalMinutes / 60} hr ${totalMinutes % 60} min"
    }
}

private data class MileageActionAvailability(
    val canView: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canWithdraw: Boolean,
)

/**
 * Draft: everything except withdraw. Submitted/pending: view and withdraw only.
 * Rejected and approved records are read-only.
 *
 * Accepts both the raw API status (`drafted`, `pending`) and the display label
 * ([travelExpenseStatusLabel] turns those into `Draft` and `Submitted`).
 */
private fun mileageActionAvailability(status: String): MileageActionAvailability {
    return when {
        isTravelExpenseDraft(status) -> MileageActionAvailability(
            canView = true,
            canEdit = true,
            canDelete = true,
            canWithdraw = false,
        )
        isTravelExpenseSubmitted(status) -> MileageActionAvailability(
            canView = true,
            canEdit = false,
            canDelete = false,
            canWithdraw = true,
        )
        else -> MileageActionAvailability(
            canView = true,
            canEdit = false,
            canDelete = false,
            canWithdraw = false,
        )
    }
}

/** Reimbursement can only be requested while the expense is still a draft. */
private fun canRequestReimbursement(status: String?): Boolean = isTravelExpenseDraft(status)

private fun statusColors(status: String): Pair<Color, Color> {
    val normalized = status.trim().lowercase(Locale.getDefault())
    return when {
        isTravelExpenseDraft(normalized) -> Pair(Color(0xFFFFF9C4), Color(0xFF827717))
        isTravelExpenseSubmitted(normalized) -> Pair(Color(0xFFF5F5F5), Color(0xFF616161))
        normalized.contains("reject") -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    }
}

@Composable
private fun MileageStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 125.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(
                text = value,
                fontFamily = GraphikFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MileageTripCard(
    trip: MileageTripUi,
    modifier: Modifier = Modifier,
    onView: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onWithdraw: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.id,
                    fontFamily = GraphikFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                val (badgeBg, badgeText) = statusColors(trip.status)
                Text(
                    text = trip.status,
                    color = badgeText,
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Divider(color = Color(0xFFEAEAEA))

            MileageDetailRow("Customer Name", trip.customerName)
            MileageDetailRow("From", trip.fromDate)
            MileageDetailRow("To", trip.toDate)
            MileageDetailRow("Start", trip.startPoint)
            MileageDetailRow("End", trip.endPoint)
            MileageDetailRow("Type", trip.type)
            MileageDetailRow("Vehicle", trip.vehicle)
            MileageDetailRow("Amount", trip.amount)
            MileageDetailRow("Distance", trip.distance)

            Divider(color = Color(0xFFEAEAEA))

            val actions = mileageActionAvailability(trip.status)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MileageActionButton(
                    icon = Icons.Default.Visibility,
                    label = "View",
                    onClick = onView,
                    enabled = actions.canView
                )
                MileageActionButton(
                    icon = Icons.Default.Edit,
                    label = "Edit",
                    onClick = onEdit,
                    enabled = actions.canEdit
                )
                MileageActionButton(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    onClick = onDelete,
                    enabled = actions.canDelete
                )
                MileageActionButton(
                    icon = Icons.Default.Cancel,
                    label = "Withdraw",
                    onClick = onWithdraw,
                    enabled = actions.canWithdraw
                )
            }
        }
    }
}

@Composable
private fun MileageDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MileageActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val contentColor = if (enabled) PrimaryRed else Color(0xFFBDBDBD)
    Card(
        modifier = Modifier
            .height(40.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        backgroundColor = if (enabled) Color(0xFFF8F8F8) else Color(0xFFF1F1F1)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontFamily = GraphikFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MileageEmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No data",
            fontFamily = GraphikFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = message,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMileageExpenseBottomSheet(
    expense: MileageTripUi? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val tripController = remember { TravelExpenseController(context) }
    val projectOptions by tripController.projectOptions
    val projectOptionsLoading by tripController.projectOptionsLoading
    val projectOptionsError by tripController.projectOptionsError
    val vehicleAssets by tripController.vehicleAssets
    val vehicleAssetsLoading by tripController.vehicleAssetsLoading
    val vehicleAssetsError by tripController.vehicleAssetsError
    val personalVehicles by tripController.personalVehicles
    val personalVehiclesLoading by tripController.personalVehiclesLoading
    val personalVehiclesError by tripController.personalVehiclesError
    val mileageRate by tripController.mileageRate
    val mileageRateLoading by tripController.mileageRateLoading
    val mileageRateError by tripController.mileageRateError

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = remember { SimpleDateFormat("MM dd, yyyy", Locale.getDefault()) }

    var customerName by rememberSaveable { mutableStateOf("") }
    var selectedProjectCode by rememberSaveable { mutableStateOf("") }
    var selectedProjectId by rememberSaveable { mutableStateOf<Int?>(null) }
    var fromDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var toDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showFromDatePicker by rememberSaveable { mutableStateOf(false) }
    var showToDatePicker by rememberSaveable { mutableStateOf(false) }
    var startLocation by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var vehicleOwner by rememberSaveable { mutableStateOf("") }
    var vehicleType by rememberSaveable { mutableStateOf("") }
    var vehicle by rememberSaveable { mutableStateOf("") }
    var calculatedPrice by rememberSaveable { mutableStateOf("") }
    var showMapPicker by rememberSaveable { mutableStateOf(false) }
    var locationFieldMode by rememberSaveable { mutableStateOf("start") }
    var activeDestinationIndex by rememberSaveable { mutableStateOf(0) }
    var startPoint by rememberSaveable { mutableStateOf<GeoPoint?>(null) }
    val destinationStops = remember { mutableStateListOf(RouteStop()) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        tripController.fetchProjectOptions()
    }

    val fromDateText = fromDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select date"
    val toDateText = toDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select date"
    val projectIdOptions = if (projectOptions.isNotEmpty()) {
        projectOptions.map { it.code }
    } else {
        listOf("No project options available")
    }
    val selectedProject = projectOptions.firstOrNull { it.code == selectedProjectCode }
    val vehicleDropdownOptions = buildVehicleDropdownOptions(vehicleOwner, vehicleAssets, personalVehicles)
    val destinationPoints = destinationStops.map { it.point }
    val destinationNames = destinationStops.map { it.name }
    val isEditMode = expense != null

    LaunchedEffect(expense) {
        expense?.let { currentExpense ->
            customerName = currentExpense.customerName
            fromDateMillis = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentExpense.fromDate)?.time
            } catch (_: Exception) {
                null
            }
            toDateMillis = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentExpense.toDate)?.time
            } catch (_: Exception) {
                null
            }
            startLocation = currentExpense.startPoint
            startPoint = currentExpense.routePoints.firstOrNull()?.let { GeoPoint(it.latitude, it.longitude) }
            destinationStops.clear()
            if (currentExpense.routePoints.size > 1) {
                currentExpense.routePoints.drop(1).forEach { point ->
                    destinationStops.add(RouteStop(point.name, GeoPoint(point.latitude, point.longitude)))
                }
            }
            if (destinationStops.isEmpty()) {
                destinationStops.add(RouteStop())
            }
            distance = currentExpense.distance
            vehicleOwner = currentExpense.vehicleOwnershipType.uppercase(Locale.getDefault())
            vehicleType = currentExpense.vehicleTypeValue.uppercase(Locale.getDefault())
            vehicle = currentExpense.vehicleName.ifBlank { currentExpense.vehicle }
            selectedProjectId = currentExpense.projectId
            selectedProjectCode = ""
        }
    }

    LaunchedEffect(projectOptions, expense) {
        if (expense != null && selectedProjectCode.isBlank()) {
            selectedProjectCode = projectOptions.firstOrNull { it.id == expense.projectId }?.code.orEmpty()
        }
    }

    val sheetTitle = if (isEditMode) "Edit Mileage Expense" else "Add Mileage Expense"
    val submitButtonText = if (isEditMode) "Edit Expense" else "Save Expense"

    LaunchedEffect(vehicleOwner, vehicleType) {
        when {
            vehicleOwner.equals("COMPANY", ignoreCase = true) && vehicleType.isNotBlank() -> {
                tripController.fetchVehicleAssets(vehicleType)
                tripController.personalVehicles.value = emptyList()
            }
            vehicleOwner.equals("PERSONAL", ignoreCase = true) && vehicleType.isNotBlank() -> {
                tripController.fetchPersonalVehicles(vehicleType)
                tripController.vehicleAssets.value = emptyList()
            }
            else -> {
                tripController.vehicleAssets.value = emptyList()
                tripController.personalVehicles.value = emptyList()
            }
        }
    }

    LaunchedEffect(startPoint, destinationPoints) {
        val routePoints = listOfNotNull(startPoint) + destinationPoints.filterNotNull()
        distance = if (routePoints.size >= 2) calculateRouteDistanceInKm(routePoints) else ""
    }

    LaunchedEffect(
        customerName,
        selectedProjectCode,
        fromDateMillis,
        toDateMillis,
        startLocation,
        destinationNames,
        distance,
        vehicleOwner,
        vehicleType,
        vehicle,
        vehicleAssets,
        personalVehicles,
    ) {
        val hasRequiredFields = customerName.isNotBlank() &&
            selectedProjectCode.isNotBlank() &&
            fromDateMillis != null &&
            toDateMillis != null &&
            startLocation.isNotBlank() &&
            destinationNames.all { it.isNotBlank() } &&
            distance.isNotBlank() &&
            vehicleOwner.isNotBlank() &&
            vehicleType.isNotBlank() &&
            vehicle.isNotBlank()

        if (!hasRequiredFields) {
            calculatedPrice = ""
            return@LaunchedEffect
        }

        val selectedVehicleId = when {
            vehicleOwner.equals("COMPANY", ignoreCase = true) ->
                vehicleAssets.firstOrNull { it.makeModel.equals(vehicle, ignoreCase = true) }?.id
            vehicleOwner.equals("PERSONAL", ignoreCase = true) ->
                personalVehicles.firstOrNull { it.makeModel.equals(vehicle, ignoreCase = true) }?.id
            else -> null
        }
        val parsedDistance = distance.toBigDecimalOrNull()
        if (selectedVehicleId == null || parsedDistance == null) {
            calculatedPrice = ""
            return@LaunchedEffect
        }

        tripController.fetchMileageRate(
            vehicleId = selectedVehicleId,
            vehicleOwnershipType = vehicleOwner,
            vehicleType = vehicleType,
            onSuccess = { rateValue ->
                val rate = rateValue.toBigDecimalOrNull()
                calculatedPrice = if (rate != null) {
                    formatCurrencyValue(rate * parsedDistance)
                } else {
                    ""
                }
            },
            onError = { message ->
                calculatedPrice = ""
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color(0xFFF6F4EE),
        dragHandle = null,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sheetTitle,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Cancel",
                    color = PrimaryRed,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(PrimaryRed.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sheetTitle,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add and manage mileage expenses for travel reimbursements.",
                            fontFamily = GraphikFontFamily,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                MileageFormInput(
                    label = "Customer name",
                    placeholder = "Enter customer name",
                    value = customerName,
                    onValueChange = { customerName = it },
                )

                if (projectOptionsLoading) {
                    Text(
                        text = "Loading project options...",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                }
                if (!projectOptionsError.isNullOrBlank()) {
                    Text(
                        text = projectOptionsError!!,
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = PrimaryRed,
                    )
                }
                MileageDropdownField(
                    label = "Project ID",
                    value = selectedProjectCode,
                    options = projectIdOptions,
                    onValueChange = { code ->
                        selectedProjectCode = code
                        val selected = projectOptions.firstOrNull { it.code == code }
                        selectedProjectId = selected?.id
                        customerName = selected?.customerName.orEmpty()
                    },
                    enabled = projectOptions.isNotEmpty(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MileageDateField(
                        label = "From Date",
                        value = fromDateText,
                        onClick = { showFromDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    MileageDateField(
                        label = "To Date",
                        value = toDateText,
                        onClick = { showToDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                MileageLocationField(
                    label = "Start location",
                    placeholder = "Select starting location",
                    value = startLocation,
                    onClick = {
                        locationFieldMode = "start"
                        showMapPicker = true
                    },
                )
                destinationStops.forEachIndexed { index, stop ->
                    MileageLocationField(
                        label = if (index == 0) "Destination" else "Destination ${index + 1}",
                        placeholder = "Select destination",
                        value = stop.name,
                        onClick = {
                            locationFieldMode = "destination"
                            activeDestinationIndex = index
                            showMapPicker = true
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (index > 0) {
                                    IconButton(onClick = {
                                        destinationStops.removeAt(index)
                                        if (activeDestinationIndex >= destinationStops.size) {
                                            activeDestinationIndex = destinationStops.lastIndex.coerceAtLeast(0)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove location", tint = PrimaryRed)
                                    }
                                }
                                if (index == destinationStops.lastIndex) {
                                    IconButton(onClick = { destinationStops.add(RouteStop()) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add location", tint = PrimaryRed)
                                    }
                                }
                            }
                        },
                    )
                }
                MileageFormInput(
                    label = "Distance (km)",
                    placeholder = "Enter distance in km",
                    value = distance,
                    onValueChange = { distance = it },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MileageDropdownField(
                        label = "Vehicle Owner",
                        value = vehicleOwner,
                        options = listOf("PERSONAL", "COMPANY"),
                        onValueChange = { vehicleOwner = it },
                        modifier = Modifier.weight(1f)
                    )
                    MileageDropdownField(
                        label = "Vehicle Type",
                        value = vehicleType,
                        options = listOf("CAR", "BIKE"),
                        onValueChange = { vehicleType = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                val vehicleListLoading = if (vehicleOwner.equals("PERSONAL", ignoreCase = true)) personalVehiclesLoading else vehicleAssetsLoading
                val vehicleListError = if (vehicleOwner.equals("PERSONAL", ignoreCase = true)) personalVehiclesError else vehicleAssetsError
                if (vehicleListLoading) {
                    Text(
                        text = "Loading vehicles...",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                }
                if (!vehicleListError.isNullOrBlank()) {
                    Text(
                        text = vehicleListError,
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = PrimaryRed,
                    )
                }
                MileageDropdownField(
                    label = "Vehicle",
                    value = vehicle,
                    options = vehicleDropdownOptions,
                    onValueChange = { vehicle = it },
                    enabled = vehicleDropdownOptions.isNotEmpty(),
                    placeholder = if (vehicleDropdownOptions.isEmpty()) "No vehicles available" else "Select",
                )

                if (mileageRateLoading) {
                    Text(
                        text = "Loading mileage rate...",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                }
                if (!mileageRateError.isNullOrBlank()) {
                    Text(
                        text = mileageRateError!!,
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = PrimaryRed,
                    )
                }
                MileageFormInput(
                    label = "Price",
                    placeholder = "Price will be calculated automatically",
                    value = calculatedPrice,
                    enabled = false,
                    onValueChange = {},
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val parsedDistance = distance.toDoubleOrNull()
                    val parsedProjectId = selectedProjectId
                    val selectedVehicleId = when {
                        vehicleOwner.equals("COMPANY", ignoreCase = true) ->
                            vehicleAssets.firstOrNull { it.makeModel.equals(vehicle, ignoreCase = true) }?.id
                        vehicleOwner.equals("PERSONAL", ignoreCase = true) ->
                            personalVehicles.firstOrNull { it.makeModel.equals(vehicle, ignoreCase = true) }?.id
                        else -> null
                    }

                    if (customerName.isBlank()) {
                        Toast.makeText(context, "Please enter customer name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (parsedProjectId == null) {
                        Toast.makeText(context, "Please select a valid project", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (fromDateMillis == null || toDateMillis == null) {
                        Toast.makeText(context, "Please select both dates", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (startLocation.isBlank() || destinationStops.any { it.name.isBlank() }) {
                        Toast.makeText(context, "Please select start and destination locations", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (parsedDistance == null || parsedDistance <= 0) {
                        Toast.makeText(context, "Please enter a valid distance", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedVehicleId == null) {
                        Toast.makeText(context, "Please select a valid vehicle", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (vehicleOwner.isBlank() || vehicleType.isBlank() || vehicle.isBlank()) {
                        Toast.makeText(context, "Please select vehicle owner, type, and vehicle", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val requestPayload = com.archeGlobal.one.network.CreateMileageExpenseRequest(
                        fromDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(fromDateMillis!!)),
                        toDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(toDateMillis!!)),
                        route = listOf(
                            com.archeGlobal.one.network.MileageExpenseRoutePoint(
                                name = startLocation,
                                latitude = startPoint?.latitude ?: 0.0,
                                longitude = startPoint?.longitude ?: 0.0,
                            ),
                        ) + destinationStops.map { stop ->
                            com.archeGlobal.one.network.MileageExpenseRoutePoint(
                                name = stop.name,
                                latitude = stop.point?.latitude ?: 0.0,
                                longitude = stop.point?.longitude ?: 0.0,
                            )
                        },
                        vehicleId = selectedVehicleId,
                        vehicleOwnershipType = if (vehicleOwner.equals("COMPANY", ignoreCase = true)) "company" else "personal",
                        vehicleType = vehicleType.lowercase(Locale.getDefault()),
                        distance = parsedDistance,
                        customerName = customerName,
                        projectId = parsedProjectId ?: 0,
                        durationSeconds = 1,
                    )

                    if (isEditMode && expense != null) {
                        tripController.updateMileageExpense(
                            expense.expenseId,
                            requestPayload,
                            onSuccess = { onSuccess() },
                            onError = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        tripController.createMileageExpense(
                            request = requestPayload,
                            onSuccess = { onSuccess() },
                            onError = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(submitButtonText, fontFamily = GraphikFontFamily, fontSize = 16.sp)
            }
        }
    }

    if (showMapPicker) {
        val activeStop = destinationStops.getOrNull(activeDestinationIndex)
        LocationPickerBottomSheet(
            title = if (locationFieldMode == "start") "Select start location" else "Select destination",
            initialLocation = if (locationFieldMode == "start") startLocation else activeStop?.name.orEmpty(),
            initialPoint = if (locationFieldMode == "start") startPoint else activeStop?.point,
            onDismiss = { showMapPicker = false },
            onSelectLocation = { locationName, point ->
                if (locationFieldMode == "start") {
                    startLocation = locationName
                    startPoint = point
                } else if (activeDestinationIndex in destinationStops.indices) {
                    destinationStops[activeDestinationIndex] = RouteStop(name = locationName, point = point)
                }
                showMapPicker = false
            }
        )
    }

    if (showFromDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = fromDateMillis ?: System.currentTimeMillis(),
            selectableDates = remember { notAfterTodaySelectableDates() },
        )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fromDateMillis = millis
                        if (toDateMillis != null && toDateMillis!! < millis) {
                            toDateMillis = millis
                        }
                    }
                    showFromDatePicker = false
                }) {
                    Text("OK", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = toDateMillis ?: fromDateMillis ?: System.currentTimeMillis(),
            selectableDates = remember(fromDateMillis) { notAfterTodaySelectableDates(minMillis = fromDateMillis) },
        )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        if (fromDateMillis == null || millis >= fromDateMillis!!) {
                            toDateMillis = millis
                        }
                    }
                    showToDatePicker = false
                }) {
                    Text("OK", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun MileageLocationField(
    label: String,
    placeholder: String,
    value: String,
    onClick: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryRed,
                )
            },
            trailingIcon = trailingIcon,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color(0xFFD4D4D4),
                disabledBorderColor = Color(0xFFD4D4D4),
                disabledTextColor = if (value.isBlank()) Color.Gray else Color.Black,
            ),
        )
    }
}

@Composable
private fun MileageFormInput(
    label: String,
    placeholder: String,
    value: String,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color(0xFFD4D4D4),
                disabledBorderColor = Color(0xFFD4D4D4),
                disabledTextColor = Color.Gray,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPickerBottomSheet(
    title: String,
    initialLocation: String,
    initialPoint: GeoPoint? = null,
    onDismiss: () -> Unit,
    onSelectLocation: (String, GeoPoint) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentContext = context.applicationContext
    var currentLocation by remember(initialLocation) { mutableStateOf(initialLocation) }
    var selectedPoint by remember(initialPoint) { mutableStateOf(initialPoint) }
    var currentGeoPoint by remember(initialPoint) { mutableStateOf(initialPoint) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeTileSource by remember { mutableStateOf<OnlineTileSourceBase?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentGeoPoint(context) { point ->
                currentGeoPoint = point
                if (point != null) {
                    selectedPoint = point
                    currentLocation = resolveAddressFromPoint(context, point)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(currentContext, currentContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        val mapplsAvailable = probeMapplsAvailability()
        activeTileSource = if (mapplsAvailable) {
            buildMapplsTileSource()
        } else {
            Log.w("MileageMap", "Mappls tile probe failed; using OpenStreetMap fallback")
            null
        }
        getCurrentGeoPoint(context) { point ->
            currentGeoPoint = point
            if (point != null) {
                selectedPoint = point
                currentLocation = resolveAddressFromPoint(context, point)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = null,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black,
                )
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tap to select a point or long-press and drag to move the marker.",
                fontFamily = GraphikFontFamily,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            getCurrentGeoPoint(context) { point ->
                                currentGeoPoint = point
                                if (point != null) {
                                    selectedPoint = point
                                    currentLocation = resolveAddressFromPoint(context, point)
                                }
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = PrimaryRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Use current location", fontFamily = GraphikFontFamily, fontSize = 13.sp)
                }
                TextButton(onClick = { showSearchDialog = true }) {
                    Text("Search location", color = PrimaryRed, fontFamily = GraphikFontFamily, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val mapView = MapView(ctx)
                        val tileSource = activeTileSource ?: TileSourceFactory.MAPNIK
                        mapView.setTileSource(tileSource)
                        mapView.setMultiTouchControls(true)
                        mapView.controller.setZoom(12.0)
                        val initialPoint = selectedPoint ?: currentGeoPoint ?: initialPoint ?: GeoPoint(12.9716, 77.5946)
                        mapView.controller.setCenter(initialPoint)

                        val marker = Marker(mapView)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.icon = ContextCompat.getDrawable(ctx, android.R.drawable.ic_menu_mylocation)
                        mapView.overlays.add(marker)

                        val markerOverlay = object : Overlay() {
                            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                                val point = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as? GeoPoint
                                if (point != null) {
                                    selectedPoint = point
                                    currentLocation = resolveAddressFromPoint(ctx, point)
                                    marker.position = point
                                    marker.setInfoWindow(null)
                                    mapView.controller.animateTo(point)
                                    mapView.invalidate()
                                }
                                return true
                            }

                            override fun onLongPress(e: MotionEvent, mapView: MapView): Boolean {
                                val point = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as? GeoPoint
                                if (point != null) {
                                    selectedPoint = point
                                    currentLocation = resolveAddressFromPoint(ctx, point)
                                    marker.position = point
                                    marker.setInfoWindow(null)
                                    mapView.controller.animateTo(point)
                                    mapView.invalidate()
                                }
                                return true
                            }

                            override fun draw(ctx: android.graphics.Canvas, pMapView: MapView, shadow: Boolean) = Unit
                        }

                        mapView.overlays.add(markerOverlay)
                        mapView.setOnTouchListener { _, event ->
                            if (event.actionMasked == MotionEvent.ACTION_UP) {
                                val centerPoint = mapView.mapCenter as? GeoPoint
                                if (centerPoint != null) {
                                    selectedPoint = centerPoint
                                    currentLocation = resolveAddressFromPoint(ctx, centerPoint)
                                    marker.position = centerPoint
                                    marker.setInfoWindow(null)
                                    mapView.invalidate()
                                }
                            }
                            false
                        }
                        mapView
                    },
                    update = { mapView ->
                        val targetPoint = selectedPoint ?: currentGeoPoint ?: initialPoint
                        val tileSource = activeTileSource ?: TileSourceFactory.MAPNIK
                        if (mapView.tileProvider.tileSource != tileSource) {
                            mapView.setTileSource(tileSource)
                        }
                        if (targetPoint != null) {
                            val marker = mapView.overlays.filterIsInstance<Marker>().firstOrNull()
                            marker?.position = targetPoint
                            mapView.controller.animateTo(targetPoint)
                            mapView.invalidate()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = currentLocation.ifBlank { "No location selected yet" },
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = {
                    val targetPoint = selectedPoint ?: currentGeoPoint
                    targetPoint?.let { point ->
                        onSelectLocation(currentLocation, point)
                    }
                },
                enabled = (selectedPoint ?: currentGeoPoint) != null,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Use this location", fontFamily = GraphikFontFamily, fontSize = 15.sp)
            }
        }
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = {
                Text("Search location", fontFamily = GraphikFontFamily, fontWeight = FontWeight.SemiBold)
            },
            text = {
                Column {
                    Text(
                        text = "Enter a place name to find it on the map.",
                        fontFamily = GraphikFontFamily,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        placeholder = {
                            Text("e.g. Bengaluru, India", fontFamily = GraphikFontFamily, fontSize = 14.sp)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color(0xFFD4D4D4),
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    searchGeoPoint(context, searchQuery) { point, displayName ->
                        if (point != null) {
                            selectedPoint = point
                            currentGeoPoint = point
                            currentLocation = displayName.ifBlank { resolveAddressFromPoint(context, point) }
                        }
                        showSearchDialog = false
                    }
                }) {
                    Text("Search", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            }
        )
    }
}

@Composable
private fun MileageDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Select",
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value.ifBlank { placeholder },
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                },
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    disabledTextColor = if (value.isBlank()) Color.Gray else Color.Black,
                    disabledBorderColor = Color(0xFFD4D4D4),
                    disabledTrailingIconColor = Color.Gray,
                ),
            )
            if (enabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true },
                )
            }
            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                    ) {
                        Text(option, fontFamily = GraphikFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
private fun MileageDateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    disabledTextColor = if (value == "Select date") Color.Gray else Color.Black,
                    disabledBorderColor = Color(0xFFD4D4D4)
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onClick() }
            )
        }
    }
}
