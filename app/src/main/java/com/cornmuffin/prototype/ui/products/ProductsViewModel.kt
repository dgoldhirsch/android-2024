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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    enum class State {
        Uninitialized,
        Error,
        Loaded,
        Loading,
        Refreshing,
    }

    sealed interface Action {
        data object Load : Action
        data class LoadFinished(val productsResponse: ProductsResponse) : Action
        data object Refresh : Action
        data class RefreshFinished(val productsResponse: ProductsResponse) : Action
        data object Retry : Action
    }

    private val productsStateMachine = ProductsStateMachine { action, state ->
        when (state) {
            State.Uninitialized,
            State.Error -> when (action) {
                is Action.Load,
                is Action.Retry -> {
                    becomeLoading()

                    viewModelScope.launch {
                        fetchFromNetwork().collect {
                            reduce(Action.LoadFinished(it))
                        }
                    }

                    State.Loading
                }

                else -> state
            }

            State.Loading -> when (action) {
                is Action.LoadFinished -> {
                    when (action.productsResponse) {
                        is ProductsResponse.Error -> {
                            becomeError(action.productsResponse.exception.message ?: "Bummer")
                            State.Error
                        }

                        is ProductsResponse.Success -> {
                            becomeSuccessfullyLoaded(action.productsResponse.data)
                            State.Loaded
                        }

                        else -> state
                    }
                }

                else -> state
            }

            State.Loaded -> when (action) {
                is Action.Refresh -> {
                    becomeRefreshing()

                    viewModelScope.launch {
                        fetchFromNetwork().collect {
                            reduce(Action.RefreshFinished(it))
                        }
                    }

                    State.Refreshing
                }

                else -> state
            }

            State.Refreshing -> when (action) {
                is Action.RefreshFinished -> {
                    when (action.productsResponse) {
                        is ProductsResponse.Error -> {
                            becomeError(action.productsResponse.exception.message ?: "Bummer")
                            State.Error
                        }

                        is ProductsResponse.Success -> {
                            becomeSuccessfullyLoaded(action.productsResponse.data)
                            State.Loaded
                        }

                        else -> state
                    }
                }

                else -> state
            }
        }
    }

    init {
        productsStateMachine.reduce(Action.Load)
    }

    /**
     * Allow UI layer to change the state, e.g., as when the user clicks on a 'Retry' button
     * or does a pull-to-refresh swipe.  So far, there is no reason why this should be used
     * from the repository or data source classes, although it ought to work fine.  Instead,
     * the view model is collecting from repository flows.
     */
    internal fun reduce(action: Action) {
        productsStateMachine.reduce(action)
    }

    private fun becomeError(message: String) {
        _uiState.update {
            it.asError(
                retryNumber = it.retryNumber + 1,
                errorMessage = message,
            )
        }
    }

    private fun becomeLoading() {
        _uiState.update { it.asLoading() }
    }

    private fun becomeSuccessfullyLoaded(products: ImmutableList<Product>) {
        _uiState.update { it.asSuccess(products) }
    }

    private fun becomeRefreshing() {
        _uiState.update { it.asRefreshing() }
    }

    private suspend fun fetchFromNetwork() = repository.products
        .flowOn(Dispatchers.IO)
        .catch {
            emit(ProductsResponse.Error(it))
        }
}
