package com.example.habitspark.ui.views.habits

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.domain.stats.StatsManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HabitViewModel(
    private val habitRepository: HabitRepository = HabitRepository(Firebase.firestore),
    private val entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
    ) : ViewModel() {

    private val _habits = mutableStateListOf<HabitModel>()
    val habits: SnapshotStateList<HabitModel> = _habits

    private val statsManager = StatsManager()

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchHabits(userId: String) {
        viewModelScope.launch {
            try {
                val result = habitRepository.getUserHabits(userId).await()

                _habits.clear()
                _habits.addAll(result)
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error fetching habits: ${e.message}")
                _error.value = e.message
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addHabit(habit: HabitModel) {
        viewModelScope.launch {
            try {
                val docRef = habitRepository.addHabit(habit).await()
                statsManager.updateStatsFromEntry(habitId = docRef.id, userIdOverride = habit.userId)
                updateStates()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteHabit(habit: HabitModel) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habit.id).await()
                entryRepository.deleteEntriesByHabitId(habit.id).await()
                statsManager.updateStatsFromEntry(habitId = habit.id, userIdOverride = habit.userId)
                updateStates()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateHabit(habit: HabitModel) {
        viewModelScope.launch {
            try {
                habitRepository.updateHabit(habit)
                statsManager.updateStatsFromEntry(habitId = habit.id, userIdOverride = habit.userId)
                updateStates()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun updateStates() {
        fetchHabits(_habits.firstOrNull()?.userId ?: return)
    }
}
