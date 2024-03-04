package com.cornmuffin.prototype.util.statemachine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StateMachine<E : StateMachineEvent>(
    val control: (StateMachine<E>, E) -> Unit,
    private val eventQueue: EventQueue<E> = EventQueue(),
    val scope: CoroutineScope,
) {
    /**
     * Enqueue an event.  If the event queue is empty, we can assume that the
     * event loop has stopped, and restart it.
     */
    @Synchronized
    internal fun enqueue(vararg events: E) {
        if (eventQueue.isNotEmpty()) {
            eventQueue.addAll(*events)
        } else {
            eventQueue.addAll(*events)
            prod()
        }
    }

    private fun prod() {
        scope.launch { eventLoop() }
    }

    private suspend fun eventLoop() {
        eventQueue.debug()

        withContext(Dispatchers.Default) {
            var event = eventQueue.popNext()
            while (event != null) {
                control(this@StateMachine, event)
                event = eventQueue.popNext()
                delay(1) // good karma to yield momentarily
            }
        }
    }
}
