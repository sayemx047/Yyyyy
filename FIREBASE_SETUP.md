# Firebase Setup — ArenaX

This project currently runs entirely on a **local Room database** (see
`app/src/main/java/com/example/data/AppDatabase.kt`), so it works fully
offline with zero setup. Firebase is kept completely isolated so it can be
switched on later without risking the current working build.

## Where everything Firebase-related lives

| File | Purpose |
|---|---|
| `data/FirebaseConfig.kt` | The single `IS_FIREBASE_ENABLED` switch + Firestore collection name constants |
| `data/firebase/FirebaseSyncRepository.kt` | Empty scaffold — implement Firestore calls here, one entity at a time |
| `ArenaXApplication.kt` | The one place `FirebaseApp.initializeApp()` gets called (only if `IS_FIREBASE_ENABLED = true`) |
| `app/build.gradle.kts` | Firebase dependencies (Firestore, Auth) are listed but commented out until you're ready |

Nothing else in the app references Firebase directly — `AppRepository.kt` and
all the ViewModels only talk to Room. This means you can safely wire Firebase
in gradually, one function at a time, without breaking anything that already
works.

## 1. Create the Firebase project

1. [Firebase Console](https://console.firebase.google.com/) → **Add project**
2. Add an Android app:
   - Package name: `com.aistudio.arenax.esports` (this is the `applicationId`
     in `app/build.gradle.kts` — **not** `com.example`, which is just the
     Kotlin package namespace)
3. Download **`google-services.json`** → place it at `app/google-services.json`

## 2. Enable products

| Product | Used for |
|---|---|
| **Firestore Database** | users, notices, tournaments, registrations, wallet_transactions, chat_messages, pinned_banners |
| **Authentication** (Email/Password) | optional — you can also keep auth in Room and only sync data via Firestore |

## 3. Enable the dependencies

In `app/build.gradle.kts`, uncomment:

```kotlin
implementation(libs.firebase.firestore)
// implementation(libs.firebase.auth)
// implementation(libs.androidx.credentials)
// implementation(libs.androidx.credentials.play.services)
// implementation(libs.googleid)
```

(`firebase-bom`, `firebase-ai`, and `firebase-appcheck-recaptcha` are already
included — the BOM keeps all Firebase library versions in sync automatically.)

## 4. Implement `FirebaseSyncRepository.kt`

Each method is a `TODO()` stub mapped 1:1 to a Room DAO function. Example —
wiring up notices:

```kotlin
suspend fun getNotices(): List<NoticeEntity> {
    val snapshot = Firebase.firestore.collection(FirebaseConfig.NOTICES_COLLECTION)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .get().await()
    return snapshot.toObjects(NoticeEntity::class.java)
}
```

Then in `AppRepository.kt`, swap the matching Room call:

```kotlin
// Before:
val tournaments = tournamentDao.getAllTournaments().firstOrNull()
// After:
val tournaments = if (FirebaseConfig.IS_FIREBASE_ENABLED)
    firebaseSyncRepository.getTournaments()
else
    tournamentDao.getAllTournaments().firstOrNull()
```

Do this one feature at a time (start with `notices` or `tournaments` — they're
read-only and low-risk) and test after each swap.

## 5. Turn it on

```kotlin
// data/FirebaseConfig.kt
var IS_FIREBASE_ENABLED: Boolean = true
```

`FirebaseConfig.checkFirebaseStatus()` (already used in `ProfileScreen.kt`)
will then correctly report "🔥 Firebase is ACTIVE and Connected!" once
`google-services.json` is in place.

## Recommended Firestore security rules (starting point)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{email} {
      allow read, write: if request.auth != null && request.auth.token.email == email;
    }
    match /tournaments/{id} { allow read: if true; allow write: if false; }
    match /notices/{id} { allow read: if true; allow write: if false; }
    match /wallet_transactions/{id} {
      allow create: if request.auth != null;
      allow read: if request.auth != null && request.auth.token.email == resource.data.userEmail;
      allow update: if false; // admin-only, via Cloud Function
    }
    match /registrations/{id} {
      allow create, read: if request.auth != null;
    }
    match /chat_messages/{id} {
      allow read, create: if request.auth != null;
      allow update: if request.auth.token.email == resource.data.senderEmail; // edit, no delete
    }
  }
}
```

Tighten before production — this is a starting point, not a final ruleset.
