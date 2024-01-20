package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.products.ProductsResponse
import com.cornmuffin.prototype.data.products.ProductsRepository
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

    init {
        initUiStateAndUpdateCache()
    }

    fun initUiStateAndUpdateCache() {
        viewModelScope.launch {
            when {
                // Drive UI and prime cache from network call
                cachedProducts.isEmpty() -> {
                    println(">>>>> CACHE IS EMPTY: DRIVE UI FROM NETWORK")
                    becomeLoading()
                    fetchFromNetwork { onFinishedLoading(it) }
                }

                // Display whatever is in cache, and update cache from network call
                else -> {
                    println(">>>>> CACHE IS FULL: REDUCE...")
                    becomeSuccessfullyLoaded(cachedProducts)
                    launch {
                        println(">>>>> KICK OFF NETWORK")
                        fetchFromNetwork { onFinishedUpdating(it) }
                        // Don't reduce with updated data.
                        // If/when user refreshes the UI, they'll get the updated data from the cache
                    }
                }
            }
        }
    }

    private fun becomeLoading() {
        _uiState.update { uiState.value.asLoading() }
    }

    private fun becomeSuccessfullyLoaded(products: ImmutableList<Product>) {
        updateCacheUsing(products)
        _uiState.update { uiState.value.asSuccess(products) }
    }

    private suspend fun fetchFromNetwork(onResponse: (ProductsResponse) -> Unit) {
        repository.products
            .flowOn(Dispatchers.IO)
            .catch {
                emit(ProductsResponse.Error(it))
            }
            .collect(onResponse)
    }

    private fun onFinishedLoading(productsResponse: ProductsResponse) {
        when (productsResponse) {
            is ProductsResponse.Error -> _uiState.update {
                uiState.value.asError(
                    retryNumber = uiState.value.retryNumber + 1,
                    errorMessage = productsResponse.exception.message ?: "Bummer",
                )
            }

            is ProductsResponse.Loading -> becomeLoading()
            is ProductsResponse.Success -> becomeSuccessfullyLoaded(productsResponse.data)
        }
    }

    private fun onFinishedUpdating(productsResponse: ProductsResponse) {
        when (productsResponse) {
            // TODO Error during update:  retry a few times, and/or toast a message that the network can't be reached
            // and that the user should please try again later.
            is ProductsResponse.Error -> println(">>>>> UPDATE Error")

            // TODO Loading during update: ignore, but maybe only up to some reasonable timeout period
            is ProductsResponse.Loading -> println(">>>>> UPDATE Loading")
            is ProductsResponse.Success -> updateCacheUsing(productsResponse.data)
            // Note that we do not update the UI state.  We've updated the cache, and the user
            // will see that new data when/if they refresh the screen.
        }
    }

    private fun updateCacheUsing(products: ImmutableList<Product>) {
        println(">>>>> UPDATING CACHE WITH $products")
        _cache.clear()
        _cache.addAll(products)
    }
}
