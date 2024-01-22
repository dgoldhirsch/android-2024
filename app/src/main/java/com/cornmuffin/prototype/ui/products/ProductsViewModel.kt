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

    // Action inputs to the ProductsStateMachine, not to be confused with UI events nor user actions.
    sealed interface PsmAction {
        data object Load : PsmAction
        data class ProcessLoadResponse(val productsResponse: ProductsResponse) : PsmAction
        data object Refresh : PsmAction
        data class ProcessRefreshResponse(val productsResponse: ProductsResponse) : PsmAction
        data object Retry : PsmAction
    }

    private val productsPsmStateMachine = ProductsStateMachine {action ->
        when (container.stateFlow.value.state) {
            ProductsUiState.State.UNINITIALIZED, // Default state when state machine is constructed
            ProductsUiState.State.ERROR -> when (action) {
                is PsmAction.Load,
                is PsmAction.Retry -> {
                    intent {
                        postSideEffect(ProductsSideEffect.FetchForLoad)
                        reduce { this.state.asLoading() }
                    }
                }
                else -> { }
            }

            ProductsUiState.State.LOADING -> when (action) {
                is PsmAction.ProcessLoadResponse -> {
                    when (action.productsResponse) {
                        is ProductsResponse.Error -> {
                            intent {
                                reduce { this.state.asError(action.productsResponse.exception.message ?: "Bummer") }
                            }
                        }

                        is ProductsResponse.Success -> {
                            intent {
                                reduce { this.state.asSuccess(action.productsResponse.data) }
                            }
                        }

                        else -> { }
                    }
                }
                else -> { }
            }

            ProductsUiState.State.SUCCESSFUL -> when (action) {
                is PsmAction.Refresh -> {
                    intent {
                        postSideEffect(ProductsSideEffect.Refresh)
                        reduce { this.state.asRefreshing() }
                    }
                }
                else -> { }
            }

            ProductsUiState.State.REFRESHING -> when (action) {
                is PsmAction.ProcessRefreshResponse -> {
                    when (action.productsResponse) {
                        is ProductsResponse.Error -> {
                            intent {
                                reduce {
                                    this.state.asError(action.productsResponse.exception.message ?: "Bummer")
                                }
                            }
                        }

                        is ProductsResponse.Success -> {
                            intent {
                                reduce { this.state.asSuccess(action.productsResponse.data) }
                            }
                        }

                        else -> { }
                    }
                }
                else -> { }
            }
        }
    }

    init {
        listenForSideEffects()
        advanceProductsStateMachine(PsmAction.Load)
    }

    internal fun advanceProductsStateMachine(action: PsmAction) {
        productsPsmStateMachine.reduce(action)
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
