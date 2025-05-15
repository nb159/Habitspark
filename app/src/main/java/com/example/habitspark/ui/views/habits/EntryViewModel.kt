package com.example.habitspark.ui.views.habits

import androidx.lifecycle.ViewModel
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.repository.EntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EntryViewModel : ViewModel() {

    private val _entries = MutableStateFlow<List<EntryModel>>(emptyList())
    val entries: StateFlow<List<EntryModel>> = _entries

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchEntriesForHabit(habitId: String) {
        _isLoading.value = true
        EntryRepository.getEntriesForHabit(habitId)
            .addOnSuccessListener { result ->
                _entries.value = result
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _error.value = exception.localizedMessage
                _isLoading.value = false
            }
    }

    fun addEntry(entry: EntryModel) {
        EntryRepository.addEntry(entry)
            .addOnSuccessListener {
                fetchEntriesForHabit(entry.habitId)
            }
            .addOnFailureListener {
                _error.value = it.localizedMessage
            }
    }

    fun deleteEntry(entryId: String, habitId: String) {
        EntryRepository.deleteEntry(entryId)
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