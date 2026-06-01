package com.altf4.ourfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altf4.ourfinance.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _saveResult = MutableSharedFlow<Result<String>>()
    val saveResult = _saveResult.asSharedFlow()

    fun addExpense(
        username: String,
        amount: Double,
        category: String,
        timestamp: Long,
        description: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                // Generate a unique 8-character Alphanumeric ID
                val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
                val generatedId = (1..8)
                    .map { kotlin.random.Random.nextInt(0, charPool.size).let { charPool[it] } }
                    .joinToString("")

                val response = RetrofitClient.apiService.addExpense(
                    id = generatedId,
                    username = username,
                    amount = amount,
                    category = category,
                    timestamp = timestamp,
                    description = description,
                    isEdited = false,
                    editCredential = "[]"
                )
                if (response.status == "success") {
                    _saveResult.emit(Result.success("Expense added successfully"))
                } else {
                    _saveResult.emit(Result.failure(Exception(response.message ?: "Failed to add expense")))
                }
            } catch (e: Exception) {
                _saveResult.emit(Result.failure(e))
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateExpense(
        id: String,
        username: String,
        amount: Double,
        category: String,
        timestamp: Long,
        description: String,
        editorName: String,
        editTime: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val response = RetrofitClient.apiService.updateExpense(
                    id = id,
                    username = username,
                    amount = amount,
                    category = category,
                    timestamp = timestamp,
                    description = description,
                    isEdited = true,
                    editCredential = "", // The script handles appending
                    editorName = editorName,
                    editTime = editTime
                )
                if (response.status == "success") {
                    _saveResult.emit(Result.success("Expense updated successfully"))
                } else {
                    _saveResult.emit(Result.failure(Exception(response.message ?: "Failed to update expense")))
                }
            } catch (e: Exception) {
                _saveResult.emit(Result.failure(e))
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun addSettlement(
        username: String,
        from: String,
        to: String,
        amount: Double,
        description: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                // Generate a unique 8-character Alphanumeric ID
                val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
                val generatedId = (1..8)
                    .map { kotlin.random.Random.nextInt(0, charPool.size).let { charPool[it] } }
                    .joinToString("")

                val response = RetrofitClient.apiService.addSettlement(
                    id = generatedId,
                    username = username,
                    from = from,
                    to = to,
                    amount = amount,
                    description = description,
                    timestamp = timestamp,
                    isEdited = false,
                    editCredential = "[]"
                )
                if (response.status == "success") {
                    _saveResult.emit(Result.success("Settlement added successfully"))
                } else {
                    _saveResult.emit(Result.failure(Exception(response.message ?: "Failed to add settlement")))
                }
            } catch (e: Exception) {
                _saveResult.emit(Result.failure(e))
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateSettlement(
        id: String,
        from: String,
        to: String,
        amount: Double,
        description: String,
        timestamp: Long,
        editorName: String,
        editTime: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val response = RetrofitClient.apiService.updateSettlement(
                    id = id,
                    from = from,
                    to = to,
                    amount = amount,
                    description = description,
                    timestamp = timestamp,
                    isEdited = true,
                    editCredential = "", // AppScript handles appending
                    editorName = editorName,
                    editTime = editTime
                )
                if (response.status == "success") {
                    _saveResult.emit(Result.success("Settlement updated successfully"))
                } else {
                    _saveResult.emit(Result.failure(Exception(response.message ?: "Failed to update settlement")))
                }
            } catch (e: Exception) {
                _saveResult.emit(Result.failure(e))
            } finally {
                _isSaving.value = false
            }
        }
    }
}
