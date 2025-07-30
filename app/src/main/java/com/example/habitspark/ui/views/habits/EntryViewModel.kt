package com.example.habitspark.ui.views.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.domain.stats.StatsManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EntryViewModel(
    private val entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
    private val habitRepository: HabitRepository = HabitRepository(Firebase.firestore),
) : ViewModel() {

    private val _entries = mutableStateListOf<EntryModel>()
    val entries: SnapshotStateList<EntryModel> = _entries

    private val _habit = mutableStateOf<HabitModel?>(null)
    val habit: State<HabitModel?> = _habit

    private val statsManager = StatsManager()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchEntriesForHabit(habitId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val results = entryRepository.getEntriesForHabit(habitId)
                _entries.clear()
                _entries.addAll(results)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchHabit(habitId: String) {
        habitRepository.getHabitById(habitId)
            .addOnSuccessListener { result ->
                _habit.value = result
            }
            .addOnFailureListener { exception ->
                _error.value = exception.localizedMessage
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addEntry(entry: EntryModel) {
        try {
            entryRepository.addEntry(entry).await()
            statsManager.updateStatsFromEntry(entry.habitId)
            updateStates()
        } catch (e: Exception) {
            _error.value = e.message
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteEntry(entry: EntryModel) {
        viewModelScope.launch {
            try {
                entryRepository.deleteEntry(entry.id).await()
                statsManager.updateStatsFromEntry(entry.habitId)
                updateStates()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun updateStates(){
        fetchEntriesForHabit(_habit.value?.id ?: "")
        fetchHabit(_habit.value?.id ?: "")
    }
}