package com.cornmuffin.prototype

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test

/**
 * This is a kind of test bed for understanding flows.
 */
class FlowTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun collectingMultipleEmissions() = runTest {
        val myAbcFlow = flow {
            ('A'..'E').forEach {
                delay(25)
                emit(it)
            }
        }

        val my123Flow = flow {
            (1..5).forEach {
                delay(50)
                emit(it)
            }
        }

        flowOf(myAbcFlow, my123Flow).flattenMerge()
            .collect {
                println("==> $it")
            }
    }

    private suspend fun <T> Flow<T>.flowMerge(other: Flow<T>): Flow<T> = flow {
        collect { emit(it) }
        other.collect { emit(it) }
    }

    @Test
    fun simpleFlow() = runTest {
        val flow = flowOf(
            { 1 },
            { 2 },
            { 2 },
            { throw Exception("Crap") },
            { throw Exception("Damn") },
            { 3 },
        ).map {
            it()
        }.distinctUntilChanged()

        flow
            .catch { println("CAUGHT ${it.message}") }
            .onCompletion { println("FINISHED") }
            .collect { println("COLLECTED $it") }
    }

    @Test
    fun stateFlow() = runTest {
        val flow = MutableStateFlow(1)

        launch {
            yield() // Without these
            flow.update { 2 }
            yield() // ... suspending calls...
            flow.update { 2 }
            yield() // ... the state flow's coroutine won't notice...
            flow.update { 3 }
            yield() // ... anything except the last _update_ to 4
            flow.update { 4 }
        }

        // We must launch the collector on the background scope that cancels the coroutine when the test finishes.
        // Without this, the collector of the state flow  will continue running until it times out with this exception:
        //    After waiting for 60000 ms, the test coroutine is not completing, there were active child jobs: ...
        backgroundScope.launch {
            flow
                .collect { println("COLLECTED $it") }
        }
    }
}
