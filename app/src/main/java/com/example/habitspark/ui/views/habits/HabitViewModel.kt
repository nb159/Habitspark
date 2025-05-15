package com.example.habitspark.ui.views.habits

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.repository.HabitRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HabitViewModel() : ViewModel() {

    private val _habits = mutableStateListOf<HabitModel>()
    val habits: SnapshotStateList<HabitModel> = _habits

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchHabits(userId: String) {
        viewModelScope.launch {
            try {
                val result = HabitRepository.getUserHabits(userId).await()
                val list = result.documents.mapNotNull {
                    it.toObject(HabitModel::class.java)?.copy(id = it.id)
                }
                _habits.clear()
                _habits.addAll(list)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addHabit(habit: HabitModel) {
        viewModelScope.launch {
            try {
                HabitRepository.addHabit(habit)
                _habits.add(habit.copy(id = habit.id))
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                HabitRepository.deleteHabit(habitId)
                _habits.removeIf { it.id == habitId }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateHabit(updatedHabit: HabitModel) {
        viewModelScope.launch {
            try {
                HabitRepository.updateHabit(updatedHabit)
                val index = _habits.indexOfFirst { it.id == updatedHabit.id }
                if (index != -1) _habits[index] = updatedHabit
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
