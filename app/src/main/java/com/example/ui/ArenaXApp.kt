package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.screens.*
import com.example.ui.theme.ArenaXTheme
import com.example.ui.viewmodels.*

@Composable
fun ArenaXApp() {
    val context = LocalContext.current
    val repository = remember {
        val db = try {
            AppDatabase.getDatabase(context)
        } catch (e: Exception) {
            android.util.Log.e("ARENAX_FATAL", "Room database failed to open: ${e.message}", e)
            throw e // re-thrown so MainActivity's CrashCatcher can show it on-screen
        }
        AppRepository(db, context)
    }

    val authViewModel: AuthViewModel = remember { AuthViewModel(repository) }
    val tournamentViewModel: TournamentViewModel = remember { TournamentViewModel(repository) }
    val walletViewModel: WalletViewModel = remember { WalletViewModel(repository) }
    val chatViewModel: ChatViewModel = remember { ChatViewModel(repository) }
    val adminViewModel: AdminViewModel = remember { AdminViewModel(repository) }

    val authState by authViewModel.uiState.collectAsState()
    val tournamentState by tournamentViewModel.uiState.collectAsState()
    val walletState by walletViewModel.uiState.collectAsState()
    val chatState by chatViewModel.uiState.collectAsState()
    val adminState by adminViewModel.uiState.collectAsState()

    val allTournaments by tournamentViewModel.allTournaments.collectAsState()
    val pinnedBanners by tournamentViewModel.pinnedBanners.collectAsState()
    val currentUser = authState.currentUser

    val userRegistrations by remember(currentUser?.email) {
        if (currentUser?.email != null) {
            tournamentViewModel.getUserRegistrations(currentUser.email)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val userTransactions by remember(currentUser?.email) {
        if (currentUser?.email != null) {
            walletViewModel.getUserTransactions(currentUser.email)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val globalNotices by remember(currentUser?.email) {
        repository.noticeDao.getNoticesForUser(currentUser?.email ?: "ALL")
    }.collectAsState(initial = emptyList())

    val globalChatMessages by chatViewModel.getGlobalMessages().collectAsState(initial = emptyList())

    val pendingTxList by adminViewModel.pendingTransactions.collectAsState()
    val allUsersList by adminViewModel.allUsers.collectAsState()
    val allTournamentsList by adminViewModel.allTournaments.collectAsState()

    val navController = rememberNavController()

    ArenaXTheme {
        NavHost(
            navController = navController,
            startDestination = if (authState.isLoggedIn) "home" else "login",
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // LOGIN SCREEN
            composable("login") {
                LoginScreen(
                    uiState = authState,
                    onEmailChange = authViewModel::onEmailChange,
                    onPasswordChange = authViewModel::onPasswordChange,
                    onSavePasswordToggle = authViewModel::onSavePasswordToggle,
                    onLoginClick = {
                        authViewModel.login()
                    },
                    onBypassQuickLogin = {
                        authViewModel.bypassLoginQuick()
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onOpenLiveChat = { navController.navigate("live_chat") },
                    onForgotPasswordClick = {
                        authViewModel.requestForgotPasswordInChat()
                        navController.navigate("live_chat")
                    },
                    onDismissDialog = authViewModel::dismissDialog
                )

                LaunchedEffect(authState.isLoggedIn) {
                    if (authState.isLoggedIn) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            }

            // REGISTER SCREEN
            composable("register") {
                RegisterScreen(
                    isLoading = authState.isLoading,
                    onRegisterSubmit = { name, email, pass, ffUid, whatsapp ->
                        authViewModel.register(name, email, pass, ffUid, whatsapp)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )

                LaunchedEffect(authState.isLoggedIn) {
                    if (authState.isLoggedIn) {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                }
            }

            // HOME DASHBOARD (TAB CONTAINER)
            composable("home") {
                MainTabScreen(
                    currentUser = currentUser,
                    tournaments = allTournaments,
                    registrations = userRegistrations,
                    pinnedBanners = pinnedBanners,
                    notices = globalNotices,
                    filterMode = tournamentState.filterMode,
                    onFilterChange = tournamentViewModel::setFilterMode,
                    onJoinClick = { tournament ->
                        tournamentViewModel.openRegistrationDialog(tournament)
                    },
                    onOpenPrivateRoomInfo = { tournament ->
                        tournamentViewModel.openPrivateRoomInfo(tournament)
                        navController.navigate("private_room")
                    },
                    onNavigateToAdminPanel = { navController.navigate("admin_panel") },
                    // Wallet props
                    transactions = userTransactions,
                    txIdInput = walletState.txIdInput,
                    amountInput = walletState.amountInput,
                    walletStatusMessage = walletState.statusMessage,
                    isWalletLoading = walletState.isLoading,
                    onTxIdChange = walletViewModel::onTxIdChange,
                    onAmountChange = walletViewModel::onAmountChange,
                    onSubmitRecharge = {
                        walletViewModel.submitRecharge(currentUser?.email ?: "")
                    },
                    onClearWalletStatus = walletViewModel::clearStatus,
                    // Chat props
                    globalChatMessages = globalChatMessages,
                    chatMessageText = chatState.messageText,
                    selectedImageUrl = chatState.selectedImageUrl,
                    editingMessageId = chatState.editingMessageId,
                    limitErrorAlert = chatState.limitErrorAlert,
                    showGalleryPicker = chatState.showGalleryPicker,
                    onChatMessageTextChange = chatViewModel::onMessageTextChange,
                    onSelectPresetImage = chatViewModel::selectPresetImage,
                    onToggleGalleryPicker = chatViewModel::toggleGalleryPicker,
                    onStartEditingMessage = chatViewModel::startEditingMessage,
                    onCancelEditing = chatViewModel::cancelEditing,
                    onDismissLimitAlert = chatViewModel::dismissLimitAlert,
                    onSendChatMessage = {
                        chatViewModel.sendMessage(
                            senderEmail = currentUser?.email ?: "unauthenticated@arenax.com",
                            senderName = currentUser?.name ?: "Guest Gamer",
                            isAdmin = false,
                            tournamentId = null
                        )
                    },
                    // Profile props
                    onChangePasswordSubmit = { currentPass, newPass ->
                        authViewModel.changePassword(currentPass, newPass)
                    },
                    onDeleteAccountSubmit = { currentPass ->
                        authViewModel.deleteAccount(currentPass)
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )

                // Registration Dialog
                if (tournamentState.showRegistrationDialog && tournamentState.selectedTournament != null) {
                    TournamentRegistrationDialog(
                        tournament = tournamentState.selectedTournament!!,
                        userEmail = currentUser?.email ?: "",
                        initialFfUid = currentUser?.ffUid ?: "",
                        initialWhatsapp = currentUser?.whatsapp ?: "",
                        onDismiss = tournamentViewModel::closeRegistrationDialog,
                        onSubmitRegistration = { ffUid, firstName, lastName, squadName, p1, p2, p3, p4, wa ->
                            tournamentViewModel.submitTournamentRegistration(
                                userEmail = currentUser?.email ?: "",
                                ffUid = ffUid,
                                firstName = firstName,
                                lastName = lastName,
                                squadName = squadName,
                                player1 = p1,
                                player2 = p2,
                                player3 = p3,
                                player4 = p4,
                                whatsapp = wa
                            )
                        }
                    )
                }

                // Insufficient Credit Dialog ("Not enough credit. Recharge now?")
                if (tournamentState.showInsufficientCreditDialog) {
                    InsufficientCreditDialog(
                        requiredAmount = tournamentState.requiredRechargeAmount,
                        currentBalance = tournamentState.currentWalletBalance,
                        onDismiss = tournamentViewModel::dismissInsufficientCreditDialog,
                        onRechargeClick = {
                            tournamentViewModel.dismissInsufficientCreditDialog()
                        }
                    )
                }

                // Registration Ticket Confirmed
                if (tournamentState.registrationSuccessTicket != null) {
                    RegistrationTicketDialog(
                        registration = tournamentState.registrationSuccessTicket!!,
                        onDismiss = tournamentViewModel::dismissRegistrationTicket,
                        onViewPrivateRoom = {
                            val t = allTournaments.find { it.id == tournamentState.registrationSuccessTicket!!.tournamentId }
                            if (t != null) {
                                tournamentViewModel.openPrivateRoomInfo(t)
                                navController.navigate("private_room")
                            }
                            tournamentViewModel.dismissRegistrationTicket()
                        }
                    )
                }
            }

            // WALLET & BKASH RECHARGE
            composable("wallet") {
                WalletScreen(
                    currentUser = currentUser,
                    transactions = userTransactions,
                    txIdInput = walletState.txIdInput,
                    amountInput = walletState.amountInput,
                    statusMessage = walletState.statusMessage,
                    isLoading = walletState.isLoading,
                    onTxIdChange = walletViewModel::onTxIdChange,
                    onAmountChange = walletViewModel::onAmountChange,
                    onSubmitRecharge = {
                        walletViewModel.submitRecharge(currentUser?.email ?: "")
                    },
                    onClearStatus = walletViewModel::clearStatus,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // PRIVATE TOURNAMENT ROOM INFO
            composable("private_room") {
                val selectedTournament = tournamentState.selectedPrivateRoomTournament ?: allTournaments.firstOrNull()
                if (selectedTournament != null) {
                    val tournamentChatMessages by chatViewModel.getTournamentMessages(selectedTournament.id)
                        .collectAsState(initial = emptyList())

                    TournamentPrivateInfoScreen(
                        tournament = selectedTournament,
                        currentUser = currentUser,
                        chatMessages = tournamentChatMessages,
                        chatMessageText = chatState.messageText,
                        onChatMessageChange = chatViewModel::onMessageTextChange,
                        onSendChatMessage = {
                            chatViewModel.sendMessage(
                                senderEmail = currentUser?.email ?: "user@arenax.com",
                                senderName = currentUser?.name ?: "Gamer",
                                isAdmin = false,
                                tournamentId = selectedTournament.id
                            )
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // LIVE SUPPORT CHAT
            composable("live_chat") {
                LiveChatScreen(
                    currentUser = currentUser,
                    messages = globalChatMessages,
                    messageText = chatState.messageText,
                    selectedImageUrl = chatState.selectedImageUrl,
                    editingMessageId = chatState.editingMessageId,
                    limitErrorAlert = chatState.limitErrorAlert,
                    showGalleryPicker = chatState.showGalleryPicker,
                    onMessageTextChange = chatViewModel::onMessageTextChange,
                    onSelectPresetImage = chatViewModel::selectPresetImage,
                    onToggleGalleryPicker = chatViewModel::toggleGalleryPicker,
                    onStartEditingMessage = chatViewModel::startEditingMessage,
                    onCancelEditing = chatViewModel::cancelEditing,
                    onDismissLimitAlert = chatViewModel::dismissLimitAlert,
                    onSendMessage = {
                        chatViewModel.sendMessage(
                            senderEmail = currentUser?.email ?: "unauthenticated@arenax.com",
                            senderName = currentUser?.name ?: "Guest Gamer",
                            isAdmin = false,
                            tournamentId = null
                        )
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // PLAYER PROFILE
            composable("profile") {
                ProfileScreen(
                    currentUser = currentUser,
                    onChangePasswordSubmit = { currentPass, newPass ->
                        authViewModel.changePassword(currentPass, newPass)
                    },
                    onDeleteAccountSubmit = { currentPass ->
                        authViewModel.deleteAccount(currentPass)
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ADMIN CONTROL PANEL
            composable("admin_panel") {
                AdminPanelScreen(
                    pendingTransactions = pendingTxList,
                    allUsers = allUsersList,
                    allTournaments = allTournamentsList,
                    noticeTitle = adminState.noticeTitle,
                    noticeContent = adminState.noticeContent,
                    noticeTargetEmail = adminState.noticeTargetEmail,
                    statusFeedback = adminState.statusFeedback,
                    onNoticeTitleChange = adminViewModel::onNoticeTitleChange,
                    onNoticeContentChange = adminViewModel::onNoticeContentChange,
                    onNoticeTargetChange = adminViewModel::onNoticeTargetChange,
                    onApproveTx = adminViewModel::approveTransaction,
                    onRejectTx = adminViewModel::rejectTransaction,
                    onPublishNotice = adminViewModel::publishNotice,
                    onUpdateRoomDetails = adminViewModel::updateTournamentRoom,
                    onClearFeedback = adminViewModel::clearFeedback,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
