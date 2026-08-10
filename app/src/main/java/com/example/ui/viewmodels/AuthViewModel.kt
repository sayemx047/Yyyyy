package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.AuthResult
import com.example.data.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val isLoggedIn: Boolean = false,
    val emailInput: String = "",
    val passwordInput: String = "",
    val isSavePasswordChecked: Boolean = true,
    val isLoading: Boolean = false,
    val alertDialogTitle: String? = null,
    val alertDialogMessage: String? = null,
    val isSuccessAlert: Boolean = false,
    val isBypassMode: Boolean = false
)

class AuthViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Observe user changes from DB reactive flow (only resubscribes when the logged-in email changes)
    init {
        viewModelScope.launch {
            _uiState
                .map { it.currentUser?.email }
                .distinctUntilChanged()
                .collectLatest { email ->
                    if (email != null) {
                        repository.userDao.getUserByEmail(email).collect { updatedUser ->
                            if (updatedUser != null) {
                                _uiState.update { it.copy(currentUser = updatedUser) }
                            }
                        }
                    }
                }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(emailInput = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(passwordInput = password) }
    }

    fun onSavePasswordToggle(saved: Boolean) {
        _uiState.update { it.copy(isSavePasswordChecked = saved) }
        val email = _uiState.value.currentUser?.email
        if (email != null) {
            viewModelScope.launch {
                repository.updateSavePasswordPreference(email, saved)
            }
        }
    }

    fun login() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = repository.login(state.emailInput, state.passwordInput)
                _uiState.update { it.copy(isLoading = false) }
                when (result) {
                    is AuthResult.Success -> {
                        _uiState.update {
                            it.copy(
                                currentUser = result.user,
                                isLoggedIn = true,
                                alertDialogTitle = "Login Success 🎉",
                                alertDialogMessage = result.message,
                                isSuccessAlert = true
                            )
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.update {
                            it.copy(
                                alertDialogTitle = "Login Failed ❌",
                                alertDialogMessage = result.message,
                                isSuccessAlert = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        alertDialogTitle = "Login Failed ❌",
                        alertDialogMessage = "Something went wrong: ${e.message ?: "unknown error"}. Please try again.",
                        isSuccessAlert = false
                    )
                }
            }
        }
    }

    fun bypassLoginQuick() {
        _uiState.update { it.copy(emailInput = "x", passwordInput = "y") }
        login()
    }

    fun register(name: String, email: String, pass: String, ffUid: String, whatsapp: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.registerUser(name, email, pass, ffUid, whatsapp)
            _uiState.update { it.copy(isLoading = false) }
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentUser = result.user,
                            isLoggedIn = true,
                            alertDialogTitle = "Account Created! 🎮",
                            alertDialogMessage = result.message,
                            isSuccessAlert = true
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            alertDialogTitle = "Registration Failed ❌",
                            alertDialogMessage = result.message,
                            isSuccessAlert = false
                        )
                    }
                }
            }
        }
    }

    fun changePassword(currentPass: String, newPass: String) {
        val email = _uiState.value.currentUser?.email ?: return
        viewModelScope.launch {
            val success = repository.changePassword(email, currentPass, newPass)
            if (success) {
                _uiState.update {
                    it.copy(
                        alertDialogTitle = "Password Changed 🔐",
                        alertDialogMessage = "Your password has been securely updated.",
                        isSuccessAlert = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        alertDialogTitle = "Update Failed ❌",
                        alertDialogMessage = "Incorrect current password.",
                        isSuccessAlert = false
                    )
                }
            }
        }
    }

    fun deleteAccount(currentPass: String) {
        val email = _uiState.value.currentUser?.email ?: return
        viewModelScope.launch {
            val success = repository.deleteAccount(email, currentPass)
            if (success) {
                logout()
                _uiState.update {
                    it.copy(
                        alertDialogTitle = "Account Deleted 🗑️",
                        alertDialogMessage = "Your account and profile details have been permanently removed.",
                        isSuccessAlert = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        alertDialogTitle = "Deletion Failed ❌",
                        alertDialogMessage = "Incorrect password confirmation.",
                        isSuccessAlert = false
                    )
                }
            }
        }
    }

    fun requestForgotPasswordInChat() {
        val email = _uiState.value.emailInput.ifBlank { "Unauthenticated User" }
        viewModelScope.launch {
            repository.requestPasswordResetViaChat(email, "Gamer")
            _uiState.update {
                it.copy(
                    alertDialogTitle = "Reset Request Sent 📨",
                    alertDialogMessage = "Password reset request posted to Live Chat! An admin will verify and contact you.",
                    isSuccessAlert = true
                )
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(alertDialogTitle = null, alertDialogMessage = null) }
    }

    fun logout() {
        _uiState.update {
            AuthUiState(
                currentUser = null,
                isLoggedIn = false,
                emailInput = "",
                passwordInput = ""
            )
        }
    }
}
