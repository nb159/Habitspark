package com.example.habitspark.ui.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object StatsEventBus {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val events: SharedFlow<String> = _events

    suspend fun emit(message: String) {
        _events.emit(message)
    }
}