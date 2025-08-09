package com.example.habitspark.data.repository

import com.example.habitspark.data.models.EntryModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EntryRepository(db: FirebaseFirestore) {

    private val entriesCollection = db.collection("entries")

    fun addEntry(entry: EntryModel): Task<DocumentReference> {
        return entriesCollection.add(entry)
    }

    fun updateEntry(entry: EntryModel): Task<Void> {
        return entriesCollection.document(entry.id).set(entry)
    }

    fun deleteEntry(entryId: String): Task<Void> {
        return entriesCollection.document(entryId).delete()
    }

    suspend fun getEntriesForHabit(habitId: String): List<EntryModel> {
        return entriesCollection
            .whereEqualTo("habitId", habitId)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(EntryModel::class.java)?.copy(id = doc.id)
            }
    }

    fun listenEntriesForHabit(habitId: String): Flow<List<EntryModel>> = callbackFlow {
        val reg =entriesCollection
            .whereEqualTo("habitId", habitId)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject(EntryModel::class.java)?.copy(id = it.id) }.orEmpty()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }


    fun getEntriesByUserId(userId: String): Task<List<EntryModel>> {
        return entriesCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .get()
            .continueWith { task ->
                task.result?.documents?.mapNotNull { doc ->
                    doc.toObject(EntryModel::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
    }

    fun deleteEntriesByHabitId(habitId: String): Task<Void> {
        return entriesCollection
            .whereEqualTo("habitId", habitId)
            .get()
            .continueWithTask { task ->
                val batch = entriesCollection.firestore.batch()
                task.result?.documents?.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
    }
}