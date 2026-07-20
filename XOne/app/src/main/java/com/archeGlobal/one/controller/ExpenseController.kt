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
import com.archeGlobal.one.network.ApiValidationErrorResponse

class ExpenseController(private val context: Context) {
    private val tag = "ExpenseController"
    private val preferencesManager = PreferencesManager(context)

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var expenses = mutableStateOf<List<ExpenseUi>>(emptyList())
    var submittedExpenses = mutableStateOf<List<SubmittedExpenseUi>>(emptyList())
    var submittedLoading = mutableStateOf(false)
    var submittedError = mutableStateOf<String?>(null)
    var detailLoading = mutableStateOf(false)
    var downloadLoading = mutableStateOf(false)
    var userOptions = mutableStateOf<List<ExpenseUserOption>>(emptyList())
    var userOptionsLoading = mutableStateOf(false)
    var splitLoading = mutableStateOf(false)

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
                        onError("Failed to get file URL (${detailResponse.code()})")
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
                        onError("Failed to load expense details (${response.code()})")
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
                        onError("Failed to upload expense (${response.code()})")
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

    private fun parseErrorMessage(errorBody: String, fallback: String): String {
        if (errorBody.isBlank()) return fallback
        return try {
            val parsed = Gson().fromJson(errorBody, ApiValidationErrorResponse::class.java)
            val messages = parsed.detail?.mapNotNull { detail ->
                detail.message?.takeIf { it.isNotBlank() }
                    ?: detail.msg?.takeIf { it.isNotBlank() }
            }.orEmpty()
            if (messages.isNotEmpty()) messages.joinToString("\n") else fallback
        } catch (_: Exception) {
            fallback
        }
    }

    companion object {
        const val KEY_EXPENSE_USER_ID = "expense_user_id"
        const val KEY_EXPENSE_USER_EMAIL = "expense_user_email"
    }
}
