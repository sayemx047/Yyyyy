package com.example.data.firebase

import com.example.data.ChatMessageEntity
import com.example.data.NoticeEntity
import com.example.data.RegistrationEntity
import com.example.data.TournamentEntity
import com.example.data.UserEntity
import com.example.data.WalletTransactionEntity

/**
 * FIREBASE SYNC LAYER — isolated on purpose.
 *
 * This is the ONE place to add Firestore code when you're ready to move off
 * Room and onto Firebase. Nothing here is wired into the app yet, so it can't
 * break the current (working) Room-based build.
 *
 * How to activate:
 *   1. Add app/google-services.json (see FIREBASE_SETUP.md at project root)
 *   2. Uncomment the Firestore/Auth dependencies already listed (commented
 *      out) in app/build.gradle.kts
 *   3. Implement the methods below using FirebaseFirestore / FirebaseAuth
 *   4. In AppRepository.kt, swap the relevant Room DAO calls for calls into
 *      this class — one function at a time, so you can test as you go.
 *   5. Flip FirebaseConfig.IS_FIREBASE_ENABLED = true
 *
 * Collections (mirrors the Room table names 1:1, so migration is mechanical):
 *   users, notices, tournaments, registrations, wallet_transactions,
 *   chat_messages, pinned_banners
 */
class FirebaseSyncRepository {

    // --- Users -----------------------------------------------------------
    suspend fun getUser(email: String): UserEntity? {
        TODO("Firestore: users/{email}.get()")
    }

    suspend fun upsertUser(user: UserEntity) {
        TODO("Firestore: users/{user.email}.set(user)")
    }

    // --- Notices -----------------------------------------------------------
    suspend fun getNotices(): List<NoticeEntity> {
        TODO("Firestore: notices collection, ordered by timestamp desc")
    }

    // --- Tournaments -----------------------------------------------------------
    suspend fun getTournaments(): List<TournamentEntity> {
        TODO("Firestore: tournaments collection")
    }

    // --- Registrations -----------------------------------------------------------
    suspend fun getRegistrations(userEmail: String): List<RegistrationEntity> {
        TODO("Firestore: registrations where userEmail == userEmail")
    }

    suspend fun addRegistration(registration: RegistrationEntity) {
        TODO("Firestore: registrations.add(registration)")
    }

    // --- Wallet -----------------------------------------------------------
    suspend fun submitTransaction(tx: WalletTransactionEntity) {
        TODO("Firestore: wallet_transactions.add(tx), status = PENDING")
    }

    // --- Chat -----------------------------------------------------------
    suspend fun getChatMessages(tournamentId: Int?): List<ChatMessageEntity> {
        TODO("Firestore: chat_messages where tournamentId == tournamentId, ordered by timestamp")
    }

    suspend fun sendChatMessage(message: ChatMessageEntity) {
        TODO("Firestore: chat_messages.add(message)")
    }
}
