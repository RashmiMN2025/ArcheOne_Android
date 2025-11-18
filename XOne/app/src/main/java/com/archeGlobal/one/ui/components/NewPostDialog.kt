package com.archeGlobal.one.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.archeGlobal.one.network.CreatePostRequest
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.utils.UserDataManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostDialog(
    profilePicUrl: String? = null,
    userName: String = "User",
    userAccess: String = "",
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, category: String) -> Unit,
    onHistoryClick: () -> Unit = {},
    existingPost: com.archeGlobal.one.network.CreatedPost? = null,
) {
    // Form type switcher state - initialize based on existing post type
    var formType by remember {
        mutableStateOf(
            if (existingPost?.post_type == "homeView") "Event" else "Post"
        )
    }

    // Context and user data
    val context = LocalContext.current
    val userDataManager = UserDataManager.getInstance(context)
    val userData = userDataManager.getUserData()
    val coroutineScope = rememberCoroutineScope()

    // Submission state
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Fetched locations, departments, projects, and announcement categories
    var fetchedLocations by remember { mutableStateOf<List<String>>(emptyList()) }
    var fetchedDepartments by remember { mutableStateOf<List<String>>(emptyList()) }
    var fetchedProjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var announcementCategories by remember { mutableStateOf<List<com.archeGlobal.one.network.AnnouncementCategory>>(emptyList()) }

    // Determine if we're in edit mode
    val isEditMode = existingPost != null

    // Helper function to convert ISO date to display format (dd-MM-yyyy)
    fun formatISOToDisplayDate(isoDateString: String?): String {
        if (isoDateString.isNullOrEmpty()) return ""
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(isoDateString)

            if (date != null) {
                val displayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                displayFormat.format(date)
            } else {
                ""
            }
        } catch (e: Exception) {
            android.util.Log.e("NewPostDialog", "Error converting ISO to display date: $isoDateString", e)
            ""
        }
    }

    // Helper function to extract time from ISO date (HH:mm)
    fun formatISOToDisplayTime(isoDateString: String?): String {
        if (isoDateString.isNullOrEmpty()) return ""
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(isoDateString)

            if (date != null) {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeFormat.format(date)
            } else {
                ""
            }
        } catch (e: Exception) {
            android.util.Log.e("NewPostDialog", "Error converting ISO to display time: $isoDateString", e)
            ""
        }
    }

    // Post form states - initialize with existing post data if in edit mode
    var postType by remember { mutableStateOf(existingPost?.post_type ?: "Planned Post") }
    var postSubject by remember { mutableStateOf(existingPost?.subject ?: "Select Post Subject") }
    var postPriority by remember { mutableStateOf(existingPost?.priority ?: "Select Post Priority") }
    var postGroup by remember { mutableStateOf(
        when (existingPost?.target_group) {
            "DepartmentBased" -> "Department-based"
            "LocationBased" -> "Location-based"
            "EmployeeBased" -> "Employee-based"
            else -> "Employee-based"
        }
    ) }
    var employeeSearchQuery by remember { mutableStateOf("") }
    var selectedEmployee by remember { mutableStateOf<com.archeGlobal.one.model.SuggestedUser?>(null) }
    var suggestedEmployees by remember { mutableStateOf<List<com.archeGlobal.one.model.SuggestedUser>>(emptyList()) }
    var isSearchingEmployees by remember { mutableStateOf(false) }
    var department by remember { mutableStateOf(existingPost?.target_department?.firstOrNull() ?: "Select Department") }
    var location by remember { mutableStateOf(existingPost?.target_location?.firstOrNull() ?: "Select Location") }
    var project by remember { mutableStateOf("Select Project") }
    var announcementDescription by remember { mutableStateOf(existingPost?.description ?: "") }

    // Fetch locations, departments, projects, and announcement categories from API
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getLocationsAndDepartments()
            if (response.isSuccessful && response.body()?.status == 200) {
                val data = response.body()?.data
                fetchedLocations = data?.cities ?: emptyList()
                fetchedDepartments = data?.departments ?: emptyList()
                fetchedProjects = data?.projects ?: emptyList()
                announcementCategories = data?.announcementCategories ?: emptyList()
                android.util.Log.d("NewPostDialog", "Fetched announcement categories: ${announcementCategories.size}")
                android.util.Log.d("NewPostDialog", "Fetched projects: ${fetchedProjects.size}")
                android.util.Log.d("NewPostDialog", "User access: $userAccess")
            }
        } catch (e: Exception) {
            android.util.Log.e("NewPostDialog", "Error fetching data", e)
            // Handle error silently, keep empty lists
        }
    }

    // Search employees function
    fun searchEmployees(query: String) {
        employeeSearchQuery = query
        if (query.length < 2) {
            suggestedEmployees = emptyList()
            return
        }

        isSearchingEmployees = true
        val call = RetrofitClient.apiService.suggestUsers(query)
        call.enqueue(object : retrofit2.Callback<List<com.archeGlobal.one.model.SuggestedUser>> {
            override fun onResponse(
                call: retrofit2.Call<List<com.archeGlobal.one.model.SuggestedUser>>,
                response: retrofit2.Response<List<com.archeGlobal.one.model.SuggestedUser>>
            ) {
                isSearchingEmployees = false
                if (response.isSuccessful) {
                    suggestedEmployees = response.body() ?: emptyList()
                }
            }

            override fun onFailure(
                call: retrofit2.Call<List<com.archeGlobal.one.model.SuggestedUser>>,
                t: Throwable
            ) {
                isSearchingEmployees = false
                suggestedEmployees = emptyList()
            }
        })
    }
    var postStartDate by remember { mutableStateOf<String>(formatISOToDisplayDate(existingPost?.start_date)) }
    var postEndDate by remember { mutableStateOf<String>(formatISOToDisplayDate(existingPost?.end_date)) }
    var startDurationDate by remember { mutableStateOf<String>(formatISOToDisplayDate(existingPost?.activity_start)) }
    var startDurationTime by remember { mutableStateOf<String>(formatISOToDisplayTime(existingPost?.activity_start)) }
    var endDurationDate by remember { mutableStateOf<String>(formatISOToDisplayDate(existingPost?.activity_end)) }
    var endDurationTime by remember { mutableStateOf<String>(formatISOToDisplayTime(existingPost?.activity_end)) }
    var supportChannelDetails by remember { mutableStateOf(existingPost?.support_channel ?: "") }
    var postImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImageUrls by remember { mutableStateOf(existingPost?.image_urls ?: emptyList()) }
    var showPostPreview by remember { mutableStateOf(false) }

    // Event form states - initialize with existing post data if in edit mode and post type is event
    var eventSubject by remember {
        mutableStateOf(
            if (existingPost?.post_type == "homeView") existingPost.subject else ""
        )
    }
    var eventDescription by remember {
        mutableStateOf(
            if (existingPost?.post_type == "homeView") existingPost.description else ""
        )
    }
    var eventImageUri by remember { mutableStateOf<Uri?>(null) }
    var eventDate by remember { mutableStateOf<String>(formatISOToDisplayDate(existingPost?.event_date)) }
    var eventStartDate by remember {
        mutableStateOf<String>(
            if (existingPost?.post_type == "homeView") formatISOToDisplayDate(existingPost.start_date) else ""
        )
    }
    var eventEndDate by remember {
        mutableStateOf<String>(
            if (existingPost?.post_type == "homeView") formatISOToDisplayDate(existingPost.end_date) else ""
        )
    }
    var showEventPreview by remember { mutableStateOf(false) }

    val postTypes = listOf("Planned", "Unplanned/Emergency")

    val plannedSubjects = listOf(
        "Housekeeping schedule",
        "Pest control or deep cleaning activities",
        "Pantry & cafeteria updates",
        "Air conditioning or lighting maintenance",
        "Fire drills or emergency activities",
        "Lost & found notifications",
        "Security protocol reminders",
        "Access restriction or badge issues",
        "Lift/escalator maintenance",
        "Parking space updates",
        "Delivery or courier notifications",
        "Clean desk policy reminders",
        "Power outage or generator testing",
        "Visitor on floor alerts",
        "Noise level reminders",
        "Seating arrangement changes"
    )

    val unplannedSubjects = listOf(
        "Air conditioning/lighting maintenance",
        "Pantry & cafeteria update",
        "Fire drills/emergency",
        "Lost & found",
        "Lift/escalator maintenance",
        "Delivery or courier notification",
        "Power outage/generator testing",
        "Noise level reminder"
    )

    val postSubjects = if (postType == "Planned") plannedSubjects else unplannedSubjects
    val postPriorities = listOf("High", "Medium", "Low")
    val postGroups = listOf("Employee-based", "Department-based", "Location-based", "All")

    // Helper function to convert URI to MultipartBody.Part
    fun uriToMultipartBodyPart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            android.util.Log.d("NewPostDialog", "Converting URI to multipart: $uri")
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)

            if (inputStream == null) {
                android.util.Log.e("NewPostDialog", "Failed to open input stream for URI: $uri")
                return null
            }

            // Get filename
            var filename = "image.jpg"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                filename = cursor.getString(nameIndex)
            }
            android.util.Log.d("NewPostDialog", "Filename: $filename")

            // Create temp file
            val tempFile = File(context.cacheDir, filename)
            android.util.Log.d("NewPostDialog", "Creating temp file: ${tempFile.absolutePath}")

            FileOutputStream(tempFile).use { outputStream ->
                val bytesCopied = inputStream.copyTo(outputStream)
                android.util.Log.d("NewPostDialog", "Copied $bytesCopied bytes to temp file")
            }
            inputStream.close()

            android.util.Log.d("NewPostDialog", "Temp file size: ${tempFile.length()} bytes")

            // Create multipart body part
            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
            android.util.Log.d("NewPostDialog", "Multipart body part created successfully")
            part
        } catch (e: Exception) {
            android.util.Log.e("NewPostDialog", "Error converting URI to multipart", e)
            e.printStackTrace()
            null
        }
    }

    // Helper function to format date to ISO 8601
    fun formatDateToISO(dateString: String, timeString: String = "00:00"): String {
        return try {
            if (dateString.isEmpty()) {
                android.util.Log.d("NewPostDialog", "formatDateToISO: Empty date string, returning empty")
                return ""
            }

            android.util.Log.d("NewPostDialog", "formatDateToISO: Input date='$dateString', time='$timeString'")

            // Try to parse date with multiple formats
            val date = when {
                // ISO format (yyyy-MM-dd)
                dateString.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")) -> {
                    android.util.Log.d("NewPostDialog", "formatDateToISO: Date in ISO format")
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    isoFormat.parse(dateString.substring(0, 10))
                }
                // "dd MMM yyyy" format (e.g., "14 Nov 2025")
                dateString.matches(Regex("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")) -> {
                    android.util.Log.d("NewPostDialog", "formatDateToISO: Date in 'dd MMM yyyy' format")
                    val shortMonthFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                    shortMonthFormat.parse(dateString)
                }
                // dd-MM-yyyy format
                else -> {
                    android.util.Log.d("NewPostDialog", "formatDateToISO: Trying dd-MM-yyyy format")
                    val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    inputFormat.parse(dateString)
                }
            }

            if (date == null) {
                android.util.Log.e("NewPostDialog", "formatDateToISO: Failed to parse date '$dateString'")
                return ""
            }

            // Use UTC timezone for calendar to avoid timezone conversion issues
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = date
                // Parse time if provided
                if (timeString.isNotEmpty()) {
                    val timeParts = timeString.split(":")
                    if (timeParts.size == 2) {
                        val hour = timeParts[0].toIntOrNull() ?: 0
                        val minute = timeParts[1].toIntOrNull() ?: 0
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        android.util.Log.d("NewPostDialog", "formatDateToISO: Time set to $hour:$minute in UTC")
                    }
                } else {
                    // Set time to 00:00:00 if not provided
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }

            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val result = outputFormat.format(calendar.time)

            android.util.Log.d("NewPostDialog", "formatDateToISO: Output='$result'")
            result
        } catch (e: Exception) {
            android.util.Log.e("NewPostDialog", "formatDateToISO: Exception occurred", e)
            e.printStackTrace()
            ""
        }
    }

    // Submit post function
    fun submitPost() {
        android.util.Log.d("NewPostDialog", "=== POST CREATION STARTED ===")
        android.util.Log.d("NewPostDialog", "Form Type: $formType")
        android.util.Log.d("NewPostDialog", "User Email: ${userData?.email}")
        android.util.Log.d("NewPostDialog", "Username: ${userData?.name}")
        android.util.Log.d("NewPostDialog", "Employee ID: ${userData?.employeeId}")

        coroutineScope.launch {
            isSubmitting = true
            android.util.Log.d("NewPostDialog", "Submission state set to true")

            try {
                android.util.Log.d("NewPostDialog", "--- STEP 1: Preparing target group ---")

                // Determine target group (Events default to Everyone)
                val targetGroup = if (formType == "Event") {
                    "Everyone"
                } else {
                    when (postGroup) {
                        "Employee-based" -> "EmployeeBased"
                        "Department-based" -> "DepartmentBased"
                        "Location-based" -> "LocationBased"
                        "All" -> "Everyone"
                        else -> "Everyone"
                    }
                }
                android.util.Log.d("NewPostDialog", "Target Group: $targetGroup (from postGroup: $postGroup)")

                android.util.Log.d("NewPostDialog", "--- STEP 2: Preparing target arrays ---")

                // Prepare target arrays based on group (only for Posts)
                val targetDepartment = if (formType == "Post" && postGroup == "Department-based" && department != "Select Department") {
                    listOf(department)
                } else null
                android.util.Log.d("NewPostDialog", "Target Department: $targetDepartment")

                val targetLocation = if (formType == "Post" && postGroup == "Location-based" && location != "Select Location") {
                    listOf(location)
                } else null
                android.util.Log.d("NewPostDialog", "Target Location: $targetLocation")

                val targetEmployee = if (formType == "Post" && postGroup == "Employee-based" && selectedEmployee != null) {
                    listOf(selectedEmployee!!.mail)
                } else null
                android.util.Log.d("NewPostDialog", "Target Employee: $targetEmployee")

                android.util.Log.d("NewPostDialog", "--- STEP 3: Creating post request object ---")

                // Format activity start/end as ISO dates (combining date + time)
                val activityStartISO = if (startDurationDate.isNotEmpty() && startDurationTime.isNotEmpty()) {
                    formatDateToISO(startDurationDate, startDurationTime)
                } else null

                val activityEndISO = if (endDurationDate.isNotEmpty() && endDurationTime.isNotEmpty()) {
                    formatDateToISO(endDurationDate, endDurationTime)
                } else null

                android.util.Log.d("NewPostDialog", "Activity Start ISO: $activityStartISO")
                android.util.Log.d("NewPostDialog", "Activity End ISO: $activityEndISO")

                // Create post request
                val postRequest = CreatePostRequest(
                    user_email = userData?.email ?: "",
                    username = userData?.name ?: userName,
                    emp_id = userData?.employeeId ?: "",
                    profile_pic = userData?.profilePic,
                    post_type = if (formType == "Post") "headsUp" else "homeView",
                    subject = if (formType == "Post") postSubject else eventSubject,
                    priority = if (formType == "Post") postPriority else "Medium",
                    target_group = targetGroup,
                    target_department = targetDepartment,
                    target_location = targetLocation,
                    target_employee = targetEmployee,
                    description = if (formType == "Post") announcementDescription else eventDescription,
                    start_date = formatDateToISO(if (formType == "Post") postStartDate else eventStartDate),
                    end_date = formatDateToISO(if (formType == "Post") postEndDate else eventEndDate),
                    event_date = if (formType == "Event" && eventDate.isNotEmpty()) formatDateToISO(eventDate) else null,
                    activity_start = activityStartISO,
                    activity_end = activityEndISO,
                    support_channel = if (supportChannelDetails.isNotEmpty()) supportChannelDetails else null
                )

                android.util.Log.d("NewPostDialog", "Post Type: ${postRequest.post_type}")
                android.util.Log.d("NewPostDialog", "Subject: ${postRequest.subject}")
                android.util.Log.d("NewPostDialog", "Priority: ${postRequest.priority}")
                android.util.Log.d("NewPostDialog", "Description length: ${postRequest.description.length}")
                android.util.Log.d("NewPostDialog", "Start Date - Display: ${if (formType == "Post") postStartDate else eventStartDate} → ISO: ${postRequest.start_date}")
                android.util.Log.d("NewPostDialog", "End Date - Display: ${if (formType == "Post") postEndDate else eventEndDate} → ISO: ${postRequest.end_date}")
                android.util.Log.d("NewPostDialog", "Event Date - Display: $eventDate → ISO: ${postRequest.event_date}")
                android.util.Log.d("NewPostDialog", "Activity Start - Display: $startDurationDate $startDurationTime → ISO: ${postRequest.activity_start}")
                android.util.Log.d("NewPostDialog", "Activity End - Display: $endDurationDate $endDurationTime → ISO: ${postRequest.activity_end}")

                android.util.Log.d("NewPostDialog", "--- STEP 4: Validating ISO dates ---")

                // Validate ISO dates
                val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                isoDateFormat.timeZone = TimeZone.getTimeZone("UTC")

                val datesToValidate = mapOf(
                    "start_date" to postRequest.start_date,
                    "end_date" to postRequest.end_date,
                    "activity_start" to postRequest.activity_start,
                    "activity_end" to postRequest.activity_end
                )

                datesToValidate.forEach { (fieldName, dateValue) ->
                    if (dateValue != null && dateValue.isNotEmpty()) {
                        try {
                            val parsedDate = isoDateFormat.parse(dateValue)
                            if (parsedDate != null) {
                                android.util.Log.d("NewPostDialog", "✓ $fieldName is valid ISO date: $dateValue")
                            } else {
                                android.util.Log.e("NewPostDialog", "✗ $fieldName parsing returned null: $dateValue")
                                throw IllegalArgumentException("Invalid ISO date in $fieldName: $dateValue")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("NewPostDialog", "✗ $fieldName has invalid ISO date: $dateValue - ${e.message}")
                            throw IllegalArgumentException("Invalid ISO date in $fieldName: $dateValue")
                        }
                    }
                }

                android.util.Log.d("NewPostDialog", "--- STEP 5: Converting to JSON ---")

                // Convert to JSON
                val gson = Gson()
                val postJson = gson.toJson(postRequest)
                android.util.Log.d("NewPostDialog", "JSON Data: $postJson")

                val postRequestBody = postJson.toRequestBody("application/json".toMediaTypeOrNull())
                android.util.Log.d("NewPostDialog", "Request body created successfully")

                android.util.Log.d("NewPostDialog", "--- STEP 6: Processing images ---")

                // Convert image URIs to multipart
                val imageParts = if (formType == "Post") {
                    android.util.Log.d("NewPostDialog", "Processing ${postImageUris.size} post images")
                    postImageUris.mapIndexedNotNull { index, uri ->
                        android.util.Log.d("NewPostDialog", "Converting image $index: $uri")
                        val part = uriToMultipartBodyPart(context, uri, "files")
                        if (part != null) {
                            android.util.Log.d("NewPostDialog", "Image $index converted successfully")
                        } else {
                            android.util.Log.e("NewPostDialog", "Failed to convert image $index")
                        }
                        part
                    }
                } else {
                    if (eventImageUri != null) {
                        android.util.Log.d("NewPostDialog", "Processing 1 event image: $eventImageUri")
                        val part = uriToMultipartBodyPart(context, eventImageUri!!, "files")
                        if (part != null) {
                            android.util.Log.d("NewPostDialog", "Event image converted successfully")
                            listOf(part)
                        } else {
                            android.util.Log.e("NewPostDialog", "Failed to convert event image")
                            emptyList()
                        }
                    } else {
                        android.util.Log.d("NewPostDialog", "No event image to process")
                        emptyList()
                    }
                }
                android.util.Log.d("NewPostDialog", "Total images ready for upload: ${imageParts?.size ?: 0}")

                android.util.Log.d("NewPostDialog", "--- STEP 7: Making API call ---")
                android.util.Log.d("NewPostDialog", "API Endpoint: POST /announcements/v1/post")
                android.util.Log.d("NewPostDialog", "Number of file parts: ${imageParts?.size ?: 0}")

                val response = withContext(Dispatchers.IO) {
                    android.util.Log.d("NewPostDialog", "Executing API request...")
                    try {
                        val result = if (isEditMode && existingPost != null) {
                            android.util.Log.d("NewPostDialog", "Updating existing post: ${existingPost.post_id}")
                            RetrofitClient.apiService.updatePost(existingPost.post_id, postRequestBody, imageParts)
                        } else {
                            android.util.Log.d("NewPostDialog", "Creating new post")
                            RetrofitClient.apiService.createPost(postRequestBody, imageParts)
                        }
                        android.util.Log.d("NewPostDialog", "API request completed")
                        result
                    } catch (e: Exception) {
                        android.util.Log.e("NewPostDialog", "API request threw exception", e)
                        throw e
                    }
                }

                android.util.Log.d("NewPostDialog", "--- STEP 8: Processing response ---")
                android.util.Log.d("NewPostDialog", "Response Code: ${response.code()}")
                android.util.Log.d("NewPostDialog", "Response Message: ${response.message()}")
                android.util.Log.d("NewPostDialog", "Response Body: ${response.body()}")
                android.util.Log.d("NewPostDialog", "Response Error Body: ${response.errorBody()?.string()}")

                isSubmitting = false
                android.util.Log.d("NewPostDialog", "Submission state set to false")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val responseStatus = if (isEditMode) {
                        (responseBody as? com.archeGlobal.one.network.UpdatePostResponse)?.status
                    } else {
                        (responseBody as? com.archeGlobal.one.network.CreatePostResponse)?.status
                    }

                    if (responseStatus == 200) {
                        android.util.Log.d("NewPostDialog", "✓ SUCCESS: Post ${if (isEditMode) "updated" else "created"}!")
                        showSuccessDialog = true
                    } else {
                        val errorMsg = if (isEditMode) {
                            (responseBody as? com.archeGlobal.one.network.UpdatePostResponse)?.message ?: "Failed to update post"
                        } else {
                            (responseBody as? com.archeGlobal.one.network.CreatePostResponse)?.error ?: "Failed to create post"
                        }
                        errorMessage = errorMsg
                        android.util.Log.e("NewPostDialog", "✗ FAILED: $errorMessage")
                        showErrorDialog = true
                    }
                } else {
                    errorMessage = response.errorBody()?.string() ?: "Failed to ${if (isEditMode) "update" else "create"} post (Code: ${response.code()})"
                    android.util.Log.e("NewPostDialog", "✗ FAILED: $errorMessage")
                    showErrorDialog = true
                }
                android.util.Log.d("NewPostDialog", "=== POST CREATION COMPLETED ===")
            } catch (e: Exception) {
                android.util.Log.e("NewPostDialog", "=== POST CREATION FAILED WITH EXCEPTION ===")
                android.util.Log.e("NewPostDialog", "Exception Type: ${e.javaClass.simpleName}")
                android.util.Log.e("NewPostDialog", "Exception Message: ${e.message}")
                android.util.Log.e("NewPostDialog", "Stack Trace:", e)
                isSubmitting = false
                errorMessage = e.message ?: "An error occurred"
                showErrorDialog = true
            }
        }
    }

    // Reset post subject when post type changes (but not during initial load in edit mode)
    var isInitialLoad by remember { mutableStateOf(true) }
    LaunchedEffect(postType) {
        if (!isInitialLoad) {
            postSubject = "Select Post Subject"
        } else {
            isInitialLoad = false
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4EE))
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (formType == "Post") "Create Post" else "Create Event",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(x = 24.dp) // Standard offset for proper centering
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .clickable { onHistoryClick() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "History",
                            color = PrimaryRed,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp, // Matching DeskCartScreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = PrimaryRed,
                            modifier = Modifier.size(20.dp) // Matching DeskCartScreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Form Type Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { formType = "Post" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formType == "Post") PrimaryRed else Color.White,
                            contentColor = if (formType == "Post") Color.White else Color.Black,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "HeadsUp",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = { formType = "Event" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formType == "Event") PrimaryRed else Color.White,
                            contentColor = if (formType == "Event") Color.White else Color.Black,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Home Page",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Show Post Form or Event Form based on formType
                    if (formType == "Post") {
                        // POST FORM CONTENT
                        PostFormContent(
                            userAccess = userAccess,
                            announcementCategories = announcementCategories,
                            postType = postType,
                            onPostTypeChange = { postType = it },
                            postSubject = postSubject,
                            onPostSubjectChange = { postSubject = it },
                            postPriority = postPriority,
                            onPostPriorityChange = { postPriority = it },
                            postGroup = postGroup,
                            onPostGroupChange = { postGroup = it },
                            employeeSearchQuery = employeeSearchQuery,
                            onEmployeeSearchQueryChange = { searchEmployees(it) },
                            selectedEmployee = selectedEmployee,
                            onEmployeeSelected = { selectedEmployee = it; suggestedEmployees = emptyList() },
                            suggestedEmployees = suggestedEmployees,
                            isSearchingEmployees = isSearchingEmployees,
                            department = department,
                            onDepartmentChange = { department = it },
                            location = location,
                            onLocationChange = { location = it },
                            project = project,
                            onProjectChange = { project = it },
                            fetchedDepartments = fetchedDepartments,
                            fetchedLocations = fetchedLocations,
                            fetchedProjects = fetchedProjects,
                            announcementDescription = announcementDescription,
                            onAnnouncementDescriptionChange = { announcementDescription = it },
                            postStartDate = postStartDate,
                            onPostStartDateChange = { postStartDate = it },
                            postEndDate = postEndDate,
                            onPostEndDateChange = { postEndDate = it },
                            startDurationDate = startDurationDate,
                            onStartDurationDateChange = { startDurationDate = it },
                            startDurationTime = startDurationTime,
                            onStartDurationTimeChange = { startDurationTime = it },
                            endDurationDate = endDurationDate,
                            onEndDurationDateChange = { endDurationDate = it },
                            endDurationTime = endDurationTime,
                            onEndDurationTimeChange = { endDurationTime = it },
                            supportChannelDetails = supportChannelDetails,
                            onSupportChannelDetailsChange = { supportChannelDetails = it },
                            postImageUris = postImageUris,
                            onPostImageUrisChange = { postImageUris = it },
                            existingImageUrls = existingImageUrls,
                            onExistingImageUrlsChange = { existingImageUrls = it },
                            isEditMode = isEditMode,
                            onPreview = { showPostPreview = true },
                            onSubmit = {
                                android.util.Log.d("NewPostDialog", "Post button clicked!")
                                submitPost()
                            },
                            isSubmitting = isSubmitting,
                        )
                    } else {
                        // EVENT FORM CONTENT
                        EventFormContent(
                            eventSubject = eventSubject,
                            onEventSubjectChange = { eventSubject = it },
                            eventDescription = eventDescription,
                            onEventDescriptionChange = { eventDescription = it },
                            eventImageUri = eventImageUri,
                            onEventImageUriChange = { eventImageUri = it },
                            existingImageUrls = existingImageUrls,
                            onExistingImageUrlsChange = { existingImageUrls = it },
                            eventDate = eventDate,
                            onEventDateChange = { eventDate = it },
                            eventStartDate = eventStartDate,
                            onEventStartDateChange = { eventStartDate = it },
                            eventEndDate = eventEndDate,
                            onEventEndDateChange = { eventEndDate = it },
                            onPreview = { showEventPreview = true },
                            onSubmit = {
                                android.util.Log.d("NewPostDialog", "Event button clicked!")
                                submitPost()
                            },
                            isSubmitting = isSubmitting,
                        )
                    }
                }
            }
        }

        // Show Post Preview Dialog
        if (showPostPreview) {
            PostPreviewDialog(
                profilePicUrl = profilePicUrl,
                userName = userName,
                postType = postType,
                postSubject = postSubject,
                postPriority = postPriority,
                postGroup = postGroup,
                employee = selectedEmployee?.displayName ?: "",
                announcementDescription = announcementDescription,
                postStartDate = postStartDate,
                postEndDate = postEndDate,
                startDurationDate = startDurationDate,
                startDurationTime = startDurationTime,
                endDurationDate = endDurationDate,
                endDurationTime = endDurationTime,
                supportChannelDetails = supportChannelDetails,
                postImageUris = postImageUris,
                onDismiss = { showPostPreview = false }
            )
        }

        // Show Event Preview Dialog
        if (showEventPreview) {
            EventPreviewDialog(
                eventSubject = eventSubject,
                eventDescription = eventDescription,
                eventImageUri = eventImageUri,
                eventDate = eventDate,
                eventStartDate = eventStartDate,
                eventEndDate = eventEndDate,
                onDismiss = { showEventPreview = false }
            )
        }

        // Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    android.util.Log.d("NewPostDialog", "Success dialog dismissed - calling onSubmit callback")
                    showSuccessDialog = false
                    val title = if (formType == "Post") postSubject else eventSubject
                    val description = if (formType == "Post") announcementDescription else eventDescription
                    onSubmit(title, description, formType)
                },
                title = {
                    Text(
                        text = "Success!",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text(
                        text = "Your ${if (formType == "Post") "post" else "event"} has been ${if (isEditMode) "updated" else "created"} successfully.",
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            android.util.Log.d("NewPostDialog", "Success dialog OK clicked - calling onSubmit callback")
                            showSuccessDialog = false
                            val title = if (formType == "Post") postSubject else eventSubject
                            val description = if (formType == "Post") announcementDescription else eventDescription
                            onSubmit(title, description, formType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                    ) {
                        Text("OK", fontFamily = GraphikFontFamily)
                    }
                }
            )
        }

        // Error Dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = {
                    Text(
                        text = "Error",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Red
                    )
                },
                text = {
                    Text(
                        text = errorMessage,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showErrorDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                    ) {
                        Text("OK", fontFamily = GraphikFontFamily)
                    }
                }
            )
        }
    }
}

