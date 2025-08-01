package com.example.habitspark.data.repository

import com.example.habitspark.data.models.AchievementModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AchievementRepository(db: FirebaseFirestore) {
    private val achievementCollection = db.collection("achievements")

    suspend fun fetchAchievements(): List<AchievementModel> {
        return achievementCollection
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(AchievementModel::class.java)?.copy(id = doc.id)
            }
    }
}