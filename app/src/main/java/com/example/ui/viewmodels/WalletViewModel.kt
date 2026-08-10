package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.WalletTransactionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WalletUiState(
    val txIdInput: String = "",
    val amountInput: String = "50",
    val statusMessage: String? = null,
    val isLoading: Boolean = false
)

class WalletViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    fun onTxIdChange(txId: String) {
        _uiState.update { it.copy(txIdInput = txId) }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amountInput = amount) }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun getUserTransactions(userEmail: String): Flow<List<WalletTransactionEntity>> {
        return repository.walletDao.getTransactionsForUser(userEmail)
    }

    fun submitRecharge(userEmail: String) {
        val state = _uiState.value
        val amount = state.amountInput.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val message = repository.submitRechargeRequest(userEmail, state.txIdInput, amount)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    statusMessage = message,
                    txIdInput = if (message.contains("submitted")) "" else it.txIdInput
                )
            }
        }
    }
}
