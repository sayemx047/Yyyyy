package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TournamentUiState(
    val filterMode: String = "ALL", // "ALL", "SOLO", "DUO", "SQUAD"
    val selectedTournament: TournamentEntity? = null,
    val showRegistrationDialog: Boolean = false,
    val showInsufficientCreditDialog: Boolean = false,
    val requiredRechargeAmount: Double = 0.0,
    val currentWalletBalance: Double = 0.0,
    val registrationSuccessTicket: RegistrationEntity? = null,
    val selectedPrivateRoomTournament: TournamentEntity? = null
)

class TournamentViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TournamentUiState())
    val uiState: StateFlow<TournamentUiState> = _uiState.asStateFlow()

    val allTournaments: StateFlow<List<TournamentEntity>> = repository.tournamentDao.getAllTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedBanners: StateFlow<List<PinnedBannerEntity>> = repository.pinnedBannerDao.getPinnedBanners()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilterMode(mode: String) {
        _uiState.update { it.copy(filterMode = mode) }
    }

    fun openRegistrationDialog(tournament: TournamentEntity) {
        _uiState.update {
            it.copy(
                selectedTournament = tournament,
                showRegistrationDialog = true
            )
        }
    }

    fun closeRegistrationDialog() {
        _uiState.update { it.copy(showRegistrationDialog = false) }
    }

    fun openPrivateRoomInfo(tournament: TournamentEntity) {
        _uiState.update { it.copy(selectedPrivateRoomTournament = tournament) }
    }

    fun closePrivateRoomInfo() {
        _uiState.update { it.copy(selectedPrivateRoomTournament = null) }
    }

    fun dismissInsufficientCreditDialog() {
        _uiState.update { it.copy(showInsufficientCreditDialog = false) }
    }

    fun dismissRegistrationTicket() {
        _uiState.update { it.copy(registrationSuccessTicket = null) }
    }

    fun getUserRegistrations(userEmail: String): Flow<List<RegistrationEntity>> {
        return repository.registrationDao.getRegistrationsForUser(userEmail)
    }

    fun submitTournamentRegistration(
        userEmail: String,
        ffUid: String,
        firstName: String,
        lastName: String,
        squadName: String,
        player1: String,
        player2: String,
        player3: String,
        player4: String,
        whatsapp: String
    ) {
        val tournament = _uiState.value.selectedTournament ?: return
        viewModelScope.launch {
            val result = repository.joinTournament(
                tournamentId = tournament.id,
                userEmail = userEmail,
                ffUid = ffUid,
                firstName = firstName,
                lastName = lastName,
                squadName = squadName,
                player1 = player1,
                player2 = player2,
                player3 = player3,
                player4 = player4,
                whatsapp = whatsapp
            )

            when (result) {
                is JoinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showRegistrationDialog = false,
                            registrationSuccessTicket = result.registration
                        )
                    }
                }
                is JoinResult.InsufficientFunds -> {
                    _uiState.update {
                        it.copy(
                            showRegistrationDialog = false,
                            showInsufficientCreditDialog = true,
                            requiredRechargeAmount = result.requiredAmount,
                            currentWalletBalance = result.currentBalance
                        )
                    }
                }
                is JoinResult.Error -> {
                    // Handled gracefully
                }
            }
        }
    }
}
