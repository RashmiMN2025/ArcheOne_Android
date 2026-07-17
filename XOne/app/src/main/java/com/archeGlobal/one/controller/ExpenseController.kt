package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.network.ExpenseRetrofitClient
import com.archeGlobal.one.network.ExpenseUi
import com.archeGlobal.one.network.toUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ExpenseController(private val context: Context) {
    private val tag = "ExpenseController"

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var expenses = mutableStateOf<List<ExpenseUi>>(emptyList())

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
                    Log.e(tag, "Failed to fetch expenses: HTTP ${'$'}{response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        expenses.value = emptyList()
                        errorMessage.value = "Failed to load expenses (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching expenses: ${'$'}{e.message}", e)
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
                    Log.e(tag, "Failed to upload expense: HTTP ${'$'}{response.code()} $errorBody")
                    withContext(Dispatchers.Main) {
                        onError("Failed to upload expense (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception uploading expense: ${'$'}{e.message}", e)
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
}
