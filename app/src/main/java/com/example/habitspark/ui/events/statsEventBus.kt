package com.example.habitspark.ui.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
interface HasMessage {
    val message: String
}
sealed class StatsEvent {
    data class AchievementUnlocked(override val message: String) : StatsEvent(), HasMessage
    data object UserDataChanged : StatsEvent()
    data class HighlightPurchased(override val message: String) : StatsEvent(), HasMessage
    data class TextCopied(override val message: String) : StatsEvent(), HasMessage
}
object StatsEventBus {
    private val _events = MutableSharedFlow<StatsEvent>()
    val events: SharedFlow<StatsEvent> = _events

    suspend fun emit(event: StatsEvent) {
        _events.emit(event)
    }
}