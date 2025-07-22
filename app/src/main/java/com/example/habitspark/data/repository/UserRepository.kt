package com.example.habitspark.data.repository

import com.example.habitspark.data.models.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject


class UserRepository(db: FirebaseFirestore) {

    private val usersCollection = db.collection("users")

    fun addUser(user: UserModel): Task<DocumentReference> {
        return usersCollection.add(user)
    }

    fun updateUser(user: UserModel): Task<Void> {
        return usersCollection.document(user.id).set(user)
    }

    fun getUserById(userId: String): Task<UserModel?> {
        return usersCollection.document(userId)
            .get()
            .continueWith { task ->
                //already returns the habitModel object instead of the calling code having to do it
                task.result?.toObject<UserModel>()?.copy(id = task.result.id)
            }
    }
}