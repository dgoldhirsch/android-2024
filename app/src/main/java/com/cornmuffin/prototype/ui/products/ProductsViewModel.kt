package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.data.products.ProductsRepository
import com.cornmuffin.prototype.data.products.ProductsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    sealed interface PsmAction {
        data object Load : PsmAction
        data class ProcessLoadResponse(val productsResponse: ProductsResponse) : PsmAction
        data object Refresh : PsmAction
        data class ProcessRefreshResponse(val productsResponse: ProductsResponse) : PsmAction
        data object Retry : PsmAction
    }

    private val productsPsmStateMachine = ProductsStateMachine { event, state ->
        when (state) {
            PsmState.Uninitialized, // Default state when state machine is constructed
            PsmState.Error -> when (event) {
                is PsmAction.Load,
                is PsmAction.Retry -> {
                    intent {
                        postSideEffect(ProductsSideEffect.FetchForLoad)
                        reduce { this.state.asLoading() }
                    }
                    PsmState.Loading
                }
                else -> state
            }

            PsmState.Loading -> when (event) {
                is PsmAction.ProcessLoadResponse -> {
                    when (event.productsResponse) {
                        is ProductsResponse.Error -> {
                            intent {
                                reduce { this.state.asError(event.productsResponse.exception.message ?: "Bummer") }
                            }
                            PsmState.Error
                        }

                        is ProductsResponse.Success -> {
                            intent {
                                reduce { this.state.asSuccess(event.productsResponse.data) }
                            }
                            PsmState.Loaded
                        }

                        else -> state
                    }
                }
                else -> state
            }

            PsmState.Loaded -> when (event) {
                is PsmAction.Refresh -> {
                    intent {
                        postSideEffect(ProductsSideEffect.Refresh)
                        reduce { this.state.asRefreshing() }
                    }
                    PsmState.Refreshing
                }
                else -> state
            }

            PsmState.Refreshing -> when (event) {
                is PsmAction.ProcessRefreshResponse -> {
                    when (event.productsResponse) {
                        is ProductsResponse.Error -> {
                            intent {
                                reduce {
                                    this.state.asError(event.productsResponse.exception.message ?: "Bummer")
                                }
                            }
                            PsmState.Error
                        }

                        is ProductsResponse.Success -> {
                            intent {
                                reduce { this.state.asSuccess(event.productsResponse.data) }
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
        listenForSideEffects()
        productsPsmStateMachine.advance(PsmAction.Load)
    }

    internal fun advanceProductsStateMachine(event: PsmAction) {
        productsPsmStateMachine.advance(event)
    }

    private suspend fun fetchFromNetwork() = repository.products
        .flowOn(Dispatchers.IO)
        .catch {
            emit(ProductsResponse.Error(it))
        }

    private fun listenForSideEffects() {
        viewModelScope.launch {
            container.sideEffectFlow.collect { productsSideEffect ->
                when (productsSideEffect) {
                    ProductsSideEffect.FetchForLoad -> viewModelScope.launch {
                        fetchFromNetwork().collect {
                            advanceProductsStateMachine(PsmAction.ProcessLoadResponse(it))
                        }
                    }

                    ProductsSideEffect.Refresh -> viewModelScope.launch {
                        fetchFromNetwork().collect {
                            advanceProductsStateMachine(PsmAction.ProcessRefreshResponse(it))
                        }
                    }
                }
            }
        }
    }
}
