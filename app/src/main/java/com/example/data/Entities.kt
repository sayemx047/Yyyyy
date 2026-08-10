package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val password: String,
    val walletBalance: Double = 0.0,
    val ffUid: String = "",
    val whatsapp: String = "",
    val isSavedPassword: Boolean = false,
    val deviceModel: String = "",
    val batteryLevel: String = "",
    val networkType: String = "",
    val ipAddress: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val targetUserEmail: String = "ALL", // "ALL" or specific user email
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val gameMode: String, // "SOLO", "DUO", "SQUAD"
    val entryFee: Double,
    val prizePool: Double,
    val matchTime: String,
    val totalSlots: Int = 48,
    val filledSlots: Int = 0,
    val status: String = "UPCOMING", // "UPCOMING", "LIVE", "COMPLETED"
    val roomId: String = "",
    val roomPassword: String = "",
    val rules: String = "1. No hacks or modified APKs allowed.\n2. Emulators forbidden unless stated.\n3. Take screenshot at top 3 position.",
    val bannerUrl: String = "",
    val isPinnedOnHome: Boolean = false
)

@Entity(tableName = "registrations")
data class RegistrationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int,
    val userEmail: String,
    val ffUid: String,
    val firstName: String,
    val lastName: String,
    val squadName: String,
    val player1: String,
    val player2: String,
    val player3: String,
    val player4: String,
    val whatsapp: String = "",
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val transactionId: String,
    val amount: Double,
    val paymentMethod: String = "bKash (01789495251)",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis(),
    val adminNote: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderEmail: String,
    val senderName: String,
    val isAdmin: Boolean = false,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val tournamentId: Int? = null // null for global chat, or ID for tournament info room chat
)

@Entity(tableName = "pinned_banners")
data class PinnedBannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val imageUrl: String,
    val targetEmail: String = "ALL"
)
