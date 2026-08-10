package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserDirect(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices WHERE targetUserEmail = 'ALL' OR targetUserEmail = :email ORDER BY timestamp DESC")
    fun getNoticesForUser(email: String): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices ORDER BY timestamp DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Int)
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY id DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    fun getTournamentById(id: Int): Flow<TournamentEntity?>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getTournamentDirect(id: Int): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournament(id: Int)
}

@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registrations WHERE userEmail = :email ORDER BY registeredAt DESC")
    fun getRegistrationsForUser(email: String): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE userEmail = :email AND tournamentId = :tournamentId LIMIT 1")
    fun getRegistration(email: String, tournamentId: Int): Flow<RegistrationEntity?>

    @Query("SELECT * FROM registrations WHERE userEmail = :email AND tournamentId = :tournamentId LIMIT 1")
    suspend fun getRegistrationDirect(email: String, tournamentId: Int): RegistrationEntity?

    @Query("SELECT * FROM registrations WHERE tournamentId = :tournamentId ORDER BY registeredAt ASC")
    fun getRegistrationsForTournament(tournamentId: Int): Flow<List<RegistrationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistration(registration: RegistrationEntity)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getTransactionsForUser(email: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): WalletTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransactionEntity)

    @Update
    suspend fun updateTransaction(tx: WalletTransactionEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE tournamentId IS NULL ORDER BY timestamp ASC")
    fun getGlobalChatMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE tournamentId = :tournamentId ORDER BY timestamp ASC")
    fun getTournamentChatMessages(tournamentId: Int): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Int): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessageEntity)

    @Update
    suspend fun updateMessage(msg: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE timestamp < :cutoffTimestamp")
    suspend fun purgeOldMessages(cutoffTimestamp: Long)
}

@Dao
interface PinnedBannerDao {
    @Query("SELECT * FROM pinned_banners ORDER BY id DESC")
    fun getPinnedBanners(): Flow<List<PinnedBannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: PinnedBannerEntity)

    @Query("DELETE FROM pinned_banners WHERE id = :id")
    suspend fun deleteBanner(id: Int)
}
