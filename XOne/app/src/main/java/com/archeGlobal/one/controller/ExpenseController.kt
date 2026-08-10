package com.archeGlobal.one.controller

import com.archeGlobal.one.network.ExpenseDetailUi
import com.archeGlobal.one.network.ExpenseRetrofitClient
import com.archeGlobal.one.network.ExpenseUi
import com.archeGlobal.one.network.ExpenseUserOption
import com.archeGlobal.one.network.SplitExpenseRequest
import com.archeGlobal.one.network.SplitExpenseResponse
import com.archeGlobal.one.network.SubmittedExpenseUi
import com.archeGlobal.one.network.toDetailUi
import com.archeGlobal.one.network.toSubmittedUi
import com.archeGlobal.one.network.toUi
import com.archeGlobal.one.utils.PreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.mutableStateOf

class ExpenseController(private val context: Context) {
    private val tag = "ExpenseController"
    private val preferencesManager = PreferencesManager(context)

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var expenses = mutableStateOf<List<ExpenseUi>>(emptyList())
    var submittedExpenses = mutableStateOf<List<SubmittedExpenseUi>>(emptyList())
    var submittedLoading = mutableStateOf(false)
    var submittedError = mutableStateOf<String?>(null)
    var otherExpenses = mutableStateOf<List<ExpenseUi>>(emptyList())
    var otherLoading = mutableStateOf(false)
    var otherError = mutableStateOf<String?>(null)
    var approvalExpenses = mutableStateOf<List<SubmittedExpenseUi>>(emptyList())
    var approvalExpensesLoading = mutableStateOf(false)
    var approvalExpensesError = mutableStateOf<String?>(null)
    var detailLoading = mutableStateOf(false)
    var downloadLoading = mutableStateOf(false)
    var userOptions = mutableStateOf<List<ExpenseUserOption>>(emptyList())
    var userOptionsLoading = mutableStateOf(false)
    var splitLoading = mutableStateOf(false)
    var userExpenseStatusLoading = mutableStateOf(false)

    fun getStoredExpenseUserId(): Int = preferencesManager.getInt(KEY_EXPENSE_USER_ID, -1)

    fun getStoredExpenseUserEmail(): String = preferencesManager.getString(KEY_EXPENSE_USER_EMAIL, "").orEmpty()

    fun fetchExpenses(statuses: List<String> = listOf("Uploaded", "Extracted", "Extracting")) {
        isLoading.value = true
        errorMessage.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.getExpenses(page = 1, perPage = 10, statuses = statuses)
                if (response.isSuccessful) {
                    val items = response.body()?.data?.map { it.toUi() }.orEmpty()
                    withContext(Dispatchers.Main) {
                        expenses.value = items
                        errorMessage.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch expenses: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        expenses.value = emptyList()
                        errorMessage.value = "Failed to load expenses (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching expenses: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    expenses.value = emptyList()
                    errorMessage.value = e.message ?: "Failed to load expenses"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }

    fun fetchOtherExpenses(page: Int = 1, perPage: Int = 10) {
        otherLoading.value = true
        otherError.value = null

        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.getExpenses(
                    page = page,
                    perPage = perPage,
                    statuses = listOf("Extracted"),
                    category = "others",
                )
                if (response.isSuccessful) {
                    val items = response.body()?.data?.map { it.toUi() }.orEmpty()
                    withContext(Dispatchers.Main) {
                        otherExpenses.value = items
                        otherError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch other expenses: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        otherExpenses.value = emptyList()
                        otherError.value = parseErrorMessage(errorBody, "Failed to load expenses (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching other expenses: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    otherExpenses.value = emptyList()
                    otherError.value = e.message ?: "Failed to load expenses"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    otherLoading.value = false
                }
            }
        }
    }

    fun fetchSubmittedExpenses(page: Int = 1, perPage: Int = 10) {
        submittedLoading.value = true
        submittedError.value = null
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.getSubmittedExpenses(page = page, perPage = perPage)
                if (response.isSuccessful) {
                    val items = response.body()?.data?.map { it.toSubmittedUi() }.orEmpty()
                    withContext(Dispatchers.Main) {
                        submittedExpenses.value = items
                        submittedError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch submitted expenses: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        submittedExpenses.value = emptyList()
                        submittedError.value = "Failed to load submitted expenses (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching submitted expenses: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    submittedExpenses.value = emptyList()
                    submittedError.value = e.message ?: "Failed to load submitted expenses"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    submittedLoading.value = false
                }
            }
        }
    }

    fun fetchApprovalExpenses(page: Int = 1, perPage: Int = 10) {
        approvalExpensesLoading.value = true
        approvalExpensesError.value = null
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.getSubmittedExpenses(
                    page = page,
                    perPage = perPage,
                    view = "approvals",
                )
                if (response.isSuccessful) {
                    val items = response.body()?.data?.map { it.toSubmittedUi() }.orEmpty()
                    withContext(Dispatchers.Main) {
                        approvalExpenses.value = items
                        approvalExpensesError.value = null
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch approval expenses: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        approvalExpenses.value = emptyList()
                        approvalExpensesError.value = "Failed to load approval expenses (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching approval expenses: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    approvalExpenses.value = emptyList()
                    approvalExpensesError.value = e.message ?: "Failed to load approval expenses"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    approvalExpensesLoading.value = false
                }
            }
        }
    }

