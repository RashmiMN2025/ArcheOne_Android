package com.archeGlobal.one.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.archeGlobal.one.controller.TravelExpenseController
import com.archeGlobal.one.network.VehicleAssetItem
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class MileageTripUi(
    val id: String,
    val customerName: String,
    val date: String,
    val startPoint: String,
    val endPoint: String,
    val type: String,
    val vehicle: String,
    val amount: String,
    val distance: String,
    val status: String
)

internal fun buildVehicleDropdownOptions(
    vehicleOwner: String,
    vehicleAssets: List<VehicleAssetItem>,
): List<String> {
    if (!vehicleOwner.equals("COMPANY", ignoreCase = true)) {
        return emptyList()
    }

    return vehicleAssets
        .mapNotNull { it.makeModel?.takeIf(String::isNotBlank) }
        .distinct()
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

    val provider = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    val location = provider?.let { locationManager.getLastKnownLocation(it) }
    onResult(location?.let { GeoPoint(it.latitude, it.longitude) })
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

    var searchText by rememberSaveable { mutableStateOf("") }
    var showAddMileageSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        expenseController.fetchMileageExpenses()
    }

    val trips = remember(mileageExpenses) {
        mileageExpenses.map { expense ->
            MileageTripUi(
                id = expense.id,
                customerName = expense.customerName,
                date = expense.date,
                startPoint = expense.startPoint,
                endPoint = expense.endPoint,
                type = expense.type,
                vehicle = expense.vehicle,
                amount = expense.amount,
                distance = expense.distance,
                status = expense.status,
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalDistance = trips.sumOf { it.distance.filter { char -> char.isDigit() }.toLongOrNull() ?: 0L }
                    val totalAmount = trips.sumOf { it.amount.filter { char -> char.isDigit() }.toLongOrNull() ?: 0L }
                    MileageStatCard(
                        title = "Total distance logged",
                        value = "${totalDistance} km",
                        icon = Icons.Default.DirectionsCar,
                        modifier = Modifier.weight(1f)
                    )
                    MileageStatCard(
                        title = "Total Carbon Emission",
                        value = "0.00 Kg CO₂e",
                        icon = Icons.Default.Eco,
                        modifier = Modifier.weight(1f)
                    )
                    MileageStatCard(
                        title = "Total claim amount",
                        value = "Rs $totalAmount",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
        AddMileageExpenseBottomSheet(onDismiss = { showAddMileageSheet = false })
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
        modifier = modifier.height(125.dp),
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
                color = Color.Black
            )
        }
    }
}

