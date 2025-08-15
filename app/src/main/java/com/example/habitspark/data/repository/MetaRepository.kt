package com.example.habitspark.data.repository

import com.example.habitspark.data.models.metaUserGroupsModel
import com.example.habitspark.domain.featureGate.UserGroup
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class MetaRepository(db: FirebaseFirestore) {
    private val metaCollection = db.collection("meta")

    //Bad design but gets the job done
    suspend fun assignUserGroup(): UserGroup {
        return try {
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val metaRef = metaCollection.document("ab_userGroups")
                val snapshot = transaction.get(metaRef)
                val currentAssignments = snapshot.getLong(metaUserGroupsModel::totalAssgined.name) ?: 0L

                val updates = mutableMapOf<String, Any>(
                    metaUserGroupsModel::totalAssgined.name to FieldValue.increment(1),
                )
                // Determine the group to assign
                val assignedGroup = if (currentAssignments % 2L == 0L) {
                    updates[metaUserGroupsModel::A_ALL_Count.name] = FieldValue.increment(1)
                    UserGroup.A_ALL
                } else {
                    updates[metaUserGroupsModel::B_GATED_count.name] = FieldValue.increment(1)
                    UserGroup.B_GATED
                }

                transaction.update(metaRef, updates)

                assignedGroup
            }.await()
        } catch (e: Exception) {
            UserGroup.A_ALL
        }
    }

}