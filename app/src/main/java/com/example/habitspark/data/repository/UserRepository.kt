package com.example.habitspark.data.repository

import com.example.habitspark.data.models.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


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
}