package com.example.habitspark.data.repository

import com.example.habitspark.data.dataTypes.HighlightPurchaseResult
import com.example.habitspark.data.dataTypes.HighlightStyle
import com.example.habitspark.data.models.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import kotlin.math.max


class UserRepository(db: FirebaseFirestore) {

    private val usersCollection = db.collection("users")

    fun addUser(user: UserModel): Task<DocumentReference> {
        return usersCollection.add(user)
    }

    fun updateUser(user: UserModel): Task<Void> {
        return usersCollection.document(user.id).set(user)
    }

    fun updateUserMetrics(userId: String, metrics: Map<String, Any>): Task<Void> {
        return usersCollection.document(userId).update(metrics)
    }

    fun getUserById(userId: String): Task<UserModel?> {
        return usersCollection.document(userId)
            .get()
            .continueWith { task ->
                //already returns the habitModel object instead of the calling code having to do it
                task.result?.toObject<UserModel>()?.copy(id = task.result.id)
            }
    }

    fun listenUser(userId: String): Flow<UserModel?> = callbackFlow {
        val reg =usersCollection.document(userId)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(snap?.toObject(UserModel::class.java)?.copy(id = snap.id))
            }
        awaitClose { reg.remove() }
    }

    // used for leader board by total minutes spent
    fun listenAllTimeLeaderboard(limit: Long = 10): Flow<List<UserModel>> =
        callbackFlow {
            val reg = usersCollection
                .orderBy("metrics.totalMinutesSpent", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snap, e ->
                    if (e != null) { close(e); return@addSnapshotListener }
                    val users = snap?.documents.orEmpty()
                        .mapNotNull { it.toObject(UserModel::class.java)?.copy(id = it.id) }
                    trySend(users)
                }
            awaitClose { reg.remove() }
    }


    fun purchaseHighlight(
        userId: String,
        style: HighlightStyle,
    ): Task<HighlightPurchaseResult> {

        val userRef = usersCollection.document(userId)

        return usersCollection.firestore.runTransaction { tx ->
            val snap = tx.get(userRef)
            val currentCoins = (snap.getLong("coin") ?: 0L).toInt()
            if (currentCoins < style.cost) {
                throw IllegalStateException("Not enough coins.")
            }

            val nowMs = System.currentTimeMillis()
            val currentExpiryMs = snap.getTimestamp("highlightExpiresAt")?.toDate()?.time ?: 0L

            // Extend if still active; otherwise start now
            val baseStart = max(nowMs, currentExpiryMs)
            val newExpiry = Timestamp(Date(baseStart + style.expiresInDays * 24L * 60L * 60L * 1000L))

            val newCoins = currentCoins - style.cost
            val patch = mapOf(
                "coin" to newCoins,
                "highlightStyle" to style.name,
                "highlightExpiresAt" to newExpiry
            )
            tx.update(userRef, patch)

            HighlightPurchaseResult(
                newCoins = newCoins,
                styleName = style.name,
                expiresAt = newExpiry
            )
        }
    }
}