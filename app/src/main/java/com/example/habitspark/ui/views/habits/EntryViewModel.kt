package com.example.habitspark.ui.views.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitspark.data.dataTypes.ActionOperation
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.domain.stats.onEntryAction
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

    private val _entriesListener = MutableStateFlow<List<EntryModel>>(emptyList())
    val entriesListener: StateFlow<List<EntryModel>> = _entriesListener

    private val _habitListener = MutableStateFlow<HabitModel?>(null)
    val habit: StateFlow<HabitModel?> = _habitListener


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun startEntriesForHabitListener(habitId: String) {
        viewModelScope.launch {
            entryRepository.listenEntriesForHabit(habitId).collect { entries ->
                _entriesListener.value = entries
            }
        }
    }

    fun startHabitListener(habitId: String) {
        viewModelScope.launch {
            habitRepository.listenHabitById(habitId).collect { habit ->
                _habitListener.value = habit
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addEntry(entry: EntryModel) {
        try {
            entryRepository.addEntry(entry).await()
            onEntryAction(
                entry,
                ActionOperation.ADD
            )
        } catch (e: Exception) {
            _error.value = e.message
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteEntry(entry: EntryModel) {
        viewModelScope.launch {
            try {
                entryRepository.deleteEntry(entry.id).await()
                onEntryAction(
                    entry,
                    ActionOperation.DELETE
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun fetchEntriesByUserId(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val results = entryRepository.getEntriesByUserId(userId).await()
                _entries.clear()
                _entries.addAll(results)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}