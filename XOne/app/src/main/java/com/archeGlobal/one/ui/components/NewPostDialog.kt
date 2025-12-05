package com.archeGlobal.one.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.R
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
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// Helper function to convert API date format to display format (dd-MMM-yyyy)
fun formatApiDateToDisplayDate(apiDateString: String?): String {
    if (apiDateString.isNullOrEmpty()) return ""
    val displayFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    
    // List of patterns to try
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )
    
    for (pattern in patterns) {
        try {
            val format = SimpleDateFormat(pattern, Locale.getDefault())
            if (pattern.contains("'Z'")) {
                 format.timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = format.parse(apiDateString)
            if (date != null) {
                return displayFormat.format(date)
            }
        } catch (e: Exception) {
            // Continue to next pattern
        }
    }
    return ""
}

// Helper function to convert API date format to full month display format (dd MMMM yyyy)
fun formatApiDateToFullMonthDisplayDate(apiDateString: String?): String {
    if (apiDateString.isNullOrEmpty()) return ""
    val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    
    // List of patterns to try
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )
    
    for (pattern in patterns) {
        try {
            val format = SimpleDateFormat(pattern, Locale.getDefault())
            if (pattern.contains("'Z'")) {
                 format.timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = format.parse(apiDateString)
            if (date != null) {
                return displayFormat.format(date)
            }
        } catch (e: Exception) {
            // Continue to next pattern
        }
    }
    return ""
}

