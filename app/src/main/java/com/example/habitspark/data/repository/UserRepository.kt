package com.example.habitspark.data.repository

import com.example.habitspark.data.models.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore


object UserRepository {

    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    fun addUser(user: UserModel): Task<DocumentReference> {
        return usersCollection.add(user)
    }

    fun updateUser(userId: String, user: User): Task<Void> {
        return usersCollection.document(userId).set(user)
    }

    fun getUser(userId: String): Task<DocumentSnapshot> {
        return usersCollection.document(userId).get()
    }
}