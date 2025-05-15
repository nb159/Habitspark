package com.example.habitspark.data.repository

import com.example.habitspark.data.models.HabitModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

object HabitRepository {

    private val db = Firebase.firestore
    private val habitsCollection = db.collection("habits")

    fun addHabit(habit: HabitModel): Task<DocumentReference> {
        return habitsCollection.add(habit)
    }

    fun updateHabit(habit: HabitModel): Task<Void> {
        return habitsCollection.document(habit.id).set(habit)
    }

    fun deleteHabit(habitId: String): Task<Void> {
        return habitsCollection.document(habitId).delete()
    }

    fun getHabit(habitId: String): Task<HabitModel?> {
        return habitsCollection.document(habitId)
            .get()
            .continueWith { task ->
                //already returns the habitModel object instead of the calling code having to do it
                task.result?.toObject<HabitModel>()?.copy(id = task.result.id)
            }
    }

    fun getUserHabits(userId: String): Task<QuerySnapshot> {
        return habitsCollection.whereEqualTo("userId", userId).get()
    }
}