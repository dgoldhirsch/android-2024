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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository,
    private val settingsRepository: SettingsRepository,
    private val navigator: Navigator,
) : CannotGoBack, ViewModel() {

    // For now we need the up-to-date settings in the layout.
    // Maybe it would be better to move them from here into the
    // container state?  Or, better, decouple them entirely from this
    // view model, because they exist in a singleton repository.
    internal var settings: Settings = Settings()
    private val container = ProductsContainer(viewModelScope)

    // Action inputs to the state machine, not to be confused with UI user actions.
    sealed interface Action {
        data object Load : Action
        data class NavigateTo(val navTarget: Navigator.NavTarget) : Action
        data class ProcessLoadResponse(val productsResponse: ProductsResponse) : Action
        data object Refresh : Action
        data class ProcessRefreshResponse(val productsResponse: ProductsResponse) : Action
        data object Retry : Action
    }

    // Controls the view model based on current Orbit container state and a given action (event).
    private val stateMachine: (Action) -> Unit = { action ->
        when (container.stateFlow.value.state) {
            ProductsViewModelState.State.UNINITIALIZED, // Default state when state machine is constructed
            ProductsViewModelState.State.ERROR -> when (action) {
                is Action.Load,
                is Action.Retry -> {
                    container.intent {
                        postSideEffect(ProductsSideEffect.GetSettings)
                        postSideEffect(ProductsSideEffect.FetchForLoad)
                        reduce { this.state.asLoading() }
                    }
                }

                else -> {}
            }

            ProductsViewModelState.State.LOADING -> when (action) {
                is Action.ProcessLoadResponse -> {
                    when (action.productsResponse) {
                        is ProductsResponse.Error -> {
                            container.intent {
                                reduce { this.state.asError(action.productsResponse.exception.message ?: "Bummer") }
                            }
                        }

                        is ProductsResponse.Success -> {
                            container.intent {
                                reduce { this.state.asSuccess(action.productsResponse.data) }
                            }
                        }

                        else -> {}
                    }
                }

                else -> {}
            }

            ProductsViewModelState.State.SUCCESSFUL -> when (action) {
                is Action.Refresh -> {
                    container.intent {
                        postSideEffect(ProductsSideEffect.Refresh)
                        reduce { this.state.asRefreshing() }
                    }
                }

                is Action.NavigateTo -> {
                    container.intent {
                        navigator.navigateTo(action.navTarget)
                    }
                }

                else -> {}
            }

            ProductsViewModelState.State.REFRESHING -> when (action) {
                is Action.ProcessRefreshResponse -> {
                    when (action.productsResponse) {
                        is ProductsResponse.Error -> {
                            container.intent {
                                reduce {
                                    this.state.asError(action.productsResponse.exception.message ?: "Bummer")
                                }
                            }
                        }

                        is ProductsResponse.Success -> {
                            container.intent {
                                reduce { this.state.asSuccess(action.productsResponse.data) }
                            }
                        }

                        else -> {}
                    }
                }

                else -> {}
            }
        }
    }

    init {
        listenForSideEffects()
        reduceViewModel(Action.Load)
    }

    /**
     * Allow UI and other callers to advance our state based on an action.
     */
    internal fun reduceViewModel(action: Action) {
        stateMachine(action)
    }

    /**
     * Allow UI and other callers to listen to our state flow without having to understand
     * that our state is managed by an Orbit container.
     */
    internal fun stateFlow() = container.stateFlow

    private suspend fun fetchFromNetwork() = try {
        withContext(Dispatchers.IO) {
            repository.getProducts()
        }
    } catch (e: Exception) {
        ProductsResponse.Error(e)
    }

    private fun listenForSideEffects() {
        viewModelScope.launch {
            container.sideEffectFlow.collect { productsSideEffect ->
                when (productsSideEffect) {
                    is ProductsSideEffect.FetchForLoad -> viewModelScope.launch(Dispatchers.IO) {
                        reduceViewModel(Action.ProcessLoadResponse(fetchFromNetwork()))
                    }

                    is ProductsSideEffect.GetSettings -> viewModelScope.launch(Dispatchers.IO) {
                        settingsRepository.initialize()
                        settingsRepository.settings.collect { settings = it }
                    }

                    is ProductsSideEffect.Refresh -> viewModelScope.launch(Dispatchers.IO) {
                        reduceViewModel(Action.ProcessRefreshResponse(fetchFromNetwork()))
                    }
                }
            }
        }
    }
}
