package com.example.habitspark.ui.views.habits

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchEntriesForHabit(habitId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val results = entryRepository.getEntriesForHabit(habitId).await()
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
        habitRepository.getHabit(habitId)
            .addOnSuccessListener { result ->
                _habit.value = result
            }
            .addOnFailureListener { exception ->
                _error.value = exception.localizedMessage
            }
    }

    fun addEntry(entry: EntryModel) {
        entryRepository.addEntry(entry)
            .addOnSuccessListener {
                fetchEntriesForHabit(entry.habitId)
            }
            .addOnFailureListener {
                _error.value = it.localizedMessage
            }
    }

    fun deleteEntry(entryId: String, habitId: String) {
        entryRepository.deleteEntry(entryId)
            .addOnSuccessListener {
                fetchEntriesForHabit(habitId)
            }
            .addOnFailureListener {
                _error.value = it.localizedMessage
            }
    }

    fun clearError() {
        _error.value = null
    }
}