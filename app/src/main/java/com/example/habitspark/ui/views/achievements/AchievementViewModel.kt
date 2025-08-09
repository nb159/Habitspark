package com.example.habitspark.ui.views.achievements

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.habitspark.data.models.AchievementModel
import com.example.habitspark.data.repository.AchievementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AchievementViewModel(
): ViewModel() {

    private val _achievements = mutableStateListOf<AchievementModel>()
    val achievements: SnapshotStateList<AchievementModel> = _achievements

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    suspend fun fetchAchievements() {
        _isLoading.value = true
        try {
            val results = AchievementRepository.fetchAchievements()
            _achievements.clear()
            _achievements.addAll(results)
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }
}