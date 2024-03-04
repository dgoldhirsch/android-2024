package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.Navigator
import com.cornmuffin.prototype.data.products.ProductsRepository
import com.cornmuffin.prototype.data.products.ProductsResponse
import com.cornmuffin.prototype.data.settings.SettingsRepository
import com.cornmuffin.prototype.ui.common.CannotGoBack
import com.cornmuffin.prototype.util.statemachine.StateMachine
import com.cornmuffin.prototype.util.statemachine.StateMachineEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository,
    val settingsRepository: SettingsRepository,
    private val navigator: Navigator,
) : CannotGoBack, ViewModel() {
    private val _stateFlow = MutableStateFlow(ProductsViewModelState())
    internal fun stateFlow(): StateFlow<ProductsViewModelState> = _stateFlow.asStateFlow()

    sealed interface Event : StateMachineEvent {
        data class NavigateTo(val navTarget: Navigator.NavTarget) : Event
        data object ProductsUninitialized : Event
        data class ReceivedProducts(val productsResponse: ProductsResponse) : Event
        data class ReceivedRefreshedProducts(val productsResponse: ProductsResponse) : Event
        data object RefreshProducts : Event
        data object RetryProducts : Event

        data object SettingsUninitialized : Event {
            override fun isTopPriority(): Boolean = true
        }
    }

    private val stateMachine = StateMachine(
        scope = viewModelScope,
        control = { stateMachine: StateMachine<Event>, event: StateMachineEvent ->
            if (event is Event.SettingsUninitialized) {
                viewModelScope.launch {
                    getSettings()
                }
            } else {
                when (stateFlow().value.state) {
                    ProductsViewModelState.State.UNINITIALIZED,
                    ProductsViewModelState.State.ERROR -> when (event) {
                        is Event.ProductsUninitialized,
                        is Event.RetryProducts -> {
                            _stateFlow.value = _stateFlow.value.asLoading()

                            viewModelScope.launch {
                                stateMachine.enqueue(Event.ReceivedProducts(getProducts()))
                            }
                        }

                        else -> {}
                    }

                    ProductsViewModelState.State.LOADING -> when (event) {
                        is Event.ReceivedProducts -> {
                            when (event.productsResponse) {
                                is ProductsResponse.Error -> {
                                    _stateFlow.value = _stateFlow.value.asError(event.productsResponse.exception.message ?: "Bummer")
                                }

                                is ProductsResponse.Success -> {
                                    _stateFlow.value = _stateFlow.value.asSuccess(event.productsResponse.data)
                                }

                                else -> {}
                            }
                        }

                        else -> {}
                    }

                    ProductsViewModelState.State.SUCCESSFUL -> when (event) {
                        is Event.RefreshProducts -> {
                            _stateFlow.value = _stateFlow.value.asRefreshing()

                            viewModelScope.launch {
                                refresh()
                            }
                        }

                        is Event.NavigateTo -> {
                            navigator.navigateTo(event.navTarget)
                        }

                        else -> {}
                    }

                    ProductsViewModelState.State.REFRESHING -> when (event) {
                        is Event.ReceivedRefreshedProducts -> {
                            when (event.productsResponse) {
                                is ProductsResponse.Error -> {
                                    _stateFlow.value = _stateFlow.value.asError(event.productsResponse.exception.message ?: "Bummer")
                                }

                                is ProductsResponse.Success -> {
                                    _stateFlow.value = _stateFlow.value.asSuccess(event.productsResponse.data)
                                }

                                else -> {}
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    )

    init {
        stateMachine.enqueue(Event.SettingsUninitialized, Event.ProductsUninitialized)
    }

    /**
     * Public interface so that Layout can prod our state with an event.
     */
    fun enqueue(vararg events: Event) {
        stateMachine.enqueue(*events)
    }

    private suspend fun getProducts() = try {
        withContext(Dispatchers.IO) { repository.getProducts() }
    } catch (e: Exception) {
        ProductsResponse.Error(e)
    }

    private suspend fun getSettings() {
        withContext(Dispatchers.IO) { settingsRepository.initialize() }
    }

    private suspend fun refresh() {
        withContext(Dispatchers.IO) {
            repository.flushCache()
            stateMachine.enqueue(Event.ReceivedRefreshedProducts(getProducts()))
        }
    }
}
