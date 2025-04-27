package com.example.habitspark.data.repository

import com.example.habitspark.data.models.HabitModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

object HabitRepository {

    private val db = Firebase.firestore
    private val habitsCollection = db.collection("habits")

    fun addHabit(habit: HabitModel): Task<DocumentReference> {
        return habitsCollection.add(habit)
    }

    fun updateHabit(habitId: String, habit: HabitModel): Task<Void> {
        return habitsCollection.document(habitId).set(habit)
    }

    fun getHabit(habitId: String): Task<DocumentSnapshot> {
        return habitsCollection.document(habitId).get()
    }

    fun getUserHabits(userId: String): Task<QuerySnapshot> {
        return habitsCollection.whereEqualTo("userId", userId).get()
    }
}