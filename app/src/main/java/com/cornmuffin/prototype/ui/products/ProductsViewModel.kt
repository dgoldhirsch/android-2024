package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.Navigator
import com.cornmuffin.prototype.data.products.ProductsRepository
import com.cornmuffin.prototype.data.products.ProductsResponse
import com.cornmuffin.prototype.data.settings.SettingsRepository
import com.cornmuffin.prototype.ui.common.CannotGoBack
import com.cornmuffin.prototype.util.statemachine.StateMachine
import com.cornmuffin.prototype.util.statemachine.StateMachineAction
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

    sealed interface Action : StateMachineAction {
        data object GetSettings : Action {
            override fun isImmediate(): Boolean = true
        }

        data object Load : Action
        data class NavigateTo(val navTarget: Navigator.NavTarget) : Action
        data class ProcessLoadResponse(val productsResponse: ProductsResponse) : Action
        data object Refresh : Action
        data class ProcessRefreshResponse(val productsResponse: ProductsResponse) : Action
        data object Retry : Action
    }

    private val stateMachine = StateMachine(
        scope = viewModelScope,
        control = { stateMachine: StateMachine, action: StateMachineAction ->
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
                                stateMachine.enqueue(Action.ProcessLoadResponse(getProducts()))
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
    )

    init {
        stateMachine.enqueue(Action.GetSettings, Action.Load)
    }

    /**
     * Public interface so that Layout can prod our state with an event.
     */
    fun enqueue(vararg actions: Action) {
        stateMachine.enqueue(*actions)
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
            stateMachine.enqueue(Action.ProcessRefreshResponse(getProducts()))
        }
    }
}
