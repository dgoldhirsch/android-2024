package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.products.ProductsRepository
import com.cornmuffin.prototype.data.products.ProductsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository,
) : ViewModel(), ContainerHost<ProductsUiState, ProductsSideEffect> {
    private val _uiState = MutableStateFlow(ProductsUiState())

    /**
     * This drives the entire UI via [ProductsLayout]
     */
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    override val container: Container<ProductsUiState, ProductsSideEffect> = viewModelScope.container(
        ProductsUiState()
    )

    // State of ProductsStateMachine, not to be confused with the Compose State interface,
    // nor the State of the Orbit Container.
    enum class PsmState {
        Uninitialized,
        Error,
        Loaded,
        Loading,
        Refreshing,
    }

    // Events of the ProductsStateMachine, not to be confused with UI events nor user actions.
    sealed interface PsmEvent {
        data object Load : PsmEvent
        data class LoadFinished(val productsResponse: ProductsResponse) : PsmEvent
        data object Refresh : PsmEvent
        data class RefreshFinished(val productsResponse: ProductsResponse) : PsmEvent
        data object Retry : PsmEvent
    }

    private val productsPsmStateMachine = ProductsStateMachine { event, state ->
        when (state) {
            PsmState.Uninitialized, // Default state when state machine is constructed
            PsmState.Error -> when (event) {
                is PsmEvent.Load,
                is PsmEvent.Retry -> {
                    intent {
                        postSideEffect(ProductsSideEffect.FetchForLoad)
                        reduce { uiStateAsLoading() }
                    }
                    PsmState.Loading
                }
                else -> state
            }

            PsmState.Loading -> when (event) {
                is PsmEvent.LoadFinished -> {
                    when (event.productsResponse) {
                        is ProductsResponse.Error -> {
                            intent {
                                reduce { uiStateAsError(event.productsResponse.exception.message ?: "Bummer") }
                            }
                            PsmState.Error
                        }

                        is ProductsResponse.Success -> {
                            intent {
                                reduce { uiStateAsSuccessfullyLoaded(event.productsResponse.data) }
                            }
                            PsmState.Loaded
                        }

                        else -> state
                    }
                }
                else -> state
            }

            PsmState.Loaded -> when (event) {
                is PsmEvent.Refresh -> {
                    intent {
                        postSideEffect(ProductsSideEffect.Refresh)
                        reduce { uiStateAsRefreshing() }
                    }
                    PsmState.Refreshing
                }
                else -> state
            }

            PsmState.Refreshing -> when (event) {
                is PsmEvent.RefreshFinished -> {
                    when (event.productsResponse) {
                        is ProductsResponse.Error -> {
                            intent {
                                reduce { uiStateAsError(event.productsResponse.exception.message ?: "Bummer") }
                            }
                            PsmState.Error
                        }

                        is ProductsResponse.Success -> {
                            intent {
                                reduce { uiStateAsSuccessfullyLoaded(event.productsResponse.data) }
                            }
                            PsmState.Loaded
                        }

                        else -> state
                    }
                }
                else -> state
            }
        }
    }

    init {
        listenToOrbitFlows()
        productsPsmStateMachine.advance(PsmEvent.Load)
    }

    internal fun advanceProductsStateMachine(event: PsmEvent) {
        productsPsmStateMachine.advance(event)
    }

    private suspend fun fetchFromNetwork() = repository.products
        .flowOn(Dispatchers.IO)
        .catch {
            emit(ProductsResponse.Error(it))
        }

    private fun uiStateAsLoading() = uiState.value.asLoading()
    private fun uiStateAsError(message: String) = uiState.value.asError(message = message)
    private fun uiStateAsRefreshing() = uiState.value.asRefreshing()
    private fun uiStateAsSuccessfullyLoaded(products: ImmutableList<Product>) = uiState.value.asSuccess(products = products)

    private fun listenToOrbitFlows() {
        viewModelScope.launch {
            container.stateFlow.collect {
                _uiState.value = it
            }
        }

        viewModelScope.launch {
            container.sideEffectFlow.collect { productsSideEffect ->
                when (productsSideEffect) {
                    ProductsSideEffect.FetchForLoad -> viewModelScope.launch {
                        fetchFromNetwork().collect {
                            advanceProductsStateMachine(PsmEvent.LoadFinished(it))
                        }
                    }

                    ProductsSideEffect.Refresh -> viewModelScope.launch {
                        fetchFromNetwork().collect {
                            advanceProductsStateMachine(PsmEvent.RefreshFinished(it))
                        }
                    }
                }
            }
        }
    }
}