@Composable
private fun PostFormContent(
    userAccess: String,
    announcementCategories: List<com.archeGlobal.one.network.AnnouncementCategory>,
    postType: String,
    onPostTypeChange: (String) -> Unit,
    postSubject: String,
    onPostSubjectChange: (String) -> Unit,
    postPriority: String,
    onPostPriorityChange: (String) -> Unit,
    postGroup: String,
    onPostGroupChange: (String) -> Unit,
    employeeSearchQuery: String,
    onEmployeeSearchQueryChange: (String) -> Unit,
    selectedEmployee: com.archeGlobal.one.model.SuggestedUser?,
    onEmployeeSelected: (com.archeGlobal.one.model.SuggestedUser) -> Unit,
    suggestedEmployees: List<com.archeGlobal.one.model.SuggestedUser>,
    isSearchingEmployees: Boolean,
    department: String,
    onDepartmentChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    project: String,
    onProjectChange: (String) -> Unit,
    fetchedDepartments: List<String>,
    fetchedLocations: List<String>,
    fetchedProjects: List<String>,
    announcementDescription: String,
    onAnnouncementDescriptionChange: (String) -> Unit,
    postStartDate: String,
    onPostStartDateChange: (String) -> Unit,
    postEndDate: String,
    onPostEndDateChange: (String) -> Unit,
    startDurationDate: String,
    onStartDurationDateChange: (String) -> Unit,
    startDurationTime: String,
    onStartDurationTimeChange: (String) -> Unit,
    endDurationDate: String,
    onEndDurationDateChange: (String) -> Unit,
    endDurationTime: String,
    onEndDurationTimeChange: (String) -> Unit,
    supportChannelDetails: String,
    onSupportChannelDetailsChange: (String) -> Unit,
    postImageUris: List<Uri>,
    onPostImageUrisChange: (List<Uri>) -> Unit,
    existingImageUrls: List<String>,
    onExistingImageUrlsChange: (List<String>) -> Unit,
    isEditMode: Boolean = false,
    onPreview: () -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean = false,
) {
    // Find the announcement category for the current user's access level
    val userCategory = announcementCategories.find {
        it.access.equals(userAccess, ignoreCase = true)
    }

    // Get post types and subjects based on user access
    val postTypes = mutableListOf<String>()
    val postSubjectsByType = mutableMapOf<String, List<String>>()

    userCategory?.fields?.forEach { field ->
        val categoryName = when {
            field.category.contains("Planned & Unplanned", ignoreCase = true) -> {
                // Split into two separate categories
                postTypes.add("Planned Post")
                postTypes.add("Unplanned Post")
                postSubjectsByType["Planned Post"] = field.subcategory
                postSubjectsByType["Unplanned Post"] = field.subcategory
                null
            }
            field.category.contains("Planned", ignoreCase = true) -> "Planned Post"
            field.category.contains("Unplanned", ignoreCase = true) -> "Unplanned Post"
            else -> field.category
        }

        categoryName?.let {
            if (!postTypes.contains(it)) {
                postTypes.add(it)
            }
            postSubjectsByType[it] = field.subcategory
        }
    }

    // Remove duplicates
    val uniquePostTypes = postTypes.distinct()

    // Get subjects for the currently selected post type
    val postSubjects = postSubjectsByType[postType] ?: emptyList()

    android.util.Log.d("PostFormContent", "User access: $userAccess")
    android.util.Log.d("PostFormContent", "Available post types: $uniquePostTypes")
    android.util.Log.d("PostFormContent", "Current post type: $postType")
    android.util.Log.d("PostFormContent", "Available subjects: $postSubjects")
    val postPriorities = listOf("High", "Medium", "Low")
    val postGroups = listOf("Everyone@Arche", "Department Based", "Location Based", "Project Based", "Employee Based")

    // Use fetched data, fallback to empty if not loaded yet
    val departments = fetchedDepartments
    val locations = fetchedLocations
    val projects = fetchedProjects

    // Image picker launcher for multiple images (max 3)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val totalImages = existingImageUrls.size + postImageUris.size
            if (totalImages < 3) {
                onPostImageUrisChange(postImageUris + it)
            }
        }
    }

    // Post Type Dropdown
    PostDropdown(
        label = "Post Type",
        selectedValue = postType,
        options = uniquePostTypes,
        onValueSelected = onPostTypeChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Subject Dropdown
    PostDropdown(
        label = "Post Subject",
        selectedValue = postSubject,
        options = postSubjects,
        onValueSelected = onPostSubjectChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Priority Dropdown
    PostDropdown(
        label = "Post Priority",
        selectedValue = postPriority,
        options = postPriorities,
        onValueSelected = onPostPriorityChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Group Dropdown
    PostDropdown(
        label = "Post Group",
        selectedValue = postGroup,
        options = postGroups,
        onValueSelected = onPostGroupChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Conditional field based on Post Group selection
    when (postGroup) {
        "Employee Based" -> {
            // Employee Search Field with suggestions
            Column {
                // Search field
                OutlinedTextField(
                    value = employeeSearchQuery,
                    onValueChange = { query ->
                        onEmployeeSearchQueryChange(query)
                    },
                    placeholder = {
                        Text(
                            text = "Search Member to add",
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                    ),
                    textStyle = TextStyle(
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    trailingIcon = {
                        if (employeeSearchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onEmployeeSearchQueryChange("")
                            }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                    contentDescription = "Clear",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Loading indicator
                if (isSearchingEmployees) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF1976D2),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Searching members...",
                            fontFamily = GraphikFontFamily,
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }
                }

                // Suggestions list
                if (suggestedEmployees.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            suggestedEmployees.take(5).forEach { employee ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onEmployeeSelected(employee)
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = employee.displayName,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = employee.mail,
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                        )
                                    }
                                }
                                if (employee != suggestedEmployees.take(5).last()) {
                                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Display selected employee
                if (selectedEmployee != null && selectedEmployee?.mail?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Selected Employee",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedEmployee?.displayName ?: "",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                            )
                            Text(
                                text = selectedEmployee?.mail ?: "",
                                fontFamily = GraphikFontFamily,
                                fontSize = 10.sp,
                                color = Color.Gray,
                            )
                        }
                        IconButton(
                            onClick = {
                                onEmployeeSelected(com.archeGlobal.one.model.SuggestedUser("", ""))
                            },
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.bin),
                                contentDescription = "Remove",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Department Based" -> {
            // Department Dropdown
            PostDropdown(
                label = "Department",
                selectedValue = department,
                options = departments,
                onValueSelected = onDepartmentChange,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Location Based" -> {
            // Location Dropdown
            PostDropdown(
                label = "Location",
                selectedValue = location,
                options = locations,
                onValueSelected = onLocationChange,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Project Based" -> {
            // Project Dropdown
            PostDropdown(
                label = "Project",
                selectedValue = project,
                options = projects,
                onValueSelected = onProjectChange,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Everyone@Arche" -> {
            // No field for Everyone@Arche
        }
    }

    // Announcement Description (multiline)
    PostTextField(
        label = "Announcement Description",
        value = announcementDescription,
        onValueChange = onAnnouncementDescriptionChange,
        placeholder = "Announcement Description...",
        singleLine = false,
        minLines = 5,
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Select images to attach button (max 3 images)
    val totalImages = existingImageUrls.size + postImageUris.size
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = if (totalImages < 3) Color.White else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = totalImages < 3) {
                if (totalImages < 3) {
                    imagePickerLauncher.launch("image/*")
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_gallery),
                contentDescription = "Gallery",
                tint = if (totalImages < 3) Color.Gray else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (totalImages < 3)
                    "Select images to attach ($totalImages/3)"
                else
                    "Maximum 3 images reached",
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                color = if (totalImages < 3) Color.Gray else Color.Gray.copy(alpha = 0.5f),
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Attached Photos Section (show if there are existing images OR new images)
    if (existingImageUrls.isNotEmpty() || postImageUris.isNotEmpty()) {
        Text(
            text = "Attached Photos",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Horizontal scrollable row of images
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Display existing images (from URL)
            existingImageUrls.forEach { imageUrl ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Existing Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Remove image button (X)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(20.dp)
                            .background(
                                color = PrimaryRed,
                                shape = CircleShape
                            )
                            .clickable {
                                onExistingImageUrlsChange(existingImageUrls.filter { it != imageUrl })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Display newly selected images (from URI)
            postImageUris.forEach { uri ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "New Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Remove image button (X)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(20.dp)
                            .background(
                                color = PrimaryRed,
                                shape = CircleShape
                            )
                            .clickable {
                                onPostImageUrisChange(postImageUris.filter { it != uri })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Post Start Date
    PostDateField(
        label = "Post Start Date",
        value = postStartDate,
        onValueChange = onPostStartDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post End Date
    PostDateField(
        label = "Post End Date",
        value = postEndDate,
        onValueChange = onPostEndDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Activity Duration Section
    Text(
        text = "Activity Duration",
        fontFamily = GraphikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 12.dp),
    )

    // Start Duration Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Start\nDuration",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.width(80.dp),
            lineHeight = 18.sp
        )
        PostDateField(
            label = "",
            value = startDurationDate,
            onValueChange = onStartDurationDateChange,
            compact = true,
            modifier = Modifier.weight(1f)
        )
        PostTimeField(
            label = "",
            value = startDurationTime,
            onValueChange = onStartDurationTimeChange,
            compact = true,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // End Duration Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "End\nDuration",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.width(80.dp),
            lineHeight = 18.sp
        )
        PostDateField(
            label = "",
            value = endDurationDate,
            onValueChange = onEndDurationDateChange,
            compact = true,
            modifier = Modifier.weight(1f)
        )
        PostTimeField(
            label = "",
            value = endDurationTime,
            onValueChange = onEndDurationTimeChange,
            compact = true,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Support Channel Details (optional)
    PostTextField(
        label = "Support Channel Details (optional)",
        value = supportChannelDetails,
        onValueChange = onSupportChannelDetailsChange,
        placeholder = "Support Channel Details (optional)",
        singleLine = true,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Preview and Post Buttons Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Preview Button
        Button(
            onClick = onPreview,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_view_eye),
                contentDescription = "Preview",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Preview",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
        }

        // Post Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSubmitting && announcementDescription.isNotBlank() && postSubject != "Select Post Subject" && postPriority != "Select Post Priority",
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = if (isEditMode) "Update" else "Post",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEditMode) "Update" else "Post",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        }
    }

    Spacer(modifier = Modifier.height(48.dp))
}

@Composable
private fun EventFormContent(
    eventSubject: String,
    onEventSubjectChange: (String) -> Unit,
    eventDescription: String,
    onEventDescriptionChange: (String) -> Unit,
    eventImageUri: Uri?,
    onEventImageUriChange: (Uri?) -> Unit,
    existingImageUrls: List<String>,
    onExistingImageUrlsChange: (List<String>) -> Unit,
    eventDate: String,
    onEventDateChange: (String) -> Unit,
    eventStartDate: String,
    onEventStartDateChange: (String) -> Unit,
    eventEndDate: String,
    onEventEndDateChange: (String) -> Unit,
    onPreview: () -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean = false,
) {
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onEventImageUriChange(it)
        }
    }
    // Post Subject (Event Subject)
    PostTextField(
        label = "Post Subject",
        value = eventSubject,
        onValueChange = onEventSubjectChange,
        placeholder = "Enter Subject",
        singleLine = true,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Note about home screen display
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "*",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Red,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "This will be displayed on the home screen",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Gray,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Post Description (Event Description)
    PostTextField(
        label = "Post Description",
        value = eventDescription,
        onValueChange = onEventDescriptionChange,
        placeholder = "Enter post description...",
        singleLine = false,
        minLines = 5,
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Select image to attach button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { imagePickerLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_gallery),
                contentDescription = "Gallery",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Select image to attach",
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                color = Color.Gray,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Attached Photo Section (show if there are existing images OR new image)
    if (existingImageUrls.isNotEmpty() || eventImageUri != null) {
        Text(
            text = "Attached Photo",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Horizontal scrollable row for images
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Display existing images (from URL)
            existingImageUrls.forEach { imageUrl ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Existing Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Remove image button (X)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(20.dp)
                            .background(
                                color = PrimaryRed,
                                shape = CircleShape
                            )
                            .clickable {
                                onExistingImageUrlsChange(existingImageUrls.filter { it != imageUrl })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Display new image (from URI)
            eventImageUri?.let { uri ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "New Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Remove image button (X)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(20.dp)
                            .background(
                                color = PrimaryRed,
                                shape = CircleShape
                            )
                            .clickable {
                                onEventImageUriChange(null)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    // Event Date
    PostDateField(
        label = "Event Date",
        value = eventDate,
        onValueChange = onEventDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Start Date
    PostDateField(
        label = "Post Start Date",
        value = eventStartDate,
        onValueChange = onEventStartDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post End Date
    PostDateField(
        label = "Post End Date",
        value = eventEndDate,
        onValueChange = onEventEndDateChange,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Preview and Create Event Buttons Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Preview Button
        Button(
            onClick = onPreview,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.archeGlobal.one.R.drawable.ic_view_eye),
                contentDescription = "Preview",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Preview",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
        }

        // Create Event Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSubmitting && eventSubject.isNotBlank() && eventDescription.isNotBlank(),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Create Event",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Event",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }

    Spacer(modifier = Modifier.height(48.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = PrimaryRed,
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                ),
                textStyle = TextStyle(
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontFamily = GraphikFontFamily,
                                fontSize = 16.sp,
                                color = Color.Black,
                            )
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PostTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    Column {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder.isNotEmpty()) {
                {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp,
                    )
                }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (singleLine) {
                        Modifier.height(56.dp)
                    } else {
                        Modifier.heightIn(min = (56 * minLines).dp)
                    }
                ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
            ),
            textStyle = TextStyle(
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            singleLine = singleLine,
            minLines = if (!singleLine) minLines else 1,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        )
    }
}

@Composable
private fun PostDateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d %s %04d", selectedDay,
                getMonthName(selectedMonth), selectedYear)
            onValueChange(formattedDate)
        },
        year,
        month,
        day,
    )

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = if (compact) Color(0xFFE8E8E8) else Color.White,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable { datePickerDialog.show() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else if (compact) "31 Oct 2025" else "Select date",
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isNotEmpty()) Color.Black else Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun PostTimeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)

    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d %s",
                if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour,
                selectedMinute,
                if (selectedHour >= 12) "PM" else "AM"
            )
            onValueChange(formattedTime)
        },
        hour,
        minute,
        false, // 12-hour format
    )

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = if (compact) Color(0xFFE8E8E8) else Color.White,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable { timePickerDialog.show() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else "5:08 PM",
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isNotEmpty()) Color.Black else Color.Gray,
                )
            }
        }
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        0 -> "Jan"
        1 -> "Feb"
        2 -> "Mar"
        3 -> "Apr"
        4 -> "May"
        5 -> "Jun"
        6 -> "Jul"
        7 -> "Aug"
        8 -> "Sep"
        9 -> "Oct"
        10 -> "Nov"
        11 -> "Dec"
        else -> ""
    }
}

@Composable
private fun PostPreviewDialog(
    profilePicUrl: String?,
    userName: String,
    postType: String,
    postSubject: String,
    postPriority: String,
    postGroup: String,
    employee: String,
    announcementDescription: String,
    postStartDate: String,
    postEndDate: String,
    startDurationDate: String,
    startDurationTime: String,
    endDurationDate: String,
    endDurationTime: String,
    supportChannelDetails: String,
    postImageUris: List<Uri>,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EBE3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    // Title
                    Text(
                        text = "HeadsUp Preview",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // White content card (includes profile header)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header with profile and priority
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Profile picture
                                    Card(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(containerColor = Color.Gray)
                                    ) {
                                        if (profilePicUrl != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(profilePicUrl),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Gray),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = userName.take(1).uppercase(),
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = userName,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = Color.Black,
                                        )
                                        Text(
                                            text = "12 November 2025",
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                        )
                                    }
                                }

                                // Priority badge - only show if selected
                                if (postPriority != "Select Post Priority") {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = when (postPriority) {
                                                    "High" -> Color(0xFFD32F2F)
                                                    "Medium" -> Color(0xFFFFA726)
                                                    "Low" -> Color(0xFF66BB6A)
                                                    else -> Color.Gray
                                                },
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = postPriority,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            // Post Subject - only show if not default selection
                            if (postSubject != "Select Post Subject") {
                                Text(
                                    text = postSubject,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Announcement Description - only show if not empty
                            if (announcementDescription.isNotEmpty()) {
                                Text(
                                    text = announcementDescription,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Support and Duration labels (always show)
                            Text(
                                text = "Support:",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                            Text(
                                text = "Duration:",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )

                            // Attached Images - display in grid if images exist
                            if (postImageUris.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))

                                // Grid layout for images (2 columns)
                                val rows = (postImageUris.size + 1) / 2
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (rowIndex in 0 until rows) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            for (colIndex in 0 until 2) {
                                                val imageIndex = rowIndex * 2 + colIndex
                                                if (imageIndex < postImageUris.size) {
                                                    Card(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(140.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                    ) {
                                                        Image(
                                                            painter = rememberAsyncImagePainter(postImageUris[imageIndex]),
                                                            contentDescription = "Attached Image",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Start and End dates - only show if at least one is filled
                            if (postStartDate.isNotEmpty() || postEndDate.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Post Start Date - only if filled
                                    if (postStartDate.isNotEmpty()) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = "Post Start Date",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(id = com.archeGlobal.one.R.drawable.green),
                                                    contentDescription = "Start Date",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFF66BB6A)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = postStartDate,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 12.sp,
                                                    color = Color.Black,
                                                )
                                            }
                                        }
                                    }

                                    // Post End Date - only if filled
                                    if (postEndDate.isNotEmpty()) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = "Post End Date",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(id = com.archeGlobal.one.R.drawable.red),
                                                    contentDescription = "End Date",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFFD32F2F)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = postEndDate,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 12.sp,
                                                    color = Color.Black,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(140.dp)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventPreviewDialog(
    eventSubject: String,
    eventDescription: String,
    eventImageUri: Uri?,
    eventDate: String,
    eventStartDate: String,
    eventEndDate: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EBE3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Event Image
                    eventImageUri?.let { uri ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Event Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Event Title/Subject
                    Text(
                        text = eventSubject.ifEmpty { "Event Title" },
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Event Date
                    if (eventDate.isNotEmpty()) {
                        Text(
                            text = eventDate,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Start and End Dates
                    if (eventStartDate.isNotEmpty() && eventEndDate.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "Start: $eventStartDate",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "End: $eventEndDate",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Event Description
                    if (eventDescription.isNotEmpty()) {
                        Text(
                            text = eventDescription,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
