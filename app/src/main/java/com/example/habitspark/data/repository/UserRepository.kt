package com.example.habitspark.data.repository

import com.example.habitspark.data.models.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class UserRepository(db: FirebaseFirestore) {

    private val usersCollection = db.collection("users")

    fun addUser(user: UserModel): Task<DocumentReference> {
        return usersCollection.add(user)
    }

    fun updateUser(userId: String, user: UserModel): Task<Void> {
        return usersCollection.document(userId).set(user)
    }

    fun getUser(userId: String): Task<DocumentSnapshot> {
        return usersCollection.document(userId).get()
    }
}