// Helper function to convert API date format to display time (h:mm a)
fun formatApiTimeToDisplayTime(apiDateString: String?): String {
    if (apiDateString.isNullOrEmpty()) return ""
    // Log the input for debugging
    // android.util.Log.d("NewPostDialog", "Formatting time for: $apiDateString")
    
    val displayFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    // List of patterns to try
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )
    
    for (pattern in patterns) {
        try {
            val format = SimpleDateFormat(pattern, Locale.getDefault())
            if (pattern.contains("'Z'")) {
                 format.timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = format.parse(apiDateString)
            if (date != null) {
                return displayFormat.format(date)
            }
        } catch (e: Exception) {
            // Continue to next pattern
        }
    }
    
    // android.util.Log.e("NewPostDialog", "Failed to parse time: $apiDateString")
    return ""
}

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
    var errorMessage by remember { mutableStateOf("") }

    // Fetched locations, departments, projects, and announcement categories
    var fetchedLocations by remember { mutableStateOf<List<String>>(emptyList()) }
    var fetchedDepartments by remember { mutableStateOf<List<String>>(emptyList()) }
    var fetchedProjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var announcementCategories by remember { mutableStateOf<List<com.archeGlobal.one.network.AnnouncementCategory>>(emptyList()) }

    // Determine if we're in edit mode
    val isEditMode = existingPost != null

    // Post form states - initialize with existing post data if in edit mode
    // Note: Don't pre-fill postType from existingPost as we'll infer it from subject
    var postType by remember { mutableStateOf("") }
    var postSubject by remember { mutableStateOf(existingPost?.subject ?: "") }

    // Infer post type from post subject in edit mode
    LaunchedEffect(announcementCategories, userAccess, isEditMode, postSubject) {
        // Only infer post type in edit mode when we have a subject but no post type
        if (isEditMode && postType.isEmpty() && postSubject.isNotEmpty() && announcementCategories.isNotEmpty()) {
            android.util.Log.d("NewPostDialog", "Attempting to infer post type from subject: '$postSubject'")

            // Get user's category
            val userCategory = announcementCategories.find {
                it.access.equals(userAccess, ignoreCase = true)
            }

            // Build subject-to-type mapping
            userCategory?.fields?.forEach { field ->
                val categoryName = when {
                    field.category.contains("Planned & Unplanned", ignoreCase = true) -> {
                        // Check if subject exists in this category
                        if (field.subcategory.any { it.equals(postSubject, ignoreCase = true) }) {
                            // Default to "Planned Post" for combined categories
                            postType = "Planned Post"
                            android.util.Log.d("NewPostDialog", "Inferred postType: 'Planned Post' from 'Planned & Unplanned' category")
                        }
                        return@forEach
                    }
                    field.category.contains("Unplanned", ignoreCase = true) -> "Unplanned Post"
                    field.category.contains("Planned", ignoreCase = true) -> "Planned Post"
                    else -> field.category
                }

                // Check if the subject exists in this category's subcategories
                if (field.subcategory.any { it.equals(postSubject, ignoreCase = true) }) {
                    postType = categoryName
                    android.util.Log.d("NewPostDialog", "Inferred postType: '$categoryName' from subject: '$postSubject'")
                }
            }

            if (postType.isEmpty()) {
                android.util.Log.w("NewPostDialog", "Could not infer post type from subject: '$postSubject'")
            }
        }
    }
    var postPriority by remember { mutableStateOf(existingPost?.priority ?: "") }
    var postGroup by remember {
        // Infer the post group based on which target field is filled (only in edit mode)
        val inferredGroup = existingPost?.let { post ->
            android.util.Log.d("NewPostDialog", "Inferring postGroup from target fields:")
            android.util.Log.d("NewPostDialog", "  target_employee: ${post.target_employee?.size ?: 0} items")
            android.util.Log.d("NewPostDialog", "  target_department: ${post.target_department?.size ?: 0} items")
            android.util.Log.d("NewPostDialog", "  target_location: ${post.target_location?.size ?: 0} items")

            when {
                !post.target_employee.isNullOrEmpty() -> {
                    android.util.Log.d("NewPostDialog", "Inferred: Employee Based")
                    "Employee Based"
                }
                !post.target_department.isNullOrEmpty() -> {
                    android.util.Log.d("NewPostDialog", "Inferred: Department Based")
                    "Department Based"
                }
                !post.target_location.isNullOrEmpty() -> {
                    android.util.Log.d("NewPostDialog", "Inferred: Location Based")
                    "Location Based"
                }
                else -> {
                    android.util.Log.d("NewPostDialog", "Inferred: Everyone@Arche (no specific targets)")
                    "Everyone@Arche"
                }
            }
        } ?: "" // Empty for new posts - user must select

        android.util.Log.d("NewPostDialog", "Final postGroup: '$inferredGroup'")
        mutableStateOf(inferredGroup)
    }
    var employeeSearchQuery by remember { mutableStateOf("") }
    var selectedEmployees by remember { mutableStateOf<List<com.archeGlobal.one.model.SuggestedUser>>(existingPost?.target_employee?.mapNotNull { email ->
        com.archeGlobal.one.model.SuggestedUser(email, email.substringBefore("@"))
    } ?: emptyList()) }
    var suggestedEmployees by remember { mutableStateOf<List<com.archeGlobal.one.model.SuggestedUser>>(emptyList()) }
    var isSearchingEmployees by remember { mutableStateOf(false) }
    var departmentSearchQuery by remember { mutableStateOf("") }
    var selectedDepartments by remember { mutableStateOf<List<String>>(existingPost?.target_department ?: emptyList()) }
    var suggestedDepartments by remember { mutableStateOf<List<String>>(emptyList()) }
    var locationSearchQuery by remember { mutableStateOf("") }
    var selectedLocations by remember { mutableStateOf<List<String>>(existingPost?.target_location ?: emptyList()) }
    var suggestedLocations by remember { mutableStateOf<List<String>>(emptyList()) }
    var projectSearchQuery by remember { mutableStateOf("") }
    var selectedProjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var suggestedProjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var announcementDescription by remember { mutableStateOf(existingPost?.description ?: "") }

    // Debug logging for existingPost - runs once
    LaunchedEffect(existingPost) {
        android.util.Log.d("NewPostDialog", "=== NewPostDialog LaunchedEffect (Edit Mode Check) ===")
        android.util.Log.d("NewPostDialog", "existingPost is null: ${existingPost == null}")
        android.util.Log.d("NewPostDialog", "IsEditMode: $isEditMode")
        existingPost?.let { post ->
            android.util.Log.d("NewPostDialog", "Post ID: ${post.post_id}")
            android.util.Log.d("NewPostDialog", "Subject: ${post.subject}")
            android.util.Log.d("NewPostDialog", "Inferred postGroup: '$postGroup'")
            android.util.Log.d("NewPostDialog", "Selected employees count: ${selectedEmployees.size}")
            android.util.Log.d("NewPostDialog", "Selected departments count: ${selectedDepartments.size}")
            android.util.Log.d("NewPostDialog", "Selected locations count: ${selectedLocations.size}")
        }
    }

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

                // Detailed logging for debugging
                android.util.Log.d("NewPostDialog", "=== API Response Details ===")
                android.util.Log.d("NewPostDialog", "Fetched announcement categories: ${announcementCategories.size}")
                android.util.Log.d("NewPostDialog", "User access: $userAccess")
                announcementCategories.forEach { category ->
                    android.util.Log.d("NewPostDialog", "Category access: ${category.access}")
                    category.fields.forEach { field ->
                        android.util.Log.d("NewPostDialog", "  - Category: ${field.category}, Subcategories count: ${field.subcategory.size}")
                        android.util.Log.d("NewPostDialog", "  - Subcategories: ${field.subcategory}")
                    }
                }
            } else {
                android.util.Log.e("NewPostDialog", "API call failed: ${response.code()} - ${response.message()}")
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

    // Search departments function
    fun searchDepartments(query: String) {
        departmentSearchQuery = query
        if (query.isEmpty()) {
            suggestedDepartments = emptyList()
            return
        }
        // Filter from fetched departments
        suggestedDepartments = fetchedDepartments.filter {
            it.contains(query, ignoreCase = true)
        }.take(5)
    }

    // Search locations function
    fun searchLocations(query: String) {
        locationSearchQuery = query
        if (query.isEmpty()) {
            suggestedLocations = emptyList()
            return
        }
        // Filter from fetched locations
        suggestedLocations = fetchedLocations.filter {
            it.contains(query, ignoreCase = true) && !selectedLocations.contains(it)
        }.take(5)
    }

    // Search projects function
    fun searchProjects(query: String) {
        projectSearchQuery = query
        if (query.isEmpty()) {
            suggestedProjects = emptyList()
            return
        }
        // Filter from fetched projects
        suggestedProjects = fetchedProjects.filter {
            it.contains(query, ignoreCase = true)
        }.take(5)
    }
    var postStartDate by remember {
        mutableStateOf<String>(
            if (existingPost != null) {
                formatApiDateToFullMonthDisplayDate(existingPost.start_date)
            } else {
                // Default to current date for new posts
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
            }
        )
    }
    var postEndDate by remember {
        mutableStateOf<String>(
            if (existingPost != null) {
                formatApiDateToFullMonthDisplayDate(existingPost.end_date)
            } else {
                // Default to current date for new posts
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
            }
        )
    }
    var startDurationDate by remember {
        mutableStateOf<String>(
            if (existingPost != null) {
                formatApiDateToDisplayDate(existingPost.activity_start)
            } else {
                // Default to current date for new posts
                SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
            }
        )
    }
    var startDurationTime by remember {
        mutableStateOf<String>(
            if (existingPost != null) {
                formatApiTimeToDisplayTime(existingPost.activity_start)
            } else {
                // Default to current time for new posts
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            }
        )
    }
    var endDurationDate by remember {
        mutableStateOf<String>(
            if (existingPost != null) {
                formatApiDateToDisplayDate(existingPost.activity_end)
            } else {
                // Default to current date for new posts
                SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
            }
        )
    }
    var endDurationTime by remember {
        mutableStateOf<String>(
            if (existingPost != null) {
                formatApiTimeToDisplayTime(existingPost.activity_end)
            } else {
                // Default to current time for new posts
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            }
        )
    }
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
    var eventDate by remember { mutableStateOf<String>(formatApiDateToDisplayDate(existingPost?.event_date)) }
    var eventStartDate by remember {
        mutableStateOf<String>(
            if (existingPost?.post_type == "homeView") {
                formatApiDateToDisplayDate(existingPost.start_date)
            } else {
                // Default to current date for new posts
                SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
            }
        )
    }
    var eventEndDate by remember {
        mutableStateOf<String>(
            if (existingPost?.post_type == "homeView") {
                formatApiDateToDisplayDate(existingPost.end_date)
            } else {
                // Default to current date for new posts
                SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
            }
        )
    }
    var showEventPreview by remember { mutableStateOf(false) }

    // Helper function to download image from URL and convert to MultipartBody.Part
    suspend fun urlToMultipartBodyPart(imageUrl: String, partName: String, index: Int): MultipartBody.Part? {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("NewPostDialog", "Downloading image from URL: $imageUrl")
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()

                // Create temp file
                val filename = "existing_image_$index.png"
                val tempFile = File(context.cacheDir, filename)
                android.util.Log.d("NewPostDialog", "Creating temp file for existing image: ${tempFile.absolutePath}")

                // Copy to temp file
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                outputStream.close()
                inputStream.close()

                android.util.Log.d("NewPostDialog", "Downloaded image size: ${tempFile.length()} bytes")

                // Create multipart body part
                val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
                android.util.Log.d("NewPostDialog", "Multipart body part created for existing image")
                part
            } catch (e: Exception) {
                android.util.Log.e("NewPostDialog", "Error downloading image from URL: $imageUrl", e)
                null
            }
        }
    }

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
                return ""
            }
    
            val dateTimeString = if (timeString.contains("AM") || timeString.contains("PM")) {
                "$dateString $timeString"
            } else {
                "$dateString $timeString"
            }
    
                    val inputFormat = if (timeString.contains("AM") || timeString.contains("PM")) {
                        SimpleDateFormat("dd-MMM-yyyy h:mm a", Locale.ENGLISH)
                    } else if (dateString.matches(Regex("\\d{1,2}\\s+[A-Za-z]{4,}\\s+\\d{4}"))) { // "dd MMMM yyyy"
                        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
                    } else { // "dd-MMM-yyyy"
                        SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
                    }    
            val date = inputFormat.parse(dateTimeString)
    
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(date)
        } catch (e: Exception) {
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
                        "Employee Based" -> "EmployeeBased"
                        "Department Based" -> "DepartmentBased"
                        "Location Based" -> "LocationBased"
                        "Project Based" -> "ProjectBased"
                        "Everyone@Arche" -> "Everyone"
                        "All" -> "Everyone"
                        else -> "Everyone"
                    }
                }
                android.util.Log.d("NewPostDialog", "Target Group: $targetGroup (from postGroup: $postGroup)")

                android.util.Log.d("NewPostDialog", "--- STEP 2: Preparing target arrays (mutually exclusive) ---")

                // Prepare target arrays based on group - only ONE should be non-null
                val targetDepartment: List<String>?
                val targetLocation: List<String>?
                val targetProject: List<String>?
                val targetEmployee: List<String>?

                when (targetGroup) {
                    "DepartmentBased" -> {
                        targetDepartment = if (selectedDepartments.isNotEmpty()) selectedDepartments else null
                        targetLocation = null
                        targetProject = null
                        targetEmployee = null
                    }
                    "LocationBased" -> {
                        targetDepartment = null
                        targetLocation = if (selectedLocations.isNotEmpty()) selectedLocations else null
                        targetProject = null
                        targetEmployee = null
                    }
                    "ProjectBased" -> {
                        targetDepartment = null
                        targetLocation = null
                        targetProject = if (selectedProjects.isNotEmpty()) selectedProjects else null
                        targetEmployee = null
                    }
                    "EmployeeBased" -> {
                        targetDepartment = null
                        targetLocation = null
                        targetProject = null
                        targetEmployee = if (selectedEmployees.isNotEmpty()) selectedEmployees.map { it.mail } else null
                    }
                    else -> { // "Everyone"
                        targetDepartment = null
                        targetLocation = null
                        targetProject = null
                        targetEmployee = null
                    }
                }

                android.util.Log.d("NewPostDialog", "Target Department: $targetDepartment")
                android.util.Log.d("NewPostDialog", "Target Location: $targetLocation")
                android.util.Log.d("NewPostDialog", "Target Project: $targetProject")
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

                // Create post request as a Map to conditionally include fields
                android.util.Log.d("NewPostDialog", "=== FORM TYPE DETECTION ===")
                android.util.Log.d("NewPostDialog", "formType value: '$formType'")
                android.util.Log.d("NewPostDialog", "Will send post_type as: ${if (formType == "Post") "headsUp" else "homeView"}")
                android.util.Log.d("NewPostDialog", "===========================")

                val postRequestMap = mutableMapOf<String, Any?>(
                    "user_email" to (userData?.email ?: ""),
                    "username" to (userData?.name ?: userName),
                    "emp_id" to (userData?.employeeId ?: ""),
                    "post_type" to if (formType == "Post") "headsUp" else "homeView",
                    "subject" to if (formType == "Post") postSubject else eventSubject,
                    "priority" to if (formType == "Post") postPriority else "Medium",
                    "description" to if (formType == "Post") announcementDescription else eventDescription,
                    "start_date" to formatDateToISO(if (formType == "Post") postStartDate else eventStartDate),
                    "end_date" to formatDateToISO(if (formType == "Post") postEndDate else eventEndDate)
                )

                // Add profile_pic if available
                userData?.profilePic?.let { postRequestMap["profile_pic"] = it }

                // Add target fields based on target group - only include relevant field
                postRequestMap["target_group"] = targetGroup
                when (targetGroup) {
                    "Everyone" -> {
                        // target_group already set above
                    }
                    "DepartmentBased" -> {
                        targetDepartment?.let { postRequestMap["target_department"] = it }
                    }
                    "LocationBased" -> {
                        targetLocation?.let { postRequestMap["target_location"] = it }
                    }
                    "EmployeeBased" -> {
                        targetEmployee?.let { postRequestMap["target_employee"] = it }
                    }
                    "ProjectBased" -> {
                        targetProject?.let { postRequestMap["target_project"] = it }
                    }
                }

                // Add event_date if Event
                if (formType == "Event" && eventDate.isNotEmpty()) {
                    postRequestMap["event_date"] = formatDateToISO(eventDate)
                }

                // Add activity times if available
                activityStartISO?.let { postRequestMap["activity_start"] = it }
                activityEndISO?.let { postRequestMap["activity_end"] = it }

                // Add support channel if provided
                if (supportChannelDetails.isNotEmpty()) {
                    postRequestMap["support_channel"] = supportChannelDetails
                }

                // Remove existing_images from the map as they are now sent as multipart files
                postRequestMap.remove("existing_images")

                android.util.Log.d("NewPostDialog", "=== FINAL API REQUEST DATA ===")
                android.util.Log.d("NewPostDialog", "Post Type: ${postRequestMap["post_type"]}")
                android.util.Log.d("NewPostDialog", "Subject: ${postRequestMap["subject"]}")
                android.util.Log.d("NewPostDialog", "Priority: ${postRequestMap["priority"]}")
                android.util.Log.d("NewPostDialog", "Description length: ${(postRequestMap["description"] as? String)?.length ?: 0}")
                // android.util.Log.d("NewPostDialog", "Existing images in request: ${postRequestMap["existing_images"]}") // Removed as existing images are now multipart
                android.util.Log.d("NewPostDialog", "Start Date - Display: ${if (formType == "Post") postStartDate else eventStartDate} → ISO: ${postRequestMap["start_date"]}")
                android.util.Log.d("NewPostDialog", "End Date - Display: ${if (formType == "Post") postEndDate else eventEndDate} → ISO: ${postRequestMap["end_date"]}")
                android.util.Log.d("NewPostDialog", "Event Date - Display: $eventDate → ISO: ${postRequestMap["event_date"]}")
                android.util.Log.d("NewPostDialog", "Activity Start - Display: $startDurationDate $startDurationTime → ISO: ${postRequestMap["activity_start"]}")
                android.util.Log.d("NewPostDialog", "Activity End - Display: $endDurationDate $endDurationTime → ISO: ${postRequestMap["activity_end"]}")
                android.util.Log.d("NewPostDialog", "Target Group: ${postRequestMap["target_group"]}")
                android.util.Log.d("NewPostDialog", "Target Department: ${postRequestMap["target_department"]}")
                android.util.Log.d("NewPostDialog", "Target Location: ${postRequestMap["target_location"]}")
                android.util.Log.d("NewPostDialog", "Target Employee: ${postRequestMap["target_employee"]}")
                android.util.Log.d("NewPostDialog", "Target Project: ${postRequestMap["target_project"]}")
                android.util.Log.d("NewPostDialog", "Support Channel: ${postRequestMap["support_channel"]}")
                android.util.Log.d("NewPostDialog", "User Email: ${postRequestMap["user_email"]}")
                android.util.Log.d("NewPostDialog", "Username: ${postRequestMap["username"]}")
                android.util.Log.d("NewPostDialog", "Employee ID: ${postRequestMap["emp_id"]}")
                android.util.Log.d("NewPostDialog", "Profile Pic: ${postRequestMap["profile_pic"]}")
                android.util.Log.d("NewPostDialog", "===============================")

                android.util.Log.d("NewPostDialog", "--- STEP 4: Validating ISO dates ---")

                // Validate ISO dates
                val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                isoDateFormat.timeZone = TimeZone.getTimeZone("UTC")

                val datesToValidate = mapOf(
                    "start_date" to postRequestMap["start_date"] as? String,
                    "end_date" to postRequestMap["end_date"] as? String,
                    "activity_start" to postRequestMap["activity_start"] as? String,
                    "activity_end" to postRequestMap["activity_end"] as? String
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
                val postJson = gson.toJson(postRequestMap)
                android.util.Log.d("NewPostDialog", "JSON Data: $postJson")

                val postRequestBody = postJson.toRequestBody("application/json".toMediaTypeOrNull())
                android.util.Log.d("NewPostDialog", "Request body created successfully")

                android.util.Log.d("NewPostDialog", "--- STEP 6: Processing images ---")

                val allImageParts = mutableListOf<MultipartBody.Part>()

                // Process new images (from URIs)
                if (formType == "Post") {
                    android.util.Log.d("NewPostDialog", "Processing ${postImageUris.size} new post images (from URIs)")
                    postImageUris.mapIndexedNotNullTo(allImageParts) { index, uri ->
                        android.util.Log.d("NewPostDialog", "Converting new image $index: $uri")
                        val part = uriToMultipartBodyPart(context, uri, "files")
                        if (part != null) {
                            android.util.Log.d("NewPostDialog", "New image $index converted successfully")
                        } else {
                            android.util.Log.e("NewPostDialog", "Failed to convert new image $index")
                        }
                        part
                    }
                } else { // Event form
                    if (eventImageUri != null) {
                        android.util.Log.d("NewPostDialog", "Processing 1 new event image (from URI): $eventImageUri")
                        val part = uriToMultipartBodyPart(context, eventImageUri!!, "files")
                        if (part != null) {
                            android.util.Log.d("NewPostDialog", "New event image converted successfully")
                            allImageParts.add(part)
                        } else {
                            android.util.Log.e("NewPostDialog", "Failed to convert new event image")
                        }
                    } else {
                        android.util.Log.d("NewPostDialog", "No new event image to process")
                    }
                }

                // Process existing images (from URLs) if in edit mode
                if (isEditMode && existingImageUrls.isNotEmpty()) {
                    android.util.Log.d("NewPostDialog", "Processing ${existingImageUrls.size} existing images (from URLs)")
                    existingImageUrls.mapIndexedNotNullTo(allImageParts) { index, imageUrl ->
                        android.util.Log.d("NewPostDialog", "Converting existing image $index: $imageUrl")
                        val part = urlToMultipartBodyPart(imageUrl, "files", index)
                        if (part != null) {
                            android.util.Log.d("NewPostDialog", "Existing image $index converted successfully")
                        } else {
                            android.util.Log.e("NewPostDialog", "Failed to convert existing image $index")
                        }
                        part
                    }
                }

                android.util.Log.d("NewPostDialog", "Total images ready for upload: ${allImageParts.size}")

                android.util.Log.d("NewPostDialog", "--- STEP 7: Making API call ---")
                android.util.Log.d("NewPostDialog", "API Endpoint: POST /announcements/v1/post")
                android.util.Log.d("NewPostDialog", "Number of file parts: ${allImageParts.size}")

                val response = withContext(Dispatchers.IO) {
                    android.util.Log.d("NewPostDialog", "Executing API request...")
                    try {
                        val result = if (isEditMode && existingPost != null) {
                            android.util.Log.d("NewPostDialog", "Updating existing post: ${existingPost.post_id}")
                            RetrofitClient.apiService.updatePost(existingPost.post_id, postRequestBody, allImageParts)
                        } else {
                            android.util.Log.d("NewPostDialog", "Creating new post")
                            RetrofitClient.apiService.createPost(postRequestBody, allImageParts)
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
                        // Show success toast
                        val successMessage = if (formType == "Post") {
                            "HeadsUp post ${if (isEditMode) "updated" else "created"} successfully"
                        } else {
                            "Home Page post ${if (isEditMode) "updated" else "created"} successfully"
                        }
                        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                        // Navigate to history
                        val title = if (formType == "Post") postSubject else eventSubject
                        val description = if (formType == "Post") announcementDescription else eventDescription
                        onSubmit(title, description, formType)
                    } else {
                        val errorMsg = if (isEditMode) {
                            (responseBody as? com.archeGlobal.one.network.UpdatePostResponse)?.message ?: "Failed to update post"
                        } else {
                            (responseBody as? com.archeGlobal.one.network.CreatePostResponse)?.error ?: "Failed to create post"
                        }
                        errorMessage = errorMsg
                        android.util.Log.e("NewPostDialog", "✗ FAILED: $errorMessage")
                        // Show error toast
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                } else {
                    errorMessage = response.errorBody()?.string() ?: "Failed to ${if (isEditMode) "update" else "create"} post (Code: ${response.code()})"
                    android.util.Log.e("NewPostDialog", "✗ FAILED: $errorMessage")
                    // Show error toast
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
                android.util.Log.d("NewPostDialog", "=== POST CREATION COMPLETED ===")
            } catch (e: Exception) {
                android.util.Log.e("NewPostDialog", "=== POST CREATION FAILED WITH EXCEPTION ===")
                android.util.Log.e("NewPostDialog", "Exception Type: ${e.javaClass.simpleName}")
                android.util.Log.e("NewPostDialog", "Exception Message: ${e.message}")
                android.util.Log.e("NewPostDialog", "Stack Trace:", e)
                isSubmitting = false
                errorMessage = e.message ?: "An error occurred"
                // Show error toast
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Reset post subject when post type changes (but not during initial load or in edit mode)
    var isInitialLoad by remember { mutableStateOf(true) }
    LaunchedEffect(postType) {
        if (!isInitialLoad && !isEditMode) {
            // Only reset subject in create mode when user manually changes post type
            postSubject = ""
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
                            text = "Create Post",
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
                            .clickable {
                                onDismiss()
                                onHistoryClick()
                            }
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
                            selectedEmployees = selectedEmployees,
                            onEmployeeAdded = { employee ->
                                if (!selectedEmployees.any { it.mail == employee.mail }) {
                                    selectedEmployees = selectedEmployees + employee
                                    employeeSearchQuery = ""
                                    suggestedEmployees = emptyList()
                                }
                            },
                            onEmployeeRemoved = { employee ->
                                selectedEmployees = selectedEmployees.filter { it.mail != employee.mail }
                            },
                            suggestedEmployees = suggestedEmployees,
                            isSearchingEmployees = isSearchingEmployees,
                            departmentSearchQuery = departmentSearchQuery,
                            onDepartmentSearchQueryChange = { searchDepartments(it) },
                            selectedDepartments = selectedDepartments,
                            onDepartmentAdded = { dept ->
                                if (!selectedDepartments.contains(dept)) {
                                    selectedDepartments = selectedDepartments + dept
                                    departmentSearchQuery = ""
                                    suggestedDepartments = emptyList()
                                }
                            },
                            onDepartmentRemoved = { dept ->
                                selectedDepartments = selectedDepartments.filter { it != dept }
                            },
                            suggestedDepartments = suggestedDepartments,
                            locationSearchQuery = locationSearchQuery,
                            onLocationSearchQueryChange = { searchLocations(it) },
                            selectedLocations = selectedLocations,
                            onLocationAdded = { location ->
                                if (!selectedLocations.contains(location)) {
                                    selectedLocations = selectedLocations + location
                                    locationSearchQuery = ""
                                    suggestedLocations = emptyList()
                                }
                            },
                            onLocationRemoved = { location ->
                                selectedLocations = selectedLocations.filter { it != location }
                            },
                            suggestedLocations = suggestedLocations,
                            projectSearchQuery = projectSearchQuery,
                            onProjectSearchQueryChange = { searchProjects(it) },
                            selectedProjects = selectedProjects,
                            onProjectAdded = { project ->
                                if (!selectedProjects.contains(project)) {
                                    selectedProjects = selectedProjects + project
                                    projectSearchQuery = ""
                                    suggestedProjects = emptyList()
                                }
                            },
                            onProjectRemoved = { project ->
                                selectedProjects = selectedProjects.filter { it != project }
                            },
                            suggestedProjects = suggestedProjects,
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
                            onEventDescriptionChange = { if (it.length <= 100) eventDescription = it },
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
                employee = selectedEmployees.firstOrNull()?.displayName ?: "",
                announcementDescription = announcementDescription,
                postStartDate = postStartDate,
                postEndDate = postEndDate,
                startDurationDate = startDurationDate,
                startDurationTime = startDurationTime,
                endDurationDate = endDurationDate,
                endDurationTime = endDurationTime,
                supportChannelDetails = supportChannelDetails,
                postImageUris = postImageUris,
                existingImageUrls = existingImageUrls,
                selectedEmployees = selectedEmployees,
                selectedDepartments = selectedDepartments,
                selectedLocations = selectedLocations,
                selectedProjects = selectedProjects,
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
    selectedEmployees: List<com.archeGlobal.one.model.SuggestedUser>,
    onEmployeeAdded: (com.archeGlobal.one.model.SuggestedUser) -> Unit,
    onEmployeeRemoved: (com.archeGlobal.one.model.SuggestedUser) -> Unit,
    suggestedEmployees: List<com.archeGlobal.one.model.SuggestedUser>,
    isSearchingEmployees: Boolean,
    departmentSearchQuery: String,
    onDepartmentSearchQueryChange: (String) -> Unit,
    selectedDepartments: List<String>,
    onDepartmentAdded: (String) -> Unit,
    onDepartmentRemoved: (String) -> Unit,
    suggestedDepartments: List<String>,
    locationSearchQuery: String,
    onLocationSearchQueryChange: (String) -> Unit,
    selectedLocations: List<String>,
    onLocationAdded: (String) -> Unit,
    onLocationRemoved: (String) -> Unit,
    suggestedLocations: List<String>,
    projectSearchQuery: String,
    onProjectSearchQueryChange: (String) -> Unit,
    selectedProjects: List<String>,
    onProjectAdded: (String) -> Unit,
    onProjectRemoved: (String) -> Unit,
    suggestedProjects: List<String>,
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

    android.util.Log.d("PostFormContent", "=== PostFormContent Debug ===")
    android.util.Log.d("PostFormContent", "User access: $userAccess")
    android.util.Log.d("PostFormContent", "Total announcement categories: ${announcementCategories.size}")
    android.util.Log.d("PostFormContent", "User category found: ${userCategory != null}")
    android.util.Log.d("PostFormContent", "User category fields count: ${userCategory?.fields?.size ?: 0}")

    // Get post types and subjects based on user access
    val postTypes = mutableListOf<String>()
    val postSubjectsByType = mutableMapOf<String, List<String>>()

    userCategory?.fields?.forEach { field ->
        android.util.Log.d("PostFormContent", "Processing field category: '${field.category}'")
        android.util.Log.d("PostFormContent", "Subcategories in this field: ${field.subcategory.size}")

        val categoryName = when {
            // First check for "Planned & Unplanned" - exact match for combined category
            field.category.contains("Planned & Unplanned", ignoreCase = true) -> {
                android.util.Log.d("PostFormContent", "Matched 'Planned & Unplanned' - splitting into two")
                // Split into two separate categories with same subjects
                postTypes.add("Planned Post")
                postTypes.add("Unplanned Post")
                postSubjectsByType["Planned Post"] = field.subcategory
                postSubjectsByType["Unplanned Post"] = field.subcategory
                null
            }
            // Check for "Unplanned" BEFORE "Planned" to avoid false matches
            field.category.contains("Unplanned", ignoreCase = true) -> {
                android.util.Log.d("PostFormContent", "Matched 'Unplanned' category")
                "Unplanned Post"
            }
            // Check for "Planned" (but not "Unplanned")
            field.category.contains("Planned", ignoreCase = true) -> {
                android.util.Log.d("PostFormContent", "Matched 'Planned' category")
                "Planned Post"
            }
            else -> {
                android.util.Log.d("PostFormContent", "Using category name as-is: '${field.category}'")
                field.category
            }
        }

        categoryName?.let {
            android.util.Log.d("PostFormContent", "Adding category: '$it' with ${field.subcategory.size} subjects")
            if (!postTypes.contains(it)) {
                postTypes.add(it)
            }
            // Merge subjects if category already exists, otherwise set new list
            val existingSubjects = postSubjectsByType[it] ?: emptyList()
            if (existingSubjects.isNotEmpty() && existingSubjects != field.subcategory) {
                // Merge and remove duplicates
                postSubjectsByType[it] = (existingSubjects + field.subcategory).distinct()
                android.util.Log.d("PostFormContent", "Merged subjects for '$it': ${postSubjectsByType[it]?.size} total")
            } else {
                postSubjectsByType[it] = field.subcategory
            }
        }
    }

    // Remove duplicates
    val uniquePostTypes = postTypes.distinct()

    // Get subjects for the currently selected post type and add "Other" at the bottom
    val postSubjects = (postSubjectsByType[postType] ?: emptyList()) + "Other"

    android.util.Log.d("PostFormContent", "=== Final Results ===")
    android.util.Log.d("PostFormContent", "Available post types: $uniquePostTypes")
    android.util.Log.d("PostFormContent", "Current post type: '$postType'")
    android.util.Log.d("PostFormContent", "Available subjects for '$postType': ${postSubjects.size} items")
    android.util.Log.d("PostFormContent", "Subjects: $postSubjects")
    android.util.Log.d("PostFormContent", "All mapped types: ${postSubjectsByType.keys}")
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
        placeholder = "Select Post Type",
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Subject Dropdown
    PostDropdown(
        label = "Post Subject",
        selectedValue = postSubject,
        options = postSubjects,
        onValueSelected = onPostSubjectChange,
        placeholder = "Select Post Subject",
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Priority Dropdown
    PostDropdown(
        label = "Post Priority",
        selectedValue = postPriority,
        options = postPriorities,
        onValueSelected = onPostPriorityChange,
        placeholder = "Select Post Priority",
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Post Group Dropdown
    PostDropdown(
        label = "Post Group",
        selectedValue = postGroup,
        options = postGroups,
        onValueSelected = onPostGroupChange,
        placeholder = "Select Group To Tag",
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
                                            onEmployeeAdded(employee)
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

                // Display selected employees as chips
                if (selectedEmployees.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Selected Mail ID",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    // Display chips in a scrollable row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedEmployees.forEach { employee ->
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .background(Color.White, RoundedCornerShape(20.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = employee.mail,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        onEmployeeRemoved(employee)
                                    },
                                    modifier = Modifier.size(20.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cross),
                                        contentDescription = "Remove",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Department Based" -> {
            // Department Search Field with suggestions
            Column {
                Text(
                    text = "Department Search",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                // Search field
                OutlinedTextField(
                    value = departmentSearchQuery,
                    onValueChange = { query ->
                        onDepartmentSearchQueryChange(query)
                    },
                    placeholder = {
                        Text(
                            text = "Search for Departments to Add",
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
                        if (departmentSearchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onDepartmentSearchQueryChange("")
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

                // Suggestions list
                if (suggestedDepartments.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn {
                            items(suggestedDepartments) { dept ->
                                Text(
                                    text = dept,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!selectedDepartments.contains(dept)) {
                                                onDepartmentAdded(dept)
                                            }
                                            onDepartmentSearchQueryChange("")
                                        }
                                        .padding(12.dp),
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 14.sp,
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }

                // Display selected departments as chips
                if (selectedDepartments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Selected Departments",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    // Display chips in a column
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedDepartments.forEach { dept ->
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .background(Color.White, RoundedCornerShape(20.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = dept,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        onDepartmentRemoved(dept)
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cross),
                                        contentDescription = "Remove Department",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Location Based" -> {
            // Location Search Field with suggestions
            Column {
                Text(
                    text = "Location Search",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                // Search field
                OutlinedTextField(
                    value = locationSearchQuery,
                    onValueChange = { query ->
                        onLocationSearchQueryChange(query)
                    },
                    placeholder = {
                        Text(
                            text = "Search for Locations to Add",
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
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = com.archeGlobal.one.R.drawable.search11),
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestions list
                if (suggestedLocations.isNotEmpty()) {
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
                            suggestedLocations.forEach { location ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onLocationAdded(location)
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = location,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                    )
                                }
                                if (location != suggestedLocations.last()) {
                                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Display selected locations as chips
                if (selectedLocations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Selected Locations",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    // Display chips in a column
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedLocations.forEach { location ->
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .background(Color.White, RoundedCornerShape(20.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = location,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        onLocationRemoved(location)
                                    },
                                    modifier = Modifier.size(20.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cross),
                                        contentDescription = "Remove",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Project Based" -> {
            // Project Search Field with suggestions
            Column {
                Text(
                    text = "Project Search",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                // Search field
                OutlinedTextField(
                    value = projectSearchQuery,
                    onValueChange = { query ->
                        onProjectSearchQueryChange(query)
                    },
                    placeholder = {
                        Text(
                            text = "Search for Projects to Add",
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
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = com.archeGlobal.one.R.drawable.search11),
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestions list
                if (suggestedProjects.isNotEmpty()) {
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
                            suggestedProjects.forEach { project ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onProjectAdded(project)
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = project,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                    )
                                }
                                if (project != suggestedProjects.last()) {
                                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Display selected projects as chips
                if (selectedProjects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Selected Projects",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    // Display chips in a column
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedProjects.forEach { project ->
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .background(Color.White, RoundedCornerShape(20.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = project,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        onProjectRemoved(project)
                                    },
                                    modifier = Modifier.size(20.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cross),
                                        contentDescription = "Remove",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        "Everyone@Arche" -> {
            // Display everyone@arche.global as a chip
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Selected Group",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Display chip
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "everyone@arche.global",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
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

    // Select images to attach button (max 4 images)
    val totalImages = existingImageUrls.size + postImageUris.size
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (totalImages < 4) Color.White else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = totalImages < 4) {
                if (totalImages < 4) {
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
                tint = if (totalImages < 4) Color.Gray else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (totalImages < 4)
                    "Select images to attach"
                else
                    "Maximum 3 images reached",
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                color = if (totalImages < 4) Color.Gray else Color.Gray.copy(alpha = 0.5f),
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
            useShortMonth = true,
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
            useShortMonth = true,
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
        label = "Support Channel Details",
        value = supportChannelDetails,
        onValueChange = onSupportChannelDetailsChange,
        placeholder = "Support Channel Details",
        singleLine = false,
        minLines = 2,
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
                disabledContainerColor = PrimaryRed,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSubmitting && announcementDescription.isNotBlank() && postSubject.isNotBlank() && postPriority.isNotBlank(),
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

    // Info message about home screen display
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0x1ADD3825),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { /* Info message */ },
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.info),
                contentDescription = "Info",
                tint = Color(0xFFDD3825),
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "This will be displayed on the home screen",
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = GraphikFontFamily,
            textAlign = TextAlign.Left,
            color = Color.Black,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Post Subject (Event Subject)
    PostTextField(
        label = "Post Subject",
        value = eventSubject,
        onValueChange = onEventSubjectChange,
        placeholder = "Enter Subject",
        singleLine = true,
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Post Description (Event Description)
    PostTextField(
        label = "Post Description",
        value = eventDescription,
        onValueChange = onEventDescriptionChange,
        placeholder = "Enter post description...",
        singleLine = false,
        minLines = 3,
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Select image to attach button - only show if no image is attached
    if (existingImageUrls.isEmpty() && eventImageUri == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
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
    }

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
                disabledContainerColor = PrimaryRed,
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
                contentDescription = "Create Post",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Post",
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
    placeholder: String = "",
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
                placeholder = if (selectedValue.isEmpty() && placeholder.isNotEmpty()) {
                    {
                        Text(
                            text = placeholder,
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                } else {
                    null
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.dropdown),
                        contentDescription = "Dropdown",
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
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
                maxLines = 3,
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
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
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
    useShortMonth: Boolean = false,
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
            val monthName = if (useShortMonth) getMonthNameShort(selectedMonth) else getMonthName(selectedMonth)
            val formattedDate = String.format("%02d %s %04d", selectedDay, monthName, selectedYear)
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
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
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
                horizontalArrangement = Arrangement.Start,  // Changed from Center to Start
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else {
                        val monthName = if (useShortMonth) getMonthNameShort(month) else getMonthName(month)
                        String.format("%02d %s %04d", day, monthName, year)
                    },
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isNotEmpty()) Color.Black else Color.Gray,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = com.archeGlobal.one.R.drawable.created),
                    contentDescription = "Calendar",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
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
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
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
                    text = if (value.isNotEmpty()) value else {
                        String.format("%02d:%02d %s",
                            if (hour > 12) hour - 12 else if (hour == 0) 12 else hour,
                            minute,
                            if (hour >= 12) "PM" else "AM"
                        )
                    },
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
        0 -> "January"
        1 -> "February"
        2 -> "March"
        3 -> "April"
        4 -> "May"
        5 -> "June"
        6 -> "July"
        7 -> "August"
        8 -> "September"
        9 -> "October"
        10 -> "November"
        11 -> "December"
        else -> ""
    }
}

private fun getMonthNameShort(month: Int): String {
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

// Convert date string with full month to abbreviated month for preview display
private fun convertToShortMonth(dateString: String): String {
    if (dateString.isEmpty()) return dateString

    return dateString
        .replace("January", "Jan")
        .replace("February", "Feb")
        .replace("March", "Mar")
        .replace("April", "Apr")
        .replace("May", "May")
        .replace("June", "Jun")
        .replace("July", "Jul")
        .replace("August", "Aug")
        .replace("September", "Sep")
        .replace("October", "Oct")
        .replace("November", "Nov")
        .replace("December", "Dec")
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
    existingImageUrls: List<String> = emptyList(),
    selectedEmployees: List<com.archeGlobal.one.model.SuggestedUser> = emptyList(),
    selectedDepartments: List<String> = emptyList(),
    selectedLocations: List<String> = emptyList(),
    selectedProjects: List<String> = emptyList(),
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
                        .padding(16.dp),
                ) {
                    // Title
                    Text(
                        text = "HeadsUp Preview",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // White content card (includes profile header)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Header with profile, name, and priority in same line
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Profile picture
                                    Card(
                                        modifier = Modifier.size(32.dp),
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
                                                    fontSize = 12.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = userName,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color.Black,
                                        )
                                        Text(
                                            text = "12 November 2025",
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                        )
                                        // Target audience with icon
                                        val targetLabel = when {
                                            postGroup == "Everyone" -> "Tagged Employees"
                                            selectedDepartments.isNotEmpty() -> "Tagged Department"
                                            selectedLocations.isNotEmpty() -> "Tagged Location"
                                            selectedEmployees.isNotEmpty() -> "Tagged Employees"
                                            selectedProjects.isNotEmpty() -> "Tagged Project"
                                            else -> null
                                        }

                                        if (targetLabel != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.item_name),
                                                    contentDescription = "Target Audience",
                                                    modifier = Modifier.size(10.dp),
                                                    tint = Color.Gray
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = targetLabel,
                                                    fontFamily = GraphikFontFamily,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF666666)
                                                )
                                            }
                                        }

                                        // Show actual target details
                                        val targetDetails = when {
                                            postGroup == "Everyone" -> "Everyone@arche.global"
                                            selectedDepartments.isNotEmpty() -> selectedDepartments.joinToString("\n")
                                            selectedLocations.isNotEmpty() -> selectedLocations.joinToString("\n")
                                            selectedEmployees.isNotEmpty() -> selectedEmployees.map { it.mail }.joinToString("\n")
                                            selectedProjects.isNotEmpty() -> selectedProjects.joinToString("\n")
                                            else -> null
                                        }

                                        if (targetDetails != null) {
                                            Text(
                                                text = targetDetails,
                                                fontFamily = GraphikFontFamily,
                                                fontSize = 10.sp,
                                                color = Color(0xFF999999),
                                                modifier = Modifier.padding(start = 14.dp),
                                                softWrap = true,
                                                overflow = TextOverflow.Visible
                                            )
                                        }
                                    }
                                }

                                // Priority badge - only show if selected
                                if (postPriority.isNotBlank()) {
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
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = postPriority,
                                            fontFamily = GraphikFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            // Post Subject - only show if not default selection
                            if (postSubject.isNotBlank()) {
                                Text(
                                    text = postSubject,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Announcement Description - only show if not empty
                            if (announcementDescription.isNotEmpty()) {
                                Text(
                                    text = announcementDescription,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Attached Images - display horizontally scrollable like in HeadsUpScreen
                            val totalImages = existingImageUrls.size + postImageUris.size
                            if (totalImages > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy((-20).dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Show existing images first, then new images
                                    existingImageUrls.forEach { imageUrl ->
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUrl),
                                            contentDescription = "Post image",
                                            modifier = Modifier
                                                .width(130.dp)
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    postImageUris.forEach { imageUri ->
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUri),
                                            contentDescription = "Post image",
                                            modifier = Modifier
                                                .width(130.dp)
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }

                            // Support Details if available
                            if (supportChannelDetails.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Support Details :",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = supportChannelDetails,
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            // Activity Duration if available
                            if (startDurationDate.isNotEmpty() && startDurationTime.isNotEmpty() &&
                                endDurationDate.isNotEmpty() && endDurationTime.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Activity Duration :",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Convert short month format to full month format for preview
                                val startDateFull = startDurationDate.replace(" Nov ", " November ")
                                    .replace(" Jan ", " January ")
                                    .replace(" Feb ", " February ")
                                    .replace(" Mar ", " March ")
                                    .replace(" Apr ", " April ")
                                    .replace(" May ", " May ")
                                    .replace(" Jun ", " June ")
                                    .replace(" Jul ", " July ")
                                    .replace(" Aug ", " August ")
                                    .replace(" Sep ", " September ")
                                    .replace(" Oct ", " October ")
                                    .replace(" Dec ", " December ")
                                val endDateFull = endDurationDate.replace(" Nov ", " November ")
                                    .replace(" Jan ", " January ")
                                    .replace(" Feb ", " February ")
                                    .replace(" Mar ", " March ")
                                    .replace(" Apr ", " April ")
                                    .replace(" May ", " May ")
                                    .replace(" Jun ", " June ")
                                    .replace(" Jul ", " July ")
                                    .replace(" Aug ", " August ")
                                    .replace(" Sep ", " September ")
                                    .replace(" Oct ", " October ")
                                    .replace(" Dec ", " December ")
                                Text(
                                    text = "$startDateFull, $startDurationTime - $endDateFull, $endDurationTime",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Start and End dates - only show if at least one is filled
                            if (postStartDate.isNotEmpty() || postEndDate.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
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
                                                fontSize = 9.sp,
                                                color = Color.Gray,
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(id = com.archeGlobal.one.R.drawable.green),
                                                    contentDescription = "Start Date",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = Color(0xFF66BB6A)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = convertToShortMonth(postStartDate),
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 10.sp,
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
                                                fontSize = 9.sp,
                                                color = Color.Gray,
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(id = com.archeGlobal.one.R.drawable.red),
                                                    contentDescription = "End Date",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = Color(0xFFD32F2F)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = convertToShortMonth(postEndDate),
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 10.sp,
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
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EBE3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    // Title
                    Text(
                        text = "Event Preview",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // White content card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(16.dp)
                        ) {
                            // Event Image
                            eventImageUri?.let { uri ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = "Event Image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Event Title/Subject - Centered and bold
                            if (eventSubject.isNotEmpty()) {
                                Text(
                                    text = eventSubject,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Event Date - Centered and gray
                            if (eventDate.isNotEmpty()) {
                                Text(
                                    text = eventDate,
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Event Description - Centered
                            if (eventDescription.isNotEmpty()) {
                                Text(
                                    text = eventDescription,
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Start and End Dates
                            if (eventStartDate.isNotEmpty() || eventEndDate.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    if (eventStartDate.isNotEmpty()) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Post Start Date",
                                                fontFamily = GraphikFontFamily,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.green),
                                                    contentDescription = "Start Date",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFF66BB6A)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = convertToShortMonth(eventStartDate),
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }

                                    if (eventEndDate.isNotEmpty()) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Post End Date",
                                                fontFamily = GraphikFontFamily,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.red),
                                                    contentDescription = "End Date",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFFD32F2F)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = convertToShortMonth(eventEndDate),
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                        shape = RoundedCornerShape(10.dp)
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
