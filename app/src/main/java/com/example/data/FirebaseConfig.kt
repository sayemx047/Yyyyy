package com.example.data

import android.content.Context
import android.util.Log

/**
 * Firebase Integration & Configuration Hub
 * 
 * To switch control to real Firebase:
 * 1. Add your `google-services.json` file inside `/app/`
 * 2. Set [IS_FIREBASE_ENABLED] to `true` or let auto-detection check Google App ID.
 * 3. User accounts, tournaments, notices, and chat sync seamlessly!
 */
object FirebaseConfig {
    
    // Set to true when google-services.json is added
    var IS_FIREBASE_ENABLED: Boolean = false
    
    // Optional custom Firebase Firestore Collection Names
    const val USERS_COLLECTION = "users"
    const val TOURNAMENTS_COLLECTION = "tournaments"
    const val REGISTRATIONS_COLLECTION = "registrations"
    const val TRANSACTIONS_COLLECTION = "wallet_transactions"
    const val CHAT_COLLECTION = "chat_messages"
    const val NOTICES_COLLECTION = "notices"

    fun checkFirebaseStatus(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
            if (resId != 0) {
                IS_FIREBASE_ENABLED = true
                "🔥 Firebase is ACTIVE and Connected!"
            } else {
                "⚡ Offline / Room Engine Mode (Ready for Firebase Integration)"
            }
        } catch (e: Exception) {
            "⚡ Offline / Room Engine Mode"
        }
    }
}
