package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUiState(
    val selectedTab: Int = 0, // 0: TxIDs, 1: Tournaments & Room IDs, 2: Notices & Banners, 3: Device Logs, 4: Live Support
    val noticeTitle: String = "",
    val noticeContent: String = "",
    val noticeTargetEmail: String = "ALL",
    val statusFeedback: String? = null
)

class AdminViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    val pendingTransactions: StateFlow<List<WalletTransactionEntity>> = repository.walletDao.getPendingTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<WalletTransactionEntity>> = repository.walletDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTournaments: StateFlow<List<TournamentEntity>> = repository.tournamentDao.getAllTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onNoticeTitleChange(title: String) {
        _uiState.update { it.copy(noticeTitle = title) }
    }

    fun onNoticeContentChange(content: String) {
        _uiState.update { it.copy(noticeContent = content) }
    }

    fun onNoticeTargetChange(email: String) {
        _uiState.update { it.copy(noticeTargetEmail = email) }
    }

    fun approveTransaction(txId: Int) {
        viewModelScope.launch {
            repository.approveWalletTransaction(txId)
            _uiState.update { it.copy(statusFeedback = "Transaction approved & balance updated!") }
        }
    }

    fun rejectTransaction(txId: Int) {
        viewModelScope.launch {
            repository.rejectWalletTransaction(txId)
            _uiState.update { it.copy(statusFeedback = "Transaction rejected.") }
        }
    }

    fun publishNotice() {
        val state = _uiState.value
        if (state.noticeTitle.isBlank() || state.noticeContent.isBlank()) return
        viewModelScope.launch {
            repository.noticeDao.insertNotice(
                NoticeEntity(
                    title = state.noticeTitle,
                    content = state.noticeContent,
                    targetUserEmail = state.noticeTargetEmail
                )
            )
            _uiState.update {
                it.copy(
                    noticeTitle = "",
                    noticeContent = "",
                    statusFeedback = "Notice published to ${state.noticeTargetEmail}!"
                )
            }
        }
    }

    fun updateTournamentRoom(tournamentId: Int, roomId: String, roomPass: String) {
        viewModelScope.launch {
            val t = repository.tournamentDao.getTournamentDirect(tournamentId) ?: return@launch
            val updated = t.copy(roomId = roomId, roomPassword = roomPass)
            repository.tournamentDao.updateTournament(updated)
            _uiState.update { it.copy(statusFeedback = "Room details updated for ${t.title}!") }
        }
    }

    fun createNewTournament(
        title: String,
        gameMode: String,
        entryFee: Double,
        prizePool: Double,
        matchTime: String
    ) {
        viewModelScope.launch {
            repository.tournamentDao.insertTournament(
                TournamentEntity(
                    title = title,
                    gameMode = gameMode,
                    entryFee = entryFee,
                    prizePool = prizePool,
                    matchTime = matchTime,
                    totalSlots = 48,
                    filledSlots = 0,
                    status = "UPCOMING"
                )
            )
            _uiState.update { it.copy(statusFeedback = "New Tournament created!") }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(statusFeedback = null) }
    }
}
