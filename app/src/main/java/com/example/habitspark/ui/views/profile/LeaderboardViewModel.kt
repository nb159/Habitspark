package com.example.habitspark.ui.views.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitspark.data.dataTypes.LeaderboardRow
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel (
    private val habitRepository: HabitRepository = HabitRepository(Firebase.firestore),
    private val userRepository: UserRepository = UserRepository(Firebase.firestore),
    private  val entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
) : ViewModel() {

    private val _timeSpentRows = MutableStateFlow<List<LeaderboardRow>>(emptyList())
    val timeSpentRows: StateFlow<List<LeaderboardRow>> = _timeSpentRows

    fun startAllTimeLeaderboard(limit: Long = 10) {
        viewModelScope.launch {
            userRepository.listenAllTimeLeaderboard(limit)
                .collect { users ->
                    _timeSpentRows.value = users
                        .filter { it.metrics.totalMinutesSpent > 0 }
                        .mapIndexed { idx, user ->
                        LeaderboardRow(
                            userId = user.id,
                            name   = user.name.ifBlank { "Anonymous" },
                            minutes= user.metrics.totalMinutesSpent,
                            rank   = idx + 1
                        )
                    }
                }
        }
    }

}