package com.cornmuffin.prototype.util.eventprocessor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EventQueue<E : EventQueue.Item>(
    private val queue: ArrayDeque<E> = ArrayDeque()
) {
    private val _flow = MutableStateFlow<E?>(null)
    val flow = _flow.asStateFlow()

    fun add(event: E) {
        addToQueue(event)
        _flow.tryEmit(queue.removeFirstOrNull())
    }

    @Synchronized
    @Suppress("Unused")
    fun debug() {
        queue.forEachIndexed { index, event ->
            println("=> [$index] $event")
        }
    }

    interface Item {
        fun isTopPriority() = false
    }

    private fun addToQueue(event: E) {
        val existingIndex = queue.indexOfFirst { it::class == event::class }

        if (existingIndex >= 0) {
            // Replace existing event of this type with newer one
            println("=> REPLACE $event")
            queue[existingIndex] = event
        } else if (event.isTopPriority()) {
            // A top-priority event will be the next one to be popped
            queue.addFirst(event)
        } else {
            // Anything else goes to the back of the queue
            queue.addLast(event)
        }
    }
}
