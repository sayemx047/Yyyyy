package com.example

import android.app.Application
import android.util.Log
import com.example.data.FirebaseConfig

/**
 * ArenaX Application class.
 *
 * Two jobs, kept deliberately isolated from the rest of the app:
 *
 * 1. A global uncaught-exception logger. If the app ever crashes, the FULL
 *    stack trace is written to Logcat under the tag "ARENAX_FATAL" before the
 *    app closes — open CodeAssist's Logcat panel right after a crash and
 *    search for that tag to see exactly which line caused it.
 *
 * 2. The ONE place Firebase gets initialized, once you're ready to switch on
 *    from the local Room database to Firebase. See FIREBASE_SETUP.md at the
 *    project root for the full walkthrough.
 */
class ArenaXApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // --- Global crash logger -------------------------------------------------
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(
                "ARENAX_FATAL",
                "App crashed on thread '${thread.name}': ${throwable.message}",
                throwable
            )
            // Still let the system handle it normally (shows the "app stopped" dialog),
            // we just make sure the real reason is visible in Logcat first.
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // --- Firebase (isolated) ---------------------------------------------------
        // Only runs once you've added app/google-services.json AND flipped
        // FirebaseConfig.IS_FIREBASE_ENABLED = true (see FIREBASE_SETUP.md).
        if (FirebaseConfig.IS_FIREBASE_ENABLED) {
            try {
                com.google.firebase.FirebaseApp.initializeApp(this)
            } catch (e: Exception) {
                Log.e("ARENAX_FIREBASE", "Firebase init failed: ${e.message}", e)
            }
        }
    }
}
