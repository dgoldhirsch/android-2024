package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.products.ProductsRepository
import com.cornmuffin.prototype.data.products.ProductsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
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
    private val _cache: MutableSet<Product> = mutableSetOf()
    private val cachedProducts: ImmutableList<Product>
        get() = _cache.toPersistentList()

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    enum class State {
        Uninitialized,
        Error,
        Loading,
        Loaded,
        Refreshing,
    }

    sealed interface Action {
        data object Load : Action
        data class LoadFinished(val productsResponse: ProductsResponse): Action
        data object Refresh : Action
        data class RefreshFinished(val productsResponse: ProductsResponse): Action
    }

    private val productsStateMachine = ProductsStateMachine { action, state ->
        when (state) {
            State.Uninitialized -> when (action) {
                is Action.Load -> {
                    becomeLoading()

                    viewModelScope.launch {
                        fetchFromNetwork {
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
                    becomeRefreshing(cachedProducts) // TODO better modify existing composition without re-rendering products

                    viewModelScope.launch {
                        fetchFromNetwork {
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
            // No-op
            else -> state
        }
    }

    init {
        productsStateMachine.reduce(Action.Load)
    }

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
        updateCacheUsing(products)
    }

    private fun becomeRefreshing(products: ImmutableList<Product>) {
        _uiState.update { it.asRefreshing() }
    }

    private suspend fun fetchFromNetwork(onResponse: (ProductsResponse) -> Unit) {
        repository.products
            .flowOn(Dispatchers.IO)
            .catch {
                emit(ProductsResponse.Error(it))
            }
            .collect(onResponse)
    }

    private fun updateCacheUsing(products: ImmutableList<Product>) {
        println(">>>>> UPDATING CACHE WITH $products")
        _cache.clear()
        _cache.addAll(products)
    }
}