    fun downloadExpenseFile(
        expenseId: String,
        fallbackFileName: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        downloadLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val detailResponse = ExpenseRetrofitClient.expenseService.getExpense(expenseId)
                if (!detailResponse.isSuccessful) {
                    val errorBody = detailResponse.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch expense for download: HTTP ${detailResponse.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to get file URL (${detailResponse.code()})"))
                    }
                    return@launch
                }

                val detail = detailResponse.body()
                val fileUrl = detail?.url
                if (fileUrl.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        onError("File URL not available")
                    }
                    return@launch
                }

                val fileName = (detail.name ?: fallbackFileName ?: "expense_$expenseId")
                    .replace(Regex("""[\\/:*?"<>|]"""), "_")

                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(Uri.parse(fileUrl))
                    .setTitle(fileName)
                    .setDescription("Downloading expense document")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                downloadManager.enqueue(request)
                withContext(Dispatchers.Main) {
                    onSuccess("Download started: $fileName")
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception downloading expense file: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to download file")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    downloadLoading.value = false
                }
            }
        }
    }

    fun fetchExpenseDetail(
        expenseId: String,
        onSuccess: (ExpenseDetailUi) -> Unit,
        onError: (String) -> Unit,
    ) {
        detailLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.getExpense(expenseId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        withContext(Dispatchers.Main) {
                            onSuccess(body.toDetailUi())
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onError("Failed to load expense details")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch expense detail: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to load expense details (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching expense detail: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to load expense details")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    detailLoading.value = false
                }
            }
        }
    }

    fun deleteExpense(
        expenseId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.deleteExpense(expenseId)
                if (response.isSuccessful) {
                    val message = response.body()?.message?.takeIf { it.isNotBlank() }
                        ?: "Expense deleted successfully"
                    withContext(Dispatchers.Main) {
                        fetchExpenses()
                        onSuccess(message)
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to delete expense: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to delete expense (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception deleting expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to delete expense")
                }
            }
        }
    }

    fun fetchUserOptions(
        onSuccess: (List<ExpenseUserOption>) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        userOptionsLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.getUserOptions()
                if (response.isSuccessful) {
                    val options = response.body().orEmpty()
                    withContext(Dispatchers.Main) {
                        userOptions.value = options
                        onSuccess(options)
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to fetch user options: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        userOptions.value = emptyList()
                        onError(parseErrorMessage(errorBody, "Failed to load users (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching user options: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    userOptions.value = emptyList()
                    onError(e.message ?: "Failed to load users")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    userOptionsLoading.value = false
                }
            }
        }
    }

    fun updateUserExpenseStatus(
        userExpenseId: Int,
        status: String,
        onSuccess: (com.archeGlobal.one.network.UserExpenseStatusUpdateResponse) -> Unit,
        onError: (String) -> Unit,
    ) {
        userExpenseStatusLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.updateUserExpenseStatus(
                    userExpenseId = userExpenseId,
                    request = com.archeGlobal.one.network.UserExpenseStatusUpdateRequest(status = status),
                )
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
                    Log.e(tag, "Failed to update user expense status: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to update status (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception updating user expense status: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to update status")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    userExpenseStatusLoading.value = false
                }
            }
        }
    }

    fun splitExpense(
        expenseId: String,
        request: SplitExpenseRequest,
        onSuccess: (SplitExpenseResponse) -> Unit,
        onError: (String) -> Unit,
    ) {
        splitLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.splitExpense(expenseId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    withContext(Dispatchers.Main) {
                        if (body != null) {
                            onSuccess(body)
                        } else {
                            onError("Empty split response")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to split expense: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to split expense (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception splitting expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to split expense")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    splitLoading.value = false
                }
            }
        }
    }

    fun uploadExpense(
        fileBytes: ByteArray,
        filename: String?,
        mimeType: String = "application/octet-stream",
        onSuccess: (com.archeGlobal.one.network.UploadExpenseResponse) -> Unit,
        onError: (String) -> Unit,
    ) {
        isLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaType = mimeType.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()
                val requestBody = fileBytes.toRequestBody(mediaType)
                val part = MultipartBody.Part.createFormData("file", filename ?: "upload", requestBody)
                val filenamePart = filename?.toRequestBody("text/plain".toMediaTypeOrNull())
                val response = ExpenseRetrofitClient.expenseService.uploadExpense(part, filenamePart)
                if (response.isSuccessful) {
                    val body = response.body()
                    withContext(Dispatchers.Main) {
                        if (body != null) {
                            onSuccess(body)
                            fetchExpenses()
                        } else {
                            onError("Empty upload response")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to upload expense: HTTP ${response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError(parseErrorMessage(errorBody, "Failed to upload expense (${response.code()})"))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception uploading expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to upload expense")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }

    fun submitExpense(
        expenseId: String,
        request: com.archeGlobal.one.network.ExpenseSubmitRequest,
        onSuccess: (com.archeGlobal.one.network.ExpenseSubmitResponse) -> Unit,
        onLimitExceeded: (com.archeGlobal.one.network.ExpenseSubmitErrorDetail) -> Unit,
        onDuplicateExpense: (com.archeGlobal.one.network.ExpenseSubmitErrorDetail) -> Unit,
        onError: (String) -> Unit,
    ) {
        isLoading.value = true
        ExpenseRetrofitClient.initialize(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ExpenseRetrofitClient.expenseService.submitExpense(expenseId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    withContext(Dispatchers.Main) {
                        if (body != null) {
                            onSuccess(body)
                        } else {
                            onError("Empty submit response")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(tag, "Failed to submit expense: HTTP ${response.code()} $errorBody")
                    val parsed = try {
                        Gson().fromJson(errorBody, com.archeGlobal.one.network.ExpenseSubmitErrorResponse::class.java)
                    } catch (_: Exception) {
                        null
                    }
                    val detail = parsed?.detail
                    val fallbackError = "Failed to submit expense (${response.code()})"
                    when {
                        detail != null &&
                            (detail.errorCode == "LIMIT_EXCEEDED" ||
                                detail.errorCode == "LIMIT_EXHAUSTED" ||
                                detail.message?.contains("spending limit", ignoreCase = true) == true) -> {
                            withContext(Dispatchers.Main) {
                                onLimitExceeded(detail)
                            }
                        }
                        detail != null && detail.errorCode == "DUPLICATE_EXPENSE" -> {
                            withContext(Dispatchers.Main) {
                                onDuplicateExpense(detail)
                            }
                        }
                        else -> {
                            val errorMessage = detail?.message?.takeIf { it.isNotBlank() }
                                ?: parseErrorMessage(errorBody, fallbackError)
                            withContext(Dispatchers.Main) {
                                onError(errorMessage)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception submitting expense: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to submit expense")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }

    private fun parseErrorMessage(errorBody: String, fallback: String): String =
        com.archeGlobal.one.network.parseApiErrorMessage(errorBody, fallback)

    companion object {
        const val KEY_EXPENSE_USER_ID = "expense_user_id"
        const val KEY_EXPENSE_USER_EMAIL = "expense_user_email"
    }
}
