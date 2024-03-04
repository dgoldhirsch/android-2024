package com.cornmuffin.prototype.util.statemachine

class EventQueue {
    private val queue: ArrayDeque<StateMachineAction> = ArrayDeque()

    @Synchronized
    fun add(action: StateMachineAction) {
        val existingActionIndex = queue.indexOfFirst { it::class == action::class }

        if (existingActionIndex >= 0) {
            // Replace existing action of this type with newer version
            queue[existingActionIndex] = action
        } else if (action.isImmediate()) {
            // An immediate action will be the next one to pop
            queue.addFirst(action)
        } else {
            // Anything else goes to the back of the queue
            queue.addLast(action)
        }
    }

    fun addAll(vararg actions: StateMachineAction) {
        actions.forEach { add(it) }
    }

    fun debug() {
        queue.forEachIndexed { index, action ->
            println(">>>>> [$index] $action")
        }
    }

    fun isNotEmpty() = queue.isNotEmpty()

    @Synchronized
    fun popNext(): StateMachineAction? = queue.removeLastOrNull()
}
