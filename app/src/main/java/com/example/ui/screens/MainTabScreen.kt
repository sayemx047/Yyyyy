package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun MainTabScreen(
    currentUser: UserEntity?,
    tournaments: List<TournamentEntity>,
    registrations: List<RegistrationEntity>,
    pinnedBanners: List<PinnedBannerEntity>,
    notices: List<NoticeEntity>,
    filterMode: String,
    onFilterChange: (String) -> Unit,
    onJoinClick: (TournamentEntity) -> Unit,
    onOpenPrivateRoomInfo: (TournamentEntity) -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    // Wallet props
    transactions: List<WalletTransactionEntity>,
    txIdInput: String,
    amountInput: String,
    walletStatusMessage: String?,
    isWalletLoading: Boolean,
    onTxIdChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onSubmitRecharge: () -> Unit,
    onClearWalletStatus: () -> Unit,
    // Chat props
    globalChatMessages: List<ChatMessageEntity>,
    chatMessageText: String,
    selectedImageUrl: String?,
    editingMessageId: Int?,
    limitErrorAlert: String?,
    showGalleryPicker: Boolean,
    onChatMessageTextChange: (String) -> Unit,
    onSelectPresetImage: (String?) -> Unit,
    onToggleGalleryPicker: (Boolean) -> Unit,
    onStartEditingMessage: (ChatMessageEntity) -> Unit,
    onCancelEditing: () -> Unit,
    onDismissLimitAlert: () -> Unit,
    onSendChatMessage: () -> Unit,
    // Profile props
    onChangePasswordSubmit: (currentPass: String, newPass: String) -> Unit,
    onDeleteAccountSubmit: (currentPass: String) -> Unit,
    onLogoutClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tournaments, 1: Wallet, 2: Support, 3: Profile

    FrostedGlassBackground {
        Scaffold(
            bottomBar = {
                Surface(
                    color = Color(0x181E1B2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2BFFFFFF)),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(68.dp)
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = "Tournaments",
                                    tint = if (selectedTab == 0) PurplePrimary else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    "Tournaments",
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) PurplePrimary else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0x2B6366F1)
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = if (selectedTab == 1) BkashPink else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    "Wallet",
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) BkashPink else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0x2BE91E63)
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Support",
                                    tint = if (selectedTab == 2) GoldAccent else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    "Support",
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 2) GoldAccent else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0x2BFBBF24)
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = if (selectedTab == 3) CyanPrimary else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    "Profile",
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 3) CyanPrimary else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0x2B06B6D4)
                            )
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        currentUser = currentUser,
                        tournaments = tournaments,
                        registrations = registrations,
                        pinnedBanners = pinnedBanners,
                        notices = notices,
                        filterMode = filterMode,
                        onFilterChange = onFilterChange,
                        onJoinClick = onJoinClick,
                        onOpenPrivateRoomInfo = onOpenPrivateRoomInfo,
                        onNavigateToWallet = { selectedTab = 1 },
                        onNavigateToProfile = { selectedTab = 3 },
                        onNavigateToAdminPanel = onNavigateToAdminPanel,
                        onOpenLiveChat = { selectedTab = 2 }
                    )
                    1 -> WalletScreen(
                        currentUser = currentUser,
                        transactions = transactions,
                        txIdInput = txIdInput,
                        amountInput = amountInput,
                        statusMessage = walletStatusMessage,
                        isLoading = isWalletLoading,
                        onTxIdChange = onTxIdChange,
                        onAmountChange = onAmountChange,
                        onSubmitRecharge = onSubmitRecharge,
                        onClearStatus = onClearWalletStatus,
                        onNavigateBack = { selectedTab = 0 }
                    )
                    2 -> LiveChatScreen(
                        currentUser = currentUser,
                        messages = globalChatMessages,
                        messageText = chatMessageText,
                        selectedImageUrl = selectedImageUrl,
                        editingMessageId = editingMessageId,
                        limitErrorAlert = limitErrorAlert,
                        showGalleryPicker = showGalleryPicker,
                        onMessageTextChange = onChatMessageTextChange,
                        onSelectPresetImage = onSelectPresetImage,
                        onToggleGalleryPicker = onToggleGalleryPicker,
                        onStartEditingMessage = onStartEditingMessage,
                        onCancelEditing = onCancelEditing,
                        onDismissLimitAlert = onDismissLimitAlert,
                        onSendMessage = onSendChatMessage,
                        onNavigateBack = { selectedTab = 0 }
                    )
                    3 -> ProfileScreen(
                        currentUser = currentUser,
                        onChangePasswordSubmit = onChangePasswordSubmit,
                        onDeleteAccountSubmit = onDeleteAccountSubmit,
                        onLogoutClick = onLogoutClick,
                        onNavigateBack = { selectedTab = 0 }
                    )
                }
            }
        }
    }
}
