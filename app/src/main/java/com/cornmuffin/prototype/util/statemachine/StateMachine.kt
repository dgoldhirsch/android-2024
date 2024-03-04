package com.cornmuffin.prototype.util.statemachine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StateMachine(
    val control: (StateMachine, StateMachineAction) -> Unit,
    private val eventQueue: EventQueue = EventQueue(),
    val scope: CoroutineScope,
) {
    /**
     * Enqueue an action.  If the event queue is empty, we can assume that the
     * event loop has stopped, and restart it.
     */
    @Synchronized
    internal fun enqueue(vararg actions: StateMachineAction) {
        if (eventQueue.isNotEmpty()) {
            eventQueue.addAll(*actions)
        } else {
            eventQueue.addAll(*actions)
            prod()
        }
    }

    private fun prod() {
        scope.launch { eventLoop() }
    }

    private suspend fun eventLoop() {
        eventQueue.debug()

        withContext(Dispatchers.Default) {
            var action = eventQueue.popNext()
            while (action != null) {
                control(this@StateMachine, action)
                action = eventQueue.popNext()
                delay(1) // good karma to yield momentarily
            }
        }
    }
}
