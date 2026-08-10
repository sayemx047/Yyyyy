package com.example.data

import android.content.Context
import com.example.util.DeviceUtils
import com.example.util.DeviceTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: UserEntity, val message: String = "Login Successful") : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class JoinResult {
    data class Success(val registration: RegistrationEntity) : JoinResult()
    data class InsufficientFunds(val requiredAmount: Double, val currentBalance: Double) : JoinResult()
    data class Error(val message: String) : JoinResult()
}

class AppRepository(private val db: AppDatabase, private val context: Context) {

    val userDao = db.userDao()
    val noticeDao = db.noticeDao()
    val tournamentDao = db.tournamentDao()
    val registrationDao = db.registrationDao()
    val walletDao = db.walletDao()
    val chatDao = db.chatDao()
    val pinnedBannerDao = db.pinnedBannerDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedInitialDataIfNeeded()
            } catch (e: Exception) {
                // Never let a seeding failure take down the whole app —
                // worst case the demo data / bypass account is just missing.
                android.util.Log.e("ARENAX_SEED", "Seeding failed: ${e.message}", e)
            }
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val tournaments = tournamentDao.getAllTournaments().firstOrNull()
        if (tournaments.isNullOrEmpty()) {
            // Seed tournaments
            tournamentDao.insertTournament(
                TournamentEntity(
                    title = "🔥 Free Fire Squad Championship Season 12",
                    gameMode = "SQUAD",
                    entryFee = 50.0,
                    prizePool = 1200.0,
                    matchTime = "Today, 08:30 PM",
                    totalSlots = 48,
                    filledSlots = 28,
                    status = "UPCOMING",
                    roomId = "ROOM-88231",
                    roomPassword = "7788",
                    rules = "1. FF UID strictly required.\n2. Emulator not allowed.\n3. Room ID & Password will unlock 15 mins prior.",
                    bannerUrl = "",
                    isPinnedOnHome = true
                )
            )
            tournamentDao.insertTournament(
                TournamentEntity(
                    title = "🎯 Battle Royale Solo Rush - Free Fire",
                    gameMode = "SOLO",
                    entryFee = 20.0,
                    prizePool = 400.0,
                    matchTime = "Tonight, 10:00 PM",
                    totalSlots = 48,
                    filledSlots = 14,
                    status = "UPCOMING",
                    roomId = "ROOM-55102",
                    roomPassword = "1234",
                    rules = "1. Solo classic Bermuda match.\n2. Winner gets 250 BDT, 2nd gets 100 BDT, 3rd gets 50 BDT.",
                    bannerUrl = ""
                )
            )
            tournamentDao.insertTournament(
                TournamentEntity(
                    title = "⚡ FF Duo Showdown (Bermuda Remastered)",
                    gameMode = "DUO",
                    entryFee = 30.0,
                    prizePool = 600.0,
                    matchTime = "Tomorrow, 07:00 PM",
                    totalSlots = 24,
                    filledSlots = 8,
                    status = "UPCOMING",
                    roomId = "ROOM-99411",
                    roomPassword = "4321",
                    rules = "1. Duo teams required.\n2. Custom room map: Bermuda.",
                    bannerUrl = ""
                )
            )

            // Seed Admin notice
            noticeDao.insertNotice(
                NoticeEntity(
                    title = "📢 ArenaX Tournament Rules & BKash Recharge Notice",
                    content = "Welcome to ArenaX! Send money via bKash to 01789495251 and submit your TxID for instant wallet approval. Good luck gamers!",
                    targetUserEmail = "ALL"
                )
            )

            // Seed Admin welcome chat message
            chatDao.insertMessage(
                ChatMessageEntity(
                    senderEmail = "admin@arenax.com",
                    senderName = "ArenaX Admin Support",
                    isAdmin = true,
                    text = "Welcome to ArenaX Live Support! Feel free to ask questions or request password resets here.",
                    timestamp = System.currentTimeMillis() - 60000
                )
            )

            // Seed pinned banner
            pinnedBannerDao.insertBanner(
                PinnedBannerEntity(
                    title = "⚡ Grand Free Fire Championship - 1200 BDT Prize Pool!",
                    imageUrl = "",
                    targetEmail = "ALL"
                )
            )

            // Seed default test user 'x' with password 'y' for easy testing
            val existingX = userDao.getUserDirect("x@arenax.com")
            if (existingX == null) {
                userDao.insertUser(
                    UserEntity(
                        email = "x@arenax.com",
                        name = "Player X (Bypass Account)",
                        password = "y",
                        walletBalance = 150.0,
                        ffUid = "554921008",
                        whatsapp = "01700000000",
                        isSavedPassword = true,
                        deviceModel = "POCO X3 Pro",
                        batteryLevel = "88%",
                        networkType = "Wi-Fi (High Speed)",
                        ipAddress = "192.168.1.15"
                    )
                )
            }
        }
    }

    // --- AUTHENTICATION ---
    suspend fun login(emailInput: String, passwordInput: String): AuthResult {
        return withContext(Dispatchers.IO) {
            val formattedEmail = emailInput.trim().lowercase()
            
            // Bypass logic for user x / pass y
            if ((emailInput.trim() == "x" || formattedEmail == "x@arenax.com") && passwordInput == "y") {
                val bypassEmail = "x@arenax.com"
                var user = userDao.getUserDirect(bypassEmail)
                if (user == null) {
                    user = UserEntity(
                        email = bypassEmail,
                        name = "Player X",
                        password = "y",
                        walletBalance = 150.0,
                        ffUid = "554921008",
                        whatsapp = "01700000000",
                        isSavedPassword = true
                    )
                    userDao.insertUser(user)
                }
                // Update telemetry
                val telemetry = try {
                    DeviceUtils.getDeviceTelemetry(context)
                } catch (e: Exception) {
                    DeviceTelemetry()
                }
                val updatedUser = user.copy(
                    deviceModel = telemetry.model,
                    batteryLevel = telemetry.batteryLevel,
                    networkType = telemetry.networkType,
                    ipAddress = telemetry.ipAddress
                )
                userDao.updateUser(updatedUser)
                return@withContext AuthResult.Success(updatedUser, "Welcome back! Logged in via Bypass Account.")
            }

            // Standard check
            val user = userDao.getUserDirect(formattedEmail)
            if (user == null) {
                return@withContext AuthResult.Error("Account not found for email: $emailInput")
            }
            if (user.password != passwordInput) {
                return@withContext AuthResult.Error("Incorrect password. Please try again.")
            }

            // Update telemetry
            val telemetry = try {
                DeviceUtils.getDeviceTelemetry(context)
            } catch (e: Exception) {
                DeviceTelemetry()
            }
            val updatedUser = user.copy(
                deviceModel = telemetry.model,
                batteryLevel = telemetry.batteryLevel,
                networkType = telemetry.networkType,
                ipAddress = telemetry.ipAddress
            )
            userDao.updateUser(updatedUser)

            AuthResult.Success(updatedUser, "Login Successful!")
        }
    }

    suspend fun registerUser(
        name: String,
        emailInput: String,
        passwordInput: String,
        ffUid: String,
        whatsapp: String
    ): AuthResult {
        return withContext(Dispatchers.IO) {
            val formattedEmail = emailInput.trim().lowercase()
            if (formattedEmail.isEmpty() || passwordInput.isEmpty()) {
                return@withContext AuthResult.Error("Email and Password cannot be empty.")
            }

            val existing = userDao.getUserDirect(formattedEmail)
            if (existing != null) {
                return@withContext AuthResult.Error("An account already exists with this email address. (1 account per email policy)")
            }

            val telemetry = try {
                DeviceUtils.getDeviceTelemetry(context)
            } catch (e: Exception) {
                DeviceTelemetry()
            }
            val newUser = UserEntity(
                email = formattedEmail,
                name = name.ifBlank { "Gamer" },
                password = passwordInput,
                walletBalance = 0.0,
                ffUid = ffUid,
                whatsapp = whatsapp,
                isSavedPassword = true,
                deviceModel = telemetry.model,
                batteryLevel = telemetry.batteryLevel,
                networkType = telemetry.networkType,
                ipAddress = telemetry.ipAddress
            )
            userDao.insertUser(newUser)
            AuthResult.Success(newUser, "Account created successfully!")
        }
    }

    suspend fun changePassword(email: String, currentPass: String, newPass: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserDirect(email) ?: return@withContext false
            if (user.password != currentPass) return@withContext false
            val updated = user.copy(password = newPass)
            userDao.updateUser(updated)
            true
        }
    }

    suspend fun deleteAccount(email: String, currentPass: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserDirect(email) ?: return@withContext false
            if (user.password != currentPass) return@withContext false
            userDao.deleteUser(user)
            true
        }
    }

    suspend fun updateSavePasswordPreference(email: String, isSaved: Boolean) {
        withContext(Dispatchers.IO) {
            val user = userDao.getUserDirect(email) ?: return@withContext
            userDao.updateUser(user.copy(isSavedPassword = isSaved))
        }
    }

    suspend fun updateDeviceTelemetry(email: String) {
        withContext(Dispatchers.IO) {
            val user = userDao.getUserDirect(email) ?: return@withContext
            val telemetry = try {
                DeviceUtils.getDeviceTelemetry(context)
            } catch (e: Exception) {
                DeviceTelemetry()
            }
            userDao.updateUser(
                user.copy(
                    deviceModel = telemetry.model,
                    batteryLevel = telemetry.batteryLevel,
                    networkType = telemetry.networkType,
                    ipAddress = telemetry.ipAddress
                )
            )
        }
    }

    // --- WALLET ---
    suspend fun submitRechargeRequest(email: String, txId: String, amount: Double): String {
        return withContext(Dispatchers.IO) {
            if (txId.trim().isEmpty() || amount <= 0) {
                return@withContext "Invalid Transaction ID or Amount."
            }
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    userEmail = email,
                    transactionId = txId.trim().uppercase(),
                    amount = amount,
                    paymentMethod = "bKash (01789495251)",
                    status = "PENDING",
                    timestamp = System.currentTimeMillis()
                )
            )
            "Recharge request submitted! Admin will verify TxID $txId shortly."
        }
    }

    suspend fun approveWalletTransaction(txId: Int, adminNote: String = "Approved by Admin") {
        withContext(Dispatchers.IO) {
            val tx = walletDao.getTransactionById(txId) ?: return@withContext
            if (tx.status != "PENDING") return@withContext

            val updatedTx = tx.copy(status = "APPROVED", adminNote = adminNote)
            walletDao.updateTransaction(updatedTx)

            val user = userDao.getUserDirect(tx.userEmail)
            if (user != null) {
                val newBalance = user.walletBalance + tx.amount
                userDao.updateUser(user.copy(walletBalance = newBalance))
            }
        }
    }

    suspend fun rejectWalletTransaction(txId: Int, adminNote: String = "Rejected by Admin") {
        withContext(Dispatchers.IO) {
            val tx = walletDao.getTransactionById(txId) ?: return@withContext
            val updatedTx = tx.copy(status = "REJECTED", adminNote = adminNote)
            walletDao.updateTransaction(updatedTx)
        }
    }

    // --- TOURNAMENTS & REGISTRATION ---
    suspend fun joinTournament(
        tournamentId: Int,
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
    ): JoinResult {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserDirect(userEmail)
                ?: return@withContext JoinResult.Error("User not found.")
            val tournament = tournamentDao.getTournamentDirect(tournamentId)
                ?: return@withContext JoinResult.Error("Tournament not found.")

            // Check if already registered
            val existingReg = registrationDao.getRegistrationDirect(userEmail, tournamentId)
            if (existingReg != null) {
                return@withContext JoinResult.Success(existingReg)
            }

            // Check wallet balance
            if (user.walletBalance < tournament.entryFee) {
                return@withContext JoinResult.InsufficientFunds(
                    requiredAmount = tournament.entryFee,
                    currentBalance = user.walletBalance
                )
            }

            // Deduct entry fee
            val updatedBalance = user.walletBalance - tournament.entryFee
            userDao.updateUser(user.copy(walletBalance = updatedBalance))

            // Create registration
            val registration = RegistrationEntity(
                tournamentId = tournamentId,
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
            registrationDao.insertRegistration(registration)

            // Update slots
            val updatedTournament = tournament.copy(filledSlots = tournament.filledSlots + 1)
            tournamentDao.updateTournament(updatedTournament)

            JoinResult.Success(registration)
        }
    }

    // --- LIVE CHAT ---
    suspend fun sendChatMessage(
        senderEmail: String,
        senderName: String,
        isAdmin: Boolean,
        text: String,
        imageUrl: String? = null,
        tournamentId: Int? = null
    ): String? {
        return withContext(Dispatchers.IO) {
            // Non-admin 3 consecutive message limit check
            if (!isAdmin) {
                val messagesFlow = if (tournamentId == null) {
                    chatDao.getGlobalChatMessages()
                } else {
                    chatDao.getTournamentChatMessages(tournamentId)
                }
                val messages = messagesFlow.firstOrNull() ?: emptyList()

                var userCountAfterAdmin = 0
                for (m in messages.reversed()) {
                    if (m.isAdmin) break
                    if (m.senderEmail.equals(senderEmail, ignoreCase = true)) {
                        userCountAfterAdmin++
                    }
                }

                if (userCountAfterAdmin >= 3) {
                    return@withContext "Limit reached (3 messages). Please wait for Admin reply before sending more!"
                }
            }

            chatDao.insertMessage(
                ChatMessageEntity(
                    senderEmail = senderEmail,
                    senderName = senderName,
                    isAdmin = isAdmin,
                    text = text,
                    imageUrl = imageUrl,
                    timestamp = System.currentTimeMillis(),
                    tournamentId = tournamentId
                )
            )

            // Auto-purge old messages older than 48 hours
            val cutoff = System.currentTimeMillis() - (48 * 60 * 60 * 1000L)
            chatDao.purgeOldMessages(cutoff)

            null
        }
    }

    suspend fun editChatMessage(messageId: Int, newText: String): Boolean {
        return withContext(Dispatchers.IO) {
            val msg = chatDao.getMessageById(messageId) ?: return@withContext false
            val updated = msg.copy(text = newText, isEdited = true)
            chatDao.updateMessage(updated)
            true
        }
    }

    suspend fun requestPasswordResetViaChat(email: String, name: String) {
        withContext(Dispatchers.IO) {
            sendChatMessage(
                senderEmail = email,
                senderName = name.ifBlank { "User" },
                isAdmin = false,
                text = "🆘 PASSWORD RESET REQUEST: Please verify my account ($email) and reset my password.",
                tournamentId = null
            )
        }
    }
}
