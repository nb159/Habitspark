package com.example.habitspark.data.repository

import com.example.habitspark.data.models.EntryModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject

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

    fun getEntry(entryId: String): Task<EntryModel?> {
        return entriesCollection.document(entryId)
            .get()
            .continueWith { task ->
                //already returns the entryModel object instead of the calling code having to do it
                task.result?.toObject<EntryModel>()?.copy(id = task.result.id)
            }
    }

    fun getEntriesForHabit(habitId: String): Task<List<EntryModel>> {
        return entriesCollection
            .whereEqualTo("habitId", habitId)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .get()
            .continueWith { task ->
                task.result?.documents?.mapNotNull { doc ->
                    doc.toObject(EntryModel::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
    }


    fun getEntriesForUser(userId: String): Task<List<EntryModel>> {
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
}