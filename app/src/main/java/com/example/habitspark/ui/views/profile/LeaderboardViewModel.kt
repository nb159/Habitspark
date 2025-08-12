package com.example.habitspark.ui.views.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitspark.data.dataTypes.HighlightStyle
import com.example.habitspark.data.dataTypes.LeaderboardRow
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel (
    private val habitRepository: HabitRepository = HabitRepository(Firebase.firestore),
    private val userRepository: UserRepository = UserRepository(Firebase.firestore),
    private  val entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
) : ViewModel() {

    private val _timeSpentRows = MutableStateFlow<List<LeaderboardRow>>(emptyList())
    val timeSpentRows: StateFlow<List<LeaderboardRow>> = _timeSpentRows

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing = _isPurchasing.asStateFlow()

    private val _purchaseMessage = MutableStateFlow<String?>(null)
    val purchaseMessage = _purchaseMessage.asStateFlow()
    private var hasStarted = false


    fun startAllTimeLeaderboard(limit: Long = 10) {
        if (hasStarted) return
        hasStarted = true
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
                            rank   = idx + 1,
                            highlightStyle = user.highlightStyle,
                            highlightExpiresAtMs = user.highlightExpiresAt?.toDate()?.time
                        )
                    }
                }
        }
    }

    fun purchaseHighlight(
        userId: String,
        style: HighlightStyle,
    ) {
        if (_isPurchasing.value) return
        _isPurchasing.value = true
        _purchaseMessage.value = null

        userRepository.purchaseHighlight(userId, style)
            .addOnSuccessListener { result ->
                _purchaseMessage.value = "✨ Highlight activated! (${style.label})"
            }
            .addOnFailureListener { e ->
                _purchaseMessage.value = "❌ ${e.message ?: "Purchase failed"}"
            }
            .addOnCompleteListener {
                _isPurchasing.value = false
            }
    }

    fun clearPurchaseMessage() {
        _purchaseMessage.value = null
    }

}