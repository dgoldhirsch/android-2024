package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.Navigator
import com.cornmuffin.prototype.data.products.ProductsRepository
import com.cornmuffin.prototype.data.products.ProductsResponse
import com.cornmuffin.prototype.data.settings.Settings
import com.cornmuffin.prototype.data.settings.SettingsRepository
import com.cornmuffin.prototype.ui.common.CannotGoBack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository,
    private val settingsRepository: SettingsRepository,
    private val navigator: Navigator,
) : CannotGoBack, ViewModel() {

    /**
     * State of settings
     */
    internal var settings: Settings = Settings()

    /**
     * Our state
     */
    private val _stateFlow = MutableStateFlow(ProductsViewModelState())
    internal fun stateFlow(): StateFlow<ProductsViewModelState> = _stateFlow.asStateFlow()

    class EventQueue {
        private val queue: ArrayDeque<Action> = ArrayDeque()

        @Synchronized
        fun add(action: Action) {
            val existingActionIndex = queue.indexOfFirst { it::class == action::class }

            if (existingActionIndex >= 0) {
                // Replace existing action of this type with newer version
                queue[existingActionIndex] = action
            } else if (action is Action.GetSettings) {
                // Responding to changed settings is always the top priority
                queue.addFirst(action)
            } else {
                // Anything else goes to the back of the queue
                queue.addLast(action)
            }
        }

        fun addAll(vararg actions: Action) {
            actions.forEach { add(it) }
        }

        fun debug() {
            queue.forEachIndexed { index, action ->
                println(">>>>> [$index] $action")
            }
        }

        fun isNotEmpty() = queue.isNotEmpty()

        @Synchronized
        fun popNext(): Action? = queue.removeLastOrNull()
    }

    private val eventQueue: EventQueue = EventQueue()

    // Action inputs to the state machine, not to be confused with UI user actions.
    sealed interface Action {
        data object GetSettings : Action
        data object Load : Action
        data class NavigateTo(val navTarget: Navigator.NavTarget) : Action
        data class ProcessLoadResponse(val productsResponse: ProductsResponse) : Action
        data object Refresh : Action
        data class ProcessRefreshResponse(val productsResponse: ProductsResponse) : Action
        data object Retry : Action
        data object SettingsHaveChanged : Action
    }

    // Controls the view model based on current Orbit container state and a given action (event).
    private val stateMachine: (Action) -> Unit = { action ->
        if (action is Action.GetSettings) {
            viewModelScope.launch {
                getSettings()
            }
        } else {
            when (stateFlow().value.state) {
                ProductsViewModelState.State.UNINITIALIZED,
                ProductsViewModelState.State.ERROR -> when (action) {
                    is Action.Load,
                    is Action.Retry -> {
                        _stateFlow.value = _stateFlow.value.asLoading()

                        viewModelScope.launch {
                            enqueue(Action.ProcessLoadResponse(getProducts()))
                        }
                    }

                    else -> {}
                }

                ProductsViewModelState.State.LOADING -> when (action) {
                    is Action.ProcessLoadResponse -> {
                        when (action.productsResponse) {
                            is ProductsResponse.Error -> {
                                _stateFlow.value = _stateFlow.value.asError(action.productsResponse.exception.message ?: "Bummer")
                            }

                            is ProductsResponse.Success -> {
                                _stateFlow.value = _stateFlow.value.asSuccess(action.productsResponse.data)
                            }

                            else -> {}
                        }
                    }

                    else -> {}
                }

                ProductsViewModelState.State.SUCCESSFUL -> when (action) {
                    is Action.Refresh -> {
                        _stateFlow.value = _stateFlow.value.asRefreshing()

                        viewModelScope.launch {
                            refresh()
                        }
                    }

                    is Action.NavigateTo -> {
                        navigator.navigateTo(action.navTarget)
                    }

                    else -> {}
                }

                ProductsViewModelState.State.REFRESHING -> when (action) {
                    is Action.ProcessRefreshResponse -> {
                        when (action.productsResponse) {
                            is ProductsResponse.Error -> {
                                _stateFlow.value = _stateFlow.value.asError(action.productsResponse.exception.message ?: "Bummer")
                            }

                            is ProductsResponse.Success -> {
                                _stateFlow.value = _stateFlow.value.asSuccess(action.productsResponse.data)
                            }

                            else -> {}
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    init {
        enqueue(Action.GetSettings, Action.Load)
    }

    /**
     * Enqueue an action.  If the event queue is empty, we can assume that the
     * event loop has stopped, and restart it.
     */
    @Synchronized
    internal fun enqueue(vararg actions: Action) {
        if (eventQueue.isNotEmpty()) {
            eventQueue.addAll(*actions)
        } else {
            eventQueue.addAll(*actions)
            prod()
        }
    }

    private fun prod() {
        viewModelScope.launch {
            eventLoop()
        }
    }
    private suspend fun eventLoop() {
        eventQueue.debug()

        withContext(Dispatchers.Default) {
            var action = eventQueue.popNext()
            while (action != null) {
                stateMachine(action)
                action = eventQueue.popNext()
                delay(1) // good karma to yield momentarily
            }
        }
    }

    private suspend fun getProducts() = try {
        withContext(Dispatchers.IO) {
            repository.getProducts()
        }
    } catch (e: Exception) {
        ProductsResponse.Error(e)
    }

    private suspend fun getSettings() {
        withContext(Dispatchers.IO) {
            settingsRepository.initialize()
            settingsRepository.settings.collect { settings = it }
        }
    }

    private suspend fun refresh() {
        withContext(Dispatchers.IO) {
            repository.flushCache()
            enqueue(Action.ProcessRefreshResponse(getProducts()))
        }
    }
}
