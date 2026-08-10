package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.ChatMessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val messageText: String = "",
    val selectedImageUrl: String? = null,
    val editingMessageId: Int? = null,
    val limitErrorAlert: String? = null,
    val showGalleryPicker: Boolean = false
)

class ChatViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onMessageTextChange(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun selectPresetImage(imageUrl: String?) {
        _uiState.update { it.copy(selectedImageUrl = imageUrl, showGalleryPicker = false) }
    }

    fun toggleGalleryPicker(show: Boolean) {
        _uiState.update { it.copy(showGalleryPicker = show) }
    }

    fun startEditingMessage(message: ChatMessageEntity) {
        _uiState.update {
            it.copy(
                editingMessageId = message.id,
                messageText = message.text
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editingMessageId = null, messageText = "") }
    }

    fun dismissLimitAlert() {
        _uiState.update { it.copy(limitErrorAlert = null) }
    }

    fun getGlobalMessages(): Flow<List<ChatMessageEntity>> {
        return repository.chatDao.getGlobalChatMessages()
    }

    fun getTournamentMessages(tournamentId: Int): Flow<List<ChatMessageEntity>> {
        return repository.chatDao.getTournamentChatMessages(tournamentId)
    }

    fun sendMessage(
        senderEmail: String,
        senderName: String,
        isAdmin: Boolean = false,
        tournamentId: Int? = null
    ) {
        val state = _uiState.value
        val text = state.messageText.trim()
        if (text.isEmpty() && state.selectedImageUrl == null) return

        viewModelScope.launch {
            if (state.editingMessageId != null) {
                repository.editChatMessage(state.editingMessageId, text)
                _uiState.update { it.copy(editingMessageId = null, messageText = "", selectedImageUrl = null) }
            } else {
                val limitError = repository.sendChatMessage(
                    senderEmail = senderEmail,
                    senderName = senderName,
                    isAdmin = isAdmin,
                    text = text,
                    imageUrl = state.selectedImageUrl,
                    tournamentId = tournamentId
                )

                if (limitError != null) {
                    _uiState.update { it.copy(limitErrorAlert = limitError) }
                } else {
                    _uiState.update { it.copy(messageText = "", selectedImageUrl = null) }
                }
            }
        }
    }
}
