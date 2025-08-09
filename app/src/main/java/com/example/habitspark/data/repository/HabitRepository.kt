package com.example.habitspark.data.repository

import com.example.habitspark.data.models.HabitModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class  HabitRepository(db: FirebaseFirestore) {

    private val habitsCollection = db.collection("habits")

    fun addHabit(habit: HabitModel): Task<DocumentReference> {
        return habitsCollection.add(habit)
    }

    fun updateHabit(habit: HabitModel): Task<Void> {
        return habitsCollection.document(habit.id).set(habit)
    }
    fun updateHabitFields(habitId: String, fields: Map<String, Any>): Task<Void> {
        return habitsCollection.document(habitId).update(fields)
    }

    fun deleteHabit(habitId: String): Task<Void> {
        return habitsCollection.document(habitId).delete()
    }

    fun getHabitById(habitId: String): Task<HabitModel?> {
        return habitsCollection.document(habitId)
            .get()
            .continueWith { task ->
                //already returns the habitModel object instead of the calling code having to do it
                task.result?.toObject<HabitModel>()?.copy(id = task.result.id)
            }
    }
    fun listenHabitById(habitId: String): Flow<HabitModel?> = callbackFlow {
        val reg = habitsCollection.document(habitId)
            .addSnapshotListener() { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                trySend(snap?.toObject(HabitModel::class.java)?.copy(id = snap.id)).isSuccess
            }
        awaitClose { reg.remove() }
    }

    fun getUserHabits(userId: String): Task<List<HabitModel>> {
        return habitsCollection.whereEqualTo("userId", userId)
            .get()
            .continueWith { task ->
                task.result?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitModel::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
    }

    fun listenUserHabitsById(userId: String): Flow<List<HabitModel>> = callbackFlow {
        val reg = habitsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(HabitModel::class.java)?.copy(id = doc.id)
                    }
                    .orEmpty()

                trySend(list).isSuccess
            }

        awaitClose { reg.remove() }
    }
}