@Composable
private fun MileageTripCard(
    trip: MileageTripUi,
    modifier: Modifier = Modifier
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
                Text(
                    text = trip.status,
                    color = Color(0xFF2E7D32),
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Divider(color = Color(0xFFEAEAEA))

            MileageDetailRow("Customer Name", trip.customerName)
            MileageDetailRow("Date", trip.date)
            MileageDetailRow("Route", "${trip.startPoint} → ${trip.endPoint}")
            MileageDetailRow("Type", trip.type)
            MileageDetailRow("Vehicle", trip.vehicle)
            MileageDetailRow("Amount", trip.amount)
            MileageDetailRow("Distance", trip.distance)

            Divider(color = Color(0xFFEAEAEA))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "View Details",
                    color = PrimaryRed,
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
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
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(130.dp)
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
private fun AddMileageExpenseBottomSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val tripController = remember { TravelExpenseController(context) }
    val projectOptions by tripController.projectOptions
    val projectOptionsLoading by tripController.projectOptionsLoading
    val projectOptionsError by tripController.projectOptionsError
    val vehicleAssets by tripController.vehicleAssets
    val vehicleAssetsLoading by tripController.vehicleAssetsLoading
    val vehicleAssetsError by tripController.vehicleAssetsError
    val mileageRate by tripController.mileageRate
    val mileageRateLoading by tripController.mileageRateLoading
    val mileageRateError by tripController.mileageRateError

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var customerName by rememberSaveable { mutableStateOf("") }
    var selectedProjectCode by rememberSaveable { mutableStateOf("") }
    var selectedProjectId by rememberSaveable { mutableStateOf<Int?>(null) }
    var fromDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var toDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showFromDatePicker by rememberSaveable { mutableStateOf(false) }
    var showToDatePicker by rememberSaveable { mutableStateOf(false) }
    var startLocation by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var vehicleOwner by rememberSaveable { mutableStateOf("") }
    var vehicleType by rememberSaveable { mutableStateOf("") }
    var vehicle by rememberSaveable { mutableStateOf("") }
    var calculatedPrice by rememberSaveable { mutableStateOf("") }
    var showMapPicker by rememberSaveable { mutableStateOf(false) }
    var locationFieldMode by rememberSaveable { mutableStateOf("start") }
    var selectedPoint by rememberSaveable { mutableStateOf<GeoPoint?>(null) }
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
    val vehicleDropdownOptions = buildVehicleDropdownOptions(vehicleOwner, vehicleAssets)

    LaunchedEffect(vehicleOwner, vehicleType) {
        if (vehicleOwner.equals("COMPANY", ignoreCase = true) && vehicleType.isNotBlank()) {
            tripController.fetchVehicleAssets(vehicleType)
        } else {
            tripController.vehicleAssets.value = emptyList()
        }
    }

    LaunchedEffect(
        customerName,
        selectedProjectCode,
        fromDateMillis,
        toDateMillis,
        startLocation,
        destination,
        distance,
        vehicleOwner,
        vehicleType,
        vehicle,
        vehicleAssets,
    ) {
        val hasRequiredFields = customerName.isNotBlank() &&
            selectedProjectCode.isNotBlank() &&
            fromDateMillis != null &&
            toDateMillis != null &&
            startLocation.isNotBlank() &&
            destination.isNotBlank() &&
            distance.isNotBlank() &&
            vehicleOwner.isNotBlank() &&
            vehicleType.isNotBlank() &&
            vehicle.isNotBlank()

        if (!hasRequiredFields) {
            calculatedPrice = ""
            return@LaunchedEffect
        }

        val selectedVehicle = vehicleAssets.firstOrNull { it.makeModel.equals(vehicle, ignoreCase = true) }
        val parsedDistance = distance.toBigDecimalOrNull()
        if (selectedVehicle?.id == null || parsedDistance == null) {
            calculatedPrice = ""
            return@LaunchedEffect
        }

        if (vehicleOwner.equals("COMPANY", ignoreCase = true)) {
            tripController.fetchMileageRate(
                vehicleId = selectedVehicle.id,
                vehicleOwnershipType = "company",
                vehicleType = vehicleType,
                onSuccess = { rateValue ->
                    val rate = rateValue.toBigDecimalOrNull()
                    if (rate != null) {
                        calculatedPrice = formatCurrencyValue(rate * parsedDistance)
                    } else {
                        calculatedPrice = ""
                    }
                },
                onError = {
                    calculatedPrice = ""
                }
            )
        } else {
            calculatedPrice = ""
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
                .fillMaxHeight(0.88f)
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
                    text = "Add Mileage Expense",
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
                            text = "Add Mileage Expense",
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
                        selectedProjectId = projectOptions.firstOrNull { it.code == code }?.id
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
                MileageLocationField(
                    label = "Destination",
                    placeholder = "Select destination",
                    value = destination,
                    onClick = {
                        locationFieldMode = "destination"
                        showMapPicker = true
                    },
                )
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
                if (vehicleAssetsLoading) {
                    Text(
                        text = "Loading vehicles...",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                }
                if (!vehicleAssetsError.isNullOrBlank()) {
                    Text(
                        text = vehicleAssetsError!!,
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
                    val selectedVehicle = vehicleAssets.firstOrNull { it.makeModel.equals(vehicle, ignoreCase = true) }

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
                    if (startLocation.isBlank() || destination.isBlank()) {
                        Toast.makeText(context, "Please select start and destination locations", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (parsedDistance == null || parsedDistance <= 0) {
                        Toast.makeText(context, "Please enter a valid distance", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedVehicle?.id == null) {
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
                                latitude = selectedPoint?.latitude ?: 0.0,
                                longitude = selectedPoint?.longitude ?: 0.0,
                            ),
                            com.archeGlobal.one.network.MileageExpenseRoutePoint(
                                name = destination,
                                latitude = selectedPoint?.latitude ?: 0.0,
                                longitude = selectedPoint?.longitude ?: 0.0,
                            )
                        ),
                        vehicleId = selectedVehicle?.id,
                        vehicleOwnershipType = if (vehicleOwner.equals("COMPANY", ignoreCase = true)) "company" else "personal",
                        vehicleType = vehicleType.lowercase(Locale.getDefault()),
                        distance = parsedDistance,
                        customerName = customerName,
                        projectId = parsedProjectId ?: 0,
                        durationSeconds = 1,
                    )

                    tripController.createMileageExpense(
                        request = requestPayload,
                        onSuccess = { onDismiss() },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
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
                Text("Save Expense", fontFamily = GraphikFontFamily, fontSize = 16.sp)
            }
        }
    }

    if (showMapPicker) {
        LocationPickerBottomSheet(
            title = if (locationFieldMode == "start") "Select start location" else "Select destination",
            initialLocation = if (locationFieldMode == "start") startLocation else destination,
            onDismiss = { showMapPicker = false },
            onSelectLocation = { locationName, point ->
                if (locationFieldMode == "start") {
                    startLocation = locationName
                } else {
                    destination = locationName
                }
                selectedPoint = point
                showMapPicker = false
            }
        )
    }

    if (showFromDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = fromDateMillis ?: System.currentTimeMillis()
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
            initialSelectedDateMillis = toDateMillis ?: fromDateMillis ?: System.currentTimeMillis()
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
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
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
    onDismiss: () -> Unit,
    onSelectLocation: (String, GeoPoint) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val currentContext = context.applicationContext
    var currentLocation by remember(initialLocation) { mutableStateOf(initialLocation) }
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var currentGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                        mapView.setTileSource(TileSourceFactory.MAPNIK)
                        mapView.setMultiTouchControls(true)
                        mapView.controller.setZoom(12.0)
                        val initialPoint = selectedPoint ?: currentGeoPoint ?: GeoPoint(12.9716, 77.5946)
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
                        val targetPoint = selectedPoint ?: currentGeoPoint
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
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value.ifBlank { "Select" },
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
