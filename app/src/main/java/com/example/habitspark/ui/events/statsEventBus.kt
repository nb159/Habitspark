package com.example.habitspark.ui.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class StatsEvent {
    data class AchievementUnlocked(val message: String) : StatsEvent()
    data object UserDataChanged : StatsEvent()
}
object StatsEventBus {
    private val _events = MutableSharedFlow<StatsEvent>()
    val events: SharedFlow<StatsEvent> = _events

    suspend fun emit(event: StatsEvent) {
        _events.emit(event)
    }
}