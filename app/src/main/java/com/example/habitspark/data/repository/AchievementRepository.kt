package com.example.habitspark.data.repository

import android.annotation.SuppressLint
import com.example.habitspark.data.models.AchievementModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AchievementRepository {
    @SuppressLint("StaticFieldLeak")
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val achievementCollection = db.collection("achievements")

    private var cachedAchievements: List<AchievementModel>? = null


    suspend fun fetchAchievements(forceRefresh: Boolean = false): List<AchievementModel> {
        if (cachedAchievements != null && !forceRefresh) {
            return cachedAchievements!!
        }

        val results = achievementCollection
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(AchievementModel::class.java)?.copy(id = doc.id)
            }
        cachedAchievements = results
        return results
    }
}