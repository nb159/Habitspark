package com.example.habitspark.data.repository

import com.example.habitspark.data.models.metaUserGroupsModel
import com.example.habitspark.domain.featureGate.UserGroup
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
                val currentAssignments = snapshot.toObject(metaUserGroupsModel::class.java) ?: metaUserGroupsModel()

                // Determine the group to assign
                val assignedGroup = if (currentAssignments.totalAssgined % 2 == 0) {
                    UserGroup.A_ALL
                } else {
                    UserGroup.B_GATED
                }

                // Update the counts and total assigned
                val updatedAssignments = currentAssignments.copy(
                    totalAssgined = currentAssignments.totalAssgined + 1,
                    A_ALL_Count = if (assignedGroup == UserGroup.A_ALL) currentAssignments.A_ALL_Count + 1 else currentAssignments.A_ALL_Count,
                    B_GATED_count = if (assignedGroup == UserGroup.B_GATED) currentAssignments.B_GATED_count + 1 else currentAssignments.B_GATED_count
                )

                transaction.set(metaRef, updatedAssignments)

                assignedGroup
            }.await()
        } catch (e: Exception) {
            UserGroup.A_ALL
        }
    }

    suspend fun updateUserGroupAssignments(assignments: metaUserGroupsModel): Boolean {
        return try {
            metaCollection.document("ab_userGroups")
                .set(assignments)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}