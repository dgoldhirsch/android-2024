package com.cornmuffin.prototype.util.eventprocessor

class EventQueue<E : EventQueue.Item> {
    private val queue: ArrayDeque<E> = ArrayDeque()

    @Synchronized
    fun add(event: E) {
        val existingIndex = queue.indexOfFirst { it::class == event::class }

        if (existingIndex >= 0) {
            // Replace existing event of this type with newer one
            queue[existingIndex] = event
        } else if (event.isTopPriority()) {
            // A top-priority event will be the next one to be popped
            queue.addFirst(event)
        } else {
            // Anything else goes to the back of the queue
            queue.addLast(event)
        }
    }

    fun addAll(vararg events: E) {
        events.forEach { add(it) }
    }

    fun debug() {
        queue.forEachIndexed { index, event ->
            println(">>>>> [$index] $event")
        }
    }

    fun isNotEmpty() = queue.isNotEmpty()

    @Synchronized
    fun popNext(): E? = queue.removeLastOrNull()
    
    interface Item {
        fun isTopPriority() = false
    }
}
